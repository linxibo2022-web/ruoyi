/**
 * 对象工具函数集合
 *
 * 包含以下功能类别:
 * - 对象基本操作: 提供检查、比较、合并、克隆等基础功能 (isEmptyObject, shallowEqual, objectMerge, deepClone)
 * - 对象属性访问: 安全获取和设置嵌套属性 (getPropertyByPath, get, set)
 * - 对象属性筛选: 选取、排除或清理对象属性 (pick, omit, removeEmpty)
 * - 数组操作: 清理、去重、分组等数组工具函数 (cleanArray, uniqueArr, groupBy)
 * - URL与查询字符串: 查询字符串和对象互相转换 (queryToObject, objectToQuery)
 * - 对象键名转换: 对象属性名称格式转换 (camelizeKeys, snakeizeKeys)
 */

// ==================== 对象基本操作 ====================

/**
 * 检查对象是否为空（无属性）
 * @param obj 要检查的对象
 * @returns {boolean} 如果对象为空则返回true
 * @example
 * // 返回 true
 * isEmptyObject({})
 *
 * // 返回 false
 * isEmptyObject({ a: 1 })
 *
 * // 返回 true
 * isEmptyObject(null)
 */
export const isEmptyObject = (obj: Record<string, any>): boolean => {
  if (!obj || typeof obj !== 'object' || Array.isArray(obj)) {
    return true
  }
  return Object.keys(obj).length === 0
}

/**
 * 比较两个对象是否相等（浅比较）
 * 只比较对象的直接属性，不递归比较嵌套对象
 * @param obj1 第一个对象
 * @param obj2 第二个对象
 * @returns {boolean} 如果对象相等则返回true
 * @example
 * // 返回 true
 * shallowEqual({ a: 1, b: 2 }, { a: 1, b: 2 })
 *
 * // 返回 false
 * shallowEqual({ a: 1, b: { c: 2 } }, { a: 1, b: { c: 2 } })
 */
export const shallowEqual = (obj1: any, obj2: any): boolean => {
  if (obj1 === obj2) {
    return true
  }

  if (typeof obj1 !== 'object' || typeof obj2 !== 'object' || obj1 === null || obj2 === null) {
    return false
  }

  const keys1 = Object.keys(obj1)
  const keys2 = Object.keys(obj2)

  if (keys1.length !== keys2.length) {
    return false
  }

  for (const key of keys1) {
    if (obj1[key] !== obj2[key]) {
      return false
    }
  }

  return true
}

/**
 * 合并两个对象，后者优先
 * 会深度合并对象的属性
 * @param {Object} target 目标对象
 * @param {Object|Array} source 源对象或数组
 * @returns {Object} 合并后的对象
 * @example
 * // 返回 { a: 1, b: { c: 3, d: 4 } }
 * objectMerge({ a: 1, b: { c: 2 } }, { b: { d: 4, c: 3 } })
 */
export const objectMerge = <T>(target: T, source: any): T => {
  if (typeof target !== 'object') {
    target = {} as T
  }
  if (Array.isArray(source)) {
    return source.slice() as unknown as T
  }
  Object.keys(source).forEach((property) => {
    const sourceProperty = source[property]
    if (typeof sourceProperty === 'object' && sourceProperty !== null) {
      target[property as keyof T] = objectMerge(target[property as keyof T] || ({} as any), sourceProperty)
    } else {
      target[property as keyof T] = sourceProperty
    }
  })
  return target
}

/**
 * 深拷贝对象
 * 注意：这是一个简单版本的深拷贝，有许多边缘情况的bug
 * 如果需要使用完美的深拷贝，请使用lodash的_.cloneDeep
 * @param {Object} source 源对象
 * @returns {Object} 拷贝后的对象
 * @example
 * // 返回一个新对象 { a: 1, b: { c: 2 } }，与原对象不共享引用
 * const original = { a: 1, b: { c: 2 } };
 * const copy = deepClone(original);
 * copy.b.c = 3; // 不会影响 original.b.c
 */
export const deepClone = <T>(source: T): T => {
  if (!source || typeof source !== 'object') {
    throw new Error('deepClone: 参数错误')
  }

  if (Array.isArray(source)) {
    return source.map((item) => {
      return typeof item === 'object' && item !== null ? deepClone(item) : item
    }) as unknown as T
  }

  const targetObj = {} as T
  Object.keys(source as object).forEach((key) => {
    const value = source[key as keyof T]
    if (value && typeof value === 'object') {
      targetObj[key as keyof T] = deepClone(value)
    } else {
      targetObj[key as keyof T] = value
    }
  })

  return targetObj
}

