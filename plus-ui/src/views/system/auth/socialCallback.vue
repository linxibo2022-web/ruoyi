<!-- 社交登录回调 -->
<template>
  <div v-loading="isProcessing" class="social-callback"></div>
</template>

<script setup lang="ts" name="SocialCallback">
import { socialBind, userLogin } from '@/api/system/auth/authApi'
import type { SocialLoginBody } from '@/api/system/auth/authTypes'
import { showMsgError, showMsgSuccess } from '@/utils/modal'
import { SystemConfig } from '@/systemConfig'
// ==================== 常量定义 ====================
const REDIRECT_DELAY = 2000 // 重定向延迟时间(毫秒)
const DEFAULT_TENANT_ID = '000000' // 默认租户ID

// ==================== 工具与配置 ====================
const route = useRoute()
const tokenManager = useToken()

// ==================== 响应式状态 ====================
const isProcessing = ref(true) // 是否正在处理回调

// ==================== 类型定义 ====================
interface CallbackParams {
  code: string // 授权码
  state: string // 状态参数
  source: string // 社交平台来源
  tenantId: string // 租户ID
  targetDomain: string // 目标域名
}

interface StateParams {
  tenantId?: string
  domain: string
}

// ==================== 参数解析方法 ====================
/**
 * 解析状态参数
 * @param stateParam base64编码的状态参数
 * @returns 解析后的状态对象
 */
const parseStateParams = (stateParam: string): StateParams => {
  try {
    return JSON.parse(atob(stateParam))
  } catch (error) {
    console.error('状态参数解析失败:', error)
    throw new Error('无效的状态参数格式')
  }
}

/**
 * 提取并验证回调参数
 * @returns 解析后的回调参数
 */
const extractCallbackParams = (): CallbackParams => {
  const code = route.query.code as string
  const state = route.query.state as string
  const source = route.query.source as string

  // 验证必需参数
  if (!code || !state || !source) {
    throw new Error('缺少必需的回调参数: code, state, source')
  }

  // 解析状态参数
  const stateData = parseStateParams(state)
  const { tenantId = DEFAULT_TENANT_ID, domain: targetDomain } = stateData

  if (!targetDomain) {
    throw new Error('状态参数中缺少目标域名')
  }

  return {
    code,
    state,
    source,
    tenantId,
    targetDomain
  }
}

// ==================== 域名处理方法 ====================
/**
 * 检查是否需要跨域重定向
 * @param targetDomain 目标域名
 * @returns 是否需要重定向
 */
const shouldRedirectToDomain = (targetDomain: string): boolean => {
  return window.location.host !== targetDomain
}

/**
 * 执行跨域重定向
 * @param targetDomain 目标域名
 */
const redirectToDomain = (targetDomain: string): void => {
  try {
    const redirectUrl = new URL(window.location.href)
    redirectUrl.host = targetDomain
    window.location.href = redirectUrl.toString()
  } catch (error) {
    console.error('域名重定向失败:', error)
    throw new Error('无法构建重定向URL')
  }
}

// ==================== 响应处理方法 ====================
/**
 * 处理 [err, data] 格式的响应
 * @param err 错误信息
 * @param data 响应数据
 */
const handleApiResponse = (err: any, data: any): void => {
  if (err) {
    redirectToHome()
    return
  }

  // 处理成功响应
  handleSuccessResponse(data)
}

/**
 * 处理成功响应
 * @param data 成功响应数据
 */
const handleSuccessResponse = (data: any): void => {
  // 设置访问令牌（仅登录接口有token）
  const accessToken = data?.access_token
  if (accessToken) {
    tokenManager.setToken(accessToken)
  }

  // 显示成功消息
  showMsgSuccess('登录成功')

  // 重定向到首页
  redirectToHome()
}

/**
 * 显示错误消息并重定向
 * @param message 错误消息
 */
const showErrorAndRedirect = (message: string): void => {
  showMsgError(message)
  redirectToHome()
}

/**
 * 重定向到首页
 */
const redirectToHome = (): void => {
  setTimeout(() => {
    window.location.href = `${SystemConfig.app.contextPath}index`
  }, REDIRECT_DELAY)
}

// ==================== 业务处理方法 ====================
/**
 * 构建社交登录请求体
 * @param params 回调参数
 * @returns 社交登录请求体
 */
const buildSocialLoginBody = (params: CallbackParams): SocialLoginBody => {
  return {
    authType: 'social',
    socialCode: params.code,
    socialState: params.state,
    tenantId: params.tenantId,
    source: params.source
  }
}

/**
 * 执行社交账号绑定
 * @param loginBody 社交登录请求体
 */
const executeSocialBind = async (loginBody: SocialLoginBody): Promise<void> => {
  const [err, data] = await socialBind(loginBody)
  handleApiResponse(err, data)
}

/**
 * 执行社交账号登录
 * @param loginBody 社交登录请求体
 */
const executeSocialLogin = async (loginBody: SocialLoginBody): Promise<void> => {
  const [err, data] = await userLogin(loginBody)
  handleApiResponse(err, data)
}

/**
 * 根据用户状态选择处理方式
 * @param loginBody 社交登录请求体
 */
const processSocialAuth = async (loginBody: SocialLoginBody): Promise<void> => {
  const hasToken = !!tokenManager.getToken()

  if (hasToken) {
    // 用户已登录，执行账号绑定
    await executeSocialBind(loginBody)
  } else {
    // 用户未登录，执行社交登录
    await executeSocialLogin(loginBody)
  }
}

// ==================== 主流程控制 ====================
/**
 * 初始化社交登录回调处理
 */
const initializeCallback = async (): Promise<void> => {
  try {
    // 1. 提取并验证回调参数
    const params = extractCallbackParams()

    // 2. 检查是否需要跨域重定向
    if (shouldRedirectToDomain(params.targetDomain)) {
      redirectToDomain(params.targetDomain)
      return
    }

    // 3. 构建请求体并处理认证
    const loginBody = buildSocialLoginBody(params)
    await processSocialAuth(loginBody)
  } catch (error) {
    console.error('社交登录回调初始化失败:', error)
    const errorMessage = error instanceof Error ? error.message : '未知错误'
    showErrorAndRedirect(errorMessage)
  } finally {
    isProcessing.value = false
  }
}

// ==================== 生命周期钩子 ====================
/**
 * 组件挂载后初始化
 */
onMounted(async () => {
  await nextTick()
  await initializeCallback()
})
</script>
