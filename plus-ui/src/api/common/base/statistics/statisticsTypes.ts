/**
 * 首页统计数据类型定义
 */

/**欢迎信息*/
export interface WelcomeVo {
  /** 是否可见 */
  visible: boolean
  /** 欢迎标题 */
  title: string
  /** 欢迎副标题 */
  subtitle: string
  /** 欢迎消息 */
  message: string
  /** 快捷操作列表 */
  quickActions: QuickAction[]
}

/**快捷操作*/
export interface QuickAction {
  /** 操作名称 */
  name: string
  /** 操作图标 */
  icon: string
  /** 跳转路径 */
  path: string
  /** 操作描述 */
  description: string
}

/**核心统计数据*/
export interface CoreStatsVo {
  /** 整体是否可见 */
  visible: boolean
  /** 总用户数 */
  totalUsers: number
  /** 用户增长率 */
  userGrowthRate: number
  /** 今日活跃 */
  todayActive: number
  /** 活跃增长率 */
  activeGrowthRate: number
  /** 今日订单 */
  todayOrders: number
  /** 订单增长率 */
  orderGrowthRate: number
  /** 今日收入 */
  todayRevenue: string
  /** 收入增长率 */
  revenueGrowthRate: number
}

/**图表统计数据*/
export interface ChartStatsVo {
  /** 是否可见 */
  visible: boolean
  /** 描述文本 */
  description: string
  /** 副标题 */
  subtitle: string
  /** 图表数据 */
  chartData: number[]
  /** X轴数据 */
  xAxisData: string[]
  /** 底部统计数据 */
  stats: StatItem[]
}

/**统计项*/
export interface StatItem {
  /** 标签 */
  label: string
  /** 值 */
  value: string
}

/**首页统计响应数据*/
export interface HomeStatisticsVo {
  /** 当前用户角色标识 */
  roleKey: string
  /** 欢迎信息 */
  welcome: WelcomeVo
  /** 核心统计卡片数据(第一行4个AStatsCard) */
  coreStats: CoreStatsVo
  /** 用户增长数据(第二行第1个-折线图) */
  userGrowth: ChartStatsVo
  /** 订单转化数据(第二行第2个-柱状图) */
  orderConversion: ChartStatsVo
  /** 订单统计数据(第二行第3个-柱状图) */
  orderStats: ChartStatsVo
}
