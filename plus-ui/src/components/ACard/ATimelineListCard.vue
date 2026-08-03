<!--
时间轴列表卡片组件 ATimelineListCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ATimelineListCard
      title="最近交易"
      :list="[
        { time: '上午 09:30', content: '收到支付 385.90 元', status: 'success' },
        { time: '上午 10:00', content: '新销售记录', code: 'ML-3467', status: 'info' },
        { time: '上午 11:15', content: '订单退款 128.00 元', status: 'warning' }
      ]"
    />
  </el-col>
</el-row>

2. 带副标题
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ATimelineListCard
      title="操作日志"
      subtitle="2024年3月20日"
      :list="[
        { time: '09:00', content: '用户登录系统', status: 'success' },
        { time: '10:30', content: '修改用户信息', status: 'info' },
        { time: '14:20', content: '删除过期数据', status: 'warning' },
        { time: '16:45', content: '导出报表文件', status: 'success' }
      ]"
    />
  </el-col>
</el-row>

3. 自定义状态颜色
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ATimelineListCard
      title="系统事件"
      :list="[
        { time: '14:30', content: '系统维护通知', status: '#f56c6c' },
        { time: '15:00', content: '维护完成', status: '#67c23a' },
        { time: '15:30', content: '服务恢复正常', status: '#409eff' }
      ]"
    />
  </el-col>
</el-row>

4. 两列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ATimelineListCard
      title="项目进度A"
      :list="[
        { time: '09:00', content: '需求评审完成', status: 'success' },
        { time: '10:30', content: '技术方案制定', status: 'info' },
        { time: '14:00', content: '开发中', status: 'warning' }
      ]"
    />
  </el-col>
  <el-col :xs="24" :md="12">
    <ATimelineListCard
      title="项目进度B"
      :list="[
        { time: '10:00', content: '设计稿完成', status: 'success' },
        { time: '11:30', content: '前端开发', status: 'info' },
        { time: '15:00', content: '联调测试', status: 'warning' }
      ]"
    />
  </el-col>
</el-row>

5. 带查看更多功能
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ATimelineListCard
      title="项目进度"
      subtitle="最近更新"
      :list="[
        { time: '08:00', content: '项目启动', status: 'success', code: 'PROJ-001' },
        { time: '09:30', content: '需求分析', status: 'success', code: 'PROJ-002' },
        { time: '11:00', content: '设计评审', status: 'success', code: 'PROJ-003' },
        { time: '14:00', content: '开发阶段', status: 'info', code: 'PROJ-004' },
        { time: '16:30', content: '测试阶段', status: 'warning', code: 'PROJ-005' },
        { time: '18:00', content: '待上线', status: 'info', code: 'PROJ-006' }
      ]"
      :max-count="5"
      :show-more-button="true"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="timeline-list-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-content">
        <h3 class="card-title">{{ title }}</h3>
        <p v-if="subtitle" class="card-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="showHeaderAction" class="header-action">
        <slot name="header-action">
          <el-button text @click="handleHeaderAction">
            <Icon code="more" :size="16" />
          </el-button>
        </slot>
      </div>
    </div>

    <!-- 时间轴内容 -->
    <div v-if="displayList.length > 0" class="card-body">
      <div class="timeline-container">
        <div
          v-for="(item, index) in displayList"
          :key="index"
          class="timeline-item"
          :class="{ 'is-clickable': itemClickable, 'is-last': index === displayList.length - 1 }"
          @click="handleItemClick(item, index)"
        >
          <!-- 时间轴节点 -->
          <div class="timeline-node" :style="{ backgroundColor: getStatusColor(item.status) }">
            <div class="node-dot"></div>
          </div>

          <!-- 时间轴线 -->
          <div v-if="index !== displayList.length - 1" class="timeline-line"></div>

          <!-- 内容区域 -->
          <div class="timeline-content">
            <div class="content-header">
              <span class="content-time">{{ item.time }}</span>
              <el-tag v-if="item.code" size="small" type="info">{{ item.code }}</el-tag>
            </div>
            <p class="content-text">{{ item.content }}</p>
            <p v-if="item.description" class="content-description">{{ item.description }}</p>
          </div>
        </div>
      </div>

      <!-- 查看更多按钮 -->
      <div v-if="showMoreButton && hasMore" class="card-footer">
        <el-button text @click="handleMore">
          {{ t('card.timeline.viewMore') }}
          <Icon code="right" :size="14" />
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="card-empty">
      <slot name="empty">
        <Icon code="empty" :size="48" />
        <span class="empty-text">{{ t('card.timeline.noData') }}</span>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts" name="ATimelineListCard">
const { t } = useI18n()

