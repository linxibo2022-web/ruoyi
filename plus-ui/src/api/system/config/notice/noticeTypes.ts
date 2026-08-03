/** 通知公告查询类型 */
export interface SysNoticeQuery extends PageQuery {
  /** 公告标题 */
  noticeTitle?: string

  /** 创建人 */
  createByName?: string

  /** 公告状态 */
  status?: string

  /** 公告类型(1通知 2公告) */
  noticeType?: string
}

/** 通知公告表单类型 */
export interface SysNoticeBo {
  /** 公告ID */
  noticeId?: string | number

  /** 公告类型(1通知 2公告) */
  noticeType?: string

  /** 推送配置JSON */
  targetConfig?: string

  /** 目标用户ID列表 */
  targetUserIds?: string | number

  /** 已读用户ID列表 */
  readUserIds?: string | number

  /** 公告标题 */
  noticeTitle?: string

  /** 公告内容 */
  noticeContent: string

  /** 公告状态 */
  status: string

  /** 备注 */
  remark?: string

  /** 创建人 */
  createByName?: string

  // ========== 推送相关扩展字段 ==========

  /** 推送类型：all-全员 dept-部门 role-角色 user-指定用户 */
  pushType?: string

  /** 选中的部门ID列表 */
  deptIds?: (string | number)[]

  /** 选中的角色ID列表 */
  roleIds?: (string | number)[]

  /** 选中的用户ID列表 */
  userIds?: (string | number)[]
}

/** 通知公告视图类型 */
export interface SysNoticeVo {
  /** 公告ID */
  noticeId: string | number

  /** 公告类型(1通知 2公告) */
  noticeType: string

  /** 推送配置JSON */
  targetConfig: string

  /** 目标用户ID列表 */
  targetUserIds: string

  /** 已读用户ID列表 */
  readUserIds: string

  /** 公告标题 */
  noticeTitle: string

  /** 公告内容 */
  noticeContent: string

  /** 公告状态 */
  status: string

  /** 创建时间 */
  createTime: string

  /** 备注 */
  remark: string

  /** 创建人 */
  createByName?: string

  // ========== 前端扩展字段 ==========

  /** 当前用户是否已读（前端使用） */
  isRead?: boolean

  /** 推送范围显示文本（前端使用） */
  targetText?: string

  /** 已读人数（前端使用） */
  readCount?: number

  /** 目标人数（前端使用） */
  targetCount?: number
}

/** 用户公告 */
export interface UserNoticeVo {
  /** 公告id */
  noticeId: number
  /** 公告标题 */
  noticeTitle: string
  /** 公告类型 */
  noticeType: string
  /** 公告内容 */
  noticeContent: string
  /** 公告状态 */
  status: string
  /** 创建时间 */
  createTime: string
  /** 创建人 */
  createByName?: string
  /** 是否已读 */
  isRead: boolean
}
