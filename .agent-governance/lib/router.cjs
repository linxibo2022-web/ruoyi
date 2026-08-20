const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..');
const manifestPath = path.join(root, '.agent-governance', 'skills-manifest.json');
const claudeSkillsRoot = path.join(root, '.claude', 'skills');
const codexSkillsRoot = path.join(root, '.agents', 'skills');

function emptyRoute(bypass = false) {
  return { primary: null, helpers: [], matches: [], reason: 'empty', bypass };
}

function loadManifest() {
  const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
  const requiredLimit = Number(manifest.maxRequiredDependenciesPerSkill);
  if (!Number.isInteger(requiredLimit) || requiredLimit < 0) {
    throw new Error('maxRequiredDependenciesPerSkill 必须是非负整数');
  }
  for (const skill of manifest.skills || []) {
    const required = new Set((skill.dependencies || [])
      .filter(dependency => dependency.required)
      .flatMap(dependency => dependency.skills || []));
    if (required.size > requiredLimit) {
      throw new Error(`${skill.name} 的必需依赖数 ${required.size} 超过绝对上限 ${requiredLimit}`);
    }
  }
  return manifest;
}

function normalizePrompt(prompt) {
  return String(prompt || '')
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/(?:[a-z]:)?[\\/][\w .@()\-\\/]+/gi, ' ')
    .replace(/["'`“”‘’]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

function containsAny(text, values) {
  return values.some(value => text.includes(String(value).toLowerCase()));
}

function escapeRegExp(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function isNamedReference(text, wholeName, intents) {
  const reference = (intents || []).map(intent => escapeRegExp(intent)).join('|');
  if (!reference) return false;
  return new RegExp(`(?:${reference})\\s*(?:技能\\s*)?${wholeName}|${wholeName}(?:技能)?\\s*(?:放)?\\s*(?:${reference})`, 'i').test(text);
}

function namedSkillMatches(text, manifest) {
  const config = manifest.namedSkillRouting || {};
  return (config.skills || []).filter(name => {
    const escaped = escapeRegExp(name);
    const before = (config.callPrefixAny || []).map(intent => escapeRegExp(intent)).join('|');
    const after = (config.callSuffixAny || []).map(intent => escapeRegExp(intent)).join('|');
    const wholeName = `(?<![a-z0-9-])${escaped}(?![a-z0-9-])`;
    if (isNamedReference(text, wholeName, config.referenceIntentAny)) return false;
    return new RegExp(`(?:${before})\\s*(?:技能\\s*)?${wholeName}|${wholeName}(?:技能)?\\s*(?:${after})`, 'i').test(text);
  });
}

/**
 * 显式 `$skill-name` 是用户直接选择，不依赖自然语言触发词；仅允许两端均存在的共享技能，
 * 避免 Claude Hook 输出不存在的路径或将 Codex 专属能力误路由到 Claude。
 */
function isSharedSkill(name) {
  return fs.existsSync(path.join(claudeSkillsRoot, name, 'SKILL.md'))
    && fs.existsSync(path.join(codexSkillsRoot, name, 'SKILL.md'));
}

function isClaudeAvailable(name) {
  return fs.existsSync(path.join(claudeSkillsRoot, name, 'SKILL.md'))
    || fs.existsSync(path.join(root, '.claude', 'commands', `${name}.md`));
}

function selectRoute(prompt, suppliedManifest, runtime = 'codex') {
  let manifest;
  try {
    manifest = suppliedManifest || loadManifest();
  } catch {
    return emptyRoute();
  }
  const raw = String(prompt || '').trim();
  if (/^\/[^/\s]+/.test(raw) || /<command-name>/i.test(raw)) return emptyRoute(true);
  const explicit = raw.match(/(?:^|\s)\$([a-z][a-z0-9-]*)\b/i);
  if (explicit) {
    const name = explicit[1].toLowerCase();
    const listed = manifest.skills.some(item => item.name === name);
    return listed || isSharedSkill(name)
      ? { primary: name, helpers: [], matches: [name], reason: 'explicit', bypass: false }
      : emptyRoute();
  }
  const text = normalizePrompt(raw);
  if (!text) return emptyRoute();
  const keywordMatches = manifest.skills
    .map((skill, index) => ({ skill, index }))
    .filter(({ skill }) => containsAny(text, skill.includeAny || []) && !containsAny(text, skill.excludeAny || []))
    .sort((left, right) => right.skill.priority - left.skill.priority || left.index - right.index);
  const namedMatches = namedSkillMatches(text, manifest)
    .filter(name => runtime !== 'claude' || isClaudeAvailable(name))
    .map((name, index) => ({ skill: { name, priority: 1000 - index, dependencies: [] }, index: -1 - index }));
  const matches = [...namedMatches, ...keywordMatches]
    .filter((item, index, all) => all.findIndex(candidate => candidate.skill.name === item.skill.name) === index)
    .sort((left, right) => right.skill.priority - left.skill.priority || left.index - right.index);
  if (!matches.length) return emptyRoute();
  const primary = matches[0].skill;
  const matchedNames = matches.map(({ skill }) => skill.name);
  const helpers = [];
  for (const dependency of primary.dependencies || []) {
    if (!containsAny(text, dependency.whenAny || [])) continue;
    for (const name of dependency.skills || []) {
      if (name === primary.name || helpers.some(item => item.name === name)) continue;
      helpers.push({ name, required: Boolean(dependency.required) });
    }
  }
  const cap = Number(manifest.maxSkillsPerTask || 3) - 1;
  const optional = helpers.filter(item => !item.required);
  const required = helpers.filter(item => item.required);
  const selected = [...required, ...optional.slice(0, Math.max(0, cap - required.length))];
  return {
    primary: primary.name,
    helpers: selected.map(item => item.name),
    matches: matchedNames,
    reason: required.length > cap ? 'required-dependency' : 'match',
    bypass: false
  };
}

module.exports = { isSharedSkill, loadManifest, normalizePrompt, selectRoute };
