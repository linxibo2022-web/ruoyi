/**
 * 格式化工具函数
 *
 * 包含以下功能类别:
 * - 数值格式化: 处理单位、数字、金额、百分比和单位 (formatUnit, formatNumber, formatPercent, formatAmount, formatCurrency, formatFileSize, formatDistance)
 * - 时间与持续时间: 处理时长的格式化 (formatDuration)
 * - 隐私和敏感数据处理: 处理敏感信息的格式化和脱敏 (formatPrivacy, formatIDCard, formatPhone, formatBankCard, formatIP)
 * - 文本和URL处理: 处理字符串截断和格式化 (formatStringLength, formatFileName, formatURL)
 * - 集合和枚举处理: 处理列表、枚举和布尔值的格式化 (formatList, formatEnum, formatBoolean)
 * - 状态与颜色: 根据状态值返回对应的颜色类名 (formatStatusColor)
 * - 表格数据格式化: 为表格单元格提供通用的格式化功能 (formatTableCell)
 */

type FormatOptions = {
  /**
   * 是否强制转换无效值为默认结果
   * true: 对于无效输入返回指定默认值
   * false: 对于无效输入原样返回
   */
  strict?: boolean

  /**
   * 无效输入时的默认返回值
   */
  defaultValue?: string
}

// 基础默认选项
const DEFAULT_OPTIONS: FormatOptions = {
  strict: true,
  defaultValue: ''
}

// ==================== 数值格式化 ====================

/**
 * 格式化CSS单位值
 * 如果值是数字，自动添加px单位；如果已有单位或非数字字符，保持原样
 *
 * @param {number|string} val 要格式化的值
 * @param {string} [defaultUnit='px'] 默认单位
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的CSS单位值
 * @example
 * formatUnit(100) // "100px"
 * formatUnit('100') // "100px"
 * formatUnit('100%') // "100%"
 * formatUnit('auto') // "auto"
 * formatUnit(100, 'rem') // "100rem"
 */
export const formatUnit = (val: number | string, defaultUnit: string = 'px', options?: FormatOptions): string => {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (val === '' || val == null) {
    return opts.strict ? opts.defaultValue : ''
  }

  // 如果是数字类型，直接添加单位
  if (typeof val === 'number') {
    return `${val}${defaultUnit}`
  }

  const strVal = String(val)

  // 检查是否已有单位（包含非数字、小数点和负号以外的字符）
  if (/[^\d.-]/.test(strVal)) {
    return strVal
  }

  // 确保是有效数字
  if (!isNaN(Number(strVal)) && isFinite(Number(strVal))) {
    return `${strVal}${defaultUnit}`
  }

  return opts.strict ? opts.defaultValue : strVal
}

/**
 * 格式化数字
 * 将数字格式化为指定小数位数，可选千分位分隔
 *
 * @param {number} value 数值
 * @param {number} [decimals=0] 小数位数
 * @param {boolean} [useThousandsSeparator=false] 是否使用千分位分隔符
 * @param {string} [thousandsSeparator=','] 千分位分隔符
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的数字
 * @example
 * formatNumber(1234.56) // "1235"
 * formatNumber(1234.56, 2) // "1234.56"
 * formatNumber(1234.56, 2, true) // "1,234.56"
 */
export function formatNumber(
  value: number,
  decimals: number = 0,
  useThousandsSeparator: boolean = false,
  thousandsSeparator: string = ',',
  options?: FormatOptions
): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (value === undefined || value === null || isNaN(value)) {
    return opts.strict ? opts.defaultValue : String(value)
  }

  // 处理小数部分
  let result = Math.abs(value).toFixed(decimals)

  // 添加千分位分隔符
  if (useThousandsSeparator) {
    const parts = result.split('.')
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSeparator)
    result = parts.join('.')
  }

  // 处理负数
  if (value < 0) {
    result = '-' + result
  }

  return result
}

