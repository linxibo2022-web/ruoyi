package plus.ruoyi.common.pay.balance.handler;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.pay.core.handler.PayHandler;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;

import java.math.BigDecimal;

/**
 * 余额支付处理器
 *
 * 主要功能：
 * - 余额支付请求处理（模拟实现）
 * - 余额退款处理（模拟实现）
 * - 参数验证和预处理
 *
 * 注意：此为简化实现版本，仅用于演示和测试
 * 生产环境需要：
 * 1. 实现真实的用户余额查询和扣减
 * 2. 实现余额流水记录
 * 3. 实现余额账户管理
 * 4. 增加事务控制和并发控制
 *
 * @author 抓蛙师
 */
@Slf4j
public class BalancePayHandler implements PayHandler {

    @Override
    public DictPaymentMethod getPaymentMethod() {
        return DictPaymentMethod.BALANCE;
    }

    /**
     * 处理余额支付请求
     *
     * 流程：
     * 1. 预处理支付请求参数
     * 2. 验证支付请求参数
     * 3. 执行余额支付业务逻辑（模拟）
     *
     * @param request 支付请求参数
     * @return 支付响应结果
     */
    @Override
    public PayResponse pay(PayRequest request) {
        try {
            log.info("余额支付请求开始: 订单号={}, 金额={}",
                request.getOutTradeNo(), request.getTotalFee());

            // 1. 预处理支付请求参数
            preprocessPaymentRequest(request);

            // 2. 验证支付请求参数
            validatePaymentRequest(request);

            // 3. 执行余额支付业务逻辑
            PayResponse response = executeBalancePayment(request);

            log.info("余额支付请求完成: 订单号={}, 结果={}",
                request.getOutTradeNo(), response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("余额支付失败: 订单号={}, 错误={}",
                request.getOutTradeNo(), e.getMessage(), e);
            throw new ServiceException("余额支付失败: " + e.getMessage());
        }
    }

    /**
     * 处理余额退款请求
     *
     * 流程：
     * 1. 预处理退款请求参数
     * 2. 验证退款请求参数
     * 3. 执行余额退款业务逻辑（模拟）
     *
     * @param request 退款请求参数
     * @return 退款响应结果
     */
    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            log.info("余额退款请求开始: 退款单号={}, 原订单号={}, 退款金额={}",
                request.getOutRefundNo(), request.getOutTradeNo(), request.getRefundFee());

            // 1. 预处理退款请求参数
            preprocessRefundRequest(request);

            // 2. 验证退款请求参数
            validateRefundRequest(request);

            // 3. 执行余额退款业务逻辑
            RefundResponse response = executeBalanceRefund(request);

            log.info("余额退款请求完成: 退款单号={}, 结果={}",
                request.getOutRefundNo(), response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("余额退款失败: 退款单号={}, 错误={}",
                request.getOutRefundNo(), e.getMessage(), e);
            throw new ServiceException("余额退款失败: " + e.getMessage());
        }
    }

    /**
     * 查询余额支付状态
     *
     * 余额支付是同步的，支付成功即完成，无需查询
     *
     * @param outTradeNo 商户订单号
     * @param appid      应用ID（余额支付不需要）
     * @return 支付状态查询结果
     */
    @Override
    public PayResponse queryPayment(String outTradeNo, String appid) {
        log.info("查询余额支付状态: 订单号={}", outTradeNo);

        // 余额支付无需查询，直接返回成功状态
        return PayResponse.builder()
            .success(true)
            .message("余额支付查询成功")
            .outTradeNo(outTradeNo)
            .paymentMethod(DictPaymentMethod.BALANCE.getValue())
            .tradeState("SUCCESS")
            .build();
    }

    /**
     * 查询余额退款状态
     *
     * 余额退款是同步的，退款成功即完成，无需查询
     *
     * @param outRefundNo 商户退款单号
     * @param appid       应用ID（余额支付不需要）
     * @return 退款状态查询结果
     */
    @Override
    public RefundResponse queryRefund(String outRefundNo, String appid) {
        log.info("查询余额退款状态: 退款单号={}", outRefundNo);

        // 余额退款无需查询，直接返回成功状态
        return RefundResponse.builder()
            .success(true)
            .message("余额退款查询成功")
            .outRefundNo(outRefundNo)
            .refundStatus("SUCCESS")
            .build();
    }

    /**
     * 处理余额支付回调
     *
     * 余额支付是同步的，无异步回调
     *
     * @param request 回调请求数据
     * @return 回调处理结果
     */
    @Override
    public NotifyResponse handleNotify(NotifyRequest request) {
        log.warn("余额支付不支持异步回调通知");
        return NotifyResponse.fail("余额支付不支持回调");
    }

    // ===================== 私有方法 - 参数预处理 =====================

