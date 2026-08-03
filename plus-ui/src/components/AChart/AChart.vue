<!--
基础图表引擎组件
职责：
- 管理ECharts实例的生命周期
- 提供统一的加载和空状态展示
- 处理主题切换和响应式调整
- 统一的事件处理和传递

该组件不直接对外使用，仅作为其他图表组件的基础
-->
<template>
  <div ref="chartContainer" class="a-chart" :style="containerStyle">
    <!-- 加载状态 -->
    <div v-if="loading" class="chart-loading">
      <slot name="loading">
        <div class="loading-content">
          <i class="loading-icon"></i>
          <span class="loading-text">{{ t('filePreview.loading') }}</span>
        </div>
      </slot>
    </div>

    <!-- 空状态 -->
    <div v-else-if="empty" class="chart-empty">
      <slot name="empty">
        <div class="empty-content">
          <Icon code="empty" size="2xl"></Icon>
          <span class="empty-text">{{ t('message.noData') }}</span>
        </div>
      </slot>
    </div>

    <!-- 图表容器 -->
    <div v-else ref="chartRef" class="chart-content"></div>
  </div>
</template>

<script setup lang="ts" name="AChart">
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { getChartThemeConfig } from './composables/useChart'

const { t } = useI18n()
const layout = useLayout()

/**数据项通用接口*/
export interface ChartDataItem {
  /**数据名称*/
  name: string
  /**数据值*/
  value: number
  /**自定义颜色*/
  color?: string
}

/**多系列数据项接口*/
export interface SeriesDataItem {
  /**系列名称*/
  name: string
  /**数据数组*/
  data: number[]

  /**自定义配置*/
  [key: string]: any
}

/**基础图表属性接口 - 所有图表组件的通用属性*/
export interface BaseChartProps {
  /**图表高度，支持CSS单位*/
  height?: string
  /**是否显示加载状态*/
  loading?: boolean
  /**是否为空状态*/
  isEmpty?: boolean
  /**主题色彩配置*/
  colors?: string[]
  /**是否显示坐标轴标签*/
  showAxisLabel?: boolean
  /**是否显示坐标轴线*/
  showAxisLine?: boolean
  /**是否显示分割线*/
  showSplitLine?: boolean
  /**是否显示提示框*/
  showTooltip?: boolean
  /**是否显示图例*/
  showLegend?: boolean
  /**图例位置*/
  legendPosition?: 'top' | 'bottom' | 'left' | 'right'
}

/**基础图表组件属性接口*/
interface AChartProps {
  /**图表配置*/
  config?: EChartsOption
  /**图表高度*/
  height?: string
  /**是否显示加载状态*/
  loading?: boolean
  /**是否为空状态*/
  empty?: boolean
  /**是否禁用*/
  disabled?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AChartProps>(), {
  config: () => ({}),
  height: '16rem',
  loading: false,
  empty: false,
  disabled: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: echarts.ECharts]
  /**图表更新完成*/
  chartUpdated: [chart: echarts.ECharts]
  /**图表点击事件*/
  chartClick: [params: any]
  /**图表销毁*/
  chartDestroyed: []
}>()

/**响应式引用*/
const chartContainer = ref<HTMLElement>()
const chartRef = ref<HTMLElement>()

/**图表实例*/
let chartInstance: echarts.ECharts | null = null

/**是否已销毁*/
let isDestroyed = false

/**容器样式*/
const containerStyle = computed(() => ({
  height: props.height,
  width: '100%',
  position: 'relative' as const
}))

/**
 * 初始化图表实例
 */
const initChart = async () => {
  if (!chartRef.value || isDestroyed || chartInstance) return

  try {
    // 创建ECharts实例
    chartInstance = echarts.init(chartRef.value)

    // 绑定点击事件
    chartInstance.on('click', (params) => {
      emit('chartClick', params)
    })

    // 设置配置
    if (props.config) {
      chartInstance.setOption(props.config)
    }

    // 触发准备就绪事件
    emit('chartReady', chartInstance)
  } catch (error) {
    console.error('图表初始化失败:', error)
  }
}

/**
 * 更新图表配置
 */
const updateChart = () => {
  if (!chartInstance || isDestroyed) return

  try {
    if (props.config) {
      chartInstance.setOption(props.config)
      emit('chartUpdated', chartInstance)
    }
  } catch (error) {
    console.error('图表更新失败:', error)
  }
}

