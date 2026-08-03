package plus.ruoyi.common.pay.domain.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 退款请求对象
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class RefundRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    private String appid;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 子商户号(服务商模式)
     */
    private String subMchId;

    /**
     * 子商户appid(服务商模式)
     */
    private String subAppid;

    /**
     * 原商户订单号
     */
    private String outTradeNo;

    /**
     * 第三方交易号
     */
    private String transactionId;

    /**
     * 商户退款单号
     */
    private String outRefundNo;

    /**
     * 订单总金额(元)
     */
    private BigDecimal totalFee;

    /**
     * 退款金额(元)
     */
    private BigDecimal refundFee;

    /**
     * 退款货币类型(默认CNY)
     */
    private String refundFeeType;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款描述
     */
    private String refundDesc;

    /**
     * 退款资金来源
     */
    private String refundAccount;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 操作员
     */
    private String operatorId;

    /**
     * 门店编号
     */
    private String storeId;

    /**
     * 终端编号
     */
    private String terminalId;

    /**
     * 签名类型
     */
    private String signType;

    /**
     * 退款请求来源
     * API-接口
     * VENDOR_PLATFORM-商户平台
     */
    private String refundSource;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

    /**
     * 创建微信退款请求
     */
    public static RefundRequest createWxRefundRequest(String appId, String mchId,
                                                     String outTradeNo, String outRefundNo,
                                                     BigDecimal totalFee, BigDecimal refundFee,
                                                     String reason) {
        return RefundRequest.builder()
            .appid(appId)
            .mchId(mchId)
            .outTradeNo(outTradeNo)
            .outRefundNo(outRefundNo)
            .totalFee(totalFee)
            .refundFee(refundFee)
            .reason(reason)
            .refundFeeType("CNY")
            .build();
    }

    /**
     * 创建支付宝退款请求
     */
    public static RefundRequest createAlipayRefundRequest(String appId, String outTradeNo,
                                                         String outRefundNo, BigDecimal refundFee,
                                                         String reason) {
        return RefundRequest.builder()
            .appid(appId)
            .outTradeNo(outTradeNo)
            .outRefundNo(outRefundNo)
            .refundFee(refundFee)
            .reason(reason)
            .build();
    }

    /**
     * 创建余额退款请求
     */
    public static RefundRequest createBalanceRefundRequest(String outTradeNo, String outRefundNo,
                                                          BigDecimal refundFee, String reason) {
        return RefundRequest.builder()
            .outTradeNo(outTradeNo)
            .outRefundNo(outRefundNo)
            .refundFee(refundFee)
            .reason(reason)
            .build();
    }

    /**
     * 验证必需参数
     */
    public boolean isValid() {
        return (outTradeNo != null && !outTradeNo.trim().isEmpty()) ||
               (transactionId != null && !transactionId.trim().isEmpty())
            && outRefundNo != null && !outRefundNo.trim().isEmpty()
            && refundFee != null && refundFee.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 是否为全额退款
     */
    public boolean isFullRefund() {
        return totalFee != null && refundFee != null
            && totalFee.compareTo(refundFee) == 0;
    }

    /**
     * 获取退款比例
     */
    public BigDecimal getRefundRatio() {
        if (totalFee == null || refundFee == null || totalFee.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return refundFee.divide(totalFee, 4, RoundingMode.HALF_UP);
    }
}