/**
 * 格式化百分比
 * 将小数转换为百分比格式
 *
 * @param {number} value 小数值
 * @param {number} [decimals=2] 小数位数
 * @param {boolean} [withSymbol=true] 是否包含百分号
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的百分比
 * @example
 * formatPercent(0.1234) // "12.34%"
 * formatPercent(0.1234, 1, false) // "12.3"
 */
export function formatPercent(value: number, decimals: number = 2, withSymbol: boolean = true, options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (value === undefined || value === null || isNaN(value)) {
    return opts.strict ? opts.defaultValue : String(value)
  }

  const percent = (value * 100).toFixed(decimals)
  return withSymbol ? `${percent}%` : percent
}

/**
 * 格式化金额
 * 将数字格式化为千分位分隔的金额格式
 *
 * @param {number} amount 金额数值
 * @param {number} [decimals=2] 小数位数
 * @param {string} [decimalSeparator='.'] 小数点分隔符
 * @param {string} [thousandsSeparator=','] 千分位分隔符
 * @param {string} [prefix=''] 前缀（如货币符号）
 * @param {string} [suffix=''] 后缀
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的金额字符串
 * @example
 * formatAmount(1234.56) // "1,234.56"
 * formatAmount(1234.56, 2, '.', ',', '¥') // "¥1,234.56"
 */
export function formatAmount(
  amount: number,
  decimals: number = 2,
  decimalSeparator: string = '.',
  thousandsSeparator: string = ',',
  prefix: string = '',
  suffix: string = '',
  options?: FormatOptions
): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (amount === undefined || amount === null || isNaN(amount)) {
    return opts.strict ? opts.defaultValue : String(amount)
  }

  // 处理小数部分
  const fixed = Math.abs(amount).toFixed(decimals)
  const [intPart, decimalPart] = fixed.split('.')

  // 添加千分位分隔符
  const formattedInt = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSeparator)

  // 组合各个部分
  let result = prefix

  // 处理负数
  if (amount < 0) {
    result += '-'
  }

  result += formattedInt

  if (decimals > 0) {
    result += decimalSeparator + decimalPart
  }

  result += suffix

  return result
}

/**
 * 金额格式化选项
 */
export type CurrencyFormatOptions = {
  /**
   * 小数位数
   * @default 2
   */
  decimals?: number

  /**
   * 货币符号
   * @default ''
   */
  currencySymbol?: string

  /**
   * 货币符号位置
   * @default 'prefix'
   */
  symbolPosition?: 'prefix' | 'suffix'

  /**
   * 小数点分隔符
   * @default '.'
   */
  decimalSeparator?: string

  /**
   * 千分位分隔符
   * @default ','
   */
  thousandsSeparator?: string

  /**
   * 格式化选项
   */
  formatOptions?: FormatOptions
}

/**
 * 通用货币格式化，支持不同区域的货币格式
 *
 * @param {number} amount 金额
 * @param {string} [currencyCode='CNY'] 货币代码 (ISO 4217)
 * @param {CurrencyFormatOptions} [options] 货币格式化选项
 * @returns {string} 格式化后的货币字符串
 * @example
 * formatCurrency(1234.56) // "¥1,234.56"
 * formatCurrency(1234.56, 'USD') // "$1,234.56"
 * formatCurrency(1234.56, 'EUR') // "€1,234.56"
 */
export function formatCurrency(amount: number, currencyCode: string = 'CNY', options?: CurrencyFormatOptions): string {
  // 默认选项
  const defaultOptions: CurrencyFormatOptions = {
    decimals: 2,
    symbolPosition: 'prefix',
    decimalSeparator: '.',
    thousandsSeparator: ',',
    currencySymbol: ''
  }

  // 合并选项
  const opts = { ...defaultOptions, ...options }

  // 货币符号映射
  const currencySymbols: Record<string, string> = {
    'CNY': '¥',
    'USD': '$',
    'EUR': '€',
    'GBP': '£',
    'JPY': '¥',
    'KRW': '₩',
    'RUB': '₽'
  }

  // 使用提供的货币符号或从映射中获取
  const symbol = opts.currencySymbol || currencySymbols[currencyCode] || ''

  // 根据符号位置设置前缀和后缀
  const prefix = opts.symbolPosition === 'prefix' ? symbol : ''
  const suffix = opts.symbolPosition === 'suffix' ? symbol : ''

  return formatAmount(amount, opts.decimals, opts.decimalSeparator, opts.thousandsSeparator, prefix, suffix, opts.formatOptions)
}

