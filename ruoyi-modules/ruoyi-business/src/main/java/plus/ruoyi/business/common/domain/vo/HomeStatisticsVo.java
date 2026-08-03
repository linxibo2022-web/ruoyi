package plus.ruoyi.business.common.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 首页统计数据 VO
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatisticsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前用户角色标识
     */
    private String roleKey;

    /**
     * 欢迎信息
     */
    private WelcomeVo welcome;

    /**
     * 核心统计卡片数据(第一行4个AStatsCard)
     */
    private CoreStatsVo coreStats;

    /**
     * 用户增长数据(第二行第1个-折线图)
     */
    private ChartStatsVo userGrowth;

    /**
     * 订单转化数据(第二行第2个-柱状图)
     */
    private ChartStatsVo orderConversion;

    /**
     * 订单统计数据(第二行第3个-柱状图)
     */
    private ChartStatsVo orderStats;

    /**
     * 欢迎信息 VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WelcomeVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 是否可见
         */
        private Boolean visible;
        /**
         * 欢迎标题
         */
        private String title;
        /**
         * 欢迎副标题
         */
        private String subtitle;
        /**
         * 欢迎消息
         */
        private String message;
        /**
         * 快捷操作列表
         */
        private List<QuickAction> quickActions;
    }

    /**
     * 快捷操作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickAction implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 操作名称
         */
        private String name;
        /**
         * 操作图标
         */
        private String icon;
        /**
         * 跳转路径
         */
        private String path;
        /**
         * 操作描述
         */
        private String description;
    }

    /**
     * 核心统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoreStatsVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 整体是否可见
         */
        private Boolean visible;

        /**
         * 总用户数
         */
        private Long totalUsers;

        /**
         * 用户增长率
         */
        private Double userGrowthRate;

        /**
         * 今日活跃
         */
        private Long todayActive;

        /**
         * 活跃增长率
         */
        private Double activeGrowthRate;

        /**
         * 今日订单
         */
        private Long todayOrders;

        /**
         * 订单增长率
         */
        private Double orderGrowthRate;

        /**
         * 今日收入(字符串格式,如¥123.45)
         */
        private String todayRevenue;

        /**
         * 收入增长率
         */
        private Double revenueGrowthRate;
    }

    /**
     * 图表统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartStatsVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 是否可见
         */
        private Boolean visible;
        /**
         * 描述文本(如:较上周 +18.5%)
         */
        private String description;
        /**
         * 副标题
         */
        private String subtitle;
        /**
         * 图表数据
         */
        private List<Integer> chartData;
        /**
         * X轴数据
         */
        @JsonProperty("xAxisData")
        private List<String> xAxisData;
        /**
         * 底部统计数据
         */
        private List<StatItem> stats;
    }

    /**
     * 统计项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 标签
         */
        private String label;
        /**
         * 值
         */
        private String value;
    }
}
