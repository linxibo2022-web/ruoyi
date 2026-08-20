# 头脑风暴：Claude Code 与 Codex 子代理技能匹配统一治理

**创建时间**：2026-08-20（Asia/Shanghai）
**最后更新**：2026-08-20（Asia/Shanghai）
**状态**：已修订；须先完成 P0 运行时探针与静态门禁，随后方可进入实施
**替代文档**：`docs/子代理技能匹配治理方案-2026-08-20.md`（已清理）

---

## 1. 结论速览

本方案同时覆盖 Claude Code 与 Codex，最终裁决如下。

1. 把“何时委派”写入全局规范是合理的，因为主代理必须先看到规则才会主动拆分任务；但全局规范只应保留短小、能力中立的硬原则。当前“跨多文件就尽量委派、命中 description 即调用”范围过宽，应收窄。
2. 子代理启动不会再次触发主会话的 `UserPromptSubmit`。真正的任务级技能评估应在创建子代理前，通过 `PreToolUse(Agent)` 读取具体子任务文本，复用现有 `selectRoute()`，再把技能路由信封写回委派消息。
3. `SubagentStart` 两端都可用，但输入只有 `agent_type`、`agent_id` 等信息，没有子任务正文。因此它只负责角色边界、缺失路由兜底和审计，不能独立完成按任务路由。
4. 内置子代理与项目自定义子代理不应互相替代：内置角色承担通用探索和实现，项目角色只承担稳定、重复、项目特有的工作流。
5. `skills-manifest.json` 继续只做技能触发词唯一来源；新增独立 `agents-manifest.json` 管理项目子代理及其跨端能力引用。两个 manifest 分域单源，并由校验器交叉校验。
6. Claude 与 Codex 的运行时字段不追求字面一致，追求语义一致。Claude 原生 `skills:` 是全文预加载；Codex 的 `skills.config` 是配置覆盖，二者不能机械互译。
7. 子代理主要节省主线程上下文，不保证降低总 Token。两端官方文档都明确：多代理通常会增加总模型与工具工作量。
8. 对高风险委派实行分级失败策略：通用只读探索可降级；写入、项目审查和文档更新在路由器错误、任务字段缺失或最终信封不可信时，必须在 `PreToolUse(Agent)` 阶段拒绝创建，而不是要求子代理事后自觉报告。
9. 默认深度为 1 是治理目标而不是跨端既有事实：Claude 可通过子代理工具调用携带的 `agent_id` 强制阻止递归；Codex 必须先完成真实事件关联技术验证，验证前仅作为行为策略，不得宣称已强制拒绝孙代理。

推荐架构：

```text
用户请求
  ├─ UserPromptSubmit：主会话技能路由
  └─ 主代理：拆分独立子任务、选择内置或项目角色
        ↓
PreToolUse(Agent)：读取每个子任务 prompt/message
        ↓
selectRoute(子任务文本, runtime)
        ↓
将固定技能路由信封写回 Agent 参数
        ↓
SubagentStart：注入角色边界和缺失信封告警
        ↓
子代理按需读取 1 个主技能 + 最多 2 个辅助技能
        ↓
只回传结论、证据、风险和验证结果
        ↓
主代理负责综合判断、写入决策和最终验收
```

---

## 2. 问题边界与术语

### 2.1 两个正交决策

技能路由和子代理选择不是同一件事。

| 决策 | 回答的问题 | 决策依据 | 结果 |
|---|---|---|---|
| 技能路由 | 这个任务应遵守哪套领域方法？ | 子任务文本、`skills-manifest.json` | 主技能、辅助技能、固定路径 |
| 委派选择 | 这个任务由谁执行更合适？ | 独立性、读写模式、角色描述、运行时能力 | 主代理、内置子代理、项目子代理 |

不能用原始用户提示词代替拆分后的子任务做技能路由。一个全栈请求可能拆成数据库、后端、PC、移动端四个子任务，每个子任务应得到不同技能。

### 2.2 上下文目标

- 主线程上下文：保留需求、决策、冲突处理和最终结论。
- 子代理上下文：承载搜索结果、文件内容、日志、测试输出和专项推理。
- 总 Token：主代理与所有子代理之和，通常不会因委派自动下降。

本治理目标是隔离主上下文污染、提高并行性和专项质量，不以“总 Token 一定更少”为承诺。

---

## 3. 当前事实基线

### 3.1 现有技能链路

```text
.agent-governance/skills-manifest.json
        ↓
.agent-governance/lib/router.cjs::selectRoute()
        ├─ .claude/hooks/skill-forced-eval.cjs
        └─ .codex/hooks/skill-forced-eval.cjs
```

当前 `UserPromptSubmit` 只评估用户提交给主会话的提示。已有路由支持显式 `$skill`、斜杠绕过、触发词/排除词、优先级、依赖和最多三个技能。

### 3.2 现有子代理链路

| 项目 | Claude Code | Codex |
|---|---|---|
| 项目角色目录 | `.claude/agents/*.md` | `.codex/agents/*.toml` |
| 当前项目角色 | `code-reviewer`、`project-manager` | 同名两个角色 |
| 子任务技能评估 | 未接线 | 未接线 |
| Agent 创建前 Hook | 当前未配置 | 当前未配置 |
| `SubagentStart` | 当前未配置 | 当前未配置 |
| Agent 同步/校验 | 无 | 无 |

