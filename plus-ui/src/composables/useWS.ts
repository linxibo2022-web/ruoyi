// WebSocket通信
import { SystemConfig } from '@/systemConfig'
import { getCurrentTime } from '@/utils/date'
import { showNotifySuccess, showNotify } from '@/utils/modal'

// 消息类型定义

/**
 * WebSocket 消息类型枚举
 *
 * 定义了系统中所有可能的 WebSocket 消息类型，用于消息路由和处理
 */
export enum WSMessageType {
  // 系统级消息 - 需要显示通知和存储
  SYSTEM_NOTICE = 'system_notice', // 系统通知（包括通知和公告，统一右上角显示）

  // AI 聊天消息
  AI_CHAT_START = 'ai_chat_start', // 开始生成
  AI_CHAT_STREAM = 'ai_chat_stream', // 流式响应
  AI_CHAT_COMPLETE = 'ai_chat_complete', // 生成完成
  AI_CHAT_ERROR = 'ai_chat_error', // 生成错误

  // 业务消息 - 静默处理或特定显示
  CHAT_MESSAGE = 'chat_message', // 聊天消息（由聊天组件处理）

  // 开发工具消息
  DEV_LOG = 'devLog', // 开发日志（仅开发环境，超管可见）

  // 技术消息 - 系统内部使用
  HEARTBEAT = 'heartbeat'
}

/**
 * WebSocket 消息结构
 *
 * 标准化的消息格式，确保所有消息都有统一的结构
 */
export interface WSMessage {
  type: WSMessageType // 消息类型
  data: any // 消息数据
  timestamp: number // 时间戳
  id?: string // 消息ID（可选）
}

/**
 * 通知消息数据结构
 */
export interface NotificationData {
  title?: string // 通知标题
  content: string // 通知内容
  duration?: number // 显示时长
  type?: 'success' | 'info' | 'warning' | 'error' // 通知类型
}

/**
 * AI 聊天流式数据结构
 *
 * 后端通过 phase 区分两种增量：
 * - thinking: 深度思考 / 推理阶段，reasoningContent 有值
 * - content : 最终回复阶段，content 有值（不传 phase 时按 content 处理）
 */
export interface AiChatStreamData {
  sessionId: string
  messageId: string
  /** 最终回复增量（phase === 'content'） */
  content: string
  /** 推理 / 思考增量（phase === 'thinking'）；完成消息中为完整推理内容 */
  reasoningContent?: string
  /** 阶段标识：thinking | content */
  phase?: 'thinking' | 'content'
  finished: boolean
  tokenUsage?: {
    promptTokens: number
    completionTokens: number
    totalTokens: number
  }
  error?: string
  references?: any[]
}

/**
 * 聊天消息数据结构
 */
export interface ChatMessageData {
  fromUserId: string // 发送者ID
  fromUsername: string // 发送者用户名
  content: string // 消息内容
  chatRoomId?: string // 聊天室ID（群聊）
  messageType?: 'text' | 'image' | 'file' // 消息类型
}

// 消息处理器接口和实现

/**
 * 消息处理器接口
 *
 * 定义了消息处理器的标准接口，所有处理器都必须实现此接口
 */
export interface MessageHandler {
  /**
   * 处理消息
   * @param message 标准化的消息对象
   * @returns 是否继续传播消息到下一个处理器 (支持异步)
   */
  handle(message: WSMessage): boolean | Promise<boolean>
}

/**
 * 系统通知处理器
 *
 * 负责处理系统级别的通知消息，包括：
 * - 显示系统通知弹窗
 * - 存储通知到通知中心
 * - 根据通知类型决定显示样式
 */
export class SystemNoticeHandler implements MessageHandler {
  private noticeStore: any = null

  /**
   * 获取通知存储实例（延迟初始化）
   */
  private getNoticeStore() {
    if (!this.noticeStore) {
      this.noticeStore = useNoticeStore()
    }
    return this.noticeStore
  }

