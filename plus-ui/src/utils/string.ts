/**
 * 字符串工具类
 *
 * 包含以下功能类别：
 * - 基本字符串操作：转换空值、检查空字符串、首字母大写、截断字符串、计算字节长度、创建唯一标识
 * (parseStrEmpty, isEmpty, capitalize, truncate, byteLength, createUniqueString)
 * - 字符串格式化与替换：类printf格式化 (sprintf)
 * - HTML处理：HTML转纯文本、获取文本摘要、转义特殊字符 (html2Text, getTextExcerpt, escapeHtml)
 * - URL处理：外部链接判断、HTTP链接判断、查询参数解析与构建 (isExternal, isHttp, getQueryObject, objectToQuery)
 * - 路径处理：路径标准化、路径匹配 (normalizePath, isPathMatch)
 * - 格式转换：驼峰命名与短横线命名互转 (camelToKebab, kebabToCamel)
 * - 验证函数：JSON格式验证 (isValidJSON)
 */

// ==================== 基本字符串操作 ====================

/**
 * 转换可能为undefined或null的字符串为空字符串
 *
 * @param {any} str 要转换的值
 * @returns {string} 转换后的字符串，undefined或null会被转为空字符串
 * @example
 * // 返回 ""
 * parseStrEmpty(null)
 * // 返回 "hello"
 * parseStrEmpty('hello')
 */
export const parseStrEmpty = (str: any): string => {
  if (str === undefined || str === null || str === 'undefined' || str === 'null') {
    return ''
  }
  return String(str)
}

/**
 * 检查字符串是否为空
 * 检查 null、undefined、空字符串和仅包含空格的字符串
 *
 * @param {string} str 要检查的字符串
 * @returns {boolean} 如果字符串为空则返回true
 * @example
 * // 返回 true
 * isEmpty(null)
 *
 * // 返回 true
 * isEmpty('')
 *
 * // 返回 true
 * isEmpty('   ')
 *
 * // 返回 false
 * isEmpty('hello')
 */
export const isEmpty = (str: any): boolean => {
  return str === null || str === undefined || String(str).trim() === ''
}

/**
 * 首字母大写
 *
 * @param {string} str 输入字符串
 * @returns {string} 首字母大写后的字符串
 * @example
 * // 返回 "Hello"
 * capitalize("hello")
 *
 * // 返回 "World"
 * capitalize("world")
 *
 * // 返回 ""
 * capitalize("")
 */