现有两个项目角色均为手工双份维护。`code-reviewer` 约六百行，重复了大量 `/check` 规则；Codex 版本还引用 `.claude/skills/...`，存在跨端错误。`project-manager` 与 `init-docs`、`update-status`、`add-todo`、`progress` 等既有技能/命令重叠，并包含手工修改生成文件的旧指令。

当前 `verify-agent-assets.cjs` 的全绿只能证明技能、根规则和命令映射正常，不能证明子代理治理正常。

### 3.3 两端官方能力边界

| 能力 | Claude Code | Codex |
|---|---|---|
| 主动委派 | 根据请求、agent `description` 和上下文自动选择；可显式点名 | 用户直接要求，或适用 `AGENTS.md` / skill 指令要求时委派 |
| 内置通用角色 | `Explore`、`Plan`、`general-purpose` | `explorer`、`worker`、`default` |
| 自定义角色 | `.claude/agents/*.md` | `.codex/agents/*.toml` |
| Agent 创建工具 Hook | `PreToolUse` 可匹配 `Agent` 并改写完整输入 | `spawn_agent` 在 Hook 中可匹配 `Agent`，可改写完整输入 |
| 启动 Hook | `SubagentStart` 可注入 `additionalContext` | `SubagentStart` 可注入 `additionalContext` |
| 启动 Hook 是否有任务正文 | 否 | 否 |
| 根规则继承 | 普通自定义 agent 会加载；内置 `Explore`、`Plan` 跳过 `CLAUDE.md` | 子会话加载项目指导与父会话配置，但不能等同于重新触发 `UserPromptSubmit` |
| 静态技能能力 | agent frontmatter `skills:` 会全文预加载 | agent 可覆盖 `skills.config`，不是 Claude 式全文预加载 |

---

## 4. 四个问题的直接回答

### 4.1 全局规范中要求尽量使用子代理是否合理

方向合理，位置也合理，但规则需要缩短和收窄。

全局规范适合保留：

- 哪类任务优先委派；
- 哪类任务禁止或不值得委派；
- 并行写入的所有权规则；
- 默认委派深度；
- 子代理回传格式；
- 主代理的最终责任。

详细的触发矩阵、角色清单、信封结构、测试样例不应写进 `AGENTS.md` / `CLAUDE.md`，否则每次会话都会占用根上下文。

建议替换 `.agent-governance/core-rules.md` 中现有子代理段为以下短规则，并通过生成脚本同步双端：

```markdown
## 子代理委派与上下文

- 主线程保留需求、关键决策、冲突处理和最终验收；边界清晰、读重、原始输出噪声高且可独立汇总的检索、审查、测试与研究优先委派。
- 单文件或单符号定位、短答案、强顺序依赖任务由主线程完成；并行收益低于交接成本时不委派。
- 并行子任务必须相互独立；写任务必须声明文件或模块所有权，不得让多个代理修改重叠文件。
- 默认目标为一级委派；Claude 子代理的递归调用由 Hook 拒绝。Codex 在身份关联探针验证前仅作为可审计行为规则，不承诺强制拒绝。
- 委派原始消息必须声明目标、范围、只读/可写模式、所有权、验收标准和回传格式；最终送入子代理的消息必须含技能路由状态，正常由 Preflight Hook 追加。子代理只回传结论、证据路径、风险与验证结果，不回传原始日志。
- 主代理必须复核子代理结论、整合跨任务冲突并完成最终验证；运行时无可用子代理时自动降级为主线程执行。
```

### 4.2 子代理不进行技能评估，如何调整

采用“创建前精确路由 + 启动时兜底”的双层机制。

第一层是决定性主链：

1. 为 `PreToolUse` 新增独立 `Agent` matcher。
2. Hook 使用运行时严格字段提取器：Claude 只接受 `tool_input.prompt`，Codex 只接受 `tool_input.message`；未知 schema 不猜测字段。高风险角色拒绝，通用只读探索才降级。
3. 调用扩展后的 `selectRouteWithStatus(taskText, runtime)`，明确区分正常命中、正常未命中和路由器错误。
4. 合并 agent 固定基线能力；动态主技能和必需依赖不静默丢弃，最多两个限制只作用于非必需辅助能力。
5. 在原参数中追加固定格式技能信封，完整保留 `subagent_type`、`task_name`、`agent_type`、`fork_turns`、`model` 等其他字段。
6. 通过 `updatedInput` 让运行时使用改写后的委派消息。

第二层是启动兜底：

- `SubagentStart` 匹配所有 `agent_type`，已登记角色注入固定边界，未知角色注入最小通用边界并标记 `UNREGISTERED_AGENT`；
- 要求子代理先检查委派消息中的技能信封；
- `SubagentStart` 不能阻止启动，不能承担写入/审查门禁；最终信封缺失、路由器错误或任务字段缺失时，高风险角色必须在创建前由 `PreToolUse(Agent)` 拒绝，通用只读探索才按通用规则降级；
- 不从 `transcript_path` 反向解析任务文本，转录格式和并发时序都不是稳定接口。

