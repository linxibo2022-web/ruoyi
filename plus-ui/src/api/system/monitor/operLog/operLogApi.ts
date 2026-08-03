// 操作日志 API
import type { SysOperLogQuery, SysOperLogVo } from './operLogTypes'

/**
 * 查询操作日志列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysOperLogVo>>} 结果
 */
export const pageOperLogs = (query?: SysOperLogQuery): Result<PageResult<SysOperLogVo>> => {
  return http.get<PageResult<SysOperLogVo>>('/monitor/operLog/pageOperLogs', query)
}

/**
 * 删除操作日志
 * @param operId 日志ID
 * @returns {Result<void>} 结果
 */
export const deleteOperLogs = (operId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/monitor/operLog/deleteOperLogs/${operId}`)
}

/**
 * 清空操作日志
 * @returns {Result<void>} 结果
 */
export const clearOperLogs = (): Result<void> => {
  return http.del<void>('/monitor/operLog/clearOperLogs')
}
