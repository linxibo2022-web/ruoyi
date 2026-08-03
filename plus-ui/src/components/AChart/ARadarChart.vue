<!--
雷达图组件

基础使用方式示例：

1. 基本用法 - 单个数据系列
<ARadarChart
  :indicator="[
    { name: '销售', max: 100 },
    { name: '管理', max: 100 },
    { name: '信息技术', max: 100 },
    { name: '客服', max: 100 },
    { name: '研发', max: 100 },
    { name: '市场', max: 100 }
  ]"
  :data="[
    { name: '预算分配', value: [43, 72, 65, 53, 99, 70] }
  ]"
/>

2. 多个数据系列对比
<ARadarChart
  :indicator="indicators"
  :data="[
    { name: '张三', value: [80, 90, 85, 75, 95, 88] },
    { name: '李四', value: [70, 85, 90, 80, 85, 92] },
    { name: '王五', value: [90, 80, 75, 85, 80, 85] }
  ]"
  :show-legend="true"
/>

3. 自定义颜色和样式
<ARadarChart
  :indicator="indicators"
  :data="data"
  :colors="['#5470c6', '#91cc75', '#fac858']"
  :show-legend="true"
  legend-position="bottom"
/>

4. 设置指标范围
<ARadarChart
  :indicator="[
    { name: '语文', max: 150, min: 0 },
    { name: '数学', max: 150, min: 0 },
    { name: '英语', max: 150, min: 0 },
    { name: '物理', max: 100, min: 0 },
    { name: '化学', max: 100, min: 0 },
    { name: '生物', max: 100, min: 0 }
  ]"
  :data="studentScores"
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

<script setup lang="ts" name="ARadarChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { getChartThemeConfig } from './composables/useChart'
import { ChartConfigFactory } from './utils/chart-factory'
import { BaseChartProps } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**雷达图指标接口*/
export interface RadarIndicator {
  /**指标名称*/
  name: string
  /**最大值*/
  max: number
  /**最小值*/
  min?: number
}

/**雷达图数据项接口*/
export interface RadarDataItem {
  /**数据名称*/
  name: string
  /**数据值数组*/
  value: number[]
}

/**雷达图组件属性接口*/
export interface ARadarChartProps extends BaseChartProps {
  /**雷达图指标配置*/
  indicator?: RadarIndicator[]
  /**数据*/
  data?: RadarDataItem[]
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<ARadarChartProps>(), {
  /**默认高度*/
  height: '16rem',
  /**是否加载中*/
  loading: false,
  /**是否为空*/
  isEmpty: false,
  /**指标配置*/
  indicator: () => [],
  /**数据*/
  data: () => [],
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
  /**数据系列点击事件*/
  seriesClick: [
    params: {
      name: string
      value: number[]
      indicator: RadarIndicator
      indicatorIndex: number
    }
  ]
}>()

/**使用图表配置生成器*/
const { generateRadarConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateRadarData(props)
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**指标数量*/
const indicatorCount = computed(() => {
  return props.indicator?.length || 0
})

/**数据系列数量*/
const seriesCount = computed(() => {
  return props.data?.length || 0
})

/**验证数据完整性*/
const dataIntegrity = computed(() => {
  if (!props.data?.length || !props.indicator?.length) {
    return { isValid: false, message: '数据或指标为空' }
  }

  // 检查每个数据系列的值数量是否与指标数量匹配
  const invalidSeries = props.data.filter((item) => !item.value || item.value.length !== indicatorCount.value)

  if (invalidSeries.length > 0) {
    return {
      isValid: false,
      message: `数据系列 "${invalidSeries[0].name}" 的值数量与指标数量不匹配`
    }
  }

  return { isValid: true, message: '数据完整' }
})

/**生成图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value || !dataIntegrity.value.isValid) {
    return {}
  }

  return generateRadarConfig(props)
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

  // 如果是雷达图数据点击，触发专门的系列点击事件
  if (params.componentType === 'series' && params.seriesType === 'radar') {
    const dataIndex = params.dataIndex
    const indicator = props.indicator?.[dataIndex]

    if (indicator) {
      const seriesClickData = {
        name: params.name,
        value: params.value,
        indicator,
        indicatorIndex: dataIndex
      }
      emit('seriesClick', seriesClickData)
    }
  }
}

/**
 * 获取指定系列的数据
 * @param seriesName 系列名称
 * @returns 系列数据
 */
const getSeriesData = (seriesName: string): RadarDataItem | undefined => {
  return props.data?.find((item) => item.name === seriesName)
}

/**
 * 获取指定指标的信息
 * @param indicatorName 指标名称
 * @returns 指标信息
 */
const getIndicatorInfo = (indicatorName: string): RadarIndicator | undefined => {
  return props.indicator?.find((item) => item.name === indicatorName)
}

/**
 * 计算指定系列在指定指标上的得分百分比
 * @param seriesName 系列名称
 * @param indicatorName 指标名称
 * @returns 百分比（0-100）
 */
const getScorePercentage = (seriesName: string, indicatorName: string): number => {
  const seriesData = getSeriesData(seriesName)
  const indicator = getIndicatorInfo(indicatorName)

  if (!seriesData || !indicator) return 0

  const indicatorIndex = props.indicator?.findIndex((item) => item.name === indicatorName) || 0
  const value = seriesData.value[indicatorIndex] || 0
  const max = indicator.max || 100
  const min = indicator.min || 0

  return Number((((value - min) / (max - min)) * 100).toFixed(1))
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露组件方法和数据*/
defineExpose({
  /**是否为空*/
  isEmpty,
  /**指标数量*/
  indicatorCount,
  /**数据系列数量*/
  seriesCount,
  /**数据验证结果*/
  dataValidation,
  /**数据完整性验证*/
  dataIntegrity,
  /**获取系列数据*/
  getSeriesData,
  /**获取指标信息*/
  getIndicatorInfo,
  /**获取得分百分比*/
  getScorePercentage
})
</script>

<style lang="scss" scoped>
// 雷达图特有样式
.a-radar-chart {
  position: relative;
  width: 100%;

  // 可以添加雷达图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