### 4.3 内置子代理与项目自定义子代理如何兼顾

按“通用能力归运行时、项目流程归项目”的原则分层。

| 任务类型 | Claude Code | Codex | 是否创建项目角色 |
|---|---|---|---|
| 跨目录只读探索 | `Explore` | `explorer` | 否 |
| 计划阶段研究 | `Plan` | 主代理/`explorer` | 否 |
| 通用实现或修复 | `general-purpose` | `worker` | 否 |
| 无法精确归类 | 主代理/`general-purpose` | `default` | 否 |
| RuoYi 项目规范代码审查 | `ruoyi-code-reviewer` | `ruoyi-code-reviewer` | 是 |
| RuoYi 项目状态与文档查询 | `ruoyi-project-reporter` | `ruoyi-project-reporter` | 是 |
| RuoYi 项目文档更新 | `ruoyi-project-updater` | `ruoyi-project-updater` | 是 |

`ruoyi-project-updater` 仅在用户明确授权更新 `docs/` 后可被选择；状态查询、进度汇总和“看看文档”一律选择 `ruoyi-project-reporter` 或主代理，不能因 description 泛匹配获得写能力。

选择优先级：

1. 用户显式指定的合法 agent；
2. 精确命中、能力满足的项目自定义 agent；
3. 运行时内置通用 agent；
4. 主代理直接完成。

项目角色统一使用 `ruoyi-*` 命名空间，不得使用 Claude 的 `Explore`、`Plan`、`general-purpose` 或 Codex 的 `default`、`worker`、`explorer`。Codex 官方规定同名自定义 agent 会覆盖内置角色，Claude 也允许同名项目角色覆盖内置角色，因此必须由校验器阻止意外碰撞。

不在首版实现强制 `selectAgent(prompt)` 并覆盖运行时选择。`description` 和治理 manifest 只提供推荐；`Agent PreToolUse` 负责校验和注入技能，不擅自改写 `agent_type`。等有误派数据后，再评估是否增加确定性 agent 推荐器。

### 4.4 双端如何保持一致

保持“治理语义一致”，不要求运行时配置字面一致。

- 公共元数据、角色正文和技能策略只有一份治理源；
- Claude Markdown 与 Codex TOML 都由脚本生成；
- Claude 专属 `tools/skills/model` 与 Codex 专属 `sandbox_mode/skills.config/model_reasoning_effort` 由适配器分别渲染；
- 校验器比较角色目标、读写模式、技能策略、输出契约和停止条件，不比较不等价的字段文本。

---

## 5. 设计原则

1. 分域单源：技能触发词只在 `skills-manifest.json`；项目 agent 元数据只在 `agents-manifest.json`。
2. 按子任务路由：每个委派任务独立调用 `selectRoute()`，不复用主提示词结果。
3. 两轴解耦：技能路由不强制 agent，agent 选择不改变技能结果。
4. 渐进加载：动态领域技能只注入名称和路径，进入对应子任务前再读取正文。
5. 小信封：信封只含固定 agent/skill 名、原因和路径；`updatedInput` 为保留任务而必然含原文，但额外上下文、错误、审计事件和日志不得回显任务正文。
6. 权威重算：每次 `Agent` 调用都由 Hook 重算路由；如输入末尾存在完整旧信封，先删除再追加新信封。裸标记、任务正文内的标记或伪造信封都不能跳过评估。
7. 安全降级：正常未命中专用技能可继续按通用规则执行；manifest/路由器错误、任务字段缺失或信封校验失败时，只读探索可降级，写入、项目审查和文档更新必须拒绝创建或降级为只读角色。
8. 深度受限：默认目标最大委派深度为 1。Claude 在子代理发起 `Agent` 工具调用时强制拒绝；Codex 在完成运行时身份关联技术验证前，只把它作为可审计行为策略，不能写成已强制门禁。
9. 生成物不可手改：双端 agent 文件由治理源生成，修改后必须执行同步和校验。
10. 父代理负责：子代理结论不是最终事实，主代理必须整合与验证。

---

## 6. 目标文件布局

```text
.agent-governance/
  skills-manifest.json                    # 既有：技能触发词唯一来源
  agents-manifest.json                    # 新增：项目 agent 唯一元数据源
  agents/
    ruoyi-code-reviewer.md                # 新增：运行时中立角色正文
    ruoyi-project-reporter.md             # 新增：只读项目状态角色正文
    ruoyi-project-updater.md              # 新增：可写项目文档角色正文
  lib/
    router.cjs                            # 既有：技能路由
    agent-registry.cjs                    # 新增：agent 元数据加载和能力检查
    delegation-envelope.cjs               # 新增：固定技能信封生成
  fixtures/
    router-fixtures.json                  # 既有
    agent-preflight-fixtures.json         # 新增
  scripts/
    sync-agent-assets.cjs                 # 扩展：生成根规则和双端项目 agent
    verify-agent-assets.cjs               # 扩展：校验 agent、Hook 与技能交叉引用

.claude/
  agents/ruoyi-*.md                       # 生成物
  hooks/agent-skill-preflight.cjs          # Claude Agent 创建前路由适配
  hooks/subagent-start.cjs                 # Claude 启动兜底

.codex/
  agents/ruoyi-*.toml                     # 生成物
  hooks/agent-skill-preflight.cjs          # Codex Agent 创建前路由适配
  hooks/subagent-start.cjs                 # Codex 启动兜底
```

