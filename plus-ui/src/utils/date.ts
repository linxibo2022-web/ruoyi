/**
 * 日期工具类
 * 提供日期格式化、转换、计算的通用工具函数
 * 自动兼容两种格式语法:
 * - yyyy-MM-dd HH:mm:ss
 * - YYYY-MM-DD HH:mm:ss
 *
 * 包含以下功能类别:
 * - 日期格式化: 将日期转换为特定格式的字符串 (formatDate, formatTableDate, formatDay, formatRelativeTime, formatDateRange)
 * - 日期解析: 将字符串解析为日期对象 (parseDate)
 * - 当前时间获取: 获取当前时间的各种格式 (getCurrentTime, getCurrentDate, getCurrentDateTime)
 * - 日期范围获取: 获取特定时间范围 (getTimeStamp, getDateRange, getCurrentWeekRange, getCurrentMonthRange, addDateRange)
 * - 日期范围预设: 根据预设类型快速获取日期范围 (getDateRangeByType, initDateRangeFromQuery)
 * - 日期计算: 计算日期之间的关系和差值 (getDaysBetween, isSameDay, getWeekOfYear, dateAdd)
 * - 辅助函数: 数字补零等辅助功能 (padZero)
 */

// ==================== 日期格式化 ====================

/**
 * 数字前补零
 * @param num 数字
 * @param targetLength 目标长度
 * @returns 补零后的字符串
 */
const padZero = (num: number, targetLength: number = 2): string => {
  return num.toString().padStart(targetLength, '0')
}

/**
 * 日期格式化
 * 自动兼容两种格式语法 (yyyy-MM-dd 和 YYYY-MM-DD)
 * @param time 日期
 * @param pattern 格式化模式，默认为 yyyy-MM-dd HH:mm:ss
 * @returns 格式化后的日期字符串
 * @example
 * formatDate(new Date(), 'yyyy-MM-dd') // => 2025-03-29
 * formatDate(new Date(), 'YYYY-MM-DD') // => 2025-03-29
 */
export const formatDate = (time: Date | string | number, pattern: string = 'yyyy-MM-dd HH:mm:ss'): string => {
  if (!time) {
    return ''
  }

  let date: Date
  if (typeof time === 'object') {
    date = time as Date
  } else {
    if (typeof time === 'string') {
      if (/^\d+$/.test(time)) {
        time = Number.parseInt(time)
      } else {
        // 处理 ISO 日期字符串
        time = time.replace(/-/g, '/').replace('T', ' ').replace(/\.\d+/, '')
      }
    }

    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }

  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours()
  const minutes = date.getMinutes()
  const seconds = date.getSeconds()
  const milliseconds = date.getMilliseconds()
  const week = date.getDay()

  // 同时支持两种格式语法
  const formatObj: { [key: string]: number | string } = {
    yyyy: year,
    YYYY: year,
    MM: padZero(month),
    M: month,
    dd: padZero(day),
    DD: padZero(day),
    d: day,
    D: day,
    HH: padZero(hours),
    H: hours,
    mm: padZero(minutes),
    m: minutes,
    ss: padZero(seconds),
    s: seconds,
    SSS: padZero(milliseconds, 3),
    w: week
  }

  return pattern.replace(/(yyyy|YYYY|MM|M|dd|DD|[dD]|HH|H|mm|m|SSS|ss|[sw])/g, (match) => {
    const value = formatObj[match]
    if (match === 'w') {
      // 星期
      return ['日', '一', '二', '三', '四', '五', '六'][value as number]
    }
    return value.toString()
  })
}

/**
 * 表格时间格式化
 * @param cellValue 单元格时间值
 * @param pattern 日期格式，默认为 yyyy-MM-dd HH:mm:ss
 * @returns 格式化后的时间字符串
 */
export const formatTableDate = (cellValue: string, pattern: string = 'yyyy-MM-dd HH:mm:ss'): string => {
  if (!cellValue) return ''
  return formatDate(new Date(cellValue), pattern)
}

/**
 * 日期格式化简化版 - 只返回年月日
 * @param time 日期
 * @returns 格式化后的年月日字符串 (yyyy-MM-dd)
 */
export const formatDay = (time: Date | string | number): string => {
  return formatDate(time, 'yyyy-MM-dd')
}

/**
 * 相对时间格式化
 * @param time 时间
 * @param option 可选格式化模式
 * @returns 相对时间字符串，如"刚刚"、"30分钟前"
 */
