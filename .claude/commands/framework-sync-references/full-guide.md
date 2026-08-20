
# 框架同步指南（Framework Sync）

## 概述

本技能用于将 ruoyi-plus-uniapp 框架仓库的更新同步到基于它创建的子项目中。子项目通过 `project-init` 创建后，框架会持续迭代更新，本技能提供选择性合并的完整流程。

**核心理念**：
- **选择性合并**：不是所有更新都需要，逐个分析后用户确认
- **标识符自动替换**：合并时自动将框架标识符替换为子项目标识符
- **历史可追溯**：每次同步都记录到 `.framework-sync.json`
- **多分支支持**：master（多租户）/ single（单租户）/ workflow（工作流）

**与其他技能的关系**：

| 技能 | 方向 | 场景 |
|------|------|------|
| `project-init` | 框架 → 新项目 | 首次创建子项目 |
| **`framework-sync`**（本技能） | **框架 → 已有子项目** | **持续同步框架更新** |
| `sync-local`（命令） | 上游 ruoyi-vue-plus → 本框架 | 框架自身同步上游 |
| `project-migration` | 其他项目 → 本框架 | 迁移外部项目 |

---

## 前置条件

### 1. upstream remote 已配置

子项目中必须配置 `upstream` remote 指向框架仓库：

```bash
# 检查是否已配置
git remote -v

# 如果没有 upstream，手动添加
git remote add upstream <框架仓库地址>

# 示例
git remote add upstream https://gitcode.com/yechaoa/ruoyi-plus-uniapp.git
# 或本地路径
git remote add upstream /path/to/ruoyi-plus-uniapp
```

### 2. `.framework-sync.json` 配置文件

该文件在 `project-init` 时自动生成，记录同步元数据。如果不存在，首次执行 `/framework-sync` 时交互式创建。

**文件结构**：

```json
{
  "upstream": {
    "url": "https://gitcode.com/yechaoa/ruoyi-plus-uniapp.git",
    "branch": "master",
    "initCommit": "5316b605e",
    "initDate": "2026-03-16"
  },
  "identifierMap": {
    "old_id": "ryplus_uni",
    "new_id": "mall_shop",
    "old_hyphen": "ryplus-uni",
    "new_hyphen": "mall-shop",
    "old_port": "5500",
    "new_port": "5501",
    "old_title": "ryplus-uni后台管理",
    "new_title": "mall-shop后台管理"
  },
  "retainedProjects": ["plus-ui", "plus-uniapp"],
  "lastSync": null,
  "lastCommit": "5316b605e",
  "syncs": []
}
```

**字段说明**：

| 字段 | 说明 |
|------|------|
| `upstream.url` | 框架仓库地址 |
| `upstream.branch` | 基于哪个分支创建（master/single/workflow） |
| `upstream.initCommit` | 创建项目时框架的 commit hash |
| `identifierMap` | 标识符映射表（同步时自动替换） |
| `retainedProjects` | 保留的子项目列表（未保留的自动跳过） |
| `lastSync` | 上次同步时间 |
| `lastCommit` | 上次同步到的 commit hash |
| `syncs` | 同步历史记录 |

### 3. 各分支的标识符映射

| 属性 | master | single | workflow |
|------|--------|--------|----------|
| **旧标识符** | `ryplus_uni` | `ryplus_uni_single` | `ryplus_uni_workflow` |
| **旧连字符** | `ryplus-uni` | `ryplus-uni-single` | `ryplus-uni-workflow` |
| **旧端口** | 5500 | 5504 | 5503 |

---

## 执行流程

### 第零步：环境检查

```bash
# 1. 检查 upstream remote
git remote -v | grep upstream

# 2. 检查 .framework-sync.json
cat .framework-sync.json
```

**如果 upstream remote 不存在**：

不要直接报错，而是**询问用户提供框架仓库地址**：

