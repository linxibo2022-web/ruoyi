#!/usr/bin/env node
/** 验证 Codex 路由与会话经验索引 Hook 的输入输出边界。 */
const assert = require('assert');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { spawnSync } = require('child_process');

const root = path.resolve(__dirname, '..', '..', '..');
const fixtures = require('../../../.agent-governance/fixtures/router-fixtures.json');
const { selectRoute } = require('../../../.agent-governance/lib/router.cjs');

function invoke(relativePath, input) {
  const result = spawnSync(process.execPath, [path.join(root, relativePath)], {
    cwd: root,
    input: JSON.stringify(input),
    encoding: 'utf8'
  });
  assert.strictEqual(result.status, 0, result.stderr);
  return result.stdout;
}

let maxBytes = 0;
for (const fixture of fixtures) {
  const output = invoke('.codex/hooks/skill-forced-eval.cjs', { prompt: fixture.prompt });
  const route = selectRoute(fixture.prompt);
  assert.ok(output.startsWith('⚙️ 强制技能评估：'), fixture.id);
  if (route.bypass) assert.ok(output.includes('跳过自动路由'), fixture.id);
  else if (!route.primary) assert.ok(output.includes('未匹配专用技能'), fixture.id);
  else {
    assert.ok(output.includes('匹配技能'), fixture.id);
    assert.ok(output.includes(`【🟨 ${route.primary}】`), fixture.id);
  }
  assert.ok(!output.includes(fixture.prompt), `${fixture.id} 不得回显提示词`);
  maxBytes = Math.max(maxBytes, Buffer.byteLength(output, 'utf8'));
}
assert.strictEqual(invoke('.codex/hooks/skill-forced-eval.cjs', { prompt: 'Conversation compacted' }), '');

const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'codex-hook-'));
const summary = path.join(temp, '.claude', 'docs', 'experience', '20260819', 'sample-exp-summary.md');
fs.mkdirSync(path.dirname(summary), { recursive: true });
fs.writeFileSync(summary, '不应注入正文', 'utf8');
const sessionOutput = invoke('.codex/hooks/session-start.cjs', { source: 'startup', cwd: temp });
const context = JSON.parse(sessionOutput).additionalContext;
assert.ok(context.includes('sample-exp-summary.md'));
assert.ok(!context.includes('不应注入正文'));
assert.ok(Buffer.byteLength(sessionOutput, 'utf8') <= 512);
assert.strictEqual(invoke('.codex/hooks/session-start.cjs', { source: 'resume', cwd: temp }), '{}');
fs.rmSync(temp, { recursive: true, force: true });

assert.ok(maxBytes <= 512);
console.log(`[OK] Codex Hook 路由 fixture ${fixtures.length} 项全部通过`);
console.log(`[OK] 恢复会话与经验索引通过；最大普通输出 ${maxBytes} B`);
