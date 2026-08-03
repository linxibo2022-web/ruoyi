<!--
  AAiAssistant AI辅助组件

  使用示例：

  1. 文本优化模式
  <AAiAssistant
    v-model="form.description"
    mode="optimize"
    field="description"
    :context="form"
  />

  2. 数据生成模式
  <AAiAssistant
    mode="generate"
    :schema="fieldSchema"
    @generated="handleGenerated"
  />

  3. 内联按钮模式
  <el-input v-model="form.adName">
    <template #append>
      <AAiAssistant
        v-model="form.adName"
        mode="optimize"
        trigger="button"
        size="small"
      />
    </template>
  </el-input>

  4. 内容审核模式
  <AAiAssistant
    mode="review"
    :content="form"
    :rules="reviewRules"
    @review-complete="handleReview"
  />

  5. 完整配置示例
  <AAiAssistant
    v-model="form.description"
    mode="optimize"
    field="description"
    field-type="description"
    :context="form"
    :provider="aiConfig.provider"
    :model-name="aiConfig.model"
    role="专业的广告文案优化助手"
    :system-prompt="customPrompt"
    :features="{
      optimize: true,
      generate: true,
      translate: true
    }"
    trigger="icon"
    position="popup"
    size="medium"
    :session-id="sessionId"
    :max-history="10"
    @result="handleResult"
    @error="handleError"
  />
-->

