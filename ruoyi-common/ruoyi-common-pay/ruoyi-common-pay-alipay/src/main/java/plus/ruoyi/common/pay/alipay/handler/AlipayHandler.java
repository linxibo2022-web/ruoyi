package plus.ruoyi.common.pay.alipay.handler;

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

/**
 * 支付宝支付处理器
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class AlipayHandler implements PayHandler {

    private final PayConfigManager configManager;
    private final AlipayStrategy alipayStrategy;

    @Override
    public DictPaymentMethod getPaymentMethod() {
        return DictPaymentMethod.ALIPAY;
    }

    @Override
    public PayResponse pay(PayRequest request) {
        try {
            log.info("支付宝支付请求: outTradeNo={}, tradeType={}",
                request.getOutTradeNo(), request.getTradeType());

            PayConfig config = getConfigByAppid(request.getAppid());

            PayResponse response = alipayStrategy.executePay(request, config);

            log.info("支付宝支付完成: outTradeNo={}, success={}",
                request.getOutTradeNo(), response.isSuccess());

            return response;
        } catch (Exception e) {
            log.error("支付宝支付失败: outTradeNo={}, error={}",
                request.getOutTradeNo(), e.getMessage(), e);
            return PayResponse.fail("支付宝支付失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            log.info("支付宝退款请求: outRefundNo={}, outTradeNo={}",
                request.getOutRefundNo(), request.getOutTradeNo());

            PayConfig config = getConfigByAppid(request.getAppid());

            RefundResponse response = alipayStrategy.executeRefund(request, config);

            log.info("支付宝退款完成: outRefundNo={}, success={}",
                request.getOutRefundNo(), response.isSuccess());

            return response;
        } catch (Exception e) {
            log.error("支付宝退款失败: outRefundNo={}, error={}",
                request.getOutRefundNo(), e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("支付宝退款失败: " + e.getMessage())
                .outRefundNo(request.getOutRefundNo())
                .build();
        }
    }

    @Override
    public PayResponse queryPayment(String outTradeNo, String appid) {
        try {
            log.info("查询支付宝支付: outTradeNo={}, appid={}", outTradeNo, appid);

            PayConfig config = getConfigByAppid(appid);

            return alipayStrategy.queryPayment(outTradeNo, config);

        } catch (Exception e) {
            log.error("查询支付宝支付失败: outTradeNo={}, error={}",
                outTradeNo, e.getMessage(), e);

            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, String appid) {
        try {
            log.info("查询支付宝退款: outRefundNo={}, appid={}", outRefundNo, appid);

            PayConfig config = getConfigByAppid(appid);

            return alipayStrategy.queryRefund(outRefundNo, config);

        } catch (Exception e) {
            log.error("查询支付宝退款失败: outRefundNo={}, error={}",
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
            // 回调URL格式: /payment/notify/alipay/{appid}
            String appid = request.getMchId();
            log.info("处理支付宝支付回调: appid={}", appid);

            PayConfig config = getConfigByAppid(appid);

            return alipayStrategy.handleNotify(request, config);

        } catch (Exception e) {
            log.error("处理支付宝回调失败: error={}", e.getMessage(), e);
            return NotifyResponse.fail("处理失败");
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 根据appid获取配置
     * 如果appid为空，返回支付宝支付的默认配置
     */
    private PayConfig getConfigByAppid(String appid) {
        try {
            // 如果appid为空，使用支付方式获取默认配置
            if (appid == null || appid.trim().isEmpty()) {
                log.info("未指定appid,使用支付宝支付的默认配置");
                PayConfig config = configManager.getConfig(getPaymentMethod().getValue(), null);
                if (config == null) {
                    throw new PayException("未找到支付宝支付的默认配置");
                }
                log.info("使用支付宝默认配置: appid={}", config.getAppid());
                return config;
            }
            return configManager.getConfigByAppid(appid);
        } catch (Exception e) {
            throw new PayException("未找到支付宝支付配置: appid=" + appid, e);
        }
    }
}