// ==================== 对象属性访问 ====================

/**
 * 获取对象的指定属性路径的值
 * 支持通过点表示法访问嵌套属性
 * @param obj 源对象
 * @param path 属性路径，如 'user.profile.name'
 * @param defaultValue 如果路径不存在，返回的默认值
 * @returns 属性值或默认值
 * @example
 * // 返回 'John'
 * getPropertyByPath({ user: { profile: { name: 'John' } } }, 'user.profile.name')
 *
 * // 返回 'Unknown'
 * getPropertyByPath({ user: {} }, 'user.profile.name', 'Unknown')
 */
export const getPropertyByPath = (obj: Record<string, any>, path: string, defaultValue?: any): any => {
  if (!obj || !path) {
    return defaultValue
  }

  const keys = path.split('.')
  let current = obj

  for (const key of keys) {
    if (current === null || current === undefined || typeof current !== 'object') {
      return defaultValue
    }
    current = current[key]
  }

  return current === undefined ? defaultValue : current
}

/**
 * 安全地获取对象的嵌套属性，避免空指针异常
 * @param object 源对象
 * @param path 属性路径，支持数组索引，如 'users[0].name'
 * @param defaultValue 默认值，如果路径不存在则返回此值
 * @returns 属性值或默认值
 * @example
 * // 返回 'John'
 * get({ users: [{ name: 'John' }] }, 'users[0].name')
 *
 * // 返回 'Unknown'
 * get({ users: [] }, 'users[0].name', 'Unknown')
 *
 * // 也支持传入数组作为路径
 * // 返回 'John'
 * get({ users: [{ name: 'John' }] }, ['users', '0', 'name'])
 */
export const get = (object: any, path: string | string[], defaultValue?: any): any => {
  const keys = Array.isArray(path) ? path : path.replace(/\[(\d+)\]/g, '.$1').split('.')
  let result = object

  for (const key of keys) {
    result = result?.[key]
    if (result === undefined) return defaultValue
  }

  return result
}

/**
 * 设置对象的嵌套属性值，自动创建中间对象
 * @param object 源对象
 * @param path 属性路径，支持数组表示法，如 'users[0].name'
 * @param value 要设置的值
 * @returns 修改后的对象
 * @example
 * // 返回 { users: [{ name: 'John' }] }
 * set({}, 'users[0].name', 'John')
 *
 * // 也支持传入数组作为路径
 * // 返回 { users: [{ name: 'John' }] }
 * set({}, ['users', '0', 'name'], 'John')
 */
export const set = <T extends Record<string, any>>(object: T, path: string | string[], value: any): T => {
  if (!object || typeof object !== 'object') return object

  const keys = Array.isArray(path) ? path : path.replace(/\[(\d+)\]/g, '.$1').split('.')
  let current: any = object

  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i]
    // 如果路径中的当前部分是数组索引
    if (/^\d+$/.test(key) && Array.isArray(current)) {
      const index = parseInt(key)
      current[index] = current[index] || (/^\d+$/.test(keys[i + 1]) ? [] : {})
      current = current[index]
    } else {
      // 创建路径中不存在的对象
      current[key] = current[key] || (/^\d+$/.test(keys[i + 1]) ? [] : {})
      current = current[key]
    }
  }

  // 设置最终值
  const lastKey = keys[keys.length - 1]
  current[lastKey] = value

  return object
}

// ==================== 对象属性筛选 ====================

/**
 * 从对象中拾取指定属性
 * @param obj 源对象
 * @param keys 要拾取的属性数组
 * @returns 只包含指定属性的新对象
 * @example
 * // 返回 { a: 1, c: 3 }
 * pick({ a: 1, b: 2, c: 3, d: 4 }, ['a', 'c'])
 */
export const pick = <T extends Record<string, any>, K extends keyof T>(obj: T, keys: K[]): Pick<T, K> => {
  const result = {} as Pick<T, K>

  for (const key of keys) {
    if (key in obj) {
      result[key] = obj[key]
    }
  }

  return result
}

/**
 * 从对象中省略指定属性
 * @param obj 源对象
 * @param keys 要省略的属性数组
 * @returns 不包含指定属性的新对象
 * @example
 * // 返回 { b: 2, d: 4 }
 * omit({ a: 1, b: 2, c: 3, d: 4 }, ['a', 'c'])
 */
export const omit = <T extends Record<string, any>, K extends keyof T>(obj: T, keys: K[]): Omit<T, K> => {
  const result = { ...obj }

  for (const key of keys) {
    delete result[key]
  }

  return result as Omit<T, K>
}

