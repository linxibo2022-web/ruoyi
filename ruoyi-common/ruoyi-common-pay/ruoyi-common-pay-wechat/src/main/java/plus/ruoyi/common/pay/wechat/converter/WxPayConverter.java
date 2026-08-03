package plus.ruoyi.common.pay.wechat.converter;

import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.util.SignUtils;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.utils.PayUtils;
import plus.ruoyi.common.pay.utils.QrCodeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付对象转换器
 *
 * 将统一的 PayRequest/PayResponse 与 wxjava 对象互转
 *
 * @author 抓蛙师
 */
@Slf4j
public class WxPayConverter {

    /**
     * 将 PayRequest 转换为 WxPayUnifiedOrderRequest(v2)
     */
    public static WxPayUnifiedOrderRequest toWxV2Request(PayRequest request, String notifyUrl) {
        WxPayUnifiedOrderRequest wxRequest = new WxPayUnifiedOrderRequest();

        // 基础信息
        wxRequest.setOutTradeNo(request.getOutTradeNo());
        wxRequest.setBody(request.getBody());
        wxRequest.setDetail(request.getDetail());
        wxRequest.setTotalFee(PayUtils.yuanToFen(request.getTotalFee())); // 元转分
        wxRequest.setSpbillCreateIp(request.getClientIp());
        wxRequest.setTradeType(request.getTradeType());

        // JSAPI需要openid
        if ("JSAPI".equals(request.getTradeType())) {
            wxRequest.setOpenid(request.getOpenId());
        }

        // NATIVE需要product_id (扫码支付必填字段)
        if ("NATIVE".equals(request.getTradeType())) {
            // 使用订单号作为product_id (微信v2要求，商品ID由商户定义)
            wxRequest.setProductId(request.getOutTradeNo());
        }

        // 回调地址
        wxRequest.setNotifyUrl(notifyUrl);

        // 可选参数
        if (StringUtils.isNotBlank(request.getAttach())) {
            wxRequest.setAttach(request.getAttach());
        }

        if (StringUtils.isNotBlank(request.getTimeStart())) {
            wxRequest.setTimeStart(request.getTimeStart());
        } else {
            wxRequest.setTimeStart(PayUtils.generateTimeStart());
        }

        if (StringUtils.isNotBlank(request.getTimeExpire())) {
            wxRequest.setTimeExpire(request.getTimeExpire());
        } else {
            wxRequest.setTimeExpire(PayUtils.generateTimeExpire(30)); // 默认30分钟
        }

        return wxRequest;
    }

    /**
     * 将 WxPayUnifiedOrderResult 转换为 PayResponse (v2)
     *
     * @param wxResult wxjava统一下单结果
     * @param request 原始支付请求
     * @param wxPayService 微信支付服务(用于生成JSAPI签名)
     * @return PayResponse
     */
    public static PayResponse toPayResponse(WxPayUnifiedOrderResult wxResult, PayRequest request, WxPayService wxPayService) {
        if (wxResult == null) {
            return PayResponse.fail("微信支付返回结果为空");
        }

        String tradeType = request.getTradeType();

        // JSAPI支付 - 手动构建签名参数
        if ("JSAPI".equals(tradeType)) {
            try {
                // 构建小程序/公众号支付所需参数
                String timeStamp = String.valueOf(PayUtils.generateTimestamp());
                String nonceStr = PayUtils.generateNonceStr();
                String packageValue = "prepay_id=" + wxResult.getPrepayId();

                Map<String, String> payInfo = new HashMap<>();
                payInfo.put("appId", wxResult.getAppid());
                payInfo.put("timeStamp", timeStamp);
                payInfo.put("nonceStr", nonceStr);
                payInfo.put("package", packageValue);
                payInfo.put("signType", WxPayConstants.SignType.MD5);

                // 使用wxjava的SignUtils生成签名 - 传入需要签名的参数对象和签名类型
                String paySign = SignUtils.createSign(payInfo, WxPayConstants.SignType.MD5, wxPayService.getConfig().getMchKey(), new String[0]);
                payInfo.put("paySign", paySign);

                return PayResponse.wechatJsapi(
                    request.getOrderNo(),
                    request.getOutTradeNo(),
                    DictPaymentMethod.WECHAT.getValue(),
                    request.getTotalFee(),
                    wxResult.getPrepayId(),
                    payInfo
                );
            } catch (Exception e) {
                log.error("生成微信v2 JSAPI签名失败: prepayId={}", wxResult.getPrepayId(), e);
                return PayResponse.fail("生成支付签名失败: " + e.getMessage());
            }
        }

        // NATIVE支付 - 返回二维码URL和Base64图片
        if ("NATIVE".equals(tradeType)) {
            String codeUrl = wxResult.getCodeURL();
            String qrCodeBase64 = QrCodeUtils.generateBase64(codeUrl);

            return PayResponse.wechatNative(
                request.getOrderNo(),
                request.getOutTradeNo(),
                DictPaymentMethod.WECHAT.getValue(),
                request.getTotalFee(),
                codeUrl,
                qrCodeBase64
            );
        }

        // APP/H5支付 - 返回支付URL
        if ("APP".equals(tradeType) || "MWEB".equals(tradeType)) {
            return PayResponse.wechatH5(
                request.getOrderNo(),
                request.getOutTradeNo(),
                DictPaymentMethod.WECHAT.getValue(),
                request.getTotalFee(),
                wxResult.getMwebUrl()
            );
        }

        // 默认返回
        return PayResponse.success(
            "下单成功",
            request.getOrderNo(),
            request.getOutTradeNo(),
            DictPaymentMethod.WECHAT.getValue(),
            request.getTotalFee()
        );
    }

