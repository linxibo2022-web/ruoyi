<!--
通知卡片组件 ANotificationCard

使用示例:

1. 基本用法
<el-row :gutter="20">
  <el-col :span="24">
    <ANotificationCard
      title="通知消息"
      :notifications="[
        { id: 1, title: '新消息', content: '您有一条新消息待查看', time: Date.now() - 300000, read: false },
        { id: 2, title: '系统通知', content: '系统将于今晚23:00维护', time: Date.now() - 3600000, read: true }
      ]"
    />
  </el-col>
</el-row>

2. 带类型图标
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ANotificationCard
      title="消息中心"
      :notifications="[
        { id: 1, title: '订单支付成功', content: '订单#12345已成功支付', time: Date.now() - 10000, type: 'success', read: false },
        { id: 2, title: '系统警告', content: '磁盘空间不足,请及时清理', time: Date.now() - 600000, type: 'warning', read: false },
        { id: 3, title: '操作失败', content: '文件上传失败,请重试', time: Date.now() - 1800000, type: 'error', read: true }
      ]"
      show-type-icon
    />
  </el-col>
</el-row>

3. 带操作按钮
<el-row :gutter="20">
  <el-col :span="24">
    <ANotificationCard
      title="待处理通知"
      :notifications="[
        { id: 1, title: '审批请求', content: '您有一个待审批的请求', time: Date.now() - 120000, read: false },
        { id: 2, title: '任务分配', content: '您被分配了一个新任务', time: Date.now() - 900000, read: false }
      ]"
      :max-count="5"
      show-actions
      :show-more-button="true"
    />
  </el-col>
</el-row>

4. 显示未读数量
<el-row :gutter="20">
  <el-col :xs="24" :md="12">
    <ANotificationCard
      title="系统消息"
      :notifications="[
        { id: 1, title: '版本更新', content: '发现新版本v2.0', time: Date.now() - 86400000, type: 'info', read: false },
        { id: 2, title: '安全提醒', content: '检测到异常登录', time: Date.now() - 172800000, type: 'warning', read: false },
        { id: 3, title: '维护通知', content: '系统已完成维护', time: Date.now() - 259200000, type: 'success', read: true }
      ]"
      show-unread-count
      show-type-icon
    />
  </el-col>
</el-row>

5. 完整功能展示
<el-row :gutter="20">
  <el-col :span="24">
    <ANotificationCard
      title="全部通知"
      :notifications="[
        { id: 1, title: '订单提醒', content: '您有新的订单需要处理', time: Date.now() - 5000, type: 'info', read: false },
        { id: 2, title: '支付成功', content: '订单支付成功,等待发货', time: Date.now() - 300000, type: 'success', read: false },
        { id: 3, title: '库存告警', content: '商品库存不足', time: Date.now() - 3600000, type: 'warning', read: true },
        { id: 4, title: '系统错误', content: '操作失败,请联系管理员', time: Date.now() - 7200000, type: 'error', read: true }
      ]"
      :max-count="3"
      show-unread-count
      show-type-icon
      show-actions
      :show-more-button="true"
      max-height="400px"
    />
  </el-col>
</el-row>
-->
<template>
  <div class="notification-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-content">
        <h3 class="card-title">{{ title }}</h3>
        <el-badge v-if="showUnreadCount && unreadCount > 0" :value="unreadCount" class="unread-badge" />
      </div>
      <div class="header-action">
        <slot name="header-action">
          <el-button v-if="unreadCount > 0" text size="small" @click="handleReadAll"> {{ t('card.notification.markAllRead') }} </el-button>
        </slot>
      </div>
    </div>

    <!-- 通知列表 -->
    <div v-if="!loading && displayNotifications.length > 0" class="card-body" :style="{ maxHeight: props.maxHeight }">
      <div
        v-for="notification in displayNotifications"
        :key="notification.id"
        class="notification-item"
        :class="{ 'is-unread': !notification.read, 'is-clickable': itemClickable }"
        role="article"
        :aria-label="`${notification.title}, ${notification.read ? '已读' : '未读'}`"
        tabindex="0"
        @click="handleItemClick(notification)"
        @keydown.enter="handleItemClick(notification)"
      >
        <!-- 类型图标 -->
        <div v-if="showTypeIcon" class="item-icon" :class="`type-${notification.type || 'info'}`">
          <Icon :code="getTypeIcon(notification.type)" :size="20" />
        </div>

        <!-- 未读标识 -->
        <div v-if="!notification.read && !showTypeIcon" class="unread-indicator">
          <div class="unread-dot"></div>
        </div>

        <!-- 内容区域 -->
        <div class="item-content">
          <div class="item-header">
            <h4 class="item-title">{{ notification.title }}</h4>
            <span class="item-time">{{ formatTime(notification.time) }}</span>
          </div>
          <p v-if="notification.content" class="item-description">{{ notification.content }}</p>

          <!-- 附加信息 -->
          <div v-if="notification.extra" class="item-extra">
            <slot name="extra" :notification="notification">
              <span>{{ notification.extra }}</span>
            </slot>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div v-if="showActions || $slots.actions" class="item-actions">
          <slot name="actions" :notification="notification">
            <el-button v-if="!notification.read" text size="small" @click.stop="handleRead(notification)"> {{ t('card.notification.markRead') }} </el-button>
            <el-button text size="small" type="danger" @click.stop="handleDelete(notification)"> {{ t('card.notification.delete') }} </el-button>
          </slot>
        </div>
      </div>

      <!-- 查看更多 -->
      <div v-if="showMoreButton && hasMore" class="card-footer">
        <el-button text @click="handleMore">
          {{ t('card.notification.viewAll') }}
          <Icon code="right" :size="14" />
        </el-button>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-else-if="loading" class="card-loading">
      <Icon code="loading" :size="32" />
      <span class="loading-text">{{ t('card.notification.loading') }}</span>
    </div>

    <!-- 空状态 -->
    <div v-else class="card-empty">
      <Icon code="empty" :size="48" />
      <span class="empty-text">{{ t('card.notification.noData') }}</span>
    </div>
  </div>
