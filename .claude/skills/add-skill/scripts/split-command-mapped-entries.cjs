#!/usr/bin/env node
/*
 * 将策略文件声明的 Claude Command / Codex Skill 映射迁移为短入口 + 完整资料。
 * 普通映射以 Codex 为基准；双入口技能必须显式选择 Claude 或 Codex 基准。
 */
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..', '..', '..');
const policy = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance', 'skill-sync-policy.json'), 'utf8'));
const codexRoot = path.join(root, '.agents', 'skills');
const commandRoot = path.join(root, '.claude', 'commands');
const args = process.argv.slice(2);
const valueAfter = flag => {
  const index = args.indexOf(flag);
  return index >= 0 ? args[index + 1] : undefined;
};
const dualEntryBase = valueAfter('--dual-entry-base');
const dualEntries = new Set((policy.dualEntrySkills || []).map(item => item.name));

function read(file) { return fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''); }
function write(file, content) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, content.replace(/^\uFEFF/, ''), 'utf8');
}
function splitFrontMatter(content, name) {
  const matched = content.match(/^(---\r?\n[\s\S]*?\r?\n---\r?\n)([\s\S]*)$/);
  if (!matched) throw new Error(`${name} 缺少 YAML 头部`);
  return { frontMatter: matched[1], body: matched[2] };
}
function buildEntry(name, frontMatter, fullGuide, title = name) {
  const headings = [...fullGuide.matchAll(/^##\s+(.+)$/gm)].map(item => `- ${item[1].trim()}`);
  const index = headings.length ? headings.join('\n') : '- 完整执行规范与历史案例';
  return `${frontMatter}# ${title}\n\n## 执行边界\n\n- 本入口只保留触发说明、最小执行原则与资料索引；开始实质实施前，先识别任务涉及的专题，再按需阅读对应完整资料。\n- 项目根规则中的中文、编码、安全、架构及并发保护要求始终优先；不得因资料拆分降低既有约束。\n- 不需要完整资料的只读解释、状态查询或简单定位，不得默认加载全文。\n\n## 最小步骤\n\n1. 根据当前任务确定所需专题。\n2. 按需读取 \`references/full-guide.md\` 中相关章节，并遵守其中原有硬约束。\n3. 仅在任务范围内实施并执行受影响范围的验证。\n\n## 资料索引\n\n- \`references/full-guide.md\`：原入口的完整规范、模板、案例和边界。主要专题：\n${index}\n`;
}
function normalizeName(frontMatter, name) {
  return frontMatter.replace(/^name:\s*[^\r\n]+\s*$/m, `name: ${name}`);
}

const overlaps = (policy.commandMappings || []).filter(mapping =>
  fs.existsSync(path.join(root, '.claude', 'skills', mapping.codexSkill))
);
const undeclared = overlaps.filter(mapping => !dualEntries.has(mapping.name));
if (undeclared.length) {
  throw new Error(`检测到未声明的命令/共享技能重叠：${undeclared.map(item => item.name).join(', ')}`);
}
if (overlaps.length && !['claude', 'codex'].includes(dualEntryBase)) {
  throw new Error(`双入口技能 ${overlaps.map(item => item.name).join(', ')} 要求显式传入 --dual-entry-base claude|codex`);
}

const converted = [];
for (const mapping of policy.commandMappings || []) {
  const name = mapping.name;
  const skill = path.join(codexRoot, mapping.codexSkill, 'SKILL.md');
  const command = path.join(commandRoot, mapping.claudeCommand);
  if (!fs.existsSync(skill) || !fs.existsSync(command)) throw new Error(`${name} 映射文件缺失`);
  const sharedSource = path.join(root, '.claude', 'skills', mapping.codexSkill);
  const isDualEntry = dualEntries.has(name);
  const baselineSkill = isDualEntry && dualEntryBase === 'claude'
    ? path.join(sharedSource, 'SKILL.md')
    : skill;
  const { frontMatter, body } = splitFrontMatter(read(baselineSkill), name);
  const normalizedFrontMatter = normalizeName(frontMatter, mapping.codexSkill);
  const codexGuide = path.join(path.dirname(skill), 'references', 'full-guide.md');
  const sharedGuide = path.join(sharedSource, 'references', 'full-guide.md');
  const commandStem = path.basename(mapping.claudeCommand, '.md');
  const commandGuide = path.join(commandRoot, `${commandStem}-references`, 'full-guide.md');
  const fullGuide = isDualEntry && dualEntryBase === 'claude' && fs.existsSync(sharedGuide)
    ? read(sharedGuide)
    : fs.existsSync(codexGuide)
    ? read(codexGuide)
    : (fs.existsSync(commandGuide) ? read(commandGuide) : body);
  const title = mapping.codexSkill === 'dev' ? '/dev - 开发新功能' : mapping.codexSkill;
  const entry = buildEntry(mapping.codexSkill, normalizedFrontMatter, fullGuide, title);
  write(codexGuide, fullGuide);
  write(skill, entry);
  write(commandGuide, fullGuide);
  write(command, entry.slice(normalizedFrontMatter.length));
  const codexSkillDir = path.dirname(skill);
  if (isDualEntry && dualEntryBase === 'claude') {
    write(sharedGuide, fullGuide);
    write(path.join(sharedSource, 'SKILL.md'), entry);
  } else if (isDualEntry) {
    fs.rmSync(sharedSource, { recursive: true, force: true });
    fs.cpSync(codexSkillDir, sharedSource, { recursive: true });
  }
  converted.push(name);
}

console.log(`已迁移 ${converted.length} 个命令映射：${converted.join(', ')}`);
