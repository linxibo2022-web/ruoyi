// 字典数据 API
import type { SysDictDataBo, SysDictDataQuery, SysDictDataVo } from './dictDataTypes'

/**
 * 根据字典类型查询字典数据信息
 * @param dictType 字典类型
 * @returns {Result<SysDictDataVo[]>} 结果
 */
export const listDictDatasByDictType = (dictType: string): Result<SysDictDataVo[]> => {
  return http.get<SysDictDataVo[]>(`/system/dictData/listDictDatasByDictType/${dictType}`)
}

/**
 * 查询字典数据列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysDictDataVo>>} 结果
 */
export const pageDictDatas = (query: SysDictDataQuery): Result<PageResult<SysDictDataVo>> => {
  return http.get<PageResult<SysDictDataVo>>('/system/dictData/pageDictDatas', query)
}

/**
 * 查询字典数据详细
 * @param dictDataId 字典编码
 * @returns {Result<SysDictDataVo>} 结果
 */
export const getDictData = (dictDataId: string | number): Result<SysDictDataVo> => {
  return http.get<SysDictDataVo>(`/system/dictData/getDictData/${dictDataId}`)
}

/**
 * 新增字典数据
 * @param data 字典数据
 * @returns {Result<any>} 结果
 */
export const addDictData = (data: SysDictDataBo): Result<any> => {
  return http.post<any>('/system/dictData/addDictData', data)
}

/**
 * 修改字典数据
 * @param data 字典数据
 * @returns {Result<any>} 结果
 */
export const updateDictData = (data: SysDictDataBo): Result<any> => {
  return http.put<any>('/system/dictData/updateDictData', data)
}

/**
 * 删除字典数据
 * @param dictDataId 字典编码
 * @returns {Result<any>} 结果
 */
export const deleteDictDatas = (
  dictDataId: string | number | Array<string | number>,
): Result<any> => {
  return http.del<any>(`/system/dictData/deleteDictDatas/${dictDataId}`)
}