  handle(message: WSMessage): boolean {
    // 处理系统通知
    if (message.type === WSMessageType.SYSTEM_NOTICE) {
      const notificationData = message.data as NotificationData

      // 存储到通知中心（延迟获取store）
      this.getNoticeStore().addNotice({
        id: message.id || Date.now().toString(),
        message: notificationData.content,
        title: notificationData.title,
        read: false,
        time: new Date(message.timestamp).toLocaleString()
      })

      // 显示系统通知弹窗
      showNotifySuccess({
        title: notificationData.title || '系统通知',
        message: notificationData.content,
        duration: notificationData.duration || 4000,
        position: 'top-right',
        offset: 50
      })

      console.log('📢 处理系统通知:', notificationData.content)
      return false // 阻止继续传播
    }

    return true // 不是系统通知类型，继续传播
  }
}

/**
 * AI 聊天流式消息处理器
 *
 * 负责处理 AI 聊天相关的消息，包括：
 * - ai_chat_start: 开始生成
 * - ai_chat_stream: 流式内容
 * - ai_chat_complete: 生成完成
 * - ai_chat_error: 生成错误
 */
export class AiChatStreamHandler implements MessageHandler {
  private aiChatStore: any = null
  private storePromise: Promise<any> | null = null

  /**
   * 获取 AI 聊天 Store (延迟初始化，避免循环依赖)
   */
  private async getAiChatStore() {
    if (this.aiChatStore) {
      return this.aiChatStore
    }

    // 如果正在加载中，等待之前的 Promise
    if (this.storePromise) {
      return this.storePromise
    }

    // 动态导入 Store，避免循环依赖
    this.storePromise = import('@/stores/modules/aiChat')
      .then((module) => {
        this.aiChatStore = module.useAiChatStore()
        this.storePromise = null
        return this.aiChatStore
      })
      .catch((error) => {
        console.error('❌ 加载 AI 聊天 Store 失败:', error)
        this.storePromise = null
        return null
      })

    return this.storePromise
  }

  /**
   * 处理消息 (异步)
   */
  async handle(message: WSMessage): Promise<boolean> {
    const type = message.type
    const store = await this.getAiChatStore()

    if (!store) {
      console.error('AI 聊天 Store 不可用，无法处理消息')
      return true // 继续传播，让其他处理器处理
    }

    // 处理 AI 聊天开始
    if (type === WSMessageType.AI_CHAT_START) {
      const data = message.data as { sessionId: string; messageId?: string; message?: string }
      store.onChatStart(data.sessionId, data.messageId)
      return false
    }

    // 处理流式内容（按 phase 区分思考 / 最终回复两路增量）
    if (type === WSMessageType.AI_CHAT_STREAM) {
      const data = message.data as AiChatStreamData
      if (data.phase === 'thinking') {
        store.appendStreamReasoning(data.sessionId, data.messageId, data.reasoningContent || '')
      } else {
        store.appendStreamContent(data.sessionId, data.messageId, data.content)
      }
      return false
    }

    // 处理生成完成（reasoningContent 为后端回传的完整推理内容，可能为空）
    if (type === WSMessageType.AI_CHAT_COMPLETE) {
      const data = message.data as AiChatStreamData
      store.onChatComplete(data.sessionId, data.messageId, data.tokenUsage, data.reasoningContent)
      return false
    }

    // 处理生成错误
    if (type === WSMessageType.AI_CHAT_ERROR) {
      const data = message.data as AiChatStreamData
      store.onChatError(data.sessionId, data.error || '生成失败')

      // 显示错误通知
      showNotify({
        title: 'AI 生成失败',
        message: data.error || '生成过程中出现错误',
        type: 'error',
        duration: 4000
      })
      return false
    }

    return true // 不是 AI 聊天消息，继续传播
  }
}

