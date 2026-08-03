<!-- 模板配置 -->
<template>
  <AModal v-model="dialogVisible" title="订阅配置管理" size="large" :loading="saveLoading" @confirm="handleSave" @cancel="handleClose">
    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb-4">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">
          {{ t('新增') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
          {{ t('修改') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
          {{ t('删除') }}
        </el-button>
      </el-col>
    </el-row>

    <!-- 配置列表表格 -->
    <el-table ref="templateTableRef" :data="templateList" stripe max-height="600px" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="t('templateId', '模板ID')" prop="templateId" align="center" min-width="100" />
      <el-table-column :label="t('title', '模板标题')" prop="title" align="center" min-width="120" />
      <el-table-column :label="t('content', '模板内容')" prop="content" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column :label="t('fields', '字段列表')" prop="fields" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column :label="t('status', '状态')" prop="status" align="center" width="80">
        <template #default="{ row }">
          <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="110" />
      <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="110" />
      <el-table-column :label="t('remark', '备注')" prop="remark" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column :label="t('操作')" align="center" width="120" fixed="right">
        <template #default="{ row }">
          <el-tooltip :content="t('修改')" placement="top">
            <el-button link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
          </el-tooltip>
          <el-tooltip :content="t('删除')" placement="top">
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改订阅配置对话框 -->
    <AModal
      v-model="templateDialog.visible"
      :title="templateDialog.title"
      mode="dialog"
      size="medium"
      :loading="templateButtonLoading"
      @confirm="submitTemplateForm"
      @cancel="cancelTemplate"
    >
      <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="auto">
        <el-row>
          <AFormInput label="模板ID" v-model="templateForm.templateId" prop="templateId" :span="24"></AFormInput>
          <AFormInput label="模板标题" v-model="templateForm.title" prop="title" :span="24"></AFormInput>
          <AFormInput type="textarea" label="模板内容" v-model="templateForm.content" prop="content" :span="24"></AFormInput>
          <AFormInput label="字段列表" v-model="templateForm.fields" prop="fields" :span="24" placeholder="请输入字段列表，逗号隔开"></AFormInput>
          <AFormRadio label="是否启用" v-model="templateForm.status" prop="status" :options="sys_enable_status" :span="24"></AFormRadio>
          <AFormInput type="textarea" label="备注" v-model="templateForm.remark" prop="remark" :span="24"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 主弹窗底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">{{ t('取消') }}</el-button>
        <el-button :loading="saveLoading" type="primary" @click="handleSave">{{ t('保存') }}</el-button>
      </div>
    </template>
  </AModal>
</template>

<script setup lang="ts" name="TemplateConfigs">
import type { TemplateConfig } from '@/api/business/base/platform/platformTypes'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showMsgError, showConfirm } from '@/utils/modal'
import { getCurrentDateTime } from '@/utils/date'
import { isTrue, toggleStatus } from '@/utils/boolean'

const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

interface Props {
  /**弹窗绑定*/
  modelValue: boolean
  /**配置列表*/
  templateConfigs?: string | TemplateConfig[]
}

// Emits 定义
interface Emits {
  (e: 'update:modelValue', value: boolean): void

  (e: 'save', configs: TemplateConfig[]): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  templateConfigs: () => []
})

const emit = defineEmits<Emits>()

const { t } = useI18n()

// 响应式数据
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**模板列表*/
const templateList = ref<TemplateConfig[]>([])
/**选择项*/
const selectionItems = ref<TemplateConfig[]>([])
/**保存加载中*/
const saveLoading = ref(false)

// =========== 订阅配置表单相关 ===========
/**初始表单数据*/
const initTemplateFormData: TemplateConfig = {
  id: undefined,
  templateId: undefined,
  title: undefined,
  content: undefined,
  fields: undefined,
  status: '1', // 默认启用
  remark: undefined
}

/** 生成唯一ID */
let nextId = 1
const generateId = (): number => {
  return nextId++
}

/**表单引用*/
const templateFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const templateButtonLoading = ref(false)
/**对话框配置对象*/
const templateDialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const templateForm = ref<TemplateConfig>({ ...initTemplateFormData })
/**表单校验规则*/
const templateRules = ref<ElFormRules>({
  templateId: [{ required: true, message: t('templateId cannot be empty', '模板ID不能为空'), trigger: 'blur' }],
  title: [{ required: true, message: t('title cannot be empty', '模板标题不能为空'), trigger: 'blur' }],
  content: [{ required: true, message: t('content cannot be empty', '模板内容不能为空'), trigger: 'blur' }]
})

// 监听弹窗打开，初始化数据
watch(dialogVisible, (newValue) => {
  if (newValue) {
    initTemplateList()
  }
})

// 初始化模板列表
const initTemplateList = () => {
  try {
    if (typeof props.templateConfigs === 'string') {
      templateList.value = props.templateConfigs ? JSON.parse(props.templateConfigs) : []
    } else {
      templateList.value = Array.isArray(props.templateConfigs) ? [...props.templateConfigs] : []
    }

    // 为没有ID的配置项生成ID，并更新nextId
    templateList.value.forEach((item) => {
      if (!item.id) {
        item.id = generateId()
      } else if (typeof item.id === 'number' && item.id >= nextId) {
        nextId = item.id + 1
      }
    })
  } catch (error) {
    console.error('解析 templateConfigs 失败:', error)
    templateList.value = []
    showMsgSuccess(t('解析配置数据失败'))
  }
}

// 表格选择变化
const handleSelectionChange = (selection: TemplateConfig[]) => {
  selectionItems.value = selection
}

/** 订阅配置表单重置 */
const resetTemplate = () => {
  templateForm.value = { ...initTemplateFormData }
  templateFormRef.value?.resetFields()
}

/** 取消订阅配置编辑 */
const cancelTemplate = () => {
  resetTemplate()
  templateDialog.value.visible = false
}

/** 新增订阅配置操作 */
const handleAdd = () => {
  resetTemplate()
  templateDialog.value.visible = true
  templateDialog.value.title = `${t('新增')}${t('订阅配置')}`
}

/** 修改订阅配置操作 */
const handleUpdate = async (row?: TemplateConfig) => {
  resetTemplate()
  const itemToEdit = row || selectionItems.value[0]
  Object.assign(templateForm.value, itemToEdit)
  templateDialog.value.visible = true
  templateDialog.value.title = `${t('修改')}${t('订阅配置')}`
}

/** 提交订阅配置表单 */
const submitTemplateForm = async () => {
  const [validateErr] = await toValidate(templateFormRef)
  if (validateErr) return

  templateButtonLoading.value = true

  const now = getCurrentDateTime()

  // 判断是修改还是新增（通过 id 是否存在判断）
  const isEdit = !!templateForm.value.id

  if (isEdit) {
    // 修改：检查模板ID是否与其他配置重复（排除自己）
    const isDuplicate = templateList.value.some(
      (item) => item.id !== templateForm.value.id && item.templateId === templateForm.value.templateId
    )
    if (isDuplicate) {
      showMsgError(`模板ID ${templateForm.value.templateId} 已被其他配置使用，请使用其他ID`)
      templateButtonLoading.value = false
      return
    }

    // 查找原记录并更新
    const existingIndex = templateList.value.findIndex((item) => item.id === templateForm.value.id)
    if (existingIndex >= 0) {
      templateList.value[existingIndex] = {
        ...templateForm.value,
        updateTime: now
      }
      showMsgSuccess(t('message.updateSuccess'))
    }
  } else {
    // 新增：检查模板ID是否重复
    const isDuplicate = templateList.value.some((item) => item.templateId === templateForm.value.templateId)
    if (isDuplicate) {
      showMsgError(`模板ID ${templateForm.value.templateId} 已存在，请使用其他ID`)
      templateButtonLoading.value = false
      return
    }

    templateList.value.push({
      ...templateForm.value,
      id: generateId(),
      createTime: now,
      updateTime: now
    })
    showMsgSuccess(t('message.addSuccess'))
  }

  templateDialog.value.visible = false
  templateButtonLoading.value = false
}

/** 订阅配置启用禁用状态修改 */
const handleStatusChange = async (row: TemplateConfig) => {
  const text = isTrue(row.status) ? t('启用') : t('禁用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.title}(${row.templateId})?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }

  // 更新时间
  row.updateTime = getCurrentDateTime()
  showMsgSuccess(`${text}成功`)
}

/** 删除订阅配置操作 */
const handleDelete = async (row?: TemplateConfig) => {
  const itemsToDelete = row ? [row] : selectionItems.value
  if (itemsToDelete.length === 0) return

  const itemNames = itemsToDelete.map((item) => item.title).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemNames}`)
  if (confirmErr) return

  // 删除选中的配置（通过id匹配）
  itemsToDelete.forEach((itemToDelete) => {
    const index = templateList.value.findIndex((item) => item.id === itemToDelete.id)
    if (index >= 0) {
      templateList.value.splice(index, 1)
    }
  })

  showMsgSuccess(t('message.deleteSuccess'))
  selectionItems.value = []
}

// 保存配置
const handleSave = async () => {
  saveLoading.value = true
  try {
    emit('save', [...templateList.value])
    dialogVisible.value = false
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 取消配置
const handleCancel = () => {
  dialogVisible.value = false
}

// 关闭弹窗
const handleClose = () => {
  selectionItems.value = []
  templateList.value = []
}
</script>

<style scoped></style>
