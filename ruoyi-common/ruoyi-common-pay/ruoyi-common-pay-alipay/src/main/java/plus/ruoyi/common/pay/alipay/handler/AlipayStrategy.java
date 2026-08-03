package plus.ruoyi.common.pay.alipay.handler;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.alipay.converter.AlipayConverter;
import plus.ruoyi.common.pay.alipay.registry.AlipayClientRegistry;
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
import plus.ruoyi.common.pay.utils.PayNotifyUrlBuilder;
import plus.ruoyi.common.pay.utils.PayUtils;
import plus.ruoyi.common.pay.utils.QrCodeUtils;
import plus.ruoyi.common.core.utils.SpringUtils;

import java.util.Map;
import java.util.Set;

/**
 * 支付宝支付策略实现
 *
 * 支持支付、退款、查询等功能
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class AlipayStrategy implements PayVersionStrategy {

    private final AlipayClientRegistry registry;
    private final AppProperties appProperties;

    /** 条码支付未知态:查询确认最大次数 */
    private static final int BARCODE_QUERY_MAX_ATTEMPTS = 3;
    /** 条码支付未知态:撤销最大重试次数 */
    private static final int BARCODE_CANCEL_MAX_ATTEMPTS = 3;
    /** 条码支付未知态:查询/撤销重试间隔(毫秒) */
    private static final long BARCODE_RETRY_INTERVAL_MS = 2000L;

    /**
     * 条码支付"明确失败"子错误码(资金未变动,无需撤销)
     * 不在此列表的未知错误码统一走"查询+撤销"兜底,防止"用户已扣款但商户判失败"的资损
     */
    private static final Set<String> BARCODE_DEFINITE_FAIL_SUB_CODES = Set.of(
        "ACQ.PAYMENT_AUTH_CODE_INVALID",          // 付款码无效/已使用
        "ACQ.TRADE_HAS_CLOSE",                    // 交易已关闭
        "ACQ.BUYER_BALANCE_NOT_ENOUGH",           // 买家余额不足
        "ACQ.BUYER_BANKCARD_BALANCE_NOT_ENOUGH",  // 买家银行卡余额不足
        "ACQ.PAYER_USER_STATUS_LIMIT_ERROR",      // 买家账户状态受限
        "ACQ.SELLER_BEEN_BLOCKED",                // 商户账户被冻结
        "ACQ.ERROR_BALANCE_PAYMENT_DISABLE",      // 余额支付被关闭
        "ACQ.INVALID_PARAMETER",                  // 参数无效
        "ACQ.ACCESS_FORBIDDEN",                   // 无权限调用
        "ACQ.MERCHANT_STATUS_NOT_NORMAL"          // 商户状态非正常
    );

    @Override
    public PayResponse executePay(PayRequest request, PayConfig config) {
        try {
            log.info("执行支付宝支付: appid={}, outTradeNo={}",
                config.getAppid(), request.getOutTradeNo());

            // 1. 获取 AlipayClient
            AlipayClient alipayClient = getAlipayClient(config.getAppid());

            // 2. 构建回调地址
            String notifyUrl = buildNotifyUrl(config.getAppid());

            // 3. 根据交易类型执行不同的支付接口
            PayResponse response = executePayByTradeType(
                alipayClient, request, notifyUrl, config
            );

            if (response.isSuccess()) {
                log.info("支付宝支付成功: outTradeNo={}", request.getOutTradeNo());
            } else {
                log.warn("支付宝支付失败: outTradeNo={}, message={}", request.getOutTradeNo(), response.getMessage());
            }
            return response;

        } catch (AlipayApiException e) {
            log.error("支付宝支付失败: outTradeNo={}, errCode={}, errMsg={}",
                request.getOutTradeNo(), e.getErrCode(), e.getErrMsg(), e);
            return PayResponse.fail(e.getErrCode(), e.getErrMsg());

        } catch (Exception e) {
            log.error("支付宝支付异常: outTradeNo={}, error={}",
                request.getOutTradeNo(), e.getMessage(), e);
            return PayResponse.fail("支付失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse executeRefund(RefundRequest request, PayConfig config) {
        try {
            log.info("执行支付宝退款: appid={}, outRefundNo={}",
                config.getAppid(), request.getOutRefundNo());

            AlipayClient alipayClient = getAlipayClient(config.getAppid());

            // 构建退款请求
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(request.getOutTradeNo());
            model.setOutRequestNo(request.getOutRefundNo());
            model.setRefundAmount(request.getRefundFee().toString());
            model.setRefundReason(request.getReason());

            AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
            alipayRequest.setBizModel(model);

            // 调用支付宝退款接口
            AlipayTradeRefundResponse alipayResponse = alipayClient.execute(alipayRequest);

            if (alipayResponse.isSuccess()) {
                return RefundResponse.builder()
                    .success(true)
                    .message("退款申请成功")
                    .outRefundNo(request.getOutRefundNo())
                    .outTradeNo(request.getOutTradeNo())
                    .refundAmount(alipayResponse.getRefundFee())
                    .refundStatus("SUCCESS")
                    .build();
            } else {
                return RefundResponse.builder()
                    .success(false)
                    .errorCode(alipayResponse.getCode())
                    .message(alipayResponse.getMsg())
                    .outRefundNo(request.getOutRefundNo())
                    .outTradeNo(request.getOutTradeNo())
                    .build();
            }

        } catch (AlipayApiException e) {
            log.error("支付宝退款失败: outRefundNo={}, errCode={}, errMsg={}",
                request.getOutRefundNo(), e.getErrCode(), e.getErrMsg(), e);

            return RefundResponse.builder()
                .success(false)
                .errorCode(e.getErrCode())
                .message(e.getErrMsg())
                .outRefundNo(request.getOutRefundNo())
                .outTradeNo(request.getOutTradeNo())
                .build();

        } catch (Exception e) {
            log.error("支付宝退款异常: outRefundNo={}, error={}",
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
            log.info("查询支付宝支付状态: appid={}, outTradeNo={}",
                config.getAppid(), outTradeNo);

            AlipayClient alipayClient = getAlipayClient(config.getAppid());

            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                return PayResponse.builder()
                    .success(true)
                    .message("查询成功")
                    .outTradeNo(response.getOutTradeNo())
                    .transactionId(response.getTradeNo())
                    .totalAmount(PayUtils.stringToDecimal(response.getTotalAmount()))
                    .tradeState(response.getTradeStatus())
                    .build();
            } else {
                return PayResponse.fail("查询失败: " + response.getMsg());
            }

        } catch (Exception e) {
            log.error("查询支付宝支付状态失败: outTradeNo={}, error={}",
                outTradeNo, e.getMessage(), e);
            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, PayConfig config) {
        try {
            log.info("查询支付宝退款状态: appid={}, outRefundNo={}",
                config.getAppid(), outRefundNo);

            AlipayClient alipayClient = getAlipayClient(config.getAppid());

            AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
            model.setOutRequestNo(outRefundNo);

            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            request.setBizModel(model);

            AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                return RefundResponse.builder()
                    .success(true)
                    .message("查询成功")
                    .outRefundNo(outRefundNo)
                    .refundAmount(response.getRefundAmount())
                    .refundStatus(response.getRefundStatus())
                    .build();
            } else {
                return RefundResponse.builder()
                    .success(false)
                    .message("查询失败: " + response.getMsg())
                    .outRefundNo(outRefundNo)
                    .build();
            }

        } catch (Exception e) {
            log.error("查询支付宝退款状态失败: outRefundNo={}, error={}",
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
            log.info("处理支付宝支付回调: appid={}", config.getAppid());

            AlipayClient alipayClient = getAlipayClient(config.getAppid());

            // 获取回调参数
            Map<String, String> params = request.getFormData();

            // 验证签名
            boolean signVerified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                params,
                config.getAlipayPublicKey(),
                "UTF-8",
                "RSA2"
            );

            if (!signVerified) {
                log.error("支付宝回调验签失败: appid={}", config.getAppid());
                return NotifyResponse.fail("验签失败");
            }

            // 检查支付状态
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("支付宝回调支付未成功: outTradeNo={}, tradeStatus={}",
                    params.get("out_trade_no"), tradeStatus);
                return NotifyResponse.success("支付未成功已确认");
            }

            // 发布支付成功事件
            publishPaySuccessEvent(params);

            log.info("支付宝支付回调处理成功: outTradeNo={}, tradeNo={}",
                params.get("out_trade_no"), params.get("trade_no"));

            return NotifyResponse.success();

        } catch (Exception e) {
            log.error("处理支付宝回调异常: error={}", e.getMessage(), e);
            return NotifyResponse.fail("处理失败");
        }
    }

    /**
     * 根据交易类型执行不同的支付接口
     */
    private PayResponse executePayByTradeType(
        AlipayClient alipayClient,
        PayRequest request,
        String notifyUrl,
        PayConfig config) throws AlipayApiException {

        String tradeType = request.getTradeType();

        // 根据不同的支付方式调用不同的接口
        return switch (tradeType) {
            case "APP" -> executeAppPay(alipayClient, request, notifyUrl);
            case "PAGE" -> executePagePay(alipayClient, request, notifyUrl);
            case "WAP" -> executeWapPay(alipayClient, request, notifyUrl);
            case "NATIVE" -> executeNativePay(alipayClient, request, notifyUrl); // 当面付-扫码支付(用户扫商家)
            case "BARCODE" -> executeBarcodePay(alipayClient, request, notifyUrl); // 当面付-付款码支付(商家扫用户)
            default -> throw new PayException("不支持的交易类型: " + tradeType);
        };
    }

    /**
     * APP支付
     */
    private PayResponse executeAppPay(AlipayClient alipayClient, PayRequest request, String notifyUrl)
        throws AlipayApiException {

        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setTotalAmount(request.getTotalFee().toString());
        model.setSubject(request.getBody());
        model.setBody(request.getDetail());

        AlipayTradeAppPayRequest alipayRequest = new AlipayTradeAppPayRequest();
        alipayRequest.setBizModel(model);
        alipayRequest.setNotifyUrl(notifyUrl);

        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(alipayRequest);

        if (response.isSuccess()) {
            return PayResponse.builder()
                .success(true)
                .message("下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(response.getBody()) // 返回SDK调起字符串
                .build();
        } else {
            return PayResponse.fail(response.getCode(), response.getMsg());
        }
    }

    /**
     * 网页支付(PC)
     */
    private PayResponse executePagePay(AlipayClient alipayClient, PayRequest request, String notifyUrl)
        throws AlipayApiException {

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setTotalAmount(request.getTotalFee().toString());
        model.setSubject(request.getBody());
        model.setBody(request.getDetail());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setBizModel(model);
        alipayRequest.setNotifyUrl(notifyUrl);
        alipayRequest.setReturnUrl(request.getReturnUrl());

        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);

        if (response.isSuccess()) {
            return PayResponse.builder()
                .success(true)
                .message("下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(response.getBody()) // 返回HTML表单
                .build();
        } else {
            return PayResponse.fail(response.getCode(), response.getMsg());
        }
    }

    /**
     * 手机网页支付(H5)
     */
    private PayResponse executeWapPay(AlipayClient alipayClient, PayRequest request, String notifyUrl)
        throws AlipayApiException {

        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setTotalAmount(request.getTotalFee().toString());
        model.setSubject(request.getBody());
        model.setBody(request.getDetail());
        model.setProductCode("QUICK_WAP_WAY");

        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setBizModel(model);
        alipayRequest.setNotifyUrl(notifyUrl);
        alipayRequest.setReturnUrl(request.getReturnUrl());

        AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);

        if (response.isSuccess()) {
            return PayResponse.builder()
                .success(true)
                .message("下单成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .totalAmount(request.getTotalFee())
                .body(response.getBody()) // 返回HTML表单
                .build();
        } else {
            return PayResponse.fail(response.getCode(), response.getMsg());
        }
    }

    /**
     * 扫码支付(NATIVE) - 当面付
     */
    private PayResponse executeNativePay(AlipayClient alipayClient, PayRequest request, String notifyUrl)
        throws AlipayApiException {

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setTotalAmount(request.getTotalFee().toString());
        model.setSubject(request.getBody());
        model.setBody(request.getDetail());

        AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
        alipayRequest.setBizModel(model);
        alipayRequest.setNotifyUrl(notifyUrl);

        AlipayTradePrecreateResponse response = alipayClient.execute(alipayRequest);

        if (response.isSuccess()) {
            String qrCode = response.getQrCode();
            if (qrCode == null || qrCode.trim().isEmpty()) {
                log.warn("支付宝返回成功但二维码为空: outTradeNo={}", request.getOutTradeNo());
                return PayResponse.fail("二维码生成失败，请重试");
            }

            // 生成Base64二维码图片
            String qrCodeBase64 = QrCodeUtils.generateBase64(qrCode);

            return PayResponse.builder()
                .success(true)
                .message("二维码生成成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .totalAmount(request.getTotalFee())
                .codeUrl(qrCode) // 二维码链接
                .qrCodeBase64(qrCodeBase64)     // Base64二维码图片
                .tradeState("WAIT_PAY")
                .expireTime(PayUtils.generateTimeExpire(30))
                .build();
        } else {
            return PayResponse.fail(response.getCode(),
                response.getSubMsg() != null ? response.getSubMsg() : response.getMsg());
        }
    }

    /**
     * 付款码支付(BARCODE) - 当面付,商家扫用户付款码
     * 底层调用 alipay.trade.pay,扣款同步发生。返回结果分四类处理:
     *  1) 成功(10000)          - 扣款成功,直接发布支付成功事件
     *  2) 等待密码(10003)       - 用户需输入密码,返回 USERPAYING,前端轮询订单查询接口
     *  3) 明确失败(付款码无效/余额不足/参数错误等) - 资金未变动,直接失败,无需撤销
     *  4) 未知态(系统异常/网络超时/SYSTEM_ERROR)   - 查询确认,必要时撤销,防止"用户已扣款但商户判失败"的资损
     */
    private PayResponse executeBarcodePay(AlipayClient alipayClient, PayRequest request, String notifyUrl) {

        String authCode = request.getAuthCode();
        if (authCode == null || authCode.isBlank()) {
            return PayResponse.fail("付款码(authCode)不能为空");
        }

        AlipayTradePayModel model = new AlipayTradePayModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setTotalAmount(request.getTotalFee().toString());
        model.setSubject(request.getBody());
        model.setBody(request.getDetail());
        model.setAuthCode(authCode);
        // 场景固定为条码支付(也支持 wave_code 声波 / security_code 刷脸,本次只做条码)
        model.setScene("bar_code");

        AlipayTradePayRequest alipayRequest = new AlipayTradePayRequest();
        alipayRequest.setBizModel(model);
        alipayRequest.setNotifyUrl(notifyUrl);

        AlipayTradePayResponse response;
        try {
            response = alipayClient.execute(alipayRequest);
        } catch (AlipayApiException e) {
            // 网络异常/读超时 = 未知态:此刻钱可能已扣,必须查询+撤销兜底,严禁直接判失败造成资损
            log.error("支付宝条码支付通信异常,转未知态兜底: outTradeNo={}, errCode={}, errMsg={}",
                request.getOutTradeNo(), e.getErrCode(), e.getErrMsg());
            return handleBarcodeUnknownState(alipayClient, request, "网络异常:" + e.getErrMsg());
        }

        // 1) 扣款成功
        if (response.isSuccess()) {
            log.info("支付宝条码支付成功: outTradeNo={}, tradeNo={}, buyerLogonId={}",
                request.getOutTradeNo(), response.getTradeNo(), response.getBuyerLogonId());

            // 条码支付是同步扣款,直接发布支付成功事件(异步回调不一定触发)
            publishBarcodePaySuccessEvent(response.getOutTradeNo(), response.getTradeNo(),
                response.getTotalAmount(), response.getBuyerUserId());

            return PayResponse.builder()
                .success(true)
                .message("支付成功")
                .orderNo(request.getOrderNo())
                .outTradeNo(response.getOutTradeNo())
                .transactionId(response.getTradeNo())
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .totalAmount(PayUtils.stringToDecimal(response.getTotalAmount()))
                .tradeState("SUCCESS")
                .build();
        }

        String code = response.getCode();
        String subCode = response.getSubCode();
        String errMsg = response.getSubMsg() != null ? response.getSubMsg() : response.getMsg();

        // 2) 等待用户输入密码 → 前端轮询订单查询接口
        if ("10003".equals(code)) {
            log.info("支付宝条码支付等待用户输入密码: outTradeNo={}, subCode={}",
                request.getOutTradeNo(), subCode);
            return PayResponse.builder()
                .success(true)
                .message("等待用户输入密码")
                .orderNo(request.getOrderNo())
                .outTradeNo(request.getOutTradeNo())
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .totalAmount(request.getTotalFee())
                .tradeState("USERPAYING")
                .build();
        }

        // 3) 明确失败(资金未变动) → 直接失败,无需撤销
        if (isBarcodeDefiniteFail(code, subCode)) {
            log.warn("支付宝条码支付明确失败: outTradeNo={}, code={}, subCode={}, msg={}",
                request.getOutTradeNo(), code, subCode, errMsg);
            return PayResponse.fail(code, errMsg);
        }

        // 4) 未知态(系统繁忙/SYSTEM_ERROR/上下文不一致等) → 查询确认,必要时撤销,防资损
        log.warn("支付宝条码支付返回未知状态,转查询撤销兜底: outTradeNo={}, code={}, subCode={}, msg={}",
            request.getOutTradeNo(), code, subCode, errMsg);
        return handleBarcodeUnknownState(alipayClient, request, errMsg);
    }

    /**
     * 条码支付未知态兜底:先轮询查询确认最终结果,确认未成功再撤销,
     * 避免"用户已扣款但商户侧判失败"导致的资损
     *
     * @param reason 触发未知态的原因(网络异常/系统错误信息),透传给前端便于排查
     */
    private PayResponse handleBarcodeUnknownState(AlipayClient alipayClient, PayRequest request, String reason) {
        String outTradeNo = request.getOutTradeNo();

        // 1. 轮询查询最终态
        AlipayTradeQueryResponse query = loopQueryBarcodeResult(alipayClient, outTradeNo);
        if (query != null && query.isSuccess()) {
            String tradeStatus = query.getTradeStatus();

            // 1.1 已扣款成功 → 补发支付成功事件,按成功返回
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                log.info("条码支付经查询确认已成功: outTradeNo={}, tradeNo={}", outTradeNo, query.getTradeNo());
                publishBarcodePaySuccessEvent(query.getOutTradeNo(), query.getTradeNo(),
                    query.getTotalAmount(), query.getBuyerUserId());
                return PayResponse.builder()
                    .success(true)
                    .message("支付成功")
                    .orderNo(request.getOrderNo())
                    .outTradeNo(query.getOutTradeNo())
                    .transactionId(query.getTradeNo())
                    .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                    .totalAmount(PayUtils.stringToDecimal(query.getTotalAmount()))
                    .tradeState("SUCCESS")
                    .build();
            }

            // 1.2 仍在等待用户付款 → 交前端轮询,不撤销(用户可能继续完成支付)
            if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                log.info("条码支付查询为等待用户付款: outTradeNo={}", outTradeNo);
                return PayResponse.builder()
                    .success(true)
                    .message("等待用户付款")
                    .orderNo(request.getOrderNo())
                    .outTradeNo(outTradeNo)
                    .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                    .totalAmount(request.getTotalFee())
                    .tradeState("USERPAYING")
                    .build();
            }

            // 1.3 已关闭等 → 未扣款/已撤,安全失败
            log.warn("条码支付查询为未成功状态: outTradeNo={}, tradeStatus={}", outTradeNo, tradeStatus);
            return PayResponse.fail("支付未成功(" + tradeStatus + "):" + reason);
        }

        // 2. 查询无法确认(交易不存在/查询持续异常) → 撤销兜底
        boolean cancelled = cancelBarcodeTrade(alipayClient, outTradeNo);
        String msg = cancelled
            ? "支付状态未知,已撤销交易,请用户重新支付:" + reason
            : "支付状态未知且撤销未完成,请稍后查询订单确认:" + reason;
        log.warn("条码支付未知态处理完成: outTradeNo={}, cancelled={}, reason={}", outTradeNo, cancelled, reason);
        return PayResponse.fail(msg);
    }

    /**
     * 轮询查询条码支付最终结果(最多 BARCODE_QUERY_MAX_ATTEMPTS 次)
     * 命中终态(成功/关闭)立即返回,WAIT_BUYER_PAY 继续重试
     *
     * @return 末次查询响应;查询持续异常返回 null
     */
    private AlipayTradeQueryResponse loopQueryBarcodeResult(AlipayClient alipayClient, String outTradeNo) {
        AlipayTradeQueryResponse last = null;
        for (int i = 1; i <= BARCODE_QUERY_MAX_ATTEMPTS; i++) {
            try {
                AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                model.setOutTradeNo(outTradeNo);
                AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
                req.setBizModel(model);
                last = alipayClient.execute(req);
                if (last.isSuccess() && !"WAIT_BUYER_PAY".equals(last.getTradeStatus())) {
                    // 已到终态(成功/已关闭),无需继续轮询
                    return last;
                }
            } catch (AlipayApiException e) {
                log.warn("条码支付查询异常(第{}次): outTradeNo={}, errMsg={}", i, outTradeNo, e.getErrMsg());
            }
            if (i < BARCODE_QUERY_MAX_ATTEMPTS) {
                sleepQuietly(BARCODE_RETRY_INTERVAL_MS);
            }
        }
        return last;
    }

    /**
     * 撤销条码支付交易(alipay.trade.cancel),retry_flag=Y 时按间隔重试
     *
     * @return 撤销是否完成
     */
    private boolean cancelBarcodeTrade(AlipayClient alipayClient, String outTradeNo) {
        for (int i = 1; i <= BARCODE_CANCEL_MAX_ATTEMPTS; i++) {
            try {
                AlipayTradeCancelModel model = new AlipayTradeCancelModel();
                model.setOutTradeNo(outTradeNo);
                AlipayTradeCancelRequest req = new AlipayTradeCancelRequest();
                req.setBizModel(model);
                AlipayTradeCancelResponse resp = alipayClient.execute(req);
                if (resp.isSuccess()) {
                    // retry_flag=N 表示撤销动作已完成,无需再试
                    if (!"Y".equalsIgnoreCase(resp.getRetryFlag())) {
                        log.info("条码支付撤销成功: outTradeNo={}, action={}", outTradeNo, resp.getAction());
                        return true;
                    }
                    log.warn("条码支付撤销需重试(第{}次): outTradeNo={}", i, outTradeNo);
                } else {
                    log.warn("条码支付撤销失败(第{}次): outTradeNo={}, msg={}",
                        i, outTradeNo, resp.getSubMsg() != null ? resp.getSubMsg() : resp.getMsg());
                }
            } catch (AlipayApiException e) {
                log.error("条码支付撤销异常(第{}次): outTradeNo={}, errMsg={}", i, outTradeNo, e.getErrMsg());
            }
            if (i < BARCODE_CANCEL_MAX_ATTEMPTS) {
                sleepQuietly(BARCODE_RETRY_INTERVAL_MS);
            }
        }
        return false;
    }

    /**
     * 判断条码支付错误是否为"明确失败"(资金未变动,无需撤销)
     * 其余未知错误码返回 false,统一走查询+撤销兜底
     */
    private boolean isBarcodeDefiniteFail(String code, String subCode) {
        // 参数缺失(40001)/非法参数(40002)/权限不足(40006):请求未进入扣款,资金未变动
        if ("40001".equals(code) || "40002".equals(code) || "40006".equals(code)) {
            return true;
        }
        return subCode != null && BARCODE_DEFINITE_FAIL_SUB_CODES.contains(subCode);
    }

    /**
     * 当前线程静默休眠,正确响应中断
     */
    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 条码支付同步成功或查询确认成功时发布支付成功事件
     * 复用 PaySuccessEvent,字段对齐异步回调(金额单位:元)
     *
     * @param outTradeNo  商户订单号
     * @param tradeNo     支付宝交易号
     * @param totalAmount 支付金额(元)
     * @param buyerUserId 买家支付宝用户号(2088 开头)
     */
    private void publishBarcodePaySuccessEvent(String outTradeNo, String tradeNo,
                                               String totalAmount, String buyerUserId) {
        try {
            PaySuccessEvent event = PaySuccessEvent.builder()
                .source(this)
                .outTradeNo(outTradeNo)
                .transactionId(tradeNo)
                .totalFee(totalAmount)
                .openId(buyerUserId)
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .build();
            SpringUtils.context().publishEvent(event);
            log.debug("条码支付成功事件已发布: outTradeNo={}", outTradeNo);
        } catch (Exception e) {
            log.error("发布条码支付成功事件失败: outTradeNo={}, error={}", outTradeNo, e.getMessage(), e);
        }
    }

    /**
     * 获取 AlipayClient (统一处理异常)
     */
    private AlipayClient getAlipayClient(String appid) {
        AlipayClient alipayClient = registry.getClient(appid);
        if (alipayClient == null) {
            throw new PayException("未找到AlipayClient: appid=" + appid);
        }
        return alipayClient;
    }

    /**
     * 构建支付宝回调地址
     */
    private String buildNotifyUrl(String appid) {
        String baseApi = appProperties.getBaseApi();
        String notifyUrl = PayNotifyUrlBuilder.buildAlipayNotifyUrl(baseApi, appid);
        log.debug("支付宝回调地址: {}", notifyUrl);
        return notifyUrl;
    }

    /**
     * 发布支付成功事件
     */
    private void publishPaySuccessEvent(Map<String, String> params) {
        try {
            PaySuccessEvent event = PaySuccessEvent.builder()
                .source(this)
                .outTradeNo(params.get("out_trade_no"))
                .transactionId(params.get("trade_no"))
                .totalFee(params.get("total_amount"))
                .mchId(params.get("seller_id"))
                .appId(params.get("app_id"))
                .openId(params.get("buyer_id"))
                .paymentMethod(DictPaymentMethod.ALIPAY.getValue())
                .build();

            SpringUtils.context().publishEvent(event);

            log.debug("支付成功事件已发布: outTradeNo={}", params.get("out_trade_no"));

        } catch (Exception e) {
            log.error("发布支付成功事件失败: outTradeNo={}, error={}",
                params.get("out_trade_no"), e.getMessage(), e);
        }
    }
}
