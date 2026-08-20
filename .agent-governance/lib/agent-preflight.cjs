const crypto = require('crypto');
const { selectRouteWithStatus } = require('./router.cjs');
const { loadManifest, resolveAgent, resolveCapabilities } = require('./agent-registry.cjs');
const { replaceTerminalEnvelope } = require('./delegation-envelope.cjs');

const RUNTIMES = new Set(['claude', 'codex']);

function extractRuntime(input) {
  return input && typeof input === 'object' && RUNTIMES.has(input.runtime) ? input.runtime : null;
}

function configHash(agentManifest, skillManifest) {
  return crypto.createHash('sha256').update(JSON.stringify({ agentManifest, skillManifest })).digest('hex');
}

function audit(status, agentType, hash) {
  // 明确白名单：审计绝不携带消息、提示词、路径或完整配置。
  return { status, agent: agentType || null, configHash: hash || null };
}

function deny(status, agentType, hash, reason) {
  return { status, decision: 'deny', reason, updatedInput: null, audit: audit(status, agentType, hash) };
}

/**
 * 公共子代理创建预检。调用端负责把运行时原始字段适配为 runtime、agentType、message、input；
 * 返回的 updatedInput 保留 input 的全部字段，仅权威替换 message。
 */
function preflight(input, options = {}) {
  const runtime = extractRuntime(input);
  if (!runtime || !input || typeof input.agentType !== 'string' || !input.agentType || typeof input.message !== 'string' || !input.input || typeof input.input !== 'object') {
    return deny('invalid-input', input?.agentType, null, 'required-field-missing');
  }
  let agentManifest;
  let skillManifest;
  try {
    agentManifest = options.agentManifest || loadManifest(options.agentRegistryOptions);
    skillManifest = options.skillManifest;
  } catch {
    return deny('router-error', input.agentType, null, 'manifest-error');
  }
  const hash = configHash(agentManifest, skillManifest || {});
  const agent = resolveAgent(input.agentType, runtime, agentManifest);
  if (!agent) return deny('invalid-input', input.agentType, hash, 'unknown-agent');
  const route = selectRouteWithStatus(input.message, skillManifest, runtime);
  if (route.status === 'invalid-input') return deny('invalid-input', input.agentType, hash, 'message-invalid');
  if (route.status === 'router-error') {
    if (agent.risk === 'read-only') {
      return allow('router-error', 'degrade', input, agentManifest, agent, runtime, { primary: null, helpers: [], status: 'router-error' }, hash);
    }
    return deny('router-error', input.agentType, hash, 'route-unavailable');
  }
  if (route.reason === 'required-dependency') return deny('matched', input.agentType, hash, 'required-dependency-limit');
  return allow(route.status, 'allow', input, agentManifest, agent, runtime, route, hash);
}

function allow(status, decision, input, agentManifest, agent, runtime, route, hash) {
  const capabilities = resolveCapabilities(agent.id, runtime, agentManifest) || [];
  const baselineCapabilities = capabilities.map(item => item.id);
  const max = Number(input.maxSkillsPerTask || 3);
  const baselineSkills = capabilities.filter(item => item.kind === 'skill' && item.preloadable).map(item => item.id);
  const candidates = agent.skillPolicy.taskRoute
    ? [route.primary, ...(route.helpers || [])].filter(name => name && agent.skillPolicy.optional.includes(name))
    : [];
  const optionalSkills = [...new Set(candidates)].slice(0, Math.max(0, max - baselineSkills.length));
  const envelope = {
    schemaVersion: 1,
    status,
    primary: route.primary || null,
    helpers: route.helpers || [],
    baselineCapabilities,
    optionalSkills
  };
  const replaced = replaceTerminalEnvelope(input.message, envelope);
  return {
    status,
    decision,
    reason: decision === 'degrade' ? 'read-only-route-degraded' : 'ok',
    route: { primary: envelope.primary, helpers: envelope.helpers, status: envelope.status },
    capabilities,
    skills: [...baselineSkills, ...optionalSkills],
    updatedInput: { ...input.input, message: replaced.message },
    audit: audit(status, agent.id, hash)
  };
}

module.exports = { extractRuntime, preflight, configHash };