export const capitalize = (str: string): string => {
  if (!str || str.length === 0) return ''
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * 截断字符串到指定长度，并添加省略号
 *
 * @param {string} str 原始字符串
 * @param {number} maxLength 最大长度
 * @param {string} ellipsis 省略号字符，默认为 '...'
 * @returns {string} 截断后的字符串
 * @example
 * // 返回 "Hello..."
 * truncate("Hello world", 8)
 *
 * // 返回 "Hello world"
 * truncate("Hello world", 20)
 *
 * // 返回 "Hello world…"
 * truncate("Hello world", 12, "…")
 */
export const truncate = (str: string, maxLength: number, ellipsis: string = '...'): string => {
  if (!str) return ''
  if (str.length <= maxLength) return str

  return str.substr(0, maxLength - ellipsis.length) + ellipsis
}

/**
 * 计算字符串的字节长度
 * 支持UTF-8编码：
 * - 单字节字符(ASCII等): 计为1个字节
 * - 双字节字符(如欧洲语系字符): 计为2个字节
 * - 三字节字符(如中日韩文字): 计为3个字节
 * - 四字节字符(如Emoji): 计为4个字节
 *
 * @param {string} str 要计算的字符串
 * @returns {number} 字节长度
 * @example
 * // 返回 5 (ASCII字符各占1字节)
 * byteLength('hello')
 *
 * // 返回 6 (中文字符在UTF-8中各占3字节)
 * byteLength('你好')
 *
 * // 返回 4 (Emoji通常占4字节)
 * byteLength('😊')
 */
export const byteLength = (str: string): number => {
  if (!str) return 0

  let byteSize = 0
  for (let i = 0; i < str.length; i++) {
    const code = str.charCodeAt(i)
    if (code <= 0x7F) {
      // 单字节字符 (0-127)
      byteSize += 1
    } else if (code <= 0x7FF) {
      // 双字节字符 (128-2047)
      byteSize += 2
    } else if (code >= 0xD800 && code <= 0xDFFF) {
      // 处理代理对 (用于表示Unicode中的辅助平面字符)
      // 占4个字节，需要跳过下一个代码单元
      byteSize += 4
      i++
    } else {
      // 三字节字符 (2048-65535)
      byteSize += 3
    }
  }
  return byteSize
}

/**
 * 创建唯一字符串标识
 * 基于当前时间戳和随机数生成一个唯一标识符
 *
 * @returns {string} 生成的唯一字符串
 * @example
 * // 返回类似 "a1b21615528749883"（16进制随机数 + 时间戳）
 * createUniqueString()
 *
 * // 多次调用生成不同的值
 * const id1 = createUniqueString();
 * const id2 = createUniqueString();
 * // id1 !== id2
 */
export const createUniqueString = (): string => {
  const timestamp = Date.now().toString()
  const randomNum = Math.floor((1 + Math.random()) * 0x10000)
    .toString(16)
    .substring(1)
  return `${randomNum}${timestamp}`
}

// ==================== 字符串格式化与替换 ====================

/**
 * 格式化字符串 (类似于printf的%s格式)
 * 将字符串中的%s替换为提供的参数
 *
 * @param {string} str 包含%s占位符的模板字符串
 * @param {...any} args 要插入的参数
 * @returns {string} 格式化后的字符串
 * @example
 * // 返回 "Hello, User! Your score is 95"
 * sprintf("Hello, %s! Your score is %s", "User", 95)
 *
 * // 返回 "File not found" (忽略额外的参数)
 * sprintf("File not found", "extra")
 *
 * // 返回 "Welcome" (占位符多于提供的参数时，多余占位符被替换为空字符串)
 * sprintf("Welcome %s", undefined)
 */
export const sprintf = (str: string, ...args: any[]): string => {
  if (!str) return ''
  if (args.length === 0) return str

  let index = 0
  return str.replace(/%s/g, () => {
    const replacement = args[index]
    index++
    if (replacement === undefined) {
      return ''
    }
    return String(replacement)
  })
}

// ==================== HTML处理 ====================

/**
 * 将HTML内容转换为纯文本
 * 通过创建临时DOM元素并提取其文本内容实现
 *
 * @param {string} html HTML字符串
 * @returns {string} 提取的纯文本
 * @example
 * // 返回 "Hello World"
 * html2Text('<div><h1>Hello</h1> <b>World</b></div>')
 *
 * // 返回 "Click here"
 * html2Text('<a href="https://example.com">Click here</a>')
 */
export const html2Text = (html: string): string => {
  if (!html) return ''

  const div = document.createElement('div')
  div.innerHTML = html
  return div.textContent || div.textContent || ''
}

/**
 * 从HTML字符串中获取纯文本内容的摘要
 *
 * @param {string} html HTML字符串
 * @param {number} length 摘要长度
 * @param {string} ellipsis 省略号字符，默认为 '...'
 * @returns {string} 文本摘要
 * @example
 * // 返回 "This is a paragraph..."
 * getTextExcerpt('<p>This is a paragraph with <b>bold</b> text and a <a href="#">link</a>.</p>', 20)
 */
export const getTextExcerpt = (html: string, length: number, ellipsis: string = '...'): string => {
  const text = html2Text(html)
  return truncate(text, length, ellipsis)
}

/**
 * 转义特殊字符，防止XSS攻击
 *
 * @param {string} html 需要转义的字符串
 * @returns {string} 转义后的字符串
 * @example
 * // 返回 "&lt;script&gt;alert(&quot;XSS&quot;)&lt;/script&gt;"
 * escapeHtml('<script>alert("XSS")</script>')
 */
export const escapeHtml = (html: string): string => {
  if (!html) return ''
  return html
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

// ==================== URL处理 ====================

/**
 * 判断URL是否为外部链接
 * 检查URL是否以http://, https://, mailto: 或 tel: 开头
 *
 * @param {string} path 要检查的URL
 * @returns {boolean} 如果是外部链接则返回true
 * @example
 * // 返回 true
 * isExternal('https://example.com')
 * // 返回 false
 * isExternal('/internal/path')
 */
export const isExternal = (path: string): boolean => {
  return /^(?:https?:|mailto:|tel:)/.test(path)
}

/**
 * 判断URL是否是HTTP或HTTPS链接
 *
 * @param {string} url 要检查的URL
 * @returns {boolean} 如果是HTTP或HTTPS链接则返回true
 * @example
 * // 返回 true
 * isHttp('http://example.com')
 * // 返回 false
 * isHttp('/internal/path')
 */
export const isHttp = (url: string): boolean => {
  return url.startsWith('http://') || url.startsWith('https://')
}

/**
 * 从URL中解析查询参数
 *
 * @param {string} url URL字符串
 * @returns {object} 包含查询参数的对象
 * @example
 * // 返回 { name: 'John', age: '30' }
 * getQueryObject('https://example.com?name=John&age=30')

 * // 返回 { q: 'test search', page: '1' }
 * getQueryObject('https://example.com/search?q=test%20search&page=1')
 */
export const getQueryObject = (url: string): Record<string, string> => {
  url = url || window.location.href
  const search = url.substring(url.lastIndexOf('?') + 1)
  const obj: Record<string, string> = {}
  const reg = /([^?&=]+)=([^?&=]*)/g

  search.replace(reg, (_, key, value) => {
    const decodedKey = decodeURIComponent(key)
    obj[decodedKey] = decodeURIComponent(value)
    return ''
  })

  return obj
}

/**
 * 将参数对象转换为URL查询字符串
 * 支持嵌套对象，例如 {a: {b: 1}} 转换为 a[b]=1
 *
 * @param {Record<string, any>} params 参数对象
 * @returns {string} 生成的查询字符串 (不含前缀?)
 * @example
 * // 返回 "name=test&age=25"
 * objectToQuery({name: 'test', age: 25})
 *
 * // 返回 "name=test&filter[status]=1&filter[type]=2"
 * objectToQuery({name: 'test', filter: {status: 1, type: 2}})
 */
export const objectToQuery = (params: Record<string, any>): string => {
  if (!params) return ''

  let result = ''

  for (const propName of Object.keys(params)) {
    const value = params[propName]
    const part = `${encodeURIComponent(propName)}=`

    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object' && !Array.isArray(value)) {
        // 处理嵌套对象
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            const nestedParam = `${propName}[${key}]`
            const subPart = `${encodeURIComponent(nestedParam)}=`
            result += `${subPart + encodeURIComponent(value[key])}&`
          }
        }
      } else {
        // 处理基本类型值和数组
        result += `${part + encodeURIComponent(value)}&`
      }
    }
  }

  // 移除末尾的 & 字符（如果存在）
  return result.endsWith('&') ? result.slice(0, -1) : result
}

