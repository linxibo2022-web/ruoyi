// HTTP请求
import router, { resetRouter } from '@/router/router'
import { localCache, sessionCache } from '@/utils/cache'
import axios, { type AxiosResponse, type AxiosRequestConfig, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { getLanguage } from '@/locales/i18n'
import { encodeBase64, decodeBase64, encryptWithAes, generateAesKey, decryptWithAes } from '@/utils/crypto'
import { rsaEncrypt, rsaDecrypt } from '@/utils/rsa'
import { objectToQuery } from '@/utils/string'
import { SystemConfig } from '@/systemConfig'
import { to } from '@/utils/to'
import { showMsgError, showNotifyError, showConfirm } from '@/utils/modal'
import { formatDate } from '@/utils/date'

/** HTTP状态码 - 仅定义使用到的 */
const HttpCode = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  INTERNAL_SERVER_ERROR: 500,
  WARN: 601
} as const

/** 错误消息 - 仅用于前端固定场景 */
const ErrorMsg = {
  NETWORK: '接口连接异常',
  TIMEOUT: '接口请求超时',
  REPEAT_SUBMIT: '数据正在处理，请勿重复提交',
  DECRYPT_FAILED: '响应数据解密失败',
  SESSION_EXPIRED: '无效的会话，或者会话已过期，请重新登录。',
  UNKNOWN: '网络错误'
} as const

/**
 * HTTP 请求服务 (useHttp)
 *
 * 基于 Axios 的 HTTP 请求封装，提供完整的请求拦截、响应处理和错误处理机制。
 *
 * 包含以下功能：
 * - 请求方法: 封装标准 HTTP 方法 (get, post, put, delete, request)
 * - 拦截器处理: 自动处理请求/响应拦截器配置
 * - 认证管理: 自动添加 Token 和处理未授权情况
 * - 数据加密: 支持请求数据 AES 加密和 RSA 密钥交换
 * - 防重复提交: 阻止短时间内重复提交相同数据
 * - 国际化支持: 自动添加语言请求头
 * - 错误处理: 统一处理各类 HTTP 错误和状态码
 * - 请求取消: 支持取消未完成的请求
 * - 统一返回格式: 所有请求统一返回 [error, data] 数组格式
 * - 泛型支持: 完整的 TypeScript 类型推导
 *
 * @author 抓蛙师
 * @since 2025-05-29
 */

/**是否显示重新登录（导出供路由守卫使用）*/
export const isReLogin = { show: false }

/**加密请求头名称*/
const encryptHeader = 'encrypt-key'

/**
 * 创建axios实例
 */
