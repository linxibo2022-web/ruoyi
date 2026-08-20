
# 交付物同步指南（delivery-sync）

## 概述

把主项目（含 `.git/`、`.claude/`、`.codex/`、`AGENTS.md`、`CLAUDE.md`、内部 docs 等）镜像同步到同级"交付目录"，生成一份**可交付给外部**的干净副本。

**核心约束**（用户硬性要求）：
- 交付目录**不带任何 git 历史**（无 `.git/`）
- 交付目录**不带任何技能体系**（无 `.claude/`、`.codex/`、`AGENTS.md`、`CLAUDE.md`）
- 交付目录**不带内部协作文档**（无 `docs/tasks/`、`docs/experience/`、`docs/brainstorm-*.md` 等）
- 交付目录**只保留交付方需要的文档**（`delivery/` 目录下的 README.md、DEPLOY.md 等）

**核心机制**：
- 主项目根维护 `.deliveryignore`（gitignore 风格）声明排除清单
- 主项目根维护 `.delivery-sync.json` 记录上次同步的 baseline commit + 历史
- 通过 Python 脚本（零外部依赖、跨平台）执行单向镜像同步
- 同步前 `--dry-run` 预览所有变更，用户确认后才实际执行

**与 module-strip 的关系**：
- 本技能负责**文件路径级**排除（含整个端目录如 `plus-uniapp/`）
- **业务模块语义级**裁剪（如删 `mall` 后还要改 pom.xml/SQL/菜单）由第二期 `module-strip` 技能处理
- 两个技能共用 `.delivery-sync.json` 配置文件

---

## 文件结构

### 主项目侧（开发仓库）

```
ruoyi-plus-uniapp/
├── .delivery-sync.json          # ⚠️ 加 .gitignore，纯本地，记 baseline
├── .deliveryignore              # ⚠️ 加 .gitignore，排除清单（防泄漏内部目录结构）
├── delivery/                    # ✅ 进版本控制，给交付方看的文档
│   ├── README.md                # 项目说明（替代被删的 CLAUDE.md）
│   ├── DEPLOY.md                # 部署文档
│   └── CHANGELOG.md             # 可选，发版日志
├── .claude/skills/delivery-sync/
│   ├── SKILL.md                 # 本技能文档
│   ├── scripts/
│   │   └── delivery_sync.py    # 实施脚本（Python，零依赖）
│   └── templates/
│       ├── deliveryignore.template  # 默认排除清单
│       ├── README.md.template       # delivery/README.md 初始模板
│       └── DEPLOY.md.template       # delivery/DEPLOY.md 初始模板
└── .claude/commands/
    └── sync-delivery.md         # 斜杠命令（同步到 .agents/skills/sync-delivery/）
```

### 交付目录侧（产出物）

```
ruoyi-plus-uniapp-delivery/      # 默认在主项目同级
├── README.md                    # 来自主项目 delivery/README.md
├── DEPLOY.md                    # 来自主项目 delivery/DEPLOY.md
├── ruoyi-admin/                 # 全量同步
├── ruoyi-modules/               # 全量同步
├── ruoyi-common/                # 全量同步
├── plus-ui/                     # 视配置保留或排除
├── plus-uniapp/                 # 视配置保留或排除
├── plus-app/                    # 视配置保留或排除
├── script/                      # SQL 脚本等
├── pom.xml
└── package.json
```

**绝对不存在**：`.git/`、`.claude/`、`.codex/`、`AGENTS.md`、`CLAUDE.md`、`docs/tasks/`、`docs/experience/`、`prototype/`、`tpl*.png`、IDE 配置等。

---

## 配置文件详解

### `.delivery-sync.json`（主项目根，本地不提交）

```json
{
  "deliveryDir": "../ruoyi-plus-uniapp-delivery",
  "lastSyncCommit": "82a65e5fc",
  "lastSyncDate": "2026-05-02T13:45:00+08:00",
  "syncCount": 3,
  "presets": {
    "default": {
      "extraExclude": []
    },
    "客户A-基础版": {
      "extraExclude": ["plus-uniapp/", "plus-app/"]
    },
    "客户B-纯后端": {
      "extraExclude": ["plus-ui/", "plus-uniapp/", "plus-app/"]
    }
  },
  "activePreset": "default",
  "history": [
    {
      "date": "2026-05-02T13:45:00+08:00",
      "fromCommit": "019f2d186",
      "toCommit": "82a65e5fc",
      "preset": "default",
      "filesAdded": 5,
      "filesModified": 12,
      "filesDeleted": 2
    }
  ]
}
```

