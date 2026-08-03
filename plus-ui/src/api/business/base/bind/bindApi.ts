// 账号绑定 API
import type { BindQuery, BindBo, BindVo } from './bindTypes'

/**
 * 查询账号绑定列表
 * @param query 查询参数
 * @returns {Result<PageResult<BindVo>>} 结果
 */
export const pageBinds = (query?: BindQuery): Result<PageResult<BindVo>> => {
  return http.get<PageResult<BindVo>>('/base/bind/pageBinds', query)
}

/**
 * 查询账号绑定详细
 * @param id 账号绑定ID
 * @returns {Result<BindVo>} 结果
 */
export const getBind = (id: string | number): Result<BindVo> => {
  return http.get<BindVo>(`/base/bind/getBind/${id}`)
}

/**
 * 新增账号绑定
 * @param data 账号绑定数据
 * @returns {Result<string | number>} 结果
 */
export const addBind = (data: BindBo): Result<string | number> => {
  return http.post<string | number>('/base/bind/addBind', data)
}

/**
 * 修改账号绑定
 * @param data 账号绑定数据
 * @returns {Result<void>} 结果
 */
export const updateBind = (data: BindBo): Result<void> => {
  return http.put<void>('/base/bind/updateBind', data)
}

/**
 * 删除账号绑定
 * @param ids 账号绑定ID
 * @returns {Result<void>} 结果
 */
export const deleteBinds = (ids: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/base/bind/deleteBinds/${ids}`)
}

/**
 * 查询指定用户的账号绑定列表（管理员）
 * @param userId 用户ID
 * @returns {Result<BindVo[]>} 结果
 */
export const listBindsByUserId = (userId: string | number): Result<BindVo[]> => {
  return http.get<BindVo[]>(`/base/bind/listBindsByUserId/${userId}`)
}
