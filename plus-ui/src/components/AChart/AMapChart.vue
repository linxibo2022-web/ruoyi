<!--
地图图表组件

基础使用方式示例：

1. 基本用法 - 中国地图
<AMapChart
  :map-data="[
    { name: '北京', value: 100, adcode: '110000', level: 'province' },
    { name: '上海', value: 200, adcode: '310000', level: 'province' },
    { name: '广东', value: 300, adcode: '440000', level: 'province' }
  ]"
/>

2. 带散点标记
<AMapChart
  :map-data="provinceData"
  :show-scatter="true"
  :show-labels="true"
/>

3. 隐藏标签的简洁地图
<AMapChart
  :map-data="mapData"
  :show-labels="false"
  :show-scatter="false"
/>

4. 自定义高度的地图
<AMapChart
  :map-data="mapData"
  height="calc(100vh - 200px)"
  @region-click="handleRegionClick"
/>
-->
<template>
  <div class="a-map-chart" :style="{ height: containerHeight }">
    <div v-if="isEmpty" class="chart-empty-state">
      <slot name="empty">
        <div class="empty-content">
          <i class="iconfont-sys empty-icon">&#xe6da;</i>
          <span class="empty-text">{{ t('message.noData') }}</span>
        </div>
      </slot>
    </div>

    <div v-else id="china-map" ref="chinaMapRef" class="china-map" />
  </div>
</template>

<script setup lang="ts" name="AMapChart">
import * as echarts from 'echarts'
import chinaMapJson from './json/chinaMap.json'
import { ChartConfigFactory } from './utils/chart-factory'
import { BaseChartProps } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

const { t } = useI18n()

/**地图数据项接口*/
export interface MapDataItem {
  /**区域名称*/
  name: string
  /**数据值*/
  value: number
  /**区域代码*/
  adcode?: string
  /**级别*/
  level?: string
  /**是否选中*/
  selected?: boolean
}

/**区域点击事件数据接口*/
export interface RegionClickData {
  /**区域名称*/
  name: string
  /**区域代码*/
  adcode: string
  /**级别*/
  level: string
}

/**地图组件属性接口*/
interface AMapChartProps extends BaseChartProps {
  /**地图数据*/
  mapData?: MapDataItem[]
  /**选中的区域*/
  selectedRegion?: string
  /**是否显示标签*/
  showLabels?: boolean
  /**是否显示散点标记*/
  showScatter?: boolean
  /**自定义高度*/
  height?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<AMapChartProps>(), {
  mapData: () => [],
  selectedRegion: '',
  showLabels: true,
  showScatter: true,
  isEmpty: false,
  height: '16rem'
})

/**组件事件定义*/
const emit = defineEmits<{
  /**渲染完成*/
  renderComplete: []
  /**区域点击*/
  regionClick: [region: RegionClickData]
  /**图表准备就绪*/
  chartReady: [chart: echarts.ECharts]
}>()

/**响应式引用*/
const chinaMapRef = ref<HTMLElement | null>(null)
const chartInstance = shallowRef<echarts.ECharts | null>(null)

const layout = useLayout()

// 计算属性：获取暗黑模式状态
const isDark = computed(() => layout.dark.value)

/**容器高度*/
const containerHeight = computed(() => props.height)

/**数据验证结果*/
const dataValidation = computed(() => {
  return ChartConfigFactory.validateMapData(props)
})

/**检查是否为空数据*/
const isEmpty = computed(() => {
  return dataValidation.value.isEmpty
})

/**
 * 根据 geoJson 数据准备地图数据
 * @param geoJson 地理数据
 * @returns 处理后的地图数据
 */
const prepareMapData = (geoJson: any): MapDataItem[] => {
  return geoJson.features.map((feature: any) => ({
    name: feature.properties.name,
    value: Math.round(Math.random() * 1000),
    adcode: feature.properties.adcode,
    level: feature.properties.level,
    selected: false
  }))
}

/**
 * 获取主题相关的样式配置
 * @returns 样式配置对象
 */
const getThemeStyles = () => ({
  borderColor: isDark.value ? 'rgba(255,255,255,0.6)' : 'rgba(147,235,248,1)',
  shadowColor: isDark.value ? 'rgba(0,0,0,0.8)' : 'rgba(128,217,248,1)',
  labelColor: isDark.value ? '#fff' : '#333',
  backgroundColor: isDark.value ? 'rgba(0,0,0,0.8)' : 'rgba(255,255,255,0.9)'
})

/**
 * 构造 ECharts 配置项
 * @param mapData 地图数据
 * @returns ECharts配置对象
 */
