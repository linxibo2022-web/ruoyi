# Codex 与 Claude Code 上下文注入优化方案

## 计划契约头

- **执行模式**：plan-executor。
- **判定依据**：用户已明确询问并要求按 `plan-executor` 能力完成本方案；因此本计划采用完整任务契约，而非仅供人工阅读的路线图。
- **目标**：在不降低项目规范、编码安全和双端技能一致性的前提下，降低 Codex 与 Claude Code 的固定上下文、技能路由和 Hook 注入成本。
- **架构**：以 `.agent-governance/` 管理共同规则文本、技能路由和同步校验；模板将共同硬规则直接生成到 `AGENTS.md` 与 `CLAUDE.md`，不依赖运行时跳转读取；技能继续遵循 `add-skill` 的 `.claude/skills/` 主目录 → `.agents/skills/` Codex 镜像机制。
- **范围**：仅调整 Agent 治理文件、技能文档与 Hook；不改业务代码、数据库、前后端运行逻辑。
- **端支持**：Codex、Claude Code。
- **非目标**：本阶段不删除业务技能，不改变现有编码规范，不调整模型、推理等级或第三方插件。
- **全局成功标准**：双端根规则直接包含同一版本的硬规则；双端路由使用同一 manifest 并对同一输入输出相同路由；普通 Hook 输出满足本计划字节上限；全部 Claude 共享技能完成短入口与按需资料改造，并与 Codex 镜像的完整技能包递归哈希一致；路由和安全回归样例全部通过。
- **自动裁决策略**：保持向后兼容、数据安全优先、最小权限、最小改动；manifest 或 Hook 解析失败时降级为空路由，不阻断独立安全 Hook。
- **最大并发数**：2；只有文件所有权和共享契约完全不重叠的任务才可并行。
- **默认最大重试次数**：2；仅允许在任务的“允许改动”范围内修复。
- **计划级验证命令**：`node .agent-governance/scripts/verify-agent-assets.cjs`，预期退出码 0 且输出所有 `[OK]`。

## 现状与问题

| 项目 | 当前观察 | 问题 |
|---|---:|---|
| `AGENTS.md` | 2,104 行、约 73 KiB | 硬规则、技能目录、模板、案例和流程混在一起，形成每会话固定成本。 |
| 项目技能 | 78 个，合计约 1.63 MiB | 大技能命中后全文阅读，会将无关教程带入上下文。 |
| Codex 路由 Hook | 每个非斜杠请求都要求完整读取所有命中技能 | 只读分析和小任务也被强制阻塞。 |
| Claude 路由 Hook | 每轮注入几十个技能及触发词 | 技能目录被重复注入，且触发词维护存在漂移风险。 |
| SessionStart | Codex 最多自动注入 8 KiB 历史经验 | 恢复历史经验的收益不稳定，容易挤占当前任务上下文。 |

## 目标架构

```text
.agent-governance/                 # 唯一事实源；不由运行时直接注入
├── core-rules.md                   # 双端共同硬规则源文本
├── skills-manifest.json            # 路由、优先级、依赖、排除词
├── templates/
│   ├── AGENTS.md.tpl               # Codex 薄入口模板
│   └── CLAUDE.md.tpl               # Claude Code 薄入口模板
└── scripts/
    ├── sync-agent-assets.cjs       # 生成/同步双端文件
    └── verify-agent-assets.cjs     # 校验内容与路由一致性

.agents/skills/                     # Codex 技能镜像
.claude/skills/                     # Claude Code 技能镜像
.codex/hooks/                       # Codex 专用 Hook 适配层
.claude/hooks/                      # Claude Code 专用 Hook 适配层
AGENTS.md                           # 直接包含共同硬规则的 Codex 入口
CLAUDE.md                           # 直接包含共同硬规则的 Claude Code 入口
```

原则：共同硬规则与路由逻辑只维护一次，但共同硬规则必须被直接生成到两个根规则文件；全部技能改造均由 `add-skill` 从 `.claude/skills/` 同步到 `.agents/skills/`，同步后必须校验完整技能包。Hook 实现可以不同，但不得各自维护一份完整触发词表。

## 设计一：根规则文件瘦身

### 保留内容

`AGENTS.md` 与 `CLAUDE.md` 直接保留：中文沟通、UTF-8 无 BOM、`plus.ruoyi` 架构禁令、并发工作区保护、前端/移动端组件约束、技能按需加载和同步校验要求。它们是始终生效的硬规则，不能仅保留一个外部路径引用。

### 迁移内容

下列内容从根规则迁到具体技能或其 `references/`：完整技能清单、触发词、CRUD 模板、建表模板、技术选型案例、FAQ、长篇操作步骤。

### Codex 薄入口实例

