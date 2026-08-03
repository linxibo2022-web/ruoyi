<!-- AI对话按钮 -->
<template>
  <!-- Navbar 工具栏按钮 - 仅当 langchain4j 启用时显示 -->
  <el-tooltip v-if="featureStore.features.langchain4jEnabled" :content="t('navbar.aiChat')" effect="dark" placement="bottom" :offset="6">
    <div class="flex justify-center items-center h-full px-1">
      <div class="navbar-tool-item flex justify-center items-center w-9 h-9 rounded-2 cursor-pointer" @click="handleOpenChat">
        <Icon code="robot" size="md" animate="shake" />
        <!-- 正在生成中的指示器 -->
        <span v-if="isGenerating" class="generating-indicator"></span>
      </div>
    </div>
  </el-tooltip>

  <!-- AI 聊天抽屉 -->
  <el-drawer
    v-model="drawerVisible"
    direction="rtl"
    size="70%"
    :title="t('navbar.aiAssistant.title')"
    :z-index="2000"
    :show-footer="false"
    append-to-body
    close-on-click-modal
    :destroy-on-close="false"
    class="ai-chat-drawer"
  >
    <div class="ai-chat-container">
      <!-- 会话列表侧边栏 -->
      <div class="session-sidebar">
        <div class="sidebar-header">
          <el-button type="primary" size="small" @click="handleCreateSession"> <Icon code="add-plus" class="mr-1" /> {{ t('navbar.aiAssistant.newChat') }} </el-button>
        </div>

        <div class="session-list">
          <div
            v-for="session in sessionList"
            :key="session.id"
            :class="['session-item', { active: session.id === currentSessionId }]"
            @click="handleSwitchSession(session.id)"
          >
            <div class="session-content">
              <div class="session-title">{{ session.title }}</div>
              <div class="session-time">{{ formatTime(session.updatedAt) }}</div>
            </div>
            <el-button type="danger" size="small" text circle class="btn-delete" @click.stop="handleDeleteSession(session.id)">
              <Icon code="delete" size="sm" />
            </el-button>
          </div>

          <!-- 空状态 -->
          <div v-if="sessionList.length === 0" class="session-empty">
            <Icon code="chat" size="lg" />
            <p>{{ t('navbar.aiAssistant.noHistory') }}</p>
          </div>
        </div>
      </div>

      <!-- 聊天主区域 -->
      <div class="chat-main">
        <!-- 顶部工具栏 -->
        <div class="chat-header">
          <div class="header-left">
            <h2>{{ currentSession?.title || t('navbar.aiAssistant.startNewChat') }}</h2>
            <span v-if="currentSession" class="model-badge">
              <Icon code="chip" size="sm" />
              {{ currentSession.provider }}<template v-if="currentSession.modelName"> · {{ currentSession.modelName }}</template>
            </span>
          </div>
          <div class="header-right">
            <el-button size="small" @click="handleClearMessages"> <Icon code="delete" class="mr-1" /> {{ t('navbar.aiAssistant.clearMessages') }} </el-button>
            <el-button size="small" @click="showSettings = !showSettings"> <Icon code="settings" class="mr-1" /> {{ t('navbar.aiAssistant.settings') }} </el-button>
          </div>
        </div>

        <!-- 消息列表容器 -->
        <div class="message-container">
          <div class="message-list" ref="messageListRef">
            <!-- 空状态 -->
            <div v-if="currentMessages.length === 0" class="empty-state">
              <div class="empty-icon">
                <Icon code="robot" size="60px" />
              </div>
              <h3 class="empty-title">{{ t('navbar.aiAssistant.startChatTitle') }}</h3>
              <p class="empty-desc">{{ t('navbar.aiAssistant.startChatDesc') }}</p>
            </div>

            <!-- 消息列表 -->
            <div v-for="message in currentMessages" :key="message.id" :class="['message-item', `message-${message.role}`]">
              <div class="message-avatar">
                <div v-if="message.role === 'user'" class="avatar-image">
                  <img :src="userAvatar" alt="用户头像" />
                </div>
                <div v-else class="avatar-icon">
                  <Icon code="robot" size="lg" />
                </div>
              </div>

              <div class="message-body">
                <div class="message-header">
                  <span class="message-role">{{ message.role === 'user' ? t('navbar.aiAssistant.you') : t('navbar.aiAssistant.assistant') }}</span>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>

                <!-- 深度思考过程（推理模型返回 reasoningContent 时显示，可折叠） -->
                <div v-if="message.role === 'assistant' && message.reasoningContent" class="thinking-block">
                  <div class="thinking-header" @click="toggleThinking(message)">
                    <Icon v-if="!message.reasoningFinished" code="loading" size="sm" class="rotating thinking-icon" />
                    <Icon v-else code="light-bulb" size="sm" class="thinking-icon" />
                    <span class="thinking-title">
                      {{ message.reasoningFinished ? t('navbar.aiAssistant.thinkingDone') : t('navbar.aiAssistant.thinkingInProgress') }}
                    </span>
                    <span class="thinking-spacer"></span>
                    <el-tooltip
                      :content="isThinkingExpanded(message) ? t('navbar.aiAssistant.collapseThinking') : t('navbar.aiAssistant.expandThinking')"
                      placement="top"
                      effect="light"
                      :show-arrow="false"
                      :offset="4"
                    >
                      <Icon :code="isThinkingExpanded(message) ? 'up' : 'down'" size="sm" class="thinking-arrow" />
                    </el-tooltip>
                  </div>
                  <el-collapse-transition>
                    <div v-show="isThinkingExpanded(message)" class="thinking-content">
                      <div class="content-text thinking-text" v-html="renderMarkdown(message.reasoningContent)"></div>
                    </div>
                  </el-collapse-transition>
                </div>

                <!-- 消息内容区域（思考流式进行中且尚无最终回复时，仅展示上方思考面板，不渲染空气泡） -->
                <div
                  class="message-content"
                  v-if="
                    message.content ||
                    message.error ||
                    editingMessageId === message.id ||
                    (message.role === 'assistant' && !(message.reasoningContent && !message.reasoningFinished))
                  "
                >
                  <!-- 编辑模式 -->
                  <div v-if="editingMessageId === message.id" class="edit-mode">
                    <el-input
                      :ref="(el) => setEditInputRef(el, message.id)"
                      v-model="editingContent"
                      type="textarea"
                      :rows="4"
                      :placeholder="t('navbar.aiAssistant.editPlaceholder')"
                      @keydown.enter.ctrl="handleSaveEdit"
                      class="edit-textarea"
                    />
                    <div class="edit-actions">
                      <el-button size="small" @click="handleCancelEdit">{{ t('navbar.aiAssistant.cancel') }}</el-button>
                      <el-button type="primary" size="small" @click="handleSaveEdit" :disabled="!editingContent.trim()"> {{ t('navbar.aiAssistant.saveAndResend') }} </el-button>
                    </div>
                  </div>

                  <!-- 正常显示模式 -->
                  <div v-else>
                    <!-- AI 消息生成中状态：显示"生成中"（思考流式进行中时由思考面板代为提示，这里不重复） -->
                    <div
                      v-if="
                        !message.content &&
                        !(message.reasoningContent && !message.reasoningFinished) &&
                        (message.status === 'streaming' || message.status === 'sending')
                      "
                      class="generating-text"
                    >
                      <Icon code="loading" class="rotating mr-1" /> {{ t('navbar.aiAssistant.generating') }}
                    </div>

                    <!-- 用户消息：纯文本显示 -->
                    <div v-else-if="message.role === 'user'" class="content-text user-content">
                      {{ message.content }}
                    </div>

                    <!-- AI 消息：Markdown 渲染 -->
                    <div v-else-if="message.content" class="content-text" v-html="renderMarkdown(message.content)"></div>
                  </div>

                  <!-- 错误信息 -->
                  <div v-if="message.error" class="message-error">
                    <Icon code="error" size="sm" />
                    {{ message.error }}
                  </div>
                </div>

                <!-- Token 使用情况 + 操作按钮 -->
                <div class="message-footer">
                  <!-- 操作按钮 -->
                  <div class="message-actions">
                    <!-- 用户消息:编辑按钮 -->
                    <template v-if="message.role === 'user'">
                      <el-tooltip :content="t('navbar.aiAssistant.copy')" placement="bottom" effect="light" :show-arrow="false" :offset="4">
                        <el-button size="small" text @click="handleCopyMessage(message.content)" class="action-btn">
                          <Icon code="copy" size="sm" />
                        </el-button>
                      </el-tooltip>
                      <el-tooltip :content="t('navbar.aiAssistant.edit')" placement="bottom" effect="light" :show-arrow="false" :offset="4">
                        <el-button size="small" text @click="handleEditMessage(message)" class="action-btn" :disabled="isGenerating">
                          <Icon code="edit" size="sm" />
                        </el-button>
                      </el-tooltip>
                    </template>

                    <!-- AI 消息:复制和重新生成 -->
                    <template v-if="message.role === 'assistant' && message.status === 'complete'">
                      <el-tooltip :content="t('navbar.aiAssistant.copy')" placement="bottom" effect="light" :show-arrow="false" :offset="4">
                        <el-button size="small" text @click="handleCopyMessage(message.content)" class="action-btn">
                          <Icon code="copy" size="sm" />
                        </el-button>
                      </el-tooltip>
                      <el-tooltip
                        v-if="isLastAssistantMessage(message)"
                        :content="t('navbar.aiAssistant.regenerate')"
                        placement="bottom"
                        effect="light"
                        :show-arrow="false"
                        :offset="4"
                      >
                        <el-button size="small" text @click="handleRegenerate" class="action-btn">
                          <Icon code="refresh" size="sm" />
                        </el-button>
                      </el-tooltip>
                    </template>
                  </div>

                  <!-- Token 使用情况 -->
                  <div v-if="message.tokenUsage" class="token-usage">
                    <Icon code="chip" size="sm" />
                    <span>{{ t('navbar.aiAssistant.tokenInput') }} {{ message.tokenUsage.promptTokens }}</span>
                    <span>{{ t('navbar.aiAssistant.tokenOutput') }} {{ message.tokenUsage.completionTokens }}</span>
                    <span>{{ t('navbar.aiAssistant.tokenTotal') }} {{ message.tokenUsage.totalTokens }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 高级设置 -->
          <el-collapse-transition>
            <div v-if="showSettings" class="settings-panel">
              <el-form :model="settingsForm" label-width="100px" size="small">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item :label="t('navbar.aiAssistant.modelProvider')">
                      <el-select v-model="settingsForm.provider" :placeholder="t('navbar.aiAssistant.selectProvider')">
                        <el-option label="DeepSeek" value="deepseek" />
                        <el-option label="OpenAI" value="openai" />
                        <el-option label="Claude" value="claude" />
                        <el-option :label="t('ai.assistant.providers.qianwen')" value="qianwen" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="Temperature">
                      <el-slider v-model="settingsForm.temperature" :min="0" :max="2" :step="0.1" show-input :show-input-controls="false" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="Max Tokens">
                      <el-input-number v-model="settingsForm.maxTokens" :min="100" :max="8192" :step="100" controls-position="right" />
                    </el-form-item>
                  </el-col>
                  <!-- 深度思考档位：仅当后端开启了深度思考时显示 -->
                  <el-col v-if="thinkingAvailable" :span="12">
                    <el-form-item :label="t('navbar.aiAssistant.deepThinking')">
                      <el-select v-model="settingsForm.thinkingLevel">
                        <el-option :label="t('navbar.aiAssistant.thinkingLevelStandard')" value="STANDARD" />
                        <el-option :label="t('navbar.aiAssistant.thinkingLevelDeep')" value="DEEP" />
                        <el-option :label="t('navbar.aiAssistant.thinkingLevelOff')" value="OFF" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </el-collapse-transition>

          <div class="input-box">
            <el-input
              ref="inputRef"
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              :autosize="{ minRows: 3, maxRows: 10 }"
              :placeholder="t('navbar.aiAssistant.inputPlaceholder')"
              :disabled="isGenerating"
              @keydown.enter.exact.prevent="handleSendMessage"
              class="input-textarea"
            />

            <el-button
              type="primary"
              size="large"
              :disabled="!inputMessage.trim() || isGenerating"
              :loading="isGenerating"
              @click="handleSendMessage"
              class="btn-send"
            >
              <Icon code="send" class="mr-1" /> {{ t('navbar.aiAssistant.send') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts" name="AiChat">
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { showConfirm, showMsgError, showMsgSuccess } from '@/utils/modal'
import { webSocket } from '@/composables/useWS'

const { t } = useI18n()

// Store
const aiChatStore = useAiChatStore()
const { currentSessionId, currentSession, currentMessages, isGenerating, sessionList } = storeToRefs(aiChatStore)
// 获取功能配置 Store
const featureStore = useFeatureStore()
// 获取用户信息 Store
const userStore = useUserStore()

// 用户头像
const userAvatar = computed(() => userStore.userInfo?.avatar)

// 抽屉显示状态
const drawerVisible = ref(false)

// 输入相关
const inputMessage = ref('')
const showSettings = ref(false)
const settingsForm = ref({
  provider: 'deepseek',
  temperature: 0.7,
  maxTokens: 2048,
  // 深度思考档位：OFF=本次关闭 STANDARD=标准（用后端预算） DEEP=深度（加大预算）
  // 仅在后端开启了深度思考时该控件才出现；默认走标准档
  thinkingLevel: 'STANDARD' as 'OFF' | 'STANDARD' | 'DEEP'
})

/** 深度思考是否可用（后端启用且至少一个 provider 开了 enableThinking） */
const thinkingAvailable = computed(() => featureStore.features.langchain4jThinkingEnabled === true)

/** 深度档位对应的预算 token 数 */
const DEEP_THINKING_BUDGET = 16384

/**
 * 把当前「深度思考」档位转换为 sendMessage / regenerateLastMessage 的选项
 * - 后端未开启思考时不下发任何字段（交给后端配置）
 * - OFF：thinkingEnabled=false（临时关闭本次思考）
 * - STANDARD：thinkingEnabled=true（用后端配置的预算）
 * - DEEP：thinkingEnabled=true + 加大预算
 */
const resolveThinkingOptions = (): { thinkingEnabled?: boolean; thinkingBudgetTokens?: number } => {
  if (!thinkingAvailable.value) return {}
  switch (settingsForm.value.thinkingLevel) {
    case 'OFF':
      return { thinkingEnabled: false }
    case 'DEEP':
      return { thinkingEnabled: true, thinkingBudgetTokens: DEEP_THINKING_BUDGET }
    case 'STANDARD':
    default:
      return { thinkingEnabled: true }
  }
}

// 编辑相关
const editingMessageId = ref<string | null>(null)
const editingContent = ref('')
const editInputRefs = new Map<string, any>()

// 消息列表引用
const messageListRef = ref<HTMLElement>()
// 输入框引用
const inputRef = ref()
// 是否允许自动滚动
const allowAutoScroll = ref(true)

// 设置编辑输入框的 ref
const setEditInputRef = (el: any, messageId: string) => {
  if (el) {
    editInputRefs.set(messageId, el)
  } else {
    editInputRefs.delete(messageId)
  }
}

// ========== 深度思考面板 ==========
// 用户手动控制的展开状态（messageId -> boolean）；未设置时按"思考是否结束"决定默认值
const thinkingExpandedMap = ref<Record<string, boolean>>({})

// 思考面板是否展开：默认思考进行中展开、结束后收起；用户点击后以用户选择为准
const isThinkingExpanded = (message: any): boolean => {
  if (message.id && message.id in thinkingExpandedMap.value) {
    return thinkingExpandedMap.value[message.id]
  }
  return !message.reasoningFinished
}

// 切换思考面板展开/收起
const toggleThinking = (message: any) => {
  if (!message.id) return
  thinkingExpandedMap.value[message.id] = !isThinkingExpanded(message)
}

// 检查 WebSocket 连接
const checkWebSocketConnection = () => {
  if (!webSocket.isConnected) {
    showMsgError(t('navbar.aiAssistant.message.wsNotConnected'))
    return false
  }
  return true
}

// 打开聊天
const handleOpenChat = () => {
  if (!checkWebSocketConnection()) {
    return
  }

  drawerVisible.value = true

  // 如果没有会话,创建一个
  if (sessionList.value.length === 0) {
    aiChatStore.createSession({
      title: `${t('navbar.aiAssistant.newSessionTitle')} ${new Date().toLocaleString()}`,
      provider: settingsForm.value.provider
    })
  }
}

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return t('navbar.aiAssistant.time.justNow')
  if (diff < 3600000) return t('navbar.aiAssistant.time.minutesAgo').replace('{n}', String(Math.floor(diff / 60000)))
  if (diff < 86400000) return date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

// 渲染 Markdown
const renderMarkdown = (content: string) => {
  if (!content) return ''
  return DOMPurify.sanitize(marked(content) as string)
}

// 获取状态类型
const getStatusType = (status: string): 'success' | 'info' | 'warning' | 'danger' => {
  const typeMap: Record<string, 'success' | 'info' | 'warning' | 'danger'> = {
    sending: 'info',
    streaming: 'warning',
    complete: 'success',
    error: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    sending: t('navbar.aiAssistant.status.sending'),
    streaming: t('navbar.aiAssistant.status.streaming'),
    complete: t('navbar.aiAssistant.status.complete'),
    error: t('navbar.aiAssistant.status.error')
  }
  return statusMap[status] || status
}

// 判断是否是最后一条助手消息
const isLastAssistantMessage = (message: any) => {
  const assistantMessages = currentMessages.value.filter((m) => m.role === 'assistant')
  return assistantMessages.length > 0 && assistantMessages[assistantMessages.length - 1].id === message.id
}

// 滚动到底部
const scrollToBottom = () => {
  if (!allowAutoScroll.value) return

  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 监听消息变化,自动滚动
watch(
  currentMessages,
  () => {
    scrollToBottom()
  },
  { deep: true }
)

// 处理发送消息
const handleSendMessage = () => {
  if (!inputMessage.value.trim() || isGenerating.value) return

  if (!checkWebSocketConnection()) {
    return
  }

  // 发送消息时允许自动滚动
  allowAutoScroll.value = true

  aiChatStore.sendMessage(inputMessage.value, {
    provider: settingsForm.value.provider,
    temperature: settingsForm.value.temperature,
    maxTokens: settingsForm.value.maxTokens,
    ...resolveThinkingOptions()
  })

  inputMessage.value = ''
  scrollToBottom()
}

// 处理创建会话
const handleCreateSession = () => {
  aiChatStore.createSession({
    title: `${t('navbar.aiAssistant.newSessionTitle')} ${new Date().toLocaleString()}`,
    provider: settingsForm.value.provider
  })
  showMsgSuccess(t('navbar.aiAssistant.message.sessionCreated'))
}

// 处理切换会话
const handleSwitchSession = (sessionId: string) => {
  aiChatStore.switchSession(sessionId)
}

// 处理删除会话
const handleDeleteSession = async (sessionId: string) => {
  const [err] = await showConfirm(t('navbar.aiAssistant.message.confirmDeleteSession'))
  if (err) return

  aiChatStore.deleteSession(sessionId)
  showMsgSuccess(t('navbar.aiAssistant.message.sessionDeleted'))
}

// 处理清空消息
const handleClearMessages = async () => {
  if (!currentSessionId.value) return

  const session = currentSession.value
  const [err] = await showConfirm(t('navbar.aiAssistant.message.confirmClearMessages'))
  if (err) return

  if (session) {
    session.messages = []
    showMsgSuccess(t('navbar.aiAssistant.message.messagesCleared'))
  }
}

// 处理重新生成
const handleRegenerate = () => {
  // 重新生成时允许自动滚动
  allowAutoScroll.value = true
  aiChatStore.regenerateLastMessage(resolveThinkingOptions())
}

// 处理复制消息
const handleCopyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    showMsgSuccess(t('navbar.aiAssistant.message.copiedToClipboard'))
  } catch {
    showMsgError(t('navbar.aiAssistant.message.copyFailed'))
  }
}

// 处理编辑消息 - 进入编辑模式
const handleEditMessage = (message: any) => {
  if (isGenerating.value) return

  // 禁用自动滚动
  allowAutoScroll.value = false

  editingMessageId.value = message.id
  editingContent.value = message.content

  // 聚焦到编辑输入框
  nextTick(() => {
    const editInput = editInputRefs.get(message.id)
    if (editInput) {
      const textareaEl = editInput.$el?.querySelector('textarea')
      if (textareaEl) {
        // 使用 setTimeout 确保在下一个事件循环中执行，避免被其他逻辑打断
        setTimeout(() => {
          textareaEl.focus({ preventScroll: true })
        }, 0)
      }
    }
  })
}

// 处理取消编辑
const handleCancelEdit = () => {
  editingMessageId.value = null
  editingContent.value = ''
  // 恢复自动滚动
  allowAutoScroll.value = true
}

// 处理保存编辑并重新发送
const handleSaveEdit = () => {
  if (!editingContent.value.trim()) return
  if (!checkWebSocketConnection()) return

  const session = currentSession.value
  if (!session) return

  // 找到正在编辑的消息的索引
  const messageIndex = session.messages.findIndex((m) => m.id === editingMessageId.value)
  if (messageIndex === -1) return

  // 删除该消息及之后的所有消息
  session.messages = session.messages.slice(0, messageIndex)

  // 退出编辑模式
  editingMessageId.value = null
  const contentToSend = editingContent.value
  editingContent.value = ''

  // 恢复自动滚动
  allowAutoScroll.value = true

  // 重新发送消息
  aiChatStore.sendMessage(contentToSend, {
    provider: settingsForm.value.provider,
    temperature: settingsForm.value.temperature,
    maxTokens: settingsForm.value.maxTokens,
    ...resolveThinkingOptions()
  })

  scrollToBottom()
}
</script>

<style lang="scss">
.el-drawer.ai-chat-drawer {
  .el-drawer__header {
    margin-bottom: 0 !important;
  }

  .el-drawer__body {
    padding: 0 !important;
  }
}
</style>

<style scoped lang="scss">
// ========== 生成中指示器 ==========
.generating-indicator {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: var(--el-color-danger);
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

// ========== AI 聊天容器 ==========
.ai-chat-container {
  display: flex;
  height: 100%;
  background: var(--el-bg-color-page);
}

// ========== 会话侧边栏 ==========
.session-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-lighter);

  .sidebar-header {
    padding: 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    flex-shrink: 0;

    h3 {
      margin: 0 0 12px 0;
      font-size: 15px;
      color: var(--el-text-color-primary);
    }

    .el-button {
      width: 100%;
    }
  }

  .session-list {
    flex: 1;
    overflow-y: auto;
    padding: 12px 8px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: 3px;
    }
  }

  .session-item {
    display: flex;
    align-items: center;
    padding: 12px;
    margin-bottom: 6px;
    background: var(--el-fill-color-lighter);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--el-fill-color);
      transform: translateX(2px);

      .btn-delete {
        opacity: 1;
      }
    }

    &.active {
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary);

      .session-title {
        color: var(--el-color-primary);
      }
    }

    .session-content {
      flex: 1;
      min-width: 0;
      padding-right: 8px;
    }

    .session-title {
      font-size: 14px;
      margin-bottom: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: var(--el-text-color-primary);
      transition: all 0.2s;
    }

    .session-time {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .btn-delete {
      opacity: 0;
      transition: opacity 0.2s;
      flex-shrink: 0;
    }
  }

  .session-empty {
    padding: 60px 20px;
    text-align: center;
    color: var(--el-text-color-secondary);

    p {
      margin: 12px 0 0 0;
      font-size: 13px;
    }
  }
}

