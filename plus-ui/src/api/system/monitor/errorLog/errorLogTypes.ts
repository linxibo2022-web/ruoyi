/** 错误日志查询类型 */
export interface ErrorLogQuery extends PageQuery {
  /** 主键ID */
  id?: string | number
  /** 严重级别(ERROR/WARN/FATAL) */
  errorLevel?: string
  /** 异常类名 */
  errorType?: string
  /** 业务错误码 */
  errorCode?: string
  /** 错误消息 */
  errorMessage?: string
  /** 链路追踪ID */
  traceId?: string
  /** 用户ID */
  userId?: string | number
  /** 用户名 */
  userName?: string
  /** 平台类型(PC/WECHAT/ANDROID/IOS/H5) */
  clientType?: string
  /** 模块名称 */
  moduleName?: string
  /** 处理状态(0待处理 1已处理 2已忽略) */
  handleStatus?: string
  /** 模糊搜索关键词 */
  searchValue?: string
}

/** 错误日志表单类型（用于更新处理状态） */
export interface ErrorLogBo {
  /** 主键ID */
  id?: string | number
  /** 主键ID集合 */
  ids?: Array<string | number>
  /** 处理状态(0待处理 1已处理 2已忽略) */
  handleStatus?: string
  /** 处理人ID */
  handleBy?: string | number
  /** 处理备注 */
  handleRemark?: string
}

/** 错误日志视图类型 */
export interface ErrorLogVo {
  /** 主键ID */
  id: string | number
  /** 租户ID */
  tenantId: string
  /** 严重级别(ERROR/WARN/FATAL) */
  errorLevel: string
  /** 异常类名 */
  errorType: string
  /** 业务错误码 */
  errorCode?: string
  /** 错误消息 */
  errorMessage: string
  /** 异常堆栈 */
  errorStack?: string
  /** 链路追踪ID */
  traceId?: string
  /** 请求URI */
  requestUri?: string
  /** 请求路径模板 */
  requestPattern?: string
  /** 请求方法(GET/POST/PUT/DELETE) */
  requestMethod?: string
  /** 请求IP */
  requestIp?: string
  /** 请求参数 */
  requestParams?: string
  /** 用户代理 */
  userAgent?: string
  /** 用户ID */
  userId?: string | number
  /** 用户名 */
  userName?: string
  /** 部门ID */
  deptId?: string | number
  /** 模块名称 */
  moduleName?: string
  /** 业务类型 */
  businessType?: string
  /** 业务关键字 */
  businessKey?: string
  /** 平台类型(PC/WECHAT/ANDROID/IOS/H5) */
  clientType?: string
  /** 客户端版本 */
  clientVersion?: string
  /** 服务器名称 */
  serverName?: string
  /** 服务器IP */
  serverIp?: string
  /** 应用版本 */
  appVersion?: string
  /** SQL语句 */
  sqlStatement?: string
  /** SQL参数 */
  sqlParams?: string
  /** SQL耗时(毫秒) */
  sqlDuration?: number
  /** 发生次数 */
  occurrenceCount: number
  /** 首次发生时间 */
  firstTime: string
  /** 最后发生时间 */
  lastTime: string
  /** 处理状态(0待处理 1已处理 2已忽略) */
  handleStatus: string
  /** 处理人ID */
  handleBy?: string | number
  /** 处理时间 */
  handleTime?: string
  /** 处理备注 */
  handleRemark?: string
  /** 创建时间 */
  createTime: string
  /** 备注 */
  remark?: string
}
