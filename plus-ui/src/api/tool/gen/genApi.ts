// 代码生成 API
import type { GenTableQuery, GenTable, GenTableDetailVo, GenConfigVo, CodeGenResult } from './genTypes'

/**
 * 查询生成表数据
 * @param query 查询参数
 * @returns {Result<PageResult<GenTable>>} 结果
 */
export const pageGens = (query: GenTableQuery): Result<PageResult<GenTable>> => {
  return http.get<PageResult<GenTable>>('/tool/gen/pageGens', query)
}

/**
 * 查询db数据库列表
 * @param query 查询参数
 * @returns {Result<PageResult<GenTable>>} 结果
 */
export const pageGenDbs = (query: GenTableQuery): Result<PageResult<GenTable>> => {
  return http.get<PageResult<GenTable>>('/tool/gen/pageGenDbs', query)
}

/**
 * 查询表详细信息
 * @param tableId 表ID
 * @returns {Result<GenTableDetailVo>} 结果
 */
export const getGen = (tableId: string | number): Result<GenTableDetailVo> => {
  return http.get<GenTableDetailVo>(`/tool/gen/getGen/${tableId}`)
}

/**
 * 修改代码生成信息
 * @param data 表单数据
 * @returns {Result<void>} 结果
 */
export const updateGen = (data: GenTable): Result<void> => {
  return http.put<void>('/tool/gen/updateGen', data)
}

/**
 * 导入表
 * @param data 导入数据
 * @returns {Result<GenTableDetailVo>} 结果
 */
export const importGens = (data: { tables: string; dataName: string }): Result<GenTableDetailVo> => {
  return http.post<GenTableDetailVo>('/tool/gen/importGens', null, { params: data })
}

/**
 * 预览生成代码
 * @param tableId 表ID
 * @returns {Result<Record<string, string>>} 结果
 */
export const previewGen = (tableId: string | number): Result<Record<string, string>> => {
  return http.get<Record<string, string>>(`/tool/gen/previewGen/${tableId}`)
}

/**
 * 删除表数据
 * @param tableId 表ID
 * @returns {Result<void>} 结果
 */
export const deleteGens = (tableId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/tool/gen/deleteGens/${tableId}`)
}

/**
 * 生成代码（自定义路径）
 * @param tableId 表ID
 * @returns {Result<CodeGenResult>} 结果
 */
export const generateCodes = (tableId: string | number): Result<CodeGenResult> => {
  return http.get<CodeGenResult>(`/tool/gen/generateCodes/${tableId}`)
}

/**
 * 同步数据库
 * @param tableId 表ID
 * @returns {Result<void>} 结果
 */
export const syncGenDb = (tableId: string | number): Result<void> => {
  return http.get<void>(`/tool/gen/syncGenDb/${tableId}`)
}

/**
 * 获取数据源名称
 * @returns {Result<string[]>} 结果
 */
export const getDataSourceNames = (): Result<string[]> => {
  return http.get<string[]>('/tool/gen/getDataSourceNames')
}

/**
 * 获取代码生成器配置信息
 * @returns {Result<GenConfigVo>} 结果
 */
export const getGenConfig = (): Result<GenConfigVo> => {
  return http.get<GenConfigVo>('/tool/gen/getGenConfig')
}
