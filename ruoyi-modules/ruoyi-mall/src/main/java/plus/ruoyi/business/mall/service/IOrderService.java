package plus.ruoyi.business.mall.service;

import plus.ruoyi.business.mall.domain.bo.CreateOrderBo;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.business.mall.domain.vo.CreateOrderVo;
import plus.ruoyi.business.mall.domain.vo.OrderVo;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 订单服务接口
 *
 * @author 抓蛙师
 */
public interface IOrderService {

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    OrderVo get(Long id);

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    List<OrderVo> list(OrderBo bo);

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<OrderVo> page(OrderBo bo, PageQuery pageQuery);

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    Long add(OrderBo bo);

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    int update(OrderBo bo);

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    int batchSave(List<OrderBo> boList);

    /**
     * 创建订单
     *
     * @param bo 订单创建参数
     * @return 创建订单结果
     */
    CreateOrderVo createOrder(CreateOrderBo bo);

    /**
     * 根据商户订单号更新订单状态
     *
     * @param outTradeNo    商户订单号
     * @param orderStatus   新的订单状态
     * @param transactionId 第三方交易号
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    boolean updateOrderByOutTradeNo(String outTradeNo, String orderStatus, String transactionId, String paymentMethod);

    /**
     * 更新订单的支付方式
     * <p>
     * 用于在发起支付时记录用户选择的支付方式，不更新订单状态
     *
     * @param orderNo       订单号
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    boolean updatePaymentMethod(String orderNo, String paymentMethod);

    /**
     * 根据商户订单号查询订单
     * <p>
     * 如果 syncPaymentStatus=true 且订单状态为待支付(pending),会主动调用支付平台查询真实支付状态并同步
     *
     * @param outTradeNo        商户订单号
     * @param syncPaymentStatus 是否同步支付状态(true=查询支付平台同步状态, false=只查数据库)
     * @return 订单信息
     */
    OrderVo getByOutTradeNo(String outTradeNo, boolean syncPaymentStatus);

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @return 是否取消成功
     */
    boolean cancelOrder(String orderNo, Long userId);

    /**
     * 订单发货
     *
     * @param orderId      订单ID
     * @param shippingInfo 物流信息(JSON格式)
     * @return 是否发货成功
     */
    boolean deliverOrder(Long orderId, String shippingInfo);
}
