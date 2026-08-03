/**
 * 函数工具类
 *
 * 包含以下功能类别:
 * - 复制文本到剪贴板并显示提示(copy):
 * - 快速创建带headers的配置(withHeaders):
 * - 防抖与节流: 控制函数触发频率 (debounce, throttle)
 * - 函数执行控制: 控制函数执行方式 (once, delay, retry, withTimeout)
 * - 函数转换与组合: 转换函数调用方式 (curry, partial, memoize)
 * - 异步函数工具: 处理异步函数执行 (serial, parallel, withRetry, rateLimit)
 */

import type { AxiosRequestConfig } from 'axios'
import { CustomHeaders } from '@/types/http'
import { showMsgSuccess, showMsgError } from './modal'

/**
 * 复制文本到剪贴板
 *
 * @param text 要复制的文本
 * @param message 可选的提示消息，不传则使用默认提示
 * @returns Promise<boolean> 是否复制成功
 */
export const copy = async (text: string, message?: string): Promise<boolean> => {
  try {
    // 优先使用现代 Clipboard API
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
    } else {
      // 回退到传统方法
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.opacity = '0'

      document.body.appendChild(textArea)
      textArea.select()

      const successful = document.execCommand('copy')
      document.body.removeChild(textArea)

      if (!successful) {
        console.error('执行execCommand失败')
        return false
      }
    }

    // 复制成功，根据内容长度显示不同提示
    if (message) {
      showMsgSuccess(message)
    } else {
      showMsgSuccess(text.length > 200 ? '复制成功' : `复制成功: ${text}`)
    }

    return true
  } catch (error) {
    console.error('复制失败:', error)
    showMsgError('复制失败，请手动复制')
    return false
  }
}

/**
 * 快速创建带headers的配置
 */
export const withHeaders = (headers: CustomHeaders, config?: AxiosRequestConfig): AxiosRequestConfig => {
  return {
    ...config,
    headers: {
      ...config?.headers,
      ...headers
    }
  }
}

// ==================== 防抖与节流 ====================

/**
 * 函数防抖
 * 在指定时间内多次调用，只执行最后一次（或第一次）
 *
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间（毫秒）
 * @param {boolean} immediate 是否立即执行（true: 第一次调用立即执行，之后等待；false: 等待后执行）
 * @returns {Function} 防抖处理后的函数
 * @example
 * // 窗口调整大小时，只在停止调整 200ms 后执行一次 resize 函数
 * window.addEventListener('resize', debounce(handleResize, 200));
 *
 * // 立即执行模式：第一次点击立即响应，之后如果在 300ms 内点击则忽略
 * 新增EventListener('click', debounce(handleClick, 300, true));
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  immediate: boolean = false
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let result: any

  const debounced = function (this: any, ...args: Parameters<T>) {
    const context = this

    const later = function () {
      timeout = null
      if (!immediate) {
        result = func.apply(context, args)
      }
    }

    const callNow = immediate && !timeout

    if (timeout) {
      clearTimeout(timeout)
    }
    timeout = setTimeout(later, wait)

    if (callNow) {
      result = func.apply(context, args)
    }

    return result
  }

  debounced.cancel = function () {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
  }

  return debounced as (...args: Parameters<T>) => ReturnType<T>
}

/**
 * 函数节流
 * 在指定时间内，函数最多执行一次
 *
 * @param {Function} func 要节流的函数
 * @param {number} wait 等待时间（毫秒）
 * @param {Object} options 配置选项
 * @param {boolean} options.leading 是否在开始时执行一次（默认true）
 * @param {boolean} options.trailing 是否在结束时再执行一次（默认true）
 * @returns {Function} 节流处理后的函数
 * @example
 * // 滚动时，每200ms最多执行一次handleScroll函数
 * window.addEventListener('scroll', throttle(handleScroll, 200));
 *
 * // 不在开始和结束时执行的节流函数
 * window.addEventListener('scroll', throttle(handleScroll, 200, { leading: false, trailing: false }));
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  options: { leading?: boolean; trailing?: boolean } = {}
): ((...args: Parameters<T>) => ReturnType<T>) => {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0
  let args: Parameters<T> | null = null
  let context: any = null
  let result: any

  // 设置默认选项
  if (!options) options = {}
  const leading = 'leading' in options ? !!options.leading : true
  const trailing = 'trailing' in options ? !!options.trailing : true

  const later = function () {
    previous = leading === false ? 0 : Date.now()
    timeout = null
    if (args && context) {
      result = func.apply(context, args)
      context = args = null
    }
  }

  const throttled = function (this: any, ...currentArgs: Parameters<T>) {
    const now = Date.now()
    context = this
    args = currentArgs

    if (!previous && leading === false) {
      previous = now
    }

    const remaining = wait - (now - previous)

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      result = func.apply(context, args)
      context = args = null
    } else if (!timeout && trailing !== false) {
      timeout = setTimeout(later, remaining)
    }

    return result
  }

  throttled.cancel = function () {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
    previous = 0
    args = context = null
  }

  return throttled as (...args: Parameters<T>) => ReturnType<T>
}

// ==================== 函数执行控制 ====================

/**
 * 确保函数只执行一次
 *
 * @param {Function} func 要控制的函数
 * @returns {Function} 包装后的函数，只会执行一次
 * @example
 * const initialize = once(() => {
 *   console.log('初始化操作，只执行一次');
 * });
 *
 * // 多次调用，实际只执行一次
 * initialize();
 * initialize();
 */