/**
 * 移除对象中的空值属性（null、undefined、空字符串等）
 * @param object 要处理的对象
 * @param {Object} options 选项
 * @param {boolean} options.deep 是否深度清理，默认为false
 * @param {Array<any>} options.emptyValues 被视为空的值列表，默认为[null, undefined, '']
 * @returns 清理后的对象
 * @example
 * // 返回 { a: 1, c: 3 }
 * removeEmpty({ a: 1, b: null, c: 3, d: '' })
 *
 * // 深度清理
 * // 返回 { a: 1, b: { d: 4 } }
 * removeEmpty({ a: 1, b: { c: null, d: 4 }, e: '' }, { deep: true })
 *
 * // 自定义空值
 * // 返回 { a: 1 }
 * removeEmpty({ a: 1, b: 0, c: false }, { emptyValues: [0, false] })
 */
export const removeEmpty = <T extends Record<string, any>>(
  object: T,
  options: {
    deep?: boolean
    emptyValues?: any[]
  } = {}
): T => {
  const { deep = false, emptyValues = [null, undefined, ''] } = options
  if (!object || typeof object !== 'object') return object

  const result = { ...object }
  for (const key in result) {
    const value = result[key]
    // 检查是否为空值
    if (emptyValues.includes(value)) {
      delete result[key]
    }
    // 深度处理嵌套对象
    else if (deep && typeof value === 'object' && value !== null) {
      if (Array.isArray(value)) {
        result[key] = value.map((item) => (typeof item === 'object' && item !== null ? removeEmpty(item, options) : item))
      } else {
        result[key] = removeEmpty(value, options)
        // 如果处理后对象为空，移除该属性
        if (isEmptyObject(result[key])) {
          delete result[key]
        }
      }
    }
  }

  return result
}

// ==================== 数组操作 ====================

/**
 * 清理数组中的空值
 * @param {Array} actual 原始数组
 * @returns {Array} 清理后的数组
 * @example
 * // 返回 [1, 2, 3]
 * cleanArray([0, 1, false, 2, '', 3, null, undefined])
 */
export const cleanArray = <T>(actual: T[]): T[] => {
  return actual.filter((item) => !!item)
}

/**
 * 数组去重
 * @param {Array} arr 原始数组
 * @returns {Array} 去重后的数组
 * @example
 * // 返回 [1, 2, 3, 4]
 * uniqueArr([1, 2, 2, 3, 3, 4])
 */
export const uniqueArr = <T>(arr: T[]): T[] => {
  return Array.from(new Set(arr))
}

/**
 * 将对象数组按指定键值分组
 * @param array 对象数组
 * @param key 分组依据的属性名或函数
 * @returns 分组后的对象
 * @example
 * // 按属性分组
 * // 返回 { active: [{id: 1, status: 'active'}, {id: 3, status: 'active'}], inactive: [{id: 2, status: 'inactive'}] }
 * groupBy(
 *   [{id: 1, status: 'active'}, {id: 2, status: 'inactive'}, {id: 3, status: 'active'}],
 *   'status'
 * )
 *
 * // 使用函数分组
 * // 返回 { even: [{id: 2}], odd: [{id: 1}, {id: 3}] }
 * groupBy(
 *   [{id: 1}, {id: 2}, {id: 3}],
 *   item => item.id % 2 === 0 ? 'even' : 'odd'
 * )
 */
export const groupBy = <T extends Record<string, any>>(array: T[], key: keyof T | ((item: T) => string)): Record<string, T[]> => {
  const result: Record<string, T[]> = {}

  array.forEach((item) => {
    const groupKey = typeof key === 'function' ? key(item) : String(item[key])
    result[groupKey] = result[groupKey] || []
    result[groupKey].push(item)
  })

  return result
}

// ==================== URL与查询字符串 ====================

/**
 * 将URL查询参数字符串转换为对象
 * @param {string} url 包含查询参数的URL或查询参数字符串
 * @returns {Object} 解析后的参数对象
 * @example
 * // 返回 { name: 'John', age: '30' }
 * queryToObject('https://example.com?name=John&age=30')
 *
 * // 返回 { name: 'John', age: '30' }
 * queryToObject('name=John&age=30')
 */
