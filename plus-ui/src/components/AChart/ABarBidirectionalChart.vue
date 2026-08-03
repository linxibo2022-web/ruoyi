<!--
双向柱状图组件

基础使用方式示例：

1. 基本用法 - 正负向数据对比
<ABarBidirectionalChart
  :positive-data="[20, 25, 30, 18, 22, 28, 35]"
  :negative-data="[15, 20, 25, 12, 18, 24, 30]"
  :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
  positive-name="收入"
  negative-name="支出"
/>

2. 带图例显示
<ABarBidirectionalChart
  :positive-data="positiveData"
  :negative-data="negativeData"
  :x-axis-data="categories"
  positive-name="男性用户"
  negative-name="女性用户"
  :show-legend="true"
/>

3. 显示数据标签
<ABarBidirectionalChart
  :positive-data="positiveData"
  :negative-data="negativeData"
  :x-axis-data="categories"
  :show-data-label="true"
  :y-axis-min="-50"
  :y-axis-max="50"
/>

4. 自定义样式
<ABarBidirectionalChart
  :positive-data="positiveData"
  :negative-data="negativeData"
  :x-axis-data="categories"
  :bar-width="20"
  :positive-border-radius="[8, 8, 0, 0]"
  :negative-border-radius="[0, 0, 8, 8]"
  :colors="['#5470c6', '#91cc75']"
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

<script setup lang="ts" name="ABarBidirectionalChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { ChartConfigFactory } from './utils/chart-factory'
import { getChartThemeConfig } from './composables/useChart'
import { BaseChartProps } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**双向柱状图组件属性接口*/
export interface ABarBidirectionalChartProps extends BaseChartProps {
  /**正向数据*/
  positiveData?: number[]
  /**负向数据*/
  negativeData?: number[]
  /**X轴数据*/
  xAxisData?: string[]
  /**正向数据名称*/
  positiveName?: string
  /**负向数据名称*/
  negativeName?: string
  /**柱宽度*/
  barWidth?: number
  /**Y轴最小值*/
  yAxisMin?: number
  /**Y轴最大值*/
  yAxisMax?: number
  /**是否显示数据标签*/
  showDataLabel?: boolean
  /**正向柱子圆角*/
  positiveBorderRadius?: number | number[]
  /**负向柱子圆角*/
  negativeBorderRadius?: number | number[]
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<ABarBidirectionalChartProps>(), {
  /**默认高度*/
  height: '16rem',
  /**是否加载中*/
  loading: false,
  /**是否为空*/
  isEmpty: false,
  /**正向数据*/
  positiveData: () => [],
  /**负向数据*/
  negativeData: () => [],
  /**X轴数据*/
  xAxisData: () => [],
  /**正向数据名称*/
  positiveName: '正向数据',
  /**负向数据名称*/
  negativeName: '负向数据',
  /**柱宽度*/
  barWidth: 16,
  /**Y轴最小值*/
  yAxisMin: -100,
  /**Y轴最大值*/
  yAxisMax: 100,
  /**是否显示数据标签*/
  showDataLabel: false,
  /**正向柱子圆角*/
  positiveBorderRadius: () => [10, 10, 0, 0],
  /**负向柱子圆角*/
  negativeBorderRadius: () => [0, 0, 10, 10],
  /**是否显示坐标轴标签*/
  showAxisLabel: true,
  /**是否显示坐标轴线*/
  showAxisLine: false,
  /**是否显示分割线*/
  showSplitLine: false,
  /**是否显示提示框*/
  showTooltip: true,
  /**是否显示图例*/
  showLegend: false,
  /**图例位置*/
  legendPosition: 'bottom' as const,
  colors: () => getChartThemeConfig().colors
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**图表点击事件*/
  chartClick: [params: any]
  /**柱子点击事件*/
  barClick: [
    params: {
      name: string
      value: number
      seriesName?: string
      isPositive: boolean
      originalValue: number
    }
  ]
}>()

/**使用图表配置生成器*/
const { generateBarBidirectionalConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateBidirectionalBarData(props)
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**处理后的负向数据（确保为负值）*/
const processedNegativeData = computed(() => {
  return ChartConfigFactory.processNegativeData(props.negativeData || [])
})

/**数据范围信息*/
const dataRange = computed(() => {
  const positiveMax = props.positiveData?.length ? Math.max(...props.positiveData) : 0
  const negativeMin = processedNegativeData.value.length ? Math.min(...processedNegativeData.value) : 0

  return {
    positiveMax,
    negativeMin,
    range: positiveMax - negativeMin
  }
})

/**生成图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value) {
    return {}
  }

  return generateBarBidirectionalConfig(props)
})

/**
 * 处理图表准备就绪事件
 * @param chart 图表实例
 */
const handleChartReady = (chart: any) => {
  emit('chartReady', chart)
}

/**
 * 处理图表点击事件
 * @param params 点击参数
 */
const handleChartClick = (params: any) => {
  emit('chartClick', params)

  // 如果是柱子点击，触发专门的柱子点击事件
  if (params.componentType === 'series' && params.seriesType === 'bar') {
    const isPositive = params.seriesName === props.positiveName
    const originalValue = Math.abs(params.value) // 获取原始绝对值

    const barClickData = {
      name: params.name,
      value: params.value,
      seriesName: params.seriesName,
      isPositive,
      originalValue
    }
    emit('barClick', barClickData)
  }
}

/**
 * 获取正向数据的最大值
 * @returns 正向最大值
 */
const getPositiveMax = (): number => {
  return dataRange.value.positiveMax
}

/**
 * 获取负向数据的最小值
 * @returns 负向最小值
 */
const getNegativeMin = (): number => {
  return dataRange.value.negativeMin
}

/**
 * 获取数据总范围
 * @returns 数据范围
 */
const getDataRange = (): number => {
  return dataRange.value.range
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
  /**处理后的负向数据*/
  processedNegativeData,
  /**数据范围信息*/
  dataRange,
  /**获取正向最大值*/
  getPositiveMax,
  /**获取负向最小值*/
  getNegativeMin,
  /**获取数据范围*/
  getDataRange
})
</script>

<style lang="scss" scoped>
// 双向柱状图特有样式
.a-bar-bidirectional-chart {
  position: relative;
  width: 100%;

  // 可以添加双向柱状图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
