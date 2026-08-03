/**
 * 验证码信息
 */
export interface CaptchaVo {
  /** 是否开启多租户 */
  tenantEnabled: boolean
  /** 租户id */
  tenantId: string
  /** 租户标题 */
  tenantTitle: string
  /** 是否开启注册 */
  registerEnabled: boolean
  /** 是否开启验证码 */
  captchaEnabled: boolean
  /** 验证码唯一标识 */
  uuid: string
  /** 验证码图片（Base64编码） */
  img: string
  /** 已配置的社交登录类型（逗号分隔） */
  socialTypes?: string
  /** 是否开启社交登录自动注册 */
  socialAutoRegisterEnabled?: boolean
}

/**
 * 基础登录对象
 */
export interface LoginBody {
  /** 认证方式 */
  authType: 'password' | 'email' | 'sms' | 'social'
  /** 验证码 */
  code?: string
  /** 唯一标识 */
  uuid?: string
}

/**
 * 密码登录对象
 */
export interface PasswordLoginBody extends LoginBody {
  /** 租户id 不会传到后端 在请求头进行控制 */
  tenantId?: string
  /** 认证类型 */
  authType: 'password'
  /** 用户名 */
  userName: string
  /** 用户密码 */
  password: string
  /** 记住密码（仅前端使用，不提交后端） */
  rememberMe?: boolean
}

/**
 * 邮箱登录对象
 */
export interface EmailLoginBody extends LoginBody {
  /** 认证类型 */
  authType: 'email'
  /** 邮箱 */
  email: string
  /** 邮箱验证码 */
  emailCode: string
}

/**
 * 短信登录对象
 */
export interface SmsLoginBody extends LoginBody {
  /** 认证类型 */
  authType: 'sms'
  /** 手机号 */
  phone: string
  /** 短信验证码 */
  smsCode: string
}

/**
 * 三方登录对象
 */
export interface SocialLoginBody extends LoginBody {
  /** 认证类型 */
  authType: 'social'
  /** 租户ID */
  tenantId: string
  /** 第三方登录平台 */
  source: string
  /** 第三方登录code */
  socialCode: string
  /** 第三方登录socialState */
  socialState: string
}

/**
 * 统一登录请求类型（兼容所有登录方式）
 */
export type LoginRequest = PasswordLoginBody | EmailLoginBody | SmsLoginBody | SocialLoginBody

/**
 * 用户注册对象
 */
export interface RegisterBody extends LoginBody {
  /** 租户id 不会传到后端 在请求头进行控制 */
  tenantId?: string
  /** 认证类型 */
  authType: 'password'
  /** 用户名 */
  userName: string
  /** 用户密码 */
  password: string
  /** 用户类型 */
  userType?: 'pc_user' | 'app_user'
  /**确认密码 (只于前端校验)*/
  confirmPassword?: string
  /** 邀请码*/
  inviteCode?: string
}

/**
 * 认证令牌响应对象
 */
export interface AuthTokenVo {
  /** 访问令牌 */
  access_token: string
  /** 刷新令牌 */
  refresh_token?: string
  /** 访问令牌有效期（秒） */
  expire_in: number
  /** 刷新令牌有效期（秒） */
  refresh_expire_in?: number
  /** 令牌权限范围 */
  scope?: string
}

/**
 * 租户配置信息
 */
export interface TenantConfigVo {
  /** 租户功能是否启用 */
  tenantEnabled: boolean

  /** 可用租户选项列表 */
  voList: TenantOptionVo[]
}

/**
 * 租户选择选项对象
 */
export interface TenantOptionVo {
  /** 租户ID */
  tenantId: string

  /** 企业名称 */
  companyName: string

  /** 域名 */
  domain: string
}

