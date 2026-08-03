<!--
折线图卡片组件 ALineChartCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ALineChartCard
      title="用户增长趋势"
      :data="[120, 200, 150, 180, 220, 190, 240]"
      :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
    />
  </el-col>
</el-row>

2. 带指标显示
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ALineChartCard
      title="本月访问量"
      :value="8520"
      unit="次"
      :trend="{ value: 12.5, isUp: true }"
      description="较上月"
      :data="[800, 900, 850, 920, 880, 950, 1020]"
      :x-axis-data="['1周', '2周', '3周', '4周']"
      :show-area-color="true"
    />
  </el-col>
</el-row>

3. 多系列对比
<el-row :gutter="20">
  <el-col :span="24">
    <ALineChartCard
      title="流量对比"
      :data="[
        { name: '本月', data: [120, 200, 150, 180] },
        { name: '上月', data: [100, 180, 140, 160] }
      ]"
      :x-axis-data="['第1周', '第2周', '第3周', '第4周']"
      :show-legend="true"
    />
  </el-col>
</el-row>

4. 三列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ALineChartCard
      title="Q1访问量"
      :data="[150, 180, 200]"
      :x-axis-data="['1月', '2月', '3月']"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="8">
    <ALineChartCard
      title="Q2访问量"
      :data="[220, 190, 240]"
      :x-axis-data="['4月', '5月', '6月']"
    />
  </el-col>
  <el-col :xs="24" :sm="12" :md="8">
    <ALineChartCard
      title="Q3访问量"
      :data="[180, 200, 210]"
      :x-axis-data="['7月', '8月', '9月']"
    />
  </el-col>
</el-row>

5. 平滑曲线 + 区域填充
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ALineChartCard
      title="销售额趋势"
      :data="[2300, 2800, 2400, 3200, 3500, 3100, 3700]"
      :x-axis-data="['1月', '2月', '3月', '4月', '5月', '6月', '7月']"
      :smooth="true"
      :show-area-color="true"
      :line-width="3"
      symbol="circle"
      :symbol-size="6"
    />
  </el-col>
</el-row>

6. 完整功能组合
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ALineChartCard
      title="营收趋势"
      subtitle="最近30天"
      :value="450000"
      unit="元"
      icon="trend"
      icon-color="#91cc75"
      :trend="{ value: 18.5, isUp: true }"
      description="较上月"
      :data="[30000, 35000, 32000, 42000, 45000, 48000, 52000]"
      :x-axis-data="['第1周', '第2周', '第3周', '第4周']"
      :smooth="true"
      :show-area-color="true"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="line-chart-card">
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

      <!-- 右侧:指标显示 -->
      <div v-if="value !== undefined || trend" class="header-right">
        <div class="metric-display">
          <el-statistic :value="value" :precision="decimals" :group-separator="separator">
            <template #suffix>
              <span v-if="unit" class="metric-unit">{{ unit }}</span>
            </template>
          </el-statistic>

          <!-- 趋势和描述 -->
          <div v-if="description || trend" class="metric-footer">
            <span v-if="description" class="metric-description">{{ description }}</span>
            <div v-if="trend" class="metric-trend" :class="{ 'is-up': trend.isUp, 'is-down': !trend.isUp }">
              <i class="trend-icon">{{ trend.isUp ? '↑' : '↓' }}</i>
              <span class="trend-value">{{ trend.value }}%</span>
            </div>
          </div>
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
      <ALineChart
        :data="data"
        :x-axis-data="xAxisData"
        :line-width="lineWidth"
        :show-area-color="showAreaColor"
        :smooth="smooth"
        :symbol="symbol"
        :symbol-size="symbolSize"
        :colors="colors"
        :show-legend="showLegend"
        :legend-position="legendPosition"
        :show-axis-label="showAxisLabel"
        :show-axis-line="showAxisLine"
        :show-split-line="showSplitLine"
        :show-tooltip="showTooltip"
        :loading="loading"
        :height="chartHeight"
        :animation-delay="animationDelay"
        @chart-ready="handleChartReady"
        @point-click="handlePointClick"
      >
        <template v-if="$slots.empty" #empty>
          <slot name="empty"></slot>
        </template>
        <template v-if="$slots.loading" #loading>
          <slot name="loading"></slot>
        </template>
      </ALineChart>
    </div>
  </div>
</template>

<script setup lang="ts" name="ALineChartCard">
import type { LineDataItem } from '@/components/AChart/ALineChart.vue'
import { triggerChartResize } from '@/utils/function'

/**趋势数据接口*/
interface TrendData {
  /**数值*/
  value: number
  /**是否上升*/
  isUp: boolean
}

/**折线图卡片属性接口*/
interface ALineChartCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**指标数值*/
  value?: number
  /**单位*/
  unit?: string
  /**描述文本*/
  description?: string
  /**趋势数据*/
  trend?: TrendData
  /**图标代码*/
  icon?: IconCode
  /**图标颜色*/
  iconColor?: string
  /**图标背景色*/
  iconBgColor?: string
  /**折线图数据*/
  data: number[] | LineDataItem[]
  /**X轴数据*/
  xAxisData: string[]
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
  /**是否显示图例*/
  showLegend?: boolean
  /**图例位置*/
  legendPosition?: 'top' | 'bottom' | 'left' | 'right'
  /**是否显示坐标轴标签*/
  showAxisLabel?: boolean
  /**是否显示坐标轴线*/
  showAxisLine?: boolean
  /**是否显示分割线*/
  showSplitLine?: boolean
  /**是否显示提示框*/
  showTooltip?: boolean
  /**动画延迟*/
  animationDelay?: number
  /**主题色彩配置*/
  colors?: string[]
}

/**组件属性定义*/
const props = withDefaults(defineProps<ALineChartCardProps>(), {
  subtitle: '',
  unit: '',
  description: '',
  iconColor: 'var(--el-color-primary)',
  iconBgColor: 'var(--el-color-primary-light-9)',
  lineWidth: 2.5,
  showAreaColor: false,
  smooth: true,
  symbol: 'none',
  symbolSize: 6,
  chartHeight: '12rem',
  showAction: false,
  loading: false,
  decimals: 0,
  separator: ',',
  showLegend: false,
  legendPosition: 'bottom',
  showAxisLabel: true,
  showAxisLine: false,
  showSplitLine: true,
  showTooltip: true,
  animationDelay: 200
})

/**组件事件定义*/
const emit = defineEmits<{
  /**操作按钮点击*/
  action: []
  /**图表准备就绪*/
  chartReady: [chart: any]
  /**数据点点击*/
  pointClick: [params: { name: string; value: number; seriesName?: string }]
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

/**处理数据点点击*/
const handlePointClick = (params: { name: string; value: number; seriesName?: string }) => {
  emit('pointClick', params)
}

/**组件激活时 - 处理keep-alive缓存场景*/
onActivated(() => {
  triggerChartResize()
})

/**暴露方法*/
defineExpose({
  /**当前指标值*/
  currentValue: computed(() => props.value)
})
</script>

<style lang="scss" scoped>
.line-chart-card {
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
    min-height: 85px;

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
    }

    .metric-footer {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 8px;
      margin-top: 4px;
    }

    .metric-description {
      font-size: 12px;
      color: var(--el-text-color-regular);
    }

    .metric-trend {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      font-weight: 500;

      &.is-up {
        color: var(--el-color-success);
      }

      &.is-down {
        color: var(--el-color-danger);
      }

      .trend-icon {
        font-size: 14px;
        font-weight: bold;
        font-style: normal;
        line-height: 1;
      }

      .trend-value {
        line-height: 1;
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
