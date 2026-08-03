// SSE推送
import { useEventSource } from '@vueuse/core'
import { useToken } from '@/composables/useToken'
import { getCurrentTime } from '@/utils/date'
import { showNotifySuccess } from '@/utils/modal'

/**
 * SSE (Server-Sent Events) 实时消息推送钩子 (useSSE) - 动态退避策略
 *
 * 包含以下功能：
 * - 自动连接: 建立与服务器的SSE连接 (基于VueUse useEventSource)
 * - 动态退避重连: 连接断开后按指数退避策略自动重连 (自定义重连逻辑)
 * - 消息接收: 监听并接收服务器推送的实时消息 (watch data)
 * - 消息通知: 收到消息后通过Element Plus通知提醒 (showNotifySuccess)
 * - 未读数量更新: 收到新消息后自动更新未读数量 (getNoticeUnreadCount)
 * - 连接管理: 提供手动关闭和重连的方法 (close, reconnect)
 * - 状态监控: 实时监控连接状态变化 (status reactive)
 * - 错误处理: 监听并处理连接错误情况 (watch error)
 * - 资源清理: 组件卸载时自动清理连接和定时器 (onUnmounted)
 *
 * @param url SSE连接地址
 * @param options 配置选项 { maxRetries?: number, baseDelay?: number }
 * @returns { close, reconnect, status, unreadCount } 返回控制方法和状态
 */
export const useSSE = (url: string, options: { maxRetries?: number; baseDelay?: number } = {}) => {
  const featureStore = useFeatureStore()
  const noticeStore = useNoticeStore()

  if (!featureStore.features.sseEnabled) {
    return {
      close: () => {},
      reconnect: () => {},
      status: ref('disabled'),
      unreadCount: computed(() => noticeStore.unreadCount)
    }
  }

  const token = useToken()

  // 退避策略配置
  const maxRetries = options.maxRetries ?? 8 // 默认8次重试
  const baseDelay = options.baseDelay ?? 3 // 默认3秒基础延迟

  const finalUrl = `${url}?${token.getAuthQuery()}`

  // 记录重试状态
  let currentRetryCount = 0
  let retryTimeoutId: number | null = null
  let isManuallyClose = false
  let isReconnecting = false // 添加重连状态标记

  /**
   * 动态计算退避延迟时间
   * 规律：3 -> 6 -> 12 -> 24 -> 48 -> 96 -> 192 -> 384...
   * 公式：delay = baseDelay * (2^retryIndex)
   */
  const getRetryDelay = (retryIndex: number): number => {
    const delaySeconds = baseDelay * Math.pow(2, retryIndex)
    return delaySeconds * 1000
  }

  // 使用VueUse的useEventSource，禁用默认重连，使用自定义退避策略
  const { status, data, error, close, open, eventSource } = useEventSource(finalUrl, [], {
    autoReconnect: false, // 禁用默认重连，使用自定义退避策略
    immediate: true
  })

  /**
   * 自定义退避重连逻辑
   */
  const attemptReconnect = () => {
    // 防止重复重连
    if (isReconnecting) {
      return
    }

    if (currentRetryCount >= maxRetries) {
      console.log(`[${getCurrentTime()}] 🛑 SSE重试${maxRetries}次后连接失败，停止重试`)
      isReconnecting = false
      return
    }

    isReconnecting = true // 设置重连状态
    const delay = getRetryDelay(currentRetryCount)
    const delaySeconds = delay / 1000

    console.log(`🔄 SSE将在${delaySeconds}秒后进行第${currentRetryCount + 1}次重连...`)

    retryTimeoutId = window.setTimeout(() => {
      console.log(`[${getCurrentTime()}] 🚀 开始第${currentRetryCount + 1}次重连尝试...`)
      currentRetryCount++

      // 先关闭再打开，确保状态正确
      close()
      setTimeout(() => {
        isReconnecting = false // 在尝试重连前重置状态
        open()
      }, 100)
    }, delay)
  }

  // 监听连接状态变化
  watch(
    status,
    (newStatus, oldStatus) => {
      if (newStatus === 'OPEN' && oldStatus !== 'OPEN') {
        // 连接成功
        console.log(`[${getCurrentTime()}] ✅ SSE连接已建立`)
        currentRetryCount = 0 // 连接成功后重置重试计数
        isReconnecting = false // 重置重连状态

        // 清除重试定时器
        if (retryTimeoutId) {
          clearTimeout(retryTimeoutId)
          retryTimeoutId = null
        }

        // 连接成功后立即更新未读数量
        noticeStore.refreshUnreadCount()
      } else if (newStatus === 'CLOSED' && oldStatus !== 'CLOSED') {
        // 连接断开
        console.log(`[${getCurrentTime()}] ❌ SSE连接已断开`)

        // 如果不是手动关闭，则尝试重连
        if (!isManuallyClose) {
          // 重置重连状态，允许新的重连
          isReconnecting = false
          attemptReconnect()
        }
      }
    },
    { immediate: true }
  )

  // 监听错误变化
  watch(error, (newError) => {
    if (newError) {
      console.error('❌ SSE连接错误:', newError)

      // 如果连接失败且不是手动关闭，也尝试重连
      // 这里处理连接失败但状态没有变为 CLOSED 的情况
      if (!isManuallyClose && !isReconnecting) {
        attemptReconnect()
      }
    }
  })

  // 监听数据变化
  watch(data, async (newData) => {
    if (!newData) return

    console.log('📨 SSE收到消息:', newData)

    // 显示通知（延迟执行避免重叠）
    setTimeout(() => {
      showNotifySuccess({
        title: '新通知',
        message: newData,
        duration: 3000,
        position: 'top-right',
        offset: 50
      })
    }, 100)

    // 收到新消息后更新未读数量
    await noticeStore.refreshUnreadCount()

    // 清空数据
    data.value = null
  })

  /**
   * 手动重新连接
   */
  const reconnect = () => {
    console.log(`[${getCurrentTime()}] 🔧 手动重新连接SSE...`)

    // 清除现有的重试定时器
    if (retryTimeoutId) {
      clearTimeout(retryTimeoutId)
      retryTimeoutId = null
    }

    // 重置状态
    currentRetryCount = 0
    isManuallyClose = false
    isReconnecting = false // 重置重连状态

    // 先关闭再重新打开
    close()
    setTimeout(() => {
      open()
    }, 100)
  }

  /**
   * 增强的关闭方法
   */
  const enhancedClose = () => {
    console.log(`[${getCurrentTime()}] 🔒 手动关闭SSE连接`)
    isManuallyClose = true
    isReconnecting = false // 重置重连状态

    // 清除重试定时器
    if (retryTimeoutId) {
      clearTimeout(retryTimeoutId)
      retryTimeoutId = null
    }

    close()
  }

  // 组件卸载时清理资源
  onUnmounted(() => {
    enhancedClose()
  })

  // 返回控制方法和状态
  return {
    close: enhancedClose,
    reconnect,
    status: readonly(status),
    unreadCount: computed(() => noticeStore.unreadCount),
    eventSource: readonly(eventSource)
  }
}
