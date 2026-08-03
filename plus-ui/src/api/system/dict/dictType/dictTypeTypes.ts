/** 字典类型查询类型 */
export interface SysDictTypeQuery extends PageQuery {
  /** 字典名称 */
  dictName?: string

  /** 字典编码 */
  dictType?: string

  /** 状态 */
  status?: string
}

/** 字典类型表单类型 */
export interface SysDictTypeBo {
  /** 字典主键 */
  dictId?: string | number

  /** 字典名称 */
  dictName?: string

  /** 字典编码 */
  dictType?: string

  /** 状态 */
  status?: string

  /** 是否系统级 (0-否, 1-是) */
  isSystem?: string

  /** 备注 */
  remark?: string
}

/** 字典类型视图类型 */
export interface SysDictTypeVo {
  /** 字典主键 */
  dictId: string | number

  /** 字典名称 */
  dictName: string

  /** 字典编码 */
  dictType: string

  /** 状态 */
  status: string

  /** 是否系统级 (0-否, 1-是) */
  isSystem: string

  /** 备注 */
  remark: string

  /** 创建时间 */
  createTime: string
}