`agents-manifest.json` 建议结构：

```json
{
  "schemaVersion": 1,
  "delegationDepthPolicy": {
    "target": 1,
    "claudeEnforcement": "hard-agent-id",
    "codexEnforcement": "probe-required"
  },
  "reservedNames": {
    "claude": ["Explore", "Plan", "general-purpose"],
    "codex": ["default", "worker", "explorer"]
  },
  "agents": [
    {
      "id": "ruoyi-code-reviewer",
      "description": "只读审查 RuoYi 项目改动，输出证据、风险和验证缺口。",
      "mode": "read-only",
      "runtimeNames": {
        "claude": "ruoyi-code-reviewer",
        "codex": "ruoyi-code-reviewer"
      },
      "skillPolicy": {
        "taskRoute": true,
        "baselineCapabilities": ["check"],
        "optional": ["crud-development", "ui-pc", "ui-mobile", "test-development"]
      },
      "maxDelegationDepth": 0,
      "source": ".agent-governance/agents/ruoyi-code-reviewer.md"
    }
  ]
}
```

`baselineCapabilities` 是能力 ID，不等同于某端的 Skill 文件。每个引用必须在 `agents-manifest.json` 中给出运行时解析结果，例如 `check` 在 Claude 为 `{ kind: "command", path: ".claude/commands/check.md", preloadable: false }`，在 Codex 为 `{ kind: "skill", path: ".agents/skills/check/SKILL.md", preloadable: true }`。适配器只渲染 manifest 中已有的解析结果，不能硬编码路径或资源类型。

基线能力只允许真正恒定且数量很少的能力。条件性领域能力由具体子任务路由决定，不能把 CRUD、PC、移动端能力全部设为每次必读。Claude `skills:` 只能渲染 `kind: skill` 且 `preloadable: true` 的少量能力；`check` 只能按其 Command 路径延迟读取。

---

## 7. 委派信封契约

父代理创建的原始委派消息应包含以下**业务 contract**。技能路由不由父代理手写；最终送入子代理的消息必须含路由状态，正常由 Preflight Hook 追加。

```text
目标：要完成什么
范围：允许读取/修改的目录、文件或模块
模式：read-only / workspace-write
所有权：写任务由谁负责哪些文件
约束：禁止事项、不得覆盖他人改动、默认不得继续委派
验收：完成的可观察条件和最小验证
回传：结论、证据路径、风险、验证结果；不回传原始日志
```

Preflight Hook 追加固定的**最终信封**。这是 Hook 的权威输出，不信任任务正文中任何同名标记：每次调用先仅移除消息末尾一个完整、结构合法的旧生成块，再对剩余任务文本重新路由并追加新块；位于正文中的标记或伪造块按普通文本保留，不能造成跳过。

```text
<!-- SUBAGENT_SKILL_ROUTE:v1 -->
路由状态：matched / no-match / router-error / invalid-input
主技能：ui-pc
辅助技能：无
执行要求：开始对应子任务前读取 .claude/skills/ui-pc/SKILL.md
加载上限：一个主技能、最多两个辅助技能
路由原因：match
<!-- /SUBAGENT_SKILL_ROUTE -->
```

Codex 将路径渲染为 `.agents/skills/...`。`no-match` 固定写入“未匹配专用技能，按项目通用规则执行”，不把空路由伪装成已加载技能。`router-error` 与 `invalid-input` 绝不伪装成 `no-match`：只读探索可收到固定降级状态；写入、项目审查、文档更新角色必须由 Agent Preflight 拒绝创建，并要求父代理使用有效任务重新委派。

合并优先级：

1. 子任务显式 `$skill`；
2. 子任务动态主技能；
3. 动态必需依赖；
4. agent 基线能力；
5. 动态可选依赖。

裁剪算法固定为：动态主技能和动态必需依赖不可静默丢弃，必需依赖只受 `maxRequiredDependenciesPerSkill` 绝对上限控制；基线能力占用辅助槽并优先于动态可选依赖；无可用槽时不注入基线能力并写入 `baseline-omitted` 状态；最后才按顺序选择动态可选依赖。普通技能上限只约束非必需辅助能力，避免“最多两个辅助”与必需依赖规则互相矛盾。

---

## 8. 双端 Hook 实施

### 8.1 公共 preflight 行为

两个适配脚本应复用同一公共实现，行为如下：

