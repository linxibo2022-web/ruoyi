// 广告配置 API
import type { AdQuery, AdBo, AdVo } from './adTypes'

/**
 * 查询广告配置列表
 * @param query 查询参数
 * @returns {Result<PageResult<AdVo>>} 结果
 */
export const pageAds = (query?: AdQuery): Result<PageResult<AdVo>> => {
  return http.get<PageResult<AdVo>>('/base/ad/pageAds', query)
}

/**
 * 查询广告配置详细
 * @param id 广告配置ID
 * @returns {Result<AdVo>} 结果
 */
export const getAd = (id: string | number): Result<AdVo> => {
  return http.get<AdVo>(`/base/ad/getAd/${id}`)
}

/**
 * 新增广告配置
 * @param data 广告配置数据
 * @returns {Result<string | number>} 结果
 */
export const addAd = (data: AdBo): Result<string | number> => {
  return http.post<string | number>('/base/ad/addAd', data)
}

/**
 * 修改广告配置
 * @param data 广告配置数据
 * @returns {Result<void>} 结果
 */
export const updateAd = (data: AdBo): Result<void> => {
  return http.put<void>('/base/ad/updateAd', data)
}

/**
 * 删除广告配置
 * @param ids 广告配置ID
 * @returns {Result<void>} 结果
 */
export const deleteAds = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/base/ad/deleteAds/${ids}`)
}
