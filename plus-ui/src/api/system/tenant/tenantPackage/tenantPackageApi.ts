// 租户套餐 API
import type { SysTenantPackageQuery, SysTenantPackageBo, SysTenantPackageVo } from './tenantPackageTypes'

/**
 * 查询租户套餐列表
 * @param query 查询参数
 * @returns {Result<PageResult<SysTenantPackageVo>>} 结果
 */
export const pageTenantPackages = (query?: SysTenantPackageQuery): Result<PageResult<SysTenantPackageVo>> => {
  return http.get<PageResult<SysTenantPackageVo>>('/system/tenant/pageTenantPackages', query)
}

/**
 * 查询租户套餐下拉选列表
 * @returns {Result<SysTenantPackageVo[]>} 结果
 */
export const listTenantPackages = (): Result<SysTenantPackageVo[]> => {
  return http.get<SysTenantPackageVo[]>('/system/tenant/listTenantPackages')
}

/**
 * 查询租户套餐详细
 * @param packageId 套餐ID
 * @returns {Result<SysTenantPackageVo>} 结果
 */
export const getTenantPackage = (packageId: string | number): Result<SysTenantPackageVo> => {
  return http.get<SysTenantPackageVo>(`/system/tenant/getTenantPackage/${packageId}`)
}

/**
 * 新增租户套餐
 * @param data 套餐数据
 * @returns {Result<string | number>} 结果
 */
export const addTenantPackage = (data: SysTenantPackageBo): Result<string | number> => {
  return http.post<string | number>('/system/tenant/addTenantPackage', data)
}

/**
 * 修改租户套餐
 * @param data 套餐数据
 * @returns {Result<void>} 结果
 */
export const updateTenantPackage = (data: SysTenantPackageBo): Result<void> => {
  return http.put<void>('/system/tenant/updateTenantPackage', data)
}

/**
 * 租户套餐状态修改
 * @param packageId 套餐ID
 * @param status 状态
 * @returns {Result<void>} 结果
 */
export const changeTenantPackageStatus = (packageId: number | string, status: string): Result<void> => {
  const data = {
    packageId,
    status
  }
  return http.put<void>('/system/tenant/changeTenantPackageStatus', data)
}

/**
 * 删除租户套餐
 * @param packageId 套餐ID
 * @returns {Result<void>} 结果
 */
export const deleteTenantPackages = (packageId: string | number | Array<string | number>): Result<void> => {
  return http.del<void>(`/system/tenant/deleteTenantPackages/${packageId}`)
}
