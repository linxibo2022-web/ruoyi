/** 在线用户查询参数 */
export interface SysUserOnlineQuery extends PageQuery {
  /** 登录IP地址 */
  ipaddr?: string
  /** 用户名称 */
  userName?: string
}

/** 当前在线会话 */
export interface SysUserOnlineVo {
  /** 会话编号 */
  tokenId: string
  /** 部门名称 */
  deptName: string
  /** 用户名称 */
  userName: string
  /** 客户端 */
  clientKey: string
  /** 设备类型 */
  deviceType: string
  /** 登录IP地址 */
  ipaddr: string
  /** 登录地址 */
  loginLocation: string
  /** 浏览器类型 */
  browser: string
  /** 操作系统 */
  os: string
  /** 登录时间 */
  loginTime: number
}
