/** 字典数据查询类型 */
export interface SysDictDataQuery extends PageQuery {
  /** 字典类型 */
  dictType?: string

  /** 字典标签 */
  dictLabel?: string

  /** 状态 */
  status?: string

  /** 创建时间 */
  createTime?: string
}

/** 字典数据表单类型 */
export interface SysDictDataBo {
  /** 字典编码 */
  dictDataId?: string | number

  /** 字典类型 */
  dictType?: string

  /** 字典标签 */
  dictLabel?: string

  /** 字典键值 */
  dictValue?: string

  /** 样式属性（其他样式扩展） */
  cssClass?: string

  /** 表格回显样式 */
  listClass?: ElTagType

  /** 字典排序 */
  dictSort?: number

  /** 状态 */
  status?: string

  /** 备注 */
  remark?: string
}

/** 字典数据视图类型 */
export interface SysDictDataVo {
  /** 字典编码 */
  dictDataId: string | number

  /** 字典标签 */
  dictLabel: string

  /** 字典键值 */
  dictValue: string

  /** 样式属性（其他样式扩展） */
  cssClass: string

  /** 表格回显样式 */
  listClass: ElTagType

  /** 字典排序 */
  dictSort: number

  /** 状态 */
  status: string

  /** 备注 */
  remark: string
}
