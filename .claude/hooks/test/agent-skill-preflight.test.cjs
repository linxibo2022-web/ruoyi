#!/usr/bin/env node
'use strict';

const assert = require('node:assert');
const childProcess = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..', '..');
const hook = path.join(root, '.claude', 'hooks', 'agent-skill-preflight.cjs');

function invoke(input) {
  const result = childProcess.spawnSync(process.execPath, [hook], {
    cwd: root,
    encoding: 'utf8',
    input: JSON.stringify(input)
  });
  assert.strictEqual(result.status, 0, result.stderr);
  return JSON.parse(result.stdout);
}

const invalid = invoke({ tool_name: 'Agent', tool_input: { subagent_type: 'Explore' } });
assert.strictEqual(invalid.hookSpecificOutput.permissionDecision, 'deny', '缺少 prompt 必须拒绝');

const recursive = invoke({
  tool_name: 'Agent',
  agent_id: 'agent-level-one',
  tool_input: { prompt: '只读取当前目录结构', subagent_type: 'Explore', description: '保留描述' }
});
assert.strictEqual(recursive.hookSpecificOutput.permissionDecision, 'deny', '存在 agent_id 必须拒绝嵌套委派');

const allowed = invoke({
  tool_name: 'Agent',
  tool_input: { prompt: '只读取当前目录结构', subagent_type: 'Explore', description: '保留描述' }
});
assert.strictEqual(allowed.hookSpecificOutput.permissionDecision, 'allow', '只读代理应可正常通过');
assert.deepStrictEqual(allowed.hookSpecificOutput.updatedInput.description, '保留描述', 'description 必须保留');
assert.strictEqual(allowed.hookSpecificOutput.updatedInput.subagent_type, 'Explore', 'subagent_type 必须保留');
assert.strictEqual(typeof allowed.hookSpecificOutput.updatedInput.prompt, 'string', '任务字段必须映射回 prompt');
assert.ok(!Object.prototype.hasOwnProperty.call(allowed.hookSpecificOutput.updatedInput, 'message'), 'Claude 输入不得泄漏共享 message 字段');

assert.deepStrictEqual(invoke({ tool_name: 'Bash', tool_input: { command: 'echo ok' } }), {}, '非 Agent 工具必须无副作用');

const settings = JSON.parse(fs.readFileSync(path.join(root, '.claude', 'settings.json'), 'utf8'));
const preToolUse = settings.hooks.PreToolUse;
const securityEntry = preToolUse.find(entry => entry.matcher === 'Bash|PowerShell|Edit|Write');
const agentEntry = preToolUse.find(entry => entry.matcher === '^Agent$');
assert.ok(securityEntry?.hooks?.every(item => item.command === 'node' && Array.isArray(item.args)), '安全 Hook 必须使用跨平台 node + args');
assert.deepStrictEqual(agentEntry?.hooks?.[0]?.args, ['${CLAUDE_PROJECT_DIR}/.claude/hooks/agent-skill-preflight.cjs'], 'Agent 必须使用独立 matcher 接线');

console.log('[OK] Claude Agent 预检：严格字段、递归拒绝和字段保留通过');
