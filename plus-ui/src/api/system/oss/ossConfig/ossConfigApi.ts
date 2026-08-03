// 存储配置 API
import type { SysOssConfigQuery, SysOssConfigBo, SysOssConfigVo } from './ossConfigTypes'

/**
 * 查询对象存储配置列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysOssConfigVo>>} 结果
 */
export const pageOssConfigs = (query?: SysOssConfigQuery): Result<PageResult<SysOssConfigVo>> => {
  return http.get<PageResult<SysOssConfigVo>>('/resource/ossConfig/pageOssConfigs', query)
}

/**
 * 查询对象存储配置详细
 * @param ossConfigId 配置ID
 * @returns {Result<SysOssConfigVo>} 结果
 */
export const getOssConfig = (ossConfigId: string | number): Result<SysOssConfigVo> => {
  return http.get<SysOssConfigVo>(`/resource/ossConfig/getOssConfig/${ossConfigId}`)
}

/**
 * 新增对象存储配置
 * @param data 配置数据
 * @returns {Result<string | number>} 结果
 */
export const addOssConfig = (data: SysOssConfigBo): Result<string | number> => {
  return http.post<string | number>('/resource/ossConfig/addOssConfig', data)
}

/**
 * 修改对象存储配置
 * @param data 配置数据
 * @returns {Result<void>} 结果
 */
export const updateOssConfig = (data: SysOssConfigBo): Result<void> => {
  return http.put<void>('/resource/ossConfig/updateOssConfig', data)
}

/**
 * 删除对象存储配置
 * @param ossConfigId 配置ID
 * @returns {Result<void>} 结果
 */
export const deleteOssConfigs = (ossConfigId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/resource/ossConfig/deleteOssConfigs/${ossConfigId}`)
}

/**
 * 对象存储状态修改
 * @param ossConfigId 配置ID
 * @param status 状态
 * @param configKey 配置键
 * @returns {Result<void>} 结果
 */
export const changeOssConfigStatus = (ossConfigId: string | number, status: string, configKey: string): Result<void> => {
  const data = {
    ossConfigId,
    status,
    configKey
  }
  return http.put<void>('/resource/ossConfig/changeOssConfigStatus', data)
}