// ==================== 路径处理 ====================

/**
 * 处理路径，标准化路径格式
 * - 将连续的斜杠替换为单个斜杠
 * - 移除末尾的斜杠
 *
 * @param {string} path 要处理的路径
 * @returns {string} 标准化后的路径
 * @example
 * // 返回 "/api/users"
 * normalizePath('/api//users/')
 *
 * // 返回 "/api/users"
 * normalizePath('/api/users//////')
 */
export const normalizePath = (path: string): string => {
  if (!path || path === 'undefined') {
    return ''
  }

  // 将多个连续斜杠替换为单个斜杠
  let normalized = path.replace(/\/+/g, '/')

  // 移除末尾的斜杠
  if (normalized.endsWith('/')) {
    normalized = normalized.slice(0, -1)
  }

  return normalized
}

/**
 * 路径匹配器
 * 支持通配符 * (匹配单一段) 和 ** (匹配多段)
 *
 * @param {string} pattern 匹配模式，例如 "/api/*" 或 "/api/**"
 * @param {string} path 要检查的路径
 * @returns {boolean} 如果路径匹配模式则返回true
 * @example
 * // 返回 true
 * isPathMatch('/api/*', '/api/users')
 *
 * // 返回 false
 * isPathMatch('/api/*', '/api/users/details')
 *
 * // 返回 true (** 匹配多段路径)
 * isPathMatch('/api/**', '/api/users/details')
 *
 * // 返回 false
 * isPathMatch('/api/users/*', '/api/orders/123')
 */
export const isPathMatch = (pattern: string, path: string): boolean => {
  if (!pattern || !path) return false

  const regexPattern = pattern
    .replace(/\//g, '\\/')
    .replace(/\*\*/g, '__DOUBLE_STAR__')
    .replace(/\*/g, '[^\\/]*')
    .replace(/__DOUBLE_STAR__/g, '.*')

  const regex = new RegExp(`^${regexPattern}$`)
  return regex.test(path)
}

// ==================== 格式转换 ====================

/**
 * 将驼峰命名转换为短横线命名(kebab-case)
 * 例如：myVariableName -> my-variable-name
 *
 * @param {string} str 驼峰命名的字符串
 * @returns {string} 短横线命名的字符串
 * @example
 * // 返回 "my-variable-name"
 * camelToKebab("myVariableName")
 *
 * // 返回 "background-color"
 * camelToKebab("backgroundColor")
 */
export const camelToKebab = (str: string): string => {
  if (!str) return ''
  return str.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase()
}

/**
 * 将短横线命名(kebab-case)转换为驼峰命名
 * 例如：my-variable-name -> myVariableName
 *
 * @param {string} str 短横线命名的字符串
 * @returns {string} 驼峰命名的字符串
 * @example
 * // 返回 "myVariableName"
 * kebabToCamel("my-variable-name")
 *
 * // 返回 "backgroundColor"
 * kebabToCamel("background-color")
 */
export const kebabToCamel = (str: string): string => {
  if (!str) return ''
  return str.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase())
}

// ==================== 验证函数 ====================

/**
 * 检查字符串是否是有效的JSON
 *
 * @param {string} str 要检查的字符串
 * @returns {boolean} 如果是有效的JSON则返回true
 * @example
 * // 返回 true
 * isValidJSON('{"name":"John","age":30}')
 *
 * // 返回 true
 * isValidJSON('[1,2,3]')
 *
 * // 返回 false
 * isValidJSON('{name:"John"}')
 */
export const isValidJSON = (str: string): boolean => {
  if (!str || typeof str !== 'string') return false
  try {
    JSON.parse(str)
    return true
  } catch (e) {
    return false
  }
}