```md
# 项目开发约束

以下共同硬规则始终生效：使用中文、UTF-8 无 BOM、包名前缀 `plus.ruoyi`、禁止 `ServiceImpl` 继承、保护其他会话未提交改动、PC 使用 A* 组件、移动端使用 wd-* 组件。

## 技能加载

- Codex 自动发现 `.agents/skills/` 的技能元数据。
- 默认只加载一个直接相关的主技能；有明确依赖时最多增加两个辅助技能。
- 开始实质实现前再读取技能正文；只读解释、状态询问与简单定位不得因泛词加载大型开发技能。

## 双端同步

共同硬规则由 `.agent-governance/core-rules.md` 生成；技能仍按 `add-skill` 从 `.claude/skills/` 同步到 `.agents/skills/`；变更后运行同步校验。
```

### Claude Code 薄入口实例

```md
# 项目开发约束

以下共同硬规则始终生效：使用中文、UTF-8 无 BOM、包名前缀 `plus.ruoyi`、禁止 `ServiceImpl` 继承、保护其他会话未提交改动、PC 使用 A* 组件、移动端使用 wd-* 组件。

## 技能加载

- `.claude/hooks/skill-router.cjs` 决定本轮主技能和辅助技能。
- Hook 只输出本轮路由，不得注入全部技能清单或完整技能正文。
- 开始实质实现前再读取被路由的技能。
```

## 设计二：技能短入口与参考资料分层

### 目录实例

以高频的 `crud-development` 为试点：

```text
.agents/skills/crud-development/
├── SKILL.md
└── references/
    ├── backend-four-layer.md
    ├── ddl-checklist.md
    ├── pc-page-pattern.md
    └── mobile-page-pattern.md
```

`.claude/skills/crud-development/` 为主目录；修改后由 `add-skill` 同步到 `.agents/skills/crud-development/`。

### 精简后的 `SKILL.md` 实例

```md
---
name: crud-development
description: 用于新增或改造标准业务 CRUD 模块。
---

# 必做步骤

1. 先读取同领域的现有业务模块代码。
2. 涉及数据表时，加载 `database-ops`。
3. 后端遵循 Controller → Service → DAO → Mapper 四层结构。
4. PC 页面按需读取 `references/pc-page-pattern.md`；移动端按需读取 `references/mobile-page-pattern.md`。
5. 对受影响模块执行编译或类型检查。

## 边界

- 不适用于只读解释、简单文件定位和单行修复。
- 详细模板、案例和 FAQ 位于 `references/`，不得默认全文加载。
```

### 约束

- `SKILL.md` 建议控制在 100–200 行。
- `references/` 按问题类型拆分；任务只读取直接需要的文件。
- 首批拆分：`crud-development`、`ui-pc`、`ui-mobile`、`architecture-design`、`dev`。

## 设计三：统一技能路由清单

`.agent-governance/skills-manifest.json` 是唯一的技能触发规则来源。示例：

```json
{
  "maxSkillsPerTask": 3,
  "skills": [
    {
      "name": "crud-development",
      "priority": 90,
      "includeAny": ["新增CRUD", "业务模块", "Entity", "Service", "DAO"],
      "excludeAny": ["只回答", "解释", "定位文件"],
      "dependencies": {
        "涉及建表": ["database-ops"],
        "PC页面": ["ui-pc"],
        "移动端页面": ["ui-mobile"]
      }
    },
    {
      "name": "performance-doctor",
      "priority": 80,
      "includeAny": ["慢查询", "响应慢", "性能瓶颈", "渲染卡顿"],
      "excludeAny": ["上下文", "提示词", "模型延迟"],
      "dependencies": {}
    }
  ]
}
```

### 路由契约

两端 Hook 必须使用同一套确定性规则：

1. 显式 `$skill-name` 优先，直接选择该技能；斜杠命令直接放行，不做二次自动路由。
2. 将提示词转为小写并去除路径、代码块和引用内容后，再与 `includeAny` 匹配；`excludeAny` 命中时排除对应技能。
3. 直接命中按 `priority` 降序排列；同优先级按 manifest 中的声明顺序打破平局。
4. 选择最高优先级的一个主技能；只在 manifest 定义的依赖条件明确命中时加入辅助技能。
5. 默认上限为一个主技能和两个辅助技能；安全、数据库迁移、数据权限等被 manifest 标记为 `required` 的直接依赖可突破上限，但 Hook 必须在输出中说明原因。
6. 无命中时输出空路由；只读解释、状态询问和简单定位不得仅因“开发、优化、方案”等泛词命中。
7. Hook 只输出 manifest 中的固定技能名、固定路径和固定原因，不回显用户提示词，防止提示词内容成为注入文本。

“开发、优化、方案”等泛词不得独自触发技能。

