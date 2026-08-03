// 错误日志 API
import type { ErrorLogQuery, ErrorLogBo, ErrorLogVo } from './errorLogTypes'

/**
 * 查询错误日志列表
 */
export const pageErrorLogs = (query?: ErrorLogQuery): Result<PageResult<ErrorLogVo>> => {
  return http.get<PageResult<ErrorLogVo>>('/monitor/errorLog/pageErrorLogs', query)
}

/**
 * 查询错误日志详细
 */
export const getErrorLog = (id: string | number): Result<ErrorLogVo> => {
  return http.get<ErrorLogVo>(`/monitor/errorLog/getErrorLog/${id}`)
}

/**
 * 更新错误日志处理状态
 */
export const updateHandleStatus = (data: ErrorLogBo): Result<void> => {
  return http.put<void>('/monitor/errorLog/updateHandleStatus', data)
}

/**
 * 批量更新错误日志处理状态
 */
export const updateHandleStatusBatch = (data: ErrorLogBo): Result<void> => {
  return http.put<void>('/monitor/errorLog/updateHandleStatusBatch', data)
}

/**
 * 按相同错误批量更新处理状态
 */
export const updateSameErrorLogs = (data: ErrorLogBo): Result<void> => {
  return http.put<void>('/monitor/errorLog/updateSameErrorLogs', data)
}

/**
 * 按相同接口批量更新处理状态
 */
export const updateSameRequestLogs = (data: ErrorLogBo): Result<void> => {
  return http.put<void>('/monitor/errorLog/updateSameRequestLogs', data)
}

/**
 * 删除错误日志
 */
export const deleteErrorLogs = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/monitor/errorLog/deleteErrorLogs/${ids}`)
}

/**
 * 清空错误日志
 */
export const clearErrorLogs = (): Result<void> => {
  return http.del<void>('/monitor/errorLog/clearErrorLogs')
}
