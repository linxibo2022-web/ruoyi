/** 角色信息查询类型 */
export interface SysRoleQuery extends PageQuery {
  /** 角色名称 */
  roleName?: string

  /** 角色权限字符串 */
  roleKey?: string

  /** 角色状态 */
  status?: string
}

/** 角色信息表单类型 */
export interface SysRoleBo {
  /** 角色ID */
  roleId?: string | number

  /** 角色名称 */
  roleName: string

  /** 角色权限字符串 */
  roleKey: string

  /** 显示顺序 */
  roleSort: number

  /** 角色状态 */
  status: string

  /** 菜单树选择项是否关联显示 */
  menuCheckStrictly: boolean

  /** 部门树选择项是否关联显示 */
  deptCheckStrictly: boolean

  /** 备注 */
  remark: string

  /** 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限） */
  dataScope?: string

  /**菜单id数组*/
  menuIds: Array<string | number>

  /**部门id数组*/
  deptIds: Array<string | number>
}

/** 角色信息视图类型 */
export interface SysRoleVo {
  /** 角色ID */
  roleId: string | number

  /** 角色名称 */
  roleName: string

  /** 角色权限字符串 */
  roleKey: string

  /** 显示顺序 */
  roleSort: number

  /** 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限） */
  dataScope: string

  /** 菜单树选择项是否关联显示 */
  menuCheckStrictly: boolean

  /** 部门树选择项是否关联显示 */
  deptCheckStrictly: boolean

  /** 角色状态 */
  status: string

  /** 备注 */
  remark: any

  /**标记*/
  flag: boolean

  /**菜单id数组*/
  menuIds: Array<string | number>

  /**部门id数组*/
  deptIds: Array<string | number>

  /**是否管理员*/
  admin: boolean
}

/** 菜单树形结构类型 */
export interface SysDeptTreeOption {
  /** id */
  id: string

  /** 标签 */
  label: string

  /** 父id */
  parentId: string

  /** 权重 */
  weight: number

  /** 子节点 */
  children?: SysDeptTreeOption[]
}

export interface SysRoleDeptTree {
  /**选中的id列表*/
  checkedKeys: string[]

  /**部门树选项*/
  depts: SysDeptTreeOption[]
}

/**
 * 角色邀请信息
 */
export interface RoleInviteVo {
  inviteCode: string
  tenantId: string
  userId: number
  userName: string
  roleId: number
  roleName: string
  deptId: number
  deptName: string
  maxUseCount: number
  currentUseCount: number
  validUntil: number
  createTime: number
  remark?: string
  needApproval: boolean
  inviteStatus: string
  remainingCount: number
  isValid: boolean
  inviteUrlQrcode: string
}

/**
 * 创建角色邀请请求
 */
export interface CreateRoleInviteBo {
  roleId: number | string
  deptId: number
  validHours: number
  maxUseCount: number
  remark?: string
  needApproval: boolean
}

/**
 * 角色邀请查询请求
 */
export interface RoleInviteQueryBo {
  roleId?: number | string
  status?: string
}
