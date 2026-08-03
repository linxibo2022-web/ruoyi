<!--
地图图表卡片组件 AMapChartCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <AMapChartCard
      title="全国销售分布"
      :map-data="[
        { name: '北京', value: 100, adcode: '110000', level: 'province' },
        { name: '上海', value: 200, adcode: '310000', level: 'province' },
        { name: '广东', value: 300, adcode: '440000', level: 'province' },
        { name: '浙江', value: 280, adcode: '330000', level: 'province' },
        { name: '江苏', value: 250, adcode: '320000', level: 'province' }
      ]"
    />
  </el-col>
</el-row>

2. 带指标显示
<el-row :gutter="20">
  <el-col :span="24">
    <AMapChartCard
      title="区域业绩分布"
      :total-value="8520"
      unit="万元"
      :map-data="[
        { name: '北京', value: 1520, adcode: '110000', level: 'province' },
        { name: '上海', value: 2100, adcode: '310000', level: 'province' },
        { name: '广东', value: 3200, adcode: '440000', level: 'province' },
        { name: '浙江', value: 1700, adcode: '330000', level: 'province' }
      ]"
      :show-scatter="true"
    />
  </el-col>
</el-row>

3. 两列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <AMapChartCard
      title="华东地区销售"
      :map-data="[
        { name: '上海', value: 850, adcode: '310000', level: 'province' },
        { name: '浙江', value: 720, adcode: '330000', level: 'province' },
        { name: '江苏', value: 680, adcode: '320000', level: 'province' }
      ]"
    />
  </el-col>
  <el-col :xs="24" :md="12">
    <AMapChartCard
      title="华北地区销售"
      :map-data="[
        { name: '北京', value: 920, adcode: '110000', level: 'province' },
        { name: '天津', value: 560, adcode: '120000', level: 'province' },
        { name: '河北', value: 480, adcode: '130000', level: 'province' }
      ]"
    />
  </el-col>
</el-row>

4. 完整功能组合
<el-row :gutter="20">
  <el-col :span="24">
    <AMapChartCard
      title="全国门店分布"
      subtitle="截止2024年3月"
      icon="map"
      icon-color="#91cc75"
      :total-value="356"
      unit="家"
      :map-data="[
        { name: '北京', value: 45, adcode: '110000', level: 'province' },
        { name: '上海', value: 62, adcode: '310000', level: 'province' },
        { name: '广东', value: 88, adcode: '440000', level: 'province' },
        { name: '浙江', value: 52, adcode: '330000', level: 'province' },
        { name: '江苏', value: 48, adcode: '320000', level: 'province' },
        { name: '四川', value: 35, adcode: '510000', level: 'province' },
        { name: '湖北', value: 26, adcode: '420000', level: 'province' }
      ]"
      :show-labels="true"
      :show-scatter="true"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="map-chart-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <!-- 左侧：图标和标题 -->
      <div class="header-left">
        <div v-if="icon" class="header-icon" :style="iconStyle">
          <Icon :code="icon" :size="20" />
        </div>
        <div class="header-content">
          <h3 class="card-title">{{ title }}</h3>
          <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
        </div>
      </div>

      <!-- 右侧：指标显示 -->
      <div v-if="totalValue !== undefined" class="header-right">
        <div class="metric-display">
          <el-statistic :value="totalValue" :precision="decimals" :group-separator="separator">
            <template #suffix>
              <span v-if="unit" class="metric-unit">{{ unit }}</span>
            </template>
          </el-statistic>
          <span v-if="description" class="metric-description">{{ description }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="showAction" class="header-action">
        <el-button text @click="handleAction">
          <Icon code="more" :size="16" />
        </el-button>
      </div>
    </div>

    <!-- 图表内容 -->
    <div class="card-body" :style="{ height: chartHeight }">
      <AMapChart
        :map-data="mapData"
        :selected-region="selectedRegion"
        :show-labels="showLabels"
        :show-scatter="showScatter"
        :loading="loading"
        :height="chartHeight"
        @chart-ready="handleChartReady"
        @region-click="handleRegionClick"
      >
        <template v-if="$slots.empty" #empty>
          <slot name="empty"></slot>
        </template>
        <template v-if="$slots.loading" #loading>
          <slot name="loading"></slot>
        </template>
      </AMapChart>
    </div>
  </div>
</template>

<script setup lang="ts" name="AMapChartCard">
import type { MapDataItem, RegionClickData } from '@/components/AChart/AMapChart.vue'
import { triggerChartResize } from '@/utils/function'

/**地图卡片属性接口*/
interface AMapChartCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**总数值*/
  totalValue?: number
  /**单位*/
  unit?: string
  /**描述文本*/
  description?: string
  /**图标代码*/
  icon?: IconCode
  /**图标颜色*/
  iconColor?: string
  /**图标背景色*/
  iconBgColor?: string
  /**地图数据*/
  mapData: MapDataItem[]
  /**选中的区域*/
  selectedRegion?: string
  /**是否显示标签*/
  showLabels?: boolean
  /**是否显示散点标记*/
  showScatter?: boolean
  /**图表高度*/
  chartHeight?: string
  /**是否显示操作按钮*/
  showAction?: boolean
  /**是否加载中*/
  loading?: boolean
  /**小数位数*/
  decimals?: number
  /**千分位分隔符*/
  separator?: string
}

