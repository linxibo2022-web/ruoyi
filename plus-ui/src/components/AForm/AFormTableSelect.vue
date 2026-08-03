<!--
  AFormTableSelect 表格弹窗选择组件

  使用示例：

  1. 基本用法（单选）
  <AFormTableSelect
    v-model="form.storeId"
    label="选择门店"
    prop="storeId"
    :columns="storeColumns"
    :api="pageStores"
    value-key="id"
    label-key="storeName"
  />

  2. 编辑回显（使用 initial-label 避免显示 ID）
  <AFormTableSelect
    v-model="form.storeId"
    label="选择门店"
    prop="storeId"
    :columns="storeColumns"
    :api="pageStores"
    value-key="id"
    label-key="storeName"
    :initial-label="form.storeName"
  />
  说明：编辑表单时，form.storeName 已从详情接口获取，
       传入 initial-label 后，组件会显示 "北京门店" 而不是 ID "123"

  3. 多选模式
  <AFormTableSelect
    v-model="form.storeIds"
    label="选择门店"
    prop="storeIds"
    multiple
    :columns="storeColumns"
    :api="pageStores"
    show-tags
  />

  4. 静态数据
  <AFormTableSelect
    v-model="form.storeId"
    label="选择门店"
    :columns="storeColumns"
    :data="storeList"
  />

  5. 列配置示例
  const storeColumns: TableColumnConfig[] = [
    { prop: 'storeName', label: '门店名称', minWidth: 150 },
    { prop: 'address', label: '地址', minWidth: 200 },
    { prop: 'status', label: '状态', type: 'dict', dictOptions: sys_enable_status, width: 100 }
  ]
-->