```javascript
const args = input.tool_input || {}
const taskKey = runtime === 'claude'
  ? (typeof args.prompt === 'string' && args.prompt.trim() ? 'prompt' : null)
  : (typeof args.message === 'string' && args.message.trim() ? 'message' : null)
const risk = resolveRiskProfile(args, runtime) // read-only / review / write / docs-write

if (!taskKey) return denyOrDegrade(risk, 'invalid-input')
const taskText = stripTerminalGeneratedEnvelope(args[taskKey])
const route = selectRouteWithStatus(taskText, runtime) // 明确返回 no-match 与 router-error
if (route.status === 'router-error' && risk !== 'read-only') {
  return deny('SKILL_ROUTE_ERROR，请父代理修正任务后重新委派')
}
const contract = renderEnvelope(route, runtime, resolveAgentType(args), risk)
const updatedInput = {
  ...args,
  [taskKey]: `${taskText}\n\n${contract}`
}

return {
  hookSpecificOutput: {
    hookEventName: 'PreToolUse',
    permissionDecision: 'allow',
    updatedInput
  }
}
```

`updatedInput` 会替换整个工具参数对象，因此必须先展开原参数，不能只返回修改后的 `prompt/message`。它为保留任务而必然包含原任务一次；“不得回显任务”只约束 `additionalContext`、`systemMessage`、stderr、审计事件和调试日志，不能对完整 `updatedInput` 的 JSON stdout 作全局禁止。

风险级别由运行时 agent 类型与 `agents-manifest.json` 的角色策略解析，不从自由文本“模式”猜测。`read-only` 仅允许安全降级；`review`、`write`、`docs-write` 遇到 `router-error`、`invalid-input` 或最终信封校验失败必须拒绝。Preflight 仅有一个项目级 Agent 参数重写器；若 `/hooks` 或 Claude `/hooks` 发现用户/插件另有 Agent `updatedInput` 重写器，运行时告警并暂停启用本机制，避免并发改写竞争。

### 8.2 Claude Code 配置

在 `.claude/settings.json` 增加独立 matcher，不把 Agent 逻辑混进现有命令安全 Hook：

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash|PowerShell|Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "node",
            "args": ["${CLAUDE_PROJECT_DIR}/.claude/hooks/pre-tool-use.cjs"],
            "timeout": 5
          }
        ]
      },
      {
        "matcher": "^Agent$",
        "hooks": [
          {
            "type": "command",
            "command": "node",
            "args": ["${CLAUDE_PROJECT_DIR}/.claude/hooks/agent-skill-preflight.cjs"],
            "timeout": 5
          }
        ]
      }
    ],
    "SubagentStart": [
      {
        "matcher": ".*",
        "hooks": [
          {
            "type": "command",
            "command": "node",
            "args": ["${CLAUDE_PROJECT_DIR}/.claude/hooks/subagent-start.cjs"],
            "timeout": 5
          }
        ]
      }
    ]
  }
}
```

这是对现有 `.claude/settings.json` 的**数组合并**，不能覆盖已有 `UserPromptSubmit`、命令安全 `PreToolUse`、`Stop` 等 Hook。当前安全 Hook 如启用 `PowerShell` matcher，脚本也必须真实处理 `tool_name === 'PowerShell'`；否则保持既有 Bash matcher，禁止只改 matcher。

Claude 项目 agent 的 `skills:` 只预加载 0～2 个稳定、小型、每次必需且 `preloadable: true` 的 Skill。动态业务能力不预加载全文。若 agent 需要运行时主动发现技能，应保留 `Skill` 工具；如果治理完全依赖信封中的文件读取，则确保其具有 `Read`。Claude 的子代理递归门禁为：PreToolUse 输入存在 `agent_id` 且工具名为 `Agent` 时拒绝；该检查必须有一级子代理尝试再委派的真实验收。

### 8.3 Codex 配置

在 `.codex/hooks.json` 增加：

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "^(Agent|spawn_agent)$",
        "hooks": [
          {
            "type": "command",
            "command": "node \"$(git rev-parse --show-toplevel)/.codex/hooks/agent-skill-preflight.cjs\"",
            "commandWindows": "powershell.exe -NoProfile -Command \"& node (Join-Path (git rev-parse --show-toplevel) '.codex/hooks/agent-skill-preflight.cjs')\"",
            "statusMessage": "评估子任务技能",
            "timeout": 10,
            "additionalContextLimit": 800
          }
        ]
      }
    ],
    "SubagentStart": [
      {
        "matcher": ".*",
        "hooks": [
          {
            "type": "command",
            "command": "node \"$(git rev-parse --show-toplevel)/.codex/hooks/subagent-start.cjs\"",
            "commandWindows": "powershell.exe -NoProfile -Command \"& node (Join-Path (git rev-parse --show-toplevel) '.codex/hooks/subagent-start.cjs')\"",
            "statusMessage": "加载子代理治理约束",
            "timeout": 10,
            "additionalContextLimit": 800
          }
        ]
      }
    ]
  }
}
```

Codex 官方建议仓库 Hook 从 Git 根目录解析脚本，上例同时给出 Windows `commandWindows`，避免从子目录启动 Codex 时相对路径失效。`additionalContextLimit: 800` 是 Token 限制而非 1 KiB 字节限制；信封 UTF-8 字节数与启动上下文 Token 数必须分别验收。项目 `.codex/` 还必须处于 trusted 状态，变更后的 Hook **定义**哈希需要在 `/hooks` 中重新审阅；脚本内容漂移仍由仓库校验器负责，不能把两者混为一谈。

