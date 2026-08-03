/**
 * 表格选择管理组合函数 (useSelection)
 *
 * 专为 Element Plus el-table 设计的选择解决方案，统一处理单选和多选模式。
 *
 * 🚀 主要特性：
 * - 统一接口：单选和多选使用相同的API，组件无需关心内部实现
 * - 跨页选择：多选模式下自动保持选中状态，切换页面不丢失选择
 * - 智能同步：自动同步内部状态与表格UI，确保显示一致性
 * - 灵活移除：支持标签移除、批量移除等多种移除方式
 * - 类型安全：完整的 TypeScript 支持，泛型确保类型安全
 * - 模式切换：支持运行时动态切换单选/多选模式
 *
 * 📋 使用场景：
 * - 用户选择器：支持单选用户或多选用户
 * - 数据批量操作：选择多个数据项进行批量处理
 * - 权限分配：选择角色或权限项
 * - 任何需要选择功能的表格场景
 *
 * 💡 使用示例：
 * ```typescript
 * // 多选模式
 * const { selectionItems, selectionIsRestoring, selectionChange, selectionSync, selectionRemove, selectionClear, selectionInit } = useSelection('userId', tableRef, userList, ref(true));
 *
 * // 单选模式
 * const { selectionItems, selectionIsRestoring, selectionChange, selectionSync, selectionRemove, selectionClear, selectionInit } = useSelection('userId', tableRef, userList, ref(false));
 *
 * // 表格事件绑定
 * <el-table @selection-change="selectionChange">
 *
 * // 单选radio事件绑定
 * <el-radio @change="selectionChange">
 *
 * // 标签移除
 * <ASelectionTags @close="selectionRemove" />
 * ```
 *
 * @param keyField 数据对象的唯一标识字段名，如 'id'、'userId' 等
 * @param tableRef Element Plus 表格实例的引用，用于操作表格选中状态
 * @param dataListRef 当前页面数据列表的引用，用于跨页选择逻辑
 * @param multiple 是否为多选模式的响应式引用，true=多选，false=单选
 * @returns 返回选择相关的响应式状态和操作方法
 */