// /**
//  * 聊天消息处理器
//  *
//  * 负责处理聊天相关的消息，包括：
//  * - 更新聊天数据
//  * - 显示聊天专用的通知（非系统通知）
//  * - 处理不同类型的聊天消息
//  */
// export class ChatMessageHandler implements MessageHandler {
//   private chatStore: any = null
//
//   /**
//    * 获取聊天存储实例（延迟初始化）
//    */
//   private getChatStore() {
//     if (!this.chatStore) {
//       this.chatStore = useChatStore()
//     }
//     return this.chatStore
//   }
//   /**
//    * 显示聊天专用通知
//    * 与系统通知不同，聊天通知通常更简洁，时间更短
//    */
//   private showChatNotification(chatData: ChatMessageData) {
//     // 检查当前是否在聊天页面，如果是则不显示通知
//     const currentRoute = useRoute()
//     const isInChatPage = currentRoute.path.includes('/chat')
//
//     if (!isInChatPage) {
//       showNotify({
//         title: `来自 ${chatData.fromUsername}`,
//         message: chatData.content,
//         duration: 3000,
//         position: 'bottom-right',
//         offset: 20
//       })
//     }
//   }
//   handle(message: WSMessage): boolean {
//     if (message.type === WSMessageType.CHAT_MESSAGE) {
//       const chatData = message.data as ChatMessageData
//
//       // 更新聊天数据存储（延迟获取store）
//       this.getChatStore().addMessage({
//         id: message.id || Date.now().toString(),
//         fromUserId: chatData.fromUserId,
//         fromUsername: chatData.fromUsername,
//         content: chatData.content,
//         timestamp: message.timestamp,
//         chatRoomId: chatData.chatRoomId,
//         messageType: chatData.messageType || 'text'
//       })
//
//       // 如果不是当前用户发送的消息，显示聊天通知
//       const userStore = useUserStore()
//       const currentUserId = userStore.userInfo?.userId
//       if (chatData.fromUserId !== currentUserId) {
//         this.showChatNotification(chatData)
//       }
//
//       console.log('💬 处理聊天消息:', `${chatData.fromUsername}: ${chatData.content}`)
//       return false // 阻止继续传播
//     }
//
//     return true // 不是聊天消息，继续传播
//   }
// }

/**
 * 心跳消息处理器
 *
 * 负责处理心跳相关的消息，包括：
 * - ping/pong 消息
 * - 连接保活消息
 * 这些消息完全不需要显示给用户，只做日志记录
 */
export class HeartbeatHandler implements MessageHandler {
  handle(message: WSMessage): boolean {
    if (message.type === WSMessageType.HEARTBEAT) {
      // 只做简单的日志记录，不做其他处理
      console.log('💓 心跳消息:', message.data)
      return false // 阻止继续传播
    }

    return true // 不是心跳消息，继续传播
  }
}

// 消息处理管道

/**
 * 消息处理管道
 *
 * 负责管理所有消息处理器，实现消息的依次处理：
 * - 按顺序调用处理器
 * - 支持处理器中断传播
 * - 统一的消息解析和标准化
 * - 错误处理和日志记录
 */
export class MessagePipeline {
  private handlers: MessageHandler[] = []

  /**
   * 添加消息处理器
   * @param handler 消息处理器实例
   */
  addHandler(handler: MessageHandler): void {
    this.handlers.push(handler)
    // console.log(`📝 添加消息处理器: ${handler.constructor.name}`)
  }

  /**
   * 移除消息处理器
   * @param handlerClass 要移除的处理器类
   */
  removeHandler(handlerClass: new () => MessageHandler): void {
    const index = this.handlers.findIndex((handler) => handler instanceof handlerClass)
    if (index !== -1) {
      this.handlers.splice(index, 1)
      // (`🗑️ 移除消息处理器: ${handlerClass.name}`)
    }
  }

  /**
   * 处理消息 (支持异步处理器)
   * @param rawMessage 原始消息数据
   */
  async process(rawMessage: any): Promise<void> {
    try {
      // 解析并标准化消息
      const message = this.parseMessage(rawMessage)
      if (!message) {
        console.warn('⚠️ 无法解析的消息，忽略处理:', rawMessage)
        return
      }

      // console.log(`📨 WebSocket收到消息 [${message.type}]:`, message.data)

      // 依次通过处理器处理消息 (支持异步)
      for (const handler of this.handlers) {
        try {
          const shouldContinue = await handler.handle(message)
          if (!shouldContinue) {
            // console.log(`✋ 消息被 ${handler.constructor.name} 处理，停止传播`)
            break // 处理器阻止了继续传播
          }
        } catch (handlerError) {
          console.error(`❌ 处理器 ${handler.constructor.name} 处理消息时出错:`, handlerError)
          // 继续执行下一个处理器，不因为一个处理器的错误而中断整个流程
        }
      }
    } catch (error) {
      console.error('❌ 消息处理管道出错:', error, '原始消息:', rawMessage)
    }
  }