<template>
  <!-- 模式1: 有 span 且显示 form-item -->
  <el-col v-if="shouldUseCol && showFormItem" :span="computedSpan">
    <el-form-item :label="computedLabel" :label-width="labelWidth" :prop="prop" :required="required">
      <!-- 多选标签模式 -->
      <div v-if="multiple && showTags && hasSelection" class="a-form-table-select-tags" @click="handleTriggerClick">
        <el-tag
          v-for="item in displayItems.slice(0, maxTagCount)"
          :key="getItemValue(item)"
          size="small"
          closable
          :disabled="disabled"
          class="selection-tag"
          @close.stop="handleRemoveItem(item)"
        >
          {{ getItemLabel(item) }}
        </el-tag>
        <el-tag v-if="displayItems.length > maxTagCount" size="small" type="info"> +{{ displayItems.length - maxTagCount }} </el-tag>
        <el-icon class="grid-icon-inline"><Grid /></el-icon>
      </div>
      <!-- 单选或无选中：使用 el-input 做触发器（宽度自动与其他表单项一致） -->
      <el-input
        v-else
        :model-value="displayText"
        :placeholder="placeholder"
        readonly
        :disabled="disabled"
        :suffix-icon="Grid"
        :clearable="clearable && hasSelection"
        :style="computedWidth ? { width: computedWidth } : {}"
        @click="handleTriggerClick"
        @clear="handleClear"
      />
    </el-form-item>
  </el-col>

  <!-- 模式2: 无 span 但显示 form-item -->
  <el-form-item v-else-if="showFormItem" :label="computedLabel" :label-width="labelWidth" :prop="prop" :required="required">
    <div v-if="multiple && showTags && hasSelection" class="a-form-table-select-tags" @click="handleTriggerClick">
      <el-tag
        v-for="item in displayItems.slice(0, maxTagCount)"
        :key="getItemValue(item)"
        size="small"
        closable
        :disabled="disabled"
        class="selection-tag"
        @close.stop="handleRemoveItem(item)"
      >
        {{ getItemLabel(item) }}
      </el-tag>
      <el-tag v-if="displayItems.length > maxTagCount" size="small" type="info"> +{{ displayItems.length - maxTagCount }} </el-tag>
      <el-icon class="grid-icon-inline"><Grid /></el-icon>
    </div>
    <el-input
      v-else
      :model-value="displayText"
      :placeholder="placeholder"
      readonly
      :disabled="disabled"
      :suffix-icon="Grid"
      :clearable="clearable && hasSelection"
      :style="computedWidth ? { width: computedWidth } : {}"
      @click="handleTriggerClick"
      @clear="handleClear"
    />
  </el-form-item>

  <!-- 模式3: 不显示 form-item -->
  <template v-else>
    <div v-if="multiple && showTags && hasSelection" class="a-form-table-select-tags" @click="handleTriggerClick">
      <el-tag
        v-for="item in displayItems.slice(0, maxTagCount)"
        :key="getItemValue(item)"
        size="small"
        closable
        :disabled="disabled"
        class="selection-tag"
        @close.stop="handleRemoveItem(item)"
      >
        {{ getItemLabel(item) }}
      </el-tag>
      <el-tag v-if="displayItems.length > maxTagCount" size="small" type="info"> +{{ displayItems.length - maxTagCount }} </el-tag>
      <el-icon class="grid-icon-inline"><Grid /></el-icon>
    </div>
    <el-input
      v-else
      :model-value="displayText"
      :placeholder="placeholder"
      readonly
      :disabled="disabled"
      :suffix-icon="Grid"
      :clearable="clearable && hasSelection"
      :style="computedWidth ? { width: computedWidth } : {}"
      @click="handleTriggerClick"
      @clear="handleClear"
    />
  </template>

  <!-- 选择弹窗 -->
  <AModal v-model="dialogVisible" :title="modalTitle" :size="modalSize" :loading="loading" @confirm="handleConfirm" @cancel="handleCancel">
    <!-- 搜索区域 -->
    <div v-if="showSearch" class="search-area mb-3">
      <el-input v-model="searchValue" :placeholder="searchPlaceholder" clearable prefix-icon="Search" @input="handleSearch" />
    </div>

    <!-- 表格区域 -->
    <el-table
      ref="tableRef"
      v-loading="tableLoading"
      :data="tableData"
      :height="tableHeight"
      stripe
      :row-key="valueKey"
      highlight-current-row
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange"
    >
      <!-- 多选列 -->
      <el-table-column v-if="multiple" type="selection" width="50" align="center" :reserve-selection="true" />

      <!-- 单选列 -->
      <el-table-column v-else width="50" align="center">
        <template #default="{ row }">
          <el-radio v-model="selectedRadioValue" :value="getItemValue(row)" @change="handleRadioChange(row)">
            <span></span>
          </el-radio>
        </template>
      </el-table-column>

      <!-- 数据列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth || 100"
        :align="column.align || 'center'"
        :show-overflow-tooltip="column.showOverflowTooltip ?? true"
      >
        <template #default="{ row }">
          <!-- 图片类型 -->
          <template v-if="column.type === 'image'">
            <ImagePreview :src="getNestedValue(row, column.prop)" />
          </template>
          <!-- 字典类型 -->
          <template v-else-if="column.type === 'dict'">
            <DictTag :options="column.dictOptions" :value="getNestedValue(row, column.prop)" />
          </template>
          <!-- 默认文本 -->
          <template v-else>
            {{ formatColumnValue(row, column) }}
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-if="showPagination" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="loadData" />

    <!-- 已选择提示 -->
    <div v-if="multiple && tempSelection.length > 0" class="selection-tip mt-2">
      <el-text type="info" size="small">
        {{ t('userSelect.selectedCount', { count: tempSelection.length }) }}
        <el-button type="primary" link size="small" @click="handleClearTemp">{{ t('button.clean') }}</el-button>
      </el-text>
    </div>
  </AModal>
</template>

<script setup lang="ts" name="AFormTableSelect">
import { useResponsiveSpan } from '@/composables/useResponsiveSpan'
import { Grid } from '@element-plus/icons-vue'
import { debounce } from '@/utils/function'