export const useSelection = <T extends Record<string, any>>(
  keyField: keyof T,
  tableRef: Ref<any>,
  dataListRef: Ref<T[]>,
  multiple: Ref<boolean> = ref(true)
) => {
  // ========== 状态定义 ==========

  /**
   * 选中的完整对象数组
   */
  const selectionItems = ref<T[]>([]) as Ref<T[]>

  /**
   * 是否正在恢复选中状态（用于忽略恢复过程中的事件）
   */
  const selectionIsRestoring = ref(false)

  // ========== 内部工具方法 ==========

  /**
   * 确保ID类型一致（转换为字符串比较）
   */
  const idsEqual = (id1: any, id2: any): boolean => {
    return String(id1) === String(id2)
  }

  // ========== 公开方法 ==========

  /**
   * 处理表格选择变化
   * 统一处理单选和多选的选择变化事件
   *
   * @param selection 多选模式：表格选中的行数据数组；单选模式：单个用户对象或空数组
   */
  const selectionChange = (selection: T[] | T) => {
    // 如果正在恢复状态，忽略此次事件
    if (selectionIsRestoring.value) return

    if (multiple.value) {
      // 多选模式：处理跨页选择逻辑
      const selectionArray = Array.isArray(selection) ? selection : [selection]

      // 获取当前页面的数据ID列表
      const currentPageIds = dataListRef.value.map((item) => String(item[keyField]))

      // 保留其他页面的选择（不在当前页面的选中项）
      const otherPagesSelection = selectionItems.value.filter((item) => !currentPageIds.includes(String(item[keyField])))

      // 合并其他页面的选择和当前页面的新选择
      selectionItems.value = [...otherPagesSelection, ...selectionArray]
    } else {
      // 单选模式：只保留一个选中项
      if (selection && !Array.isArray(selection)) {
        selectionItems.value = [selection]
      } else if (Array.isArray(selection) && selection.length > 0) {
        selectionItems.value = [selection[0]]
      } else {
        selectionItems.value = []
      }
    }
  }

  /**
   * 同步表格选中状态
   * 根据内部选中项列表同步表格的UI选中状态
   * 用于页面切换后恢复选中状态，确保表格显示与内部状态一致
   */
  const selectionSync = async () => {
    if (!tableRef.value) return

    await nextTick()

    // 设置恢复标志，忽略同步过程中的选择变化事件
    selectionIsRestoring.value = true

    if (multiple.value) {
      // 多选模式：同步表格checkbox状态
      tableRef.value.clearSelection()
      await nextTick()

      // 遍历当前页面数据，恢复应该选中的行
      dataListRef.value.forEach((item) => {
        const isSelected = selectionItems.value.some((selectedItem) => idsEqual(selectedItem[keyField], item[keyField]))
        if (isSelected) {
          tableRef.value?.toggleRowSelection(item, true)
        }
      })
    }
    // 单选模式：不需要同步表格状态，radio会根据绑定值自动更新

    // 等待选中操作完成后，重置恢复标志
    await nextTick()
    selectionIsRestoring.value = false
  }

  /**
   * 移除选中项
   * 支持通过 key 或完整对象移除选中项
   * 用于标签关闭等场景
   *
   * @param key 要移除的项的唯一标识或完整对象
   * @param item 完整的数据对象（可选，用于 ASelectionTags 的双参数传递）
   */
  const selectionRemove = async (key: any, item?: T) => {
    // 确定目标ID（优先使用完整对象中的ID）
    const targetId = item ? item[keyField] : key

    if (!targetId && targetId !== 0) return

    // 查找并移除选中项
    const index = selectionItems.value.findIndex((selectedItem) => idsEqual(selectedItem[keyField], targetId))

    if (index > -1) {
      selectionItems.value.splice(index, 1)

      // 如果被移除的项在当前页面，更新表格选中状态
      const tableRow = dataListRef.value.find((dataItem) => idsEqual(dataItem[keyField], targetId))
      if (tableRow && tableRef.value) {
        tableRef.value.toggleRowSelection(tableRow, false)
      }

      // 触发选择变化事件，确保状态同步
      await nextTick()
      if (tableRef.value) {
        const currentSelection = tableRef.value.getSelectionRows()
        selectionChange(currentSelection)
      }
    }
  }

  /**
   * 清空所有选中项
   * 清除内部状态和表格选中状态
   */
  const selectionClear = () => {
    selectionItems.value = []
    if (tableRef.value) {
      tableRef.value.clearSelection()
    }
  }

  /**
   * 初始化选中项
   * 根据提供的数据设置初始选中状态，支持多种数据格式
   * 常用于对话框打开时预设选中项，或从外部传入默认选择
   *
   * @param data 初始数据，支持以下格式：
   *   - ID数组: [1, 2, 3] 或 ['1', '2', '3']
   *   - 对象数组: [{id: 1, name: 'A'}, {id: 2, name: 'B'}]
   *   - 异步函数: () => Promise<T[]> 返回对象数组的函数
   */
  const selectionInit = async (data: any[] | (() => Promise<T[]>)) => {
    let initialData: T[] = []

    if (typeof data === 'function') {
      // 如果是异步函数，执行获取数据
      try {
        initialData = await data()
      } catch (error) {
        console.error('selectionInit: 初始化数据获取失败', error)
        return
      }
    } else if (Array.isArray(data)) {
      if (data.length > 0) {
        if (typeof data[0] === 'object') {
          // 如果是对象数组，直接使用
          initialData = data as T[]
        } else {
          // 如果是ID数组，需要从当前数据中查找对应对象
          initialData = dataListRef.value.filter((item) => data.some((id) => idsEqual(item[keyField], id)))
        }
      }
    }

    // 根据模式设置初始选中项
    if (initialData.length > 0) {
      if (multiple.value) {
        // 多选模式：设置所有初始数据
        selectionItems.value = initialData
      } else {
        // 单选模式：只设置第一个
        selectionItems.value = [initialData[0]]
      }

      await nextTick()
      await selectionSync()
    }
  }

  // 返回公开的状态和方法
  return {
    // 状态
    selectionItems,
    selectionIsRestoring,

    // 方法
    selectionChange,
    selectionSync,
    selectionRemove,
    selectionClear,
    selectionInit
  }
}
