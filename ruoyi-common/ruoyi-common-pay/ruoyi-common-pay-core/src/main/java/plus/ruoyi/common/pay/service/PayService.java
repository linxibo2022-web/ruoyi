package plus.ruoyi.common.pay.service;

import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;

import java.util.List;

/**
 * 统一支付服务接口
 *
 * 对外提供统一的支付服务入口
 *
 * @author 抓蛙师
 */
public interface PayService {

    /**
     * 发起支付
     *
     * @param paymentMethod 支付方式
     * @param request       支付请求
     * @return 支付响应
     */
    PayResponse pay(DictPaymentMethod paymentMethod, PayRequest request);

    /**
     * 申请退款
     *
     * @param paymentMethod 支付方式
     * @param request       退款请求
     * @return 退款响应
     */
    RefundResponse refund(DictPaymentMethod paymentMethod, RefundRequest request);

    /**
     * 查询支付状态
     *
     * @param paymentMethod 支付方式
     * @param outTradeNo    商户订单号
     * @param appid         应用ID
     * @return 支付状态响应
     */
    PayResponse queryPayment(DictPaymentMethod paymentMethod, String outTradeNo, String appid);

    /**
     * 查询退款状态
     *
     * @param paymentMethod 支付方式
     * @param outRefundNo   商户退款单号
     * @param appid         应用ID
     * @return 退款状态响应
     */
    RefundResponse queryRefund(DictPaymentMethod paymentMethod, String outRefundNo, String appid);

    /**
     * 处理支付回调
     *
     * @param paymentMethod 支付方式
     * @param request       回调请求
     * @return 回调响应
     */
    NotifyResponse handleNotify(DictPaymentMethod paymentMethod, NotifyRequest request);

    /**
     * 获取支持的支付方式列表
     *
     * @return 支付方式列表
     */
    List<DictPaymentMethod> getSupportedPaymentMethods();

    /**
     * 检查是否支持指定支付方式
     *
     * @param paymentMethod 支付方式
     * @return 是否支持
     */
    boolean isSupported(DictPaymentMethod paymentMethod);
}
