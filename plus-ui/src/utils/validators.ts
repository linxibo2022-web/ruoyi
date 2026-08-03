/**
 * 通用验证工具函数集合
 *
 * 包含以下功能类别:
 * - 文件验证: 验证文件类型和数据格式 (isBlob, isAllowedFileType, isImageFile, isWithinFileSize)
 * - URL验证: 验证URL和路径格式 (isPathMatch, isHttp, isExternal, isValidURL, isDomain, isURLParam)
 * - 字符串验证: 验证字符串格式和内容 (isEmail, isLowerCase, isUpperCase, isAlphabets, isValidUserName, containsSubstring, onlyContains, isHexColor, isValidFilename)
 * - 类型检查: 验证数据类型 (isString, isArray, isObject, isEmptyObject, isValidJSON)
 * - 数值验证: 验证数值的有效性 (isNumber, isInteger, isPositiveNumber, isInRange)
 * - 日期验证: 验证日期格式和有效性 (isValidDate, isDateFormat, isBeforeDate, isAfterDate)
 * - 中国特定验证: 验证中国特定格式 (isChineseIdCard, isChinesePhoneNumber, isPostalCode)
 * - 表单验证: 常用表单验证 (isPassword, isRequired, hasMinLength, hasMaxLength, isName, isEqual, isOneOf)
 * - 网络标识验证: 验证网络地址和标识 (isIPAddress, isMACAddress, isPort, isUUID)
 * - 金融验证: 验证金融相关格式 (isBankCardNumber, isCreditCardNumber)
 * - 社交媒体验证: 验证社交媒体标识格式 (isSocialMediaUserName)
 * - 数组操作验证: 验证数组操作 (arrayContains)
 */

// ==================== 文件验证 ====================

/**
 * 验证是否为blob格式
 * @param data
 * @returns {boolean} 是否为非JSON的blob数据
 *
 * @example
 * // 检查接口返回的数据是否为blob
 * if (isBlob(res.data)) {
 *   // 处理blob数据，例如下载文件
 *   const blob = new Blob([res.data]);
 *   FileSaver.saveAs(blob, fileName);
 * }
 */
export const isBlob = (data: { type: string }): boolean => {
  return data?.type !== 'application/json'
}

/**
 * 验证文件类型是否在允许列表中
 * @param {File} file - 要验证的文件对象
 * @param {string[]} allowedTypes - 允许的文件类型扩展名数组，如 ['jpg', 'png', 'pdf']
 * @returns {boolean} 文件类型是否在允许列表中
 *
 * @example
 * // 检查上传的文件是否属于允许的类型
 * const file = event.target.files[0];
 * if (isAllowedFileType(file, ['jpg', 'png', 'gif'])) {
 *   // 处理有效的图片文件
 * } else {
 *   alert('只允许上传JPG, PNG或GIF格式的图片');
 * }
 */
export const isAllowedFileType = (file: File, allowedTypes: string[]): boolean => {
  if (!file || !file.name) return false
  const extension = file.name.split('.').pop()?.toLowerCase() || ''
  return allowedTypes.includes(extension)
}

/**
 * 验证是否为图片文件
 * @param {File} file - 要验证的文件对象
 * @returns {boolean} 是否为图片文件
 *
 * @example
 * // 检查上传的文件是否为图片
 * const file = event.target.files[0];
 * if (isImageFile(file)) {
 *   // 处理图片文件
 *   previewImage(file);
 * } else {
 *   alert('请选择图片文件');
 * }
 */
export const isImageFile = (file: File): boolean => {
  if (!file || !file.type) return false
  return file.type.startsWith('image/')
}

/**
 * 验证文件大小是否在限制范围内
 * @param {File} file - 要验证的文件对象
 * @param {number} maxSizeInMB - 最大允许大小，单位MB
 * @returns {boolean} 文件大小是否在限制范围内
 *
 * @example
 * // 检查上传的文件大小是否超过5MB
 * const file = event.target.files[0];
 * if (isWithinFileSize(file, 5)) {
 *   // 处理文件
 *   uploadFile(file);
 * } else {
 *   alert('文件大小不能超过5MB');
 * }
 */