/**
 * 格式化文件大小
 * 将字节数转换为易读的文件大小格式
 *
 * @param {number} bytes 文件大小（字节）
 * @param {number} [decimals=2] 小数位数
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的文件大小
 * @example
 * formatFileSize(1024) // "1.00 KB"
 * formatFileSize(1234567) // "1.18 MB"
 */
export function formatFileSize(bytes: number, decimals: number = 2, options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (bytes === undefined || bytes === null || isNaN(bytes)) {
    return opts.strict ? opts.defaultValue : String(bytes)
  }

  if (bytes === 0) return '0 Bytes'

  const k = 1024
  const dm = decimals < 0 ? 0 : decimals
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']

  const i = Math.floor(Math.log(Math.abs(bytes)) / Math.log(k))
  const value = parseFloat((bytes / Math.pow(k, i)).toFixed(dm))

  return `${value} ${sizes[i]}`
}

/**
 * 格式化距离单位
 * 根据米数自动选择合适的单位（米/公里）
 *
 * @param {number} meters 米数
 * @param {number} [decimals=1] 公里小数位数
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的距离
 * @example
 * formatDistance(1500) // "1.5公里"
 * formatDistance(500) // "500米"
 */
export function formatDistance(meters: number, decimals: number = 1, options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (meters === undefined || meters === null || isNaN(meters)) {
    return opts.strict ? opts.defaultValue : String(meters)
  }

  if (meters < 1000) {
    return `${Math.floor(meters)}米`
  } else {
    return `${(meters / 1000).toFixed(decimals)}公里`
  }
}

// ==================== 时间与持续时间 ====================

/**
 * 格式化时长
 * 将秒数转换为时分秒格式
 *
 * @param {number} seconds 秒数
 * @param {boolean} [showZeroHours=false] 是否显示为0的小时
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的时长
 * @example
 * formatDuration(3661) // "1小时1分1秒"
 * formatDuration(61) // "1分1秒"
 */
export function formatDuration(seconds: number, showZeroHours: boolean = false, options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (seconds === undefined || seconds === null || isNaN(seconds) || seconds < 0) {
    return opts.strict ? opts.defaultValue : String(seconds)
  }

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = Math.floor(seconds % 60)

  let result = ''

  if (hours > 0 || showZeroHours) {
    result += `${hours}小时`
  }

  if (minutes > 0 || hours > 0) {
    result += `${minutes}分`
  }

  result += `${remainingSeconds}秒`

  return result
}

// ==================== 隐私和敏感数据处理 ====================

/**
 * 隐私数据格式化选项
 */
export type PrivacyOptions = {
  /**
   * 显示开头字符数
   * @default 0
   */
  showStart?: number

  /**
   * 显示结尾字符数
   * @default 0
   */
  showEnd?: number

  /**
   * 掩码字符
   * @default '*'
   */
  maskChar?: string

  /**
   * 掩码字符重复次数，0表示根据原始长度自动计算
   * @default 0
   */
  maskLength?: number
}

/**
 * 格式化敏感数据，提供隐私保护
 * 可自定义显示首尾字符数量和掩码字符
 *
 * @param {string} data 敏感数据
 * @param {PrivacyOptions} [options] 隐私选项
 * @param {FormatOptions} [formatOptions] 格式化选项
 * @returns {string} 格式化后的数据
 * @example
 * formatPrivacy('13812345678', { showStart: 3, showEnd: 4 }) // "138****5678"
 * formatPrivacy('张三丰', { showStart: 1, showEnd: 0, maskChar: '*' }) // "张**"
 */
