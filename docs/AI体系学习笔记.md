# AI 体系学习笔记（Claude Code 治理体系）

> **最后更新**：2026-08-14 07:38（UTC+8）
> **用途**：系统性学习本项目的「AI 行为治理体系」与「项目开发体系」的学习笔记与进度台账
> **如何继续学习**：下次会话说「读 docs/AI体系学习笔记.md，继续学习」，AI 会从此文恢复上下文

---

## 〇、学习进度总台账

### 主线一：AI 治理体系（✅ 第一轮已学完，见本文第二、三章）

| 章节 | 状态 |
|------|------|
| 第 1 层 CLAUDE.md | ✅ 已学 |
| 第 2 层 Hooks（含 2 个 hook 逐行精读） | ✅ 已学 |
| 第 3 层 Skills | ✅ 已学 |
| 第 4 层 Agents（子代理） | ✅ 已学 |
| 第 5 层 Memory | ✅ 已学 |
| 第 6 层 Commands | ✅ 已学 |
| 文档体系（README/CLAUDE.md/AGENTS.md） | ✅ 已学 |

### 主线二：项目开发学习（6 阶段路线）

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段 0 | 全局认知：三端一后端结构、术语、文档体系 | ✅ 已完成 |
| 阶段 1 | 把项目跑起来（环境已手动装好，待验证+启动） | ⬜ 待进行 |
| 阶段 2 | 后端四层架构精读（Ad 模块参考代码） | ⬜ 待进行 |
| 阶段 3 | PC 前端 plus-ui（ad.vue + A* 组件） | ⬜ 待进行 |
| 阶段 4 | 移动端 plus-uniapp（Home.vue/login.vue + WD UI） | ⬜ 待进行 |
| 阶段 5 | 横切机制逐个深入（Sa-Token/多租户/数据权限/Redis 等） | ⬜ 待进行 |
| 阶段 6 | 动手实战（/crud 生成模块、/check 自检、E2E 验收） | ⬜ 待进行 |

### 主线三：体系实践清单

| 事项 | 状态 |
|------|------|
| 写一个自定义 hook 实战 | ⬜ 待进行 |
| add-agent 规范空白（扩展 add-skill 或新建技能） | ⬜ 可选讨论 |

---

## 一、项目学习 6 阶段详细路线

### 阶段 0：建立全局认知（已完成 ✅）

- 「三端一后端」：plus-ui（PC 管理端，Vue3+ElementPlus）/ plus-uniapp（移动端，UniApp+WD UI，有 src/）/ plus-app（原生 APP，扁平无 src/）→ 都调 ruoyi-admin（Spring Boot 3.5.8，Java 21）
- 后端：ruoyi-modules（ruoyi-system / ruoyi-business / ruoyi-generator）+ ruoyi-common（20+ 子模块）
- 数据库：MySQL 8 + Redis（+可选 RocketMQ/OSS）

### 阶段 1：把项目跑起来

- 环境要求（用户已手动配置）：JDK 21 / Maven 3.8+ / Node 22（nvm 管理）/ pnpm 10 / MySQL 8 / Redis 7
- 数据库初始化：建库 `ry_plus_uni` → 按序导入 `script/sql/ry_plus_sys.sql`、`ry_plus_app.sql`、`ry_plus_new.sql` → 改 `ruoyi-admin/src/main/resources/application-dev.yml`（密码、只启用一个 datasource）
- 后端启动坑：**不能** `mvn -pl ruoyi-admin -am spring-boot:run`（报 No plugin found）；必须分两步：`mvn install -DskipTests` 再 `mvn spring-boot:run -pl ruoyi-admin -DskipTests`
- 前端装依赖坑：`ERR_PNPM_META_FETCH_FAIL` 时用 `pnpm install --network-concurrency 8`
- 健康检查白名单：`GET /actuator/health`、`GET /auth/imgCode`（Windows 代理环境加 `--noproxy '*'`，否则 502）
- 端口：后端看 `application.yml` 的 server.port（dev-startup 技能记录默认 5500，本分支可能调整）；前端 80 顺延；H5 5173

