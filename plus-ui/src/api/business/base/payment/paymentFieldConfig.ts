/**
 * 支付方式字段配置元数据
 *
 * 用于动态渲染支付配置表单，根据不同的支付方式和模式显示不同的字段
 */

/** 字段配置接口 */
export interface PaymentFieldConfig {
  key: string // 字段key（对应数据库字段）
  label: string // 字段标签
  placeholder?: string // 占位符
  tooltip?: string // 提示信息
  required: boolean // 是否必填
  type: 'input' | 'textarea' | 'password' // 输入类型
  maxlength?: number // 最大长度
  span?: number | 'auto' // 栅格占位（24 或 'auto'）
  showFileButton?: boolean // 是否显示文件选择按钮
}

/** 支付模式配置接口 */
export interface PaymentModeConfig {
  mode: string // 模式标识（如 'public_key', 'cert'）
  modeLabel: string // 模式名称（如 '公钥模式', '证书模式'）
  fields: PaymentFieldConfig[] // 该模式下的字段配置
}

/** 支付方式配置接口 */
export interface PaymentMethodConfig {
  method: string // 支付方式值（如 'alipay', 'wechat'）
  label: string // 支付方式名称
  supportModes: boolean // 是否支持多模式切换
  defaultMode?: string // 默认模式（多模式时使用）
  modes?: PaymentModeConfig[] // 模式配置（多模式时使用）
  fields?: PaymentFieldConfig[] // 字段配置（单模式时使用）
}

/**
 * 支付方式字段配置
 *
 * 配置说明：
 * - supportModes=false: 单模式，使用 fields 配置
 * - supportModes=true: 多模式，使用 modes 配置，每个模式有自己的 fields
 */
