
# 技能创建与维护指南

## 治理模型

技能内容、自动路由和双端规则分别由不同位置负责，修改时不得混淆职责。

| 职责 | 唯一维护位置 | 说明 |
| --- | --- | --- |
| Claude 技能正文 | `.claude/skills/<名称>/SKILL.md` | 技能内容主副本 |
| Codex 技能镜像 | `.agents/skills/<名称>/SKILL.md` | 与 Claude 技能入口和参考资料保持一致 |
| 自动路由规则 | `.agent-governance/skills-manifest.json` | Hook 使用的唯一技能清单、触发词、优先级和依赖关系 |
| 公共路由实现 | `.agent-governance/lib/router.cjs` | Codex 与 Claude Hook 的共同路由逻辑，通常无需因新增技能修改 |
| 双端根规则 | `.agent-governance/core-rules.md`、`templates/` | 通过渲染脚本生成 `AGENTS.md` 与 `CLAUDE.md` |
| 一致性验收 | `.agent-governance/scripts/verify-agent-assets.cjs` | 校验路由、根规则、试点镜像和命令映射 |

> 不再在 `.claude/hooks/skill-forced-eval.cjs` 手写技能列表，也不手动编辑 `AGENTS.md`、`CLAUDE.md` 的技能表。Hook 只负责调用公共 router。

> 常规维护以 Claude 技能目录为内容源。唯一例外是 `skill-check` 已完成基准自检且用户明确要求“差异直接修复”时：本次修复以 `skill-check` 指定的合格基准为源同步另一端；该例外不改变后续常规维护的默认来源。

## 目录边界

- Claude Code 技能目录：`.claude/skills/`。
- Codex 技能目录：`.agents/skills/`；不要创建或恢复 `.codex/skills/`。
- Codex 的 Hook、配置和子 Agent 仍在 `.codex/`，不要迁移。
- Claude Command 位于 `.claude/commands/<名称>.md`，只供 Claude Code 的 `/名称` 使用。
- Codex 不读取项目级 `.claude/commands/`；需要等价能力时，创建 `.agents/skills/<名称>/SKILL.md` 镜像，由 `$名称` 或自然语言触发。

## 新增或修改技能流程

### 1. 确定边界

先读取至少一个同领域的现有技能，确认没有重叠。技能名称使用 kebab-case；一个技能只解决一个明确领域。

若技能需要自动触发，检查 `.agent-governance/skills-manifest.json` 是否已有等价路由。禁止使用单独的泛词作为触发条件，例如“开发”“优化”“方案”。

### 2. 编写技能入口

在 `.claude/skills/<名称>/SKILL.md` 编写或更新入口，必须使用 UTF-8 无 BOM，并包含 YAML 头部：

```yaml
---
name: example-skill
description: |
  当需要处理明确的业务领域时自动使用此 Skill。

  触发场景：
  - 需要处理具体场景一
  - 需要处理具体场景二
  - 需要处理具体场景三

  触发词：关键词一、关键词二、关键词三、关键词四、关键词五
---
```

入口只保留执行准则、关键步骤和资料索引。较长的教程、历史案例或完整指南放在同技能目录的 `references/`，例如：

```text
.claude/skills/example-skill/
├─ SKILL.md
└─ references/
   └─ full-guide.md
```

不要把参考资料放入其他技能目录；镜像时 `SKILL.md` 和 `references/` 必须一起同步。

### 3. 配置自动路由

仅当技能需要通过自然语言自动激活时，在 `.agent-governance/skills-manifest.json` 的 `skills` 数组新增或更新条目：

```json
{
  "name": "example-skill",
  "priority": 80,
  "includeAny": ["精确触发词一", "精确触发词二"],
  "excludeAny": ["只回答", "解释"],
  "dependencies": [
    { "whenAny": ["附加条件"], "skills": ["dependency-skill"], "required": true }
  ]
}
```

规则要求：

- `name` 必须对应实际技能目录名。
- `includeAny` 只放领域特征明显的短语；需要解释、定位文件等只读意图应放入 `excludeAny`。
- `priority` 用于解决多个候选技能；依赖通过 `dependencies` 声明，`required: true` 表示不可省略。
- 同一次任务最多选择 `maxSkillsPerTask` 个技能，避免无关上下文注入。
- 更新路由同时在 `.agent-governance/fixtures/router-fixtures.json` 补充正例、负例及依赖样例，且只记录样例 ID 和预期路由，不写入真实用户内容。

### 4. 同步 Codex 镜像

创建对应目录并同步完整技能包：

```powershell
New-Item -ItemType Directory -Force -Path .agents/skills/<名称> | Out-Null
Copy-Item -Recurse -Force .claude/skills/<名称>/* .agents/skills/<名称>/
```

