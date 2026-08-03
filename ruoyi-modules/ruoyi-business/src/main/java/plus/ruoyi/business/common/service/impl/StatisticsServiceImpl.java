package plus.ruoyi.business.common.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plus.ruoyi.business.common.domain.vo.HomeStatisticsVo;
import plus.ruoyi.business.common.service.IStatisticsService;
import plus.ruoyi.business.mall.dao.IOrderDao;
import plus.ruoyi.business.mall.domain.Order;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictOrderStatus;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.dao.ISysUserDao;
import plus.ruoyi.system.core.domain.SysRole;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 统计服务实现类
 *
 * @author 抓蛙师
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final ISysUserDao userDao;
    private final IOrderDao orderDao;
    private final ISysRoleDao roleDao;

    /**
     * 获取首页统计数据
     * 根据用户角色返回不同的数据和可见性配置
     */
    @Override
    public HomeStatisticsVo getHomeStatistics() {
        // 获取当前用户的所有角色
        Set<String> userRoles = LoginHelper.getRoleKeys();

        // 配置各统计模块的可见角色范围
        Set<String> coreStatsVisibleRoles = Set.of(TenantConstants.SUPER_ADMIN_ROLE_KEY,
            TenantConstants.TENANT_ADMIN_ROLE_KEY);
        Set<String> userGrowthVisibleRoles = Set.of(TenantConstants.SUPER_ADMIN_ROLE_KEY,
            TenantConstants.TENANT_ADMIN_ROLE_KEY);
        Set<String> orderConversionVisibleRoles = Set.of(TenantConstants.SUPER_ADMIN_ROLE_KEY,
            TenantConstants.TENANT_ADMIN_ROLE_KEY);
        Set<String> orderStatsVisibleRoles = Set.of(TenantConstants.SUPER_ADMIN_ROLE_KEY,
            TenantConstants.TENANT_ADMIN_ROLE_KEY);

        // 判断各模块是否可见（用户角色与可见角色有交集即可见）
        boolean coreStatsVisible = LoginHelper.hasAnyRole(userRoles, coreStatsVisibleRoles);
        boolean userGrowthVisible = LoginHelper.hasAnyRole(userRoles, userGrowthVisibleRoles);
        boolean orderConversionVisible = LoginHelper.hasAnyRole(userRoles, orderConversionVisibleRoles);
        boolean orderStatsVisible = LoginHelper.hasAnyRole(userRoles, orderStatsVisibleRoles);

        // 获取主要角色用于欢迎信息展示
        String primaryRole = getPrimaryRole(userRoles);

        return HomeStatisticsVo.builder()
            .roleKey(primaryRole)
            .welcome(buildWelcome(userRoles, primaryRole))
            .coreStats(getCoreStats(coreStatsVisible))
            .userGrowth(getChartStats("userGrowth", userGrowthVisible))
            .orderConversion(getChartStats("orderConversion", orderConversionVisible))
            .orderStats(getChartStats("orderStats", orderStatsVisible))
            .build();
    }


    /**
     * 从用户角色集合中获取主要角色（用于展示）
     * 优先级：superadmin > admin > pc_user > app_user > 其他
     *
     * @param userRoles 用户角色集合
     * @return 主要角色标识
     */
    private String getPrimaryRole(Set<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return "guest";
        }

        // 定义角色优先级
        String[] rolePriority = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY,
            UserType.PC_USER.getUserType(), UserType.APP_USER.getUserType()};

        // 按优先级返回第一个匹配的角色
        for (String role : rolePriority) {
            if (userRoles.contains(role)) {
                return role;
            }
        }

        // 如果没有匹配到预定义角色,返回第一个角色
        return userRoles.iterator().next();
    }

    /**
     * 构建欢迎信息
     *
     * @param userRoles   用户角色集合
     * @param primaryRole 主要角色（用于展示）
     */
    private HomeStatisticsVo.WelcomeVo buildWelcome(Set<String> userRoles, String primaryRole) {
        String username = LoginHelper.getUserName();
        String title;
        String subtitle;
        String message;
        List<HomeStatisticsVo.QuickAction> quickActions = new ArrayList<>();

        // 根据主要角色构建欢迎信息
        if (userRoles.contains(TenantConstants.SUPER_ADMIN_ROLE_KEY)) {
            title = "欢迎回来, " + username;
            subtitle = "超级管理员";
            message = "今天也要元气满满哦!";
            quickActions.add(HomeStatisticsVo.QuickAction.builder()
                .name("用户管理").icon("user").path("/system/user").description("管理系统用户").build());
            quickActions.add(HomeStatisticsVo.QuickAction.builder()
                .name("订单管理").icon("ShoppingBag").path("/mallManage/order").description("查看订单详情").build());
        } else if (userRoles.contains(TenantConstants.TENANT_ADMIN_ROLE_KEY)) {
            String tenantId = LoginHelper.getTenantId();
            title = "欢迎回来, " + username;
            subtitle = "租户管理员" + (tenantId != null ? " (租户ID: " + tenantId + ")" : "");
            message = "管理好您的租户数据!";
            quickActions.add(HomeStatisticsVo.QuickAction.builder()
                .name("用户管理").icon("user").path("/system/user").description("管理租户用户").build());
            quickActions.add(HomeStatisticsVo.QuickAction.builder()
                .name("订单管理").icon("ShoppingBag").path("/mallManage/order").description("查看订单详情").build());
        } else if (userRoles.contains(UserType.PC_USER.getUserType()) || userRoles.contains(UserType.APP_USER.getUserType()) || userRoles.contains("user")) {
            title = "你好, " + username;
            subtitle = "普通用户";
            message = "欢迎使用本系统";
            quickActions.add(HomeStatisticsVo.QuickAction.builder()
                .name("个人中心").icon("user").path("/user/profile").description("管理个人信息").build());
        } else {
            title = "欢迎, " + username;
            subtitle = getRoleDisplayName(primaryRole);
            message = "您当前权限有限,如需更多功能请联系管理员";
        }

        return HomeStatisticsVo.WelcomeVo.builder()
            .visible(true)
            .title(title)
            .subtitle(subtitle)
            .message(message)
            .quickActions(quickActions)
            .build();
    }

    /**
     * 获取核心统计数据
     *
     * @param visible 是否可见,不可见则返回空数据结构
     */
    private HomeStatisticsVo.CoreStatsVo getCoreStats(boolean visible) {
        if (!visible) {
            return HomeStatisticsVo.CoreStatsVo.builder()
                .visible(false)
                .build();
        }

        // 可见时查询真实数据
        // 总用户数
        long totalUsers = userDao.count(null);
        long yesterdayTotalUsers = userDao.countByTimeRange(null, DateUtil.endOfDay(DateUtil.yesterday()));
        Double userGrowthRate = calculatePercent(totalUsers, yesterdayTotalUsers);

        // 今日活跃
        long todayActive = userDao.countActiveByLoginDateRange(
            DateUtil.beginOfDay(new Date()),
            DateUtil.endOfDay(new Date())
        );
        long yesterdayActive = userDao.countActiveByLoginDateRange(
            DateUtil.beginOfDay(DateUtil.yesterday()),
            DateUtil.endOfDay(DateUtil.yesterday())
        );
        Double activeGrowthRate = calculatePercent(todayActive, yesterdayActive);

        // 今日订单
        long todayOrders = orderDao.countByTimeRange(DateUtil.beginOfDay(new Date()), null);
        long yesterdayOrders = orderDao.countByTimeRange(DateUtil.beginOfDay(DateUtil.yesterday()), DateUtil.endOfDay(DateUtil.yesterday()));
        Double orderGrowthRate = calculatePercent(todayOrders, yesterdayOrders);

        // 今日收入(使用BigDecimal统计)
        List<String> paidStatuses = List.of(
            DictOrderStatus.PAID.getValue(),
            DictOrderStatus.DELIVERED.getValue(),
            DictOrderStatus.COMPLETED.getValue()
        );
        List<Order> todayPaidOrders = orderDao.listByStatusesAndTimeRange(
            paidStatuses,
            DateUtil.beginOfDay(new Date()),
            null
        );
        BigDecimal todayRevenue = todayPaidOrders.stream()
            .map(Order::getActualAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, NumberUtil::add);

        // 昨日收入
        List<Order> yesterdayPaidOrders = orderDao.listByStatusesAndTimeRange(
            paidStatuses,
            DateUtil.beginOfDay(DateUtil.yesterday()),
            DateUtil.endOfDay(DateUtil.yesterday())
        );
        BigDecimal yesterdayRevenue = yesterdayPaidOrders.stream()
            .map(Order::getActualAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, NumberUtil::add);

        Double revenueGrowthRate = calculatePercentByBigDecimal(todayRevenue, yesterdayRevenue);

        return HomeStatisticsVo.CoreStatsVo.builder()
            .visible(true)
            .totalUsers(totalUsers)
            .userGrowthRate(userGrowthRate)
            .todayActive(todayActive)
            .activeGrowthRate(activeGrowthRate)
            .todayOrders(todayOrders)
            .orderGrowthRate(orderGrowthRate)
            .todayRevenue(String.valueOf(todayRevenue))
            .revenueGrowthRate(revenueGrowthRate)
            .build();
    }

    /**
     * 获取图表统计数据(统一方法)
     *
     * @param chartType 图表类型: userGrowth, orderConversion, orderStats
     * @param visible   是否可见,不可见则返回空数据结构
     */
    private HomeStatisticsVo.ChartStatsVo getChartStats(String chartType, boolean visible) {
        if (!visible) {
            return HomeStatisticsVo.ChartStatsVo.builder()
                .visible(false)
                .build();
        }

        // 根据图表类型调用对应的查询方法
        return switch (chartType) {
            case "userGrowth" -> getUserGrowthChart();
            case "orderConversion" -> getOrderConversionChart();
            case "orderStats" -> getOrderStatsChart();
            default -> HomeStatisticsVo.ChartStatsVo.builder().visible(false).build();
        };
    }

    /**
     * 获取用户增长图表数据
     */
    private HomeStatisticsVo.ChartStatsVo getUserGrowthChart() {
        // 获取最近9天的用户新增数据
        List<Integer> chartData = new ArrayList<>();
        for (int i = 8; i >= 0; i--) {
            Date startTime = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -i));
            Date endTime = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -i));
            long count = userDao.countByTimeRange(startTime, endTime);
            chartData.add((int) count);
        }

        // X轴数据(最近9天的日期,格式: MM-dd)
        List<String> xAxisData = new ArrayList<>();
        for (int i = 8; i >= 0; i--) {
            Date date = DateUtil.offsetDay(new Date(), -i);
            xAxisData.add(DateUtil.format(date, "MM-dd"));
        }

        // 底部统计数据
        long todayCount = chartData.get(8);
        long yesterdayCount = chartData.get(7);
        long weekCount = userDao.countByTimeRange(DateUtil.beginOfWeek(new Date()), null);
        long monthCount = userDao.countByTimeRange(DateUtil.beginOfMonth(new Date()), null);

        List<HomeStatisticsVo.StatItem> stats = new ArrayList<>();
        stats.add(HomeStatisticsVo.StatItem.builder().label("今日新增").value(formatNumber(todayCount)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("昨日新增").value(formatNumber(yesterdayCount)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("本周新增").value(formatNumber(weekCount)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("本月新增").value(formatNumber(monthCount)).build());

        // 计算周增长率
        long lastWeekCount = userDao.countByTimeRange(
            DateUtil.offsetWeek(DateUtil.beginOfWeek(new Date()), -1),
            DateUtil.offsetWeek(DateUtil.endOfWeek(new Date()), -1)
        );
        Double growthRate = calculatePercent(weekCount, lastWeekCount);

        return HomeStatisticsVo.ChartStatsVo.builder()
            .visible(true)
            .description("较上周 " + (growthRate > 0 ? "+" : "") + String.format("%.1f", growthRate) + "%")
            .subtitle("")
            .chartData(chartData)
            .xAxisData(xAxisData)
            .stats(stats)
            .build();
    }

    /**
     * 获取管理员的订单转化图表数据(第二行第2个)
     */
    private HomeStatisticsVo.ChartStatsVo getOrderConversionChart() {
        // 统计各状态订单数量
        long pendingCount = orderDao.countByStatus(DictOrderStatus.PENDING.getValue());
        long paidCount = orderDao.countByStatus(DictOrderStatus.PAID.getValue());
        long deliveredCount = orderDao.countByStatus(DictOrderStatus.DELIVERED.getValue());
        long completedCount = orderDao.countByStatus(DictOrderStatus.COMPLETED.getValue());

        // 总订单数(排除已取消和已退款)
        long totalOrders = pendingCount + paidCount + deliveredCount + completedCount;

        // 计算转化率: (已支付+已发货+已完成) / 总订单数 * 100
        double conversionRate = 0.0;
        if (totalOrders > 0) {
            conversionRate = ((paidCount + deliveredCount + completedCount) * 100.0) / totalOrders;
        }

        // 图表数据: 显示各状态订单数量
        List<Integer> chartData = new ArrayList<>();
        chartData.add((int) pendingCount);
        chartData.add((int) paidCount);
        chartData.add((int) deliveredCount);
        chartData.add((int) completedCount);

        List<String> xAxisData = List.of("待支付", "已支付", "已发货", "已完成");

        // 底部统计数据
        List<HomeStatisticsVo.StatItem> stats = new ArrayList<>();
        stats.add(HomeStatisticsVo.StatItem.builder().label("待支付").value(formatNumber(pendingCount)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("已支付").value(formatNumber(paidCount)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("已完成").value(formatNumber(completedCount)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("转化率").value(String.format("%.1f%%", conversionRate)).build());

        // 计算昨日转化率用于对比
        long yesterdayTotal = orderDao.countByTimeRange(
            DateUtil.beginOfDay(DateUtil.yesterday()),
            DateUtil.endOfDay(DateUtil.yesterday())
        );
        List<String> paidStatuses = List.of(
            DictOrderStatus.PAID.getValue(),
            DictOrderStatus.DELIVERED.getValue(),
            DictOrderStatus.COMPLETED.getValue()
        );
        long yesterdayPaid = orderDao.countByStatusesAndTimeRange(
            paidStatuses,
            DateUtil.beginOfDay(DateUtil.yesterday()),
            DateUtil.endOfDay(DateUtil.yesterday())
        );
        double yesterdayRate = yesterdayTotal > 0 ? (yesterdayPaid * 100.0) / yesterdayTotal : 0.0;
        double rateChange = conversionRate - yesterdayRate;

        return HomeStatisticsVo.ChartStatsVo.builder()
            .visible(true)
            .description("转化率 " + (rateChange > 0 ? "+" : "") + String.format("%.1f%%", rateChange))
            .subtitle("")
            .chartData(chartData)
            .xAxisData(xAxisData)
            .stats(stats)
            .build();
    }

    /**
     * 获取管理员的订单统计图表数据(第二行第3个,使用BigDecimal)
     */
    private HomeStatisticsVo.ChartStatsVo getOrderStatsChart() {
        List<String> paidStatuses = List.of(
            DictOrderStatus.PAID.getValue(),
            DictOrderStatus.DELIVERED.getValue(),
            DictOrderStatus.COMPLETED.getValue()
        );

        // 获取最近9天的已支付订单金额数据
        List<Integer> chartData = new ArrayList<>();
        for (int i = 8; i >= 0; i--) {
            Date startTime = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -i));
            Date endTime = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -i));

            List<Order> orders = orderDao.listByStatusesAndTimeRange(paidStatuses, startTime, endTime);

            BigDecimal dayAmount = orders.stream()
                .map(Order::getActualAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, NumberUtil::add);

            // 转换为分(整数)用于图表显示
            chartData.add(NumberUtil.mul(dayAmount, BigDecimal.valueOf(100)).intValue());
        }

        // X轴数据(最近9天的日期,格式: MM-dd)
        List<String> xAxisData = new ArrayList<>();
        for (int i = 8; i >= 0; i--) {
            Date date = DateUtil.offsetDay(new Date(), -i);
            xAxisData.add(DateUtil.format(date, "MM-dd"));
        }

        // 底部统计数据(从图表数据转回元)
        BigDecimal todayRevenue = NumberUtil.div(BigDecimal.valueOf(chartData.get(8)), BigDecimal.valueOf(100), 2);
        BigDecimal yesterdayRevenue = NumberUtil.div(BigDecimal.valueOf(chartData.get(7)), BigDecimal.valueOf(100), 2);

        // 本周收入
        List<Order> weekOrders = orderDao.listByStatusesAndTimeRange(
            paidStatuses,
            DateUtil.beginOfWeek(new Date()),
            null
        );
        BigDecimal weekRevenue = weekOrders.stream()
            .map(Order::getActualAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, NumberUtil::add);

        // 本月收入
        List<Order> monthOrders = orderDao.listByStatusesAndTimeRange(
            paidStatuses,
            DateUtil.beginOfMonth(new Date()),
            null
        );
        BigDecimal monthRevenue = monthOrders.stream()
            .map(Order::getActualAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, NumberUtil::add);

        List<HomeStatisticsVo.StatItem> stats = new ArrayList<>();
        stats.add(HomeStatisticsVo.StatItem.builder().label("今日收入").value(formatMoneyByBigDecimal(todayRevenue)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("昨日收入").value(formatMoneyByBigDecimal(yesterdayRevenue)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("本周收入").value(formatMoneyByBigDecimal(weekRevenue)).build());
        stats.add(HomeStatisticsVo.StatItem.builder().label("本月收入").value(formatMoneyByBigDecimal(monthRevenue)).build());

        // 计算上周收入用于对比
        List<Order> lastWeekOrders = orderDao.listByStatusesAndTimeRange(
            paidStatuses,
            DateUtil.offsetWeek(DateUtil.beginOfWeek(new Date()), -1),
            DateUtil.offsetWeek(DateUtil.endOfWeek(new Date()), -1)
        );
        BigDecimal lastWeekRevenue = lastWeekOrders.stream()
            .map(Order::getActualAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, NumberUtil::add);

        Double growthRate = calculatePercentByBigDecimal(weekRevenue, lastWeekRevenue);

        return HomeStatisticsVo.ChartStatsVo.builder()
            .visible(true)
            .description("比上周 " + (growthRate > 0 ? "+" : "") + String.format("%.0f", growthRate) + "%")
            .subtitle("")
            .chartData(chartData)
            .xAxisData(xAxisData)
            .stats(stats)
            .build();
    }

    // ==================== 工具方法 ====================

    /**
     * 获取角色中文显示名称
     *
     * @param roleKey 角色标识
     * @return 中文显示名称
     */
    private String getRoleDisplayName(String roleKey) {
        // 超级管理员和租户管理员固定显示
        if (TenantConstants.SUPER_ADMIN_ROLE_KEY.equals(roleKey)) {
            return "超级管理员";
        }
        if (TenantConstants.TENANT_ADMIN_ROLE_KEY.equals(roleKey)) {
            return "租户管理员";
        }

        // 其他角色从数据库查询
        SysRole role = roleDao.getByRoleKey(roleKey);
        if (role != null && role.getRoleName() != null) {
            return role.getRoleName();
        }

        // 兜底方案：显示角色key
        return "角色: " + roleKey;
    }

    /**
     * 计算增长率(long类型)
     */
    private Double calculatePercent(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return NumberUtil.div(
            NumberUtil.mul(BigDecimal.valueOf(current - previous), BigDecimal.valueOf(100)),
            BigDecimal.valueOf(previous),
            1,
            RoundingMode.HALF_UP
        ).doubleValue();
    }

    /**
     * 计算增长率(BigDecimal类型)
     */
    private Double calculatePercentByBigDecimal(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return NumberUtil.div(
            NumberUtil.mul(NumberUtil.sub(current, previous), BigDecimal.valueOf(100)),
            previous,
            1,
            RoundingMode.HALF_UP
        ).doubleValue();
    }

    /**
     * 格式化数字(带k/w单位)
     */
    private String formatNumber(long num) {
        if (num >= 10000) {
            return String.format("%.1fw", num / 10000.0);
        } else if (num >= 1000) {
            return String.format("%.1fk", num / 1000.0);
        }
        return String.valueOf(num);
    }

    /**
     * 格式化金额(BigDecimal,带k/w单位)
     */
    private String formatMoneyByBigDecimal(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return String.format("¥%.1fw", NumberUtil.div(amount, BigDecimal.valueOf(10000), 1).doubleValue());
        } else if (amount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return String.format("¥%.1fk", NumberUtil.div(amount, BigDecimal.valueOf(1000), 1).doubleValue());
        } else {
            return "¥" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
    }
}