export const once = <T extends (...args: any[]) => any>(func: T): ((...args: Parameters<T>) => ReturnType<T>) => {
  let ran = false
  let result: any

  return function (this: any, ...args: Parameters<T>) {
    if (ran) return result
    ran = true
    result = func.apply(this, args)
    return result
  } as (...args: Parameters<T>) => ReturnType<T>
}

/**
 * 延迟执行函数
 *
 * @param {Function} func 要延迟执行的函数
 * @param {number} wait 延迟时间（毫秒）
 * @param {...any} args 传递给函数的参数
 * @returns {Promise} 返回Promise，函数执行后resolve
 * @example
 * // 延迟1秒后输出'Hello'
 * await delay(console.log, 1000, 'Hello');
 *
 * // 延迟处理用户操作
 * 新增EventListener('click', () => {
 *   delay(handleAction, 500, clickEvent);
 * });
 */
export const delay = <T extends (...args: any[]) => any>(func: T, wait: number = 0, ...args: Parameters<T>): Promise<ReturnType<T>> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(func(...args))
    }, wait)
  })
}

/**
 * 尝试多次执行函数，直到成功或达到最大尝试次数
 *
 * @param {Function} func 要执行的函数，应返回promise或值
 * @param {Object} options 配置选项
 * @param {number} options.maxAttempts 最大尝试次数，默认3
 * @param {number} options.delay 尝试间隔（毫秒），默认1000
 * @param {number} options.backoff 间隔增长系数，默认2（指数退避）
 * @returns {Promise} 返回Promise，成功执行后resolve，或者尝试次数用完后reject
 * @example
 * // 尝试最多5次获取数据，间隔时间为2秒，并且每次失败后等待时间翻倍
 * await retry(fetchData, { maxAttempts: 5, delay: 2000, backoff: 2 });
 */
export const retry = async <T>(
  func: () => Promise<T> | T,
  options: {
    maxAttempts?: number
    delay?: number
    backoff?: number
  } = {}
): Promise<T> => {
  const { maxAttempts = 3, delay = 1000, backoff = 2 } = options
  let attempts = 0
  let lastError: any
  let nextDelay = delay

  while (attempts < maxAttempts) {
    try {
      return await func()
    } catch (error) {
      attempts++
      lastError = error

      if (attempts >= maxAttempts) {
        break
      }

      // 等待下一次尝试
      await new Promise((resolve) => setTimeout(resolve, nextDelay))
      nextDelay *= backoff // 指数退避
    }
  }

  throw new Error(`最大尝试次数(${maxAttempts})已达到: ${lastError?.message || lastError}`)
}

/**
 * 超时控制函数
 * 如果函数在指定时间内未完成，则抛出超时错误
 *
 * @param {Function} func 异步函数
 * @param {number} ms 超时时间（毫秒）
 * @returns {Function} 带超时控制的函数
 * @example
 * // 创建一个5秒超时的API调用
 * const fetchWithTimeout = withTimeout(fetchData, 5000);
 *
 * try {
 *   const data = await fetchWithTimeout();
 * } catch (error) {
 *   if (error.name === 'TimeoutError') {
 *     console.log('请求超时');
 *   }
 * }
 */
