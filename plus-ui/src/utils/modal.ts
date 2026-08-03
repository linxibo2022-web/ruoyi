// 弹窗消息
import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'
import { MessageBoxData } from 'element-plus'
import { LoadingInstance } from 'element-plus/es/components/loading/src/loading'
import { ElMessageBoxOptions } from 'element-plus/es/components/message-box/src/message-box.type'
import { NotificationParamsTyped, NotificationParams } from 'element-plus/es/components/notification/src/notification'
import type { MessageParams } from 'element-plus/es/components/message/src/message'
import { to } from './to'
import i18n from '@/locales/i18n'

/** 获取 modal 相关的翻译文本 */
const t = (key: string): string => {
  return i18n.global.t(`modal.${key}`)
}

/**
 * Element Plus 消息与弹窗工具函数集 (utils/modal.ts)
 *
 * 包含以下功能：
 * - 消息提示: 各种类型的消息提示 (showMsg, showMsgError, showMsgSuccess, showMsgWarning)
 * - 弹窗提示: 各种类型的弹窗提示 (showAlert, showAlertError, showAlertSuccess, showAlertWarning)
 * - 通知提示: 各种类型的通知提示 (showNotify, showNotifyError, showNotifySuccess, showNotifyWarning)
 * - 交互弹窗: 确认框和输入框 (showConfirm, showPrompt)
 * - 加载遮罩: 控制全局加载遮罩 (showLoading, hideLoading)
 *
 * 💡 所有异步方法都返回 Result<T> 格式：[Error | null, T | null]
 * 💡 同步方法保持原有调用方式不变
 * 💡 统一使用 show 前缀，便于智能提示和语义理解
 *
 * @author 抓蛙师
 * @since 2025-05-29
 */

let loadingInstance: LoadingInstance

/**
 * 显示一般信息消息
 *
 * @param content 消息内容或消息配置对象
 *
 * @example
 * // 显示简单的普通信息消息
 * showMsg('操作已完成');
 *
 * @example
 * // 使用配置对象定制消息
 * showMsg({
 *   message: '操作已完成',
 *   duration: 5000,
 *   showClose: true
 * });
 */
export const showMsg = (content: string | MessageParams) => {
  ElMessage.info(content)
}

/**
 * 显示错误消息
 *
 * @param content 错误消息内容或消息配置对象
 *
 * @example
 * // 显示简单的错误消息
 * showMsgError('操作失败，请重试');
 *
 * @example
 * // 使用配置对象定制错误消息
 * showMsgError({
 *   message: '操作失败，请重试',
 *   duration: 8000,
 *   showClose: true
 * });
 */
export const showMsgError = (content: string | MessageParams) => {
  ElMessage.error(content)
}

/**
 * 显示成功消息
 *
 * @param content 成功消息内容或消息配置对象
 *
 * @example
 * // 显示简单的成功消息
 * showMsgSuccess('数据保存成功');
 *
 * @example
 * // 使用配置对象定制成功消息
 * showMsgSuccess({
 *   message: '数据保存成功',
 *   duration: 3000,
 *   showClose: true
 * });
 */
export const showMsgSuccess = (content: string | MessageParams) => {
  ElMessage.success(content)
}

/**
 * 显示警告消息
 *
 * @param content 警告消息内容或消息配置对象
 *
 * @example
 * // 显示简单的警告消息
 * showMsgWarning('该操作可能导致数据丢失');
 *
 * @example
 * // 使用配置对象定制警告消息
 * showMsgWarning({
 *   message: '该操作可能导致数据丢失',
 *   duration: 10000,
 *   showClose: true
 * });
 */
export const showMsgWarning = (content: string | MessageParams) => {
  ElMessage.warning(content)
}

/**
 * MessageBox警告框配置接口
 *
 * @property message - 消息内容
 * @property title - 标题，默认为"系统提示"
 * @property type - 消息类型，可选值: success/warning/info/error
 */
interface AlertOptions extends Omit<ElMessageBoxOptions, 'type'> {
  type?: 'success' | 'warning' | 'info' | 'error'
  title?: string
}

