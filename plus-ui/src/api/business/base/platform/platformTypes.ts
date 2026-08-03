// 平台配置类型
export interface PlatformQuery extends PageQuery {
  /** 平台配置id */
  id?: string | number

  /** 平台类型 */
  type?: string

  /** 名称 */
  name?: string

  /** appid */
  appid?: string | number

  /** 密钥 */
  secret?: string

  /** 接口token */
  token?: string

  /** 加密密钥 */
  aeskey?: string

  /** 关联支付配置 */
  paymentIds?: string

  /** 状态 */
  status?: string

  /** 创建时间 */
  createTime?: string
}

export interface PlatformBo {
  /** 平台配置id */
  id?: string | number

  /** 平台类型 */
  type?: string

  /** 名称 */
  name?: string

  /** appid */
  appid?: string | number

  /** 密钥 */
  secret?: string

  /** 接口token */
  token?: string

  /** 加密密钥 */
  aeskey?: string

  /** 关联支付配置 */
  paymentIds?: string

  /** 模板配置 */
  templateConfigs?: string

  /** 状态 */
  status?: string

  /** 备注 */
  remark?: string
}

export interface PlatformVo {
  /** 平台配置id */
  id: string | number

  /** 平台类型 */
  type: string

  /** 名称 */
  name: string

  /** appid */
  appid: string | number

  /** 密钥 */
  secret: string

  /** 接口token */
  token: string

  /** 加密密钥 */
  aeskey: string

  /** 关联支付配置 */
  paymentIds: string

  /** 模板配置 */
  templateConfigs?: string

  /** 状态 */
  status: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}

/**
 * 订阅配置接口
 * 对应后端 TemplateConfig 实体类
 */
export interface TemplateConfig {
  /** 虚拟主键ID，用于区分不同配置 */
  id?: string | number
  /** 模板ID */
  templateId?: string
  /** 模板标题 */
  title?: string
  /** 模板内容/描述 */
  content?: string
  /** 字段列表，逗号隔开 */
  fields?: string
  /** 状态：'1'启用，'0'禁用 */
  status?: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
  /** 备注 */
  remark?: string
  /** 是否仅更新订阅配置，为 true 时不触发平台配置刷新 */
  onlyTemplateUpdate?: boolean
}
