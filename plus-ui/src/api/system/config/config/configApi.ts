// 参数配置 API
import type { SysConfigQuery, SysConfigBo, SysConfigVo } from './configTypes'

/**
 * 查询参数列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysConfigVo>>} 结果
 */
export const pageConfigs = (query?: SysConfigQuery): Result<PageResult<SysConfigVo>> => {
  return http.get<PageResult<SysConfigVo>>('/system/config/pageConfigs', query)
}

/**
 * 查询参数详细
 * @param configId 参数ID
 * @returns {Result<SysConfigVo>} 结果
 */
export const getConfig = (configId: string | number): Result<SysConfigVo> => {
  return http.get<SysConfigVo>(`/system/config/getConfig/${configId}`)
}

/**
 * 根据参数键名查询参数值
 * @param configKey 参数键名
 * @returns {Result<string>} 结果
 */
export const getByConfigKey = (configKey: string): Result<string> => {
  return http.get<string>(`/system/config/getByConfigKey/${configKey}`)
}

/**
 * 新增参数配置
 * @param data 参数数据
 * @returns {Result<string | number>} 结果
 */
export const addConfig = (data: SysConfigBo): Result<string | number> => {
  return http.post<string | number>('/system/config/addConfig', data)
}

/**
 * 修改参数配置
 * @param data 参数数据
 * @returns {Result<void>} 结果
 */
export const updateConfig = (data: SysConfigBo): Result<void> => {
  return http.put<void>('/system/config/updateConfig', data)
}

/**
 * 修改参数配置
 * @param key 参数键名
 * @param value 参数键值
 * @returns {Result<void>} 结果
 */
export const updateConfigByKey = (key: string, value: any): Result<void> => {
  return http.put<void>('/system/config/updateConfigByKey', {
    configKey: key,
    configValue: value
  })
}

/**
 * 删除参数配置
 * @param configIds 参数ID
 * @returns {Result<void>} 结果
 */
export const deleteConfigs = (configIds: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/system/config/deleteConfigs/${configIds}`)
}

/**
 * 刷新参数缓存
 * @returns {Result<void>} 结果
 */
export const clearConfigCache = (): Result<void> => {
  return http.del<void>('/system/config/clearConfigCache')
}
