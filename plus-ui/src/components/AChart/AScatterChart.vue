<!--
散点图组件

基础使用方式示例：

1. 基本用法 - 简单散点分布
<AScatterChart
  :data="[
    { value: [10, 20] },
    { value: [15, 25] },
    { value: [20, 18] },
    { value: [25, 30] },
    { value: [30, 22] }
  ]"
/>

2. 带名称的散点
<AScatterChart
  :data="[
    { value: [10, 20], name: '数据点1' },
    { value: [15, 25], name: '数据点2' },
    { value: [20, 18], name: '数据点3' },
    { value: [25, 30], name: '数据点4' },
    { value: [30, 22], name: '数据点5' }
  ]"
/>

3. 自定义样式
<AScatterChart
  :data="scatterData"
  :symbol-size="20"
  :colors="['#5470c6']"
  :show-axis-line="true"
  :show-split-line="true"
/>

4. 相关性分析散点图
<AScatterChart
  :data="correlationData"
  :symbol-size="12"
  height="20rem"
  :show-tooltip="true"
/>

5. 多系列散点图（通过多个组件实现）
<div class="scatter-container">
  <AScatterChart
    :data="series1Data"
    :colors="['#5470c6']"
  />
  <AScatterChart
    :data="series2Data"
    :colors="['#91cc75']"
  />
</div>
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

<script setup lang="ts" name="AScatterChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { ChartConfigFactory } from './utils/chart-factory'
import { getChartThemeConfig } from './composables/useChart'
import { BaseChartProps } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**散点图数据项接口*/
export interface ScatterDataItem {
  /**数据值 [x, y]*/
  value: [number, number]
  /**数据名称*/
  name?: string
}

/**散点图组件属性接口*/
export interface AScatterChartProps extends BaseChartProps {
  /**数据*/
  data?: ScatterDataItem[]
  /**标记点大小*/
  symbolSize?: number
}