export const PAYMENT_METHOD_CONFIGS: Record<string, PaymentMethodConfig> = {
  /**
   * 微信支付
   * 多模式配置：v2 / v3 / v2_v3（兼容）
   */
  wechat: {
    method: 'wechat',
    label: '微信支付',
    supportModes: true,
    defaultMode: 'v2_v3',
    modes: [
      {
        mode: 'v2',
        modeLabel: 'v2',
        fields: [
          {
            key: 'mchName',
            label: '商户名称',
            required: false,
            type: 'input',
            maxlength: 30,
            span: 'auto',
            tooltip: '商户在微信支付平台注册的显示名称，便于识别和管理'
          },
          {
            key: 'mchId',
            label: '商户号',
            required: false,
            type: 'input',
            maxlength: 20,
            span: 'auto',
            tooltip: '微信支付分配的商户号（mch_id）'
          },
          {
            key: 'mchKey',
            label: '商户密钥',
            required: false,
            type: 'password',
            maxlength: 50,
            span: 'auto',
            tooltip: 'APIv2密钥（32位字符串），用于API签名验证。获取路径：微信商户平台 → 账户中心 → API安全'
          },
          {
            key: 'p12CertPath',
            label: 'p12证书路径',
            required: false,
            type: 'input',
            maxlength: 500,
            span: 24,
            tooltip: '微信支付V2退款专用p12证书文件路径。仅支持文件路径,不支持直接粘贴内容。下载路径：微信商户平台 → 账户中心 → API安全 → API证书'
          },
          {
            key: 'remark',
            label: '备注',
            required: false,
            type: 'textarea',
            maxlength: 255,
            span: 'auto',
            tooltip: '记录该支付配置的用途、注意事项或其他说明信息'
          }
        ]
      },
      {
        mode: 'v3',
        modeLabel: 'v3',
        fields: [
          {
            key: 'mchName',
            label: '商户名称',
            required: false,
            type: 'input',
            maxlength: 30,
            span: 'auto',
            tooltip: '商户在微信支付平台注册的显示名称'
          },
          {
            key: 'mchId',
            label: '商户号',
            required: false,
            type: 'input',
            maxlength: 20,
            span: 'auto',
            tooltip: '微信支付分配的商户号（mch_id）'
          },
          {
            key: 'apiV3Key',
            label: 'APIv3密钥',
            required: false,
            type: 'password',
            maxlength: 50,
            span: 'auto',
            tooltip: '微信支付APIv3专用密钥（32位字符串），用于请求签名和回调验证。设置路径：微信商户平台 → 账户中心 → API安全'
          },
          {
            key: 'certSerialNo',
            label: '证书序列号',
            required: false,
            type: 'password',
            maxlength: 100,
            span: 'auto',
            tooltip: 'API证书的唯一标识号，用于身份验证。获取路径：微信商户平台 → 账户中心 → API安全 → API证书 → 管理证书'
          },
          {
            key: 'publicKeyId',
            label: '公钥ID(公钥模式)',
            required: false,
            type: 'input',
            maxlength: 100,
            span: 'auto',
            tooltip: '仅"微信支付公钥模式"需要填写。格式为 PUB_KEY_ID_xxx，获取路径：微信商户平台 → 账户中心 → API安全 → 微信支付公钥。注意：与"证书序列号"不同，公钥模式下验签依赖此ID，留空则回退使用证书序列号'
          },
          {
            key: 'certPath',
            label: '证书路径',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: 'API证书文件路径或PEM内容。支持文件路径（如：/path/to/apiclient_cert.pem）或直接粘贴PEM格式的证书内容'
          },
          {
            key: 'keyPath',
            label: '密钥路径',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: '私钥文件路径或PEM内容。支持文件路径（如：/path/to/apiclient_key.pem）或直接粘贴PEM格式的私钥内容'
          },
          {
            key: 'platformCertPath',
            label: '平台证书/公钥',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: '微信支付平台证书或公钥文件路径/内容。平台证书模式填证书，公钥模式填以"-----BEGIN PUBLIC KEY-----"开头的微信支付公钥内容。用于验证微信支付回调的真实性'
          },
          {
            key: 'remark',
            label: '备注',
            required: false,
            type: 'textarea',
            maxlength: 255,
            span: 'auto',
            tooltip: '记录该支付配置的用途、注意事项或其他说明信息'
          }
        ]
      },
      {
        mode: 'v2_v3',
        modeLabel: 'v2/v3',
        fields: [
          {
            key: 'mchName',
            label: '商户名称',
            required: false,
            type: 'input',
            maxlength: 30,
            span: 'auto',
            tooltip: '商户在微信支付平台注册的显示名称'
          },
          {
            key: 'mchId',
            label: '商户号',
            required: false,
            type: 'input',
            maxlength: 20,
            span: 'auto',
            tooltip: '微信支付分配的商户号（mch_id）'
          },
          {
            key: 'mchKey',
            label: '商户密钥(v2)',
            required: false,
            type: 'password',
            maxlength: 50,
            span: 'auto',
            tooltip: 'APIv2密钥（32位字符串），用于API签名验证'
          },
          {
            key: 'apiV3Key',
            label: 'APIv3密钥',
            required: false,
            type: 'password',
            maxlength: 50,
            span: 'auto',
            tooltip: '微信支付APIv3专用密钥（32位字符串）'
          },
          {
            key: 'certSerialNo',
            label: '证书序列号(v3)',
            required: false,
            type: 'password',
            maxlength: 100,
            span: 'auto',
            tooltip: 'API证书的唯一标识号，用于v3身份验证'
          },
          {
            key: 'publicKeyId',
            label: '公钥ID(v3公钥模式)',
            required: false,
            type: 'input',
            maxlength: 100,
            span: 'auto',
            tooltip: '仅"微信支付公钥模式"需要填写。格式为 PUB_KEY_ID_xxx，获取路径：微信商户平台 → 账户中心 → API安全 → 微信支付公钥。与"证书序列号"不同，留空则回退使用证书序列号'
          },
          {
            key: 'certPath',
            label: '证书路径(v3)',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: 'v3 API证书文件路径或PEM内容'
          },
          {
            key: 'keyPath',
            label: '密钥路径(v3)',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: 'v3私钥文件路径或PEM内容'
          },
          {
            key: 'platformCertPath',
            label: '平台证书/公钥(v3)',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: 'v3微信支付平台证书或公钥文件路径/内容。公钥模式下填以"-----BEGIN PUBLIC KEY-----"开头的微信支付公钥内容'
          },
          {
            key: 'p12CertPath',
            label: 'p12证书路径(v2)',
            required: false,
            type: 'input',
            maxlength: 500,
            span: 24,
            tooltip: 'v2退款专用p12证书文件路径'
          },
          {
            key: 'remark',
            label: '备注',
            required: false,
            type: 'textarea',
            maxlength: 255,
            span: 'auto',
            tooltip: '记录该支付配置的用途、注意事项或其他说明信息'
          }
        ]
      }
    ]
  },

  /**
   * 支付宝
   * 多模式配置：公钥模式 / 证书模式
   */
  alipay: {
    method: 'alipay',
    label: '支付宝',
    supportModes: true,
    defaultMode: 'public_key',
    modes: [
      {
        mode: 'public_key',
        modeLabel: '公钥模式',
        fields: [
          {
            key: 'mchName',
            label: '商户名称',
            required: false,
            type: 'input',
            maxlength: 30,
            span: 'auto',
            tooltip: '商户在支付宝开放平台注册的应用名称'
          },
          {
            key: 'mchId',
            label: '应用ID(APPID)',
            required: false,
            type: 'input',
            maxlength: 20,
            span: 'auto',
            tooltip: '支付宝开放平台分配的应用APPID'
          },
          {
            key: 'keyPath',
            label: '应用私钥',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: '应用私钥内容或文件路径，用于签名。支持PKCS#1和PKCS#8格式，系统会自动转换'
          },
          {
            key: 'platformCertPath',
            label: '支付宝公钥',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: '支付宝公钥内容或文件路径，用于验签。获取路径：支付宝开放平台 → 应用详情 → 接口加签方式'
          },
          {
            key: 'remark',
            label: '备注',
            required: false,
            type: 'textarea',
            maxlength: 255,
            span: 'auto',
            tooltip: '记录该支付配置的用途、注意事项或其他说明信息'
          }
        ]
      },
      {
        mode: 'cert',
        modeLabel: '证书模式',
        fields: [
          {
            key: 'mchName',
            label: '商户名称',
            required: false,
            type: 'input',
            maxlength: 30,
            span: 'auto',
            tooltip: '商户在支付宝开放平台注册的应用名称'
          },
          {
            key: 'mchId',
            label: '应用ID(APPID)',
            required: false,
            type: 'input',
            maxlength: 20,
            span: 'auto',
            tooltip: '支付宝开放平台分配的应用APPID'
          },
          {
            key: 'keyPath',
            label: '应用私钥',
            required: false,
            type: 'input',
            maxlength: 2000,
            span: 24,
            showFileButton: true,
            tooltip: '应用私钥内容或文件路径，用于签名'
          },
          {
            key: 'certPath',
            label: '应用公钥证书路径',
            required: false,
            type: 'input',
            maxlength: 500,
            span: 24,
            showFileButton: true,
            tooltip: '应用公钥证书文件路径（appCertPublicKey.crt）'
          },
          {
            key: 'apiV3Key',
            label: '支付宝公钥证书路径',
            required: false,
            type: 'input',
            maxlength: 500,
            span: 24,
            showFileButton: true,
            tooltip: '支付宝公钥证书文件路径（alipayCertPublicKey_RSA2.crt）'
          },
          {
            key: 'platformCertPath',
            label: '支付宝根证书路径',
            required: false,
            type: 'input',
            maxlength: 500,
            span: 24,
            showFileButton: true,
            tooltip: '支付宝根证书文件路径（alipayRootCert.crt）'
          },
          {
            key: 'remark',
            label: '备注',
            required: false,
            type: 'textarea',
            maxlength: 255,
            span: 'auto',
            tooltip: '记录该支付配置的用途、注意事项或其他说明信息'
          }
        ]
      }
    ]
  },

  /**
   * 银联支付
   * 预留配置（暂未实现）
   */
  unionpay: {
    method: 'unionpay',
    label: '银联支付',
    supportModes: false,
    fields: [
      {
        key: 'mchName',
        label: '商户名称',
        required: false,
        type: 'input',
        maxlength: 30,
        span: 'auto',
        tooltip: '商户在银联平台注册的名称'
      },
      {
        key: 'mchId',
        label: '商户号',
        required: false,
        type: 'input',
        maxlength: 20,
        span: 'auto',
        tooltip: '银联分配的商户号'
      },
      {
        key: 'keyPath',
        label: '签名私钥路径',
        required: false,
        type: 'input',
        maxlength: 500,
        span: 24,
        showFileButton: true,
        tooltip: '签名私钥文件路径'
      },
      {
        key: 'certPath',
        label: '签名证书路径',
        required: false,
        type: 'input',
        maxlength: 500,
        span: 24,
        showFileButton: true,
        tooltip: '签名证书文件路径'
      },
      {
        key: 'platformCertPath',
        label: '验签证书路径',
        required: false,
        type: 'input',
        maxlength: 500,
        span: 24,
        showFileButton: true,
        tooltip: '验签证书文件路径'
      },
      {
        key: 'remark',
        label: '备注',
        required: false,
        type: 'textarea',
        maxlength: 255,
        span: 'auto',
        tooltip: '记录该支付配置的用途、注意事项或其他说明信息'
      }
    ]
  },

  /**
   * 余额支付
   * 无需第三方配置
   */
  balance: {
    method: 'balance',
    label: '余额支付',
    supportModes: false,
    fields: [
      {
        key: 'mchName',
        label: '配置名称',
        required: false,
        type: 'input',
        maxlength: 30,
        span: 'auto',
        tooltip: '余额支付配置的名称，便于识别'
      },
      {
        key: 'remark',
        label: '备注',
        required: false,
        type: 'textarea',
        maxlength: 255,
        span: 'auto',
        tooltip: '余额支付说明信息'
      }
    ]
  },

  /**
   * 积分抵扣
   * 无需第三方配置
   */
  points: {
    method: 'points',
    label: '积分抵扣',
    supportModes: false,
    fields: [
      {
        key: 'mchName',
        label: '配置名称',
        required: false,
        type: 'input',
        maxlength: 30,
        span: 'auto',
        tooltip: '积分抵扣配置的名称，便于识别'
      },
      {
        key: 'remark',
        label: '备注',
        required: false,
        type: 'textarea',
        maxlength: 255,
        span: 'auto',
        tooltip: '积分抵扣说明信息'
      }
    ]
  }
}

/**
 * 获取支付方式配置
 * @param method 支付方式
 * @returns 支付方式配置对象
 */
export function getPaymentMethodConfig(method: string): PaymentMethodConfig | undefined {
  return PAYMENT_METHOD_CONFIGS[method]
}

/**
 * 获取当前需要显示的字段列表
 * @param method 支付方式
 * @param mode 模式（多模式时需要）
 * @returns 字段配置列表
 */
export function getCurrentFields(method: string, mode?: string): PaymentFieldConfig[] {
  const config = getPaymentMethodConfig(method)
  if (!config) return []

  // 单模式：直接返回 fields
  if (!config.supportModes) {
    return config.fields || []
  }

  // 多模式：返回当前模式的 fields
  const modeConfig = config.modes?.find((m) => m.mode === mode)
  return modeConfig?.fields || []
}