**字段说明**：

| 字段 | 含义 |
|------|------|
| `deliveryDir` | 交付目录路径（相对/绝对均可） |
| `lastSyncCommit` | 上次同步对应的主项目 commit hash |
| `lastSyncDate` | 上次同步时间（东八区） |
| `syncCount` | 累计同步次数 |
| `presets` | 预设配置，每个预设可指定额外排除规则（叠加在 `.deliveryignore` 之上） |
| `activePreset` | 当前激活的预设名 |
| `history` | 同步历史，每条记录一次同步的统计 |

### `.deliveryignore`（主项目根，本地不提交）

类 `.gitignore` 语法。首次同步时从 `templates/deliveryignore.template` 自动初始化。

**默认内容**：

```
# === 强制排除（无 git、无技能体系约束）===
.git/
.github/
.claude/
.codex/
AGENTS.md
CLAUDE.md

# === 内部状态文件 ===
.framework-sync.json
.branch-sync-local.json
.delivery-sync.json
.deliveryignore

# === 内部协作文档 ===
docs/tasks/
docs/experience/
docs/brainstorm-*.md
docs/login-designs/
docs/login-styles-gallery.html
docs/prototype-login-tab.html
docs/安全漏洞扫描报告.md

# === 设计稿/截图 ===
prototype/
tpl*.png

# === IDE / 本地配置 ===
.idea/
.vscode/
*.iml
*.sw[op]

# === 编译产物 / 日志 ===
*.log
hs_err_pid*.log
target/
node_modules/
dist/
.DS_Store
Thumbs.db

# === 测试产物 ===
test-results/
playwright-report/
coverage/
```

**用户可编辑**：每次同步会读取最新的 `.deliveryignore`。如果需要排除更多内容（如某个业务模块的临时实验目录），直接编辑此文件。

---

## 触发命令

### 主命令

```
/sync-delivery                   # 用 default 预设增量同步
/sync-delivery --preset 客户A     # 用指定预设
/sync-delivery --dry-run          # 仅预览不执行
/sync-delivery --first-time       # 首次创建交付目录
/sync-delivery --status           # 仅查看 baseline 与待同步提交数
```

### 自然语言触发

- "把代码同步到交付目录"
- "做一份交付副本"
- "更新交付版本到最新"
- "看看交付目录还差几个提交"

---

## 同步流程

### 首次创建（交付目录不存在）

```
1. 用户触发 /sync-delivery --first-time
2. 检查 .delivery-sync.json 是否存在 → 不存在则交互式创建
   - 提示用户输入交付目录路径（默认 ../ruoyi-plus-uniapp-delivery）
   - 检查路径是否已有内容 → 有则警告并要求 --force 或换路径
3. 检查主项目 git status 是否干净
   - 不干净 → 警告但允许继续（用户决定）
4. 检查 .deliveryignore 是否存在 → 不存在则从模板复制
5. 检查 delivery/README.md / delivery/DEPLOY.md 是否存在 → 不存在则从模板复制
6. 用户确认排除清单（显示生效的 .deliveryignore 内容）
7. Python 脚本执行 dry-run，输出"将复制 X 个文件、跳过 Y 个文件"
8. 用户确认后实际执行
9. 后置清理：再次扫描交付目录，删除可能漏网的敏感文件
   - 删除 .git/.claude/.codex/AGENTS.md/CLAUDE.md（防御性删除，万一 ignore 失效）
10. 复制 delivery/*.md → 交付目录根
11. 写入 .delivery-sync.json：lastSyncCommit = 主项目 HEAD，syncCount=1
12. 输出报告：交付目录大小、文件数、目录树概览
```

### 增量同步（交付目录已存在）

