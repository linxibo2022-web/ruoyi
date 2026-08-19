#!/usr/bin/env node
/**
 * Claude Code 技能路由适配层。
 *
 * 路由规则统一由 .agent-governance/lib/router.cjs 维护，避免 Hook 与
 * manifest 出现触发词漂移。本文件只负责将确定的路由渲染为固定路径。
 */

const path = require('path');
const { selectRoute } = require('../../.agent-governance/lib/router.cjs');

/**
 * 根据 Hook 输入选择路由。
 *
 * @param {{ prompt?: unknown }} input Hook 的标准输入对象
 * @returns {{ primary: string | null, helpers: string[], reason: string, bypass: boolean }} 路由结果
 */
function routeInput(input) {
  try {
    return selectRoute(typeof input?.prompt === 'string' ? input.prompt : '');
  } catch {
    // 公共路由异常时降级为空路由，不能阻断 Claude Code 的正常会话。
    return { primary: null, helpers: [], reason: 'empty', bypass: false };
  }
}

/**
 * 将路由转换为不含用户提示词的固定说明。
 *
 * @param {{ primary: string | null, helpers: string[], reason: string, bypass: boolean }} route 路由结果
 * @returns {string} 可注入 Claude Code 上下文的说明
 */
function renderRoute(route) {
  if (route.bypass || !route.primary) return '';

  const skills = [route.primary, ...route.helpers];
  const paths = skills.map(skill => path.posix.join('.claude/skills', skill, 'SKILL.md'));
  const helperLines = route.helpers.length
    ? `\n辅助技能：${route.helpers.map(skill => `\`${skill}\``).join('、')}`
    : '';
  const requiredNotice = route.reason === 'required-dependency'
    ? '\n说明：manifest 标记的必需依赖可突破默认辅助技能上限。'
    : '';

  return `## 技能评估结果\n主技能：\`${route.primary}\`${helperLines}\n路由原因：\`${route.reason}\`${requiredNotice}\n开始实现前按顺序读取：\n${paths.map((file, index) => `${index + 1}. \`${file}\``).join('\n')}`;
}

module.exports = { renderRoute, routeInput };
