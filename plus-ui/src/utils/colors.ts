/**
 * 颜色处理相关工具函数
 *
 * 包含以下功能：
 * - 颜色验证: 验证 hex 和 RGB 颜色格式 (isValidHex, isValidRgb)
 * - 颜色转换: hex ↔ RGB 相互转换 (hexToRgb, rgbToHex, hexToRgba)
 * - 颜色混合: 按比例混合两种颜色 (blendColor)
 * - 颜色调节: 调亮调暗颜色 (lightenColor, darkenColor)
 * - 主题设置: Element Plus 主题颜色设置 (setThemeColor, handleThemeColor)
 * - CSS 变量: 获取 CSS 变量值 (getCssVar)
 */

import { ElMessage } from 'element-plus'

/**
 * 默认颜色常量
 */
const DEFAULT_COLOR = '#5d87ff' // 系统当前默认主题色
const DEFAULT_RGB = [93, 135, 255] // 对应上面颜色的RGB值

/**
 * 颜色转换结果接口
 */
interface RgbaResult {
  red: number
  green: number
  blue: number
  rgba: string
}

/**
 * 获取 CSS 变量值
 * @param {string} name - CSS 变量名
 * @returns {string} - CSS 变量值
 */
export const getCssVar = (name: string): string => {
  return getComputedStyle(document.documentElement).getPropertyValue(name)
}

/**
 * 验证 hex 颜色格式
 * @param {string} hex - hex 颜色值
 * @returns {boolean} - 是否为有效的 hex 颜色
 */