```markdown
未检测到 upstream remote。请提供框架仓库地址，我来帮你配置：

- 远程地址示例：`https://gitcode.com/yechaoa/ruoyi-plus-uniapp.git`
- 本地路径示例：`D:/projects/ruoyi-plus-uniapp`
```

用户提供地址后执行：
```bash
git remote add upstream <用户提供的地址>
git fetch upstream
```

**如果 `.framework-sync.json` 不存在**：

进入首次配置流程（见下方"首次配置流程"章节），交互式收集信息并生成配置文件。

**环境就绪后**：

```bash
# 拉取 upstream 最新信息
git fetch upstream <branch>
```

---

### 第一步：提交数量检测（智能分批）

```bash
# 读取上次同步的 commit
LAST_COMMIT=$(cat .framework-sync.json | grep -oP '"lastCommit":\s*"([^"]+)"' | grep -oP '(?<=")[^"]+$')
BRANCH=$(cat .framework-sync.json | grep -oP '"branch":\s*"([^"]+)"' | grep -oP '(?<=")[^"]+$')

# 统计待同步的提交数量
git log ${LAST_COMMIT}..upstream/${BRANCH} --oneline | wc -l

# 查看提交列表
git log ${LAST_COMMIT}..upstream/${BRANCH} --oneline --date-order
```

**根据数量决定策略**：

| 总提交数量 | 策略 | 说明 |
|-----------|------|------|
| 0 | 无更新 | 提示"框架已是最新" |
| 1-30 | 一次性处理 | 正常流程 |
| 31-100 | 分批处理 | 按时间段分批，每批 ≤ 30 条 |
| > 100 | 重点筛选 | 只看 fix/feat/update，跳过 docs/chore |

**输出提交数量摘要**：

```markdown
## 框架更新检测

从上次同步（{lastCommit}）到最新，共有 **X** 个新提交。

请选择处理方式：
1. **一次性处理**（推荐，≤ 30 条时）
2. **分批处理** — 按时间段分批
3. **只看重点** — 只显示 fix/feat/update 类型
4. **按模块筛选** — 只看特定目录的变更
5. **指定范围** — 输入起始和结束 commit
```

---

### 第二步：智能分类

对待同步的提交进行自动分类：

```bash
# 查看每个提交影响的文件
git log ${LAST_COMMIT}..upstream/${BRANCH} --oneline --stat
```

**分类规则**：

#### 建议合并（框架核心）

| 文件路径 | 分类 | 说明 |
|---------|------|------|
| `ruoyi-common/*` | 框架核心 | 工具类、通用模块 |
| `ruoyi-modules/ruoyi-system/*` | 系统模块 | 系统管理功能 |
| `ruoyi-admin/*` | 启动模块 | 启动配置、依赖管理 |
| `ruoyi-extend/*` | 扩展模块 | 可选功能模块 |
| `plus-ui/src/components/*` | 前端框架 | 封装组件 |
| `plus-ui/src/utils/*` | 前端工具 | 工具函数 |
| `plus-ui/src/composables/*` | 前端组合式 | Composables |
| `plus-uniapp/src/wd/*` | 移动端框架 | WD UI 封装 |
| `plus-uniapp/src/composables/*` | 移动端组合式 | Composables |
| `plus-app/wd/*` | APP框架 | WD UI 封装（APP端） |
| `.claude/skills/*` | 技能文档 | AI 辅助技能 |
| `.claude/commands/*` | 命令文档 | AI 辅助命令 |
| `pom.xml` | 依赖管理 | Maven 依赖 |
| `script/*` | 部署脚本 | Docker、SQL 等 |

#### 建议跳过

| 文件路径 | 分类 | 跳过原因 |
|---------|------|---------|
| `ruoyi-modules/ruoyi-business/*` | 业务示例 | 用户有自己的业务代码 |
| `plus-ui/src/views/business/*` | 业务页面 | 用户有自己的业务页面 |
| `plus-uniapp/src/pages-sub/business/*` | 移动端业务 | 用户有自己的业务页面 |
| 用户未保留的子项目目录 | 未使用 | `retainedProjects` 中不包含 |

#### 需要评估

| 文件路径 | 分类 | 评估原因 |
|---------|------|---------|
| `*/env/.env*` | 环境配置 | 可能有用户定制 |
| `application*.yml` | 后端配置 | 可能有用户定制 |
| `CLAUDE.md` / `AGENTS.md` | 项目文档 | 可能有用户修改 |
| `README.md` | 说明文档 | 用户可能已修改 |

**输出分类结果**：

```markdown
## 提交分类分析

### 建议合并（共 X 个）

