/**
 * AI助手API接口
 * @author 抓蛙师
 * @date 2025-01-26
 */

import type { AiChatBo, AiChatVo } from './aiTypes'

/**
 * AI对话 - 统一入口
 * @param aiChatBo 对话业务对象 (对应后端 AiChatBo)
 * @returns {Result<AiChatVo>} AI对话视图对象
 */
export const aiChat = (aiChatBo: AiChatBo): Result<AiChatVo> => {
  return http.post<AiChatVo>('/base/ai/aiChat', aiChatBo)
}

/**
 * 文本优化
 * @param aiChatBo 对话业务对象
 * @returns {Result<AiChatVo>} 优化结果
 */
export const aiOptimize = (aiChatBo: AiChatBo): Result<AiChatVo> => {
  return http.post<AiChatVo>('/base/ai/aiOptimize', aiChatBo)
}

/**
 * 数据生成
 * @param aiChatBo 对话业务对象
 * @returns {Result<AiChatVo>} 生成结果
 */
export const aiGenerate = (aiChatBo: AiChatBo): Result<AiChatVo> => {
  return http.post<AiChatVo>('/base/ai/aiGenerate', aiChatBo)
}

/**
 * 内容审核
 * @param aiChatBo 对话业务对象
 * @returns {Result<AiChatVo>} 审核结果
 */
export const aiReview = (aiChatBo: AiChatBo): Result<AiChatVo> => {
  return http.post<AiChatVo>('/base/ai/aiReview', aiChatBo)
}

/**
 * 文本翻译
 * @param aiChatBo 对话业务对象
 * @returns {Result<AiChatVo>} 翻译结果
 */
export const aiTranslate = (aiChatBo: AiChatBo): Result<AiChatVo> => {
  return http.post<AiChatVo>('/base/ai/aiTranslate', aiChatBo)
}