/**
 * 显示一般信息弹窗
 *
 * @param content 弹窗内容或弹窗配置对象
 * @param title 弹窗标题（可选，当content为字符串时使用）
 * @param options 弹窗选项（可选，当content为字符串时使用）
 * @returns Result<MessageBoxData> [Error | null, MessageBoxData | null]
 *
 * @example
 * // 基础用法
 * const [err, result] = await showAlert('系统将在5分钟后进行维护');
 * if (err) {
 *   console.log('用户取消了操作');
 *   return;
 * }
 * console.log('用户确认了操作');
 *
 * @example
 * // 带标题的信息弹窗
 * const [err] = await showAlert('系统将在5分钟后进行维护', '维护通知');
 * if (!err) {
 *   proceedWithMaintenance();
 * }
 *
 * @example
 * // 使用配置对象定制弹窗
 * const [err] = await showAlert({
 *   message: '系统将在5分钟后进行维护',
 *   title: '维护通知',
 *   closeOnClickModal: false
 * });
 */
export const showAlert = async (content: string | AlertOptions, title?: string, options?: ElMessageBoxOptions): Result<MessageBoxData> => {
  const defaultTitle = t('systemPrompt')
  if (typeof content === 'string') {
    return to(ElMessageBox.alert(content, title || defaultTitle, options))
  } else {
    const { message, title: customTitle = defaultTitle, ...rest } = content
    return to(ElMessageBox.alert(message as string, customTitle, rest))
  }
}

/**
 * 显示错误信息弹窗
 *
 * @param content 错误信息内容或弹窗配置对象
 * @param title 弹窗标题（可选，当content为字符串时使用）
 * @param options 弹窗选项（可选，当content为字符串时使用）
 * @returns Result<MessageBoxData> [Error | null, MessageBoxData | null]
 *
 * @example
 * // 显示简单的错误弹窗
 * const [err] = await showAlertError('系统遇到错误，请联系管理员');
 * if (!err) {
 *   contactAdmin();
 * }
 *
 * @example
 * // 使用配置对象定制错误弹窗
 * const [err] = await showAlertError({
 *   message: '系统遇到错误，请联系管理员',
 *   title: '错误提示',
 *   confirmButtonText: '我知道了'
 * });
 */
export const showAlertError = async (content: string | AlertOptions, title?: string, options?: ElMessageBoxOptions): Result<MessageBoxData> => {
  const defaultTitle = t('systemPrompt')
  if (typeof content === 'string') {
    return to(ElMessageBox.alert(content, title || defaultTitle, { ...options, type: 'error' }))
  } else {
    const { message, title: customTitle = defaultTitle, ...rest } = content
    return to(ElMessageBox.alert(message as string, customTitle, { ...rest, type: 'error' }))
  }
}

/**
 * 显示成功信息弹窗
 *
 * @param content 成功信息内容或弹窗配置对象
 * @param title 弹窗标题（可选，当content为字符串时使用）
 * @param options 弹窗选项（可选，当content为字符串时使用）
 * @returns Result<MessageBoxData> [Error | null, MessageBoxData | null]
 *
 * @example
 * // 显示简单的成功弹窗
 * const [err] = await showAlertSuccess('您的申请已通过审核');
 * if (!err) {
 *   redirectToNextStep();
 * }
 *
 * @example
 * // 使用配置对象定制成功弹窗
 * const [err] = await showAlertSuccess({
 *   message: '您的申请已通过审核',
 *   title: '审核结果',
 *   confirmButtonText: '继续操作'
 * });
 */
export const showAlertSuccess = async (content: string | AlertOptions, title?: string, options?: ElMessageBoxOptions): Result<MessageBoxData> => {
  const defaultTitle = t('systemPrompt')
  if (typeof content === 'string') {
    return to(ElMessageBox.alert(content, title || defaultTitle, { ...options, type: 'success' }))
  } else {
    const { message, title: customTitle = defaultTitle, ...rest } = content
    return to(ElMessageBox.alert(message as string, customTitle, { ...rest, type: 'success' }))
  }
}

/**
 * 显示警告信息弹窗
 *
 * @param content 警告信息内容或弹窗配置对象
 * @param title 弹窗标题（可选，当content为字符串时使用）
 * @param options 弹窗选项（可选，当content为字符串时使用）
 * @returns Result<MessageBoxData> [Error | null, MessageBoxData | null]
 *
 * @example
 * // 显示简单的警告弹窗
 * const [err] = await showAlertWarning('您的账户余额不足');
 * if (!err) {
 *   redirectToRecharge();
 * }
 *
 * @example
 * // 使用配置对象定制警告弹窗
 * const [err] = await showAlertWarning({
 *   message: '您的账户余额不足',
 *   title: '余额警告',
 *   confirmButtonText: '立即充值',
 *   cancelButtonText: '稍后处理',
 *   showCancelButton: true
 * });
 */