const createAxiosInstance = (config?: AxiosRequestConfig): AxiosInstance => {
  // 创建 axios 实例
  const instance = axios.create({
    baseURL: SystemConfig.api.baseUrl,
    timeout: 50000,
    headers: {
      'Content-Type': 'application/json;charset=utf-8'
    },
    transitional: {
      // 超时错误更明确
      clarifyTimeoutError: true
    },
    ...config
  })

  // 请求拦截器
  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // 设置国际化 因为运行过程可能发生改变所以需要请求拦截获取设置
      config.headers['Content-Language'] = getLanguage()

      // 添加请求ID用于日志链路追踪 - 格式：yyyyMMddHHmmssSSS
      config.headers['X-Request-Id'] = formatDate(new Date(), 'yyyyMMddHHmmssSSS')

      // 是否需要认证
      if (config.headers?.auth !== false) {
        // 附加请求头
        Object.assign(config.headers, useToken().getAuthHeaders())
      }
      // 是否需要附加租户id
      if (config.headers?.tenant !== false) {
        const tenantId = getTenantId()
        if (tenantId) {
          config.headers['X-Tenant-Id'] = tenantId
        }
      }

      // get请求映射params参数
      if (config.method === 'get' && config.params) {
        const queryString = objectToQuery(config.params)
        if (queryString) {
          config.url = config.url + '?' + queryString
        }
        config.params = {}
      }

      // 是否需要防止数据重复提交
      if (config.headers?.repeatSubmit !== false && (config.method === 'post' || config.method === 'put')) {
        const requestObj = {
          url: config.url,
          data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
          time: new Date().getTime()
        }
        const repeatSubmitCache = sessionCache.getJSON('repeatSubmitCache')
        if (repeatSubmitCache === undefined || repeatSubmitCache === null || repeatSubmitCache === '') {
          sessionCache.setJSON('repeatSubmitCache', requestObj)
        } else {
          const s_url = repeatSubmitCache.url // 请求地址
          const s_data = repeatSubmitCache.data // 请求数据
          const s_time = repeatSubmitCache.time // 请求时间
          const interval = 5000 // 间隔时间(ms)，小于此时间视为重复提交
          if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
            console.warn(`[${s_url}]: ` + ErrorMsg.REPEAT_SUBMIT)
            return Promise.reject(new Error(ErrorMsg.REPEAT_SUBMIT))
          } else {
            sessionCache.setJSON('repeatSubmitCache', requestObj)
          }
        }
      }

      // 参数加密处理
      if (SystemConfig.security.apiEncrypt) {
        // 当开启参数加密
        if (config.headers?.isEncrypt === 'true' && (config.method === 'post' || config.method === 'put')) {
          // 生成一个 AES 密钥
          const aesKey = generateAesKey()
          config.headers[encryptHeader] = rsaEncrypt(encodeBase64(aesKey))
          config.data = typeof config.data === 'object' ? encryptWithAes(JSON.stringify(config.data), aesKey) : encryptWithAes(config.data, aesKey)
        }
      }

      // FormData数据去请求头Content-Type
      if (config.data instanceof FormData) {
        delete config.headers['Content-Type']
      }

      return config
    },
    (error) => {
      console.error('请求错误:', error)
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  instance.interceptors.response.use(
    (res: AxiosResponse) => {
      // 加密数据解密
      if (SystemConfig.security.apiEncrypt) {
        // 加密后的 AES 秘钥
        const keyStr = res.headers[encryptHeader]
        // 加密
        if (keyStr != null && keyStr != '') {
          try {
            const data = res.data
            // 请求体 AES 解密
            const base64Str = rsaDecrypt(keyStr)
            // base64 解码 得到请求头的 AES 秘钥
            const aesKey = decodeBase64(base64Str.toString())
            // aesKey 解码 data
            const decryptData = decryptWithAes(data as any, aesKey)
            // 将结果 (得到的是 JSON 字符串) 转为 JSON
            res.data = JSON.parse(decryptData)
          } catch (err) {
            console.error(`[响应解密]${ErrorMsg.DECRYPT_FAILED}:${err}`)
            return Promise.reject(new Error(`${ErrorMsg.DECRYPT_FAILED}:${err}`))
          }
        }
      }

      // 二进制数据 包装一层R
      if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
        return Promise.resolve(res)
      }

      // 未设置状态码则默认成功状态
      const code = res.data.code || HttpCode.SUCCESS
      const msg = res.data.msg || ''
      const data = res.data.data

      // 是否禁用错误提示（用于业务层自定义错误处理）
      const noMsgError = (res.config as any)?.noMsgError === true

      // 处理不同状态码 - 只处理关键状态码
      if (code === HttpCode.SUCCESS) {
        return Promise.resolve(data)
      }

      if (code === HttpCode.UNAUTHORIZED) {
        // 如果禁用了错误提示,则不显示401提示
        if (!noMsgError) {
          handleUnauthorized()
        }
        return Promise.reject(new Error(ErrorMsg.SESSION_EXPIRED))
      }

      if (code === HttpCode.INTERNAL_SERVER_ERROR) {
        const errorMsg = msg || ErrorMsg.UNKNOWN
        // 如果设置了 noMsgError，则不显示错误提示
        if (!noMsgError) {
          showMsgError(errorMsg)
        }
        return Promise.reject(new Error(errorMsg))
      }

      if (code === HttpCode.WARN) {
        const warnMsg = msg || ErrorMsg.UNKNOWN
        // 如果设置了 noMsgError，则不显示警告提示
        if (!noMsgError) {
          showMsgError({ message: warnMsg, type: 'warning' })
        }
        return Promise.reject(new Error(warnMsg))
      }

      // 其他错误直接使用后端返回的消息
      const errorMsg = msg || ErrorMsg.UNKNOWN
      // 如果设置了 noMsgError，则不显示错误通知
      if (!noMsgError) {
        showNotifyError(errorMsg)
      }
      return Promise.reject(new Error(errorMsg))
    },
    // 错误处理
    (error) => {
      // 处理响应错误
      let { message } = error
      // 如果是网络错误，获取请求的URL
      const url = error?.config?.url || ''
      // 是否禁用错误提示
      const noMsgError = (error?.config as any)?.noMsgError === true
      // 错误日志
      console.error(`[网络错误] ${message} - URL: ${url}`, error)

      if (message === 'Network Error') {
        message = `${ErrorMsg.NETWORK}[${url}]`
      } else if (message.includes('timeout')) {
        message = `${ErrorMsg.TIMEOUT}[${url}]`
      } else if (message.includes('Request failed with status code')) {
        const statusCode = message.substr(message.length - 3)
        message = `接口${statusCode}异常[${url}]`
      }

      // 如果设置了 noMsgError，则不显示网络错误通知
      if (!noMsgError) {
        showNotifyError(message)
      }
      // 返回错误
      return Promise.reject(new Error(message))
    }
  )

  return instance
}

