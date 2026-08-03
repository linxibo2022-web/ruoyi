package plus.ruoyi.business.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.NumberUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import plus.ruoyi.business.mall.domain.bo.CreateOrderBo;
import plus.ruoyi.business.mall.domain.bo.OrderBo;
import plus.ruoyi.business.mall.domain.bo.RefundBo;
import plus.ruoyi.business.mall.domain.vo.CreateOrderVo;
import plus.ruoyi.business.mall.domain.vo.OrderVo;
import plus.ruoyi.business.mall.service.IOrderService;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictOrderStatus;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.pay.domain.bo.GoodsDetailBo;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.service.PayService;
import plus.ruoyi.common.pay.utils.PayUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单支付接口
 * <p>
 * 只有当支付模块启用时才加载此控制器
 *
 * @author 抓蛙师
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/common/mall/order")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class OrderPayController {

    /**
     * 小程序商品详情页路径
     * 用于微信订单管理跳转
     */
    private static final String GOODS_DETAIL_PATH = "pages/goods/detail";

    /**
     * 支付服务
     */
    private final PayService payService;
    /**
     * 订单服务
     */
    private final IOrderService orderService;

    /**
     * 获取支持的支付方式列表
     *
     * @return 支持的支付方式
     */
    @SaIgnore
    @GetMapping("/supportedPaymentMethods")
    public R<List<DictPaymentMethod>> getSupportedPaymentMethods() {
        List<DictPaymentMethod> methods = payService.getSupportedPaymentMethods();
        return R.ok(methods);
    }

    /**
     * 创建订单
     *
     * @param bo 订单信息
     * @return 订单数据
     */
    @PostMapping("/createOrder")
    public R<CreateOrderVo> createOrder(@Validated @RequestBody CreateOrderBo bo) {
        // 生成订单号
        String orderNo = PayUtils.generateOutTradeNo("ORDER");

        // 设置系统字段
        bo.setOrderNo(orderNo);
        bo.setUserId(LoginHelper.getUserId());

        // 开发环境下将订单金额固定为0.01元，便于测试支付功能
        if (isDevelopmentEnvironment()) {
            log.info("开发环境：订单金额设置为0.01元 - 原价格: {}, 数量: {}, 原金额: {}",
                bo.getPrice(), bo.getQuantity(), NumberUtil.mul(bo.getPrice(), bo.getQuantity()));
            bo.setTotalAmount(new BigDecimal("0.01"));
        } else {
            bo.setTotalAmount(NumberUtil.mul(bo.getPrice(), bo.getQuantity()));
        }

        bo.setOrderStatus(DictOrderStatus.PENDING.getValue());

        // 保存订单并返回结果
        CreateOrderVo vo = orderService.createOrder(bo);

        return R.ok("订单创建成功", vo);
    }

    /**
     * 查询订单状态
     * <p>
     * 此接口会主动查询支付平台同步支付状态，确保即使回调丢失也能获取正确状态
     *
     * @param orderNo 订单号
     * @return 支付状态结果
     */
    @GetMapping("queryOrderStatus")
    public R<Void> queryOrderStatus(@Validated @NotBlank(message = "订单号不为空") String orderNo) {
        // 主动同步支付状态，确保获取最新状态
        OrderVo orderVo = orderService.getByOutTradeNo(orderNo, true);
        if (ObjectUtils.isNull(orderVo)) {
            return R.fail("订单不存在");
        }
        return DictOrderStatus.isPaid(orderVo.getOrderStatus()) ? R.ok("支付成功") :
            R.fail(DictOrderStatus.getByValue(orderVo.getOrderStatus()).getLabel());
    }

    /**
     * 获取订单信息
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("getOrderByOrderNo")
    public R<OrderVo> getOrderByOrderNo(@Validated @NotBlank(message = "订单号不为空") String orderNo) {
        // 普通查询，只查数据库
        OrderVo orderVo = orderService.getByOutTradeNo(orderNo, false);
        return R.ok("查询成功", orderVo);
    }

    /**
     * 查询订单列表
     *
     * @param bo 查询条件
     * @param pageQuery 分页参数
     * @return 订单分页数据
     */
    @GetMapping("/pageOrders")
    public R<PageResult<OrderVo>> pageOrders(OrderBo bo, PageQuery pageQuery) {
        // 设置当前用户ID，只查询自己的订单
        bo.setUserId(LoginHelper.getUserId());

        PageResult<OrderVo> pageResult = orderService.page(bo, pageQuery);
        return R.ok("查询成功", pageResult);
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @PostMapping("/cancelOrder")
    public R<Void> cancelOrder(@Validated @NotBlank(message = "订单号不能为空") @RequestParam String orderNo) {
        // 1. 验证并获取订单信息
        OrderVo order = validateOrder(orderNo);

        // 2. 验证用户权限
        validateUserPermission(order);

        // 3. 验证订单是否可以取消
        validateOrderCancel(order);

        // 4. 执行取消订单操作
        boolean success = orderService.cancelOrder(orderNo, LoginHelper.getUserId());

        // 5. 返回结果
        return success ? R.ok("订单取消成功") : R.fail("订单取消失败");
    }

    /**
     * 统一支付接口
     *
     * @param request 支付请求参数
     * @return 支付结果
     */
    @PostMapping("/createPayment")
    public R<PayResponse> createPayment(@Validated @RequestBody PayRequest request) {
        try {
            // 1. 验证并获取订单信息
            OrderVo order = validateOrder(request.getOrderNo());

            // 2. 验证用户权限
            validateUserPermission(order);

            // 3. 验证订单状态
            validateOrderStatus(order);

            // 4. 验证支付方式
            DictPaymentMethod paymentMethod = validatePaymentMethod(request.getPaymentMethod());

            // 5. 设置订单相关信息到请求中
            enrichPaymentRequest(request, order);

            // 6. 调用支付服务处理（所有业务逻辑都在Handler中）
            PayResponse response = payService.pay(paymentMethod, request);

            // 7. 支付请求成功后，立即更新订单的支付方式
            // 注意：不更新订单状态，状态由支付回调更新
            if (response.isSuccess()) {
                orderService.updatePaymentMethod(order.getOrderNo(), paymentMethod.getValue());
                log.info("订单支付方式已更新: orderNo={}, paymentMethod={}", order.getOrderNo(), paymentMethod.getValue());
            }

            return R.ok("支付请求成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("支付参数错误: orderNo={}, paymentMethod={}, error={}",
                request.getOrderNo(), request.getPaymentMethod(), e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("支付失败: orderNo={}, paymentMethod={}, error={}",
                request.getOrderNo(), request.getPaymentMethod(), e.getMessage(), e);
            return R.fail("支付失败: " + e.getMessage());
        }
    }

    /**
     * 订单退款
     *
     * @param request 退款请求参数
     * @return 退款结果
     */
    @SaCheckRole(value = TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @PostMapping("/refundOrder")
    public R<RefundResponse> refundOrder(@Validated @RequestBody RefundBo request) {
        // 删除此行后再开启自动退款
//        if (true) {
//            throw new ServiceException("请删除此行代码才能开启用户自主发起自动退款");
//        }
        try {
            // 1. 验证并获取订单信息
            OrderVo order = validateOrder(request.getOrderNo());

            // 2. 验证用户权限
            validateUserPermission(order);

            // 3. 验证订单退款状态
            validateRefundOrderStatus(order);

            // 4. 验证退款金额
            BigDecimal actualRefundAmount = validateRefundAmount(request.getRefundAmount(), order.getTotalAmount());

            // 5. 解析支付方式
            if (order.getPaymentMethod() == null || order.getPaymentMethod().isEmpty()) {
                return R.fail("订单支付方式为空,无法退款。请确认订单已完成支付");
            }

            DictPaymentMethod paymentMethod = DictPaymentMethod.getByValue(order.getPaymentMethod());
            if (paymentMethod == null) {
                return R.fail("未知的支付方式: " + order.getPaymentMethod());
            }

            // 6. 构建退款请求
            RefundRequest refundRequest = RefundRequest.builder()
                .outTradeNo(order.getOrderNo())
                .outRefundNo(PayUtils.generateOutRefundNo())
                .totalFee(order.getTotalAmount())
                .refundFee(actualRefundAmount)
                .reason(request.getReason())
                .build();

            // 7. 发起退款
            RefundResponse response = payService.refund(paymentMethod, refundRequest);

            // 如果退款成功,更新订单状态
            if (response.isSuccess()) {
                orderService.updateOrderByOutTradeNo(
                    order.getOrderNo(),
                    DictOrderStatus.REFUNDED.getValue(),
                    response.getRefundId(),
                    order.getPaymentMethod()
                );
            }

            log.info("订单退款请求完成: orderNo={}, refundAmount={}, success={}",
                request.getOrderNo(), actualRefundAmount, response.isSuccess());
            return R.ok(response);

        } catch (Exception e) {
            log.error("订单退款失败: orderNo={}, refundAmount={}, error={}",
                request.getOrderNo(), request.getRefundAmount(), e.getMessage(), e);
            return R.fail("订单退款失败: " + e.getMessage());
        }
    }

    // ============== 私有验证方法 ==============

    /**
     * 验证并获取订单信息
     *
     * @param orderNo 订单号
     * @return 订单数据
     */
    private OrderVo validateOrder(String orderNo) {
        // 验证时只查数据库，不同步支付状态
        OrderVo order = orderService.getByOutTradeNo(orderNo, false);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    /**
     * 验证用户权限
     *
     * @param order 订单数据
     */
    private void validateUserPermission(OrderVo order) {
        if (!order.getUserId().equals(LoginHelper.getUserId())) {
            throw new IllegalArgumentException("无权限操作此订单");
        }
    }

    /**
     * 验证订单状态
     *
     * @param order 订单数据
     */
    private void validateOrderStatus(OrderVo order) {
        if (!DictOrderStatus.PENDING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("订单状态不允许支付，当前状态: " +
                DictOrderStatus.getByValue(order.getOrderStatus()).getLabel());
        }
    }

    /**
     * 验证订单退款状态
     *
     * @param order 订单数据
     */
    private void validateRefundOrderStatus(OrderVo order) {
        if (!DictOrderStatus.isPaid(order.getOrderStatus())) {
            throw new IllegalArgumentException("订单状态不允许退款，当前状态: " +
                DictOrderStatus.getByValue(order.getOrderStatus()).getLabel());
        }
    }

    /**
     * 验证订单是否可以取消
     *
     * @param order 订单数据
     */
    private void validateOrderCancel(OrderVo order) {
        if (!DictOrderStatus.PENDING.getValue().equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("订单状态不允许取消，当前状态: " +
                DictOrderStatus.getByValue(order.getOrderStatus()).getLabel());
        }
    }

    /**
     * 验证支付方式
     *
     * @param paymentMethodValue 支付方式值
     * @return 支付方式枚举
     */
    private DictPaymentMethod validatePaymentMethod(String paymentMethodValue) {
        DictPaymentMethod paymentMethod = DictPaymentMethod.getByValue(paymentMethodValue);

        if (!payService.isSupported(paymentMethod)) {
            throw new IllegalArgumentException(paymentMethod.getLabel() + "暂不可用");
        }

        return paymentMethod;
    }

    /**
     * 验证退款金额
     *
     * @param refundAmount 退款金额
     * @param totalAmount  订单金额
     * @return 实际退款金额
     */
    private BigDecimal validateRefundAmount(BigDecimal refundAmount, BigDecimal totalAmount) {
        BigDecimal actualRefundAmount = refundAmount != null ? refundAmount : totalAmount;

        if (actualRefundAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("退款金额不能超过订单金额");
        }

        return actualRefundAmount;
    }

    /**
     * 丰富支付请求信息
     *
     * @param request 支付请求
     * @param order   订单数据
     */
    private void enrichPaymentRequest(PayRequest request, OrderVo order) {
        // 设置订单相关信息
        if (request.getBody() == null) {
            request.setBody("订单支付-" + order.getOrderNo());
        }

        // 设置商品详情描述(微信订单管理必填，支付宝body字段)
        if (request.getDescription() == null) {
            String description = order.getGoodsName() + " x " + order.getQuantity();
            request.setDescription(description);
            log.debug("设置商品详情描述: {}", description);
        }

        // 设置商品详情(支付宝的body字段)
        if (request.getDetail() == null) {
            request.setDetail(order.getGoodsName() + " x " + order.getQuantity());
        }

        // 设置商品详情列表
        if (request.getGoodsDetail() == null) {
            List<GoodsDetailBo> goodsDetailList = buildGoodsDetailList(order);
            request.setGoodsDetail(goodsDetailList);
            log.debug("设置商品详情列表: {}", goodsDetailList);
        }

        // 安全修复：支付金额必须从订单获取，禁止前端传入（防止篡改金额攻击）
        if (isDevelopmentEnvironment()) {
            log.info("开发环境：支付金额设置为0.01元 - 订单原金额: {}", order.getTotalAmount());
            request.setTotalFee(new BigDecimal("0.01"));
        } else {
            request.setTotalFee(order.getTotalAmount());
        }
        if (request.getOutTradeNo() == null) {
            request.setOutTradeNo(order.getOrderNo());
        }

        // 设置客户端IP (微信v2支付必填字段)
        if (request.getClientIp() == null) {
            request.setClientIp(ServletUtils.getClientIP()); // 默认本地IP,实际应该从请求中获取
        }
    }

    /**
     * 构建商品详情列表
     *
     * @param order 订单信息
     * @return 商品详情列表
     */
    private List<GoodsDetailBo> buildGoodsDetailList(OrderVo order) {
        List<GoodsDetailBo> goodsDetailList = new ArrayList<>();

        // 构建商品详情页路径
        String detailPath = GOODS_DETAIL_PATH + "?id=" + order.getGoodsId();

        GoodsDetailBo goodsDetail = GoodsDetailBo.builder()
            .goodsId(order.getGoodsId())
            .goodsName(order.getGoodsName())
            .quantity(order.getQuantity())
            .unitPrice(order.getPrice())
            .goodsImg(order.getGoodsImg())
            .detailPath(detailPath)
            .build();

        goodsDetailList.add(goodsDetail);
        return goodsDetailList;
    }

    /**
     * 判断是否为开发环境
     *
     * @return true 表示开发环境
     */
    private boolean isDevelopmentEnvironment() {
        String activeProfile = SpringUtils.getActiveProfile();
        return "dev".equals(activeProfile);
    }
}
