// 菜单管理 API
import type { SysMenuQuery, SysMenuBo, SysMenuVo, SysMenuTreeOption, SysRoleMenuTree } from './menuTypes'
import type { RouteRecordRaw } from 'vue-router'

/**
 * 获取路由
 * @returns {Result<RouteRecordRaw[]>} 结果
 */
export const getRouters = (): Result<RouteRecordRaw[]> => {
  return http.get<RouteRecordRaw[]>('/system/menu/getRouters')
}

/**
 * 查询菜单列表
 * @param query 查询参数
 * @returns {Result<SysMenuVo[]>} 结果
 */
export const listMenus = (query?: SysMenuQuery): Result<SysMenuVo[]> => {
  return http.get<SysMenuVo[]>('/system/menu/listMenus', query)
}

/**
 * 查询菜单详细
 * @param menuId 菜单ID
 * @returns {Result<SysMenuVo>} 结果
 */
export const getMenu = (menuId: string | number): Result<SysMenuVo> => {
  return http.get<SysMenuVo>(`/system/menu/getMenu/${menuId}`)
}

/**
 * 查询菜单下拉树结构
 * @returns {Result<SysMenuTreeOption[]>} 结果
 */
export const getMenuTreeOptions = (): Result<SysMenuTreeOption[]> => {
  return http.get<SysMenuTreeOption[]>('/system/menu/getMenuTreeOptions')
}

/**
 * 根据角色ID查询菜单下拉树结构
 * @param roleId 角色ID
 * @returns {Result<SysRoleMenuTree>} 结果
 */
export const getRoleMenuTree = (roleId: string | number): Result<SysRoleMenuTree> => {
  return http.get<SysRoleMenuTree>(`/system/menu/getRoleMenuTree/${roleId}`)
}

/**
 * 根据租户套餐ID查询菜单下拉树结构
 * @param packageId 套餐ID
 * @returns {Result<SysRoleMenuTree>} 结果
 */
export const getTenantPackageMenuTree = (packageId: string | number): Result<SysRoleMenuTree> => {
  return http.get<SysRoleMenuTree>(`/system/menu/getTenantPackageMenuTree/${packageId}`)
}

/**
 * 新增菜单
 * @param data 菜单数据
 * @returns {Result<string | number>} 结果
 */
export const addMenu = (data: SysMenuBo): Result<string | number> => {
  return http.post<string | number>('/system/menu/addMenu', data)
}

/**
 * 修改菜单
 * @param data 菜单数据
 * @returns {Result<void>} 结果
 */
export const updateMenu = (data: SysMenuBo): Result<void> => {
  return http.put<void>('/system/menu/updateMenu', data)
}

/**
 * 删除菜单
 * @param menuId 菜单ID
 * @returns {Result<void>} 结果
 */
export const deleteMenu = (menuId: string | number): Result<void> => {
  return http.del<void>(`/system/menu/deleteMenu/${menuId}`)
}