| 提交ID | 说明 | 涉及文件 | 分类 |
|--------|------|---------|------|
| abc123 | fix: 修复 RedisUtils 序列化问题 | 2 files | 框架核心 |
| def456 | feat: 新增 usePayment composable | 3 files | 移动端框架 |

### 建议跳过（共 Y 个）

| 提交ID | 说明 | 跳过原因 |
|--------|------|---------|
| ghi789 | feat: 广告管理页面优化 | 业务示例代码 |

### 需要评估（共 Z 个）

| 提交ID | 说明 | 评估原因 |
|--------|------|---------|
| jkl012 | update: 更新 Spring Boot 版本 | 依赖升级需确认兼容性 |

---

请选择要合并的提交：
- 输入 "all" — 合并所有"建议合并"的提交
- 输入 "abc123,def456" — 选择特定提交
- 输入 "skip" — 跳过本次同步
```

---

### 第三步：逐个分析选中的提交

对用户选中的每个提交，进行详细分析：

```bash
# 查看提交详情
git show <commit_id> --stat

# 查看具体改动
git diff <commit_id>~1 <commit_id> -- <相关文件>
```

**输出分析结果**：

```markdown
## 提交分析：abc123

### 提交信息
- **类型**：修复
- **说明**：fix: 修复 RedisUtils 序列化问题
- **日期**：2026-03-15

### 涉及文件
| 文件路径 | 操作 | 行数变化 |
|---------|------|---------|
| ruoyi-common/ruoyi-common-redis/src/.../RedisUtils.java | 修改 | +5 -2 |

### 改动内容摘要
修复了 RedisUtils 在处理复杂泛型时的序列化异常...

### 本项目对应文件
- 框架：`ruoyi-common/ruoyi-common-redis/src/.../RedisUtils.java`
- 本项目：`ruoyi-common/ruoyi-common-redis/src/.../RedisUtils.java`（路径相同）

### 标识符替换预览
此文件不包含项目标识符，无需替换。

### 合并建议
- [x] 直接合并（路径相同，无需适配）
- [ ] 替换后合并（需要标识符替换）
- [ ] 跳过

---

是否合并此提交？（合并 / 跳过 / 详细查看）
```

---

### 第四步：执行合并

#### 4.1 路径相同的文件（大部分情况）

由于子项目与框架结构相同，大部分文件路径一致：

```bash
# 查看框架版本的文件内容
git show upstream/<branch>:<文件路径>

# 对比当前文件与框架版本的差异
git diff HEAD upstream/<branch> -- <文件路径>
```

使用 Edit 工具应用具体的改动行，而不是整个文件覆盖（用户可能在同一文件中有自己的修改）。

#### 4.2 需要标识符替换的文件

对于包含项目标识符的文件（如配置文件），合并时需要自动替换：

**替换规则**（从 `identifierMap` 读取）：

```
框架中的值              →  子项目中的值
─────────────────────────────────────
ryplus_uni              →  mall_shop（标识符）
ryplus-uni              →  mall-shop（连字符）
ryplus-uni后台管理       →  mall-shop后台管理（标题）
5500                    →  5501（端口，仅在特定文件中）
```

**替换顺序**（先长后短，防止部分匹配）：
1. 先替换带后缀的完整字符串（如 `ryplus-uni后台管理`）
2. 再替换连字符版（如 `ryplus-uni`）
3. 最后替换下划线版（如 `ryplus_uni`）

**端口替换仅限以下文件**：
- `application.yml` / `application-dev.yml`
- `*/env/.env.development`
- Docker compose 文件

#### 4.3 冲突处理

当同一文件在框架和子项目中都有修改时：

```markdown
## ⚠️ 冲突检测

文件 `ruoyi-common/.../SomeUtil.java` 存在冲突：
- **框架修改**：第 42 行，修复空指针异常
- **本项目修改**：第 45 行，添加了自定义方法

处理方式：
1. **框架优先** — 应用框架的改动，保留本项目的其他修改
2. **本项目优先** — 跳过此文件的框架更新
3. **手动合并** — 展示两端差异，由用户决定

