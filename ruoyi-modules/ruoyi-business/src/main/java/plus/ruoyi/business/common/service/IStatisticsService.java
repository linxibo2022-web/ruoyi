package plus.ruoyi.business.common.service;

import plus.ruoyi.business.common.domain.vo.HomeStatisticsVo;

/**
 * 统计服务接口
 *
 * @author 抓蛙师
 */
public interface IStatisticsService {

    /**
     * 获取首页统计数据
     *
     * @return 首页统计数据
     */
    HomeStatisticsVo getHomeStatistics();
}
