<!--
饼图/环形图组件

基础使用方式示例：

1. 基本饼图
<APieChart
  :data="[
    { name: '直接访问', value: 335 },
    { name: '邮件营销', value: 310 },
    { name: '联盟广告', value: 234 },
    { name: '视频广告', value: 135 }
  ]"
/>

2. 环形图（甜甜圈图）
<APieChart
  :data="pieData"
  :radius="['40%', '70%']"
  center-text="总销售额"
/>

3. 带图例的饼图
<APieChart
  :data="pieData"
  :show-legend="true"
  legend-position="right"
/>

4. 带标签的饼图
<APieChart
  :data="pieData"
  :show-label="true"
  :border-radius="5"
/>

5. 自定义颜色
<APieChart
  :data="pieData"
  :colors="['#5470c6', '#91cc75', '#fac858', '#ee6666']"
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

<script setup lang="ts" name="APieChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { getChartThemeConfig } from './composables/useChart'
import { ChartConfigFactory } from './utils/chart-factory'
import { BaseChartProps, ChartDataItem } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**饼图组件属性接口*/
export interface APieChartProps extends BaseChartProps {
  /**数据*/
  data?: ChartDataItem[]
  /**半径配置 [内半径, 外半径]*/
  radius?: [string, string]
  /**圆角大小*/
  borderRadius?: number
  /**中心文字*/
  centerText?: string
  /**是否显示标签*/
  showLabel?: boolean
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<APieChartProps>(), {
  /**默认高度*/
  height: '16rem',
  /**是否加载中*/
  loading: false,
  /**是否为空*/
  isEmpty: false,
  /**默认数据*/
  data: () => [],
  /**半径配置 [内半径, 外半径]*/
  radius: () => ['50%', '80%'] as [string, string],
  /**圆角大小*/
  borderRadius: 10,
  /**中心文字*/
  centerText: '',
  /**是否显示标签*/
  showLabel: false,
  /**是否显示提示框*/
  showTooltip: true,
  /**是否显示图例*/
  showLegend: false,
  /**图例位置*/
  legendPosition: 'right' as const,
  colors: () => getChartThemeConfig().colors
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**图表点击事件*/
  chartClick: [params: any]
  /**扇区点击事件*/
  sectorClick: [params: { name: string; value: number; percent: number }]
  /**图例点击事件*/
  legendClick: [params: { name: string; selected: boolean }]
}>()

/**使用图表配置生成器*/
const { generatePieConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validatePieData(props)
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**是否为环形图*/
const isDonut = computed(() => {
  if (!props.radius || !Array.isArray(props.radius)) return false

  const innerRadius = parseFloat(props.radius[0])
  return innerRadius > 0
})

/**计算数据总和*/
const dataTotal = computed(() => {
  if (!props.data?.length) return 0
  return props.data.reduce((sum, item) => sum + item.value, 0)
})

/**生成图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value) {
    return {}
  }

  return generatePieConfig(props)
})

/**
 * 处理图表准备就绪事件
 * @param chart 图表实例
 */
const handleChartReady = (chart: any) => {
  // 绑定图例点击事件
  chart.on('legendselectchanged', (params: any) => {
    emit('legendClick', {
      name: params.name,
      selected: params.selected[params.name]
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

  // 如果是扇区点击，触发专门的扇区点击事件
  if (params.componentType === 'series' && params.seriesType === 'pie') {
    const sectorClickData = {
      name: params.name,
      value: params.value,
      percent: params.percent
    }
    emit('sectorClick', sectorClickData)
  }
}

/**
 * 获取指定数据项的百分比
 * @param name 数据项名称
 * @returns 百分比
 */
const getPercentage = (name: string): number => {
  if (!props.data?.length || dataTotal.value === 0) return 0

  const item = props.data.find((item) => item.name === name)
  if (!item) return 0

  return Number(((item.value / dataTotal.value) * 100).toFixed(1))
}

/**
 * 获取指定颜色
 * @param index 索引
 * @returns 颜色值
 */
const getColorByIndex = (index: number): string => {
  if (!props.colors?.length) return getChartThemeConfig().colors[0]
  return props.colors[index % props.colors.length]
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露组件方法和数据*/
defineExpose({
  /**是否为空*/
  isEmpty,
  /**是否为环形图*/
  isDonut,
  /**数据总和*/
  dataTotal,
  /**数据验证结果*/
  dataValidation,
  /**获取百分比*/
  getPercentage,
  /**根据索引获取颜色*/
  getColorByIndex
})
</script>

<style lang="scss" scoped>
// 饼图特有样式
.a-pie-chart {
  position: relative;
  width: 100%;

  // 可以添加饼图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
