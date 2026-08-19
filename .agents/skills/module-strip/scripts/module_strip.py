#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
module-strip 业务模块裁剪脚本

在交付目录里删除指定业务模块（删目录 + 改 pom.xml），是 delivery-sync 的下游补充。
零外部依赖，跨平台。

用法：
    python module_strip.py [模块名...] [--list] [--preview] [--verify]
"""

import argparse
import json
import shutil
import subprocess
import sys
from pathlib import Path


def load_delivery_config(source_root: Path) -> dict:
    """读 .delivery-sync.json，确认交付目录已建立"""
    cfg_path = source_root / ".delivery-sync.json"
    if not cfg_path.exists():
        print("[ERROR] 未找到 .delivery-sync.json，请先执行 /sync-delivery --first-time")
        sys.exit(2)
    return json.loads(cfg_path.read_text(encoding="utf-8"))


def resolve_target(source_root: Path, cfg: dict) -> Path:
    target = Path(cfg["deliveryDir"])
    if not target.is_absolute():
        target = (source_root / target).resolve()
    if not target.exists():
        print(f"[ERROR] 交付目录不存在: {target}")
        print("        请先执行 /sync-delivery 生成交付目录")
        sys.exit(2)
    marker = target / ".delivery-sync-marker"
    if not marker.exists():
        print(f"[ERROR] 缺 .delivery-sync-marker: {target}")
        print("        该目录不是受 delivery-sync 管理的交付目录，拒绝执行裁剪")
        sys.exit(2)
    return target


def list_presets(presets_dir: Path):
    print("可用模块预设:")
    print("-" * 60)
    for preset_file in sorted(presets_dir.glob("*.json")):
        if preset_file.stem.startswith("_"):
            continue  # 跳过模板/占位文件
        try:
            data = json.loads(preset_file.read_text(encoding="utf-8"))
            print(f"  {preset_file.stem:<12} {data.get('name', '')}")
            print(f"               {data.get('description', '')}")
        except Exception as e:
            print(f"  {preset_file.stem:<12} [无法解析: {e}]")


def load_preset(presets_dir: Path, module: str) -> dict:
    f = presets_dir / f"{module}.json"
    if not f.exists():
        print(f"[ERROR] 未找到预设: {f}")
        print("        可用预设：")
        list_presets(presets_dir)
        sys.exit(2)
    return json.loads(f.read_text(encoding="utf-8"))


def strip_one(target: Path, preset: dict, dry_run: bool) -> dict:
    """裁剪一个模块，返回统计"""
    stats = {
        "name": preset.get("name", "?"),
        "deleted_dirs": [],
        "deleted_files": [],
        "skipped_dirs": [],
        "pom_lines_removed": 0,
        "pom_files_changed": [],
        "manual_actions": preset.get("manual_actions", []),
    }

    # 删目录
    for rel in preset.get("delete_dirs", []):
        d = target / rel
        if d.exists() and d.is_dir():
            if not dry_run:
                shutil.rmtree(d)
            stats["deleted_dirs"].append(rel)
        else:
            stats["skipped_dirs"].append(rel)

    # 删单文件
    for rel in preset.get("delete_files", []):
        f = target / rel
        if f.exists() and f.is_file():
            if not dry_run:
                f.unlink()
            stats["deleted_files"].append(rel)

    # 处理 pom.xml 行/块删除（智能识别上下文）
    for rule in preset.get("remove_pom_lines", []):
        pom_path = target / rule["file"]
        if not pom_path.exists():
            continue
        patterns = rule.get("patterns", [])
        original = pom_path.read_text(encoding="utf-8")
        new_text, removed = _remove_pom_matches(original, patterns)
        if removed > 0:
            stats["pom_lines_removed"] += removed
            stats["pom_files_changed"].append(rule["file"])
            if not dry_run:
                pom_path.write_text(new_text, encoding="utf-8")

    return stats


def _remove_pom_matches(text: str, patterns: list, max_lookaround: int = 50) -> tuple:
    """
    智能删除 pom.xml 中匹配 pattern 的内容：
    - 若 pattern 命中行位于 <dependency>/<plugin>/<exclusion> 块内 → 删整个块
    - 若 pattern 命中行是独立单行（如 <module>...</module>）→ 删该单行
    - 命中行在 XML 注释 <!-- ... --> 内 → 跳过（避免误删被注释掉的占位）
    返回 (new_text, removed_count)

    实现：用「待删行号集合」方式，避免连续删除多个块时 result 索引错位。
    """
    BLOCK_TAGS = {
        "<dependency>": "</dependency>",
        "<plugin>": "</plugin>",
        "<exclusion>": "</exclusion>",
    }
    lines = text.splitlines(keepends=True)
    n = len(lines)
    to_delete = set()

    def _in_comment(idx: int) -> bool:
        """判断 lines[idx] 是否在 XML 注释 <!-- ... --> 内（含跨行）"""
        line = lines[idx]
        # 简单情形：单行注释 <!-- ... -->
        if "<!--" in line and "-->" in line:
            i_start = line.index("<!--")
            i_end = line.index("-->", i_start)
            # pattern 必须在注释范围内才算
            return True  # 行内有注释，且 pattern 在该行 → 视为可能在注释里（保守跳过）
        # 跨行注释：向前找最近的 <!-- 是否未被 --> 关闭
        for j in range(idx, -1, -1):
            ln = lines[j]
            if "-->" in ln and j < idx:
                return False  # 注释已闭合
            if "<!--" in ln and "-->" not in ln:
                return True  # 注释未闭合，本行在注释内
            if "<!--" in ln and "-->" in ln and j == idx:
                continue  # 同行已处理
        return False

    for i in range(n):
        if i in to_delete:
            continue
        if not any(p in lines[i] for p in patterns):
            continue
        if _in_comment(i):
            continue  # 跳过注释里的占位（如 <!-- <artifactId>xxx</artifactId> -->）

        # 命中 pattern：尝试识别块上下文
        block_open_tag = None
        block_close_tag = None
        block_start = i
        # 向前回溯找最近的 <dependency>/<plugin>/<exclusion> 开始标签
        for j in range(i, max(-1, i - max_lookaround), -1):
            stripped = lines[j].strip()
            for open_tag, close_tag in BLOCK_TAGS.items():
                if stripped == open_tag or stripped.startswith(open_tag):
                    # 确保从 j 到 i 之间没有提前关闭这个块
                    closed_between = False
                    for k in range(j + 1, i):
                        if close_tag in lines[k]:
                            closed_between = True
                            break
                    if not closed_between:
                        block_open_tag = open_tag
                        block_close_tag = close_tag
                        block_start = j
                    break
            if block_open_tag:
                break

        if block_open_tag:
            # 向后找 close 标签
            block_end = -1
            for j in range(i, min(n, i + max_lookaround)):
                if block_close_tag in lines[j]:
                    block_end = j
                    break
            if block_end < 0:
                # 未找到 close，回退到单行删除
                to_delete.add(i)
                continue
            for k in range(block_start, block_end + 1):
                to_delete.add(k)
        else:
            # 单行删除
            to_delete.add(i)

    new_lines = [line for idx, line in enumerate(lines) if idx not in to_delete]
    return "".join(new_lines), len(to_delete)


def verify_compile(target: Path) -> bool:
    """跑 mvn compile 验证编译"""
    print("\n执行 mvn compile 验证（可能需要几分钟）...")
    try:
        result = subprocess.run(
            ["mvn", "-DskipTests", "-q", "compile"],
            cwd=str(target),
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
        )
        if result.returncode == 0:
            print("[OK] mvn compile 通过")
            return True
        print("[FAIL] mvn compile 失败:")
        print(result.stdout[-2000:] if result.stdout else "")
        print(result.stderr[-2000:] if result.stderr else "")
        return False
    except FileNotFoundError:
        print("[WARN] 未找到 mvn 命令，跳过编译验证")
        return False


def report(all_stats: list, dry_run: bool):
    """输出报告"""
    print()
    print("=" * 60)
    print(f"{'[DRY-RUN] ' if dry_run else ''}模块裁剪报告")
    print("=" * 60)

    total_dirs = 0
    total_files = 0
    total_pom = 0
    all_manual = []

    for s in all_stats:
        print(f"\n模块: {s['name']}")
        if s["deleted_dirs"]:
            print(f"  删除目录 ({len(s['deleted_dirs'])}):")
            for d in s["deleted_dirs"]:
                print(f"    - {d}")
            total_dirs += len(s["deleted_dirs"])
        if s["skipped_dirs"]:
            print(f"  跳过（不存在）: {len(s['skipped_dirs'])}")
        if s["deleted_files"]:
            print(f"  删除文件: {len(s['deleted_files'])}")
            total_files += len(s["deleted_files"])
        if s["pom_files_changed"]:
            print(f"  改 pom.xml ({len(s['pom_files_changed'])} 个，共 {s['pom_lines_removed']} 行):")
            for p in s["pom_files_changed"]:
                print(f"    - {p}")
            total_pom += s["pom_lines_removed"]
        all_manual.extend([(s["name"], a) for a in s["manual_actions"]])

    print()
    print("-" * 60)
    print(f"汇总: 删 {total_dirs} 个目录、{total_files} 个文件、改 {total_pom} 行 pom")

    if all_manual:
        print()
        print("⚠️  待手动处理（脚本无法自动完成）:")
        for module, action in all_manual:
            print(f"  [{module}] {action}")


def main():
    parser = argparse.ArgumentParser(description="交付目录业务模块裁剪")
    parser.add_argument("modules", nargs="*", help="要裁剪的模块（覆盖配置）")
    parser.add_argument("--list", action="store_true", help="列出可用预设")
    parser.add_argument("--preview", action="store_true", help="仅预览，不实际执行")
    parser.add_argument("--verify", action="store_true", help="裁剪后跑 mvn compile 验证")
    args = parser.parse_args()

    # 项目根 = 脚本路径上溯 5 层（scripts/module_strip.py → module-strip → skills → .claude → 根）
    source_root = Path(__file__).resolve().parents[4]
    presets_dir = source_root / ".claude" / "skills" / "module-strip" / "presets"

    if args.list:
        list_presets(presets_dir)
        return

    cfg = load_delivery_config(source_root)
    target = resolve_target(source_root, cfg)

    # 决定要裁剪的模块列表
    if args.modules:
        modules = args.modules
    else:
        # 优先用激活预设里的 stripModules
        preset_name = cfg.get("activePreset", "default")
        preset = cfg.get("presets", {}).get(preset_name, {})
        modules = preset.get("stripModules") or cfg.get("stripModules", [])

    if not modules:
        print("[INFO] 未指定要裁剪的模块（命令行 + .delivery-sync.json 都未配置）")
        print("       用法: /strip-modules <模块名...> 或在 .delivery-sync.json 配 stripModules")
        list_presets(presets_dir)
        return

    print(f"目标交付目录: {target}")
    print(f"待裁剪模块: {', '.join(modules)}")
    if args.preview:
        print("[PREVIEW 模式] 仅预览，不实际执行\n")

    all_stats = []
    for module in modules:
        preset = load_preset(presets_dir, module)
        # 处理 depends_on
        for dep in preset.get("depends_on", []):
            if dep not in modules:
                print(f"[WARN] 模块 {module} 依赖 {dep}，但未指定，可能产生孤立引用")
        stats = strip_one(target, preset, dry_run=args.preview)
        all_stats.append(stats)

    report(all_stats, dry_run=args.preview)

    if args.verify and not args.preview:
        verify_compile(target)


if __name__ == "__main__":
    main()
