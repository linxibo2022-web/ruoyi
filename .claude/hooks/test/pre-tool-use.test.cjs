#!/usr/bin/env node
/** 验证 PreToolUse 对 Edit、Write 敏感文件和 Bash 危险命令的覆盖。 */

const assert = require('assert');
const childProcess = require('child_process');
const path = require('path');

const root = path.resolve(__dirname, '..', '..', '..');
const hook = path.join(root, '.claude', 'hooks', 'pre-tool-use.cjs');

function invoke(toolName, toolInput) {
  const result = childProcess.spawnSync(process.execPath, [hook], {
    cwd: root,
    encoding: 'utf8',
    input: JSON.stringify({ tool_name: toolName, tool_input: toolInput })
  });
  assert.strictEqual(result.status, 0, result.stderr);
  return JSON.parse(result.stdout);
}

for (const toolName of ['Edit', 'Write']) {
  const output = invoke(toolName, { file_path: 'D:/workspace/application-prod.yml' });
  assert.strictEqual(output.continue, true, `${toolName} 应允许操作继续`);
  assert.ok(output.systemMessage?.includes('敏感文件'), `${toolName} 应提醒敏感文件`);
}

assert.deepStrictEqual(
  invoke('Edit', { file_path: 'D:/workspace/application-dev.yml' }),
  { continue: true },
  '非敏感文件不应产生额外提醒'
);

const blocked = invoke('Bash', { command: 'drop database production' });
assert.strictEqual(blocked.decision, 'block', '危险 Bash 命令必须继续被阻止');

console.log('[OK] Claude PreToolUse 的 Edit/Write 敏感文件提醒与 Bash 阻断通过');
