/** 对象存储配置查询类型 */
export interface SysOssConfigQuery extends PageQuery {
  /** 配置key */
  configKey?: string

  /** 桶名称 */
  bucketName?: string

  /** 启用状态 */
  status?: string
}

/** 对象存储配置表单类型 */
export interface SysOssConfigBo {
  /** 主键 */
  ossConfigId?: string | number

  /** 配置key */
  configKey: string

  /** accessKey */
  accessKey: string

  /** 秘钥 */
  secretKey: string

  /** 桶名称 */
  bucketName: string

  /** 前缀 */
  prefix: string

  /** 访问站点 */
  endpoint: string

  /** 自定义域名 */
  domain: string

  /** 是否https（1=是,0=否） */
  isHttps: string

  /** 域 */
  region: string

  /** 桶权限类型(0=private 1=public 2=custom) */
  accessPolicy: string

  /** 启用状态 */
  status: string

  /** 备注 */
  remark: string
}

/** 对象存储配置视图类型 */
export interface SysOssConfigVo {
  /** 主键 */
  ossConfigId: string | number

  /** 配置key */
  configKey: string

  /** accessKey */
  accessKey: string

  /** 秘钥 */
  secretKey: string

  /** 桶名称 */
  bucketName: string

  /** 前缀 */
  prefix: string

  /** 访问站点 */
  endpoint: string

  /** 自定义域名 */
  domain: string

  /** 是否https（1=是,0=否） */
  isHttps: string

  /** 域 */
  region: string

  /** 桶权限类型(0=private 1=public 2=custom) */
  accessPolicy: string

  /** 启用状态 */
  status: string

  /** 扩展字段 */
  ext1: string

  /** 备注 */
  remark: string
}
