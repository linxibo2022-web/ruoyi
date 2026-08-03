<!--
折线图组件，支持多组数据，支持阶梯式动画效果

基础使用方式示例：

1. 基本用法 - 简单数值数组
<ALineChart
  :data="[120, 200, 150, 80, 70, 110, 130]"
  :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
/>

2. 多系列数据 - 对比趋势
<ALineChart
  :data="[
    { name: '访问量', data: [120, 200, 150, 80, 70, 110, 130] },
    { name: '用户数', data: [80, 120, 100, 60, 50, 80, 90] }
  ]"
  :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
  :show-legend="true"
/>

3. 区域填充
<ALineChart
  :data="data"
  :x-axis-data="categories"
  :show-area-color="true"
  :smooth="true"
/>

4. 自定义样式
<ALineChart
  :data="data"
  :x-axis-data="categories"
  :line-width="3"
  symbol="circle"
  :symbol-size="8"
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

<script setup lang="ts" name="ALineChart">
import type { EChartsOption } from 'echarts'
import { useChartConfig } from './composables/useChartConfig'
import { getChartThemeConfig } from './composables/useChart'
import { ChartConfigFactory } from './utils/chart-factory'
import { BaseChartProps, SeriesDataItem } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**折线图数据项接口*/
export interface LineDataItem extends SeriesDataItem {
  /**是否平滑曲线*/
  smooth?: boolean
  /**线条宽度*/
  lineWidth?: number
  /**标记点样式*/
  symbol?: string
  /**标记点大小*/
  symbolSize?: number
  /**是否显示区域填充*/
  showAreaColor?: boolean
  /**区域样式配置*/
  areaStyle?: {
    /**是否为自定义样式*/
    custom?: any
    /**起始透明度*/
    startOpacity?: number
    /**结束透明度*/
    endOpacity?: number
  }
}

/**折线图组件属性接口*/
export interface ALineChartProps extends BaseChartProps {
  /**数据：支持简单数组或复杂对象数组*/
  data?: number[] | LineDataItem[]
  /**X轴数据*/
  xAxisData?: string[]
  /**线条宽度*/
  lineWidth?: number
  /**是否显示区域填充*/
  showAreaColor?: boolean
  /**是否平滑曲线*/
  smooth?: boolean
  /**标记点样式*/
  symbol?: string
  /**标记点大小*/
  symbolSize?: number
  /**动画延迟*/
  animationDelay?: number
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
const props = withDefaults(defineProps<ALineChartProps>(), {
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
  /**线条宽度*/
  lineWidth: 2.5,
  /**是否显示区域填充*/
  showAreaColor: false,
  /**是否平滑曲线*/
  smooth: true,
  /**标记点样式*/
  symbol: 'none',
  /**标记点大小*/
  symbolSize: 6,
  /**动画延迟*/
  animationDelay: 200,
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
  colors: () => getChartThemeConfig().colors,
  /**Y轴配置默认值*/
  yAxisMin: undefined,
  yAxisMax: undefined,
  yAxisInterval: undefined,
  yAxisSplitNumber: 5,
  yAxisMinInterval: undefined
})

/**组件事件定义*/
const emit = defineEmits<{
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**图表点击事件*/
  chartClick: [params: any]
  /**数据点点击事件*/
  pointClick: [params: { name: string; value: number; seriesName?: string }]
  /**动画完成事件*/
  animationFinished: []
}>()

/**使用图表配置生成器*/
const { generateLineConfig } = useChartConfig()

/**动画状态管理*/
const isAnimating = ref(false)
const animationTimer = ref<ReturnType<typeof setTimeout>>()
const animatedData = ref<number[] | LineDataItem[]>([])

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateLineData(props)
})

/**是否为空状态*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**判断是否为多数据系列*/
const isMultipleData = computed(() => {
  return dataValidation.value.isMultiple
})

/**清理动画定时器*/
const clearAnimationTimer = () => {
  if (animationTimer.value) {
    clearTimeout(animationTimer.value)
    animationTimer.value = undefined
  }
}

