/**
 * 基础图表Hook
 *
 * 提供图表组件的核心功能：
 * - ECharts实例管理
 * - 主题切换支持
 * - 响应式处理
 * - 空状态管理
 * - 样式配置生成
 */

import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { storeToRefs } from 'pinia'
import { getCssVar } from '@/utils/colors'

/**动画配置常量*/
export const ANIMATION_CONFIG = {
  /**默认动画延迟*/
  DEFAULT_DELAY: 50,
  /**默认动画持续时间*/
  DEFAULT_DURATION: 1500,
  /**默认缓动函数*/
  DEFAULT_EASING: 'quarticOut' as const,
  /**步进动画延迟*/
  STEP_DELAY: 200
}

/**响应式处理延迟配置*/
export const RESIZE_DELAYS = {
  /**普通resize延迟*/
  NORMAL: [50, 100, 200, 350] as const,
  /**菜单resize延迟*/
  MENU: [50, 100, 200] as const,
  /**防抖延迟*/
  DEBOUNCE: 100
}

/**图表容器样式常量*/
export const CHART_CONTAINER_STYLES = {
  /**空状态样式*/
  EMPTY_STATE: {
    position: 'absolute',
    top: '0',
    left: '0',
    right: '0',
    bottom: '0',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '14px',
    background: 'transparent',
    zIndex: '10',
    gap: '8px'
  } as const,
  /**加载状态样式*/
  LOADING_STATE: {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    zIndex: '10'
  } as const
}

/**获取图表主题配置*/
export const getChartThemeConfig = (): ChartThemeConfig => ({
  /**默认图表高度*/
  chartHeight: '16rem',
  /**字体大小*/
  fontSize: 13,
  /**字体颜色*/
  fontColor: '#999',
  /**主题色*/
  themeColor: getCssVar('--el-color-primary-light-1'),
  /**色彩组*/
  colors: [getCssVar('--el-color-primary-light-1'), '#4ABEFF', '#EDF2FF', '#14DEBA', '#FFAF20', '#FA8A6C', '#FFAF20']
})

/**图表配置选项接口*/
export interface UseChartOptions {
  /**初始化选项*/
  initOptions?: EChartsOption
  /**初始化延迟*/
  initDelay?: number
  /**可见性阈值*/
  threshold?: number
  /**是否自动主题*/
  autoTheme?: boolean
}

/**图表主题配置接口*/
export interface ChartThemeConfig {
  /**默认图表高度*/
  chartHeight: string
  /**字体大小*/
  fontSize: number
  /**字体颜色*/
  fontColor: string
  /**主题色*/
  themeColor: string
  /**色彩组*/
  colors: string[]
}

/**
 * 获取图表主题配置
 * @returns 图表主题配置对象
 */
export const useChartTheme = (): ChartThemeConfig => getChartThemeConfig()

/**
 * 基础图表Hook
 * @param options 配置选项
 * @returns 图表相关的响应式数据和方法
 */
