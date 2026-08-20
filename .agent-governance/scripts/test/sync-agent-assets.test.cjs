#!/usr/bin/env node
'use strict';
const assert = require('assert');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const root = path.resolve(__dirname, '..', '..', '..');
const script = path.join(root, '.agent-governance', 'scripts', 'sync-agent-assets.cjs');
const stage = path.join(root, '.agent-governance', 'staging');
function run(args) { execFileSync(process.execPath, [script, ...args], { cwd: root, stdio: 'pipe' }); }
function hash(file) { return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex'); }
run(['--staging']);
const files = ['claude/agents/ruoyi-code-reviewer.md', 'claude/agents/ruoyi-project-reporter.md', 'claude/agents/ruoyi-project-updater.md', 'codex/agents/ruoyi-code-reviewer.toml', 'codex/agents/ruoyi-project-reporter.toml', 'codex/agents/ruoyi-project-updater.toml'].map(file => path.join(stage, file));
assert(files.every(fs.existsSync), 'staging 必须生成六个角色文件');
const first = files.map(hash); run(['--staging']); assert.deepStrictEqual(files.map(hash), first, '重复生成必须稳定'); run(['--check', '--staging']);
for (const file of files) { const content = fs.readFileSync(file, 'utf8'); assert(!content.startsWith('\uFEFF') && !content.includes('\r'), `${file} 必须为 UTF-8 无 BOM 且 LF`); }
run(['--activate']);
const runtimeFiles = [
  ['claude/agents/ruoyi-code-reviewer.md', '.claude/agents/ruoyi-code-reviewer.md'],
  ['claude/agents/ruoyi-project-reporter.md', '.claude/agents/ruoyi-project-reporter.md'],
  ['claude/agents/ruoyi-project-updater.md', '.claude/agents/ruoyi-project-updater.md'],
  ['codex/agents/ruoyi-code-reviewer.toml', '.codex/agents/ruoyi-code-reviewer.toml'],
  ['codex/agents/ruoyi-project-reporter.toml', '.codex/agents/ruoyi-project-reporter.toml'],
  ['codex/agents/ruoyi-project-updater.toml', '.codex/agents/ruoyi-project-updater.toml']
];
for (const [staged, runtime] of runtimeFiles) {
  assert(fs.readFileSync(path.join(stage, staged)).equals(fs.readFileSync(path.join(root, runtime))), `${runtime} 必须与 staging 逐字节一致`);
}
run(['--check', '--activate']);
run(['--activate']);
console.log('[OK] sync-agent-assets staging 生成、稳定性、激活幂等性与 --check --activate 验证通过');
