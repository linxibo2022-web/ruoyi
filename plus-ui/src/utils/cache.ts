// 缓存工具
import { SystemConfig } from '@/systemConfig'
/**
 * 浏览器缓存工具 (cache.ts)
 *
 * 提供对浏览器存储机制的封装，支持会话级和持久化的数据存储操作。
 * 所有缓存键会自动添加应用ID前缀，避免多应用冲突。
 *
 * 包含以下功能:
 * - 会话缓存: 基于sessionStorage的临时数据存储 (sessionCache)
 *   - 基础操作: 存取字符串数据 (set, get)
 *   - JSON处理: 存取JSON对象数据 (setJSON, getJSON)
 *   - 数据清理: 移除指定缓存数据 (remove)
 *
 * - 本地缓存: 基于localStorage的持久化数据存储 (localCache)
 *   - 基础操作: 存取字符串数据 (set, get)
 *   - JSON处理: 存取JSON对象数据 (setJSON, getJSON)
 *   - 数据清理: 移除指定缓存数据 (remove)
 *   - 过期时间: 支持设置缓存有效期 (expireSeconds)
 *   - 自动清理: 定期清理过期或损坏的缓存
 *   - 存储统计: 提供缓存使用情况统计 (getStats)
 */

// 缓存键前缀，防止多应用冲突
const KEY_PREFIX = `${SystemConfig.app.id}:`

/**
 * 为缓存键添加应用前缀
 * @param key 原始缓存键
 * @returns 添加了前缀的缓存键
 */
const getPrefixedKey = (key: string): string => {
  return `${KEY_PREFIX}${key}`
}

/**
 * 数据包装器，用于本地缓存支持过期时间
 */
interface CacheWrapper<T = any> {
  data: T
  _expire?: number // 过期时间戳（毫秒）
}

/**
 * 自动清理过期或损坏的本地缓存
 */
const autoCleanup = (): void => {
  if (!localStorage) {
    return
  }
  try {
    const keysToRemove: string[] = []
    const now = Date.now()
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(KEY_PREFIX)) {
        try {
          const wrapper: CacheWrapper = JSON.parse(localStorage.getItem(key)!)
          if (wrapper && wrapper._expire && wrapper._expire < now) {
            keysToRemove.push(key)
          }
        } catch (e) {
          keysToRemove.push(key) // 删除损坏的缓存
        }
      }
    }
    keysToRemove.forEach((key) => localStorage.removeItem(key))
    if (keysToRemove.length > 0) {
      console.log(`清理了 ${keysToRemove.length} 个过期缓存项`)
    }
  } catch (e) {
    console.warn('自动清理失败:', e)
  }
}

// 应用启动时清理过期缓存
if (typeof localStorage !== 'undefined') {
  setTimeout(() => {
    autoCleanup()
    const stats = localCache.getStats()
    if (stats) {
      //console.log(`本地缓存初始化完成，当前使用 ${stats.appKeys} 项，占用 ${stats.usagePercent}%`)
    }
  }, 1000)

  // 每小时清理一次过期缓存（可调整或禁用）
  setInterval(autoCleanup, 60 * 60 * 1000)
}

/**
 * 会话级缓存工具，基于 sessionStorage
 * 页面关闭后数据会被清除
 */