请选择：
```

---

### 第五步：记录同步历史

每次同步完成后，更新 `.framework-sync.json`：

```json
{
  "lastSync": "2026-03-16T14:30:00+08:00",
  "lastCommit": "新的commit_hash",
  "syncs": [
    {
      "date": "2026-03-16T14:30:00+08:00",
      "fromCommit": "旧commit_hash",
      "toCommit": "新commit_hash",
      "totalCommits": 15,
      "merged": [
        {
          "commit": "abc123",
          "message": "fix: 修复 RedisUtils 序列化问题",
          "files": ["ruoyi-common/.../RedisUtils.java"],
          "replaced": false
        },
        {
          "commit": "def456",
          "message": "update: 更新 application.yml 配置",
          "files": ["ruoyi-admin/.../application.yml"],
          "replaced": true,
          "note": "标识符已自动替换"
        }
      ],
      "skipped": [
        {
          "commit": "ghi789",
          "message": "feat: 广告管理页面优化",
          "reason": "业务示例代码"
        }
      ]
    }
  ]
}
```

---

### 第六步：输出同步报告

```markdown
## 框架同步完成报告

**同步时间**：2026-03-16 14:30
**框架分支**：master
**提交范围**：abc123 → xyz789

### 统计
- 总提交数：15
- 已合并：10
- 已跳过：5

### 已合并的提交

| 提交ID | 说明 | 文件数 | 标识符替换 |
|--------|------|--------|-----------|
| abc123 | fix: 修复 RedisUtils 序列化 | 1 | 否 |
| def456 | feat: 新增 usePayment | 3 | 否 |
| mno345 | update: application.yml | 1 | 是 |

### 已跳过的提交

| 提交ID | 说明 | 跳过原因 |
|--------|------|---------|
| ghi789 | feat: 广告管理优化 | 业务示例代码 |

### 后续操作
1. 编译后端项目，检查是否有编译错误
2. 运行前端/移动端，确认功能正常
3. 如有问题，可使用 `git log` 查看最近的提交并回滚

### 下次同步起始点
- commit: xyz789
- 日期: 2026-03-16
```

---

## 首次配置流程

当 `.framework-sync.json` 不存在时，交互式创建。**逐步询问用户，不要一次性列出所有问题**：

### 步骤 1：确认框架仓库地址

```markdown
# 检查 upstream remote 是否已配置
```

- **如果已配置**：展示当前地址，询问是否正确
  ```
  检测到 upstream remote 已配置为：`https://gitcode.com/xxx/ruoyi-plus-uniapp.git`
  这个地址正确吗？如需修改请提供新地址。
  ```
- **如果未配置**：询问用户提供地址
  ```
  未检测到 upstream remote，请提供框架仓库地址：
  - 远程地址示例：https://gitcode.com/yechaoa/ruoyi-plus-uniapp.git
  - 本地路径示例：D:/projects/ruoyi-plus-uniapp
  ```
  用户提供后执行 `git remote add upstream <地址>`。

### 步骤 2：确认基于哪个分支

**先尝试自动检测**（从项目代码中推断）：

```bash
# 检查 application.yml 中的标识符来推断分支
grep "id:" ruoyi-admin/src/main/resources/application.yml | head -1
# ryplus_uni → master
# ryplus_uni_single → single
# ryplus_uni_workflow → workflow
# 其他值（如 mall_shop）→ 已替换，需要进一步检测
```

- **如果能自动推断**：向用户确认推断结果
  ```
  根据项目配置，推断您基于 **master（多租户）** 分支创建。请确认是否正确？
  ```
- **如果无法推断**：询问用户
  ```
  无法自动检测基于哪个分支创建，请选择：
  - master（多租户）
  - single（单租户）
  - workflow（工作流）

  如果不确定，可以告诉我项目的业务场景，我帮你判断。
  ```

### 步骤 3：确认项目标识符

```bash
# 从 application.yml 中读取当前标识符
grep "id:" ruoyi-admin/src/main/resources/application.yml | head -1
```

向用户确认读取到的标识符是否正确。

### 步骤 4：确认创建时的 commit

```bash
# 尝试从子项目的第一条提交信息推断
git log --reverse --oneline | head -1
# 输出类似: init: 基于 ruoyi-plus-uniapp 框架初始化 mall-shop后台管理