export const withTimeout = <T, A extends any[]>(func: (...args: A) => Promise<T>, ms: number): ((...args: A) => Promise<T>) => {
  return async (...args: A): Promise<T> => {
    return new Promise<T>((resolve, reject) => {
      const timeoutId = setTimeout(() => {
        const error = new Error(`操作超时 (${ms}ms)`)
        error.name = 'TimeoutError'
        reject(error)
      }, ms)

      func(...args)
        .then((result) => {
          clearTimeout(timeoutId)
          resolve(result)
        })
        .catch((error) => {
          clearTimeout(timeoutId)
          reject(error)
        })
    })
  }
}

// ==================== 函数转换与组合 ====================

/**
 * 柯里化函数，将接受多个参数的函数转换为一系列接受单个参数的函数
 *
 * @param {Function} func 要柯里化的函数
 * @returns {Function} 柯里化后的函数
 * @example
 * const add = (a, b, c) => a + b + c;
 * const curriedAdd = curry(add);
 *
 * curriedAdd(1)(2)(3); // 6
 * curriedAdd(1, 2)(3); // 6
 * curriedAdd(1)(2, 3); // 6
 */
export const curry = <T extends (...args: any[]) => any>(func: T): any => {
  const arity = func.length

  return function curried(...args: any[]): any {
    if (args.length >= arity) {
      return func(...args)
    }

    return function (...moreArgs: any[]): any {
      return curried(...args, ...moreArgs)
    }
  }
}

/**
 * 偏函数应用，固定函数的部分参数
 *
 * @param {Function} func 原始函数
 * @param {...any} partialArgs 要固定的参数
 * @returns {Function} 新函数，接受剩余参数
 * @example
 * const multiply = (a, b) => a * b;
 * const double = partial(multiply, 2);
 * double(4); // 8
 *
 * const fetchFromApi = (endpoint, params) => fetch(`/api/${endpoint}`, params);
 * const fetchUsers = partial(fetchFromApi, 'users');
 * fetchUsers({ sort: 'name' }); // fetch('/api/users', { sort: 'name' })
 */
export const partial = <T extends (...args: any[]) => any>(func: T, ...partialArgs: any[]): ((...args: any[]) => ReturnType<T>) => {
  return function (this: any, ...args: any[]): ReturnType<T> {
    return func.apply(this, [...partialArgs, ...args])
  }
}

/**
 * 记忆化函数，缓存函数的计算结果
 *
 * @param {Function} func 要记忆化的函数
 * @param {Function} resolver 可选的键解析器函数，用于生成缓存键
 * @returns {Function} 记忆化后的函数
 * @example
 * // 斐波那契函数记忆化，避免重复计算
 * const fib = memoize((n) => {
 *   if (n <= 1) return n;
 *   return fib(n - 1) + fib(n - 2);
 * });
 *
 * fib(40); // 快速计算，不会重复计算子问题
 *
 * // 自定义键解析器
 * const getUser = memoize(
 *   (id, force) => api.fetchUser(id, force),
 *   (id, force) => force ? `user:${id}:force` : `user:${id}`
 * );
 */
export const memoize = <T extends (...args: any[]) => any>(
  func: T,
  resolver?: (...args: Parameters<T>) => string
): ((...args: Parameters<T>) => ReturnType<T>) => {
  const cache = new Map<string, ReturnType<T>>()

  return function (this: any, ...args: Parameters<T>): ReturnType<T> {
    const key = resolver ? resolver(...args) : JSON.stringify(args)

    if (cache.has(key)) {
      return cache.get(key) as ReturnType<T>
    }

    const result = func.apply(this, args)
    cache.set(key, result)
    return result
  }
}

// ==================== 异步函数工具 ====================

/**
 * 串行执行异步函数
 *
 * @param {Function[]} funcs 异步函数数组
 * @param {any} initial 初始值
 * @returns {Promise} Promise实例，解析为最终结果
 * @example
 * await serial([
 *   () => fetchUsers(),
 *   users => filterActiveUsers(users),
 *   activeUsers => sortUsersByName(activeUsers)
 * ]);
 */