export const queryToObject = (url: string): Record<string, string> => {
  // 处理完整URL或纯查询字符串
  const search = url.includes('?') ? decodeURIComponent(url.split('?')[1] || '') : decodeURIComponent(url)

  search.replace(/\+/g, ' ')

  if (!search) {
    return {}
  }

  const obj: Record<string, string> = {}
  const searchArr = search.split('&')

  searchArr.forEach((v) => {
    const index = v.indexOf('=')
    if (index !== -1) {
      const name = v.substring(0, index)
      const val = v.substring(index + 1, v.length)
      obj[name] = val
    }
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
    const part = encodeURIComponent(propName) + '='

    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object' && !Array.isArray(value)) {
        // 处理嵌套对象
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            const nestedParam = propName + '[' + key + ']'
            const subPart = encodeURIComponent(nestedParam) + '='
            result += subPart + encodeURIComponent(value[key]) + '&'
          }
        }
      } else {
        // 处理基本类型值和数组
        result += part + encodeURIComponent(value) + '&'
      }
    }
  }

  // 移除末尾的 & 字符（如果存在）
  return result.endsWith('&') ? result.slice(0, -1) : result
}

// ==================== 对象键名转换 ====================

/**
 * 对象键名驼峰转换（将下划线或中划线转为驼峰）
 * @param obj 源对象
 * @param options 配置选项
 * @returns 转换后的对象
 * @example
 * // 返回 { firstName: 'John', lastName: 'Doe' }
 * camelizeKeys({ first_name: 'John', last_name: 'Doe' })
 *
 * // 嵌套对象
 * // 返回 { user: { firstName: 'John', address: { streetName: 'Main' } } }
 * camelizeKeys({ user: { first_name: 'John', address: { street_name: 'Main' } } })
 *
 * // 排除特定键
 * // 返回 { firstName: 'John', last_name: 'Doe' }
 * camelizeKeys({ first_name: 'John', last_name: 'Doe' }, { exclude: ['last_name'] })
 */
export const camelizeKeys = (
  obj: Record<string, any>,
  options: {
    recursive?: boolean
    exclude?: string[]
  } = {}
): Record<string, any> => {
  const { recursive = true, exclude = [] } = options

  const camelizeStr = (str: string): string => {
    return str.replace(/[-_]([a-z])/g, (_, letter) => letter.toUpperCase())
  }

  // 如果不是对象或是null，直接返回
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  // 处理数组
  if (Array.isArray(obj)) {
    return recursive ? obj.map((item) => camelizeKeys(item, options)) : obj
  }

  const result: Record<string, any> = {}

  // 处理对象的每个键
  Object.keys(obj).forEach((key) => {
    const value = obj[key]

    // 如果键在排除列表中，保持原样
    const newKey = exclude.includes(key) ? key : camelizeStr(key)

    // 如果启用递归且值是对象，递归处理它
    result[newKey] = recursive && typeof value === 'object' && value !== null ? camelizeKeys(value, options) : value
  })

  return result
}

/**
 * 对象键名蛇形转换（将驼峰转为下划线）
 * @param obj 源对象
 * @param options 配置选项
 * @returns 转换后的对象
 * @example
 * // 返回 { first_name: 'John', last_name: 'Doe' }
 * snakeizeKeys({ firstName: 'John', lastName: 'Doe' })
 *
 * // 嵌套对象
 * // 返回 { user: { first_name: 'John', address: { street_name: 'Main' } } }
 * snakeizeKeys({ user: { firstName: 'John', address: { streetName: 'Main' } } })
 *
 * // 排除特定键
 * // 返回 { first_name: 'John', lastName: 'Doe' }
 * snakeizeKeys({ firstName: 'John', lastName: 'Doe' }, { exclude: ['lastName'] })
 */
export const snakeizeKeys = (
  obj: Record<string, any>,
  options: {
    recursive?: boolean
    exclude?: string[]
  } = {}
): Record<string, any> => {
  const { recursive = true, exclude = [] } = options

  const snakeizeStr = (str: string): string => {
    return str.replace(/([A-Z])/g, (_, letter) => `_${letter.toLowerCase()}`)
  }

  // 如果不是对象或是null，直接返回
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  // 处理数组
  if (Array.isArray(obj)) {
    return recursive ? obj.map((item) => snakeizeKeys(item, options)) : obj
  }

  const result: Record<string, any> = {}

  // 处理对象的每个键
  Object.keys(obj).forEach((key) => {
    const value = obj[key]

    // 如果键在排除列表中，保持原样
    const newKey = exclude.includes(key) ? key : snakeizeStr(key)

    // 如果启用递归且值是对象，递归处理它
    result[newKey] = recursive && typeof value === 'object' && value !== null ? snakeizeKeys(value, options) : value
  })

  return result
}
