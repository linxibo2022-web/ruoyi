/** 账号绑定查询类型 */
export interface BindQuery extends PageQuery {
  /** id */
  id?: string | number

  /** 用户id */
  userId?: string | number

  /** 平台类型 */
  platformType?: string

  /** appid */
  appid?: string | number

  /** unionid */
  unionid?: string | number

  /** openid */
  openid?: string | number

  /** 扩展数据 */
  extraData?: string

  /** 创建时间 */
  createTime?: string
}

/** 账号绑定表单类型 */
export interface BindBo {
  /** id */
  id?: string | number

  /** 用户id */
  userId?: string | number

  /** 平台类型 */
  platformType?: string

  /** appid */
  appid?: string | number

  /** unionid */
  unionid?: string | number

  /** openid */
  openid?: string | number

  /** 扩展数据 */
  extraData?: string

  /** 备注 */
  remark?: string
}

/** 账号绑定视图类型 */
export interface BindVo {
  /** id */
  id: string | number

  /** 用户id */
  userId: string | number

  /** 平台类型 */
  platformType: string

  /** appid */
  appid: string | number

  /** unionid */
  unionid: string | number

  /** openid */
  openid: string | number

  /** 扩展数据 */
  extraData: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}
