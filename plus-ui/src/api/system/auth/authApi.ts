// 认证授权 API
import type { CaptchaVo, LoginRequest, AuthTokenVo, SocialLoginBody, TenantConfigVo } from './authTypes'
import { withHeaders } from '@/utils/function'
import { SystemConfig } from '@/systemConfig'

// ==================== 基础认证相关接口 ====================

/**
 * 用户登录接口
 * @param data 登录数据对象，包含用户名、密码等信息
 * @returns {Result<AuthTokenVo>} 返回登录结果，包含token等用户认证信息
 */
export const userLogin = (data: LoginRequest): Result<AuthTokenVo> => {
  const params = {
    ...data,
    authType: data.authType || 'password'
  }
  return http.post<AuthTokenVo>(
    '/auth/userLogin',
    params,
    withHeaders({
      // 不需要token认证
      auth: false,
      // 需要加密传输
      isEncrypt: true,
      // 防止重复提交
      repeatSubmit: false
    })
  )
}

/**
 * 用户注销接口
 * 退出登录并关闭SSE连接(如果开启)
 * @returns {Result<void>} 返回注销结果
 */
export const userLogout = (): Result<void> => {
  const featureStore = useFeatureStore()
  if (featureStore.features.sseEnabled) {
    http.get<void>('/resource/sse/close')
  }
  // 禁用所有错误提示,包括401状态码的提示
  return http.noMsgError().post<void>('/auth/userLogout', {}, withHeaders({ repeatSubmit: false }))
}

/**
 * 用户注册接口
 * @param data 注册数据对象，包含用户信息
 * @returns {Result<void>} 返回注册结果
 */
export const userRegister = (data: any): Result<void> => {
  const params = {
    ...data,
    authType: 'password'
  }
  return http.post<void>(
    '/auth/userRegister',
    params,
    withHeaders({
      // 不需要token认证
      auth: false,
      // 需要加密传输
      isEncrypt: true,
      // 防止重复提交
      repeatSubmit: false
    })
  )
}

/**
 * 获取验证码
 * 用于登录时的图形验证码获取
 * @returns {Result<CaptchaVo>} 返回验证码图片和校验信息
 */
export const imgCode = (): Result<CaptchaVo> => {
  return http.get<CaptchaVo>(
    '/auth/imgCode',
    {},
    withHeaders(
      {
        // 不需要token认证
        auth: false
      },
      {
        // 等待时间较长，避免超时
        timeout: 20000
      }
    )
  )
}

// ==================== 社交认证相关接口 ====================

/**
 * 获取社交认证跳转URL
 * @param source 第三方平台来源
 * @param inviteCode 邀请码(可选，用于注册时绑定角色和部门)
 * @returns {Result<string>} 返回授权跳转链接
 */
export const socialBindUrl = (source: string, inviteCode?: string): Result<string> => {
  return http.get<string>(
    `/auth/socialBindUrl/${source}`,
    {
      domain: window.location.host,
      inviteCode: inviteCode || undefined
    },
    withHeaders({
      auth: false
    })
  )
}

/**
 * 第三方社交账号登录回调
 * 处理第三方平台的OAuth登录回调
 * @param data 社交登录数据
 * @returns {Result<void>} 返回登录结果
 */
export const socialBind = (data: SocialLoginBody): Result<void> => {
  const params = {
    ...data,
    // 认证方式为社交账号
    authType: 'social'
  }
  return http.post<void>('/auth/socialBind', params)
}

/**
 * 解绑社交账号授权
 * @param socialId 社交账号ID
 * @returns {Result<void>} 返回操作结果
 */
export const socialUnbind = (socialId: string | number): Result<void> => {
  return http.del<void>(`/auth/socialUnbind/${socialId}`)
}

/**
 * 获取租户开关和租户列表
 * 用于多租户系统中切换不同租户
 * @returns {Result<TenantConfigVo>} 返回租户开关和可用的租户列表
 */
export const getTenantConfig = (): Result<TenantConfigVo> => {
  return http.get<TenantConfigVo>('/auth/getTenantConfig')
}