## 设计四：Codex Hook 实例

### 路由 Hook

Codex 已具备 `.agents/skills/` 的技能发现能力，因此 `.codex/hooks/skill-forced-eval.cjs` 不再要求“所有命中技能全文读取后才能运行命令”。改为轻量提示：

```js
const route = selectPrimarySkill(prompt);

if (!route) {
  process.stdout.write('');
  process.exit(0);
}

process.stdout.write(
  `本轮主技能：${route.name}。` +
  '开始实质实现前读取其 SKILL.md；仅在任务满足依赖条件时加载辅助技能。'
);
```

输出示例：

```text
本轮主技能：crud-development。开始实质实现前读取其 SKILL.md；仅在任务涉及建表时加载 database-ops。
```

### SessionStart Hook

现有 SessionStart 对历史经验的注入从最多 8 KiB 改为默认不注入正文，仅输出路径索引：

```text
最近经验摘要位于 `.claude/docs/experience/.../xxx-exp-summary.md`；仅当本轮任务涉及历史问题或用户明确要求时读取。
```

编码、数据迁移、安全漏洞等“阻断级”经验可自动注入，但必须使用人工审核的结构化记录（标识、来源、失效日期、固定说明），总量上限为 1 KiB；其他经验一律仅输出路径。

### 保留的 Codex 安全 Hook

`PreToolUse` 继续负责危险命令、Git 范围、编码和受保护路径校验；不得重新注入技能内容或重复路由。

## 设计五：Claude Code Hook 实例

Claude Code 端继续通过 `UserPromptSubmit` 做路由，但不再在 `.claude/hooks/skill-forced-eval.cjs` 内硬编码并注入全部技能表。改为读取共享 manifest。

`.claude/hooks/skill-router.cjs` 的输出示例：

```text
## 技能路由

- 主技能：`crud-development`（用户请求新增业务 CRUD）
- 辅助技能：`database-ops`（需求涉及建表）
- 加载上限：2 个；未列出的技能不得因泛关键词加载。

开始实现前依次读取：
1. `.claude/skills/crud-development/SKILL.md`
2. `.claude/skills/database-ops/SKILL.md`
```

只读问题的输出示例：

```text
本轮为只读架构分析，不加载业务开发技能；按需读取项目规则与 Hook 配置。
```

Claude 的 `PreToolUse` 与 `Stop` Hook 继续承担安全拦截、范围检查和同步提醒；不得把完整技能正文再写入上下文。

## 设计六：双端同步与校验

### 同步职责

| 内容 | 唯一来源 | 目标位置 |
|---|---|---|
| 共同硬规则 | `.agent-governance/core-rules.md` | 直接生成到 `AGENTS.md`、`CLAUDE.md` |
| 技能正文与 references | `.claude/skills/`（按 `add-skill` 维护） | `.agents/skills/`（Codex 镜像） |
| 路由规则 | `skills-manifest.json` | Codex 与 Claude Hook 共同读取 |
| Hook 行为策略 | `hooks-policy` | 双端各自的 Hook 适配文件 |

### 校验输出实例

```text
[OK] 本次修改的共享技能及 references 在双端哈希一致
[OK] manifest 仅引用其声明为共享的技能
[OK] AGENTS.md 与 CLAUDE.md 使用相同核心规则版本
[OK] 两端 Hook 均读取 skills-manifest.json
[FAIL] ui-pc/references 在 .claude/skills 中缺失
```

试点阶段的双端技能数量差异不再视为范围外：后续全量改造必须覆盖全部 `.claude/skills/` 共享技能，并由 `add-skill` 执行 `.claude/skills/` → `.agents/skills/` 同步。同步完成后必须运行递归完整技能包校验；失败时先修复镜像，若校验能力不能覆盖新的目录结构或无法给出可操作差异，才允许升级校验器和其样例，但不得降低断言、跳过技能或把失败降级为警告。最多重试 2 次；仍失败时记录失败指纹并阻塞该技能，校验失败时禁止提交治理文件。同步脚本不得使用 `git add -A`，只处理本方案涉及的精确文件。

## 实施任务

