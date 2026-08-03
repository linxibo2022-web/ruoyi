package plus.ruoyi.common.pay.core.handler;

import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;

/**
 * 支付处理器接口
 *
 * 所有支付方式(微信、支付宝、余额等)都需要实现此接口
 *
 * @author 抓蛙师
 */
public interface PayHandler {

    /**
     * 获取支持的支付方式
     *
     * @return 支付方法字典枚举
     */
    DictPaymentMethod getPaymentMethod();

    /**
     * 发起支付
     *
     * @param request 支付请求
     * @return 支付响应
     */
    PayResponse pay(PayRequest request);

    /**
     * 申请退款
     *
     * @param request 退款请求
     * @return 退款响应
     */
    RefundResponse refund(RefundRequest request);

    /**
     * 查询支付状态
     *
     * @param outTradeNo 商户订单号
     * @param appid      应用ID
     * @return 支付响应
     */
    default PayResponse queryPayment(String outTradeNo, String appid) {
        throw new UnsupportedOperationException("该支付方式不支持查询功能");
    }

    /**
     * 查询退款状态
     *
     * @param outRefundNo 商户退款单号
     * @param appid       应用ID
     * @return 退款响应
     */
    default RefundResponse queryRefund(String outRefundNo, String appid) {
        throw new UnsupportedOperationException("该支付方式不支持退款查询功能");
    }

    /**
     * 处理支付回调
     *
     * @param request 回调请求
     * @return 回调响应
     */
    default NotifyResponse handleNotify(NotifyRequest request) {
        throw new UnsupportedOperationException("该支付方式不支持回调处理");
    }
}