```
1. 用户触发 /sync-delivery
2. L1 校验：主项目 git status 是否干净
   - 不干净则提示："建议先 commit 当前改动以便记录精确 baseline，是否继续？"
3. 读取 .delivery-sync.json
4. 比较主项目 HEAD vs lastSyncCommit
   - 一致 → "交付目录已是最新版本（commit: xxx）" 退出
   - 不一致 → 显示新增提交列表（git log lastSyncCommit..HEAD --oneline）
5. 检查交付目录是否有用户的额外修改（git 不可用，只能通过文件 mtime 对比）
   - 发现交付目录有新增文件（不在主项目对应路径） → 询问是否删除
   - 发现交付目录文件 mtime 比主项目对应文件晚 → 警告但默认覆盖
6. dry-run 预览：将新增 X 个、修改 Y 个、删除 Z 个文件
7. 用户确认
8. 实际执行（带 --delete）
9. 后置清理（同首次）
10. 复制 delivery/*.md（如果有更新）
11. 更新 .delivery-sync.json：追加 history 一条，更新 lastSyncCommit
12. 输出报告
```

### 仅状态查看（不写入）

```
1. 用户触发 /sync-delivery --status
2. 输出：
   - deliveryDir: 路径
   - lastSyncCommit: 上次 baseline
   - 主项目 HEAD vs baseline 差异：N 个新提交
   - 待同步提交清单（git log）
   - 当前激活预设：xxx
   - 累计同步次数：N
```

---

## 端选择（通过预设实现）

要排除整个端，直接在预设里写整个目录路径：

```json
{
  "presets": {
    "客户A-基础版": {
      "extraExclude": [
        "plus-uniapp/",
        "plus-app/"
      ]
    }
  }
}
```

**端目录速查**：

| 端 | 排除路径 | 说明 |
|----|---------|------|
| PC 前端 | `plus-ui/` | Vue 3 + Element Plus 后台管理 |
| 移动端（CLI） | `plus-uniapp/` | 小程序/H5/不需原生插件的 APP |
| 移动端（原生） | `plus-app/` | HBuilderX 构建的原生 APP/鸿蒙 |
| 后端 | （不能排除） | `ruoyi-admin/`、`ruoyi-modules/`、`ruoyi-common/` 是项目核心 |

> ⚠️ **业务模块裁剪**（如去掉 mall/crm/iot/支付）**不能**靠 `.deliveryignore` 完成——还要改 pom.xml、删菜单 SQL、删字典数据。这部分由第二期 `module-strip` 技能处理。本技能只支持文件路径级排除。

---

## 安全检查（防意外破坏）

### 主项目侧

| 检查 | 触发条件 | 处置 |
|------|---------|------|
| `git status` 不干净 | 有未提交改动 | 警告并询问是否继续（不强制阻止） |
| `.deliveryignore` 缺关键项 | 检测到无 `.git/`、`.claude/` 等强制排除 | 拒绝执行，提示从模板恢复 |
| 主项目根写权限失败 | `.delivery-sync.json` 写不进去 | 报错并退出 |

### 交付目录侧

| 检查 | 触发条件 | 处置 |
|------|---------|------|
| 路径指向主项目本身 | `deliveryDir` 解析后 = 主项目路径 | **强制阻止**，避免自我覆盖 |
| 路径指向无关已存在目录 | 首次创建时目标已有内容且无 `.delivery-sync-marker` | 拒绝，要求 `--force` 或换路径 |
| 同步删除超过 30% 文件 | dry-run 显示删除量异常 | 强制要求二次确认 |
| 同步会删除 `.git/` 等敏感目录 | dry-run 检测到 | **绝不执行删除外部 .git**，警告退出 |

### 同步标记文件

每次成功同步后，在交付目录根写入隐藏标记：

```
ruoyi-plus-uniapp-delivery/.delivery-sync-marker
```

内容：
```
sourceProject=ruoyi-plus-uniapp
sourceCommit=82a65e5fc
syncedAt=2026-05-02T13:45:00+08:00
managedBy=delivery-sync skill
```

**作用**：
1. 识别该目录是受管的交付目录（防止误同步到无关目录）
2. 提示交付方"此目录由主项目自动维护"

---

## 实战示例

### 示例 1：首次创建交付目录（默认配置）

