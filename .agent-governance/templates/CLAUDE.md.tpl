<!-- 此文件由 .agent-governance/scripts/sync-agent-assets.cjs 生成，请修改模板或 core-rules.md。 -->

{{CORE_RULES}}

## Claude Code 入口

- `.claude/hooks/skill-forced-eval.cjs` 决定本轮主技能和辅助技能；Hook 只输出路由，不注入全量技能清单或技能正文。
- 开始实质实现前按路由读取 `.claude/skills/<技能名>/SKILL.md`；`PreToolUse` 与 `Stop` Hook 继续独立承担安全拦截和同步提醒。
