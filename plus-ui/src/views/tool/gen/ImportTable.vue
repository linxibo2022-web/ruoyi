<!-- 导入表 -->
<template>
  <!-- 导入表对话框 -->
  <AModal
    v-model="visible"
    :title="t('Import Table', '导入表')"
    mode="dialog"
    size="xl"
    :loading="buttonLoading"
    @confirm="handleImportTable"
    @cancel="cancel"
    @close="closeDialog"
  >
    <!-- 导入表搜索栏 -->
    <ASearchForm ref="queryFormRef" v-model="queryParams">
      <AFormSelect label="数据源" v-model="queryParams.dataName" prop="dataName" :options="dataNameOptions" @change="handleQuery"></AFormSelect>
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
    </ASearchForm>

    <!-- 导入表表格数据 -->
    <el-table ref="dbTableRef" v-loading="isLoading" :data="dbTableList" height="438" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="t('tableName', '表名称')" prop="tableName" align="center" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('tableComment', '表描述')" prop="tableComment" align="center" min-width="150" show-overflow-tooltip />
      <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" min-width="180" />
      <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" min-width="180" />
    </el-table>

    <Pagination
      :pageSizes="[8, 20, 50, 1000]"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </AModal>
</template>

<script setup lang="ts">
import { pageGenDbs, importGens, getDataSourceNames } from '@/api/tool/gen/genApi'
import type { GenTableQuery, GenTable } from '@/api/tool/gen/genTypes'
import { showMsgSuccess, showMsgError } from '@/utils/modal'
const { t } = useI18n()
// =========== 基础状态相关 ===========
/**对话框显示状态*/
const visible = ref(false)
/**表格加载状态*/
const isLoading = ref(false)
/**按钮加载状态*/
const buttonLoading = ref(false)

// =========== 导入表表格数据相关 ===========
/**数据库表列表*/
const dbTableList = ref<GenTable[]>([])
/**总记录数*/
const total = ref(0)
/**数据源名称选项*/
const dataNameOptions = ref<string[]>([])
/**表格实例*/
const dbTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<GenTable[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: GenTable[]) => {
  selectionItems.value = selection
}

// =========== 表单引用相关 ===========
/**查询表单引用*/
const queryFormRef = ref<ElFormInstance>()

/**查询参数对象*/
const queryParams = ref<GenTableQuery>({
  pageNum: 1,
  pageSize: 8,
  dataName: undefined,
  tableName: undefined,
  tableComment: undefined,
  orderByColumn: 'updateTime,createTime',
  isAsc: 'desc'
})

// =========== 事件定义 ===========
const emit = defineEmits(['ok'])

// =========== 查询操作相关 ===========

/** 导入表搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 导入表重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 查询导入表数据 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageGenDbs(queryParams.value)
  if (!err) {
    dbTableList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

// =========== 对话框操作相关 ===========

/** 显示导入表对话框 */
const show = async (dataName?: string) => {
  const [err, data] = await getDataSourceNames()
  if (!err) {
    dataNameOptions.value = data
    if (dataName) {
      queryParams.value.dataName = dataName
    } else {
      queryParams.value.dataName = 'master'
    }
    await getList()
    visible.value = true
  }
}

/** 取消导入表操作 */
const cancel = () => {
  visible.value = false
  reset()
}

/** 关闭导入表对话框 */
const closeDialog = () => {
  reset()
}

/** 重置导入表数据 */
const reset = () => {
  dbTableList.value = []
  total.value = 0
  selectionItems.value = []
  queryParams.value = {
    pageNum: 1,
    pageSize: 8,
    dataName: undefined,
    tableName: undefined,
    tableComment: undefined,
    orderByColumn: 'updateTime,createTime',
    isAsc: 'desc'
  }
  queryFormRef.value?.resetFields()
}

// =========== 导入操作相关 ===========

/** 导入表按钮操作 */
const handleImportTable = async () => {
  const tableNames = selectionItems.value.map((item) => item.tableName).join(',')
  if (tableNames === '') {
    showMsgError(t('Please select tables to import', '请选择要导入的表'))
    return
  }

  buttonLoading.value = true
  const [err] = await importGens({ tables: tableNames, dataName: queryParams.value.dataName })
  if (!err) {
    showMsgSuccess(t('Import Success', '导入成功'))
    visible.value = false
    emit('ok')
    reset()
  }
  buttonLoading.value = false
}

// =========== 暴露方法 ===========
defineExpose({
  show
})
</script>
