<!-- 此文件由 .agent-governance/scripts/sync-agent-assets.cjs 生成，请修改模板或 core-rules.md。 -->

{{CORE_RULES}}

## Codex 入口

- Codex 自动发现 `.agents/skills/` 的技能元数据；开始实质实现前读取被路由技能的 `SKILL.md`。
- `.codex/hooks/skill-forced-eval.cjs` 仅提供本轮轻量路由提示；`PreToolUse` 继续独立执行安全与范围校验。
