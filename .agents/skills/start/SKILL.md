---
name: start
description: |
  /start - 项目智能导航，自动识别环境（框架仓库/子项目/全新项目/开发中项目）和用户身份，提供个性化引导。

  触发场景：
  - 新打开一个 Claude Code 窗口，需要快速了解项目
  - 需要查看项目当前状态和可用命令
  - 框架维护者需要查看同步命令和技能维护入口
  - 子项目开发者需要了解框架同步和日常开发流程
  - 接手项目需要快速上手

  触发词：/start、新窗口、快速了解、项目状态、开始、启动、项目概览、上手指南、项目导航
---

# /start - 项目智能导航

作为项目智能导航助手，识别当前环境和用户身份，提供个性化的项目引导。

---

## 第一步：环境检测

按以下顺序检测，确定当前处于哪种环境：

### 检测信号

```bash
# 1. 检查是否为框架仓库（维护者环境）
ls .claude/commands/sync-local.md 2>/dev/null
ls .claude/commands/sync-wot-local.md 2>/dev/null
ls .claude/commands/sync-branches-local.md 2>/dev/null

# 2. 检查是否为子项目（使用者环境）
ls .framework-sync.json 2>/dev/null

# 3. 检查框架配置
ls .claude/framework-config.json 2>/dev/null

# 4. 检查业务模块数量
ls ruoyi-modules/ 2>/dev/null

# 5. 检查项目文档状态
ls docs/项目状态.md docs/待办清单.md docs/需求文档.md 2>/dev/null
ls docs/tasks/active/ 2>/dev/null

# 6. 检查 Git 状态和最近提交
git status --short
git log -5 --format="%H|%an|%cn|%s" --no-merges
```

### 判定规则

| 条件 | 环境 |
|------|------|
| 存在 `.claude/commands/sync-local.md` 且不存在 `.framework-sync.json` | **框架仓库**（维护者环境） |
| 存在 `.framework-sync.json` | **子项目**（使用者环境） |
| 都不存在，且业务模块数 = 0 | **全新项目** |
| 都不存在，且业务模块数 > 0 | **开发中项目** |

---

## 第二步：读取框架配置

如果存在 `.claude/framework-config.json`，读取它来区分框架模块和业务模块。

**排除的框架模块**（不计入业务统计）：
- ruoyi-system（系统管理）
- ruoyi-generator（代码生成器）

---

## 第三步：Git 提交过滤

获取最近提交时，过滤非开发提交（与 /next、/progress 一致）：

| 过滤条件 | 识别方式 | 分类 |
|---------|---------|------|
| Cherry-pick 提交 | commit message 包含 `cherry picked from` | 同步提交 |
| 上游同步提交 | commit message 以 `sync:` 或 `upstream:` 开头 | 同步提交 |
| Author ≠ Committer | author 和 committer 名字不同 | 可能是 cherry-pick |
| 文档/配置提交 | commit message 以 `docs:` `chore:` `style:` 开头 | 非功能提交 |

---

## 第四步：输出报告（按环境选择模板）

### 环境 A：框架仓库（维护者环境）

> 检测到 sync-local/sync-wot/sync-branches 等维护者命令，识别为框架维护者。

