// 文件下载
import FileSaver from 'file-saver'
import { http } from '@/composables/useHttp'
import { objectToQuery } from '@/utils/string'
import { isBlob } from '@/utils/validators'
import { type AxiosResponse } from 'axios'
import { useToken } from '@/composables/useToken'
import { getCurrentDateTime } from '@/utils/date'
import { showMsgSuccess, showMsgError, showConfirm, showLoading, hideLoading } from '@/utils/modal'
import i18n from '@/locales/i18n'
import { LanguageCode } from '@/systemConfig'

/**
 * 文件下载相关的组合式API (useDownload)
 *
 * 包含以下功能：
 * - 通用下载： 基础下载文件方法 (download)
 * - Excel导出： 支持导出全部/当前页数据 (exportExcel)
 * - OSS下载： 下载OSS存储的文件 (downloadOss)
 * - ZIP下载： 下载ZIP压缩文件 (downloadZip)
 * - 下载状态： 提供下载中状态标志 (downloading)
 * @returns 下载文件相关的方法和状态
 */
export const useDownload = () => {
  // 下载状态
  const downloading = ref(false)

  /**
   * 翻译函数 - 使用 i18n.global 避免 setup 上下文限制
   * @param key 英文翻译键
   * @param fallback 中文备用文本
   */
  const t = (key: string, fallback?: string): string => {
    // 先检查是否有对应的 i18n 键
    if (i18n.global.te(key)) {
      return i18n.global.t(key)
    }
    // 根据当前语言返回对应文本
    const currentLang = i18n.global.locale.value
    if (currentLang === LanguageCode.zh_CN && fallback) {
      return fallback
    }
    return key
  }

  /**
   * 解析错误信息
   * @param data Blob数据
   */
  const printErrMsg = async (data: Blob) => {
    try {
      const blob = new Blob([data])
      const resText = await blob.text()
      const rspObj = JSON.parse(resText)
      showMsgError(rspObj.msg)
    } catch (error) {
      showMsgError(t('Parse response failed', '解析响应失败'))
    }
  }

  /**
   * 通用下载方法
   * @param title 文件名
   * @param url 下载接口地址
   * @param params 请求参数（可选）
   * @returns Result<void>
   */
  const download = async (title: string, url: string, params?: any): Result<void> => {
    downloading.value = true
    showLoading(t('Downloading data, please wait', '正在下载数据，请稍候'))

    const [err, data] = await http.post(url, params || {}, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      transformRequest: [(p: any) => objectToQuery(p || {})],
      responseType: 'blob'
    })

    hideLoading()
    downloading.value = false

    if (err) {
      console.error(err)
      showMsgError(t('Download error, please contact admin', '下载文件出现错误，请联系管理员！'))
      return [err, null]
    }
    if (isBlob(data)) {
      const blob = new Blob([data.data])
      FileSaver.saveAs(blob, title)
      showMsgSuccess(t('File downloaded successfully', '文件下载成功'))
    } else {
      await printErrMsg(data)
      return [new Error(t('Download failed', '下载失败')), null]
    }

    return [null, null]
  }

  /**
   * 导出Excel文件
   * @param title 文件标题
   * @param url API路径
   * @param params 查询参数
   * @returns Promise<void>
   */
  const exportExcel = async <T extends Partial<PageQuery>>(title: string, url: string, params: T): Result<void> => {
    const [confirmErr] = await showConfirm({
      message: t('Please select export range for {title}', '请选择导出{title}的范围').replace('{title}', title),
      title: t('Prompt', '提示'),
      confirmButtonText: t('Export All', '导出全部'),
      cancelButtonText: t('Export Current Page', '导出本页'),
      type: 'warning',
      distinguishCancelAndClose: true
    })

    if (confirmErr) {
      // 用户点击了取消按钮或关闭，导出当前页
      if (confirmErr.message === 'cancel') {
        const pageLabel = t('Page {num}', '第{num}页').replace('{num}', String(params.pageNum))
        return await download(`${title}-${pageLabel}_${getCurrentDateTime()}.xlsx`, url, params)
      }
      // 其他错误（如点击X关闭）
      return [confirmErr, null]
    } else {
      // 用户点击了确认按钮，导出全部
      const exportParams = { ...params }
      exportParams.pageNum = 1
      exportParams.pageSize = 2147483647
      const allLabel = t('All', '全部')
      return await download(`${title}-${allLabel}_${getCurrentDateTime()}.xlsx`, url, exportParams)
    }
  }

  /**
   * 下载OSS文件
   * @param ossId OSS文件ID
   * @returns Result<R<void>>
   */
  const downloadOss = async (ossId: string | number): Result<R<void>> => {
    const url = `/resource/oss/download/${ossId}`
    downloading.value = true
    showLoading(t('Downloading data, please wait', '正在下载数据，请稍候'))

    const [err, data] = await http.get<AxiosResponse<Blob>>(
      url,
      {},
      {
        responseType: 'blob',
        headers: useToken().getAuthHeaders()
      }
    )

    hideLoading()
    downloading.value = false

    if (err) {
      showMsgError(t('Download error, please contact admin', '下载文件出现错误，请联系管理员！'))
      return [err, null]
    }

    if (isBlob(data?.data)) {
      const blob = new Blob([data.data], { type: 'application/octet-stream' })
      FileSaver.saveAs(blob, decodeURIComponent(data.headers['download-filename'] as string))
      showMsgSuccess(t('File downloaded successfully', '文件下载成功'))
    } else {
      await printErrMsg(data?.data)
      return [new Error(t('Download failed', '下载失败')), null]
    }

    return [null, null]
  }

  /**
   * 下载ZIP文件
   * @param url API路径
   * @param name 文件名
   * @returns Result<void>
   */
  const downloadZip = async (url: string, name: string): Result<void> => {
    downloading.value = true
    showLoading(t('Downloading data, please wait', '正在下载数据，请稍候'))

    const [err, data] = await http.get<AxiosResponse<Blob>>(
      url,
      {},
      {
        responseType: 'blob',
        headers: useToken().getAuthHeaders()
      }
    )

    hideLoading()
    downloading.value = false

    if (err) {
      showMsgError(t('Download error, please contact admin', '下载文件出现错误，请联系管理员！'))
      return [err, null]
    }

    if (isBlob(data?.data)) {
      const blob = new Blob([data.data], { type: 'application/zip' })
      FileSaver.saveAs(blob, name)
      showMsgSuccess(t('File downloaded successfully', '文件下载成功'))
    } else {
      await printErrMsg(data?.data)
      return [new Error(t('Download failed', '下载失败')), null]
    }

    return [null, null]
  }

  return {
    downloading,
    download,
    exportExcel,
    downloadOss,
    downloadZip
  }
}