/** 组件 Props 定义 */
interface AFormTableSelectProps {
  /** 绑定值，单选为单个值，多选为数组 */
  modelValue?: any
  /** 是否多选 */
  multiple?: boolean
  /** 静态数据 */
  data?: any[]
  /** 动态加载 API */
  api?: (params: any) => Result<PageResult<any>>
  /** 表格列配置 */
  columns: TableColumnConfig[]
  /** 值字段 */
  valueKey?: string
  /** 显示字段 */
  labelKey?: string
  /** 占位文本 */
  placeholder?: string
  /** 是否显示已选标签 */
  showTags?: boolean
  /** 最大显示标签数 */
  maxTagCount?: number
  /** 弹窗标题 */
  modalTitle?: string
  /** 弹窗尺寸 */
  modalSize?: 'small' | 'medium' | 'large' | 'xl'
  /** 表格高度 */
  tableHeight?: number | string
  /** 是否显示搜索 */
  showSearch?: boolean
  /** 搜索占位符 */
  searchPlaceholder?: string
  /** 搜索字段 */
  searchField?: string
  /** 是否显示分页 */
  showPagination?: boolean
  /** 是否可清空 */
  clearable?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /** 返回类型：'id' 只返回ID，'object' 返回完整对象 */
  returnType?: 'id' | 'object'
  /** 初始显示文本（用于编辑时回显，避免打开弹窗前只显示ID） */
  initialLabel?: string
  /** 宽度（数字自动加px，字符串直接用；不传时在inline表单中默认220px） */
  width?: number | string

  // ========== AForm* 通用属性 ==========
  /** 标签 */
  label?: string
  /** 表单字段名 */
  prop?: string
  /** 标签宽度 */
  labelWidth?: string
  /** 栅格 span */
  span?: SpanType
  /** 是否必填 */
  required?: boolean
  /** 是否显示 form-item */
  showFormItem?: boolean
}

/** 组件 Props */
const props = withDefaults(defineProps<AFormTableSelectProps>(), {
  multiple: false,
  data: () => [],
  columns: () => [],
  valueKey: 'id',
  labelKey: 'name',
  placeholder: '',
  showTags: true,
  maxTagCount: 3,
  modalTitle: '',
  modalSize: 'medium',
  tableHeight: 400,
  showSearch: true,
  searchPlaceholder: '',
  searchField: 'searchValue',
  showPagination: true,
  clearable: true,
  disabled: false,
  returnType: 'id',
  showFormItem: true
})

/** 事件定义 */
const emit = defineEmits<{
  /** 更新绑定值 */
  'update:modelValue': [value: any]
  /** 值变化事件 */
  change: [value: any, items: any[]]
  /** 清空事件 */
  clear: []
}>()

/** 国际化 */
const { t } = useI18n()

/** 响应式 span */
const { computedSpan, shouldUseCol } = useResponsiveSpan(toRef(props, 'span'))

/** 计算宽度：有传入值时使用传入值，否则不设置（由 el-input 自身决定） */
const computedWidth = computed(() => {
  if (props.width !== undefined) {
    return typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  return undefined
})

/** 计算标签（支持国际化） */
const computedLabel = computed(() => {
  if (!props.label) return ''
  return props.label.startsWith('t(') ? t(props.label.slice(2, -1)) : props.label
})

/** 占位符（支持国际化） */
const placeholder = computed(() => {
  return props.placeholder || t('placeholder.select')
})

/** 弹窗标题（支持国际化） */
const modalTitle = computed(() => {
  return props.modalTitle || t('formTableSelect.modalTitle')
})

/** 搜索占位符（支持国际化） */
const searchPlaceholder = computed(() => {
  return props.searchPlaceholder || t('formTableSelect.searchPlaceholder')
})

/** 弹窗状态 */
const dialogVisible = ref(false)
const loading = ref(false)
const tableLoading = ref(false)

/** 表格相关 */
const tableRef = ref()
const tableData = ref<any[]>([])
const total = ref(0)

/** 查询参数 */
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  searchValue: ''
})
const searchValue = ref('')

/** 选择相关 */
const selectedRadioValue = ref<any>(null)
const tempSelection = ref<any[]>([])

/** 缓存已选择的完整对象（用于显示） */
const cachedItems = ref<Map<any, any>>(new Map())

/** 是否有选择 */
const hasSelection = computed(() => {
  if (props.multiple) {
    return Array.isArray(props.modelValue) && props.modelValue.length > 0
  }
  return props.modelValue !== undefined && props.modelValue !== null && props.modelValue !== ''
})

