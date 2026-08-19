<!-- 此文件由 .agent-governance/scripts/sync-agent-assets.cjs 生成，请修改模板或 core-rules.md。 -->

<!-- core-rules-version: 2026-08-19.1 -->

# 项目共同硬规则

## 沟通、编码与文档

- 所有面向用户的回复、错误说明、技术讨论和新增代码注释使用简体中文；技术术语、类名和方法名可以保留英文。
- 所有源码、配置、脚本和文档均使用 UTF-8 无 BOM；修改 Java 文件后应抽查文件头没有 `EF BB BF`。出现 `java: 非法字符: '\ufeff'` 时，只移除 BOM 并扫描同目录 Java 文件，不改业务逻辑。
- PowerShell 读取包含中文的文件必须使用 `Get-Content -Encoding UTF8`，并优先使用支持 UTF-8 的检索工具。
- 保留已有业务注释；新增注释说明原因。接口方法注释至少说明用途、参数、返回值以及异常或边界行为。
- 除非用户明确要求，不创建 Markdown、README 等文档；获授权的文档只能放在项目根目录 `docs/`。
- 所有日期时间使用东八区 `Asia/Shanghai`。

## 项目架构禁令

- 本项目是深度重构的 `ruoyi-plus-uniapp`，不是 `ruoyi-vue-plus`；包名前缀必须是 `plus.ruoyi`，禁止参考或复制后者的架构。
- 编写业务代码前，先阅读本项目同领域既有实现，再检索是否已有可复用工具类。
- Entity 继承 `TenantEntity`；BO 使用 `@AutoMappers`；Service 直接实现接口且不继承 `ServiceImpl` 或 `IBaseService`；Mapper 只继承 `BaseMapper<Entity>`；DAO 独立构建查询并具有 `buildQueryWrapper()`；对象转换统一使用 `MapstructUtils`。
- 禁止用 `Map<String, Object>` 传递业务数据；禁止在代码中使用完整包名代替 import。
- 生成后至少检查：包名不是 `com.ruoyi`、四层职责完整、实体/BO/DAO 规则符合以上约束。

## 页面与接口约束

- PC 前端为 `plus-ui/`，任何页面或 API 开发前必须阅读广告模块 `plus-ui/src/views/business/base/ad/ad.vue`、`plus-ui/src/api/business/base/ad/adApi.ts` 与 `adTypes.ts`；使用 A* 封装组件，禁止原生 `el-*` 组件及 `ElMessage`。
- 移动端 CLI 为 `plus-uniapp/`，原生 APP 为 `plus-app/`；开发移动端页面前阅读 Home 和登录参考及项目 `wd-*` 封装组件。必须使用 WD UI，禁止 `uni-*` UI 组件。
- API 调用遵循项目现有 `[err, data]` 风格；不自行引入平行封装。

## 技能、路由与同步

- Codex 技能目录为 `.agents/skills/`；Claude Code 主技能目录为 `.claude/skills/`。新增或修改技能由 `add-skill` 从 Claude 主目录同步到 Codex 镜像。
- 开始实质实现前才读取主技能正文；只读解释、状态询问和简单定位不因泛词加载大型开发技能。最多加载一个主技能与两个非必需辅助技能。
- `.agent-governance/skills-manifest.json` 是双端技能路由的唯一触发词来源；Hook 只输出固定技能名、固定路径和固定原因，不得回显用户提示词或技能全文。manifest 解析失败时降级为空路由，不阻断独立安全 Hook。
- 共同硬规则由本文件生成到 `AGENTS.md` 与 `CLAUDE.md`，不得以外部链接替代正文。变更治理资源后运行 `node .agent-governance/scripts/verify-agent-assets.cjs`。

## 并发工作区保护

- 会话首次操作时执行 `git status -s` 和 `git branch --show-current`；现有未提交且与本任务无关的文件属于他者占用区，不修改、不 stash、不 checkout、不 reset。
- 修改既有文件前执行 `git log -1 --format="%ar|%s" <file>`。15 分钟内的未提交他者改动应静默绕开；无法绕开时才询问用户是否继续。
- 禁止 `git stash`、`git reset --hard`、丢弃文件改动的 checkout、`git add -A`、`git add .`、`git clean -fd`，以及终止非本会话启动的进程。提交前逐文件暂存并核对暂存清单。

## 安全与验证

- 不执行破坏性、不可逆或生产数据操作，除非用户已明确授权并确认精确目标。
- 代码改动后执行受影响模块的最小编译、测试或类型检查；涉及编码时确认中文可读和 UTF-8 无 BOM。


## Codex 入口

- Codex 自动发现 `.agents/skills/` 的技能元数据；开始实质实现前读取被路由技能的 `SKILL.md`。
- `.codex/hooks/skill-forced-eval.cjs` 仅提供本轮轻量路由提示；`PreToolUse` 继续独立执行安全与范围校验。
