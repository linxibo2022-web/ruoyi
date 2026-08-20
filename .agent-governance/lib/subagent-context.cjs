'use strict';

const { loadManifest, resolveAgent } = require('./agent-registry.cjs');

const DEFAULT_TOKEN_LIMIT = 800;
const DEFAULT_BYTE_LIMIT = 3200;

const BOUNDARIES = {
  'read-only': '只读角色：不得修改文件、配置或外部状态；仅回传结论与证据。',
  review: '审查角色：只审查与报告，不修改实现；指出风险与验证缺口。',
  write: '实现角色：仅在已委派范围内修改；保留他人改动并完成最小验证。',
  'docs-write': '文档角色：仅在明确授权时修改 docs/；其余内容只读并回传证据。'
};

function utf8Bytes(value) {
  return Buffer.byteLength(value, 'utf8');
}

/**
 * 估算上下文 token，采用每 3 UTF-8 字节一个 token 的保守近似；
 * 运行时 token 计数不可用时，字节数仍作为可复核的硬上限。
 */
function estimateTokens(value) {
  return Math.ceil(utf8Bytes(value) / 3);
}

function trimUtf8(value, byteLimit) {
  if (!Number.isInteger(byteLimit) || byteLimit <= 0) return '';
  let result = '';
  let used = 0;
  for (const char of value) {
    const size = utf8Bytes(char);
    if (used + size > byteLimit) break;
    result += char;
    used += size;
  }
  return result;
}

function loadAgentManifest(options) {
  if (Object.prototype.hasOwnProperty.call(options, 'agentManifest')) {
    if (!options.agentManifest) throw new Error('manifest-unavailable');
    return options.agentManifest;
  }
  return loadManifest(options.agentRegistryOptions);
}

function renderLines(agentType, agent, status) {
  const header = status === 'registered'
    ? `SUBAGENT_STARTUP: ${agent.id}`
    : 'SUBAGENT_STARTUP: UNREGISTERED_AGENT';
  const boundary = agent
    ? BOUNDARIES[agent.risk]
    : '未知角色：遵守最小权限；未获明确授权时只读、不得委派子代理。';
  return [
    header,
    boundary,
    '检查委派消息末尾完整的 SUBAGENT_SKILL_ROUTE 信封；仅按其中技能结果按需读取。',
    '信封缺失、截断或不可信时记录限制并遵守项目规则；不得在启动 Hook 中重路由或从 transcript 读取任务。',
    `角色状态：${status}${agentType ? `；运行时类型：${agentType}` : ''}`
  ].join('\n');
}

/**
 * 渲染不含任务正文的公共启动上下文。无论 manifest 或预算失败均只降级上下文，
 * 调用端不得将本函数结果解释为启动拒绝。
 */
function renderStartupContext(input = {}, options = {}) {
  const tokenLimit = options.tokenLimit ?? DEFAULT_TOKEN_LIMIT;
  const byteLimit = options.byteLimit ?? DEFAULT_BYTE_LIMIT;
  const agentType = typeof input.agentType === 'string' && input.agentType.trim() ? input.agentType.trim() : null;
  let agent = null;
  let status = 'unregistered-agent';

  try {
    const manifest = loadAgentManifest(options);
    agent = agentType ? resolveAgent(agentType, input.runtime || 'claude', manifest) : null;
    status = agent ? 'registered' : 'unregistered-agent';
  } catch {
    status = 'manifest-unavailable';
  }

  let additionalContext = renderLines(agentType, agent, status);
  let byteLength = utf8Bytes(additionalContext);
  let estimatedTokenCount = estimateTokens(additionalContext);
  let budgetStatus = 'within-budget';
  const permittedBytes = Math.max(0, Math.min(byteLimit, tokenLimit * 3));

  if (byteLength > byteLimit || estimatedTokenCount > tokenLimit) {
    budgetStatus = 'context-budget-exceeded';
    additionalContext = trimUtf8(additionalContext, permittedBytes);
    byteLength = utf8Bytes(additionalContext);
    estimatedTokenCount = estimateTokens(additionalContext);
  }

  return {
    status,
    budgetStatus,
    agentId: agent?.id || null,
    additionalContext,
    byteLength,
    estimatedTokenCount,
    tokenLimit,
    byteLimit
  };
}

module.exports = {
  DEFAULT_TOKEN_LIMIT,
  DEFAULT_BYTE_LIMIT,
  utf8Bytes,
  estimateTokens,
  trimUtf8,
  renderStartupContext
};
