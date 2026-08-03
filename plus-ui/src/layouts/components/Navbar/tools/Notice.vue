<!-- 消息通知 -->
<template>
  <!-- 通知消息提示框 -->
  <el-tooltip :content="t('navbar.message')" effect="dark" placement="bottom">
    <div class="flex-center h-full px-1">
      <!-- 通知弹窗容器 -->
      <el-popover
        placement="bottom"
        trigger="click"
        transition="el-zoom-in-top"
        :width="380"
        :persistent="false"
        popper-class="notice-popover"
        @show="handlePopoverShow"
      >
        <!-- 触发器：带徽章的消息图标 -->
        <template #reference>
          <el-badge class="cursor-pointer" :value="unreadCount > 0 ? unreadCount : ''" :max="99" :offset="[-5, 5]">
            <div class="navbar-tool-item flex-center w-9 h-9 rounded-2 cursor-pointer">
              <Icon code="message" size="md" class="text-[18px]" animate="shake" />
            </div>
          </el-badge>
        </template>

        <!-- 弹窗内容：通知列表 -->
        <template #default>
          <div v-loading="isLoading" class="notice-container">
            <!-- 通知头部 -->
            <div class="notice-header">
              <div class="notice-title">{{ t('navbar.notice.title') }}</div>
              <div class="notice-actions">
                <el-button v-if="unreadCount > 0" type="primary" text size="small" @click="handleMarkAllAsRead"> {{ t('navbar.notice.markAllAsRead') }} </el-button>
              </div>
            </div>

            <!-- 标签栏（预留扩展） -->
            <ul class="notice-tabs">
              <li class="active">{{ t('navbar.notice.tabs.notice') }} ({{ unreadCount }})</li>
              <!-- 预留：后续可添加更多标签页 -->
              <!-- <li>消息 (0)</li> -->
              <!-- <li>待办 (0)</li> -->
            </ul>

            <!-- 通知内容区域 -->
            <div class="notice-content">
              <!-- 有消息时显示消息列表 -->
              <template v-if="noticeList.length > 0">
                <div
                  v-for="notice in noticeList"
                  :key="notice.noticeId"
                  class="notice-item"
                  :class="{ 'notice-item--unread': !notice.isRead }"
                  @click="handleNoticeClick(notice)"
                >
                  <!-- 图标 -->
                  <div class="notice-icon" :style="getNoticeTypeStyle(notice.noticeType)">
                    <Icon :code="getNoticeTypeIcon(notice.noticeType)" />
                  </div>

                  <!-- 消息内容 -->
                  <div class="notice-item-content">
                    <div class="notice-item-title">
                      <span class="title-text">{{ notice.noticeTitle }}</span>
                      <el-tag v-if="!notice.isRead" type="danger" size="small" class="unread-badge">{{ t('navbar.notice.unread') }}</el-tag>
                    </div>
                    <div class="notice-item-meta">
                      <span class="notice-item-author">{{ notice.createByName }}</span>
                      <span class="notice-item-time">{{ notice.createTime }}</span>
                    </div>
                  </div>
                </div>
              </template>

              <!-- 无消息时显示空状态 -->
              <div v-else class="empty-tips">
                <Icon code="notification-off" class="empty-icon" />
                <p>{{ t('navbar.notice.empty') }}</p>
              </div>
            </div>

            <!-- 底部操作区 -->
            <div class="notice-footer">
              <!-- 分页器 -->
              <Pagination
                v-if="total > 0"
                size="small"
                :background="false"
                layout="total, prev, pager, next"
                v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize"
                :total="total"
                @pagination="loadNoticeList"
              />
            </div>
          </div>
        </template>
      </el-popover>
    </div>
  </el-tooltip>

  <!-- 通知详情弹窗 -->
  <AModal
    v-model="detailDialogVisible"
    :title="currentNotice?.noticeTitle"
    mode="dialog"
    size="large"
    footer-type="close-only"
    :cancel-text="t('navbar.notice.close')"
    class="notice-detail-dialog"
  >
    <div v-if="currentNotice" class="notice-detail">
      <!-- 通知信息 -->
      <div class="notice-detail-header">
        <div class="notice-detail-meta">
          <DictTag :value="currentNotice.noticeType" :options="sys_notice_type"></DictTag>
          <span class="notice-detail-author">{{ t('navbar.notice.publisher') }}：{{ currentNotice.createByName }}</span>
          <span class="notice-detail-time">{{ t('navbar.notice.publishTime') }}：{{ currentNotice.createTime }}</span>
        </div>
      </div>

      <!-- 通知内容 - 固定高度 + 滚动 -->
      <div class="notice-detail-content-wrapper">
        <el-scrollbar max-height="500px">
          <div class="notice-detail-content" v-html="DOMPurify.sanitize(currentNotice.noticeContent)"></div>
        </el-scrollbar>
      </div>
    </div>
  </AModal>
