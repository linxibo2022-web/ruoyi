// 缓存监控 API
import type { CacheMonitorVo } from './cacheTypes'

/**
 * 查询缓存详细
 * @returns {Result<CacheMonitorVo>} 结果
 */
export const getCacheInfo = (): Result<CacheMonitorVo> => {
  return http.get<CacheMonitorVo>('/monitor/cache/getCacheInfo')
}
