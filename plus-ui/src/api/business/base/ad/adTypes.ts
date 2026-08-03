/** 广告配置查询类型 */
export interface AdQuery extends PageQuery {
  /** 主键id */
  id?: string | number

  /** appid */
  appid?: string | number

  /** 广告位id */
  adUnitId?: string | number

  /** 广告名称 */
  adName?: string

  /** 广告类型 */
  adType?: string

  /** 投放位置 */
  position?: string

  /** 广告图片 */
  img?: string

  /** 描述 */
  description?: string

  /** 跳转appid */
  jumpAppid?: string | number

  /** 跳转路径 */
  jumpPath?: string

  /** 样式配置 */
  styleConfig?: string

  /** 排序值 */
  sortOrder?: number

  /** 状态 */
  status?: string

  /** 创建时间 */
  createTime?: string

}

/** 广告配置表单类型 */
export interface AdBo {
  /** 主键id */
  id?: string | number

  /** appid */
  appid?: string | number

  /** 广告位id */
  adUnitId?: string | number

  /** 广告名称 */
  adName?: string

  /** 广告类型 */
  adType?: string

  /** 投放位置 */
  position?: string

  /** 广告图片 */
  img?: string

  /** 描述 */
  description?: string

  /** 跳转appid */
  jumpAppid?: string | number

  /** 跳转路径 */
  jumpPath?: string

  /** 样式配置 */
  styleConfig?: string

  /** 排序值 */
  sortOrder?: number

  /** 状态 */
  status?: string

  /** 备注 */
  remark?: string

}

/** 广告配置视图类型 */
export interface AdVo {
  /** 主键id */
  id: string | number

  /** appid */
  appid: string | number

  /** 广告位id */
  adUnitId: string | number

  /** 广告名称 */
  adName: string

  /** 广告类型 */
  adType: string

  /** 投放位置 */
  position: string

  /** 广告图片 */
  img: string

  /** 描述 */
  description: string

  /** 跳转appid */
  jumpAppid: string | number

  /** 跳转路径 */
  jumpPath: string

  /** 样式配置 */
  styleConfig: string

  /** 排序值 */
  sortOrder: number

  /** 状态 */
  status: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string

}
