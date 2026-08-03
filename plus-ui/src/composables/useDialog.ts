/**
 * 对话框管理钩子函数 (useDialog)
 *
 * 提供对话框状态管理和操作的组合式函数。
 *
 * 包含以下功能：
 * - 状态管理:
 *   - 可见性控制 (visible)
 *   - 标题管理 (title)
 * - 操作方法:
 *   - 打开对话框 (openDialog)
 *   - 关闭对话框 (closeDialog)
 */

/**
 * 对话框配置选项
 */
interface Options {
  /**
   * 对话框标题
   */
  title?: string
}

/**
 * 对话框钩子返回值
 */
interface Return {
  /**
   * 对话框标题
   */
  title: Ref<string>
  /**
   * 对话框可见状态
   */
  visible: Ref<boolean>
  /**
   * 打开对话框方法
   */
  openDialog: () => void
  /**
   * 关闭对话框方法
   */
  closeDialog: () => void
}

/**
 * 对话框管理钩子
 * @description 提供对话框的显示、隐藏和标题管理功能
 * @param ops - 对话框配置选项
 * @returns 包含对话框相关操作的方法和状态
 */
export const useDialog = (ops?: Options): Return => {
  /**
   * 对话框可见状态
   */
  const visible = ref(false)

  /**
   * 对话框标题
   */
  const title = ref(ops?.title || '')

  /**
   * 打开对话框
   * @description 设置对话框为可见状态
   */
  const openDialog = (): void => {
    visible.value = true
  }

  /**
   * 关闭对话框
   * @description 设置对话框为隐藏状态
   */
  const closeDialog = (): void => {
    visible.value = false
  }

  return {
    title,
    visible,
    openDialog,
    closeDialog
  }
}
