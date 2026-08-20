#!/usr/bin/env node
'use strict';

const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..');
const reportFile = path.join(__dirname, 'subagent-runtime-probe.json');
const fixtureFile = path.join(root, '.agent-governance', 'fixtures', 'subagent-hook-input-fixtures.json');
let failed = false;

function fail(message) { failed = true; console.error(`[FAIL] ${message}`); }
function ok(message) { console.log(`[OK] ${message}`); }
function hash(file) { return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex'); }
function isStringArray(value) { return Array.isArray(value) && value.every(item => typeof item === 'string'); }

let report;
let fixture;
try { report = JSON.parse(fs.readFileSync(reportFile, 'utf8')); } catch (error) { fail(`无法读取 P0 结论报告：${error.message}`); }
try { fixture = JSON.parse(fs.readFileSync(fixtureFile, 'utf8')); } catch (error) { fail(`无法读取脱敏 fixture：${error.message}`); }

if (report && fixture) {
  if (report.schemaVersion !== 1) fail('报告 schemaVersion 必须为 1');
  if (report.evidence?.fixture !== '.agent-governance/fixtures/subagent-hook-input-fixtures.json') fail('报告 fixture 路径不正确');
  if (report.evidence?.fixtureSha256 !== hash(fixtureFile)) fail('报告 fixtureSha256 与当前 fixture 不一致');
  const forbidden = ['prompt-body', 'transcript-body', 'user-input', 'absolute-path-value'];
  if (!forbidden.every(item => fixture.redactionContract?.forbiddenData?.includes(item))) fail('fixture 脱敏契约不完整');
  if (report.claude?.captureStatus !== 'observed' || fixture.claude?.runtime?.captureStatus !== 'observed') fail('Claude 捕获状态必须为 observed');
  if (report.codex?.captureStatus !== 'not-observable' || fixture.codex?.runtime?.captureStatus !== 'not-observable') fail('Codex 捕获状态必须为 not-observable');
  if (report.claude?.agentPreflight?.strictTaskField !== 'tool_input.prompt') fail('Claude 严格任务字段必须冻结为 tool_input.prompt');
  if (!isStringArray(report.claude?.subagentStart?.agentIdentityFields) || !report.claude.subagentStart.agentIdentityFields.includes('agent_id')) fail('Claude agent_id 结论缺失');
  if (report.claude?.recursion?.capability !== 'hard-capable') fail('Claude 递归能力结论必须为 hard-capable');
  if (report.codex?.recursion?.capability !== 'unobservable') fail('Codex 递归能力结论必须为 unobservable');
  if (!report.codex?.recursion?.policy?.startsWith('采用 soft-only：')) fail('Codex 不可观测时必须冻结为 soft-only 策略');
  if (fixture.codex?.events?.length !== 0) fail('Codex 未观测结论要求 events 为空');
  if ((fixture.claude?.events || []).length < 4) fail('Claude observed 结论至少需要四条真实事件');
  if (!failed) ok('P0 报告、fixture 与深度策略结论一致，且未把 Codex 未观测能力升级为硬门禁');
}

process.exitCode = failed ? 1 : 0;