export const formatRelativeTime = (time: string | number, option?: string): string => {
  let t: number
  if (`${time}`.length === 10) {
    t = Number.parseInt(time as string) * 1000
  } else {
    t = +time
  }
  const date = new Date(t)
  const now = Date.now()

  const diff = (now - date.getTime()) / 1000

  if (diff < 30) {
    return '刚刚'
  } else if (diff < 3600) {
    // 小于1小时
    return `${Math.ceil(diff / 60)}分钟前`
  } else if (diff < 3600 * 24) {
    // 小于24小时
    return `${Math.ceil(diff / 3600)}小时前`
  } else if (diff < 3600 * 24 * 2) {
    return '1天前'
  }

  if (option) {
    return formatDate(date, option)
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日${date.getHours()}时${date.getMinutes()}分`
  }
}

/**
 * 格式化日期范围为字符串
 * @param dateRange 日期范围
 * @param separator 分隔符
 * @param format 日期格式
 * @returns 格式化后的日期范围字符串
 */
export const formatDateRange = (dateRange: [Date, Date], separator: string = '~', format: string = 'yyyy-MM-dd'): string => {
  if (!dateRange || dateRange.length !== 2) {
    return ''
  }
  return `${formatDate(dateRange[0], format)} ${separator} ${formatDate(dateRange[1], format)}`
}

// ==================== 当前时间获取 ====================

/**
 * 获取当前时间
 * @param pattern 格式化模式，默认为 HH:mm:ss (时分秒)
 * @returns 格式化后的当前时间字符串
 * @example
 * getCurrentTime() // => 22:17:33
 * getCurrentTime('yyyy-MM-dd HH:mm:ss') // => 2025-06-07 22:17:33
 * getCurrentTime('yyyy年MM月dd日') // => 2025年06月07日
 */
export const getCurrentTime = (pattern: string = 'HH:mm:ss'): string => {
  return formatDate(new Date(), pattern)
}

/**
 * 获取当前年月日
 * @returns 当前年月日字符串 (yyyy-MM-dd)
 * @example
 * getCurrentDate() // => 2025-06-07
 */
export const getCurrentDate = (): string => {
  return formatDate(new Date(), 'yyyy-MM-dd')
}

/**
 * 获取当前完整日期时间
 * @returns 当前完整日期时间字符串 (yyyy-MM-dd HH:mm:ss)
 * @example
 * getCurrentDateTime() // => 2025-06-07 22:17:33
 */
export const getCurrentDateTime = (): string => {
  return formatDate(new Date(), 'yyyy-MM-dd HH:mm:ss')
}

// ==================== 日期解析 ====================

/**
 * 解析日期字符串为Date对象
 * @param dateStr 日期字符串
 * @returns Date对象，解析失败返回null
 */
export const parseDate = (dateStr: string): Date | null => {
  if (!dateStr) {
    return null
  }

  const date = new Date(dateStr)
  return Number.isNaN(date.getTime()) ? null : date
}

// ==================== 日期范围获取 ====================

/**
 * 获取时间戳
 * @param type 时间戳类型 'ms'(毫秒) 或 's'(秒)
 * @returns 当前时间的时间戳
 */
export const getTimeStamp = (type: 'ms' | 's' = 'ms'): number => {
  const now = new Date().getTime()
  return type === 'ms' ? now : Math.floor(now / 1000)
}

/**
 * 获取日期范围
 * @param days 天数，负数表示过去，正数表示未来
 * @returns [开始日期, 结束日期]
 */
export const getDateRange = (days: number): [Date, Date] => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() + 3600 * 1000 * 24 * days)
  return [start, end]
}

/**
 * 获取本周的开始和结束日期
 * @returns [周一日期, 周日日期]
 */
export const getCurrentWeekRange = (): [Date, Date] => {
  const now = new Date()
  const currentDay = now.getDay() || 7 // 周日是0，转为7
  const monday = new Date(now)
  monday.setDate(now.getDate() - (currentDay - 1))
  monday.setHours(0, 0, 0, 0)

  const sunday = new Date(now)
  sunday.setDate(now.getDate() + (7 - currentDay))
  sunday.setHours(23, 59, 59, 999)

  return [monday, sunday]
}

/**
 * 获取本月的开始和结束日期
 * @returns [本月第一天, 本月最后一天]
 */
export const getCurrentMonthRange = (): [Date, Date] => {
  const now = new Date()
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  lastDay.setHours(23, 59, 59, 999)

  return [firstDay, lastDay]
}

/**
 * 添加日期范围
 * @param params 参数对象
 * @param dateRange 日期范围数组
 * @param propName 属性名称（可选）
 * @returns 添加了日期范围的参数对象
 */
export const addDateRange = (params: any, dateRange: any[], propName?: string): any => {
  // 确保 params 属性是一个对象
  if (!params.params || typeof params.params !== 'object' || Array.isArray(params.params)) {
    params.params = {}
  }

  // 设置字段名，propName 首字母转大写
  const beginFieldName = propName ? `begin${propName.charAt(0).toUpperCase() + propName.slice(1)}` : 'beginTime'
  const endFieldName = propName ? `end${propName.charAt(0).toUpperCase() + propName.slice(1)}` : 'endTime'

  // 检查日期范围是否有效
  const isValidRange =
    Array.isArray(dateRange) && dateRange.length === 2 && dateRange[0] && dateRange[1] && dateRange[0] !== '' && dateRange[1] !== ''

  // 如果日期范围有效，添加到参数对象
  if (isValidRange) {
    params.params[beginFieldName] = dateRange[0]
    params.params[endFieldName] = dateRange[1]
  } else {
    // 如果日期范围无效，删除相关属性
    delete params.params[beginFieldName]
    delete params.params[endFieldName]
  }

  return params
}

// ==================== 日期范围预设 ====================

/**
 * 根据日期类型获取日期范围
 * @param dateType 日期类型: 'today' | 'yesterday' | 'week' | 'month' | 'year'
 * @returns 日期范围 [开始时间, 结束时间]，如果类型无效返回 null
 * @example
 * getDateRangeByType('today') // => ['2025-10-05T00:00:00.000Z', '2025-10-05T23:59:59.999Z']
 * getDateRangeByType('week') // => ['2025-09-29T00:00:00.000Z', '2025-10-05T23:59:59.999Z']
 * getDateRangeByType('month') // => ['2025-10-01T00:00:00.000Z', '2025-10-05T23:59:59.999Z']
 */
export const getDateRangeByType = (dateType: string): [string, string] | null => {
  if (!dateType) return null

  const today = new Date()
  const endDate = new Date(today)
  endDate.setHours(23, 59, 59, 999)

  let startDate: Date

  switch (dateType) {
    case 'today':
      startDate = new Date(today)
      startDate.setHours(0, 0, 0, 0)
      break

    case 'yesterday':
      startDate = new Date(today)
      startDate.setDate(startDate.getDate() - 1)
      startDate.setHours(0, 0, 0, 0)
      endDate.setDate(endDate.getDate() - 1)
      endDate.setHours(23, 59, 59, 999)
      break

    case 'week':
      startDate = new Date(today)
      const dayOfWeek = startDate.getDay()
      const daysToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1
      startDate.setDate(startDate.getDate() - daysToMonday)
      startDate.setHours(0, 0, 0, 0)
      break

    case 'month':
      startDate = new Date(today.getFullYear(), today.getMonth(), 1)
      break

    case 'year':
      startDate = new Date(today.getFullYear(), 0, 1)
      break

    default:
      return null
  }

  // 使用 formatDate 返回普通格式，而不是 ISO 格式
  return [formatDate(startDate, 'yyyy-MM-dd HH:mm:ss'), formatDate(endDate, 'yyyy-MM-dd HH:mm:ss')]
}

/**
 * 从路由查询参数初始化日期范围
 * @param query 路由查询对象
 * @param dateParamName 日期参数名称，默认为 'dateType'
 * @returns 日期范围数组或空数组
 * @example
 * // 在组件中使用
 * const route = useRoute()
 * const dateRange = ref(initDateRangeFromQuery(route.query))
 *
 * // 当 route.query.dateType = 'today' 时
 * // 返回: ['2025-10-05T00:00:00.000Z', '2025-10-05T23:59:59.999Z']
 *
 * // 当 route.query 没有 dateType 时
 * // 返回: ['', '']
 */
export const initDateRangeFromQuery = (query: Record<string, any>, dateParamName: string = 'dateType'): [string, string] | ['', ''] => {
  const dateType = query[dateParamName]
  if (!dateType) return ['', '']

  const range = getDateRangeByType(dateType as string)
  return range || ['', '']
}

// ==================== 日期计算 ====================

/**
 * 计算两个日期之间的天数
 * @param start 开始日期
 * @param end 结束日期
 * @returns 天数
 */
export const getDaysBetween = (start: Date, end: Date): number => {
  const startTime = new Date(start.getFullYear(), start.getMonth(), start.getDate()).getTime()
  const endTime = new Date(end.getFullYear(), end.getMonth(), end.getDate()).getTime()
  const days = (endTime - startTime) / (1000 * 60 * 60 * 24)
  return Math.abs(Math.round(days))
}

/**
 * 判断是否为同一天
 * @param date1 日期1
 * @param date2 日期2
 * @returns 是否为同一天
 */
export const isSameDay = (date1: Date, date2: Date): boolean => {
  return date1.getFullYear() === date2.getFullYear() && date1.getMonth() === date2.getMonth() && date1.getDate() === date2.getDate()
}

/**
 * 获取日期是一年中的第几周
 * @param date 日期
 * @returns 周数
 */
export const getWeekOfYear = (date: Date): number => {
  const firstDayOfYear = new Date(date.getFullYear(), 0, 1)
  const pastDaysOfYear = (date.getTime() - firstDayOfYear.getTime()) / 86400000
  return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7)
}

/**
 * 日期加减
 * @param date 基准日期
 * @param type 类型：'day', 'month', 'year'
 * @param value 增量值，可为负数
 * @returns 新日期
 */
export const dateAdd = (date: Date, type: 'day' | 'month' | 'year', value: number): Date => {
  const result = new Date(date)

  switch (type) {
    case 'day':
      result.setDate(result.getDate() + value)
      break
    case 'month':
      result.setMonth(result.getMonth() + value)
      break
    case 'year':
      result.setFullYear(result.getFullYear() + value)
      break
  }

  return result
}