/**
 * 重置图表尺寸
 */
const resizeChart = () => {
  if (chartInstance && !isDestroyed) {
    try {
      chartInstance.resize()
    } catch (error) {
      console.error('图表resize失败:', error)
    }
  }
}

/**
 * 销毁图表实例
 */
const destroyChart = () => {
  if (chartInstance) {
    try {
      chartInstance.dispose()
      emit('chartDestroyed')
    } catch (error) {
      console.error('图表销毁失败:', error)
    } finally {
      chartInstance = null
    }
  }
  isDestroyed = true
}

/**
 * 获取图表实例
 * @returns ECharts实例
 */
const getChartInstance = (): echarts.ECharts | null => {
  return chartInstance
}

/**
 * 清空图表
 */
const clearChart = () => {
  if (chartInstance && !isDestroyed) {
    try {
      chartInstance.clear()
    } catch (error) {
      console.error('清空图表失败:', error)
    }
  }
}

/**监听配置变化*/
watch(
  () => props.config,
  () => {
    if (!props.loading && !props.empty) {
      updateChart()
    }
  },
  { deep: true }
)

/**监听状态变化*/
watch([() => props.loading, () => props.empty], ([newLoading, newEmpty]) => {
  if (!newLoading && !newEmpty) {
    // 延迟初始化，确保DOM渲染完成
    nextTick(() => {
      if (!chartInstance) {
        initChart()
      } else {
        updateChart()
      }
    })
  } else if (chartInstance && (newLoading || newEmpty)) {
    // 清空图表内容但不销毁实例
    clearChart()
  }
})

/**
 * 使用主题颜色
 */
const useThemeColors = (config: EChartsOption): EChartsOption => {
  if (!config.series || !Array.isArray(config.series)) {
    return config
  }

  const themeColors = getChartThemeConfig().colors

  config.series = config.series.map((series: any, index: number) => {
    const color = themeColors[index % themeColors.length]

    // 1. 处理折线图 (line)
    if (series.type === 'line') {
      // 线条颜色
      if (series.lineStyle) {
        series.lineStyle.color = color
      } else {
        series.lineStyle = { color }
      }

      // 区域填充颜色
      if (series.areaStyle) {
        if (series.areaStyle.color && typeof series.areaStyle.color === 'object') {
          // 处理渐变色
          const gradientColor = series.areaStyle.color as any
          if (gradientColor.colorStops) {
            gradientColor.colorStops = gradientColor.colorStops.map((stop: any, stopIndex: number) => {
              // 计算透明度
              const opacity = stopIndex === 0 ? 0.3 : 0.05
              const alphaHex = Math.round(255 * opacity)
                .toString(16)
                .padStart(2, '0')
              return {
                ...stop,
                color: `${color}${alphaHex}`
              }
            })
          }
        } else {
          // 简单填充色
          series.areaStyle.color = color
        }
      }

      // 标记点颜色
      if (series.itemStyle) {
        series.itemStyle.color = color
      } else {
        series.itemStyle = { color }
      }
    }

    // 2. 处理柱状图 (bar)
    else if (series.type === 'bar') {
      if (series.itemStyle) {
        series.itemStyle.color = color
      } else {
        series.itemStyle = { color }
      }
    }

    // 3. 处理饼图 (pie)
    else if (series.type === 'pie') {
      if (Array.isArray(series.data)) {
        series.data = series.data.map((item: any, dataIndex: number) => {
          const itemColor = themeColors[dataIndex % themeColors.length]
          if (typeof item === 'object') {
            return {
              ...item,
              itemStyle: {
                ...item.itemStyle,
                color: itemColor
              }
            }
          }
          return item
        })
      }
    }

    // 4. 处理雷达图 (radar)
    else if (series.type === 'radar') {
      if (series.lineStyle) {
        series.lineStyle.color = color
      } else {
        series.lineStyle = { color }
      }

      if (series.itemStyle) {
        series.itemStyle.color = color
      } else {
        series.itemStyle = { color }
      }

      if (series.areaStyle) {
        series.areaStyle.color = `${color}33` // 添加20%透明度
      }
    }

    // 5. 处理散点图 (scatter)
    else if (series.type === 'scatter') {
      if (series.itemStyle) {
        series.itemStyle.color = color
      } else {
        series.itemStyle = { color }
      }
    }

    // 6. 处理K线图 (candlestick)
    else if (series.type === 'candlestick') {
      const upColor = themeColors[0] || '#ef5350'
      const downColor = themeColors[1] || '#26a69a'

      series.itemStyle = {
        ...series.itemStyle,
        color: upColor,
        color0: downColor,
        borderColor: upColor,
        borderColor0: downColor
      }
    }

    // 7. 处理地图 (map)
    else if (series.type === 'map') {
      if (series.itemStyle && series.itemStyle.areaColor) {
        // 保持地图的渐变色配置，只更新基础色
        if (typeof series.itemStyle.areaColor === 'object' && series.itemStyle.areaColor.colorStops) {
          const gradient = series.itemStyle.areaColor as any
          gradient.colorStops = gradient.colorStops.map((stop: any, stopIndex: number) => ({
            ...stop,
            color:
              stopIndex === 0
                ? `${color}4D` // 30% opacity
                : `${color}E6` // 90% opacity
          }))
        }
      }
    }

    // 8. 通用处理 - 如果没有匹配到具体类型，设置基础颜色
    else {
      if (series.itemStyle) {
        series.itemStyle.color = color
      } else {
        series.itemStyle = { color }
      }
    }

    return series
  })

  return config
}