// ========== 聊天主区域 ==========
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg-level-2);
}

// 顶部工具栏
.chat-header {
  padding: 14px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    flex: 1;
    min-width: 0;

    h2 {
      margin: 0 0 6px 0;
      font-size: 16px;
      color: var(--el-text-color-primary);
    }

    .model-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 14px;
      background: var(--el-fill-color-light);
      border-radius: 7px;
      font-size: 12px;
      color: var(--el-text-color-regular);
    }
  }

  .header-right {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

// ========== 消息容器 ==========
.message-container {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  position: relative;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 24px;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--el-border-color);
    border-radius: 4px;
    transition: background 0.2s;

    &:hover {
      background: var(--el-border-color-dark);
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  padding: 60px 40px;

  .empty-icon {
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, var(--el-color-primary-light-8) 100%);
    border-radius: 50%;
    margin-bottom: 32px;
    color: var(--el-color-primary);
    font-size: 48px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    animation: floatIcon 3s ease-in-out infinite;
  }

  @keyframes floatIcon {
    0%,
    100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-10px);
    }
  }

  .empty-title {
    margin: 0 0 12px 0;
    font-size: 24px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .empty-desc {
    margin: 0;
    font-size: 15px;
    color: var(--el-text-color-secondary);
  }
}

// ========== 消息项 ==========
.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 15px;
  animation: messageSlideIn 0.3s ease-out;

  @keyframes messageSlideIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .message-avatar {
    flex-shrink: 0;
    padding-top: 4px;

    .avatar-icon {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
    }

    .avatar-image {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }

  // 用户消息样式：头像在右，内容右对齐
  &.message-user {
    flex-direction: row-reverse;

    .message-body {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }

    .message-header {
      justify-content: flex-end;
    }
  }

  // AI 助手消息样式
  &.message-assistant {
    .message-avatar .avatar-icon {
      background: linear-gradient(135deg, var(--el-color-primary-light-8) 0%, var(--el-color-primary) 100%);
      color: white;
    }
  }

  .message-body {
    flex: 1;
    min-width: 0;
    position: relative;
  }

  .message-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .message-role {
      font-weight: 600;
      font-size: 14px;
      color: var(--el-text-color-primary);
    }

    .message-time {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  // ========== 深度思考面板 ==========
  .thinking-block {
    max-width: 94%;
    margin-bottom: 8px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    background: var(--el-fill-color-lighter);
    overflow: hidden;

    .thinking-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 14px;
      cursor: pointer;
      user-select: none;
      font-size: 13px;
      color: var(--el-text-color-secondary);
      transition: background 0.2s;

      &:hover {
        background: var(--el-fill-color);
      }

      .thinking-icon {
        color: var(--el-color-warning);
        flex-shrink: 0;
      }

      .thinking-title {
        font-weight: 500;
      }

      .thinking-spacer {
        flex: 1;
      }

      .thinking-arrow {
        color: var(--el-text-color-secondary);
        flex-shrink: 0;
      }
    }

    .thinking-content {
      padding: 6px 14px 12px 14px;
      border-top: 1px dashed var(--el-border-color-lighter);

      .thinking-text {
        font-size: 13px;
        color: var(--el-text-color-secondary);
        line-height: 1.7;

        :deep(p) {
          margin: 0 0 8px 0;

          &:last-child {
            margin-bottom: 0;
          }
        }

        :deep(pre) {
          background: var(--el-fill-color);
          padding: 12px;
          border-radius: 6px;
          overflow-x: auto;
          margin: 8px 0;
        }

        :deep(code) {
          background: var(--el-fill-color);
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 12px;
        }
      }
    }
  }

  .message-content {
    position: relative;
    padding: 10px 16px;
    background: var(--el-bg-color);
    border-radius: 10px;
    line-height: 1.8;
    word-wrap: break-word;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--el-border-color-lighter);
    transition: all 0.2s;
    /* 默认显示模式宽度限制 */
    max-width: 94%;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    /* 编辑模式下，让容器变宽 */
    &:has(.edit-mode) {
      width: 90%;
    }

    .status-tag {
      position: absolute;
      top: 16px;
      right: 20px;
    }

    // 编辑模式样式
    .edit-mode {
      /* 让编辑模式占据更宽的空间 */
      width: 100%;
      position: relative;

      .edit-textarea {
        width: 100%;

        :deep(.el-textarea__inner) {
          resize: none;
          border-radius: 8px;
          padding: 12px 12px 52px 12px; /* 底部留出按钮空间 */
          line-height: 1.6;
          font-size: 14px;
          border: 1px solid var(--el-color-primary);
          width: 100%;

          &:focus {
            border-color: var(--el-color-primary);
            box-shadow: 0 0 0 3px var(--el-color-primary-light-9);
          }
        }
      }

      .edit-actions {
        position: absolute;
        bottom: 8px;
        right: 12px;
        display: flex;
        gap: 8px;
        z-index: 1;
      }
    }

    .content-text {
      font-size: 14px;
      color: var(--el-text-color-primary);

      /* 用户消息纯文本样式 */
      &.user-content {
        white-space: pre-wrap; /* 保留换行和空格 */
        word-break: break-word; /* 长单词换行 */
      }

      :deep(p) {
        margin: 0 0 12px 0;

        &:last-child {
          margin-bottom: 0;
        }
      }

      :deep(pre) {
        background: var(--el-fill-color-dark);
        padding: 16px;
        border-radius: 8px;
        overflow-x: auto;
        margin: 12px 0;

        code {
          background: none;
          padding: 0;
          color: var(--el-text-color-primary);
        }
      }

      :deep(code) {
        background: var(--el-fill-color-light);
        padding: 3px 8px;
        border-radius: 4px;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 13px;
        color: var(--el-color-danger);
      }

      :deep(ul),
      :deep(ol) {
        padding-left: 24px;
        margin: 12px 0;

        li {
          margin: 4px 0;
        }
      }

      :deep(blockquote) {
        border-left: 3px solid var(--el-color-primary);
        padding-left: 16px;
        margin: 12px 0;
        color: var(--el-text-color-secondary);
      }

      :deep(h1),
      :deep(h2),
      :deep(h3),
      :deep(h4) {
        margin: 16px 0 8px 0;
        font-weight: 600;
      }

      :deep(a) {
        color: var(--el-color-primary);
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
      }
    }

    .generating-text {
      display: flex;
      align-items: center;
      color: var(--el-color-warning);
      font-size: 14px;
    }

    .message-error {
      margin-top: 12px;
      padding: 12px 16px;
      background: var(--el-color-danger-light-9);
      color: var(--el-color-danger);
      border-radius: 8px;
      font-size: 13px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  // message-footer 在气泡外
  .message-footer {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0;
    /* 让 footer 宽度跟随 message-content */
    max-width: 94%;

    .token-usage {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 4px 10px;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-left: auto; /* 推到右侧 */

      span {
        &:not(:last-child)::after {
          content: '·';
          margin-left: 8px;
        }
      }
    }

    .message-actions {
      display: flex;
      gap: 6px;

      .action-btn {
        width: 28px;
        height: 28px;
        padding: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 6px;
        color: var(--el-text-color-secondary);
        transition: all 0.2s;

        &:hover {
          color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
    }
  }
}

// ========== 旋转动画 ==========
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.rotating {
  display: inline-block;
  animation: rotate 1s linear infinite;
}

// ========== 输入区域 ==========
.input-area {
  flex-shrink: 0;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);

  .settings-panel {
    padding: 20px 24px;
    background: var(--el-fill-color-lighter);
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .input-box {
    padding: 20px 24px;
    display: flex;
    gap: 12px;
    align-items: flex-end;

    .input-textarea {
      flex: 1;
      width: 100%;

      :deep(.el-textarea__inner) {
        resize: none;
        border-radius: 8px;
        padding: 12px 16px;
        line-height: 1.5;
        transition: all 0.2s;

        &:focus {
          border-color: var(--el-color-primary);
        }

        &::placeholder {
          color: var(--el-text-color-placeholder);
        }
      }
    }

    .btn-send {
      height: 32px;
      padding: 0 24px;
      border-radius: 8px;
      font-size: 14px;
    }
  }
}
</style>
