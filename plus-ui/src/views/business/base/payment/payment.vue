<!-- 支付配置 -->
<template>
  <div>
    <!-- 支付配置搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" v-model="queryParams.searchValue" prop="searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="商户类型" v-model="queryParams.type" prop="type" :options="sys_payment_method" @change="handleQuery"></AFormSelect>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate label="创建时间" v-model="dateRangeCreateTime" prop="createTime" type="daterange" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 支付配置工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['base:payment:add']">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">
              {{ t('新增') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:payment:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:payment:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['base:payment:import']">
            <AImportExcel
              v-slot="{ openImportExcel }"
              :title="t('Payment Data', '支付配置')"
              templateUrl="/base/payment/templatePayments"
              importUrl="/base/payment/importPayments"
              @import-success="getList"
            >
              <el-button type="info" plain icon="Top" @click="openImportExcel">
                {{ t('导入') }}
              </el-button>
            </AImportExcel>
          </el-col>
          <el-col :span="1.5" v-permi="['base:payment:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="paymentList"
          ></TableToolbar>
        </el-row>

        <!-- 显示已选项 -->
        <!-- <ASelectionTags :items="selectionItems" :on-clear="selectionClear" @close="selectionRemove" /> -->
      </template>

      <!-- 支付配置表格数据 -->
      <el-table
        ref="paymentTableRef"
        v-loading="isLoading"
        :data="paymentList"
        :height="tableHeight"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column v-if="true" :label="t('id', '支付配置id')" prop="id" align="center" min-width="105" />
        <el-table-column :label="t('type', '商户类型')" prop="type" align="center">
          <template #default="{ row }">
            <DictTag :value="row.type" :options="sys_payment_method" />
          </template>
        </el-table-column>
        <el-table-column :label="t('mchName', '商户名称')" prop="mchName" align="center" />
        <el-table-column :label="t('mchId', '商户号')" prop="mchId" align="center" />
        <el-table-column :label="t('status', '状态')" prop="status" align="center">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="105" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="105" />
        <el-table-column :label="t('remark', '备注')" prop="remark" align="center" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['base:payment:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['base:payment:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['base:payment:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改支付配置对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" size="large" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="paymentFormRef" :model="form" :rules="rules" label-width="auto">
        <el-row :gutter="10">
          <!-- 支付方式选择 -->
          <AFormSelect
            :label="t('Payment Method', '支付方式')"
            v-model="form.type"
            :options="sys_payment_method"
            prop="type"
            span="auto"
            :tooltip="t('Select payment channel type', '选择支付渠道类型，如微信支付、支付宝等，决定后续配置项的要求')"
            @change="handleTypeChange"
          ></AFormSelect>

          <!-- 如果支持多模式，显示模式选择器 -->
          <AFormRadio
            v-if="currentConfig?.supportModes"
            :label="t('Config Mode', '配置模式')"
            v-model="certMode"
            :options="modeOptions"
            span="auto"
            :tooltip="t('Select certificate config mode', '选择证书配置模式')"
            @change="handleModeChange"
          ></AFormRadio>

          <!-- 动态渲染字段 -->
          <template v-for="field in currentFields" :key="field.key">
            <!-- 文本域（备注） -->
            <AFormInput
              v-if="field.type === 'textarea'"
              :label="field.label"
              v-model="form[field.key]"
              :maxlength="field.maxlength"
              :prop="field.key"
              :span="field.span"
              type="textarea"
              :tooltip="field.tooltip"
            ></AFormInput>

            <!-- 密码框 -->
            <AFormInput
              v-else-if="field.type === 'password'"
              :label="field.label"
              v-model="form[field.key]"
              :maxlength="field.maxlength"
              :prop="field.key"
              :span="field.span"
              type="password"
              show-password
              :tooltip="field.tooltip"
            ></AFormInput>

            <!-- 输入框（默认） -->
            <AFormInput
              v-else
              :label="field.label"
              v-model="form[field.key]"
              :maxlength="field.maxlength"
              :prop="field.key"
              :span="field.span"
              :tooltip="field.tooltip"
            >
              <template v-if="field.showFileButton" #append>
                <el-button @click="handleSelectFile(field.key)" icon="FolderOpened">{{ t('Select File', '选择文件') }}</el-button>
              </template>
            </AFormInput>
          </template>

          <!-- 状态（所有支付方式都有） -->
          <AFormRadio
            :label="t('Status', '状态')"
            v-model="form.status"
            prop="status"
            :options="sys_enable_status"
            span="auto"
            :tooltip="t('Enable status tooltip', '启用后该支付配置将生效，停用后将暂停使用但保留配置信息')"
          ></AFormRadio>

          <!-- 提示信息：余额/积分支付 -->
          <el-col :span="24" v-if="['balance', 'points'].includes(form.type || '')">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                {{
                  form.type === 'balance'
                    ? t('Balance payment tip', '余额支付无需配置第三方参数，只需填写配置名称和备注')
                    : t('Points payment tip', '积分抵扣无需配置第三方参数，只需填写配置名称和备注')
                }}
              </template>
            </el-alert>
          </el-col>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看支付配置详情对话框 -->
    <ADetail label-width="auto" v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />
  </div>
</template>

<script setup lang="ts" name="Payment">
import { pagePayments, getPayment, addPayment, updatePayment, deletePayments } from '@/api/business/base/payment/paymentApi'
import type { PaymentQuery, PaymentBo, PaymentVo } from '@/api/business/base/payment/paymentTypes'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm, showMsgError } from '@/utils/modal'
import { getPaymentMethodConfig, getCurrentFields, type PaymentFieldConfig } from '@/api/business/base/payment/paymentFieldConfig'

/** 支付配置字典数据 */
const { sys_boolean_flag, sys_platform_type, sys_enable_status, sys_payment_method } = useDict(
  DictTypes.sys_boolean_flag,
  DictTypes.sys_platform_type,
  DictTypes.sys_enable_status,
  DictTypes.sys_payment_method
)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 支付配置查询相关 ===========

/**查询参数对象*/
const queryParams = ref<PaymentQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  id: undefined,
  type: undefined,
  mchName: undefined,
  mchId: undefined,
  mchKey: undefined,
  apiV3Key: undefined,
  certPath: undefined,
  keyPath: undefined,
  platformCertPath: undefined,
  certSerialNo: undefined,
  publicKeyId: undefined,
  p12CertPath: undefined,
  status: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 支付配置搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 支付配置重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 支付配置表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const paymentList = ref<PaymentVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const paymentTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<PaymentVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: PaymentVo[]) => {
  selectionItems.value = selection
}

/** 查询支付配置列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pagePayments(queryParams.value)
  if (!err) {
    paymentList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出支付配置数据 */
const handleExport = () => {
  useDownload().exportExcel(t('Payment Data', '支付配置'), '/base/payment/exportPayments', queryParams.value)
}

/** 删除支付配置操作 */
const handleDelete = async (row?: PaymentVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.mchName : selectionItems.value.map((item) => item.mchName).join(', ')
  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deletePayments(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 支付配置表单相关 ===========
/**初始表单数据*/
const initFormData: PaymentBo = {
  id: undefined,
  type: undefined,
  mchName: undefined,
  mchId: undefined,
  mchKey: undefined,
  apiV3Key: undefined,
  certPath: undefined,
  keyPath: undefined,
  platformCertPath: undefined,
  certSerialNo: undefined,
  publicKeyId: undefined,
  p12CertPath: undefined,
  status: '1',
  remark: undefined
}

/** 证书模式（用于支持多模式的支付方式） */
const certMode = ref<string>('v2_v3')

/** 当前支付方式配置 */
const currentConfig = computed(() => {
  if (!form.value.type) return null
  return getPaymentMethodConfig(form.value.type)
})

/** 模式选项（用于多模式支付方式） */
const modeOptions = computed(() => {
  if (!currentConfig.value?.supportModes || !currentConfig.value.modes) return []
  return currentConfig.value.modes.map((mode) => ({
    label: mode.modeLabel,
    value: mode.mode
  }))
})

/** 当前需要显示的字段 */
const currentFields = computed(() => {
  if (!form.value.type) return []
  return getCurrentFields(form.value.type, certMode.value)
})

/** 支付方式变更处理 */
const handleTypeChange = () => {
  const config = currentConfig.value

  // 如果是多模式，设置默认模式
  if (config?.supportModes && config.defaultMode) {
    certMode.value = config.defaultMode
  }

  // 清空其他字段（保留 type 和 status）
  const preservedFields = { type: form.value.type, status: form.value.status }
  form.value = { ...initFormData, ...preservedFields }
}

/** 模式变更处理 */
const handleModeChange = () => {
  // 切换模式时不清空任何字段，只是改变显示的字段
  // 用户已填写的内容会保留在表单数据中
  // 这样用户可以在不同模式间切换而不丢失数据
}

/** 选择证书文件并读取内容 */
const handleSelectFile = (fieldName: string) => {
  // 创建隐藏的文件选择input
  const input = document.createElement('input')
  input.type = 'file'
  // 扩展支持的文件类型，包括 txt
  input.accept = '.pem,.key,.crt,.cer,.p12,.pfx,.txt'

  input.onchange = async (e: Event) => {
    const target = e.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file) return

    try {
      // 读取文件内容
      const content = await file.text()
      // 设置到对应字段（使用类型断言）
      form.value[fieldName as keyof PaymentBo] = content as any
    } catch (error) {
      console.error('读取文件失败:', error)
      showMsgError(t('Failed to read file', '读取文件失败,请检查文件格式'))
    }
  }

  input.click()
}

/**表单引用*/
const paymentFormRef = ref<ElFormInstance>()
/**表单提交按钮加载状态*/
const buttonLoading = ref(false)
/**对话框配置对象*/
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
/**表单数据对象*/
const form = ref<PaymentBo>({ ...initFormData })
/**表单校验规则（只校验必填项）*/
const rules = ref<ElFormRules>({
  type: [{ required: true, message: t('Please select payment method', '请选择支付方式'), trigger: 'change' }],
  status: [{ required: true, message: t('Please select status', '请选择状态'), trigger: 'change' }]
})

/** 支付配置表单重置 */
const reset = () => {
  form.value = { ...initFormData }
  certMode.value = 'v2_v3' // 重置为默认模式
  paymentFormRef.value?.resetFields()
}

/** 取消支付配置编辑 */
const cancel = () => {
  reset()
  dialog.value.visible = false
}

/** 新增支付配置操作 */
const handleAdd = () => {
  reset()
  dialog.value.visible = true
  dialog.value.title = `${t('新增')}${t('payment', '支付配置')}`
}

/** 修改支付配置操作 */
const handleUpdate = async (row?: PaymentVo) => {
  reset()
  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getPayment(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)

    // 智能判断证书模式（仅对支持多模式的支付方式）
    const config = getPaymentMethodConfig(data.type || '')
    if (config?.supportModes) {
      if (config.method === 'wechat') {
        // 微信支付：根据字段判断是 v2 / v3 / v2_v3
        const hasV2 = data.mchKey || data.p12CertPath
        const hasV3 = data.apiV3Key || data.certSerialNo || data.publicKeyId

        if (hasV2 && hasV3) {
          certMode.value = 'v2_v3' // 两者都有，兼容模式
        } else if (hasV3) {
          certMode.value = 'v3' // 只有v3
        } else if (hasV2) {
          certMode.value = 'v2' // 只有v2
        } else {
          certMode.value = config.defaultMode || 'v2_v3'
        }
      } else if (config.method === 'alipay') {
        // 支付宝：如果有 certPath，则为证书模式
        certMode.value = data.certPath ? 'cert' : 'public_key'
      }
    }

    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('payment', '支付配置')}`
  }
}

/** 提交支付配置表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(paymentFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (form.value.id) {
    ;[err] = await updatePayment(form.value)
  } else {
    ;[err] = await addPayment(form.value)
  }
  if (!err) {
    showMsgSuccess(form.value.id ? t('message.updateSuccess') : t('message.addSuccess'))

    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 支付配置启用禁用状态修改 */
const handleStatusChange = async (row: PaymentVo) => {
  const text = isTrue(row.status) ? t('启用') : t('停用')
  const [confirmErr] = await showConfirm(`${t('是否确认')}${text}${row.id}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updatePayment(row)
  if (!updateErr) {
    await getList()
    showMsgSuccess(`${text}${t('成功')}`)
  } else {
    row.status = toggleStatus(row.status)
  }
}

/**查看对话框配置*/
const viewDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/**查看数据*/
const viewData = ref<PaymentVo>({} as PaymentVo)

/**详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  { prop: 'id', label: t('id', '支付配置id') },
  { prop: 'type', label: t('type', '商户类型'), type: 'dict', dictOptions: sys_payment_method },
  { prop: 'mchName', label: t('mchName', '商户名称') },
  { prop: 'mchId', label: t('mchId', '商户号') },
  { prop: 'mchKey', label: t('mchKey', '商户密钥'), type: 'password' },
  { prop: 'apiV3Key', label: t('apiV3Key', 'APIv3密钥'), type: 'password' },
  { prop: 'certPath', label: t('certPath', '证书路径(V3 PEM)'), type: 'password' },
  { prop: 'keyPath', label: t('keyPath', '密钥路径(V3 PEM)'), type: 'password' },
  { prop: 'platformCertPath', label: t('platformCertPath', '平台证书路径(V3 PEM)'), type: 'password' },
  { prop: 'certSerialNo', label: t('certSerialNo', '证书序列号'), type: 'password' },
  { prop: 'publicKeyId', label: t('publicKeyId', '微信支付公钥ID'), type: 'password' },
  { prop: 'p12CertPath', label: t('p12CertPath', 'p12证书路径(V2退款)') },
  { prop: 'status', label: t('status', '状态'), type: 'dict', dictOptions: sys_enable_status },
  { prop: 'createTime', label: t('createTime', '创建时间'), type: 'datetime' },
  { prop: 'updateTime', label: t('updateTime', '更新时间'), type: 'datetime' },
  { prop: 'remark', label: t('remark', '备注') }
])

/** 查看支付配置详情操作 */
const handleView = async (row: PaymentVo) => {
  const [err, data] = await getPayment(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('payment', '支付配置')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/**初始化支付配置数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新支付配置列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