  /**
   * 解析消息
   * @param rawMessage 原始消息
   * @returns 标准化的消息对象或null
   */
  private parseMessage(rawMessage: any): WSMessage | null {
    try {
      // 如果是字符串，尝试解析为JSON
      if (typeof rawMessage === 'string') {
        // 快速检查是否为心跳消息
        const lowerMessage = rawMessage.toLowerCase()
        if (lowerMessage.includes('ping') || lowerMessage.includes('pong')) {
          return {
            type: WSMessageType.HEARTBEAT,
            data: rawMessage,
            timestamp: Date.now()
          }
        }

        // 尝试解析为JSON
        try {
          const parsed = JSON.parse(rawMessage)
          return this.normalizeMessage(parsed)
        } catch {
          // 解析失败，当作普通文本消息处理
          return {
            type: WSMessageType.SYSTEM_NOTICE,
            data: { content: rawMessage },
            timestamp: Date.now()
          }
        }
      }

      // 如果已经是对象，直接标准化
      if (typeof rawMessage === 'object' && rawMessage !== null) {
        return this.normalizeMessage(rawMessage)
      }

      // 其他类型的消息，转换为字符串处理
      return {
        type: WSMessageType.SYSTEM_NOTICE,
        data: { content: String(rawMessage) },
        timestamp: Date.now()
      }
    } catch (error) {
      console.error('⚠️ 消息解析异常:', error, '原始消息:', rawMessage)
      return null
    }
  }

  /**
   * 标准化消息格式
   * @param obj 消息对象
   * @returns 标准化的消息对象
   */
  private normalizeMessage(obj: any): WSMessage {
    // 确定消息类型
    let messageType: WSMessageType = WSMessageType.SYSTEM_NOTICE

    if (obj.type && Object.values(WSMessageType).includes(obj.type)) {
      messageType = obj.type
    } else if (obj.messageType && Object.values(WSMessageType).includes(obj.messageType)) {
      messageType = obj.messageType
    }

    // 提取消息数据
    let messageData: any

    // 如果是 AI 聊天相关的消息,保留完整对象作为 data
    if (messageType.startsWith('ai_chat')) {
      messageData = obj
    } else {
      // 其他消息类型,尝试提取 data 字段
      messageData = obj.data || obj.message || obj.content || obj
    }

    // 如果数据是字符串且看起来像JSON,尝试解析
    if (typeof messageData === 'string') {
      try {
        messageData = JSON.parse(messageData)
      } catch {
        // 解析失败,保持原字符串
        messageData = { content: messageData }
      }
    }

    return {
      type: messageType,
      data: messageData,
      timestamp: obj.timestamp || Date.now(),
      id: obj.id || obj.messageId
    }
  }

  /**
   * 获取当前注册的处理器列表
   */
  getHandlers(): string[] {
    return this.handlers.map((handler) => handler.constructor.name)
  }
}

// ================================
// WebSocket 组合函数（纯净版）
// ================================

/**
 * WebSocket 通信钩子函数 (useWebSocket) - 动态退避策略
 *
 * 包含以下功能：
 * - 自动连接: 创建与服务器的WebSocket连接 (基于VueUse实现)
 * - 动态退避重连: 连接断开后按指数退避策略自动重连 (自定义重连逻辑)
 * - 心跳检测: 定时发送心跳消息保持连接活跃 (heartbeat配置)
 * - 消息接收: 监听并处理服务器推送的消息 (onMessage)
 * - 消息发送: 提供发送消息的方法 (send)
 * - 连接管理: 提供手动连接、断开和重连的方法 (connect, disconnect, reconnect)
 * - 状态监控: 实时监控连接状态变化 (status, isConnected)
 * - 资源清理: 组件卸载时自动清理连接 (onUnmounted)
 * - 认证支持: 自动附加令牌进行身份验证 (getAuthQuery)
 *
 * @param url WebSocket服务器地址
 * @param options 配置选项 { maxRetries?: number, baseDelay?: number, heartbeatInterval?: number, heartbeatMessage?: string, onMessage?: Function, onConnected?: Function, onDisconnected?: Function, onError?: Function }
 * @returns { connect, disconnect, reconnect, send, status, isConnected, data } 返回控制方法、连接状态和数据
 */
