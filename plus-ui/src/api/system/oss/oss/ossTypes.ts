/** OSS对象存储查询类型 */
export interface SysOssQuery extends PageQuery {
  /** 所属目录ID */
  directoryId?: string | number

  /** 文件名 */
  fileName?: string

  /** 原名 */
  originalName?: string

  /** 文件后缀名 */
  fileSuffix?: string

  /** 文件大小(字节) */
  fileSize?: number

  /** 创建时间 */
  createTime?: string

  /** 服务商 */
  service?: string
}

/** OSS对象存储表单类型 */
export interface SysOssBo {
  /**文件*/
  file: undefined | string
}

/** OSS对象存储视图类型 */
export interface SysOssVo {
  /** 对象存储主键 */
  ossId: string | number

  /** 所属目录ID */
  directoryId: string | number

  /** 所属目录 */
  directoryName: string

  /** 文件名 */
  fileName: string

  /** 原名 */
  originalName: string

  /** 文件后缀名 */
  fileSuffix: string

  /** 文件大小(字节) */
  fileSize: number

  /** URL地址（预览用，私有库会自动生成预签名URL） */
  url: string

  /** 原始URL地址（存储用，不含预签名参数） */
  originalUrl: string

  /** 创建名称 */
  createByName: string

  /** 服务商 */
  service: string

  /**更新时间*/
  updateTime: string
}

/** 获取预签名URL请求类型 */
export interface PresignedUrlBo {
  /** 文件名 */
  fileName: string

  /** 文件类型 */
  fileType: string

  /** 模块名称 */
  moduleName?: string

  /** 目录id */
  directoryId?: string

  /** 文件路径 */
  directoryPath?: string
}

/** 预签名URL响应类型 */
export interface PresignedUrlVo {
  /** 预签名上传URL */
  presignedUrl: string

  /** 模块名称 */
  moduleName?: string

  /** 目录id */
  directoryId?: string

  /** 文件路径 */
  directoryPath?: string

  /** 文件键 */
  fileKey: string

  /** 文件访问URL */
  fileUrl: string

  /** 过期时间（秒） */
  expiration: number

  /** 上传提示信息 */
  uploadTip: string
}

/** 确认直传上传请求类型 */
export interface ConfirmDirectUploadBo {
  /** 模块名称，用于控制物理存储路径 */
  moduleName?: string

  /** 目录路径，用于控制逻辑分类，如：/文档/办公 */
  directoryPath?: string

  /** 文件名 */
  fileName: string

  /** 文件键 */
  fileKey: string

  /** 文件URL */
  fileUrl: string

  /** 目录ID（可选） */
  directoryId?: number

  /** 文件大小（可选） */
  fileSize?: number
}

/** 上传响应类型 */
export interface SysOssUploadVo {
  /** 文件URL（预览用，私有库会自动生成预签名URL） */
  url: string

  /** 原始URL地址（存储用，不含预签名参数） */
  originalUrl: string

  /** 文件名 */
  fileName: string

  /** OSS文件ID */
  ossId: string

  /** 更新时间 */
  updateTime: string
}
