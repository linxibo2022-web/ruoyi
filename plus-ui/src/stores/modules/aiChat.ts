/**
 * AI 聊天管理 (useAiChatStore)
 *
 * 基于 Pinia 的 AI 聊天数据管理模块，提供统一的会话管理和消息处理功能。
 *
 * 包含以下功能：
 * - 会话管理: 创建、切换、删除会话 (createSession, switchSession, deleteSession)
 * - 消息管理: 发送消息、接收流式响应 (sendMessage, appendStreamContent)
 * - 状态管理: 生成状态、会话列表 (isGenerating, sessionList)
 * - WebSocket 通信: 与后端 AI 服务进行实时通信
 * - 历史记录: 保存对话历史和会话信息
 */

/** 应用模块名称 */
const AI_CHAT_MODULE = 'aiChat'

/**
 * AI 聊天消息接口
 */
export interface AiChatMessage {
  /** 消息唯一标识 */
  id: string
  /** 消息角色：user-用户 | assistant-AI助手 | system-系统 */
  role: 'user' | 'assistant' | 'system'
  /** 消息内容（最终回复） */
  content: string
  /**
   * 深度思考 / 推理内容
   * @description 推理模型（如 deepseek-reasoner）且开启 enableThinking 时，先于 content 流式返回的思考过程
   */
  reasoningContent?: string
  /**
   * 思考阶段是否已结束
   * @description 收到第一个 content 增量或对话完成时置为 true，UI 据此切换思考面板状态/收起动画
   */
  reasoningFinished?: boolean
  /** 消息时间戳 */
  timestamp: number
  /** Token 使用情况 */
  tokenUsage?: {
    promptTokens: number
    completionTokens: number
    totalTokens: number
  }
  /** 引用的文档列表 */
  references?: any[]
  /** 消息状态：sending-发送中 | streaming-生成中 | complete-完成 | error-错误 */
  status?: 'sending' | 'streaming' | 'complete' | 'error'
  /** 错误信息 */
  error?: string
}

/**
 * AI 聊天会话接口
 */
export interface AiChatSession {
  /** 会话唯一标识 */
  id: string
  /** 会话标题 */
  title: string
  /** 消息列表 */
  messages: AiChatMessage[]
  /** 创建时间 */
  createdAt: number
  /** 更新时间 */
  updatedAt: number
  /** 模型提供商 */
  provider?: string
  /** 模型名称 */
  modelName?: string
}

