// 通知公告 API
import type { SysNoticeQuery, SysNoticeBo, SysNoticeVo, UserNoticeVo } from './noticeTypes'

/**
 * 查询公告列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysNoticeVo>>} 结果
 */
export const pageNotices = (query?: SysNoticeQuery): Result<PageResult<SysNoticeVo>> => {
  return http.get<PageResult<SysNoticeVo>>('/system/notice/pageNotices', query)
}

/**
 * 查询公告详细
 * @param noticeId 公告ID
 * @returns {Result<SysNoticeVo>} 结果
 */
export const getNotice = (noticeId: string | number): Result<SysNoticeVo> => {
  return http.get<SysNoticeVo>(`/system/notice/getNotice/${noticeId}`)
}

/**
 * 新增公告
 * @param data 公告数据
 * @returns {Result<string | number>} 结果
 */
export const addNotice = (data: SysNoticeBo): Result<string | number> => {
  return http.post<string | number>('/system/notice/addNotice', data)
}

/**
 * 修改公告
 * @param data 公告数据
 * @returns {Result<void>} 结果
 */
export const updateNotice = (data: SysNoticeBo): Result<void> => {
  return http.put<void>('/system/notice/updateNotice', data)
}

/**
 * 删除公告
 * @param noticeIds 公告ID
 * @returns {Result<void>} 结果
 */
export const deleteNotices = (noticeIds: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/system/notice/deleteNotices/${noticeIds}`)
}

/**
 * 获取用户通知列表（分页）
 * @param query 查询参数
 * @returns {Result<PageResult<UserNoticeVo>>} 结果
 */
export const pageUserNotices = (query?: PageQuery): Result<PageResult<UserNoticeVo>> => {
  return http.get<PageResult<UserNoticeVo>>('/system/notice/pageUserNotices', query)
}

/**
 * 获取用户未读通知数量
 * @returns {Result<number>} 结果
 */
export const getNoticeUnreadCount = (): Result<number> => {
  return http.get<number>('/system/notice/getNoticeUnreadCount')
}

/**
 * 获取用户通知详情
 * @param noticeId 通知ID
 * @returns {Result<UserNoticeVo>} 结果
 */
export const getUserNoticeDetail = (noticeId: string | number): Result<UserNoticeVo> => {
  return http.get<UserNoticeVo>(`/system/notice/getUserNotice/${noticeId}`)
}

/**
 * 标记通知为已读
 * @param noticeId 通知ID
 * @returns {Result<void>} 结果
 */
export const markNoticeAsRead = (noticeId: string | number): Result<void> => {
  return http.post<void>(`/system/notice/markNoticeAsRead/${noticeId}`)
}

/**
 * 标记所有通知为已读
 * @returns {Result<void>} 结果
 */
export const markAllNoticesAsRead = (): Result<void> => {
  return http.post<void>('/system/notice/markAllNoticesAsRead')
}