export const isWithinFileSize = (file: File, maxSizeInMB: number): boolean => {
  if (!file) return false
  const fileSizeInMB = file.size / (1024 * 1024)
  return fileSizeInMB <= maxSizeInMB
}

// ==================== URL和路径验证 ====================

/**
 * 判断路径是否匹配特定模式
 * @param {string} pattern - 匹配模式，支持 * 和 ** 通配符
 * @param {string} path - 要验证的路径
 * @returns {boolean} 是否匹配
 *
 * @example
 * // 检查路径是否匹配API模式
 * if (isPathMatch('/api/*', '/api/users')) {
 *   console.log('路径匹配API模式');
 * }
 */
export const isPathMatch = (pattern: string, path: string): boolean => {
  const regexPattern = pattern
    .replace(/\//g, '\\/')
    .replace(/\*\*/g, '__DOUBLE_STAR__')
    .replace(/\*/g, '[^\\/]*')
    .replace(/__DOUBLE_STAR__/g, '.*')
  const regex = new RegExp(`^${regexPattern}$`)
  return regex.test(path)
}

/**
 * 判断URL是否是HTTP或HTTPS协议
 * @param {string} url - 要验证的URL
 * @returns {boolean} 是否是HTTP或HTTPS URL
 *
 * @example
 * // 检查URL是否使用HTTP或HTTPS协议
 * if (isHttp('https://example.com')) {
 *   console.log('URL使用HTTP或HTTPS协议');
 * }
 */
export const isHttp = (url: string): boolean => {
  return url.includes('http://') || url.includes('https://')
}

/**
 * 判断路径是否为外部链接
 * @param {string} path - 要验证的路径
 * @returns {boolean} 是否是外部链接
 *
 * @example
 * // 检查链接是否为外部链接
 * if (isExternal('https://external-site.com')) {
 *   // 在新窗口打开外部链接
 *   window.open(url, '_blank');
 * }
 */
export const isExternal = (path: string): boolean => {
  return /^(?:https?:|mailto:|tel:)/.test(path)
}

/**
 * 验证URL是否符合标准格式
 * @param {string} url - 要验证的URL
 * @returns {boolean} URL是否有效
 *
 * @example
 * // 验证用户输入的URL是否有效
 * if (isValidURL(userInputUrl)) {
 *   // 处理有效URL
 * } else {
 *   // 显示错误信息
 *   showError('请输入有效的URL地址');
 * }
 */
export const isValidURL = (url: string): boolean => {
  const reg =
    /^(?:https?|ftp):\/\/(?:[a-zA-Z0-9.-]+(?::[a-zA-Z0-9.&%$-]+)*@)*(?:(?:25[0-5]|2[0-4]\d|1\d{2}|[1-9]\d?)(?:\.(?:25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)){3}|(?:[a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(?:com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(?::\d+)*(?:\/(?:$|[\w.,?'\\+&%$#=~-]+))*$/
  return reg.test(url)
}

/**
 * 验证域名是否有效
 * @param {string} domain - 要验证的域名
 * @returns {boolean} 域名是否有效
 *
 * @example
 * // 验证用户输入的域名是否有效
 * if (isDomain('example.com')) {
 *   // 处理有效域名
 * } else {
 *   // 显示错误信息
 *   showError('请输入有效的域名');
 * }
 */
export const isDomain = (domain: string): boolean => {
  const reg = /^(?:[a-z0-9](?:[a-z0-9\-]{0,61}[a-z0-9])?\.)+[a-z]{2,}$/i
  return reg.test(domain)
}

// ==================== 字符串验证 ====================

/**
 * 验证字符串是否是合法的电子邮件地址
 * @param {string} email - 要验证的电子邮件地址
 * @returns {boolean} 是否是有效的电子邮件
 *
 * @example
 * // 验证用户输入的邮箱是否有效
 * if (isEmail('user@example.com')) {
 *   // 处理有效邮箱
 * } else {
 *   // 显示错误信息
 *   form.setError('email', { message: '请输入有效的邮箱地址' });
 * }
 */
export const isEmail = (email: string): boolean => {
  const reg =
    /^(?:[^<>()\]\\.,;:\s@"]+(?:\.[^<>()\]\\.,;:\s@"]+)*|".+")@(?:\[\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\]|(?:[a-z\-0-9]+\.)+[a-z]{2,})$/i
  return reg.test(email)
}

/**
 * 验证字符串是否全为小写字母
 * @param {string} str - 要验证的字符串
 * @returns {boolean} 是否全为小写字母
 *
 * @example
 * // 验证输入是否全为小写字母
 * if (isLowerCase('abcdef')) {
 *   console.log('输入全为小写字母');
 * }
 */
export const isLowerCase = (str: string): boolean => {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/**
 * 验证字符串是否全为大写字母
 * @param {string} str - 要验证的字符串
 * @returns {boolean} 是否全为大写字母
 *
 * @example
 * // 验证输入是否全为大写字母
 * if (isUpperCase('ABCDEF')) {
 *   console.log('输入全为大写字母');
 * }
 */
export const isUpperCase = (str: string): boolean => {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/**
 * 验证字符串是否全为字母（不包含数字和其他字符）
 * @param {string} str - 要验证的字符串
 * @returns {boolean} 是否全为字母
 *
 * @example
 * // 验证输入是否仅包含字母
 * if (isAlphabets('ABCdef')) {
 *   console.log('输入仅包含字母');
 * }
 */
export const isAlphabets = (str: string): boolean => {
  const reg = /^[A-Z]+$/i
  return reg.test(str)
}

/**
 * 验证用户名是否有效（仅示例用途）
 * @param {string} str - 要验证的用户名
 * @returns {boolean} 用户名是否有效
 *
 * @example
 * // 验证用户名是否有效
 * if (isValidUserName('admin')) {
 *   // 处理有效用户名
 *   allowLogin();
 * }
 */
export const isValidUserName = (str: string): boolean => {
  const valid_map = ['admin', 'editor']
  return valid_map.includes(str.trim())
}

// ==================== 类型检查 ====================

/**
 * 验证值是否为字符串类型
 * @param {any} value - 要验证的值
 * @returns {boolean} 是否为字符串
 *
 * @example
 * // 检查变量是否为字符串类型
 * if (isString(value)) {
 *   // 处理字符串类型
 *   return value.toUpperCase();
 * }
 */
export const isString = (value: any): boolean => {
  return typeof value === 'string' || typeof value === 'string'
}

/**
 * 验证值是否为数组
 * @param {any} value - 要验证的值
 * @returns {boolean} 是否为数组
 *
 * @example
 * // 检查变量是否为数组
 * if (isArray(value)) {
 *   // 处理数组类型
 *   return value.map(item => item * 2);
 * }
 */
export const isArray = (value: any): boolean => {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(value) === '[object Array]'
  }
  return Array.isArray(value)
}

/**
 * 验证值是否为纯对象（不包括数组、函数等）
 * @param {any} value - 要验证的值
 * @returns {boolean} 是否为纯对象
 *
 * @example
 * // 检查变量是否为纯对象
 * if (isObject(value)) {
 *   // 处理对象类型
 *   const keys = Object.keys(value);
 * }
 */
export const isObject = (value: any): boolean => {
  return Object.prototype.toString.call(value) === '[object Object]'
}

/**
 * 验证对象是否为空对象
 * @param {object} obj - 要验证的对象
 * @returns {boolean} 是否为空对象
 *
 * @example
 * // 检查对象是否为空
 * if (isEmptyObject(obj)) {
 *   console.log('对象是空的');
 * }
 */
export const isEmptyObject = (obj: object): boolean => {
  return isObject(obj) && Object.keys(obj).length === 0
}

/**
 * 验证字符串是否为有效的JSON格式
 * @param {string} str - 要验证的字符串
 * @returns {boolean} 是否为有效的JSON
 *
 * @example
 * // 检查字符串是否为有效的JSON
 * if (isValidJSON(jsonString)) {
 *   const data = JSON.parse(jsonString);
 * } else {
 *   console.error('无效的JSON格式');
 * }
 */
export const isValidJSON = (str: string): boolean => {
  if (!isString(str)) return false
  try {
    JSON.parse(str)
    return true
  } catch (e) {
    return false
  }
}

// ==================== 数值验证 ====================

/**
 * 验证值是否为有效数字
 * @param {any} value - 要验证的值
 * @returns {boolean} 是否为有效数字
 *
 * @example
 * // 检查输入是否为数字
 * if (isNumber(userInput)) {
 *   // 处理数字
 *   const result = Math.sqrt(userInput);
 * } else {
 *   alert('请输入有效的数字');
 * }
 */
export const isNumber = (value: any): boolean => {
  return typeof value === 'number' && !Number.isNaN(value) && Number.isFinite(value)
}

/**
 * 验证值是否为整数
 * @param {any} value - 要验证的值
 * @returns {boolean} 是否为整数
 *
 * @example
 * // 检查输入是否为整数
 * if (isInteger(userInput)) {
 *   // 处理整数
 * } else {
 *   alert('请输入整数');
 * }
 */
export const isInteger = (value: any): boolean => {
  return isNumber(value) && Number.isInteger(value)
}

/**
 * 验证值是否为正数
 * @param {any} value - 要验证的值
 * @returns {boolean} 是否为正数
 *
 * @example
 * // 检查输入是否为正数
 * if (isPositiveNumber(userInput)) {
 *   // 处理正数
 * } else {
 *   alert('请输入正数');
 * }
 */
export const isPositiveNumber = (value: any): boolean => {
  return isNumber(value) && value > 0
}

/**
 * 验证数值是否在指定范围内
 * @param {number} value - 要验证的数值
 * @param {number} min - 最小值
 * @param {number} max - 最大值
 * @returns {boolean} 数值是否在范围内
 *
 * @example
 * // 检查年龄是否在有效范围内
 * if (isInRange(age, 18, 60)) {
 *   // 处理有效年龄
 * } else {
 *   alert('年龄必须在18到60岁之间');
 * }
 */
export const isInRange = (value: number, min: number, max: number): boolean => {
  return isNumber(value) && value >= min && value <= max
}

// ==================== 日期验证 ====================

/**
 * 验证日期对象是否有效
 * @param {Date} date - 要验证的日期对象
 * @returns {boolean} 日期是否有效
 *
 * @example
 * // 检查日期对象是否有效
 * const date = new Date(userInput);
 * if (isValidDate(date)) {
 *   // 处理有效日期
 * } else {
 *   alert('请输入有效的日期');
 * }
 */
export const isValidDate = (date: Date): boolean => {
  return date instanceof Date && !Number.isNaN(date.getTime())
}

/**
 * 验证字符串是否符合指定的日期格式
 * @param {string} dateStr - 要验证的日期字符串
 * @param {string} format - 日期格式，如 'YYYY-MM-DD'
 * @returns {boolean} 日期字符串是否符合格式
 *
 * @example
 * // 检查日期字符串是否符合YYYY-MM-DD格式
 * if (isDateFormat('2023-01-01', 'YYYY-MM-DD')) {
 *   // 处理有效日期
 * } else {
 *   alert('请输入正确格式的日期(YYYY-MM-DD)');
 * }
 */
export const isDateFormat = (dateStr: string, format: string): boolean => {
  if (!isString(dateStr)) return false

  let regex
  switch (format.toUpperCase()) {
    case 'YYYY-MM-DD':
      regex = /^\d{4}-\d{2}-\d{2}$/
      break
    case 'MM/DD/YYYY':
      regex = /^\d{2}\/\d{2}\/\d{4}$/
      break
    case 'DD/MM/YYYY':
      regex = /^\d{2}\/\d{2}\/\d{4}$/
      break
    default:
      return false
  }

  if (!regex.test(dateStr)) return false

  // 验证日期是否有效
  const parts = dateStr.split(/[-/]/)
  let year, month, day

  if (format.toUpperCase() === 'YYYY-MM-DD') {
    year = Number.parseInt(parts[0], 10)
    month = Number.parseInt(parts[1], 10) - 1
    day = Number.parseInt(parts[2], 10)
  } else if (format.toUpperCase() === 'MM/DD/YYYY') {
    month = Number.parseInt(parts[0], 10) - 1
    day = Number.parseInt(parts[1], 10)
    year = Number.parseInt(parts[2], 10)
  } else if (format.toUpperCase() === 'DD/MM/YYYY') {
    day = Number.parseInt(parts[0], 10)
    month = Number.parseInt(parts[1], 10) - 1
    year = Number.parseInt(parts[2], 10)
  }

  const date = new Date(year!, month!, day!)
  return date.getFullYear() === year && date.getMonth() === month && date.getDate() === day
}

/**
 * 验证日期是否在某个日期之前
 * @param {Date} date - 要验证的日期
 * @param {Date} beforeDate - 比较日期
 * @returns {boolean} date是否在beforeDate之前
 *
 * @example
 * // 检查输入的日期是否在今天之前
 * if (isBeforeDate(selectedDate, new Date())) {
 *   // 处理过去的日期
 * } else {
 *   alert('请选择今天之前的日期');
 * }
 */
export const isBeforeDate = (date: Date, beforeDate: Date): boolean => {
  return isValidDate(date) && isValidDate(beforeDate) && date.getTime() < beforeDate.getTime()
}

/**
 * 验证日期是否在某个日期之后
 * @param {Date} date - 要验证的日期
 * @param {Date} afterDate - 比较日期
 * @returns {boolean} date是否在afterDate之后
 *
 * @example
 * // 检查输入的日期是否在今天之后
 * if (isAfterDate(selectedDate, new Date())) {
 *   // 处理未来的日期
 * } else {
 *   alert('请选择今天之后的日期');
 * }
 */
export const isAfterDate = (date: Date, afterDate: Date): boolean => {
  return isValidDate(date) && isValidDate(afterDate) && date.getTime() > afterDate.getTime()
}

// ==================== 中国特定验证 ====================

/**
 * 验证是否为有效的中国身份证号码
 * @param {string} id - 要验证的身份证号码
 * @returns {boolean} 是否为有效的身份证号码
 *
 * @example
 * // 检查输入是否为有效的中国身份证号码
 * if (isChineseIdCard(idCard)) {
 *   // 处理有效身份证号
 * } else {
 *   alert('请输入有效的身份证号码');
 * }
 */
export const isChineseIdCard = (id: string): boolean => {
  // 15位和18位身份证号码的正则表达式
  const reg = /^\d{15}$|^\d{18}$|^\d{17}[\dX]$/i
  if (!reg.test(id)) return false

  // 简单校验，实际应用中可能需要更复杂的验证（如校验码计算）
  if (id.length === 18) {
    const idArr = id.split('')
    // 加权因子
    const factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    // 校验位
    const parity = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    let sum = 0
    for (let i = 0; i < 17; i++) {
      sum += Number.parseInt(idArr[i]) * factor[i]
    }
    const index = sum % 11
    return parity[index].toLowerCase() === idArr[17].toLowerCase()
  }

  return true
}

/**
 * 验证是否为有效的中国手机号码
 * @param {string} phone - 要验证的手机号码
 * @returns {boolean} 是否为有效的手机号码
 *
 * @example
 * // 检查输入是否为有效的中国手机号码
 * if (isChinesePhoneNumber(phone)) {
 *   // 处理有效手机号
 * } else {
 *   alert('请输入有效的手机号码');
 * }
 */
export const isChinesePhoneNumber = (phone: string): boolean => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证是否为有效的中国邮政编码
 * @param {string} code - 要验证的邮政编码
 * @returns {boolean} 是否为有效的邮政编码
 *
 * @example
 * // 检查输入是否为有效的中国邮政编码
 * if (isPostalCode(code)) {
 *   // 处理有效邮政编码
 * } else {
 *   alert('请输入有效的邮政编码');
 * }
 */
export const isPostalCode = (code: string): boolean => {
  const reg = /^[1-9]\d{5}$/
  return reg.test(code)
}

// ==================== 表单验证 ====================

/**
 * 验证密码强度
 * @param {string} password - 要验证的密码
 * @param {object} options - 配置选项
 * @param {number} [options.minLength] - 最小长度
 * @param {boolean} [options.requireLowercase] - 是否要求包含小写字母
 * @param {boolean} [options.requireUppercase] - 是否要求包含大写字母
 * @param {boolean} [options.requireNumbers] - 是否要求包含数字
 * @param {boolean} [options.requireSpecialChars] - 是否要求包含特殊字符
 * @returns {boolean} 密码是否满足强度要求
 *
 * @example
 * // 检查密码是否满足强度要求
 * if (isPassword(password, { minLength: 10, requireSpecialChars: true })) {
 *   // 处理有效密码
 * } else {
 *   alert('密码必须至少10个字符，并包含特殊字符');
 * }
 */
export const isPassword = (
  password: string,
  options: {
    minLength?: number
    requireLowercase?: boolean
    requireUppercase?: boolean
    requireNumbers?: boolean
    requireSpecialChars?: boolean
  } = {},
): boolean => {
  const {
    minLength = 8,
    requireLowercase = true,
    requireUppercase = true,
    requireNumbers = true,
    requireSpecialChars = true,
  } = options

  if (!isString(password) || password.length < minLength) return false

  if (requireLowercase && !/[a-z]/.test(password)) return false
  if (requireUppercase && !/[A-Z]/.test(password)) return false
  if (requireNumbers && !/\d/.test(password)) return false
  if (requireSpecialChars && !/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password)) return false

  return true
}

/**
 * 验证值是否非空
 * @param {any} value - 要验证的值
 * @returns {boolean} 值是否非空
 *
 * @example
 * // 检查输入是否非空
 * if (isRequired(userInput)) {
 *   // 处理有效输入
 * } else {
 *   alert('此字段不能为空');
 * }
 */
export const isRequired = (value: any): boolean => {
  if (isString(value)) return value.trim().length > 0
  if (isArray(value)) return value.length > 0
  if (isObject(value)) return !isEmptyObject(value)
  return value !== undefined && value !== null
}

/**
 * 验证字符串是否满足最小长度要求
 * @param {string} str - 要验证的字符串
 * @param {number} length - 最小长度
 * @returns {boolean} 是否满足最小长度
 *
 * @example
 * // 检查用户名是否至少有3个字符
 * if (hasMinLength(userName, 3)) {
 *   // 处理有效用户名
 * } else {
 *   alert('用户名至少需要3个字符');
 * }
 */
export const hasMinLength = (str: string, length: number): boolean => {
  return isString(str) && str.length >= length
}

/**
 * 验证字符串是否不超过最大长度
 * @param {string} str - 要验证的字符串
 * @param {number} length - 最大长度
 * @returns {boolean} 是否不超过最大长度
 *
 * @example
 * // 检查标题是否不超过100个字符
 * if (hasMaxLength(title, 100)) {
 *   // 处理有效标题
 * } else {
 *   alert('标题不能超过100个字符');
 * }
 */
export const hasMaxLength = (str: string, length: number): boolean => {
  return isString(str) && str.length <= length
}

/**
 * 验证姓名格式是否有效（不含特殊字符）
 * @param {string} name - 要验证的姓名
 * @returns {boolean} 姓名格式是否有效
 *
 * @example
 * // 检查姓名是否有效
 * if (isName(fullName)) {
 *   // 处理有效姓名
 * } else {
 *   alert('姓名不能包含特殊字符');
 * }
 */
export const isName = (name: string): boolean => {
  return isString(name) && /^[a-z\u4E00-\u9FA5\s]+$/i.test(name)
}

// ==================== 网络标识验证 ====================

/**
 * 验证是否为有效的IPv4地址
 * @param {string} ip - 要验证的IP地址
 * @returns {boolean} 是否为有效的IPv4地址
 *
 * @example
 * // 检查输入是否为有效的IP地址
 * if (isIPAddress(serverIP)) {
 *   // 处理有效IP地址
 * } else {
 *   alert('请输入有效的IP地址');
 * }
 */
export const isIPAddress = (ip: string): boolean => {
  const reg =
    /^(?:(?:25[0-5]|2[0-4]\d|[01]?\d{1,2})\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d{1,2})$/
  return isString(ip) && reg.test(ip)
}

/**
 * 验证是否为有效的MAC地址
 * @param {string} mac - 要验证的MAC地址
 * @returns {boolean} 是否为有效的MAC地址
 *
 * @example
 * // 检查输入是否为有效的MAC地址
 * if (isMACAddress(deviceMAC)) {
 *   // 处理有效MAC地址
 * } else {
 *   alert('请输入有效的MAC地址');
 * }
 */
export const isMACAddress = (mac: string): boolean => {
  const reg = /^(?:[0-9A-F]{2}[:-]){5}[0-9A-F]{2}$/i
  return isString(mac) && reg.test(mac)
}

/**
 * 验证是否为有效的端口号
 * @param {number} port - 要验证的端口号
 * @returns {boolean} 是否为有效的端口号
 *
 * @example
 * // 检查输入是否为有效的端口号
 * if (isPort(serverPort)) {
 *   // 处理有效端口号
 * } else {
 *   alert('请输入有效的端口号(0-65535)');
 * }
 */
export const isPort = (port: number): boolean => {
  return isInteger(port) && port >= 0 && port <= 65535
}

// ==================== 其他通用验证 ====================

/**
 * 验证两个值是否相等
 * @param {any} value1 - 第一个值
 * @param {any} value2 - 第二个值
 * @returns {boolean} 两个值是否相等
 *
 * @example
 * // 检查两个密码输入是否匹配
 * if (isEqual(password, confirmPassword)) {
 *   // 处理匹配的密码
 * } else {
 *   alert('两次输入的密码不匹配');
 * }
 */
export const isEqual = (value1: any, value2: any): boolean => {
  if (isObject(value1) && isObject(value2)) {
    return JSON.stringify(value1) === JSON.stringify(value2)
  }
  return value1 === value2
}

/**
 * 验证字符串是否包含特定子串
 * @param {string} str - 要验证的字符串
 * @param {string} substring - 要检查的子串
 * @param {boolean} [caseSensitive] - 是否区分大小写
 * @returns {boolean} 字符串是否包含子串
 *
 * @example
 * // 检查文本是否包含关键词
 * if (containsSubstring(content, 'important', false)) {
 *   // 处理包含关键词的内容
 * }
 */
export const containsSubstring = (
  str: string,
  substring: string,
  caseSensitive: boolean = true,
): boolean => {
  if (!isString(str) || !isString(substring)) return false

  if (caseSensitive) {
    return str.includes(substring)
  } else {
    return str.toLowerCase().includes(substring.toLowerCase())
  }
}

/**
 * 验证字符串是否只包含允许的字符
 * @param {string} str - 要验证的字符串
 * @param {string} allowedChars - 允许的字符集
 * @returns {boolean} 字符串是否只包含允许的字符
 *
 * @example
 * // 检查输入是否只包含数字和字母
 * if (onlyContains(input, 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789')) {
 *   // 处理有效输入
 * } else {
 *   alert('只允许输入字母和数字');
 * }
 */
export const onlyContains = (str: string, allowedChars: string): boolean => {
  if (!isString(str)) return false
  const regex = new RegExp(`^[${allowedChars}]+$`)
  return regex.test(str)
}

/**
 * 验证值是否在允许的列表中
 * @param {any} value - 要验证的值
 * @param {any[]} allowedValues - 允许的值列表
 * @returns {boolean} 值是否在允许的列表中
 *
 * @example
 * // 检查选择的角色是否在允许的角色列表中
 * if (isOneOf(selectedRole, ['admin', 'editor', 'user'])) {
 *   // 处理有效角色
 * } else {
 *   alert('请选择有效的角色');
 * }
 */
export const isOneOf = (value: any, allowedValues: any[]): boolean => {
  return isArray(allowedValues) && allowedValues.includes(value)
}

/**
 * 验证字符串是否为有效的颜色十六进制代码
 * @param {string} color - 要验证的颜色代码
 * @returns {boolean} 是否为有效的颜色代码
 *
 * @example
 * // 检查输入是否为有效的颜色代码
 * if (isHexColor('#FF5733')) {
 *   // 处理有效颜色代码
 * } else {
 *   alert('请输入有效的颜色代码(如#FF5733)');
 * }
 */
export const isHexColor = (color: string): boolean => {
  return isString(color) && /^#(?:[0-9A-F]{3}){1,2}$/i.test(color)
}

/**
 * 验证字符串是否为有效的URL参数
 * @param {string} param - 要验证的URL参数
 * @returns {boolean} 是否为有效的URL参数
 *
 * @example
 * // 检查URL参数是否有效
 * if (isURLParam('user_id=123&type=admin')) {
 *   // 处理有效URL参数
 * } else {
 *   alert('URL参数格式无效');
 * }
 */
export const isURLParam = (param: string): boolean => {
  return isString(param) && /^[^=&?]+=[^=&?]+(?:&[^=&?]+=[^=&?]+)*$/.test(param)
}

/**
 * 验证是否为有效的文件名(不包含操作系统禁止的特殊字符)
 * @param {string} filename - 要验证的文件名
 * @returns {boolean} 是否为有效的文件名
 *
 * @example
 * // 检查文件名是否有效
 * if (isValidFilename('my-document.pdf')) {
 *   // 处理有效文件名
 * } else {
 *   alert('文件名包含无效字符');
 * }
 */
export const isValidFilename = (filename: string): boolean => {
  return isString(filename) && !/[/:*?"<>|]/.test(filename)
}

/**
 * 验证是否为UUID格式
 * @param {string} uuid - 要验证的UUID字符串
 * @returns {boolean} 是否为有效的UUID
 *
 * @example
 * // 检查字符串是否为有效UUID
 * if (isUUID('550e8400-e29b-41d4-a716-446655440000')) {
 *   // 处理有效UUID
 * } else {
 *   alert('请提供有效的UUID');
 * }
 */
export const isUUID = (uuid: string): boolean => {
  const regex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
  return isString(uuid) && regex.test(uuid)
}

/**
 * 验证是否为有效的社交媒体用户名
 * @param {string} userName - 要验证的用户名
 * @param {string} platform - 社交媒体平台 ('twitter', 'instagram', 等)
 * @returns {boolean} 是否为有效的社交媒体用户名
 *
 * @example
 * // 检查是否为有效的Twitter用户名
 * if (isSocialMediaUserName('@example_user', 'twitter')) {
 *   // 处理有效Twitter用户名
 * } else {
 *   alert('请提供有效的Twitter用户名');
 * }
 */
export const isSocialMediaUserName = (userName: string, platform: string): boolean => {
  if (!isString(userName)) return false

  const patterns: Record<string, RegExp> = {
    twitter: /^@?\w{1,15}$/,
    instagram: /^@?[\w.]{1,30}$/,
    facebook: /^[a-z0-9.]{5,50}$/i,
    linkedin: /^[a-z0-9-]{3,100}$/i,
  }

  return platform in patterns && patterns[platform].test(userName)
}

/**
 * 验证是否为银行卡号（简单验证，不包含校验位算法）
 * @param {string} cardNumber - 要验证的银行卡号
 * @returns {boolean} 是否为有效的银行卡号格式
 *
 * @example
 * // 检查是否为有效的银行卡号
 * if (isBankCardNumber('6222020111122220000')) {
 *   // 处理有效银行卡号
 * } else {
 *   alert('请提供有效的银行卡号');
 * }
 */
export const isBankCardNumber = (cardNumber: string): boolean => {
  // 银行卡号一般为13-19位数字
  return isString(cardNumber) && /^\d{13,19}$/.test(cardNumber)
}

/**
 * 验证是否为有效的信用卡号（使用Luhn算法）
 * @param {string} cardNumber - 要验证的信用卡号
 * @returns {boolean} 是否为有效的信用卡号
 *
 * @example
 * // 检查是否为有效的信用卡号
 * if (isCreditCardNumber('4111111111111111')) {
 *   // 处理有效信用卡号
 * } else {
 *   alert('请提供有效的信用卡号');
 * }
 */
export const isCreditCardNumber = (cardNumber: string): boolean => {
  if (!isString(cardNumber) || !/^\d+$/.test(cardNumber)) return false

  // Luhn算法校验
  let sum = 0
  let shouldDouble = false

  // 从右向左遍历
  for (let i = cardNumber.length - 1; i >= 0; i--) {
    let digit = Number.parseInt(cardNumber.charAt(i))

    if (shouldDouble) {
      digit *= 2
      if (digit > 9) digit -= 9
    }

    sum += digit
    shouldDouble = !shouldDouble
  }

  return sum % 10 === 0
}
