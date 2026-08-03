// 登录日志 API
import type { SysLoginLogQuery, SysLoginLogVo } from './loginLogTypes'

/**
 * 查询登录日志列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysLoginLogVo>>} 结果
 */
export const pageLoginLogs = (query?: SysLoginLogQuery): Result<PageResult<SysLoginLogVo>> => {
  return http.get<PageResult<SysLoginLogVo>>('/monitor/loginLog/pageLoginLogs', query)
}

/**
 * 删除登录日志
 * @param infoId 日志ID
 * @returns {Result<void>} 结果
 */
export const deleteLoginLogs = (infoId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/monitor/loginLog/deleteLoginLogs/${infoId}`)
}

/**
 * 解锁用户登录状态
 * @param userName 用户名
 * @returns {Result<void>} 结果
 */
export const unlockLoginLog = (userName: string): Result<void> => {
  return http.get<void>(`/monitor/loginLog/unlockLoginLog/${userName}`)
}

/**
 * 清空登录日志
 * @returns {Result<void>} 结果
 */
export const clearLoginLogs = (): Result<void> => {
  return http.del<void>('/monitor/loginLog/clearLoginLogs')
}