export function useChart(options: UseChartOptions = {}) {
  const { initOptions, initDelay = 0, threshold = 0.1, autoTheme = true } = options
  const { t } = useI18n()

  // 获取统一布局状态管理
  const layout = useLayout()

  // 从layout中获取所需状态
  const { sidebar, device, dark } = layout

  // 计算属性：获取侧边栏是否打开
  const menuOpen = computed(() => sidebar.value.opened)

  // 计算属性：获取设备类型作为菜单类型
  const menuType = computed(() => device.value)

  // 计算属性：获取暗黑模式状态
  const isDark = computed(() => dark.value)

  // 响应式引用
  const chartRef = ref<HTMLElement>()

  // 内部状态
  let chart: echarts.ECharts | null = null
  let intersectionObserver: IntersectionObserver | null = null
  let pendingOptions: EChartsOption | null = null
  let resizeTimeoutId: number | null = null
  let resizeFrameId: number | null = null
  let isDestroyed = false
  let emptyStateDiv: HTMLElement | null = null

  /**
   * 清理定时器
   */
  const clearTimers = () => {
    if (resizeTimeoutId) {
      clearTimeout(resizeTimeoutId)
      resizeTimeoutId = null
    }
    if (resizeFrameId) {
      cancelAnimationFrame(resizeFrameId)
      resizeFrameId = null
    }
  }

  /**
   * 使用requestAnimationFrame优化resize处理
   */
  const requestAnimationResize = () => {
    if (resizeFrameId) {
      cancelAnimationFrame(resizeFrameId)
    }
    resizeFrameId = requestAnimationFrame(() => {
      handleResize()
      resizeFrameId = null
    })
  }

  /**
   * 防抖的resize处理（用于窗口resize事件）
   */
  const debouncedResize = () => {
    if (resizeTimeoutId) {
      clearTimeout(resizeTimeoutId)
    }
    resizeTimeoutId = window.setTimeout(() => {
      requestAnimationResize()
      resizeTimeoutId = null
    }, RESIZE_DELAYS.DEBOUNCE)
  }

  /**
   * 多延迟resize处理 - 统一方法
   * @param delays 延迟时间数组
   */
  const multiDelayResize = (delays: readonly number[]) => {
    // 立即调用一次，快速响应
    nextTick(requestAnimationResize)

    // 使用延迟时间，确保图表正确适应变化
    delays.forEach((delay) => {
      setTimeout(requestAnimationResize, delay)
    })
  }

  /**
   * 创建线条样式配置
   * @param color 颜色
   * @param width 宽度
   * @param type 线条类型
   * @returns 线条样式对象
   */
  const createLineStyle = (color: string, width = 1, type?: 'solid' | 'dashed') => ({
    color,
    width,
    ...(type && { type })
  })

  /**
   * 获取坐标轴线样式
   * @param show 是否显示
   * @returns 轴线样式配置
   */
  const getAxisLineStyle = (show: boolean = true) => ({
    show,
    lineStyle: createLineStyle(isDark.value ? '#444' : '#EDEDED')
  })

  /**
   * 获取分割线样式
   * @param show 是否显示
   * @returns 分割线样式配置
   */
  const getSplitLineStyle = (show: boolean = true) => ({
    show,
    lineStyle: createLineStyle(isDark.value ? '#444' : '#EDEDED', 1, 'dashed')
  })

  /**
   * 获取坐标轴标签样式
   * @param show 是否显示
   * @returns 轴标签样式配置
   */
  const getAxisLabelStyle = (show: boolean = true) => {
    const { fontColor, fontSize } = useChartTheme()
    return {
      show,
      color: fontColor,
      fontSize
    }
  }

  /**
   * 获取坐标轴刻度样式
   * @returns 轴刻度样式配置
   */
  const getAxisTickStyle = () => ({
    show: false
  })

  /**
   * 获取动画配置
   * @param animationDelay 动画延迟
   * @param animationDuration 动画持续时间
   * @returns 动画配置对象
   */
  const getAnimationConfig = (
    animationDelay: number = ANIMATION_CONFIG.DEFAULT_DELAY,
    animationDuration: number = ANIMATION_CONFIG.DEFAULT_DURATION
  ) => ({
    animationDelay: (idx: number) => idx * animationDelay + ANIMATION_CONFIG.STEP_DELAY,
    animationDuration: (idx: number) => animationDuration - idx * 50,
    animationEasing: ANIMATION_CONFIG.DEFAULT_EASING
  })

  /**
   * 获取统一的tooltip配置
   * @param trigger 触发方式
   * @param customOptions 自定义配置
   * @returns tooltip配置对象
   */
  const getTooltipStyle = (trigger: 'item' | 'axis' = 'axis', customOptions: any = {}) => ({
    trigger,
    backgroundColor: isDark.value ? 'rgba(0, 0, 0, 0.8)' : 'rgba(255, 255, 255, 0.9)',
    borderColor: isDark.value ? '#333' : '#ddd',
    borderWidth: 1,
    textStyle: {
      color: isDark.value ? '#fff' : '#333'
    },
    ...customOptions
  })

  /**
   * 获取统一的图例配置
   * @param position 图例位置
   * @param customOptions 自定义配置
   * @returns 图例配置对象
   */
  const getLegendStyle = (position: 'bottom' | 'top' | 'left' | 'right' = 'bottom', customOptions: any = {}) => {
    const baseConfig = {
      textStyle: {
        color: isDark.value ? '#fff' : '#333'
      },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 20,
      ...customOptions
    }

    // 根据位置设置不同的配置
    switch (position) {
      case 'bottom':
        return {
          ...baseConfig,
          bottom: 0,
          left: 'center',
          orient: 'horizontal',
          icon: 'roundRect'
        }
      case 'top':
        return {
          ...baseConfig,
          top: 0,
          left: 'center',
          orient: 'horizontal',
          icon: 'roundRect'
        }
      case 'left':
        return {
          ...baseConfig,
          left: 0,
          top: 'center',
          orient: 'vertical',
          icon: 'roundRect'
        }
      case 'right':
        return {
          ...baseConfig,
          right: 0,
          top: 'center',
          orient: 'vertical',
          icon: 'roundRect'
        }
      default:
        return baseConfig
    }
  }

  /**
   * 根据图例位置计算grid配置
   * @param showLegend 是否显示图例
   * @param legendPosition 图例位置
   * @param baseGrid 基础grid配置
   * @returns grid配置对象
   */
  const getGridWithLegend = (showLegend: boolean, legendPosition: 'bottom' | 'top' | 'left' | 'right' = 'bottom', baseGrid: any = {}) => {
    const defaultGrid = {
      top: 15,
      right: 15,
      bottom: 8,
      left: 0,
      containLabel: true,
      ...baseGrid
    }

    if (!showLegend) {
      return defaultGrid
    }

    // 根据图例位置调整grid
    switch (legendPosition) {
      case 'bottom':
        return { ...defaultGrid, bottom: 40 }
      case 'top':
        return { ...defaultGrid, top: 40 }
      case 'left':
        return { ...defaultGrid, left: 120 }
      case 'right':
        return { ...defaultGrid, right: 120 }
      default:
        return defaultGrid
    }
  }

  /**
   * 空状态管理器
   */
  const emptyStateManager = {
    /**
     * 创建空状态展示
     */
    create: () => {
      if (!chartRef.value || emptyStateDiv) return

      emptyStateDiv = document.createElement('div')

      // 应用样式
      Object.assign(emptyStateDiv.style, CHART_CONTAINER_STYLES.EMPTY_STATE)
      emptyStateDiv.style.color = isDark.value ? '#666' : '#999'

      emptyStateDiv.innerHTML = `
        <i class="iconfont-sys" style="font-size: 48px; color: ${isDark.value ? '#555' : '#ccc'};">&#xe6da;</i>
        <span>${t('chart.noData')}</span>
      `

      // 确保父容器有相对定位
      if (chartRef.value.style.position !== 'relative' && chartRef.value.style.position !== 'absolute') {
        chartRef.value.style.position = 'relative'
      }

      chartRef.value.appendChild(emptyStateDiv)
    },

    /**
     * 移除空状态展示
     */
    remove: () => {
      if (emptyStateDiv && chartRef.value) {
        chartRef.value.removeChild(emptyStateDiv)
        emptyStateDiv = null
      }
    },

    /**
     * 更新空状态样式（主题切换时使用）
     */
    updateStyle: () => {
      if (emptyStateDiv) {
        emptyStateDiv.style.color = isDark.value ? '#666' : '#999'
        const iconElement = emptyStateDiv.querySelector('i.iconfont-sys')
        if (iconElement) {
          ;(iconElement as HTMLElement).style.color = isDark.value ? '#555' : '#ccc'
        }
      }
    }
  }

  /**
   * 创建IntersectionObserver
   */
  const createIntersectionObserver = () => {
    if (intersectionObserver || !chartRef.value) return

    intersectionObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && pendingOptions && !isDestroyed) {
            // 使用requestAnimationFrame确保在下一帧初始化图表
            requestAnimationFrame(() => {
              if (!isDestroyed && pendingOptions) {
                try {
                  // 元素变为可见，初始化图表
                  if (!chart) {
                    chart = echarts.init(entry.target as HTMLElement)
                  }

                  // 触发自定义事件，让组件处理动画逻辑
                  const event = new CustomEvent('chartVisible', {
                    detail: { options: pendingOptions }
                  })
                  entry.target.dispatchEvent(event)

                  pendingOptions = null
                  cleanupIntersectionObserver()
                } catch (error) {
                  console.error('图表初始化失败:', error)
                }
              }
            })
          }
        })
      },
      { threshold }
    )

    intersectionObserver.observe(chartRef.value)
  }

  /**
   * 清理IntersectionObserver
   */
  const cleanupIntersectionObserver = () => {
    if (intersectionObserver) {
      intersectionObserver.disconnect()
      intersectionObserver = null
    }
  }

  /**
   * 检查容器是否可见
   * @param element HTML元素
   * @returns 是否可见
   */
  const isContainerVisible = (element: HTMLElement): boolean => {
    const rect = element.getBoundingClientRect()
    return rect.width > 0 && rect.height > 0 && rect.top < window.innerHeight && rect.bottom > 0
  }

  /**
   * 图表初始化核心逻辑
   * @param options ECharts配置选项
   */
  const performChartInit = (options: EChartsOption) => {
    if (!chart && chartRef.value && !isDestroyed) {
      chart = echarts.init(chartRef.value)
    }
    if (chart && !isDestroyed) {
      chart.setOption(options)
      pendingOptions = null
    }
  }

  /**
   * 初始化图表
   * @param options ECharts配置选项
   * @param isEmpty 是否为空状态
   */
  const initChart = (options: EChartsOption = {}, isEmpty: boolean = false) => {
    if (!chartRef.value || isDestroyed) return

    const mergedOptions = { ...initOptions, ...options }

    try {
      if (isEmpty) {
        // 处理空数据情况 - 显示自定义空状态div
        if (chart) {
          chart.clear()
        }
        emptyStateManager.create()
        return
      } else {
        // 有数据时移除空状态div
        emptyStateManager.remove()
      }

      if (isContainerVisible(chartRef.value)) {
        // 容器可见，正常初始化
        if (initDelay > 0) {
          setTimeout(() => performChartInit(mergedOptions), initDelay)
        } else {
          performChartInit(mergedOptions)
        }
      } else {
        // 容器不可见，保存选项并设置监听器
        pendingOptions = mergedOptions
        createIntersectionObserver()
      }
    } catch (error) {
      console.error('图表初始化失败:', error)
    }
  }

  /**
   * 更新图表
   * @param options ECharts配置选项
   */
  const updateChart = (options: EChartsOption) => {
    if (isDestroyed) return

    try {
      if (!chart) {
        // 如果图表不存在，先初始化
        initChart(options)
        return
      }
      chart.setOption(options)
    } catch (error) {
      console.error('图表更新失败:', error)
    }
  }

  /**
   * 处理窗口大小变化
   */
  const handleResize = () => {
    if (chart && !isDestroyed) {
      try {
        chart.resize()
      } catch (error) {
        console.error('图表resize失败:', error)
      }
    }
  }

  /**
   * 销毁图表
   */
  const destroyChart = () => {
    isDestroyed = true

    if (chart) {
      try {
        chart.dispose()
      } catch (error) {
        console.error('图表销毁失败:', error)
      } finally {
        chart = null
      }
    }

    // 清理空状态div
    emptyStateManager.remove()
    cleanupIntersectionObserver()
    clearTimers()
    pendingOptions = null
  }

  /**
   * 获取图表实例
   * @returns ECharts实例
   */
  const getChartInstance = () => chart

  /**
   * 获取图表是否已初始化
   * @returns 是否已初始化
   */
  const isChartInitialized = () => chart !== null

  // 监听菜单收缩时，重新计算图表大小
  watch(menuOpen, () => multiDelayResize(RESIZE_DELAYS.NORMAL))

  // 菜单类型变化触发
  watch(menuType, () => {
    nextTick(requestAnimationResize)
    setTimeout(() => multiDelayResize(RESIZE_DELAYS.MENU), 0)
  })

  // 主题变化时重新设置图表选项
  if (autoTheme) {
    watch(isDark, () => {
      // 更新空状态样式
      emptyStateManager.updateStyle()

      if (chart && !isDestroyed) {
        // 使用requestAnimationFrame优化主题更新
        requestAnimationFrame(() => {
          if (chart && !isDestroyed) {
            const currentOptions = chart.getOption()
            if (currentOptions) {
              updateChart(currentOptions as EChartsOption)
            }
          }
        })
      }
    })
  }

  // 生命周期管理
  onMounted(() => {
    window.addEventListener('resize', debouncedResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', debouncedResize)
  })

  onUnmounted(() => {
    destroyChart()
  })

  return {
    // 响应式数据
    isDark,
    chartRef,

    // 图表管理方法
    initChart,
    updateChart,
    handleResize,
    destroyChart,
    getChartInstance,
    isChartInitialized,

    // 样式配置方法
    getAxisLineStyle,
    getSplitLineStyle,
    getAxisLabelStyle,
    getAxisTickStyle,
    getAnimationConfig,
    getTooltipStyle,
    getLegendStyle,
    getGridWithLegend,

    // 工具方法
    emptyStateManager,
    useChartTheme
  }
}