- [x] T-01. 建立治理目录与基线统计（类型：配置）
  - 任务目标：产出不可变的改造前统计和路由样例，作为后续性能与行为回归基线。
  - 前置条件：无；只读取当前规则、Hook 和技能目录。
  - 依赖：无；后继任务：T-02。
  - 执行模式：串行。
  - 文件所有权：`.agent-governance/baseline/`。
  - 共享契约：`baseline.json` 的字段为根规则字节数、双端技能数、Hook 最大输出字节数和样例路由结果。
  - 允许改动：仅新增 `.agent-governance/baseline/` 下统计脚本和 JSON 快照。
  - 禁止改动：`AGENTS.md`、`CLAUDE.md`、任一 Hook、任一技能目录及业务代码。
  - 实施步骤：统计文件与技能体积；以固定样例运行现有 Hook；保存机器可读的基线快照。
  - 测试标准：统计脚本对缺失可选文件返回明确状态；六类样例均有记录。
  - 验收标准：`baseline.json` 包含上述全部字段且数值非负；样例至少包括 CRUD、建表、页面、报错、只读问答、性能分析。
  - 验证命令：`node .agent-governance/baseline/collect-baseline.cjs`，预期退出码 0 且生成 `baseline.json`。
  - 审查清单：快照不含用户提示词全文、密钥或业务数据；不改变任何既有运行时行为。
  - 失败处理边界：仅修复统计脚本路径和 JSON 序列化，最多重试 2 次；无法读取的文件记录为缺失并停止，不臆造数据。
  - 完成证据：2026-08-19 执行 `node .agent-governance/baseline/collect-baseline.cjs` 成功生成快照；`baseline.json` SHA-256 为 `AC98B7D01DACC3C206D653DDFB906DE169A46A4FAFCBEA529BA9670C61DB3236`；已统计 `AGENTS.md`、`CLAUDE.md`、双端技能目录及三类 Hook，六类匿名样例均有记录。
  - 提交：`chore(agent-governance): 添加上下文基线统计`。

- [x] T-02. 建立统一 manifest、路由库与同步校验（类型：配置）
  - 任务目标：建立可由双端 Hook 共同调用的确定性路由实现，消除 Hook 内的完整技能清单。
  - 前置条件：T-01 已完成并存在可读基线。
  - 依赖：T-01；后继任务：T-03、T-04、T-05、T-06。
  - 执行模式：串行。
  - 文件所有权：`.agent-governance/skills-manifest.json`、`.agent-governance/lib/router.cjs`、`.agent-governance/scripts/sync-agent-assets.cjs`、`.agent-governance/scripts/verify-agent-assets.cjs`、`.agent-governance/fixtures/`。
  - 共享契约：`selectRoute(prompt)` 返回 `primary`、`helpers`、`reason`、`bypass`；manifest 是唯一触发词来源。
  - 允许改动：上述治理目录中的新增文件；为双端 Hook 导出只读调用接口。
  - 禁止改动：既有技能内容与目录映射；业务代码；按用户要求，不新增“双端技能分类”。
  - 实施步骤：定义 manifest schema 与样例；实现显式技能、斜杠放行、标准化、优先级、依赖、上限和空路由；实现同步与校验脚本。
  - 测试标准：fixtures 覆盖显式技能、斜杠命令、无命中、排除词、依赖和 required 例外；两端使用同一 router 返回相同 JSON。
  - 验收标准：`verify-agent-assets.cjs` 在 fixture 全通过时退出 0；manifest 不含“开发/优化/方案”等单独泛词触发。
  - 验证命令：`node .agent-governance/scripts/verify-agent-assets.cjs`，预期退出码 0 且每个 fixture 输出 `[OK]`。
  - 审查清单：不回显原始提示词；路由失败降级为空路由；JSON schema 与错误信息不泄露本地内容。
  - 失败处理边界：仅修复 manifest、router 与 fixtures，最多重试 2 次；发现既有双端技能差异时记录为范围外，不自动同步存量差异。
  - 完成证据：2026-08-19 执行 `node .agent-governance/scripts/verify-agent-assets.cjs` 成功，7 个路由 fixture 全部通过；manifest SHA-256 为 `E60CBEF6B77D430DFB5961EFD22B31BCDF0FC9E6843E09831C6CA9DB71CE4F18`；路由 API 为 `.agent-governance/lib/router.cjs` 导出的 `selectRoute(prompt)`。
  - 提交：`feat(agent-governance): 增加统一技能路由与校验`。