export const useAiChatStore = defineStore(AI_CHAT_MODULE, () => {
  // ==================== 响应式状态 ====================

  /**
   * 会话集合
   * @description 使用 Map 存储多个会话，key 为会话 ID，value 为会话对象
   */
  const sessions = ref<Map<string, AiChatSession>>(new Map())

  /**
   * 当前活跃的会话 ID
   */
  const currentSessionId = ref<string | null>(null)

  /**
   * 当前正在流式生成的消息 ID
   */
  const streamingMessageId = ref<string | null>(null)

  /**
   * 流式内容缓冲区
   * @description 暂存接收到的流式内容片段
   */
  const streamContentBuffer = ref<string>('')

  /**
   * 流式推理（思考）内容缓冲区
   * @description 暂存接收到的 thinking 阶段增量；当目标消息暂不可定位时兜底使用
   */
  const streamReasoningBuffer = ref<string>('')

  // ==================== 计算属性 ====================

  /**
   * 获取当前会话
   * @returns 当前活跃的会话对象或 null
   */
  const currentSession = computed((): AiChatSession | null => {
    if (!currentSessionId.value) return null
    return sessions.value.get(currentSessionId.value) || null
  })

  /**
   * 获取当前会话的消息列表
   * @returns 当前会话的消息数组
   */
  const currentMessages = computed((): AiChatMessage[] => {
    return currentSession.value?.messages || []
  })

  /**
   * 是否正在生成中
   * @returns 当前是否有消息正在生成
   */
  const isGenerating = computed((): boolean => {
    return streamingMessageId.value !== null
  })

  /**
   * 获取所有会话列表（按更新时间倒序）
   * @returns 会话数组，最新的在前
   */
  const sessionList = computed((): AiChatSession[] => {
    return Array.from(sessions.value.values()).sort((a, b) => b.updatedAt - a.updatedAt)
  })

  // ==================== 会话管理方法 ====================

  /**
   * 创建新会话
   * @param options 会话配置选项
   * @param options.title 会话标题
   * @param options.provider 模型提供商
   * @param options.modelName 模型名称
   * @returns 新创建的会话 ID
   * @example
   * createSession({
   *   title: '新对话',
   *   provider: 'deepseek',
   *   modelName: 'deepseek-chat'
   * })
   */
  const createSession = (options?: { title?: string; provider?: string; modelName?: string }): string => {
    const sessionId = generateSessionId()
    const now = Date.now()

    // modelName 不设默认值：未显式指定时由后端按 provider 的 yml 配置选默认模型
    // （ModelFactory.getModelName 优先级：请求传入 > yml provider.modelName > 内置兜底）
    const newSession: AiChatSession = {
      id: sessionId,
      title: options?.title || `New Chat ${new Date().toLocaleString()}`,
      messages: [],
      createdAt: now,
      updatedAt: now,
      provider: options?.provider || 'deepseek',
      modelName: options?.modelName
    }

    sessions.value.set(sessionId, newSession)
    currentSessionId.value = sessionId

    return sessionId
  }

  /**
   * 切换会话
   * @param sessionId 要切换到的会话 ID
   * @returns 是否切换成功
   * @example switchSession('session_123456')
   */
  const switchSession = (sessionId: string): boolean => {
    if (sessions.value.has(sessionId)) {
      currentSessionId.value = sessionId
      return true
    }
    console.warn(`会话不存在: ${sessionId}`)
    return false
  }

  /**
   * 删除会话
   * @param sessionId 要删除的会话 ID
   * @returns 是否删除成功
   * @example deleteSession('session_123456')
   */
  const deleteSession = (sessionId: string): boolean => {
    if (!sessions.value.delete(sessionId)) {
      return false
    }

    // 如果删除的是当前会话，切换到第一个会话或创建新会话
    if (currentSessionId.value === sessionId) {
      const firstSession = sessionList.value[0]
      if (firstSession) {
        currentSessionId.value = firstSession.id
      } else {
        createSession()
      }
    }

    return true
  }

  /**
   * 清空所有会话
   * @description 删除所有会话数据并重置状态
   * @example clearAllSessions()
   */
  const clearAllSessions = (): void => {
    sessions.value.clear()
    currentSessionId.value = null
    streamingMessageId.value = null
    streamContentBuffer.value = ''
    streamReasoningBuffer.value = ''
  }

  /**
   * 更新会话标题
   * @param sessionId 会话 ID
   * @param title 新标题
   * @returns 是否更新成功
   * @example updateSessionTitle('session_123456', '关于 AI 的讨论')
   */
  const updateSessionTitle = (sessionId: string, title: string): boolean => {
    const session = sessions.value.get(sessionId)
    if (session) {
      session.title = title
      session.updatedAt = Date.now()
      return true
    }
    return false
  }

  // ==================== 消息管理方法 ====================

  /**
   * 发送消息到 AI
   * @param content 消息内容
   * @param options 发送选项
   * @param options.sessionId 会话 ID
   * @param options.provider 模型提供商
   * @param options.modelName 模型名称
   * @param options.systemPrompt 系统提示词
   * @param options.temperature 温度参数
   * @param options.maxTokens 最大 Token 数
   * @param options.thinkingEnabled 本次是否启用深度思考（null/undefined 表示沿用后端配置）
   * @param options.thinkingBudgetTokens 思考预算 Token 数（仅 Claude/通义千问生效）
   * @returns 是否发送成功
   * @example
   * sendMessage('你好，请介绍一下自己', {
   *   provider: 'deepseek',
   *   temperature: 0.7
   * })
   */
  const sendMessage = (
    content: string,
    options?: {
      sessionId?: string
      provider?: string
      modelName?: string
      systemPrompt?: string
      temperature?: number
      maxTokens?: number
      thinkingEnabled?: boolean
      thinkingBudgetTokens?: number
    }
  ): boolean => {
    // 确保有会话
    let sessionId = options?.sessionId || currentSessionId.value
    if (!sessionId) {
      sessionId = createSession()
    }

    const session = sessions.value.get(sessionId)
    if (!session) {
      console.error(`会话不存在: ${sessionId}`)
      return false
    }

    // 添加用户消息
    const userMessage: AiChatMessage = {
      id: generateMessageId(),
      role: 'user',
      content: content,
      timestamp: Date.now(),
      status: 'complete'
    }
    session.messages.push(userMessage)

    // 添加一个空的助手消息，用于接收流式内容
    // 注意: id 先设为空字符串，等待后端返回 messageId 后回填
    const assistantMessage: AiChatMessage = {
      id: '',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      status: 'sending'
    }
    session.messages.push(assistantMessage)
    // 使用 sessionId 作为临时标识，而不是 messageId
    streamingMessageId.value = sessionId
    streamContentBuffer.value = ''
    streamReasoningBuffer.value = ''

    // 更新会话时间
    session.updatedAt = Date.now()

    // 通过 WebSocket 发送消息
    // modelName 仅在用户显式选择或会话已绑定时才下发，否则交给后端按 provider 选默认模型
    const resolvedModelName = options?.modelName || session.modelName
    const request: Record<string, unknown> = {
      type: 'ai_chat',
      sessionId: sessionId,
      message: content,
      provider: options?.provider || session.provider || 'deepseek',
      mode: 'CONTINUOUS',
      systemPrompt: options?.systemPrompt,
      temperature: options?.temperature,
      maxTokens: options?.maxTokens
    }
    if (resolvedModelName) {
      request.modelName = resolvedModelName
    }
    applyThinkingOptions(request, options)

    const success = webSocket.send(request)
    if (!success) {
      assistantMessage.status = 'error'
      assistantMessage.error = 'WebSocket 未连接，无法发送消息'
      streamingMessageId.value = null
      return false
    }

    return true
  }

  /**
   * WebSocket 回调：聊天开始
   * @param sessionId 会话 ID
   * @param messageId 后端返回的消息 ID
   * @example onChatStart('session_123456', 'msg_backend_123')
   */
  const onChatStart = (sessionId: string, messageId?: string): void => {
    const session = sessions.value.get(sessionId)
    if (!session || session.messages.length === 0) {
      console.error('AI聊天: session 不存在或没有消息')
      return
    }

    // 找到最后一条助手消息(刚创建的空消息)
    const lastMessage = session.messages[session.messages.length - 1]

    if (lastMessage.role === 'assistant') {
      // 回填后端返回的 messageId
      if (messageId && !lastMessage.id) {
        lastMessage.id = messageId
      }
      lastMessage.status = 'streaming'

      // 强制触发响应式更新
      sessions.value.set(sessionId, { ...session })
    }
  }

  /**
   * WebSocket 回调：追加流式内容
   * @param sessionId 会话 ID
   * @param messageId 消息 ID
   * @param content 内容片段
   * @example appendStreamContent('session_123456', 'msg_123456', '你好')
   */
  const appendStreamContent = (sessionId: string, messageId: string, content: string): void => {
    const session = sessions.value.get(sessionId)
    if (!session) {
      console.error('AI聊天: 找不到 session')
      return
    }

    // 查找对应的消息 - 优先通过 messageId 查找，找不到则使用最后一条助手消息
    let message = session.messages.find((msg) => msg.id === messageId)

    if (!message) {
      // 如果通过 messageId 找不到，使用最后一条助手消息
      const assistantMessages = session.messages.filter((msg) => msg.role === 'assistant')
      message = assistantMessages[assistantMessages.length - 1]

      // 回填 messageId
      if (message && !message.id) {
        message.id = messageId
      }
    }

    if (message) {
      // 收到第一个最终回复增量 → 标记思考阶段结束（即便没有思考内容也无副作用）
      message.reasoningFinished = true
      message.content += content
      message.status = 'streaming'

      // 强制触发响应式更新
      sessions.value.set(sessionId, { ...session })
    } else {
      // 如果还是找不到，追加到缓冲区
      streamContentBuffer.value += content
    }
  }

  /**
   * WebSocket 回调：追加流式推理（思考）内容
   * @description 仅推理模型 + enableThinking=true 时触发；早于 appendStreamContent
   * @param sessionId 会话 ID
   * @param messageId 消息 ID
   * @param content 推理内容片段
   * @example appendStreamReasoning('session_123456', 'msg_123456', '让我想想...')
   */
  const appendStreamReasoning = (sessionId: string, messageId: string, content: string): void => {
    if (!content) return

    const session = sessions.value.get(sessionId)
    if (!session) {
      console.error('AI聊天: 找不到 session')
      return
    }

    // 查找对应的消息 - 优先通过 messageId 查找，找不到则使用最后一条助手消息
    let message = session.messages.find((msg) => msg.id === messageId)

    if (!message) {
      const assistantMessages = session.messages.filter((msg) => msg.role === 'assistant')
      message = assistantMessages[assistantMessages.length - 1]

      // 回填 messageId
      if (message && !message.id) {
        message.id = messageId
      }
    }

    if (message) {
      message.reasoningContent = (message.reasoningContent || '') + content
      message.reasoningFinished = false
      message.status = 'streaming'

      // 强制触发响应式更新
      sessions.value.set(sessionId, { ...session })
    } else {
      // 如果还是找不到，追加到缓冲区
      streamReasoningBuffer.value += content
    }
  }

  /**
   * WebSocket 回调：聊天完成
   * @param sessionId 会话 ID
   * @param messageId 消息 ID
   * @param tokenUsage Token 使用情况
   * @param reasoningContent 完整推理内容（推理模型 + enableThinking 时由后端回传，用于校准流式拼接结果）
   * @example
   * onChatComplete('session_123456', 'msg_123456', {
   *   promptTokens: 10,
   *   completionTokens: 20,
   *   totalTokens: 30
   * })
   */
  const onChatComplete = (sessionId: string, messageId: string, tokenUsage?: any, reasoningContent?: string): void => {
    const session = sessions.value.get(sessionId)
    if (!session) {
      console.error('AI聊天: 找不到 session')
      return
    }

    // 查找对应的消息 - 优先通过 messageId 查找，找不到则使用最后一条助手消息
    let message = session.messages.find((msg) => msg.id === messageId)

    if (!message) {
      // 如果通过 messageId 找不到，使用最后一条助手消息
      const assistantMessages = session.messages.filter((msg) => msg.role === 'assistant')
      message = assistantMessages[assistantMessages.length - 1]

      // 回填 messageId
      if (message && !message.id) {
        message.id = messageId
      }
    }

    if (message) {
      message.status = 'complete'
      message.tokenUsage = tokenUsage
      message.reasoningFinished = true

      // 如果有缓冲区内容，追加上去
      if (streamContentBuffer.value) {
        message.content += streamContentBuffer.value
      }
      if (streamReasoningBuffer.value) {
        message.reasoningContent = (message.reasoningContent || '') + streamReasoningBuffer.value
      }
      // 后端回传了完整推理内容时，以其为准（修正流式拼接可能的偏差）
      if (reasoningContent) {
        message.reasoningContent = reasoningContent
      }

      // 强制触发响应式更新
      sessions.value.set(sessionId, { ...session })
    }

    // 更新会话标题（使用第一条用户消息）
    // 检查是否为默认标题（支持中英文）
    const isDefaultTitle = session.title.startsWith('新对话') || session.title.startsWith('New Chat')
    if (session.messages.length === 2 && isDefaultTitle) {
      const firstUserMessage = session.messages.find((m) => m.role === 'user')
      if (firstUserMessage) {
        session.title = firstUserMessage.content.substring(0, 30) + '...'
      }
    }

    // 清理状态
    streamingMessageId.value = null
    streamContentBuffer.value = ''
    streamReasoningBuffer.value = ''
    session.updatedAt = Date.now()
  }

  /**
   * WebSocket 回调：聊天错误
   * @param sessionId 会话 ID
   * @param error 错误信息
   * @example onChatError('session_123456', '生成失败')
   */
  const onChatError = (sessionId: string, error: string): void => {
    const session = sessions.value.get(sessionId)
    if (!session) return

    // 找到最后一条助手消息，标记为错误
    for (let i = session.messages.length - 1; i >= 0; i--) {
      const message = session.messages[i]
      if (message.role === 'assistant') {
        message.status = 'error'
        message.error = error
        break
      }
    }

    // 清理状态
    streamingMessageId.value = null
    streamContentBuffer.value = ''
    streamReasoningBuffer.value = ''
  }

  /**
   * 重新生成最后一条消息
   * @description 删除最后一条 AI 消息并重新发送最后一条用户消息
   * @param options 重新生成选项（深度思考开关 / 预算，与 sendMessage 一致；省略则沿用后端配置）
   * @returns 是否重新生成成功
   * @example regenerateLastMessage()
   */
  const regenerateLastMessage = (options?: { thinkingEnabled?: boolean; thinkingBudgetTokens?: number }): boolean => {
    if (!currentSessionId.value) return false

    const session = sessions.value.get(currentSessionId.value)
    if (!session || session.messages.length < 2) return false

    // 移除最后一条助手消息
    if (session.messages[session.messages.length - 1].role === 'assistant') {
      session.messages.pop()
    }

    // 获取最后一条用户消息
    const lastUserMessage = [...session.messages].reverse().find((m) => m.role === 'user')
    if (!lastUserMessage) {
      return false
    }

    // 添加一个空的助手消息，用于接收流式内容
    const assistantMessage: AiChatMessage = {
      id: '',
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      status: 'sending'
    }
    session.messages.push(assistantMessage)

    // 使用 sessionId 作为临时标识
    streamingMessageId.value = currentSessionId.value
    streamContentBuffer.value = ''
    streamReasoningBuffer.value = ''

    // 更新会话时间
    session.updatedAt = Date.now()

    // 通过 WebSocket 重新发送消息（不添加新的用户消息）
    // modelName 仅在会话已绑定具体模型时才下发，与首次发送保持一致
    const request: Record<string, unknown> = {
      type: 'ai_chat',
      sessionId: currentSessionId.value,
      message: lastUserMessage.content,
      provider: session.provider || 'deepseek',
      mode: 'CONTINUOUS'
    }
    if (session.modelName) {
      request.modelName = session.modelName
    }
    applyThinkingOptions(request, options)

    const success = webSocket.send(request)
    if (!success) {
      assistantMessage.status = 'error'
      assistantMessage.error = 'WebSocket 未连接，无法发送消息'
      streamingMessageId.value = null
      return false
    }

    return true
  }

  // ==================== 工具方法 ====================

  /**
   * 把深度思考选项写入 WS 请求体（仅在显式给出时下发，否则交给后端按配置决定）
   * @param request 请求体
   * @param options 含 thinkingEnabled / thinkingBudgetTokens 的选项
   */
  const applyThinkingOptions = (request: Record<string, unknown>, options?: { thinkingEnabled?: boolean; thinkingBudgetTokens?: number }): void => {
    if (!options) return
    if (typeof options.thinkingEnabled === 'boolean') {
      request.thinkingEnabled = options.thinkingEnabled
    }
    if (typeof options.thinkingBudgetTokens === 'number' && options.thinkingBudgetTokens > 0) {
      request.thinkingBudgetTokens = options.thinkingBudgetTokens
    }
  }

  /**
   * 生成会话 ID
   * @returns 唯一的会话标识符
   */
  const generateSessionId = (): string => {
    return `session_${Date.now()}_${Math.random().toString(36).substring(7)}`
  }

  /**
   * 生成消息 ID
   * @returns 唯一的消息标识符
   */
  const generateMessageId = (): string => {
    return `msg_${Date.now()}_${Math.random().toString(36).substring(7)}`
  }

  // ==================== 返回 ====================

  return {
    // 状态
    sessions,
    currentSessionId,
    streamingMessageId,

    // 计算属性
    currentSession,
    currentMessages,
    isGenerating,
    sessionList,

    // 会话管理
    createSession,
    switchSession,
    deleteSession,
    clearAllSessions,
    updateSessionTitle,

    // 消息管理
    sendMessage,
    onChatStart,
    appendStreamContent,
    appendStreamReasoning,
    onChatComplete,
    onChatError,
    regenerateLastMessage
  }
})