</template>

<script setup lang="ts" name="ANotificationCard">
const { t } = useI18n()

/**通知项接口*/
interface NotificationItem {
  /**唯一标识*/
  id: number | string
  /**标题*/
  title: string
  /**内容*/
  content?: string
  /**时间 - 支持字符串、Date对象或时间戳*/
  time: string | Date | number
  /**是否已读*/
  read: boolean
  /**类型*/
  type?: 'info' | 'success' | 'warning' | 'error'
  /**附加信息*/
  extra?: string
  /**自定义数据*/
  [key: string]: any
}

/**通知卡片属性接口*/
interface ANotificationCardProps {
  /**标题*/
  title: string
  /**通知列表*/
  notifications: NotificationItem[]
  /**最大显示数量*/
  maxCount?: number
  /**是否显示更多按钮*/
  showMoreButton?: boolean
  /**是否显示未读数量*/
  showUnreadCount?: boolean
  /**是否显示类型图标*/
  showTypeIcon?: boolean
  /**是否显示操作按钮*/
  showActions?: boolean
  /**列表项是否可点击*/
  itemClickable?: boolean
  /**是否按日期分组*/
  groupByDate?: boolean
  /**最大高度*/
  maxHeight?: string
  /**是否加载中*/
  loading?: boolean
}

/**组件属性定义*/
const props = withDefaults(defineProps<ANotificationCardProps>(), {
  maxCount: 0,
  showMoreButton: false,
  showUnreadCount: true,
  showTypeIcon: false,
  showActions: false,
  itemClickable: true,
  groupByDate: false,
  maxHeight: '500px',
  loading: false
})

/**组件事件定义*/
const emit = defineEmits<{
  /**查看更多*/
  more: []
  /**标记已读*/
  read: [notification: NotificationItem]
  /**全部已读*/
  readAll: []
  /**删除通知*/
  delete: [notification: NotificationItem]
  /**通知项点击*/
  itemClick: [notification: NotificationItem]
}>()

/**显示的通知列表*/
const displayNotifications = computed(() => {
  if (props.maxCount > 0 && props.notifications.length > props.maxCount) {
    return props.notifications.slice(0, props.maxCount)
  }
  return props.notifications
})

/**是否有更多*/
const hasMore = computed(() => {
  return props.maxCount > 0 && props.notifications.length > props.maxCount
})

/**未读数量*/
const unreadCount = computed(() => {
  return props.notifications.filter((n) => !n.read).length
})

/**类型图标映射*/
const typeIconMap: Record<string, IconCode> = {
  'info': 'info',
  'success': 'success',
  'warning': 'warning',
  'error': 'error'
}

/**获取类型图标*/
const getTypeIcon = (type?: string): IconCode => {
  return typeIconMap[type || 'info'] || 'notification'
}

