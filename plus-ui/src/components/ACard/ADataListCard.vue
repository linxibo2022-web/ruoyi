<!--
数据列表卡片组件 ADataListCard

使用示例：

1. 基本用法
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ADataListCard
      title="待办事项"
      :list="[
        { title: '完成项目报告', status: '进行中', time: '2小时前', icon: 'file' },
        { title: '客户会议', status: '待处理', time: '今天 14:00', icon: 'calendar' },
        { title: '代码审查', status: '已完成', time: '昨天 16:30', icon: 'code' }
      ]"
    />
  </el-col>
</el-row>

2. 带图标颜色
<el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8">
    <ADataListCard
      title="最近活动"
      :list="[
        { title: '新用户注册', status: '成功', time: '5分钟前', icon: 'user', iconColor: '#67c23a' },
        { title: '订单支付', status: '完成', time: '10分钟前', icon: 'money', iconColor: '#409eff' },
        { title: '系统更新', status: '进行中', time: '30分钟前', icon: 'setting', iconColor: '#e6a23c' }
      ]"
    />
  </el-col>
</el-row>

3. 带副标题和查看更多
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ADataListCard
      title="任务列表"
      subtitle="今日待处理"
      :list="[
        { title: '完成需求文档', status: '待处理', time: '今天 09:00', icon: 'document' },
        { title: '产品设计评审', status: '进行中', time: '今天 10:30', icon: 'design' },
        { title: '技术方案讨论', status: '待处理', time: '今天 14:00', icon: 'chat' },
        { title: '代码开发', status: '进行中', time: '今天 15:30', icon: 'code' },
        { title: '单元测试', status: '待处理', time: '今天 17:00', icon: 'test' },
        { title: '部署上线', status: '待处理', time: '明天 09:00', icon: 'deploy' }
      ]"
      :max-count="5"
      :show-more-button="true"
    />
  </el-col>
</el-row>

4. 两列响应式布局
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ADataListCard
      title="待办事项"
      :list="[
        { title: '完成项目报告', status: '进行中', time: '2小时前', icon: 'file' },
        { title: '客户会议', status: '待处理', time: '今天 14:00', icon: 'calendar' }
      ]"
    />
  </el-col>
  <el-col :xs="24" :md="12">
    <ADataListCard
      title="已完成"
      :list="[
        { title: '代码审查', status: '已完成', time: '昨天 16:30', icon: 'code' },
        { title: '需求分析', status: '已完成', time: '昨天 10:00', icon: 'analysis' }
      ]"
    />
  </el-col>
</el-row>

5. 带操作按钮
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ADataListCard
      title="通知消息"
      :list="[
        { title: '系统更新通知', status: '未读', time: '1小时前', icon: 'notification' },
        { title: '新评论提醒', status: '未读', time: '3小时前', icon: 'comment' },
        { title: '订单消息', status: '已读', time: '5小时前', icon: 'order' }
      ]"
      :show-actions="true"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="data-list-card">
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

    <!-- 列表内容 -->
    <div v-if="displayList.length > 0" class="card-body">
      <div
        v-for="(item, index) in displayList"
        :key="index"
        class="list-item"
        :class="{ 'is-clickable': itemClickable }"
        @click="handleItemClick(item, index)"
      >
        <!-- 图标 -->
        <div v-if="item.icon" class="item-icon" :style="getIconStyle(item)">
          <Icon :code="item.icon" :size="20" />
        </div>

        <!-- 内容 -->
        <div class="item-content">
          <div class="item-header">
            <span class="item-title">{{ item.title }}</span>
            <el-tag v-if="item.status" :type="getStatusType(item.status)" size="small">
              {{ item.status }}
            </el-tag>
          </div>
          <p v-if="item.description" class="item-description">{{ item.description }}</p>
          <span v-if="item.time" class="item-time">{{ item.time }}</span>
        </div>

        <!-- 自定义操作 -->
        <div v-if="showActions || $slots.actions" class="item-actions">
          <slot name="actions" :item="item" :index="index">
            <el-button link type="primary" size="small" @click.stop="handleAction(item, 'view')"> {{ t('card.dataList.view') }} </el-button>
          </slot>
        </div>
      </div>

      <!-- 查看更多按钮 -->
      <div v-if="showMoreButton && hasMore" class="card-footer">
        <el-button text @click="handleMore">
          {{ t('card.dataList.viewMore') }}
          <Icon code="right2" :size="14" />
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="card-empty">
      <slot name="empty">
        <Icon code="empty" :size="48" />
        <span class="empty-text">{{ t('card.dataList.noData') }}</span>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts" name="ADataListCard">