```
用户：帮我做一份交付副本

[Skill 流程]
1. 检查 .delivery-sync.json → 不存在
2. 交互：
   "交付目录路径？(默认 ../ruoyi-plus-uniapp-delivery)" → 回车采用默认
3. 检查目录 ../ruoyi-plus-uniapp-delivery → 不存在 ✓
4. 复制 templates/deliveryignore.template → .deliveryignore
5. 检查 delivery/README.md → 不存在 → 复制模板
6. dry-run 预览：
   "将创建交付目录，复制 1234 个文件（约 45 MB），排除 89 个文件/目录"
7. 用户确认 → 执行
8. 后置清理：检查 ../ruoyi-plus-uniapp-delivery/.git → 不存在 ✓
9. 复制 delivery/README.md → ../ruoyi-plus-uniapp-delivery/README.md
10. 写入 .delivery-sync.json
11. 输出：✅ 交付目录已创建 | 1234 文件 | baseline: 82a65e5fc
```

### 示例 2：增量同步（主项目有 3 个新提交）

```
用户：交付目录该更新了吧

[Skill 流程]
1. 读取 .delivery-sync.json：lastSyncCommit = 82a65e5fc
2. git log 82a65e5fc..HEAD：
   - aaaaaaaa fix(mall): 修复订单状态 bug
   - bbbbbbbb feat(user): 新增用户标签
   - cccccccc docs(workflow): 多会话避让协议
3. dry-run 预览：
   - 修改 7 个文件
   - 新增 2 个文件
   - 删除 0 个文件
4. 用户确认 → 执行
5. 更新 .delivery-sync.json：
   - lastSyncCommit = cccccccc
   - history 追加一条
6. 输出：✅ 同步完成 | +2/~7/-0 | baseline: 82a65e5fc → cccccccc
```

### 示例 3：使用预设（客户 A 不要移动端）

```
用户：/sync-delivery --preset 客户A-基础版

[Skill 流程]
1. 读取 .delivery-sync.json：activePreset = default
2. 切换到指定预设：客户A-基础版（extraExclude = ["plus-uniapp/", "plus-app/"]）
3. dry-run：将额外排除 plus-uniapp/、plus-app/ 整个目录
4. 用户确认
5. 执行同步
6. 写入：activePreset = "客户A-基础版"
7. 输出：✅ | 已排除移动端目录 | 文件数从 1234 → 856
```

### 示例 4：仅查看状态

```
用户：/sync-delivery --status

输出：
=================================
交付同步状态
=================================
交付目录:    ../ruoyi-plus-uniapp-delivery
当前预设:    default
累计同步:    3 次
上次同步:    2026-05-02 13:45 (commit 82a65e5fc)
主项目 HEAD: cccccccc

待同步提交（3 个）:
  cccccccc docs(workflow): 多会话避让协议
  bbbbbbbb feat(user): 新增用户标签
  aaaaaaaa fix(mall): 修复订单状态 bug

执行 /sync-delivery 应用这些改动。
```

---

## 与其他技能/命令的关系

| 上游/下游 | 技能/命令 | 关系 |
|----------|----------|------|
| 上游 | `git-workflow` | 同步前确保主项目干净（git commit） |
| 上游 | `framework-sync` | 主项目从框架拉取的更新可同步到交付目录 |
| 下游 | `module-strip`（第二期） | 文件级排除完成后，做语义级模块裁剪 |
| 同级 | `sync-branches-local` | 都是单向同步，但目标不同（多分支版本 vs 交付副本） |
| 关联 | `deployment-guide` | DEPLOY.md 内容可参考此技能的部署知识 |

---

## 常见错误

### ❌ 错误 1：把交付目录当 git 仓库管理

**现象**：在交付目录里 `git init`，然后做 commit。
**为什么错**：交付方拿到的应该是干净的代码，不需要主项目的内部协作历史。如果交付方需要 git，让他们自己 init。
**正确做法**：交付目录永远不存在 `.git/`，每次同步都是覆盖式镜像。

### ❌ 错误 2：直接 `cp -r` 主项目到交付目录