<template>
  <div class="ai-assistant-wrapper">
    <!-- 触发器 - 按钮模式 -->
    <el-button
      v-if="trigger === 'button' && !hideInContext"
      :type="buttonType"
      :size="size"
      :icon="buttonIcon"
      :loading="isProcessing"
      @click="handleTrigger"
    >
      {{ buttonText }}
    </el-button>

    <!-- 触发器 - 图标模式 -->
    <el-tooltip v-if="trigger === 'icon' && !hideInContext" :content="tooltipText" placement="top">
      <el-button link :type="iconType" :size="size" :icon="iconName" :loading="isProcessing" @click="handleTrigger" class="ai-trigger-icon" />
    </el-tooltip>

    <!-- 主弹窗/抽屉 -->
    <AModal
      v-model="dialogVisible"
      :title="dialogTitle"
      :mode="position === 'sidebar' ? 'drawer' : 'dialog'"
      :size="modalSize"
      :width="modalWidth"
      :direction="position === 'sidebar' ? 'rtl' : undefined"
      :closable="true"
      :mask-closable="true"
      :destroy-on-close="false"
      :append-to-body="true"
      :show-footer="false"
      @close="handleClose"
      @opened="handleOpened"
    >
      <!-- AI助手内容区 -->
      <div class="ai-assistant-content">
        <!-- 功能选择器 -->
        <div v-if="showFeatureSelector" class="feature-selector">
          <el-radio-group v-model="currentFeature" size="small">
            <el-radio-button v-if="features.optimize" value="optimize">
              <el-icon><Edit /></el-icon>
              {{ t('ai.assistant.features.optimize') }}
            </el-radio-button>
            <el-radio-button v-if="features.generate" value="generate">
              <el-icon><MagicStick /></el-icon>
              {{ t('ai.assistant.features.generate') }}
            </el-radio-button>
            <el-radio-button v-if="features.review" value="review">
              <el-icon><View /></el-icon>
              {{ t('ai.assistant.features.review') }}
            </el-radio-button>
            <el-radio-button v-if="features.translate" value="translate">
              <el-icon><Switch /></el-icon>
              {{ t('ai.assistant.features.translate') }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 动态加载对应功能组件 -->
        <component
          :is="currentComponent"
          v-bind="componentProps"
          :loading="isProcessing"
          @process="handleProcess"
          @result="handleComponentResult"
          @error="handleComponentError"
        />

        <!-- 历史记录 -->
        <div v-if="showHistory && historyList.length > 0" class="history-section">
          <el-divider content-position="left">
            <el-icon><Clock /></el-icon>
            {{ t('ai.assistant.history') }}
          </el-divider>
          <el-timeline>
            <el-timeline-item v-for="(item, index) in historyList" :key="index" :timestamp="item.timestamp" placement="top">
              <div class="history-item">
                <div class="history-input">{{ t('ai.assistant.input') }}: {{ item.input }}</div>
                <div class="history-output">{{ t('ai.assistant.output') }}: {{ item.output }}</div>
                <el-button link type="primary" size="small" @click="handleUseHistory(item)"> {{ t('ai.assistant.useThisResult') }} </el-button>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 配置面板 -->
        <el-collapse v-if="showSettings" v-model="activeSettings" class="settings-panel">
          <el-collapse-item :title="t('ai.assistant.advancedSettings')" name="advanced">
            <el-form :model="settings" label-width="100px" size="small">
              <el-form-item :label="t('ai.assistant.modelSelect')">
                <el-select v-model="settings.provider" :placeholder="t('ai.assistant.selectProvider')">
                  <el-option label="DeepSeek" value="deepseek" />
                  <el-option label="OpenAI" value="openai" />
                  <el-option :label="t('ai.assistant.providers.qianwen')" value="qianwen" />
                  <el-option label="Claude" value="claude" />
                </el-select>
              </el-form-item>
              <el-form-item :label="t('ai.assistant.creativity')">
                <el-slider v-model="settings.temperature" :min="0" :max="2" :step="0.1" show-input :input-size="'small'" />
              </el-form-item>
              <el-form-item :label="t('ai.assistant.roleSettings')">
                <el-input v-model="settings.role" type="textarea" :rows="2" :placeholder="t('ai.assistant.rolePlaceholder')" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 自定义底部 -->
      <template #footer>
        <div class="ai-assistant-footer">
          <div class="footer-left">
            <el-button link :icon="showSettings ? 'ArrowUp' : 'Setting'" @click="showSettings = !showSettings">
              {{ showSettings ? t('ai.assistant.collapseSettings') : t('ai.assistant.advancedSettings') }}
            </el-button>
          </div>
          <div class="footer-right">
            <el-button @click="handleCancel">{{ t('common.cancel') }}</el-button>
            <el-button v-if="currentResult" type="primary" :loading="isProcessing" @click="handleApply"> {{ t('ai.assistant.applyResult') }} </el-button>
          </div>
        </div>
      </template>
    </AModal>

    <!-- 快速操作气泡 -->
    <el-popover v-if="position === 'inline'" v-model:visible="popoverVisible" placement="bottom" :width="300" trigger="click">
      <template #reference>
        <span ref="popoverTrigger"></span>
      </template>
      <div class="quick-actions">
        <el-button v-if="features.optimize" text :icon="Edit" @click="handleQuickAction('optimize')"> {{ t('ai.assistant.quickActions.optimizeContent') }} </el-button>
        <el-button v-if="features.generate" text :icon="MagicStick" @click="handleQuickAction('generate')"> {{ t('ai.assistant.quickActions.regenerate') }} </el-button>
        <el-button v-if="features.translate" text :icon="Switch" @click="handleQuickAction('translate')"> {{ t('ai.assistant.quickActions.translate') }} </el-button>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts" name="AAiAssistant">
import { Edit, MagicStick, View, Switch, Clock, Setting, ArrowUp } from '@element-plus/icons-vue'
import AAiTextOptimizer from './components/AAiTextOptimizer.vue'
import AAiDataGenerator from './components/AAiDataGenerator.vue'
import AAiContentReviewer from './components/AAiContentReviewer.vue'
import type { AAiAssistantProps, AiFeature, HistoryItem, AiSettings } from './types'
import { showMsgError, showMsgSuccess } from '@/utils/modal'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

/** 组件Props定义 */
const props = withDefaults(defineProps<AAiAssistantProps>(), {
  mode: 'optimize',
  trigger: 'button',
  position: 'popup',
  size: 'default',
  provider: undefined,
  modelName: undefined,
  temperature: 0.7,
  role: undefined,
  systemPrompt: undefined,
  context: () => ({}),
  features: () => ({
    optimize: true,
    generate: true,
    review: false,
    translate: false
  }),
  sessionId: undefined,
  maxHistory: 10,
  showHistory: true,
  autoSave: true
})

/** 组件事件定义 */
const emit = defineEmits<{
  'update:modelValue': [value: string | any]
  'result': [result: any]
  'error': [error: Error]
  'cancel': []
  'process-start': []
  'process-end': []
}>()

/** 双向绑定的值 */
const modelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/** 弹窗显示状态 */
const dialogVisible = ref(false)

/** 气泡显示状态 */
const popoverVisible = ref(false)

/** 处理中状态 */
const isProcessing = ref(false)

/** 当前功能 */
const currentFeature = ref<AiFeature>(props.mode as AiFeature)

/** 当前结果 */
const currentResult = ref<any>(null)

/** 历史记录 */
const historyList = ref<HistoryItem[]>([])

/** 显示设置面板 */
const showSettings = ref(false)

/** 激活的设置面板 */
const activeSettings = ref<string[]>([])

/** 设置数据 */
const settings = ref<AiSettings>({
  provider: props.provider || 'deepseek',
  modelName: props.modelName,
  temperature: props.temperature,
  role: props.role
})

/** 气泡触发器引用 */
const popoverTrigger = ref<HTMLElement>()

/** 计算属性 - 是否在上下文中隐藏 */
const hideInContext = computed(() => {
  return props.trigger === 'auto' && !shouldShowAuto.value
})

/** 计算属性 - 自动显示条件 */
const shouldShowAuto = computed(() => {
  // 这里可以添加智能判断逻辑
  // 例如：输入框为空时显示，内容过短时提示等
  return true
})

/** 计算属性 - 按钮文本 */
const buttonText = computed(() => {
  if (isProcessing.value) return t('ai.assistant.processing')

  const textMap: Record<AiFeature, string> = {
    optimize: t('ai.assistant.buttons.optimize'),
    generate: t('ai.assistant.buttons.generate'),
    review: t('ai.assistant.buttons.review'),
    translate: t('ai.assistant.buttons.translate'),
    suggest: t('ai.assistant.buttons.suggest')
  }
  return textMap[currentFeature.value] || t('ai.assistant.title')
})

/** 计算属性 - 按钮类型 */
const buttonType = computed(() => {
  return props.size === 'small' ? 'default' : 'primary'
})

/** 计算属性 - 按钮图标 */
const buttonIcon = computed(() => {
  return isProcessing.value ? 'Loading' : 'MagicStick'
})

/** 计算属性 - 图标类型 */
const iconType = computed(() => 'primary' as ElButtonType)

/** 计算属性 - 图标名称 */
const iconName = computed(() => {
  return isProcessing.value ? 'Loading' : 'MagicStick'
})

/** 计算属性 - 提示文本 */
const tooltipText = computed(() => {
  return buttonText.value
})

/** 计算属性 - 弹窗标题 */
const dialogTitle = computed(() => {
  const titleMap: Record<AiFeature, string> = {
    optimize: t('ai.assistant.titles.optimize'),
    generate: t('ai.assistant.titles.generate'),
    review: t('ai.assistant.titles.review'),
    translate: t('ai.assistant.titles.translate'),
    suggest: t('ai.assistant.titles.suggest')
  }
  return titleMap[currentFeature.value] || t('ai.assistant.title')
})

/** 计算属性 - 模态框尺寸 */
const modalSize = computed(() => {
  const sizeMap = {
    small: 'small',
    medium: 'medium',
    large: 'large'
  }
  return sizeMap[props.size] || 'medium'
})

/** 计算属性 - 模态框宽度 */
const modalWidth = computed(() => {
  if (props.position === 'sidebar') return '600px'
  return undefined
})

/** 计算属性 - 是否显示功能选择器 */
const showFeatureSelector = computed(() => {
  const enabledFeatures = Object.values(props.features).filter(Boolean)
  return enabledFeatures.length > 1
})

/** 计算属性 - 当前组件 */
const currentComponent = computed(() => {
  const componentMap = {
    optimize: AAiTextOptimizer,
    generate: AAiDataGenerator,
    review: AAiContentReviewer,
    translate: AAiTextOptimizer, // 翻译也使用文本优化器
    suggest: AAiTextOptimizer
  }
  return componentMap[currentFeature.value]
})

/** 计算属性 - 组件属性 */
const componentProps = computed(() => {
  const baseProps = {
    value: modelValue.value,
    field: props.field,
    fieldType: props.fieldType,
    context: props.context,
    provider: settings.value.provider,
    modelName: settings.value.modelName,
    temperature: settings.value.temperature,
    role: settings.value.role,
    systemPrompt: props.systemPrompt,
    sessionId: props.sessionId
  }

  // 根据不同模式添加特定属性
  switch (currentFeature.value) {
    case 'optimize':
      return {
        ...baseProps,
        optimizeType: 'polish'
      }
    case 'generate':
      return {
        ...baseProps,
        schema: props.schema,
        count: props.generateCount
      }
    case 'review':
      return {
        ...baseProps,
        content: props.content,
        rules: props.reviewRules
      }
    case 'translate':
      return {
        ...baseProps,
        optimizeType: 'translate',
        targetLanguage: props.targetLanguage
      }
    default:
      return baseProps
  }
})

/** 处理触发 */
const handleTrigger = () => {
  if (props.position === 'inline') {
    popoverVisible.value = !popoverVisible.value
  } else {
    dialogVisible.value = true
  }
}

/** 处理快速操作 */
const handleQuickAction = (action: AiFeature) => {
  currentFeature.value = action
  popoverVisible.value = false
  dialogVisible.value = true
}

/** 处理处理请求 */
const handleProcess = async (params: any) => {
  isProcessing.value = true
  emit('process-start')

  try {
    // 这里会由子组件实际调用 API
    // 主组件只负责状态管理
  } catch (error) {
    handleComponentError(error as Error)
  }
}

/** 处理组件结果 */
const handleComponentResult = (result: any) => {
  isProcessing.value = false
  currentResult.value = result
  emit('process-end')
  emit('result', result)

  // 保存到历史记录
  if (props.showHistory) {
    addToHistory({
      input: modelValue.value,
      output: result,
      feature: currentFeature.value,
      timestamp: new Date().toLocaleString()
    })
  }

  // 自动应用结果
  if (props.autoSave && typeof result === 'string') {
    modelValue.value = result
    dialogVisible.value = false
  }
}

/** 处理组件错误 */
const handleComponentError = (error: Error) => {
  isProcessing.value = false
  emit('process-end')
  emit('error', error)
  showMsgError(error.message || t('ai.assistant.processFailed'))
}

/** 添加到历史记录 */
const addToHistory = (item: HistoryItem) => {
  historyList.value.unshift(item)
  if (historyList.value.length > props.maxHistory) {
    historyList.value = historyList.value.slice(0, props.maxHistory)
  }
}

/** 使用历史记录 */
const handleUseHistory = (item: HistoryItem) => {
  currentResult.value = item.output
  handleApply()
}

/** 应用结果 */
const handleApply = () => {
  if (currentResult.value) {
    modelValue.value = currentResult.value
    dialogVisible.value = false
    showMsgSuccess(t('ai.assistant.resultApplied'))
  }
}

/** 取消操作 */
const handleCancel = () => {
  dialogVisible.value = false
  currentResult.value = null
  emit('cancel')
}

/** 关闭弹窗 */
const handleClose = () => {
  currentResult.value = null
}

/** 弹窗打开完成 */
const handleOpened = () => {
  // 可以在这里做一些初始化操作
}

/** 暴露给父组件的方法 */
defineExpose({
  /** 打开助手 */
  open: () => {
    dialogVisible.value = true
  },
  /** 关闭助手 */
  close: () => {
    dialogVisible.value = false
  },
  /** 触发处理 */
  process: handleProcess,
  /** 清空历史 */
  clearHistory: () => {
    historyList.value = []
  }
})
</script>

<style scoped>
.ai-assistant-wrapper {
  display: inline-block;
}

.ai-trigger-icon {
  padding: 4px;
  margin-left: 4px;
}

.ai-assistant-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 400px;
}

.feature-selector {
  margin-bottom: 8px;
}

.feature-selector :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.history-item {
  background: var(--el-fill-color-light);
  padding: 12px;
  border-radius: 6px;
}

.history-input {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.history-output {
  font-size: 14px;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.settings-panel {
  margin-top: 16px;
}

.ai-assistant-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.footer-left {
  display: flex;
  gap: 8px;
}

.footer-right {
  display: flex;
  gap: 8px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-actions :deep(.el-button) {
  width: 100%;
  justify-content: flex-start;
}
</style>
