#!/usr/bin/env node
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const {
  loadManifest,
  validateManifest,
  resolveCapabilities,
  resolvePreloadableSkills,
  resolveRisk
} = require('../lib/agent-registry.cjs');

const root = path.resolve(__dirname, '..', '..');
const fixtures = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance/fixtures/agent-registry-fixtures.json'), 'utf8'));
const manifest = loadManifest();
const clone = value => JSON.parse(JSON.stringify(value));

const validCapabilities = resolveCapabilities(fixtures.valid.agentType, fixtures.valid.runtime, manifest);
assert.deepStrictEqual(validCapabilities, [fixtures.valid.capability], '合法角色应解析 Codex check Skill');

const reserved = clone(manifest);
reserved.agents.push({
  id: fixtures.reservedName.agentId,
  risk: 'read-only',
  runtimeNames: { [fixtures.reservedName.runtime]: fixtures.reservedName.name },
  maxDelegationDepth: 0,
  skillPolicy: { taskRoute: true, baselineCapabilities: [], optional: [] }
});
assert.throws(() => validateManifest(reserved), /保留名/, '项目角色不可覆盖内置保留名');

const missing = clone(manifest);
missing.capabilities.find(item => item.id === fixtures.missingResource.capabilityId)
  .runtimes[fixtures.missingResource.runtime].path = fixtures.missingResource.path;
assert.throws(() => validateManifest(missing), /资源不存在/, '能力资源缺失必须失败');

const claudeCapabilities = resolveCapabilities(fixtures.claudeCommand.agentType, fixtures.claudeCommand.runtime, manifest);
assert.deepStrictEqual(claudeCapabilities.map(({ kind, preloadable }) => ({ kind, preloadable })), [{ kind: 'command', preloadable: false }], 'Claude check 必须是不可预载 Command');
assert.deepStrictEqual(resolvePreloadableSkills(fixtures.claudeCommand.agentType, fixtures.claudeCommand.runtime, manifest), [], 'Claude Command 不得误预载');

assert.strictEqual(resolveRisk(fixtures.unknown.agentType, fixtures.unknown.runtime, manifest), null, '未知角色不应猜测风险');
for (const item of fixtures.risks) {
  assert.strictEqual(resolveRisk(item.agentType, item.runtime, manifest), item.expected, `${item.agentType} 风险级别应正确`);
}

console.log('[OK] agent registry：合法角色、保留名、资源、预载、未知角色与风险等级均通过');
