// 部门管理 API
import type { SysDeptQuery, SysDeptBo, SysDeptVo, SysDeptTreeVo } from './deptTypes'

/**
 * 查询部门列表
 * @param query 查询参数
 * @returns {Result<SysDeptVo[]>} 结果
 */
export const listDepts = (query?: SysDeptQuery): Result<SysDeptVo[]> => {
  return http.get<SysDeptVo[]>('/system/dept/listDepts', query)
}

/**
 * 通过deptIds查询部门
 * @param deptIds 部门ID数组
 * @returns {Result<SysDeptVo[]>} 结果
 */
export const listNormalDeptsByIds = (deptIds: (number | string)[]): Result<SysDeptVo[]> => {
  return http.get<SysDeptVo[]>(`/system/dept/listNormalDeptsByIds?deptIds=${deptIds}`)
}

/**
 * 查询部门列表（排除节点）
 * @param deptId 部门ID
 * @returns {Result<SysDeptVo[]>} 结果
 */
export const listDeptsExcludeChild = (deptId: string | number): Result<SysDeptVo[]> => {
  return http.get<SysDeptVo[]>(`/system/dept/listDeptsExcludeChild/${deptId}`)
}

/**
 * 查询部门详细
 * @param deptId 部门ID
 * @returns {Result<SysDeptVo>} 结果
 */
export const getDept = (deptId: string | number): Result<SysDeptVo> => {
  return http.get<SysDeptVo>(`/system/dept/getDept/${deptId}`)
}

/**
 * 新增部门
 * @param data 部门信息
 * @returns {Result<string | number>} 结果
 */
export const addDept = (data: SysDeptBo): Result<string | number> => {
  return http.post<string | number>('/system/dept/addDept', data)
}

/**
 * 修改部门
 * @param data 部门信息
 * @returns {Result<void>} 结果
 */
export const updateDept = (data: SysDeptBo): Result<void> => {
  return http.put<void>('/system/dept/updateDept', data)
}

/**
 * 删除部门
 * @param deptIds 部门ID
 * @returns {Result<void>} 结果
 */
export const deleteDept = (deptIds: number | string): Result<void> => {
  return http.del<void>(`/system/dept/deleteDept/${deptIds}`)
}

/**
 * 查询部门下拉树结构
 * @returns {Result<SysDeptTreeVo[]>} 结果
 */
export const getDeptTreeOptions = (): Result<SysDeptTreeVo[]> => {
  return http.get<SysDeptTreeVo[]>('/system/dept/getDeptTreeOptions')
}