    // ==================== v3 转换方法 ====================

    /**
     * 将 PayRequest 转换为 WxPayUnifiedOrderV3Request(v3)
     */
    public static WxPayUnifiedOrderV3Request toWxV3Request(PayRequest request, String notifyUrl) {
        WxPayUnifiedOrderV3Request wxRequest = new WxPayUnifiedOrderV3Request();

        // 基础信息
        wxRequest.setOutTradeNo(request.getOutTradeNo());
        wxRequest.setDescription(request.getBody());
        wxRequest.setNotifyUrl(notifyUrl);

        // 金额信息 - v3使用对象
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        amount.setTotal(PayUtils.yuanToFen(request.getTotalFee()));
        amount.setCurrency("CNY");
        wxRequest.setAmount(amount);

        // JSAPI需要payer信息
        if ("JSAPI".equals(request.getTradeType())) {
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(request.getOpenId());
            wxRequest.setPayer(payer);
        }

        // H5支付需要场景信息
        if ("MWEB".equals(request.getTradeType())) {
            WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
            sceneInfo.setPayerClientIp(request.getClientIp());
            WxPayUnifiedOrderV3Request.H5Info h5Info = new WxPayUnifiedOrderV3Request.H5Info();
            h5Info.setType("Wap"); // 默认Wap
            sceneInfo.setH5Info(h5Info);
            wxRequest.setSceneInfo(sceneInfo);
        }

        // 可选参数
        if (StringUtils.isNotBlank(request.getAttach())) {
            wxRequest.setAttach(request.getAttach());
        }

        // v3的时间格式为 RFC3339
        if (StringUtils.isNotBlank(request.getTimeExpire())) {
            wxRequest.setTimeExpire(request.getTimeExpire());
        }

        return wxRequest;
    }

    // ==================== v3 转换方法 ====================

    /**
     * 将 JsapiResult 转换为 Map
     *
     * 用于前端调起微信JSAPI支付
     *
     * @param result wxjava的JsapiResult对象
     * @return 包含所有调起支付必需参数的Map
     */
    public static Map<String, String> jsapiResultToMap(WxPayUnifiedOrderV3Result.JsapiResult result) {
        Map<String, String> map = new HashMap<>();
        map.put("appId", result.getAppId());
        map.put("timeStamp", result.getTimeStamp());
        map.put("nonceStr", result.getNonceStr());
        map.put("package", result.getPackageValue());
        map.put("signType", result.getSignType());
        map.put("paySign", result.getPaySign());
        return map;
    }