/**格式化相对时间*/
const formatTime = (time: string | Date | number): string => {
  // 如果已经是字符串格式的相对时间,直接返回
  if (typeof time === 'string' && /前|刚刚|ago|just/i.test(time)) {
    return time
  }

  let timestamp: number

  // 处理不同类型的时间输入
  if (time instanceof Date) {
    timestamp = time.getTime()
  } else if (typeof time === 'number') {
    // 如果是10位时间戳(秒),转为毫秒
    timestamp = time.toString().length === 10 ? time * 1000 : time
  } else {
    // 字符串类型,尝试解析
    const parsed = new Date(time)
    if (isNaN(parsed.getTime())) {
      return time // 解析失败,返回原值
    }
    timestamp = parsed.getTime()
  }

  const now = Date.now()
  const diff = Math.floor((now - timestamp) / 1000) // 转为秒

  if (diff < 30) {
    return t('card.notification.time.justNow')
  } else if (diff < 60) {
    return t('card.notification.time.secondsAgo', { n: diff })
  } else if (diff < 3600) {
    return t('card.notification.time.minutesAgo', { n: Math.floor(diff / 60) })
  } else if (diff < 86400) {
    return t('card.notification.time.hoursAgo', { n: Math.floor(diff / 3600) })
  } else if (diff < 86400 * 2) {
    return t('card.notification.time.daysAgo', { n: 1 })
  } else if (diff < 86400 * 7) {
    return t('card.notification.time.daysAgo', { n: Math.floor(diff / 86400) })
  } else {
    // 超过7天显示具体日期
    const date = new Date(timestamp)
    const month = date.getMonth() + 1
    const day = date.getDate()
    return t('card.notification.time.monthDay', { month, day })
  }
}

/**处理查看更多*/
const handleMore = () => {
  emit('more')
}

/**处理标记已读*/
const handleRead = (notification: NotificationItem) => {
  emit('read', notification)
}

/**处理全部已读*/
const handleReadAll = () => {
  emit('readAll')
}

/**处理删除*/
const handleDelete = (notification: NotificationItem) => {
  emit('delete', notification)
}

/**处理通知项点击*/
const handleItemClick = (notification: NotificationItem) => {
  if (props.itemClickable) {
    emit('itemClick', notification)
  }
}

/**暴露方法*/
defineExpose({
  /**未读数量*/
  unreadCount,
  /**总数量*/
  totalCount: computed(() => props.notifications.length)
})
</script>

<style lang="scss" scoped>
.notification-card {
  background-color: var(--bg-level-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--el-border-color);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px;

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
    }

    .card-title {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .unread-badge {
      :deep(.el-badge__content) {
        transform: translateY(-50%) translateX(50%);
      }
    }

    .header-action {
      margin-left: 16px;
    }
  }

  .card-body {
    padding: 0;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: var(--el-border-color);
      border-radius: 3px;

      &:hover {
        background-color: var(--el-border-color-dark);
      }
    }

    &::-webkit-scrollbar-track {
      background-color: transparent;
    }
  }

  .notification-item {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px 20px;
    transition: background-color 0.2s ease;

    &.is-unread {
      background-color: var(--el-color-primary-light-9);

      .item-title {
        font-weight: 600;
      }
    }

    &.is-clickable {
      cursor: pointer;

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      &:focus-visible {
        outline: 2px solid var(--el-color-primary);
        outline-offset: -2px;
      }
    }

    .unread-indicator {
      flex-shrink: 0;
      display: flex;
      align-items: flex-start;
      padding-top: 6px;

      .unread-dot {
        width: 8px;
        height: 8px;
        background-color: var(--el-color-primary);
        border-radius: 50%;
        box-shadow: 0 0 0 3px var(--el-color-primary-light-8);
        animation: pulse 2s ease-in-out infinite;
      }
    }

    @keyframes pulse {
      0%,
      100% {
        box-shadow: 0 0 0 3px var(--el-color-primary-light-8);
      }
      50% {
        box-shadow: 0 0 0 5px var(--el-color-primary-light-9);
      }
    }

    .item-icon {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 50%;

      &.type-info {
        color: var(--el-color-info);
        background-color: var(--el-color-info-light-9);
      }

      &.type-success {
        color: var(--el-color-success);
        background-color: var(--el-color-success-light-9);
      }

      &.type-warning {
        color: var(--el-color-warning);
        background-color: var(--el-color-warning-light-9);
      }

      &.type-error {
        color: var(--el-color-danger);
        background-color: var(--el-color-danger-light-9);
      }
    }

    .item-content {
      flex: 1;
      min-width: 0;
    }

    .item-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 4px;
    }

    .item-title {
      margin: 0;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      line-height: 1.4;
    }

    .item-time {
      flex-shrink: 0;
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      white-space: nowrap;
    }

    .item-description {
      margin: 0 0 8px;
      font-size: 13px;
      color: var(--el-text-color-regular);
      line-height: 1.5;
    }

    .item-extra {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .item-actions {
      display: flex;
      flex-shrink: 0;
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
    }
  }

  .card-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: var(--el-text-color-secondary);

    .loading-text {
      margin-top: 12px;
      font-size: 14px;
    }

    :deep(.icon) {
      animation: rotate 1s linear infinite;
    }

    @keyframes rotate {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
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
  .notification-card {
    .notification-item {
      &.is-unread {
        background-color: rgba(255, 255, 255, 0.05);
      }
    }
  }
}
</style>
