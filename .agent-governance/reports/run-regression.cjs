#!/usr/bin/env node
/** 以匿名样例验证双端路由，并生成不含原始提示词的灰度报告。 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { selectRoute } = require('../lib/router.cjs');

const root = path.resolve(__dirname, '..', '..');
const fixtures = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance/fixtures/router-fixtures.json'), 'utf8'));
const baseline = JSON.parse(fs.readFileSync(path.join(root, '.agent-governance/baseline/baseline.json'), 'utf8'));
function routeMatches(actual, expected) {
  const { matches, ...base } = actual;
  const { matches: expectedMatches, ...expectedBase } = expected;
  return Array.isArray(matches)
    && JSON.stringify(base) === JSON.stringify(expectedBase)
    && (!expectedMatches || JSON.stringify(matches) === JSON.stringify(expectedMatches));
}

function invoke(file, prompt) {
  const result = spawnSync(process.execPath, [path.join(root, file)], {
    cwd: root,
    input: JSON.stringify({ prompt, cwd: root }),
    encoding: 'utf8'
  });
  if (result.status !== 0) throw new Error(`${file} 退出码 ${result.status}`);
  return Buffer.byteLength(result.stdout, 'utf8');
}

const results = fixtures.map(item => {
  const route = selectRoute(item.prompt);
  const match = routeMatches(route, item.expected);
  return {
    id: item.id,
    routeMatch: match,
    codexOutputBytes: invoke('.codex/hooks/skill-forced-eval.cjs', item.prompt),
    claudeOutputBytes: invoke('.claude/hooks/skill-forced-eval.cjs', item.prompt)
  };
});
const recoveryPrompt = 'Conversation compacted';
const recovery = {
  codexOutputBytes: invoke('.codex/hooks/skill-forced-eval.cjs', recoveryPrompt),
  claudeOutputBytes: invoke('.claude/hooks/skill-forced-eval.cjs', recoveryPrompt)
};
const allPass = results.every(item => item.routeMatch) && recovery.codexOutputBytes === 0 && recovery.claudeOutputBytes === 0;
const report = {
  schemaVersion: 1,
  generatedAt: new Date().toISOString(),
  fixtures: { total: results.length, passed: results.filter(item => item.routeMatch).length, results },
  supplemental: {
    recoverySession: { passed: recovery.codexOutputBytes === 0 && recovery.claudeOutputBytes === 0, ...recovery },
    injectionProtection: { passed: results.find(item => item.id === 'injection')?.routeMatch === true }
  },
  hookBytes: {
    codexMax: Math.max(...results.map(item => item.codexOutputBytes)),
    claudeMax: Math.max(...results.map(item => item.claudeOutputBytes)),
    baselineMax: baseline.hooks.maxOutputBytes
  },
  rootRuleBytes: {
    baseline: baseline.rootRuleBytes,
    current: {
      'AGENTS.md': fs.statSync(path.join(root, 'AGENTS.md')).size,
      'CLAUDE.md': fs.statSync(path.join(root, 'CLAUDE.md')).size
    }
  },
  rollback: allPass ? '不需要回滚；所有回归门禁通过。' : '建议回滚直接责任任务的 Hook 或 manifest 改动。'
};
fs.writeFileSync(path.join(__dirname, 'regression-report.json'), `${JSON.stringify(report, null, 2)}\n`, 'utf8');
console.log(`[OK] 回归样例 ${report.fixtures.passed}/${report.fixtures.total} 通过`);
console.log(`[OK] Codex 最大 ${report.hookBytes.codexMax} B；Claude 最大 ${report.hookBytes.claudeMax} B`);
console.log(`[OK] ${report.rollback}`);
process.exit(allPass ? 0 : 1);
