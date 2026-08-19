const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..');
const manifestPath = path.join(root, '.agent-governance', 'skills-manifest.json');

function emptyRoute(bypass = false) {
  return { primary: null, helpers: [], reason: 'empty', bypass };
}

function loadManifest() {
  return JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
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

function selectRoute(prompt, suppliedManifest) {
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
    const skill = manifest.skills.find(item => item.name === explicit[1]);
    return skill ? { primary: skill.name, helpers: [], reason: 'explicit', bypass: false } : emptyRoute();
  }
  const text = normalizePrompt(raw);
  if (!text) return emptyRoute();
  const matches = manifest.skills
    .map((skill, index) => ({ skill, index }))
    .filter(({ skill }) => containsAny(text, skill.includeAny || []) && !containsAny(text, skill.excludeAny || []))
    .sort((left, right) => right.skill.priority - left.skill.priority || left.index - right.index);
  if (!matches.length) return emptyRoute();
  const primary = matches[0].skill;
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
    reason: required.length > cap ? 'required-dependency' : 'match',
    bypass: false
  };
}

module.exports = { loadManifest, normalizePrompt, selectRoute };