export const useWS = (
  url: string,
  options: {
    maxRetries?: number // 最大重试次数（默认8次）
    baseDelay?: number // 基础延迟秒数（默认3秒）
    heartbeatInterval?: number // 心跳间隔毫秒数（默认30秒）
    heartbeatMessage?: string // 心跳消息内容
    onMessage?: (data: any) => void // 消息接收回调
    onConnected?: () => void // 连接成功回调
    onDisconnected?: (code: number, reason: string) => void // 连接断开回调
    onError?: (error: any) => void // 连接错误回调
  } = {}
) => {
  // 检查系统配置
  const featureStore = useFeatureStore()
  if (!featureStore.features.websocketEnabled) {
    console.warn('[WebSocket] 系统未启用WebSocket功能')
    return {
      connect: () => {},
      disconnect: () => {},
      reconnect: () => {},
      send: () => false,
      status: ref('CLOSED'),
      isConnected: ref(false),
      data: ref(null)
    }
  }

  const { getAuthQuery } = useToken()

  // 退避策略配置
  const maxRetries = options.maxRetries ?? 8
  const baseDelay = options.baseDelay ?? 3
  const heartbeatInterval = options.heartbeatInterval ?? 30000
  const heartbeatMessage =
    options.heartbeatMessage ??
    JSON.stringify({
      type: 'ping',
      timestamp: Date.now()
    })

  // 重连状态管理
  let currentRetryCount = 0
  let retryTimeoutId: number | null = null
  let isManuallyClose = false

  /**
   * 构造WebSocket连接URL
   * 自动附加认证信息
   */
  const buildWebSocketUrl = (): string => {
    const authQuery = getAuthQuery()
    if (!authQuery) {
      console.warn('[WebSocket] 未找到有效token，可能影响连接认证')
      return url
    }

    const separator = url.includes('?') ? '&' : '?'
    return `${url}${separator}${authQuery}`
  }

  /**
   * 动态计算退避延迟时间
   * 使用指数退避算法：3 -> 6 -> 12 -> 24 -> 48 -> 96 -> 192 -> 384...
   * 公式：delay = baseDelay * (2^retryIndex)
   */
  const calculateRetryDelay = (retryIndex: number): number => {
    const delaySeconds = baseDelay * 2 ** retryIndex
    return delaySeconds * 1000
  }

  /**
   * 自定义退避重连逻辑
   */
  const attemptReconnect = () => {
    if (isManuallyClose) {
      console.log(`${getCurrentTime()} 🛑 手动关闭连接，停止重连`)
      return
    }

    if (currentRetryCount >= maxRetries) {
      console.log(`${getCurrentTime()} 🛑 WebSocket重试${maxRetries}次后连接失败，停止重试`)
      return
    }

    const delay = calculateRetryDelay(currentRetryCount)
    const delaySeconds = delay / 1000

    console.log(`${getCurrentTime()} 🔄 WebSocket将在${delaySeconds}秒后进行第${currentRetryCount + 1}次重连...`)

    retryTimeoutId = window.setTimeout(() => {
      console.log(`${getCurrentTime()} 🚀 开始第${currentRetryCount + 1}次重连尝试...`)
      currentRetryCount++
      open()
    }, delay)
  }

  // 使用 VueUse 的 WebSocket，禁用默认重连
  const { status, data, send, open, close } = useWebSocket(
    computed(() => buildWebSocketUrl()),
    {
      autoReconnect: false, // 禁用默认重连，使用自定义退避策略
      heartbeat: {
        message: heartbeatMessage,
        interval: heartbeatInterval,
        pongTimeout: 2000
      },

      onConnected(ws) {
        console.log(`${getCurrentTime()} ✅ WebSocket连接已建立`)
        currentRetryCount = 0 // 连接成功后重置重试计数
        isManuallyClose = false

        // 清除重试定时器
        if (retryTimeoutId) {
          clearTimeout(retryTimeoutId)
          retryTimeoutId = null
        }

        // 调用用户自定义的连接成功回调
        options.onConnected?.()
      },

      onDisconnected(ws, event) {
        console.log(`${getCurrentTime()} ❌ WebSocket连接已断开, code: ${event.code}, reason: ${event.reason}`)

        // 调用用户自定义的连接断开回调
        options.onDisconnected?.(event.code, event.reason)

        // 如果不是手动关闭且不是正常关闭，则尝试重连
        if (!isManuallyClose && event.code !== 1000) {
          attemptReconnect()
        }
      },

      onError(ws, event) {
        console.error(`${getCurrentTime()} ❌ WebSocket连接错误:`, event)

        // 调用用户自定义的错误回调
        options.onError?.(event)

        // 连接错误也触发重连
        if (!isManuallyClose) {
          attemptReconnect()
        }
      },

      onMessage(ws, event) {
        // 只负责传递原始消息，不做任何具体处理
        // 具体的消息处理由消息管道或用户自定义回调处理
        options.onMessage?.(event.data)
      }
    }
  )

  // 计算属性：连接状态
  const isConnected = computed(() => status.value === 'OPEN')

  /**
   * 建立WebSocket连接
   */
  const connect = () => {
    console.log(`${getCurrentTime()} 🔗 正在连接WebSocket: ${url}`)
    isManuallyClose = false
    open()
  }

  /**
   * 断开WebSocket连接
   */
  const disconnect = () => {
    console.log(`${getCurrentTime()} 🔒 手动关闭WebSocket连接`)
    isManuallyClose = true

    // 清除重试定时器
    if (retryTimeoutId) {
      clearTimeout(retryTimeoutId)
      retryTimeoutId = null
    }

    close()
  }

  /**
   * 手动重新连接（重置重试计数）
   */
  const reconnect = () => {
    console.log(`${getCurrentTime()} 🔧 手动重新连接WebSocket...`)

    // 清除现有的重试定时器
    if (retryTimeoutId) {
      clearTimeout(retryTimeoutId)
      retryTimeoutId = null
    }

    // 重置状态
    currentRetryCount = 0
    isManuallyClose = false

    // 先断开再重新连接
    close()
    setTimeout(() => {
      open()
    }, 100)
  }

  /**
   * 增强的发送方法
   * 支持发送字符串或对象，自动处理JSON序列化
   */
  const enhancedSend = (message: string | object): boolean => {
    if (!isConnected.value) {
      console.warn(`${getCurrentTime()} ⚠️ WebSocket未连接，无法发送消息，当前状态: ${status.value}`)
      return false
    }

    try {
      const data = typeof message === 'string' ? message : JSON.stringify(message)

      send(data)

      // 忽略心跳消息的日志打印，避免日志污染
      if (!data.startsWith('{"type":"ping"')) {
        console.log(`${getCurrentTime()} 📤 WebSocket发送消息成功:`, data)
      }

      return true
    } catch (error) {
      console.error(`${getCurrentTime()} ❌ WebSocket发送消息失败:`, error)
      return false
    }
  }

  // 组件卸载时自动清理
  onUnmounted(() => {
    disconnect()
  })

  return {
    connect, // 建立连接
    disconnect, // 断开连接
    reconnect, // 重新连接
    send: enhancedSend, // 发送消息
    status: readonly(status), // 连接状态
    isConnected: readonly(isConnected), // 是否已连接
    data: readonly(data) // 接收到的消息数据
  }
}