export function formatPrivacy(data: string, options?: PrivacyOptions, formatOptions?: FormatOptions): string {
  const defaultPrivacyOptions: PrivacyOptions = {
    showStart: 0,
    showEnd: 0,
    maskChar: '*',
    maskLength: 0
  }

  const opts = { ...DEFAULT_OPTIONS, ...formatOptions }
  const privacyOpts = { ...defaultPrivacyOptions, ...options }

  if (!data) {
    return opts.strict ? opts.defaultValue : data
  }

  const strData = String(data)
  const strLength = strData.length

  // 如果数据长度不超过首尾显示长度总和，直接返回
  if (strLength <= privacyOpts.showStart + privacyOpts.showEnd) {
    return opts.strict ? new Array(strLength + 1).join(privacyOpts.maskChar) : strData
  }

  const start = strData.substring(0, privacyOpts.showStart)
  const end = strData.substring(strLength - privacyOpts.showEnd)

  // 计算掩码长度
  const maskLength = privacyOpts.maskLength > 0 ? privacyOpts.maskLength : strLength - privacyOpts.showStart - privacyOpts.showEnd

  const mask = new Array(maskLength + 1).join(privacyOpts.maskChar)

  return start + mask + end
}

/**
 * 格式化身份证号
 * 提供隐私保护，可自定义显示首尾字符数量
 *
 * @param {string} idNumber 身份证号
 * @param {PrivacyOptions} [options] 隐私选项
 * @param {FormatOptions} [formatOptions] 格式化选项
 * @returns {string} 格式化后的身份证号
 * @example
 * formatIDCard('110101199001011234') // "1101**********1234"
 * formatIDCard('110101199001011234', { showStart: 6, showEnd: 4 }) // "110101********1234"
 */
export function formatIDCard(idNumber: string, options?: PrivacyOptions, formatOptions?: FormatOptions): string {
  // 身份证号默认显示前4位和后4位
  const defaultOptions: PrivacyOptions = {
    showStart: 4,
    showEnd: 4,
    maskChar: '*'
  }

  return formatPrivacy(idNumber, { ...defaultOptions, ...options }, formatOptions)
}

/**
 * 格式化手机号
 * 支持多种格式化方式
 *
 * @param {string} phone 手机号码
 * @param {string} [format='xxx-xxxx-xxxx'] 格式化模式，x将被替换为数字
 * @param {string} [mask='*'] 掩码字符
 * @param {boolean} [privacy=false] 是否启用隐私保护（中间四位使用掩码）
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的手机号
 * @example
 * formatPhone('13812345678') // "138-1234-5678"
 * formatPhone('13812345678', 'xxx xxxx xxxx') // "138 1234 5678"
 * formatPhone('13812345678', undefined, undefined, true) // "138****5678"
 */
export function formatPhone(
  phone: string,
  format: string = 'xxx-xxxx-xxxx',
  mask: string = '*',
  privacy: boolean = false,
  options?: FormatOptions
): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (!phone) {
    return opts.strict ? opts.defaultValue : phone
  }

  // 清除所有非数字字符
  const cleaned = String(phone).replace(/\D/g, '')

  // 如果清理后的号码长度不足，则返回原始值或默认值
  if (cleaned.length < 7) {
    return opts.strict ? opts.defaultValue : phone
  }

  // 隐私保护模式 - 隐藏中间四位
  if (privacy) {
    const prefix = cleaned.substring(0, 3)
    const suffix = cleaned.substring(cleaned.length - 4)
    const maskedPart = mask.repeat(4)
    return `${prefix}${maskedPart}${suffix}`
  }

  // 标准格式化模式
  let result = format
  let digitIndex = 0

  for (let i = 0; i < result.length; i++) {
    if (result[i] === 'x') {
      if (digitIndex < cleaned.length) {
        result = result.substring(0, i) + cleaned[digitIndex] + result.substring(i + 1)
        digitIndex++
      } else {
        // 如果数字不够，用空格替换剩余的x
        result = result.substring(0, i) + ' ' + result.substring(i + 1)
      }
    }
  }

  // 移除多余空格
  return result.trim()
}

