<!--
饼图卡片组件 APieChartCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APieChartCard
      title="销售分布"
      :data="[
        { name: '直接访问', value: 335 },
        { name: '邮件营销', value: 310 },
        { name: '联盟广告', value: 234 },
        { name: '视频广告', value: 135 }
      ]"
    />
  </el-col>
</el-row>

2. 环形图（甜甜圈图）
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APieChartCard
      title="产品占比"
      :data="[
        { name: '产品A', value: 450 },
        { name: '产品B', value: 320 },
        { name: '产品C', value: 280 },
        { name: '产品D', value: 200 }
      ]"
      :radius="['40%', '70%']"
      center-text="总销售"
      :show-legend="true"
    />
  </el-col>
</el-row>

3. 带图标和图例
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APieChartCard
      title="用户来源"
      icon="pie-chart"
      icon-color="#fac858"
      :data="[
        { name: '搜索引擎', value: 520 },
        { name: '直接访问', value: 380 },
        { name: '社交媒体', value: 290 },
        { name: '推荐链接', value: 160 }
      ]"
      :show-legend="true"
      legend-position="right"
    />
  </el-col>
</el-row>

4. 三列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <APieChartCard
      title="Q1销售"
      :data="[
        { name: '北区', value: 150 },
        { name: '南区', value: 180 },
        { name: '东区', value: 200 }
      ]"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="8">
    <APieChartCard
      title="Q2销售"
      :data="[
        { name: '北区', value: 220 },
        { name: '南区', value: 190 },
        { name: '东区', value: 240 }
      ]"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="8">
    <APieChartCard
      title="Q3销售"
      :data="[
        { name: '北区', value: 180 },
        { name: '南区', value: 200 },
        { name: '东区', value: 210 }
      ]"
    />
  </el-col>
</el-row>

5. 完整功能组合
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <APieChartCard
      title="营收构成"
      subtitle="2024年第一季度"
      icon="chart-pie"
      :data="[
        { name: '产品销售', value: 4500 },
        { name: '技术服务', value: 3200 },
        { name: '咨询业务', value: 2800 },
        { name: '其他收入', value: 1500 }
      ]"
      :radius="['45%', '75%']"
      :show-legend="true"
      :show-label="false"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="pie-chart-card">
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
      <APieChart
        :data="data"
        :radius="radius"
        :border-radius="borderRadius"
        :center-text="centerText"
        :show-label="showLabel"
        :colors="colors"
        :show-legend="showLegend"
        :legend-position="legendPosition"
        :show-tooltip="showTooltip"
        :loading="loading"
        :height="chartHeight"
        @chart-ready="handleChartReady"
        @sector-click="handleSectorClick"
        @legend-click="handleLegendClick"
      >
        <template v-if="$slots.empty" #empty>
          <slot name="empty"></slot>
        </template>
        <template v-if="$slots.loading" #loading>
          <slot name="loading"></slot>
        </template>
      </APieChart>
    </div>
  </div>
</template>

<script setup lang="ts" name="APieChartCard">
import type { ChartDataItem } from '@/components/AChart/AChart.vue'
import { triggerChartResize } from '@/utils/function'

/**饼图卡片属性接口*/
interface APieChartCardProps {
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
  /**饼图数据*/
  data: ChartDataItem[]
  /**半径配置 [内半径, 外半径]*/
  radius?: [string, string]
  /**圆角大小*/
  borderRadius?: number
  /**中心文字*/
  centerText?: string
  /**是否显示标签*/
  showLabel?: boolean
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
const props = withDefaults(defineProps<APieChartCardProps>(), {
  subtitle: '',
  iconColor: 'var(--el-color-primary)',
  iconBgColor: 'var(--el-color-primary-light-9)',
  radius: () => ['50%', '80%'],
  borderRadius: 10,
  centerText: '',
  showLabel: false,
  chartHeight: '16rem',
  showAction: false,
  loading: false,
  showLegend: false,
  legendPosition: 'right',
  showTooltip: true
})

/**组件事件定义*/
const emit = defineEmits<{
  /**操作按钮点击*/
  action: []
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**扇区点击*/
  sectorClick: [params: { name: string; value: number; percent: number }]
  /**图例点击*/
  legendClick: [params: { name: string; selected: boolean }]
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

/**处理扇区点击*/
const handleSectorClick = (params: { name: string; value: number; percent: number }) => {
  emit('sectorClick', params)
}

/**处理图例点击*/
const handleLegendClick = (params: { name: string; selected: boolean }) => {
  emit('legendClick', params)
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露方法*/
defineExpose({
  /**数据项数量*/
  dataCount: computed(() => props.data.length)
})
</script>

<style lang="scss" scoped>
.pie-chart-card {
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