// 全局WebSocket管理器

/**
 * 全局WebSocket管理器
 *
 * 包含以下功能：
 * - 单例管理: 确保应用级别只有一个WebSocket连接实例 (GlobalWebSocketManager)
 * - 自动初始化: 根据系统配置和用户状态自动初始化连接 (shouldInitializeWebSocket)
 * - URL构建: 自动根据当前协议构建正确的WebSocket地址 (getWebSocketUrl)
 * - 状态检查: 防止重复连接和初始化 (isInitialized, isInitializing)
 * - 连接控制: 提供连接、断开、发送消息的全局接口 (connect, disconnect, send)
 * - 消息处理: 统一处理全局级别的系统消息 (MessagePipeline)
 * - 资源清理: 提供销毁和重置功能 (destroy)
 * - 状态监控: 实时获取连接状态 (status, isConnected)
 *
 * @description 提供应用级别的WebSocket连接管理，避免重复连接，统一处理系统消息
 */
export class GlobalWebSocketManager {
  private wsInstance: ReturnType<typeof useWS> | null = null
  private isInitialized = false
  private isInitializing = false
  private messagePipeline = new MessagePipeline()

  constructor() {
    // 初始化默认的消息处理管道
    this.setupDefaultMessageHandlers()
  }

  /**
   * 设置默认的消息处理器
   * 按优先级顺序添加处理器，处理器会按顺序执行
   */
  private setupDefaultMessageHandlers() {
    // console.log('🔧 初始化消息处理管道...')

    // 1. 心跳处理器 - 最高优先级，过滤技术消息
    this.messagePipeline.addHandler(new HeartbeatHandler())

    // 2. AI 聊天处理器 - 处理 AI 相关消息
    this.messagePipeline.addHandler(new AiChatStreamHandler())

    // 3. 系统通知处理器 - 处理系统级别的通知
    this.messagePipeline.addHandler(new SystemNoticeHandler())

    // 4. 聊天消息处理器 - 处理聊天相关消息
    // this.messagePipeline.addHandler(new ChatMessageHandler())

    console.log('✅ 消息处理管道初始化完成，处理器列表:', this.messagePipeline.getHandlers())
  }

