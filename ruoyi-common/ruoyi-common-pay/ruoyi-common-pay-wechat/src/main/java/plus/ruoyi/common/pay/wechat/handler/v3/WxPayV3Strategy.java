package plus.ruoyi.common.pay.wechat.handler.v3;

import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
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
import plus.ruoyi.common.pay.wechat.adapter.WxPayV3NotifyResultAdapter;
import plus.ruoyi.common.pay.wechat.converter.WxPayConverter;
import plus.ruoyi.common.pay.wechat.handler.AbstractWxPayStrategy;
import plus.ruoyi.common.pay.wechat.registry.WxPayServiceRegistry;

import java.util.Map;

/**
 * 微信支付 v3 策略实现
 *
 * 基于 wxjava 实现微信支付v3 API
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
public class WxPayV3Strategy extends AbstractWxPayStrategy {

    public WxPayV3Strategy(WxPayServiceRegistry registry, AppProperties appProperties) {
        super(registry, appProperties);
    }

    @Override
    public PayResponse executePay(PayRequest request, PayConfig config) {
        try {
            log.info("执行微信v3支付: appid={}, outTradeNo={}",
                config.getAppid(), request.getOutTradeNo());

            // 1. 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // 2. 构建回调地址 (使用基类方法)
            String notifyUrl = buildNotifyUrl(config.getAppid());

            // 3. 转换请求对象
            WxPayUnifiedOrderV3Request wxRequest = WxPayConverter.toWxV3Request(request, notifyUrl);

            // 4. 直接调用wxjava SDK，返回值根据支付类型不同而不同
            Object result = executeV3PayByTradeType(
                wxPayService, wxRequest, request.getTradeType()
            );

            // 5. 转换响应 (使用 Converter 统一处理)
            PayResponse response = WxPayConverter.toPayResponseV3(result, request);

            log.info("微信v3支付成功: outTradeNo={}", request.getOutTradeNo());

            return response;

        } catch (WxPayException e) {
            return handlePayException(e, request.getOutTradeNo(), "v3支付");

        } catch (Exception e) {
            return handlePayGeneralException(e, request.getOutTradeNo(), "v3支付");
        }
    }

    @Override
    public RefundResponse executeRefund(RefundRequest request, PayConfig config) {
        try {
            log.info("执行微信v3退款: appid={}, outRefundNo={}",
                config.getAppid(), request.getOutRefundNo());

            // 1. 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // 2. 构建退款请求
            WxPayRefundV3Request wxRequest = new WxPayRefundV3Request();
            wxRequest.setOutTradeNo(request.getOutTradeNo());
            wxRequest.setOutRefundNo(request.getOutRefundNo());

            // v3使用金额对象
            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            amount.setTotal(PayUtils.yuanToFen(request.getTotalFee()));
            amount.setRefund(PayUtils.yuanToFen(request.getRefundFee()));
            amount.setCurrency("CNY");
            wxRequest.setAmount(amount);

            wxRequest.setReason(request.getReason());

            // 3. 调用wxjava退款接口
            WxPayRefundV3Result wxResult = wxPayService.refundV3(wxRequest);

            // 4. 构建退款响应
            return RefundResponse.builder()
                .success(true)
                .message("退款申请成功")
                .refundId(wxResult.getRefundId())
                .outRefundNo(wxResult.getOutRefundNo())
                .outTradeNo(request.getOutTradeNo())
                .refundAmount(PayUtils.fenToYuan(wxResult.getAmount().getRefund()).toString())
                .refundStatus(wxResult.getStatus())
                .build();

        } catch (WxPayException e) {
            return handleRefundException(e, request.getOutRefundNo(), request.getOutTradeNo(), "v3退款");

        } catch (Exception e) {
            return handleRefundGeneralException(e, request.getOutRefundNo(), "v3退款");
        }
    }

    @Override
    public PayResponse queryPayment(String outTradeNo, PayConfig config) {
        try {
            log.info("查询微信v3支付状态: appid={}, outTradeNo={}",
                config.getAppid(), outTradeNo);

            // 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // wxjava的v3查询订单接口
            var result = wxPayService.queryOrderV3(null, outTradeNo);

            return PayResponse.builder()
                .success(true)
                .message("查询成功")
                .outTradeNo(result.getOutTradeNo())
                .transactionId(result.getTransactionId())
                .totalAmount(PayUtils.fenToYuan(result.getAmount().getTotal()))
                .tradeState(result.getTradeState()) // v3的tradeState直接是String类型
                .build();

        } catch (Exception e) {
            log.error("查询微信v3支付状态失败: outTradeNo={}, error={}",
                outTradeNo, e.getMessage(), e);

            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, PayConfig config) {
        try {
            log.info("查询微信v3退款状态: appid={}, outRefundNo={}",
                config.getAppid(), outRefundNo);

            // 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // wxjava的v3查询退款接口
            var result = wxPayService.refundQueryV3(outRefundNo);

            return RefundResponse.builder()
                .success(true)
                .message("查询成功")
                .outRefundNo(result.getOutRefundNo())
                .refundId(result.getRefundId())
                .refundStatus(result.getStatus())
                .refundAmount(PayUtils.fenToYuan(result.getAmount().getRefund()).toString())
                .build();

        } catch (Exception e) {
            log.error("查询微信v3退款状态失败: outRefundNo={}, error={}",
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
            log.info("处理微信v3支付回调: appid={}", config.getAppid());

            // 1. 获取 WxPayService (使用基类方法)
            WxPayService wxPayService = getWxPayService(config.getAppid());

            // 2. 获取请求头中的签名信息
            Map<String, String> headers = request.getHeaders();
            if (headers == null || headers.isEmpty()) {
                log.error("微信v3回调缺少请求头信息");
                return NotifyResponse.fail("缺少请求头信息");
            }

            // 3. 构建 SignatureHeader 对象
            // wxjava SDK 使用 SignatureHeader 来验证微信回调的签名
            SignatureHeader signatureHeader = SignatureHeader.builder()
                .timeStamp(headers.get("Wechatpay-Timestamp"))
                .nonce(headers.get("Wechatpay-Nonce"))
                .serial(headers.get("Wechatpay-Serial"))
                .signature(headers.get("Wechatpay-Signature"))
                .build();

            // 4. 解析回调数据(wxjava自动验签和解密)
            String jsonData = request.getJsonData();
            var notifyResult = wxPayService.parseOrderNotifyV3Result(jsonData, signatureHeader);
            var decryptResult = notifyResult.getResult(); // DecryptNotifyResult类型

            // 4. 检查支付状态
            if (!"SUCCESS".equals(decryptResult.getTradeState())) {
                log.warn("微信v3回调支付失败: outTradeNo={}, tradeState={}",
                    decryptResult.getOutTradeNo(), decryptResult.getTradeState());
                return NotifyResponse.success("支付失败已确认");
            }

            // 5. 发布支付成功事件 (使用基类的统一方法)
            publishPaySuccessEvent(new WxPayV3NotifyResultAdapter(decryptResult));

            log.info("微信v3支付回调处理成功: outTradeNo={}, transactionId={}",
                decryptResult.getOutTradeNo(), decryptResult.getTransactionId());

            return NotifyResponse.success();

        } catch (WxPayException e) {
            return handleNotifyException(e, "v3");

        } catch (Exception e) {
            return handleNotifyGeneralException(e, "v3");
        }
    }

    /**
     * 根据交易类型执行不同的v3支付接口
     *
     * wxjava SDK对不同支付类型返回不同的类型:
     * - NATIVE: 返回 String (code_url)
     * - JSAPI: 返回 WxPayUnifiedOrderV3Result.JsapiResult (包含调起支付的参数)
     * - APP: 返回 WxPayUnifiedOrderV3Result.AppResult (包含调起支付的参数)
     * - H5: 返回 WxPayUnifiedOrderV3Result (包含h5Url)
     */
    private Object executeV3PayByTradeType(
        WxPayService wxPayService,
        WxPayUnifiedOrderV3Request request,
        String tradeType) throws WxPayException {

        // 调用对应的支付接口
        return switch (tradeType) {
            case "NATIVE" -> wxPayService.createOrderV3(TradeTypeEnum.NATIVE, request);
            case "JSAPI" -> wxPayService.createOrderV3(TradeTypeEnum.JSAPI, request);
            case "APP" -> wxPayService.createOrderV3(TradeTypeEnum.APP, request);
            case "MWEB" -> wxPayService.createOrderV3(TradeTypeEnum.H5, request);
            default -> throw new plus.ruoyi.common.pay.exception.WxPayException(
                "不支持的交易类型: " + tradeType
            );
        };
    }
}
