package plus.ruoyi.common.pay.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.core.handler.PayHandler;
import plus.ruoyi.common.pay.exception.PayException;
import plus.ruoyi.common.pay.service.PayService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一支付服务实现
 *
 * 职责:
 * 1. 管理所有支付处理器
 * 2. 根据支付方式路由到对应处理器
 * 3. 提供统一调用入口
 *
 * @author 抓蛙师
 */
@Slf4j
@Service
public class PayServiceImpl implements PayService {

    /**
     * 支付处理器映射表
     */
    private final Map<DictPaymentMethod, PayHandler> handlerMap;

    /**
     * 构造函数 - 自动注入所有支付处理器
     */
    public PayServiceImpl(List<PayHandler> handlers) {
        this.handlerMap = handlers.stream()
            .collect(Collectors.toMap(
                PayHandler::getPaymentMethod,
                Function.identity(),
                (existing, replacement) -> {
                    log.warn("发现重复的支付处理器: {}, 使用最新的: {}",
                        existing.getClass().getSimpleName(),
                        replacement.getClass().getSimpleName());
                    return replacement;
                }
            ));

        log.info("支付处理器初始化完成,支持的支付方式: {}",
            handlerMap.keySet().stream()
                .map(DictPaymentMethod::getLabel)
                .collect(Collectors.toList()));
    }

    @Override
    public PayResponse pay(DictPaymentMethod paymentMethod, PayRequest request) {
        log.info("开始处理{}支付请求: outTradeNo={}",
            paymentMethod.getLabel(), request.getOutTradeNo());

        PayHandler handler = getPaymentHandler(paymentMethod);
        return handler.pay(request);
    }

    @Override
    public RefundResponse refund(DictPaymentMethod paymentMethod, RefundRequest request) {
        log.info("开始处理{}退款请求: outRefundNo={}",
            paymentMethod.getLabel(), request.getOutRefundNo());

        PayHandler handler = getPaymentHandler(paymentMethod);
        return handler.refund(request);
    }

    @Override
    public PayResponse queryPayment(DictPaymentMethod paymentMethod, String outTradeNo, String appid) {
        log.info("开始查询{}支付状态: outTradeNo={}, appid={}",
            paymentMethod.getLabel(), outTradeNo, appid);

        PayHandler handler = getPaymentHandler(paymentMethod);
        return handler.queryPayment(outTradeNo, appid);
    }

    @Override
    public RefundResponse queryRefund(DictPaymentMethod paymentMethod, String outRefundNo, String appid) {
        log.info("开始查询{}退款状态: outRefundNo={}, appid={}",
            paymentMethod.getLabel(), outRefundNo, appid);

        PayHandler handler = getPaymentHandler(paymentMethod);
        return handler.queryRefund(outRefundNo, appid);
    }

    @Override
    public NotifyResponse handleNotify(DictPaymentMethod paymentMethod, NotifyRequest request) {
        log.info("开始处理{}回调请求: mchId={}",
            paymentMethod.getLabel(), request.getMchId());

        PayHandler handler = getPaymentHandler(paymentMethod);
        return handler.handleNotify(request);
    }

    @Override
    public List<DictPaymentMethod> getSupportedPaymentMethods() {
        return handlerMap.keySet().stream()
            .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean isSupported(DictPaymentMethod paymentMethod) {
        return handlerMap.containsKey(paymentMethod);
    }

    /**
     * 获取支付处理器
     */
    private PayHandler getPaymentHandler(DictPaymentMethod paymentMethod) {
        PayHandler handler = handlerMap.get(paymentMethod);
        if (handler == null) {
            throw new PayException("不支持的支付方式: " + paymentMethod.getLabel());
        }
        return handler;
    }
}