export const serial = async <T>(funcs: ((arg?: any) => Promise<any>)[], initial?: any): Promise<T> => {
  let result = initial

  for (const func of funcs) {
    result = await func(result)
  }

  return result as T
}

/**
 * 并行执行异步函数并限制并发数
 *
 * @param {Function[]} tasks 异步任务数组
 * @param {number} concurrency 并发限制数
 * @returns {Promise<any[]>} Promise实例，解析为所有任务的结果数组
 * @example
 * // 同时处理最多3个文件
 * const results = await parallel(
 *   files.map(file => () => processFile(file)),
 *   3
 * );
 */
export const parallel = async <T>(tasks: (() => Promise<T>)[], concurrency: number = Infinity): Promise<T[]> => {
  if (!tasks.length) return []

  const results: T[] = []
  let index = 0
  let completed = 0

  return new Promise((resolve, reject) => {
    // 最多同时执行concurrency个任务
    const runTask = () => {
      const taskIndex = index++
      if (taskIndex >= tasks.length) return

      const task = tasks[taskIndex]
      task()
        .then((result) => {
          results[taskIndex] = result
          completed++

          if (completed === tasks.length) {
            resolve(results)
          } else {
            runTask() // 一个任务完成，继续执行下一个任务
          }
        })
        .catch(reject)
    }

    // 启动初始的并发任务
    const initialCount = Math.min(concurrency, tasks.length)
    for (let i = 0; i < initialCount; i++) {
      runTask()
    }
  })
}

/**
 * 为异步函数添加错误重试功能
 *
 * @param {Function} asyncFn 异步函数
 * @param {Object} options 重试选项
 * @returns {Function} 增强后的异步函数
 * @example
 * const fetchWithRetry = withRetry(fetchData, {
 *   retries: 3,
 *   retryDelay: 1000,
 *   shouldRetry: err => err.status === 429 // 只在请求频率限制时重试
 * });
 *
 * await fetchWithRetry(url);
 */
export const withRetry = <T extends (...args: any[]) => Promise<any>>(
  asyncFn: T,
  options: {
    retries?: number
    retryDelay?: number
    shouldRetry?: (error: any) => boolean
  } = {}
): ((...args: Parameters<T>) => Promise<ReturnType<T>>) => {
  const { retries = 3, retryDelay = 300, shouldRetry = () => true } = options

  return async function (this: any, ...args: Parameters<T>): Promise<ReturnType<T>> {
    let lastError: any

    for (let attempt = 0; attempt <= retries; attempt++) {
      try {
        return await asyncFn.apply(this, args)
      } catch (error) {
        lastError = error

        // 如果已经是最后一次尝试或不应该重试，则抛出错误
        if (attempt === retries || !shouldRetry(error)) {
          throw error
        }

        // 等待后重试
        await new Promise((resolve) => setTimeout(resolve, retryDelay))
      }
    }

    throw lastError
  }
}

/**
 * 限制函数执行频率
 *
 * @param {Function} fn 要限制的函数
 * @param {number} limit 限制次数
 * @param {number} interval 时间间隔（毫秒）
 * @returns {Function} 限制后的函数
 * @example
 * // 限制API调用每分钟最多100次
 * const limitedFetch = rateLimit(fetchData, 100, 60000);
 */
export const rateLimit = <T extends (...args: any[]) => any>(
  fn: T,
  limit: number,
  interval: number
): ((...args: Parameters<T>) => Promise<ReturnType<T>>) => {
  const queue: {
    args: Parameters<T>
    resolve: (value: ReturnType<T>) => void
    reject: (reason?: any) => void
  }[] = []

  let executing = false
  let executedCount = 0
  let intervalStart = Date.now()

  const executeQueue = async () => {
    if (executing || queue.length === 0) return

    executing = true

    while (queue.length > 0) {
      const now = Date.now()

      // 检查是否需要重置计数器
      if (now - intervalStart >= interval) {
        executedCount = 0
        intervalStart = now
      }

      // 如果达到限制，等待剩余时间后再继续
      if (executedCount >= limit) {
        const waitTime = interval - (now - intervalStart)
        await new Promise((resolve) => setTimeout(resolve, waitTime))
        continue
      }

      // 执行队列中的下一个任务
      const { args, resolve, reject } = queue.shift()!

      try {
        const result = await fn(...args)
        resolve(result)
      } catch (error) {
        reject(error)
      } finally {
        executedCount++
      }
    }

    executing = false
  }

  return (...args: Parameters<T>): Promise<ReturnType<T>> => {
    return new Promise((resolve, reject) => {
      queue.push({ args, resolve, reject })
      executeQueue()
    })
  }
}

