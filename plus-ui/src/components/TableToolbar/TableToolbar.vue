<!-- 表格工具栏 -->
<template>
  <div class="ml-auto" :style="style">
    <el-row>
      <el-tooltip v-if="showPrint" class="item" effect="dark" :content="t('tooltip.print')" placement="top">
        <el-button circle icon="Printer" @click="handlePrint" />
      </el-tooltip>
      <el-tooltip v-if="search" class="item" effect="dark" :content="showSearch ? t('tooltip.hideSearch') : t('tooltip.showSearch')" placement="top">
        <el-button circle icon="Search" @click="toggleSearch" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="t('tooltip.resetSearch')" placement="top">
        <el-button circle icon="RefreshLeft" @click="resetQuery" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="t('tooltip.refresh')" placement="top">
        <el-button circle icon="Refresh" @click="refresh" />
      </el-tooltip>
      <el-tooltip v-if="columns.length" class="item" effect="dark" :content="t('tooltip.columnSettings')" placement="top">
        <div class="ml-3">
          <ATableColumnSettings
            :columns="columns"
            @update:columns="(cols) => emits('update:columns', cols)"
          />
        </div>
      </el-tooltip>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="TableToolbar">
import type { ColumnConfig } from '@/components/ATableColumnSettings/ATableColumnSettings.vue'

const { t } = useI18n()

/**
 * 表格工具栏组件Props接口
 */
interface TableToolbarProps {
  /**
   * 是否显示搜索区域
   * @default true
   */
  showSearch?: boolean

  /**
   * 列配置（支持拖拽排序和显隐控制，支持 v-model）
   */
  columns?: ColumnConfig[]

  /**
   * 是否显示搜索按钮
   * @default true
   */
  search?: boolean

  /**
   * 按钮组间距
   * @default 10
   */
  gutter?: number

  /**
   * 是否显示打印按钮
   * @default true
   */
  showPrint?: boolean

  /**
   * 打印标题
   * @default '数据列表'
   */
  printTitle?: string

  /**
   * 表格数据（用于打印）
   */
  tableData?: any[]

  /**
   * 表格列配置（用于打印）
   */
  tableColumns?: FieldConfig[]
}

// 定义 props，使用 withDefaults 提供默认值
const props = withDefaults(defineProps<TableToolbarProps>(), {
  showSearch: true,
  columns: () => [],
  search: true,
  gutter: 10,
  showPrint: false,
  printTitle: '',
  tableData: () => [],
  tableColumns: () => []
})

// 定义事件
const emits = defineEmits<{
  (e: 'update:showSearch', value: boolean): void
  (e: 'update:columns', columns: ColumnConfig[]): void
  (e: 'resetQuery'): void
  (e: 'queryTable'): void
  (e: 'print'): void
}>()

/**
 * 计算样式，根据 gutter 设置右侧边距
 */
const style = computed(() => {
  const styles: Record<string, string> = {}
  if (props.gutter) {
    styles.marginRight = `${props.gutter / 2}px`
  }
  return styles
})

/**
 * 计算打印标题（支持国际化）
 */
const computedPrintTitle = computed(() => {
  return props.printTitle || t('table.printTitle')
})

/**
 * 切换搜索区域显示状态
 */
const toggleSearch = (): void => {
  emits('update:showSearch', !props.showSearch)
}

/**
 * 重置搜索条件
 */
const resetQuery = (): void => {
  emits('resetQuery')
}

/**
 * 刷新表格数据
 */
const refresh = (): void => {
  emits('queryTable')
}

/**
 * 处理打印功能
 */
const handlePrint = async (): Promise<void> => {
  try {
    // 如果没有传入表格数据，尝试通过事件让父组件处理
    if (!props.tableData?.length || !props.tableColumns?.length) {
      emits('print')
      return
    }

    // 使用传入的数据生成打印内容
    await printTableWithData(props.tableData, props.tableColumns, computedPrintTitle.value)
    emits('print')
  } catch (error) {
    console.error('Print failed:', error)
    ElMessage.error(t('table.printFailed'))
  }
}

