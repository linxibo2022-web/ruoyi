#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

let raw = '';
let input = {};
try {
  raw = fs.readFileSync(0, 'utf8');
  if (raw) input = JSON.parse(raw);
} catch {
  process.stdout.write('{}');
  process.exit(0);
}

const source = input.source || '';
const cwd = input.cwd || process.cwd();

// 仅在新会话启动时加载（resume/clear 跳过）
if (source && source !== 'startup') {
  process.stdout.write('{}');
  process.exit(0);
}

const expDir = path.join(cwd, '.claude', 'docs', 'experience');

let summaryFile = null;
try {
  // 找最新的 *-exp-summary.md
  const dateDirs = fs.readdirSync(expDir, { withFileTypes: true })
    .filter(e => e.isDirectory())
    .map(e => e.name)
    .sort()
    .reverse();

  for (const d of dateDirs) {
    const sub = path.join(expDir, d);
    const files = fs.readdirSync(sub)
      .filter(f => f.endsWith('-exp-summary.md'))
      .sort()
      .reverse();
    if (files.length) {
      summaryFile = path.join(sub, files[0]);
      break;
    }
  }
} catch {
  // 目录不存在或无权限时保持空输出，不阻断会话。
}

if (!summaryFile) {
  process.stdout.write('{}');
  process.exit(0);
}

const relPath = path.relative(cwd, summaryFile).replace(/\\/g, '/');
const wrapped = `最近经验摘要位于 \`${relPath}\`；仅当本轮涉及历史问题或用户明确要求时读取。`;

process.stdout.write(JSON.stringify({ additionalContext: wrapped.slice(0, 512) }));
process.exit(0);
