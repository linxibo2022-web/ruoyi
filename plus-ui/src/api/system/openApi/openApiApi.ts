// 开放接口 API
import type { SysApiKeyQuery, SysApiKeyBo, SysApiKeyVo, OpenApiSecretVo } from './openApiTypes'

/**
 * 查询API密钥列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysApiKeyVo>>} 结果
 */
export const pageApiKeys = (query?: SysApiKeyQuery): Result<PageResult<SysApiKeyVo>> => {
  return http.get<PageResult<SysApiKeyVo>>('/system/openApi/pageApiKeys', query)
}

/**
 * 查询API密钥详细
 * @param id API密钥ID
 * @returns {Result<SysApiKeyVo>} 结果
 */
export const getApiKey = (id: string | number): Result<SysApiKeyVo> => {
  return http.get<SysApiKeyVo>(`/system/openApi/getApiKey/${id}`)
}

/**
 * 生成新的API密钥
 * @param data API密钥数据
 * @returns {Result<OpenApiSecretVo>} 结果(包含明文Secret)
 */
export const generateApiKey = (data: SysApiKeyBo): Result<OpenApiSecretVo> => {
  return http.post<OpenApiSecretVo>('/system/openApi/generateApiKey', data)
}

/**
 * 修改API密钥
 * @param data API密钥数据
 * @returns {Result<void>} 结果
 */
export const updateApiKey = (data: SysApiKeyBo): Result<void> => {
  return http.put<void>('/system/openApi/updateApiKey', data)
}

/**
 * 重置密钥(重新生成AppSecret)
 * @param id API密钥ID
 * @returns {Result<OpenApiSecretVo>} 结果(包含新的明文Secret)
 */
export const resetSecret = (id: string | number): Result<OpenApiSecretVo> => {
  return http.put<OpenApiSecretVo>(`/system/openApi/resetSecret/${id}`)
}

/**
 * 删除API密钥
 * @param id API密钥ID
 * @returns {Result<void>} 结果
 */
export const deleteApiKeys = (id: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/system/openApi/deleteApiKeys/${id}`)
}