/** 显示的已选项目 */
const displayItems = computed(() => {
  if (!hasSelection.value) return []

  if (props.multiple) {
    const values = props.modelValue as any[]
    return values.map((val) => {
      const cached = cachedItems.value.get(val)
      if (cached) return cached
      // 如果是对象类型
      if (typeof val === 'object') return val
      // 返回一个临时对象
      return { [props.valueKey]: val, [props.labelKey]: val }
    })
  } else {
    const val = props.modelValue
    const cached = cachedItems.value.get(val)
    if (cached) return [cached]
    if (typeof val === 'object') return [val]

    // 使用 initialLabel 实现初始回显（编辑时避免显示ID）
    if (props.initialLabel) {
      return [{ [props.valueKey]: val, [props.labelKey]: props.initialLabel }]
    }

    // Fallback：使用 ID 值
    return [{ [props.valueKey]: val, [props.labelKey]: val }]
  }
})

/** 显示文本 */
const displayText = computed(() => {
  if (!hasSelection.value) return ''
  if (props.multiple) {
    return displayItems.value.map((item) => getItemLabel(item)).join(', ')
  }
  return getItemLabel(displayItems.value[0])
})

/** 获取项目的值 */
const getItemValue = (item: any) => {
  if (!item) return null
  return typeof item === 'object' ? item[props.valueKey] : item
}

/** 获取项目的显示文本 */
const getItemLabel = (item: any) => {
  if (!item) return ''
  return typeof item === 'object' ? item[props.labelKey] : String(item)
}

/** 获取嵌套属性值 */
const getNestedValue = (row: any, prop: string) => {
  if (!prop) return ''
  return prop.split('.').reduce((obj, key) => obj?.[key], row)
}

/** 格式化列值 */
const formatColumnValue = (row: any, column: TableColumnConfig) => {
  const value = getNestedValue(row, column.prop)
  if (value === null || value === undefined || value === '') return '-'
  if (column.formatter) return column.formatter(value, row)
  return String(value)
}

/** 点击触发区域 */
const handleTriggerClick = () => {
  if (props.disabled) return
  openDialog()
}

/** 打开弹窗 */
const openDialog = async () => {
  dialogVisible.value = true
  searchValue.value = ''
  queryParams.value.pageNum = 1

  // 初始化临时选择
  if (props.multiple) {
    const values = Array.isArray(props.modelValue) ? props.modelValue : []
    tempSelection.value = values.map((val) => {
      const cached = cachedItems.value.get(val)
      return cached || { [props.valueKey]: val }
    })
  } else {
    selectedRadioValue.value = props.modelValue
  }

  await loadData()

  // 恢复表格选择状态
  nextTick(() => {
    if (props.multiple && tableRef.value) {
      tempSelection.value.forEach((item) => {
        const row = tableData.value.find((r) => getItemValue(r) === getItemValue(item))
        if (row) {
          tableRef.value.toggleRowSelection(row, true)
        }
      })
    }
  })
}

/** 加载数据 */
const loadData = async () => {
  // 静态数据模式
  if (props.data && props.data.length > 0 && !props.api) {
    let filteredData = [...props.data]

    // 搜索过滤
    if (searchValue.value) {
      const keyword = searchValue.value.toLowerCase()
      filteredData = filteredData.filter((item) => {
        return props.columns.some((col) => {
          const val = getNestedValue(item, col.prop)
          return val && String(val).toLowerCase().includes(keyword)
        })
      })
    }

    // 分页
    if (props.showPagination) {
      total.value = filteredData.length
      const start = (queryParams.value.pageNum - 1) * queryParams.value.pageSize
      tableData.value = filteredData.slice(start, start + queryParams.value.pageSize)
    } else {
      tableData.value = filteredData
      total.value = filteredData.length
    }
    return
  }

  // API 模式
  if (props.api) {
    tableLoading.value = true
    try {
      const params: any = {
        pageNum: queryParams.value.pageNum,
        pageSize: queryParams.value.pageSize
      }
      if (searchValue.value && props.searchField) {
        params[props.searchField] = searchValue.value
      }

      const [err, data] = await props.api(params)
      if (!err && data) {
        tableData.value = data.records || []
        total.value = data.total || 0

        // 缓存数据
        tableData.value.forEach((item) => {
          cachedItems.value.set(getItemValue(item), item)
        })
      }
    } finally {
      tableLoading.value = false
    }
  }
}

