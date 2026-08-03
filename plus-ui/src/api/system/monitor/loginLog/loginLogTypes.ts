/** 登录日志查询类型 */
export interface SysLoginLogQuery extends PageQuery {
  /** 访问ID */
  infoId?: string | number

  /** 用户账号 */
  userName?: string

  /** 用户id */
  userId?: string | number

  /** 登录IP地址 */
  ipaddr?: string

  /** 登录状态 */
  status?: string
}

/** 登录日志视图类型 */
export interface SysLoginLogVo {
  /** 访问ID */
  infoId: string | number

  /** 用户账号 */
  userName: string

  /** 登录IP地址 */
  ipaddr: string

  /** 登录地点 */
  loginLocation: string

  /** 浏览器类型 */
  browser: string

  /** 操作系统 */
  os: string

  /** 登录状态 */
  status: string

  /** 提示消息 */
  msg: string

  /** 访问时间 */
  loginTime: string
}
