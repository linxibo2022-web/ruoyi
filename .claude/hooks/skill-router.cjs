#!/usr/bin/env node
/**
 * Claude Code 技能路由适配层。
 *
 * 路由规则统一由 .agent-governance/lib/router.cjs 维护，避免 Hook 与
 * manifest 出现触发词漂移。本文件只负责将确定的路由渲染为固定路径。
 */

const fs = require('fs');
const path = require('path');
const { selectRoute } = require('../../.agent-governance/lib/router.cjs');

/**
 * 根据 Hook 输入选择路由。
 *
 * @param {{ prompt?: unknown }} input Hook 的标准输入对象
 * @returns {{ primary: string | null, helpers: string[], matches: string[], reason: string, bypass: boolean }} 路由结果
 */
function routeInput(input) {
  try {
    return selectRoute(typeof input?.prompt === 'string' ? input.prompt : '', undefined, 'claude');
  } catch {
    // 公共路由异常时降级为空路由，不能阻断 Claude Code 的正常会话。
    return { primary: null, helpers: [], matches: [], reason: 'empty', bypass: false };
  }
}

/**
 * 将路由转换为不含用户提示词的固定说明。
 *
 * @param {{ primary: string | null, helpers: string[], matches: string[], reason: string, bypass: boolean }} route 路由结果
 * @returns {string} 命中技能时可注入 Claude Code 上下文的说明；无需路由时为空字符串
 */
function renderRoute(route) {
  const header = '⚙️ 强制技能评估';
  if (route.bypass || !route.primary) return '';

  const candidates = route.matches.length > 1
    ? `；候选技能：${route.matches.join('、')}`
    : '';
  const helperLines = route.helpers.length
    ? `；满足依赖条件时加载：${route.helpers.join('、')}`
    : '';
  const requiredNotice = route.reason === 'required-dependency'
    ? '；必需依赖按 manifest 的绝对上限保留'
    : '';

  const skillPath = path.posix.join('.claude/skills', route.primary, 'SKILL.md');
  const commandPath = path.posix.join('.claude/commands', `${route.primary}.md`);
  const target = fs.existsSync(path.join(__dirname, '..', 'skills', route.primary, 'SKILL.md')) ? skillPath : commandPath;
  return `${header}：匹配技能 【🟨 ${route.primary}】${candidates}${helperLines}；路由原因：${route.reason}${requiredNotice}。不要预读技能正文；开始对应子任务前再读取 ${target}。`;
}

module.exports = { renderRoute, routeInput };
