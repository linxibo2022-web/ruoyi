<!--
K线图组件

基础使用方式示例：

1. 基本用法 - 股票K线图
<ACandlestickChart
  :data="[
    { time: '2023-01-01', open: 100, close: 102, high: 105, low: 99 },
    { time: '2023-01-02', open: 102, close: 98, high: 103, low: 97 },
    { time: '2023-01-03', open: 98, close: 104, high: 106, low: 96 },
    { time: '2023-01-04', open: 104, close: 107, high: 109, low: 103 }
  ]"
/>

2. 带数据缩放功能
<ACandlestickChart
  :data="klineData"
  :show-data-zoom="true"
  :data-zoom-start="20"
  :data-zoom-end="80"
/>

3. 自定义颜色
<ACandlestickChart
  :data="klineData"
  :colors="['#ef5350', '#26a69a']"
  :show-data-zoom="true"
/>

4. 完整的交易数据展示
<ACandlestickChart
  :data="fullKlineData"
  :show-data-zoom="true"
  height="24rem"
  :data-zoom-start="0"
  :data-zoom-end="100"
/>
-->
<template>
  <AChart
    :config="chartConfig"
    :loading="loading"
    :empty="isEmpty"
    :height="height"
    v-bind="$attrs"
    @chart-ready="handleChartReady"
    @chart-click="handleChartClick"
  >
    <!-- 透传插槽 -->
    <template #loading>
      <slot name="loading"></slot>
    </template>
    <template #empty>
      <slot name="empty"></slot>
    </template>
  </AChart>
</template>

<script setup lang="ts" name="ACandlestickChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { ChartConfigFactory } from './utils/chart-factory'
import { getChartThemeConfig } from './composables/useChart'
import { BaseChartProps } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**K线数据项接口*/
export interface CandlestickDataItem {
  /**时间*/
  time: string
  /**开盘价*/
  open: number
  /**收盘价*/
  close: number
  /**最高价*/
  high: number
  /**最低价*/
  low: number
}

