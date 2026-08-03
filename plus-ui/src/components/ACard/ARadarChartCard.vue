<!--
雷达图卡片组件 ARadarChartCard

使用示例：

1. 基本用法 - 单数据系列
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ARadarChartCard
      title="能力评估"
      :indicator="[
        { name: '销售', max: 100 },
        { name: '管理', max: 100 },
        { name: '技术', max: 100 },
        { name: '客服', max: 100 },
        { name: '研发', max: 100 }
      ]"
      :data="[
        { name: '当前', value: [80, 90, 85, 75, 95] }
      ]"
    />
  </el-col>
</el-row>

2. 多数据系列对比
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ARadarChartCard
      title="团队绩效对比"
      :indicator="[
        { name: '销售', max: 100 },
        { name: '管理', max: 100 },
        { name: '技术', max: 100 },
        { name: '客服', max: 100 },
        { name: '研发', max: 100 },
        { name: '创新', max: 100 }
      ]"
      :data="[
        { name: '张三', value: [80, 90, 85, 75, 95, 88] },
        { name: '李四', value: [70, 85, 90, 80, 85, 92] }
      ]"
      :show-legend="true"
    />
  </el-col>
</el-row>

3. 两列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ARadarChartCard
      title="团队A绩效"
      :indicator="[
        { name: '效率', max: 100 },
        { name: '质量', max: 100 },
        { name: '创新', max: 100 },
        { name: '协作', max: 100 }
      ]"
      :data="[
        { name: '本季度', value: [85, 90, 78, 92] }
      ]"
    />
  </el-col>
  <el-col :xs="24" :md="12">
    <ARadarChartCard
      title="团队B绩效"
      :indicator="[
        { name: '效率', max: 100 },
        { name: '质量', max: 100 },
        { name: '创新', max: 100 },
        { name: '协作', max: 100 }
      ]"
      :data="[
        { name: '本季度', value: [88, 82, 90, 85] }
      ]"
    />
  </el-col>
</el-row>

4. 完整功能组合
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ARadarChartCard
      title="员工绩效分析"
      subtitle="2024年第一季度"
      icon="radar"
      icon-color="#5470c6"
      :indicator="[
        { name: '工作态度', max: 100 },
        { name: '专业技能', max: 100 },
        { name: '团队协作', max: 100 },
        { name: '创新能力', max: 100 },
        { name: '执行力', max: 100 }
      ]"
      :data="[
        { name: '张三', value: [85, 92, 88, 78, 90] },
        { name: '李四', value: [78, 85, 92, 88, 85] }
      ]"
      :show-legend="true"
      legend-position="bottom"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="radar-chart-card">
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

      <!-- 操作按钮 -->
      <div v-if="showAction" class="header-action">
        <el-button text @click="handleAction">
          <Icon code="more" :size="16" />
        </el-button>
      </div>
    </div>

    <!-- 图表内容 -->
    <div class="card-body" :style="{ height: chartHeight }">
      <ARadarChart
        :indicator="indicator"
        :data="data"
        :colors="colors"
        :show-legend="showLegend"
        :legend-position="legendPosition"
        :show-tooltip="showTooltip"
        :loading="loading"
        :height="chartHeight"
        @chart-ready="handleChartReady"
        @series-click="handleSeriesClick"
      >
        <template v-if="$slots.empty" #empty>
          <slot name="empty"></slot>
        </template>
        <template v-if="$slots.loading" #loading>
          <slot name="loading"></slot>
        </template>
      </ARadarChart>
    </div>
  </div>
</template>

<script setup lang="ts" name="ARadarChartCard">
import type { RadarIndicator, RadarDataItem } from '@/components/AChart/ARadarChart.vue'
import { triggerChartResize } from '@/utils/function'

/**雷达图卡片属性接口*/
interface ARadarChartCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**图标代码*/
  icon?: IconCode
  /**图标颜色*/
  iconColor?: string
  /**图标背景色*/
  iconBgColor?: string
  /**雷达图指标配置*/
  indicator: RadarIndicator[]
  /**数据*/
  data: RadarDataItem[]
  /**图表高度*/
  chartHeight?: string
  /**是否显示操作按钮*/
  showAction?: boolean
  /**是否加载中*/
  loading?: boolean
  /**是否显示图例*/
  showLegend?: boolean
  /**图例位置*/
  legendPosition?: 'top' | 'bottom' | 'left' | 'right'
  /**是否显示提示框*/
  showTooltip?: boolean
  /**主题色彩配置*/
  colors?: string[]
}

/**组件属性定义*/
const props = withDefaults(defineProps<ARadarChartCardProps>(), {
  subtitle: '',
  iconColor: 'var(--el-color-primary)',
  iconBgColor: 'var(--el-color-primary-light-9)',
  chartHeight: '16rem',
  showAction: false,
  loading: false,
  showLegend: false,
  legendPosition: 'bottom',
  showTooltip: true
})

/**组件事件定义*/
const emit = defineEmits<{
  /**操作按钮点击*/
  action: []
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**数据系列点击*/
  seriesClick: [
    params: {
      name: string
      value: number[]
      indicator: RadarIndicator
      indicatorIndex: number
    }
  ]
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

/**处理系列点击*/
const handleSeriesClick = (params: { name: string; value: number[]; indicator: RadarIndicator; indicatorIndex: number }) => {
  emit('seriesClick', params)
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露方法*/
defineExpose({
  /**指标数量*/
  indicatorCount: computed(() => props.indicator.length),
  /**数据系列数量*/
  seriesCount: computed(() => props.data.length)
})
</script>

<style lang="scss" scoped>
.radar-chart-card {
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
