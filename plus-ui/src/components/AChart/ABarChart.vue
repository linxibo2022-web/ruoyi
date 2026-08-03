<!--
柱状图组件

基础使用方式示例：

1. 基本用法 - 简单数值数组
<ABarChart
  :data="[120, 200, 150, 80, 70, 110, 130]"
  :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
/>

2. 多系列数据 - 对比展示
<ABarChart
  :data="[
    { name: '销售额', data: [120, 200, 150, 80, 70, 110, 130] },
    { name: '利润', data: [80, 120, 100, 60, 50, 80, 90] }
  ]"
  :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
  :show-legend="true"
/>

3. 堆叠柱状图
<ABarChart
  :data="stackData"
  :x-axis-data="categories"
  :stack="true"
  :show-legend="true"
/>

4. 自定义样式
<ABarChart
  :data="data"
  :x-axis-data="categories"
  bar-width="60%"
  :border-radius="8"
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

<script setup lang="ts" name="ABarChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { ChartConfigFactory } from './utils/chart-factory'
import { getChartThemeConfig } from './composables/useChart'
import { BaseChartProps, SeriesDataItem } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**柱状图组件属性接口*/
export interface ABarChartProps extends BaseChartProps {
  /**数据：支持简单数组或复杂对象数组*/
  data?: number[] | SeriesDataItem[]
  /**X轴数据（类目轴）*/
  xAxisData?: string[]
  /**柱宽度*/
  barWidth?: string | number
  /**是否堆叠*/
  stack?: boolean
  /**柱子圆角*/
  borderRadius?: number
  /**Y轴最小值*/
  yAxisMin?: number | string
  /**Y轴最大值*/
  yAxisMax?: number | string
  /**Y轴步进值*/
  yAxisInterval?: number
  /**Y轴分割段数*/
  yAxisSplitNumber?: number
  /**Y轴最小间隔(防止小数刻度)*/
  yAxisMinInterval?: number
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<ABarChartProps>(), {
  /**默认高度*/
  height: '16rem',
  /**是否加载中*/
  loading: false,
  /**是否为空*/
  isEmpty: false,
  /**默认数据*/
  data: () => [0, 0, 0, 0, 0, 0, 0],
  /**X轴数据*/
  xAxisData: () => [],
  /**柱宽度*/
  barWidth: '40%',
  /**是否堆叠*/
  stack: false,
  /**圆角大小*/
  borderRadius: 4,
  /**是否显示坐标轴标签*/
  showAxisLabel: true,
  /**是否显示坐标轴线*/
  showAxisLine: true,
  /**是否显示分割线*/
  showSplitLine: true,
  /**是否显示提示框*/
  showTooltip: true,
  /**是否显示图例*/
  showLegend: false,
  /**图例位置*/
  legendPosition: 'bottom' as const,
  /**主题色彩配置*/
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
const { generateBarConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateBarData(props)
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

  return generateBarConfig(props)
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
// 柱状图特有样式
.a-bar-chart {
  position: relative;
  width: 100%;

  // 可以添加柱状图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