**现象**：用户跳过 skill，直接 `cp -r ruoyi-plus-uniapp ruoyi-plus-uniapp-delivery`。
**为什么错**：会带上 `.git/`、`.claude/`、`AGENTS.md`、`node_modules/` 等不该交付的内容。
**正确做法**：用 `/sync-delivery`，按 `.deliveryignore` 精确排除。

### ❌ 错误 3：在交付目录直接修改代码

**现象**：交付方反馈 bug 后，直接在交付目录里改代码。
**为什么错**：下次同步时会被覆盖。
**正确做法**：在主项目改 → commit → 同步到交付目录。

### ❌ 错误 4：把 `.deliveryignore` 提交到 git

**现象**：用户运行 `git add .` 把 `.deliveryignore` 加进了仓库。
**为什么错**：泄漏内部目录结构（暴露 `docs/tasks/`、`docs/experience/` 等）。
**正确做法**：`.deliveryignore` + `.delivery-sync.json` 都要在 `.gitignore` 中。本技能首次执行时会自动追加。

### ❌ 错误 5：靠 `.deliveryignore` 删业务模块

**现象**：在 `.deliveryignore` 加 `ruoyi-modules/ruoyi-business/.../mall/`。
**为什么错**：能删目录但不会改 pom.xml，结果交付目录编译失败。
**正确做法**：等第二期 `module-strip` 技能。本技能只支持端级目录排除。

---

## 实施细节

### Python 脚本入口

```bash
python .claude/skills/delivery-sync/scripts/delivery_sync.py [选项]
```

**选项**：
- `--first-time` 首次创建模式
- `--dry-run` 仅预览
- `--preset <名称>` 使用指定预设
- `--status` 仅查看状态
- `--force` 跳过部分安全检查

**返回码**：
- 0：成功
- 1：用户主动取消
- 2：安全检查失败
- 3：环境异常（如目录不可写）

### 跨平台兼容

- 路径全部用 `pathlib.Path`，自动适配 Windows / Linux / macOS
- ignore 匹配用 `fnmatch`（stdlib 自带，零依赖）
- 终端输出用 ASCII，避免 Windows 控制台乱码（`PYTHONIOENCODING=utf-8` 前置）

### `.gitignore` 自动维护

首次执行 skill 时，检查主项目 `.gitignore`：

```
# Delivery 交付物管理（不要提交本地状态）
.delivery-sync.json
.deliveryignore
```

如不存在则自动追加。

---

## 触发自检

执行本技能前，必查：

- [ ] 主项目根有 `.git/`（确认是 git 仓库）
- [ ] 主项目当前在 master 分支（其他分支需用户确认）
- [ ] Python 3.7+ 可用（`python --version`）
- [ ] `.delivery-sync.json` 路径合法（解析后不指向主项目自身）
- [ ] 交付目录所在父目录可写

---

## 触发后自检

执行本技能后，必查：

- [ ] 交付目录不含 `.git/`
- [ ] 交付目录不含 `.claude/`、`.codex/`
- [ ] 交付目录不含 `AGENTS.md`、`CLAUDE.md`
- [ ] 交付目录根有 `README.md`（来自 `delivery/README.md`）
- [ ] 交付目录根有 `.delivery-sync-marker`
- [ ] 主项目 `.delivery-sync.json` 已更新 lastSyncCommit
- [ ] 主项目 `.gitignore` 已包含 `.delivery-sync.json` 和 `.deliveryignore`

---

## 与第二期 `module-strip` 的预留接口

`.delivery-sync.json` 已预留 `stripModules` 字段（本期不读取，第二期使用）：

```json
{
  "stripModules": ["mall", "crm", "iot", "pay"],
  "stripModulesEffect": {
    "mall": "deleteDir + cleanPom + cleanMenuSql + cleanDictSql",
    "...": "..."
  }
}
```

**第二期会做的事**（本期不做）：
- 在交付目录删除业务模块目录后，处理 `pom.xml` 中的 `<module>mall</module>` 引用
- 删除 `script/sql/*.sql` 中该模块的菜单/字典初始化语句
- 删除前端路由表中相关页面引用
- 处理跨模块依赖（如 mall 被其他模块依赖时拒绝裁剪）

本期用户如果只想排除整个端（plus-uniapp 等），用 `extraExclude` 即可，无需等第二期。
