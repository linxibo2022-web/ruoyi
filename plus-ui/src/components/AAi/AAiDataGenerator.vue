<!--
  AAiDataGenerator 智能数据生成器组件

  功能：
  - 根据字段schema生成测试数据
  - 批量生成多条数据
  - 生成真实感数据
  - 字段关联生成
  - 数据预览和编辑
-->

<template>
  <div class="ai-data-generator">
    <!-- 生成配置 -->
    <div class="config-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.dataGenerator.generateConfig') }}</span>
      </div>

      <el-form :model="generateConfig" label-width="100px" size="small">
        <el-form-item :label="t('ai.dataGenerator.generateCount')">
          <el-input-number v-model="generateConfig.count" :min="1" :max="100" controls-position="right" />
        </el-form-item>

        <el-form-item :label="t('ai.dataGenerator.dataType')">
          <el-radio-group v-model="generateConfig.dataType">
            <el-radio value="test">{{ t('ai.dataGenerator.testData') }}</el-radio>
            <el-radio value="realistic">{{ t('ai.dataGenerator.realisticData') }}</el-radio>
            <el-radio value="demo">{{ t('ai.dataGenerator.demoData') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="t('ai.dataGenerator.locale')">
          <el-select v-model="generateConfig.locale" :placeholder="t('ai.dataGenerator.selectLocale')">
            <el-option :label="t('ai.dataGenerator.locales.zhCN')" value="zh_CN" />
            <el-option :label="t('ai.dataGenerator.locales.zhHK')" value="zh_HK" />
            <el-option :label="t('ai.dataGenerator.locales.zhTW')" value="zh_TW" />
            <el-option :label="t('ai.dataGenerator.locales.enUS')" value="en_US" />
            <el-option :label="t('ai.dataGenerator.locales.jaJP')" value="ja_JP" />
            <el-option :label="t('ai.dataGenerator.locales.koKR')" value="ko_KR" />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('ai.dataGenerator.fieldRelation')">
          <el-switch v-model="generateConfig.related" :active-text="t('common.enable')" :inactive-text="t('common.disable')" />
          <el-tooltip :content="t('ai.dataGenerator.fieldRelationTip')" placement="top">
            <el-icon class="ml-1"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>

        <el-form-item :label="t('ai.dataGenerator.uniqueness')">
          <el-switch v-model="generateConfig.unique" :active-text="t('common.enable')" :inactive-text="t('common.disable')" />
          <el-tooltip :content="t('ai.dataGenerator.uniquenessTip')" placement="top">
            <el-icon class="ml-1"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
      </el-form>
    </div>

    <!-- 字段配置 -->
    <div class="fields-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.dataGenerator.fieldConfig') }}</span>
        <el-button link type="primary" size="small" @click="handleAutoDetect">
          <el-icon><MagicStick /></el-icon>
          {{ t('ai.dataGenerator.autoDetect') }}
        </el-button>
      </div>

      <div class="fields-list">
        <div v-for="(field, index) in fieldList" :key="index" class="field-item">
          <div class="field-info">
            <el-checkbox v-model="field.enabled" />
            <span class="field-name">{{ field.label }}</span>
            <el-tag size="small" :type="getFieldTypeColor(field.type)">
              {{ field.type }}
            </el-tag>
          </div>

          <div v-if="field.enabled" class="field-config">
            <el-input v-if="field.type === 'string'" v-model="field.example" :placeholder="t('ai.dataGenerator.exampleOptional')" size="small" />
            <el-select
              v-else-if="field.type === 'enum'"
              v-model="field.options"
              multiple
              filterable
              allow-create
              :placeholder="t('ai.dataGenerator.enumOptions')"
              size="small"
            />
            <el-input v-else v-model="field.format" :placeholder="t('ai.dataGenerator.formatOptional')" size="small" />
          </div>

          <el-button link type="danger" size="small" @click="handleRemoveField(index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>

        <el-button type="primary" plain size="small" @click="handleAddField" block>
          <el-icon><Plus /></el-icon>
          {{ t('ai.dataGenerator.addField') }}
        </el-button>
      </div>
    </div>

    <!-- 参考数据 -->
    <el-collapse v-model="activeCollapse" class="reference-section">
      <el-collapse-item :title="t('ai.dataGenerator.referenceData')" name="reference">
        <div class="reference-content">
          <el-input v-model="referenceDataText" type="textarea" :rows="4" :placeholder="t('ai.dataGenerator.referencePlaceholder')" />
          <div class="reference-actions">
            <el-button size="small" @click="handleParseReference">{{ t('ai.dataGenerator.parseReference') }}</el-button>
            <el-button size="small" @click="handleLoadFromTable">{{ t('ai.dataGenerator.loadFromTable') }}</el-button>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 生成按钮 -->
    <div class="action-section">
      <el-button type="primary" :loading="loading" :disabled="!canGenerate" @click="handleGenerate" block>
        <el-icon v-if="!loading"><MagicStick /></el-icon>
        {{ loading ? t('ai.dataGenerator.generating') : t('ai.dataGenerator.generateCount', { count: generateConfig.count }) }}
      </el-button>
    </div>

    <!-- 生成结果 -->
    <div v-if="generatedData.length > 0" class="result-section">
      <div class="section-header">
        <span class="section-title">{{ t('ai.dataGenerator.generateResult') }}</span>
        <div class="result-actions">
          <el-tag size="small">{{ t('ai.dataGenerator.totalCount', { count: generatedData.length }) }}</el-tag>
          <el-button link type="primary" size="small" @click="handleExportJson">
            <el-icon><Download /></el-icon>
            {{ t('ai.dataGenerator.exportJson') }}
          </el-button>
          <el-button link type="primary" size="small" @click="handleExportExcel">
            <el-icon><Document /></el-icon>
            {{ t('ai.dataGenerator.exportExcel') }}
          </el-button>
          <el-button link type="primary" size="small" @click="handleCopyAll">
            <el-icon><CopyDocument /></el-icon>
            {{ t('ai.dataGenerator.copyAll') }}
          </el-button>
        </div>
      </div>

      <!-- 数据预览表格 -->
      <el-table :data="generatedData" border stripe max-height="400" class="result-table">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column
          v-for="field in enabledFields"
          :key="field.name"
          :prop="field.name"
          :label="field.label"
          :min-width="getColumnWidth(field)"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="cell-content">
              <span v-if="field.type === 'image'">
                <el-image :src="row[field.name]" fit="cover" style="width: 40px; height: 40px" :preview-src-list="[row[field.name]]" />
              </span>
              <span v-else-if="field.type === 'date'">
                {{ formatDate(row[field.name]) }}
              </span>
              <span v-else>{{ row[field.name] }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="120" align="center" fixed="right">
          <template #default="{ row, $index }">
            <el-button link type="primary" size="small" @click="handleEditRow(row, $index)">{{ t('common.edit') }}</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteRow($index)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions">
        <el-button size="small" @click="handleRegenerateAll">
          <el-icon><RefreshRight /></el-icon>
          {{ t('ai.dataGenerator.regenerateAll') }}
        </el-button>
        <el-button size="small" @click="handleClearAll">
          <el-icon><Delete /></el-icon>
          {{ t('ai.dataGenerator.clearAll') }}
        </el-button>
      </div>
    </div>

    <!-- 编辑数据弹窗 -->
    <AModal v-model="editDialogVisible" :title="t('ai.dataGenerator.editData')" size="medium" @confirm="handleSaveEdit" @cancel="editDialogVisible = false">
      <el-form :model="editingData" label-width="100px">
        <el-form-item v-for="field in enabledFields" :key="field.name" :label="field.label">
          <el-input v-if="field.type === 'string' || field.type === 'number'" v-model="editingData[field.name]" />
          <el-date-picker v-else-if="field.type === 'date'" v-model="editingData[field.name]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          <el-select v-else-if="field.type === 'enum'" v-model="editingData[field.name]">
            <el-option v-for="option in field.options" :key="option" :label="option" :value="option" />
          </el-select>
          <AFormImgUpload v-else-if="field.type === 'image'" v-model="editingData[field.name]" />
        </el-form-item>
      </el-form>
    </AModal>

    <!-- 添加字段弹窗 -->
    <AModal v-model="addFieldDialogVisible" :title="t('ai.dataGenerator.addField')" size="small" @confirm="handleConfirmAddField" @cancel="addFieldDialogVisible = false">
      <el-form :model="newField" label-width="80px">
        <el-form-item :label="t('ai.dataGenerator.fieldName')" required>
          <el-input v-model="newField.name" :placeholder="t('ai.dataGenerator.fieldNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('ai.dataGenerator.fieldLabel')" required>
          <el-input v-model="newField.label" :placeholder="t('ai.dataGenerator.fieldLabelPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('ai.dataGenerator.fieldType')" required>
          <el-select v-model="newField.type">
            <el-option :label="t('ai.dataGenerator.types.string')" value="string" />
            <el-option :label="t('ai.dataGenerator.types.number')" value="number" />
            <el-option :label="t('ai.dataGenerator.types.date')" value="date" />
            <el-option :label="t('ai.dataGenerator.types.image')" value="image" />
            <el-option :label="t('ai.dataGenerator.types.enum')" value="enum" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ai.dataGenerator.required')">
          <el-switch v-model="newField.required" />
        </el-form-item>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="AAiDataGenerator">
import { MagicStick, Plus, Delete, Download, Document, CopyDocument, RefreshRight, QuestionFilled } from '@element-plus/icons-vue'
import { aiGenerate } from '@/api/business/base/ai/aiApi'
import type { AiChatBo } from '@/api/business/base/ai/aiTypes'
import { showMsgSuccess, showMsgWarning, showMsgError, showMsg } from '@/utils/modal'
import { copy } from '@/utils/function'
import { formatDate } from '@/utils/date'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

/** 字段配置 */
interface FieldSchema {
  name: string
  label: string
  type: 'string' | 'number' | 'date' | 'image' | 'enum'
  required?: boolean
  enabled?: boolean
  options?: string[]
  example?: string
  format?: string
  relation?: string
}

/** 生成配置 */
interface GenerateConfig {
  count: number
  dataType: 'test' | 'realistic' | 'demo'
  locale: string
  related: boolean
  unique: boolean
}

/** 组件Props */
interface AAiDataGeneratorProps {
  /** 字段schema */
  schema?: FieldSchema[]
  /** 生成数量 */
  count?: number
  /** 参考数据 */
  referenceData?: any[]
  /** 数据模板 */
  template?: any
  /** 模型提供商 */
  provider?: string
  /** 模型名称 */
  modelName?: string
  /** 角色设定 */
  role?: string
}

const props = withDefaults(defineProps<AAiDataGeneratorProps>(), {
  schema: () => [],
  count: 10,
  referenceData: () => [],
  provider: 'deepseek'
})

/** 组件事件 */
const emit = defineEmits<{
  'process': [params: any]
  'result': [data: any[]]
  'error': [error: Error]
  'generated': [data: any[]]
}>()

/** 生成配置 */
const generateConfig = ref<GenerateConfig>({
  count: props.count,
  dataType: 'realistic',
  locale: 'zh_CN',
  related: true,
  unique: true
})

/** 字段列表 */
const fieldList = ref<FieldSchema[]>([])

/** 激活的折叠面板 */
const activeCollapse = ref<string[]>([])

/** 参考数据文本 */
const referenceDataText = ref('')

/** 加载状态 */
const loading = ref(false)

/** 生成的数据 */
const generatedData = ref<any[]>([])

/** 编辑弹窗显示 */
const editDialogVisible = ref(false)

/** 正在编辑的数据 */
const editingData = ref<any>({})

/** 正在编辑的索引 */
const editingIndex = ref(-1)

/** 添加字段弹窗 */
const addFieldDialogVisible = ref(false)

/** 新字段 */
const newField = ref<FieldSchema>({
  name: '',
  label: '',
  type: 'string',
  required: false,
  enabled: true
})

/** 初始化字段列表 */
const initFields = () => {
  if (props.schema && props.schema.length > 0) {
    fieldList.value = props.schema.map((field) => ({
      ...field,
      enabled: true
    }))
  } else {
    // 默认字段
    fieldList.value = [
      { name: 'name', label: '名称', type: 'string', enabled: true, required: true },
      { name: 'description', label: '描述', type: 'string', enabled: true },
      { name: 'status', label: '状态', type: 'enum', enabled: true, options: ['启用', '禁用'] }
    ]
  }
}

/** 启用的字段 */
const enabledFields = computed(() => {
  return fieldList.value.filter((field) => field.enabled)
})

/** 是否可以生成 */
const canGenerate = computed(() => {
  return enabledFields.value.length > 0 && generateConfig.value.count > 0
})

/** 获取字段类型颜色 */
const getFieldTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    string: '',
    number: 'success',
    date: 'warning',
    image: 'danger',
    enum: 'info'
  }
  return (colorMap[type] || '') as ElTagType
}

/** 获取列宽度 */
const getColumnWidth = (field: FieldSchema) => {
  const widthMap: Record<string, number> = {
    string: 150,
    number: 100,
    date: 150,
    image: 100,
    enum: 120
  }
  return widthMap[field.type] || 150
}

/** 自动识别字段 */
const handleAutoDetect = () => {
  showMsg(t('ai.dataGenerator.autoDetectDeveloping'))
  // TODO: 可以从上下文或表单结构自动识别字段
}

/** 添加字段 */
const handleAddField = () => {
  newField.value = {
    name: '',
    label: '',
    type: 'string',
    required: false,
    enabled: true
  }
  addFieldDialogVisible.value = true
}

/** 确认添加字段 */
const handleConfirmAddField = () => {
  if (!newField.value.name || !newField.value.label) {
    showMsgWarning(t('ai.dataGenerator.fieldRequired'))
    return
  }

  fieldList.value.push({ ...newField.value })
  addFieldDialogVisible.value = false
  showMsgSuccess(t('common.addSuccess'))
}

/** 删除字段 */
const handleRemoveField = (index: number) => {
  fieldList.value.splice(index, 1)
}

/** 解析参考数据 */
const handleParseReference = () => {
  try {
    const data = JSON.parse(referenceDataText.value)
    if (Array.isArray(data) && data.length > 0) {
      showMsgSuccess(t('ai.dataGenerator.parsedSuccess', { count: data.length }))
    } else {
      showMsgWarning(t('ai.dataGenerator.invalidFormat'))
    }
  } catch (error) {
    showMsgError(t('ai.dataGenerator.jsonError'))
  }
}

/** 从表格加载 */
const handleLoadFromTable = () => {
  showMsg(t('ai.dataGenerator.loadFromTableDeveloping'))
  // TODO: 可以从当前页面的表格数据加载
}

/** 构建生成提示词 */
const buildGeneratePrompt = (): string => {
  const dataTypeDesc = {
    test: '测试数据，可以使用简单的占位符',
    realistic: '真实感数据，要符合实际业务场景，数据之间要有逻辑关联',
    demo: '演示数据，要有代表性和多样性'
  }

  const localeDesc = {
    zh_CN: '中国大陆，使用简体中文',
    zh_HK: '中国香港，使用繁体中文',
    zh_TW: '中国台湾，使用繁体中文',
    en_US: '美国，使用英语',
    ja_JP: '日本，使用日语',
    ko_KR: '韩国，使用韩语'
  }

  let prompt = `请帮我生成 ${generateConfig.value.count} 条${dataTypeDesc[generateConfig.value.dataType]}。`
  prompt += `\n地区设定：${localeDesc[generateConfig.value.locale] || generateConfig.value.locale}`

  if (generateConfig.value.related) {
    prompt += '\n注意：字段之间要保持逻辑一致性和关联性。'
  }

  if (generateConfig.value.unique) {
    prompt += '\n注意：关键字段（如ID、名称）不能重复。'
  }

  prompt += '\n\n字段定义：\n'
  enabledFields.value.forEach((field) => {
    prompt += `- ${field.label}（${field.name}）: 类型=${field.type}`
    if (field.required) prompt += '，必填'
    if (field.example) prompt += `，示例=${field.example}`
    if (field.format) prompt += `，格式=${field.format}`
    if (field.options && field.options.length > 0) {
      prompt += `，可选值=[${field.options.join(', ')}]`
    }
    prompt += '\n'
  })

  // 添加参考数据
  if (referenceDataText.value) {
    try {
      const refData = JSON.parse(referenceDataText.value)
      prompt += `\n参考数据示例：\n${JSON.stringify(refData.slice(0, 2), null, 2)}`
      prompt += '\n请学习参考数据的风格和特征。'
    } catch (error) {
      // 忽略解析错误
    }
  }

  prompt += '\n\n请直接返回JSON数组格式的数据，不要添加任何markdown标记或其他说明。'
  prompt += '\n返回格式示例：[{"field1": "value1", "field2": "value2"}, ...]'

  return prompt
}

/** 生成数据 */
const handleGenerate = async () => {
  loading.value = true

  try {
    const prompt = buildGeneratePrompt()

    emit('process', {
      config: generateConfig.value,
      fields: enabledFields.value
    })

    const bo: AiChatBo = {
      message: prompt,
      systemPrompt: props.role || '你是一个专业的测试数据生成助手，擅长生成符合业务场景的模拟数据。',
      provider: props.provider,
      modelName: props.modelName,
      temperature: 0.8
    }

    const [err, response] = await aiGenerate(bo)

    if (err) {
      console.error('Generate failed:', err)
      emit('error', err)
      showMsgError(t('ai.dataGenerator.generateFailed'))
      return
    }

    if (!response) {
      const noDataError = new Error('Response data is empty')
      emit('error', noDataError)
      showMsgError(t('ai.dataGenerator.generateFailed'))
      return
    }

    if (response.content) {
      // 解析返回的JSON数据
      const jsonMatch = response.content.match(/\[[\s\S]*\]/)
      if (jsonMatch) {
        const data = JSON.parse(jsonMatch[0])
        if (Array.isArray(data)) {
          generatedData.value = data
          emit('result', data)
          emit('generated', data)
          showMsgSuccess(t('ai.dataGenerator.generateSuccess', { count: data.length }))
        } else {
          throw new Error('Invalid data format')
        }
      } else {
        throw new Error('Cannot parse response data')
      }
    }
  } catch (error) {
    console.error('Generate failed:', error)
    emit('error', error as Error)
    showMsgError(t('ai.dataGenerator.generateFailed'))
  } finally {
    loading.value = false
  }
}

/** 编辑行 */
const handleEditRow = (row: any, index: number) => {
  editingData.value = { ...row }
  editingIndex.value = index
  editDialogVisible.value = true
}

/** 保存编辑 */
const handleSaveEdit = () => {
  if (editingIndex.value >= 0) {
    generatedData.value[editingIndex.value] = { ...editingData.value }
    editDialogVisible.value = false
    showMsgSuccess(t('ai.dataGenerator.saveSuccess'))
  }
}

/** 删除行 */
const handleDeleteRow = (index: number) => {
  generatedData.value.splice(index, 1)
  showMsgSuccess(t('ai.dataGenerator.deleteSuccess'))
}

/** 导出JSON */
const handleExportJson = () => {
  const dataStr = JSON.stringify(generatedData.value, null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `generated_data_${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)
  showMsgSuccess(t('ai.dataGenerator.exportSuccess'))
}

/** 导出Excel */
const handleExportExcel = () => {
  showMsg(t('ai.dataGenerator.exportExcelDeveloping'))
  // TODO: 使用 xlsx 或其他工具导出
}

/** 复制全部 */
const handleCopyAll = () => {
  const dataStr = JSON.stringify(generatedData.value, null, 2)
  copy(dataStr, t('ai.dataGenerator.copySuccess'))
}

/** 重新生成全部 */
const handleRegenerateAll = () => {
  handleGenerate()
}

/** 清空数据 */
const handleClearAll = () => {
  generatedData.value = []
  showMsgSuccess(t('ai.dataGenerator.clearSuccess'))
}

/** 初始化 */
onMounted(() => {
  initFields()
})

/** 监听schema变化 */
watch(
  () => props.schema,
  () => {
    if (props.schema && props.schema.length > 0) {
      initFields()
    }
  },
  { deep: true }
)

/** 暴露方法 */
defineExpose({
  /** 获取生成的数据 */
  getData: () => generatedData.value,
  /** 清空数据 */
  clear: handleClearAll,
  /** 重新生成 */
  regenerate: handleGenerate
})
</script>

<style scoped>
.ai-data-generator {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-section,
.fields-section,
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

.fields-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}

.field-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--el-bg-color);
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter);
}

.field-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
}

.field-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.field-config {
  flex: 1;
}

.reference-section {
  margin-top: 8px;
}

.reference-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reference-actions {
  display: flex;
  gap: 8px;
}

.action-section {
  margin: 8px 0;
}

.result-table {
  margin-top: 12px;
}

.cell-content {
  display: flex;
  align-items: center;
}

.batch-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
