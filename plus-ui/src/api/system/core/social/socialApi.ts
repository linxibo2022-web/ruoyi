// 社交账号 API
import type { SysSocialVo } from './socialTypes'

/**
 * 获取授权列表
 * @returns {Result<SysSocialVo[]>} 结果
 */
export const getSocialBindingList = (): Result<SysSocialVo[]> => {
  return http.get<SysSocialVo[]>('/system/social/getSocialBindingList')
}

/**
 * 查询指定用户的社会化关系列表（管理员）
 * @param userId 用户ID
 * @returns {Result<SysSocialVo[]>} 结果
 */
export const listSocialsByUserId = (userId: string | number): Result<SysSocialVo[]> => {
  return http.get<SysSocialVo[]>(`/system/social/listSocialsByUserId/${userId}`)
}