/**组件属性定义*/
const props = withDefaults(defineProps<AMapChartCardProps>(), {
  subtitle: '',
  unit: '',
  description: '',
  iconColor: 'var(--el-color-primary)',
  iconBgColor: 'var(--el-color-primary-light-9)',
  selectedRegion: '',
  showLabels: true,
  showScatter: true,
  chartHeight: '20rem',
  showAction: false,
  loading: false,
  decimals: 0,
  separator: ','
})

/**组件事件定义*/
const emit = defineEmits<{
  /**操作按钮点击*/
  action: []
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**区域点击*/
  regionClick: [region: RegionClickData]
}>()

/**图标样式*/
const iconStyle = computed(() => ({
  color: props.iconColor,
  backgroundColor: props.iconBgColor
}))

/**处理操作按钮点击*/
const handleAction = () => {
  emit('action')
}

/**处理图表准备就绪*/
const handleChartReady = (chart: any) => {
  emit('chartReady', chart)
}

/**处理区域点击*/
const handleRegionClick = (region: RegionClickData) => {
  emit('regionClick', region)
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露方法*/
defineExpose({
  /**当前数据项数量*/
  dataCount: computed(() => props.mapData.length)
})
</script>

<style lang="scss" scoped>
.map-chart-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);
  transition: transform 0.2s ease;

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    padding: 20px 20px 12px;
    border-bottom: 1px solid transparent;

    .header-left {
      display: flex;
      gap: 12px;
      align-items: flex-start;
      flex: 1;
      min-width: 0;
    }

    .header-icon {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: var(--radius-md);
    }

    .header-content {
      flex: 1;
      min-width: 0;
    }

    .card-title {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      line-height: 1.4;
    }

    .card-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--el-text-color-secondary);
      line-height: 1.4;
    }

    .header-right {
      flex-shrink: 0;
    }

    .metric-display {
      text-align: right;

      :deep(.el-statistic) {
        .el-statistic__content {
          font-size: 24px;
          font-weight: 600;
          color: var(--el-text-color-primary);
          line-height: 1.2;
        }

        .el-statistic__suffix {
          margin-left: 4px;
        }
      }

      .metric-unit {
        font-size: 14px;
        font-weight: 400;
        color: var(--el-text-color-regular);
      }

      .metric-description {
        display: block;
        margin-top: 4px;
        font-size: 12px;
        color: var(--el-text-color-regular);
      }
    }

    .header-action {
      flex-shrink: 0;
      margin-left: 8px;

      :deep(.el-button) {
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .card-body {
    padding: 12px 20px 20px;
  }
}
</style>