    /**
     * 预处理支付请求参数
     *
     * 补充默认值和必要参数
     *
     * @param request 支付请求对象
     */
    private void preprocessPaymentRequest(PayRequest request) {
        // 设置客户端IP
        if (StringUtils.isBlank(request.getClientIp())) {
            request.setClientIp(ServletUtils.getClientIP());
        }

        // 设置默认商品描述
        if (StringUtils.isBlank(request.getBody())) {
            request.setBody("余额支付-" + request.getOutTradeNo());
        }

        // 设置交易类型
        if (StringUtils.isBlank(request.getTradeType())) {
            request.setTradeType(DictPaymentMethod.BALANCE.getValue());
        }
    }

    /**
     * 预处理退款请求参数
     *
     * 补充默认值和必要参数
     *
     * @param request 退款请求对象
     */
    private void preprocessRefundRequest(RefundRequest request) {
        // 设置客户端IP
        if (StringUtils.isBlank(request.getClientIp())) {
            request.setClientIp(ServletUtils.getClientIP());
        }

        // 设置默认退款原因
        if (StringUtils.isBlank(request.getReason())) {
            request.setReason("余额退款");
        }
    }

    // ===================== 私有方法 - 参数验证 =====================

    /**
     * 验证支付请求参数
     *
     * 检查必填参数和业务规则
     *
     * @param request 支付请求对象
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    private void validatePaymentRequest(PayRequest request) {
        if (StringUtils.isBlank(request.getOutTradeNo())) {
            throw new IllegalArgumentException("商户订单号不能为空");
        }

        if (request.getTotalFee() == null || request.getTotalFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }

        // 余额支付金额限制检查
        if (request.getTotalFee().compareTo(new BigDecimal("999999")) > 0) {
            throw new IllegalArgumentException("余额支付金额不能超过999999元");
        }
    }

    /**
     * 验证退款请求参数
     *
     * 检查必填参数和业务规则
     *
     * @param request 退款请求对象
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    private void validateRefundRequest(RefundRequest request) {
        if (StringUtils.isBlank(request.getOutTradeNo()) && StringUtils.isBlank(request.getTransactionId())) {
            throw new IllegalArgumentException("原订单号或交易号不能为空");
        }

        if (StringUtils.isBlank(request.getOutRefundNo())) {
            throw new IllegalArgumentException("退款单号不能为空");
        }

        if (request.getRefundFee() == null || request.getRefundFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
    }

    // ===================== 私有方法 - 业务逻辑执行 =====================

    /**
     * 执行余额支付业务逻辑
     *
     * TODO: 此为模拟实现，生产环境需要实现以下功能：
     * 1. 获取当前用户信息
     * 2. 检查用户余额是否足够
     * 3. 扣减用户余额（使用乐观锁）
     * 4. 创建支付记录
     * 5. 更新订单状态
     * 6. 记录余额流水
     *
     * @param request 支付请求参数
     * @return 支付响应结果
     */
    private PayResponse executeBalancePayment(PayRequest request) {
        // TODO: 实现具体的余额支付逻辑
        // 当前为模拟实现，直接返回成功

        log.info("执行余额支付: 订单号={}, 金额={}", request.getOutTradeNo(), request.getTotalFee());

        // 生成交易流水号
        String transactionId = "balance_" + System.currentTimeMillis();

        log.info("余额支付成功: 订单号={}, 交易号={}", request.getOutTradeNo(), transactionId);

        return PayResponse.balance(
            request.getOrderNo(),
            request.getOutTradeNo(),
            DictPaymentMethod.BALANCE.getValue(),
            request.getTotalFee(),
            transactionId
        );
    }

    /**
     * 执行余额退款业务逻辑
     *
     * TODO: 此为模拟实现，生产环境需要实现以下功能：
     * 1. 验证原始支付记录
     * 2. 检查退款金额是否合法
     * 3. 增加用户余额（使用乐观锁）
     * 4. 创建退款记录
     * 5. 更新订单状态
     * 6. 记录余额流水
     *
     * @param request 退款请求参数
     * @return 退款响应结果
     */
    private RefundResponse executeBalanceRefund(RefundRequest request) {
        // TODO: 实现具体的余额退款逻辑
        // 当前为模拟实现，直接返回成功

        log.info("执行余额退款: 退款单号={}, 原订单号={}, 金额={}",
            request.getOutRefundNo(), request.getOutTradeNo(), request.getRefundFee());

        // 生成退款流水号
        String refundId = "balance_refund_" + System.currentTimeMillis();

        log.info("余额退款成功: 退款单号={}, 退款流水号={}", request.getOutRefundNo(), refundId);

        return RefundResponse.builder()
            .success(true)
            .message("余额退款成功")
            .refundId(refundId)
            .outRefundNo(request.getOutRefundNo())
            .outTradeNo(request.getOutTradeNo())
            .refundAmount(request.getRefundFee().toString())
            .refundStatus("SUCCESS")
            .refundTime(String.valueOf(System.currentTimeMillis()))
            .refundAccount("用户余额账户")
            .refundReason(request.getReason())
            .build();
    }
}
