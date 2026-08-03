package plus.ruoyi.business.api.common;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.business.common.domain.vo.HomeStatisticsVo;
import plus.ruoyi.business.common.service.IStatisticsService;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.domain.R;

/**
 * 统计接口
 *
 * @author 抓蛙师
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/common/base/statistics")
public class StatisticsController {

    /** 统计服务 */
    private final IStatisticsService statisticsService;

    /**
     * 获取首页统计数据
     *
     * @return 首页统计视图
     */
    @GetMapping("/getHomeStatistics")
    public R<HomeStatisticsVo> getHomeStatistics() {
        return R.ok(statisticsService.getHomeStatistics());
    }
}