```markdown
# 👋 欢迎回到 ruoyi-plus-uniapp 框架仓库

## 📂 框架信息
- **技术栈**：Spring Boot + Vue3 + UniApp
- **Common 模块**：36 个
- **Skills/Commands**：47+ 技能 / 15 命令
- **分支体系**：master（多租户）/ single（非多租户）/ workflow（工作流）

## 🕐 最近开发动态
[过滤后的最近 3-5 条业务提交]

## 📊 当前状态
- **代码状态**：✅ 干净 / ⚠️ 有未提交修改（列出数量）
- **业务模块**：检测到 X 个（ruoyi-business 等）
- **活跃任务**：[从 docs/tasks/active/ 读取，无则显示"无"]

## 🔧 框架维护工作流

### 日常开发
| 想做什么 | 命令 |
|---------|------|
| 开发新的业务示例功能 | `/dev` |
| 从现有表快速生成 CRUD | `/crud` |
| 检查代码规范 | `/check` |
| 获取下一步开发建议 | `/next` |

### 上游同步（从源仓库拉取更新）
| 想做什么 | 命令 |
|---------|------|
| 同步 ruoyi-vue-plus 后端更新 | `/sync-local` |
| 同步 WOT Design Uni 组件库 | `/sync-wot` |
| 同步 Unibest 移动端框架 | `/sync-unibest` |

### 跨分支同步（master → single/workflow）
| 想做什么 | 命令 |
|---------|------|
| 同步到 single + workflow | `/sync-branches` |
| 仅同步到 single | `/sync-branches single` |
| 仅同步到 workflow | `/sync-branches workflow` |
| 查看同步历史 | `/sync-branches history` |

### 项目管理
| 想做什么 | 命令 |
|---------|------|
| 查看完整进度报告 | `/progress` |
| 更新项目状态文档 | `/update-status` |
| 同步三个文档一致性 | `/sync`（文档同步） |
| 添加待办事项 | `/add-todo` |

### 技能系统维护
| 想做什么 | 命令/关键词 |
|---------|------------|
| 添加新技能 | 说"添加技能" → 触发 add-skill |
| 查看技能路线图 | 读取 `.claude/SKILLS_ROADMAP.md` |

## 🗺️ 典型工作流

### 收到上游更新时
```
/sync-local → 开发/测试 → 提交 → /sync-branches
```

### 开发新功能时
```
brainstorm（讨论方案） → /init-docs（初始化文档） → /dev（开发） → /check（检查） → 提交
```

### 版本发布前
```
/progress（查看进度） → /check（全量检查） → /sync-branches（同步分支） → 推送
```

## 💬 快速开始
- "同步上游最新代码"
- "把 master 的改动同步到 single 和 workflow"
- "开发一个新的业务模块"
- "检查代码规范"
```

---

### 环境 B：子项目（使用者环境）

> 检测到 `.framework-sync.json`，识别为基于框架创建的子项目。

读取 `.framework-sync.json` 获取：
- `upstream.branch`：基于哪个框架分支
- `identifierMap.new_id`：项目标识符
- `lastSync`：上次框架同步时间
- `retainedProjects`：保留的前端/移动端项目

```markdown
# 👋 欢迎回到 {项目标识符} 项目

## 📂 项目信息
- **项目标识符**：{new_id}
- **技术栈**：Spring Boot + Vue3 + UniApp
- **基于框架分支**：{upstream.branch}
- **上次框架同步**：{lastSync || "从未同步"}

## 🕐 最近开发动态
[过滤后的最近 3-5 条业务提交]

## 📊 当前状态
- **代码状态**：✅ 干净 / ⚠️ 有未提交修改
- **业务模块**：X 个
  - 🔹 ruoyi-{模块}: Y 个 TODO
- **活跃任务**：[从 docs/tasks/active/ 读取]

## 📋 待办概览
{如果 docs/待办清单.md 存在}
- 高优先级：X 项
- 中优先级：X 项
- 低优先级：X 项

{如果不存在}
未找到待办清单（运行 `/init-docs` 创建）

## 🔧 日常开发
| 想做什么 | 命令 |
|---------|------|
| 获取下一步建议 | `/next`（推荐） |
| 开发新功能 | `/dev` |
| 从现有表生成 CRUD | `/crud` |
| 检查代码规范 | `/check` |
| 同步框架最新更新 | `/framework-sync` |

## 🗺️ 典型工作流

### 开发新功能
```
brainstorm（讨论方案） → /init-docs → /dev → /check → 提交
```

### 同步框架更新
```
/framework-sync → 检查冲突 → 编译测试 → 提交
```

### 项目管理
```
/progress（查看进度） → /next（下一步） → /update-status（更新文档）
```

## 💬 快速开始
- "我想开发一个用户反馈功能"
- "帮我同步框架最新更新"
- "检查代码是否符合规范"
- "查看项目进度"
```

**如果有业务模块但没有文档**，追加：

```markdown
## ⚠️ 建议
检测到业务代码，但尚未初始化项目文档。运行 `/init-docs` 自动生成：
- 项目状态文档（进度统计）
- 需求文档（需求管理）
- 待办清单（TODO 跟踪）
```

---

### 环境 C：全新项目（业务模块数 = 0，无 .framework-sync.json）