- [x] T-03. 改造 Codex Hook（类型：非业务逻辑）
  - 任务目标：使 Codex 仅注入精确路由提示，SessionStart 默认仅输出经验路径索引。
  - 前置条件：T-02 的 router 和 manifest 已验证通过。
  - 依赖：T-02；后继任务：T-07。
  - 执行模式：并行组 P1（与 T-04 文件所有权不重叠）。
  - 文件所有权：`.codex/hooks/skill-forced-eval.cjs`、`.codex/hooks/session-start.cjs`、`.codex/hooks/test/`。
  - 共享契约：只消费 T-02 的 `selectRoute(prompt)` 输出，不自行维护触发词。
  - 允许改动：上述 Codex Hook、对应测试和 hooks 配置中必要的路径调整。
  - 禁止改动：`.claude/`、AGENTS/CLAUDE 根规则、业务代码和安全 Hook 策略。
  - 实施步骤：替换强制全文读取指令；接入 router；限制路由与 SessionStart 输出；添加 Hook stdin/stdout fixtures。
  - 测试标准：普通命中、无命中、显式技能、斜杠命令、恢复会话和经验索引均有断言。
  - 验收标准：普通路由输出 ≤512 B；SessionStart 默认输出 ≤512 B；输出不包含完整技能表或用户提示词。
  - 验证命令：`node .codex/hooks/test/skill-router.test.cjs`，预期退出码 0 且输出所有 `[OK]`。
  - 审查清单：保留恢复会话跳过；不阻断其他 Hook；缺 manifest 时空输出而非报错注入。
  - 失败处理边界：仅修复 T-03 文件，最多重试 2 次；无法兼容 Hook 输入时保留原安全路径并标记阻塞。
  - 完成证据：2026-08-19 执行 `node .codex/hooks/test/skill-router.test.cjs` 成功；7 个共享 fixture、恢复会话跳过和经验路径索引均通过，最大普通输出 226 B；受影响文件为 `.codex/hooks/skill-forced-eval.cjs`、`.codex/hooks/session-start.cjs`、`.codex/hooks/test/skill-router.test.cjs`。
  - 提交：`feat(codex): 精简技能路由与会话经验注入`。

- [x] T-04. 改造 Claude Code Hook（类型：非业务逻辑）
  - 任务目标：让 Claude Code 输出单次精确路由，不再注入完整技能列表。
  - 前置条件：T-02 的 router 和 manifest 已验证通过。
  - 依赖：T-02；后继任务：T-07。
  - 执行模式：并行组 P1（与 T-03 文件所有权不重叠）。
  - 文件所有权：`.claude/hooks/skill-forced-eval.cjs` 或 `.claude/hooks/skill-router.cjs`、`.claude/hooks/test/`、`.claude/settings.json`。
  - 共享契约：只消费 T-02 的 `selectRoute(prompt)` 输出；输出格式为主技能、最多两个辅助技能和固定路径。
  - 允许改动：上述 Claude Hook、测试和 UserPromptSubmit 配置。
  - 禁止改动：`.codex/`、技能正文、AGENTS/CLAUDE 根规则和业务代码。
  - 实施步骤：以 router 替代硬编码清单；保留斜杠与展开命令绕过；添加 stdin/stdout fixtures；更新 settings 指向。
  - 测试标准：与 T-03 使用同一 fixture 集；主技能、辅助技能、上限、无命中和注入防护均有断言。
  - 验收标准：普通输出 ≤1 KiB；不输出全量技能表；同一 fixture 与 Codex 路由 JSON 完全一致。
  - 验证命令：`node .claude/hooks/test/skill-router.test.cjs`，预期退出码 0 且输出所有 `[OK]`。
  - 审查清单：不回显用户输入；保留 command bypass；Hook 故障降级为空路由。
  - 失败处理边界：仅修复 T-04 文件，最多重试 2 次；settings 兼容性不明时停止并保留原 Hook 入口。
  - 完成证据：2026-08-19 执行 `node .claude/hooks/test/skill-router.test.cjs` 成功；7 个共享 fixture、展开命令绕过和注入防护均通过，最大普通输出 435 B；`.claude/settings.json` 无需变更，既有入口委托新的共享路由适配层；受影响文件为 `.claude/hooks/skill-forced-eval.cjs`、`.claude/hooks/skill-router.cjs`、`.claude/hooks/test/skill-router.test.cjs`。
  - 提交：`feat(claude): 精简技能路由注入`。

