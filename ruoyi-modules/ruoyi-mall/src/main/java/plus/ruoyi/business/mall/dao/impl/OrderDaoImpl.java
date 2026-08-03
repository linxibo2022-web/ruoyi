package plus.ruoyi.business.mall.dao.impl;

import org.springframework.stereotype.Repository;
import plus.ruoyi.business.mall.dao.IOrderDao;
import plus.ruoyi.business.mall.domain.Order;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.business.mall.mapper.OrderMapper;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单数据访问实现
 *
 * @author 抓蛙师
 */
@Repository
public class OrderDaoImpl extends BaseDaoImpl<OrderMapper, Order> implements IOrderDao {

    /**
     * 构建查询条件
     *
     * @param bo 查询参数
     * @return 查询条件
     */
    @Override
    public PlusLambdaQuery<Order> buildQueryWrapper(OrderBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of();

        // 精确匹配查询条件
        lqw.eq(Order::getId, bo.getId());
        lqw.eq(Order::getOrderNo, bo.getOrderNo());
        lqw.eq(Order::getUserId, bo.getUserId());
        lqw.eq(Order::getGoodsId, bo.getGoodsId());
        lqw.eq(Order::getGoodsName, bo.getGoodsName());
        lqw.eq(Order::getGoodsImg, bo.getGoodsImg());
        lqw.eq(Order::getPrice, bo.getPrice());
        lqw.eq(Order::getQuantity, bo.getQuantity());
        lqw.eq(Order::getTotalAmount, bo.getTotalAmount());
        lqw.eq(Order::getActualAmount, bo.getActualAmount());
        lqw.in(Order::getOrderStatus, StringUtils.splitToList(bo.getOrderStatus()));
        lqw.eq(Order::getPaymentMethod, bo.getPaymentMethod());
        lqw.eq(Order::getPaymentTime, bo.getPaymentTime());
        lqw.eq(Order::getTransactionId, bo.getTransactionId());
        lqw.eq(Order::getBuyerRemark, bo.getBuyerRemark());
        lqw.eq(Order::getCreateTime, bo.getCreateTime());
        lqw.between(Order::getPaymentTime, params.get("beginPaymentTime"), params.get("endPaymentTime"));
        lqw.between(Order::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));

        // 模糊查询（对非字符串字段使用 likeCast 确保跨数据库兼容）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .likeCast(Order::getId, searchValue)                // Long 类型
                .or().like(Order::getOrderNo, searchValue)          // String 类型
                .or().likeCast(Order::getUserId, searchValue)       // Long 类型
                .or().likeCast(Order::getGoodsId, searchValue)      // Long 类型
                .or().like(Order::getGoodsName, searchValue)        // String 类型
                .or().like(Order::getGoodsImg, searchValue)         // String 类型
                .or().likeCast(Order::getPrice, searchValue)        // BigDecimal 类型
                .or().likeCast(Order::getQuantity, searchValue)     // Integer 类型
                .or().likeCast(Order::getTotalAmount, searchValue)  // BigDecimal 类型
                .or().likeCast(Order::getActualAmount, searchValue) // BigDecimal 类型
                .or().like(Order::getOrderStatus, searchValue)      // String 类型
                .or().like(Order::getPaymentMethod, searchValue)    // String 类型
                .or().likeCast(Order::getPaymentTime, searchValue)  // DateTime 类型
                .or().like(Order::getTransactionId, searchValue)    // String 类型
                .or().like(Order::getBuyerRemark, searchValue)      // String 类型
                .or().like(Order::getOrderExtInfo, searchValue)     // String 类型
                .or().like(Order::getReceiverInfo, searchValue)     // String 类型
                .or().like(Order::getShippingInfo, searchValue)     // String 类型
                .or().likeCast(Order::getCreateTime, searchValue)   // DateTime 类型
            );
        }
        return lqw;
    }

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @Override
    public Order getByOrderNo(String orderNo) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .eq(Order::getOrderNo, orderNo);
        return getOne(lqw);
    }

    /**
     * 根据订单号和用户ID查询订单
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @return 订单信息
     */
    @Override
    public Order getByOrderNoAndUserId(String orderNo, Long userId) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getUserId, userId);
        return getOne(lqw);
    }

    /**
     * 根据订单状态统计订单数量
     */
    @Override
    public long countByStatus(String status) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .eq(Order::getOrderStatus, status);
        return count(lqw);
    }

    /**
     * 根据订单状态列表统计订单数量
     */
    @Override
    public long countByStatuses(List<String> statuses) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .in(Order::getOrderStatus, statuses);
        return count(lqw);
    }

    /**
     * 根据时间范围统计订单数量
     */
    @Override
    public long countByTimeRange(Date startTime, Date endTime) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .ge(startTime != null, Order::getCreateTime, startTime)
            .le(endTime != null, Order::getCreateTime, endTime);
        return count(lqw);
    }

    /**
     * 根据订单状态列表和时间范围查询订单列表
     */
    @Override
    public List<Order> listByStatusesAndTimeRange(List<String> statuses, Date startTime, Date endTime) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .in(Order::getOrderStatus, statuses)
            .ge(startTime != null, Order::getCreateTime, startTime)
            .le(endTime != null, Order::getCreateTime, endTime);
        return list(lqw);
    }

    /**
     * 根据订单状态列表和时间范围统计订单数量
     */
    @Override
    public long countByStatusesAndTimeRange(List<String> statuses, Date startTime, Date endTime) {
        PlusLambdaQuery<Order> lqw = PlusLambdaQuery.of(Order.class)
            .in(Order::getOrderStatus, statuses)
            .ge(startTime != null, Order::getCreateTime, startTime)
            .le(endTime != null, Order::getCreateTime, endTime);
        return count(lqw);
    }
}
