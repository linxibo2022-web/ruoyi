package plus.ruoyi.business.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.mall.dao.IOrderDao;
import plus.ruoyi.business.mall.domain.bo.CreateOrderBo;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.business.mall.domain.dto.OrderExtInfoDto;
import plus.ruoyi.business.mall.domain.vo.CreateOrderVo;
import plus.ruoyi.business.mall.domain.vo.OrderVo;
import plus.ruoyi.business.mall.service.IOrderService;
import plus.ruoyi.common.core.dict.DictOrderStatus;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import org.springframework.stereotype.Service;
import plus.ruoyi.business.mall.domain.Order;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.event.PaySuccessEvent;
import plus.ruoyi.common.pay.service.PayService;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 订单Service业务层处理
 *
 * @author 抓蛙师
 * @date 2025-07-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    /**
     * 小程序商品详情页路径
     * 用于微信订单管理跳转
     */
    private static final String GOODS_DETAIL_PATH = "pages/goods/detail";

    private final IOrderDao orderDao;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 支付服务(可选注入,用于查询支付状态)
     */
    @Autowired(required = false)
    private PayService payService;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public OrderVo get(Long id) {
        Order entity = orderDao.getById(id);
        return MapstructUtils.convert(entity, OrderVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<OrderVo> list(OrderBo bo) {
        PlusLambdaQuery<Order> wrapper = orderDao.buildQueryWrapper(bo);
        List<Order> entities = orderDao.list(wrapper);
        return MapstructUtils.convert(entities, OrderVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<OrderVo> page(OrderBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<Order> wrapper = orderDao.buildQueryWrapper(bo);
        PageResult<Order> entityPage = orderDao.page(wrapper, pageQuery);
        return entityPage.convert(OrderVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(OrderBo bo) {
        Order entity = MapstructUtils.convert(bo, Order.class);
        beforeSave(entity);
        orderDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(OrderBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("订单ID不能为空");
        }
        if (!orderDao.exists(bo.getId())) {
            throw ServiceException.of("订单不存在");
        }
        Order entity = MapstructUtils.convert(bo, Order.class);
        beforeSave(entity);
        return orderDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return orderDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<OrderBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<Order> entities = new ArrayList<>(boList.size());
        for (OrderBo bo : boList) {
            Order entity = MapstructUtils.convert(bo, Order.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return orderDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(Order entity) {
        // 保存前数据校验
    }

    /**
     * 订单数据删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 删除前校验
    }

    /**
     * 创建订单
     *
     * @param bo 订单创建参数
     * @return 创建订单结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderVo createOrder(CreateOrderBo bo) {
        try {
            log.info("开始创建订单: goodsId={}, goodsName={}, userId={}",
                bo.getGoodsId(), bo.getGoodsName(), bo.getUserId());

            // 1. 数据校验（可选：商品是否存在、库存是否充足等）
            validateOrderData(bo);

            // 2. 转换为Order实体并保存
            Order order = new Order();
            order.setOrderNo(bo.getOrderNo());
            order.setUserId(bo.getUserId());
            order.setGoodsId(bo.getGoodsId());
            order.setGoodsName(bo.getGoodsName());
            order.setGoodsImg(bo.getGoodsImg());
            order.setPrice(bo.getPrice());
            order.setQuantity(bo.getQuantity());
            order.setTotalAmount(bo.getTotalAmount());
            // 设置实付金额
            order.setActualAmount(bo.getActualAmount());
            order.setOrderStatus(bo.getOrderStatus());
            order.setBuyerRemark(bo.getBuyerRemark());

            // 处理扩展信息 - 记录商品详情页路径用于微信订单管理跳转
            if (bo.getOrderExtInfo() == null) {
                bo.setOrderExtInfo(new OrderExtInfoDto());
            }
            // 自动设置appid(从登录上下文获取)
            try {
                if (LoginHelper.isLogin()) {
                    String appid = LoginHelper.getAppid();
                    if (StringUtils.isNotBlank(appid)) {
                        bo.getOrderExtInfo().setAppid(appid);
                        log.debug("订单保存appid: {}", appid);
                    }
                }
            } catch (Exception e) {
                log.warn("获取登录用户appid失败,订单将不保存appid: {}", e.getMessage());
            }
            // 自动设置商品详情页路径
            if (bo.getGoodsId() != null) {
                String goodsDetailPath = GOODS_DETAIL_PATH + "?id=" + bo.getGoodsId();
                bo.getOrderExtInfo().setGoodsDetailPath(goodsDetailPath);
            }
            order.setOrderExtInfo(JsonUtils.toJsonString(bo.getOrderExtInfo()));
            // 处理收货信息
            if (bo.getReceiverInfo() != null) {
                order.setReceiverInfo(JsonUtils.toJsonString(bo.getReceiverInfo()));
            }
            // 物流信息（创建订单时通常为空，可以直接设置）
            order.setShippingInfo(bo.getShippingInfo());
            order.setRemark(bo.getRemark());

            // 保存订单
            orderDao.insert(order);

            // 3. 构建返回对象
            CreateOrderVo vo = new CreateOrderVo();
            vo.setId(order.getId());
            vo.setOrderNo(bo.getOrderNo());
            vo.setGoodsId(bo.getGoodsId());
            vo.setGoodsName(bo.getGoodsName());
            vo.setGoodsImg(bo.getGoodsImg());
            vo.setPrice(bo.getPrice());
            vo.setQuantity(bo.getQuantity());
            vo.setTotalAmount(bo.getTotalAmount());
            vo.setOrderStatus(bo.getOrderStatus());
            vo.setOrderStatusName(DictOrderStatus.getByValue(bo.getOrderStatus()).getLabel());
            vo.setBuyerRemark(bo.getBuyerRemark());
            vo.setCreateTime(new Date());

            log.info("订单创建成功: orderId={}, orderNo={}", order.getId(), bo.getOrderNo());
            return vo;

        } catch (Exception e) {
            log.error("创建订单失败: goodsId={}, userId={}, 错误: {}",
                bo.getGoodsId(), bo.getUserId(), e.getMessage(), e);
            throw new ServiceException("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 订单数据校验
     *
     * @param bo 订单参数
     */
    private void validateOrderData(CreateOrderBo bo) {
        // 1. 基础数据校验
        if (bo.getGoodsId() == null) {
            throw ServiceException.of("商品ID不能为空");
        }

        if (bo.getPrice() == null || bo.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw ServiceException.of("商品价格必须大于0");
        }

        if (bo.getQuantity() == null || bo.getQuantity() <= 0) {
            throw ServiceException.of("购买数量必须大于0");
        }

        // 自动计算订单总金额和实付金额（方案A：简单计算模式）
        if (bo.getTotalAmount() == null) {
            BigDecimal totalAmount = bo.getPrice().multiply(new BigDecimal(bo.getQuantity()));
            bo.setTotalAmount(totalAmount);
            log.info("自动计算订单总金额: goodsId={}, price={}, quantity={}, totalAmount={}",
                bo.getGoodsId(), bo.getPrice(), bo.getQuantity(), totalAmount);
        }

        if (bo.getActualAmount() == null) {
            BigDecimal calculatedAmount = calculateActualAmount(bo);
            bo.setActualAmount(calculatedAmount);
            log.info("自动计算实付金额: goodsId={}, price={}, quantity={}, actualAmount={}",
                bo.getGoodsId(), bo.getPrice(), bo.getQuantity(), calculatedAmount);
        }

        if (bo.getActualAmount() == null || bo.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw ServiceException.of("实付金额必须大于0");
        }
        // 2. 业务规则校验（可根据需要扩展）
        // - 检查商品是否存在
        // - 检查商品是否上架
        // - 检查库存是否充足
        // - 检查用户是否有购买权限等

        log.info("订单数据校验通过: goodsId={}, quantity={}, price={}",
            bo.getGoodsId(), bo.getQuantity(), bo.getPrice());
    }

    /**
     * 计算实付金额（方案A：简单计算模式）
     * <p>
     * 简单计算规则：实付金额 = 商品价格 × 购买数量
     * 可在此基础上扩展优惠券、满减、积分抵扣等逻辑
     *
     * @param bo 订单参数
     * @return 计算后的实付金额
     */
    private BigDecimal calculateActualAmount(CreateOrderBo bo) {
        if (bo.getPrice() == null || bo.getQuantity() == null) {
            throw ServiceException.of("价格或数量为空，无法计算实付金额");
        }

        // 基础计算：价格 × 数量
        BigDecimal actualAmount = bo.getPrice().multiply(new BigDecimal(bo.getQuantity()));

        // TODO: 可在此扩展更复杂的计算逻辑
        // - 优惠券折扣
        // - 满减活动
        // - 积分抵扣
        // - 运费计算
        // - 会员折扣等

        log.debug("计算实付金额: price={}, quantity={}, actualAmount={}",
            bo.getPrice(), bo.getQuantity(), actualAmount);

        return actualAmount;
    }

    /**
     * 根据商户订单号更新订单状态
     * <p>
     * 注意: 此方法用于支付回调场景,会忽略租户隔离
     * 原因: 支付回调请求来自第三方支付平台,没有用户登录上下文,也就没有租户上下文
     *
     * @param outTradeNo    商户订单号
     * @param orderStatus   新的订单状态
     * @param transactionId 第三方交易号
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderByOutTradeNo(String outTradeNo, String orderStatus, String transactionId, String paymentMethod) {
        // 支付回调时忽略租户隔离,因为回调请求没有租户上下文
        return TenantHelper.ignore(() -> {
            try {
                log.info("根据商户订单号更新订单状态: outTradeNo={}, orderStatus={}, transactionId={}",
                    outTradeNo, orderStatus, transactionId);

                if (StringUtils.isBlank(outTradeNo)) {
                    throw new ServiceException("商户订单号不能为空");
                }

                // 1. 查询订单
                Order existingOrder = orderDao.getByOrderNo(outTradeNo);

                if (existingOrder == null) {
                    throw new ServiceException("未找到订单: " + outTradeNo);
                }

                // 2. 构建更新对象
                Order updateOrder = new Order();
                updateOrder.setId(existingOrder.getId());
                updateOrder.setOrderStatus(orderStatus);

                // 3. 根据订单状态设置相应字段
                if (DictOrderStatus.PAID.getValue().equals(orderStatus)) {
                    // 幂等护栏:仅"待支付"订单可转为"已支付"。
                    // 支付宝条码支付会"同步发事件 + 异步回调"两次触发支付成功流程,
                    // 已支付/已处理订单直接返回 false,避免重复减库存/赠积分/发货等资损
                    if (!DictOrderStatus.isPending(existingOrder.getOrderStatus())) {
                        log.info("订单已非待支付状态(当前:{}),幂等跳过支付状态更新: outTradeNo={}",
                            existingOrder.getOrderStatus(), outTradeNo);
                        return false;
                    }
                    updateOrder.setPaymentTime(DateUtils.getNowDate());
                    // 设置实际支付金额(默认等于订单总金额)
                    updateOrder.setActualAmount(existingOrder.getTotalAmount());
                    if (StringUtils.isNotBlank(transactionId)) {
                        updateOrder.setTransactionId(transactionId);
                    }
                    // 保存支付方式
                    if (StringUtils.isNotBlank(paymentMethod)) {
                        updateOrder.setPaymentMethod(paymentMethod);
                    }
                }

                // 4. 执行更新
                orderDao.updateById(updateOrder);

                return true;

            } catch (Exception e) {
                log.error("更新订单状态失败: outTradeNo={}, orderStatus={}, 错误: {}",
                    outTradeNo, orderStatus, e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * 更新订单的支付方式
     * <p>
     * 用于在发起支付时记录用户选择的支付方式，不更新订单状态
     *
     * @param orderNo       订单号
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaymentMethod(String orderNo, String paymentMethod) {
        try {
            log.info("更新订单支付方式: orderNo={}, paymentMethod={}", orderNo, paymentMethod);

            if (StringUtils.isBlank(orderNo)) {
                throw new ServiceException("订单号不能为空");
            }

            if (StringUtils.isBlank(paymentMethod)) {
                throw new ServiceException("支付方式不能为空");
            }

            // 1. 查询订单
            Order existingOrder = orderDao.getByOrderNo(orderNo);
            if (existingOrder == null) {
                throw new ServiceException("未找到订单: " + orderNo);
            }

            // 2. 只更新支付方式，不更新订单状态
            Order updateOrder = new Order();
            updateOrder.setId(existingOrder.getId());
            updateOrder.setPaymentMethod(paymentMethod);

            // 3. 执行更新
            boolean success = orderDao.updateById(updateOrder) > 0;

            if (success) {
                log.info("订单支付方式更新成功: orderNo={}, paymentMethod={}", orderNo, paymentMethod);
            }

            return success;

        } catch (Exception e) {
            log.error("更新订单支付方式失败: orderNo={}, paymentMethod={}, 错误: {}",
                orderNo, paymentMethod, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据商户订单号查询订单
     * <p>
     * 查询逻辑:
     * 1. 先查询数据库获取订单信息
     * 2. 如果 syncPaymentStatus=true 且订单状态为pending(待支付),则调用支付平台查询真实支付状态
     * 3. 如果支付平台返回已支付,则更新数据库状态
     * 4. 返回最新的订单信息
     * <p>
     * 使用场景:
     * - syncPaymentStatus=false: 普通查询,直接返回数据库状态(快速)
     * - syncPaymentStatus=true: 前端轮询查询订单支付结果时使用,确保即使回调丢失也能获取正确状态
     *
     * @param outTradeNo        商户订单号
     * @param syncPaymentStatus 是否同步支付状态
     * @return 订单信息
     */
    @Override
    public OrderVo getByOutTradeNo(String outTradeNo, boolean syncPaymentStatus) {
        if (StringUtils.isBlank(outTradeNo)) {
            return null;
        }

        // 1. 查询数据库订单
        Order order = orderDao.getByOrderNo(outTradeNo);
        if (order == null) {
            return null;
        }

        // 2. 判断是否需要同步支付状态
        if (!syncPaymentStatus) {
            // 不需要同步,直接返回数据库状态
            return MapstructUtils.convert(order, OrderVo.class);
        }

        // 3. 只有pending状态的订单才需要查询支付平台
        if (!DictOrderStatus.PENDING.getValue().equals(order.getOrderStatus())) {
            return MapstructUtils.convert(order, OrderVo.class);
        }

        // 4. 调用支付平台查询状态并同步
        syncPaymentStatusFromPlatform(order);

        // 5. 重新查询数据库获取最新状态
        order = orderDao.getByOrderNo(outTradeNo);
        return MapstructUtils.convert(order, OrderVo.class);
    }

    /**
     * 从支付平台同步支付状态
     *
     * @param order 订单对象
     */
    private void syncPaymentStatusFromPlatform(Order order) {
        try {
            // 1. 检查支付服务是否可用
            if (payService == null) {
                log.warn("支付服务未启用,无法同步支付状态: {}", order.getOrderNo());
                return;
            }

            // 2. 检查订单支付方式
            if (StringUtils.isBlank(order.getPaymentMethod())) {
                log.warn("订单未设置支付方式,无法同步支付状态: {}", order.getOrderNo());
                return;
            }

            // 3. 解析支付方式
            DictPaymentMethod paymentMethod;
            try {
                paymentMethod = DictPaymentMethod.getByValue(order.getPaymentMethod());
            } catch (Exception e) {
                log.error("不支持的支付方式: {}, orderNo: {}", order.getPaymentMethod(), order.getOrderNo());
                return;
            }

            // 4. 获取appid (使用登录用户的appid或租户默认appid)
            String appid = getAppidForOrder(order);
            if (StringUtils.isBlank(appid)) {
                log.warn("无法获取appid,无法同步支付状态: {}", order.getOrderNo());
                return;
            }

            log.info("开始同步支付状态: orderNo={}, paymentMethod={}, appid={}",
                order.getOrderNo(), paymentMethod.getValue(), appid);

            // 5. 查询支付平台
            PayResponse response = payService.queryPayment(paymentMethod, order.getOrderNo(), appid);

            // 6. 根据查询结果更新订单状态
            if (response != null && response.isSuccess()) {
                String tradeState = response.getTradeState();
                log.info("查询到支付状态: orderNo={}, tradeState={}, transactionId={}",
                    order.getOrderNo(), tradeState, response.getTransactionId());

                // 如果支付成功,发布支付成功事件（由事件监听器更新订单状态和执行后续逻辑）
                if ("SUCCESS".equals(tradeState)) {
                    // 构建支付成功事件
                    PaySuccessEvent event = PaySuccessEvent.builder()
                        .source(this)
                        .outTradeNo(order.getOrderNo())
                        .transactionId(response.getTransactionId())
                        // PaySuccessEvent.totalFee 契约统一为"元"(微信/支付宝/银联各发布点一致),
                        // response.getTotalAmount() 已是元,直接传;切勿再转分,否则事件金额放大 100 倍
                        .totalFee(response.getTotalAmount() != null ? response.getTotalAmount().toPlainString() : null)
                        .paymentMethod(paymentMethod.getValue())
                        .appId(appid)
                        .payTime(response.getPayTime() != null ? response.getPayTime().toString() : null)
                        .build();

                    // 发布事件（让OrderPaymentEventListener处理后续逻辑）
                    eventPublisher.publishEvent(event);
                    log.info("支付状态同步成功: orderNo={}, 已发布支付成功事件", order.getOrderNo());
                }
            } else {
                log.warn("查询支付状态失败: orderNo={}, message={}",
                    order.getOrderNo(), response != null ? response.getMessage() : "响应为空");
            }

        } catch (Exception e) {
            log.error("同步支付状态异常: orderNo={}, error={}",
                order.getOrderNo(), e.getMessage(), e);
        }
    }

    /**
     * 获取订单对应的appid
     * <p>
     * 获取优先级:
     * 1. 订单扩展信息中的appid (最优先,订单创建时保存的)
     * 2. 登录上下文中的appid (如果用户已登录)
     * 3. 返回null
     *
     * @param order 订单对象
     * @return appid
     */
    private String getAppidForOrder(Order order) {
        // 1. 优先从订单扩展信息中获取appid
        if (StringUtils.isNotBlank(order.getOrderExtInfo())) {
            try {
                OrderExtInfoDto extInfo = JsonUtils.parseObject(order.getOrderExtInfo(), OrderExtInfoDto.class);
                if (extInfo != null && StringUtils.isNotBlank(extInfo.getAppid())) {
                    log.debug("从订单扩展信息中获取appid: {}", extInfo.getAppid());
                    return extInfo.getAppid();
                }
            } catch (Exception e) {
                log.warn("解析订单扩展信息失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }

        // 2. 尝试从登录上下文获取appid
        try {
            if (LoginHelper.isLogin()) {
                String appid = LoginHelper.getAppid();
                if (StringUtils.isNotBlank(appid)) {
                    log.debug("从登录上下文获取appid: {}", appid);
                    return appid;
                }
            }
        } catch (Exception e) {
            log.debug("无法从登录上下文获取appid: {}", e.getMessage());
        }

        log.warn("无法获取订单对应的appid: orderNo={}", order.getOrderNo());
        return null;
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @param userId  用户ID
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, Long userId) {
        log.info("开始取消订单: orderNo={}, userId={}", orderNo, userId);

        if (StringUtils.isBlank(orderNo)) {
            throw ServiceException.of("订单号不能为空");
        }

        if (userId == null) {
            throw ServiceException.of("用户ID不能为空");
        }

        // 1. 查询订单
        Order existingOrder = orderDao.getByOrderNoAndUserId(orderNo, userId);

        if (existingOrder == null) {
            throw ServiceException.of("未找到订单或无权限操作: " + orderNo);
        }

        // 2. 检查订单状态
        if (!DictOrderStatus.PENDING.getValue().equals(existingOrder.getOrderStatus())) {
            throw ServiceException.of("订单状态不允许取消，当前状态: " + existingOrder.getOrderStatus());
        }

        // 3. 更新订单状态为已取消
        existingOrder.setOrderStatus(DictOrderStatus.CANCELLED.getValue());

        return orderDao.updateById(existingOrder) > 0;

    }

    /**
     * 订单发货
     *
     * @param orderId      订单ID
     * @param shippingInfo 物流信息（JSON格式）
     * @return 是否发货成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deliverOrder(Long orderId, String shippingInfo) {
        log.info("开始订单发货: orderId={}, shippingInfo={}", orderId, shippingInfo);

        if (orderId == null) {
            throw ServiceException.of("订单ID不能为空");
        }

        if (StringUtils.isBlank(shippingInfo)) {
            throw ServiceException.of("物流信息不能为空");
        }

        // 1. 查询订单
        Order existingOrder = orderDao.getById(orderId);
        if (existingOrder == null) {
            throw ServiceException.of("未找到订单: " + orderId);
        }

        // 2. 检查订单状态 - 只有已支付的订单才能发货
        if (!DictOrderStatus.PAID.getValue().equals(existingOrder.getOrderStatus())) {
            throw ServiceException.of("订单状态不允许发货，当前状态: " + existingOrder.getOrderStatus() + "，只有已支付的订单才能发货");
        }

        // 3. 更新订单状态为已发货，并保存物流信息
        existingOrder.setOrderStatus(DictOrderStatus.DELIVERED.getValue());
        existingOrder.setShippingInfo(shippingInfo);

        return orderDao.updateById(existingOrder) > 0;
    }
}
