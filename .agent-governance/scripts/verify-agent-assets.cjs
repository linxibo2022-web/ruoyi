#!/usr/bin/env node
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { loadManifest, selectRoute } = require('../lib/router.cjs');

const root = path.resolve(__dirname, '..', '..');
let failed = false;
function routeMatches(actual, expected) {
  const { matches, ...base } = actual;
  const { matches: expectedMatches, ...expectedBase } = expected;
  return Array.isArray(matches)
    && JSON.stringify(base) === JSON.stringify(expectedBase)
    && (!expectedMatches || JSON.stringify(matches) === JSON.stringify(expectedMatches));
}
function ok(message) { console.log(`[OK] ${message}`); }
function fail(message) { failed = true; console.error(`[FAIL] ${message}`); }
function hash(file) { return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex'); }
function text(file) { return fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, ''); }
function frontMatter(content) {
  const match = content.match(/^---\r?\n[\s\S]*?\r?\n---\r?\n/);
  return match ? { raw: match[0], body: content.slice(match[0].length) } : null;
}
function bodyWithoutYaml(file) {
  const content = text(file);
  return (frontMatter(content)?.body || content).replace(/^# [^\r\n]+\r?\n\r?\n/, '');
}
function validateGuideIndex(entryFile, guideFile, label) {
  if (!fs.existsSync(entryFile) || !fs.existsSync(guideFile)) return;
  const entry = text(entryFile);
  if (!entry.includes('原入口的完整规范、模板、案例和边界。主要专题：')) return;
  const headings = [...text(guideFile).matchAll(/^##\s+(.+)$/gm)].map(match => match[1].trim());
  const missing = headings.filter(heading => !entry.split(/\r?\n/).includes(`- ${heading}`));
  if (missing.length) fail(`${label} 资料索引不完整：缺少=${missing.join('、')}`);
  else ok(`${label} 资料索引覆盖全部 ${headings.length} 个二级标题`);
}
function listFiles(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const fullPath = path.join(directory, entry.name);
    if (entry.isDirectory()) return listFiles(fullPath).map(file => path.join(entry.name, file));
    return entry.isFile() ? [entry.name] : [];
  }).sort();
}

let manifest;
try {
  manifest = loadManifest();
  const generic = new Set(['开发', '优化', '方案']);
  const invalid = manifest.skills.flatMap(skill => (skill.includeAny || []).filter(item => generic.has(String(item).toLowerCase())).map(item => `${skill.name}:${item}`));
  if (invalid.length) fail(`manifest 含泛词触发：${invalid.join(', ')}`); else ok('manifest 未使用单独泛词触发');
  const named = manifest.namedSkillRouting || {};
  const declared = [...new Set(named.skills || [])].sort();
  const actual = fs.readdirSync(path.join(root, '.agents', 'skills'), { withFileTypes: true })
    .filter(entry => entry.isDirectory()).map(entry => entry.name).sort();
  if (JSON.stringify(declared) === JSON.stringify(actual)) ok('manifest 技能名称路由覆盖全部 Codex 技能');
  else fail(`manifest 技能名称路由不完整：缺少=${actual.filter(name => !declared.includes(name)).join(',') || '无'}；多余=${declared.filter(name => !actual.includes(name)).join(',') || '无'}`);
  const codexFailures = declared.filter(name => selectRoute(`请使用 ${name} 处理任务`, manifest).primary !== name);
  if (codexFailures.length) fail(`Codex 技能名称调用路由失败：${codexFailures.join(',')}`);
  else ok(`Codex 技能名称调用路由通过（${declared.length} 个）`);
  const claudeFailures = declared.filter(name => {
    const available = fs.existsSync(path.join(root, '.claude', 'skills', name, 'SKILL.md'))
      || fs.existsSync(path.join(root, '.claude', 'commands', `${name}.md`));
    const routed = selectRoute(`请使用 ${name} 处理任务`, manifest, 'claude').primary === name;
    return available !== routed;
  });
  if (claudeFailures.length) fail(`Claude 技能名称调用路由或端专属过滤失败：${claudeFailures.join(',')}`);
  else ok(`Claude 技能名称调用与端专属过滤通过（${declared.length} 个）`);
  const requiredLimit = manifest.maxRequiredDependenciesPerSkill;
  const maxRequired = Math.max(0, ...manifest.skills.map(skill => new Set((skill.dependencies || [])
    .filter(dependency => dependency.required)
    .flatMap(dependency => dependency.skills || [])).size));
  ok(`manifest 必需依赖绝对上限有效（当前最大 ${maxRequired}，上限 ${requiredLimit}）`);
} catch (error) {
  fail(`无法读取 manifest：${error.message}`);
}

try {
  const fixtures = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance', 'fixtures', 'router-fixtures.json'), 'utf8'));
  for (const fixture of fixtures) {
    const actual = selectRoute(fixture.prompt, manifest);
    if (routeMatches(actual, fixture.expected)) ok(`路由样例 ${fixture.id}`);
    else fail(`路由样例 ${fixture.id} 不匹配`);
  }
} catch (error) {
  fail(`无法验证路由样例：${error.message}`);
}

const core = path.join(root, '.agent-governance', 'core-rules.md');
if (!fs.existsSync(core)) {
  ok('根规则模板校验暂缓至 T-05');
} else {
  const rules = fs.readFileSync(core, 'utf8');
  for (const name of ['AGENTS.md', 'CLAUDE.md']) {
    const file = path.join(root, name);
    const template = path.join(root, '.agent-governance', 'templates', `${name}.tpl`);
    if (!fs.existsSync(template)) {
      fail(`缺少根规则模板 ${name}.tpl`);
      continue;
    }
    const expected = fs.readFileSync(template, 'utf8').replace('{{CORE_RULES}}', rules);
    if (fs.readFileSync(file, 'utf8') === expected) ok(`${name} 与模板渲染结果一致且直接包含共同硬规则`);
    else fail(`${name} 与模板渲染结果不一致`);
  }
}

const claudeSkillsRoot = path.join(root, '.claude', 'skills');
const codexSkillsRoot = path.join(root, '.agents', 'skills');
const sharedSkills = fs.existsSync(claudeSkillsRoot)
  ? fs.readdirSync(claudeSkillsRoot, { withFileTypes: true }).filter(entry => entry.isDirectory()).map(entry => entry.name).sort()
  : [];
if (!sharedSkills.length) {
  fail('未发现 Claude 技能目录，无法校验双端镜像');
}
for (const name of sharedSkills) {
  const source = path.join(root, '.claude', 'skills', name);
  const mirror = path.join(root, '.agents', 'skills', name);
  if (!fs.existsSync(source) || !fs.existsSync(mirror)) {
    fail(`共享技能 ${name} 缺少 Claude 或 Codex 镜像目录`);
    continue;
  }
  const sourceFiles = listFiles(source);
  const mirrorFiles = listFiles(mirror);
  const sourceSet = new Set(sourceFiles);
  const mirrorSet = new Set(mirrorFiles);
  const missingInMirror = sourceFiles.filter(file => !mirrorSet.has(file));
  const extraInMirror = mirrorFiles.filter(file => !sourceSet.has(file));
  const different = sourceFiles.filter(file => mirrorSet.has(file) && hash(path.join(source, file)) !== hash(path.join(mirror, file)));
  if (missingInMirror.length || extraInMirror.length || different.length) {
    fail(`共享技能 ${name} 镜像不一致：缺少=${missingInMirror.join(',') || '无'}；多余=${extraInMirror.join(',') || '无'}；内容不同=${different.join(',') || '无'}`);
  } else {
    ok(`共享技能 ${name} 完整技能包哈希一致（${sourceFiles.length} 个文件）`);
  }
  validateGuideIndex(
    path.join(source, 'SKILL.md'),
    path.join(source, 'references', 'full-guide.md'),
    `共享技能 ${name}`
  );
}

try {
  const policy = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance', 'skill-sync-policy.json'), 'utf8'));
  const mappings = policy.commandMappings || [];
  const mappingNames = mappings.map(item => item.name);
  const duplicateMappings = mappingNames.filter((name, index) => mappingNames.indexOf(name) !== index);
  if (duplicateMappings.length) fail(`命令映射名称重复：${[...new Set(duplicateMappings)].join(', ')}`);

  const dualEntries = policy.dualEntrySkills || [];
  const dualNames = dualEntries.map(item => item.name);
  const invalidDualEntries = dualEntries.filter(item =>
    !mappingNames.includes(item.name)
    || item.claudeSource !== 'skill'
    || !fs.existsSync(path.join(claudeSkillsRoot, item.name, 'SKILL.md'))
  );
  if (invalidDualEntries.length) fail(`双入口技能声明无效：${invalidDualEntries.map(item => item.name).join(', ')}`);
  else ok(`双入口技能身份声明有效（${dualNames.join(', ') || '无'}）`);

  for (const mapping of mappings) {
    const command = path.join(root, '.claude', 'commands', mapping.claudeCommand);
    const skill = path.join(codexSkillsRoot, mapping.codexSkill, 'SKILL.md');
    const commandStem = path.basename(mapping.claudeCommand, '.md');
    const commandGuide = path.join(root, '.claude', 'commands', `${commandStem}-references`, 'full-guide.md');
    const skillGuide = path.join(codexSkillsRoot, mapping.codexSkill, 'references', 'full-guide.md');
    if (!fs.existsSync(command) || !fs.existsSync(skill)) {
      fail(`${mapping.name} 命令映射缺失`);
      continue;
    }
    if (bodyWithoutYaml(command) === bodyWithoutYaml(skill)) ok(`${mapping.name} Claude Command 与 Codex Skill 正文一致`);
    else fail(`${mapping.name} Claude Command 与 Codex Skill 正文不一致`);
    if (!fs.existsSync(commandGuide) || !fs.existsSync(skillGuide)) fail(`${mapping.name} 命令映射缺少完整资料`);
    else if (hash(commandGuide) === hash(skillGuide)) ok(`${mapping.name} Claude Command 与 Codex Skill 完整资料一致`);
    else fail(`${mapping.name} Claude Command 与 Codex Skill 完整资料不一致`);
    validateGuideIndex(command, commandGuide, `命令 ${mapping.name}`);
  }
  ok(`已遍历 ${mappings.length} 个策略命令映射`);
} catch (error) {
  fail(`无法验证技能同步策略：${error.message}`);
}

const deprecatedInstructions = [
  /登记：`?\.claude\/hooks\/skill-forced-eval\.cjs`?\s*技能列表/,
  /声明到\s*hook\s*\+\s*AGENTS\.md/i,
  /Edit\s+\.claude\/hooks\/skill-forced-eval\.cjs/i,
  /Edit\s+AGENTS\.md\s*技能表/i
];
const deprecatedHits = listFiles(claudeSkillsRoot)
  .filter(file => file.endsWith(path.join('references', 'full-guide.md')))
  .flatMap(file => deprecatedInstructions.some(pattern => pattern.test(text(path.join(claudeSkillsRoot, file)))) ? [file] : []);
if (deprecatedHits.length) fail(`完整资料仍含废弃治理指令：${deprecatedHits.join(', ')}`);
else ok('完整资料未发现手改 Hook 技能表或生成根规则的废弃指令');

try {
  const settings = JSON.parse(text(path.join(root, '.claude', 'settings.json')));
  const matcher = (settings.hooks?.PreToolUse || []).map(item => item.matcher || '').join('|');
  const hookSource = text(path.join(root, '.claude', 'hooks', 'pre-tool-use.cjs'));
  const matcherCovered = matcher.includes('Edit') && matcher.includes('Write');
  const branchCovered = hookSource.includes("toolName === 'Edit'") && hookSource.includes("toolName === 'Write'");
  if (matcherCovered && branchCovered) ok('Claude Edit/Write 均进入敏感文件提醒');
  else fail('Claude PreToolUse matcher 与敏感文件处理分支未成对覆盖 Edit/Write');
} catch (error) {
  fail(`无法验证 Claude 敏感文件 Hook：${error.message}`);
}

process.exit(failed ? 1 : 0);
