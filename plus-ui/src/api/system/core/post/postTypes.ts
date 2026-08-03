/** 岗位信息查询类型 */
export interface SysPostQuery extends PageQuery {
  /** 岗位ID */
  postId?: string | number

  /** 部门id */
  deptId?: string | number

  /**所属部门id*/
  belongDeptId: number | string

  /** 岗位编码 */
  postCode: string

  /** 岗位名称 */
  postName: string

  /** 岗位类别编码 */
  postCategory: string

  /** 状态 */
  status: string
}

/** 岗位信息表单类型 */
export interface SysPostBo {
  /** 岗位ID */
  postId: number | string | undefined

  /** 部门id */
  deptId: number | string | undefined

  /** 岗位编码 */
  postCode: string

  /** 岗位名称 */
  postName: string

  /** 岗位类别编码 */
  postCategory: string

  /** 显示顺序 */
  postSort: number

  /** 状态 */
  status: string

  /** 备注 */
  remark: string
}

/** 岗位信息视图类型 */
export interface SysPostVo {
  /** 岗位ID */
  postId: string | number

  /** 部门id */
  deptId: string | number

  /** 岗位编码 */
  postCode: string

  /** 岗位名称 */
  postName: string

  /** 岗位类别编码 */
  postCategory: string

  /**部门名称*/
  deptName: string
  /** 显示顺序 */
  postSort: number

  /** 状态 */
  status: string

  /** 备注 */
  remark: string
}
