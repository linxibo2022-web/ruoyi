<!--
  ATable 通用表格组件

  使用示例：

  1. 基本用法
  <ATable
    :data="dataList"
    :columns="columns"
    :loading="isLoading"
    :height="tableHeight"
  />

  2. 带选择和分页
  <ATable
    :data="dataList"
    :columns="columns"
    :loading="isLoading"
    :height="tableHeight"
    show-selection
    show-pagination
    v-model:page="queryParams.pageNum"
    v-model:limit="queryParams.pageSize"
    :total="total"
    @selection-change="handleSelectionChange"
    @pagination="getList"
  />

  3. 列配置示例
  const columns: TableColumnConfig[] = [
    { prop: 'name', label: '名称', minWidth: 120 },
    { prop: 'status', label: '状态', type: 'dict', dictOptions: sys_enable_status, width: 100 },
    { prop: 'image', label: '图片', type: 'image', imageConfig: { width: 60, height: 60 } },
    { prop: 'createTime', label: '创建时间', type: 'datetime', width: 180 },
    {
      prop: 'actions',
      label: '操作',
      type: 'actions',
      width: 150,
      fixed: 'right',
      actions: [
        { icon: 'View', tooltip: '详情', onClick: handleView },
        { icon: 'Edit', tooltip: '编辑', permission: ['system:xxx:update'], onClick: handleEdit },
        { icon: 'Delete', tooltip: '删除', type: 'danger', permission: ['system:xxx:delete'], onClick: handleDelete }
      ]
    }
  ]

  4. 状态开关列
  {
    prop: 'status',
    label: '状态',
    type: 'switch',
    switchDisabled: (row) => row.isSystem === '1',
    onSwitchChange: handleStatusChange
  }

  5. 自定义插槽
  <ATable :data="dataList" :columns="columns">
    <template #customSlot="{ row, index }">
      <el-tag>{{ row.customField }}</el-tag>
    </template>
  </ATable>

  6. 带序号列
  <ATable
    :data="dataList"
    :columns="columns"
    show-index
    index-label="序号"
  />
-->

<template>
  <div class="a-table-wrapper">
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="data"
      :height="height"
      :max-height="maxHeight"
      :stripe="stripe"
      :border="border"
      :row-key="rowKey"
      :empty-text="computedEmptyText"
      :default-sort="defaultSort"
      :highlight-current-row="highlightCurrentRow"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDblclick"
      @current-change="handleCurrentChange"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        :width="selectionWidth"
        align="center"
        :selectable="selectable"
        :reserve-selection="reserveSelection"
      />

      <!-- 序号列 -->
      <el-table-column v-if="showIndex" type="index" :width="indexWidth" :label="computedIndexLabel" align="center" :index="indexMethod" />

      <!-- 数据列 -->
      <el-table-column
        v-for="column in visibleColumns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth || 100"
        :align="column.align || 'center'"
        :fixed="column.fixed"
        :sortable="column.sortable"
        :show-overflow-tooltip="column.showOverflowTooltip ?? true"
      >
        <template #default="{ row, $index }">
          <!-- 自定义插槽 -->
          <slot v-if="column.slot" :name="column.slot" :row="row" :column="column" :index="$index" />

          <!-- 字典类型 -->
          <template v-else-if="column.type === 'dict'">
            <DictTag :options="column.dictOptions" :value="getFieldValue(row, column.prop)" />
          </template>

          <!-- 开关类型 -->
          <template v-else-if="column.type === 'switch'">
            <AFormSwitch
              :model-value="getFieldValue(row, column.prop)"
              :disabled="isSwitchDisabled(row, column)"
              @change="(val: any) => handleSwitchChange(row, column, val)"
            />
          </template>

          <!-- 图片类型 -->
          <template v-else-if="column.type === 'image'">
            <ImagePreview
              :src="getFieldValue(row, column.prop)"
              :width="column.imageConfig?.width || 60"
              :height="column.imageConfig?.height || 60"
              :show-all="column.imageConfig?.showAll ?? true"
            />
          </template>

          <!-- 日期时间类型 -->
          <template v-else-if="column.type === 'datetime'">
            {{ formatDateTime(getFieldValue(row, column.prop)) }}
          </template>

          <!-- 日期类型 -->
          <template v-else-if="column.type === 'date'">
            {{ formatDateValue(getFieldValue(row, column.prop)) }}
          </template>

          <!-- 货币类型 -->
          <template v-else-if="column.type === 'currency'">
            {{ formatCurrency(getFieldValue(row, column.prop)) }}
          </template>

          <!-- 布尔类型 -->
          <template v-else-if="column.type === 'boolean'">
            {{ getFieldValue(row, column.prop) ? t('table.booleanYes') : t('table.booleanNo') }}
          </template>

          <!-- 可复制类型 -->
          <template v-else-if="column.type === 'copyable'">
            <div v-if="getFieldValue(row, column.prop)" class="flex items-center justify-center gap-1">
              <span class="truncate">{{ getFieldValue(row, column.prop) }}</span>
              <el-button
                link
                type="primary"
                icon="CopyDocument"
                class="flex-shrink-0"
                @click.stop="handleCopy(getFieldValue(row, column.prop), column.label)"
              />
            </div>
            <span v-else>-</span>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.type === 'actions'">
            <div class="flex items-center justify-center gap-1">
              <template v-for="(action, actionIndex) in getVisibleActions(row, column.actions)" :key="actionIndex">
                <el-tooltip v-if="action.tooltip" :content="action.tooltip" placement="top">
                  <el-button
                    v-permi="action.permission"
                    link
                    :type="action.type || 'primary'"
                    :icon="action.icon"
                    :disabled="isActionDisabled(row, action)"
                    @click.stop="action.onClick(row, $index)"
                  >
                    {{ action.text }}
                  </el-button>
                </el-tooltip>
                <el-button
                  v-else
                  v-permi="action.permission"
                  link
                  :type="action.type || 'primary'"
                  :icon="action.icon"
                  :disabled="isActionDisabled(row, action)"
                  @click.stop="action.onClick(row, $index)"
                >
                  {{ action.text }}
                </el-button>
              </template>
            </div>
          </template>

          <!-- 默认文本 -->
          <template v-else>
            {{ formatValue(row, column) }}
          </template>
        </template>
      </el-table-column>

      <!-- 默认插槽（用于添加额外的列） -->
      <slot></slot>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-if="showPagination"
      v-model:page="currentPage"
      v-model:limit="currentLimit"
      :total="total"
      :page-sizes="pageSizes"
      @pagination="handlePagination"
    />
  </div>
