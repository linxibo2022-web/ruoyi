#!/usr/bin/env node
/**
 * 冻结技能与子代理治理基线。
 *
 * 快照只保存匿名样例标识、相对路径、声明配置、体积和摘要，不保存任务原文、
 * 绝对用户目录或环境变量值。历史上下文优化基线 baseline.json 保持不变。
 */
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const root = path.resolve(__dirname, '..', '..');
const outputPath = path.join(__dirname, 'subagent-governance-baseline.json');

const samples = [
  { id: 'crud', prompt: '新增一个业务 CRUD 模块' },
  { id: 'database', prompt: '为订单功能设计并创建数据表' },
  { id: 'page', prompt: '开发 PC 端广告管理页面' },
  { id: 'error', prompt: '这个接口报错了，请排查原因' },
  { id: 'read-only', prompt: '解释当前项目的模块结构' },
  { id: 'performance', prompt: '接口响应慢，分析性能瓶颈' }
];

const agentPaths = [
  '.claude/agents/code-reviewer.md',
  '.claude/agents/project-manager.md',
  '.codex/agents/code-reviewer.toml',
  '.codex/agents/project-manager.toml'
];

function sha256(value) {
  return crypto.createHash('sha256').update(value).digest('hex');
}

function generatedAtShanghai() {
  const parts = Object.fromEntries(new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hourCycle: 'h23'
  }).formatToParts(new Date()).filter(item => item.type !== 'literal').map(item => [item.type, item.value]));
  return `${parts.year}-${parts.month}-${parts.day}T${parts.hour}:${parts.minute}:${parts.second}+08:00`;
}

function readRelative(relativePath) {
  try {
    return fs.readFileSync(path.join(root, relativePath), 'utf8');
  } catch {
    return null;
  }
}

function contentStats(content) {
  return {
    bytes: Buffer.byteLength(content, 'utf8'),
    lines: content ? content.split(/\r?\n/).length : 0,
    sha256: sha256(content)
  };
}

function fileStats(relativePath) {
  const content = readRelative(relativePath);
  return content === null
    ? { status: 'missing', bytes: 0, sha256: null }
    : { status: 'present', bytes: Buffer.byteLength(content, 'utf8'), sha256: sha256(content) };
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

function parseClaudeAgent(relativePath, content) {
  const matched = content.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/);
  const frontmatter = matched?.[1] || '';
  const body = matched?.[2] || '';
  const field = name => frontmatter.match(new RegExp(`^${name}:\\s*(.*)$`, 'm'))?.[1]?.trim() || null;
  const tools = (field('tools') || '').split(',').map(item => item.trim()).filter(Boolean);
  return {
    name: field('name'),
    relativePath,
    format: 'claude-markdown',
    tools: { declaration: tools.length ? 'explicit' : 'inherited', values: tools },
    permissions: { declaration: field('permissionMode') ? 'explicit' : 'inherited', mode: field('permissionMode') },
    body: contentStats(body),
    file: contentStats(content)
  };
}

