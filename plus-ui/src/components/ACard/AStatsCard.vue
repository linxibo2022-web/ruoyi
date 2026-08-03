<!--
统计卡片组件 AStatsCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="总用户数" :value="8520" description="较昨日" icon="user" />
  </el-col>
</el-row>

2. 带趋势指标
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="活跃用户" :value="1234" icon="hot" :trend="{ value: 12.5, isUp: true }" />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="下降指标" :value="567" icon="fire" :trend="{ value: -8.3, isUp: false }" />
  </el-col>
</el-row>

3. 带对比数据
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="订单数量"
      :value="1234"
      icon="order"
      description="较上月"
      :trend="{ value: 15.2, isUp: true }"
    />
  </el-col>
</el-row>

4. 带进度条（目标达成）
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="月度目标"
      :value="85000"
      :target="100000"
      unit="元"
      icon="goal"
      :show-progress="true"
    />
  </el-col>
</el-row>

5. 完整功能组合
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="本月销售额"
      :value="125000"
      unit="元"
      description="较上月"
      :trend="{ value: 15.2, isUp: true }"
      :target="150000"
      :show-progress="true"
      icon="money"
      icon-color="#67c23a"
      icon-bg-color="rgba(103, 194, 58, 0.1)"
    />
  </el-col>
</el-row>

6. 数字滚动动画
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="今日订单"
      :value="567"
      icon="order"
      :show-animation="true"
      :animation-duration="2000"
    />
  </el-col>
</el-row>

7. 多列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="总用户数" :value="8520" icon="user" />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="活跃用户" :value="1234" icon="hot" :trend="{ value: 12.5, isUp: true }" />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="订单数量" :value="567" icon="order" :trend="{ value: 15.2, isUp: true }" />
  </el-col>
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard title="月度目标" :value="85000" :target="100000" unit="元" icon="goal" :show-progress="true" />
  </el-col>
</el-row>

8. 自定义图标颜色
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="6">
    <AStatsCard
      title="总收入"
      :value="98000"
      unit="元"
      icon="money"
      icon-color="#f56c6c"
      icon-bg-color="rgba(245, 108, 108, 0.1)"
    />
  </el-col>
