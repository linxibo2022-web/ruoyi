<!-- 登录日志 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="操作结果" v-model="queryParams.status" prop="status" :options="sys_oper_result" @change="handleQuery"></AFormSelect>
      <AFormDate v-model="dateRange" prop="loginTime" type="daterange" label="登录时间" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['monitor:loginLog:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:loginLog:delete']">
            <el-button type="danger" plain icon="Delete" @click="handleClean">
              {{ t('button.clean') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:loginLog:unlock']">
            <el-button type="primary" plain icon="Unlock" :disabled="selectionItems.length !== 1" @click="handleUnlock">
              {{ t('button.unlock') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:loginLog:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table
        ref="loginLogTableRef"
        v-loading="isLoading"
        :data="loginInfoList"
        :height="tableHeight"
        :default-sort="{ prop: 'loginTime', order: 'descending' }"
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="t('infoId', '访问编号')" prop="infoId" align="center" width="170" />
        <el-table-column :label="t('userId', '用户id')" prop="userId" align="center" width="170" />
        <el-table-column :label="t('userName', '用户名称')" prop="userName" align="center" sortable="custom" />
        <el-table-column :label="t('deviceType', '设备类型')" prop="deviceType" align="center" />
        <el-table-column :label="t('ipaddr', '地址')" prop="ipaddr" align="center" />
        <el-table-column :label="t('loginLocation', '登录地点')" prop="loginLocation" align="center" />
        <el-table-column :label="t('os', '操作系统')" prop="os" align="center" show-overflow-tooltip />
        <el-table-column :label="t('browser', '浏览器')" prop="browser" align="center" />
        <el-table-column :label="t('status', '登录状态')" prop="status" align="center">
          <template #default="{ row }">
            <DictTag :options="sys_oper_result" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="t('msg', '描述')" prop="msg" align="center" />
        <el-table-column :label="t('loginTime', '访问时间')" prop="loginTime" align="center" width="105" sortable="custom">
          <template #default="{ row }">
            <span>{{ row.loginTime }}</span>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="LoginLog">
import { pageLoginLogs, deleteLoginLogs, clearLoginLogs, unlockLoginLog } from '@/api/system/monitor/loginLog/loginLogApi'
import type { SysLoginLogQuery, SysLoginLogVo } from '@/api/system/monitor/loginLog/loginLogTypes'
import { addDateRange } from '@/utils/date'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
const { t } = useI18n()

/**字典数据 */
const { sys_oper_result } = useDict('sys_oper_result')

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========

/**查询参数对象*/
const queryParams = ref<SysLoginLogQuery>({
  pageNum: 1,
  pageSize: 10,
  ipaddr: '',
  userName: '',
  status: '',
  orderByColumn: 'loginTime',
  isAsc: 'desc'
})

/**日期范围选择器*/
const dateRange = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 登录日志搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 登录日志重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', '']
  queryFormRef.value?.resetFields()
  queryParams.value.pageNum = 1
  queryParams.value.orderByColumn = 'loginTime'
  queryParams.value.isAsc = 'desc'
  handleQuery()
}

// =========== 登录日志表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**登录日志数据列表*/
const loginInfoList = ref<SysLoginLogVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const loginLogTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysLoginLogVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysLoginLogVo[]) => {
  selectionItems.value = selection
}

/** 查询登录日志列表 */
const getList = async () => {
  isLoading.value = true
  addDateRange(queryParams.value, dateRange.value)
  const [err, data] = await pageLoginLogs(queryParams.value)
  if (!err) {
    loginInfoList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出登录日志数据 */
const handleExport = () => {
  useDownload().exportExcel('登录日志', '/monitor/loginLog/exportLoginLogs', queryParams.value)
}

/** 排序触发事件 */
const handleSortChange = (column: any) => {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  getList()
}

/** 删除登录日志操作 */
const handleDelete = async (row?: SysLoginLogVo) => {
  const infoIds = row ? [row.infoId] : selectionItems.value.map((item) => item.infoId)
  if (infoIds.length === 0) return

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${infoIds.join(',')}`)
  if (confirmErr) return

  const [deleteErr] = await deleteLoginLogs(infoIds)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

/** 清空登录日志操作 */
const handleClean = async () => {
  const [confirmErr] = await showConfirm(t('message.confirmCleanAll'))
  if (confirmErr) return

  const [clearErr] = await clearLoginLogs()
  if (!clearErr) {
    showMsgSuccess('已清空登录日志')
    await getList()
  }
}

/** 解锁用户操作 */
const handleUnlock = async () => {
  if (selectionItems.value.length !== 1) return

  const userName = selectionItems.value[0].userName
  const [confirmErr] = await showConfirm(`${t('message.confirmUnlock')}${userName}?`)
  if (confirmErr) return

  const [unlockErr] = await unlockLoginLog(userName)
  if (!unlockErr) {
    showMsgSuccess(`${t('message.unlockSuccess')}${userName}`)
  }
}

// =========== 生命周期 ===========
/**初始化登录日志数据列表*/
onMounted(() => {
  getList()
})
</script>
