// 支付配置类型
export interface PaymentQuery extends PageQuery {
  /** 支付配置id */
  id?: string | number

  /** 商户类型 */
  type?: string

  /** 商户名称 */
  mchName?: string

  /** 商户号 */
  mchId?: string | number

  /** 商户密钥 */
  mchKey?: string

  /** APIv3密钥 */
  apiV3Key?: string

  /** 证书路径 */
  certPath?: string

  /** 密钥路径 */
  keyPath?: string

  /** 平台证书路径 */
  platformCertPath?: string

  /** 证书序列号 */
  certSerialNo?: string

  /** 微信支付公钥ID(V3公钥模式) */
  publicKeyId?: string

  /** p12证书路径(V2退款) */
  p12CertPath?: string

  /** dev证书路径 */
  devCertPath?: string

  /** dev密钥路径 */
  devKeyPath?: string

  /** dev平台证书路径 */
  devPlatformCertPath?: string

  /** 状态 */
  status?: string

  /** 创建时间 */
  createTime?: string
}

/** 支付配置表单类型 */
export interface PaymentBo {
  /** 支付配置id */
  id?: string | number

  /** 商户类型 */
  type?: string

  /** 商户名称 */
  mchName?: string

  /** 商户号 */
  mchId?: string | number

  /** 商户密钥 */
  mchKey?: string

  /** APIv3密钥 */
  apiV3Key?: string

  /** 证书路径 */
  certPath?: string

  /** 密钥路径 */
  keyPath?: string

  /** 平台证书路径 */
  platformCertPath?: string

  /** 证书序列号 */
  certSerialNo?: string

  /** 微信支付公钥ID(V3公钥模式) */
  publicKeyId?: string

  /** p12证书路径(V2退款) */
  p12CertPath?: string

  /** dev证书路径 */
  devCertPath?: string

  /** dev密钥路径 */
  devKeyPath?: string

  /** dev平台证书路径 */
  devPlatformCertPath?: string

  /** 状态 */
  status?: string

  /** 备注 */
  remark?: string
}

/** 支付配置视图类型 */
export interface PaymentVo {
  /** 支付配置id */
  id: string | number

  /** 商户类型 */
  type?: string

  /** 商户名称 */
  mchName: string

  /** 商户号 */
  mchId: string | number

  /** 商户密钥 */
  mchKey: string

  /** APIv3密钥 */
  apiV3Key: string

  /** 证书路径 */
  certPath: string

  /** 密钥路径 */
  keyPath: string

  /** 平台证书路径 */
  platformCertPath: string

  /** 证书序列号 */
  certSerialNo: string

  /** 微信支付公钥ID(V3公钥模式) */
  publicKeyId: string

  /** p12证书路径(V2退款) */
  p12CertPath: string

  /** dev证书路径 */
  devCertPath: string

  /** dev密钥路径 */
  devKeyPath: string

  /** dev平台证书路径 */
  devPlatformCertPath: string

  /** 状态 */
  status: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}
