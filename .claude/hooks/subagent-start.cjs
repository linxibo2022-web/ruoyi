#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const { renderStartupContext } = require('../../.agent-governance/lib/subagent-context.cjs');

/**
 * Claude SubagentStart 只有角色元数据，不能取得任务正文。
 * 因此本 Hook 仅注入短上下文，永不返回 permissionDecision 或读取 transcript_path。
 */
function handle(input, options = {}) {
  if (!input || input.hook_event_name !== 'SubagentStart') return null;
  const result = renderStartupContext({
    runtime: 'claude',
    agentType: input.agent_type,
    // agent_id 已由运行时提供，但刻意不进入上下文，避免身份标识泄漏。
    agentId: typeof input.agent_id === 'string' ? input.agent_id : null
  }, options);
  return {
    hookSpecificOutput: {
      hookEventName: 'SubagentStart',
      additionalContext: result.additionalContext
    }
  };
}

function main() {
  let input;
  try {
    input = JSON.parse(fs.readFileSync(0, 'utf8'));
  } catch {
    process.stdout.write(JSON.stringify({}));
    return;
  }
  process.stdout.write(JSON.stringify(handle(input) || {}));
}

if (require.main === module) main();

module.exports = { handle };
