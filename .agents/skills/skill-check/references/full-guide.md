
# 双端技能校验器

## 目标与默认行为

`skill-check` 用于检查 Claude Code 与 Codex 的技能包、Claude Command → Codex Skill 映射、端专属能力声明，以及全部技能名称的调用路由。它不改变路由规则，也不把“能通过校验”当作降低技能质量的理由。

- `$skill-check`：检查全部技能，默认以 `.agents/skills/`（Codex）为基准。
- `$skill-check <技能名>`：只检查用户列出的一个或多个技能。
- 用户明确“以 Claude Code 为准”时，基准切换为 `.claude/skills/` 与已声明的 Claude Command。
- 用户明确“差异直接修复”时，才允许以当前基准覆盖差异端；普通检查只输出结构化差异，等待用户确认。

## 执行流程

### 1. 先校验并修复基准

比较前必须校验当前基准的完整性：目录和入口存在、UTF-8 无 BOM、YAML 头可解析、`name` 与目录名一致，并且符合当前资料分层规范：必须存在 `references/full-guide.md`、`SKILL.md` 不超过 200 行、入口正文必须索引 `references/full-guide.md`。同时校验 `references/`、`scripts/`、`templates/` 与 `presets/` 的已声明内部路径不悬空。

基准存在可确定的结构问题时，直接委托 `add-skill` 修复基准并复验；例如去除 BOM、修正与目录不一致的 `name`、补齐可由该基准自身确定的资料路径或资料索引。不得从另一端复制内容来“修复”基准。

基准无法安全修复时（如缺少无法推导内容的完整资料），停止比较并报告问题、已尝试修复和最小恢复条件。

### 2. 比较双端能力

基准合格后运行：

```powershell
node .claude/skills/skill-check/scripts/skill-check.cjs --base codex
```

对普通共享技能递归比较完整文件树和 SHA-256 内容；对命令映射，比较 Claude Command 与去除 Codex YAML 头后的正文及其声明资料。映射和端专属能力唯一维护在 `.agent-governance/skill-sync-policy.json`，未声明的缺失、多余或内容差异均为失败。

同时校验 `namedSkillRouting.skills` 与 Codex 技能目录完全一致，并逐一验证“调用意图 + 技能名”在 Codex 端可路由；Claude 端仅允许共享技能或已声明 Command 映射路由，端专属能力必须被过滤。

### 3. 差异处理

- 默认：输出 `缺失`、`多余`、`内容不同`、`命令映射不同` 及精确相对路径，不修改差异端。
- 用户确认后，或请求中已明确“差异直接修复”：委托 `add-skill` 按当前基准同步精确技能包/命令映射，随后重新执行基准校验与完整比较。
- 同一失败指纹最多修复 2 次；不得跳过技能、把失败降级为警告、放宽哈希比较或覆盖未列出的文件。

## 修复边界

“以 Codex 为基准”只在本次检查中确定同步方向，不改变项目的常规技能维护职责。每次修复必须记录：基准端、受影响能力、修改文件、修复原因、复验结果。

端专属能力只允许出现在策略文件的显式声明中，并以 `SKIP（已声明端专属）` 报告；不得因为端专属而隐藏普通共享技能的缺失。

## 验收

完成检查或修复后执行：

```powershell
node .claude/skills/skill-check/scripts/skill-check.cjs --base codex
node .agent-governance/scripts/verify-agent-assets.cjs
node .codex/hooks/test/skill-router.test.cjs
node .claude/hooks/test/skill-router.test.cjs
```

全部检查仅在无未声明差异、所有基准有效、映射与端专属声明有效时退出 0。所有新增或修改文件必须为 UTF-8 无 BOM。
