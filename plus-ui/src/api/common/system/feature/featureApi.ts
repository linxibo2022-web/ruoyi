/**
 * 系统功能配置 API
 */

/**
 * 系统功能配置
 */
export interface SystemFeature {
  /** langchain4j 是否启用 */
  langchain4jEnabled?: boolean
  /** langchain4j 深度思考是否可用（启用且至少一个 provider 开了 enableThinking） */
  langchain4jThinkingEnabled?: boolean
  /** WebSocket 是否启用 */
  websocketEnabled?: boolean
  /** SSE 是否启用 */
  sseEnabled?: boolean
  /** 开放API是否启用 */
  openApiEnabled?: boolean
  /** 开放API访问控制模式 */
  openApiAccessMode?: 'ALL' | 'ROLES' | 'ADMIN' | 'SUPER_ADMIN'
  /** 开放API允许的角色列表 */
  openApiAllowedRoles?: string[]
}

/**
 * 获取系统功能配置
 * @returns {Result<SystemFeature>} 结果
 */
export const getSystemFeatures = (): Result<SystemFeature> => {
  return http.get<SystemFeature>('/common/system/features')
}