```markdown
# 👋 欢迎使用 RuoYi-Plus-Uniapp 框架

## 🎯 这是一个全新项目

**框架状态**: ✅ 已就绪
- 系统管理功能：29 个（用户、角色、菜单、字典等）
- 代码生成器：已配置
- 通用工具库：36 个模块
- 前端框架：PC端（plus-ui）+ 移动端（plus-uniapp / plus-app）

**业务模块**: ⏳ 尚未开发

## 🚀 从零开始的推荐路径

### 第 1 步：想清楚要做什么
```
brainstorm（头脑风暴，讨论方案）
```
告诉我你要开发什么业务系统，我帮你分析方案、评估复用度。

### 第 2 步：初始化项目文档
```
/init-docs {你的功能描述}
```
自动生成项目状态、需求文档、待办清单。

### 第 3 步：开始开发
```
/dev    — 完整功能开发（设计表 → 生成代码 → 前端页面）
/crud   — 已有数据库表，快速生成 CRUD
```

### 第 4 步：检查和迭代
```
/check  — 代码规范检查
/next   — 获取下一步开发建议
```

## 📖 所有可用命令

### 开发类
| 命令 | 用途 |
|------|------|
| `/dev` | 完整功能开发（双模式：全自动/半自动） |
| `/crud` | 从现有表快速生成 CRUD 代码 |
| `/check` | 全栈代码规范检查（后端+前端+移动端） |

### 项目管理类
| 命令 | 用途 |
|------|------|
| `/next` | 智能分析，给出下一步开发建议 |
| `/progress` | 完整项目进度报告 |
| `/update-status` | 更新项目状态文档 |
| `/add-todo` | 快速添加待办事项 |
| `/init-docs` | 初始化项目文档 |
| `/sync` | 同步三个文档一致性 |

### 47+ 智能技能（自动触发）
- 说"头脑风暴" → 方案探索（brainstorm）
- 说"数据库设计" → 建表指导（database-ops）
- 说"接入微信支付" → 支付集成（payment-integration）
- 说"添加数据权限" → 权限配置（data-permission）
- 更多技能在开发过程中按需自动激活

## 💡 提示
- 框架代码已完成，**只需要开发业务功能**
- 业务模块位置：`ruoyi-modules/ruoyi-{你的业务}/`
- 前端页面位置：`plus-ui/src/views/business/`
- 移动端页面位置：`plus-uniapp/src/pages-sub/business/`

## 💬 快速开始
- "我要开发一个电商系统"
- "帮我设计一个优惠券管理功能"
- "我有现成的数据库表，帮我生成代码"
```

---

### 环境 D：开发中项目（业务模块数 > 0，无 .framework-sync.json）

```markdown
# 👋 欢迎回到项目

## 📂 项目信息
- **项目名称**：[从 application.yml 的 app.title 读取]
- **技术栈**：Spring Boot + Vue3 + UniApp

## 🕐 最近开发动态
[过滤后的最近 3-5 条业务提交]

## 📊 当前状态
- **代码状态**：✅ 干净 / ⚠️ 有未提交修改
- **业务模块**：X 个
  - 🔹 ruoyi-{模块}: Y 个 TODO
- **活跃任务**：[从 docs/tasks/active/ 读取]

## 📋 待办概览
{如果 docs/待办清单.md 存在}
- 高优先级：X 项
- 中优先级：X 项
- 低优先级：X 项

{如果不存在}
未找到待办清单（运行 `/init-docs` 创建）

## 🎯 你可以：
| 想做什么 | 命令 |
|---------|------|
| 获取下一步建议 | `/next`（推荐） |
| 查看详细进度报告 | `/progress` |
| 继续开发新功能 | `/dev` |
| 从现有表生成 CRUD | `/crud` |
| 检查代码规范 | `/check` |
| 更新项目文档 | `/update-status` |

## 💬 快速开始
- "继续开发 [功能名] 功能"
- "查看项目进度"
- "我想实现 [新功能]"
- 直接告诉我你想做什么
```

**如果有业务模块但没有文档**，追加：

```markdown
## ⚠️ 建议
检测到业务代码，但尚未初始化项目文档。运行 `/init-docs` 自动生成：
- 项目状态文档（进度统计）
- 需求文档（需求管理）
- 待办清单（TODO 跟踪）
```

---

## 注意事项

- 输出要简洁，一屏内能看完（环境 A 可以稍长，因为维护者需要完整的命令地图）
- 明确区分框架（已完成）和业务（需要开发）
- 框架维护者环境重点展示同步命令和技能维护
- 子项目环境重点展示框架同步和日常开发
- 全新项目重点引导从零开始的路径
- 开发中项目重点显示业务进度和下一步
- 不要输出框架模块的 TODO，只关注业务模块
- 语气要友好、轻松
