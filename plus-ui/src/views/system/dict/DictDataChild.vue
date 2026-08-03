<!-- 字典数据 -->
<template>
  <div class="h-full flex flex-col">
    <!-- 搜索和工具栏 -->
    <div class="flex justify-between items-center mb-4 flex-wrap gap-3 flex-shrink-0">
      <div class="flex items-center gap-2 flex-wrap">
        <span class="text-sm text-[var(--el-text-color-regular)]">{{ t('Dict Label', '字典标签') }}</span>
        <AFormInput
          v-model="queryParams.searchValue"
          :placeholder="t('Please input dict label', '请输入字典标签')"
          prefix-icon="search"
          clearable
          :show-form-item="false"
          :width="160"
          @input="handleQuery"
        />
        <span class="text-sm text-[var(--el-text-color-regular)] ml-2">{{ t('Status', '状态') }}</span>
        <AFormSelect
          v-model="queryParams.status"
          :options="sys_enable_status"
          :placeholder="t('Please Select', '请选择')"
          clearable
          :show-form-item="false"
          :width="160"
          @change="handleQuery"
        />
      </div>
      <div v-if="!props.readonly" class="flex items-center gap-2">
        <el-button v-permi="['system:dict:add']" type="primary" icon="Plus" @click="handleAdd">{{ t('新增') }}</el-button>
        <AImportExcel
          v-permi="['system:dict:add']"
          v-slot="{ openImportExcel }"
          :title="t('Dict Data', '字典数据')"
          template-url="/system/dictData/templateDictDatas"
          import-url="/system/dictData/importDictDatas"
          :import-params="{ dictType: props.dictType }"
          @import-success="handleImportSuccess"
        >
          <el-button type="info" plain icon="Top" @click="openImportExcel">{{ t('导入') }}</el-button>
        </AImportExcel>
        <el-button v-permi="['system:dict:export']" type="warning" plain icon="Download" @click="handleExport">
          {{ t('导出') }}
        </el-button>
        <el-button
          v-permi="['system:dict:delete']"
          type="danger"
          plain
          icon="Delete"
          :disabled="selectionItems.length === 0 || props.isSystem"
          @click="handleBatchDelete"
        >
          {{ t('删除') }}
        </el-button>
      </div>
    </div>

    <!-- 字典数据表格 -->
    <el-table
      ref="dictDataTableRef"
      v-loading="isLoading"
      :data="dictDataList"
      :max-height="tableHeight"
      stripe
      class="flex-1"
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="!props.readonly" type="selection" width="50" align="center" :selectable="checkSelectable" />
      <el-table-column :label="t('Label', '标签')" prop="dictLabel" align="center" min-width="120">
        <template #default="{ row }">
          <span v-if="(row.listClass === '' || row.listClass === 'default') && (row.cssClass === '' || row.cssClass == null)">
            {{ row.dictLabel }}
          </span>
          <el-tag v-else :type="getTagType(row.listClass)" :class="row.cssClass">
            {{ row.dictLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('Value', '值')" prop="dictValue" align="center" min-width="100">
        <template #default="{ row }">
          <span class="cursor-pointer font-mono hover:text-[var(--el-color-primary)]" @click="copy(row.dictValue)">
            {{ row.dictValue }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('Style', '样式')" prop="listClass" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="getTagType(row.listClass)" size="small" v-if="row.listClass">{{ getStyleLabel(row.listClass) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('Sort', '排序')" prop="dictSort" align="center" width="70" />
      <el-table-column :label="t('Remark', '备注')" prop="remark" align="center" min-width="120" show-overflow-tooltip />
      <el-table-column :label="t('Status', '状态')" prop="status" align="center" width="80">
        <template #default="{ row }">
          <AFormSwitch
            v-if="!props.readonly"
            v-model="row.status"
            :disabled="props.isSystem && !isSuperAdminUser"
            @change="handleStatusChange(row)"
          />
          <el-tag v-else :type="row.status === '1' ? 'success' : 'danger'" size="small">
            {{ row.status === '1' ? t('Normal', '正常') : t('Disabled', '停用') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!props.readonly" :label="t('Operation', '操作')" align="center" width="100" fixed="right">
        <template #default="{ row }">
          <el-tooltip
            :content="props.isSystem && !isSuperAdminUser ? t('System dict data cannot be modified', '系统级字典数据不允许修改') : t('修改')"
            placement="top"
          >
            <el-button
              v-permi="['system:dict:update']"
              :disabled="props.isSystem && !isSuperAdminUser"
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(row)"
            />
          </el-tooltip>
          <el-tooltip :content="props.isSystem ? t('System dict data cannot be deleted', '系统级字典数据不允许删除') : t('删除')" placement="top">
            <el-button v-permi="['system:dict:delete']" :disabled="props.isSystem" link type="danger" icon="Delete" @click="handleDelete(row)" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 100, 1000]"
      :pager-count="5"
      layout="sizes, prev, pager, next"
      :auto-scroll="false"
      @pagination="getList"
    />

    <!-- 添加或修改字典数据对话框 -->
    <AModal
      v-if="!props.readonly"
      v-model="dialog.visible"
      :title="dialog.title"
      mode="dialog"
      size="small"
      :loading="buttonLoading"
      @confirm="submitForm"
      @cancel="cancel"
    >
      <el-form ref="dictDataFormRef" :model="form" :rules="rules" label-width="100px">
        <AFormInput :label="t('Dict Code', '字典编码')" v-model="form.dictType" prop="dictType" :disabled="true" :span="24" />
        <AFormInput :label="t('Data Label', '数据标签')" v-model="form.dictLabel" prop="dictLabel" :span="24" />
        <AFormInput :label="t('Data Value', '数据键值')" v-model="form.dictValue" prop="dictValue" :span="24" />
        <AFormInput :label="t('CSS Class', '样式属性')" v-model="form.cssClass" prop="cssClass" :span="24" />
        <AFormInput :label="t('Sort Order', '显示排序')" type="number" v-model="form.dictSort" prop="dictSort" :span="24" />
        <AFormSelect
          :label="t('Tag Style', '回显样式')"
          v-model="form.listClass"
          prop="listClass"
          :options="listClassOptions"
          option-label="label"
          option-value="value"
          :span="24"
        />
        <AFormInput :label="t('Remark', '备注')" v-model="form.remark" prop="remark" type="textarea" :span="24" />
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="DictDataPanel">
import { pageDictDatas, getDictData, addDictData, updateDictData, deleteDictDatas } from '@/api/system/dict/dictData/dictDataApi'
import type { SysDictDataQuery, SysDictDataBo, SysDictDataVo } from '@/api/system/dict/dictData/dictDataTypes'
import { toggleStatus } from '@/utils/boolean'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { copy } from '@/utils/function'
import { useAuth } from '@/composables/useAuth'

const { t } = useI18n()

/**字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// 检查是否为超级管理员
const { isSuperAdmin } = useAuth()
const isSuperAdminUser = computed(() => isSuperAdmin())

// Props 定义
interface DictDataPanelProps {
  /** 字典类型ID */
  dictId?: string | number
  /** 字典类型编码 */
  dictType?: string
  /** 是否为系统级字典 */
  isSystem?: boolean
  /** 只读模式 */
  readonly?: boolean
}

const props = defineProps<DictDataPanelProps>()

// =========== 表格高度 ===========
const { tableHeight } = useTableHeight()

// =========== 查询相关 ===========
const queryParams = ref<SysDictDataQuery>({
  pageNum: 1,
  pageSize: 10,
  dictType: '',
  dictLabel: '',
  status: undefined
})
const total = ref(0)

/** 搜索 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// =========== 表格数据相关 ===========
const isLoading = ref(false)
const dictDataList = ref<SysDictDataVo[]>([])
const dictDataTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysDictDataVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysDictDataVo[]) => {
  selectionItems.value = selection
}

/** 检查行是否可选（系统级字典不可选） */
const checkSelectable = () => {
  return !props.isSystem
}

/** 获取标签类型 */
const getTagType = (listClass: string) => {
  if (listClass === 'primary' || listClass === 'default') return 'primary'
  return listClass as any
}

/** 获取样式标签 */
const getStyleLabel = (listClass: string) => {
  const styleMap: Record<string, string> = {
    default: t('Default', '默认'),
    primary: t('Primary', '主要'),
    success: t('Success', '成功'),
    info: t('Info', '信息'),
    warning: t('Warning', '警告'),
    danger: t('Danger', '危险')
  }
  return styleMap[listClass] || listClass
}

/** 查询字典数据列表 */
const getList = async () => {
  if (!props.dictType) return

  isLoading.value = true
  queryParams.value.dictType = props.dictType

  const [err, data] = await pageDictDatas(queryParams.value)
  if (!err) {
    dictDataList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 删除字典数据 */
const handleDelete = async (row: SysDictDataVo) => {
  if (props.isSystem) {
    showMsgSuccess(t('System dict data cannot be deleted', '系统级字典数据不允许删除'))
    return
  }

  const [confirmErr] = await showConfirm(`${t('Confirm delete', '是否确认删除')}${row.dictLabel}(${row.dictValue})`)
  if (confirmErr) return

  const [deleteErr] = await deleteDictDatas([row.dictDataId])
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    useDictStore().removeDict(props.dictType!)
    await getList()
  }
}

/** 批量删除字典数据 */
const handleBatchDelete = async () => {
  if (props.isSystem) {
    showMsgSuccess(t('System dict data cannot be deleted', '系统级字典数据不允许删除'))
    return
  }

  if (selectionItems.value.length === 0) return

  const labels = selectionItems.value.map((item) => item.dictLabel).join(', ')
  const [confirmErr] = await showConfirm(`${t('Confirm delete', '是否确认删除')}${labels}`)
  if (confirmErr) return

  const ids = selectionItems.value.map((item) => item.dictDataId)
  const [deleteErr] = await deleteDictDatas(ids)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    useDictStore().removeDict(props.dictType!)
    selectionItems.value = []
    await getList()
  }
}

/** 导入成功回调 */
const handleImportSuccess = () => {
  useDictStore().removeDict(props.dictType!)
  getList()
}

/** 导出字典数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Dict Data', '字典数据'), '/system/dictData/exportDictDatas', queryParams.value)
}

// =========== 表单相关 ===========
const listClassOptions = computed(() => [
  { value: 'default', label: t('Default', '默认') },
  { value: 'primary', label: t('Primary', '主要') },
  { value: 'success', label: t('Success', '成功') },
  { value: 'info', label: t('Info', '信息') },
  { value: 'warning', label: t('Warning', '警告') },
  { value: 'danger', label: t('Danger', '危险') }
])

/**初始表单数据*/
const initFormData: SysDictDataBo = {
  dictDataId: undefined,
  dictLabel: '',
  dictValue: '',
  dictType: '',
  cssClass: '',
  listClass: 'primary',
  dictSort: 0,
  remark: ''
}

/**表单引用*/
const dictDataFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysDictDataBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  dictLabel: [{ required: true, message: t('dictLabel cannot be empty', '数据标签不能为空'), trigger: 'blur' }],
  dictValue: [{ required: true, message: t('dictValue cannot be empty', '数据键值不能为空'), trigger: 'blur' }],
  dictSort: [{ required: true, message: t('dictSort cannot be empty', '数据顺序不能为空'), trigger: 'blur' }]
})

/** 字典数据表单重置 */
const reset = () => {
  form.value = {
    ...initFormData,
    dictType: props.dictType || ''
  }
  dictDataFormRef.value?.resetFields()
}

/** 取消字典数据编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增字典数据操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('dictData', '字典数据')}`
}

/** 修改字典数据操作 */
const handleUpdate = async (row: SysDictDataVo) => {
  if (props.isSystem && !isSuperAdminUser.value) {
    showMsgSuccess(t('System dict data cannot be modified', '系统级字典数据不允许修改'))
    return
  }

  reset()
  const [err, data] = await getDictData(row.dictDataId)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('dictData', '字典数据')}`
  }
}

/** 提交字典数据表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(dictDataFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.dictDataId) {
    ;[err] = await updateDictData(form.value)
  } else {
    ;[err] = await addDictData(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.dictDataId ? t('message.updateSuccess') : t('message.addSuccess'))
    useDictStore().removeDict(props.dictType!)
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 字典数据启用禁用状态修改  */
const handleStatusChange = async (row: SysDictDataVo) => {
  if (props.isSystem && !isSuperAdminUser.value) {
    row.status = toggleStatus(row.status)
    showMsgSuccess(t('System dict data cannot be modified', '系统级字典数据不允许修改'))
    return
  }

  const text = row.status === '1' ? t('Enable', '启用') : t('Disable', '停用')
  const [confirmErr] = await showConfirm(`${t('Confirm', '是否确认')}${text}${row.dictLabel}:${row.dictValue}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateDictData(row)
  if (!updateErr) {
    useDictStore().removeDict(props.dictType!)
    await getList()
    showMsgSuccess(`${text}${t('Success', '成功')}`)
  } else {
    row.status = toggleStatus(row.status)
  }
}

// 监听字典类型变化
watch(
  () => props.dictType,
  (newType) => {
    if (newType) {
      queryParams.value.dictType = newType
      getList()
    } else {
      dictDataList.value = []
    }
  },
  { immediate: true }
)
</script>
