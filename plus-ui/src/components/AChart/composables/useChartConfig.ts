/**
 * 图表配置生成Hook
 *
 * 提供各种图表类型的ECharts配置生成功能
 * - 柱状图配置生成
 * - 折线图配置生成
 * - 饼图配置生成
 * - 雷达图配置生成
 * - 散点图配置生成
 * - K线图配置生成
 */

import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCssVar, hexToRgba } from '@/utils/colors'
import { getChartThemeConfig, useChart } from './useChart'
/* 组件props类型导入 */
import type { ABarChartProps as BarChartProps } from '../ABarChart.vue'
import type { LineDataItem } from '../ALineChart.vue'
import type { SeriesDataItem, ChartDataItem } from '../AChart.vue'
import { ABarHorizontalChartProps } from '@/components/AChart/ABarHorizontalChart.vue'
import { ABarBidirectionalChartProps } from '@/components/AChart/ABarBidirectionalChart.vue'
import { ALineChartProps } from '@/components/AChart/ALineChart.vue'
import { APieChartProps } from '@/components/AChart/APieChart.vue'
import { ARadarChartProps } from '@/components/AChart/ARadarChart.vue'
import { AScatterChartProps } from '@/components/AChart/AScatterChart.vue'
import { ACandlestickChartProps } from '@/components/AChart/ACandlestickChart.vue'

/**
 * 图表配置生成Hook
 * @returns 各种图表配置生成方法
 */
