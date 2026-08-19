#!/usr/bin/env node
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..', '..', '..');
const claudeSkills = path.join(root, '.claude', 'skills');
const codexSkills = path.join(root, '.agents', 'skills');
const policyPath = path.join(root, '.agent-governance', 'skill-sync-policy.json');
const args = process.argv.slice(2);
const valueAfter = flag => {
  const index = args.indexOf(flag);
  return index >= 0 ? args[index + 1] : undefined;
};
const base = valueAfter('--base') || 'codex';
const selected = new Set((valueAfter('--skills') || '').split(',').map(item => item.trim()).filter(Boolean));
const fixDifferences = args.includes('--fix-differences');
let failed = false;

function print(level, message) { console.log(`[${level}] ${message}`); }
function fail(message) { failed = true; print('FAIL', message); }
function readBytes(file) { return fs.readFileSync(file); }
function hasBom(bytes) { return bytes.length >= 3 && bytes[0] === 0xef && bytes[1] === 0xbb && bytes[2] === 0xbf; }
function hash(file) { return crypto.createHash('sha256').update(readBytes(file)).digest('hex'); }
function listFiles(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) return listFiles(full).map(file => path.join(entry.name, file));
    return entry.isFile() ? [entry.name] : [];
  }).sort();
}
function text(file) { return fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''); }
function frontMatter(content) {
  const match = content.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n/);
  return match ? { raw: match[0], body: content.slice(match[0].length), fields: match[1] } : null;
}
function validateSkill(directory, name, side) {
  const entry = path.join(directory, 'SKILL.md');
  if (!fs.existsSync(directory)) return { ok: false, issue: `${side} 缺少技能目录` };
  if (!fs.existsSync(entry)) return { ok: false, issue: `${side} 缺少 SKILL.md` };
  if (hasBom(readBytes(entry))) return { ok: false, issue: `${side} SKILL.md 含 UTF-8 BOM` };
  const header = frontMatter(text(entry));
  if (!header) return { ok: false, issue: `${side} SKILL.md 缺少 YAML 头` };
  const nameMatch = header.fields.match(/^name:\s*([^\r\n]+)\s*$/m);
  if (!nameMatch || nameMatch[1].trim() !== name) return { ok: false, issue: `${side} YAML name 与目录名不一致` };
  for (const file of listFiles(directory)) {
    if (hasBom(readBytes(path.join(directory, file)))) return { ok: false, issue: `${side} ${file} 含 UTF-8 BOM` };
  }
  return { ok: true };
}
function validateCommand(file, side) {
  if (!fs.existsSync(file)) return { ok: false, issue: `${side} 缺少命令文件` };
  if (hasBom(readBytes(file))) return { ok: false, issue: `${side} 命令文件含 UTF-8 BOM` };
  if (!/^#\s+\S/m.test(text(file))) return { ok: false, issue: `${side} 命令文件缺少标题` };
  return { ok: true };
}
function bodyWithoutYaml(file) {
  const content = text(file);
  return (frontMatter(content)?.body || content).replace(/^# [^\r\n]+\r?\n\r?\n/, '');
}
function shouldCheck(name) { return !selected.size || selected.has(name); }
function policy() { return JSON.parse(fs.readFileSync(policyPath, 'utf8')); }
function comparePackages(name, source, target) {
  const sourceFiles = listFiles(source);
  const targetFiles = listFiles(target);
  const sourceSet = new Set(sourceFiles);
  const targetSet = new Set(targetFiles);
  const missing = sourceFiles.filter(file => !targetSet.has(file));
  const extra = targetFiles.filter(file => !sourceSet.has(file));
  const different = sourceFiles.filter(file => targetSet.has(file) && hash(path.join(source, file)) !== hash(path.join(target, file)));
  if (!missing.length && !extra.length && !different.length) return null;
  return { missing, extra, different };
}
function copyPackage(source, target) {
  fs.rmSync(target, { recursive: true, force: true });
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.cpSync(source, target, { recursive: true });
}

if (!['codex', 'claude'].includes(base)) {
  console.error('参数 --base 仅支持 codex 或 claude');
  process.exit(2);
}

const syncPolicy = policy();
const mappings = new Map(syncPolicy.commandMappings.map(item => [item.name, item]));
const codexExclusive = new Set(syncPolicy.codexExclusiveSkills || []);
const claudeExclusive = new Set(syncPolicy.claudeExclusiveCommands || []);
const baseRoot = base === 'codex' ? codexSkills : claudeSkills;
const otherRoot = base === 'codex' ? claudeSkills : codexSkills;
const baseNames = fs.readdirSync(baseRoot, { withFileTypes: true }).filter(entry => entry.isDirectory()).map(entry => entry.name).sort();
const names = new Set(baseNames);
for (const name of mappings.keys()) names.add(name);

for (const name of [...names].sort()) {
  if (!shouldCheck(name)) continue;
  const mapping = mappings.get(name);
  if ((base === 'codex' && codexExclusive.has(name)) || (base === 'claude' && claudeExclusive.has(name))) {
    print('SKIP', `${name} 已声明为 ${base} 端专属能力`);
    continue;
  }
  if (mapping) {
    const command = path.join(root, '.claude', 'commands', mapping.claudeCommand);
    const skill = path.join(codexSkills, mapping.codexSkill, 'SKILL.md');
    const baselineFile = base === 'codex' ? skill : command;
    if (!fs.existsSync(baselineFile)) { fail(`${name} 基准映射文件缺失：${baselineFile}`); continue; }
    const baseline = base === 'codex'
      ? validateSkill(path.dirname(skill), mapping.codexSkill, 'codex 基准')
      : validateCommand(command, 'claude 基准');
    if (!baseline.ok) { fail(`${name} ${baseline.issue}；请先由 add-skill 修复基准`); continue; }
    if (!fs.existsSync(command) || !fs.existsSync(skill)) { fail(`${name} 命令映射不完整`); continue; }
    if (bodyWithoutYaml(command) === bodyWithoutYaml(skill)) print('OK', `${name} Claude Command 与 Codex Skill 正文一致`);
    else if (fixDifferences) {
      if (base === 'codex') fs.writeFileSync(command, text(skill).replace(/^---\r?\n[\s\S]*?\r?\n---\r?\n/, ''), 'utf8');
      else {
        const existing = text(skill);
        const header = frontMatter(existing)?.raw;
        if (!header) { fail(`${name} 缺少可保留的 Codex YAML 头，无法自动修复映射`); continue; }
        fs.writeFileSync(skill, `${header}${text(command)}`, 'utf8');
      }
      print('FIXED', `${name} 命令映射已按 ${base} 基准修复`);
    } else fail(`${name} Claude Command 与 Codex Skill 正文不同`);
    continue;
  }
  const baselineDir = path.join(baseRoot, name);
  const otherDir = path.join(otherRoot, name);
  const baseline = validateSkill(baselineDir, name, `${base} 基准`);
  if (!baseline.ok) { fail(`${name} ${baseline.issue}；请先由 add-skill 修复基准`); continue; }
  const other = validateSkill(otherDir, name, `${base === 'codex' ? 'claude' : 'codex'} 对端`);
  if (!other.ok) {
    if (fixDifferences && fs.existsSync(baselineDir)) { copyPackage(baselineDir, otherDir); print('FIXED', `${name} 已按 ${base} 基准补齐对端技能包`); }
    else fail(`${name} ${other.issue}`);
    continue;
  }
  const diff = comparePackages(name, baselineDir, otherDir);
  if (!diff) print('OK', `${name} 完整技能包一致`);
  else if (fixDifferences) { copyPackage(baselineDir, otherDir); print('FIXED', `${name} 已按 ${base} 基准同步：缺少=${diff.missing.length}，多余=${diff.extra.length}，内容不同=${diff.different.length}`); }
  else fail(`${name} 技能包不同：缺少=${diff.missing.join(',') || '无'}；多余=${diff.extra.join(',') || '无'}；内容不同=${diff.different.join(',') || '无'}`);
}

process.exit(failed ? 1 : 0);
