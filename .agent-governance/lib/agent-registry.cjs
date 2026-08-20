const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..');
const manifestPath = path.join(root, '.agent-governance', 'agents-manifest.json');
const RUNTIMES = new Set(['claude', 'codex']);
const RISKS = new Set(['read-only', 'review', 'write', 'docs-write']);
const RESOURCE_KINDS = new Set(['command', 'skill']);

function fail(message) {
  throw new Error(`agents-manifest 无效：${message}`);
}

function isSafeRelativePath(value) {
  return typeof value === 'string'
    && value.length > 0
    && !path.isAbsolute(value)
    && !value.split(/[\\/]+/).includes('..');
}

function validateResource(capabilityId, runtime, resource, workspaceRoot) {
  if (!resource || typeof resource !== 'object') fail(`能力 ${capabilityId} 缺少 ${runtime} 资源`);
  if (!RESOURCE_KINDS.has(resource.kind)) fail(`能力 ${capabilityId} 的 ${runtime} kind 无效`);
  if (!isSafeRelativePath(resource.path)) fail(`能力 ${capabilityId} 的 ${runtime} path 必须是安全相对路径`);
  if (typeof resource.preloadable !== 'boolean') fail(`能力 ${capabilityId} 的 ${runtime} preloadable 必须为布尔值`);
  if (resource.kind !== 'skill' && resource.preloadable) {
    fail(`能力 ${capabilityId} 的 ${runtime} ${resource.kind} 不可预载`);
  }
  if (!fs.existsSync(path.join(workspaceRoot, resource.path))) {
    fail(`能力 ${capabilityId} 的 ${runtime} 资源不存在：${resource.path}`);
  }
}

function validateManifest(manifest, options = {}) {
  const workspaceRoot = options.root || root;
  if (!manifest || typeof manifest !== 'object') fail('根对象缺失');
  if (manifest.schemaVersion !== 1) fail('schemaVersion 必须为 1');
  const depth = manifest.delegationDepthPolicy;
  if (!depth || depth.target !== 1 || depth.claudeEnforcement !== 'hard-agent-id' || depth.codexEnforcement !== 'probe-required') {
    fail('delegationDepthPolicy 不符合既定策略');
  }
  if (!manifest.reservedNames || typeof manifest.reservedNames !== 'object') fail('reservedNames 缺失');
  for (const runtime of RUNTIMES) {
    if (!Array.isArray(manifest.reservedNames[runtime])) fail(`reservedNames.${runtime} 必须为数组`);
  }
  if (!Array.isArray(manifest.capabilities) || !Array.isArray(manifest.agents)) fail('capabilities 和 agents 必须为数组');

  const capabilityIds = new Set();
  for (const capability of manifest.capabilities) {
    if (!capability || typeof capability.id !== 'string' || !capability.id) fail('能力 id 缺失');
    if (capabilityIds.has(capability.id)) fail(`能力 id 重复：${capability.id}`);
    capabilityIds.add(capability.id);
    for (const runtime of RUNTIMES) validateResource(capability.id, runtime, capability.runtimes?.[runtime], workspaceRoot);
  }

  const agentIds = new Set();
  const runtimeNames = { claude: new Set(), codex: new Set() };
  for (const agent of manifest.agents) {
    if (!agent || typeof agent.id !== 'string' || !agent.id) fail('agent id 缺失');
    if (agentIds.has(agent.id)) fail(`agent id 重复：${agent.id}`);
    agentIds.add(agent.id);
    if (!RISKS.has(agent.risk)) fail(`角色 ${agent.id} 的风险级别无效`);
    if (!Number.isInteger(agent.maxDelegationDepth) || agent.maxDelegationDepth < 0) fail(`角色 ${agent.id} 的 maxDelegationDepth 无效`);
    if (!agent.runtimeNames || typeof agent.runtimeNames !== 'object') fail(`角色 ${agent.id} 缺少 runtimeNames`);
    for (const [runtime, name] of Object.entries(agent.runtimeNames)) {
      if (!RUNTIMES.has(runtime) || typeof name !== 'string' || !name) fail(`角色 ${agent.id} 的 runtime 名无效`);
      if (manifest.reservedNames[runtime].includes(name) && !agent.builtin) fail(`角色 ${agent.id} 覆盖 ${runtime} 保留名：${name}`);
      if (runtimeNames[runtime].has(name)) fail(`${runtime} runtime 名重复：${name}`);
      runtimeNames[runtime].add(name);
    }
    const policy = agent.skillPolicy;
    if (!policy || typeof policy.taskRoute !== 'boolean' || !Array.isArray(policy.baselineCapabilities) || !Array.isArray(policy.optional)) {
      fail(`角色 ${agent.id} 的 skillPolicy 无效`);
    }
    for (const capabilityId of policy.baselineCapabilities) {
      if (!capabilityIds.has(capabilityId)) fail(`角色 ${agent.id} 引用未知能力：${capabilityId}`);
    }
  }
  return manifest;
}

function loadManifest(options = {}) {
  const file = options.path || manifestPath;
  return validateManifest(JSON.parse(fs.readFileSync(file, 'utf8')), options);
}

function resolveAgent(agentType, runtime, manifest = loadManifest()) {
  if (!RUNTIMES.has(runtime)) return null;
  return manifest.agents.find(agent => agent.id === agentType || agent.runtimeNames?.[runtime] === agentType) || null;
}

function resolveRisk(agentType, runtime, manifest = loadManifest()) {
  return resolveAgent(agentType, runtime, manifest)?.risk || null;
}

function resolveCapabilities(agentType, runtime, manifest = loadManifest()) {
  const agent = resolveAgent(agentType, runtime, manifest);
  if (!agent) return null;
  const byId = new Map(manifest.capabilities.map(capability => [capability.id, capability]));
  return agent.skillPolicy.baselineCapabilities.map(id => ({ id, ...byId.get(id).runtimes[runtime] }));
}

function resolvePreloadableSkills(agentType, runtime, manifest = loadManifest()) {
  return (resolveCapabilities(agentType, runtime, manifest) || [])
    .filter(capability => capability.kind === 'skill' && capability.preloadable);
}

module.exports = {
  RUNTIMES,
  RISKS,
  loadManifest,
  validateManifest,
  resolveAgent,
  resolveRisk,
  resolveCapabilities,
  resolvePreloadableSkills
};
