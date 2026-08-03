<!-- 参数配置 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect
        label="系统内置"
        v-model="queryParams.configType"
        prop="configType"
        :options="sys_boolean_flag"
        @change="handleQuery"
      ></AFormSelect>
      <AFormDate label="创建时间" v-model="dateRange" prop="createTime" type="daterange"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:config:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:config:update']">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="selectionItems.length !== 1 || (!isSuperAdminUser && selectionItems.length === 1 && isBuiltinConfig(selectionItems[0]))"
              @click="handleUpdate()"
            >
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:config:delete']">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="selectionItems.length === 0 || selectionItems.some((item) => isBuiltinConfig(item))"
              @click="handleDelete()"
            >
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:config:add']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              title="参数配置"
              templateUrl="/system/config/templateConfigs"
              importUrl="/system/config/importConfigs"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['system:config:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:config:delete']">
            <el-button type="danger" plain icon="Refresh" @click="handleRefreshCache">
              {{ t('button.refreshCache') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table ref="configTableRef" v-loading="isLoading" :data="configList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="t('configName', '参数名称')" prop="configName" align="center" />
        <el-table-column :label="t('configKey', '参数键名')" prop="configKey" align="center" />
        <el-table-column :label="t('configValue', '参数键值')" prop="configValue" align="center" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" />
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" />
        <el-table-column :label="t('configType', '系统内置')" prop="configType" align="center">
          <template #default="{ row }">
            <el-tag v-if="isBuiltinConfig(row)" type="danger">{{ t('Built-in', '内置') }}</el-tag>
            <el-tag v-else type="success">{{ t('Custom', '自定义') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('操作')" align="center" width="120" fixed="right">
          <template #default="{ row }">
            <el-tooltip
              :content="!isSuperAdminUser && isBuiltinConfig(row) ? t('Built-in config cannot be modified', '系统内置参数不允许修改') : t('修改')"
              placement="top"
            >
              <el-button
                v-permi="['system:config:update']"
                :disabled="!isSuperAdminUser && isBuiltinConfig(row)"
                link
                type="success"
                icon="Edit"
                @click="handleUpdate(row)"
              ></el-button>
            </el-tooltip>
            <el-tooltip
              :content="isBuiltinConfig(row) ? t('Built-in config cannot be deleted', '系统内置参数不允许删除') : t('删除')"
              placement="top"
            >
              <el-button
                v-permi="['system:config:delete']"
                :disabled="isBuiltinConfig(row)"
                link
                type="danger"
                icon="Delete"
                @click="handleDelete(row)"
              ></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改参数配置对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" size="small" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="SysConfigBoRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <AFormInput label="参数名称" v-model="form.configName" prop="configName" span="auto"></AFormInput>
          <AFormInput label="参数键名" v-model="form.configKey" prop="configKey" span="auto"></AFormInput>
          <AFormInput label="参数键值" v-model="form.configValue" prop="configValue" type="textarea" span="auto"></AFormInput>
          <!-- 只有超级管理员才能修改 configType 字段 -->
          <AFormRadio
            v-if="isSuperAdminUser"
            label="系统内置"
            v-model="form.configType"
            prop="configType"
            :options="sys_boolean_flag"
            span="auto"
          ></AFormRadio>
          <AFormInput label="备注" v-model="form.remark" prop="remark" type="textarea" span="auto"></AFormInput>
        </el-row>
      </el-form>
    </AModal>
  </div>
</template>

<script setup lang="ts" name="Config">
import { pageConfigs, getConfig, deleteConfigs, addConfig, updateConfig, clearConfigCache } from '@/api/system/config/config/configApi'
import type { SysConfigQuery, SysConfigBo, SysConfigVo } from '@/api/system/config/config/configTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { useAuth } from '@/composables/useAuth'

const { t } = useI18n()

// 检查是否为超级管理员
const { isSuperAdmin } = useAuth()
const isSuperAdminUser = computed(() => isSuperAdmin())

/**字典数据 */
const { sys_boolean_flag } = useDict('sys_boolean_flag')

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysConfigQuery>({
  pageNum: 1,
  pageSize: 10,
  configName: '',
  configKey: '',
  configType: ''
})

/**日期范围选择器*/
const dateRange = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const configList = ref<SysConfigVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const configTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysConfigVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysConfigVo[]) => {
  selectionItems.value = selection
}

/** 查询参数配置列表 */
const getList = async () => {
  isLoading.value = true
  addDateRange(queryParams.value, dateRange.value)
  const [err, data] = await pageConfigs(queryParams.value)
  if (!err) {
    configList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出参数配置数据 */
const handleExport = () => {
  useDownload().exportExcel('参数配置', '/system/config/exportConfigs', queryParams.value)
}

/** 删除参数配置操作 */
const handleDelete = async (row?: SysConfigVo) => {
  // 系统内置参数不允许删除
  const itemsToCheck = row ? [row] : selectionItems.value
  const builtinConfigs = itemsToCheck.filter((item) => isBuiltinConfig(item))
  if (builtinConfigs.length > 0) {
    showMsgSuccess(
      `${t('Built-in config cannot be deleted', '系统内置参数不允许删除')}: ${builtinConfigs.map((item) => `${item.configName}(${item.configKey})`).join(', ')}`
    )
    return
  }

  const idsToDelete = row ? [row.configId] : selectionItems.value.map((item) => item.configId)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.configName : selectionItems.value.map((item) => item.configName).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteConfigs(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 表单相关 ===========
/**初始表单数据*/
const initFormData: SysConfigBo = {
  configId: undefined,
  configName: '',
  configKey: '',
  configValue: '',
  configType: '1',
  remark: ''
}

/**表单引用*/
const SysConfigBoRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<SysConfigBo>({ ...initFormData })
/**表单校验规则*/
const rules = computed<ElFormRules>(() => ({
  configName: [{ required: true, message: t('Config name required', '参数名称不能为空'), trigger: 'blur' }],
  configKey: [{ required: true, message: t('Config key required', '参数键名不能为空'), trigger: 'blur' }],
  configValue: [{ required: true, message: t('Config value required', '参数键值不能为空'), trigger: 'blur' }]
}))

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  SysConfigBoRef.value?.resetFields()
}

/** 取消按钮 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('config', '参数')}`
}

/** 修改按钮操作 */
const handleUpdate = async (row?: SysConfigVo) => {
  // 非超管不能修改系统内置参数
  const itemToEdit = row || selectionItems.value[0]
  if (!isSuperAdminUser.value && isBuiltinConfig(itemToEdit)) {
    showMsgSuccess(t('Built-in config cannot be modified', '系统内置参数不允许修改'))
    return
  }

  reset()
  const [err, data] = await getConfig(itemToEdit.configId)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('config', '参数')}`
  }
}

/** 提交表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(SysConfigBoRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null, msg: string | null
  if (form.value.configId) {
    ;[err] = await updateConfig(form.value)
  } else {
    ;[err] = await addConfig(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.configId ? t('message.updateSuccess') : t('message.addSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 刷新缓存按钮操作 */
const handleRefreshCache = async () => {
  const [err] = await clearConfigCache()
  if (!err) {
    showMsgSuccess(t('成功'))
  }
}

/** 检查参数是否为系统内置 */
const isBuiltinConfig = (row: SysConfigVo): boolean => {
  return row.configType === '1'
}

/**初始化获取数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