/**
 * 监听主题变化，更新图表主题颜色
 */
watch(
  () => layout.theme.value,
  (newTheme) => {
    if (chartInstance && !isDestroyed && props.config) {
      // 主题变化时重新应用配置，确保颜色更新
      nextTick(() => {
        try {
          // 先清空当前配置
          chartInstance.clear()

          // 重新设置配置，应用新主题
          const finalConfig = useThemeColors(props.config)
          chartInstance.setOption(finalConfig, true) // true 表示完全重新渲染

          emit('chartUpdated', chartInstance)
        } catch (error) {
          console.error('主题更新失败:', error)
        }
      })
    }
  },
  { immediate: false }
)

/**组件挂载时初始化*/
onMounted(() => {
  // 如果不是加载状态且不是空状态，则初始化图表
  if (!props.loading && !props.empty) {
    nextTick(() => {
      initChart()
    })
  }

  // 监听窗口大小变化
  window.addEventListener('resize', resizeChart)
})

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  // 延迟执行，确保DOM已更新
  nextTick(() => {
    resizeChart()
  })
})

/**组件卸载前清理*/
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
})

/**组件卸载时销毁图表*/
onUnmounted(() => {
  destroyChart()
})

/**暴露方法给父组件*/
defineExpose({
  /**获取图表实例*/
  getChartInstance,
  /**重置图表尺寸*/
  resizeChart,
  /**更新图表*/
  updateChart,
  /**清空图表*/
  clearChart,
  /**销毁图表*/
  destroyChart
})
</script>

<style lang="scss" scoped>
.a-chart {
  position: relative;
  width: 100%;
  overflow: hidden;
  min-height: 200px; /* 添加最小高度 */
  display: flex; /* 使用 flex 布局 */
  flex-direction: column; /* 垂直排列 */

  .chart-loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    z-index: 10;

    .loading-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      color: var(--el-text-color-secondary);

      .loading-icon {
        display: inline-block;
        width: 24px;
        height: 24px;
        border: 2px solid var(--el-color-primary-light-7);
        border-top: 2px solid var(--el-color-primary);
        border-radius: 50%;
        animation: loading-spin 1s linear infinite;
      }

      .loading-text {
        font-size: 14px;
        font-weight: 400;
      }
    }
  }

  .chart-empty {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    z-index: 10;

    .empty-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: var(--el-text-color-placeholder);

      .empty-icon {
        font-size: 48px;
        color: var(--el-text-color-disabled);
      }

      .empty-text {
        font-size: 14px;
        font-weight: 400;
      }
    }
  }

  .chart-content {
    flex: 1; /* 占据剩余空间 */
    width: 100% !important;
    height: 100%;
    min-height: inherit; /* 继承最小高度 */
    min-width: 200px; /* 添加最小宽度 */
    padding: 20px;
  }
}

@keyframes loading-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// 暗色主题适配
.dark {
  .a-chart {
    .chart-loading .loading-content .loading-text,
    .chart-empty .empty-content .empty-text {
      color: var(--el-text-color-secondary);
    }

    .chart-empty .empty-content .empty-icon {
      color: var(--el-text-color-disabled);
    }
  }
}
</style>
