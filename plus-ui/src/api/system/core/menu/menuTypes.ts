/** 菜单类型枚举 */
export enum MenuType {
  /** 目录 */
  M = 'M',
  /** 菜单 */
  C = 'C',
  /** 按钮 */
  F = 'F'
}

/** 菜单权限查询类型 */
export interface SysMenuQuery extends PageQuery {
  /** 菜单名称 */
  menuName?: string

  /** 启用状态 */
  status?: string
}

/** 菜单权限表单类型 */
export interface SysMenuBo {
  /**父名称*/
  parentName?: string

  /** 父菜单ID */
  parentId?: string | number

  /**子节点*/
  children?: SysMenuBo[]

  /** 菜单ID */
  menuId?: string | number

  /** 菜单名称 */
  menuName: string

  /** 显示顺序 */
  orderNum: number

  /** 路由地址 */
  path: string

  /** 组件路径 */
  component?: string

  /** 路由参数 */
  queryParam?: string

  /** 是否为外链 */
  isExternalLink?: string

  /** 是否缓存 */
  isCache?: string

  /** 菜单类型 */
  menuType?: MenuType

  /** 显示设置 */
  visible?: string

  /** 启用状态 */
  status?: string

  /** 菜单图标 */
  icon?: string

  /** 备注 */
  remark?: string

  /**参数*/
  query?: string

  /**权限标识符*/
  perms?: string
}

/** 菜单权限视图类型 */
export interface SysMenuVo {
  /** 菜单ID */
  menuId: string | number

  /**父名称*/
  parentName: string

  /** 父菜单ID */
  parentId: string | number

  /**子节点 菜单权限视图*/
  children: SysMenuVo[]

  /** 菜单名称 */
  menuName: string

  /** 显示顺序 */
  orderNum: number

  /** 路由地址 */
  path: string

  /** 组件路径 */
  component: string

  /** 路由参数 */
  queryParam: string

  /** 是否为外链 */
  isExternalLink: string

  /** 是否缓存 */
  isCache: string

  /** 菜单类型 */
  menuType: string

  /** 显示设置 */
  visible: string

  /** 启用状态 */
  status: string

  /** 菜单图标 */
  icon: string

  /** 备注 */
  remark: string
}

/** 菜单树形结构类型 */
export interface SysMenuTreeOption {
  /**id*/
  id: string | number

  /**标签*/
  label: string

  /**父id*/
  parentId: string | number

  /**权重*/
  weight: number

  /**菜单类型 (M目录 C菜单 F按钮)*/
  menuType: string

  /**子节点*/
  children?: SysMenuTreeOption[]
}

/**角色菜单树*/
export interface SysRoleMenuTree {
  /** 菜单树 */
  menus: SysMenuTreeOption[]

  /** 选中的菜单ID列表 */
  checkedKeys: string[]
}
