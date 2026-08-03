// 租户管理 API
import type { SysTenantQuery, SysTenantBo, SysTenantVo } from './tenantTypes'
import { withHeaders } from '@/utils/function'

/**
 * 查询租户列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysTenantVo>>} 结果
 */
export const pageTenants = (query?: SysTenantQuery): Result<PageResult<SysTenantVo>> => {
  return http.get<PageResult<SysTenantVo>>('/system/tenant/pageTenants', query)
}

/**
 * 查询租户详细
 * @param id 租户ID
 * @returns {Result<SysTenantVo>} 结果
 */
export const getTenant = (id: string | number): Result<SysTenantVo> => {
  return http.get<SysTenantVo>(`/system/tenant/getTenant/${id}`)
}

/**
 * 新增租户
 * @param data 租户数据
 * @returns {Result<string | number>} 结果
 */
export const addTenant = (data: SysTenantBo): Result<string | number> => {
  return http.post<string | number>(
    '/system/tenant/addTenant',
    data,
    withHeaders({
      isEncrypt: true,
      repeatSubmit: false
    })
  )
}

/**
 * 修改租户
 * @param data 租户数据
 * @returns {Result<void>} 结果
 */
export const updateTenant = (data: SysTenantBo): Result<void> => {
  return http.put<void>('/system/tenant/updateTenant', data)
}

/**
 * 租户状态修改
 * @param id ID
 * @param tenantId 租户ID
 * @param status 状态
 * @returns {Result<void>} 结果
 */
export const changeTenantStatus = (id: string | number, tenantId: string | number, status: string): Result<void> => {
  const data = {
    id,
    tenantId,
    status
  }
  return http.put<void>('/system/tenant/changeTenantStatus', data)
}

/**
 * 删除租户
 * @param id 租户ID
 * @returns {Result<void>} 结果
 */
export const deleteTenants = (id: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/system/tenant/deleteTenants/${id}`)
}

/**
 * 动态切换租户
 * @param tenantId 租户ID
 * @returns {Result<void>} 结果
 */
export const setDynamicTenant = (tenantId: string | number): Result<void> => {
  return http.get<void>(`/system/tenant/setDynamicTenant/${tenantId}`)
}

/**
 * 清除动态租户
 * @returns {Result<void>} 结果
 */
export const clearDynamicTenant = (): Result<void> => {
  return http.get<void>('/system/tenant/clearDynamicTenant')
}

/**
 * 同步租户套餐
 * @param tenantId 租户ID
 * @param packageId 套餐ID
 * @returns {Result<void>} 结果
 */
export const syncTenantPackage = (tenantId: string | number, packageId: string | number): Result<void> => {
  const data = {
    tenantId,
    packageId
  }
  return http.get<void>('/system/tenant/syncTenantPackage', data)
}

/**
 * 同步租户角色
 * @returns {Result<void>} 结果
 */
export const syncTenantRoles = (): Result<void> => {
  return http.get<void>('/system/tenant/syncTenantRoles')
}

/**
 * 同步租户字典
 * @returns {Result<void>} 结果
 */
export const syncTenantDicts = (): Result<void> => {
  return http.get<void>('/system/tenant/syncTenantDicts')
}

/**
 * 同步租户配置
 * @returns {Result<void>} 结果
 */
export const syncTenantConfigs = (): Result<void> => {
  return http.get<void>('/system/tenant/syncTenantConfigs')
}
