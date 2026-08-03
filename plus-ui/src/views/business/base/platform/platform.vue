<!-- 平台配置 -->
<template>
  <div>
    <!-- 平台配置搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" v-model="queryParams.searchValue" prop="searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="平台类型" v-model="queryParams.type" prop="type" :options="sys_platform_type" @change="handleQuery"></AFormSelect>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate label="创建时间" v-model="dateRangeCreateTime" prop="createTime" type="daterange" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 平台配置工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['base:platform:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:platform:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:platform:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:platform:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Platform Data', '平台配置')"
              templateUrl="/base/platform/templatePlatforms"
              importUrl="/base/platform/importPlatforms"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['base:platform:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="platformList"
          ></TableToolbar>
        </el-row>
      </template>

      <!-- 平台配置表格数据 -->
      <el-table
        ref="platformTableRef"
        v-loading="isLoading"
        :data="platformList"
        :height="tableHeight"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="false" :label="t('id', '平台配置id')" prop="id" align="center" min-width="105" />
        <el-table-column :label="t('type', '平台类型')" prop="type" align="center" min-width="120">
          <template #default="{ row }">
            <DictTag :options="sys_platform_type" :value="row.type" />
          </template>
        </el-table-column>
        <el-table-column :label="t('name', '名称')" prop="name" align="center" />
        <el-table-column label="appid" prop="appid" align="center" min-width="100">
          <template #default="{ row }">
            <span class="cursor-pointer" @click="copy(row.appid)">{{ row.appid }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('paymentIds', '关联支付配置')" prop="paymentIds" align="center" min-width="105">
          <template #default="{ row }">
            <DictTag :options="paymentOptions" :value="row.paymentIds" />
          </template>
        </el-table-column>
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="160" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="160" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" min-width="100" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="160">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['base:platform:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['base:platform:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="订阅配置" placement="top">
              <el-button v-permi="['base:platform:update']" link type="success" icon="Operation" @click="handleTemplateConfigs(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['base:platform:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改平台配置对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="platformFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row>
          <AFormSelect label="平台类型" v-model="form.type" prop="type" :options="sys_platform_type" span="auto"></AFormSelect>
          <AFormInput label="名称" v-model="form.name" :maxlength="20" prop="name" span="auto"></AFormInput>
          <AFormInput label="appid" v-model="form.appid" :maxlength="30" prop="appid" span="auto"></AFormInput>
          <AFormInput label="密钥" v-model="form.secret" :maxlength="50" prop="secret" span="auto" type="password" show-password></AFormInput>
          <AFormInput label="接口token" v-model="form.token" :maxlength="50" prop="token" span="auto" type="password" show-password></AFormInput>
          <AFormInput label="加密密钥" v-model="form.aeskey" :maxlength="50" prop="aeskey" span="auto" type="password" show-password></AFormInput>
          <AFormSelect label="关联支付配置" v-model="form.paymentIds" :options="paymentOptions" multiple prop="paymentIds" span="auto"></AFormSelect>
          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
          <AFormInput type="textarea" label="备注" v-model="form.remark" :maxlength="255" prop="remark" span="auto"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看平台配置详情对话框 -->
    <ADetail v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />

    <!-- 订阅配置弹窗 -->
    <TemplateConfigs v-model="templateConfigDialog.visible" :template-configs="templateConfigDialog.configs" @save="handleSaveTemplateConfigs" />
  </div>
</template>

<script setup lang="ts" name="Platform">
import { pagePlatforms, getPlatform, addPlatform, updatePlatform, deletePlatforms } from '@/api/business/base/platform/platformApi'
import type { PlatformQuery, PlatformBo, PlatformVo } from '@/api/business/base/platform/platformTypes'
import { optionPayments } from '@/api/business/base/payment/paymentApi'
import { copy } from '@/utils/function'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import TemplateConfigs from './TemplateConfigs.vue'

/** 平台配置字典数据 */
const { sys_platform_type, sys_enable_status } = useDict(DictTypes.sys_platform_type, DictTypes.sys_enable_status)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 平台配置查询相关 ===========

/**查询参数对象*/
const queryParams = ref<PlatformQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  id: undefined,
  type: undefined,
  name: undefined,
  appid: undefined,
  secret: undefined,
  token: undefined,
  aeskey: undefined,
  paymentIds: undefined,
  status: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 平台配置搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 平台配置重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 平台配置表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const platformList = ref<PlatformVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const platformTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<PlatformVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: PlatformVo[]) => {
  selectionItems.value = selection
}

/** 查询平台配置列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pagePlatforms(queryParams.value)
  if (!err) {
    platformList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出平台配置数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Platform Data', '平台配置'), '/base/platform/exportPlatforms', queryParams.value)
}

/** 删除平台配置操作 */
const handleDelete = async (row?: PlatformVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.name : selectionItems.value.map((item) => item.name).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deletePlatforms(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 平台配置表单相关 ===========
/**初始表单数据*/
const initFormData: PlatformBo = {
  id: undefined,
  type: undefined,
  name: undefined,
  appid: undefined,
  secret: undefined,
  token: undefined,
  aeskey: undefined,
  paymentIds: undefined,
  status: '1',
  remark: undefined
}

/**表单引用*/
const platformFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<PlatformBo>({ ...initFormData })
/**表单校验规则*/
const rules = ref<ElFormRules>({
  type: [{ required: true, message: t('type cannot be empty', '平台类型不能为空'), trigger: 'change' }],
  name: [{ required: true, message: t('name cannot be empty', '名称不能为空'), trigger: 'blur' }],
  appid: [{ required: true, message: t('appid cannot be empty', 'appid不能为空'), trigger: 'blur' }],
  secret: [{ required: true, message: t('secret cannot be empty', '密钥不能为空'), trigger: 'blur' }]
})

/** 平台配置表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  platformFormRef.value?.resetFields()
}

/** 取消平台配置编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增平台配置操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('platform', '平台配置')}`
}

/** 修改平台配置操作 */
const handleUpdate = async (row?: PlatformVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getPlatform(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('platform', '平台配置')}`
  }
}

/** 提交平台配置表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(platformFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null, data: any
  if (form.value.id) {
    ;[err, data] = await updatePlatform(form.value)
  } else {
    ;[err, data] = await addPlatform(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))

    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 平台配置启用禁用状态修改 */
const handleStatusChange = async (row: PlatformVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.name}(${row.appid})?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updatePlatform(row)
  if (!updateErr) {
    await getList()
    showMsgSuccess(`${text}成功`)
  } else {
    row.status = toggleStatus(row.status)
  }
}

/** 支付选项 */
const paymentOptions = ref<DictItem[]>([])
/** 初始化支付选项 */
const initPaymentOptions = async () => {
  const [err, data] = await optionPayments()
  if (!err) {
    paymentOptions.value = data
  }
}

// =========== 订阅配置相关 ===========
/**订阅配置弹窗状态*/
const templateConfigDialog = ref({
  visible: false,
  configs: '',
  currentPlatform: null as PlatformVo | null
})

/**订阅配置*/
const handleTemplateConfigs = (row: PlatformVo) => {
  templateConfigDialog.value.currentPlatform = row
  templateConfigDialog.value.configs = row.templateConfigs || '[]'
  templateConfigDialog.value.visible = true
}

/**保存订阅配置*/
const handleSaveTemplateConfigs = async (configs: any[]) => {
  if (!templateConfigDialog.value.currentPlatform) return

  try {
    const configsJson = JSON.stringify(configs)
    const platformData = {
      ...templateConfigDialog.value.currentPlatform,
      templateConfigs: configsJson,
      onlyTemplateUpdate: true // 标识仅更新订阅配置
    }

    const [err] = await updatePlatform(platformData)
    if (!err) {
      showMsgSuccess(t('保存成功'))
      templateConfigDialog.value.visible = false
      await getList()
    }
  } catch (error) {
    console.error('保存订阅配置失败:', error)
  }
}

/**查看对话框配置*/
const viewDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/**查看数据*/
const viewData = ref<PlatformVo>({} as PlatformVo)

/**详情字段配置 */
const detailFields = ref<FieldConfig[]>([
  { prop: 'id', label: '平台配置id' },
  { prop: 'type', label: '平台类型', type: 'dict', dictOptions: sys_platform_type },
  { prop: 'name', label: '名称' },
  { prop: 'appid', label: 'appid' },
  { prop: 'secret', label: '密钥', type: 'password' },
  { prop: 'token', label: '接口token', type: 'password' },
  { prop: 'aeskey', label: '加密密钥', type: 'password' },
  { prop: 'paymentIds', label: '关联支付配置' },
  { prop: 'templateConfigs', label: '模板配置' },
  { prop: 'status', label: '状态', type: 'dict', dictOptions: sys_enable_status },
  { prop: 'createTime', label: '创建时间', type: 'datetime' },
  { prop: 'updateTime', label: '更新时间', type: 'datetime' },
  { prop: 'remark', label: '备注' }
])

/** 查看平台配置详情操作 */
const handleView = async (row: PlatformVo) => {
  const [err, data] = await getPlatform(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('platform', '平台配置')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/**初始化平台配置数据列表*/
onMounted(() => {
  getList()
  initPaymentOptions()
})
/**页面激活时刷新平台配置列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
