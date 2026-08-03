/** 租户查询类型 */
export interface SysTenantQuery extends PageQuery {
  /**租户id*/
  tenantId?: string | number

  /** 联系人 */
  contactUserName?: string

  /** 联系电话 */
  contactPhone?: string

  /** 企业名称 */
  companyName?: string

  /** 租户状态 */
  status?: string
}

/** 租户表单类型 */
export interface SysTenantBo {
  /** id */
  id?: string | number

  /**租户id*/
  tenantId: string | number

  /**用户名称*/
  userName: string

  /**用户密码*/
  password: string

  /** 联系人 */
  contactUserName: string

  /** 联系电话 */
  contactPhone: string

  /** 企业名称 */
  companyName: string

  /** 统一社会信用代码 */
  licenseNumber: string

  /** 域名 */
  domain: string

  /** 地址 */
  address: string

  /** 企业简介 */
  intro: string

  /** 备注 */
  remark: string

  /** 租户套餐编号 */
  packageId: string | number

  /** 过期时间 */
  expireTime: string

  /** 用户数量（-1不限制） */
  accountCount: number

  /** 租户状态 */
  status: string
}

/** 租户视图类型 */
export interface SysTenantVo {
  /** id */
  id: number | string

  /** 租户id */
  tenantId: number | string

  /**用户名称*/
  userName: string

  /** 联系人 */
  contactUserName: string

  /** 联系电话 */
  contactPhone: string

  /** 企业名称 */
  companyName: string

  /** 统一社会信用代码 */
  licenseNumber: string

  /** 地址 */
  address: string

  /** 域名 */
  domain: string

  /** 企业简介 */
  intro: string

  /** 备注 */
  remark: string

  /** 租户套餐编号 */
  packageId: string | number

  /** 过期时间 */
  expireTime: string

  /** 用户数量（-1不限制） */
  accountCount: number

  /** 租户状态 */
  status: string
}