function parseTomlValue(content, name) {
  const match = content.match(new RegExp(`^${name}\\s*=\\s*(.+)$`, 'm'));
  if (!match) return null;
  const value = match[1].trim();
  if (value.startsWith('[')) {
    return [...value.matchAll(/["']([^"']+)["']/g)].map(item => item[1]);
  }
  return value.replace(/^["']|["']$/g, '');
}

function parseCodexAgent(relativePath, content) {
  const marker = 'developer_instructions = """';
  const start = content.indexOf(marker);
  const end = content.lastIndexOf('"""');
  const body = start >= 0 && end > start ? content.slice(start + marker.length, end).replace(/^\r?\n/, '') : '';
  const declaredTools = parseTomlValue(content, 'tools');
  const permissionMode = parseTomlValue(content, 'permission_mode') || parseTomlValue(content, 'sandbox_mode');
  return {
    name: parseTomlValue(content, 'name'),
    relativePath,
    format: 'codex-toml',
    tools: {
      declaration: declaredTools === null ? 'inherited' : 'explicit',
      values: Array.isArray(declaredTools) ? declaredTools : declaredTools ? [declaredTools] : []
    },
    permissions: { declaration: permissionMode === null ? 'inherited' : 'explicit', mode: permissionMode },
    body: contentStats(body),
    file: contentStats(content)
  };
}

function collectAgent(relativePath) {
  const content = readRelative(relativePath);
  if (content === null) return { name: path.basename(relativePath, path.extname(relativePath)), relativePath, status: 'missing' };
  return relativePath.endsWith('.md')
    ? { status: 'present', ...parseClaudeAgent(relativePath, content) }
    : { status: 'present', ...parseCodexAgent(relativePath, content) };
}

function sectionStats(relativePath, heading) {
  const content = readRelative(relativePath);
  if (content === null) return { status: 'missing', bytes: 0, lines: 0, sha256: null };
  const start = content.indexOf(`${heading}\n`);
  if (start < 0) return { status: 'missing', bytes: 0, lines: 0, sha256: null };
  const next = content.indexOf('\n## ', start + heading.length);
  const section = content.slice(start, next < 0 ? content.length : next).trimEnd();
  return { status: 'present', ...contentStats(section) };
}

function normalizeCommand(command) {
  const match = command.match(/(?:^|[\s\\/])(\.(?:claude|codex)[\\/][^\s"']+)/);
  return match ? `node ${match[1].replace(/\\/g, '/')}` : path.basename(command.trim().split(/\s+/).pop() || '');
}

function hookConfiguration(relativePath) {
  const content = readRelative(relativePath);
  if (content === null) return { status: 'missing', events: [] };
  try {
    const parsed = JSON.parse(content);
    const events = Object.entries(parsed.hooks || {}).map(([event, groups]) => ({
      event,
      groups: groups.map(group => ({
        matcher: group.matcher || null,
        hooks: (group.hooks || []).map(hook => ({
          type: hook.type || null,
          command: normalizeCommand(hook.command || ''),
          timeout: hook.timeout ?? null
        }))
      }))
    }));
    return { status: 'present', ...contentStats(content), events };
  } catch {
    return { status: 'invalid', ...contentStats(content), events: [] };
  }
}

function runHook(relativePath, input) {
  const hook = path.join(root, relativePath);
  if (!fs.existsSync(hook)) return { status: 'missing', outputBytes: 0, outputSha256: null };
  const result = spawnSync(process.execPath, [hook], {
    cwd: root,
    input: JSON.stringify(input),
    encoding: 'utf8',
    timeout: 5000
  });
  const output = result.stdout || '';
  return {
    status: result.error || result.status !== 0 ? 'error' : 'ok',
    exitCode: result.status,
    outputBytes: Buffer.byteLength(output, 'utf8'),
    outputSha256: sha256(output)
  };
}

const hookPaths = [
  '.codex/hooks/skill-forced-eval.cjs',
  '.codex/hooks/session-start.cjs',
  '.codex/hooks/pre-tool-use.cjs',
  '.codex/hooks/stop.cjs',
  '.claude/hooks/skill-forced-eval.cjs',
  '.claude/hooks/pre-tool-use.cjs',
  '.claude/hooks/stop.cjs'
];
const routingOutputs = samples.map(sample => ({
  id: sample.id,
  promptSha256: sha256(sample.prompt),
  codex: runHook('.codex/hooks/skill-forced-eval.cjs', { prompt: sample.prompt, cwd: root, source: 'startup' }),
  claude: runHook('.claude/hooks/skill-forced-eval.cjs', { prompt: sample.prompt, cwd: root, source: 'startup' })
}));
const supplementalOutputs = {
  codexSessionStart: runHook('.codex/hooks/session-start.cjs', { source: 'startup', cwd: root }),
  codexSafeTool: runHook('.codex/hooks/pre-tool-use.cjs', { tool_name: 'shell', tool_input: { command: 'git status -s' }, cwd: root }),
  claudeSafeTool: runHook('.claude/hooks/pre-tool-use.cjs', { tool_name: 'Bash', tool_input: { command: 'git status -s' } })
};
const allOutputBytes = [
  ...routingOutputs.flatMap(item => [item.codex.outputBytes, item.claude.outputBytes]),
  ...Object.values(supplementalOutputs).map(item => item.outputBytes)
];

const baseline = {
  schemaVersion: 1,
  generatedAt: generatedAtShanghai(),
  redaction: {
    taskTextStored: false,
    absolutePathsStored: false,
    environmentValuesStored: false,
    promptRepresentation: 'sha256'
  },
  rootRuleBytes: {
    'AGENTS.md': fileStats('AGENTS.md'),
    'CLAUDE.md': fileStats('CLAUDE.md')
  },
  skills: {
    codex: { count: skillCount('.agents/skills'), ...directoryStats('.agents/skills') },
    claude: { count: skillCount('.claude/skills'), ...directoryStats('.claude/skills') }
  },
  agents: {
    expectedCount: agentPaths.length,
    presentCount: agentPaths.filter(item => readRelative(item) !== null).length,
    entries: agentPaths.map(collectAgent)
  },
  rootDelegationRules: Object.fromEntries(
    ['AGENTS.md', 'CLAUDE.md', '.agent-governance/core-rules.md']
      .map(item => [item, sectionStats(item, '## 子代理委派与上下文')])
  ),
  hooks: {
    configuration: {
      '.claude/settings.json': hookConfiguration('.claude/settings.json'),
      '.codex/hooks.json': hookConfiguration('.codex/hooks.json'),
      '.codex/config.toml': fileStats('.codex/config.toml')
    },
    files: Object.fromEntries(hookPaths.map(item => [item, fileStats(item)])),
    maxOutputBytes: allOutputBytes.length ? Math.max(...allOutputBytes) : 0,
    output: {
      maxBytes: allOutputBytes.length ? Math.max(...allOutputBytes) : 0,
      routingSamples: routingOutputs,
      supplemental: supplementalOutputs
    }
  },
  filesMeasured: [...agentPaths, 'AGENTS.md', 'CLAUDE.md', '.agents/skills', '.claude/skills', '.agent-governance/core-rules.md',
    '.claude/settings.json', '.codex/hooks.json', '.codex/config.toml', ...hookPaths]
};

fs.writeFileSync(outputPath, `${JSON.stringify(baseline, null, 2)}\n`, 'utf8');
console.log(`[OK] 已生成 ${path.relative(root, outputPath).replace(/\\/g, '/')}`);
