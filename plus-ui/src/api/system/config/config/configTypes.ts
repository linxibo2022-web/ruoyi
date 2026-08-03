/** 参数配置查询类型 */
export interface SysConfigQuery extends PageQuery {
  /** 参数名称 */
  configName?: string

  /** 参数键名 */
  configKey?: string

  /** 系统内置 */
  configType?: string
}

/** 参数配置表单类型 */
export interface SysConfigBo {
  /** 参数主键 */
  configId?: string | number

  /** 参数名称 */
  configName: string

  /** 参数键名 */
  configKey: string

  /** 参数键值 */
  configValue: string

  /** 系统内置 */
  configType: string

  /** 备注 */
  remark: string
}

/** 参数配置视图类型 */
export interface SysConfigVo {
  /** 参数主键 */
  configId: number | string

  /** 参数名称 */
  configName: string

  /** 参数键名 */
  configKey: string

  /** 参数键值 */
  configValue: string

  /** 系统内置 */
  configType: string

  /** 备注 */
  remark: string
}
