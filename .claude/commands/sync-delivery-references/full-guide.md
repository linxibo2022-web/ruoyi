
# /sync-delivery - 主项目 → 交付副本同步助手

把主项目镜像同步到同级"交付目录"，生成不带 git 历史、不带技能体系、不带内部协作文档的可交付版本。

> 本命令在 Claude Code 与 Codex CLI 双端可用（Codex 通过 `.agents/skills/sync-delivery/` 自然语言匹配触发）。

---

## 触发方式

```
/sync-delivery                    # 用 default 预设增量同步（询问确认）
/sync-delivery --first-time       # 首次创建交付目录
/sync-delivery --status           # 仅查看 baseline 与待同步提交数
/sync-delivery --dry-run          # 仅预览不执行
/sync-delivery --preset 客户A     # 用指定预设同步
/sync-delivery --force            # 跳过部分确认（慎用）
```

---

## 执行流程

### 第一步：环境检查

```bash
# 确认主项目根
test -d .git && echo "✓ git repo"

# 确认 Python 可用
python --version || python3 --version

# 确认 skill 脚本存在
ls -l .claude/skills/delivery-sync/scripts/delivery_sync.py
```

### 第二步：调用脚本

```bash
PYTHONIOENCODING=utf-8 python .claude/skills/delivery-sync/scripts/delivery_sync.py [选项]
```

**Windows 防乱码必须设 `PYTHONIOENCODING=utf-8`**（参考全局指令）。

### 第三步：根据脚本交互

脚本会根据当前状态自动进入相应流程：

- **首次创建**（无 `.delivery-sync.json`）：交互式询问交付目录路径 → 复制模板 → dry-run 预览 → 用户确认 → 实际同步
- **增量同步**（已有 baseline）：自动比对主项目 HEAD 与 baseline → 显示新增提交清单 → dry-run 预览 → 用户确认 → 同步
- **状态查看**：只读输出，不写文件

### 第四步：同步后报告

输出包含：
- 同步前后文件数变化（+X / ~Y / -Z）
- baseline commit 变化（旧 → 新）
- 交付目录绝对路径
- `.delivery-sync-marker` 文件位置（隐藏文件，给交付方查阅）

---

## 内部约定

| 项目 | 路径 | 是否提交 |
|------|------|---------|
| 状态文件 | `.delivery-sync.json` | ❌ 在 .gitignore，本地维护 |
| 排除清单 | `.deliveryignore` | ❌ 在 .gitignore，本地维护 |
| 给交付方的 README/DEPLOY | `delivery/*.md` | ✅ 进版本控制 |
| 同步脚本 | `.claude/skills/delivery-sync/scripts/delivery_sync.py` | ✅ 进版本控制 |
| 模板文件 | `.claude/skills/delivery-sync/templates/` | ✅ 进版本控制 |

---

## 详细规范

完整流程、配置字段、安全检查、常见错误等详见 skill 文档：

`.claude/skills/delivery-sync/SKILL.md`（Codex 端：`.agents/skills/delivery-sync/SKILL.md`）

需要时通过自然语言（如"交付副本"、"同步交付"等触发词）激活该 skill 获取详细指导。

---

## 关联命令

- `/sync-branches-local` 多分支同步（master → single/workflow）
- `/framework-sync` 从框架原仓库拉取更新
- `/sync-local` 项目文档同步
