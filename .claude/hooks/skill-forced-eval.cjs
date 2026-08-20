#!/usr/bin/env node
/** Claude Code UserPromptSubmit Hook：将共享 router 选择的精确技能路径注入 Hook 上下文。 */

const fs = require('fs');
const { renderRoute, routeInput } = require('./skill-router.cjs');

let raw = '';
try {
  raw = fs.readFileSync(0, 'utf8');
} catch {
  process.exit(0);
}

let input;
try {
  input = JSON.parse(raw);
} catch {
  process.exit(0);
}

const prompt = typeof input.prompt === 'string' ? input.prompt : '';
const recovery = /continued from a previous conversation|ran out of context|conversation compacted|session is being continued/i;
if (recovery.test(prompt)) process.exit(0);

const additionalContext = renderRoute(routeInput(input));
if (!additionalContext) process.exit(0);

process.stdout.write(JSON.stringify({
  hookSpecificOutput: {
    hookEventName: 'UserPromptSubmit',
    additionalContext
  }
}));
