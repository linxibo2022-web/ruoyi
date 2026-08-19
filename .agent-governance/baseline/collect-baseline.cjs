#!/usr/bin/env node
/**
 * 采集治理改造前的可复现基线。
 *
 * 快照仅记录样例标识、Hook 输出大小和摘要哈希，避免把用户提示词写入仓库。
 */
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const root = path.resolve(__dirname, '..', '..');
const outputPath = path.join(__dirname, 'baseline.json');

const samples = [
  { id: 'crud', prompt: '新增一个业务 CRUD 模块' },
  { id: 'database', prompt: '为订单功能设计并创建数据表' },
  { id: 'page', prompt: '开发 PC 端广告管理页面' },
  { id: 'error', prompt: '这个接口报错了，请排查原因' },
  { id: 'read-only', prompt: '解释当前项目的模块结构' },
  { id: 'performance', prompt: '接口响应慢，分析性能瓶颈' }
];

function sha256(value) {
  return crypto.createHash('sha256').update(value).digest('hex');
}

function fileBytes(relativePath) {
  const target = path.join(root, relativePath);
  try {
    return { status: 'present', bytes: fs.statSync(target).size };
  } catch {
    return { status: 'missing', bytes: 0 };
  }
}

function directoryStats(relativePath) {
  const target = path.join(root, relativePath);
  let files = 0;
  let bytes = 0;
  let directories = 0;
  try {
    for (const entry of fs.readdirSync(target, { withFileTypes: true })) {
      const child = path.join(target, entry.name);
      if (entry.isDirectory()) {
        directories += 1;
        const nested = directoryStats(path.relative(root, child));
        files += nested.files;
        bytes += nested.bytes;
        directories += nested.directories;
      } else if (entry.isFile()) {
        files += 1;
        bytes += fs.statSync(child).size;
      }
    }
    return { status: 'present', files, directories, bytes };
  } catch {
    return { status: 'missing', files: 0, directories: 0, bytes: 0 };
  }
}

function skillCount(relativePath) {
  const target = path.join(root, relativePath);
  try {
    return fs.readdirSync(target, { withFileTypes: true })
      .filter(entry => entry.isDirectory() && fs.existsSync(path.join(target, entry.name, 'SKILL.md')))
      .length;
  } catch {
    return 0;
  }
}

function runHook(relativePath, prompt) {
  const hook = path.join(root, relativePath);
  if (!fs.existsSync(hook)) return { status: 'missing', outputBytes: 0, outputSha256: null };
  const result = spawnSync(process.execPath, [hook], {
    cwd: root,
    input: JSON.stringify({ prompt, cwd: root, source: 'startup' }),
    encoding: 'utf8',
    timeout: 5000
  });
  if (result.error || result.status !== 0) {
    return {
      status: 'error',
      outputBytes: Buffer.byteLength(result.stdout || '', 'utf8'),
      outputSha256: sha256(result.stdout || '')
    };
  }
  return {
    status: 'ok',
    outputBytes: Buffer.byteLength(result.stdout || '', 'utf8'),
    outputSha256: sha256(result.stdout || '')
  };
}

const hookPaths = [
  '.codex/hooks/skill-forced-eval.cjs',
  '.codex/hooks/session-start.cjs',
  '.claude/hooks/skill-forced-eval.cjs'
];
const hookOutputs = samples.map(sample => ({
  id: sample.id,
  codex: runHook('.codex/hooks/skill-forced-eval.cjs', sample.prompt),
  claude: runHook('.claude/hooks/skill-forced-eval.cjs', sample.prompt)
}));
const allOutputs = hookOutputs.flatMap(item => [item.codex.outputBytes, item.claude.outputBytes]);

const baseline = {
  schemaVersion: 1,
  generatedAt: new Date().toISOString(),
  rootRuleBytes: {
    'AGENTS.md': fileBytes('AGENTS.md'),
    'CLAUDE.md': fileBytes('CLAUDE.md')
  },
  skills: {
    codex: { count: skillCount('.agents/skills'), ...directoryStats('.agents/skills') },
    claude: { count: skillCount('.claude/skills'), ...directoryStats('.claude/skills') }
  },
  hooks: {
    files: Object.fromEntries(hookPaths.map(item => [item, fileBytes(item)])),
    maxOutputBytes: allOutputs.length ? Math.max(...allOutputs) : 0,
    samples: hookOutputs
  },
  filesMeasured: ['AGENTS.md', 'CLAUDE.md', '.agents/skills', '.claude/skills', ...hookPaths]
};

fs.writeFileSync(outputPath, `${JSON.stringify(baseline, null, 2)}\n`, 'utf8');
console.log(`[OK] 已生成 ${path.relative(root, outputPath).replace(/\\/g, '/')}`);
