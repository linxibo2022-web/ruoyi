// 平台配置 API
import type { PlatformQuery, PlatformBo, PlatformVo } from '@/api/business/base/platform/platformTypes'

/**
 * 查询平台配置列表
 * @param query 查询参数
 * @returns {Result<PageResult<PlatformVo>>} 结果
 */
export const pagePlatforms = (query?: PlatformQuery): Result<PageResult<PlatformVo>> => {
  return http.get<PageResult<PlatformVo>>('/base/platform/pagePlatforms', query)
}

/**
 * 查询平台配置详细
 * @param id 平台配置ID
 * @returns {Result<PlatformVo>} 结果
 */
export const getPlatform = (id: string | number): Result<PlatformVo> => {
  return http.get<PlatformVo>(`/base/platform/getPlatform/${id}`)
}

/**
 * 新增平台配置
 * @param data 平台配置数据
 * @returns {Result<string | number>} 结果
 */
export const addPlatform = (data: PlatformBo): Result<string | number> => {
  return http.post<string | number>('/base/platform/addPlatform', data)
}

/**
 * 修改平台配置
 * @param data 平台配置数据
 * @returns {Result<void>} 结果
 */
export const updatePlatform = (data: PlatformBo): Result<void> => {
  return http.put<void>('/base/platform/updatePlatform', data)
}

/**
 * 删除平台配置
 * @param ids 平台配置ID
 * @returns {Result<void>} 结果
 */
export const deletePlatforms = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/base/platform/deletePlatforms/${ids}`)
}
