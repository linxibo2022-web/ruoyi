<!--
柱状图统计卡片组件 ABarStatsCard

使用示例：

1. 基本用法 - 用户增长统计
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <ABarStatsCard
      title="用户增长"
      description="比上周 +23%"
      :chart-data="[160, 100, 150, 80, 190, 100, 175, 120, 160]"
      :x-axis-data="['1', '2', '3', '4', '5', '6', '7', '8', '9']"
      :stats="[
        { label: '今日新增', value: '1.2k' },
        { label: '昨日新增', value: '985' },
        { label: '本周新增', value: '8.5k' },
        { label: '本月新增', value: '32k' }
      ]"
    />
  </el-col>
</el-row>

2. 订单统计
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <ABarStatsCard
      title="订单统计"
      description="环比增长 +15.8%"
      :chart-data="[120, 180, 150, 200, 160, 190, 210]"
      :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
      :stats="[
        { label: '今日订单', value: '210' },
        { label: '本周订单', value: '1,210' },
        { label: '本月订单', value: '5,680' },
        { label: '完成率', value: '95%' }
      ]"
      bar-color="#67c23a"
    />
  </el-col>
</el-row>

3. 自定义样式
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <ABarStatsCard
      title="销售额"
      description="同比上升 +28%"
      :chart-data="[200, 250, 180, 300, 260, 280, 320]"
      :x-axis-data="['1月', '2月', '3月', '4月', '5月', '6月', '7月']"
      :stats="[
        { label: '今日', value: '¥12.8k', clickable: true },
        { label: '本周', value: '¥85.6k', clickable: true },
        { label: '本月', value: '¥356k', clickable: true },
        { label: '增长', value: '+28%', clickable: false }
      ]"
      bar-color="#f56c6c"
      bar-width="60%"
      chart-height="14rem"
      @stat-click="handleStatClick"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="bar-stats-card">
    <!-- 图表区域 -->
    <div class="chart-container">
      <ABarChart
        :data="chartData"
        :x-axis-data="xAxisData"
        :bar-width="barWidth"
        :border-radius="borderRadius"
        :colors="colors"
        :height="chartHeight"
        :show-axis-line="showAxisLine"
        :show-split-line="showSplitLine"
        :show-axis-label="showAxisLabel"
        :show-tooltip="showTooltip"
        :show-legend="showLegend"
        :y-axis-min="yAxisMin"
        :y-axis-max="yAxisMax"
        :y-axis-interval="yAxisInterval"
        :y-axis-split-number="yAxisSplitNumber"
        :y-axis-min-interval="yAxisMinInterval"
      />
    </div>

    <!-- 文字内容区 -->
    <div class="content-section">
      <h3 class="card-title">{{ title }}</h3>
      <p v-if="description" class="description" v-html="highlightText(description)"></p>
    </div>

    <!-- 统计数据列表 -->
    <div class="stats-list">
      <div
        v-for="(stat, index) in stats"
        :key="index"
        class="stat-item"
        :class="{ 'is-clickable': isStatClickable(stat) }"
        @click="handleStatItemClick(stat, index)"
      >
        <p class="stat-value">{{ stat.value }}</p>
        <p class="stat-label">{{ stat.label }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="ABarStatsCard">
/**统计项接口*/
export interface StatItem {
  /**标签*/
  label: string
  /**数值*/
  value: string | number
  /**是否可点击（优先级高于全局设置）*/
  clickable?: boolean
}

/**柱状图统计卡片属性接口*/
interface ABarStatsCardProps {
  /**卡片标题*/
  title: string
  /**描述文本（支持HTML高亮）*/
  description?: string
  /**图表数据*/
  chartData: number[]
  /**X轴数据*/
  xAxisData: string[]
  /**统计数据列表*/
  stats: StatItem[]
  /**主题色彩配置*/
  colors?: string[]
  /**柱宽度*/
  barWidth?: string | number
  /**柱子圆角*/
  borderRadius?: number
  /**图表高度*/
  chartHeight?: string
  /**是否显示坐标轴标签*/
  showAxisLabel?: boolean
  /**是否显示坐标轴线*/
  showAxisLine?: boolean
  /**是否显示分割线*/
  showSplitLine?: boolean
  /**是否显示提示框*/
  showTooltip?: boolean
  /**是否显示图例*/
  showLegend?: boolean
  /**Y轴最小值*/
  yAxisMin?: number | string
  /**Y轴最大值*/
  yAxisMax?: number | string
  /**Y轴步进值*/
  yAxisInterval?: number
  /**Y轴分割段数*/
  yAxisSplitNumber?: number
  /**Y轴最小间隔*/
  yAxisMinInterval?: number
  /**全局控制统计项是否可点击（当单个统计项未设置 clickable 时生效）*/
  statsClickable?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<ABarStatsCardProps>(), {
  description: '',
  barWidth: '40%',
  borderRadius: 4,
  chartHeight: '13.7rem',
  showAxisLabel: true,
  showAxisLine: false,
  showSplitLine: true,
  showTooltip: true,
  showLegend: false,
  yAxisMin: undefined,
  yAxisMax: undefined,
  yAxisInterval: undefined,
  yAxisSplitNumber: 5,
  yAxisMinInterval: undefined,
  statsClickable: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**统计项点击事件*/
  statClick: [stat: StatItem, index: number]
}>()

/**
 * 判断统计项是否可点击
 * 优先级：stat.clickable > statsClickable
 */
const isStatClickable = (stat: StatItem): boolean => {
  // 如果单个统计项明确设置了 clickable，以它为准
  if (stat.clickable !== undefined) {
    return stat.clickable
  }
  // 否则使用全局设置
  return props.statsClickable
}

/**
 * 处理统计项点击
 */
const handleStatItemClick = (stat: StatItem, index: number) => {
  if (isStatClickable(stat)) {
    emit('statClick', stat, index)
  }
}

/**
 * 高亮显示描述文本中的特殊内容
 * 例如：比上周 +23% -> 比上周 <span class="highlight-success">+23%</span>
 */
const highlightText = (text: string): string => {
  // 匹配 +数字% 或 -数字%
  return text.replace(/([+\-]\d+\.?\d*%)/g, (match) => {
    const isPositive = match.startsWith('+')
    const className = isPositive ? 'highlight-success' : 'highlight-danger'
    return `<span class="${className}">${match}</span>`
  })
}

/**暴露方法*/
defineExpose({
  /**标题*/
  title: computed(() => props.title)
})
</script>

<style lang="scss" scoped>
.bar-stats-card {
  width: 100%;
  height: 400px;
  padding: 16px;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);
  box-sizing: border-box;

  .chart-container {
    width: 100%;
    height: 220px;
    padding: 10px;
    border-radius: calc(var(--radius-md) - 4px);
    box-sizing: border-box;
  }

  .content-section {
    margin: 20px 0 0 3px;

    .card-title {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .description {
      margin: 5px 0 0;
      font-size: 14px;
      color: var(--el-text-color-regular);

      :deep(.highlight-success) {
        color: var(--el-color-success);
        font-weight: 500;
      }

      :deep(.highlight-danger) {
        color: var(--el-color-danger);
        font-weight: 500;
      }
    }

  }

  .stats-list {
    display: flex;
    justify-content: space-between;
    margin-left: 3px;

    .stat-item {
      flex: 1;
      padding: 8px;
      border-radius: var(--radius-sm);
      transition: background-color 0.2s ease;

      // 可点击状态
      &.is-clickable {
        cursor: pointer;
      }

      .stat-value {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .stat-label {
        margin: 4px 0 0;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// 响应式适配
@media screen and (max-width: 768px) {
  .bar-stats-card {
    height: auto;
    min-height: 420px;

    .chart-container {
      padding: 15px 0 0;
    }

    .stats-list {
      flex-wrap: wrap;

      .stat-item {
        flex: 0 0 50%;
        margin-bottom: 12px;
      }
    }
  }
}
</style>