`.codex/config.toml` 建议补充：

```toml
[agents]
enabled = true
max_concurrent_threads_per_session = 3
default_subagent_reasoning_effort = "medium"
```

默认模型不在项目中强行锁死，优先继承父会话；只有明确的轻量探索或高强度审查角色才单独覆盖模型与 reasoning effort。

---

## 9. 项目自定义 agent 重构

### 9.1 `ruoyi-code-reviewer`

- 设为只读，不直接修复代码；修复需父代理或 `worker` 获得明确授权。
- 正文只保留角色目标、严重程度口径、证据格式、停止条件。
- 通用检查清单迁移到 `check` / `code-patterns` 的唯一来源，不在 agent 中复制约六百行。
- PC、移动端、CRUD、测试规范由具体审查子任务动态路由。
- 删除 Codex 生成物中的 `.claude/skills/...` 路径。

### 9.2 `ruoyi-project-reporter` 与 `ruoyi-project-updater`

- 拆分原 `ruoyi-project-manager`：`ruoyi-project-reporter` 只有只读工具，负责状态、进度和文档事实汇总；`ruoyi-project-updater` 才有 `docs/` 写入权限，且仅在用户明确要求更新时选用。
- 两者均为轻量编排角色，不复制 `init-docs`、`update-status`、`add-todo`、`progress` 全文；路由到的能力在任务开始时按需读取。
- 删除“手工修改 AGENTS.md/CLAUDE.md”旧指令；治理文件必须改源文件并由脚本生成。
- 时间统一使用 `Asia/Shanghai`。

### 9.3 生成要求

- 中立正文源使用 LF、UTF-8 无 BOM；
- Claude 适配器生成合法 YAML frontmatter；
- Codex 适配器生成合法 TOML 多行字符串，不产生字面量 `\r`；
- 双端生成物顶部标记“由治理脚本生成，请勿手改”。

---

## 10. 分阶段实施计划

### P0：冻结基线与能力探针

- 保存现有技能路由 fixture 和测试结果；
- 统计现有 agent 名称、引用路径、工具能力和正文体积；
- 为当前全局委派规则记录基线；
- 采集 Claude `Agent` / `SubagentStart` 与 Codex `spawn_agent` / `SubagentStart` 的真实脱敏输入 fixture，确认任务字段、调用者身份字段和嵌套委派可观测性；
- 明确 Codex 是否能稳定把子代理内 `spawn_agent` 与其 `agent_id` / 深度关联。未证实前，深度 1 只作为行为策略，验收不得写“强制拒绝”。
- 不改业务代码。

完成条件：现有 `verify-agent-assets.cjs`、Claude/Codex 技能 Hook 测试均通过；四类脱敏 Hook fixture、上下文体积基线与递归可观测性结论已入库。

### P1：子任务技能 preflight

- 新增公共信封渲染器；
- 新增 Claude/Codex `agent-skill-preflight.cjs`；
- 两端注册 `PreToolUse(Agent)`；
- 扩展路由 API，显式区分 `matched`、`no-match`、`router-error`、`invalid-input`，不再把 manifest 错误伪装成空路由；
- 新增正向、负向、显式、伪造标记、依赖、字段保留、风险分级拒绝和失败降级测试。

完成条件：每个不同子任务得到独立技能结果；改写后 Agent 参数除任务文本外完全保留；高风险角色在错误或缺任务字段时真实 deny，普通只读探索才允许降级。

### P2：启动兜底与全局规则收敛

- 两端注册匹配全部 agent 的 `SubagentStart`，已登记角色注入专属边界，未知角色收到最小通用边界与 `UNREGISTERED_AGENT` 状态；
- 分别限制信封 UTF-8 字节数、启动 `additionalContext` Token 数、agent 正文体积、Claude 静态预载正文总量和单次回传摘要长度；
- 精简 `.agent-governance/core-rules.md` 子代理段并重新生成根规则；
- 在 Claude 真实验收子代理递归 deny；Codex 仅在 P0 关联探针通过后接入同等级门禁，否则保留可审计软约束。

完成条件：内置探索 agent 即使不加载根规则，也能收到技能信封与关键边界；未知角色不会漏掉通用约束；各项上下文预算均可由校验器检查。

### P3：项目 agent 单源化与静态门禁

- 新增 `agents-manifest.json` 和中立正文源；
- 将 agent 的 runtime 名称、工具、默认权限、模型、能力 ID、每端资源 `kind/path/preloadable` 全部写入 manifest，适配器只做格式转换；
- 扩展 `sync-agent-assets.cjs` 生成到非运行时发现的 staging 目录；
- 先扩展 `verify-agent-assets.cjs`，校验 schema、资源类型、保留名、工具能力、Hook 接线、生成物漂移、编码和上下文预算；
- 在 staging 通过 YAML/TOML 解析与 `--check` 后，才准备切换旧角色和所有引用。

完成条件：双端 agent 文件不再手工维护；无保留的跨端路径、重复大段规则和字面量 `\r`；静态门禁在任何运行时可发现新 agent 之前已通过。

### P4：原子激活与校验闭环

