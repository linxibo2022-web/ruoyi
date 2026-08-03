<!-- 操作日志 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="操作类型" v-model="queryParams.operType" prop="operType" :options="sys_oper_type" @change="handleQuery"></AFormSelect>
      <AFormSelect label="操作结果" v-model="queryParams.status" prop="status" :options="sys_oper_result" @change="handleQuery"></AFormSelect>
      <AFormDate label="操作时间" v-model="dateRange" prop="operTime" type="daterange" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['monitor:operLog:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:operLog:delete']">
            <el-button type="danger" plain icon="WarnTriangleFilled" @click="handleClean">
              {{ t('button.clean') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['monitor:operLog:export']">
            <el-button type="warning" plain icon="Download" @click="handleExport">
              {{ t('导出') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table
        ref="operLogTableRef"
        v-loading="isLoading"
        :data="operLogList"
        :height="tableHeight"
        :default-sort="{ prop: 'operTime', order: 'descending' }"
        @sort-change="handleSortChange"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="t('operId', '日志编号')" prop="operId" align="center" width="180" />
        <el-table-column :label="t('title', '系统模块')" prop="title" align="center" min-width="90" />
        <el-table-column :label="t('operType', '操作类型')" prop="operType" align="center">
          <template #default="{ row }">
            <DictTag :options="sys_oper_type" :value="row.operType" />
          </template>
        </el-table-column>
        <el-table-column :label="t('operName', '操作人员')" prop="operName" align="center" sortable="custom" />
        <el-table-column :label="t('deptName', '部门')" prop="deptName" align="center" />
        <el-table-column :label="t('operIp', '操作地址')" prop="operIp" align="center" />
        <el-table-column :label="t('status', '操作结果')" prop="status" align="center">
          <template #default="{ row }">
            <DictTag :options="sys_oper_result" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="t('operTime', '操作日期')" prop="operTime" align="center" width="180" sortable="custom">
          <template #default="{ row }">
            <span>{{ formatDate(row.operTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('costTime', '消耗时间')" prop="costTime" align="center" sortable="custom">
          <template #default="{ row }">
            <span>{{ row.costTime }}{{ t('ms', '毫秒') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('操作')" align="center" width="90" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('button.view')" placement="top">
              <el-button v-permi="['monitor:operLog:query']" link type="primary" icon="View" @click="handleView(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 操作日志详细 -->
    <OperInfoDialog ref="operInfoDialogRef" />
  </div>
</template>

<script setup lang="ts" name="Operlog">
import { pageOperLogs, deleteOperLogs, clearOperLogs } from '@/api/system/monitor/operLog/operLogApi'
import type { SysOperLogQuery, SysOperLogVo } from '@/api/system/monitor/operLog/operLogTypes'
import OperInfoDialog from './operInfoDialog.vue'
import { formatDate } from '@/utils/date'
import { addDateRange } from '@/utils/date'
import { showMsgSuccess, showConfirm } from '@/utils/modal'
const { t } = useI18n()

/**字典数据 */
const { sys_oper_type, sys_oper_result } = useDict('sys_oper_type', 'sys_oper_result')

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========
/**查询参数对象*/
const queryParams = ref<SysOperLogQuery>({
  pageNum: 1,
  pageSize: 10,
  operIp: '',
  title: '',
  operName: '',
  operType: '',
  status: '',
  orderByColumn: 'operTime',
  isAsc: 'desc'
})

/**日期范围选择器*/
const dateRange = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/** 操作日志搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 操作日志重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', '']
  queryFormRef.value?.resetFields()
  queryParams.value.pageNum = 1
  queryParams.value.orderByColumn = 'operTime'
  queryParams.value.isAsc = 'desc'
  handleQuery()
}

// =========== 操作日志表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**操作日志数据列表*/
const operLogList = ref<SysOperLogVo[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const operLogTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<SysOperLogVo[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: SysOperLogVo[]) => {
  selectionItems.value = selection
}

/** 查询操作日志列表 */
const getList = async () => {
  isLoading.value = true
  addDateRange(queryParams.value, dateRange.value)
  const [err, data] = await pageOperLogs(queryParams.value)
  if (!err) {
    operLogList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 导出操作日志数据 */
const handleExport = () => {
  useDownload().exportExcel(t('operLog', '操作日志'), '/monitor/operLog/exportOperLogs', queryParams.value)
}

/** 排序触发事件 */
const handleSortChange = (column: any) => {
  queryParams.value.orderByColumn = column.prop
  queryParams.value.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  getList()
}

/** 查看操作日志详情 */
const operInfoDialogRef = ref<InstanceType<typeof OperInfoDialog>>()
/** 详细按钮操作 */
const handleView = (row: SysOperLogVo) => {
  operInfoDialogRef.value.openDialog(row)
}

/** 删除操作日志操作 */
const handleDelete = async (row?: SysOperLogVo) => {
  const operIds = row ? [row.operId] : selectionItems.value.map((item) => item.operId)
  if (operIds.length === 0) return

  const [confirmErr] = await showConfirm(`${t('是否确认删除')}${operIds.join(',')}`)
  if (confirmErr) return

  const [deleteErr] = await deleteOperLogs(operIds)
  if (!deleteErr) {
    showMsgSuccess(t('message.deleteSuccess'))
    await getList()
  }
}

/** 清空操作日志操作 */
const handleClean = async () => {
  const [confirmErr] = await showConfirm(t('message.confirmCleanAll'))
  if (confirmErr) return

  const [clearErr] = await clearOperLogs()
  if (!clearErr) {
    showMsgSuccess(t('Clear oper log success', '清空操作日志成功'))
    await getList()
  }
}

// =========== 生命周期 ===========
/**初始化操作日志数据列表*/
onMounted(() => {
  getList()
})

/**页面激活时刷新操作日志列表*/
onActivated(() => {
  if (isLoading.value) return
  getList()
})
</script>
