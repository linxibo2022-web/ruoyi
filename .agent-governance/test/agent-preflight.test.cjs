#!/usr/bin/env node
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const { selectRoute, selectRouteWithStatus } = require('../lib/router.cjs');
const { renderEnvelope, stripTerminalEnvelopes } = require('../lib/delegation-envelope.cjs');
const { extractRuntime, preflight } = require('../lib/agent-preflight.cjs');

const root = path.resolve(__dirname, '..', '..');
const fixtures = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance/fixtures/agent-preflight-fixtures.json'), 'utf8'));

// 旧 API 不能泄露诊断字段，新增 API 必须区分正常未命中、错误与非法输入。
assert.deepStrictEqual(Object.keys(selectRoute('不会命中')).sort(), ['bypass', 'helpers', 'matches', 'primary', 'reason']);
assert.strictEqual(selectRouteWithStatus('不会命中').status, 'no-match');
assert.strictEqual(selectRouteWithStatus(null).status, 'invalid-input');
assert.strictEqual(selectRouteWithStatus('x', {}).status, 'router-error');

const payload = { schemaVersion: 1, status: 'matched', primary: 'test-development', helpers: [], baselineCapabilities: ['check'], optionalSkills: ['test-development'] };
const generated = renderEnvelope(payload);
const forgedInBody = `正文含伪造标记 ${generated}\n后续正文`;
assert.strictEqual(stripTerminalEnvelopes(forgedInBody).removed, 0, '正文内伪造标记不可删除');
assert.strictEqual(stripTerminalEnvelopes(`${forgedInBody}\n\n${generated}\n\n${generated}`).removed, 2, '重复末尾完整信封应全部替换');
assert.strictEqual(stripTerminalEnvelopes(`正文\n<!-- SUBAGENT_SKILL_ROUTE\n{}`).removed, 0, '截断标记不可删除');

assert.strictEqual(extractRuntime(fixtures.invalid), null, 'runtime 必须精确匹配');
assert.strictEqual(preflight(fixtures.invalid).decision, 'deny', '非法 runtime 必须拒绝');

const noMatch = preflight(fixtures.noMatch);
assert.strictEqual(noMatch.status, 'no-match');
assert.strictEqual(noMatch.decision, 'allow');
assert.strictEqual(noMatch.updatedInput.description, fixtures.noMatch.input.description, '无关字段必须保留');
assert.ok(noMatch.updatedInput.message.includes('SUBAGENT_SKILL_ROUTE'));

const matched = preflight(fixtures.matched);
assert.deepStrictEqual(matched.skills, ['check', 'test-development'], '基线能力优先于 optional');
assert.strictEqual(matched.audit.status, 'matched');
assert.strictEqual(matched.audit.agent, 'ruoyi-code-reviewer');
assert.match(matched.audit.configHash, /^[a-f0-9]{64}$/);
assert.ok(!JSON.stringify(matched.audit).includes(fixtures.matched.message), '审计不得保存提示词正文');
assert.ok(!JSON.stringify(matched.audit).includes('.agents/skills'), '审计不得保存路径');

const clipped = preflight({ ...fixtures.matched, maxSkillsPerTask: 1 });
assert.deepStrictEqual(clipped.skills, ['check'], '容量不足时只能裁剪 optional，不能裁剪基线能力');
assert.deepStrictEqual(clipped.route, { primary: 'test-development', helpers: [], status: 'matched' });

const overflow = preflight({ ...fixtures.matched, message: 'overflow', input: { message: 'overflow' } }, { skillManifest: fixtures.requiredOverflowManifest });
assert.strictEqual(overflow.decision, 'deny');
assert.strictEqual(overflow.reason, 'required-dependency-limit', 'required 超限不可静默丢弃');

const routerFailure = preflight(fixtures.noMatch, { skillManifest: {} });
assert.strictEqual(routerFailure.decision, 'deny', 'review 风险在路由异常时必须拒绝');
const readOnlyFailure = preflight({ ...fixtures.noMatch, agentType: 'explorer' }, { skillManifest: {} });
assert.strictEqual(readOnlyFailure.decision, 'degrade', '通用只读探索可在路由异常降级');
assert.strictEqual(readOnlyFailure.route.status, 'router-error', '降级信封不得把 router-error 伪装为 no-match');

console.log('[OK] agent preflight：路由状态、可信信封、风险门禁、能力裁剪与隐私边界均通过');