/**
 * 使用数据生成打印表格
 */
const printTableWithData = async (data: any[], columns: any[], title: string): Promise<void> => {
  // 过滤掉不需要打印的列
  const printColumns = columns.filter((col) => !col.noPrint && col.prop !== 'selection' && col.label !== t('table.actionsColumn') && col.prop !== 'actions')

  // 平均分配列宽
  const columnWidth = `${100 / printColumns.length}%`

  // 生成表头
  const headerHtml = printColumns
    .map(
      (col) =>
        `<th style="border: 1px solid #ddd; padding: 6px 8px; text-align: center; font-weight: bold; background-color: #f5f7fa; width: ${columnWidth};">${col.label}</th>`
    )
    .join('')

  // 生成表体
  const bodyHtml = data
    .map((row) => {
      const cells = printColumns
        .map((col) => {
          const cellValue = row[col.prop]

          // 根据列类型处理显示值
          switch (col.type) {
            case 'image':
              return cellValue ? `<img src="${cellValue}" style="width: 30px; height: 30px; object-fit: cover;" />` : '-'
            case 'switch':
              return cellValue === '1' || cellValue === true ? t('table.switchEnabled') : t('table.switchDisabled')
            case 'boolean':
              return cellValue ? t('table.booleanYes') : t('table.booleanNo')
            case 'dict':
              if (col.dictOptions && Array.isArray(col.dictOptions)) {
                const dictItem = col.dictOptions.find((item: any) => item.value === cellValue)
                return dictItem ? dictItem.label : cellValue || '-'
              }
              return cellValue || '-'
            case 'datetime':
              return cellValue ? new Date(cellValue).toLocaleString() : '-'
            case 'date':
              return cellValue ? new Date(cellValue).toLocaleDateString() : '-'
            case 'currency':
              return cellValue ? `${t('table.currencySymbol')}${Number(cellValue).toLocaleString()}` : '-'
            case 'array':
              return Array.isArray(cellValue) ? cellValue.join(', ') : String(cellValue || '-')
            case 'html':
              // 移除HTML标签，只显示纯文本
              return cellValue ? cellValue.replace(/<[^>]*>/g, '').substring(0, 50) + (cellValue.length > 50 ? '...' : '') : '-'
            case 'password':
              // 打印时不显示密码内容
              return cellValue ? '******' : '-'
            default:
              // 如果有自定义格式化函数，使用它
              if (col.formatter) {
                return col.formatter(cellValue, row)
              }
              // 长文本截断
              const textValue = String(cellValue || '-')
              return textValue.length > 20 ? textValue.substring(0, 20) + '...' : textValue
          }
        })
        .map((value) => {
          return `<td style="border: 1px solid #ddd; padding: 6px 8px; text-align: center; word-break: break-all; vertical-align: middle;">${value}</td>`
        })
        .join('')

      return `<tr>${cells}</tr>`
    })
    .join('')

  // 生成完整HTML
  const printHtml = `
    <div style="text-align: center; margin-bottom: 20px; font-size: 18px; font-weight: bold;">
      ${title}
    </div>
    <table style="width: 100%; border-collapse: collapse; font-size: 12px;">
      <thead>
        <tr>${headerHtml}</tr>
      </thead>
      <tbody>
        ${bodyHtml}
      </tbody>
    </table>
  `

  // 打印
  const { printHtml: doPrintHtml } = usePrint()
  const styles = `
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 20px 0;
      table-layout: fixed;
    }
    th, td {
      border: 1px solid #ddd;
      padding: 6px 8px;
      word-wrap: break-word;
      vertical-align: middle;
      overflow: hidden;
    }
    th {
      background-color: #f5f7fa;
      font-weight: bold;
      text-align: center;
    }
    img {
      max-width: 30px;
      max-height: 30px;
      object-fit: cover;
      vertical-align: middle;
    }
    @page {
      margin: 1cm;
      size: A4 landscape;
    }
    @media print {
      table {
        font-size: 10px;
      }
      th, td {
        padding: 4px 6px;
      }
    }
  `
  await doPrintHtml(printHtml, styles)
}
</script>
