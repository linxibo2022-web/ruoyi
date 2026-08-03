<!--
  AAiTextOptimizer 文本优化器组件

  功能：
  - 文本润色美化
  - 内容扩写/缩写
  - 风格转换（正式/口语/营销）
  - 多语言翻译
  - SEO优化
-->

<template>
  <div class="ai-text-optimizer">
    <!-- 当前内容显示 -->
    <div class="content-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.textOptimizer.currentContent') }}</span>
        <el-tag v-if="value" size="small">{{ t('ai.textOptimizer.charCount', { count: getTextLength(value) }) }}</el-tag>
      </div>
      <el-input v-model="inputValue" type="textarea" :rows="6" :placeholder="t('ai.textOptimizer.inputPlaceholder')" :disabled="loading" />
    </div>

    <!-- 优化选项 -->
    <div class="options-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.textOptimizer.optimizeMethod') }}</span>
      </div>

      <!-- 快速优化按钮 -->
      <div class="quick-options">
        <el-button
          v-for="option in optimizeTypeOptions"
          :key="option.value"
          :type="selectedOptimizeType === option.value ? 'primary' : 'default'"
          :icon="option.icon"
          size="small"
          @click="handleQuickOptimize(option.value)"
        >
          {{ option.label }}
        </el-button>
      </div>

      <!-- 高级选项 -->
      <el-collapse v-model="activeAdvanced" class="advanced-options">
        <el-collapse-item :title="t('ai.textOptimizer.advancedOptions')" name="advanced">
          <el-form :model="optimizeParams" label-width="80px" size="small">
            <el-form-item :label="t('ai.textOptimizer.styleRequirement')">
              <el-select v-model="optimizeParams.style" :placeholder="t('ai.textOptimizer.selectStyle')">
                <el-option :label="t('ai.textOptimizer.styles.formal')" value="formal" />
                <el-option :label="t('ai.textOptimizer.styles.casual')" value="casual" />
                <el-option :label="t('ai.textOptimizer.styles.marketing')" value="marketing" />
                <el-option :label="t('ai.textOptimizer.styles.academic')" value="academic" />
                <el-option :label="t('ai.textOptimizer.styles.creative')" value="creative" />
              </el-select>
            </el-form-item>

            <el-form-item :label="t('ai.textOptimizer.targetLength')">
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-input-number
                    v-model="optimizeParams.minLength"
                    :min="0"
                    :max="optimizeParams.maxLength || 10000"
                    :placeholder="t('ai.textOptimizer.minLength')"
                    controls-position="right"
                  />
                </el-col>
                <el-col :span="12">
                  <el-input-number
                    v-model="optimizeParams.maxLength"
                    :min="optimizeParams.minLength || 0"
                    :max="10000"
                    :placeholder="t('ai.textOptimizer.maxLength')"
                    controls-position="right"
                  />
                </el-col>
              </el-row>
            </el-form-item>

            <el-form-item :label="t('ai.textOptimizer.keywords')">
              <el-select v-model="optimizeParams.keywords" multiple filterable allow-create :placeholder="t('ai.textOptimizer.keywordsPlaceholder')"> </el-select>
            </el-form-item>

            <el-form-item :label="t('ai.textOptimizer.translateLanguage')" v-if="optimizeType === 'translate'">
              <el-select v-model="optimizeParams.targetLanguage" :placeholder="t('ai.textOptimizer.targetLanguagePlaceholder')">
                <el-option :label="t('ai.textOptimizer.languages.en')" value="en" />
                <el-option :label="t('ai.textOptimizer.languages.ja')" value="ja" />
                <el-option :label="t('ai.textOptimizer.languages.ko')" value="ko" />
                <el-option :label="t('ai.textOptimizer.languages.fr')" value="fr" />
                <el-option :label="t('ai.textOptimizer.languages.de')" value="de" />
                <el-option :label="t('ai.textOptimizer.languages.es')" value="es" />
              </el-select>
            </el-form-item>

            <el-form-item :label="t('ai.textOptimizer.customRequirement')">
              <el-input v-model="optimizeParams.customRequirement" type="textarea" :rows="2" :placeholder="t('ai.textOptimizer.customRequirementPlaceholder')" />
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 处理按钮 -->
    <div class="action-section">
      <el-button type="primary" :loading="loading" :disabled="!inputValue || !selectedOptimizeType" @click="handleProcess" block>
        <el-icon v-if="!loading"><MagicStick /></el-icon>
        {{ loading ? t('ai.textOptimizer.processing') : t('ai.textOptimizer.startOptimize') }}
      </el-button>
    </div>

    <!-- 结果显示 -->
    <div v-if="resultContent" class="result-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.textOptimizer.optimizeResult') }}</span>
        <div class="result-actions">
          <el-tag size="small" type="success">{{ t('ai.textOptimizer.charCount', { count: getTextLength(resultContent) }) }}</el-tag>
          <el-button link type="primary" size="small" @click="handleCopy">
            <el-icon><CopyDocument /></el-icon>
            {{ t('common.copy') }}
          </el-button>
          <el-button link type="primary" size="small" @click="handleCompare">
            <el-icon><View /></el-icon>
            {{ t('ai.textOptimizer.compare') }}
          </el-button>
        </div>
      </div>

      <!-- 流式输出效果 -->
      <div v-if="isStreaming" class="streaming-result">
        <el-input :model-value="resultContent" type="textarea" :rows="6" readonly />
        <div class="streaming-indicator">
          <el-icon class="is-loading"><Loading /></el-icon>
          {{ t('ai.textOptimizer.generating') }}
        </div>
      </div>

      <!-- 完成结果 -->
      <div v-else class="final-result">
        <el-input v-model="resultContent" type="textarea" :rows="6" readonly />

        <!-- Token使用情况 -->
        <div v-if="tokenUsage" class="token-usage">
          <el-tooltip :content="t('ai.textOptimizer.tokenUsage.inputToken')" placement="top">
            <el-tag size="small">{{ t('ai.textOptimizer.tokenUsage.input') }}: {{ tokenUsage.promptTokens }}</el-tag>
          </el-tooltip>
          <el-tooltip :content="t('ai.textOptimizer.tokenUsage.outputToken')" placement="top">
            <el-tag size="small" type="success">{{ t('ai.textOptimizer.tokenUsage.output') }}: {{ tokenUsage.completionTokens }}</el-tag>
          </el-tooltip>
          <el-tooltip :content="t('ai.textOptimizer.tokenUsage.totalToken')" placement="top">
            <el-tag size="small" type="info">{{ t('ai.textOptimizer.tokenUsage.total') }}: {{ tokenUsage.totalTokens }}</el-tag>
          </el-tooltip>
        </div>
      </div>

      <!-- 多个结果选择 -->
      <div v-if="resultVariants.length > 1" class="result-variants">
        <el-divider content-position="left">{{ t('ai.textOptimizer.otherSuggestions') }}</el-divider>
        <el-radio-group v-model="selectedVariant" @change="handleVariantChange">
          <el-radio v-for="(variant, index) in resultVariants" :key="index" :value="index" border class="variant-item">
            {{ variant.substring(0, 50) }}{{ variant.length > 50 ? '...' : '' }}
          </el-radio>
        </el-radio-group>
      </div>
    </div>

    <!-- 对比视图弹窗 -->
    <AModal v-model="compareVisible" :title="t('ai.textOptimizer.contentCompare')" size="large" :show-footer="false">
      <div class="compare-view">
        <div class="compare-column">
          <div class="compare-header">{{ t('ai.textOptimizer.originalText') }}</div>
          <div class="compare-content">{{ inputValue }}</div>
          <div class="compare-stats">{{ t('ai.textOptimizer.wordCount') }}: {{ getTextLength(inputValue) }}</div>
        </div>
        <div class="compare-divider">
          <el-icon><Right /></el-icon>
        </div>
        <div class="compare-column">
          <div class="compare-header">{{ t('ai.textOptimizer.optimizedText') }}</div>
          <div class="compare-content">{{ resultContent }}</div>
          <div class="compare-stats">
            {{ t('ai.textOptimizer.wordCount') }}: {{ getTextLength(resultContent) }}
            <el-tag v-if="getLengthDiff() > 0" type="success" size="small"> +{{ getLengthDiff() }} </el-tag>
            <el-tag v-else-if="getLengthDiff() < 0" type="warning" size="small">
              {{ getLengthDiff() }}
            </el-tag>
          </div>
        </div>
      </div>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="AAiTextOptimizer">