</template>

<script setup lang="ts" name="ATable">
import { copy } from '@/utils/function'
import { formatDate, formatDay } from '@/utils/date'

const { t } = useI18n()

/** 组件 Props 定义 */
interface ATableProps {
  /** 表格数据 */
  data: any[]
  /** 列配置 */
  columns: TableColumnConfig[]
  /** 是否显示加载状态 */
  loading?: boolean
  /** 表格高度 */
  height?: string | number
  /** 表格最大高度 */
  maxHeight?: string | number
  /** 是否显示斑马纹 */
  stripe?: boolean
  /** 是否显示边框 */
  border?: boolean
  /** 行数据的 Key */
  rowKey?: string | ((row: any) => string)
  /** 空数据描述 */
  emptyText?: string
  /** 默认排序 */
  defaultSort?: { prop: string; order: 'ascending' | 'descending' }
  /** 是否高亮当前行 */
  highlightCurrentRow?: boolean

  // ========== 选择相关 ==========
  /** 是否显示选择列 */
  showSelection?: boolean
  /** 选择列宽度 */
  selectionWidth?: number
  /** 选择列是否可选 */
  selectable?: (row: any, index: number) => boolean
  /** 是否保留之前选择 */
  reserveSelection?: boolean

  // ========== 序号相关 ==========
  /** 是否显示序号列 */
  showIndex?: boolean
  /** 序号列宽度 */
  indexWidth?: number
  /** 序号列标签 */
  indexLabel?: string

  // ========== 分页相关 ==========
  /** 是否显示分页 */
  showPagination?: boolean
  /** 当前页码 */
  page?: number
  /** 每页大小 */
  limit?: number
  /** 总记录数 */
  total?: number
  /** 分页大小选项 */
  pageSizes?: number[]
}

/** 组件 Props */
const props = withDefaults(defineProps<ATableProps>(), {
  data: () => [],
  columns: () => [],
  loading: false,
  stripe: true,
  border: false,
  rowKey: 'id',
  emptyText: '暂无数据',
  highlightCurrentRow: false,
  showSelection: false,
  selectionWidth: 50,
  reserveSelection: false,
  showIndex: false,
  indexWidth: 60,
  indexLabel: '序号',
  showPagination: false,
  page: 1,
  limit: 10,
  total: 0,
  pageSizes: () => [10, 20, 50, 100]
})

/** 事件定义 */
const emit = defineEmits<{
  /** 更新页码 */
  'update:page': [value: number]
  /** 更新每页大小 */
  'update:limit': [value: number]
  /** 选择变化 */
  'selection-change': [selection: any[]]
  /** 排序变化 */
  'sort-change': [sortInfo: { column: any; prop: string; order: string | null }]
  /** 分页事件 */
  pagination: []
  /** 行点击 */
  'row-click': [row: any, column: any, event: Event]
  /** 行双击 */
  'row-dblclick': [row: any, column: any, event: Event]
  /** 当前行变化 */
  'current-change': [currentRow: any, oldCurrentRow: any]
}>()

/** 表格实例引用 */
const tableRef = ref()

/** 国际化的空数据文本 */
const computedEmptyText = computed(() => {
  return props.emptyText === '暂无数据' ? t('table.emptyText') : props.emptyText
})