- 在无运行中 Claude/Codex 会话的维护窗口内，以一个变更集同时写入新 `ruoyi-*` 生成物、切换所有引用并删除旧 `code-reviewer` / `project-manager`；
- 强制重启两端运行时后增加端到端人工验收；
- 分别完成项目 trust、`/hooks` Hook 定义审阅、根目录/子目录/含空格路径启动验证；
- 观察误派率和主上下文变化。

完成条件：任何 agent 漂移、保留名覆盖、技能引用缺失或 Hook 断线均以非零退出并给出明确路径；运行时没有同时发现新旧项目角色。

### P5：可选增强

只有在观测数据证明运行时经常误选项目角色时，才实现 advisory `selectAgent()`；首版不把它接入每轮技能 Hook，也不自动改写 `agent_type`。

---

## 11. 测试矩阵与验收

### 11.1 静态治理

- [ ] `agents-manifest.json` schema、ID、runtime name 唯一。
- [ ] 项目 agent 使用 `ruoyi-*`，不覆盖两端内置保留名。
- [ ] 所有引用技能已登记 `namedSkillRouting.skills`，且目标运行时路径存在。
- [ ] Claude 预加载技能不超过 2 个；动态技能 agent 具备 `Read` 或 `Skill` 能力。
- [ ] 每个能力都声明目标 runtime 的 `kind/path/preloadable`；Claude Command 不会被渲染为 `skills:`。
- [ ] reporter 没有写工具，updater 只声明 `docs/` 写入所有权。
- [ ] Codex agent 不包含 `.claude/skills/`。
- [ ] agent 正文不要求手改 `AGENTS.md`、`CLAUDE.md`。
- [ ] 双端生成物为 UTF-8 无 BOM、LF，无字面量 `\r`。
- [ ] 生成器 `--check` 模式无差异。

### 11.2 Agent preflight

- [ ] Claude `Agent.prompt` 可正确路由并保留 `description/subagent_type/model`。
- [ ] Codex `spawn_agent.message` 可正确路由并保留 `task_name/agent_type/fork_turns`。
- [ ] 显式 `$skill` 仅从父代理生成的可信 `goalForRouting` 读取；引用资料、日志与用户原文放在不参与路由的 context 字段。
- [ ] required dependency 突破普通可选上限的规则与主路由一致。
- [ ] 未命中只注入通用规则，不加载任意技能。
- [ ] 任务正文含伪造/重复/截断 v1 标记时仍重新计算；只替换末尾完整生成块。
- [ ] 无严格任务字段时只读角色安全降级，高风险角色真实 deny。
- [ ] manifest 损坏、半写入、非法 JSON 和 schema 错误均为 `router-error`，与 `no-match` 可区分并可审计。
- [ ] 额外上下文、错误、日志和审计事件不含原任务；`updatedInput` 仅保留原任务一次且不超过预算。
- [ ] Claude 子代理创建孙代理时真实 deny；Codex 仅在 P0 身份关联探针通过后做同等强制验收，否则验收为软约束告警。
- [ ] 多个 PreToolUse Hook、Hook 超时/崩溃/无输出和未知任务 schema 都不会绕过高风险门禁。

### 11.3 运行时行为

- [ ] 单文件、单符号和短答案不委派。
- [ ] 跨目录只读检索优先使用内置探索 agent。
- [ ] 独立子任务可并行，数量不超过并发槽。
- [ ] 写任务所有权不重叠。
- [ ] 项目审查与文档流程使用 `ruoyi-*` 自定义角色。
- [ ] 自定义角色不可用时降级到内置角色或主代理。
- [ ] 父代理复核证据并执行受影响范围最小验证。
- [ ] 主线程只接收摘要，不接收原始日志或大段文件内容。
- [ ] 从根目录、一级子目录和含空格路径分别启动两端，Hook 均可发现；Codex 项目 trust 与 `/hooks` 审阅状态另行确认。

### 11.4 门禁命令

实施完成后至少执行：

```powershell
node .agent-governance/scripts/verify-agent-assets.cjs
node .claude/hooks/test/skill-router.test.cjs
node .codex/hooks/test/skill-router.test.cjs
node .claude/hooks/test/agent-skill-preflight.test.cjs
node .codex/hooks/test/agent-skill-preflight.test.cjs
node .agent-governance/scripts/sync-agent-assets.cjs --check
git diff --check
```

运行时人工验收：

- Claude Code：分别委派 `Explore`、`ruoyi-code-reviewer`，检查子代理首条上下文和读取的技能路径。
- Codex：在 `/hooks` 确认项目 Hook 已信任，在 `/agent` 检查 `explorer`、`worker`、`ruoyi-code-reviewer` 的线程和技能信封。

---

## 12. 迁移、清理与回滚

迁移顺序必须避免运行时同时发现新旧两套项目角色。

