/**
 * 页面设计器状态管理
 */
import { ref, computed } from 'vue'
import { createUniqueString } from '@/utils/string'
import { sessionCache } from '@/utils/cache'
import type { FormSchema, FormItemSchema, FormItemType, HistoryRecord } from '../types'
import { LAYOUT_COMPONENT_TYPES, CONTAINER_TYPES } from '../types'
import { getComponentConfig, defaultFormSchema } from '../config/componentConfig'

/** 最大历史记录数 */
const MAX_HISTORY = 50

/** sessionStorage 存储键 */
const STORAGE_KEY = 'page-designer-schema'

/** 页面设计器状态管理 */
export function useFormSchema() {
  // 表单配置 - 优先从 sessionStorage 恢复
  const schema = ref<FormSchema>(sessionCache.getJSON<FormSchema>(STORAGE_KEY) || defaultFormSchema())

  // 当前选中的组件ID
  const selectedId = ref<string | null>(null)

  // 历史记录
  const history = ref<HistoryRecord[]>([])
  const historyIndex = ref(-1)

  // 剪贴板
  const clipboard = ref<FormItemSchema | null>(null)

  // 正在拖拽的组件类型
  const draggingItemType = ref<FormItemType | null>(null)

  // 当前选中的组件（支持递归查找嵌套组件）
  const selectedItem = computed(() => {
    if (!selectedId.value) return null
    return findItemByIdInItems(selectedId.value, schema.value.items)
  })

  // 辅助函数：在items中递归查找
  function findItemByIdInItems(id: string, items: FormItemSchema[]): FormItemSchema | null {
    for (const item of items) {
      if (item.id === id) return item
      if (item.children?.length) {
        const found = findItemByIdInItems(id, item.children)
        if (found) return found
      }
    }
    return null
  }

  // 是否可以撤销
  const canUndo = computed(() => historyIndex.value > 0)

  // 是否可以重做
  const canRedo = computed(() => historyIndex.value < history.value.length - 1)

  // 是否有组件
  const hasItems = computed(() => schema.value.items.length > 0)

  /**
   * 保存到历史记录
   */
  function saveHistory(action: string) {
    // 删除当前位置之后的历史记录
    if (historyIndex.value < history.value.length - 1) {
      history.value = history.value.slice(0, historyIndex.value + 1)
    }

    // 添加新记录
    history.value.push({
      timestamp: Date.now(),
      schema: JSON.parse(JSON.stringify(schema.value)),
      action
    })

    // 限制历史记录数量
    if (history.value.length > MAX_HISTORY) {
      history.value.shift()
    } else {
      historyIndex.value++
    }

    // 自动保存到 sessionStorage
    sessionCache.setJSON(STORAGE_KEY, schema.value)
  }

  /**
   * 撤销
   */
  function undo() {
    if (!canUndo.value) return
    historyIndex.value--
    const record = history.value[historyIndex.value]
    schema.value = JSON.parse(JSON.stringify(record.schema))
    // 检查选中项是否还存在（递归检查嵌套组件）
    if (selectedId.value && !findItemByIdInItems(selectedId.value, schema.value.items)) {
      selectedId.value = null
    }
  }

  /**
   * 重做
   */
  function redo() {
    if (!canRedo.value) return
    historyIndex.value++
    const record = history.value[historyIndex.value]
    schema.value = JSON.parse(JSON.stringify(record.schema))
    // 检查选中项是否还存在（递归检查嵌套组件）
    if (selectedId.value && !findItemByIdInItems(selectedId.value, schema.value.items)) {
      selectedId.value = null
    }
  }

  /**
   * 创建表单项
   */
  function createFormItem(type: FormItemType): FormItemSchema {
    const config = getComponentConfig(type)
    const id = createUniqueString()

    // 布局组件默认占整行，其他组件默认 auto
    const defaultSpan = LAYOUT_COMPONENT_TYPES.includes(type) ? 24 : 'auto'

    const item: FormItemSchema = {
      id,
      type,
      prop: `field_${id}`,
      label: config?.name || type,
      span: defaultSpan,
      required: false,
      rules: [],
      props: config?.defaultProps ? { ...config.defaultProps } : {},
      options: ['select', 'radio', 'checkbox', 'cascader'].includes(type)
        ? [
            { label: '选项1', value: '1' },
            { label: '选项2', value: '2' },
            { label: '选项3', value: '3' }
          ]
        : undefined
    }

    // 容器组件初始化 children 数组
    if (CONTAINER_TYPES.includes(type)) {
      item.children = []
    }

    return item
  }

  /**
   * 添加组件
   */
  function addItem(type: FormItemType, index?: number) {
    const item = createFormItem(type)
    if (typeof index === 'number') {
      schema.value.items.splice(index, 0, item)
    } else {
      schema.value.items.push(item)
    }
    selectedId.value = item.id
    saveHistory(`添加${getComponentConfig(type)?.name || type}`)
    return item
  }

  /**
   * 递归查找组件
   */
  function findItemById(id: string, items: FormItemSchema[] = schema.value.items): FormItemSchema | null {
    for (const item of items) {
      if (item.id === id) return item
      if (item.children?.length) {
        const found = findItemById(id, item.children)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 查找组件的父容器
   */
  function findParentContainer(id: string, items: FormItemSchema[] = schema.value.items, parent: FormItemSchema | null = null): { parent: FormItemSchema | null; items: FormItemSchema[] } | null {
    for (const item of items) {
      if (item.id === id) {
        return { parent, items }
      }
      if (item.children?.length) {
        const found = findParentContainer(id, item.children, item)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 向容器组件添加子组件
   */
  function addItemToContainer(containerId: string, type: FormItemType, index?: number) {
    const container = findItemById(containerId)
    if (!container || !CONTAINER_TYPES.includes(container.type)) {
      console.warn('目标不是容器组件')
      return null
    }

    if (!container.children) {
      container.children = []
    }

    const item = createFormItem(type)
    if (typeof index === 'number') {
      container.children.splice(index, 0, item)
    } else {
      container.children.push(item)
    }

    selectedId.value = item.id
    saveHistory(`添加${getComponentConfig(type)?.name || type}到${container.label}`)
    return item
  }

  /**
   * 删除组件（支持递归删除嵌套组件）
   */
  function removeItem(id: string) {
    const result = findParentContainer(id)
    if (!result) return

    const { items } = result
    const index = items.findIndex((item) => item.id === id)
    if (index === -1) return

    const item = items[index]
    items.splice(index, 1)

    // 如果删除的是当前选中项，选中下一个或上一个
    if (selectedId.value === id) {
      if (items.length > 0) {
        const newIndex = Math.min(index, items.length - 1)
        selectedId.value = items[newIndex].id
      } else {
        selectedId.value = null
      }
    }

    saveHistory(`删除${item.label}`)
  }

  /**
   * 递归生成新的 ID（用于复制嵌套组件时更新所有子组件的 ID）
   */
  function regenerateIds(item: FormItemSchema): FormItemSchema {
    const newItem: FormItemSchema = {
      ...JSON.parse(JSON.stringify(item)),
      id: createUniqueString(),
      prop: `field_${createUniqueString()}`
    }
    // 递归处理子组件
    if (newItem.children && newItem.children.length > 0) {
      newItem.children = newItem.children.map(child => regenerateIds(child))
    }
    return newItem
  }

  /**
   * 复制组件（支持嵌套组件，直接创建副本插入到当前组件后面）
   */
  function copyItem(id: string) {
    const result = findParentContainer(id)
    if (!result) return

    const { items } = result
    const index = items.findIndex((item) => item.id === id)
    if (index === -1) return

    const item = items[index]
    // 创建副本（递归更新所有 ID）
    const newItem = regenerateIds(item)

    // 插入到原组件后面
    items.splice(index + 1, 0, newItem)
    selectedId.value = newItem.id
    saveHistory(`复制${item.label}`)

    // 同时保存到剪贴板
    clipboard.value = JSON.parse(JSON.stringify(item))
  }

  /**
   * 粘贴组件
   */
  function pasteItem(index?: number) {
    if (!clipboard.value) return

    const newItem: FormItemSchema = {
      ...JSON.parse(JSON.stringify(clipboard.value)),
      id: createUniqueString(),
      prop: `field_${createUniqueString()}`
    }

    if (typeof index === 'number') {
      schema.value.items.splice(index, 0, newItem)
    } else {
      schema.value.items.push(newItem)
    }

    selectedId.value = newItem.id
    saveHistory(`粘贴${newItem.label}`)
    return newItem
  }

  /**
   * 移动组件
   */
  function moveItem(oldIndex: number, newIndex: number) {
    if (oldIndex === newIndex) return
    if (oldIndex < 0 || oldIndex >= schema.value.items.length) return
    if (newIndex < 0 || newIndex >= schema.value.items.length) return

    const items = [...schema.value.items]
    const [removed] = items.splice(oldIndex, 1)
    items.splice(newIndex, 0, removed)
    schema.value.items = items

    saveHistory('调整顺序')
  }

  /**
   * 更新组件属性
   */
  function updateItem(id: string, updates: Partial<FormItemSchema>, skipHistory = false) {
    const item = findItemById(id)
    if (item) {
      Object.assign(item, updates)
      if (!skipHistory) {
        saveHistory(`更新${item.label}`)
      }
    }
  }

  /**
   * 批量更新组件属性（用于 AI 优化）
   * @param updates 更新列表
   */
  function batchUpdateItems(updates: Array<{ id: string; updates: Partial<FormItemSchema> }>) {
    if (updates.length === 0) return

    let updatedCount = 0
    for (const { id, updates: itemUpdates } of updates) {
      const item = findItemById(id)
      if (item) {
        Object.assign(item, itemUpdates)
        updatedCount++
      }
    }

    if (updatedCount > 0) {
      saveHistory(`AI优化${updatedCount}个组件`)
    }
  }

  /**
   * 更新组件 props
   */
  function updateItemProps(id: string, props: Record<string, any>) {
    const item = findItemById(id)
    if (item) {
      item.props = { ...item.props, ...props }
      saveHistory(`更新${item.label}属性`)
    }
  }

  /**
   * 选中组件
   */
  function selectItem(id: string | null) {
    selectedId.value = id
  }

  /**
   * 清空所有组件
   */
  function clearItems() {
    schema.value.items = []
    selectedId.value = null
    saveHistory('清空所有')
  }

  /**
   * 更新表单配置
   */
  function updateFormConfig(config: Partial<FormSchema>) {
    Object.assign(schema.value, config)
    saveHistory('更新表单配置')
  }

  /**
   * 重置表单
   */
  function resetForm() {
    schema.value = defaultFormSchema()
    selectedId.value = null
    history.value = []
    historyIndex.value = -1
    clipboard.value = null
    // 清除 sessionStorage 中的缓存数据
    sessionCache.remove(STORAGE_KEY)
  }

  /**
   * 导入配置
   */
  function importSchema(newSchema: FormSchema) {
    schema.value = JSON.parse(JSON.stringify(newSchema))
    selectedId.value = null
    saveHistory('导入配置')
  }

  /**
   * 批量添加组件（用于 AI 生成）
   * @param items 要添加的组件列表
   * @param mode 添加模式：append-追加到现有组件，replace-替换所有组件
   */
  function batchAddItems(items: FormItemSchema[], mode: 'append' | 'replace' = 'append') {
    if (mode === 'replace') {
      schema.value.items = []
    }

    // 添加组件
    for (const item of items) {
      schema.value.items.push(item)
    }

    // 选中第一个添加的组件
    if (items.length > 0) {
      selectedId.value = items[0].id
    }

    saveHistory(`AI生成${items.length}个组件`)
  }

  /**
   * 导出配置
   */
  function exportSchema(): FormSchema {
    return JSON.parse(JSON.stringify(schema.value))
  }

  /**
   * 设置正在拖拽的组件类型
   */
  function setDraggingItemType(type: FormItemType) {
    draggingItemType.value = type
  }

  /**
   * 清除正在拖拽的组件类型
   */
  function clearDraggingItemType() {
    draggingItemType.value = null
  }

  // 初始化历史记录
  saveHistory('初始化')

  return {
    // 状态
    schema,
    selectedId,
    selectedItem,
    clipboard,
    draggingItemType,
    history,
    historyIndex,

    // 计算属性
    canUndo,
    canRedo,
    hasItems,

    // 方法
    createFormItem,
    addItem,
    addItemToContainer,
    batchAddItems,
    batchUpdateItems,
    findItemById,
    findParentContainer,
    removeItem,
    copyItem,
    pasteItem,
    moveItem,
    updateItem,
    updateItemProps,
    selectItem,
    clearItems,
    updateFormConfig,
    resetForm,
    importSchema,
    exportSchema,
    undo,
    redo,
    saveHistory,
    setDraggingItemType,
    clearDraggingItemType
  }
}

/** 导出类型 */
export type FormSchemaReturn = ReturnType<typeof useFormSchema>