// ==================== URL 工具 ====================

/**
 * 预签名URL的常见参数特征
 * 用于判断一个URL是否为预签名URL（私有库文件访问）
 */
const PRESIGNED_URL_PARAMS = [
  // AWS S3
  'X-Amz-Signature',
  'X-Amz-Credential',
  'X-Amz-Algorithm',
  // 阿里云 OSS
  'OSSAccessKeyId',
  'Signature',
  // 腾讯云 COS
  'q-sign-algorithm',
  'q-ak',
  'q-signature',
  // 华为云 OBS
  'X-Obs-Signature',
  'AccessKeyId',
  // MinIO (兼容S3)
  'X-Amz-Date',
  // 七牛云
  'e', // 过期时间戳
  'token'
]

/**
 * 判断URL是否为预签名URL（私有库文件访问）
 * 预签名URL带有签名参数，不能添加额外的时间戳参数
 *
 * @param url 要判断的URL
 * @returns 是否为预签名URL
 * @example
 * isPresignedUrl('https://bucket.s3.amazonaws.com/file.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=xxx')
 * // => true
 *
 * isPresignedUrl('https://cdn.example.com/public/image.jpg')
 * // => false
 */
export const isPresignedUrl = (url: string): boolean => {
  if (!url) return false

  try {
    const urlObj = new URL(url, window.location.origin)
    const searchParams = urlObj.searchParams

    // 检查URL参数是否包含预签名特征
    for (const param of PRESIGNED_URL_PARAMS) {
      if (searchParams.has(param)) {
        return true
      }
    }

    return false
  } catch {
    return false
  }
}

/**
 * 为公开库URL添加缓存破坏参数（时间戳）
 * 用于解决浏览器缓存导致图片替换后预览不更新的问题
 *
 * @param url 原始URL
 * @param timestamp 时间戳（可选），不传则使用当前时间
 * @returns 添加了时间戳的URL
 * @example
 * // 使用当前时间戳
 * addCacheBuster('https://cdn.example.com/image.jpg')
 * // => 'https://cdn.example.com/image.jpg?_t=1702950000000'
 *
 * // 使用指定时间戳（如文件更新时间）
 * addCacheBuster('https://cdn.example.com/image.jpg', '2024-01-01 12:00:00')
 * // => 'https://cdn.example.com/image.jpg?_t=1704067200000'
 */
export const addCacheBuster = (url: string, timestamp?: string | number | Date): string => {
  if (!url) return url

  // 预签名URL不添加时间戳，避免破坏签名
  if (isPresignedUrl(url)) {
    return url
  }

  try {
    const urlObj = new URL(url, window.location.origin)

    // 计算时间戳
    let ts: number
    if (timestamp === undefined || timestamp === null) {
      ts = Date.now()
    } else if (timestamp instanceof Date) {
      ts = timestamp.getTime()
    } else if (typeof timestamp === 'number') {
      ts = timestamp
    } else {
      // 字符串时间，尝试解析
      const parsed = new Date(timestamp).getTime()
      ts = isNaN(parsed) ? Date.now() : parsed
    }

    // 添加或更新时间戳参数
    urlObj.searchParams.set('_t', String(ts))

    return urlObj.toString()
  } catch {
    // URL解析失败，使用简单的字符串拼接
    const separator = url.includes('?') ? '&' : '?'
    const ts = timestamp ? new Date(timestamp).getTime() : Date.now()
    return `${url}${separator}_t=${ts}`
  }
}

// ==================== 图表工具 ====================

/**
 * 触发图表 resize (防抖版本)
 *
 * 在 keep-alive 缓存场景下,多个图表组件被激活时,
 * 统一使用此函数触发 resize,避免重复触发造成性能问题
 *
 * @example
 * onActivated(() => {
 *   triggerChartResize()
 * })
 */
export const triggerChartResize = (() => {
  const trigger = debounce(() => {
    window.dispatchEvent(new Event('resize'))
  }, 100)

  return (): void => {
    requestAnimationFrame(() => {
      trigger()
    })
  }
})()
