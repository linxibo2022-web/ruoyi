<!-- 字典类型 -->
<template>
  <div class="h-full">
    <!-- 使用可拖拽面板组件 -->
    <AResizablePanels v-model:leftWidth="leftPanelWidth" :min-width="440" :max-width="700">
      <!-- 左侧面板：字典类型列表 -->
      <template #left>
        <el-card shadow="hover" class="h-full flex flex-col">
          <!-- 标题和搜索 -->
          <template #header>
            <div class="flex justify-between items-center mb-3">
              <span class="text-base font-semibold">{{ t('Dict List', '字典列表') }}（{{ total }}）</span>
              <el-input
                v-model="queryParams.searchValue"
                :placeholder="t('Search dict', '搜索字典名称或编码')"
                prefix-icon="Search"
                clearable
                style="width: 180px"
                @input="handleQuery"
              />
            </div>
            <!-- 按钮组 -->
            <div class="flex justify-end items-center gap-2">
              <el-button v-permi="['system:dict:add']" type="primary" size="small" icon="Plus" @click="handleAdd">
                {{ t('新增') }}
              </el-button>
              <AImportExcel
                v-permi="['system:dict:add']"
                v-slot="{ openImportExcel }"
                :title="t('Dict Type', '字典类型')"
                template-url="/system/dictType/templateDictTypes"
                import-url="/system/dictType/importDictTypes"
                @import-success="getList"
              >
                <el-button type="info" size="small" plain icon="Top" @click="openImportExcel">
                  {{ t('导入') }}
                </el-button>
              </AImportExcel>
              <el-button v-permi="['system:dict:export']" type="warning" size="small" plain icon="Download" @click="handleExport">
                {{ t('导出') }}
              </el-button>
              <el-button size="small" plain icon="Refresh" @click="resetQuery">{{ t('刷新') }}</el-button>
              <el-button type="success" size="small" plain icon="Refresh" @click="handleRefreshCache">
                {{ t('Flush', '刷新缓存') }}
              </el-button>
            </div>
          </template>

          <!-- 字典类型表格 -->
          <el-table
            ref="tableRef"
            v-loading="isLoading"
            :data="dictTypeList"
            :height="tableHeight"
            stripe
            highlight-current-row
            :current-row-key="selectedDictType?.dictId"
            row-key="dictId"
            @row-click="handleSelectDictType"
          >
            <el-table-column :label="t('Dict Name', '字典名称')" prop="dictName" align="center" min-width="90" show-overflow-tooltip />
            <el-table-column :label="t('Dict Code', '字典编码')" prop="dictType" align="center" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="cursor-pointer font-mono text-xs hover:text-[var(--el-color-primary)]" @click.stop="handleDictCodeClick(row)">
                  {{ row.dictType }}
                </span>
              </template>
            </el-table-column>
            <el-table-column :label="t('Status', '状态')" prop="status" align="center" width="70">
              <template #default="{ row }">
                <DictTag :options="sys_enable_status" :value="row.status" size="small" />
              </template>
            </el-table-column>
            <el-table-column :label="t('Level', '级别')" prop="isSystem" align="center" width="75">
              <template #default="{ row }">
                <DictTag :options="dictLevelOptions" :value="row.isSystem" size="small" />
              </template>
            </el-table-column>
            <el-table-column :label="t('Create Time', '创建时间')" prop="createTime" align="center" width="110">
              <template #default="{ row }">
                <span class="text-xs">{{ formatDate(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('Operation', '操作')" align="center" width="60">
              <template #default="{ row }">
                <el-tooltip :content="isSystemLevel(row) ? t('System dict cannot be deleted', '系统级字典不允许删除') : t('删除')" placement="top">
                  <el-button
                    v-permi="['system:dict:delete']"
                    :disabled="isSystemLevel(row)"
                    link
                    type="danger"
                    icon="Delete"
                    @click.stop="handleDelete(row)"
                  />
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <Pagination
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            :pager-count="5"
            size="small"
            layout="sizes, prev, pager, next"
            @pagination="getList"
          />
        </el-card>
      </template>

      <!-- 右侧面板：字典数据配置 -->
      <template #right>
        <el-card shadow="hover" class="h-full flex flex-col">
          <!-- 未选择字典类型时的空状态 -->
          <div v-if="!selectedDictType" class="h-full flex items-center justify-center">
            <el-empty :description="t('Please select a dict type from the left', '请从左侧选择一个字典类型')">
              <template #image>
                <el-icon :size="60" color="var(--el-color-info-light-3)"><Document /></el-icon>
              </template>
            </el-empty>
          </div>

          <!-- 字典数据面板 -->
          <div v-else class="h-full p-4 overflow-y-auto flex flex-col">
            <!-- 字典类型详情编辑区域 -->
            <div class="flex-shrink-0">
              <div class="mb-3">
                <el-form
                  ref="SysDictTypeBoRef"
                  :model="editForm"
                  :rules="rules"
                  inline
                  label-width="80px"
                  class="inline-edit-form flex flex-wrap items-center gap-2"
                >
                  <AFormInput
                    :label="t('Dict Name', '字典名称')"
                    v-model="editForm.dictName"
                    prop="dictName"
                    :disabled="!canEditSelectedDictType"
                    :width="160"
                  />
                  <AFormInput
                    :label="t('Dict Code', '字典编码')"
                    v-model="editForm.dictType"
                    prop="dictType"
                    :disabled="!canEditSelectedDictType"
                    :width="160"
                  />
                  <AFormSwitch
                    :label="t('Status', '状态')"
                    v-model="editForm.status"
                    prop="status"
                    :disabled="!canEditSelectedDictType"
                    :show-form-item="true"
                  />
                  <AFormSwitch
                    v-if="isSuperAdminUser"
                    :label="t('System Level', '系统级')"
                    v-model="editForm.isSystem"
                    prop="isSystem"
                    :show-form-item="true"
                  />
                </el-form>
              </div>
              <div class="flex justify-end gap-2">
                <el-button
                  v-permi="['system:dict:update']"
                  type="primary"
                  :loading="buttonLoading"
                  :disabled="!canEditSelectedDictType"
                  @click="handleSaveDictType"
                >
                  {{ t('button.save') }}
                </el-button>
                <el-button
                  v-permi="['system:dict:delete']"
                  type="danger"
                  plain
                  :disabled="isSystemLevel(selectedDictType)"
                  @click="handleDeleteSelected"
                >
                  {{ t('删除') }}
                </el-button>
              </div>
            </div>

            <!-- 字典数据子组件 -->
            <DictDataChild
              :key="selectedDictType.dictId"
              :dict-id="selectedDictType.dictId"
              :dict-type="selectedDictType.dictType"
              :is-system="isSystemLevel(selectedDictType)"
              :readonly="!canEditDictType(selectedDictType)"
              class="flex-1 mt-3"
            />
          </div>
        </el-card>
      </template>
    </AResizablePanels>

    <!-- 新增字典类型对话框 -->
    <AModal size="small" v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="addFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput :label="t('Dict Name', '字典名称')" v-model="form.dictName" prop="dictName" span="auto" />
          <AFormInput :label="t('Dict Code', '字典编码')" v-model="form.dictType" prop="dictType" span="auto" />
          <AFormRadio :label="t('Status', '状态')" v-model="form.status" prop="status" :options="sys_enable_status" span="auto" />
          <AFormRadio
            v-if="isSuperAdminUser"
            :label="t('Is System Level', '是否系统级')"
            v-model="form.isSystem"
            prop="isSystem"
            :options="[
              { label: t('No', '否'), value: '0' },
              { label: t('Yes', '是'), value: '1' }
            ]"
            span="auto"
          />
          <AFormInput :label="t('Remark', '备注')" v-model="form.remark" prop="remark" span="auto" />
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Dict">
import DictDataChild from './DictDataChild.vue'
import { Document } from '@element-plus/icons-vue'
import { pageDictTypes, getDictType, deleteDictTypes, addDictType, updateDictType, refreshDictCache } from '@/api/system/dict/dictType/dictTypeApi'
import type { SysDictTypeQuery, SysDictTypeBo, SysDictTypeVo } from '@/api/system/dict/dictType/dictTypeTypes'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { copy } from '@/utils/function'
import { useAuth } from '@/composables/useAuth'

const { t } = useI18n()

// 检查是否为超级管理员
const { isSuperAdmin } = useAuth()
const isSuperAdminUser = computed(() => isSuperAdmin())
/**字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

// 表格高度计算
const { tableHeight } = useTableHeight(10)

/** 字典级别选项 */
const dictLevelOptions = computed<DictItem[]>(() => [
  { label: t('Tenant Level', '租户级'), value: '0', elTagType: 'success' },
  { label: t('System Level', '系统级'), value: '1', elTagType: 'primary' }
])

/**左侧面板宽度*/
const leftPanelWidth = ref(440)

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysDictTypeQuery>({
  pageNum: 1,
  pageSize: 10,
  searchValue: '',
  status: undefined
})

/** 字典类型搜索按钮操作 */
const handleQuery = async () => {
  queryParams.value.pageNum = 1
  await getList()
}

/** 字典类型重置按钮操作 */
const resetQuery = () => {
  queryParams.value.searchValue = ''
  queryParams.value.status = undefined
  handleQuery()
}

// =========== 字典类型列表数据相关 ===========
/**列表加载状态*/
const isLoading = ref(true)
/**数据列表*/
const dictTypeList = ref<SysDictTypeVo[]>([])
/**总记录数*/
const total = ref(0)
/**当前选中的字典类型*/
const selectedDictType = ref<SysDictTypeVo | null>(null)
/**表格引用*/
const tableRef = ref<ElTableInstance>()

/** 检查是否为系统级字典 */
const isSystemLevel = (row: SysDictTypeVo): boolean => {
  return row.isSystem === '1'
}

/** 检查当前用户是否可以编辑该字典（超管可以编辑所有，普通用户不能编辑系统级） */
const canEditDictType = (row: SysDictTypeVo): boolean => {
  if (isSuperAdminUser.value) return true
  return !isSystemLevel(row)
}

/** 格式化日期显示 */
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

/** 查询字典类型列表 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageDictTypes(queryParams.value)
  if (!err) {
    dictTypeList.value = data.records
    total.value = data.total

    if (!selectedDictType.value && dictTypeList.value.length > 0) {
      selectedDictType.value = dictTypeList.value[0]
      syncEditForm(dictTypeList.value[0])
    }
  }
  isLoading.value = false
}

/** 选择字典类型 */
const handleSelectDictType = (item: SysDictTypeVo) => {
  selectedDictType.value = item
  syncEditForm(item)
}

/** 点击字典编码：复制 + 激活 */
const handleDictCodeClick = (item: SysDictTypeVo) => {
  copy(item.dictType)
  handleSelectDictType(item)
}

/** 删除字典类型操作 */
const handleDelete = async (row: SysDictTypeVo) => {
  if (isSystemLevel(row)) {
    showMsgSuccess(t('System dict cannot be deleted', '系统级字典不允许删除') + `: ${row.dictName}(${row.dictType})`)
    return
  }

  const [confirmErr] = await showConfirm(`${t('Confirm delete', '是否确认删除')}${row.dictName}(${row.dictType})`)
  if (confirmErr) return

  // 记录删除前的位置
  const deleteIndex = dictTypeList.value.findIndex((item) => String(item.dictId) === String(row.dictId))
  const isSelectedItem = selectedDictType.value?.dictId === row.dictId

  const [deleteErr] = await deleteDictTypes([row.dictId])
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()

    // 如果当前页没有数据了，跳到前一页
    if (dictTypeList.value.length === 0 && queryParams.value.pageNum! > 1) {
      queryParams.value.pageNum = queryParams.value.pageNum! - 1
      await getList()
    }

    // 如果删除的是当前选中项，重新聚焦
    if (isSelectedItem && dictTypeList.value.length > 0) {
      // 删除最后一条则聚焦前一条，否则聚焦同位置
      const focusIndex = Math.min(deleteIndex, dictTypeList.value.length - 1)
      selectedDictType.value = dictTypeList.value[focusIndex]
      syncEditForm(dictTypeList.value[focusIndex])
    } else if (dictTypeList.value.length === 0) {
      selectedDictType.value = null
    }
  }
}

// =========== 字典类型表单相关 ===========
/**初始表单数据*/
const initFormData: SysDictTypeBo = {
  dictId: undefined,
  dictName: '',
  dictType: '',
  status: '1',
  isSystem: '0',
  remark: ''
}

/**表单引用*/
const SysDictTypeBoRef = ref<ElFormInstance>()
const addFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**新增表单数据对象*/
const form = ref<SysDictTypeBo>({ ...initFormData })
/**右侧编辑表单数据对象*/
const editForm = ref<SysDictTypeBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  dictName: [{ required: true, message: t('dictName cannot be empty', '字典名称不能为空'), trigger: 'blur' }],
  dictType: [{ required: true, message: t('dictType cannot be empty', '字典编码不能为空'), trigger: 'blur' }]
})

