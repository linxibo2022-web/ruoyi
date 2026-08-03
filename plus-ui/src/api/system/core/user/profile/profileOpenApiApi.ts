// 个人中心开放接口 API
import type { SysApiKeyBo, SysApiKeyVo, OpenApiSecretVo, OpenApiInfoVo } from '@/api/system/openApi/openApiTypes'

/**
 * 查询当前用户的API密钥列表
 * @returns {Result<SysApiKeyVo[]>} 结果
 */
export const listMyApiKeys = (): Result<SysApiKeyVo[]> => {
  return http.get<SysApiKeyVo[]>('/system/user/profile/openApi/listMyApiKeys')
}

/**
 * 生成个人API密钥
 * @param data API密钥数据
 * @returns {Result<OpenApiSecretVo>} 结果(包含明文Secret)
 */
export const generateMyApiKey = (data: SysApiKeyBo): Result<OpenApiSecretVo> => {
  return http.post<OpenApiSecretVo>('/system/user/profile/openApi/generateMyApiKey', data)
}

/**
 * 删除个人API密钥
 * @param id API密钥ID
 * @returns {Result<void>} 结果
 */
export const deleteMyApiKey = (id: string | number): Result<void> => {
  return http.del<void>(`/system/user/profile/openApi/deleteMyApiKey/${id}`)
}

/**
 * 重置个人API密钥
 * @param id API密钥ID
 * @returns {Result<OpenApiSecretVo>} 结果(包含新的明文Secret)
 */
export const resetMyApiKey = (id: string | number): Result<OpenApiSecretVo> => {
  return http.put<OpenApiSecretVo>(`/system/user/profile/openApi/resetMyApiKey/${id}`)
}

/**
 * 更新个人API密钥的白名单
 * @param id API密钥ID
 * @param data API密钥数据(仅whiteIps字段)
 * @returns {Result<void>} 结果
 */
export const updateMyApiKeyWhiteIps = (id: string | number, data: SysApiKeyBo): Result<void> => {
  return http.put<void>(`/system/user/profile/openApi/updateMyApiKeyWhiteIps/${id}`, data)
}

/**
 * 获取最大密钥数量配置
 * @returns {Result<number>} 结果
 */
export const getMaxKeys = (): Result<number> => {
  return http.get<number>('/system/user/profile/openApi/getMaxKeys')
}

/**
 * 获取当前用户可访问的开放接口列表
 * @returns {Result<OpenApiInfoVo[]>} 结果
 */
export const listMyOpenApis = (): Result<OpenApiInfoVo[]> => {
  return http.get<OpenApiInfoVo[]>('/system/user/profile/openApi/listMyOpenApis')
}
