package plus.ruoyi.business.mall.dao;

import plus.ruoyi.business.mall.domain.Order;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;

import java.util.Date;
import java.util.List;

/**
 * 订单DAO接口
 *
 * @author 抓蛙师
 */
public interface IOrderDao extends IBaseDao<Order> {

    /**
     * 根据业务对象构建查询条件
     */
    PlusLambdaQuery<Order> buildQueryWrapper(OrderBo bo);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    Order getByOrderNo(String orderNo);

    /**
     * 根据订单号和用户ID查询订单
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @return 订单信息
     */
    Order getByOrderNoAndUserId(String orderNo, Long userId);

    /**
     * 根据订单状态统计订单数量
     *
     * @param status 订单状态
     * @return 订单数量
     */
    long countByStatus(String status);

    /**
     * 根据订单状态列表统计订单数量
     *
     * @param statuses 订单状态列表
     * @return 订单数量
     */
    long countByStatuses(List<String> statuses);

    /**
     * 根据时间范围统计订单数量
     *
     * @param startTime 开始时间(可为null)
     * @param endTime   结束时间(可为null)
     * @return 订单数量
     */
    long countByTimeRange(Date startTime, Date endTime);

    /**
     * 根据订单状态列表和时间范围查询订单列表
     *
     * @param statuses  订单状态列表
     * @param startTime 开始时间(可为null)
     * @param endTime   结束时间(可为null)
     * @return 订单列表
     */
    List<Order> listByStatusesAndTimeRange(List<String> statuses, Date startTime, Date endTime);

    /**
     * 根据订单状态列表和时间范围统计订单数量
     *
     * @param statuses  订单状态列表
     * @param startTime 开始时间(可为null)
     * @param endTime   结束时间(可为null)
     * @return 订单数量
     */
    long countByStatusesAndTimeRange(List<String> statuses, Date startTime, Date endTime);
}