</template>

<script setup lang="ts" name="Notice">
import DOMPurify from 'dompurify'
import { pageUserNotices, markNoticeAsRead, markAllNoticesAsRead, getUserNoticeDetail } from '@/api/system/config/notice/noticeApi'
import { type UserNoticeVo } from '@/api/system/config/notice/noticeTypes'
import { showMsgSuccess, showMsgError } from '@/utils/modal'
const { sys_notice_type } = useDict(DictTypes.sys_notice_type)

const { t } = useI18n()

// 使用通知 Store
const noticeStore = useNoticeStore()

// ========== 响应式数据 ==========
const isLoading = ref(false)
const unreadCount = computed(() => noticeStore.unreadCount) // 使用 Store 的未读数量
const noticeList = ref<UserNoticeVo[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentNotice = ref<UserNoticeVo | null>(null)

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 5
})

// ========== 生命周期 ==========
onMounted(() => {
  // 初始化时刷新未读数量
  noticeStore.refreshUnreadCount()
})

// ========== 方法 ==========

/**
 * 加载通知列表
 */
const loadNoticeList = async () => {
  isLoading.value = true
  const [err, data] = await pageUserNotices(queryParams.value)
  if (!err) {
    noticeList.value = data.records || []
    total.value = data.total || 0
  } else {
    console.error('获取通知列表失败:', err)
  }
  isLoading.value = false
}

/**
 * 弹窗显示时的处理
 */
const handlePopoverShow = () => {
  // 重置分页
  queryParams.value.pageNum = 1
  // 并行加载列表和未读数量
  Promise.all([loadNoticeList(), noticeStore.refreshUnreadCount()])
}

/**
 * 点击通知项
 */
const handleNoticeClick = async (notice: UserNoticeVo) => {
  // 未读通知：并行获取详情 + 标记已读（不等待标记已读完成）
  if (!notice.isRead) {
    const [detailPromise, markPromise] = [getUserNoticeDetail(notice.noticeId), markNoticeAsRead(notice.noticeId)]
    const [err, data] = await detailPromise
    if (!err) {
      currentNotice.value = data
      currentNotice.value.isRead = true
      detailDialogVisible.value = true
      notice.isRead = true
      noticeStore.decrementUnreadCount()
    } else {
      showMsgError(t('navbar.notice.getDetailFailed'))
    }
    await markPromise
  } else {
    // 已读通知：只获取详情
    const [err, data] = await getUserNoticeDetail(notice.noticeId)
    if (!err) {
      currentNotice.value = data
      detailDialogVisible.value = true
    } else {
      showMsgError(t('navbar.notice.getDetailFailed'))
    }
  }
}

/**
 * 标记所有通知为已读
 */
const handleMarkAllAsRead = async () => {
  const [err] = await markAllNoticesAsRead()
  if (!err) {
    // 更新本地状态
    noticeList.value.forEach((notice) => {
      notice.isRead = true
    })
    // 本地清零，无需发 API
    noticeStore.clearUnreadCount()
    showMsgSuccess(t('navbar.notice.markedAsRead'))
  }
}

/**
 * 根据通知类型获取图标
 */
const getNoticeTypeIcon = (noticeType: string): string => {
  const iconMap: Record<string, string> = {
    '1': 'notification', // 通知
    '2': 'announcement', // 公告
    '3': 'message-info', // 消息
    'notice': 'notification',
    'announcement': 'announcement',
    'message': 'message-info'
  }
  return iconMap[noticeType] || 'notification'
}

/**
 * 根据通知类型获取样式
 */
const getNoticeTypeStyle = (noticeType: string) => {
  const styleMap: Record<string, { backgroundColor: string; color: string }> = {
    '1': {
      // 通知 - 蓝色系
      backgroundColor: 'rgba(64, 158, 255, 0.1)',
      color: 'var(--el-color-primary)'
    },
    '2': {
      // 公告 - 橙色系
      backgroundColor: 'rgba(230, 162, 60, 0.1)',
      color: 'var(--el-color-warning)'
    },
    '3': {
      // 消息 - 绿色系
      backgroundColor: 'rgba(103, 194, 58, 0.1)',
      color: 'var(--el-color-success)'
    }
  }
  return styleMap[noticeType] || styleMap['1']
}
</script>

<style lang="scss" scoped>
// ==================== 弹窗圆角 ====================
:global(.notice-popover.el-popper) {
  border-radius: 8px;
  padding: 0 !important;
  overflow: hidden;
}

// ==================== 容器布局 ====================
.notice-container {
  width: 100%;
  height: 520px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// ==================== 通知头部 ====================
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px 12px;
  margin-top: 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .notice-title {
    font-size: 16px;
    font-weight: 500;
    color: var(--app-text);
  }

  .notice-actions {
    :deep(.el-button) {
      font-size: 12px;
    }
  }
}