/**K线图组件属性接口*/
export interface ACandlestickChartProps extends BaseChartProps {
  /**K线数据*/
  data?: CandlestickDataItem[]
  /**是否显示数据缩放*/
  showDataZoom?: boolean
  /**数据缩放起始位置*/
  dataZoomStart?: number
  /**数据缩放结束位置*/
  dataZoomEnd?: number
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<ACandlestickChartProps>(), {
  /**默认高度*/
  height: '16rem',
  /**是否加载中*/
  loading: false,
  /**是否为空*/
  isEmpty: false,
  /**默认数据*/
  data: () => [],
  /**是否显示数据缩放*/
  showDataZoom: false,
  /**数据缩放起始位置*/
  dataZoomStart: 0,
  /**数据缩放结束位置*/
  dataZoomEnd: 100,
  colors: () => getChartThemeConfig().colors
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**图表点击事件*/
  chartClick: [params: any]
  /**K线点击事件*/
  candleClick: [
    params: {
      time: string
      open: number
      close: number
      high: number
      low: number
      isRising: boolean
      amplitude: number
      change: number
      changePercent: number
    }
  ]
  /**数据缩放事件*/
  dataZoom: [params: { start: number; end: number; startValue?: string; endValue?: string }]
}>()

/**使用图表配置生成器*/
const { generateCandlestickConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateCandlestickData(props)
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**数据统计信息*/
const dataStatistics = computed(() => {
  if (!props.data?.length) {
    return {
      count: 0,
      priceRange: { min: 0, max: 0 },
      risingCount: 0,
      fallingCount: 0,
      risingRate: 0,
      averageAmplitude: 0
    }
  }

  const count = props.data.length
  let risingCount = 0
  let totalAmplitude = 0

  // 收集所有价格数据
  const allPrices: number[] = []

  props.data.forEach((item) => {
    allPrices.push(item.open, item.close, item.high, item.low)

    // 统计涨跌
    if (item.close > item.open) {
      risingCount++
    }

    // 计算振幅
    const amplitude = ((item.high - item.low) / item.open) * 100
    totalAmplitude += amplitude
  })

  const fallingCount = count - risingCount
  const risingRate = Number(((risingCount / count) * 100).toFixed(1))
  const averageAmplitude = Number((totalAmplitude / count).toFixed(2))

  return {
    count,
    priceRange: {
      min: Math.min(...allPrices),
      max: Math.max(...allPrices)
    },
    risingCount,
    fallingCount,
    risingRate,
    averageAmplitude
  }
})

/**格式化的K线数据*/
const formattedData = computed(() => {
  if (!props.data?.length) return { times: [], values: [] }

  return ChartConfigFactory.formatCandlestickData(props.data)
})

/**生成图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value) {
    return {}
  }

  return generateCandlestickConfig(props)
})

/**
 * 处理图表准备就绪事件
 * @param chart 图表实例
 */
const handleChartReady = (chart: any) => {
  // 绑定数据缩放事件
  chart.on('datazoom', (params: any) => {
    emit('dataZoom', {
      start: params.start,
      end: params.end,
      startValue: params.startValue,
      endValue: params.endValue
    })
  })

  emit('chartReady', chart)
}

/**
 * 处理图表点击事件
 * @param params 点击参数
 */
const handleChartClick = (params: any) => {
  emit('chartClick', params)

  // 如果是K线点击，触发专门的K线点击事件
  if (params.componentType === 'series' && params.seriesType === 'candlestick') {
    const dataIndex = params.dataIndex
    const klineData = props.data?.[dataIndex]

    if (klineData) {
      const isRising = klineData.close > klineData.open
      const amplitude = ((klineData.high - klineData.low) / klineData.open) * 100
      const change = klineData.close - klineData.open
      const changePercent = (change / klineData.open) * 100

      const candleClickData = {
        time: klineData.time,
        open: klineData.open,
        close: klineData.close,
        high: klineData.high,
        low: klineData.low,
        isRising,
        amplitude: Number(amplitude.toFixed(2)),
        change: Number(change.toFixed(2)),
        changePercent: Number(changePercent.toFixed(2))
      }
      emit('candleClick', candleClickData)
    }
  }
}

/**
 * 获取指定时间段的数据
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @returns 时间段内的K线数据
 */
const getDataByTimeRange = (startTime: string, endTime: string): CandlestickDataItem[] => {
  if (!props.data?.length) return []

  return props.data.filter((item) => item.time >= startTime && item.time <= endTime)
}

/**
 * 计算移动平均线数据
 * @param period 周期
 * @returns 移动平均线数据
 */
const calculateMovingAverage = (period: number): number[] => {
  if (!props.data?.length || period > props.data.length) return []

  const ma: number[] = []

  for (let i = 0; i < props.data.length; i++) {
    if (i < period - 1) {
      ma.push(0) // 前面不足周期的点用0填充
    } else {
      let sum = 0
      for (let j = i - period + 1; j <= i; j++) {
        sum += props.data[j].close
      }
      ma.push(Number((sum / period).toFixed(2)))
    }
  }

  return ma
}

/**
 * 获取价格区间统计
 * @param minPrice 最低价
 * @param maxPrice 最高价
 * @returns 区间内的K线统计
 */
const getPriceRangeStats = (minPrice: number, maxPrice: number) => {
  if (!props.data?.length) return { count: 0, data: [] }

  const filteredData = props.data.filter((item) => {
    return item.low >= minPrice && item.high <= maxPrice
  })

  return {
    count: filteredData.length,
    data: filteredData
  }
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露组件方法和数据*/
defineExpose({
  /**是否为空*/
  isEmpty,
  /**数据验证结果*/
  dataValidation,
  /**数据统计信息*/
  dataStatistics,
  /**格式化的数据*/
  formattedData,
  /**获取时间段数据*/
  getDataByTimeRange,
  /**计算移动平均线*/
  calculateMovingAverage,
  /**获取价格区间统计*/
  getPriceRangeStats
})
</script>

<style lang="scss" scoped>
// K线图特有样式
.a-candlestick-chart {
  position: relative;
  width: 100%;

  // 可以添加K线图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
