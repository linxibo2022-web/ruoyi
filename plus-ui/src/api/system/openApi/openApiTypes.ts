/** API密钥查询类型 */
export interface SysApiKeyQuery extends PageQuery {
  /** API密钥ID */
  id?: string | number

  /** 应用名称 */
  appName?: string

  /** AppKey */
  appKey?: string

  /** 关联用户ID */
  userId?: string | number

  /** 状态 */
  status?: string

  /** 过期时间 */
  expireTime?: string

  /** 创建时间 */
  createTime?: string
}

/** API密钥表单类型 */
export interface SysApiKeyBo {
  /** API密钥ID */
  id?: string | number

  /** 应用名称 */
  appName?: string

  /** AppKey */
  appKey?: string

  /** AppSecret */
  appSecret?: string

  /** 关联用户ID */
  userId?: string | number

  /** 过期时间 */
  expireTime?: string

  /** 状态 */
  status?: string

  /** IP白名单,逗号分隔 */
  whiteIps?: string

  /** 备注 */
  remark?: string
}

/** API密钥视图类型 */
export interface SysApiKeyVo {
  /** API密钥ID */
  id: string | number

  /** 应用名称 */
  appName: string

  /** AppKey */
  appKey: string

  /** 关联用户ID */
  userId: string | number

  /** 关联用户名称 */
  userName: string

  /** 过期时间 */
  expireTime: string

  /** 状态 */
  status: string

  /** IP白名单,逗号分隔 */
  whiteIps: string

  /** 调用次数 */
  callCount: number

  /** 最后调用时间 */
  lastCallTime: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}

/** API密钥详情视图类型(包含明文Secret) */
export interface OpenApiSecretVo {
  /** API密钥ID */
  id: string | number

  /** 应用名称 */
  appName: string

  /** AppKey */
  appKey: string

  /** AppSecret(明文) */
  appSecret: string

  /** 提示信息 */
  tips: string
}

/** 开放接口信息视图类型 */
export interface OpenApiInfoVo {
  /** 接口路径 */
  path: string

  /** 请求方法 (GET/POST/PUT/DELETE) */
  method: string

  /** 接口说明 */
  description: string

  /** 所属模块 */
  module: string

  /** Controller类名 */
  className: string

  /** 方法名 */
  methodName: string

  /** 权限要求 */
  permission?: string

  /** 权限模式 (AND/OR) */
  permissionMode?: string

  /** 角色要求 */
  roleCode?: string

  /** 角色模式 (AND/OR) */
  roleMode?: string

  /** 是否无权限限制 */
  noAuth: boolean

  /** 请求参数列表 */
  parameters: ParameterInfo[]

  /** 响应类型 */
  responseType: string

  /** 响应信息(包含泛型和字段详情) */
  responseInfo?: ResponseInfo
}

/** 参数信息类型 */
export interface ParameterInfo {
  /** 参数名 */
  name: string

  /** 参数类型 */
  type: string

  /** 是否必填 */
  required: boolean

  /** 参数说明 */
  description: string

  /** 参数位置 (PATH/QUERY/BODY/FORM/HEADER) */
  location: string

  /** 子字段列表(用于复杂对象类型) */
  fields?: FieldInfo[]
}

/** 参数位置常量 */
export const ParameterLocation = {
  /** 路径参数 */
  PATH: 'PATH',
  /** 查询参数 */
  QUERY: 'QUERY',
  /** 请求体 */
  BODY: 'BODY',
  /** 表单参数 */
  FORM: 'FORM',
  /** 请求头 */
  HEADER: 'HEADER',
  /** 默认(未指定) */
  UNDEFINED: 'UNDEFINED'
} as const

/** 响应信息类型 */
export interface ResponseInfo {
  /** 响应完整类型(包含泛型) 例如: R, List, PageResult */
  fullType: string

  /** 响应体的实际类型(泛型参数) 例如: PageResult<AdVo>, List<AdVo>, AdVo */
  dataType: string

  /** 响应体字段信息(仅当dataType是对象类型时有值) */
  fields?: FieldInfo[]

  /** 响应示例JSON */
  example?: string
}

/** 字段信息类型 */
export interface FieldInfo {
  /** 字段名 */
  name: string

  /** 字段类型 */
  type: string

  /** 是否必填 */
  required: boolean

  /** 字段说明 */
  description: string

  /** 示例值 */
  example: string
}