1. 先实现 manifest、资源解析器、生成器、preflight、完整 fixture 和校验器；校验器必须早于运行时发现目录的任何变更。
2. 将 `ruoyi-*` 生成物写入 staging 非发现目录，执行 UTF-8/LF、YAML/TOML 解析、资源映射和 `sync --check` 验证。
3. 在无活动 Claude/Codex 会话的维护窗口内，全仓切换旧角色引用；以一个变更集同时写入新生成物并删除旧 `.claude/agents/code-reviewer.md`、`project-manager.md` 与 `.codex/agents` 对应 TOML。
4. 强制重启运行时，检查角色发现、Hook trust 和真实 preflight；失败即由上一版治理源重新生成回滚，不恢复手工双份文件。
5. 清理重复检查清单、跨端技能路径、手改生成文件指令和 Codex 字面量 `\r`。
6. 保留既有技能路由的外部行为与 fixture；允许为诊断状态和公共能力解析调整内部实现。

回滚单位是治理源、生成器和 Hook 配置的一组提交。回滚时由上一版治理源重新生成双端文件，不回到手工维护两份 agent 的状态。

---

## 13. 风险与应对

| 风险 | 应对 |
|---|---|
| 子代理增加总 Token | 只委派边界清晰、读重、高噪声任务；限制并发、深度和回传体积 |
| Agent Hook 特殊路径绕过 | 根规则和信封仅为协议层；高风险创建在 PreToolUse 缺可信终态时 deny，`SubagentStart` 只告警，不能当作阻断器 |
| Hook 改写丢失工具参数 | `updatedInput` 必须展开原对象；fixture 校验所有字段 |
| 内置角色不加载根规则 | 关键约束放入委派消息和 `SubagentStart`，不只依赖根文件 |
| Claude `skills:` 预加载挤占子上下文 | 仅静态预载 0～2 个小技能；动态领域技能按信封读取 |
| 项目 agent 与内置/插件重名 | `ruoyi-*` 命名空间 + 保留名/重复名校验 |
| 并行写冲突 | 明确文件所有权；默认读任务并行、写任务谨慎串行 |
| manifest 或 Hook 故障阻断工作 | `no-match` 可继续；`router-error`/`invalid-input` 仅通用只读 fail-open，高风险角色 deny；审计只记录状态、运行时、角色和配置 hash，不记录任务正文 |
| 信封标记被伪造 | 每次权威重算，只替换末尾完整生成块；裸标记、截断块和正文中的同名文本均不能跳过 |
| Codex 递归深度无法证明 | P0 先验证调用者身份关联；未通过时降级为可审计软约束，不将“最大深度 1”写为跨端强制保证 |
| 从子目录启动导致 Codex Hook 路径失效 | 使用 Git 根目录解析和 `commandWindows`，端到端从根目录/子目录各验收一次 |

---

## 14. 最终决策记录

| 决策项 | 最终选择 | 理由 |
|---|---|---|
| 全局委派规则 | 保留精简硬原则 | 主代理需在拆任务前看到，但不应承载详细流程 |
| 子任务技能评估 | `PreToolUse(Agent)` 二次路由 | 能取得具体子任务文本并改写委派参数 |
| `SubagentStart` | 兜底与审计 | 没有任务正文，不能独立做动态路由 |
| agent manifest | 独立 `agents-manifest.json` | 保持技能触发词单源，避免职责混杂 |
| Claude `requiredSkills` 自定义字段 | 不使用 | 不是官方 frontmatter 字段 |
| Claude 原生 `skills:` | 仅少量静态必需技能 | 会把全文预加载到子代理上下文 |
| agent 强制路由 | 首版不做 | 避免与运行时内置选择竞争和过度委派 |
| 内置/自定义分工 | 通用归内置，项目流程归 `ruoyi-*` | 降低重复、避免覆盖内置角色 |
| 双端 agent 维护 | 中立源生成双端文件 | 消除手工双份漂移 |
| 默认递归深度 | 目标为 1；Claude 强制、Codex 待 P0 证明 | 不把当前无法验证的 Codex 能力误写为跨端强制保证 |

---

## 15. 官方资料依据

- Claude Code：[Create custom subagents](https://code.claude.com/docs/en/subagents)
- Claude Code：[Hooks reference](https://code.claude.com/docs/en/hooks)
- Codex：[Subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents)
- Codex：[Hooks](https://learn.chatgpt.com/docs/hooks)
- Codex：[Custom instructions with AGENTS.md](https://learn.chatgpt.com/docs/agent-configuration/agents-md)
- Codex：[Build skills](https://learn.chatgpt.com/docs/build-skills)

以上资料均按 2026-08-20 可见官方文档校准。运行时升级后，应先复核 Hook 输入字段、Agent 工具别名、内置角色名和自定义 agent schema，再修改治理实现。

---

## 16. 讨论记录

### 2026-08-20 初始讨论与定稿

- 确认全局规范适合保存短委派原则，但当前规则过宽。
- 确认子代理不会重跑主会话 `UserPromptSubmit`。
- 确认 Claude Code 与 Codex 均支持 `SubagentStart`，但该事件没有子任务正文。
- 确认两端都可在 Agent 创建前通过 `PreToolUse` 观察和改写任务参数。
- 放弃“静态技能索引等于动态技能评估”的旧方案。
- 放弃 Claude 非官方 `requiredSkills/optionalSkills` 字段。
- 确定采用独立 agent manifest、任务级技能信封、内置/项目角色分层和生成式双端同步。