const createChartOption = (mapData: MapDataItem[]) => {
  const themeStyles = getThemeStyles()

  return {
    animation: false, // 关闭动画效果，减少鼠标移动高亮时的掉帧感
    tooltip: {
      show: true,
      backgroundColor: themeStyles.backgroundColor,
      borderColor: isDark.value ? '#333' : '#ddd',
      borderWidth: 1,
      textStyle: {
        color: themeStyles.labelColor
      },
      formatter: ({ data }: any) => {
        const { name, adcode, level } = data || {}
        return `
          <div style="padding: 8px;">
            <div><strong>${t('chart.map.name')}:</strong> ${name || t('chart.map.unknownRegion')}</div>
            <div><strong>${t('chart.map.code')}:</strong> ${adcode || t('common.empty')}</div>
            <div><strong>${t('chart.map.level')}:</strong> ${level || t('common.empty')}</div>
          </div>
        `
      }
    },
    geo: {
      map: 'china',
      zoom: 1,
      show: true,
      roam: 'move',
      scaleLimit: {
        min: 0.8,
        max: 3
      },
      layoutSize: '100%',
      emphasis: {
        label: { show: props.showLabels },
        itemStyle: {
          areaColor: 'rgba(82,180,255,0.9)',
          borderColor: '#fff',
          borderWidth: 3
        }
      },
      itemStyle: {
        borderColor: themeStyles.borderColor,
        borderWidth: 2,
        shadowColor: themeStyles.shadowColor,
        shadowOffsetX: 2,
        shadowOffsetY: 15,
        shadowBlur: 15
      }
    },
    series: [
      {
        type: 'map',
        map: 'china',
        aspectScale: 0.75,
        zoom: 1,
        label: {
          show: props.showLabels,
          color: '#fff',
          fontSize: 10
        },
        itemStyle: {
          borderColor: 'rgba(147,235,248,0.8)',
          borderWidth: 2,
          areaColor: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(147,235,248,0.3)' },
              { offset: 1, color: 'rgba(32,120,207,0.9)' }
            ]
          },
          shadowColor: 'rgba(32,120,207,1)',
          shadowOffsetY: 15,
          shadowBlur: 20
        },
        emphasis: {
          label: {
            show: true,
            color: '#fff',
            fontSize: 12
          },
          itemStyle: {
            areaColor: 'rgba(82,180,255,0.9)',
            borderColor: '#fff',
            borderWidth: 3
          }
        },
        select: {
          label: {
            show: true,
            color: '#fff',
            fontWeight: 'bold'
          },
          itemStyle: {
            areaColor: '#4FAEFB',
            borderColor: '#fff',
            borderWidth: 2
          }
        },
        data: mapData
      },
      // 散点标记配置（例如：城市标记）
      ...(props.showScatter
        ? [
            {
              name: '城市',
              type: 'scatter',
              coordinateSystem: 'geo',
              symbol: 'pin',
              symbolSize: 15,
              label: { show: false },
              itemStyle: {
                color: '#F99020',
                shadowBlur: 10,
                shadowColor: '#333'
              },
              data: [
                { name: '北京', value: [116.405285, 39.904989, 100] },
                { name: '上海', value: [121.472644, 31.231706, 100] },
                { name: '深圳', value: [114.085947, 22.547, 100] }
              ]
            }
          ]
        : [])
    ]
  }
}

/**
 * 初始化并渲染地图
 */
const initMap = async (): Promise<void> => {
  if (!chinaMapRef.value) return

  chartInstance.value = echarts.init(chinaMapRef.value)

  echarts.registerMap('china', chinaMapJson as any)
  const mapData = props.mapData.length > 0 ? props.mapData : prepareMapData(chinaMapJson)
  const option = createChartOption(mapData)

  chartInstance.value.setOption(option)

  // 绑定事件
  chartInstance.value.on('click', handleMapClick)

  emit('chartReady', chartInstance.value)
  emit('renderComplete')
}

/**
 * 处理地图点击事件
 * @param params 点击参数
 */
const handleMapClick = (params: any) => {
  if (params.componentType === 'series') {
    const regionData: RegionClickData = {
      name: params.name,
      adcode: params.data?.adcode || '',
      level: params.data?.level || ''
    }

    console.log(`选中区域: ${params.name}`, params)

    // 高亮选中区域
    chartInstance.value?.dispatchAction({
      type: 'select',
      seriesIndex: 0,
      dataIndex: params.dataIndex
    })

    emit('regionClick', regionData)
  }
}

/**
 * 窗口 resize 时调整图表大小
 */
const resizeChart = () => {
  chartInstance.value?.resize()
}

/**
 * 处理组件销毁
 */
const cleanupChart = () => {
  if (chartInstance.value) {
    chartInstance.value.off('click', handleMapClick)
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  window.removeEventListener('resize', resizeChart)
}

/**
 * 更新地图数据
 * @param newMapData 新的地图数据
 */
const updateMapData = (newMapData: MapDataItem[]) => {
  if (chartInstance.value && !isEmpty.value) {
    const option = createChartOption(newMapData)
    chartInstance.value.setOption(option)
  }
}

/**生命周期钩子*/
onMounted(() => {
  if (!isEmpty.value) {
    initMap().then(() => {
      setTimeout(resizeChart, 100)
    })
  }
  window.addEventListener('resize', resizeChart)
})

onUnmounted(cleanupChart)

/**监听主题变化，重新初始化地图*/
watch(isDark, (newVal, oldVal) => {
  if (newVal !== oldVal && chartInstance.value) {
    cleanupChart()
    nextTick(() => {
      if (!isEmpty.value) {
        initMap()
      }
    })
  }
})

/**监听数据变化*/
watch(
  () => props.mapData,
  (newData) => {
    updateMapData(newData)
  },
  { deep: true }
)

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
  /**获取图表实例*/
  getChartInstance: () => chartInstance.value,
  /**更新地图数据*/
  updateMapData,
  /**重置图表尺寸*/
  resizeChart,
  /**清理图表*/
  cleanupChart
})
</script>

<style lang="scss" scoped>
.a-map-chart {
  position: relative;
  width: 100%;

  .chart-empty-state {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;

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

  .china-map {
    width: 100%;
    height: 100%;
    overflow: hidden;
    border-radius: 8px;
  }
}

// 暗色主题适配
.dark {
  .a-map-chart {
    .chart-empty-state .empty-content {
      .empty-text {
        color: var(--el-text-color-secondary);
      }

      .empty-icon {
        color: var(--el-text-color-disabled);
      }
    }
  }
}
</style>