/**初始化动画数据*/
const initAnimationData = () => {
  return ChartConfigFactory.initLineAnimationData(props, isMultipleData.value)
}

/**复制真实数据*/
const copyRealData = () => {
  return ChartConfigFactory.copyRealData(props.data, isMultipleData.value)
}

/**生成图表配置（支持动画数据）*/
const generateChartOptions = (isInitial = false): EChartsOption => {
  return generateLineConfig(props, isInitial ? animatedData.value : undefined)
}

/**当前使用的图表配置*/
const chartConfig = computed((): EChartsOption => {
  if (isEmpty.value) {
    return {}
  }

  // 如果正在动画中，使用动画数据；否则使用原始数据
  return generateLineConfig(props, isAnimating.value ? animatedData.value : undefined)
})

/**
 * 初始化带动画的图表
 */
const initChartWithAnimation = () => {
  if (!isEmpty.value) {
    clearAnimationTimer()
    isAnimating.value = true

    // 如果是多数据情况，使用阶梯式动画
    if (isMultipleData.value) {
      const multiData = props.data as LineDataItem[]

      // 先将数据初始化为0
      animatedData.value = initAnimationData()

      // 阶梯式更新每组数据
      multiData.forEach((item, index) => {
        setTimeout(
          () => {
            // 逐个更新数据组
            const currentAnimatedData = animatedData.value as LineDataItem[]
            currentAnimatedData[index] = { ...item }
            animatedData.value = [...currentAnimatedData]
          },
          index * props.animationDelay + 100
        )
      })

      // 标记动画完成
      const totalDelay = (multiData.length - 1) * props.animationDelay + 1500
      setTimeout(() => {
        isAnimating.value = false
        emit('animationFinished')
      }, totalDelay)
    } else {
      // 单数据情况保持原有的简单动画
      animatedData.value = initAnimationData()

      animationTimer.value = setTimeout(() => {
        animatedData.value = copyRealData() as number[] | LineDataItem[]
        isAnimating.value = false
        emit('animationFinished')
      }, 100)
    }
  } else {
    animatedData.value = copyRealData() as number[] | LineDataItem[]
  }
}

/**
 * 处理图表准备就绪事件
 * @param chart 图表实例
 */
const handleChartReady = (chart: any) => {
  emit('chartReady', chart)

  // 图表准备完成后启动动画
  nextTick(() => {
    initChartWithAnimation()
  })
}

/**
 * 处理图表点击事件
 * @param params 点击参数
 */
const handleChartClick = (params: any) => {
  emit('chartClick', params)

  // 如果是数据点点击，触发专门的点击事件
  if (params.componentType === 'series' && params.seriesType === 'line') {
    const pointClickData = {
      name: params.name,
      value: params.value,
      seriesName: params.seriesName
    }
    emit('pointClick', pointClickData)
  }
}

/**
 * 处理图表进入可视区域时的动画
 */
const handleChartVisible = () => {
  initChartWithAnimation()
}

/**监听数据变化 - 优化监听器，减少不必要的重新渲染*/
watch(
  [() => props.data, () => props.xAxisData, () => props.colors],
  () => {
    // 只有在不播放动画时才触发重新渲染
    if (!isAnimating.value) {
      initChartWithAnimation()
    }
  },
  { deep: true }
)

/**组件挂载时设置事件监听*/
onMounted(() => {
  // 这里可以添加自定义的图表可见事件监听
  // 实际的图表初始化由AChartBase处理
})

/**组件卸载前清理*/
onBeforeUnmount(() => {
  clearAnimationTimer()
})

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
  /**是否正在动画*/
  isAnimating,
  /**数据验证结果*/
  dataValidation,
  /**手动触发动画*/
  triggerAnimation: initChartWithAnimation
})
</script>

<style lang="scss" scoped>
// 折线图特有样式
.a-line-chart {
  position: relative;
  width: calc(100% + 10px);

  // 可以添加折线图特有的样式定制
  :deep(.chart-content) {
    // ECharts 容器样式调整
  }
}
</style>
