#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
delivery-sync 同步脚本

把主项目镜像同步到交付目录，按 .deliveryignore 排除内部文件。
零外部依赖（只用 stdlib），跨平台（Windows/Linux/macOS）。

用法：
    python delivery_sync.py [--first-time] [--dry-run] [--preset 名称] [--status] [--force]
"""

import argparse
import fnmatch
import json
import os
import shutil
import subprocess
import sys
from datetime import datetime, timezone, timedelta
from pathlib import Path

# === 常量 ===

# 强制排除（即使用户从 .deliveryignore 删了这些条目，本脚本也强行排除）
FORCED_EXCLUDES = {
    ".git", ".claude", ".codex",
    "AGENTS.md", "CLAUDE.md",
    ".delivery-sync.json", ".deliveryignore",
}

# 后置二次清理（rsync 完成后再扫一遍，删任何漏网的敏感目录）
POST_CLEAN_PATTERNS = [
    ".git", ".claude", ".codex", ".github",
    "AGENTS.md", "CLAUDE.md",
    ".framework-sync.json", ".branch-sync-local.json",
]

CST = timezone(timedelta(hours=8))  # 东八区
SAFETY_DELETE_RATIO_THRESHOLD = 0.30  # 删除超过 30% 文件需二次确认


# === 工具函数 ===

def now_iso() -> str:
    """返回东八区 ISO 时间戳"""
    return datetime.now(CST).strftime("%Y-%m-%dT%H:%M:%S+08:00")


def run_git(args: list, cwd: Path) -> str:
    """执行 git 命令并返回 stdout（失败返回空字符串）"""
    try:
        result = subprocess.run(
            ["git"] + args, cwd=str(cwd),
            capture_output=True, text=True, encoding="utf-8", errors="replace",
            check=False
        )
        return result.stdout.strip() if result.returncode == 0 else ""
    except Exception:
        return ""


def parse_ignore_file(ignore_path: Path) -> list:
    """
    解析 gitignore 风格文件，返回 (pattern, is_negate, is_dir_only) 元组列表。
    支持：
    - 注释（#）
    - 否定规则（!）
    - 目录规则（trailing /）
    - 通配符（*, **, ?）
    """
    if not ignore_path.exists():
        return []
    patterns = []
    for line in ignore_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        is_negate = line.startswith("!")
        if is_negate:
            line = line[1:]
        is_dir_only = line.endswith("/")
        if is_dir_only:
            line = line.rstrip("/")
        patterns.append((line, is_negate, is_dir_only))
    return patterns


def match_ignore(rel_path: str, is_dir: bool, patterns: list) -> bool:
    """
    判断路径是否被 ignore（gitignore 语义简化版）。
    rel_path 用 / 作分隔符（POSIX 风格）。
    """
    matched = False
    for pat, is_negate, is_dir_only in patterns:
        if is_dir_only and not is_dir:
            # 目录-only 规则不能匹配文件
            # 但如果该文件位于此目录下，会在父级目录判断时拦截
            continue
        if _match_pattern(rel_path, pat):
            matched = not is_negate
    return matched


def _match_pattern(rel_path: str, pattern: str) -> bool:
    """
    单条 pattern 匹配。简化版 gitignore：
    - 含 / 的 pattern 锚定到根
    - 不含 / 的 pattern 匹配任意层级
    - 支持 * ** ?
    """
    # 锚定到根（pattern 含 / 或以 / 开头）
    anchored = "/" in pattern.rstrip("/")
    if pattern.startswith("/"):
        pattern = pattern[1:]
    # 匹配整个路径
    if anchored:
        # 整路径前缀匹配
        if fnmatch.fnmatchcase(rel_path, pattern):
            return True
        # 也允许该路径下的子项被排除
        if fnmatch.fnmatchcase(rel_path, pattern + "/*"):
            return True
        if rel_path.startswith(pattern + "/"):
            return True
    else:
        # 任意层级匹配：split 后比对每段或后缀
        if fnmatch.fnmatchcase(rel_path, pattern):
            return True
        # 末尾段匹配（如 *.log 匹配 a/b/c.log）
        for part in rel_path.split("/"):
            if fnmatch.fnmatchcase(part, pattern):
                return True
        # 路径包含该目录段
        if "/" + pattern + "/" in "/" + rel_path + "/":
            return True
    return False


def is_forced_exclude(rel_path: str) -> bool:
    """强制排除清单（无论 .deliveryignore 怎么写都排除）"""
    parts = rel_path.split("/")
    if parts[0] in FORCED_EXCLUDES:
        return True
    return False


def collect_source_files(source_root: Path, ignore_patterns: list) -> set:
    """遍历主项目，按 ignore 排除，返回相对路径集合（POSIX 风格）"""
    kept = set()
    for root, dirs, files in os.walk(source_root):
        rel_root = Path(root).relative_to(source_root).as_posix()
        if rel_root == ".":
            rel_root = ""

        # 过滤目录（剪枝）
        new_dirs = []
        for d in dirs:
            rel = f"{rel_root}/{d}" if rel_root else d
            if is_forced_exclude(rel):
                continue
            if match_ignore(rel, is_dir=True, patterns=ignore_patterns):
                continue
            new_dirs.append(d)
        dirs[:] = new_dirs

        # 过滤文件
        for f in files:
            rel = f"{rel_root}/{f}" if rel_root else f
            if is_forced_exclude(rel):
                continue
            if match_ignore(rel, is_dir=False, patterns=ignore_patterns):
                continue
            kept.add(rel)
    return kept


def collect_target_files(target_root: Path) -> set:
    """遍历交付目录，返回所有相对路径集合（用于对比）"""
    if not target_root.exists():
        return set()
    kept = set()
    for root, dirs, files in os.walk(target_root):
        rel_root = Path(root).relative_to(target_root).as_posix()
        if rel_root == ".":
            rel_root = ""
        # 跳过隐藏目录（.delivery-sync-marker 例外，单独处理）
        new_dirs = [d for d in dirs if not d.startswith(".")]
        dirs[:] = new_dirs
        for f in files:
            if f == ".delivery-sync-marker":
                continue  # 标记文件不参与对比
            rel = f"{rel_root}/{f}" if rel_root else f
            kept.add(rel)
    return kept


def files_differ(src: Path, dst: Path) -> bool:
    """快速判断两个文件是否不同（按 size + mtime，适合大文件）"""
    if not dst.exists():
        return True
    s_stat = src.stat()
    d_stat = dst.stat()
    if s_stat.st_size != d_stat.st_size:
        return True
    # mtime 相差小于 1 秒视为相同（跨文件系统精度差异）
    if abs(s_stat.st_mtime - d_stat.st_mtime) > 1:
        return True
    return False


def post_clean(target_root: Path, dry_run: bool) -> list:
    """后置清理：删除可能漏网的敏感文件/目录"""
    deleted = []
    for pattern in POST_CLEAN_PATTERNS:
        target = target_root / pattern
        if target.exists():
            if not dry_run:
                if target.is_dir():
                    shutil.rmtree(target)
                else:
                    target.unlink()
            deleted.append(str(target.relative_to(target_root)))
    return deleted


# === 主逻辑 ===

class DeliverySync:
    def __init__(self, source_root: Path, args):
        self.source_root = source_root.resolve()
        self.args = args
        self.config_path = self.source_root / ".delivery-sync.json"
        self.ignore_path = self.source_root / ".deliveryignore"
        self.template_dir = self.source_root / ".claude" / "skills" / "delivery-sync" / "templates"
        self.delivery_doc_dir = self.source_root / "delivery"
        self.config = self._load_config()

    def _load_config(self) -> dict:
        if self.config_path.exists():
            return json.loads(self.config_path.read_text(encoding="utf-8"))
        return None

    def _save_config(self):
        self.config_path.write_text(
            json.dumps(self.config, ensure_ascii=False, indent=2),
            encoding="utf-8"
        )

    def _ensure_gitignore(self):
        gi = self.source_root / ".gitignore"
        needed = [".delivery-sync.json", ".deliveryignore"]
        existing = gi.read_text(encoding="utf-8") if gi.exists() else ""
        missing = [n for n in needed if n not in existing]
        if missing:
            block = "\n# === Delivery 交付物管理（本地状态，不提交） ===\n"
            block += "\n".join(missing) + "\n"
            with gi.open("a", encoding="utf-8") as f:
                f.write(block)
            print(f"[INFO] 已追加到 .gitignore: {', '.join(missing)}")

    def _ensure_ignore_file(self):
        if not self.ignore_path.exists():
            tpl = self.template_dir / "deliveryignore.template"
            if tpl.exists():
                shutil.copy(tpl, self.ignore_path)
                print(f"[INFO] 已从模板初始化 .deliveryignore")
            else:
                print(f"[ERROR] .deliveryignore 缺失且找不到模板: {tpl}")
                sys.exit(3)

    def _ensure_delivery_docs(self):
        if not self.delivery_doc_dir.exists():
            self.delivery_doc_dir.mkdir(parents=True)
        for name in ["README.md", "DEPLOY.md"]:
            target = self.delivery_doc_dir / name
            if not target.exists():
                tpl = self.template_dir / f"{name}.template"
                if tpl.exists():
                    shutil.copy(tpl, target)
                    print(f"[INFO] 已从模板初始化 delivery/{name}")

    def _resolve_delivery_dir(self) -> Path:
        if not self.config:
            # 首次创建交互式获取
            default = "../ruoyi-plus-uniapp-delivery"
            user_input = input(f"交付目录路径? (默认 {default}): ").strip()
            target = Path(user_input or default)
        else:
            target = Path(self.config["deliveryDir"])
        if not target.is_absolute():
            target = (self.source_root / target).resolve()
        # 安全检查：不能指向主项目自身
        if target == self.source_root:
            print(f"[ERROR] 交付目录不能指向主项目自身: {target}")
            sys.exit(2)
        return target

    def _safety_check_target(self, target: Path, is_first_time: bool):
        marker = target / ".delivery-sync-marker"
        if is_first_time and target.exists() and any(target.iterdir()):
            if not marker.exists() and not self.args.force:
                print(f"[ERROR] 目标目录已存在内容且无 marker 文件: {target}")
                print("        如确认要覆盖请加 --force 或换路径")
                sys.exit(2)

    def status(self):
        if not self.config:
            print("[INFO] .delivery-sync.json 不存在，请先 --first-time 创建")
            return
        head = run_git(["rev-parse", "--short", "HEAD"], self.source_root)
        last = self.config.get("lastSyncCommit", "(无)")
        print("=" * 50)
        print("交付同步状态")
        print("=" * 50)
        print(f"交付目录:    {self.config['deliveryDir']}")
        print(f"当前预设:    {self.config.get('activePreset', 'default')}")
        print(f"累计同步:    {self.config.get('syncCount', 0)} 次")
        print(f"上次同步:    {self.config.get('lastSyncDate', '(无)')} (commit {last})")
        print(f"主项目 HEAD: {head}")
        if last and head and last != head:
            log = run_git(["log", f"{last}..HEAD", "--oneline"], self.source_root)
            commits = [l for l in log.split("\n") if l]
            print(f"\n待同步提交（{len(commits)} 个）:")
            for c in commits[:20]:
                print(f"  {c}")
            if len(commits) > 20:
                print(f"  ... 还有 {len(commits) - 20} 条")
            print(f"\n执行同步: python delivery_sync.py")
        else:
            print("\n[OK] 已是最新版本")

    def _get_active_excludes(self) -> list:
        """读取 .deliveryignore + 当前预设的 extraExclude，合并成 ignore patterns"""
        patterns = parse_ignore_file(self.ignore_path)
        if self.config:
            preset_name = self.args.preset or self.config.get("activePreset", "default")
            preset = self.config.get("presets", {}).get(preset_name, {})
            extra = preset.get("extraExclude", [])
            for line in extra:
                is_dir_only = line.endswith("/")
                pat = line.rstrip("/")
                patterns.append((pat, False, is_dir_only))
        return patterns

    def sync(self):
        is_first_time = self.args.first_time or not self.config
        target = self._resolve_delivery_dir()
        self._safety_check_target(target, is_first_time)
        self._ensure_ignore_file()
        self._ensure_delivery_docs()
        self._ensure_gitignore()

        head = run_git(["rev-parse", "HEAD"], self.source_root)
        head_short = run_git(["rev-parse", "--short", "HEAD"], self.source_root)
        if not head:
            print("[ERROR] 主项目不是 git 仓库或 git 不可用")
            sys.exit(3)

        git_dirty = run_git(["status", "--porcelain"], self.source_root)
        if git_dirty and not self.args.force:
            print("[WARN] 主项目有未提交改动:")
            for line in git_dirty.split("\n")[:10]:
                print(f"  {line}")
            yn = input("继续同步? (y/N): ").strip().lower()
            if yn != "y":
                sys.exit(1)

        # 收集源文件清单
        patterns = self._get_active_excludes()
        print(f"[INFO] 扫描主项目文件...")
        source_files = collect_source_files(self.source_root, patterns)
        target_files = collect_target_files(target)

        # 计算差异
        to_add = source_files - target_files
        to_delete = target_files - source_files
        common = source_files & target_files
        to_modify = set()
        for rel in common:
            src = self.source_root / rel
            dst = target / rel
            if files_differ(src, dst):
                to_modify.add(rel)

        # 安全检查：删除比例
        if target_files:
            del_ratio = len(to_delete) / len(target_files)
            if del_ratio > SAFETY_DELETE_RATIO_THRESHOLD and not self.args.force:
                print(f"[WARN] 将删除 {len(to_delete)} 个文件 ({del_ratio:.0%})，超过 {SAFETY_DELETE_RATIO_THRESHOLD:.0%} 阈值")
                yn = input("继续? (y/N): ").strip().lower()
                if yn != "y":
                    sys.exit(1)

        # 输出预览
        print()
        print("=" * 50)
        print(f"{'[DRY-RUN] ' if self.args.dry_run else ''}同步预览")
        print("=" * 50)
        print(f"源:    {self.source_root}")
        print(f"目标:  {target}")
        print(f"预设:  {self.args.preset or (self.config.get('activePreset', 'default') if self.config else 'default')}")
        print(f"baseline: {self.config.get('lastSyncCommit', '(无)') if self.config else '(首次)'} → {head_short}")
        print(f"\n变更统计:")
        print(f"  新增: {len(to_add)} 个")
        print(f"  修改: {len(to_modify)} 个")
        print(f"  删除: {len(to_delete)} 个")
        print(f"  保留: {len(common) - len(to_modify)} 个未变")

        if self.args.dry_run:
            print("\n[DRY-RUN 模式] 不执行实际操作")
            return

        if not (to_add or to_modify or to_delete):
            print("\n[OK] 无变更，无需同步")
            return

        if not self.args.force:
            yn = input("\n执行同步? (y/N): ").strip().lower()
            if yn != "y":
                sys.exit(1)

        # 实际执行
        target.mkdir(parents=True, exist_ok=True)
        for rel in sorted(to_add | to_modify):
            src = self.source_root / rel
            dst = target / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst)

        for rel in sorted(to_delete, reverse=True):
            dst = target / rel
            if dst.exists():
                dst.unlink()
            # 清理空目录
            try:
                dst.parent.rmdir()
            except OSError:
                pass

        # 后置清理
        cleaned = post_clean(target, dry_run=False)
        if cleaned:
            print(f"[INFO] 后置清理删除: {', '.join(cleaned)}")

        # 清理空目录链（rmtree 删文件后可能留下空目录壳）
        for root, dirs, files in os.walk(target, topdown=False):
            for d in dirs:
                dpath = Path(root) / d
                try:
                    dpath.rmdir()  # 仅在为空时成功
                except OSError:
                    pass

        # 复制 delivery/ 文档到根
        for name in ["README.md", "DEPLOY.md", "CHANGELOG.md"]:
            doc = self.delivery_doc_dir / name
            if doc.exists():
                shutil.copy2(doc, target / name)

        # 写入 marker
        marker = target / ".delivery-sync-marker"
        marker.write_text(
            f"sourceProject=ruoyi-plus-uniapp\n"
            f"sourceCommit={head}\n"
            f"syncedAt={now_iso()}\n"
            f"managedBy=delivery-sync skill\n",
            encoding="utf-8"
        )

        # 更新 .delivery-sync.json
        if not self.config:
            # 用 os.path.relpath 保留 "../" 前缀（关键：让相对路径解析时能正确指向源目录之外）
            try:
                rel_dir = os.path.relpath(target, self.source_root).replace("\\", "/")
            except ValueError:
                # Windows 跨盘符时 relpath 抛 ValueError，回退绝对路径
                rel_dir = str(target).replace("\\", "/")
            self.config = {
                "deliveryDir": rel_dir,
                "lastSyncCommit": "",
                "lastSyncDate": "",
                "syncCount": 0,
                "presets": {"default": {"extraExclude": []}},
                "activePreset": "default",
                "history": []
            }
        old_commit = self.config.get("lastSyncCommit", "")
        self.config["lastSyncCommit"] = head_short
        self.config["lastSyncDate"] = now_iso()
        self.config["syncCount"] = self.config.get("syncCount", 0) + 1
        if self.args.preset:
            self.config["activePreset"] = self.args.preset
        self.config.setdefault("history", []).insert(0, {
            "date": now_iso(),
            "fromCommit": old_commit,
            "toCommit": head_short,
            "preset": self.args.preset or self.config.get("activePreset", "default"),
            "filesAdded": len(to_add),
            "filesModified": len(to_modify),
            "filesDeleted": len(to_delete),
        })
        self._save_config()

        print()
        print("=" * 50)
        print("[OK] 同步完成")
        print("=" * 50)
        print(f"目标: {target}")
        print(f"+{len(to_add)} ~{len(to_modify)} -{len(to_delete)}")
        print(f"baseline: {old_commit or '(首次)'} → {head_short}")


def main():
    parser = argparse.ArgumentParser(description="主项目 → 交付目录单向同步")
    parser.add_argument("--first-time", action="store_true", help="首次创建交付目录")
    parser.add_argument("--dry-run", action="store_true", help="仅预览不执行")
    parser.add_argument("--preset", type=str, help="使用指定预设")
    parser.add_argument("--status", action="store_true", help="仅查看状态")
    parser.add_argument("--force", action="store_true", help="跳过部分确认")
    args = parser.parse_args()

    # 主项目根 = 脚本路径上溯 5 层（scripts → delivery-sync → skills → .claude → 项目根 = parents[4]）
    source_root = Path(__file__).resolve().parents[4]
    sync = DeliverySync(source_root, args)

    if args.status:
        sync.status()
    else:
        sync.sync()


if __name__ == "__main__":
    main()
