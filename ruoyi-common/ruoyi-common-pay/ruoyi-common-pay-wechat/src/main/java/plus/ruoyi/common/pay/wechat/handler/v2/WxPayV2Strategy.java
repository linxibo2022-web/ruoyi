package plus.ruoyi.common.pay.wechat.handler.v2;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.utils.PayUtils;
import plus.ruoyi.common.pay.wechat.adapter.WxPayV2NotifyResultAdapter;
import plus.ruoyi.common.pay.wechat.converter.WxPayConverter;
import plus.ruoyi.common.pay.wechat.handler.AbstractWxPayStrategy;
import plus.ruoyi.common.pay.wechat.registry.WxPayServiceRegistry;

/**
 * 微信支付 v2 策略实现
 *
 * 基于 wxjava 实现微信支付v2 API
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
public class WxPayV2Strategy extends AbstractWxPayStrategy {

    public WxPayV2Strategy(WxPayServiceRegistry registry, AppProperties appProperties) {
        super(registry, appProperties);
    }

    @Override
    public PayResponse executePay(PayRequest request, PayConfig config) {
        try {
            log.info("执行微信v2支付: appid={}, outTradeNo={}",
                config.getAppid(), request.getOutTradeNo());

            // 1. 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // 2. 构建回调地址 (使用基类方法)
            String notifyUrl = buildNotifyUrl(config.getAppid());

            // 3. 转换请求对象
            WxPayUnifiedOrderRequest wxRequest = WxPayConverter.toWxV2Request(request, notifyUrl);

            // 4. 调用wxjava统一下单
            WxPayUnifiedOrderResult wxResult = wxPayService.unifiedOrder(wxRequest);

            // 5. 转换响应对象 - 传入wxPayService用于生成签名
            PayResponse response = WxPayConverter.toPayResponse(wxResult, request, wxPayService);

            log.info("微信v2支付成功: outTradeNo={}, prepayId={}",
                request.getOutTradeNo(), wxResult.getPrepayId());

            return response;

        } catch (WxPayException e) {
            return handlePayException(e, request.getOutTradeNo(), "v2支付");

        } catch (Exception e) {
            return handlePayGeneralException(e, request.getOutTradeNo(), "v2支付");
        }
    }

    @Override
    public RefundResponse executeRefund(RefundRequest request, PayConfig config) {
        try {
            log.info("执行微信v2退款: appid={}, outRefundNo={}",
                config.getAppid(), request.getOutRefundNo());

            // 1. 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // 2. 构建退款请求
            WxPayRefundRequest wxRequest = new WxPayRefundRequest();
            wxRequest.setOutTradeNo(request.getOutTradeNo());
            wxRequest.setOutRefundNo(request.getOutRefundNo());
            wxRequest.setTotalFee(PayUtils.yuanToFen(request.getTotalFee()));
            wxRequest.setRefundFee(PayUtils.yuanToFen(request.getRefundFee()));
            wxRequest.setRefundDesc(request.getReason());

            // 3. 调用wxjava退款接口
            WxPayRefundResult wxResult = wxPayService.refund(wxRequest);

            // 4. 构建退款响应
            return RefundResponse.builder()
                .success(true)
                .message("退款申请成功")
                .refundId(wxResult.getRefundId())
                .outRefundNo(wxResult.getOutRefundNo())
                .outTradeNo(wxResult.getOutTradeNo())
                .refundAmount(PayUtils.fenToYuan(wxResult.getRefundFee()).toString())
                .refundStatus("PROCESSING")
                .build();

        } catch (WxPayException e) {
            return handleRefundException(e, request.getOutRefundNo(), request.getOutTradeNo(), "v2退款");

        } catch (Exception e) {
            return handleRefundGeneralException(e, request.getOutRefundNo(), "v2退款");
        }
    }

    @Override
    public PayResponse queryPayment(String outTradeNo, PayConfig config) {
        try {
            log.info("查询微信v2支付状态: appid={}, outTradeNo={}",
                config.getAppid(), outTradeNo);

            // 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // wxjava的查询订单接口
            var result = wxPayService.queryOrder(null, outTradeNo);

            return PayResponse.builder()
                .success(true)
                .message("查询成功")
                .outTradeNo(result.getOutTradeNo())
                .transactionId(result.getTransactionId())
                .totalAmount(PayUtils.fenToYuan(result.getTotalFee()))
                .tradeState(result.getTradeState())
                .build();

        } catch (Exception e) {
            log.error("查询微信v2支付状态失败: outTradeNo={}, error={}",
                outTradeNo, e.getMessage(), e);

            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, PayConfig config) {
        try {
            log.info("查询微信v2退款状态: appid={}, outRefundNo={}",
                config.getAppid(), outRefundNo);

            // 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // wxjava的查询退款接口
            var result = wxPayService.refundQuery(null, null, outRefundNo, null);

            return RefundResponse.builder()
                .success(true)
                .message("查询成功")
                .outRefundNo(outRefundNo)
                .refundStatus("SUCCESS")
                .build();

        } catch (Exception e) {
            log.error("查询微信v2退款状态失败: outRefundNo={}, error={}",
                outRefundNo, e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("查询失败: " + e.getMessage())
                .outRefundNo(outRefundNo)
                .build();
        }
    }

    @Override
    public NotifyResponse handleNotify(NotifyRequest request, PayConfig config) {
        try {
            log.info("处理微信v2支付回调: appid={}", config.getAppid());

            // 1. 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // 2. 解析回调数据(wxjava自动验签)
            String xmlData = request.getXmlData();
            WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xmlData);

            // 3. 检查支付状态
            if (!"SUCCESS".equals(result.getResultCode())) {
                log.warn("微信v2回调支付失败: outTradeNo={}, resultCode={}",
                    result.getOutTradeNo(), result.getResultCode());
                return NotifyResponse.success("支付失败已确认");
            }

            // 4. 发布支付成功事件 (使用基类的统一方法)
            publishPaySuccessEvent(new WxPayV2NotifyResultAdapter(result));

            log.info("微信v2支付回调处理成功: outTradeNo={}, transactionId={}",
                result.getOutTradeNo(), result.getTransactionId());

            return NotifyResponse.success();

        } catch (WxPayException e) {
            return handleNotifyException(e, "v2");

        } catch (Exception e) {
            return handleNotifyGeneralException(e, "v2");
        }
    }
}
