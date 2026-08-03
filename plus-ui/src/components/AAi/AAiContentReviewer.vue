<!--
  AAiContentReviewer 智能内容审核器组件

  功能：
  - 内容合规性检查
  - 敏感词检测
  - 质量评分
  - 格式验证
  - 自动修复建议
  - 多维度审核报告
-->

<template>
  <div class="ai-content-reviewer">
    <!-- 审核配置 -->
    <div class="config-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.contentReviewer.reviewConfig') }}</span>
      </div>

      <el-form :model="reviewConfig" label-width="100px" size="small">
        <el-form-item :label="t('ai.contentReviewer.reviewLevel')">
          <el-radio-group v-model="reviewConfig.level">
            <el-radio value="loose">{{ t('ai.contentReviewer.levels.loose') }}</el-radio>
            <el-radio value="normal">{{ t('ai.contentReviewer.levels.normal') }}</el-radio>
            <el-radio value="strict">{{ t('ai.contentReviewer.levels.strict') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="t('ai.contentReviewer.checkItems')">
          <el-checkbox-group v-model="reviewConfig.checkItems">
            <el-checkbox value="compliance">{{ t('ai.contentReviewer.items.compliance') }}</el-checkbox>
            <el-checkbox value="sensitive">{{ t('ai.contentReviewer.items.sensitive') }}</el-checkbox>
            <el-checkbox value="quality">{{ t('ai.contentReviewer.items.quality') }}</el-checkbox>
            <el-checkbox value="completeness">{{ t('ai.contentReviewer.items.completeness') }}</el-checkbox>
            <el-checkbox value="format">{{ t('ai.contentReviewer.items.format') }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item :label="t('ai.contentReviewer.autoFix')">
          <el-switch v-model="reviewConfig.autoFix" :active-text="t('common.enable')" :inactive-text="t('common.disable')" />
          <el-tooltip :content="t('ai.contentReviewer.autoFixTip')" placement="top">
            <el-icon class="ml-1"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
      </el-form>
    </div>

    <!-- 待审核内容 -->
    <div class="content-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.contentReviewer.contentToReview') }}</span>
        <el-button link type="primary" size="small" @click="handleLoadFromForm">
          <el-icon><DocumentCopy /></el-icon>
          {{ t('ai.contentReviewer.loadFromForm') }}
        </el-button>
      </div>

      <div class="content-fields">
        <div v-for="(value, key) in reviewContent" :key="key" class="content-field">
          <div class="field-label">
            {{ getFieldLabel(key) }}
            <el-tag v-if="fieldStatus[key]" :type="getStatusType(fieldStatus[key])" size="small">
              {{ getStatusText(fieldStatus[key]) }}
            </el-tag>
          </div>
          <el-input v-model="reviewContent[key]" type="textarea" :rows="getFieldRows(key)" :placeholder="t('ai.contentReviewer.inputPlaceholder', { field: getFieldLabel(key) })" />
        </div>
      </div>
    </div>

    <!-- 自定义规则 -->
    <el-collapse v-model="activeCollapse" class="rules-section">
      <el-collapse-item :title="t('ai.contentReviewer.customRules')" name="rules">
        <div class="rules-list">
          <div v-for="(rule, index) in customRules" :key="index" class="rule-item">
            <el-form :model="rule" inline size="small">
              <el-form-item :label="t('ai.contentReviewer.field')">
                <el-select v-model="rule.field" :placeholder="t('ai.contentReviewer.selectField')" style="width: 120px">
                  <el-option v-for="key in Object.keys(reviewContent)" :key="key" :label="getFieldLabel(key)" :value="key" />
                </el-select>
              </el-form-item>

              <el-form-item :label="t('ai.contentReviewer.ruleType')">
                <el-select v-model="rule.type" :placeholder="t('ai.contentReviewer.ruleType')" style="width: 120px">
                  <el-option :label="t('ai.contentReviewer.ruleTypes.required')" value="required" />
                  <el-option :label="t('ai.contentReviewer.ruleTypes.length')" value="length" />
                  <el-option :label="t('ai.contentReviewer.ruleTypes.format')" value="format" />
                  <el-option :label="t('ai.contentReviewer.ruleTypes.keywords')" value="keywords" />
                  <el-option :label="t('ai.contentReviewer.ruleTypes.regex')" value="regex" />
                </el-select>
              </el-form-item>

              <el-form-item :label="t('ai.contentReviewer.ruleValue')">
                <el-input v-model="rule.value" :placeholder="t('ai.contentReviewer.ruleValue')" style="width: 120px" />
              </el-form-item>

              <el-form-item :label="t('ai.contentReviewer.tip')">
                <el-input v-model="rule.message" :placeholder="t('ai.contentReviewer.errorTip')" style="width: 150px" />
              </el-form-item>

              <el-form-item>
                <el-button link type="danger" @click="handleRemoveRule(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-button type="primary" plain size="small" @click="handleAddRule">
            <el-icon><Plus /></el-icon>
            {{ t('ai.contentReviewer.addRule') }}
          </el-button>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 审核按钮 -->
    <div class="action-section">
      <el-button type="primary" :loading="loading" :disabled="!canReview" @click="handleReview" block>
        <el-icon v-if="!loading"><View /></el-icon>
        {{ loading ? t('ai.contentReviewer.reviewing') : t('ai.contentReviewer.startReview') }}
      </el-button>
    </div>

    <!-- 审核结果 -->
    <div v-if="reviewResult" class="result-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.contentReviewer.reviewResult') }}</span>
        <el-tag :type="getOverallStatusType(reviewResult.status)" size="large">
          {{ getOverallStatusText(reviewResult.status) }}
        </el-tag>
      </div>

      <!-- 总体评分 -->
      <div class="overall-score">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ t('ai.contentReviewer.overallScore') }}</span>
              <el-rate v-model="reviewResult.score" disabled show-score text-color="#ff9900" />
            </div>
          </template>
          <div class="score-details">
            <div class="score-item">
              <span class="score-label">{{ t('ai.contentReviewer.scores.compliance') }}</span>
              <el-progress :percentage="reviewResult.details?.compliance || 0" :color="getProgressColor(reviewResult.details?.compliance)" />
            </div>
            <div class="score-item">
              <span class="score-label">{{ t('ai.contentReviewer.scores.quality') }}</span>
              <el-progress :percentage="reviewResult.details?.quality || 0" :color="getProgressColor(reviewResult.details?.quality)" />
            </div>
            <div class="score-item">
              <span class="score-label">{{ t('ai.contentReviewer.scores.completeness') }}</span>
              <el-progress :percentage="reviewResult.details?.completeness || 0" :color="getProgressColor(reviewResult.details?.completeness)" />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 问题列表 -->
      <div v-if="reviewResult.issues && reviewResult.issues.length > 0" class="issues-section">
        <el-alert :title="t('ai.contentReviewer.foundIssues', { count: reviewResult.issues.length })" type="warning" :closable="false" show-icon> </el-alert>

        <div class="issues-list">
          <el-collapse v-model="activeIssues">
            <el-collapse-item v-for="(issue, index) in reviewResult.issues" :key="index" :name="index">
              <template #title>
                <div class="issue-title">
                  <el-tag :type="getSeverityType(issue.severity)" size="small">
                    {{ getSeverityText(issue.severity) }}
                  </el-tag>
                  <span class="issue-field">{{ getFieldLabel(issue.field) }}</span>
                  <span class="issue-message">{{ issue.message }}</span>
                </div>
              </template>

              <div class="issue-content">
                <div class="issue-detail">
                  <div class="detail-row">
                    <span class="detail-label">{{ t('ai.contentReviewer.issueLocation') }}</span>
                    <span class="detail-value">{{ issue.location || t('ai.contentReviewer.entireField') }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">{{ t('ai.contentReviewer.issueContent') }}</span>
                    <div class="detail-value highlight-text">{{ issue.content }}</div>
                  </div>
                  <div v-if="issue.suggestion" class="detail-row">
                    <span class="detail-label">{{ t('ai.contentReviewer.fixSuggestion') }}</span>
                    <div class="detail-value suggestion-text">{{ issue.suggestion }}</div>
                  </div>
                  <div v-if="issue.fixedContent" class="detail-row">
                    <span class="detail-label">{{ t('ai.contentReviewer.afterFix') }}</span>
                    <div class="detail-value fixed-text">{{ issue.fixedContent }}</div>
                  </div>
                </div>

                <div class="issue-actions">
                  <el-button v-if="issue.fixedContent" type="primary" size="small" @click="handleApplyFix(issue)">
                    <el-icon><Check /></el-icon>
                    {{ t('ai.contentReviewer.applyFix') }}
                  </el-button>
                  <el-button size="small" @click="handleIgnoreIssue(index)">
                    <el-icon><Close /></el-icon>
                    {{ t('ai.contentReviewer.ignore') }}
                  </el-button>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

      <!-- 通过提示 -->
      <div v-else class="pass-section">
        <el-result icon="success" :title="t('ai.contentReviewer.reviewPassed')" :sub-title="t('ai.contentReviewer.contentMeetsStandards')">
          <template #extra>
            <el-button type="primary" @click="handleExportReport">
              <el-icon><Document /></el-icon>
              {{ t('ai.contentReviewer.exportReport') }}
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 审核摘要 -->
      <div class="summary-section">
        <el-descriptions :title="t('ai.contentReviewer.reviewSummary')" :column="2" border>
          <el-descriptions-item :label="t('ai.contentReviewer.summary.reviewTime')">
            {{ reviewResult.timestamp }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('ai.contentReviewer.summary.reviewLevel')">
            {{ getLevelText(reviewConfig.level) }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('ai.contentReviewer.summary.reviewFields')"> {{ t('ai.contentReviewer.summary.countUnit', { count: Object.keys(reviewContent).length }) }} </el-descriptions-item>
          <el-descriptions-item :label="t('ai.contentReviewer.summary.foundIssues')"> {{ t('ai.contentReviewer.summary.countUnit', { count: reviewResult.issues?.length || 0 }) }} </el-descriptions-item>
          <el-descriptions-item :label="t('ai.contentReviewer.summary.severeIssues')"> {{ t('ai.contentReviewer.summary.countUnit', { count: countIssuesBySeverity('high') }) }} </el-descriptions-item>
          <el-descriptions-item :label="t('ai.contentReviewer.summary.suggestedOptimizations')"> {{ t('ai.contentReviewer.summary.countUnit', { count: countIssuesBySeverity('medium') + countIssuesBySeverity('low') }) }} </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 操作按钮 -->
      <div class="result-actions">
        <el-button @click="handleReReview">
          <el-icon><RefreshRight /></el-icon>
          {{ t('ai.contentReviewer.reReview') }}
        </el-button>
        <el-button v-if="reviewConfig.autoFix && hasFixableIssues" type="primary" @click="handleApplyAllFixes">
          <el-icon><Check /></el-icon>
          {{ t('ai.contentReviewer.applyAllFixes') }}
        </el-button>
        <el-button @click="handleExportReport">
          <el-icon><Download /></el-icon>
          {{ t('ai.contentReviewer.exportReport') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AAiContentReviewer">
import { View, DocumentCopy, QuestionFilled, Plus, Delete, Check, Close, Document, Download, RefreshRight } from '@element-plus/icons-vue'
import { aiReview } from '@/api/business/base/ai/aiApi'
import type { AiChatBo } from '@/api/business/base/ai/aiTypes'
import { showMsgSuccess, showMsgWarning, showMsgError, showMsg } from '@/utils/modal'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

/** 审核级别 */
type ReviewLevel = 'loose' | 'normal' | 'strict'

/** 严重程度 */
type Severity = 'low' | 'medium' | 'high'

/** 审核状态 */
type ReviewStatus = 'pass' | 'warning' | 'fail'

/** 审核配置 */
interface ReviewConfig {
  level: ReviewLevel
  checkItems: string[]
  autoFix: boolean
}

/** 审核规则 */
interface ReviewRule {
  field: string
  type: 'required' | 'length' | 'format' | 'keywords' | 'regex'
  value: string
  message: string
}

/** 审核问题 */
interface ReviewIssue {
  field: string
  severity: Severity
  message: string
  content: string
  location?: string
  suggestion?: string
  fixedContent?: string
}

/** 审核结果 */
interface ReviewResult {
  status: ReviewStatus
  score: number
  details?: {
    compliance: number
    quality: number
    completeness: number
  }
  issues?: ReviewIssue[]
  timestamp: string
}

/** 组件Props */
interface AAiContentReviewerProps {
  /** 待审核内容 */
  content?: Record<string, any>
  /** 字段配置 - 复用FieldConfig */
  fields?: FieldConfig[]
  /** 审核规则 */
  rules?: ReviewRule[]
  /** 审核级别 */
  level?: ReviewLevel
  /** 模型提供商 */
  provider?: string
  /** 模型名称 */
  modelName?: string
  /** 角色设定 */
  role?: string
  /** 会话ID */
  sessionId?: string
}

const props = withDefaults(defineProps<AAiContentReviewerProps>(), {
  content: () => ({}),
  fields: () => [],
  rules: () => [],
  level: 'normal',
  provider: 'deepseek'
})

/** 组件事件 */
const emit = defineEmits<{
  'process': [params: any]
  'result': [result: ReviewResult]
  'error': [error: Error]
  'review-complete': [result: ReviewResult]
}>()

/** 审核配置 */
const reviewConfig = ref<ReviewConfig>({
  level: props.level,
  checkItems: ['compliance', 'sensitive', 'quality', 'completeness'],
  autoFix: true
})

/** 待审核内容 */
const reviewContent = ref<Record<string, any>>({ ...props.content })

/** 字段状态 */
const fieldStatus = ref<Record<string, ReviewStatus>>({})

/** 激活的折叠面板 */
const activeCollapse = ref<string[]>([])

/** 自定义规则 */
const customRules = ref<ReviewRule[]>([...props.rules])

/** 加载状态 */
const loading = ref(false)

/** 审核结果 */
const reviewResult = ref<ReviewResult | null>(null)

/** 激活的问题 */
const activeIssues = ref<number[]>([])

/** 字段标签映射 - 从fields构建 */
const fieldLabels = computed(() => {
  const labels: Record<string, string> = {}
  if (props.fields && props.fields.length > 0) {
    props.fields.forEach((field) => {
      labels[field.prop] = field.label
    })
  }
  return labels
})

/** 是否可以审核 */
const canReview = computed(() => {
  return Object.keys(reviewContent.value).length > 0 && reviewConfig.value.checkItems.length > 0
})

/** 是否有可修复的问题 */
const hasFixableIssues = computed(() => {
  return reviewResult.value?.issues?.some((issue) => issue.fixedContent) || false
})

/** 获取字段标签 */
const getFieldLabel = (key: string) => {
  return fieldLabels.value[key] || key
}

/** 获取字段配置 */
const getFieldConfig = (key: string): FieldConfig | undefined => {
  return props.fields?.find((f) => f.prop === key)
}

/** 获取字段行数 */
const getFieldRows = (key: string) => {
  const field = getFieldConfig(key)
  if (field?.type === 'text') return 4
  if (key === 'description' || key === 'remark') return 4
  return 2
}

/** 获取状态类型 */
const getStatusType = (status: ReviewStatus) => {
  const typeMap: Record<ReviewStatus, string> = {
    pass: 'success',
    warning: 'warning',
    fail: 'danger'
  }
  return (typeMap[status] || '') as ElTagType
}

/** 获取状态文本 */
const getStatusText = (status: ReviewStatus) => {
  const textMap: Record<ReviewStatus, string> = {
    pass: t('ai.contentReviewer.status.pass'),
    warning: t('ai.contentReviewer.status.warning'),
    fail: t('ai.contentReviewer.status.fail')
  }
  return textMap[status] || ''
}

/** 获取整体状态类型 */
const getOverallStatusType = (status: ReviewStatus) => {
  return getStatusType(status)
}

/** 获取整体状态文本 */
const getOverallStatusText = (status: ReviewStatus) => {
  return getStatusText(status)
}

/** 获取级别文本 */
const getLevelText = (level: ReviewLevel) => {
  const textMap: Record<ReviewLevel, string> = {
    loose: t('ai.contentReviewer.levels.loose'),
    normal: t('ai.contentReviewer.levels.normal'),
    strict: t('ai.contentReviewer.levels.strict')
  }
  return textMap[level] || ''
}

/** 获取严重程度类型 */
const getSeverityType = (severity: Severity) => {
  const typeMap: Record<Severity, string> = {
    low: 'info',
    medium: 'warning',
    high: 'danger'
  }
  return (typeMap[severity] || '') as ElTagType
}

/** 获取严重程度文本 */
const getSeverityText = (severity: Severity) => {
  const textMap: Record<Severity, string> = {
    low: t('ai.contentReviewer.severity.low'),
    medium: t('ai.contentReviewer.severity.medium'),
    high: t('ai.contentReviewer.severity.high')
  }
  return textMap[severity] || ''
}

/** 获取进度条颜色 */
const getProgressColor = (percentage: number | undefined) => {
  if (!percentage) return '#909399'
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

/** 统计问题数量 */
const countIssuesBySeverity = (severity: Severity) => {
  return reviewResult.value?.issues?.filter((issue) => issue.severity === severity).length || 0
}

/** 从表单加载 */
const handleLoadFromForm = () => {
  showMsg(t('ai.contentReviewer.loadFromFormRequiresContext'))
  // TODO: 可以从上下文加载表单数据
}

/** 添加规则 */
const handleAddRule = () => {
  customRules.value.push({
    field: '',
    type: 'required',
    value: '',
    message: ''
  })
}

/** 删除规则 */
const handleRemoveRule = (index: number) => {
  customRules.value.splice(index, 1)
}

/** 构建审核提示词 */
const buildReviewPrompt = (): string => {
  const levelDesc = {
    loose: '宽松模式：只检查明显的问题',
    normal: '标准模式：检查常见问题和潜在风险',
    strict: '严格模式：全面深入检查，要求最高标准'
  }

  let prompt = `请作为专业的内容审核专家，对以下内容进行审核。\n`
  prompt += `审核级别：${levelDesc[reviewConfig.value.level]}\n\n`

  prompt += '审核项目：\n'
  if (reviewConfig.value.checkItems.includes('compliance')) {
    prompt += '1. 合规性检查：确保内容符合广告法、平台规范等要求\n'
  }
  if (reviewConfig.value.checkItems.includes('sensitive')) {
    prompt += '2. 敏感词检测：识别违禁词、敏感词、不当表达\n'
  }
  if (reviewConfig.value.checkItems.includes('quality')) {
    prompt += '3. 质量评估：评估文案质量、专业性、吸引力\n'
  }
  if (reviewConfig.value.checkItems.includes('completeness')) {
    prompt += '4. 完整性检查：检查必填项、信息完整度\n'
  }
  if (reviewConfig.value.checkItems.includes('format')) {
    prompt += '5. 格式验证：检查格式规范、链接有效性\n'
  }

  // 添加自定义规则
  if (customRules.value.length > 0) {
    prompt += '\n自定义规则：\n'
    customRules.value.forEach((rule, index) => {
      prompt += `${index + 1}. ${getFieldLabel(rule.field)} - ${rule.type}: ${rule.value} (${rule.message})\n`
    })
  }

  prompt += '\n待审核内容：\n'
  Object.entries(reviewContent.value).forEach(([key, value]) => {
    prompt += `【${getFieldLabel(key)}】: ${value}\n`
  })

  if (reviewConfig.value.autoFix) {
    prompt += '\n请为发现的问题提供具体的修复建议和修复后的内容。\n'
  }

  prompt += '\n请按以下JSON格式返回审核结果：\n'
  prompt += `{
  "status": "pass|warning|fail",
  "score": 4.5,
  "details": {
    "compliance": 95,
    "quality": 85,
    "completeness": 90
  },
  "issues": [
    {
      "field": "字段名",
      "severity": "low|medium|high",
      "message": "问题描述",
      "content": "有问题的内容",
      "location": "具体位置",
      "suggestion": "修复建议",
      "fixedContent": "修复后的内容"
    }
  ]
}\n`

  prompt += '\n如果没有问题，issues数组为空即可。请直接返回JSON，不要添加任何markdown标记。'

  return prompt
}

/** 审核内容 */
const handleReview = async () => {
  loading.value = true
  reviewResult.value = null
  fieldStatus.value = {}

  try {
    const prompt = buildReviewPrompt()

    emit('process', {
      config: reviewConfig.value,
      content: reviewContent.value,
      rules: customRules.value
    })

    const bo: AiChatBo = {
      message: prompt,
      systemPrompt: props.role || '你是一个专业的内容审核专家，擅长识别内容中的各类问题并提供专业的修复建议。',
      provider: props.provider,
      modelName: props.modelName,
      temperature: 0.3 // 审核需要更稳定的输出
    }

    const [err, response] = await aiReview(bo)

    if (err) {
      console.error('审核失败:', err)
      emit('error', err)
      showMsgError(t('ai.contentReviewer.reviewFailed'))
      return
    }

    if (!response) {
      const noDataError = new Error('响应数据为空')
      emit('error', noDataError)
      showMsgError(t('ai.contentReviewer.reviewFailed'))
      return
    }

    if (response.content) {
      // 解析返回的JSON
      const jsonMatch = response.content.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const result = JSON.parse(jsonMatch[0])
        result.timestamp = new Date().toLocaleString()

        reviewResult.value = result

        // 更新字段状态
        if (result.issues && result.issues.length > 0) {
          result.issues.forEach((issue: ReviewIssue) => {
            if (!fieldStatus.value[issue.field] || getSeverityLevel(issue.severity) > getSeverityLevel(fieldStatus.value[issue.field])) {
              fieldStatus.value[issue.field] = severityToStatus(issue.severity)
            }
          })
        }

        emit('result', result)
        emit('review-complete', result)

        if (result.status === 'pass') {
          showMsgSuccess(t('ai.contentReviewer.reviewPassedMsg'))
        } else if (result.status === 'warning') {
          showMsgWarning(t('ai.contentReviewer.foundWarningIssues', { count: result.issues?.length || 0 }))
        } else {
          showMsgError(t('ai.contentReviewer.foundSevereIssues', { count: result.issues?.length || 0 }))
        }
      } else {
        throw new Error('无法解析审核结果')
      }
    }
  } catch (error) {
    console.error('审核失败:', error)
    emit('error', error as Error)
    showMsgError(t('ai.contentReviewer.reviewFailed'))
  } finally {
    loading.value = false
  }
}

/** 严重程度转状态 */
const severityToStatus = (severity: Severity): ReviewStatus => {
  if (severity === 'high') return 'fail'
  if (severity === 'medium') return 'warning'
  return 'pass'
}

/** 获取严重程度级别 */
const getSeverityLevel = (severity: Severity | ReviewStatus): number => {
  const levelMap: Record<string, number> = {
    low: 1,
    pass: 1,
    medium: 2,
    warning: 2,
    high: 3,
    fail: 3
  }
  return levelMap[severity] || 0
}

/** 应用修复 */
const handleApplyFix = (issue: ReviewIssue) => {
  if (issue.fixedContent) {
    reviewContent.value[issue.field] = issue.fixedContent
    showMsgSuccess(t('ai.contentReviewer.fixApplied'))
  }
}

/** 忽略问题 */
const handleIgnoreIssue = (index: number) => {
  if (reviewResult.value?.issues) {
    reviewResult.value.issues.splice(index, 1)
    showMsg(t('ai.contentReviewer.issueIgnored'))
  }
}

/** 应用所有修复 */
const handleApplyAllFixes = () => {
  if (reviewResult.value?.issues) {
    let fixCount = 0
    reviewResult.value.issues.forEach((issue) => {
      if (issue.fixedContent) {
        reviewContent.value[issue.field] = issue.fixedContent
        fixCount++
      }
    })

    if (fixCount > 0) {
      showMsgSuccess(t('ai.contentReviewer.fixesApplied', { count: fixCount }))
      // 清空已修复的问题
      reviewResult.value.issues = reviewResult.value.issues.filter((issue) => !issue.fixedContent)
    }
  }
}

/** 重新审核 */
const handleReReview = () => {
  handleReview()
}

/** 导出报告 */
const handleExportReport = () => {
  if (!reviewResult.value) return

  const report = {
    [t('ai.contentReviewer.report.reviewTime')]: reviewResult.value.timestamp,
    [t('ai.contentReviewer.report.reviewLevel')]: getLevelText(reviewConfig.value.level),
    [t('ai.contentReviewer.report.reviewStatus')]: getOverallStatusText(reviewResult.value.status),
    [t('ai.contentReviewer.report.overallScore')]: reviewResult.value.score,
    [t('ai.contentReviewer.report.scoreDetails')]: reviewResult.value.details,
    [t('ai.contentReviewer.report.issuesList')]: reviewResult.value.issues,
    [t('ai.contentReviewer.report.reviewContent')]: reviewContent.value
  }

  const dataStr = JSON.stringify(report, null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `review_report_${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)

  showMsgSuccess(t('ai.contentReviewer.reportExported'))
}

/** 监听content变化 */
watch(
  () => props.content,
  (newContent) => {
    reviewContent.value = { ...newContent }
  },
  { deep: true }
)

/** 监听fields变化，初始化reviewContent */
watch(
  () => props.fields,
  (newFields) => {
    if (newFields && newFields.length > 0 && Object.keys(reviewContent.value).length === 0) {
      // 如果没有传入content，从fields初始化空对象
      const content: Record<string, any> = {}
      newFields.forEach((field) => {
        if (props.content && props.content[field.prop]) {
          content[field.prop] = props.content[field.prop]
        } else {
          content[field.prop] = ''
        }
      })
      reviewContent.value = content
    }
  },
  { immediate: true, deep: true }
)

/** 暴露方法 */
defineExpose({
  /** 获取审核结果 */
  getResult: () => reviewResult.value,
  /** 重新审核 */
  review: handleReview,
  /** 清空结果 */
  clear: () => {
    reviewResult.value = null
    fieldStatus.value = {}
  }
})
</script>

<style scoped>
.ai-content-reviewer {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-section,
.content-section,
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

.content-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.rules-section {
  margin-top: 8px;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}

.action-section {
  margin: 8px 0;
}

.overall-score {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-label {
  min-width: 80px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.issues-section {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.issues-list {
  margin-top: 12px;
}

.issue-title {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.issue-field {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.issue-message {
  flex: 1;
  color: var(--el-text-color-secondary);
}

.issue-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}

.issue-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  gap: 12px;
}

.detail-label {
  min-width: 80px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
}

.detail-value {
  flex: 1;
  color: var(--el-text-color-primary);
}

.highlight-text {
  padding: 8px;
  background: var(--el-color-danger-light-9);
  border-left: 3px solid var(--el-color-danger);
  border-radius: 4px;
}

.suggestion-text {
  padding: 8px;
  background: var(--el-color-warning-light-9);
  border-left: 3px solid var(--el-color-warning);
  border-radius: 4px;
}

.fixed-text {
  padding: 8px;
  background: var(--el-color-success-light-9);
  border-left: 3px solid var(--el-color-success);
  border-radius: 4px;
}

.issue-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pass-section {
  margin: 20px 0;
}

.summary-section {
  margin-top: 20px;
}

.result-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 20px;
}

.ml-1 {
  margin-left: 4px;
}
</style>
