package plus.ruoyi.common.pay.unionpay.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.core.strategy.PayVersionStrategy;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.event.PaySuccessEvent;
import plus.ruoyi.common.pay.exception.PayException;
import plus.ruoyi.common.pay.unionpay.enums.UnionpayTradeType;
import plus.ruoyi.common.pay.unionpay.util.UnionpayHttpUtil;
import plus.ruoyi.common.pay.unionpay.util.UnionpaySignUtil;
import plus.ruoyi.common.pay.utils.PayNotifyUrlBuilder;
import plus.ruoyi.common.pay.utils.QrCodeUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 银联开放平台支付策略
 *
 * 支持功能:
 * 1. 统一支付接口
 * 2. 二维码支付
 * 3. WAP支付
 * 4. 订单查询
 * 5. 退款
 * 6. 支付回调
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class UnionpayStrategy implements PayVersionStrategy {

    private final AppProperties appProperties;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 银联开放平台网关地址
     */
    private static final String GATEWAY_URL = "https://open.unionpay.com/gateway/api/";

    /**
     * 测试环境网关地址
     */
    private static final String TEST_GATEWAY_URL = "https://open.test.unionpay.com/gateway/api/";

    @Override
    public PayResponse executePay(PayRequest request, PayConfig config) {
        try {
            log.info("银联支付请求: outTradeNo={}, tradeType={}",
                request.getOutTradeNo(), request.getTradeType());

            // 根据交易类型路由到不同的支付方式
            UnionpayTradeType tradeType = UnionpayTradeType.getByCode(request.getTradeType());

            return switch (tradeType) {
                case QRCODE -> executeQrcodePay(request, config);    // 二维码支付
                case WAP -> executeWapPay(request, config);          // WAP支付
                case APP -> executeAppPay(request, config);          // APP支付
                case WEB -> executeWebPay(request, config);          // 网关支付
                case JSAPI -> executeJsapiPay(request, config);      // 小程序支付
            };

        } catch (Exception e) {
            log.error("银联支付失败: outTradeNo={}, error={}",
                request.getOutTradeNo(), e.getMessage(), e);
            return PayResponse.fail("银联支付失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse executeRefund(RefundRequest request, PayConfig config) {
        try {
            log.info("银联退款请求: outRefundNo={}, outTradeNo={}",
                request.getOutRefundNo(), request.getOutTradeNo());

            // 构建退款请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appId", config.getAppid());
            params.put("outTradeNo", request.getOutTradeNo());
            params.put("outRefundNo", request.getOutRefundNo());
            params.put("refundAmount", request.getRefundFee().multiply(new BigDecimal("100")).intValue()); // 转为分
            params.put("totalAmount", request.getTotalFee().multiply(new BigDecimal("100")).intValue());
            params.put("refundReason", request.getReason());
            params.put("notifyUrl", buildNotifyUrl(config.getAppid(), "refund"));
            params.put("timestamp", getCurrentTimestamp());
            params.put("nonceStr", generateNonceStr());

            // 签名
            String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
            params.put("sign", sign);

            // 发送请求
            String apiUrl = getGatewayUrl(config) + "trade/refund";
            String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

            // 解析响应
            Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
                responseJson, new TypeReference<>() {});

            // 验签
            if (!verifySign(responseMap, config)) {
                return RefundResponse.builder()
                    .success(false)
                    .message("银联响应验签失败")
                    .outRefundNo(request.getOutRefundNo())
                    .build();
            }

            // 检查退款结果
            String resultCode = (String) responseMap.get("resultCode");
            if ("SUCCESS".equals(resultCode)) {
                return RefundResponse.builder()
                    .success(true)
                    .message("退款成功")
                    .outRefundNo(request.getOutRefundNo())
                    .outTradeNo(request.getOutTradeNo())
                    .refundAmount(String.valueOf(request.getRefundFee()))
                    .refundStatus("SUCCESS")
                    .build();
            } else {
                String errorMsg = (String) responseMap.getOrDefault("resultMsg", "退款失败");
                return RefundResponse.builder()
                    .success(false)
                    .errorCode(resultCode)
                    .message(errorMsg)
                    .outRefundNo(request.getOutRefundNo())
                    .build();
            }

        } catch (Exception e) {
            log.error("银联退款失败: outRefundNo={}, error={}",
                request.getOutRefundNo(), e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("退款失败: " + e.getMessage())
                .outRefundNo(request.getOutRefundNo())
                .build();
        }
    }

    @Override
    public PayResponse queryPayment(String outTradeNo, PayConfig config) {
        try {
            log.info("查询银联支付状态: outTradeNo={}", outTradeNo);

            // 构建查询请求
            Map<String, Object> params = new HashMap<>();
            params.put("appId", config.getAppid());
            params.put("outTradeNo", outTradeNo);
            params.put("timestamp", getCurrentTimestamp());
            params.put("nonceStr", generateNonceStr());

            // 签名
            String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
            params.put("sign", sign);

            // 发送请求
            String apiUrl = getGatewayUrl(config) + "trade/query";
            String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

            // 解析响应
            Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
                responseJson, new TypeReference<>() {});

            // 验签
            if (!verifySign(responseMap, config)) {
                return PayResponse.fail("银联响应验签失败");
            }

            // 返回查询结果
            String tradeState = (String) responseMap.get("tradeState");
            Integer totalAmount = (Integer) responseMap.get("totalAmount");

            return PayResponse.builder()
                .success("SUCCESS".equals(tradeState))
                .message("查询成功")
                .outTradeNo(outTradeNo)
                .transactionId((String) responseMap.get("transactionId"))
                .totalAmount(totalAmount != null ? new BigDecimal(totalAmount).divide(new BigDecimal("100")) : null)
                .tradeState(tradeState)
                .build();

        } catch (Exception e) {
            log.error("查询银联支付失败: outTradeNo={}, error={}", outTradeNo, e.getMessage(), e);
            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, PayConfig config) {
        try {
            log.info("查询银联退款状态: outRefundNo={}", outRefundNo);

            // 构建查询请求
            Map<String, Object> params = new HashMap<>();
            params.put("appId", config.getAppid());
            params.put("outRefundNo", outRefundNo);
            params.put("timestamp", getCurrentTimestamp());
            params.put("nonceStr", generateNonceStr());

            // 签名
            String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
            params.put("sign", sign);

            // 发送请求
            String apiUrl = getGatewayUrl(config) + "trade/refundQuery";
            String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

            // 解析响应
            Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
                responseJson, new TypeReference<>() {});

            // 验签
            if (!verifySign(responseMap, config)) {
                return RefundResponse.builder()
                    .success(false)
                    .message("银联响应验签失败")
                    .outRefundNo(outRefundNo)
                    .build();
            }

            // 返回查询结果
            String refundStatus = (String) responseMap.get("refundStatus");
            Integer refundAmount = (Integer) responseMap.get("refundAmount");

            return RefundResponse.builder()
                .success("SUCCESS".equals(refundStatus))
                .message("查询成功")
                .outRefundNo(outRefundNo)
                .refundAmount(refundAmount != null ? String.valueOf(new BigDecimal(refundAmount).divide(new BigDecimal("100"))) : null)
                .refundStatus(refundStatus)
                .build();

        } catch (Exception e) {
            log.error("查询银联退款失败: outRefundNo={}, error={}", outRefundNo, e.getMessage(), e);

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
            log.info("处理银联支付回调: appid={}", config.getAppid());

            // 获取回调参数
            Map<String, String> formData = request.getFormData();
            Map<String, Object> params = new HashMap<>(formData);

            // 验签
            String sign = params.remove("sign").toString();
            if (!UnionpaySignUtil.verifyWithRsa(params, sign, config.getPlatformCertPath())) {
                log.error("银联回调验签失败: appid={}", config.getAppid());
                return NotifyResponse.fail("验签失败");
            }

            // 检查支付状态
            String tradeState = (String) params.get("tradeState");
            if (!"SUCCESS".equals(tradeState)) {
                log.warn("银联回调支付未成功: outTradeNo={}, tradeState={}",
                    params.get("outTradeNo"), tradeState);
                return NotifyResponse.success("支付未成功已确认");
            }

            // 发布支付成功事件
            publishPaySuccessEvent(params);

            log.info("银联支付回调处理成功: outTradeNo={}, transactionId={}",
                params.get("outTradeNo"), params.get("transactionId"));

            return NotifyResponse.success();

        } catch (Exception e) {
            log.error("处理银联回调异常: error={}", e.getMessage(), e);
            return NotifyResponse.fail("处理失败");
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 二维码支付
     */
    private PayResponse executeQrcodePay(PayRequest request, PayConfig config) throws Exception {
        log.info("执行银联二维码支付: outTradeNo={}", request.getOutTradeNo());

        // 构建请求参数
        Map<String, Object> params = buildCommonParams(request, config);
        params.put("tradeType", "QRCODE");

        // 签名
        String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
        params.put("sign", sign);

        // 发送请求
        String apiUrl = getGatewayUrl(config) + "trade/qrcode";
        String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

        // 解析响应
        Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
            responseJson, new TypeReference<>() {});

        // 验签
        if (!verifySign(responseMap, config)) {
            return PayResponse.fail("银联响应验签失败");
        }

        // 检查结果
        String resultCode = (String) responseMap.get("resultCode");
        if ("SUCCESS".equals(resultCode)) {
            String codeUrl = (String) responseMap.get("qrCode");
            String qrCodeBase64 = QrCodeUtils.generateBase64(codeUrl);

            return PayResponse.builder()
                .success(true)
                .message("二维码生成成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.UNIONPAY.getValue())
                .totalAmount(request.getTotalFee())
                .codeUrl(codeUrl)
                .qrCodeBase64(qrCodeBase64)
                .tradeState("WAIT_PAY")
                .build();
        } else {
            String errorMsg = (String) responseMap.getOrDefault("resultMsg", "下单失败");
            return PayResponse.fail(resultCode, errorMsg);
        }
    }

    /**
     * WAP支付（手机网页）
     */
    private PayResponse executeWapPay(PayRequest request, PayConfig config) throws Exception {
        log.info("执行银联WAP支付: outTradeNo={}", request.getOutTradeNo());

        // 构建请求参数
        Map<String, Object> params = buildCommonParams(request, config);
        params.put("tradeType", "WAP");
        params.put("returnUrl", request.getReturnUrl()); // 前端跳转地址

        // 签名
        String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
        params.put("sign", sign);

        // 发送请求
        String apiUrl = getGatewayUrl(config) + "trade/wap";
        String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

        // 解析响应
        Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
            responseJson, new TypeReference<>() {});

        // 验签
        if (!verifySign(responseMap, config)) {
            return PayResponse.fail("银联响应验签失败");
        }

        // 检查结果
        String resultCode = (String) responseMap.get("resultCode");
        if ("SUCCESS".equals(resultCode)) {
            String payUrl = (String) responseMap.get("payUrl");

            return PayResponse.builder()
                .success(true)
                .message("WAP支付下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.UNIONPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(payUrl) // 跳转URL
                .build();
        } else {
            String errorMsg = (String) responseMap.getOrDefault("resultMsg", "下单失败");
            return PayResponse.fail(resultCode, errorMsg);
        }
    }

    /**
     * APP支付
     */
    private PayResponse executeAppPay(PayRequest request, PayConfig config) throws Exception {
        log.info("执行银联APP支付: outTradeNo={}", request.getOutTradeNo());

        // 构建请求参数
        Map<String, Object> params = buildCommonParams(request, config);
        params.put("tradeType", "APP");

        // 签名
        String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
        params.put("sign", sign);

        // 发送请求
        String apiUrl = getGatewayUrl(config) + "trade/app";
        String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

        // 解析响应
        Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
            responseJson, new TypeReference<>() {});

        // 验签
        if (!verifySign(responseMap, config)) {
            return PayResponse.fail("银联响应验签失败");
        }

        // 检查结果
        String resultCode = (String) responseMap.get("resultCode");
        if ("SUCCESS".equals(resultCode)) {
            String payInfo = (String) responseMap.get("payInfo");

            return PayResponse.builder()
                .success(true)
                .message("APP支付下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.UNIONPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(payInfo) // APP调起参数
                .build();
        } else {
            String errorMsg = (String) responseMap.getOrDefault("resultMsg", "下单失败");
            return PayResponse.fail(resultCode, errorMsg);
        }
    }

    /**
     * 网关支付（PC）
     */
    private PayResponse executeWebPay(PayRequest request, PayConfig config) throws Exception {
        log.info("执行银联网关支付: outTradeNo={}", request.getOutTradeNo());

        // 构建请求参数
        Map<String, Object> params = buildCommonParams(request, config);
        params.put("tradeType", "WEB");
        params.put("returnUrl", request.getReturnUrl());

        // 签名
        String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
        params.put("sign", sign);

        // 发送请求
        String apiUrl = getGatewayUrl(config) + "trade/web";
        String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

        // 解析响应
        Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
            responseJson, new TypeReference<>() {});

        // 验签
        if (!verifySign(responseMap, config)) {
            return PayResponse.fail("银联响应验签失败");
        }

        // 检查结果
        String resultCode = (String) responseMap.get("resultCode");
        if ("SUCCESS".equals(resultCode)) {
            String payUrl = (String) responseMap.get("payUrl");

            return PayResponse.builder()
                .success(true)
                .message("网关支付下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.UNIONPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(payUrl)
                .build();
        } else {
            String errorMsg = (String) responseMap.getOrDefault("resultMsg", "下单失败");
            return PayResponse.fail(resultCode, errorMsg);
        }
    }

    /**
     * 小程序支付（JSAPI）
     */
    private PayResponse executeJsapiPay(PayRequest request, PayConfig config) throws Exception {
        log.info("执行银联小程序支付: outTradeNo={}", request.getOutTradeNo());

        // 构建请求参数
        Map<String, Object> params = buildCommonParams(request, config);
        params.put("tradeType", "JSAPI");
        params.put("openId", request.getOpenId()); // 小程序openId

        // 签名
        String sign = UnionpaySignUtil.signWithRsa(params, config.getCertPath());
        params.put("sign", sign);

        // 发送请求
        String apiUrl = getGatewayUrl(config) + "trade/jsapi";
        String responseJson = UnionpayHttpUtil.postJson(apiUrl, params, buildHeaders(config));

        // 解析响应
        Map<String, Object> responseMap = OBJECT_MAPPER.readValue(
            responseJson, new TypeReference<>() {});

        // 验签
        if (!verifySign(responseMap, config)) {
            return PayResponse.fail("银联响应验签失败");
        }

        // 检查结果
        String resultCode = (String) responseMap.get("resultCode");
        if ("SUCCESS".equals(resultCode)) {
            String payInfo = (String) responseMap.get("payInfo");

            return PayResponse.builder()
                .success(true)
                .message("小程序支付下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.UNIONPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(payInfo)
                .build();
        } else {
            String errorMsg = (String) responseMap.getOrDefault("resultMsg", "下单失败");
            return PayResponse.fail(resultCode, errorMsg);
        }
    }

    /**
     * 构建通用请求参数
     */
    private Map<String, Object> buildCommonParams(PayRequest request, PayConfig config) {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", config.getAppid());
        params.put("outTradeNo", request.getOutTradeNo());
        params.put("totalAmount", request.getTotalFee().multiply(new BigDecimal("100")).intValue()); // 转为分
        params.put("subject", request.getBody());
        params.put("body", request.getDetail());
        params.put("notifyUrl", buildNotifyUrl(config.getAppid(), "pay"));
        params.put("timestamp", getCurrentTimestamp());
        params.put("nonceStr", generateNonceStr());

        return params;
    }

    /**
     * 构建请求头
     */
    private Map<String, String> buildHeaders(PayConfig config) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Authorization", "Bearer " + config.getMchKey()); // AppSecret
        return headers;
    }

    /**
     * 验证签名
     */
    private boolean verifySign(Map<String, Object> params, PayConfig config) {
        String sign = (String) params.get("sign");
        if (sign == null) {
            log.error("银联响应缺少签名字段");
            return false;
        }

        return UnionpaySignUtil.verifyWithRsa(params, sign, config.getPlatformCertPath());
    }

    /**
     * 构建回调地址
     */
    private String buildNotifyUrl(String appid, String type) {
        String baseApi = appProperties.getBaseApi();
        return String.format("%s/payment/notify/unionpay/%s/%s", baseApi, appid, type);
    }

    /**
     * 获取网关地址
     */
    private String getGatewayUrl(PayConfig config) {
        // 如果配置了自定义网关，使用自定义网关
        // 否则根据租户ID判断测试/生产环境
        return GATEWAY_URL; // 默认生产环境
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 发布支付成功事件
     */
    private void publishPaySuccessEvent(Map<String, Object> params) {
        try {
            Integer totalAmount = (Integer) params.get("totalAmount");

            PaySuccessEvent event = PaySuccessEvent.builder()
                .source(this)
                .outTradeNo((String) params.get("outTradeNo"))
                .transactionId((String) params.get("transactionId"))
                .totalFee(totalAmount != null ? String.valueOf(new BigDecimal(totalAmount).divide(new BigDecimal("100"))) : null)
                .mchId((String) params.get("mchId"))
                .appId((String) params.get("appId"))
                .openId((String) params.get("openId"))
                .paymentMethod(DictPaymentMethod.UNIONPAY.getValue())
                .build();

            SpringUtils.context().publishEvent(event);

            log.debug("银联支付成功事件已发布: outTradeNo={}", params.get("outTradeNo"));

        } catch (Exception e) {
            log.error("发布银联支付成功事件失败: outTradeNo={}, error={}",
                params.get("outTradeNo"), e.getMessage(), e);
        }
    }
}
