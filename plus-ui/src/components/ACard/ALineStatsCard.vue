<!--
折线图统计卡片组件 ALineStatsCard

使用示例：

1. 基本用法 - 访问量趋势
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <ALineStatsCard
      title="访问量趋势"
      description="较上周 +18.5%"
      subtitle="访问量持续增长，用户粘性提升"
      :chart-data="[120, 150, 180, 160, 200, 190, 220, 240, 260]"
      :x-axis-data="['1', '2', '3', '4', '5', '6', '7', '8', '9']"
      :stats="[
        { label: '今日访问', value: '8.2k' },
        { label: '昨日访问', value: '7.5k' },
        { label: '本周访问', value: '52k' },
        { label: '月访问', value: '185k' }
      ]"
    />
  </el-col>
</el-row>

2. 销售趋势
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <ALineStatsCard
      title="销售趋势"
      description="环比增长 +25.3%"
      :chart-data="[1200, 1450, 1380, 1620, 1580, 1720, 1850]"
      :x-axis-data="['周一', '周二', '周三', '周四', '周五', '周六', '周日']"
      :stats="[
        { label: '今日销售', value: '¥18.5k' },
        { label: '本周销售', value: '¥115k' },
        { label: '本月销售', value: '¥456k' },
        { label: '增长率', value: '+25%' }
      ]"
      line-color="#67c23a"
      :show-area-color="true"
    />
  </el-col>
</el-row>

3. 完整功能
<el-row :gutter="20">
  <el-col :xs="24" :md="12" :lg="8">
    <ALineStatsCard
      title="活跃用户"
      description="同比上升 +32%"
      subtitle="用户活跃度显著提升，留存率创新高"
      :chart-data="[80, 95, 110, 105, 125, 140, 135, 150, 165]"
      :x-axis-data="['1', '2', '3', '4', '5', '6', '7', '8', '9']"
      :stats="[
        { label: '今日活跃', value: '1.65k' },
        { label: '日均活跃', value: '1.28k' },
        { label: '周活跃', value: '5.8k' },
        { label: '留存率', value: '78%' }
      ]"
      line-color="#409eff"
      :line-width="3"
      :smooth="true"
      :show-area-color="true"
      chart-height="14rem"
      stats-clickable
      @stat-click="handleStatClick"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="line-stats-card">
    <!-- 图表区域 -->
    <div class="chart-container">
      <ALineChart
        :data="chartData"
        :x-axis-data="xAxisData"
        :line-width="lineWidth"
        :smooth="smooth"
        :symbol="symbol"
        :symbol-size="symbolSize"
        :show-area-color="showAreaColor"
        :colors="colors"
        :height="chartHeight"
        :show-axis-line="showAxisLine"
        :show-split-line="showSplitLine"
        :show-axis-label="showAxisLabel"
        :show-tooltip="showTooltip"
        :show-legend="showLegend"
        :animation-delay="animationDelay"
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
      <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
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

<script setup lang="ts" name="ALineStatsCard">
/**统计项接口*/
interface StatItem {
  /**标签*/
  label: string
  /**数值*/
  value: string | number
  /**是否可点击（优先级高于全局设置）*/
  clickable?: boolean
}

/**折线图统计卡片属性接口*/
interface ALineStatsCardProps {
  /**卡片标题*/
  title: string
  /**描述文本（支持HTML高亮）*/
  description?: string
  /**副标题*/
  subtitle?: string
  /**图表数据*/
  chartData: number[]
  /**X轴数据*/
  xAxisData: string[]
  /**统计数据列表*/
  stats: StatItem[]
  /**主题色彩配置*/
  colors?: string[]
  /**线条宽度*/
  lineWidth?: number
  /**是否平滑曲线*/
  smooth?: boolean
  /**标记点样式*/
  symbol?: string
  /**标记点大小*/
  symbolSize?: number
  /**是否显示区域填充*/
  showAreaColor?: boolean
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
  /**Y轴最小间隔*/
  yAxisMinInterval?: number
  /**全局控制统计项是否可点击（当单个统计项未设置 clickable 时生效）*/
  statsClickable?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<ALineStatsCardProps>(), {
  description: '',
  subtitle: '',
  lineWidth: 2.5,
  smooth: true,
  symbol: 'none',
  symbolSize: 6,
  showAreaColor: false,
  chartHeight: '13.7rem',
  showAxisLabel: true,
  showAxisLine: false,
  showSplitLine: true,
  showTooltip: true,
  showLegend: false,
  animationDelay: 200,
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
 * 例如：较上周 +18.5% -> 较上周 <span class="highlight-success">+18.5%</span>
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
.line-stats-card {
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

    .subtitle {
      height: 42px;
      margin: 5px 0 0;
      font-size: 14px;
      color: var(--el-text-color-secondary);
      line-height: 1.5;
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
  .line-stats-card {
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
