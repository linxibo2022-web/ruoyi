// Token 管理钩子
import { objectToQuery } from '@/utils/object'
import { localCache } from '@/utils/cache'
/**
 * Token 管理钩子 (useToken)
 *
 * 基于封装的 cache 工具实现 token 的本地持久化管理。
 *
 * 包含以下功能:
 * - 获取 Token: 获取当前存储的 token (getToken)
 * - 设置 Token: 设置并持久化新的 token (setToken)
 * - 移除 Token: 清除存储的 token (removeToken)
 * - 认证头部: 获取认证头部的不同格式 (getAuthHeaders, getAuthQuery)
 *
 * @description 提供 token 的存取、删除和认证头部生成功能，基于 cache 工具类实现
 * @returns 包含 token 相关操作的方法
 */
export const useToken = () => {
  /**
   * Token 缓存键名
   */
  const TOKEN_KEY = 'token'

  /**
   * 获取 token
   * @returns 当前存储的 token
   */
  const getToken = (): string | null => {
    return localCache.get(TOKEN_KEY)
  }

  /**
   * 设置 token
   * @param accessToken 要存储的 token
   * @param expireSeconds 过期时间（秒），不传则永不过期
   */
  const setToken = (accessToken: string, expireSeconds?: number): void => {
    localCache.set(TOKEN_KEY, accessToken, expireSeconds)
  }

  /**
   * 移除 token
   */
  const removeToken = (): void => {
    localCache.remove(TOKEN_KEY)
  }

  /**
   * 获取认证头部 (Record 格式)
   * @returns 认证头部对象，如果没有 token 则返回空对象
   *
   * @example
   * const headers = getAuthHeaders();
   * // 返回: { Authorization: 'Bearer token123' } 或 {}
   */
  const getAuthHeaders = (): Record<string, string> => {
    const tokenValue = getToken()
    if (!tokenValue) {
      return {}
    }

    return {
      Authorization: `Bearer ${tokenValue}`
    }
  }

  /**
   * 获取认证头部 (查询字符串格式)
   * @returns 认证头部的查询字符串，如果没有 token 则返回空字符串
   *
   * @example
   * const queryString = getAuthQuery();
   * // 返回: "Authorization=Bearer%20token123" 或 ""
   */
  const getAuthQuery = (): string => {
    const headers = getAuthHeaders()
    return objectToQuery(headers)
  }

  return {
    getToken,
    setToken,
    removeToken,
    getAuthHeaders,
    getAuthQuery
  }
}
