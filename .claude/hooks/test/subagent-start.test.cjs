#!/usr/bin/env node
'use strict';

const assert = require('node:assert');
const childProcess = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const { renderStartupContext, DEFAULT_TOKEN_LIMIT } = require('../../../.agent-governance/lib/subagent-context.cjs');
const { handle } = require('../subagent-start.cjs');

const root = path.resolve(__dirname, '..', '..', '..');
const hook = path.join(root, '.claude', 'hooks', 'subagent-start.cjs');

function invoke(input) {
  const result = childProcess.spawnSync(process.execPath, [hook], { cwd: root, encoding: 'utf8', input: JSON.stringify(input) });
  assert.strictEqual(result.status, 0, result.stderr);
  return JSON.parse(result.stdout);
}

const builtin = renderStartupContext({ runtime: 'claude', agentType: 'Explore' });
assert.strictEqual(builtin.status, 'registered', '内置角色必须由 manifest 识别');
assert.match(builtin.additionalContext, /只读角色/, '内置只读边界必须注入');

const project = renderStartupContext({ runtime: 'claude', agentType: 'ruoyi-code-reviewer' });
assert.strictEqual(project.status, 'registered', '项目角色必须由 manifest 识别');
assert.match(project.additionalContext, /审查角色/, '项目审查边界必须注入');
assert.match(project.additionalContext, /SUBAGENT_SKILL_ROUTE/, '必须提示检查委派信封');

const unknown = renderStartupContext({ runtime: 'claude', agentType: 'third-party-agent' });
assert.strictEqual(unknown.status, 'unregistered-agent');
assert.match(unknown.additionalContext, /UNREGISTERED_AGENT/, '未知角色必须显式降级');

const overBudget = renderStartupContext({ runtime: 'claude', agentType: 'Explore' }, { tokenLimit: 4, byteLimit: 12 });
assert.strictEqual(overBudget.budgetStatus, 'context-budget-exceeded', '超预算必须可观测');
assert.ok(overBudget.byteLength <= 12 && overBudget.estimatedTokenCount <= 4, '超预算上下文必须被裁剪到双重预算内');

const unavailable = renderStartupContext({ runtime: 'claude', agentType: 'Explore' }, { agentManifest: null });
assert.strictEqual(unavailable.status, 'manifest-unavailable', 'manifest 缺失必须降级而非抛出');
assert.match(unavailable.additionalContext, /UNREGISTERED_AGENT/, '无 manifest 时必须提供通用边界');

const taskBody = '绝不能写入启动上下文的任务正文';
const output = invoke({ hook_event_name: 'SubagentStart', agent_type: 'Explore', agent_id: 'agent-sensitive-id', prompt: taskBody, transcript_path: 'C:/secret/transcript.json' });
assert.strictEqual(output.hookSpecificOutput.hookEventName, 'SubagentStart');
assert.ok(!Object.hasOwn(output.hookSpecificOutput, 'permissionDecision'), '启动 Hook 不得阻断');
assert.ok(!JSON.stringify(output).includes(taskBody), '启动输出不得回显任务正文');
assert.ok(!JSON.stringify(output).includes('agent-sensitive-id'), '启动输出不得回显 agent_id');
assert.deepStrictEqual(invoke({ hook_event_name: 'PreToolUse', agent_type: 'Explore' }), {}, '非 SubagentStart 事件必须无副作用');

const settings = JSON.parse(fs.readFileSync(path.join(root, '.claude', 'settings.json'), 'utf8'));
const entry = settings.hooks.SubagentStart?.find(item => item.matcher === '.*');
assert.deepStrictEqual(entry?.hooks?.[0]?.args, ['${CLAUDE_PROJECT_DIR}/.claude/hooks/subagent-start.cjs'], 'SubagentStart 必须匹配全部角色并接线');
assert.strictEqual(entry?.hooks?.[0]?.timeout, 5000, '启动 Hook 必须使用有限超时');
assert.strictEqual(DEFAULT_TOKEN_LIMIT, 800, '默认 token 预算必须与计划契约一致');

console.log('[OK] Claude SubagentStart：角色边界、未知降级、预算与隐私边界均通过');