import { MagicStick, CopyDocument, View, Right, Loading } from '@element-plus/icons-vue'
import { aiOptimize } from '@/api/business/base/ai/aiApi'
import type { AiChatBo } from '@/api/business/base/ai/aiTypes'
import { showMsgSuccess, showMsgWarning, showMsgError } from '@/utils/modal'
import { copy } from '@/utils/function'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

/** 优化类型 */
type OptimizeType = 'polish' | 'expand' | 'shorten' | 'formal' | 'casual' | 'marketing' | 'translate' | 'seo'

/** 优化参数 */
interface OptimizeParams {
  style?: string
  minLength?: number
  maxLength?: number
  keywords?: string[]
  targetLanguage?: string
  customRequirement?: string
}

/** Token使用情况 */
interface TokenUsage {
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

/** 组件Props */
interface AAiTextOptimizerProps {
  /** 当前值 */
  value?: string
  /** 字段名称 */
  field?: string
  /** 字段类型 */
  fieldType?: 'title' | 'description' | 'remark' | 'custom'
  /** 上下文信息 */
  context?: Record<string, any>
  /** 优化类型 */
  optimizeType?: OptimizeType
  /** 模型提供商 */
  provider?: string
  /** 模型名称 */
  modelName?: string
  /** 温度参数 */
  temperature?: number
  /** 角色设定 */
  role?: string
  /** 系统提示词 */
  systemPrompt?: string
}

const props = withDefaults(defineProps<AAiTextOptimizerProps>(), {
  value: '',
  fieldType: 'custom',
  context: () => ({}),
  optimizeType: 'polish',
  provider: 'deepseek',
  temperature: 0.7
})

/** 组件事件 */
const emit = defineEmits<{
  'process': [params: any]
  'result': [result: string]
  'error': [error: Error]
}>()

/** 输入值 */
const inputValue = ref(props.value)

/** 选中的优化类型 */
const selectedOptimizeType = ref<OptimizeType>(props.optimizeType)

/** 优化参数 */
const optimizeParams = ref<OptimizeParams>({
  style: undefined,
  minLength: undefined,
  maxLength: undefined,
  keywords: [],
  targetLanguage: 'en',
  customRequirement: ''
})

/** 激活的高级选项 */
const activeAdvanced = ref<string[]>([])

/** 处理中状态 */
const loading = ref(false)

/** 流式输出状态 */
const isStreaming = ref(false)

/** 结果内容 */
const resultContent = ref('')

/** 结果变体 */
const resultVariants = ref<string[]>([])

/** 选中的变体 */
const selectedVariant = ref(0)

/** Token使用情况 */
const tokenUsage = ref<TokenUsage | null>(null)

/** 对比视图显示 */
const compareVisible = ref(false)

/** 优化类型选项 */
const optimizeTypeOptions = computed<Array<{ label: string; value: OptimizeType; icon: string }>>(() => [
  { label: t('ai.textOptimizer.types.polish'), value: 'polish', icon: 'Edit' },
  { label: t('ai.textOptimizer.types.expand'), value: 'expand', icon: 'ZoomIn' },
  { label: t('ai.textOptimizer.types.shorten'), value: 'shorten', icon: 'ZoomOut' },
  { label: t('ai.textOptimizer.types.formal'), value: 'formal', icon: 'Document' },
  { label: t('ai.textOptimizer.types.casual'), value: 'casual', icon: 'ChatDotRound' },
  { label: t('ai.textOptimizer.types.marketing'), value: 'marketing', icon: 'Promotion' },
  { label: t('ai.textOptimizer.types.seo'), value: 'seo', icon: 'Search' }
])

/** 监听value变化 */
watch(
  () => props.value,
  (newValue) => {
    inputValue.value = newValue
  }
)

/** 获取文本长度 */
const getTextLength = (text: string | undefined) => {
  if (!text) return 0
  return text.length
}

/** 获取长度差异 */
const getLengthDiff = () => {
  return getTextLength(resultContent.value) - getTextLength(inputValue.value)
}

/** 快速优化 */
const handleQuickOptimize = (type: OptimizeType) => {
  selectedOptimizeType.value = type
  // 可以立即处理或等待用户点击开始按钮
}

/** 构建系统提示词 */
const buildSystemPrompt = (): string => {
  if (props.systemPrompt) {
    return props.systemPrompt
  }

  const role = props.role || '专业的内容优化助手'
  let prompt = `你是${role}。`

  // 根据字段类型添加上下文
  const fieldTypeContext = {
    title: '你正在优化标题内容，要求简洁有力，吸引眼球。',
    description: '你正在优化描述内容，要求详细生动，信息完整。',
    remark: '你正在优化备注内容，要求清晰准确，便于理解。',
    custom: '你正在优化文本内容。'
  }
  prompt += fieldTypeContext[props.fieldType] || ''

  // 添加上下文信息
  if (Object.keys(props.context).length > 0) {
    prompt += `\n\n相关上下文信息：\n${JSON.stringify(props.context, null, 2)}`
  }

  return prompt
}

/** 构建用户提示词 */
const buildUserPrompt = (): string => {
  const typeInstructions = {
    polish: '请帮我润色和美化以下内容，使其更加流畅、专业、易读。',
    expand: '请帮我扩写以下内容，增加更多细节和描述，使内容更加丰富完整。',
    shorten: '请帮我精简以下内容，保留核心信息，使其更加简洁明了。',
    formal: '请帮我将以下内容转换为正式、专业的表达方式。',
    casual: '请帮我将以下内容转换为轻松、口语化的表达方式。',
    marketing: '请帮我将以下内容改写为营销推广文案，要有吸引力和说服力。',
    translate: `请将以下内容翻译为${optimizeParams.value.targetLanguage === 'en' ? '英语' : '目标语言'}。`,
    seo: '请帮我优化以下内容的SEO效果，增加关键词密度，提升搜索排名。'
  }

  let prompt = typeInstructions[selectedOptimizeType.value] || '请优化以下内容：'

  // 添加约束条件
  const constraints: string[] = []

  if (optimizeParams.value.minLength) {
    constraints.push(`最少${optimizeParams.value.minLength}字`)
  }
  if (optimizeParams.value.maxLength) {
    constraints.push(`最多${optimizeParams.value.maxLength}字`)
  }
  if (optimizeParams.value.keywords && optimizeParams.value.keywords.length > 0) {
    constraints.push(`必须包含关键词：${optimizeParams.value.keywords.join('、')}`)
  }
  if (optimizeParams.value.style) {
    constraints.push(`风格要求：${optimizeParams.value.style}`)
  }
  if (optimizeParams.value.customRequirement) {
    constraints.push(`其他要求：${optimizeParams.value.customRequirement}`)
  }

  if (constraints.length > 0) {
    prompt += `\n\n约束条件：\n${constraints.join('\n')}`
  }

  prompt += `\n\n原文：\n${inputValue.value}`
  prompt += '\n\n请直接输出优化后的内容，不要添加任何解释说明。'

  return prompt
}

/** 处理优化 */
const handleProcess = async () => {
  if (!inputValue.value || !selectedOptimizeType.value) {
    showMsgWarning(t('ai.textOptimizer.inputAndSelectRequired'))
    return
  }

  loading.value = true
  isStreaming.value = true
  resultContent.value = ''
  tokenUsage.value = null

  try {
    const systemPrompt = buildSystemPrompt()
    const userPrompt = buildUserPrompt()

    emit('process', {
      type: selectedOptimizeType.value,
      input: inputValue.value,
      params: optimizeParams.value
    })

    const bo: AiChatBo = {
      message: userPrompt,
      systemPrompt: systemPrompt,
      provider: props.provider,
      modelName: props.modelName,
      temperature: props.temperature
    }

    const [err, response] = await aiOptimize(bo)

    if (err) {
      console.error('Optimize failed:', err)
      emit('error', err)
      showMsgError(t('ai.textOptimizer.optimizeFailed'))
      return
    }

    if (!response) {
      const noDataError = new Error('Response data is empty')
      emit('error', noDataError)
      showMsgError(t('ai.textOptimizer.optimizeFailed'))
      return
    }

    // 设置结果内容
    if (response.content) {
      resultContent.value = response.content
      emit('result', resultContent.value)
      showMsgSuccess(t('ai.textOptimizer.optimizeComplete'))
    }

    // 设置 token 使用情况
    if (response.tokenUsage) {
      tokenUsage.value = response.tokenUsage
    }
  } catch (error) {
    console.error('Optimize failed:', error)
    emit('error', error as Error)
    showMsgError(t('ai.textOptimizer.optimizeFailed'))
  } finally {
    loading.value = false
    isStreaming.value = false
  }
}

/** 复制结果 */
const handleCopy = () => {
  copy(resultContent.value, t('ai.textOptimizer.copySuccess'))
}

/** 对比查看 */
const handleCompare = () => {
  compareVisible.value = true
}

/** 切换变体 */
const handleVariantChange = (index: number) => {
  resultContent.value = resultVariants.value[index]
}

/** 暴露方法 */
defineExpose({
  /** 获取结果 */
  getResult: () => resultContent.value,
  /** 清空结果 */
  clearResult: () => {
    resultContent.value = ''
    tokenUsage.value = null
  },
  /** 重新处理 */
  reprocess: handleProcess
})
</script>

<style scoped>
.ai-text-optimizer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-section,
.options-section,
.result-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.advanced-options {
  margin-top: 12px;
}

.action-section {
  margin: 8px 0;
}

.streaming-result {
  position: relative;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  color: var(--el-color-primary);
  font-size: 13px;
}

.final-result {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.token-usage {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.result-variants {
  margin-top: 16px;
}

.variant-item {
  width: 100%;
  margin-bottom: 8px;
  white-space: normal;
  height: auto;
  padding: 12px;
}

.compare-view {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 20px;
  min-height: 400px;
}

.compare-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.compare-header {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  padding-bottom: 8px;
  border-bottom: 2px solid var(--el-color-primary);
}

.compare-content {
  flex: 1;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  line-height: 1.8;
  overflow-y: auto;
}

.compare-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.compare-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
}
</style>