/**时间轴列表项接口*/
interface TimelineItem {
  /**时间*/
  time: string
  /**内容*/
  content: string
  /**状态颜色或状态名称*/
  status?: string
  /**代码/编号*/
  code?: string
  /**描述*/
  description?: string
  /**自定义数据*/
  [key: string]: any
}

/**时间轴列表卡片属性接口*/
interface ATimelineListCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**列表数据*/
  list: TimelineItem[]
  /**最大显示数量*/
  maxCount?: number
  /**是否显示更多按钮*/
  showMoreButton?: boolean
  /**是否显示头部操作*/
  showHeaderAction?: boolean
  /**列表项是否可点击*/
  itemClickable?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<ATimelineListCardProps>(), {
  subtitle: '',
  maxCount: 0,
  showMoreButton: false,
  showHeaderAction: false,
  itemClickable: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**查看更多*/
  more: []
  /**列表项点击*/
  itemClick: [item: TimelineItem, index: number]
  /**头部操作点击*/
  headerAction: []
}>()

/**显示的列表数据*/
const displayList = computed(() => {
  if (props.maxCount > 0 && props.list.length > props.maxCount) {
    return props.list.slice(0, props.maxCount)
  }
  return props.list
})

/**是否有更多数据*/
const hasMore = computed(() => {
  return props.maxCount > 0 && props.list.length > props.maxCount
})

/**获取状态颜色*/
const getStatusColor = (status?: string): string => {
  if (!status) return 'var(--el-color-primary)'

  // 如果是颜色值,直接返回
  if (status.startsWith('#') || status.startsWith('rgb')) {
    return status
  }

  // 预定义状态颜色映射
  const statusColorMap: Record<string, string> = {
    'success': 'var(--el-color-success)',
    'warning': 'var(--el-color-warning)',
    'danger': 'var(--el-color-danger)',
    'error': 'var(--el-color-danger)',
    'info': 'var(--el-color-info)',
    'primary': 'var(--el-color-primary)'
  }

  return statusColorMap[status.toLowerCase()] || 'var(--el-color-primary)'
}

/**处理查看更多*/
const handleMore = () => {
  emit('more')
}

/**处理列表项点击*/
const handleItemClick = (item: TimelineItem, index: number) => {
  if (props.itemClickable) {
    emit('itemClick', item, index)
  }
}

/**处理头部操作*/
const handleHeaderAction = () => {
  emit('headerAction')
}

/**暴露方法*/
defineExpose({
  /**显示的数据数量*/
  displayCount: computed(() => displayList.value.length),
  /**总数据数量*/
  totalCount: computed(() => props.list.length)
})
</script>

<style lang="scss" scoped>
.timeline-list-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 20px;

    .header-content {
      flex: 1;
    }

    .card-title {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .card-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .header-action {
      margin-left: 16px;

      :deep(.el-button) {
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .card-body {
    padding: 20px;
  }

  .timeline-container {
    position: relative;
  }

  .timeline-item {
    position: relative;
    display: flex;
    gap: 16px;
    padding-bottom: 20px;
    transition: background-color 0.2s ease;

    &.is-last {
      padding-bottom: 0;
    }

    &.is-clickable {
      cursor: pointer;

      &:hover .timeline-content {
        background-color: var(--el-fill-color-light);
      }
    }

    .timeline-node {
      position: relative;
      z-index: 2;
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 12px;
      height: 12px;
      margin-top: 6px;
      border-radius: 50%;

      .node-dot {
        width: 6px;
        height: 6px;
        background-color: #fff;
        border-radius: 50%;
      }
    }

    .timeline-line {
      position: absolute;
      top: 20px;
      left: 5.5px;
      width: 1px;
      height: calc(100% - 18px);
      background-color: var(--el-border-color);
    }

    .timeline-content {
      flex: 1;
      min-width: 0;
      padding: 8px 12px;
      border-radius: var(--radius-md);
      transition: background-color 0.2s ease;

      .content-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;
      }

      .content-time {
        font-size: 13px;
        font-weight: 500;
        color: var(--el-text-color-regular);
      }

      .content-text {
        margin: 0;
        font-size: 14px;
        color: var(--el-text-color-primary);
        line-height: 1.5;
      }

      .content-description {
        margin: 4px 0 0;
        font-size: 13px;
        color: var(--el-text-color-secondary);
        line-height: 1.5;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: center;
    padding: 12px 20px 20px;
    margin-top: 8px;

    :deep(.el-button) {
      color: var(--el-color-primary);

      &:hover {
        color: var(--el-color-primary-light-3);
      }
    }
  }

  .card-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: var(--el-text-color-placeholder);

    .empty-text {
      margin-top: 12px;
      font-size: 14px;
    }
  }
}
</style>