export const sessionCache = {
  /**
   * 设置会话缓存
   * @param key 缓存键（自动添加应用ID前缀）
   * @param value 缓存值（仅支持字符串）
   *
   * @example
   * // 存储用户名
   * sessionCache.set('userName', 'admin');
   * // 实际存储的键为 'appId:userName'
   *
   * // 存储数字需要转为字符串
   * sessionCache.set('userId', '12345');
   * sessionCache.set('count', String(42));
   */
  set(key: string, value: string): void {
    if (!sessionStorage) {
      return
    }
    if (key != null && value != null) {
      const prefixedKey = getPrefixedKey(key)
      sessionStorage.setItem(prefixedKey, value)
    }
  },

  /**
   * 获取会话缓存
   * @param key 缓存键（自动添加应用ID前缀）
   * @returns 缓存值或 null
   *
   * @example
   * // 获取用户名
   * const userName = sessionCache.get('userName');
   */
  get(key: string): string | null {
    if (!sessionStorage) {
      return null
    }
    if (key == null) {
      return null
    }
    const prefixedKey = getPrefixedKey(key)
    const value = sessionStorage.getItem(prefixedKey)
    return value
  },

  /**
   * 获取数字类型缓存（便捷转换方法）
   * @param key 缓存键（自动添加应用ID前缀）
   * @returns 数字或 null
   *
   * @example
   * // 获取数字
   * sessionCache.set('count', '42');
   * const count = sessionCache.getNumber('count'); // 42
   */
  getNumber(key: string): number | null {
    const value = this.get(key)
    if (value == null) return null
    const num = Number(value)
    return isNaN(num) ? null : num
  },

  /**
   * 设置 JSON 对象到会话缓存
   * @param key 缓存键（自动添加应用ID前缀）
   * @param jsonValue 要缓存的 JSON 对象
   *
   * @example
   * // 存储用户信息对象
   * sessionCache.setJSON('userInfo', { id: 1, name: 'admin', role: 'administrator' });
   */
  setJSON<T>(key: string, jsonValue: T): void {
    if (jsonValue != null) {
      this.set(key, JSON.stringify(jsonValue))
    }
  },

  /**
   * 从会话缓存获取 JSON 对象
   * @param key 缓存键（自动添加应用ID前缀）
   * @returns 解析后的 JSON 对象或 null
   *
   * @example
   * // 获取用户信息对象
   * const userInfo = sessionCache.getJSON<UserInfo>('userInfo');
   * if (userInfo) {
   *   console.log(userInfo.name); // TypeScript 类型安全
   * }
   */
  getJSON<T = any>(key: string): T | null {
    const value = this.get(key)
    if (value != null) {
      try {
        return JSON.parse(value) as T
      } catch (e) {
        console.error(`Error parsing JSON for key ${key}:`, e)
        return null
      }
    }
    return null
  },

  /**
   * 移除会话缓存项
   * @param key 要移除的缓存键（自动添加应用ID前缀）
   *
   * @example
   * // 移除用户信息
   * sessionCache.remove('userInfo');
   */
  remove(key: string): void {
    if (!sessionStorage || key == null) {
      return
    }
    const prefixedKey = getPrefixedKey(key)
    sessionStorage.removeItem(prefixedKey)
  },

  /**
   * 检查会话缓存是否存在
   * @param key 缓存键
   * @returns 是否存在
   *
   * @example
   * if (sessionCache.has('userToken')) {
   *   // 令牌存在
   * }
   */
  has(key: string): boolean {
    return this.get(key) !== null
  },

  /**
   * 清除所有带有当前应用前缀的会话缓存
   *
   * @example
   * // 清除当前应用的所有会话缓存
   * sessionCache.clearAll();
   */
  clearAll(): void {
    if (!sessionStorage) {
      return
    }

    // 遍历所有会话存储项，删除带有当前应用前缀的项
    for (let i = sessionStorage.length - 1; i >= 0; i--) {
      const key = sessionStorage.key(i)
      if (key && key.startsWith(KEY_PREFIX)) {
        sessionStorage.removeItem(key)
      }
    }
  },

  /**
   * 获取会话缓存键的原始名称（移除前缀）
   * 仅用于调试目的
   * @param prefixedKey 带前缀的缓存键
   * @returns 原始缓存键名称
   */
  getOriginalKey(prefixedKey: string): string {
    if (prefixedKey.startsWith(KEY_PREFIX)) {
      return prefixedKey.substring(KEY_PREFIX.length)
    }
    return prefixedKey
  }
}

/**
 * 本地缓存工具，基于 localStorage
 * 数据将永久保存，除非手动清除或浏览器清除缓存
 * 支持过期时间管理和自动清理
 */