/**组件属性定义 - 使用默认值*/
const props = withDefaults(defineProps<AScatterChartProps>(), {
  /**默认高度*/
  height: '16rem',
  /**是否加载中*/
  loading: false,
  /**是否为空*/
  isEmpty: false,
  /**默认数据*/
  data: () => [{ value: [0, 0] }, { value: [0, 0] }],
  /**标记点大小*/
  symbolSize: 14,
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
  colors: () => getChartThemeConfig().colors
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**图表点击事件*/
  chartClick: [params: any]
  /**散点点击事件*/
  pointClick: [
    params: {
      name?: string
      value: [number, number]
      x: number
      y: number
    }
  ]
}>()

/**使用图表配置生成器*/
const { generateScatterConfig } = useChartConfig()

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateScatterData(props)
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**数据点数量*/
const dataPointCount = computed(() => {
  return props.data?.length || 0
})

/**数据范围统计*/
const dataRange = computed(() => {
  if (!props.data?.length) {
    return {
      xMin: 0,
      xMax: 0,
      yMin: 0,
      yMax: 0,
      xRange: 0,
      yRange: 0
    }
  }

  const xValues = props.data.map((item) => item.value[0])
  const yValues = props.data.map((item) => item.value[1])

  const xMin = Math.min(...xValues)
  const xMax = Math.max(...xValues)
  const yMin = Math.min(...yValues)
  const yMax = Math.max(...yValues)

  return {
    xMin,
    xMax,
    yMin,
    yMax,
    xRange: xMax - xMin,
    yRange: yMax - yMin
  }
})

/**数据统计信息*/
const dataStatistics = computed(() => {
  if (!props.data?.length) {
    return {
      count: 0,
      xMean: 0,
      yMean: 0,
      xStd: 0,
      yStd: 0,
      correlation: 0
    }
  }

  const count = props.data.length
  const xValues = props.data.map((item) => item.value[0])
  const yValues = props.data.map((item) => item.value[1])

  // 计算均值
  const xMean = xValues.reduce((sum, val) => sum + val, 0) / count
  const yMean = yValues.reduce((sum, val) => sum + val, 0) / count

  // 计算标准差
  const xVariance = xValues.reduce((sum, val) => sum + Math.pow(val - xMean, 2), 0) / count
  const yVariance = yValues.reduce((sum, val) => sum + Math.pow(val - yMean, 2), 0) / count
  const xStd = Math.sqrt(xVariance)
  const yStd = Math.sqrt(yVariance)

  // 计算相关系数
  const covariance =
    props.data.reduce((sum, item) => {
      return sum + (item.value[0] - xMean) * (item.value[1] - yMean)
    }, 0) / count
  const correlation = xStd * yStd !== 0 ? covariance / (xStd * yStd) : 0

  return {
    count,
    xMean: Number(xMean.toFixed(2)),
    yMean: Number(yMean.toFixed(2)),
    xStd: Number(xStd.toFixed(2)),
    yStd: Number(yStd.toFixed(2)),
    correlation: Number(correlation.toFixed(3))
  }
})

/**生成图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value) {
    return {}
  }

  return generateScatterConfig(props)
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

  // 如果是散点点击，触发专门的散点点击事件
  if (params.componentType === 'series' && params.seriesType === 'scatter') {
    const [x, y] = params.value
    const pointClickData = {
      name: params.name,
      value: params.value,
      x,
      y
    }
    emit('pointClick', pointClickData)
  }
}

/**
 * 获取指定坐标范围内的数据点
 * @param xRange X轴范围 [min, max]
 * @param yRange Y轴范围 [min, max]
 * @returns 符合条件的数据点
 */
const getPointsInRange = (xRange: [number, number], yRange: [number, number]): ScatterDataItem[] => {
  if (!props.data?.length) return []

  return props.data.filter((item) => {
    const [x, y] = item.value
    return x >= xRange[0] && x <= xRange[1] && y >= yRange[0] && y <= yRange[1]
  })
}

/**
 * 查找最接近指定坐标的数据点
 * @param targetX 目标X坐标
 * @param targetY 目标Y坐标
 * @returns 最近的数据点及距离
 */
const findNearestPoint = (targetX: number, targetY: number) => {
  if (!props.data?.length) return null

  let nearestPoint = props.data[0]
  let minDistance = Infinity

  props.data.forEach((item) => {
    const [x, y] = item.value
    const distance = Math.sqrt(Math.pow(x - targetX, 2) + Math.pow(y - targetY, 2))

    if (distance < minDistance) {
      minDistance = distance
      nearestPoint = item
    }
  })

  return {
    point: nearestPoint,
    distance: Number(minDistance.toFixed(2))
  }
}

/**
 * 检测异常值（使用IQR方法）
 * @returns 异常值数据点
 */
const detectOutliers = () => {
  if (!props.data?.length || props.data.length < 4) return []

  const xValues = props.data.map((item) => item.value[0]).sort((a, b) => a - b)
  const yValues = props.data.map((item) => item.value[1]).sort((a, b) => a - b)

  // 计算四分位数
  const getQuartiles = (values: number[]) => {
    const q1Index = Math.floor(values.length * 0.25)
    const q3Index = Math.floor(values.length * 0.75)
    const q1 = values[q1Index]
    const q3 = values[q3Index]
    const iqr = q3 - q1
    return {
      q1,
      q3,
      iqr,
      lowerBound: q1 - 1.5 * iqr,
      upperBound: q3 + 1.5 * iqr
    }
  }

  const xQuartiles = getQuartiles(xValues)
  const yQuartiles = getQuartiles(yValues)

  return props.data.filter((item) => {
    const [x, y] = item.value
    const isXOutlier = x < xQuartiles.lowerBound || x > xQuartiles.upperBound
    const isYOutlier = y < yQuartiles.lowerBound || y > yQuartiles.upperBound
    return isXOutlier || isYOutlier
  })
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露组件方法和数据*/
defineExpose({
  /**是否为空*/
  isEmpty,
  /**数据点数量*/
  dataPointCount,
  /**数据验证结果*/
  dataValidation,
  /**数据范围*/
  dataRange,
  /**数据统计信息*/
  dataStatistics,
  /**获取范围内的点*/
  getPointsInRange,
  /**查找最近的点*/
  findNearestPoint,
  /**检测异常值*/
  detectOutliers
})
</script>

<style lang="scss" scoped>
// 散点图特有样式
.a-scatter-chart {
  position: relative;
  width: 100%;

  // 可以添加散点图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
