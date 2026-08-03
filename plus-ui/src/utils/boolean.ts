/**
 * 布尔值处理工具类
 *
 * 包含以下功能类别：
 * - 布尔判断：多格式真假值检查，支持字符串、数字、布尔值、null/undefined等各种输入
 * (isTrue, isFalse)
 * - 类型转换：统一转换为标准布尔格式，输出 '1'/'0' 字符串或 boolean 类型
 * (toBool, toBoolString)
 * - 状态切换：布尔状态切换，支持多种输入格式，输出标准 '1'/'0' 字符串
 * (toggleStatus)
 * 支持的真值格式：'1', 'true', 'TRUE', 'True', 'yes', 'YES', 'Yes', 'on', 'ON', 'On', true, 1
 * 支持的假值格式：'0', 'false', 'FALSE', 'False', 'no', 'NO', 'No', 'off', 'OFF', 'Off', false, 0, null, undefined, ''
 *
 * 使用示例：
 * ```typescript
 * // 多格式布尔判断
 * isTrue('1')        // true
 * isTrue('yes')      // true
 * isTrue(true)       // true
 * isFalse(null)      // true
 *
 * // 类型转换
 * toBoolString('yes')    // '1'
 * toBool('false')        // false
 *
 * // 状态切换
 * toggleStatus('true')   // '0'
 * toggleStatus(false)    // '1'
 * ```
 */
// ==================== 布尔值处理工具函数 ====================

/**
 * 检查值是否为真值
 * 支持多种真值表示形式：'1', 'true', 'yes', 'on', true, 1 等
 *
 * @param {any} value 要检查的值
 * @returns {boolean} 如果是真值则返回true，否则返回false
 * @example
 * // 字符串形式
 * isTrue('1')        // true
 * isTrue('true')     // true
 * isTrue('TRUE')     // true
 * isTrue('yes')      // true
 * isTrue('on')       // true
 *
 * // 布尔值和数字
 * isTrue(true)       // true
 * isTrue(1)          // true
 *
 * // 假值
 * isTrue('0')        // false
 * isTrue('false')    // false
 * isTrue(false)      // false
 * isTrue(null)       // false
 * isTrue(undefined)  // false
 * isTrue('')         // false
 */
export const isTrue = (value: any): boolean => {
  // 处理 null 和 undefined
  if (value === null || value === undefined) {
    return false
  }

  // 处理布尔值
  if (typeof value === 'boolean') {
    return value
  }

  // 处理数字
  if (typeof value === 'number') {
    return value === 1
  }

  // 处理字符串（转换为小写进行比较）
  if (typeof value === 'string') {
    const lowerValue = value.toLowerCase().trim()
    return ['1', 'true', 'yes', 'on'].includes(lowerValue)
  }

  return false
}

/**
 * 检查值是否为假值
 * 支持多种假值表示形式：'0', 'false', 'no', 'off', false, 0, null, undefined, '' 等
 *
 * @param {any} value 要检查的值
 * @returns {boolean} 如果是假值则返回true，否则返回false
 * @example
 * // 字符串形式
 * isFalse('0')        // true
 * isFalse('false')    // true
 * isFalse('FALSE')    // true
 * isFalse('no')       // true
 * isFalse('off')      // true
 *
 * // 布尔值和数字
 * isFalse(false)      // true
 * isFalse(0)          // true
 *
 * // null/undefined/空字符串
 * isFalse(null)       // true
 * isFalse(undefined)  // true
 * isFalse('')         // true
 *
 * // 真值
 * isFalse('1')        // false
 * isFalse(true)       // false
 */
export const isFalse = (value: any): boolean => {
  // 处理 null、undefined 和空字符串
  if (value === null || value === undefined || value === '') {
    return true
  }

  // 处理布尔值
  if (typeof value === 'boolean') {
    return !value
  }

  // 处理数字
  if (typeof value === 'number') {
    return value === 0
  }

  // 处理字符串（转换为小写进行比较）
  if (typeof value === 'string') {
    const lowerValue = value.toLowerCase().trim()
    return ['0', 'false', 'no', 'off'].includes(lowerValue)
  }

  return false
}

/**
 * 将各种形式的布尔值转换为标准的 '1' 或 '0' 字符串
 *
 * @param {any} value 要转换的值
 * @returns {string} '1' 表示真值，'0' 表示假值
 * @example
 * // 各种真值转换
 * toBoolString(true)      // '1'
 * toBoolString('true')    // '1'
 * toBoolString('yes')     // '1'
 * toBoolString(1)         // '1'
 *
 * // 各种假值转换
 * toBoolString(false)     // '0'
 * toBoolString('false')   // '0'
 * toBoolString('no')      // '0'
 * toBoolString(0)         // '0'
 * toBoolString(null)      // '0'
 * toBoolString(undefined) // '0'
 */
export const toBoolString = (value: any): string => {
  return isTrue(value) ? '1' : '0'
}

/**
 * 将各种形式的布尔值转换为标准的 boolean 类型
 *
 * @param {any} value 要转换的值
 * @returns {boolean} true 或 false
 * @example
 * // 字符串转布尔
 * toBool('1')        // true
 * toBool('true')     // true
 * toBool('0')        // false
 * toBool('false')    // false
 *
 * // 数字转布尔
 * toBool(1)          // true
 * toBool(0)          // false
 *
 * // null/undefined转布尔
 * toBool(null)       // false
 * toBool(undefined)  // false
 */
export const toBool = (value: any): boolean => {
  return isTrue(value)
}

/**
 * 切换布尔值状态（支持多种输入格式，输出标准的 '1'/'0' 字符串）
 *
 * @param {any} value 当前值
 * @returns {string} 切换后的状态字符串 ('1' 或 '0')
 * @example
 * // 字符串切换
 * toggleStatus('1')      // '0'
 * toggleStatus('true')   // '0'
 * toggleStatus('0')      // '1'
 * toggleStatus('false')  // '1'
 *
 * // 布尔值切换
 * toggleStatus(true)     // '0'
 * toggleStatus(false)    // '1'
 *
 * // null/undefined切换
 * toggleStatus(null)     // '1'
 * toggleStatus(undefined)// '1'
 */
export const toggleStatus = (value: any): string => {
  return isTrue(value) ? '0' : '1'
}