export const showAlertWarning = async (content: string | AlertOptions, title?: string, options?: ElMessageBoxOptions): Result<MessageBoxData> => {
  const defaultTitle = t('systemPrompt')
  if (typeof content === 'string') {
    return to(ElMessageBox.alert(content, title || defaultTitle, { ...options, type: 'warning' }))
  } else {
    const { message, title: customTitle = defaultTitle, ...rest } = content
    return to(ElMessageBox.alert(message as string, customTitle, { ...rest, type: 'warning' }))
  }
}

/**
 * 显示一般信息通知
 *
 * @param content 通知内容或通知配置对象
 * @param title 通知标题（可选，当content为字符串时使用）
 * @param options 通知选项（可选，当content为字符串时使用）
 *
 * @example
 * // 显示简单的信息通知
 * showNotify('新消息已送达');
 *
 * @example
 * // 带标题的信息通知
 * showNotify('您有一条新消息', '消息通知');
 *
 * @example
 * // 使用配置对象定制通知
 * showNotify({
 *   title: '消息通知',
 *   message: '您有一条新消息',
 *   duration: 3000,
 *   position: 'top-right'
 * });
 */
export const showNotify = (content: string | NotificationParams, title?: string, options?: NotificationParams) => {
  if (typeof content === 'string') {
    const config: NotificationParams = {
      title: title || t('notification'),
      message: content
    }
    // 如果提供了选项，则合并它们
    if (options) {
      Object.assign(config, options)
    }
    ElNotification.info(config)
  } else {
    ElNotification.info(content)
  }
}

/**
 * 显示错误信息通知
 *
 * @param content 错误信息内容或通知配置对象
 * @param title 通知标题（可选，当content为字符串时使用）
 * @param options 通知选项（可选，当content为字符串时使用）
 *
 * @example
 * // 显示简单的错误通知
 * showNotifyError('网络连接失败');
 *
 * @example
 * // 带标题的错误通知
 * showNotifyError('文件上传失败', '错误');
 *
 * @example
 * // 使用配置对象定制错误通知
 * showNotifyError({
 *   title: '错误',
 *   message: '文件上传失败',
 *   duration: 5000,
 *   position: 'bottom-right'
 * });
 */
export const showNotifyError = (content: string | NotificationParams, title?: string, options?: NotificationParams) => {
  if (typeof content === 'string') {
    const config: NotificationParams = {
      title: title || t('error'),
      message: content
    }

    // 如果提供了选项，则合并它们
    if (options) {
      Object.assign(config, options)
    }

    ElNotification.error(config)
  } else {
    ElNotification.error(content)
  }
}

/**
 * 显示成功信息通知
 *
 * @param content 成功信息内容或通知配置对象
 * @param title 通知标题（可选，当content为字符串时使用）
 * @param options 通知选项（可选，当content为字符串时使用）
 *
 * @example
 * // 显示简单的成功通知
 * showNotifySuccess('文件上传成功');
 *
 * @example
 * // 带标题的成功通知
 * showNotifySuccess('您的文件已成功上传到服务器', '上传成功');
 *
 * @example
 * // 使用配置对象定制成功通知
 * showNotifySuccess({
 *   title: '上传成功',
 *   message: '您的文件已成功上传到服务器',
 *   duration: 4000,
 *   showClose: false
 * });
 */
export const showNotifySuccess = (content: string | NotificationParamsTyped, title?: string, options?: NotificationParams) => {
  if (typeof content === 'string') {
    const config: NotificationParams = {
      title: title || t('success'),
      message: content
    }
    // 如果提供了选项，则合并它们
    if (options) {
      Object.assign(config, options)
    }
    ElNotification.success(config)
  } else {
    ElNotification.success(content)
  }
}