### 阶段 2：后端四层架构（⭐ 核心）

- 参考模块：`ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/base/` 的 Ad 模块
- 调用链：Controller → Service（**不继承**基类）→ DAO（`buildQueryWrapper()` 构建查询，**本项目独有**）→ Mapper（只继承 BaseMapper）
- 核心基类：`TenantEntity`（ruoyi-common-tenant）、`BaseDaoImpl`/`PlusLambdaQuery`/`PageResult`（ruoyi-common-mybatis）
- 对象转换统一 `MapstructUtils.convert()`；异常统一 `ServiceException.of()`
- 建表规范：雪花 ID（非自增）、tenant_id、审计字段、is_deleted

### 阶段 3-4：前端 / 移动端

- 前端参考：`plus-ui/src/views/business/base/ad/ad.vue` + `plus-ui/src/api/business/base/ad/adApi.ts`
- 移动端参考：`plus-uniapp/src/components/tabbar/Home.vue`（列表）、`src/pages/auth/login.vue`（表单）
- 强制封装组件：前端 A* 组件（禁 el-*）；移动端 wd-* 组件、`@/wd` 导入（禁 uni-*、禁 'wot-design-uni' 直导）
- API 调用统一 `const [err, data] = await api()`（禁 try-catch）

### 阶段 5：横切机制（每块对应一个技能，学到哪块激活哪块）

Sa-Token 登录认证（⭐⭐⭐⭐⭐）→ 多租户（⭐⭐⭐⭐⭐）→ 数据权限（⭐⭐⭐⭐）→ Redis 缓存（⭐⭐⭐⭐，注意 @Cacheable 不可变集合陷阱）→ 文件存储 → 定时任务/MQ/AI/IoT（按需）

### 阶段 6：实战

`/crud` 生成完整模块（建表 → 后端四层 → 前端 → 菜单）→ `/check` 规范自检 → `e2e-test-pc` 浏览器验收

---

## 二、AI 治理体系：六层架构详解（核心内容）

### 2.0 核心原理

**AI 模型没有内置任何项目规则，所有约束的本质只有两种手段：**
1. **上下文注入**（软约束）：在特定时机把规则文本塞进模型上下文，模型被训练为"遵循上下文指令"
2. **程序拦截**（硬约束）：hook 脚本在工具调用前返回 `block`，模型根本无法执行

### 2.1 六层总览

