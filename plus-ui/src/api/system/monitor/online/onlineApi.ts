// 在线用户 API
import type { SysUserOnlineQuery, SysUserOnlineVo } from './onlineTypes'

/**
 * 查询在线用户列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysUserOnlineVo>>} 结果
 */
export const pageOnlineUsers = (query?: SysUserOnlineQuery): Result<PageResult<SysUserOnlineVo>> => {
  return http.get<PageResult<SysUserOnlineVo>>('/monitor/online/pageOnlineUsers', query)
}

/**
 * 强退用户
 * @param tokenId 令牌ID
 * @returns {Result<void>} 结果
 */
export const forceLogout = (tokenId: string): Result<void> => {
  return http.del<void>(`/monitor/online/forceLogout/${tokenId}`)
}

/**
 * 获取当前用户登录在线设备
 * @returns {Result<SysUserOnlineVo[]>} 结果
 */
export const listCurrentUserOnlines = (): Result<SysUserOnlineVo[]> => {
  return http.get<SysUserOnlineVo[]>('/monitor/online/listCurrentUserOnlines')
}

/**
 * 删除当前在线设备
 * @param tokenId 令牌ID
 * @returns {Result<void>} 结果
 */
export const removeCurrentDevice = (tokenId: string): Result<void> => {
  return http.del<void>(`/monitor/online/removeCurrentDevice/${tokenId}`)
}
