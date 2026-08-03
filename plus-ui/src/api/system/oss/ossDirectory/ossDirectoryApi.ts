// 文件目录 API
import type { SysOssDirectoryQuery, SysOssDirectoryBo, SysOssDirectoryVo, SysOssDirectoryTreeVo } from './ossDirectoryTypes'

/**
 * 查询OSS目录列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysOssDirectoryVo>>} 结果
 */
export const pageOssDirectorys = (query?: SysOssDirectoryQuery): Result<PageResult<SysOssDirectoryVo>> => {
  return http.get<PageResult<SysOssDirectoryVo>>('/resource/ossDirectory/pageOssDirectorys', query)
}

/**
 * 查询OSS目录详细
 * @param directoryId OSS目录ID
 * @returns {Result<SysOssDirectoryVo>} 结果
 */
export const getOssDirectory = (directoryId: string | number): Result<SysOssDirectoryVo> => {
  return http.get<SysOssDirectoryVo>(`/resource/ossDirectory/getOssDirectory/${directoryId}`)
}

/**
 * 新增OSS目录
 * @param data OSS目录数据
 * @returns {Result<string | number>} 结果
 */
export const addOssDirectory = (data: SysOssDirectoryBo): Result<string | number> => {
  return http.post<string | number>('/resource/ossDirectory/addOssDirectory', data)
}

/**
 * 修改OSS目录
 * @param data OSS目录数据
 * @returns {Result<void>} 结果
 */
export const updateOssDirectory = (data: SysOssDirectoryBo): Result<void> => {
  return http.put<void>('/resource/ossDirectory/updateOssDirectory', data)
}

/**
 * 移动OSS目录
 * @param directoryId 目标目录ID
 * @param ossIds OSS文件ID
 * @returns {Result<void>} 结果
 */
export const moveOssDirectory = (directoryId: string | number, ossIds: string | number | Array<string | number>): Result<void> => {
  return http.put<void>(`/resource/ossDirectory/moveOssDirectory/${ossIds}?directoryId=${directoryId}`)
}

/**
 * 删除OSS目录
 * @param directoryId OSS目录ID
 * @returns {Result<void>} 结果
 */
export const deleteOssDirectorys = (directoryId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/resource/ossDirectory/deleteOssDirectorys/${directoryId}`)
}

/**
 * 获取OSS目录树选项
 * @returns {Result<SysOssDirectoryTreeVo[]>} 结果
 */
export const getOssDirectoryTreeOptions = (): Result<SysOssDirectoryTreeVo[]> => {
  return http.get<SysOssDirectoryTreeVo[]>('/resource/ossDirectory/getOssDirectoryTreeOptions')
}