/**
 * 获取租户id
 */
const getTenantId = (): string | null => {
  // 1. 获取查询参数
  const urlParams = new URLSearchParams(window.location.search)

  // 2. 直接获取 tenantId 参数
  let tenantId = urlParams.get('tenantId')
  if (tenantId) {
    localCache.set('tenantId', tenantId)
    return tenantId
  }

  // 3. 检查 redirect 参数中是否包含 tenantId
  const redirect = urlParams.get('redirect')
  if (redirect) {
    // 解码redirect参数
    const decodedRedirect = decodeURIComponent(redirect)
    // 分离URL路径和查询字符串
    const [_, query] = decodedRedirect.split('?')
    if (query) {
      // 从查询字符串创建URLSearchParams
      const redirectParams = new URLSearchParams(query)
      tenantId = redirectParams.get('tenantId')
    }
  }

  // 4. 如果找到租户 ID，保存并返回
  if (tenantId) {
    localCache.set('tenantId', tenantId)
    return tenantId
  }

  // 5. 没有找到，返回之前保存的值或默认值
  return localCache.get('tenantId') || '000000'
}

/**
 * 处理未授权情况
 */
const handleUnauthorized = async () => {
  // 如果已经在处理中，避免重复处理
  if (isReLogin.show) {
    return
  }

  // 标记正在处理
  isReLogin.show = true

  // 弹出确认框
  const [err] = await showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning'
  })

  if (!err) {
    // 用户点击了重新登录
    isReLogin.show = false
    const userStore = useUserStore()
    await userStore.logoutUser()
    resetRouter()
    // 跳转到登录页
    router.replace({
      path: '/login',
      query: {
        redirect: encodeURIComponent(router.currentRoute.value.fullPath || '/')
      }
    })
  } else {
    // 用户点击了取消
    isReLogin.show = false
  }
}

// 创建默认axios实例
const axiosInstance = createAxiosInstance()

/**
 * HTTP请求钩子
 * 支持链式调用配置：http.noAuth().encrypt().post(...)
 * @param initialConfig 初始化配置
 */
