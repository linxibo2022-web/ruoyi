/**
 * 主题颜色接口
 * 定义主题的亮色和暗色变体
 */
export interface ThemeColors {
  /** 主题主色调 */
  primary: string
  /** 亮色变体(9个等级) */
  lightColors: string[]
  /** 暗色变体(9个等级) */
  darkColors: string[]
}

import { hexToRgb, rgbToHex, lightenColor, darkenColor } from '@/utils/colors'

/**
 * 主题管理钩子 (useTheme)
 *
 * 提供对应用主题的响应式管理功能，包括颜色变量设置、主题切换和自定义。
 *
 * 包含以下功能：
 * - 主题设置: 设置新的主题色并应用到整个应用 (setTheme)
 * - 主题重置: 将主题重置为系统默认值 (resetTheme)
 * - 颜色变体: 生成亮色和暗色变体 (基于colors工具类)
 * - 主题生成: 基于主色生成完整主题色系 (generateThemeColors)
 * - 主题应用: 将主题色应用到CSS变量 (applyThemeColors)
 *
 * @returns 返回主题相关的状态和方法
 *
 * @example
 * // 在组件中使用
 * import { useTheme } from '@/composables/useTheme';
 *
 * export default defineComponent({
 *   setup() {
 *     const { currentTheme, setTheme } = useTheme();
 *
 *     // 手动设置主题
 *     const changeTheme = () => {
 *       setTheme('#1890ff');
 *     };
 *
 *     return {
 *       currentTheme,
 *       changeTheme
 *     };
 *   }
 * });
 */
export const useTheme = () => {
  // 获取布局状态管理
  const layout = useLayout()

  /**当前主题色*/
  const currentTheme: Ref<string> = layout.theme

  /**
   * 将十六进制颜色转换为带透明度的颜色
   * @param hex 十六进制颜色 (例如：#282828 —> rgba(28, 28, 28, 0.5))
   * @param alpha 透明度 (0-1)
   * @returns 带透明度的十六进制颜色
   */
  const addAlphaToHex = (hex: string, alpha: number = 1): string => {
    if (alpha >= 1) return hex
    const alphaHex = Math.round(alpha * 255)
      .toString(16)
      .padStart(2, '0')
    return `${hex}${alphaHex}`
  }

  /**
   * 生成亮色变体 (使用colors工具类)
   * @param color 基础颜色
   * @param level 亮度级别 (0-1)，越大越亮
   * @returns 亮色变体的十六进制颜色
   */
  const getLightColor = (color: string, level: number): string => {
    return lightenColor(color, level)
  }

  /**
   * 生成暗色变体 (使用colors工具类)
   * @param color 基础颜色
   * @param level 暗度级别 (0-1)，越大越暗
   * @returns 暗色变体的十六进制颜色
   */
  const getDarkColor = (color: string, level: number): string => {
    return darkenColor(color, level)
  }

  /**
   * 为指定颜色生成所有变体
   * @param color 基础颜色
   * @returns 主题颜色对象，包含主色和变体
   */
  const generateThemeColors = (color: string): ThemeColors => {
    // 生成9个亮色变体
    const lightColors = Array.from({ length: 9 }, (_, i) => getLightColor(color, (i + 1) / 10))

    // 生成9个暗色变体
    const darkColors = Array.from({ length: 9 }, (_, i) => getDarkColor(color, (i + 1) / 10))

    return {
      primary: color,
      lightColors,
      darkColors
    }
  }

  /**
   * 应用主题颜色到CSS变量
   * @param color 主题颜色
   */
  const applyThemeColors = (color: string): void => {
    // 设置主色
    document.documentElement.style.setProperty('--el-color-primary', color)

    // 设置亮色变体
    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.setProperty(`--el-color-primary-light-${i}`, getLightColor(color, i / 10))
    }

    // 设置暗色变体
    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.setProperty(`--el-color-primary-dark-${i}`, getDarkColor(color, i / 10))
    }

    // 更新当前主题变量
    currentTheme.value = color
  }

  /**
   * 设置主题色
   * @param color 十六进制颜色字符串
   * @description 设置新的主题色并应用到整个应用
   */
  const setTheme = (color: string): void => {
    // 更新布局状态管理中的主题
    layout.theme.value = color
    // 应用主题颜色
    applyThemeColors(color)
  }

  /**
   * 重置为默认主题
   * @description 将主题重置为系统默认值
   */
  const resetTheme = (): void => {
    const defaultTheme = layout.theme.value
    applyThemeColors(defaultTheme)
  }

  // 初始化主题
  watchEffect(() => {
    applyThemeColors(layout.theme.value)
  })

  return {
    // 状态
    currentTheme,

    // 主题操作方法
    setTheme,
    resetTheme,

    // 颜色工具方法
    getLightColor,
    getDarkColor,
    generateThemeColors,
    addAlphaToHex
  }
}
