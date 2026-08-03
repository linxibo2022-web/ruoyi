<!-- API密钥 -->
<template>
  <div>
    <!-- 开放API密钥搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormInput label="应用名称" v-model="queryParams.appName" prop="appName" @input="handleQuery"></AFormInput>
      <AFormSelect label="状态" v-model="queryParams.status" prop="status" :options="sys_enable_status" @change="handleQuery"></AFormSelect>
      <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 开放API密钥工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['system:apiKey:add']">
            <el-button type="primary" plain icon="Plus" @click="handleGenerate">
              {{ t('Generate Key', '生成密钥') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:apiKey:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleUpdate()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:apiKey:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['system:apiKey:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="openApiList"
          ></TableToolbar>
        </el-row>
      </template>

      <!-- 开放API密钥表格数据 -->
      <el-table
        ref="openApiTableRef"
        v-loading="isLoading"
        :data="openApiList"
        :height="tableHeight"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="t('App Name', '应用名称')" prop="appName" align="center" min-width="120" />
        <el-table-column label="AppKey" prop="appKey" align="center" min-width="180">
          <template #default="{ row }">
            <el-text
              truncated
              class="cursor-pointer"
              @click="copyToClipboard(row.appKey)"
              :title="`${t('Click to copy', '点击复制')}: ${row.appKey}`"
            >
              {{ row.appKey }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column :label="t('User', '关联用户')" prop="userName" align="center" />
        <el-table-column :label="t('Call Count', '调用次数')" prop="callCount" align="center" width="100" />
        <el-table-column :label="t('Status', '状态')" prop="status" align="center" width="80">
          <template #default="{ row }">
            <AFormSwitch v-model="row.status" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="t('Last Call', '最后调用')" prop="lastCallTime" align="center" width="160" />
        <el-table-column :label="t('Expire Time', '过期时间')" prop="expireTime" align="center" width="160" />
        <el-table-column :label="t('Operation', '操作')" align="center" fixed="right" width="220">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['system:apiKey:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('修改')" placement="top">
              <el-button v-permi="['system:apiKey:update']" link type="success" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('Reset Key', '重置密钥')" placement="top">
              <el-button v-permi="['system:apiKey:update']" link type="warning" icon="RefreshRight" @click="handleResetSecret(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['system:apiKey:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 生成密钥对话框 -->
    <AModal
      v-model="generateDialog.visible"
      :title="generateDialog.title"
      :loading="buttonLoading"
      @confirm="submitGenerate"
      @cancel="cancelGenerate"
    >
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="120px" autocomplete="off" data-bwignore>
        <el-row>
          <AFormInput label="应用名称" v-model="generateForm.appName" prop="appName" span="auto"></AFormInput>
          <AFormSelect label="关联用户" v-model="generateForm.userId" prop="userId" :options="userOptions" span="auto"></AFormSelect>
          <AFormDate label="过期时间" v-model="generateForm.expireTime" prop="expireTime" type="datetime" span="auto"></AFormDate>
          <AFormInput
            label="IP白名单"
            v-model="generateForm.whiteIps"
            prop="whiteIps"
            :placeholder="t('Separate multiple IPs with commas', '多个IP用逗号分隔')"
            span="auto"
          ></AFormInput>
          <AFormInput label="备注" v-model="generateForm.remark" type="textarea" prop="remark" :span="24"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 编辑密钥对话框 -->
    <AModal v-model="dialog.visible" :title="dialog.title" :loading="buttonLoading" @confirm="submitForm" @cancel="cancel">
      <el-form ref="openApiFormRef" :model="form" :rules="rules" label-width="120px" autocomplete="off" data-bwignore>
        <el-row>
          <AFormInput label="应用名称" v-model="form.appName" prop="appName" span="auto"></AFormInput>
          <AFormSelect label="关联用户" v-model="form.userId" prop="userId" :options="userOptions" span="auto"></AFormSelect>
          <AFormDate label="过期时间" v-model="form.expireTime" prop="expireTime" type="datetime" span="auto"></AFormDate>
          <AFormInput
            label="IP白名单"
            v-model="form.whiteIps"
            prop="whiteIps"
            :placeholder="t('Separate multiple IPs with commas', '多个IP用逗号分隔')"
            span="auto"
          ></AFormInput>
          <AFormRadio label="状态" v-model="form.status" prop="status" :options="sys_enable_status" span="auto"></AFormRadio>
          <AFormInput label="备注" v-model="form.remark" type="textarea" prop="remark" :span="24"></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 密钥详情对话框(仅生成时显示) -->
    <el-dialog v-model="secretDialog.visible" :title="secretDialog.title" width="600px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <span style="color: #e6a23c">{{ secretData.tips }}</span>
        </template>
      </el-alert>
      <el-descriptions :column="1" border class="mt-4">
        <el-descriptions-item :label="t('App Name', '应用名称')">{{ secretData.appName }}</el-descriptions-item>
        <el-descriptions-item label="AppKey">
          <el-input v-model="secretData.appKey" readonly>
            <template #append>
              <el-button @click="copyToClipboard(secretData.appKey)">{{ t('Copy', '复制') }}</el-button>
            </template>
          </el-input>
        </el-descriptions-item>
        <el-descriptions-item label="AppSecret">
          <el-input v-model="secretData.appSecret" readonly>
            <template #append>
              <el-button @click="copyToClipboard(secretData.appSecret)">{{ t('Copy', '复制') }}</el-button>
            </template>
          </el-input>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="secretDialog.visible = false">{{ t('I have saved', '我已保存') }}</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <ADetail v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" />
  </div>
</template>

<script setup lang="ts" name="OpenApi">
import { pageApiKeys, getApiKey, generateApiKey, updateApiKey, resetSecret, deleteApiKeys } from '@/api/system/openApi/openApiApi'
import type { SysApiKeyQuery, SysApiKeyBo, SysApiKeyVo, OpenApiSecretVo } from '@/api/system/openApi/openApiTypes'
import { pageUsers } from '@/api/system/core/user/userApi'
import { isTrue, toggleStatus } from '@/utils/boolean'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
import { copy } from '@/utils/function'

/** 开放API密钥字典数据 */
const { sys_enable_status } = useDict(DictTypes.sys_enable_status)

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 开放API密钥查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysApiKeyQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  appName: undefined,
  appKey: undefined,
  userId: undefined,
  status: undefined
})

/**日期范围选择器*/
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 开放API密钥搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 开放API密钥重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 开放API密钥表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据列表*/
const openApiList = ref<SysApiKeyVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const openApiTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysApiKeyVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysApiKeyVo[]) => {
  selectionItems.value = selection
}

/** 查询开放API密钥列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageApiKeys(queryParams.value)
  if (!err) {
    openApiList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出开放API密钥数据 */
const handleExport = () => {
  useDownload().exportExcel('开放API密钥', '/system/openApi/exportOpenApis', queryParams.value)
}

/** 删除开放API密钥操作 */
const handleDelete = async (row?: SysApiKeyVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.appName || row.id : selectionItems.value.map((item) => item.appName || item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('Confirm delete', '是否确认删除')}${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteApiKeys(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 用户和菜单数据 ===========
const userOptions = ref<any[]>([])

/** 加载用户列表 */
const loadUsers = async () => {
  const [err, data] = await pageUsers({ pageNum: 1, pageSize: 1000 })
  if (!err) {
    userOptions.value = data.records.map((user: any) => ({
      label: user.nickName,
      value: user.userId
    }))
  }
}

// =========== 生成密钥表单相关 ===========
const generateFormRef = ref<ElFormInstance>()
const buttonLoading = ref(false)
const generateDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const generateForm = ref<SysApiKeyBo>({
  appName: undefined,
  userId: undefined,
  expireTime: undefined,
  whiteIps: undefined,
  status: '1',
  remark: undefined
})
const generateRules = ref<ElFormRules>({
  appName: [{ required: true, message: t('appName cannot be empty', '应用名称不能为空'), trigger: 'blur' }]
})

/** 生成新密钥操作 */
const handleGenerate = async () => {
  await loadUsers()
  generateForm.value = {
    appName: undefined,
    userId: undefined,
    expireTime: undefined,
    whiteIps: undefined,
    status: '1',
    remark: undefined
  }
  generateDialog.value.visible = true
  generateDialog.value.title = t('Generate API Key', '生成API密钥')
}

/** 提交生成密钥表单 */
const submitGenerate = async () => {
  const [validateErr] = await toValidate(generateFormRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err, data] = await generateApiKey(generateForm.value)
  if (!err) {
    showMsgSuccess(t('Generated', '生成成功'))
    generateDialog.value.visible = false

    // 显示密钥详情
    secretData.value = data
    secretDialog.value.visible = true
    secretDialog.value.title = t('Key Details (Keep Safe)', '密钥详情(请妥善保管)')

    await getList()
  }
  buttonLoading.value = false
}

/** 取消生成 */
const cancelGenerate = () => {
  generateDialog.value.visible = false
}

// =========== 编辑表单相关 ===========
const openApiFormRef = ref<ElFormInstance>()
const dialog = ref<DialogState>({
  visible: false,
  title: ''
})
const form = ref<SysApiKeyBo>({})
const rules = ref<ElFormRules>({
  appName: [{ required: true, message: t('appName cannot be empty', '应用名称不能为空'), trigger: 'blur' }]
})

/** 修改开放API密钥操作 */
const handleUpdate = async (row?: SysApiKeyVo) => {
  await loadUsers()

  const itemToEdit = row || selectionItems.value[0]
  const [err, data] = await getApiKey(itemToEdit.id)
  if (!err) {
    Object.assign(form.value, data)
    dialog.value.visible = true
    dialog.value.title = `${t('修改')}${t('openApi', 'API密钥')}`
  }
}

/** 提交编辑表单 */
const submitForm = async () => {
  const [validateErr] = await toValidate(openApiFormRef)
  if (validateErr) return

  buttonLoading.value = true
  const [err] = await updateApiKey(form.value)
  if (!err) {
    showMsgSuccess(t('message.updateSuccess'))
    dialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 取消编辑 */
const cancel = () => {
  dialog.value.visible = false
}

// =========== 密钥详情对话框 ===========
const secretDialog = ref<DialogState>({
  visible: false,
  title: ''
})
const secretData = ref<OpenApiSecretVo>({
  id: 0,
  appName: '',
  appKey: '',
  appSecret: '',
  tips: ''
})

/** 重置密钥操作 */
const handleResetSecret = async (row: SysApiKeyVo) => {
  const [confirmErr] = await showConfirm(`${t('Confirm reset', '是否确认重置')}${row.appName}${t("'s key", '的密钥')}?`)
  if (confirmErr) return

  const [err, data] = await resetSecret(row.id)
  if (!err) {
    showMsgSuccess(t('Reset Success', '重置成功'))

    // 显示新密钥
    secretData.value = data
    secretDialog.value.visible = true
    secretDialog.value.title = t('New Key Details (Keep Safe)', '新密钥详情(请妥善保管)')

    await getList()
  }
}

/** 复制到剪贴板 */
const copyToClipboard = (text: string) => {
  copy(text)
}

/** 开放API密钥启用禁用状态修改 */
const handleStatusChange = async (row: SysApiKeyVo) => {
  const text = isTrue(row.status) ? t('Enable', '启用') : t('Disable', '停用')
  const [confirmErr] = await showConfirm(`${t('Confirm', '是否确认')}${text}${row.appName}?`)
  if (confirmErr) {
    row.status = toggleStatus(row.status)
    return
  }
  const [updateErr] = await updateApiKey(row)
  if (!updateErr) {
    await getList()
    showMsgSuccess(`${text}${t('Success', '成功')}`)
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
const viewData = ref<SysApiKeyVo>({} as SysApiKeyVo)

/**详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  { prop: 'appName', label: t('App Name', '应用名称') },
  { prop: 'appKey', label: 'AppKey', type: 'copyable' },
  { prop: 'userName', label: t('User', '关联用户') },
  { prop: 'expireTime', label: t('Expire Time', '过期时间'), type: 'datetime' },
  { prop: 'status', label: t('Status', '状态'), type: 'dict', dictOptions: sys_enable_status },
  { prop: 'whiteIps', label: t('IP Whitelist', 'IP白名单') },
  { prop: 'callCount', label: t('Call Count', '调用次数') },
  { prop: 'lastCallTime', label: t('Last Call Time', '最后调用时间'), type: 'datetime' },
  { prop: 'createTime', label: t('Create Time', '创建时间'), type: 'datetime' },
  { prop: 'updateTime', label: t('Update Time', '更新时间'), type: 'datetime' },
  { prop: 'remark', label: t('Remark', '备注') }
])

/** 查看开放API密钥详情操作 */
const handleView = async (row: SysApiKeyVo) => {
  const [err, data] = await getApiKey(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('openApi', 'API密钥')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/**初始化开放API密钥数据列表*/
onMounted(() => {
  getList()
})
/**页面激活时刷新开放API密钥列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>

<style scoped lang="scss"></style>
