#!/usr/bin/env node
/** 将模板渲染为双端根规则；技能镜像由 add-skill 维护。 */
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..');
const governance = path.join(root, '.agent-governance');
const corePath = path.join(governance, 'core-rules.md');
const templates = [
  ['AGENTS.md.tpl', 'AGENTS.md'],
  ['CLAUDE.md.tpl', 'CLAUDE.md']
];

if (!fs.existsSync(corePath)) {
  console.log('[SKIP] 尚未提供 core-rules.md');
  process.exit(0);
}
const core = fs.readFileSync(corePath, 'utf8');
for (const [templateName, targetName] of templates) {
  const templatePath = path.join(governance, 'templates', templateName);
  if (!fs.existsSync(templatePath)) {
    console.log(`[SKIP] 尚未提供 templates/${templateName}`);
    continue;
  }
  const rendered = fs.readFileSync(templatePath, 'utf8').replace('{{CORE_RULES}}', core);
  fs.writeFileSync(path.join(root, targetName), rendered, 'utf8');
  console.log(`[OK] 已生成 ${targetName}`);
}
