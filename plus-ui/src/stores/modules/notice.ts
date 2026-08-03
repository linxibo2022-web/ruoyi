// 通知状态
import { getNoticeUnreadCount } from '@/api/system/config/notice/noticeApi'

/**
 * 通知管理中心 (useNoticeStore)
 *
 * 基于 Pinia 的通知管理模块，提供系统通知的集中存储和处理能力。
 *
 * 包含以下功能：
 * - 通知存储: 集中管理所有系统通知信息 (state.notices)
 * - 未读数量: 从后端API获取准确的未读数量 (unreadCount, refreshUnreadCount)
 * - 通知添加: 支持动态添加新的通知项 (addNotice)
 * - 通知移除: 支持移除指定的单条通知 (removeNotice)
 * - 批量已读: 一键将所有通知标记为已读状态 (readAll)
 * - 通知清空: 支持清空所有通知记录 (clearNotice)
 */

/**
 * 应用模块名称
 */
const NOTICE_MODULE = 'notice'

/**
 * 通知项接口
 * @property title 通知标题
 * @property read 是否已读
 * @property message 通知内容
 * @property time 通知时间
 */
interface NoticeItem {
  id?: string
  title?: string
  read: boolean
  message: any
  time: string
}

/**
 * 通知状态管理
 * @description 管理系统通知的存储、添加、移除等操作
 * @example
 * // 在组件中使用
 * import { useNoticeStore } from '@/stores/notice';
 */
export const useNoticeStore = defineStore(NOTICE_MODULE, () => {
  /**
   * 通知状态
   * @property notices 通知列表
   */
  const notices = ref<NoticeItem[]>([])

  /**
   * 未读通知数量（从后端API获取）
   */
  const unreadCount = ref(0)

  /**
   * 刷新未读数量
   * @description 从后端API获取最新的未读通知数量
   */
  const refreshUnreadCount = async (): Promise<void> => {
    const [err, data] = await getNoticeUnreadCount()
    if (!err) {
      unreadCount.value = data || 0
    } else {
      console.error('获取未读数量失败:', err)
    }
  }

  /**
   * 本地递减未读数量（避免不必要的 API 请求）
   */
  const decrementUnreadCount = (count = 1): void => {
    unreadCount.value = Math.max(0, unreadCount.value - count)
  }

  /**
   * 本地设置未读数量为 0
   */
  const clearUnreadCount = (): void => {
    unreadCount.value = 0
  }

  /**
   * 添加通知
   * @param notice 要添加的通知项
   * @example 添加一条通知
   * noticeStore.addNotice({
   *   title: '系统通知',
   *   read: false,
   *   message: '您有一条新消息',
   *   time: '2023-05-20 12:30:00'
   * });
   */
  const addNotice = (notice: NoticeItem): void => {
    notices.value.push(notice)
    // 本地递增未读数量，无需发 API 请求
    if (!notice.read) {
      unreadCount.value++
    }
  }

  /**
   * 移除通知
   * @param notice 要移除的通知项
   * @example 移除指定通知 noticeStore.removeNotice(noticeItem);
   */
  const removeNotice = (notice: NoticeItem): void => {
    const index = notices.value.indexOf(notice)
    if (index !== -1) {
      notices.value.splice(index, 1)
    }
  }

  /**
   * 将所有通知标记为已读
   * @example
   * 标记所有通知为已读 noticeStore.readAll();
   */
  const readAll = () => {
    notices.value.forEach((item: NoticeItem) => {
      item.read = true
    })
  }

  /**
   * 清空所有通知
   * @example
   * 清空所有通知 noticeStore.clearNotice();
   */
  const clearNotice = (): void => {
    notices.value = []
  }

  return {
    notices,
    unreadCount,
    refreshUnreadCount,
    decrementUnreadCount,
    clearUnreadCount,
    addNotice,
    removeNotice,
    readAll,
    clearNotice
  }
})