/**
 * 格式化银行卡号
 * 每4位插入一个空格，便于阅读
 *
 * @param {string} cardNumber 银行卡号
 * @param {string} [separator=' '] 分隔符
 * @param {boolean} [privacy=false] 是否启用隐私保护（只显示后四位）
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的银行卡号
 * @example
 * formatBankCard('6225365271562822') // "6225 3652 7156 2822"
 * formatBankCard('6225365271562822', '-') // "6225-3652-7156-2822"
 * formatBankCard('6225365271562822', ' ', true) // "**** **** **** 2822"
 */
export function formatBankCard(cardNumber: string, separator: string = ' ', privacy: boolean = false, options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (!cardNumber) {
    return opts.strict ? opts.defaultValue : cardNumber
  }

  // 移除所有非数字字符
  const cleaned = String(cardNumber).replace(/\D/g, '')

  if (cleaned.length < 4) {
    return opts.strict ? opts.defaultValue : cardNumber
  }

  // 隐私保护模式 - 只显示后四位
  if (privacy) {
    const lastFour = cleaned.slice(-4)
    const maskedParts = Array(Math.ceil((cleaned.length - 4) / 4))
      .fill('****')
      .join(separator)

    return maskedParts + separator + lastFour
  }

  // 标准格式化模式 - 每4位添加分隔符
  return cleaned.replace(/(\d{4})(?=\d)/g, `$1${separator}`)
}

/**
 * IP地址格式化选项
 */
export type IPFormatOptions = {
  /**
   * 是否隐藏部分IP（用于隐私保护）
   * @default false
   */
  privacy?: boolean

  /**
   * 掩码字符
   * @default '*'
   */
  maskChar?: string
}

/**
 * 格式化IP地址
 * 支持隐私保护
 *
 * @param {string} ip IP地址
 * @param {IPFormatOptions} [options] IP格式化选项
 * @param {FormatOptions} [formatOptions] 格式化选项
 * @returns {string} 格式化后的IP地址
 * @example
 * formatIP('192.168.1.1') // "192.168.1.1"
 * formatIP('192.168.1.1', { privacy: true }) // "192.168.*.*"
 */
export function formatIP(ip: string, options?: IPFormatOptions, formatOptions?: FormatOptions): string {
  const defaultOptions: IPFormatOptions = {
    privacy: false,
    maskChar: '*'
  }

  const opts = { ...DEFAULT_OPTIONS, ...formatOptions }
  const ipOpts = { ...defaultOptions, ...options }

  if (!ip) {
    return opts.strict ? opts.defaultValue : ip
  }

  // 验证IP地址格式
  const isValidIP = /^(\d{1,3}\.){3}\d{1,3}$/.test(ip)
  if (!isValidIP) {
    return opts.strict ? opts.defaultValue : ip
  }

  // 隐私保护模式 - 隐藏后半部分
  if (ipOpts.privacy) {
    const parts = ip.split('.')
    return `${parts[0]}.${parts[1]}.${ipOpts.maskChar}.${ipOpts.maskChar}`
  }

  return ip
}

// ==================== 文本和URL处理 ====================

/**
 * 字符串长度格式化选项
 */
export type StringLengthOptions = {
  /**
   * 最大长度
   * @default 20
   */
  maxLength?: number

  /**
   * 省略号字符
   * @default '...'
   */
  ellipsis?: string

  /**
   * 截取位置
   * @default 'end'
   */
  position?: 'start' | 'middle' | 'end'
}

