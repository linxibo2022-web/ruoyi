// 用户角色授权 API
import type { SysUserQuery, SysUserVo } from '@/api/system/core/user/userTypes'

/**
 * 分页查询角色已授权用户列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysUserVo>>} 结果
 */
export const pageRoleAuthorizedUsers = (query: SysUserQuery): Result<PageResult<SysUserVo>> => {
  return http.get<PageResult<SysUserVo>>('/system/userRole/pageRoleAuthorizedUsers', query)
}

/**
 * 分页查询角色未授权用户列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysUserVo>>} 结果
 */
export const pageRoleUnauthorizedUsers = (query: SysUserQuery): Result<PageResult<SysUserVo>> => {
  return http.get<PageResult<SysUserVo>>('/system/userRole/pageRoleUnauthorizedUsers', query)
}

/**
 * 撤销用户角色
 * @param data 撤销数据
 * @returns {Result<void>} 结果
 */
export const revokeUserRole = (data: { userId: string | number; roleId: string | number }): Result<void> => {
  return http.put<void>('/system/userRole/revokeUserRole', data)
}

/**
 * 批量撤销用户角色
 * @param data 批量撤销数据
 * @returns {Result<void>} 结果
 */
export const batchRevokeUserRoles = (data: { roleId: string | number; userIds: (string | number)[] }): Result<void> => {
  return http.put<void>('/system/userRole/batchRevokeUserRoles', null, { params: data })
}

/**
 * 批量授权用户角色
 * @param data 批量授权数据
 * @returns {Result<void>} 结果
 */
export const batchGrantUserRoles = (data: { userIds: (string | number)[]; roleId: string | number }): Result<void> => {
  return http.put<void>('/system/userRole/batchGrantUserRoles', null, { params: data })
}

/**
 * 用户授权角色
 * @param data 用户角色数据
 * @returns {Result<void>} 结果
 */
export const assignUserRoles = (data: { userId: string; roleIds: string }): Result<void> => {
  return http.put<void>('/system/userRole/assignUserRoles', null, { params: data })
}
