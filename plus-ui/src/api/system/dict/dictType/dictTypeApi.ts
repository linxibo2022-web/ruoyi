// 字典类型 API
import type { SysDictTypeQuery, SysDictTypeBo, SysDictTypeVo } from './dictTypeTypes'

/**
 * 查询字典类型列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysDictTypeVo>>} 结果
 */
export const pageDictTypes = (query?: SysDictTypeQuery): Result<PageResult<SysDictTypeVo>> => {
  return http.get<PageResult<SysDictTypeVo>>('/system/dictType/pageDictTypes', query)
}

/**
 * 查询字典类型详细
 * @param dictId 字典ID
 * @returns {Result<SysDictTypeVo>} 结果
 */
export const getDictType = (dictId: number | string): Result<SysDictTypeVo> => {
  return http.get<SysDictTypeVo>(`/system/dictType/getDictType/${dictId}`)
}

/**
 * 新增字典类型
 * @param data 字典数据
 * @returns {Result<string | number>} 结果
 */
export const addDictType = (data: SysDictTypeBo): Result<string | number> => {
  return http.post<string | number>('/system/dictType/addDictType', data)
}

/**
 * 修改字典类型
 * @param data 字典数据
 * @returns {Result<void>} 结果
 */
export const updateDictType = (data: SysDictTypeBo): Result<void> => {
  return http.put<void>('/system/dictType/updateDictType', data)
}

/**
 * 删除字典类型
 * @param dictId 字典ID
 * @returns {Result<void>} 结果
 */
export const deleteDictTypes = (dictId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/system/dictType/deleteDictTypes/${dictId}`)
}

/**
 * 刷新字典缓存
 * @returns {Result<void>} 结果
 */
export const refreshDictCache = (): Result<void> => {
  return http.del<void>('/system/dictType/refreshDictCache')
}

/**
 * 获取字典选择框列表
 * @returns {Result<SysDictTypeVo[]>} 结果
 */
export const getDictTypeOptions = (): Result<SysDictTypeVo[]> => {
  return http.get<SysDictTypeVo[]>('/system/dictType/getDictTypeOptions')
}