/**
 * 格式化字符串长度
 * 超出指定长度的部分用省略号代替，支持在开始、中间或结尾处截断
 *
 * @param {string} str 需要格式化的字符串
 * @param {StringLengthOptions} [options] 字符串长度选项
 * @param {FormatOptions} [formatOptions] 格式化选项
 * @returns {string} 格式化后的字符串
 * @example
 * formatStringLength('这是一个很长的字符串', { maxLength: 10 }) // "这是一个很长..."
 * formatStringLength('这是一个很长的字符串', { maxLength: 10, position: 'middle' }) // "这是一个...字符串"
 * formatStringLength('这是一个很长的字符串', { maxLength: 10, position: 'start' }) // "...很长的字符串"
 */
export function formatStringLength(str: string, options?: StringLengthOptions, formatOptions?: FormatOptions): string {
  const defaultOptions: StringLengthOptions = {
    maxLength: 20,
    ellipsis: '...',
    position: 'end'
  }

  const opts = { ...DEFAULT_OPTIONS, ...formatOptions }
  const stringOpts = { ...defaultOptions, ...options }

  if (!str) {
    return opts.strict ? opts.defaultValue : str
  }

  const maxLength = stringOpts.maxLength

  if (str.length <= maxLength) {
    return str
  }

  const ellipsisLength = stringOpts.ellipsis.length

  switch (stringOpts.position) {
    case 'start':
      return stringOpts.ellipsis + str.substring(str.length - maxLength + ellipsisLength)

    case 'middle': {
      const leftLength = Math.ceil((maxLength - ellipsisLength) / 2)
      const rightLength = Math.floor((maxLength - ellipsisLength) / 2)
      return str.substring(0, leftLength) + stringOpts.ellipsis + str.substring(str.length - rightLength)
    }

    case 'end':
    default:
      return str.substring(0, maxLength - ellipsisLength) + stringOpts.ellipsis
  }
}

/**
 * 格式化文件名，保留扩展名
 * 过长的文件名中间部分用省略号代替，保留扩展名
 *
 * @param {string} filename 文件名
 * @param {number} [maxLength=20] 最大长度
 * @param {string} [ellipsis='...'] 省略号字符
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的文件名
 * @example
 * formatFileName('very_long_filename_example.txt', 20) // "very_long_f...ple.txt"
 * formatFileName('document.pdf', 20) // "document.pdf"
 */
export function formatFileName(filename: string, maxLength: number = 20, ellipsis: string = '...', options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (!filename) {
    return opts.strict ? opts.defaultValue : filename
  }

  if (filename.length <= maxLength) {
    return filename
  }

  const lastDotIndex = filename.lastIndexOf('.')
  // 没有扩展名的情况
  if (lastDotIndex <= 0) {
    return formatStringLength(filename, { maxLength, ellipsis })
  }

  const extension = filename.substring(lastDotIndex)
  const name = filename.substring(0, lastDotIndex)

  // 扩展名过长的情况
  if (extension.length >= maxLength - ellipsis.length) {
    return ellipsis + extension.substring(extension.length - (maxLength - ellipsis.length))
  }

  // 计算名称部分的最大长度
  const nameMaxLength = maxLength - extension.length - ellipsis.length

  // 名称部分太短的情况
  if (nameMaxLength <= 0) {
    return ellipsis + extension
  }

  // 在名称中间使用省略号
  const leftLength = Math.ceil(nameMaxLength / 2)
  const rightLength = Math.floor(nameMaxLength / 2)

  return name.substring(0, leftLength) + ellipsis + name.substring(name.length - rightLength) + extension
}

/**
 * 格式化URL，可选择显示或隐藏参数
 *
 * @param {string} url URL地址
 * @param {boolean} [showParams=true] 是否显示参数
 * @param {FormatOptions} [options] 格式化选项
 * @returns {string} 格式化后的URL
 * @example
 * formatURL('https://example.com/path?query=123') // "https://example.com/path?query=123"
 * formatURL('https://example.com/path?query=123', false) // "https://example.com/path"
 */