/**
 * 显示警告信息通知
 *
 * @param content 警告信息内容或通知配置对象
 * @param title 通知标题（可选，当content为字符串时使用）
 * @param options 通知选项（可选，当content为字符串时使用）
 *
 * @example
 * // 显示简单的警告通知
 * showNotifyWarning('服务器负载较高，操作可能变慢');
 *
 * @example
 * // 带标题的警告通知
 * showNotifyWarning('系统资源使用率过高，请减少并发操作', '系统警告');
 *
 * @example
 * // 使用配置对象定制警告通知
 * showNotifyWarning({
 *   title: '系统警告',
 *   message: '系统资源使用率过高，请减少并发操作',
 *   duration: 0, // 不自动关闭
 *   position: 'top-left'
 * });
 */
export const showNotifyWarning = (content: string | NotificationParamsTyped, title?: string, options?: NotificationParams) => {
  if (typeof content === 'string') {
    const config: NotificationParams = {
      title: title || t('warning'),
      message: content
    }
    // 如果提供了选项，则合并它们
    if (options) {
      Object.assign(config, options)
    }
    ElNotification.warning(config)
  } else {
    ElNotification.warning(content)
  }
}

/**
 * 确认框配置接口
 *
 * @property message - 确认框消息内容
 * @property title - 确认框标题，默认为"系统提示"
 * @property confirmButtonText - 确认按钮文本，默认为"确定"
 * @property cancelButtonText - 取消按钮文本，默认为"取消"
 * @property type - 确认框类型，默认为"warning"
 * @property closeOnClickModal - 是否可通过点击遮罩关闭弹窗
 * @property closeOnPressEscape - 是否可通过按下ESC键关闭弹窗
 * @property showCancelButton - 是否显示取消按钮，默认为true
 */
interface ConfirmOptions extends ElMessageBoxOptions {
  message: string
  title?: string
}

/**
 * 显示确认对话框
 *
 * @param content 确认信息内容或确认框配置对象
 * @param title 确认框标题（可选，当content为字符串时使用）
 * @param options 确认框选项（可选，当content为字符串时使用）
 * @returns Result<MessageBoxData> [Error | null, MessageBoxData | null]
 *
 * @example
 * // 基本使用方式（新的 Result 格式）
 * const [err] = await showConfirm('确定要删除这条记录吗?');
 * if (err) {
 *   console.log('用户取消了删除操作');
 *   return;
 * }
 * // 用户确认了，执行删除
 * deleteRecord();
 *
 * @example
 * // 自定义标题和按钮文本
 * const [err, result] = await showConfirm('确定要删除这条记录吗?', '删除确认', {
 *   confirmButtonText: '是的，删除',
 *   cancelButtonText: '取消操作'
 * });
 * if (!err) {
 *   deleteRecord();
 *   console.log('删除操作结果:', result);
 * }
 *
 * @example
 * // 使用配置对象定制确认框
 * const [err] = await showConfirm({
 *   message: '确定要执行此操作吗?',
 *   title: '操作确认',
 *   confirmButtonText: '确定执行',
 *   cancelButtonText: '放弃操作',
 *   type: 'warning',
 *   closeOnClickModal: false
 * });
 * if (!err) {
 *   executeOperation();
 * }
 */
export const showConfirm = async (content: string | ConfirmOptions, title?: string, options?: ElMessageBoxOptions): Result<MessageBoxData> => {
  const defaultTitle = t('systemPrompt')
  const confirmText = t('ok')
  const cancelText = t('cancel')
  if (typeof content === 'string') {
    return to(
      ElMessageBox.confirm(content, title || defaultTitle, {
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        type: 'warning',
        ...options
      })
    )
  } else {
    const { message, title: customTitle = defaultTitle, ...rest } = content
    return to(
      ElMessageBox.confirm(message, customTitle, {
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        type: 'warning',
        ...rest
      })
    )
  }
}

/**
 * 输入框配置接口
 *
 * @property message - 输入框提示消息
 * @property title - 输入框标题，默认为"系统提示"
 * @property inputPattern - 输入验证正则表达式
 * @property inputErrorMessage - 输入验证失败时的错误消息
 * @property inputPlaceholder - 输入框占位文本
 * @property inputType - 输入框类型，如text、password等
 * @property confirmButtonText - 确认按钮文本，默认为"确定"
 * @property cancelButtonText - 取消按钮文本，默认为"取消"
 */
