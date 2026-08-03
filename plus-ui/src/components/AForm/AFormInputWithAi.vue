<!--
  AFormInputWithAi - 带 AI 优化功能的表单输入组件

  @description
  在 AFormInput 基础上，集成了 AI 优化功能。
  用户可以通过点击 AI 图标，快速优化输入内容。

  @example
  基础用法：
  <AFormInputWithAi
    label="文章标题"
    v-model="form.title"
    prop="title"
    field-type="title"
  />

  快捷操作按钮组：
  <AFormInputWithAi
    label="备注"
    v-model="form.remark"
    prop="remark"
    :show-quick-actions="true"
    :quick-actions="['polish', 'expand', 'shorten']"
  />

  @author 抓蛙师
  @date 2025-01-26
-->

<template>
  <AFormInput v-bind="inputProps" :model-value="localValue" @update:model-value="handleInput">
    <!-- 传递所有插槽（除了 suffix） -->
    <template v-for="(_, slot) in slotsWithoutSuffix" :key="slot" #[slot]="scope">
      <slot :name="slot" v-bind="scope || {}"></slot>
    </template>

    <!-- AI 功能插槽 -->
    <template #suffix>
      <!-- 用户自定义了 suffix 插槽，则使用用户的 -->
      <slot v-if="$slots.suffix" name="suffix"></slot>

      <!-- 否则显示 AI 功能 -->
      <template v-else>
        <!-- 快捷操作按钮组 -->
        <div v-if="showQuickActions" class="ai-quick-actions">
          <el-tooltip v-for="action in computedQuickActions" :key="action.type" :content="action.label" placement="top">
            <el-button
              link
              :icon="action.icon"
              @click="handleQuickOptimize(action.type)"
              :loading="loading && currentOptimizeType === action.type"
              :disabled="!localValue || loading"
            />
          </el-tooltip>

          <!-- 更多操作按钮 -->
          <el-tooltip :content="t('ai.inputWithAi.moreFeatures')" placement="top">
            <el-button link :icon="More" @click="openAdvancedDialog" :disabled="!localValue || loading" />
          </el-tooltip>
        </div>

        <!-- 单个 AI 图标 -->
        <el-tooltip v-else :content="aiTooltip" placement="top">
          <el-icon class="ai-trigger-icon" :class="{ 'is-loading': loading }" @click="handleOptimize">
            <MagicStick v-if="!loading" />
            <Loading v-else />
          </el-icon>
        </el-tooltip>
      </template>
    </template>
  </AFormInput>

  <!-- 高级 AI 优化弹窗 -->
  <AModal v-model="advancedDialogVisible" :title="t('ai.inputWithAi.textOptimize')" size="large" :show-footer="false">
    <AAiTextOptimizer
      v-model:value="tempValue"
      :field-type="fieldType"
      :provider="aiConfig.provider || 'deepseek'"
      :model-name="aiConfig.modelName"
      :temperature="aiConfig.temperature || 0.7"
      :system-prompt="aiConfig.systemPrompt"
      @result="handleAdvancedResult"
    />
  </AModal>
</template>

<script setup lang="ts" name="AFormInputWithAi">
import { computed, ref, watch, useSlots } from 'vue'
import { ElMessageBox } from 'element-plus'
import { MagicStick, Loading, More, Edit, ZoomIn, ZoomOut, Document, ChatDotRound } from '@element-plus/icons-vue'
import { aiOptimize } from '@/api/business/base/ai/aiApi'
import type { AiChatBo } from '@/api/business/base/ai/aiTypes'
import { showMsgSuccess, showMsgWarning, showMsgError } from '@/utils/modal'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

/** 优化类型 */
export type OptimizeType = 'polish' | 'expand' | 'shorten' | 'formal' | 'casual' | 'translate' | 'custom'

/** 字段类型 */
type FieldType = 'title' | 'description' | 'remark' | 'content' | 'custom'

/** 快捷操作配置 */
interface QuickAction {
  type: OptimizeType
  label: string
  icon: any
}

/** AI 配置 */
interface AiConfig {
  /** 是否自动应用结果 */
  autoApply?: boolean
  /** 自定义系统提示词 */
  systemPrompt?: string
  /** AI 提供商 */
  provider?: string
  /** 模型名称 */
  modelName?: string
  /** 温度参数 */
  temperature?: number
}