// ==================== 标签栏 ====================
.notice-tabs {
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  list-style: none;
  margin: 0;

  li {
    height: 48px;
    line-height: 48px;
    padding: 0 12px;
    margin-right: 20px;
    font-size: 13px;
    color: var(--el-text-color-regular);
    cursor: pointer;
    user-select: none;
    transition: all 0.3s;

    &:hover {
      color: var(--app-text);
    }

    &.active {
      color: var(--el-color-primary);
      border-bottom: 2px solid var(--el-color-primary);
      font-weight: 500;
    }
  }
}

// ==================== 通知内容区域 ====================
.notice-content {
  flex: 1;
  overflow: hidden;
}

// ==================== 通知列表项 ====================
.notice-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 0;
  margin: 0;

  &:hover {
    background-color: var(--bg-level-2);
  }

  &.notice-item--unread {
    background-color: var(--bg-level-1);

    &:hover {
      background-color: var(--bg-level-3);
    }
  }

  // 图标容器
  .notice-icon {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    font-size: 18px;
    margin-right: 12px;
    transition: all 0.2s;
  }

  // 内容区域
  .notice-item-content {
    flex: 1;
    min-width: 0;
  }

  .notice-item-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
    gap: 8px;

    .title-text {
      flex: 1;
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
      color: var(--app-text);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .unread-badge {
      flex-shrink: 0;
    }
  }

  .notice-item-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);

    .notice-item-author {
      color: var(--el-text-color-regular);
    }

    .notice-item-time {
      color: var(--el-text-color-secondary);
    }
  }
}

// ==================== 空状态 ====================
.empty-tips {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--el-text-color-secondary);

  .empty-icon {
    font-size: 60px;
    color: var(--el-text-color-placeholder);
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

// ==================== 底部操作区 ====================
.notice-footer {
  padding: 12px 15px 15px;
  border-top: 1px solid var(--el-border-color-lighter);

  // 分页器样式优化
  :deep(.pagination-container) {
    padding: 0;
    margin: 0;
    justify-content: center;

    .el-pagination {
      font-weight: normal;

      .el-pager li {
        min-width: 28px;
        height: 28px;
        line-height: 28px;
        font-size: 12px;
      }

      button {
        height: 28px;
        line-height: 28px;
      }
    }
  }
}

/* 通知详情弹窗样式 */
:deep(.notice-detail-dialog) {
  .el-dialog__body {
    padding: 15px 20px;
  }
}

.notice-detail-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.notice-detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.notice-detail-author,
.notice-detail-time {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.notice-detail-content-wrapper {
  height: 500px;
}

.notice-detail-content {
  padding: 16px;
  line-height: 1.6;
  color: var(--el-text-color-primary);

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
  }

  :deep(p) {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4),
  :deep(h5),
  :deep(h6) {
    margin: 16px 0 8px 0;
    font-weight: 600;
    color: var(--el-text-color-primary);

    &:first-child {
      margin-top: 0;
    }
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 20px;
    margin-bottom: 12px;
  }

  :deep(blockquote) {
    border-left: 4px solid var(--el-color-primary);
    margin: 16px 0;
    padding: 12px 16px;
    background-color: var(--el-color-info-light-9);
    border-radius: 4px;
    color: var(--el-text-color-regular);
  }

  :deep(code) {
    background-color: var(--el-color-info-light-9);
    padding: 2px 6px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
    color: var(--el-color-danger);
  }

  :deep(pre) {
    background-color: var(--el-color-info-light-9);
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 12px 0;

    code {
      background: none;
      padding: 0;
      color: inherit;
    }
  }

  :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 12px 0;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    overflow: hidden;
  }

  :deep(th),
  :deep(td) {
    border: 1px solid var(--el-border-color);
    padding: 8px 12px;
    text-align: left;
  }

  :deep(th) {
    background-color: var(--el-color-info-light-9);
    font-weight: 600;
  }
}

// ==================== 暗色主题适配 ====================
.dark {
  .notice-container {
    ::-webkit-scrollbar-track {
      background: transparent;
    }

    ::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  .notice-item {
    &:hover {
      background-color: rgba(255, 255, 255, 0.05);
    }

    &.notice-item--unread {
      background-color: rgba(64, 158, 255, 0.05);

      &:hover {
        background-color: rgba(64, 158, 255, 0.1);
      }
    }
  }
}

// ==================== 响应式设计 ====================
@media (max-width: 768px) {
  .notice-container {
    height: 450px;
  }

  .notice-item {
    padding: 10px 12px;

    .notice-icon {
      width: 32px;
      height: 32px;
      font-size: 16px;
      margin-right: 10px;
    }
  }

  .notice-item-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .notice-item-title {
    .title-text {
      font-size: 13px;
    }
  }

  .notice-detail-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .notice-tabs li {
    font-size: 12px;
    padding: 0 8px;
    margin-right: 12px;
  }
}
</style>
