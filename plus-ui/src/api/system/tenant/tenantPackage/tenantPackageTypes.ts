/** 租户套餐查询类型 */
export interface SysTenantPackageQuery extends PageQuery {
  /** 套餐名称 */
  packageName?: string

  /** 状态 */
  status?: string
}

/** 租户套餐表单类型 */
export interface SysTenantPackageBo {
  /** 租户套餐id */
  packageId?: string | number

  /** 套餐名称 */
  packageName: string

  /** 关联菜单id */
  menuIds: string

  /** 备注 */
  remark?: string

  /** 菜单树选择项是否关联显示 */
  menuCheckStrictly: boolean
}

/** 租户套餐视图类型 */
export interface SysTenantPackageVo {
  /** 租户套餐id */
  packageId: string | number

  /** 套餐名称 */
  packageName: string

  /** 关联菜单id */
  menuIds: string | number

  /** 备注 */
  remark: string

  /** 菜单树选择项是否关联显示 */
  menuCheckStrictly: boolean

  /** 状态 */
  status: string
}