/** AFormInputWithAi Props */
interface AFormInputWithAiProps {
  /** 绑定值 */
  modelValue?: string | number | null | undefined
  /** 标签文本 */
  label?: string
  /** 字段名 */
  prop?: string
  /** 输入框类型 */
  type?: 'text' | 'textarea' | 'number' | 'password'
  /** 字段类型（影响默认提示词） */
  fieldType?: FieldType
  /** 默认优化类型 */
  optimizeType?: OptimizeType
  /** 是否显示快捷操作按钮组 */
  showQuickActions?: boolean
  /** 快捷操作列表 */
  quickActions?: OptimizeType[]
  /** AI 配置 */
  aiConfig?: AiConfig
  /** AI 图标提示文本 */
  aiTooltip?: string
  /** 其他属性 */
  [key: string]: any
}

const props = withDefaults(defineProps<AFormInputWithAiProps>(), {
  fieldType: 'custom',
  optimizeType: 'polish',
  showQuickActions: false,
  quickActions: () => ['polish', 'expand', 'shorten'],
  aiConfig: () => ({}),
  aiTooltip: 'AI 优化'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null | undefined]
  'ai-start': [type: OptimizeType]
  'ai-result': [result: string]
  'ai-error': [error: any]
}>()

/** 本地值 */
const localValue = ref(props.modelValue)

/** 监听 props.modelValue 变化 */
watch(
  () => props.modelValue,
  (newValue) => {
    localValue.value = newValue
  }
)

/** 处理输入事件 */
const handleInput = (value: string | number | null | undefined) => {
  localValue.value = value
  emit('update:modelValue', value)
}

/** 传递给 AFormInput 的属性（排除 AI 相关属性） */
const inputProps = computed(() => {
  const { fieldType, optimizeType, showQuickActions, quickActions, aiConfig, aiTooltip, modelValue, ...rest } = props
  return rest
})

/** 过滤掉 suffix 插槽 */
const slotsWithoutSuffix = computed(() => {
  const slots = { ...useSlots() }
  delete slots.suffix
  return slots
})

/** 加载状态 */
const loading = ref(false)

/** 当前优化类型（用于显示加载状态） */
const currentOptimizeType = ref<OptimizeType | null>(null)

/** 高级弹窗显示状态 */
const advancedDialogVisible = ref(false)

/** 临时值（用于高级弹窗） */
const tempValue = ref('')

/** 快捷操作配置映射 */
const quickActionMap = computed<Record<OptimizeType, QuickAction>>(() => ({
  polish: { type: 'polish', label: t('ai.inputWithAi.polish'), icon: Edit },
  expand: { type: 'expand', label: t('ai.inputWithAi.expand'), icon: ZoomIn },
  shorten: { type: 'shorten', label: t('ai.inputWithAi.shorten'), icon: ZoomOut },
  formal: { type: 'formal', label: t('ai.inputWithAi.formal'), icon: Document },
  casual: { type: 'casual', label: t('ai.inputWithAi.casual'), icon: ChatDotRound },
  translate: { type: 'translate', label: t('ai.inputWithAi.translate'), icon: Document },
  custom: { type: 'custom', label: t('ai.inputWithAi.custom'), icon: MagicStick }
}))

/** 计算后的快捷操作列表 */
const computedQuickActions = computed(() => {
  return props.quickActions.map((type) => quickActionMap.value[type])
})

// 字段类型对应的默认提示词
const defaultPrompts: Record<FieldType, string> = {
  title: '你是专业的标题优化助手。请优化标题内容，使其简洁有力、吸引眼球。',
  description: '你是专业的描述优化助手。请优化描述内容，使其详细生动、信息完整。',
  remark: '你是专业的备注优化助手。请优化备注内容，使其清晰准确、便于理解。',
  content: '你是专业的内容优化助手。请优化文本内容，使其流畅自然、易于阅读。',
  custom: '你是专业的内容优化助手。'
}

// 优化类型对应的操作描述
const typePrompts: Record<OptimizeType, string> = {
  polish: '请润色以下内容，使其更加流畅、专业、易读',
  expand: '请扩写以下内容，增加更多细节和描述，使内容更加丰富完整',
  shorten: '请精简以下内容，保留核心信息，使其更加简洁明了',
  formal: '请将以下内容转换为正式、专业的表达方式',
  casual: '请将以下内容转换为轻松、口语化的表达方式',
  translate: '请翻译以下内容',
  custom: '请优化以下内容'
}

/**
 * AI 优化方法
 */
