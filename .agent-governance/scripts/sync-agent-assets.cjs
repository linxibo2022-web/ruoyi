#!/usr/bin/env node
'use strict';

/** 从治理单一源生成根规则及非运行时 staging 角色资产。 */
const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '..', '..');
const governance = path.join(root, '.agent-governance');
const check = process.argv.includes('--check');
const stagingOnly = process.argv.includes('--staging');
const activate = process.argv.includes('--activate');
function lf(value) { return value.replace(/\r\n?/g, '\n').replace(/^\uFEFF/, ''); }
function read(file) { return lf(fs.readFileSync(file, 'utf8')); }
function sha256(content) { return require('crypto').createHash('sha256').update(content).digest('hex'); }
function safeProjectPath(relative, label) {
  if (typeof relative !== 'string' || !relative || path.isAbsolute(relative)) throw new Error(`${label} 路径非法：${relative}`);
  const resolved = path.resolve(root, relative);
  if (!resolved.startsWith(`${root}${path.sep}`)) throw new Error(`${label} 路径越出项目：${relative}`);
  return resolved;
}
function writeOrCheck(file, content) {
  const expected = lf(content).replace(/\n*$/, '\n');
  const actual = fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : null;
  if (check) {
    if (actual !== expected) throw new Error(`生成物漂移：${path.relative(root, file)}`);
    console.log(`[OK] ${path.relative(root, file)}`);
    return;
  }
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, expected, 'utf8');
  console.log(`[OK] 已生成 ${path.relative(root, file)}`);
}
function getProjectAgents(manifest) { return manifest.agents.filter(agent => !agent.builtin && agent.id.startsWith('ruoyi-')); }
function renderClaude(agent, body) {
  const runtime = agent.runtime.claude;
  return `---\nname: ${agent.runtimeNames.claude}\ndescription: ${JSON.stringify(agent.description)}\nmodel: ${runtime.model}\ntools: ${runtime.tools.join(', ')}\n---\n\n${body}`;
}
function renderCodex(agent, body) {
  const runtime = agent.runtime.codex;
  return `name = ${JSON.stringify(agent.runtimeNames.codex)}\ndescription = ${JSON.stringify(agent.description)}\nmodel = ${JSON.stringify(runtime.model)}\ntools = [${runtime.tools.map(JSON.stringify).join(', ')}]\nmode = ${JSON.stringify(runtime.mode)}\ndeveloper_instructions = \"\"\"\n${body}\n\"\"\"`;
}
function loadActivationManifest() {
  const manifestFile = path.join(governance, 'reports', 'subagent-activation-manifest.json');
  const activation = JSON.parse(read(manifestFile)).activation;
  if (!activation || !Array.isArray(activation.staging) || !Array.isArray(activation.targets) || !Array.isArray(activation.removeAfterWrite)) {
    throw new Error('激活清单格式不完整');
  }
  if (activation.staging.length !== 6 || activation.targets.length !== 6 || activation.removeAfterWrite.length !== 4) {
    throw new Error('激活清单中的角色或旧文件数量不符合契约');
  }
  return activation;
}
function verifyFrozenStaging(activation) {
  return activation.staging.map(entry => {
    if (!entry || typeof entry.sha256 !== 'string' || !Number.isInteger(entry.bytes)) throw new Error('激活清单缺少 staging 哈希或字节数');
    const source = safeProjectPath(entry.path, 'staging');
    const content = fs.readFileSync(source);
    if (content.length !== entry.bytes || sha256(content) !== entry.sha256) throw new Error(`冻结 staging 不匹配：${entry.path}`);
    return { source, content, relative: entry.path };
  });
}
function activateRuntimeAssets() {
  const activation = loadActivationManifest();
  const staged = verifyFrozenStaging(activation);
  const targets = activation.targets.map(target => safeProjectPath(target, '运行时目标'));
  const expectedByTarget = new Map();
  for (const item of staged) {
    const prefix = '.agent-governance/staging/';
    if (!item.relative.startsWith(prefix)) throw new Error(`staging 路径不在受控目录：${item.relative}`);
    const target = safeProjectPath(`.${item.relative.slice(prefix.length)}`, '运行时目标');
    expectedByTarget.set(target, item.content);
  }
  if (expectedByTarget.size !== targets.length || targets.some(target => !expectedByTarget.has(target))) {
    throw new Error('激活清单 targets 与 staging 映射不一致');
  }
  for (const entry of activation.removeAfterWrite) {
    const target = safeProjectPath(entry.path, '旧角色');
    if (fs.existsSync(target)) {
      const content = fs.readFileSync(target);
      if (content.length !== entry.bytes || sha256(content) !== entry.sha256) throw new Error(`旧角色不是清单冻结版本，拒绝删除：${entry.path}`);
    }
  }
  if (check) {
    for (const target of targets) {
      if (!fs.existsSync(target) || !fs.readFileSync(target).equals(expectedByTarget.get(target))) throw new Error(`运行时角色漂移：${path.relative(root, target)}`);
    }
    for (const entry of activation.removeAfterWrite) {
      if (fs.existsSync(safeProjectPath(entry.path, '旧角色'))) throw new Error(`旧角色尚未移除：${entry.path}`);
    }
    console.log('[OK] 运行时角色与冻结 staging 完全一致');
    return;
  }
  for (const target of targets) {
    const expected = expectedByTarget.get(target);
    if (!fs.existsSync(target) || !fs.readFileSync(target).equals(expected)) {
      fs.mkdirSync(path.dirname(target), { recursive: true });
      fs.writeFileSync(target, expected);
      console.log(`[OK] 已激活 ${path.relative(root, target)}`);
    } else console.log(`[OK] 已是目标版本 ${path.relative(root, target)}`);
  }
  for (const entry of activation.removeAfterWrite) {
    const target = safeProjectPath(entry.path, '旧角色');
    if (fs.existsSync(target)) {
      fs.unlinkSync(target);
      console.log(`[OK] 已移除旧角色 ${entry.path}`);
    }
  }
  activateRuntimeAssetsCheck(activation, expectedByTarget);
}
function activateRuntimeAssetsCheck(activation, expectedByTarget) {
  for (const [target, expected] of expectedByTarget) {
    if (!fs.existsSync(target) || !fs.readFileSync(target).equals(expected)) throw new Error(`激活后字节校验失败：${path.relative(root, target)}`);
  }
  for (const entry of activation.removeAfterWrite) {
    if (fs.existsSync(safeProjectPath(entry.path, '旧角色'))) throw new Error(`激活后旧角色仍存在：${entry.path}`);
  }
  console.log('[OK] 激活后字节校验通过');
}
function main() {
  if (activate) {
    if (stagingOnly) throw new Error('--activate 不能与 --staging 同时使用');
    activateRuntimeAssets();
    return;
  }
  const manifest = JSON.parse(read(path.join(governance, 'agents-manifest.json')));
  const agents = getProjectAgents(manifest);
  if (agents.length !== 3) throw new Error('必须恰好生成三个 ruoyi-* 项目角色');
  for (const agent of agents) {
    if (!agent.runtime || !agent.budget || agent.budget.roleBodyMaxLines > 200) throw new Error(`角色配置不完整：${agent.id}`);
    const body = read(path.join(root, agent.source)).replace(/\n*$/, '');
    if (body.split('\n').length > 200) throw new Error(`角色正文超过 200 行：${agent.id}`);
    writeOrCheck(path.join(governance, 'staging', 'claude', 'agents', `${agent.runtimeNames.claude}.md`), renderClaude(agent, body));
    writeOrCheck(path.join(governance, 'staging', 'codex', 'agents', `${agent.runtimeNames.codex}.toml`), renderCodex(agent, body));
  }
  if (!stagingOnly && fs.existsSync(path.join(governance, 'core-rules.md'))) {
    const core = read(path.join(governance, 'core-rules.md'));
    for (const [template, target] of [['AGENTS.md.tpl', 'AGENTS.md'], ['CLAUDE.md.tpl', 'CLAUDE.md']]) {
      writeOrCheck(path.join(root, target), read(path.join(governance, 'templates', template)).replace('{{CORE_RULES}}', core));
    }
  }
}
try { main(); } catch (error) { console.error(`[FAIL] ${error.message}`); process.exitCode = 1; }
