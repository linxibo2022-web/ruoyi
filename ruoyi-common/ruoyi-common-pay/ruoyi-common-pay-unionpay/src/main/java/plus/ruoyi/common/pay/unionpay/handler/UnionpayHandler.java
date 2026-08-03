package plus.ruoyi.common.pay.unionpay.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.config.PayConfigManager;
import plus.ruoyi.common.pay.core.handler.PayHandler;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.exception.PayException;
import plus.ruoyi.common.pay.unionpay.strategy.UnionpayStrategy;

/**
 * 银联支付处理器
 *
 * 负责:
 * 1. 实现 PayHandler 接口
 * 2. 获取支付配置
 * 3. 调用 UnionpayStrategy 执行具体支付逻辑
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class UnionpayHandler implements PayHandler {

    private final PayConfigManager configManager;
    private final UnionpayStrategy unionpayStrategy;

    @Override
    public DictPaymentMethod getPaymentMethod() {
        return DictPaymentMethod.UNIONPAY;
    }

    @Override
    public PayResponse pay(PayRequest request) {
        try {
            log.info("银联支付请求: outTradeNo={}, tradeType={}",
                request.getOutTradeNo(), request.getTradeType());

            // 获取支付配置
            PayConfig config = getConfigByAppid(request.getAppid());

            // 执行支付
            PayResponse response = unionpayStrategy.executePay(request, config);

            log.info("银联支付完成: outTradeNo={}, success={}",
                request.getOutTradeNo(), response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("银联支付失败: outTradeNo={}, error={}",
                request.getOutTradeNo(), e.getMessage(), e);
            return PayResponse.fail("银联支付失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            log.info("银联退款请求: outRefundNo={}, outTradeNo={}",
                request.getOutRefundNo(), request.getOutTradeNo());

            // 获取支付配置
            PayConfig config = getConfigByAppid(request.getAppid());

            // 执行退款
            RefundResponse response = unionpayStrategy.executeRefund(request, config);

            log.info("银联退款完成: outRefundNo={}, success={}",
                request.getOutRefundNo(), response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("银联退款失败: outRefundNo={}, error={}",
                request.getOutRefundNo(), e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("银联退款失败: " + e.getMessage())
                .outRefundNo(request.getOutRefundNo())
                .build();
        }
    }

    @Override
    public PayResponse queryPayment(String outTradeNo, String appid) {
        try {
            log.info("查询银联支付: outTradeNo={}, appid={}", outTradeNo, appid);

            // 获取支付配置
            PayConfig config = getConfigByAppid(appid);

            // 查询支付状态
            return unionpayStrategy.queryPayment(outTradeNo, config);

        } catch (Exception e) {
            log.error("查询银联支付失败: outTradeNo={}, error={}",
                outTradeNo, e.getMessage(), e);

            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, String appid) {
        try {
            log.info("查询银联退款: outRefundNo={}, appid={}", outRefundNo, appid);

            // 获取支付配置
            PayConfig config = getConfigByAppid(appid);

            // 查询退款状态
            return unionpayStrategy.queryRefund(outRefundNo, config);

        } catch (Exception e) {
            log.error("查询银联退款失败: outRefundNo={}, error={}",
                outRefundNo, e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("查询失败: " + e.getMessage())
                .outRefundNo(outRefundNo)
                .build();
        }
    }

    @Override
    public NotifyResponse handleNotify(NotifyRequest request) {
        try {
            // NotifyRequest.mchId 实际存储的是 appid (由回调URL路径传入)
            // 回调URL格式: /payment/notify/unionpay/{appid}/pay
            String appid = request.getMchId();
            log.info("处理银联支付回调: appid={}", appid);

            // 获取支付配置
            PayConfig config = getConfigByAppid(appid);

            // 处理回调
            return unionpayStrategy.handleNotify(request, config);

        } catch (Exception e) {
            log.error("处理银联回调失败: error={}", e.getMessage(), e);
            return NotifyResponse.fail("处理失败");
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 根据appid获取配置
     * 如果appid为空，返回银联支付的默认配置
     */
    private PayConfig getConfigByAppid(String appid) {
        try {
            // 如果appid为空，使用支付方式获取默认配置
            if (appid == null || appid.trim().isEmpty()) {
                log.info("未指定appid,使用银联支付的默认配置");
                PayConfig config = configManager.getConfig(getPaymentMethod().getValue(), null);
                if (config == null) {
                    throw new PayException("未找到银联支付的默认配置");
                }
                log.info("使用银联默认配置: appid={}", config.getAppid());
                return config;
            }
            return configManager.getConfigByAppid(appid);
        } catch (Exception e) {
            throw new PayException("未找到银联支付配置: appid=" + appid, e);
        }
    }
}