/** 搜索防抖 */
const handleSearch = debounce(() => {
  queryParams.value.pageNum = 1
  loadData()
}, 300)

/** 单选变化 */
const handleRadioChange = (row: any) => {
  selectedRadioValue.value = getItemValue(row)
  // 缓存选中项
  cachedItems.value.set(getItemValue(row), row)
}

/** 行点击（单选模式） */
const handleRowClick = (row: any) => {
  if (!props.multiple) {
    handleRadioChange(row)
  }
}

/** 多选变化 */
const handleSelectionChange = (selection: any[]) => {
  if (props.multiple) {
    // 合并当前页选择和其他页已选
    const currentPageValues = new Set(tableData.value.map((item) => getItemValue(item)))
    const otherPageSelection = tempSelection.value.filter((item) => !currentPageValues.has(getItemValue(item)))

    tempSelection.value = [...otherPageSelection, ...selection]

    // 缓存选中项
    selection.forEach((item) => {
      cachedItems.value.set(getItemValue(item), item)
    })
  }
}

/** 确认选择 */
const handleConfirm = () => {
  let value: any
  let items: any[]

  if (props.multiple) {
    items = [...tempSelection.value]
    if (props.returnType === 'object') {
      value = items
    } else {
      value = items.map((item) => getItemValue(item))
    }
  } else {
    const selectedItem = tableData.value.find((item) => getItemValue(item) === selectedRadioValue.value)
    items = selectedItem ? [selectedItem] : []

    if (selectedItem) {
      cachedItems.value.set(getItemValue(selectedItem), selectedItem)
    }

    if (props.returnType === 'object') {
      value = selectedItem || null
    } else {
      value = selectedRadioValue.value
    }
  }

  emit('update:modelValue', value)
  emit('change', value, items)
  dialogVisible.value = false
}

/** 取消选择 */
const handleCancel = () => {
  dialogVisible.value = false
}

/** 清空选择 */
const handleClear = () => {
  const emptyValue = props.multiple ? [] : null
  emit('update:modelValue', emptyValue)
  emit('change', emptyValue, [])
  emit('clear')
}

/** 清空临时选择 */
const handleClearTemp = () => {
  tempSelection.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

/** 移除单个标签 */
const handleRemoveItem = (item: any) => {
  if (props.disabled) return

  if (props.multiple) {
    const values = (props.modelValue as any[]).filter((val) => {
      const itemVal = typeof val === 'object' ? val[props.valueKey] : val
      return itemVal !== getItemValue(item)
    })
    emit('update:modelValue', values)
    emit(
      'change',
      values,
      displayItems.value.filter((i) => getItemValue(i) !== getItemValue(item))
    )
  }
}

/** 暴露方法 */
defineExpose({
  /** 打开选择弹窗 */
  open: openDialog,
  /** 清空选择 */
  clear: handleClear,
  /** 刷新数据 */
  refresh: loadData
})
</script>

<style scoped lang="scss">
/* el-input 触发器：readonly 模式下显示为 pointer */
:deep(.el-input) {
  cursor: pointer;

  .el-input__wrapper {
    cursor: pointer;
  }

  .el-input__inner {
    cursor: pointer;
  }
}

/* 多选标签触发器 */
.a-form-table-select-tags {
  display: flex;
  flex-wrap: nowrap;
  gap: 4px;
  align-items: center;
  width: 100%;
  height: 32px;
  padding: 0 11px;
  background-color: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base);
  cursor: pointer;
  transition: border-color 0.2s;
  box-sizing: border-box;

  &:hover {
    border-color: var(--el-border-color-hover);
  }

  .selection-tag {
    max-width: 100px;

    :deep(.el-tag__content) {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .grid-icon-inline {
    margin-left: auto;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    flex-shrink: 0;
  }
}

.search-area {
  :deep(.el-input) {
    width: 300px;
  }
}

.selection-tip {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>
