<!-- 代码生成 -->
<template>
  <div>
    <!--  搜索栏  -->
    <ASearchForm ref="queryFormRef" v-model="queryParams" :visible="showSearch">
      <AFormInput label="模糊搜索" prop="searchValue" v-model="queryParams.searchValue" @input="handleQuery"></AFormInput>
      <AFormSelect label="数据源" v-model="queryParams.dataName" prop="dataName" :options="dataNameOptions" @change="handleQuery"></AFormSelect>
      <AFormDate label="创建时间" v-model="dateRange" prop="createTime" type="daterange" @change="handleQuery"></AFormDate>
    </ASearchForm>

    <el-card shadow="hover">
      <!--   工具栏   -->
      <template #header>
        <el-row :gutter="10" class="mb-2">
          <el-col :span="1.5" v-permi="['tool:gen:code']">
            <el-button type="primary" plain :icon="batchGenButtonIcon" @click="handleGenTable()">
              {{ batchGenButtonText }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['tool:gen:import']">
            <el-button type="info" plain icon="Upload" @click="openImportTable">
              {{ t('导入') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['tool:gen:update']">
            <el-button type="success" plain icon="Edit" :disabled="selectionItems.length !== 1" @click="handleEditTable()">
              {{ t('修改') }}
            </el-button>
          </el-col>
          <el-col :span="1.5" v-permi="['tool:gen:delete']">
            <el-button type="danger" plain icon="Delete" :disabled="selectionItems.length === 0" @click="handleDelete()">
              {{ t('删除') }}
            </el-button>
          </el-col>

          <TableToolbar v-model:showSearch="showSearch" @reset-query="resetQuery" @query-table="getList"></TableToolbar>
        </el-row>
      </template>

      <!--   表格数据  -->
      <el-table ref="genTableRef" v-loading="isLoading" :data="tableList" :height="tableHeight" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" :label="t('index', '序号')" align="center" width="60" />
        <el-table-column :label="t('dataName', '数据源')" prop="dataName" align="center" min-width="120" show-overflow-tooltip />
        <el-table-column :label="t('tableName', '表名称')" prop="tableName" align="center" min-width="150" show-overflow-tooltip />
        <el-table-column :label="t('tableComment', '表描述')" prop="tableComment" align="center" min-width="150" show-overflow-tooltip />
        <el-table-column :label="t('className', '实体')" prop="className" align="center" min-width="120" show-overflow-tooltip />
        <el-table-column :label="t('businessName', '业务名')" prop="businessName" align="center" min-width="120" show-overflow-tooltip />
        <el-table-column :label="t('createTime', '创建时间')" prop="createTime" align="center" width="160" />
        <el-table-column :label="t('updateTime', '更新时间')" prop="updateTime" align="center" width="160" />
        <el-table-column :label="t('操作')" align="center" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-tooltip :content="t('button.previewCode')" placement="top">
              <el-button v-permi="['tool:gen:preview']" link type="primary" icon="View" @click="handlePreview(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('button.update')" placement="top">
              <el-button v-permi="['tool:gen:update']" link type="success" icon="Edit" @click="handleEditTable(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('button.delete')" placement="top">
              <el-button v-permi="['tool:gen:delete']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="t('button.sync')" placement="top">
              <el-button v-permi="['tool:gen:update']" link type="primary" icon="Refresh" @click="handleSynchDb(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="row.genType === '1' ? t('Overwrite Code', '覆盖代码') : t('button.generateCode')" placement="top">
              <el-button
                v-permi="['tool:gen:code']"
                link
                type="primary"
                :icon="row.genType === '1' ? 'FolderOpened' : 'Download'"
                @click="handleGenTable(row)"
              ></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 预览界面 -->
    <AModal
      v-model="dialog.visible"
      :title="dialogTitle"
      mode="dialog"
      size="xl"
      :show-fullscreen-toggle="true"
      footer-type="close-only"
      :cancel-text="t('Close', '关闭')"
    >
      <CodePreview :file-data="preview.data" height="calc(80vh - 125px)" />
    </AModal>

    <ImportTable ref="importRef" @ok="handleQuery" />
  </div>
</template>

<script setup lang="ts" name="Gen">
import ImportTable from './ImportTable.vue'
import CodePreview from './CodePreview.vue'
import { useRoute, useRouter } from 'vue-router'
import { pageGens, getDataSourceNames, previewGen, generateCodes, syncGenDb, deleteGens } from '@/api/tool/gen/genApi'
import type { GenTableQuery, GenTable } from '@/api/tool/gen/genTypes'
import { addDateRange } from '@/utils/date'
import { showMsgSuccess, showMsgError, showConfirm } from '@/utils/modal'
import { formatDate } from '@/utils/date'
const { t } = useI18n()

const route = useRoute()
const router = useRouter()

// 使用表格高度处理钩子
const { tableHeight, queryFormRef, showSearch } = useTableHeight()

// =========== 查询相关 ===========
/**查询参数对象*/
const queryParams = ref<GenTableQuery>({
  pageNum: 1,
  pageSize: 10,
  tableName: '',
  tableComment: '',
  dataName: ''
})

/**日期范围选择器*/
const dateRange = ref<[ElDateModelType, ElDateModelType]>(['', ''])

/**数据源列表*/
const dataNameOptions = ref<Array<string>>([])
/**唯一标识*/
const uniqueId = ref('')

/** 查询多数据源名称 */
const getDataNameOptions = async () => {
  const [err, data] = await getDataSourceNames()
  if (!err) {
    dataNameOptions.value = data
  }
}

// =========== 计算属性 ===========
/** 批量生成按钮文字 */
const batchGenButtonText = computed(() => {
  if (selectionItems.value.length === 0) {
    return t('button.generate')
  }
  const allCustomPath = selectionItems.value.every((item) => item.genType === '1')
  const allZip = selectionItems.value.every((item) => item.genType === '0')

  if (allCustomPath) {
    return t('Batch Overwrite', '批量覆盖')
  } else if (allZip) {
    return t('button.generate')
  } else {
    return t('Batch Generate', '批量生成')
  }
})

/** 批量生成按钮图标 */
const batchGenButtonIcon = computed(() => {
  if (selectionItems.value.length === 0) {
    return 'Download'
  }
  const allCustomPath = selectionItems.value.every((item) => item.genType === '1')
  return allCustomPath ? 'FolderOpened' : 'Download'
})

/** 代码生成搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 代码生成重置按钮操作 */
const resetQuery = () => {
  dateRange.value = ['', '']
  queryFormRef.value?.resetFields()
  handleQuery()
}

// =========== 代码生成表格数据相关 ===========
/**表格加载状态*/
const isLoading = ref(true)
/**数据表列表*/
const tableList = ref<GenTable[]>([])
/**总记录数*/
const total = ref(0)
/**表格实例*/
const genTableRef = ref()
/**选中的数据项*/
const selectionItems = ref<GenTable[]>([])

/** 表格多选事件处理 */
const handleSelectionChange = (selection: GenTable[]) => {
  selectionItems.value = selection
}

/** 查询数据表集合 */
const getList = async () => {
  isLoading.value = true
  const [err, data] = await pageGens(addDateRange(queryParams.value, dateRange.value))
  if (!err) {
    tableList.value = data.records
    total.value = data.total
  }
  isLoading.value = false
}

/** 修改代码生成业务按钮操作 */
const handleEditTable = (row?: GenTable) => {
  const tableId = row?.tableId || selectionItems.value[0]?.tableId
  router.push({
    path: '/tool/genEdit/genEdit/' + tableId,
    query: { pageNum: queryParams.value.pageNum }
  })
}

/** 删除代码生成业务操作 */
const handleDelete = async (row?: GenTable) => {
  const tableIds = row ? [row.tableId] : selectionItems.value.map((item) => item.tableId)
  if (tableIds.length === 0) {
    showMsgError(t('Please select data to delete', '请选择要删除的数据'))
    return
  }
  const itemsToDelete = row ? row.tableName : selectionItems.value.map((item) => item.tableName).join(', ')
  const [confirmErr] = await showConfirm(t('Confirm delete tables: "{tables}"?', '是否确认删除下列表: "{tables}"吗？').replace('{tables}', itemsToDelete))
  if (confirmErr) return

  const [err] = await deleteGens(tableIds)
  if (!err) {
    showMsgSuccess(t('Delete Success', '删除成功'))
    await getList()
  }
}

// =========== 代码生成表单和对话框相关 ===========
/**导入表格引用*/
const importRef = ref<InstanceType<typeof ImportTable>>()

/**预览数据*/
const preview = ref<{
  data: Record<string, string>
}>({
  data: {}
})

/**对话框配置对象*/
const dialog = reactive<DialogState>({
  visible: false,
  title: ''
})

/** 对话框标题（国际化） */
const dialogTitle = computed(() => t('Code Preview', '代码预览'))

/**
 * 暂停 HMR 文件监听
 * 在代码生成前调用，防止文件变化触发页面刷新
 */
const pauseHmr = async () => {
  try {
    await fetch('/__hmr_pause')
  } catch (e) {
    // 忽略错误（生产环境或端点不存在时）
  }
}

/**
 * 恢复 HMR 文件监听
 * 在代码生成后调用，恢复正常的热更新功能
 */
const resumeHmr = async () => {
  try {
    await fetch('/__hmr_resume')
  } catch (e) {
    // 忽略错误
  }
}

/**
 * 构建生成结果消息
 */
const buildResultMessage = (data: any, genPath?: string) => {
  let message = `✅ ${t('Successfully generated {count} files', '成功生成 {count} 个文件').replace('{count}', String(data.fileCount || 0))}`
  if (data.overwriteCount && data.overwriteCount > 0) {
    message += `\n⚠️ ${t('Overwrote {count} files', '覆盖了 {count} 个文件').replace('{count}', String(data.overwriteCount))}`
  }
  if (data.menuImportResult) {
    if (data.menuImportResult === '菜单导入成功') {
      message += '\n✅ ' + t('Menu imported successfully', '菜单导入成功')
    } else if (data.menuImportResult === '菜单已存在，跳过导入') {
      message += '\n⏭️ ' + t('Menu already exists, skipped', '菜单已存在，跳过导入')
    } else if (data.menuImportResult.startsWith('菜单导入失败')) {
      message += '\n❌ ' + t('Menu import failed', '菜单导入失败')
    } else {
      message += '\n📦 ' + data.menuImportResult
    }
  }
  if (genPath) {
    message += `\n📁 ${t('Path', '路径')}：${genPath}`
  }
  return message
}

/** 生成代码操作 */
const handleGenTable = async (row?: GenTable) => {
  const download = useDownload()
  const selectedItems = row ? [row] : selectionItems.value

  if (selectedItems.length === 0) {
    showMsgError(t('Please select data to generate', '请选择要生成的数据'))
    return
  }

  // 单个生成
  if (row) {
    if (row.genType === '1') {
      // 覆盖模式：暂停 HMR → 生成代码 → 恢复 HMR
      await pauseHmr()
      try {
        const [err, data] = await generateCodes(row.tableId)
        if (!err) {
          const message = buildResultMessage(data, row.genPath)
          showMsgSuccess({
            message: message,
            duration: 5000
          })
        }
      } finally {
        // 确保无论成功失败都恢复 HMR
        await resumeHmr()
      }
    } else {
      download.downloadZip('/tool/gen/batchGenerateCodes?tableIdStr=' + row.tableId, `ruoyi_${formatDate(new Date(), 'yyyyMMddHHmmss')}.zip`)
    }
    return
  }

  // 批量生成 - 分组处理
  const customPathItems = selectedItems.filter((item) => item.genType === '1')
  const zipItems = selectedItems.filter((item) => item.genType === '0')

  // 处理自定义路径的表（覆盖模式）
  if (customPathItems.length > 0) {
    // 暂停 HMR
    await pauseHmr()

    try {
      let successCount = 0
      let totalFiles = 0
      let totalOverwrites = 0
      const menuResults: string[] = []

      for (const item of customPathItems) {
        const [err, data] = await generateCodes(item.tableId)
        if (!err) {
          successCount++
          totalFiles += data.fileCount || 0
          totalOverwrites += data.overwriteCount || 0
          if (data.menuImportResult) {
            menuResults.push(data.menuImportResult)
          }
        }
      }

      if (successCount > 0) {
        let message = `✅ ${t('Successfully overwrote {tableCount} tables, generated {fileCount} files', '成功覆盖 {tableCount} 个表的代码，共生成 {fileCount} 个文件').replace('{tableCount}', String(successCount)).replace('{fileCount}', String(totalFiles))}`
        if (totalOverwrites > 0) {
          message += `\n⚠️ ${t('Overwrote {count} files', '覆盖了 {count} 个文件').replace('{count}', String(totalOverwrites))}`
        }
        // 统计菜单导入结果
        const successMenus = menuResults.filter((r) => r === '菜单导入成功').length
        const skipMenus = menuResults.filter((r) => r === '菜单已存在，跳过导入').length
        if (successMenus > 0) {
          message += `\n✅ ${t('Successfully imported {count} menus', '成功导入 {count} 个菜单').replace('{count}', String(successMenus))}`
        }
        if (skipMenus > 0) {
          message += `\n⏭️ ${t('Skipped {count} existing menus', '跳过 {count} 个已存在的菜单').replace('{count}', String(skipMenus))}`
        }
        showMsgSuccess({
          message: message,
          duration: 5000
        })
      }
    } finally {
      // 确保恢复 HMR
      await resumeHmr()
    }
  }

  // 处理zip压缩包的表（不需要暂停 HMR，因为不覆盖本地文件）
  if (zipItems.length > 0) {
    const zipIds = zipItems.map((item) => item.tableId).join(',')
    download.downloadZip('/tool/gen/batchGenerateCodes?tableIdStr=' + zipIds, `ruoyi_${formatDate(new Date(), 'yyyyMMddHHmmss')}.zip`)
  }
}

/** 同步数据库操作 */
const handleSynchDb = async (row: GenTable) => {
  const [confirmErr] = await showConfirm(t('Confirm force sync "{table}" structure?', '确认要强制同步"{table}"表结构吗？').replace('{table}', row.tableName))
  if (confirmErr) return

  const [err] = await syncGenDb(row.tableId)
  if (!err) {
    showMsgSuccess(t('Sync Success', '同步成功'))
  }
}

/** 预览按钮 */
const handlePreview = async (row: GenTable) => {
  const [err, data] = await previewGen(row.tableId)
  if (!err) {
    preview.value.data = data
    dialog.visible = true
  }
}

/** 打开导入表弹窗 */
const openImportTable = () => {
  importRef.value?.show(queryParams.value.dataName)
}

// =========== 生命周期 ===========
/**初始化代码生成数据列表*/
onMounted(() => {
  const time = route.query.t
  if (time != null && time != uniqueId.value) {
    uniqueId.value = time as string
    queryParams.value.pageNum = Number(route.query.pageNum)
    dateRange.value = ['', '']
    queryFormRef.value?.resetFields()
  }
  getList()
  getDataNameOptions()
})

/**页面激活时检查是否需要刷新*/
onActivated(() => {
  const time = route.query.t
  if (time != null && time != uniqueId.value) {
    uniqueId.value = time as string
    queryParams.value.pageNum = Number(route.query.pageNum)
    getList()
  }
})
</script>
