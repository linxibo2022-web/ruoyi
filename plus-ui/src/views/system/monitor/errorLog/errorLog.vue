<!-- 错误日志 -->
<template>
  <div>
    <!-- 错误日志搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect
        label="处理状态"
        v-model="queryParams.handleStatus"
        prop="handleStatus"
        :options="handleStatusOptions"
        @change="handleQuery"
      ></AFormSelect>
      <AFormSelect
        label="严重级别"
        v-model="queryParams.errorLevel"
        prop="errorLevel"
        :options="errorLevelOptions"
        @change="handleQuery"
      ></AFormSelect>
      <AFormDate v-model="dateRangeCreateTime" prop="createTime" type="daterange" label="创建时间" @change="handleQuery"></AFormDate>
      <AFormSelect
        label="平台类型"
        v-model="queryParams.clientType"
        prop="clientType"
        :options="clientTypeOptions"
        @change="handleQuery"
      ></AFormSelect>
      <AFormInput label="模块名称" v-model="queryParams.moduleName" prop="moduleName" @input="handleQuery"></AFormInput>
      <AFormInput label="用户名" v-model="queryParams.userName" prop="userName" @input="handleQuery"></AFormInput>
      <AFormInput label="异常类名" v-model="queryParams.errorType" prop="errorType" @input="handleQuery"></AFormInput>
    </ASearchForm>

    <el-card shadow="hover">
      <!-- 错误日志工具栏 -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['monitor:errorLog:update']">
            <el-button type="warning" plain icon="Edit" :disabled="selectionItems.length === 0" @click="handleBatchUpdateStatus">
              {{ t('处理') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:errorLog:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:errorLog:delete']">
            <el-button type="danger" plain icon="Delete" @click="handleClear">
              {{ t('清空') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:errorLog:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar
            v-model:showSearch="showSearch"
            @reset-query="resetQuery"
            @query-table="getList"
            :table-columns="detailFields"
            :table-data="errorLogList"
          ></TableToolbar>
        </el-row>
      </template>

      <!-- 错误日志表格数据 -->
      <el-table
        ref="errorLogTableRef"
        v-loading="isLoading"
        :data="errorLogList"
        :height="tableHeight"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="t('严重级别')" prop="errorLevel" align="center" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.errorLevel === 'ERROR'" type="danger">ERROR</el-tag>
            <el-tag v-else-if="row.errorLevel === 'WARN'" type="warning">WARN</el-tag>
            <el-tag v-else-if="row.errorLevel === 'FATAL'" type="danger" effect="dark">FATAL</el-tag>
            <el-tag v-else>{{ row.errorLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('处理状态')" prop="handleStatus" align="center" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.handleStatus === '0'" type="danger">待处理</el-tag>
            <el-tag v-else-if="row.handleStatus === '1'" type="success">已处理</el-tag>
            <el-tag v-else-if="row.handleStatus === '2'" type="info">已忽略</el-tag>
            <el-tag v-else>{{ row.handleStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('错误消息')" prop="errorMessage" align="center" min-width="250" show-overflow-tooltip />
        <el-table-column :label="t('发生次数')" prop="occurrenceCount" align="center" width="100" />
        <el-table-column :label="t('最后时间')" prop="lastTime" align="center" width="160" />
        <el-table-column :label="t('请求URI')" prop="requestUri" align="left" min-width="200" show-overflow-tooltip />
        <el-table-column :label="t('模块名称')" prop="moduleName" align="center" width="120" show-overflow-tooltip />
        <el-table-column :label="t('平台类型')" prop="clientType" align="center" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.clientType === 'PC'" type="primary">PC</el-tag>
            <el-tag v-else-if="row.clientType === 'WECHAT'" type="success">微信</el-tag>
            <el-tag v-else-if="row.clientType === 'ANDROID'" type="info">安卓</el-tag>
            <el-tag v-else-if="row.clientType === 'IOS'" type="info">IOS</el-tag>
            <el-tag v-else-if="row.clientType === 'H5'" type="warning">H5</el-tag>
            <el-tag v-else>{{ row.clientType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('用户名')" prop="userName" align="center" width="100" />
        <el-table-column :label="t('操作')" align="center" fixed="right" width="120">
          <template #default="{ row }">
            <el-tooltip :content="t('查看')" placement="top">
              <el-button v-permi="['monitor:errorLog:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('处理')" placement="top">
              <el-button v-permi="['monitor:errorLog:update']" link type="warning" icon="Check" @click="handleUpdateStatus(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('删除')" placement="top">
              <el-button v-permi="['monitor:errorLog:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 更新处理状态对话框 -->
    <AModal
      size="small"
      v-model="statusDialog.visible"
      :title="statusDialog.title"
      :loading="buttonLoading"
      @confirm="submitStatusForm"
      @cancel="cancelStatus"
    >
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="auto">
        <el-row :gutter="10">
          <AFormRadio v-if="handleMode !== 'batch'" label="处理范围" v-model="handleScope" :options="handleScopeOptions" span="auto"></AFormRadio>
          <AFormRadio label="处理状态" v-model="statusForm.handleStatus" prop="handleStatus" :options="handleStatusOptions" span="auto"></AFormRadio>
          <AFormInput
            label="处理备注"
            v-model="statusForm.handleRemark"
            type="textarea"
            :maxlength="500"
            prop="handleRemark"
            span="auto"
          ></AFormInput>
        </el-row>
      </el-form>
    </AModal>

    <!-- 查看错误日志详情对话框 -->
    <ADetail v-model="viewDialog.visible" :title="viewDialog.title" :data="viewData" :fields="detailFields" :column="2" />
  </div>
</template>

<script setup lang="ts" name="ErrorLog">
import {
  pageErrorLogs,
  getErrorLog,
  updateHandleStatus,
  updateHandleStatusBatch,
  updateSameErrorLogs,
  updateSameRequestLogs,
  deleteErrorLogs,
  clearErrorLogs
} from '@/api/system/monitor/errorLog/errorLogApi'
import type { ErrorLogQuery, ErrorLogBo, ErrorLogVo } from '@/api/system/monitor/errorLog/errorLogTypes'
import { addDateRange } from '@/utils/date'
import { toValidate } from '@/utils/to'
import { showMsgSuccess, showConfirm } from '@/utils/modal'

const { t } = useI18n()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

type HandleScope = 'single' | 'sameError' | 'sameRequest'
type HandleMode = 'single' | 'batch'

// =========== 选项配置 ===========
/** 严重级别选项 */
const errorLevelOptions = ref<DictArray>([
  { label: 'ERROR', value: 'ERROR', elTagType: 'danger' },
  { label: 'WARN', value: 'WARN', elTagType: 'warning' },
  { label: 'FATAL', value: 'FATAL', elTagType: 'danger' }
])

/** 平台类型选项 */
const clientTypeOptions = ref<DictArray>([
  { label: 'PC', value: 'PC', elTagType: 'primary' },
  { label: '微信', value: 'WECHAT', elTagType: 'success' },
  { label: '安卓', value: 'ANDROID', elTagType: 'info' },
  { label: 'IOS', value: 'IOS', elTagType: 'info' },
  { label: 'H5', value: 'H5', elTagType: 'warning' },
  { label: '未知', value: 'UNKNOWN' }
])

/** 处理状态选项 */
const handleStatusOptions = ref<DictArray>([
  { label: '待处理', value: '0', elTagType: 'danger' },
  { label: '已处理', value: '1', elTagType: 'success' },
  { label: '已忽略', value: '2', elTagType: 'info' }
])

/** 处理范围选项 */
const handleScopeOptions = ref<DictArray>([
  { label: '仅本条', value: 'single' },
  { label: '同接口', value: 'sameRequest' },
  { label: '同类错误', value: 'sameError' }
])

// =========== 查询相关 ===========
/** 查询参数对象 */
const queryParams = ref<ErrorLogQuery>({
  pageNum: 1,
  pageSize: 10,
  orderByColumn: 'id',
  isAsc: 'desc',
  errorLevel: undefined,
  errorType: undefined,
  userName: undefined,
  clientType: undefined,
  moduleName: undefined,
  handleStatus: undefined,
  searchValue: undefined
})

/** 日期范围选择器 */
const dateRangeCreateTime = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  dateRangeCreateTime.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 表格数据相关 ===========
/** 表格加载状态 */
const isLoading = ref(true)
/** 数据列表 */
const errorLogList = ref<ErrorLogVo[]>([])
/** 总记录数 */
const total = ref(0)
/** 表格实例 */
const errorLogTableRef = ref()
/** 选中的数据项 */
const selectionItems = ref<ErrorLogVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: ErrorLogVo[]) => {
  selectionItems.value = selection
}

/** 查询错误日志列表 */
const getList = async () => {
  isLoading.value = true
  queryParams.value.params = {}
  addDateRange(queryParams.value, dateRangeCreateTime.value, 'createTime')
  const [err, data] = await pageErrorLogs(queryParams.value)
  if (!err) {
    errorLogList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出错误日志数据 */
const handleExport = () => {
  useDownload().exportExcel(t('ErrorLog Data', '错误日志'), '/monitor/errorLog/exportErrorLogs', queryParams.value)
}

/** 删除错误日志操作 */
const handleDelete = async (row?: ErrorLogVo) => {
  const idsToDelete = row ? [row.id] : selectionItems.value.map((item) => item.id)
  if (idsToDelete.length === 0) return
  const itemsToDelete = row ? row.id : selectionItems.value.map((item) => item.id).join(', ')

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}错误日志 ${itemsToDelete}`)
  if (confirmErr) return

  const [deleteErr] = await deleteErrorLogs(idsToDelete)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

/** 清空错误日志操作 */
const handleClear = async () => {
  const [confirmErr] = await showConfirm(`${t('是否确认清空所有错误日志')}？此操作不可恢复！`)
  if (confirmErr) return

  const [clearErr] = await clearErrorLogs()
  if (!clearErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

// =========== 处理状态更新相关 ===========
/** 初始状态表单数据 */
const initStatusFormData: ErrorLogBo = {
  id: undefined,
  ids: undefined,
  handleStatus: '1',
  handleRemark: undefined
}

/** 处理范围 */
const handleScope = ref<HandleScope>('single')

/** 处理模式 */
const handleMode = ref<HandleMode>('single')

/** 表单引用 */
const statusFormRef = ref<ElFormInstance>()
/** 表单提交按钮加载状态 */
const buttonLoading = ref(false)
/** 对话框配置对象 */
const statusDialog = ref<DialogState>({
  visible: false,
  title: ''
})
/** 表单数据对象 */
const statusForm = ref<ErrorLogBo>({ ...initStatusFormData })
/** 表单校验规则 */
const statusRules = ref<ElFormRules>({
  handleStatus: [{ required: true, message: t('处理状态不能为空'), trigger: 'blur' }]
})

/** 表单重置 */
const resetStatus = () => {
  statusForm.value = { ...initStatusFormData }
  handleScope.value = 'single'
  handleMode.value = 'single'
  statusFormRef.value?.resetFields()
}

/** 取消编辑 */
const cancelStatus = () => {
  resetStatus()
  statusDialog.value.visible = false
}

/** 更新处理状态操作 */
const handleUpdateStatus = async (row?: ErrorLogVo, scope: HandleScope = 'single') => {
  resetStatus()
  const itemToEdit = row || selectionItems.value[0]
  if (!itemToEdit) return
  handleScope.value = scope
  handleMode.value = 'single'
  statusForm.value.id = itemToEdit.id
  statusForm.value.ids = undefined
  statusForm.value.handleStatus = itemToEdit.handleStatus === '0' || !itemToEdit.handleStatus ? '1' : itemToEdit.handleStatus
  statusForm.value.handleRemark = itemToEdit.handleRemark
  statusDialog.value.visible = true
  statusDialog.value.title = `${t('处理')}${t('错误日志')}`
}

/** 批量处理选中记录 */
const handleBatchUpdateStatus = () => {
  resetStatus()
  const idsToHandle = selectionItems.value.map((item) => item.id)
  if (idsToHandle.length === 0) return
  handleMode.value = 'batch'
  statusForm.value.ids = idsToHandle
  statusDialog.value.visible = true
  statusDialog.value.title = `${t('处理')}${t('错误日志')}`
}

/** 提交状态表单 */
const submitStatusForm = async () => {
  const [validateErr] = await toValidate(statusFormRef)
  if (validateErr) return

  buttonLoading.value = true
  let err: Error | null
  if (handleMode.value === 'batch') {
    ;[err] = await updateHandleStatusBatch(statusForm.value)
  } else if (handleScope.value === 'sameError') {
    ;[err] = await updateSameErrorLogs(statusForm.value)
  } else if (handleScope.value === 'sameRequest') {
    ;[err] = await updateSameRequestLogs(statusForm.value)
  } else {
    ;[err] = await updateHandleStatus(statusForm.value)
  }
  if (!err) {
    showMsgSuccess(t('message.updateSuccess'))
    statusDialog.value.visible = false
    await getList()
  }
  buttonLoading.value = false
}

/** 查看对话框配置 */
const viewDialog = ref<DialogState>({
  visible: false,
  title: ''
})

/** 查看数据 */
const viewData = ref<ErrorLogVo>({} as ErrorLogVo)

/** 详情字段配置 */
const detailFields = computed<FieldConfig[]>(() => [
  // 状态概览
  {
    prop: 'errorLevel',
    label: t('errorLevel', '严重级别'),
    type: 'dict',
    dictOptions: errorLevelOptions,
    group: t('statusOverview', '状态概览'),
    span: 1
  },
  {
    prop: 'handleStatus',
    label: t('handleStatus', '处理状态'),
    type: 'dict',
    dictOptions: handleStatusOptions,
    group: t('statusOverview', '状态概览'),
    span: 1
  },
  { prop: 'occurrenceCount', label: t('occurrenceCount', '发生次数'), group: t('statusOverview', '状态概览'), span: 1 },
  { prop: 'firstTime', label: t('firstTime', '首次发生时间'), type: 'datetime', group: t('statusOverview', '状态概览'), span: 1 },
  { prop: 'lastTime', label: t('lastTime', '最后发生时间'), type: 'datetime', group: t('statusOverview', '状态概览'), span: 1 },

  // 核心内容
  { prop: 'errorMessage', label: t('errorMessage', '错误消息'), group: t('coreContent', '核心内容'), span: 2 },
  { prop: 'errorType', label: t('errorType', '异常类名'), group: t('coreContent', '核心内容'), span: 2 },
  { prop: 'errorCode', label: t('errorCode', '业务错误码'), group: t('coreContent', '核心内容'), span: 1 },
  { prop: 'moduleName', label: t('moduleName', '模块名称'), group: t('coreContent', '核心内容'), span: 1 },
  { prop: 'businessType', label: t('businessType', '业务类型'), group: t('coreContent', '核心内容'), span: 1 },
  { prop: 'businessKey', label: t('businessKey', '业务关键字'), group: t('coreContent', '核心内容'), span: 1 },

  // 请求信息
  { prop: 'requestUri', label: t('requestUri', '请求URI'), group: t('requestInfo', '请求信息'), span: 2 },
  { prop: 'requestPattern', label: t('requestPattern', '请求模板'), group: t('requestInfo', '请求信息'), span: 2 },
  { prop: 'requestMethod', label: t('requestMethod', '请求方法'), group: t('requestInfo', '请求信息'), span: 1 },
  { prop: 'requestIp', label: t('requestIp', '请求IP'), group: t('requestInfo', '请求信息'), span: 1 },
  { prop: 'requestParams', label: t('requestParams', '请求参数'), group: t('requestInfo', '请求信息'), span: 2 },
  { prop: 'userAgent', label: t('userAgent', 'User-Agent'), group: t('requestInfo', '请求信息'), span: 2 },
  { prop: 'traceId', label: t('traceId', '链路追踪ID'), group: t('requestInfo', '请求信息'), span: 1 },

  // 用户信息
  { prop: 'userId', label: t('userId', '用户ID'), group: t('userInfo', '用户信息'), span: 1 },
  { prop: 'userName', label: t('userName', '用户名'), group: t('userInfo', '用户信息'), span: 1 },
  { prop: 'deptId', label: t('deptId', '部门ID'), group: t('userInfo', '用户信息'), span: 1 },

  // 环境信息
  { prop: 'clientType', label: t('clientType', '平台类型'), type: 'dict', dictOptions: clientTypeOptions, group: t('envInfo', '环境信息'), span: 1 },
  { prop: 'clientVersion', label: t('clientVersion', '客户端版本'), group: t('envInfo', '环境信息'), span: 1 },
  { prop: 'appVersion', label: t('appVersion', '应用版本'), group: t('envInfo', '环境信息'), span: 1 },
  { prop: 'serverName', label: t('serverName', '服务器名称'), group: t('envInfo', '环境信息'), span: 1 },
  { prop: 'serverIp', label: t('serverIp', '服务器IP'), group: t('envInfo', '环境信息'), span: 1 },

  // 技术细节
  { prop: 'errorStack', label: t('errorStack', '异常堆栈'), type: 'copyable', group: t('techDetails', '技术细节'), span: 2 },
  { prop: 'sqlStatement', label: t('sqlStatement', 'SQL语句'), group: t('techDetails', '技术细节'), span: 2 },
  { prop: 'sqlParams', label: t('sqlParams', 'SQL参数'), group: t('techDetails', '技术细节'), span: 2 },
  { prop: 'sqlDuration', label: t('sqlDuration', 'SQL耗时(ms)'), group: t('techDetails', '技术细节'), span: 1 },

  // 处理记录
  { prop: 'handleBy', label: t('handleBy', '处理人'), group: t('handleRecord', '处理记录'), span: 1 },
  { prop: 'handleTime', label: t('handleTime', '处理时间'), type: 'datetime', group: t('handleRecord', '处理记录'), span: 1 },
  { prop: 'handleRemark', label: t('handleRemark', '处理备注'), group: t('handleRecord', '处理记录'), span: 2 },

  // 系统信息
  { prop: 'id', label: t('id', '主键ID'), group: t('systemInfo', '系统信息'), span: 1 },
  { prop: 'tenantId', label: t('tenantId', '租户ID'), group: t('systemInfo', '系统信息'), span: 1 },
  { prop: 'createTime', label: t('createTime', '创建时间'), type: 'datetime', group: t('systemInfo', '系统信息'), span: 1 },
  { prop: 'remark', label: t('remark', '备注'), group: t('systemInfo', '系统信息'), span: 2 }
])

/** 查看详情操作 */
const handleView = async (row: ErrorLogVo) => {
  const [err, data] = await getErrorLog(row.id)
  if (!err) {
    viewData.value = data
    viewDialog.value.title = `${t('查看')}${t('错误日志')}`
    viewDialog.value.visible = true
  }
}

// =========== 生命周期 ===========
/** 初始化数据列表 */
onMounted(() => {
  getList()
})
/** 页面激活时刷新列表 */
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