export const isValidHex = (hex: string): boolean => {
  if (!hex) return false
  const cleanHex = hex.trim().replace(/^#/, '')
  return /^[0-9A-Fa-f]{3}$|^[0-9A-Fa-f]{6}$/.test(cleanHex)
}

/**
 * 验证 RGB 颜色值
 * @param {number} r - 红色值
 * @param {number} g - 绿色值
 * @param {number} b - 蓝色值
 * @returns {boolean} - 是否为有效的 RGB 值
 */
export const isValidRgb = (r: number, g: number, b: number): boolean => {
  const isValid = (value: number) => Number.isInteger(value) && value >= 0 && value <= 255
  return isValid(r) && isValid(g) && isValid(b)
}

/**
 * 将 hex 颜色转换为 RGB 数组
 * @param {string} hex - hex 颜色值
 * @returns {number[]} - RGB 数组 [r, g, b]
 */
export const hexToRgb = (hex: string): number[] => {
  // 边界处理：如果传入无效值，使用默认颜色
  if (!hex || typeof hex !== 'string' || !isValidHex(hex)) {
    console.warn(`Invalid hex color "${hex}", using default color "${DEFAULT_COLOR}"`)
    return [...DEFAULT_RGB]
  }

  let cleanHex = hex.trim().replace(/^#/, '').toUpperCase()

  // 处理缩写形式 (#FFF -> #FFFFFF)
  if (cleanHex.length === 3) {
    cleanHex = cleanHex
      .split('')
      .map((char) => char.repeat(2))
      .join('')
  }

  const hexPairs = cleanHex.match(/\w\w/g)
  if (!hexPairs) {
    console.warn(`Failed to parse hex color "${hex}", using default color "${DEFAULT_COLOR}"`)
    return [...DEFAULT_RGB]
  }

  return hexPairs.map((pair) => parseInt(pair, 16))
}

/**
 * 将 RGB 颜色转换为 hex
 * @param {number} r - 红色值 (0-255)
 * @param {number} g - 绿色值 (0-255)
 * @param {number} b - 蓝色值 (0-255)
 * @returns {string} - hex 颜色值
 */
export const rgbToHex = (r: number, g: number, b: number): string => {
  // 边界处理：修复无效的RGB值
  const fixRgbValue = (value: number): number => {
    if (typeof value !== 'number' || isNaN(value)) return 0
    return Math.max(0, Math.min(255, Math.round(value)))
  }

  const fixedR = fixRgbValue(r)
  const fixedG = fixRgbValue(g)
  const fixedB = fixRgbValue(b)

  // 如果原始值有问题，给出警告
  if (fixedR !== r || fixedG !== g || fixedB !== b) {
    console.warn(`Invalid RGB values (${r}, ${g}, ${b}), fixed to (${fixedR}, ${fixedG}, ${fixedB})`)
  }

  const toHex = (value: number) => {
    const hex = value.toString(16)
    return hex.length === 1 ? `0${hex}` : hex
  }

  return `#${toHex(fixedR)}${toHex(fixedG)}${toHex(fixedB)}`
}

/**
 * 将 hex 颜色转换为 RGBA
 * @param {string} hex - hex 颜色值 (支持 #FFF 或 #FFFFFF 格式)
 * @param {number} opacity - 透明度 (0-1)
 * @returns {RgbaResult} - 包含 RGB 值和 RGBA 字符串的对象
 */
export const hexToRgba = (hex: string, opacity: number): RgbaResult => {
  // 边界处理：确保有效的透明度值
  const validOpacity = typeof opacity === 'number' && !isNaN(opacity) ? Math.max(0, Math.min(1, opacity)) : 1

  // hexToRgb 已经包含了边界处理，会返回有效的RGB数组
  const [red, green, blue] = hexToRgb(hex)
  const rgba = `rgba(${red}, ${green}, ${blue}, ${validOpacity.toFixed(2)})`

  return { red, green, blue, rgba }
}

/**
 * 混合两种颜色
 * @param {string} color1 - 第一个颜色
 * @param {string} color2 - 第二个颜色
 * @param {number} ratio - 混合比例 (0-1)
 * @returns {string} - 混合后的颜色
 */
export const blendColor = (color1: string, color2: string, ratio: number): string => {
  // 边界处理：确保有效的混合比例
  const validRatio = typeof ratio === 'number' && !isNaN(ratio) ? Math.max(0, Math.min(1, ratio)) : 0.5

  // hexToRgb 已经包含了边界处理，会返回有效的RGB数组
  const rgb1 = hexToRgb(color1)
  const rgb2 = hexToRgb(color2)

  const blendedRgb = rgb1.map((value1, index) => {
    const value2 = rgb2[index]
    return Math.round(value1 * (1 - validRatio) + value2 * validRatio)
  })

  return rgbToHex(blendedRgb[0], blendedRgb[1], blendedRgb[2])
}

/**
 * 调亮颜色
 * @param {string} color - 原始颜色
 * @param {number} level - 调亮程度 (0-1)
 * @param {boolean} isDark - 是否为暗色主题
 * @returns {string} - 调亮后的颜色
 */
export const lightenColor = (color: string, level: number, isDark: boolean = false): string => {
  // 边界处理：确保有效的调亮级别
  const validLevel = typeof level === 'number' && !isNaN(level) ? Math.max(0, Math.min(1, level)) : 0.1

  // 如果是暗色主题，使用darkenColor处理
  if (isDark) {
    return darkenColor(color, validLevel)
  }

  // hexToRgb 已经包含了边界处理，会返回有效的RGB数组
  const rgb = hexToRgb(color)
  const lightRgb = rgb.map((value) => Math.floor((255 - value) * validLevel + value))

  return rgbToHex(lightRgb[0], lightRgb[1], lightRgb[2])
}

/**
 * 调暗颜色
 * @param {string} color - 原始颜色
 * @param {number} level - 调暗程度 (0-1)
 * @returns {string} - 调暗后的颜色
 */
export const darkenColor = (color: string, level: number): string => {
  // 边界处理：确保有效的调暗级别
  const validLevel = typeof level === 'number' && !isNaN(level) ? Math.max(0, Math.min(1, level)) : 0.1

  // hexToRgb 已经包含了边界处理，会返回有效的RGB数组
  const rgb = hexToRgb(color)
  const darkRgb = rgb.map((value) => Math.floor(value * (1 - validLevel)))

  return rgbToHex(darkRgb[0], darkRgb[1], darkRgb[2])
}
