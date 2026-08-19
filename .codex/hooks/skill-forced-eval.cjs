#!/usr/bin/env node
const fs = require('fs');
const { selectRoute } = require('../../.agent-governance/lib/router.cjs');

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

let route;
try {
  route = selectRoute(prompt);
} catch {
  process.exit(0);
}
if (route.bypass || !route.primary) process.exit(0);

const helpers = route.helpers.length ? `；满足依赖条件时加载：${route.helpers.join('、')}` : '';
const required = route.reason === 'required-dependency' ? '；必需依赖按 manifest 例外保留' : '';
process.stdout.write(`本轮主技能：${route.primary}。开始实质实现前读取 .agents/skills/${route.primary}/SKILL.md${helpers}${required}。`);
