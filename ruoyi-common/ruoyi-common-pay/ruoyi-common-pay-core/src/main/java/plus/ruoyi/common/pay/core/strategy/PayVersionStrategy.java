package plus.ruoyi.common.pay.core.strategy;

import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;

/**
 * 支付版本策略接口
 *
 * 用于统一 v2 和 v3 的操作规范
 * 不同版本的API实现此接口
 *
 * @author 抓蛙师
 */
public interface PayVersionStrategy {

    /**
     * 执行支付
     *
     * @param request 支付请求
     * @param config  支付配置
     * @return 支付响应
     */
    PayResponse executePay(PayRequest request, PayConfig config);

    /**
     * 执行退款
     *
     * @param request 退款请求
     * @param config  支付配置
     * @return 退款响应
     */
    RefundResponse executeRefund(RefundRequest request, PayConfig config);

    /**
     * 查询支付
     *
     * @param outTradeNo 商户订单号
     * @param config     支付配置
     * @return 支付响应
     */
    PayResponse queryPayment(String outTradeNo, PayConfig config);

    /**
     * 查询退款
     *
     * @param outRefundNo 商户退款单号
     * @param config      支付配置
     * @return 退款响应
     */
    RefundResponse queryRefund(String outRefundNo, PayConfig config);

    /**
     * 处理回调通知
     *
     * @param request 回调请求
     * @param config  支付配置
     * @return 回调响应
     */
    NotifyResponse handleNotify(NotifyRequest request, PayConfig config);
}
