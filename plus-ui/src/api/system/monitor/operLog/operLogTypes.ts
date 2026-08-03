/** 操作日志记录查询类型 */
export interface SysOperLogQuery extends PageQuery {
  /** 日志主键 */
  operId?: string | number

  /** 操作ip */
  operIp?: string | number

  /** 模块标题 */
  title?: string

  /** 操作人员 */
  operName?: string

  /** 操作类型 */
  operType?: string

  /** 操作状态 */
  status?: string
}

/** 操作日志记录表单类型 */
export interface SysOperLogBo {
  /** 日志主键 */
  operId?: string | number

  /** 模块标题 */
  title?: string

  /** 操作类型 */
  operType?: string

  /** 方法名称 */
  method?: string

  /** 请求方式 */
  requestMethod?: string

  /** 操作操作人员类别 */
  operatorType?: string

  /** 操作人员 */
  operName?: string

  /** 部门名称 */
  deptName?: string

  /** 请求URL */
  operUrl?: string

  /** 主机地址 */
  operIp?: string

  /** 操作地点 */
  operLocation?: string

  /** 请求参数 */
  operParam?: string

  /** 返回参数 */
  jsonResult?: string

  /** 操作状态 */
  status?: string

  /** 错误消息 */
  errorMsg?: string

  /** 操作时间 */
  operTime?: string

  /** 消耗时间 */
  costTime?: number
}

/** 操作日志记录视图类型 */
export interface SysOperLogVo {
  /** 日志主键 */
  operId: string | number

  /** 模块标题 */
  title: string

  /** 操作类型 */
  operType: string

  /** 方法名称 */
  method: string

  /** 请求方式 */
  requestMethod: string

  /** 操作操作人员类别 */
  operatorType: string

  /** 操作人员 */
  operName: string

  /** 部门名称 */
  deptName: string

  /** 请求URL */
  operUrl: string

  /** 主机地址 */
  operIp: string

  /** 操作地点 */
  operLocation: string

  /** 请求参数 */
  operParam: string

  /** 返回参数 */
  jsonResult: string

  /** 操作状态 */
  status: string

  /** 错误消息 */
  errorMsg: string

  /** 操作时间 */
  operTime: string

  /** 消耗时间 */
  costTime: number
}
