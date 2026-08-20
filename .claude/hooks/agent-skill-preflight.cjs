#!/usr/bin/env node
'use strict';

/**
 * Claude Agent 创建前置检查。
 * Claude 的 Agent 输入以 prompt/subagent_type 表示任务与角色；共享预检使用
 * message/agentType，因此这里只做字段适配，绝不读取 transcript 等上下文文件。
 */
const fs = require('node:fs');
const { preflight } = require('../../.agent-governance/lib/agent-preflight.cjs');

function deny(reason) {
  return {
    hookSpecificOutput: {
      hookEventName: 'PreToolUse',
      permissionDecision: 'deny',
      permissionDecisionReason: reason
    }
  };
}

function allow(updatedInput) {
  return {
    hookSpecificOutput: {
      hookEventName: 'PreToolUse',
      permissionDecision: 'allow',
      updatedInput
    }
  };
}

/**
 * 仅适配 Claude 已验证的 Agent PreToolUse 输入形状。
 * 返回值保留 Agent 原始字段，只会由共享层权威替换 prompt。
 */
function handle(input) {
  if (!input || input.tool_name !== 'Agent') return null;

  const toolInput = input.tool_input;
  if (!toolInput || typeof toolInput !== 'object' || Array.isArray(toolInput)
    || typeof toolInput.prompt !== 'string' || typeof toolInput.subagent_type !== 'string' || !toolInput.subagent_type) {
    return deny('Agent 预检拒绝：required-field-missing');
  }

  // Claude 已验证：一级代理发起嵌套 Agent 时会携带 agent_id。
  if (typeof input.agent_id === 'string' && input.agent_id.trim()) {
    return deny('Agent 预检拒绝：不允许子代理再次委派');
  }

  let result;
  try {
    result = preflight({
      runtime: 'claude',
      agentType: toolInput.subagent_type,
      message: toolInput.prompt,
      // 共享层约定使用 message，克隆避免改变 Claude 原始输入。
      input: { ...toolInput, message: toolInput.prompt }
    });
  } catch {
    // 适配或治理层异常不能绕过非只读代理的风险门禁。
    return deny('Agent 预检拒绝：preflight-error');
  }

  if (result.decision === 'deny') {
    return deny(`Agent 预检拒绝：${result.reason}`);
  }

  const updated = { ...result.updatedInput };
  updated.prompt = updated.message;
  delete updated.message;
  return allow(updated);
}

function main() {
  let raw;
  try {
    raw = fs.readFileSync(0, 'utf8');
  } catch {
    process.stdout.write(JSON.stringify({}));
    return;
  }

  let input;
  try {
    input = JSON.parse(raw);
  } catch {
    process.stdout.write(JSON.stringify({}));
    return;
  }

  const output = handle(input);
  process.stdout.write(JSON.stringify(output || {}));
}

if (require.main === module) main();

module.exports = { handle };
