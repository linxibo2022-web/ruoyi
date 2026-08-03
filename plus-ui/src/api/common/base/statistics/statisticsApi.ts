// 首页统计 API
import type { HomeStatisticsVo } from './statisticsTypes'

/**
 * 获取首页统计数据
 * @returns {Result<HomeStatisticsVo>} 结果
 */
export const getHomeStatistics = (): Result<HomeStatisticsVo> => {
  return http.get<HomeStatisticsVo>('/common/base/statistics/getHomeStatistics')
}