/** 同步编辑表单数据 */
const syncEditForm = (item: SysDictTypeVo) => {
  editForm.value = {
    dictId: item.dictId,
    dictName: item.dictName,
    dictType: item.dictType,
    status: item.status,
    isSystem: item.isSystem,
    remark: item.remark
  }
}

/** 当前选中的字典类型是否可编辑 */
const canEditSelectedDictType = computed(() => {
  if (!selectedDictType.value) return false
  return canEditDictType(selectedDictType.value)
})

/** 字典类型表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  addFormRef.value?.resetFields()
}

/** 取消字典类型编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增字典类型操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('dict', '字典类型')}`
}

/** 提交新增字典类型表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(addFormRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err, newDictId] = await addDictType(form.value)
  if (!err) {
    showMsgSuccess(t('message.addSuccess'))
    dialog.value.visible = false
    // 跳转到最后一页
    const lastPage = Math.ceil((total.value + 1) / queryParams.value.pageSize!)
    queryParams.value.pageNum = lastPage
    await getList()
    // 聚焦到新增的字典类型
    if (newDictId) {
      const [getErr, newItem] = await getDictType(newDictId)
      if (!getErr && newItem) {
        selectedDictType.value = newItem
        syncEditForm(newItem)
        // 设置当前行（等待 DOM 更新完成）
        await nextTick()
        tableRef.value?.setCurrentRow(newItem)
      }
    }
  }
  buttonLoading.value = false
}

/** 保存字典类型（右侧编辑区域） */
const handleSaveDictType = async () => {
  if (!canEditSelectedDictType.value) {
    showMsgSuccess(t('System dict cannot be modified', '系统级字典不允许修改'))
    return
  }

  const [validateErr] = await toValidate(SysDictTypeBoRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err] = await updateDictType(editForm.value)
  if (!err) {
    showMsgSuccess(t('message.updateSuccess'))
    const currentId = editForm.value.dictId
    await getList()
    // 从刷新后的列表中找到更新的数据
    const updated = dictTypeList.value.find((item) => String(item.dictId) === String(currentId))
    if (updated) {
      selectedDictType.value = updated
      syncEditForm(updated)
    }
  }
  buttonLoading.value = false
}

/** 删除当前选中的字典类型 */
const handleDeleteSelected = async () => {
  if (!selectedDictType.value) return
  await handleDelete(selectedDictType.value)
}

// =========== 其他功能 ===========
/** 导出字典类型数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Dict Type', '字典类型'), '/system/dictType/exportDictTypes', queryParams.value)
}

/** 刷新缓存按钮操作 */
const handleRefreshCache = async () => {
  const [err] = await refreshDictCache()
  if (!err) {
    showMsgSuccess(t('Refresh Success', '刷新成功'))
    useDictStore().cleanDict()
  }
}

// =========== 生命周期 ===========
/**初始化字典类型数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新字典类型列表*/
onActivated(() => {
  if (isLoading.value) return
  handleQuery()
})
</script>

<style lang="scss" scoped>
/* 卡片样式调整 */
:deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-card__body) {
  flex: 1;
  padding: 8px 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* inline 编辑表单项间距调整（只影响右侧编辑区域） */
.inline-edit-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}
</style>