  /**
   * 获取WebSocket URL
   * 根据当前页面协议自动构建正确的WebSocket地址
   */
  private getWebSocketUrl(): string {
    const baseUrl = SystemConfig.api.baseUrl

    // 根据当前页面协议决定WebSocket协议
    const currentProtocol = window.location.protocol
    const wsUrl = currentProtocol === 'https:' ? baseUrl.replace(/^https?:/, 'wss:') : baseUrl.replace(/^https?:/, 'ws:')

    return `${wsUrl}/resource/websocket`
  }

  /**
   * 检查是否应该初始化WebSocket
   * 检查系统配置和用户登录状态
   */
  private shouldInitializeWebSocket(): { should: boolean; reason: string } {
    // 检查系统配置
    const featureStore = useFeatureStore()
    if (!featureStore.features.websocketEnabled) {
      return { should: false, reason: '系统未启用WebSocket功能' }
    }

    // 检查用户登录状态
    const { getAuthQuery } = useToken()
    const authQuery = getAuthQuery()
    if (!authQuery) {
      return { should: false, reason: '用户未登录' }
    }

    return { should: true, reason: '满足初始化条件' }
  }

  /**
   * 初始化全局WebSocket实例
   * @param url 可选的自定义WebSocket地址
   * @param options 可选的配置选项
   * @returns WebSocket实例或null
   */
  initialize(url?: string, options?: Parameters<typeof useWS>[1]) {
    // 如果已经初始化，直接返回现有实例
    if (this.isInitialized) {
      //console.log('[GlobalWebSocket] 已初始化，返回现有实例')
      return this.wsInstance
    }

    // 如果正在初始化中，返回null并提示
    if (this.isInitializing) {
      //console.log('[GlobalWebSocket] 正在初始化中，跳过重复调用')
      return null
    }

    // 设置初始化标志
    this.isInitializing = true

    try {
      // 检查是否应该初始化
      const { should, reason } = this.shouldInitializeWebSocket()

      if (!should) {
        //console.log(`[GlobalWebSocket] 跳过初始化: ${reason}`)
        this.isInitializing = false
        return null
      }

      //console.log(`[GlobalWebSocket] ${reason}，开始初始化全局WebSocket连接`)

      // 使用自定义URL或自动构建的URL
      const wsUrl = url || this.getWebSocketUrl()
      //console.log(`[GlobalWebSocket] 使用WebSocket地址: ${wsUrl}`)

      // 创建WebSocket实例
      this.wsInstance = useWS(wsUrl, {
        maxRetries: 8,
        baseDelay: 3,
        heartbeatInterval: 30000,
        ...options, // 合并用户自定义选项

        // 使用消息管道处理所有消息
        onMessage: (data) => {
          this.messagePipeline.process(data)
          // 还可以调用用户自定义的消息处理
          options?.onMessage?.(data)
        },

        onConnected: () => {
          // console.log('[GlobalWebSocket] 全局WebSocket连接建立成功')
          options?.onConnected?.()
        },

        onDisconnected: (code, reason) => {
          //console.log('[GlobalWebSocket] 全局WebSocket连接断开', { code, reason })
          options?.onDisconnected?.(code, reason)
        },

        onError: (error) => {
          //console.error('[GlobalWebSocket] 全局WebSocket连接错误', error)
          options?.onError?.(error)
        }
      })

      this.isInitialized = true
      //console.log('[GlobalWebSocket] 初始化完成')

      return this.wsInstance
    } catch (error) {
      //console.error('[GlobalWebSocket] 初始化失败:', error)
      this.wsInstance = null
      return null
    } finally {
      this.isInitializing = false
    }
  }

