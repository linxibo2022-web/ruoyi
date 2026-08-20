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
const header = '⚙️ 强制技能评估';
if (route.bypass) {
  process.stdout.write(`${header}：跳过自动路由（斜杠命令）。`);
  process.exit(0);
}
if (!route.primary) {
  process.stdout.write(`${header}：未匹配专用技能，按项目通用规则执行。`);
  process.exit(0);
}

const candidates = route.matches.length > 1 ? `；候选技能：${route.matches.join('、')}` : '';
const helpers = route.helpers.length ? `；满足依赖条件时加载：${route.helpers.join('、')}` : '';
const required = route.reason === 'required-dependency' ? '；必需依赖按 manifest 的绝对上限保留' : '';
process.stdout.write(`${header}：匹配技能 【🟨 ${route.primary}】${candidates}${helpers}；路由原因：${route.reason}${required}。不要预读技能正文；开始对应子任务前再读取 .agents/skills/${route.primary}/SKILL.md。`);