/** 国际化的序号列标签 */
const computedIndexLabel = computed(() => {
  return props.indexLabel === '序号' ? t('table.indexLabel') : props.indexLabel
})

/** 过滤可见列 */
const visibleColumns = computed(() => {
  return props.columns.filter((column) => !column.hidden)
})

/** 分页双向绑定 - 页码 */
const currentPage = computed({
  get: () => props.page,
  set: (value) => emit('update:page', value)
})

/** 分页双向绑定 - 每页大小 */
const currentLimit = computed({
  get: () => props.limit,
  set: (value) => emit('update:limit', value)
})

/**
 * 序号计算方法
 * 支持分页时的连续序号
 */
const indexMethod = (index: number) => {
  if (props.showPagination) {
    return (props.page - 1) * props.limit + index + 1
  }
  return index + 1
}

/**
 * 获取嵌套字段值
 * 支持 'user.profile.name' 格式
 */
const getFieldValue = (row: any, prop: string) => {
  if (!prop) return ''
  return prop.split('.').reduce((obj, key) => obj?.[key], row)
}

/**
 * 格式化显示值
 */
const formatValue = (row: any, column: TableColumnConfig) => {
  const value = getFieldValue(row, column.prop)
  if (value === null || value === undefined || value === '') return '-'
  if (column.formatter) return column.formatter(value, row)
  return String(value)
}

/**
 * 格式化日期时间
 */
const formatDateTime = (value: any) => {
  if (!value) return '-'
  return formatDate(value)
}

/**
 * 格式化日期
 */
const formatDateValue = (value: any) => {
  if (!value) return '-'
  return formatDay(value)
}

/**
 * 格式化货币
 */
const formatCurrency = (value: any) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${t('table.currencySymbol')}${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

/**
 * 复制内容
 */
const handleCopy = (value: any, label?: string) => {
  copy(value, label ? t('table.copyFieldSuccess', { field: label }) : t('table.copySuccess'))
}

/**
 * 获取可见的操作按钮
 */
const getVisibleActions = (row: any, actions?: TableActionConfig[]) => {
  if (!actions) return []
  return actions.filter((action) => {
    if (typeof action.show === 'function') return action.show(row)
    return action.show !== false
  })
}

/**
 * 判断操作按钮是否禁用
 */
const isActionDisabled = (row: any, action: TableActionConfig) => {
  if (typeof action.disabled === 'function') return action.disabled(row)
  return action.disabled === true
}

/**
 * 判断开关是否禁用
 */
const isSwitchDisabled = (row: any, column: TableColumnConfig) => {
  if (typeof column.switchDisabled === 'function') return column.switchDisabled(row)
  return column.switchDisabled === true
}

/**
 * 处理开关变化
 */
const handleSwitchChange = (row: any, column: TableColumnConfig, value: any) => {
  if (column.onSwitchChange) {
    column.onSwitchChange(row, value)
  }
}

/**
 * 处理选择变化
 */
const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

/**
 * 处理排序变化
 */
const handleSortChange = (sortInfo: { column: any; prop: string; order: string | null }) => {
  emit('sort-change', sortInfo)
}

/**
 * 处理分页
 */
const handlePagination = () => {
  emit('pagination')
}

/**
 * 处理行点击
 */
const handleRowClick = (row: any, column: any, event: Event) => {
  emit('row-click', row, column, event)
}

/**
 * 处理行双击
 */
const handleRowDblclick = (row: any, column: any, event: Event) => {
  emit('row-dblclick', row, column, event)
}

/**
 * 处理当前行变化
 */
const handleCurrentChange = (currentRow: any, oldCurrentRow: any) => {
  emit('current-change', currentRow, oldCurrentRow)
}

/** 暴露方法给父组件 */
defineExpose({
  /** 获取表格实例 */
  getTableRef: () => tableRef.value,
  /** 清空选择 */
  clearSelection: () => tableRef.value?.clearSelection(),
  /** 切换行选择 */
  toggleRowSelection: (row: any, selected?: boolean) => tableRef.value?.toggleRowSelection(row, selected),
  /** 切换全选 */
  toggleAllSelection: () => tableRef.value?.toggleAllSelection(),
  /** 设置当前行 */
  setCurrentRow: (row: any) => tableRef.value?.setCurrentRow(row),
  /** 排序 */
  sort: (prop: string, order: string) => tableRef.value?.sort(prop, order),
  /** 清空排序 */
  clearSort: () => tableRef.value?.clearSort(),
  /** 滚动到指定位置 */
  scrollTo: (options: ScrollToOptions | number, yCoord?: number) => tableRef.value?.scrollTo(options, yCoord),
  /** 设置滚动位置 */
  setScrollTop: (top: number) => tableRef.value?.setScrollTop(top),
  /** 设置水平滚动位置 */
  setScrollLeft: (left: number) => tableRef.value?.setScrollLeft(left)
})
</script>

<style scoped>
.a-table-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.a-table-wrapper :deep(.el-table) {
  flex: 1;
}
</style>