    /**
     * 将 AppResult 转换为 Map
     *
     * 用于前端调起微信APP支付
     *
     * wxjava SDK的AppResult字段说明:
     * - appid: 应用ID
     * - partnerid: 商户号
     * - prepayid: 预支付交易会话ID
     * - package: 扩展字段 (固定值"Sign=WXPay")
     * - noncestr: 随机字符串
     * - timestamp: 时间戳
     * - sign: 签名
     *
     * @param result wxjava的AppResult对象
     * @return 包含所有调起支付必需参数的Map
     */
    public static Map<String, String> appResultToMap(WxPayUnifiedOrderV3Result.AppResult result) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", result.getAppid());
        map.put("partnerid", result.getPartnerId());
        map.put("prepayid", result.getPrepayId());
        map.put("package", result.getPackageValue());
        map.put("noncestr", result.getNoncestr());
        map.put("timestamp", result.getTimestamp());
        map.put("sign", result.getSign());
        return map;
    }

    // ==================== v3 支付结果转换 ====================

    /**
     * 将 v3 支付结果转换为统一的 PayResponse
     *
     * wxjava SDK 对不同支付类型返回不同的类型:
     * - NATIVE: 返回 String (code_url)
     * - JSAPI: 返回 WxPayUnifiedOrderV3Result.JsapiResult
     * - APP: 返回 WxPayUnifiedOrderV3Result.AppResult
     * - H5: 返回 WxPayUnifiedOrderV3Result
     *
     * @param result wxjava SDK返回的结果对象(类型不定)
     * @param request 原始支付请求
     * @return PayResponse
     */
    public static PayResponse toPayResponseV3(Object result, PayRequest request) {
        // NATIVE支付 - 返回String类型的code_url
        if (result instanceof String codeUrl) {
            return buildNativeResponse(codeUrl, request);
        }

        // JSAPI支付 - 返回JsapiResult对象
        if (result instanceof WxPayUnifiedOrderV3Result.JsapiResult jsapiResult) {
            return buildJsapiResponse(jsapiResult, request);
        }

        // APP支付 - 返回AppResult对象
        if (result instanceof WxPayUnifiedOrderV3Result.AppResult appResult) {
            return buildAppResponse(appResult, request);
        }

        // H5支付 - 返回WxPayUnifiedOrderV3Result对象
        if (result instanceof WxPayUnifiedOrderV3Result wxResult) {
            return buildH5Response(wxResult, request);
        }

        // 未知类型
        throw new plus.ruoyi.common.pay.exception.WxPayException(
            "未知的微信支付返回类型: " + result.getClass().getName()
        );
    }

    /**
     * 构建 NATIVE 支付响应
     */
    private static PayResponse buildNativeResponse(String codeUrl, PayRequest request) {
        String qrCodeBase64 = QrCodeUtils.generateBase64(codeUrl);
        return PayResponse.wechatNative(
            request.getOrderNo(),
            request.getOutTradeNo(),
            DictPaymentMethod.WECHAT.getValue(),
            request.getTotalFee(),
            codeUrl,
            qrCodeBase64
        );
    }

    /**
     * 构建 JSAPI 支付响应
     */
    private static PayResponse buildJsapiResponse(
        WxPayUnifiedOrderV3Result.JsapiResult jsapiResult,
        PayRequest request) {

        Map<String, String> payInfo = jsapiResultToMap(jsapiResult);
        return PayResponse.wechatJsapi(
            request.getOrderNo(),
            request.getOutTradeNo(),
            DictPaymentMethod.WECHAT.getValue(),
            request.getTotalFee(),
            null, // prepayId不在JsapiResult中
            payInfo
        );
    }

    /**
     * 构建 APP 支付响应
     */
    private static PayResponse buildAppResponse(
        WxPayUnifiedOrderV3Result.AppResult appResult,
        PayRequest request) {

        Map<String, String> payInfo = appResultToMap(appResult);
        return PayResponse.builder()
            .success(true)
            .message("下单成功")
            .orderNo(request.getOrderNo())
            .outTradeNo(request.getOutTradeNo())
            .paymentMethod(DictPaymentMethod.WECHAT.getValue())
            .totalAmount(request.getTotalFee())
            .payInfo(payInfo)
            .build();
    }

    /**
     * 构建 H5 支付响应
     */
    private static PayResponse buildH5Response(
        WxPayUnifiedOrderV3Result wxResult,
        PayRequest request) {

        return PayResponse.wechatH5(
            request.getOrderNo(),
            request.getOutTradeNo(),
            DictPaymentMethod.WECHAT.getValue(),
            request.getTotalFee(),
            wxResult.getH5Url()
        );
    }
}
