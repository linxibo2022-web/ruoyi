<!--
水平柱状图组件

基础使用方式示例：

1. 基本用法 - 简单数值数组
<ABarHorizontalChart
  :data="[120, 200, 150, 80, 70, 110, 130]"
  :y-axis-data="['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G']"
/>

2. 多系列数据 - 对比展示
<ABarHorizontalChart
  :data="[
    { name: '销售额', data: [120, 200, 150, 80, 70, 110, 130] },
    { name: '目标值', data: [100, 180, 140, 90, 80, 120, 140] }
  ]"
  :y-axis-data="['产品A', '产品B', '产品C', '产品D', '产品E', '产品F', '产品G']"
  :show-legend="true"
/>

3. 堆叠水平柱状图
<ABarHorizontalChart
  :data="stackData"
  :y-axis-data="categories"
  :stack="true"
  :show-legend="true"
/>

4. 自定义样式
<ABarHorizontalChart
  :data="data"
  :y-axis-data="categories"
  bar-width="50%"
  :colors="['#5470c6', '#91cc75', '#fac858']"
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

<script setup lang="ts" name="ABarHorizontalChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { ChartConfigFactory } from './utils/chart-factory'
import { getChartThemeConfig } from './composables/useChart'
import { BaseChartProps, SeriesDataItem } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**水平柱状图组件属性接口*/
export interface ABarHorizontalChartProps extends BaseChartProps {
  /**数据：支持简单数组或复杂对象数组*/
  data?: number[] | SeriesDataItem[]
  /**Y轴数据（类目轴）*/
  yAxisData?: string[]
  /**柱宽度*/
  barWidth?: string | number
  /**是否堆叠*/
  stack?: boolean
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<ABarHorizontalChartProps>(), {
  /**柱宽度*/
  barWidth: '36%',
  /**Y轴数据（水平柱状图的类目轴）*/
  yAxisData: () => [],
  colors: () => getChartThemeConfig().colors
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**图表点击事件*/
  chartClick: [params: any]
  /**柱子点击事件*/
  barClick: [params: { name: string; value: number; seriesName?: string }]
}>()

/**使用图表配置生成器*/
const { generateBarHorizontalConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  // 复用柱状图的数据验证逻辑
  return ChartConfigFactory.validateBarData({
    ...props,
    data: props.data,
    xAxisData: props.yAxisData // 水平柱状图的类目轴是Y轴
  })
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**判断是否为多数据系列*/
const isMultipleData = computed(() => {
  return dataValidation.value.isMultiple
})

/**生成图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value) {
    return {}
  }

  return generateBarHorizontalConfig(props)
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
    const barClickData = {
      name: params.name,
      value: params.value,
      seriesName: params.seriesName
    }
    emit('barClick', barClickData)
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
  /**是否为多数据*/
  isMultipleData,
  /**数据验证结果*/
  dataValidation
})
</script>

<style lang="scss" scoped>
// 水平柱状图特有样式
.a-bar-horizontal-chart {
  position: relative;
  width: 100%;

  // 可以添加水平柱状图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