# 或从 upstream 中查找对应时间点的 commit
git log upstream/<branch> --before="<子项目创建时间>" --oneline -1
```

- **如果能推断**：向用户确认
- **如果无法推断**：询问用户提供 commit hash，或输入 "auto" 使用 upstream 分支的最早可用 commit

### 步骤 5：确认保留的子项目

```bash
# 检查项目中实际存在哪些子项目目录
ls -d plus-ui plus-uniapp plus-app 2>/dev/null
```

根据实际存在的目录自动勾选，向用户确认。

### 步骤 6：生成配置文件

收集完所有信息后，生成 `.framework-sync.json` 并向用户展示内容。

---

## 常见场景

### 场景 1：框架修复了一个 Bug

```
框架提交: fix: 修复 SaTokenDao Redis 序列化问题
涉及文件: ruoyi-common/ruoyi-common-satoken/src/.../SaTokenDao.java

→ 分类: 框架核心，建议合并
→ 路径相同，直接应用改动
→ 无标识符替换
```

### 场景 2：框架升级了依赖版本

```
框架提交: update: Spring Boot 3.5.8 → 3.6.0
涉及文件: pom.xml, ruoyi-admin/pom.xml

→ 分类: 需要评估
→ 需要确认子项目的其他依赖是否兼容
→ 可能需要同步更新子项目的自定义依赖
```

### 场景 3：框架新增了配置项

```
框架提交: feat: 新增 OSS 预签名 URL 配置
涉及文件: application.yml, application-dev.yml

→ 分类: 需要评估
→ 配置文件中包含标识符，需要替换
→ 用户可能已修改同一文件，需要检查冲突
```

### 场景 4：框架更新了移动端组件

```
框架提交: fix: 修复 wd-button 组件样式问题
涉及文件: plus-uniapp/src/wd/components/wd-button/wd-button.vue

→ 检查 retainedProjects 是否包含 plus-uniapp
→ 如果包含：建议合并
→ 如果不包含：自动跳过
```

### 场景 5：框架更新了业务示例代码

```
框架提交: feat: 广告管理新增排序功能
涉及文件: ruoyi-modules/ruoyi-business/src/.../AdController.java

→ 分类: 业务示例，建议跳过
→ 原因: 用户有自己的业务代码，框架的示例代码仅供参考
```

### 场景 6：框架更新了 Skills

```
框架提交: update: 更新 crud-development 技能
涉及文件: .claude/skills/crud-development/SKILL.md

→ 分类: 技能文档，建议合并
→ 保持 AI 辅助能力与框架同步
```

---

## 注意事项

### 1. 不要使用 git merge 或 cherry-pick

子项目与框架虽然结构相同，但标识符不同，直接 `git merge` 或 `cherry-pick` 会导致大量冲突。应该：
- 使用 `git show` 查看框架的改动内容
- 使用 Edit 工具手动应用到子项目对应文件
- 涉及标识符的部分自动替换

### 2. 配置文件特殊处理

以下文件在合并时需要特别注意，因为用户通常会修改它们：

| 文件 | 注意事项 |
|------|---------|
| `application.yml` | 端口、数据库名、应用标题 |
| `application-dev.yml` | 数据库连接、Redis 连接 |
| `*/env/.env*` | API 地址、应用标识 |
| `pom.xml` | 用户可能添加了自定义依赖 |

**策略**：对这些文件，只合并新增的配置项，不覆盖用户的定制。

### 3. SQL 文件的合并

框架的 SQL 文件更新通常是新增表或新增数据：
- **新增表**：直接追加到子项目的 SQL 文件
- **修改表结构**：生成 ALTER TABLE 语句
- **新增字典数据**：追加 INSERT 语句

### 4. 同步优先级

| 优先级 | 类型 | 说明 |
|--------|------|------|
| 最高 | 安全修复 | CVE 漏洞、认证绕过等 |
| 高 | Bug 修复 | 功能异常、数据错误 |
| 中 | 性能优化 | 查询优化、缓存改进 |
| 中 | 依赖升级 | 框架版本更新 |
| 低 | 功能增强 | 新工具类、新组件 |
| 最低 | 文档更新 | README、注释 |

### 5. 同步频率建议

| 频率 | 适用场景 |
|------|---------|
| 每周 | 活跃开发期，框架更新频繁 |
| 每月 | 稳定维护期 |
| 按需 | 框架发布重要修复时 |

---

## 常见错误

### ❌ 错误 1：直接 git merge upstream/master

```bash
# ❌ 错误：会导致大量标识符冲突
git merge upstream/master