随后比较入口与参考资料的哈希，确保双端一致。不要将技能复制到 `.codex/skills/`。

当 `skill-check` 明确指定“以 Codex 为基准并差异直接修复”时，允许反向将 `.agents/skills/<名称>/` 的完整技能包同步至 `.claude/skills/<名称>/`。仅处理 `skill-check` 报告的精确技能和文件；同步前再次确认基准校验已通过，同步后立即重跑 `skill-check` 与本节验收命令。

对 Claude Command → Codex Skill 映射，Codex 为基准时仅将去除 YAML 头后的正文同步回 `.claude/commands/<名称>.md`；不得把 YAML 头写入 Claude Command，也不得影响未在 `.agent-governance/skill-sync-policy.json` 声明的命令。

### 5. 强制校验与受限自修复

每次同步完成后，必须立即运行本技能的完整验收命令；未通过前不得把本次技能改造标记完成、提交或继续处理下一技能。`verify-agent-assets.cjs` 会递归比较每个 `.claude/skills/<名称>/` 技能包与 `.agents/skills/<名称>/` 镜像，包含 `SKILL.md`、`references/`、`scripts/` 等所有常规文件。

校验失败时按下列顺序处理，这就是本技能的“自我进化”边界：

1. 先依据失败清单，从 Claude 源目录重新同步缺失、内容不同或多余的镜像文件，再重跑校验。
2. 若失败显示校验器未覆盖新技能包结构、错误归因不准确或无法给出可操作的差异，允许在不降低断言的前提下升级 `.agent-governance/scripts/verify-agent-assets.cjs`，并补充相应的可复核样例后重跑。
3. 最多自动修复 2 次；同一失败指纹仍存在时，停止并记录差异、已尝试修复和恢复条件，交由用户决定。

禁止为了“通过”而放宽哈希比较、跳过技能、将失败改成警告，或擅自修改 Claude 源技能的业务规则、路由触发词和用户需求。自我进化只可修复同步流程和校验能力，不能绕过质量门禁。

### 6. Command 的特殊处理

Claude Command 的固定发现入口必须保留在 `.claude/commands/<名称>.md`，不要改为子目录入口，以免 `/名称` 无法发现。

命令正文过长时，将完整资料放在同级命名空间目录：

```text
.claude/commands/
├─ dev.md
└─ dev-references/
   └─ full-guide.md
```

若需让 Codex 使用同等能力，在 `.agents/skills/<名称>/SKILL.md` 创建带 YAML 头部的镜像；除 YAML 头部外，正文必须与 Claude Command 保持一致。`*-local` 命令只属于 Claude Code，不创建 Codex 镜像。

## 根规则维护

需要修改通用根规则时，只编辑 `.agent-governance/core-rules.md` 或相应的 `.agent-governance/templates/*.tpl`，然后执行：

```powershell
node .agent-governance/scripts/sync-agent-assets.cjs
```

该命令生成根目录 `AGENTS.md` 与 `CLAUDE.md`。禁止为了声明一个技能而直接手改这两个生成文件。

## 验收

完成任一技能、路由、命令或根规则调整后，依次执行：

```powershell
node .codex/hooks/test/skill-router.test.cjs
node .claude/hooks/test/skill-router.test.cjs
node .agent-governance/scripts/verify-agent-assets.cjs
node .agent-governance/reports/run-regression.cjs
```

验收通过的最低标准：

- 路由 fixture 全部通过，且无泛词触发。
- 两端 Hook 都不回显用户原文；无匹配、显式斜杠命令和恢复会话时静默绕过。
- 技能入口及其 `references/` 镜像哈希一致。
- 根规则严格等于模板渲染结果。
- Command 与 Codex 镜像去除 YAML 后正文一致。
- 所有新增或修改文件均为 UTF-8 无 BOM。
- 任一 Claude 共享技能的完整技能包均已递归通过镜像校验；不能仅校验本次试点或只校验 `SKILL.md`。

## 常见错误

| 错误 | 正确做法 |
| --- | --- |
| 在 Hook 中追加技能关键词 | 只修改 `skills-manifest.json`，由公共 router 自动生效 |
| 手动同步 AGENTS.md、CLAUDE.md | 修改治理模板或共同规则后执行同步脚本 |
| 用“开发”“优化”等泛词路由 | 使用可区分领域的精确短语，并增加负例 fixture |
| 只复制 `SKILL.md` | 连同 `references/` 一起复制并校验哈希 |
| 校验失败后跳过或降低断言 | 先同步修复；必要时升级校验器但不放宽质量门禁，最多重试 2 次 |
| 将 `dev.md` 移入 `dev/` 子目录 | 保留 `.claude/commands/dev.md`，资料放 `dev-references/` |
| 在 `.codex/skills/` 建镜像 | 只使用 `.agents/skills/` |
