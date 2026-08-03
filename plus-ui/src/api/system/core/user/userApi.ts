// 用户管理 API
import { parseStrEmpty } from '@/utils/string'
import type { SysRoleVo } from '@/api/system/core/role/roleTypes'
import type { SysUserQuery, SysUserBo, SysUserVo, SysUserInfoVo, UserInfoVo, SysUserPasswordBo, ProfileVo } from './userTypes'
import { withHeaders } from '@/utils/function'

/**
 * 获取当前用户详细信息 (登录成功后调用)
 * 用于获取登录用户的权限、角色等详细信息
 * @returns {Result<UserInfoVo>}
 */
export const getUserInfo = (): Result<UserInfoVo> => {
  return http.get<UserInfoVo>('/system/user/getUserInfo')
}

/**
 * 查询用户个人信息(个人中心)
 * @returns {Result<ProfileVo>} 结果
 */
export const getUserProfile = (): Result<ProfileVo> => {
  return http.get<ProfileVo>('/system/user/getUserProfile')
}

/**
 * 查询用户列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysUserVo>>} 结果
 */
export const pageUsers = (query: SysUserQuery): Result<PageResult<SysUserVo>> => {
  return http.get<PageResult<SysUserVo>>('/system/user/pageUsers', query)
}

/**
 * 获取用户详情
 * @param userId 用户ID
 * @returns {Result<SysUserInfoVo>} 结果
 */
export const getUser = (userId?: string | number): Result<SysUserInfoVo> => {
  return http.get<SysUserInfoVo>(`/system/user/getUser/${parseStrEmpty(userId)}`)
}

/**
 * 新增用户
 * @param data 用户数据
 * @returns {Result<string | number>} 结果
 */
export const addUser = (data: SysUserBo): Result<string | number> => {
  return http.post<string | number>('/system/user/addUser', data)
}

/**
 * 修改用户
 * @param data 用户数据
 * @returns {Result<void>} 结果
 */
export const updateUser = (data: SysUserBo): Result<void> => {
  return http.put<void>('/system/user/updateUser', data)
}

/**
 * 删除用户
 * @param userIds 用户ID
 * @returns {Result<void>} 结果
 */
export const deleteUsers = (userIds: Array<string | number> | string | number): Result<void> => {
  return http.del<void>(`/system/user/deleteUsers/${userIds}`)
}

/**
 * 用户密码重置
 * @param userId 用户ID
 * @param password 密码
 * @returns {Result<void>} 结果
 */
export const resetUserPwd = (userId: string | number, password: string): Result<void> => {
  const data = {
    userId,
    password
  }
  return http.put<void>(
    '/system/user/resetUserPwd',
    data,
    withHeaders({
      isEncrypt: true,
      repeatSubmit: false
    })
  )
}

/**
 * 修改用户个人信息
 * @param data 用户信息
 * @returns {Result<void>} 结果
 */
export const updateUserProfile = (data: SysUserBo): Result<void> => {
  return http.put<void>('/system/user/updateUserProfile', data)
}

/**
 * 用户密码重置（个人）
 * @param data 用户密码信息
 * @returns {Result<void>} 结果
 */
export const updateUserPwd = (data: SysUserPasswordBo): Result<void> => {
  return http.put<void>(
    '/system/user/updateUserPwd',
    data,
    withHeaders({
      isEncrypt: true,
      repeatSubmit: false
    })
  )
}

/**
 * 用户状态修改
 * @param userId 用户ID
 * @param status 用户状态
 * @returns {Result<void>} 结果
 */
export const changeUserStatus = (userId: number | string, status: string): Result<void> => {
  const data = {
    userId,
    status
  }
  return http.put<void>('/system/user/changeUserStatus', data)
}

/**
 * 查询授权角色
 * @param userId 用户ID
 * @returns {Result<{ user: SysUserVo; roles: SysRoleVo[] }>} 结果
 */
export const getUserWithRoles = (userId: string | number): Result<{ user: SysUserVo; roles: SysRoleVo[] }> => {
  return http.get<{ user: SysUserVo; roles: SysRoleVo[] }>(`/system/user/getUserWithRoles/${userId}`)
}

/**
 * 通过用户ids查询用户
 * @param userIds 用户ID数组
 * @returns {Result<SysUserVo[]>} 结果
 */
export const getUserOptions = (userIds: (number | string)[]): Result<SysUserVo[]> => {
  return http.get<SysUserVo[]>(`/system/user/getUserOptions?userIds=${userIds}`)
}

/**
 * 查询当前部门的所有用户信息
 * @param deptId 部门ID
 * @returns {Result<SysUserVo[]>} 结果
 */
export const listUsersByDeptId = (deptId: string | number): Result<SysUserVo[]> => {
  return http.get<SysUserVo[]>(`/system/user/listUsersByDeptId/${deptId}`)
}

/**
 * 用户头像上传
 * @param data 头像文件
 * @returns {Result<any>} 结果
 */
export const uploadAvatar = (data: FormData): Result<any> => {
  return http.post<any>('/system/user/uploadAvatar', data)
}