const { t } = useI18n()

/**列表项接口*/
interface ListItem {
  /**标题*/
  title: string
  /**状态*/
  status?: string
  /**时间*/
  time?: string
  /**描述*/
  description?: string
  /**图标*/
  icon?: IconCode
  /**图标颜色*/
  iconColor?: string
  /**图标背景色*/
  iconBgColor?: string
  /**自定义数据*/
  [key: string]: any
}

/**数据列表卡片属性接口*/
interface ADataListCardProps {
  /**标题*/
  title: string
  /**副标题*/
  subtitle?: string
  /**列表数据*/
  list: ListItem[]
  /**最大显示数量*/
  maxCount?: number
  /**是否显示更多按钮*/
  showMoreButton?: boolean
  /**是否显示头部操作*/
  showHeaderAction?: boolean
  /**是否显示操作列*/
  showActions?: boolean
  /**列表项是否可点击*/
  itemClickable?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<ADataListCardProps>(), {
  subtitle: '',
  maxCount: 0,
  showMoreButton: false,
  showHeaderAction: false,
  showActions: false,
  itemClickable: true
})

/**组件事件定义*/
const emit = defineEmits<{
  /**查看更多*/
  more: []
  /**列表项点击*/
  itemClick: [item: ListItem, index: number]
  /**操作按钮点击*/
  action: [item: ListItem, actionType: string]
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

/**获取图标样式*/
const getIconStyle = (item: ListItem) => {
  return {
    color: item.iconColor || 'var(--el-color-primary)',
    backgroundColor: item.iconBgColor || 'var(--el-color-primary-light-9)'
  }
}

/**获取状态标签类型*/
const getStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' | '' => {
  const statusMap: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    '成功': 'success',
    '完成': 'success',
    '已完成': 'success',
    '进行中': 'warning',
    '处理中': 'warning',
    '待处理': 'info',
    '失败': 'danger',
    '错误': 'danger',
    '已取消': 'info'
  }
  return statusMap[status] || ''
}

/**处理查看更多*/
const handleMore = () => {
  emit('more')
}

/**处理列表项点击*/
const handleItemClick = (item: ListItem, index: number) => {
  if (props.itemClickable) {
    emit('itemClick', item, index)
  }
}

/**处理操作按钮*/
const handleAction = (item: ListItem, actionType: string) => {
  emit('action', item, actionType)
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
.data-list-card {
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
    padding: 0;
  }

  .list-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px 20px;
    transition: background-color 0.2s ease;

    &.is-clickable {
      cursor: pointer;

      &:hover {
        background-color: var(--el-fill-color-light);
      }
    }

    .item-icon {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: var(--radius-md);
    }

    .item-content {
      flex: 1;
      min-width: 0;
    }

    .item-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }

    .item-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      line-height: 1.4;
    }

    .item-description {
      margin: 4px 0;
      font-size: 13px;
      color: var(--el-text-color-regular);
      line-height: 1.5;
    }

    .item-time {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .item-actions {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      gap: 8px;
      margin-left: 12px;
    }
  }

  .card-footer {
    display: flex;
    justify-content: center;
    padding: 12px 20px;

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

// 深色模式样式
.dark {
  .data-list-card {
    .list-item {
      .item-icon {
        background-color: #232323 !important;
      }
    }
  }
}
</style>