export const localCache = {
  /**
   * 设置本地缓存
   * @param key 缓存键（自动添加应用ID前缀）
   * @param value 缓存值
   * @param expireSeconds 过期时间（秒），不传则永不过期
   *
   * @example
   * // 存储主题设置（永久）
   * localCache.set('theme', 'dark');//实际存储的键为 'appId:theme'
   * // 存储 token（7天过期）
   * localCache.set('userToken', 'abc123', 7 * 24 * 3600);
   */
  set<T>(key: string, value: T, expireSeconds?: number): void {
    if (!localStorage || key == null || value == null) {
      return
    }
    try {
      const prefixedKey = getPrefixedKey(key)
      const wrapper: CacheWrapper<T> = {
        data: value,
        _expire: expireSeconds ? Date.now() + expireSeconds * 1000 : undefined
      }
      localStorage.setItem(prefixedKey, JSON.stringify(wrapper))
    } catch (e) {
      console.error('缓存设置失败:', e)
    }
  },

  /**
   * 获取本地缓存
   * @param key 缓存键（自动添加应用ID前缀）
   * @returns 缓存值或 null（过期或无效数据返回 null）
   *
   * @example
   * // 获取主题设置
   * const theme = localCache.get<string>('theme'); // 'dark'
   */
  get<T = any>(key: string): T | null {
    if (!localStorage || key == null) {
      return null
    }
    try {
      const prefixedKey = getPrefixedKey(key)
      const value = localStorage.getItem(prefixedKey)
      if (value == null) {
        return null
      }
      const wrapper: CacheWrapper<T> = JSON.parse(value)
      if (!wrapper || typeof wrapper !== 'object') {
        this.remove(key)
        return null
      }
      if (wrapper._expire && wrapper._expire < Date.now()) {
        this.remove(key)
        return null
      }
      return wrapper.data as T
    } catch (e) {
      console.error(`缓存获取失败 [${key}]:`, e)
      this.remove(key)
      return null
    }
  },

  /**
   * 设置 JSON 对象到本地缓存
   * @param key 缓存键（自动添加应用ID前缀）
   * @param jsonValue 要缓存的 JSON 对象
   * @param expireSeconds 过期时间（秒），不传则永不过期
   *
   * @example
   * // 存储系统配置
   * localCache.setJSON('sysConfig', { language: 'zh-CN', fontSize: 'medium', autoSave: true });
   */
  setJSON<T>(key: string, jsonValue: T, expireSeconds?: number): void {
    if (jsonValue != null) {
      this.set(key, jsonValue, expireSeconds)
    }
  },

  /**
   * 从本地缓存获取 JSON 对象
   * @param key 缓存键（自动添加应用ID前缀）
   * @returns 解析后的 JSON 对象或 null
   *
   * @example
   * // 获取系统配置
   * const sysConfig = localCache.getJSON<SystemConfig>('sysConfig');
   * if (sysConfig) {
   *   console.log(sysConfig.language); // TypeScript 类型安全
   * }
   */
  getJSON<T = any>(key: string): T | null {
    return this.get<T>(key)
  },

  /**
   * 移除本地缓存项
   * @param key 要移除的缓存键（自动添加应用ID前缀）
   *
   * @example
   * // 移除系统配置
   * localCache.remove('sysConfig');
   */
  remove(key: string): void {
    if (!localStorage || key == null) {
      return
    }
    try {
      const prefixedKey = getPrefixedKey(key)
      localStorage.removeItem(prefixedKey)
    } catch (e) {
      console.error('缓存删除失败:', e)
    }
  },

  /**
   * 检查本地缓存是否存在且未过期
   * @param key 缓存键
   * @returns 是否存在
   *
   * @example
   * if (localCache.has('userToken')) {
   *   // 用户已登录且token未过期
   * }
   */
  has(key: string): boolean {
    return this.get(key) !== null
  },

  /**
   * 清除所有带有当前应用前缀的本地缓存
   *
   * @example
   * // 清除当前应用的所有本地缓存
   * localCache.clearAll();
   */
  clearAll(): void {
    if (!localStorage) {
      return
    }
    try {
      for (let i = localStorage.length - 1; i >= 0; i--) {
        const key = localStorage.key(i)
        if (key && key.startsWith(KEY_PREFIX)) {
          localStorage.removeItem(key)
        }
      }
    } catch (e) {
      console.error('清除缓存失败:', e)
    }
  },

  /**
   * 手动清理过期缓存
   *
   * @example
   * // 清理所有过期的本地缓存
   * localCache.cleanup();
   */
  cleanup(): void {
    autoCleanup()
  },

  /**
   * 获取本地缓存统计信息
   * @returns 统计信息或 null
   *
   * @example
   * const stats = localCache.getStats();
   * if (stats) {
   *   console.log(`当前使用: ${stats.usagePercent}%`);
   * }
   */
  getStats(): {
    totalKeys: number
    appKeys: number
    usagePercent: number
  } | null {
    if (!localStorage) {
      return null
    }
    try {
      const appKeys = Array.from({ length: localStorage.length })
        .map((_, i) => localStorage.key(i))
        .filter((key) => key && key.startsWith(KEY_PREFIX))
      const totalSize = JSON.stringify(localStorage).length // 估算大小
      const limitSize = 5 * 1024 * 1024 // 假设 5MB 限制
      return {
        totalKeys: localStorage.length,
        appKeys: appKeys.length,
        usagePercent: Math.min(100, Math.round((totalSize / limitSize) * 100))
      }
    } catch (e) {
      console.error('获取存储统计失败:', e)
      return null
    }
  },

  /**
   * 获取本地缓存键的原始名称（移除前缀）
   * 仅用于调试目的
   * @param prefixedKey 带前缀的缓存键
   * @returns 原始缓存键名称
   */
  getOriginalKey(prefixedKey: string): string {
    if (prefixedKey.startsWith(KEY_PREFIX)) {
      return prefixedKey.substring(KEY_PREFIX.length)
    }
    return prefixedKey
  }
}
