// 岗位管理 API
import type { SysPostQuery, SysPostBo, SysPostVo } from './postTypes'

/**
 * 查询岗位列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysPostVo>>} 结果
 */
export const pagePosts = (query?: SysPostQuery): Result<PageResult<SysPostVo>> => {
  return http.get<PageResult<SysPostVo>>('/system/post/pagePosts', query)
}

/**
 * 查询岗位详细
 * @param postId 岗位ID
 * @returns {Result<SysPostVo>} 结果
 */
export const getPost = (postId: string | number): Result<SysPostVo> => {
  return http.get<SysPostVo>(`/system/post/getPost/${postId}`)
}

/**
 * 获取岗位选择框列表
 * @param deptId 部门ID
 * @param postIds 岗位ID列表
 * @returns {Result<SysPostVo[]>} 结果
 */
export const getPostOptions = (deptId?: number | string, postIds?: (number | string)[]): Result<SysPostVo[]> => {
  return http.get<SysPostVo[]>('/system/post/getPostOptions', {
    postIds: postIds,
    deptId: deptId
  })
}

/**
 * 新增岗位
 * @param data 岗位数据
 * @returns {Result<string | number>} 结果
 */
export const addPost = (data: SysPostBo): Result<string | number> => {
  return http.post<string | number>('/system/post/addPost', data)
}

/**
 * 修改岗位
 * @param data 岗位数据
 * @returns {Result<void>} 结果
 */
export const updatePost = (data: SysPostBo): Result<void> => {
  return http.put<void>('/system/post/updatePost', data)
}

/**
 * 删除岗位
 * @param postIds 岗位ID
 * @returns {Result<void>} 结果
 */
export const deletePosts = (postIds: string | number | (string | number)[]): Result<void> => {
  return http.del<void>(`/system/post/deletePosts/${postIds}`)
}
