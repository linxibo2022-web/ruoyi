/** OSS目录查询类型 */
export interface SysOssDirectoryQuery extends PageQuery {
  /** 目录ID */
  directoryId?: string | number

  /** 父目录ID */
  parentId?: string | number

  /** 祖级列表 */
  ancestors?: string

  /** 目录名称 */
  directoryName?: string

  /** 目录路径 */
  directoryPath?: string

  /** 显示顺序 */
  orderNum?: number

  /** 目录状态 */
  status?: string

  /** 是否默认目录 */
  isDefault?: string

  /** 创建时间 */
  createTime?: string
}

/** OSS目录表单类型 */
export interface SysOssDirectoryBo {
  /** 目录ID */
  directoryId?: string | number

  /** 父目录ID */
  parentId?: string | number

  /** 祖级列表 */
  ancestors?: string

  /** 目录名称 */
  directoryName?: string

  /** 目录路径 */
  directoryPath?: string

  /** 显示顺序 */
  orderNum?: number

  /** 目录状态 */
  status?: string

  /** 是否默认目录 */
  isDefault?: string

  /** 备注 */
  remark?: string
}

/** OSS目录视图类型 */
export interface SysOssDirectoryVo {
  /** 目录ID */
  directoryId: string | number

  /** 父目录ID */
  parentId: string | number

  /** 祖级列表 */
  ancestors: string

  /** 目录名称 */
  directoryName: string

  /** 目录路径 */
  directoryPath: string

  /** 显示顺序 */
  orderNum: number

  /** 目录状态 */
  status: string

  /** 是否默认目录 */
  isDefault: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string

  /** 子目录 */
  children?: SysOssDirectoryVo[]
}

/**
 * 目录类型
 */
export interface SysOssDirectoryTreeVo {
  /**id*/
  id: number | string

  /**标签*/
  label: string

  /**父id*/
  parentId: number | string

  /**权重*/
  weight: number

  /**子节点*/
  children: SysOssDirectoryTreeVo[]
  directoryPath: string
}