export function useChartConfig() {
  const { t } = useI18n()
  const {
    getAxisLineStyle,
    getSplitLineStyle,
    getAxisLabelStyle,
    getAxisTickStyle,
    getAnimationConfig,
    getTooltipStyle,
    getLegendStyle,
    getGridWithLegend,
    isDark,
    useChartTheme
  } = useChart()

  /**
   * 判断是否为多数据系列
   * @param data 数据
   * @returns 是否为多数据系列
   */
  const isMultipleData = (data: any[]): boolean => {
    return Array.isArray(data) && data.length > 0 && typeof data[0] === 'object' && 'name' in data[0]
  }

  /**
   * 将单个值归一化为数字（用于 ECharts series 数据）
   *
   * 后端 BigDecimal 经框架全局 Jackson 配置会被序列化为字符串，
   * ECharts 需要真实数字才能正确计算坐标轴与渲染，这里统一兜底转换。
   * 注意：null / undefined / 空串 / 非数值字符串一律保留为 null，
   * 以维持 ECharts「断点(缺口)」语义，避免本应断开的线被强行拉到 0。
   * @param val 待转换的值
   * @returns 转换后的数字；无有效数值时返回 null
   */
  const toNum = (val: any): number | null => {
    if (val === null || val === undefined || val === '') return null
    const num = Number(val)
    return Number.isNaN(num) ? null : num
  }

  /**
   * 将数字 / 数字字符串数组归一化为数字数组（保留 null 缺口）
   * @param arr 原始数组
   * @returns 归一化后的数组，元素可能为 null
   */
  const toNumArray = (arr: any[]): (number | null)[] => (Array.isArray(arr) ? arr.map(toNum) : [])

  /**
   * 计算数组中的最大值（忽略 null 缺口，兼容数字字符串）
   * @param arr 原始数组
   * @returns 最大值，无有效数值时返回 0
   */
  const maxOf = (arr: any[]): number => {
    const nums = toNumArray(arr).filter((v): v is number => v !== null)
    return nums.length ? Math.max(...nums) : 0
  }

  /**
   * 获取颜色配置
   * @param customColor 自定义颜色
   * @param index 索引
   * @param colors 颜色数组
   * @returns 计算后的颜色
   */
  const getColor = (customColor?: string, index?: number, colors?: string[]) => {
    if (customColor) return customColor

    const { colors: themeColors } = useChartTheme()
    const colorArray = colors || themeColors

    if (index !== undefined) {
      return colorArray[index % colorArray.length]
    }

    return getCssVar('--el-color-primary')
  }

  /**
   * 创建渐变色
   * @param color 基础颜色
   * @param direction 渐变方向 'vertical' | 'horizontal'
   * @returns 渐变色对象
   */
  const createGradientColor = (color: string, direction: 'vertical' | 'horizontal' = 'vertical') => {
    const [x1, y1, x2, y2] = direction === 'vertical' ? [0, 0, 0, 1] : [0, 0, 1, 0]

    return new echarts.graphic.LinearGradient(x1, y1, x2, y2, [
      {
        offset: 0,
        color: color
      },
      {
        offset: 1,
        color: getCssVar('--el-color-primary')
      }
    ])
  }

  /**
   * 生成柱状图配置
   * @param props 柱状图属性
   * @returns ECharts配置对象
   */
  const generateBarConfig = (props: BarChartProps): EChartsOption => {
    // 获取实际使用的颜色配置
    const actualColors = props.colors && props.colors.length > 0 ? props.colors : getChartThemeConfig().colors

    const isMultiple = isMultipleData(props.data || [])
    const dataToUse = props.data
    // 计算最大值用于Y轴配置
    const maxValue = computed(() => {
      if (isMultiple) {
        const multiData = dataToUse as SeriesDataItem[]
        return multiData.reduce((max, item) => {
          if (item.data?.length) {
            const itemMax = maxOf(item.data)
            return Math.max(max, itemMax)
          }
          return max
        }, 0)
      } else {
        return maxOf((dataToUse as any[]) || [])
      }
    })
    const options: EChartsOption = {
      grid: getGridWithLegend(props.showLegend && isMultiple, props.legendPosition, {
        top: 15,
        right: 0,
        left: 0
      }),
      tooltip: props.showTooltip ? getTooltipStyle() : undefined,
      xAxis: {
        type: 'category',
        data: props.xAxisData,
        axisTick: getAxisTickStyle(),
        axisLine: getAxisLineStyle(props.showAxisLine),
        axisLabel: getAxisLabelStyle(props.showAxisLabel)
      },
      yAxis: {
        type: 'value',
        min: props.yAxisMin !== undefined ? props.yAxisMin : 0,
        max: props.yAxisMax !== undefined ? props.yAxisMax : maxValue.value,
        interval: props.yAxisInterval,
        splitNumber: props.yAxisSplitNumber,
        minInterval: props.yAxisMinInterval,
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        axisLine: getAxisLineStyle(props.showAxisLine),
        splitLine: getSplitLineStyle(props.showSplitLine)
      }
    }

    // 添加图例配置
    if (props.showLegend && isMultiple) {
      options.legend = getLegendStyle(props.legendPosition)
    }

    // 生成系列数据
    if (isMultiple) {
      const multiData = props.data as SeriesDataItem[]
      options.series = multiData.map((item, index) => {
        const computedColor = getColor(actualColors?.[index], index, actualColors)

        const itemStyleColor: string | echarts.graphic.LinearGradient = computedColor

        return {
          name: item.name,
          data: toNumArray(item.data),
          type: 'bar' as const,
          stack: props.stack ? item.stack || 'total' : undefined,
          itemStyle: {
            borderRadius: props.borderRadius,
            color: itemStyleColor
          },
          barWidth: item.barWidth || props.barWidth,
          ...getAnimationConfig()
        }
      })
    } else {
      // 单数据情况
      const singleData = toNumArray((props.data as any[]) || [])
      const computedColor = getColor(undefined, 0, actualColors)

      const itemStyleColor: string | echarts.graphic.LinearGradient = computedColor

      options.series = [
        {
          data: singleData,
          type: 'bar' as const,
          itemStyle: {
            borderRadius: props.borderRadius,
            color: itemStyleColor
          },
          barWidth: props.barWidth,
          ...getAnimationConfig()
        }
      ]
    }

    return options
  }

  /**
   * 生成水平柱状图配置
   * @param props 水平柱状图属性
   * @returns ECharts配置对象
   */
  const generateBarHorizontalConfig = (props: ABarHorizontalChartProps): EChartsOption => {
    const isMultiple = isMultipleData(props.data || [])

    const options: EChartsOption = {
      grid: getGridWithLegend(props.showLegend && isMultiple, props.legendPosition, {
        top: 15,
        right: 0,
        left: 0
      }),
      tooltip: props.showTooltip ? getTooltipStyle() : undefined,
      xAxis: {
        type: 'value',
        axisTick: getAxisTickStyle(),
        axisLine: getAxisLineStyle(props.showAxisLine),
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        splitLine: getSplitLineStyle(props.showSplitLine)
      },
      yAxis: {
        type: 'category',
        data: props.yAxisData,
        axisTick: getAxisTickStyle(),
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        axisLine: getAxisLineStyle(props.showAxisLine)
      }
    }

    // 添加图例配置
    if (props.showLegend && isMultiple) {
      options.legend = getLegendStyle(props.legendPosition)
    }

    // 生成系列数据
    if (isMultiple) {
      const multiData = props.data as SeriesDataItem[]
      options.series = multiData.map((item, index) => {
        const computedColor = getColor(props.colors?.[index], index, props.colors)

        return {
          name: item.name,
          data: toNumArray(item.data),
          type: 'bar' as const,
          stack: props.stack ? item.stack || 'total' : undefined,
          itemStyle: {
            borderRadius: 4,
            color: typeof computedColor === 'string' ? createGradientColor(computedColor, 'horizontal') : computedColor
          },
          barWidth: item.barWidth || props.barWidth,
          ...getAnimationConfig()
        }
      })
    } else {
      // 单数据情况
      const singleData = toNumArray((props.data as any[]) || [])
      const computedColor = getColor(undefined, 0, props.colors)

      options.series = [
        {
          data: singleData,
          type: 'bar' as const,
          itemStyle: {
            borderRadius: 4,
            color: typeof computedColor === 'string' ? createGradientColor(computedColor, 'horizontal') : computedColor
          },
          barWidth: props.barWidth,
          ...getAnimationConfig()
        }
      ]
    }

    return options
  }

  /**
   * 生成双向柱状图配置
   * @param props 双向柱状图属性
   * @returns ECharts配置对象
   */
  const generateBarBidirectionalConfig = (props: ABarBidirectionalChartProps): EChartsOption => {
    // 处理负向数据：先归一化为数字（兼容后端 BigDecimal 序列化为字符串），再确保为负值；null 缺口保留
    const processedNegativeData = toNumArray(props.negativeData || []).map((val) => (val != null && val > 0 ? -val : val))
    // 正向数据归一化为数字
    const positiveData = toNumArray(props.positiveData || [])

    const gridConfig = {
      top: props.showLegend ? 50 : 20,
      right: 0,
      left: 0,
      bottom: 0,
      containLabel: true
    }

    const options: EChartsOption = {
      backgroundColor: 'transparent',
      animation: true,
      animationDuration: 1000,
      animationEasing: 'cubicOut',
      grid: getGridWithLegend(props.showLegend, props.legendPosition, gridConfig),

      tooltip: props.showTooltip
        ? {
            ...getTooltipStyle(),
            trigger: 'axis',
            axisPointer: {
              type: 'none'
            }
          }
        : undefined,

      legend: props.showLegend
        ? {
            ...getLegendStyle(props.legendPosition),
            data: [props.negativeName, props.positiveName]
          }
        : undefined,

      xAxis: {
        type: 'category',
        data: props.xAxisData,
        axisTick: getAxisTickStyle(),
        axisLine: getAxisLineStyle(props.showAxisLine),
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        boundaryGap: true
      },

      yAxis: {
        type: 'value',
        min: props.yAxisMin,
        max: props.yAxisMax,
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        axisLine: getAxisLineStyle(props.showAxisLine),
        splitLine: getSplitLineStyle(props.showSplitLine)
      },

      series: [
        // 负向数据系列
        {
          name: props.negativeName,
          type: 'bar',
          stack: 'total',
          barWidth: props.barWidth,
          barGap: '-100%',
          data: processedNegativeData,
          itemStyle: {
            borderRadius: props.negativeBorderRadius,
            color: props.colors?.[1] || '#4ABEFF'
          },
          label: {
            show: props.showDataLabel,
            position: 'bottom',
            formatter: (params: any) => String(Math.abs(params.value)),
            color: isDark.value ? '#ccc' : '#666',
            fontSize: 12
          },
          ...getAnimationConfig()
        },
        // 正向数据系列
        {
          name: props.positiveName,
          type: 'bar',
          stack: 'total',
          barWidth: props.barWidth,
          barGap: '-100%',
          data: positiveData,
          itemStyle: {
            borderRadius: props.positiveBorderRadius,
            color: props.colors?.[0] || getCssVar('--el-color-primary')
          },
          label: {
            show: props.showDataLabel,
            position: 'top',
            formatter: (params: any) => String(params.value),
            color: isDark.value ? '#ccc' : '#666',
            fontSize: 12
          },
          ...getAnimationConfig()
        }
      ]
    }

    return options
  }

  /**
   * 生成折线图区域样式
   * @param item 折线数据项
   * @param color 颜色
   * @returns 区域样式配置
   */
  const generateLineAreaStyle = (item: LineDataItem, color: string) => {
    if (!item.areaStyle && !item.showAreaColor) return undefined

    const areaConfig = item.areaStyle || {}
    if (areaConfig.custom) return areaConfig.custom

    return {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        {
          offset: 0,
          color: hexToRgba(color, areaConfig.startOpacity || 0.2).rgba
        },
        {
          offset: 1,
          color: hexToRgba(color, areaConfig.endOpacity || 0.02).rgba
        }
      ])
    }
  }

  /**
   * 生成折线图配置
   * @param props 折线图属性
   * @param animatedData 动画数据（可选）
   * @returns ECharts配置对象
   */
  const generateLineConfig = (props: ALineChartProps, animatedData?: any[]): EChartsOption => {
    const isMultiple = isMultipleData(props.data || [])
    const dataToUse = animatedData || props.data

    // 计算最大值用于Y轴配置
    const maxValue = computed(() => {
      if (isMultiple) {
        const multiData = dataToUse as LineDataItem[]
        return multiData.reduce((max, item) => {
          if (item.data?.length) {
            const itemMax = maxOf(item.data)
            return Math.max(max, itemMax)
          }
          return max
        }, 0)
      } else {
        return maxOf((dataToUse as any[]) || [])
      }
    })

    const options: EChartsOption = {
      animation: true,
      animationDuration: 1300,
      animationDurationUpdate: 1300,
      grid: getGridWithLegend(props.showLegend && isMultiple, props.legendPosition, {
        top: 15,
        right: 15,
        left: 0
      }),
      tooltip: props.showTooltip ? getTooltipStyle() : undefined,
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: props.xAxisData,
        axisTick: getAxisTickStyle(),
        axisLine: getAxisLineStyle(props.showAxisLine),
        axisLabel: getAxisLabelStyle(props.showAxisLabel)
      },
      yAxis: {
        type: 'value',
        min: props.yAxisMin !== undefined ? props.yAxisMin : 0,
        max: props.yAxisMax !== undefined ? props.yAxisMax : maxValue.value,
        interval: props.yAxisInterval,
        splitNumber: props.yAxisSplitNumber,
        minInterval: props.yAxisMinInterval,
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        axisLine: getAxisLineStyle(props.showAxisLine),
        splitLine: getSplitLineStyle(props.showSplitLine)
      }
    }

    // 添加图例配置
    if (props.showLegend && isMultiple) {
      options.legend = getLegendStyle(props.legendPosition)
    }

    // 生成系列数据
    if (isMultiple) {
      const multiData = dataToUse as LineDataItem[]
      options.series = multiData.map((item, index) => {
        const itemColor = getColor(props.colors?.[index], index, props.colors)
        const areaStyle = generateLineAreaStyle(item, itemColor)

        return {
          name: item.name,
          data: toNumArray(item.data),
          type: 'line' as const,
          color: itemColor,
          smooth: item.smooth ?? props.smooth,
          symbol: item.symbol ?? props.symbol,
          symbolSize: item.symbolSize ?? props.symbolSize,
          lineStyle: {
            width: item.lineWidth ?? props.lineWidth,
            color: itemColor
          },
          areaStyle,
          emphasis: {
            focus: 'series' as const,
            lineStyle: {
              width: (item.lineWidth ?? props.lineWidth ?? 2.5) + 1
            }
          }
        }
      })
    } else {
      // 单数据情况
      const singleData = toNumArray((dataToUse as any[]) || [])
      const computedColor = getColor(undefined, 0, props.colors)

      // 单数据区域样式
      const areaStyle = props.showAreaColor
        ? {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              {
                offset: 0,
                color: hexToRgba(computedColor, 0.2).rgba
              },
              {
                offset: 1,
                color: hexToRgba(computedColor, 0.02).rgba
              }
            ])
          }
        : undefined

      options.series = [
        {
          data: singleData,
          type: 'line' as const,
          color: computedColor,
          smooth: props.smooth,
          symbol: props.symbol,
          symbolSize: props.symbolSize,
          lineStyle: {
            width: props.lineWidth,
            color: computedColor
          },
          areaStyle,
          emphasis: {
            focus: 'series' as const,
            lineStyle: {
              width: (props.lineWidth ?? 2.5) + 1
            }
          }
        }
      ]
    }

    return options
  }

  /**
   * 生成饼图配置
   * @param props 饼图属性
   * @returns ECharts配置对象
   */
  const generatePieConfig = (props: APieChartProps): EChartsOption => {
    // 根据图例位置计算环形图中心位置
    const getCenterPosition = (): [string, string] => {
      if (!props.showLegend) return ['50%', '50%']

      switch (props.legendPosition) {
        case 'left':
          return ['60%', '50%']
        case 'right':
          return ['40%', '50%']
        case 'top':
          return ['50%', '60%']
        case 'bottom':
          return ['50%', '40%']
        default:
          return ['50%', '50%']
      }
    }

    const option: EChartsOption = {
      tooltip: props.showTooltip
        ? getTooltipStyle('item', {
            formatter: '{b}: {c} ({d}%)'
          })
        : undefined,
      legend: props.showLegend ? getLegendStyle(props.legendPosition) : undefined,
      series: [
        {
          name: t('chart.pie.dataRatio'),
          type: 'pie',
          radius: props.radius,
          center: getCenterPosition(),
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: props.borderRadius,
            borderColor: isDark.value ? '#2c2c2c' : '#fff',
            borderWidth: 0
          },
          label: {
            show: props.showLabel,
            formatter: '{b}\n{d}%',
            position: 'outside',
            color: isDark.value ? '#ccc' : '#999',
            fontSize: 12
          },
          emphasis: {
            label: {
              show: false,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: props.showLabel,
            length: 15,
            length2: 25,
            smooth: true
          },
          data: props.data,
          color: props.colors,
          ...getAnimationConfig(),
          animationType: 'expansion'
        }
      ]
    }

    // 添加中心文字
    if (props.centerText) {
      const centerPos = getCenterPosition()
      option.title = {
        text: props.centerText,
        left: centerPos[0],
        top: centerPos[1],
        textAlign: 'center',
        textVerticalAlign: 'middle',
        textStyle: {
          fontSize: 18,
          fontWeight: 500,
          color: isDark.value ? '#999' : '#ADB0BC'
        }
      }
    }

    return option
  }

  /**
   * 生成雷达图配置
   * @param props 雷达图属性
   * @returns ECharts配置对象
   */
  const generateRadarConfig = (props: ARadarChartProps): EChartsOption => {
    return {
      tooltip: props.showTooltip ? getTooltipStyle('item') : undefined,
      radar: {
        indicator: props.indicator,
        center: ['50%', '50%'],
        radius: '70%',
        axisName: {
          color: isDark.value ? '#ccc' : '#666',
          fontSize: 12
        },
        splitLine: {
          lineStyle: {
            color: isDark.value ? '#444' : '#e6e6e6'
          }
        },
        axisLine: {
          lineStyle: {
            color: isDark.value ? '#444' : '#e6e6e6'
          }
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: isDark.value ? ['rgba(255, 255, 255, 0.02)', 'rgba(255, 255, 255, 0.05)'] : ['rgba(0, 0, 0, 0.02)', 'rgba(0, 0, 0, 0.05)']
          }
        }
      },
      series: [
        {
          type: 'radar',
          data: props.data?.map((item, index) => ({
            name: item.name,
            value: item.value,
            symbolSize: 4,
            lineStyle: {
              width: 2,
              color: props.colors?.[index % (props.colors?.length || 1)]
            },
            itemStyle: {
              color: props.colors?.[index % (props.colors?.length || 1)]
            },
            areaStyle: {
              color: props.colors?.[index % (props.colors?.length || 1)],
              opacity: 0.1
            },
            emphasis: {
              areaStyle: {
                opacity: 0.25
              },
              lineStyle: {
                width: 3
              }
            }
          })),
          ...getAnimationConfig(200, 1800)
        }
      ]
    }
  }

  /**
   * 生成散点图配置
   * @param props 散点图属性
   * @returns ECharts配置对象
   */
  const generateScatterConfig = (props: AScatterChartProps): EChartsOption => {
    const computedColor = getColor(undefined, 0, props.colors)

    return {
      grid: {
        top: 20,
        right: 20,
        bottom: 20,
        left: 20,
        containLabel: true
      },
      tooltip: props.showTooltip
        ? getTooltipStyle('item', {
            formatter: (params: any) => {
              const [x, y] = params.value
              return `X: ${x}<br/>Y: ${y}`
            }
          })
        : undefined,
      xAxis: {
        type: 'value',
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        axisLine: getAxisLineStyle(props.showAxisLine),
        axisTick: getAxisTickStyle(),
        splitLine: getSplitLineStyle(props.showSplitLine)
      },
      yAxis: {
        type: 'value',
        axisLabel: getAxisLabelStyle(props.showAxisLabel),
        axisLine: getAxisLineStyle(props.showAxisLine),
        axisTick: getAxisTickStyle(),
        splitLine: getSplitLineStyle(props.showSplitLine)
      },
      series: [
        {
          type: 'scatter',
          data: props.data,
          symbolSize: props.symbolSize,
          itemStyle: {
            color: computedColor,
            shadowBlur: 6,
            shadowColor: isDark.value ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
            shadowOffsetY: 2
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 12,
              shadowColor: isDark.value ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.2)'
            },
            scale: true
          },
          ...getAnimationConfig()
        }
      ]
    }
  }

  /**
   * 生成K线图配置
   * @param props K线图属性
   * @returns ECharts配置对象
   */
  const generateCandlestickConfig = (props: ACandlestickChartProps): EChartsOption => {
    // 获取实际使用的颜色
    const getActualColors = () => {
      const defaultUpColor = '#4C87F3'
      const defaultDownColor = '#8BD8FC'

      return {
        upColor: props.colors?.[0] || defaultUpColor,
        downColor: props.colors?.[1] || defaultDownColor
      }
    }

    const { upColor, downColor } = getActualColors()

    return {
      grid: {
        top: 20,
        right: 20,
        bottom: props.showDataZoom ? 80 : 20,
        left: 20,
        containLabel: true
      },
      tooltip: getTooltipStyle('axis', {
        axisPointer: {
          type: 'cross'
        },
        formatter: (params: any) => {
          const param = params[0]
          const data = param.data
          return `
            <div style="padding: 5px;">
              <div><strong>${t('chart.candlestick.time')}</strong>${param.name}</div>
              <div><strong>${t('chart.candlestick.open')}</strong>${data[0]}</div>
              <div><strong>${t('chart.candlestick.close')}</strong>${data[1]}</div>
              <div><strong>${t('chart.candlestick.low')}</strong>${data[2]}</div>
              <div><strong>${t('chart.candlestick.high')}</strong>${data[3]}</div>
            </div>
          `
        }
      }),
      xAxis: {
        type: 'category',
        data: props.data?.map((item) => item.time),
        axisTick: getAxisTickStyle(),
        axisLine: getAxisLineStyle(true),
        axisLabel: getAxisLabelStyle(true)
      },
      yAxis: {
        type: 'value',
        scale: true,
        axisLabel: getAxisLabelStyle(true),
        axisLine: getAxisLineStyle(true),
        splitLine: getSplitLineStyle(true)
      },
      series: [
        {
          type: 'candlestick',
          data: props.data?.map((item) => [item.open, item.close, item.low, item.high]),
          itemStyle: {
            color: upColor,
            color0: downColor,
            borderColor: upColor,
            borderColor0: downColor,
            borderWidth: 1
          },
          emphasis: {
            itemStyle: {
              borderWidth: 2,
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            }
          },
          ...getAnimationConfig()
        }
      ],
      dataZoom: props.showDataZoom
        ? [
            {
              type: 'inside',
              start: props.dataZoomStart,
              end: props.dataZoomEnd
            },
            {
              show: true,
              type: 'slider',
              top: '90%',
              start: props.dataZoomStart,
              end: props.dataZoomEnd
            }
          ]
        : undefined
    }
  }

  return {
    // 工具方法
    isMultipleData,
    getColor,
    createGradientColor,
    generateLineAreaStyle,

    // 配置生成方法
    generateBarConfig,
    generateBarHorizontalConfig,
    generateBarBidirectionalConfig,
    generateLineConfig,
    generatePieConfig,
    generateRadarConfig,
    generateScatterConfig,
    generateCandlestickConfig
  }
}