| 层 | 配置位置 | 加载时机 | 约束强度 | 回答的问题 |
|---|---------|---------|---------|-----------|
| 1. CLAUDE.md | 项目根 | 会话启动常驻 | 软（系统层声明） | 总规则是什么 |
| 2. Hooks | .claude/settings.json + .claude/hooks/*.cjs | 每轮/每次工具调用 | 🔒 硬（可 block） | 如何保证遵守 |
| 3. Skills | .claude/skills/<name>/SKILL.md | 摘要常驻，全文按需 | 软 | 细节知识在哪 |
| 4. Agents | .claude/agents/*.md | 派发时加载 | 软+工具白名单 | 谁去专项执行 |
| 5. Memory | 项目外 ~/.claude/projects/<slug>/memory/ | 索引常驻，按需召回 | 软 | 我们经历过什么 |
| 6. Commands | .claude/commands/*.md | 用户触发时 | 软（确定性流程） | 一键干什么 |

**一句话总纲**：CLAUDE.md 是宪法，Hooks 是执法系统，Skills 是法典分册，Agents 是专业公务员（配了权限），Memory 是组织的长期记忆，Commands 是标准操作程序。每一层都在补上一层的漏洞。

### 2.2 第 1 层：CLAUDE.md

**加载机制**（"强制读取"的真相）：
- 不是每轮读文件，而是**会话启动时**由 Claude Code CLI 程序一次性读入并注入系统提示（快照；改后需新会话或用 /memory 重载）
- 注入时加前缀 "IMPORTANT: These instructions OVERRIDE any default behavior and you MUST follow them"
- "强制" = 程序确定性注入 + OVERRIDE 声明 + 模型对系统指令的服从

**位置层级（多层并存、每层一个、同时生效）**：

| 层级 | 路径 | 作用域 | 入库 |
|------|------|--------|------|
| 用户级 | `~/.claude/CLAUDE.md` | 所有项目 | 否 |
| 项目级 | `./CLAUDE.md` | 本项目团队 | ✅ |
| 本地项目级 | `./CLAUDE.local.md` | 仅本人 | ❌ gitignore |
| 子目录级 | `./任意子目录/CLAUDE.md` | 读该目录文件时加载 | 视情况 |
| 企业级 | 组织推送 | 企业环境 | - |

- 支持 `@path/to/file.md` 导入语法拆分超长文档
- 层级间无机械覆盖关系，冲突时模型按"越具体越优先"裁决
- 本项目实际：只有根目录 1 个

### 2.3 第 2 层：Hooks（已逐行精读 ✅）

**概念**：Claude Code 在主流程的固定节点（生命周期事件）执行用户注册的外部程序，允许程序改变主流程走向。类比 Git hooks / Vue 生命周期。

**通用协议**：stdin 读 JSON（事件上下文）→ 脚本判断 → stdout 输出（纯文本=注入上下文；`{"decision":"block"}`=阻止；`{"continue":true,"systemMessage"}`=警告放行；exit 2 = 旧式 block）

**关键原则**：fail-open —— hook 失败/读不到输入时一律放行，宁可不拦不卡死会话。

**事件清单**：UserPromptSubmit（注入）、PreToolUse（可 block）、PostToolUse（注入）、Notification、Stop、SubagentStop、PreCompact、SessionStart/End

**本项目 3 个 hook**（配置在 `.claude/settings.json`）：

| Hook | 文件 | 作用 |
|------|------|------|
| UserPromptSubmit | skill-forced-eval.cjs | 每轮注入「强制技能激活流程」文本；内置跳过逻辑（恢复会话防死循环、斜杠命令不评估）；把技能激活率从 25% 提到 90%+；技能清单**硬编码**在脚本里 |
| PreToolUse (Bash\|Write) | pre-tool-use.cjs | 三级判决：block（9 条危险正则：>nul、rm -rf 危险路径、drop database、强推 main 等）/ warn（4 条：force push、npm publish 等，continue+systemMessage）/ pass（默认交权限系统）；设计哲学：宁漏勿误伤、block 时给逃生通道（"请手动在终端运行"） |
| Stop | stop.cjs | 回复结束时清理 Windows 误建的 nul 文件 |

### 2.4 第 3 层：Skills

**渐进式披露**：会话启动只注入每个技能的 name+description 摘要 → 每轮评估匹配 → Skill 工具调用时才加载 SKILL.md 全文（+ Base directory + ARGUMENTS）

**SKILL.md 结构**：
- YAML 头部（检索索引）：name（kebab-case 2-4 词）+ description（一句话 + 触发场景≥3 + 触发词≥5）
- 正文骨架：概述/核心原则 → 规则表格 → ✅/❌ 错误对比 → 代码示例 → 检查清单 → 真实踩坑记录 → AI 边界声明 → 关联技能边界
- 规模：小型 200-300 行 / 中型 400-600 / 大型 600+

**新增技能要登记 3 处 + 复制 1 处**：
1. `.claude/skills/<name>/SKILL.md`（主文件）
2. `.claude/hooks/skill-forced-eval.cjs` 清单加一行（给 Claude 的强制评估索引）
3. `AGENTS.md` 技能表格加一行（给 Codex 的匹配索引）
4. 复制到 `.agents/skills/<name>/SKILL.md`（Codex 镜像，内容完全相同）

**双系统镜像演进**：原镜像到 `.codex/skills/` → Codex 0.128 迁移到开放标准 `.agents/skills/` → 实测双写会导致技能重复注入（160 条含 75 组重复）→ 删除 `.codex/skills/` 只留 `.agents/skills/`

### 2.5 第 4 层：Agents（子代理）

**与 Skills 的本质区别**：Skill = 把知识注入当前会话（自己变聪明）；Agent = 派独立"员工"（独立上下文窗口，干完只交回结果）

**frontmatter 四件套**：
```yaml
name: code-reviewer        # ID
description: ...           # ⭐ 唯一决策入口（触发匹配靠它）
model: opus                # 指定模型
tools: Read, Grep, Glob    # 工具白名单 = 能力限制
```

**三个关键结论**（本会话实测验证）：
1. **调用是模型自主行为**，项目只做"注册 + description 引导"——hook 强制评估的是 skills，**不含 agents**，agent 激活率全靠 description 质量 + 模型自觉
2. **description 是唯一决策入口**：正文里的"自动触发场景"对调用决策**无效**（正文在调用后才可见）；commands 文件里也没有引用 agents
3. 新增只需新建 `.claude/agents/xxx.md`，**不用改 settings.json / hook / AGENTS.md**

**本项目 2 个 agent**：
- `code-reviewer`：tools 只有 Read/Grep/Glob（物理上无法改代码），审查清单写成可直接执行的 Grep 命令
- `project-manager`：tools 含 Write/Bash（需要写文档），管理 docs/ 下的项目状态/需求/待办文档

**双系统现状**：`.codex/agents/` 不存在（只有 config.toml + hooks.json + hooks/）→ 两个角色是 Claude-only；Codex 侧有完整 hooks 治理 + skills 镜像，但无角色。原因：协同模式是"Claude 调 Codex 当外援"（codex-plugin-cc / codex_bridge.py），Codex 不需要人格化角色。

**规范空白**：add-skill 不覆盖 agents（其声明/同步步骤均不适用于 agents）；如需规范化可选：扩展 add-skill 或新建 add-agent 技能。

### 2.6 第 5 层：Memory

- 位置：项目外 `~/.claude/projects/<project-slug>/memory/`（不入库）
- MEMORY.md 索引每次会话加载；具体记忆按需召回
- 4 类型：user（你是谁）/ feedback（纠正）/ project（项目动态）/ reference（外部资源）
- 与 CLAUDE.md 区别：团队共享静态规范（git）vs 个人私有动态积累（项目外）
- 本项目的平行设计：`.claude/docs/experience/*-exp-summary.md` 每次会话自动读最近一份 + /exp 技能——把记忆 git 化、团队化

### 2.7 第 6 层：Commands

- `.claude/commands/*.md`：用户显式触发（/dev、/crud、/check 等），全文作为 prompt 注入
- 与 skill 区别：command = 人按按钮（流程脚本）；skill = AI 自动查资料（知识文档）
- 特殊规则：`*-local` 命令不同步 Codex；同名 skill+command 在 Codex 镜像加 `cmd-` 前缀

---

## 三、文档体系：三种文档、三个受众（互不自动加载）

| 文档 | 受众 | 加载方式 |
|------|------|---------|
| README.md | **人** | GitHub 网页渲染；AI 不自动加载（本项目共 16 个） |
| CLAUDE.md | **Claude Code** | 会话启动自动注入 |
| AGENTS.md | **Codex**（及遵循 agents.md 开放标准的工具） | 其会话自动注入 |

**16 个 README.md 分类**：
- A 仓库门面：`./README.md`
- B 子项目/模块说明：plus-ui、ruoyi-common-message、ruoyi-common-mqtt、business/base（源码包内）
- C 资源/工具说明：icons/custom、icons/svg、vite/plugins/openapi（改造说明）
- D 运维/数据：docker/redis/data、docker/rocketmp/data（**占位 + chmod 指令**——git 不能提交空目录）、sql/update
- E 项目体系：.claude/docs、ci、delivery、local-repo

**AGENTS.md 详解**（2103 行，已通读）：
- = Codex 的"CLAUDE.md"：同一宪法、两份译本（规范本体一致）
- Codex 特化：开头声明用中文、Skills 技能系统整章（技能清单表格 + 强制执行规则 + `$skill-name` 触发方式）、"绝不主动生成文档"禁令、11 个 Ad 模块完整代码模板、vs ruoyi-vue-plus 对比表
- 为什么技能表格在 AGENTS.md：**Codex 没有 hook**，只能用文本规则 + 表格替代强制评估
- ⚠️ 双文档代价：改一份不影响另一份，规则变更必须双份同步

**技能索引的三份拷贝**：① 各 SKILL.md 的 description（源头）② hook 硬编码清单（Claude）③ AGENTS.md 表格（Codex）——新增技能三处必须同步。

---

## 四、关键路径速查

| 资产 | 路径 |
|------|------|
| Claude 规则 | `CLAUDE.md`（根）、`~/.claude/CLAUDE.md`（用户级） |
| Codex 规则 | `AGENTS.md`（根） |
| Hook 注册 | `.claude/settings.json` |
| Hook 脚本 | `.claude/hooks/`（skill-forced-eval.cjs / pre-tool-use.cjs / stop.cjs） |
| 技能（主） | `.claude/skills/<name>/SKILL.md`（29+ 个） |
| 技能（Codex 镜像） | `.agents/skills/<name>/SKILL.md` |
| 子代理 | `.claude/agents/*.md`（code-reviewer / project-manager） |
| 斜杠命令 | `.claude/commands/*.md` |
| 个人记忆 | `C:\Users\Administrator\.claude\projects\D--ruoyi-project-ruoyi\memory\` |
| Codex 配置 | `.codex/config.toml` + `.codex/hooks.json` + `.codex/hooks/`（**无 .codex/agents**） |
| 经验沉淀 | `.claude/docs/experience/YYYY-MM/*-exp-summary.md`（会话启动自动读最近一份） |
| 文档模板 | `.claude/templates/` |

---

## 五、下次继续学习清单

### 恢复方式
> 下次会话说：「读 docs/AI体系学习笔记.md，继续学习」→ AI 读本文档恢复进度，从下面第一个未完成项继续。

### 待进行（按建议顺序）
- [ ] **阶段 1**：验证环境版本 → 建库导 SQL → 改 application-dev.yml → 启动后端 → 启动前端 → 健康检查（详见第一章阶段 1 的坑位清单）
- [ ] **阶段 2**：Ad 模块四层精读（Controller 追到 Mapper，画调用图）；读 TenantEntity / BaseDaoImpl 基类；对照 b_ad 建表 SQL
- [ ] **阶段 3**：plus-ui ad.vue + adApi.ts + A* 组件 + stores
- [ ] **阶段 4**：plus-uniapp Home.vue / login.vue + @/wd + composables
- [ ] **阶段 5**：Sa-Token → 多租户 → 数据权限 → Redis 缓存（逐个激活对应技能）
- [ ] **阶段 6**：/crud 实战一个模块 + /check + e2e-test-pc 验收
- [ ] **实践**：写一个自定义 hook（例如提交信息校验）
- [ ] **可选讨论**：add-agent 规范空白怎么补（扩展 add-skill / 新建技能）

### 本会话关键结论速查（Q&A 精华）
1. dev-startup 技能装好环境后仍有用：建库导 SQL、mvn 两步启动坑、pnpm 镜像坑、健康检查白名单、Windows 代理 502
2. CLAUDE.md 是启动时程序注入的快照，非每轮读取；5 级位置多层并存
3. Hooks 是唯一程序化强制层；fail-open 原则；block 靠 JSON decision 字段而非 exit code
4. Skills 渐进式披露；新增需登记 3 处 + 镜像 1 处；Codex 镜像只留 .agents/skills（防重复注入）
5. Agent 调用是模型自主行为；description 是唯一决策入口；正文不参与决策
6. .codex/agents 不存在 → 两个子代理是 Claude-only；Codex 靠 hooks + skills 治理
7. add-skill 不适用于新增子代理（登记动作完全不同）
8. README 给人、CLAUDE.md 给 Claude、AGENTS.md 给 Codex，互不自动加载
9. 16 个 README 分 5 类；docker data 目录 README = 空目录占位 + 运维指令
10. 本仓库是 ruoyi-plus-uniapp 框架的**子项目**（存在 .framework-sync.json）