export const useHttp = (initialConfig?: AxiosRequestConfig) => {
  // 创建axios实例
  const instance = initialConfig ? createAxiosInstance(initialConfig) : axiosInstance

  // 临时配置（用于链式调用）
  let chainConfig: AxiosRequestConfig = {}

  // 合并配置并重置链式配置
  const mergeAndResetConfig = (config?: AxiosRequestConfig): AxiosRequestConfig => {
    const merged = {
      ...chainConfig,
      ...config,
      headers: {
        ...chainConfig.headers,
        ...config?.headers
      }
    }
    chainConfig = {} // 重置
    return merged
  }

  /**
   * 发送GET请求
   * @param url 请求地址
   * @param params 请求参数
   * @param config 请求配置
   * @returns Result<T> [Error | null, T | null]
   *
   * @example
   * const [err, data] = await http.get<User[]>('/api/users');
   * if (!err) {
   *   console.log(data);
   * }
   */
  const get = <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Result<T> => {
    return to<T>(instance.get(url, { params, ...mergeAndResetConfig(config) }))
  }

  /**
   * 发送POST请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Result<T> [Error | null, T | null]
   *
   * @example
   * const [err, user] = await http.post<User>('/api/users', userData);
   * if (!err) {
   *   console.log(user);
   * }
   */
  const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Result<T> => {
    return to<T>(instance.post(url, data, mergeAndResetConfig(config)))
  }

  /**
   * 发送PUT请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Result<T> [Error | null, T | null]
   *
   * @example
   * const [err, user] = await http.put<User>('/api/users/123', updatedData);
   * if (!err) {
   *   console.log(user);
   * }
   */
  const put = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Result<T> => {
    return to<T>(instance.put(url, data, mergeAndResetConfig(config)))
  }

  /**
   * 发送DELETE请求
   * @param url 请求地址
   * @param params 请求参数
   * @param config 请求配置
   * @returns Result<T> [Error | null, T | null]
   *
   * @example
   * const [err, result] = await http.del<void>('/api/users/123');
   * if (!err) {
   *   console.log('删除成功');
   * }
   */
  const del = <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Result<T> => {
    return to<T>(instance.delete(url, { params, ...mergeAndResetConfig(config) }))
  }

  /**
   * 自定义请求
   * @param config 请求配置
   * @returns Result<T> [Error | null, T | null]
   *
   * @example
   * const [err, data] = await http.request<User[]>({
   *   method: 'PATCH',
   *   url: '/api/users/123',
   *   data: partialData
   * });
   * if (!err) {
   *   console.log(data);
   * }
   */
  const request = <T = any>(config: AxiosRequestConfig): Result<T> => {
    return to<T>(instance.request(mergeAndResetConfig(config)))
  }

  /**
   * 链式调用：通用配置方法
   * @example http.config({ headers: { auth: false }, timeout: 20000 }).post(...)
   */
  const config = (cfg: AxiosRequestConfig) => {
    chainConfig = { ...chainConfig, ...cfg, headers: { ...chainConfig.headers, ...cfg.headers } }
    return httpInstance
  }

  /**
   * 链式调用：禁用认证
   * @example http.noAuth().post(...)
   */
  const noAuth = () => {
    chainConfig.headers = { ...chainConfig.headers, auth: false }
    return httpInstance
  }

  /**
   * 链式调用：启用加密
   * @example http.encrypt().post(...)
   */
  const encrypt = () => {
    chainConfig.headers = { ...chainConfig.headers, isEncrypt: true }
    return httpInstance
  }

  /**
   * 链式调用：禁用防重复提交
   * @example http.noRepeatSubmit().post(...)
   */
  const noRepeatSubmit = () => {
    chainConfig.headers = { ...chainConfig.headers, repeatSubmit: false }
    return httpInstance
  }

  /**
   * 链式调用：禁用租户信息
   * @example http.noTenant().post(...)
   */
  const noTenant = () => {
    chainConfig.headers = { ...chainConfig.headers, tenant: false }
    return httpInstance
  }

  /**
   * 链式调用：禁用错误提示（用于业务层自定义错误处理）
   * @example http.noMsgError().post(...)
   */
  const noMsgError = () => {
    ;(chainConfig as any).noMsgError = true
    return httpInstance
  }

  /**
   * 链式调用：设置超时时间
   * @example http.timeout(20000).post(...)
   */
  const timeout = (ms: number) => {
    chainConfig.timeout = ms
    return httpInstance
  }

  const httpInstance = {
    get,
    post,
    put,
    del,
    request,
    axios: instance,
    // 链式调用方法
    config,
    noAuth,
    encrypt,
    noRepeatSubmit,
    noTenant,
    noMsgError,
    timeout
  }

  return httpInstance
}

// 创建一个全局默认实例
export const http = useHttp()