interface PromptOptions extends ElMessageBoxOptions {
  message: string
  title?: string
  inputPattern?: RegExp
  inputErrorMessage?: string
}

/**
 * 显示输入对话框
 *
 * @param content 提示信息内容或输入框配置对象
 * @param title 输入框标题（可选，当content为字符串时使用）
 * @param options 输入框选项（可选，当content为字符串时使用）
 * @returns Result<MessageBoxData> [Error | null, MessageBoxData | null]
 *
 * @example
 * // 基本使用方式（新的 Result 格式）
 * const [err, result] = await showPrompt('请输入备注信息');
 * if (err) {
 *   console.log('用户取消了输入');
 *   return;
 * }
 * // 用户输入的内容在 result.value 中
 * saveRemark(result.value);
 *
 * @example
 * // 自定义标题和验证
 * const [err, result] = await showPrompt('请输入备注信息', '添加备注', {
 *   inputPattern: /^.{1,50}$/,
 *   inputErrorMessage: '备注长度应在1-50个字符之间'
 * });
 * if (!err && result.value) {
 *   saveRemark(result.value);
 * }
 *
 * @example
 * // 使用配置对象定制输入框
 * const [err, result] = await showPrompt({
 *   message: '请输入备注内容',
 *   title: '添加备注',
 *   inputPattern: /^.{1,50}$/,
 *   inputErrorMessage: '备注长度应在1-50个字符之间',
 *   confirmButtonText: '保存',
 *   cancelButtonText: '不添加',
 *   inputPlaceholder: '请简要描述...'
 * });
 * if (!err && result.value) {
 *   handleRemarkInput(result.value);
 * }
 */
export const showPrompt = async (content: string | PromptOptions, title?: string, options?: ElMessageBoxOptions): Result<MessageBoxData> => {
  const defaultTitle = t('systemPrompt')
  const confirmText = t('ok')
  const cancelText = t('cancel')
  if (typeof content === 'string') {
    return to(
      ElMessageBox.prompt(content, title || defaultTitle, {
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        type: 'warning',
        ...options
      })
    )
  } else {
    const { message, title: customTitle = defaultTitle, ...rest } = content
    return to(
      ElMessageBox.prompt(message, customTitle, {
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        type: 'warning',
        ...rest
      })
    )
  }
}

/**
 * 加载遮罩配置接口
 *
 * @property text - 加载中的提示文本
 * @property background - 遮罩背景颜色，默认为"rgba(0, 0, 0, 0.7)"
 * @property lock - 是否锁定屏幕滚动，默认为true
 * @property spinner - 自定义加载图标类名
 * @property customClass - 自定义CSS类名
 * @property fullscreen - 是否全屏显示，默认为true
 * @property body - 是否将遮罩添加到body元素上
 */
interface CustomLoadingOptions {
  text?: string
  background?: string
  lock?: boolean
  spinner?: string
  customClass?: string
  fullscreen?: boolean
  body?: boolean
}

/**
 * 显示全屏加载遮罩
 *
 * @param content 加载提示文本或加载遮罩配置对象
 *
 * @example
 * // 基本使用方式
 * showLoading('数据加载中...');
 * // 操作完成后记得调用 hideLoading()
 *
 * @example
 * // 使用配置对象定制加载遮罩
 * showLoading({
 *   text: '正在加载数据，请稍候...',
 *   background: 'rgba(0, 0, 0, 0.8)',
 *   spinner: 'el-icon-loading'
 * });
 */
export const showLoading = (content: string | CustomLoadingOptions) => {
  if (typeof content === 'string') {
    loadingInstance = ElLoading.service({
      lock: true,
      text: content,
      background: 'rgba(0, 0, 0, 0.7)'
    })
  } else {
    loadingInstance = ElLoading.service({
      lock: true,
      background: 'rgba(0, 0, 0, 0.7)',
      ...content
    })
  }
}

/**
 * 隐藏全屏加载遮罩
 *
 * @example
 * // 在加载完成后隐藏遮罩
 * showLoading('正在提交数据');
 *
 * const [err] = await submitData();
 * hideLoading();
 *
 * if (err) {
 *   showMsgError('提交失败');
 * } else {
 *   showMsgSuccess('提交成功');
 * }
 */
export const hideLoading = () => {
  if (loadingInstance) {
    loadingInstance.close()
  }
}
