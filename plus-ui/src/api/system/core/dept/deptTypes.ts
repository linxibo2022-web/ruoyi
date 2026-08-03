/** 部门查询类型 */
export interface SysDeptQuery extends PageQuery {
  /** 部门名称 */
  deptName?: string

  /** 部门类别编码 */
  deptCategory?: string

  /** 区划代码 */
  areaCode?: string

  /** 部门状态 */
  status?: string
}

/** 部门表单类型 */
export interface SysDeptBo {
  /** 部门id */
  deptId?: string | number

  /**父名称*/
  parentName?: string

  /** 父部门id */
  parentId?: string | number

  /**子节点*/
  children?: SysDeptBo[]

  /** 部门名称 */
  deptName?: string

  /** 部门类别编码 */
  deptCategory?: string

  /** 显示顺序 */
  orderNum?: number

  /** 负责人 */
  leader?: number

  /** 联系电话 */
  phone?: string

  /** 邮箱 */
  email?: string

  /** 区划代码 */
  areaCode?: string

  /** 部门状态 */
  status?: string

  /**祖级*/
  ancestors?: string
}

/** 部门视图类型 */
export interface SysDeptVo {
  /**部门id*/
  id: number | string

  /** 父名称 */
  parentName: string

  /** 父部门id */
  parentId: number | string

  /**子节点*/
  children: SysDeptVo[]

  /** 部门id */
  deptId: string | number

  /** 部门名称 */
  deptName: string

  /** 部门类别编码 */
  deptCategory: string

  /** 显示顺序 */
  orderNum: number

  /** 负责人 */
  leader: number

  /** 联系电话 */
  phone: string

  /** 邮箱 */
  email: string

  /** 区划代码 */
  areaCode?: string

  /** 部门状态 */
  status: string

  /**祖节点*/
  ancestors: string

  /**菜单id*/
  menuId: string | number
}

/**
 * 部门树类型
 */
export interface SysDeptTreeVo {
  /**id*/
  id: number | string
  /**标签*/
  label: string
  /**父id*/
  parentId: number | string
  /**权重*/
  weight: number
  /**子节点*/
  children: SysDeptTreeVo[]
  /**是否禁用*/
  disabled: boolean
}