# ✅ 正确：使用 /framework-sync 命令逐个分析
```

### ❌ 错误 2：忘记检查 retainedProjects

```bash
# ❌ 错误：合并了 plus-app 的改动，但子项目没有 plus-app
# 应该先检查 .framework-sync.json 中的 retainedProjects

# ✅ 正确：跳过未保留子项目的所有改动
```

### ❌ 错误 3：覆盖用户的配置文件

```bash
# ❌ 错误：直接用框架的 application.yml 覆盖
git show upstream/master:ruoyi-admin/.../application.yml > local-file

# ✅ 正确：对比差异，只合并新增的配置项
```

### ❌ 错误 4：不记录同步历史

```bash
# ❌ 错误：合并后不更新 .framework-sync.json
# 下次同步时无法知道上次同步到哪里

# ✅ 正确：每次同步后更新 lastCommit 和 syncs 记录
```

### ❌ 错误 5：同步时不做标识符替换

```bash
# ❌ 错误：配置文件中保留了框架的标识符 ryplus_uni
# 导致数据库名、Redis 前缀等与子项目不一致

# ✅ 正确：合并含标识符的文件时自动替换
```

---

## 与 project-init 的配合

`project-init` 在创建子项目时，应在阶段三（Git 提交 & 推送）之后增加以下步骤：

### 自动配置 upstream remote

```bash
cd "$NEW_DIR"

# 添加框架仓库作为 upstream（使用模板目录的路径或远程地址）
git remote add upstream "$TEMPLATE_DIR"
# 或使用远程地址
# git remote add upstream https://gitcode.com/yechaoa/ruoyi-plus-uniapp.git
```

### 自动生成 `.framework-sync.json`

```bash
# 获取当前 commit hash
INIT_COMMIT=$(cd "$TEMPLATE_DIR" && git rev-parse HEAD)
INIT_DATE=$(TZ=Asia/Shanghai date '+%Y-%m-%dT%H:%M:%S+08:00')
```

生成配置文件，包含：
- upstream URL 和分支
- 标识符映射表（旧值 → 新值）
- 保留的子项目列表
- initCommit（创建时框架的 commit）

---

## 快速命令参考

```bash
# 检查框架是否有更新
git fetch upstream && git log $(cat .framework-sync.json | grep -oP '"lastCommit":\s*"([^"]+)"' | grep -oP '[^"]+$')..upstream/master --oneline

# 查看同步历史
cat .framework-sync.json | python -m json.tool

# 重置同步起始点（慎用）
# 修改 .framework-sync.json 中的 lastCommit

# 查看框架特定文件的改动
git diff HEAD upstream/master -- ruoyi-common/
```

---

## 常见问题

### Q1: 子项目已经修改了框架的代码怎么办？

**A:** 对于用户修改过的文件，同步时会检测冲突。用户可以选择：
- 框架优先：应用框架改动，手动合并自己的修改
- 本项目优先：跳过此文件
- 手动合并：查看两端差异后决定

### Q2: 框架新增了一个 common 模块怎么办？

**A:** 新增的模块目录在框架中是新文件，使用 `git show` 导出整个目录，然后复制到子项目。由于是新文件，不会有冲突。

### Q3: 可以只同步后端/只同步前端吗？

**A:** 可以。在第二步分类时，使用模块筛选：
```bash
# 只看后端变更
git log ${LAST_COMMIT}..upstream/${BRANCH} --oneline -- "ruoyi-*/"

# 只看前端变更
git log ${LAST_COMMIT}..upstream/${BRANCH} --oneline -- "plus-ui/"

# 只看移动端变更
git log ${LAST_COMMIT}..upstream/${BRANCH} --oneline -- "plus-uniapp/" "plus-app/"
```

### Q4: upstream 是本地路径还是远程地址？

**A:** 都可以。本地路径更快（无需网络），远程地址更灵活（可在任何机器上同步）。

### Q5: 同步后发现问题如何回滚？

**A:** 每次同步的改动都通过独立的 Edit 操作应用，可以通过 `git log` 查看变更历史。如果同步前有提交，可以 `git diff` 查看同步引入的所有变更。建议在同步前先提交当前工作，同步完成后再做一次提交，这样可以整体回滚。