  /**
   * 连接WebSocket
   * @returns 是否成功发起连接
   */
  connect(): boolean {
    if (!this.wsInstance) {
      // console.warn('[GlobalWebSocket] 未初始化，无法连接')
      return false
    }

    const currentStatus = this.wsInstance.status.value

    if (currentStatus === 'OPEN') {
      // console.log('[GlobalWebSocket] 连接已建立，跳过重复连接')
      return true
    }

    if (currentStatus === 'CONNECTING') {
      //console.log('[GlobalWebSocket] 正在连接中，跳过重复连接')
      return true
    }

    //console.log('[GlobalWebSocket] 开始建立连接...')
    this.wsInstance.connect()
    return true
  }

  /**
   * 断开WebSocket连接
   */
  disconnect(): void {
    if (!this.wsInstance) {
      //console.log('[GlobalWebSocket] 实例不存在，无需断开连接')
      return
    }

    //console.log('[GlobalWebSocket] 断开连接')
    this.wsInstance.disconnect()
  }

  /**
   * 重新连接WebSocket
   * @returns 是否成功发起重连
   */
  reconnect(): boolean {
    if (!this.wsInstance) {
      //console.warn('[GlobalWebSocket] 未初始化，无法重连')
      return false
    }

    //console.log('[GlobalWebSocket] 重新连接')
    this.wsInstance.reconnect()
    return true
  }

  /**
   * 发送消息
   * @param message 要发送的消息（字符串或对象）
   * @returns 是否发送成功
   */
  send(message: string | object): boolean {
    if (!this.wsInstance) {
      //console.warn('[GlobalWebSocket] 未初始化，无法发送消息')
      return false
    }

    return this.wsInstance.send(message)
  }

  /**
   * 获取连接状态
   */
  get status(): string {
    return this.wsInstance?.status.value || 'CLOSED'
  }

  /**
   * 获取是否已连接
   */
  get isConnected(): boolean {
    return this.wsInstance?.isConnected.value || false
  }

  /**
   * 添加自定义消息处理器
   * @param handler 消息处理器实例
   */
  addMessageHandler(handler: MessageHandler): void {
    this.messagePipeline.addHandler(handler)
    //console.log(`[GlobalWebSocket] 添加自定义消息处理器: ${handler.constructor.name}`)
  }

  /**
   * 移除消息处理器
   * @param handlerClass 要移除的处理器类
   */
  removeMessageHandler(handlerClass: new () => MessageHandler): void {
    this.messagePipeline.removeHandler(handlerClass)
    // console.log(`[GlobalWebSocket] 移除消息处理器: ${handlerClass.name}`)
  }

  /**
   * 获取当前消息处理器列表
   */
  getMessageHandlers(): string[] {
    return this.messagePipeline.getHandlers()
  }

  /**
   * 销毁全局WebSocket实例
   * 完全重置管理器状态，允许重新初始化
   */
  destroy(): void {
    if (this.wsInstance) {
      // console.log('[GlobalWebSocket] 销毁WebSocket实例')
      this.wsInstance.disconnect()
      this.wsInstance = null
    }

    // 重置状态标志
    this.isInitialized = false
    this.isInitializing = false

    // console.log('[GlobalWebSocket] 全局WebSocket管理器已重置，可重新初始化')
  }
}

// 导出全局实例和工具函数

/**
 * 全局WebSocket管理器实例
 * 整个应用共享一个实例，确保单例模式
 */
export const webSocket = new GlobalWebSocketManager()