</el-row>
-->
<template>
  <div
    class="stats-card"
    :class="{
      'is-clickable': clickable && !disabled,
      'is-disabled': disabled
    }"
    :style="cardStyle"
    @click="handleCardClick"
  >
    <!-- 图标区域 -->
    <div v-if="icon" class="stats-icon" :style="iconStyle">
      <Icon :code="icon" :size="iconSize" />
    </div>

    <!-- 内容区域 -->
    <div class="stats-content">
      <!-- 标题 -->
      <p class="stats-title">{{ title }}</p>

      <!-- 数值区域 -->
      <div class="stats-value-wrapper">
        <el-statistic :value="displayValue" :precision="decimals" :group-separator="separator">
          <template #suffix>
            <span v-if="unit" class="stats-unit">{{ unit }}</span>
          </template>
        </el-statistic>
      </div>

      <!-- 底部信息区 -->
      <div v-if="description || trend" class="stats-footer">
        <!-- 描述文本 -->
        <span v-if="description" class="stats-description">{{ description }}</span>

        <!-- 趋势标识 -->
        <div v-if="trend" class="stats-trend" :class="{ 'is-up': trend.isUp, 'is-down': !trend.isUp }">
          <i class="trend-icon">{{ trend.isUp ? '↑' : '↓' }}</i>
          <span class="trend-value">{{ formatTrendValue }}</span>
        </div>
      </div>

      <!-- 进度条 -->
      <div v-if="showProgress && target" class="stats-progress">
        <el-progress :percentage="progressPercentage" :stroke-width="4" :show-text="false" :color="progressColor" />
        <span class="progress-text">{{ progressPercentage }}% {{ t('card.stats.completed') }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div v-if="showAction" class="stats-action">
      <el-button text @click.stop="handleAction">
        <Icon :code="actionIcon" :color="actionIconColor" size="md" />
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts" name="AStatsCard">
import { useTransition, TransitionPresets } from '@vueuse/core'

const { t } = useI18n()

/**趋势数据接口*/
interface TrendData {
  /**数值*/
  value: number
  /**是否上升*/
  isUp: boolean
}

/**统计卡片属性接口*/
interface AStatsCardProps {
  /**标题*/
  title: string
  /**数值*/
  value: string | number
  /**单位*/
  unit?: string
  /**描述文本 - 配合 trend 使用时会显示 "描述 ↑15.2%"*/
  description?: string
  /**图标代码*/
  icon?: IconCode
  /**图标颜色*/
  iconColor?: string
  /**图标背景色*/
  iconBgColor?: string
  /**图标大小*/
  iconSize?: number
  /**卡片高度*/
  height?: string
  /**趋势数据 - 会自动格式化为 "↑15.2%" 或 "+15.2%"*/
  trend?: TrendData
  /**目标值 - 用于进度条计算*/
  target?: number
  /**是否显示进度条*/
  showProgress?: boolean
  /**进度条颜色*/
  progressColor?: string
  /**是否显示操作按钮*/
  showAction?: boolean
  /**操作按钮图标代码*/
  actionIcon?: IconCode
  /** 操作图标颜色*/
  actionIconColor?: string
  /**小数位数*/
  decimals?: number
  /**千分位分隔符*/
  separator?: string
  /**是否显示数字滚动动画*/
  showAnimation?: boolean
  /**动画持续时间(毫秒)*/
  animationDuration?: number
  /**背景颜色*/
  backgroundColor?: string
  /**图标对齐方式*/
  iconAlign?: 'start' | 'center'
  /**是否可点击*/
  clickable?: boolean
  /**是否禁用*/
  disabled?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<AStatsCardProps>(), {
  unit: '',
  description: '',
  iconColor: 'var(--el-color-primary)',
  iconBgColor: 'var(--el-color-primary-light-9)',
  iconSize: 24,
  height: '8rem',
  target: 0,
  showProgress: false,
  progressColor: 'var(--el-color-success)',
  showAction: false,
  actionIcon: 'more2',
  decimals: 0,
  separator: ',',
  showAnimation: false,
  animationDuration: 2000,
  backgroundColor: 'var(--bg-level-1)',
  iconAlign: 'start',
  clickable: false,
  disabled: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**卡片点击*/
  click: []
  /**操作按钮点击*/
  action: []
}>()

/**动画源数值*/
const sourceValue = ref(0)

/**使用 useTransition 实现数字滚动动画*/
const animatedValue = useTransition(sourceValue, {
  duration: props.animationDuration,
  transition: TransitionPresets.easeOutCubic
})

/**显示的数值：开启动画时使用动画值，否则直接显示原值*/
const displayValue = computed(() => {
  if (props.showAnimation) {
    return animatedValue.value
  }
  return Number(props.value)
})

/**监听 props.value 变化，更新动画源数值*/
watch(
  () => props.value,
  (newVal) => {
    sourceValue.value = Number(newVal)
  },
  { immediate: true }
)

/**卡片样式*/
const cardStyle = computed(() => ({
  height: props.height,
  backgroundColor: props.backgroundColor,
  alignItems: props.iconAlign === 'center' ? 'center' : 'flex-start'
}))

/**图标样式*/
const iconStyle = computed(() => ({
  color: props.iconColor,
  backgroundColor: props.iconBgColor
}))

/**
 * 格式化趋势值显示
 * 如果有 description，显示 "15.2%"
 * 如果没有 description，显示 "+15.2%" 或 "-15.2%"
 */
const formatTrendValue = computed(() => {
  if (!props.trend) return ''

  const value = props.trend.value

  // 如果有描述文本，只显示数字（箭头已经在前面了）
  if (props.description) {
    return `${value}%`
  }

  // 没有描述文本时，显示正负号
  const sign = props.trend.isUp ? '+' : '-'
  return `${sign}${Math.abs(value)}%`
})

/**进度百分比*/
const progressPercentage = computed(() => {
  if (!props.target || props.target === 0) return 0
  return Math.min(Math.round((Number(props.value) / props.target) * 100), 100)
})

/**处理卡片点击*/
const handleCardClick = () => {
  if (props.clickable && !props.disabled) {
    emit('click')
  }
}

/**处理操作按钮点击*/
const handleAction = () => {
  emit('action')
}

/**暴露方法*/
defineExpose({
  /**当前值*/
  currentValue: computed(() => props.value),
  /**进度百分比*/
  progressPercentage
})
</script>

<style lang="scss" scoped>
.stats-card {
  position: relative;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 20px;
  overflow: hidden;
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  transition: all 0.2s ease;
  border: 1px solid var(--el-border-color);

  // 可点击状态
  &.is-clickable {
    cursor: pointer;
    user-select: none;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    &:active {
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    }
  }

  // 禁用状态
  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.6;

    &:hover {
      box-shadow: none;
    }
  }

  .stats-icon {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    transition: transform 0.2s ease;
  }

  .stats-content {
    flex: 1;
    min-width: 0;
  }

  .stats-title {
    margin: 0 0 8px;
    font-size: 14px;
    font-weight: 400;
    color: var(--el-text-color-regular);
    line-height: 1.4;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
  }

  .stats-value-wrapper {
    margin-bottom: 8px;

    :deep(.el-statistic) {
      .el-statistic__content {
        font-size: 28px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        line-height: 1.2;
      }

      .el-statistic__suffix {
        margin-left: 4px;
      }
    }

    .stats-unit {
      font-size: 14px;
      font-weight: 400;
      color: var(--el-text-color-regular);
    }
  }

  .stats-footer {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: nowrap;
  }

  .stats-description {
    font-size: 12px;
    color: var(--el-text-color-regular);
    line-height: 1.4;
  }

  .stats-trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    font-weight: 500;
    transition: all 0.2s ease;

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

  .stats-progress {
    margin-top: 12px;

    .progress-text {
      display: block;
      margin-top: 4px;
      font-size: 12px;
      color: var(--el-text-color-regular);
    }
  }

  .stats-action {
    flex-shrink: 0;

    :deep(.el-button) {
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
}

// 深色模式样式
.dark {
  .stats-card {
    .stats-icon {
      background-color: #232323 !important;
    }
  }
}
</style>
