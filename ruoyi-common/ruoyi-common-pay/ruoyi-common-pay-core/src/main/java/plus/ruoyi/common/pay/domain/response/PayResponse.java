package plus.ruoyi.common.pay.domain.response;

import lombok.Builder;
import lombok.Data;
import plus.ruoyi.common.pay.utils.PayUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 支付响应对象
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class PayResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 原始订单号(业务订单号)
     */
    private String orderNo;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 第三方订单号
     */
    private String transactionId;

    /**
     * 预支付交易会话标识(微信支付返回)
     */
    private String prepayId;

    /**
     * 前端调起支付所需参数(微信小程序、APP等)
     */
    private Map<String, String> payInfo;

    /**
     * 微信NATIVE支付二维码相关
     */
    private String codeUrl;
    private String qrCodeBase64;

    /**
     * 支付跳转链接(支付宝H5、微信H5等返回)
     */
    private String payUrl;

    /**
     * 支付表单(支付宝PC等返回)
     */
    private String payForm;

    /**
     * 支付body数据(支付宝SDK调起字符串或HTML表单)
     */
    private String body;

    /**
     * 支付状态
     * SUCCESS-支付成功
     * WAIT_PAY-等待支付
     * CLOSED-已关闭
     * REVOKED-已撤销
     * USERPAYING-用户支付中
     * PAYERROR-支付失败
     */
    private String tradeState;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 扩展信息
     */
    private Map<String, Object> extraData;

    /**
     * 创建成功响应
     */
    public static PayResponse success(String message) {
        return PayResponse.builder()
            .success(true)
            .message(message)
            .expireTime(PayUtils.generateTimeExpire(30))
            .build();
    }

    /**
     * 创建成功响应(带基础数据)
     */
    public static PayResponse success(String message, String orderNo, String outTradeNo,
                                      String paymentMethod, BigDecimal totalAmount) {
        return PayResponse.builder()
            .success(true)
            .message(message)
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .expireTime(PayUtils.generateTimeExpire(30))
            .build();
    }

    /**
     * 创建微信JSAPI支付响应
     */
    public static PayResponse wechatJsapi(String orderNo, String outTradeNo, String paymentMethod,
                                          BigDecimal totalAmount, String prepayId, Map<String, String> payInfo) {
        return PayResponse.builder()
            .success(true)
            .message("支付请求成功")
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .prepayId(prepayId)
            .payInfo(payInfo)
            .tradeState("WAIT_PAY")
            .expireTime(PayUtils.generateTimeExpire(30))
            .build();
    }

    /**
     * 创建微信NATIVE支付响应
     */
    public static PayResponse wechatNative(String orderNo, String outTradeNo, String paymentMethod,
                                           BigDecimal totalAmount, String codeUrl, String qrCodeBase64) {
        return PayResponse.builder()
            .success(true)
            .message("二维码生成成功")
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .codeUrl(codeUrl)
            .qrCodeBase64(qrCodeBase64)
            .tradeState("WAIT_PAY")
            .expireTime(PayUtils.generateTimeExpire(30))
            .build();
    }

    /**
     * 创建微信H5支付响应
     */
    public static PayResponse wechatH5(String orderNo, String outTradeNo, String paymentMethod,
                                       BigDecimal totalAmount, String payUrl) {
        return PayResponse.builder()
            .success(true)
            .message("支付请求成功")
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .payUrl(payUrl)
            .tradeState("WAIT_PAY")
            .expireTime(PayUtils.generateTimeExpire(30))
            .build();
    }

    /**
     * 创建支付宝支付响应
     */
    public static PayResponse alipay(String orderNo, String outTradeNo, String paymentMethod,
                                     BigDecimal totalAmount, String payUrl, String payForm) {
        return PayResponse.builder()
            .success(true)
            .message("支付请求成功")
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .payUrl(payUrl)
            .payForm(payForm)
            .tradeState("WAIT_PAY")
            .expireTime(PayUtils.generateTimeExpire(30))
            .build();
    }

    /**
     * 创建余额支付响应
     */
    public static PayResponse balance(String orderNo, String outTradeNo, String paymentMethod,
                                      BigDecimal totalAmount, String transactionId) {
        return PayResponse.builder()
            .success(true)
            .message("余额支付成功")
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .transactionId(transactionId)
            .tradeState("SUCCESS")
            .payTime(new Date())
            .build();
    }

    /**
     * 创建失败响应
     */
    public static PayResponse fail(String message) {
        return PayResponse.builder()
            .success(false)
            .message(message)
            .build();
    }

    /**
     * 创建失败响应(带错误码)
     */
    public static PayResponse fail(String errorCode, String message) {
        return PayResponse.builder()
            .success(false)
            .errorCode(errorCode)
            .message(message)
            .build();
    }

    /**
     * 创建失败响应(带完整信息)
     */
    public static PayResponse fail(String orderNo, String outTradeNo, String paymentMethod,
                                   String errorCode, String message) {
        return PayResponse.builder()
            .success(false)
            .orderNo(orderNo)
            .outTradeNo(outTradeNo)
            .paymentMethod(paymentMethod)
            .errorCode(errorCode)
            .message(message)
            .build();
    }
}
