// HTTP类型声明
import type { AxiosRequestConfig } from 'axios'

/**
 * 自定义请求头接口
 */
export interface CustomHeaders {
  /** 是否需要认证，默认 true */
  auth?: boolean
  /** 是否需要租户ID，默认 true */
  tenant?: boolean
  /** 是否防止重复提交，默认 true */
  repeatSubmit?: boolean
  /** 是否加密请求数据 */
  isEncrypt?: boolean

  /** 其他自定义头部 */
  [key: string]: any
}
