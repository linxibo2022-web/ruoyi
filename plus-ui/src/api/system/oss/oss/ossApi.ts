// 文件管理 API
import type { SysOssQuery, SysOssVo, PresignedUrlBo, PresignedUrlVo, ConfirmDirectUploadBo, SysOssUploadVo } from './ossTypes'

/**
 * 查询OSS对象存储列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysOssVo>>} 结果
 */
export const pageOss = (query?: SysOssQuery): Result<PageResult<SysOssVo>> => {
  return http.get<PageResult<SysOssVo>>('/resource/oss/pageOss', query)
}

/**
 * 查询OSS对象基于id串
 * @param ossIds 对象存储ID数组（字符串格式，避免雪花ID精度丢失）
 * @returns {Result<SysOssVo[]>} 结果
 */
export const listOssByIds = (ossIds: Array<string>): Result<SysOssVo[]> => {
  return http.get<SysOssVo[]>(`/resource/oss/listOssByIds/${ossIds.join(',')}`)
}

/**
 * 查询OSS对象基于URL
 * @param url 对象存储URL
 * @returns {Result<SysOssVo>} 结果
 */
export const getOssByUrl = (url: string): Result<SysOssVo> => {
  return http.get<SysOssVo>('/resource/oss/getOssByUrl', { url })
}

/**
 * 保存外链到OSS
 * @param directoryId 目录ID
 * @param imageUrl 图片地址
 * @returns {Result<SysOssVo>} 结果
 */
export const saveRemoteImageToOss = (directoryId: string | number, imageUrl: string): Result<SysOssVo> => {
  let url = `/resource/oss/saveRemoteImageToOss?imageUrl=${imageUrl}`
  // 只有当 directoryId 有值时才添加到 URL 中
  if (directoryId) {
    url += `&directoryId=${directoryId}`
  }
  return http.post<SysOssVo>(url)
}

/**
 * 删除OSS对象存储
 * @param ossIds 对象存储ID
 * @returns {Result<void>} 结果
 */
export const deleteOss = (ossIds: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/resource/oss/deleteOss/${ossIds}`)
}

/**
 * 获取预签名上传URL
 * @param params 预签名请求参数
 * @returns {Result<PresignedUrlVo>} 预签名URL信息
 */
export const getPresignedUrl = (params: PresignedUrlBo): Result<PresignedUrlVo> => {
  return http.post<PresignedUrlVo>('/resource/oss/getPresignedUrl', params)
}

/**
 * 确认直传上传完成
 * @param params 确认请求参数
 * @returns {Result<SysOssUploadVo>} 上传结果
 */
export const confirmDirectUpload = (params: ConfirmDirectUploadBo): Result<SysOssUploadVo> => {
  return http.post<SysOssUploadVo>('/resource/oss/confirmDirectUpload', params)
}