- [x] T-05. 生成双端薄入口规则（类型：配置）
  - 任务目标：将共同硬规则生成到两份根规则，同时把长模板和案例移至治理资料或按需 references。
  - 前置条件：T-02 的同步与校验脚本已可用。
  - 依赖：T-02；后继任务：T-06、T-07。
  - 执行模式：串行。
  - 文件所有权：`.agent-governance/core-rules.md`、`.agent-governance/templates/AGENTS.md.tpl`、`.agent-governance/templates/CLAUDE.md.tpl`、`AGENTS.md`、`CLAUDE.md`。
  - 共享契约：两个根规则直接包含相同的 core-rules 版本标识和硬规则正文。
  - 允许改动：上述规则源、模板、生成产物和 T-02 同步脚本。
  - 禁止改动：技能目录与 Hook 业务逻辑；不得仅写外部链接替代根规则硬约束。
  - 实施步骤：抽取不可迁移硬规则；编写双端模板；生成根规则；将模板与案例改为按需资料；运行一致性校验。
  - 测试标准：校验脚本检测 core-rules 版本、硬规则文本和模板渲染结果；故意篡改任一生成产物应失败。
  - 验收标准：两份根规则均直接包含中文、编码、架构、并发保护、UI 约束、按需加载和同步要求；详细模板不再出现在根规则中。
  - 验证命令：`node .agent-governance/scripts/verify-agent-assets.cjs`，预期退出码 0 且报告根规则一致。
  - 审查清单：不删编码与安全禁令；生成文件 UTF-8 无 BOM；不得扩大根规则的运行时注入。
  - 失败处理边界：仅修复模板、core-rules 与生成脚本，最多重试 2 次；检测到遗漏硬规则时停止，不发布半成品。
  - 完成证据：2026-08-19 执行 `node .agent-governance/scripts/sync-agent-assets.cjs` 成功生成双端根规则；`AGENTS.md` SHA-256 为 `70E39E34DA850E8A1EACB8FE112DA509426023164D412E61FF590898804D258D`，`CLAUDE.md` SHA-256 为 `4C77D80D5B8830CB06E61969FEAD1E218981D9F9096865CE3873D15C3C97A02B`；同步校验确认两份根规则与模板渲染完全一致，且均为 UTF-8 无 BOM。
  - 提交：`refactor(agent-governance): 生成双端薄入口规则`。

- [x] T-06. 试点拆分五个高频大技能（类型：技能改造）
  - 任务目标：将五个试点技能改成短入口 + 按需 references，同时保持 add-skill 的 Claude 主目录到 Codex 镜像同步。
  - 前置条件：T-02、T-05 已完成；五个试点原文已归档并可追溯。
  - 依赖：T-02、T-05；后继任务：T-07。
  - 执行模式：串行（共享 add-skill 同步契约）。
  - 文件所有权：`.claude/skills/{crud-development,ui-pc,ui-mobile,architecture-design,dev}/` 及同名 `.agents/skills/` 镜像目录。
  - 共享契约：每个 `SKILL.md` 保持原 name/description 语义；references 和入口在两端 SHA-256 一致。
  - 允许改动：试点五个技能及其 references；必要时更新 add-skill 的校验说明。
  - 禁止改动：其他技能、现有双端技能数量差异、`.codex/skills/`、业务代码。
  - 实施步骤：按主题拆分模板和案例；将入口收敛至任务路由、硬约束和 reference 索引；从 Claude 源目录同步镜像；校验哈希。
  - 测试标准：每个入口 ≤200 行；每个原有硬约束能在入口或指定 reference 中检索到；镜像差异为零。
  - 验收标准：五组技能与 references 双端 SHA-256 一致；新任务只需读取入口及被索引的必要 reference；不引入“双端技能分类”。
  - 验证命令：`node .agent-governance/scripts/verify-agent-assets.cjs`，预期退出码 0 且五组技能均报告 `[OK]`。
  - 审查清单：先改 `.claude/skills/` 再同步 `.agents/skills/`；description 不扩大触发范围；UTF-8 无 BOM。
  - 失败处理边界：仅修复五个试点目录和同步脚本，最多重试 2 次；任一技能无法无损拆分则保留原技能并记录阻塞。
  - 完成证据：2026-08-19 执行 `node .agent-governance/scripts/verify-agent-assets.cjs` 成功；`crud-development`、`ui-pc`、`ui-mobile`、`architecture-design` 的入口与 `references/full-guide.md` 双端 SHA-256 均一致，入口行数依次为 31、21、19、21；`dev` 经用户确认按 `.claude/commands/dev.md` → `.agents/skills/dev/SKILL.md` 命令映射处理，正文一致且不新增 `.claude/skills/dev/`。此次任务级重基线：原“五组共享技能哈希”调整为“四组共享技能哈希 + dev 命令映射正文一致”，依据为用户 2026-08-19 的明确裁决及 add-skill 的命令映射规则；受影响下游 T-07 将按该规则回归。
  - 提交：`refactor(skills): 试点短入口与按需参考资料`。