export function formatURL(url: string, showParams: boolean = true, options?: FormatOptions): string {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  if (!url) {
    return opts.strict ? opts.defaultValue : url
  }

  try {
    const urlObj = new URL(url)

    if (!showParams) {
      return `${urlObj.protocol}//${urlObj.hostname}${urlObj.pathname}`
    }

    return url
  } catch (e) {
    // URL不合法
    return opts.strict ? opts.defaultValue : url
  }
}

// ==================== 集合和枚举处理 ====================

/**
 * 列表格式化选项
 */
export type ListFormatOptions = {
  /**
   * 分隔符
   * @default ','
   */
  separator?: string

  /**
   * 最大显示项数，超出部分使用省略项替代
   * @default 0 (不限制)
   */
  maxItems?: number

  /**
   * 省略提示文本
   * @default '...'
   */
  ellipsis?: string

  /**
   * 空列表显示文本
   * @default ''
   */
  emptyText?: string
}

/**
 * 格式化列表数据为分隔符连接的字符串
 *
 * @param {any[]} list 数据列表
 * @param {string|Function} [field] 要提取的字段名或转换函数
 * @param {ListFormatOptions} [options] 列表格式化选项
 * @param {FormatOptions} [formatOptions] 格式化选项
 * @returns {string} 格式化后的字符串
 * @example
 * formatList([1, 2, 3]) // "1,2,3"
 * formatList([{id: 1, name: 'Alice'}, {id: 2, name: 'Bob'}], 'name') // "Alice,Bob"
 * formatList([1, 2, 3, 4, 5], null, { maxItems: 3, ellipsis: '等' }) // "1,2,3等"
 */
export function formatList(list: any[], field?: string | ((item: any) => any), options?: ListFormatOptions, formatOptions?: FormatOptions): string {
  const defaultOptions: ListFormatOptions = {
    separator: ',',
    maxItems: 0,
    ellipsis: '...',
    emptyText: ''
  }

  const opts = { ...DEFAULT_OPTIONS, ...formatOptions }
  const listOpts = { ...defaultOptions, ...options }

  if (!list || !Array.isArray(list) || list.length === 0) {
    return listOpts.emptyText || opts.defaultValue
  }

  let items: any[] = [...list]

  // 提取字段或应用转换函数
  if (field) {
    if (typeof field === 'function') {
      items = items.map(field)
    } else {
      items = items.map((item) => (item && typeof item === 'object' ? item[field] : item))
    }
  }

  // 限制数量
  let result = ''
  if (listOpts.maxItems > 0 && items.length > listOpts.maxItems) {
    result = items.slice(0, listOpts.maxItems).join(listOpts.separator)

    result += listOpts.ellipsis
  } else {
    result = items.join(listOpts.separator)
  }

  return result
}

/**
 * 格式化枚举值为显示文本
 *
 * @param {any} value 枚举值
 * @param {Record<string|number, string>} enumMap 枚举映射对象
 * @param {string} [defaultText=''] 默认文本，当映射不存在时返回
 * @returns {string} 格式化后的显示文本
 * @example
 * formatEnum(1, { 0: '禁用', 1: '启用' }) // "启用"
 * formatEnum('PENDING', { PENDING: '待处理', SUCCESS: '成功' }) // "待处理"
 */
export function formatEnum(value: any, enumMap: Record<string | number, string>, defaultText: string = ''): string {
  if (value === undefined || value === null) {
    return defaultText
  }

  const key = value.toString()
  return enumMap[key] !== undefined ? enumMap[key] : defaultText
}

/**
 * 格式化布尔值
 *
 * @param {any} value 布尔值
 * @param {string} [trueText='是'] 布尔值为true时的显示文本
 * @param {string} [falseText='否'] 布尔值为false时的显示文本
 * @returns {string} 格式化后的文本
 * @example
 * formatBoolean(true) // "是"
 * formatBoolean(false, '启用', '禁用') // "禁用"
 */