const optimize = async (content: string, type: OptimizeType): Promise<string | null> => {
  if (!content || content.trim() === '') {
    showMsgWarning(t('ai.inputWithAi.contentRequired'))
    return null
  }

  loading.value = true

  try {
    // 构建系统提示词
    const basePrompt = props.aiConfig.systemPrompt || defaultPrompts[props.fieldType]
    const operationPrompt = typePrompts[type]
    const fullSystemPrompt = `${basePrompt}\n\n${operationPrompt}。\n\n请直接输出优化后的内容，不要添加任何解释说明。`

    // 构建请求参数
    const aiChatBo: AiChatBo = {
      message: content,
      systemPrompt: fullSystemPrompt,
      provider: props.aiConfig.provider || 'deepseek',
      modelName: props.aiConfig.modelName,
      temperature: props.aiConfig.temperature || 0.7
    }

    // 调用 AI API
    const [err, response] = await aiOptimize(aiChatBo)

    if (err) {
      console.error('AI optimize failed:', err)
      showMsgError(t('ai.inputWithAi.optimizeFailed'))
      return null
    }

    if (!response || !response.content) {
      showMsgError(t('ai.inputWithAi.emptyResult'))
      return null
    }

    // 如果自动应用，直接返回结果
    if (props.aiConfig.autoApply) {
      showMsgSuccess(t('ai.inputWithAi.optimizeSuccess'))
      return response.content
    }

    // 否则弹出确认框
    try {
      await ElMessageBox.confirm(response.content, t('ai.inputWithAi.applyResult'), {
        confirmButtonText: t('common.apply'),
        cancelButtonText: t('common.cancel'),
        type: 'info',
        distinguishCancelAndClose: true
      })
      showMsgSuccess(t('ai.inputWithAi.applied'))
      return response.content
    } catch {
      return null
    }
  } catch (error) {
    console.error('AI optimize error:', error)
    showMsgError(t('ai.inputWithAi.optimizeError'))
    return null
  } finally {
    loading.value = false
  }
}

/**
 * 处理 AI 优化
 */
const handleOptimize = async () => {
  if (!localValue.value) {
    return
  }

  currentOptimizeType.value = props.optimizeType
  emit('ai-start', props.optimizeType)

  try {
    const result = await optimize(String(localValue.value), props.optimizeType)

    if (result) {
      localValue.value = result
      emit('update:modelValue', result)
      emit('ai-result', result)
    }
  } catch (error) {
    emit('ai-error', error)
  } finally {
    currentOptimizeType.value = null
  }
}

/**
 * 处理快捷优化
 */
const handleQuickOptimize = async (type: OptimizeType) => {
  if (!localValue.value) {
    return
  }

  currentOptimizeType.value = type
  emit('ai-start', type)

  try {
    const result = await optimize(String(localValue.value), type)

    if (result) {
      localValue.value = result
      emit('update:modelValue', result)
      emit('ai-result', result)
    }
  } catch (error) {
    emit('ai-error', error)
  } finally {
    currentOptimizeType.value = null
  }
}

/**
 * 打开高级优化弹窗
 */
const openAdvancedDialog = () => {
  tempValue.value = String(localValue.value || '')
  advancedDialogVisible.value = true
}

/**
 * 处理高级优化结果
 */
const handleAdvancedResult = (result: string) => {
  localValue.value = result
  emit('update:modelValue', result)
  advancedDialogVisible.value = false
  emit('ai-result', result)
}

/**
 * 监听弹窗关闭，同步 tempValue 到 localValue
 */
watch(advancedDialogVisible, (visible) => {
  if (!visible && tempValue.value !== localValue.value) {
    localValue.value = tempValue.value
    emit('update:modelValue', tempValue.value)
  }
})

/**
 * 暴露方法供外部调用
 */
defineExpose({
  /** 触发 AI 优化 */
  triggerAi: handleOptimize,
  /** 触发快捷 AI 优化 */
  triggerQuickAi: handleQuickOptimize,
  /** 打开高级优化弹窗 */
  openAdvanced: openAdvancedDialog
})
</script>

<style scoped>
.ai-trigger-icon {
  cursor: pointer;
  color: var(--el-color-primary);
  transition: all 0.3s;
  font-size: 16px;
}

.ai-trigger-icon:hover {
  color: var(--el-color-primary-light-3);
  transform: scale(1.1);
}

.ai-trigger-icon.is-loading {
  animation: rotating 2s linear infinite;
  cursor: not-allowed;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.ai-quick-actions {
  display: flex;
  gap: 4px;
  align-items: center;
}

.ai-quick-actions :deep(.el-button) {
  padding: 4px;
}

.ai-quick-actions :deep(.el-button.is-link) {
  color: var(--el-color-primary);
}

.ai-quick-actions :deep(.el-button.is-link:hover) {
  color: var(--el-color-primary-light-3);
}

.ai-quick-actions :deep(.el-button.is-disabled) {
  color: var(--el-text-color-disabled);
  cursor: not-allowed;
}
</style>
