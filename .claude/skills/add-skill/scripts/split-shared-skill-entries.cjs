#!/usr/bin/env node
/*
 * 将共享技能的长入口无损归档为按需资料，并同步 Claude 源到 Codex 镜像。
 * 仅处理尚未拆分的共享技能；不会修改命令映射或 .codex/skills。
 */
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..', '..', '..');
const claudeRoot = path.join(root, '.claude', 'skills');
const codexRoot = path.join(root, '.agents', 'skills');
const refreshIndexes = process.argv.slice(2).includes('--refresh-indexes');

function read(file) {
  return fs.readFileSync(file, 'utf8');
}

function write(file, content) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, content.replace(/^\uFEFF/, ''), 'utf8');
}

function splitFrontMatter(source, name) {
  const match = source.match(/^(---\r?\n[\s\S]*?\r?\n---\r?\n)([\s\S]*)$/);
  if (!match) throw new Error(`${name} 缺少 YAML 头部`);
  return { frontMatter: match[1], body: match[2] };
}

function buildEntry(name, frontMatter, body) {
  const headings = [...body.matchAll(/^##\s+(.+)$/gm)].map(match => match[1].trim());
  const index = headings.length
    ? headings.map(heading => `- ${heading}`).join('\n')
    : '- 完整执行规范与历史案例';
  return `${frontMatter}# ${name}\n\n## 执行边界\n\n- 本入口只保留触发说明、最小执行原则与资料索引；开始实质实施前，先识别任务涉及的专题，再按需阅读对应完整资料。\n- 项目根规则中的中文、编码、安全、架构及并发保护要求始终优先；不得因资料拆分降低既有约束。\n- 不需要完整资料的只读解释、状态查询或简单定位，不得默认加载全文。\n\n## 最小步骤\n\n1. 根据当前任务确定所需专题。\n2. 按需读取 \`references/full-guide.md\` 中相关章节，并遵守其中原有硬约束。\n3. 仅在任务范围内实施并执行受影响范围的验证。\n\n## 资料索引\n\n- \`references/full-guide.md\`：原入口的完整规范、模板、案例和边界。主要专题：\n${index}\n`;
}

function sync(source, mirror) {
  fs.rmSync(mirror, { recursive: true, force: true });
  fs.cpSync(source, mirror, { recursive: true });
}

const converted = [];
for (const entry of fs.readdirSync(claudeRoot, { withFileTypes: true }).filter(item => item.isDirectory()).sort((a, b) => a.name.localeCompare(b.name))) {
  const name = entry.name;
  const source = path.join(claudeRoot, name);
  const skillFile = path.join(source, 'SKILL.md');
  const guideFile = path.join(source, 'references', 'full-guide.md');
  if (!fs.existsSync(skillFile)) continue;
  const { frontMatter, body } = splitFrontMatter(read(skillFile), name);
  if (fs.existsSync(guideFile)) {
    if (!refreshIndexes || !body.includes('原入口的完整规范、模板、案例和边界。主要专题：')) continue;
    const refreshed = buildEntry(name, frontMatter, read(guideFile));
    const mirrorSkill = path.join(codexRoot, name, 'SKILL.md');
    if (read(skillFile) === refreshed && fs.existsSync(mirrorSkill) && read(mirrorSkill) === refreshed) continue;
    if (read(skillFile) !== refreshed) write(skillFile, refreshed);
  } else {
    write(guideFile, body);
    write(skillFile, buildEntry(name, frontMatter, body));
  }
  sync(source, path.join(codexRoot, name));
  converted.push(name);
}

// 本脚本属于 add-skill 的完整技能包，也一并同步，保持递归镜像门禁成立。
sync(path.join(claudeRoot, 'add-skill'), path.join(codexRoot, 'add-skill'));
console.log(`已拆分并同步 ${converted.length} 个共享技能：${converted.join(', ')}`);