- [x] T-07. 试点回归验证与灰度发布（类型：验证）
  - 任务目标：证明双端路由一致、上下文注入下降且所有硬规则与同步机制保持有效。
  - 前置条件：T-03、T-04、T-05、T-06 全部完成并具有局部验证证据。
  - 依赖：T-03、T-04、T-05、T-06；后继任务：T-08。
  - 执行模式：串行。
  - 文件所有权：`.agent-governance/fixtures/`、`.agent-governance/baseline/`、`.agent-governance/reports/`。
  - 共享契约：使用 T-01 基线和 T-02 fixture schema；不更改已发布路由规则来迁就测试。
  - 允许改动：测试样例、验证脚本、对比报告和任务完成证据。
  - 禁止改动：任何生产业务代码、双端 Hook 策略和技能内容（除非回归失败后回到其所属任务）。
  - 实施步骤：运行双端 Hook fixtures；测量字节数；运行同步校验；对比 T-01 基线；形成灰度报告与回滚判定。
  - 测试标准：覆盖 CRUD、建表、页面、报错、只读问答、性能分析、显式 `$skill`、斜杠命令、恢复会话、无命中、端专属技能和提示词注入。
  - 验收标准：所有 fixtures 通过；双端 route JSON 一致；所有指标满足本计划“验收指标”；不相关技能不被路由。
  - 验证命令：`node .agent-governance/scripts/verify-agent-assets.cjs`，预期退出码 0 且输出回归总数、通过数和所有 `[OK]`。
  - 审查清单：报告不含原始用户提示词；对比仅使用 T-01 基线；未通过时不标记任务完成。
  - 失败处理边界：只修复验证材料或退回直接责任任务，最多重试 2 次；超过上限记录错误原文、影响范围和回滚建议。
  - 完成证据：2026-08-19 运行 Codex 与 Claude Hook 测试、`verify-agent-assets.cjs` 及 `.agent-governance/reports/run-regression.cjs` 均成功；12/12 匿名路由样例通过，覆盖 CRUD、建表、页面、报错、只读、性能、显式技能、斜杠命令、恢复会话、无命中、端专属技能和注入防护；Codex/Claude 最大普通输出为 226 B/435 B，基线为 8685 B；根规则从 73834 B/23335 B 降至 4436 B/4496 B；报告位于 `.agent-governance/reports/regression-report.json`，回滚结论为不需要回滚。
  - 提交：`test(agent-governance): 验证上下文注入优化回归`。

- [ ] T-08. 全量拆分并同步其余共享技能（类型：技能改造）
  - 任务目标：在已完成五个试点的基础上，改造其余全部 Claude 共享技能为“短入口 + 按需 `references/`”，并由 `add-skill` 将每个完整技能包同步到 Codex 镜像。
  - 前置条件：T-06、T-07 已完成；`add-skill` 已具备同步后全量递归校验与受限自修复规则。
  - 依赖：T-06、T-07；后继任务：T-09。
  - 执行模式：串行（所有技能共享同一 `add-skill` 同步和校验契约）。
  - 文件所有权：除已完成试点外的 `.claude/skills/*/` 及同名 `.agents/skills/*/` 镜像目录；`.agent-governance/scripts/verify-agent-assets.cjs` 及其校验样例仅在校验能力不足时可修改。
  - 共享契约：每个 Claude 技能目录是内容源；对应 Codex 目录必须包含完全相同的常规文件树和 SHA-256 内容。`dev` 保持既有 Claude Command → Codex Skill 映射，不伪造 `.claude/skills/dev/`。
  - 允许改动：其余共享技能的入口、`references/`、必要的本地 `scripts/`；对应 Codex 镜像；必要时升级 `add-skill`、全量校验器及其样例。
  - 禁止改动：业务代码、`.codex/skills/`、Hook 路由策略；不得以删除规则、放宽断言、跳过目录或将失败转警告的方式获得校验通过。
  - 实施步骤：逐个读取现有技能并抽取任务必需的准则、触发边界与资料索引；将长教程、模板和案例移入同技能 `references/`；使用 `add-skill` 从 Claude 源同步完整目录到 Codex；每个技能或同一无冲突批次同步后立即运行全量校验，失败按 add-skill 的受限自修复流程处理。
  - 测试标准：每个入口保留原 `name` 与 `description` 语义且不扩大触发范围；入口不超过 200 行；每个原有硬约束可在入口或其索引资料中检索；校验器能够报告缺失、多余、内容不同的具体相对路径。
  - 验收标准：所有 `.claude/skills/*/` 均有同名 `.agents/skills/*/` 镜像，递归文件清单与 SHA-256 完全一致；所有改造后的技能均为短入口 + 按需资料；`dev` 命令映射仍正文一致；未出现重复 `.codex/skills/`。
  - 验证命令：`node .agent-governance/scripts/verify-agent-assets.cjs`，预期退出码 0，并逐个输出所有共享技能的完整技能包校验 `[OK]`。
  - 审查清单：每次改造均实际使用 `add-skill`；先改 Claude 源再同步 Codex；所有文件 UTF-8 无 BOM；同步失败的修复仅限镜像与校验能力；不将用户原文写入 fixture 或报告。
  - 失败处理边界：每个失败指纹最多重试 2 次；优先修复镜像，再在不降低质量断言的前提下升级校验器；同一失败仍存在则记录差异、尝试与恢复条件并停止该技能，不影响无依赖技能继续改造。
  - 完成证据：待执行。需记录技能总数、已改造数、每个技能的入口行数、完整技能包校验输出及所有失败指纹处理结果。
  - 提交：`refactor(skills): 全量拆分并同步共享技能`。

