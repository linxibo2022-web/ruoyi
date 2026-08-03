/**
 * 表格高度自适应钩子 (useTableHeight)
 *
 * 基于 Vue Composition API 实现表格高度的自适应计算和响应式管理。
 *
 * 包含以下功能：
 * - 高度计算: 动态计算表格的最佳高度，确保内容完整显示 (calculateTableHeight)
 * - 响应式调整: 响应窗口大小、侧边栏状态、页签配置和表单显示状态变化 (watch)
 * - 防抖处理: 避免短时间内重复计算表格高度 (debouncedCalculateHeight)
 * - 生命周期管理: 自动添加和移除事件监听器 (onMounted, onBeforeUnmount)
 * - 搜索表单管理: 提供表单引用和显示状态控制 (queryFormRef, showSearch)
 * - 高度调整: 支持自定义高度调整值 (heightAdjustment)
 *
 * @description 使表格容器自适应填充可用空间，避免页面滚动，同时保持分页控件在底部
 * @param {number} [heightAdjustment=0] - 高度调整值，正数减少高度，负数增加高度
 * @returns 包含表格高度状态和控制方法的对象
 */
export const useTableHeight = (heightAdjustment: number = 0) => {
  // 表格高度响应式变量
  const tableHeight = ref<number>(500) // 默认高度
  const queryFormRef = ref<any>(null)

  // 从Pinia获取应用状态
  const layout = useLayout()

  /**
   * 计算表格高度
   * @description 动态计算表格的最佳高度，考虑各种页面元素和布局状态
   */
  const calculateTableHeight = async () => {
    await nextTick()

    // 获取查询表单的高度
    const formHeight = queryFormRef.value?.$el?.offsetHeight || 0

    // ===== 页面布局元素高度 =====

    // 顶部导航栏高度
    const navbarHeight = 50

    // 页签区域高度 - 根据设置决定是否计入
    const tagsHeight = layout.tagsView.value ? 34 : 0

    // 页面容器padding (.p-2 = 8px * 2)
    const pageContainerPadding = 16

    // 卡片头部高度 (工具栏区域)
    const cardHeaderHeight = 62

    // 表格容器内边距和间距
    const tablePadding = 40

    // 分页组件高度和边距
    const paginationHeight = 56

    // 其他元素和边距的补偿值
    const otherPadding = 18

    // 应用自定义高度调整
    // 正数值减少高度，负数值增加高度
    const adjustment = heightAdjustment

    // 计算表格可用高度 = 视口高度 - 所有其他元素高度 - 调整值(正数减少，负数增加)
    const availableHeight =
      window.innerHeight -
      navbarHeight -
      tagsHeight -
      pageContainerPadding -
      formHeight -
      cardHeaderHeight -
      tablePadding -
      paginationHeight -
      otherPadding -
      adjustment

    // 设置最小高度为200px，避免表格过小
    tableHeight.value = Math.max(availableHeight, 200)
  }

  // 防抖处理的高度计算
  let heightCalculationTimer: number | null = null
  const debouncedCalculateHeight = (delay: number = 100) => {
    if (heightCalculationTimer !== null) {
      clearTimeout(heightCalculationTimer)
    }
    heightCalculationTimer = window.setTimeout(() => {
      calculateTableHeight()
      heightCalculationTimer = null
    }, delay)
  }

  /**
   * 窗口大小变化处理函数
   * @description 响应窗口大小变化，使用防抖重新计算表格高度
   */
  const handleResize = () => {
    // 窗口调整使用150ms的防抖延迟
    debouncedCalculateHeight(150)
  }

  // 在组件挂载时添加事件监听
  onMounted(() => {
    window.addEventListener('resize', handleResize)
    // 延迟计算以确保DOM已完全渲染
    debouncedCalculateHeight(100)
  })

  // 在组件激活时添加事件监听
  onActivated(() => {
    window.addEventListener('resize', handleResize)
    // 延迟计算以确保DOM已完全渲染
    debouncedCalculateHeight(100)
  })

  // 在组件卸载前移除事件监听和清理计时器
  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    // 清理计时器避免内存泄漏
    if (heightCalculationTimer !== null) {
      clearTimeout(heightCalculationTimer)
    }
    // 清理 ResizeObserver
    if (formResizeObserver) {
      formResizeObserver.disconnect()
      formResizeObserver = null
    }
  })

  // 监听页签配置变化
  watch(
    () => layout.tagsView.value,
    () => {
      debouncedCalculateHeight(100)
    }
  )

  // 监听搜索表单展示状态
  const showSearch = ref(true)
  watch(showSearch, () => {
    debouncedCalculateHeight(100)
  })

  // 使用 ResizeObserver 监听搜索表单高度变化
  let formResizeObserver: ResizeObserver | null = null

  watch(
    () => queryFormRef.value?.$el,
    (formElement) => {
      // 清理旧的观察器
      if (formResizeObserver) {
        formResizeObserver.disconnect()
        formResizeObserver = null
      }

      // 如果表单元素存在，创建新的观察器
      if (formElement) {
        formResizeObserver = new ResizeObserver(() => {
          // 当表单尺寸变化时（包括展开/收起），重新计算表格高度
          debouncedCalculateHeight(100)
        })
        formResizeObserver.observe(formElement)
      }
    },
    { immediate: true }
  )

  return {
    tableHeight,
    queryFormRef,
    calculateTableHeight,
    showSearch
  }
}
