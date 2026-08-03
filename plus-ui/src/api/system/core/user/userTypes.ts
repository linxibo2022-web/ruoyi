// 用户管理类型
import { SysRoleVo } from '@/api/system/core/role/roleTypes'
import { SysPostVo } from '@/api/system/core/post/postTypes'

/** 用户信息查询类型 */
export interface SysUserQuery extends PageQuery {
  /** 用户账号 */
  userName?: string

  /** 手机号码 */
  phone?: string

  /** 帐号状态 */
  status?: string

  /** 部门id */
  deptId?: string | number

  /** 角色id */
  roleId?: string | number

  /**用户id列表*/
  userIds?: string | number
}

/** 用户信息表单类型 */
export interface SysUserBo {
  /** 用户ID */
  userId?: string | number

  /** 部门ID */
  deptId?: string | number

  /** 用户账号 */
  userName: string

  /** 用户昵称 */
  nickName?: string

  /** 密码 */
  password: string

  /** 手机号码 */
  phone?: string

  /** 用户邮箱 */
  email?: string

  /** 用户性别 */
  gender?: string

  /** 帐号状态 */
  status: string

  /** 备注 */
  remark?: string

  /**岗位id列表*/
  postIds: string[]

  /**角色id列表*/
  roleIds: string[]
}

/** 用户信息视图类型 */
export interface SysUserVo {
  /** 用户ID */
  userId: string | number

  /** 租户id */
  tenantId: string

  /** 部门ID */
  deptId: string | number

  /** 用户账号 */
  userName: string

  /** 用户昵称 */
  nickName: string

  /** 用户类型 */
  userType: string

  /** 用户邮箱 */
  email: string

  /** 手机号码 */
  phone: string

  /** 用户性别 */
  gender: string

  /** 头像地址 */
  avatar: string

  /** 帐号状态 */
  status: string

  /** 最后登录IP */
  loginIp: string

  /** 最后登录时间 */
  loginDate: string

  /** 备注 */
  remark: string

  /** 部门名称 */
  deptName: string

  /**角色列表*/
  roles: SysRoleVo[]

  /** 角色id列表 */
  roleIds: any

  /**岗位id列表*/
  postIds: any

  /**角色id*/
  roleId: any

  /**是否管理员*/
  admin: boolean

  /**创建时间*/
  createTime?: string
}

/**
 * 用户信息
 */
export interface UserInfoVo {
  /**用户基本信息*/
  user: SysUserVo

  /**角色标识符列表*/
  roles: string[]

  /**权限标识符列表*/
  permissions: string[]
}

/** 用户密码信息 */
export interface SysUserPasswordBo {
  /**旧密码*/
  oldPassword: string

  /**新密码*/
  newPassword: string

  /**确认新密码*/
  confirmPassword: string
}

export interface ProfileVo {
  /**用户信息*/
  user: SysUserVo

  /**用户所属角色组*/
  roleGroup: string

  /**用户所属岗位组*/
  postGroup: string
}

/**用户信息*/
export interface SysUserInfoVo {
  /**用户信息*/
  user: SysUserVo

  /**角色列表*/
  roles: SysRoleVo[]

  /**角色ID列表*/
  roleIds: string[]

  /**岗位列表*/
  posts: SysPostVo[]

  /**岗位ID列表*/
  postIds: string[]
}