- [ ] T-09. 全量技能回归与治理验收（类型：验证）
  - 任务目标：在 T-08 后验证双端 Hook、根规则、全量共享技能镜像和 `add-skill` 受限自修复门禁共同有效。
  - 前置条件：T-08 全部成功，或所有被阻塞技能已有用户明确裁决。
  - 依赖：T-08；后继任务：无。
  - 执行模式：串行。
  - 文件所有权：`.agent-governance/fixtures/`、`.agent-governance/reports/`、本任务完成证据；回归失败时仅退回其直接责任任务。
  - 共享契约：不得为了回归通过而改变既有路由语义、降低镜像哈希断言或跳过任何 Claude 共享技能。
  - 允许改动：验证样例、报告与完成证据；校验能力缺陷只能按 T-08 的自修复边界处理。
  - 禁止改动：业务代码、技能内容、Hook 路由策略；除非回归失败后退回直接责任任务。
  - 实施步骤：运行双端 Hook 测试、全量治理校验和回归报告；检查每个共享技能的递归校验结果；模拟或审查一个镜像差异，确认校验失败可被定位且不会自动放宽断言。
  - 测试标准：原 12 类匿名路由样例全部通过；全量技能均输出 `[OK]`；镜像差异会使校验退出非 0 且指明差异路径。
  - 验收标准：所有治理验证退出码为 0；全量镜像完整一致；add-skill 对失败具备“修复镜像/必要时升级校验器/不放宽门禁/两次上限”的可复核证据。
  - 验证命令：`node .codex/hooks/test/skill-router.test.cjs`、`node .claude/hooks/test/skill-router.test.cjs`、`node .agent-governance/scripts/verify-agent-assets.cjs`、`node .agent-governance/reports/run-regression.cjs`，预期均退出 0。
  - 审查清单：报告不含原始用户提示词；UTF-8 无 BOM；无跳过技能的隐藏白名单；失败场景的恢复过程不改源技能规则。
  - 失败处理边界：只修复验证材料或退回直接责任任务，最多重试 2 次；超过上限记录失败指纹、影响范围和用户需要决定的最小事项。
  - 完成证据：待执行。需保存全量验证输出、差异防护断言和回归报告位置。
  - 提交：`test(agent-governance): 验收全量技能同步治理`。

## 验收指标

| 指标 | 目标 |
|---|---|
| Codex 普通请求的路由 Hook 输出 | 不超过 512 B |
| Claude 普通请求的路由 Hook 输出 | 不超过 1 KiB |
| Codex SessionStart 默认历史经验注入 | 不超过 512 B |
| 单次路由技能数 | 1 个主技能 + 最多 2 个辅助技能 |
| 必需依赖突破上限 | 仅 manifest 标记为 `required` 时允许，且必须输出原因 |
| 全量共享技能入口长度 | 不超过 200 行 |
| 全量 Claude 共享技能与 Codex 镜像一致性 | 递归文件清单及 SHA-256 一致 |
| 同步失败自修复 | 仅可修复镜像或增强校验器；不得放宽断言；同一失败最多 2 次 |
| 共同硬规则一致性 | 直接生成到两个根规则文件并校验通过 |
| 路由正确性 | 覆盖正向、负向、显式、恢复、冲突与注入样例 |

## 风险与回滚

| 风险 | 处理方式 |
|---|---|
| 路由过度收窄导致漏加载技能 | 保留 manifest 测试样例；试点后逐批完成全部技能改造并做全量回归。 |
| 新增或修改技能时双端目录发生漂移 | 强制由 `add-skill` 执行 `.claude/skills/` → `.agents/skills/` 同步，并递归校验每个完整技能包。 |
| 校验器对新目录结构覆盖不足 | 允许在不放宽断言的前提下升级校验器与样例；同一失败最多重试 2 次，仍失败则记录并阻塞。 |
| 瘦身后遗漏关键规则 | 将编码、架构禁令、并发安全列为根规则直接包含的不可迁移项。 |
| Hook 故障影响使用 | Hook 读取 manifest 失败时降级为空路由提示，不阻塞安全 Hook 和正常会话；安全 Hook 保持独立。 |
| 结构化经验被错误自动注入 | 仅允许人工审核、未过期且带固定标识的阻断级记录自动注入。 |

回滚方式：保留原 Hook 与根规则的版本标签；若路由回归失败，恢复上一版 Hook 和生成产物，不回退业务代码。

## 完成证据

- 同步脚本输出本次修改技能的一致性报告。
- Hook 样例输入与输出快照。
- 根规则、SessionStart、路由 Hook 的改造前后大小对比。
- 六类高频任务的双端路由回归结果。