export function formatBoolean(value: any, trueText: string = '是', falseText: string = '否'): string {
  if (value === undefined || value === null) {
    return falseText
  }

  // 处理字符串的"true"和"false"
  if (typeof value === 'string') {
    const lowerValue = value.toLowerCase()
    if (lowerValue === 'true') return trueText
    if (lowerValue === 'false') return falseText
  }

  return Boolean(value) ? trueText : falseText
}

// ==================== 状态与颜色 ====================

/**
 * 状态颜色类型定义
 */
export type StatusColorType = 'primary' | 'success' | 'info' | 'warning' | 'danger' | 'default'

/**
 * 自定义状态颜色映射
 */
export type StatusColorMap = Record<string | number, StatusColorType>

/**
 * 默认状态颜色映射
 */
const DEFAULT_STATUS_COLORS: StatusColorMap = {
  '0': 'info', // 默认或待处理
  '1': 'success', // 成功或正常
  '2': 'warning', // 警告或异常
  '3': 'danger', // 错误或禁用
  'default': 'info'
}

/**
 * 格式化状态颜色
 * 根据状态值返回对应的颜色类名
 *
 * @param {number|string} status 状态值
 * @param {StatusColorMap} [customColorMap] 自定义颜色映射
 * @returns {StatusColorType} 状态对应的颜色类名
 * @example
 * formatStatusColor(1) // "success"
 * formatStatusColor('error', { 'error': 'danger' }) // "danger"
 */
export function formatStatusColor(status: number | string, customColorMap?: StatusColorMap): StatusColorType {
  if (status === undefined || status === null) {
    return 'default'
  }

  const colorMap = { ...DEFAULT_STATUS_COLORS, ...customColorMap }
  return (colorMap[status.toString()] || colorMap.default) as StatusColorType
}

// ==================== 表格数据格式化 ====================

/**
 * 表格数据格式化
 * 为表格单元格提供格式化函数，支持多种数据类型格式化
 *
 * @param {any} value 单元格值
 * @param {string} type 数据类型，支持：'text'|'number'|'amount'|'percent'|'date'|'datetime'|'boolean'|'enum'
 * @param {Record<string, any>} [options] 格式化选项，根据type不同而不同
 * @returns {string} 格式化后的数据
 * @example
 * formatTableCell(1234.56, 'amount', { decimals: 2, prefix: '¥' }) // "¥1,234.56"
 * formatTableCell('2023-01-01', 'date') // "2023-01-01"
 * formatTableCell(1, 'enum', { enumMap: { 0: '禁用', 1: '启用' } }) // "启用"
 */
export function formatTableCell(
  value: any,
  type: 'text' | 'number' | 'amount' | 'percent' | 'date' | 'datetime' | 'boolean' | 'enum',
  options?: Record<string, any>
): string {
  if (value === undefined || value === null) {
    return ''
  }

  switch (type) {
    case 'number':
      return formatNumber(value, options?.decimals, options?.useThousandsSeparator, options?.thousandsSeparator)

    case 'amount':
      return formatAmount(
        value,
        options?.decimals,
        options?.decimalSeparator,
        options?.thousandsSeparator,
        options?.prefix || '',
        options?.suffix || ''
      )

    case 'percent':
      return formatPercent(value, options?.decimals, options?.withSymbol)

    case 'date':
      if (!(value instanceof Date) && typeof value === 'string') {
        // 简单日期格式化，仅展示部分
        const date = new Date(value)
        if (isNaN(date.getTime())) {
          return String(value)
        }
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
      }
      return String(value)

    case 'datetime':
      if (!(value instanceof Date) && typeof value === 'string') {
        // 简单日期时间格式化
        const date = new Date(value)
        if (isNaN(date.getTime())) {
          return String(value)
        }
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
      }
      return String(value)

    case 'boolean':
      return formatBoolean(value, options?.trueText, options?.falseText)

    case 'enum':
      return formatEnum(value, options?.enumMap || {}, options?.defaultText || '')

    case 'text':
    default:
      return String(value)
  }
}
