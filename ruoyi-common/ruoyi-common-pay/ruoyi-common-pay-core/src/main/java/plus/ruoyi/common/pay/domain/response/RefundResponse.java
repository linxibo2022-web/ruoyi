package plus.ruoyi.common.pay.domain.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 退款响应对象
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class RefundResponse implements Serializable {

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
     * 第三方退款单号
     */
    private String refundId;

    /**
     * 商户退款单号
     */
    private String outRefundNo;

    /**
     * 原商户订单号
     */
    private String outTradeNo;

    /**
     * 原第三方订单号
     */
    private String transactionId;

    /**
     * 退款金额(元)
     */
    private String refundAmount;

    /**
     * 退款状态
     * SUCCESS-退款成功
     * CLOSED-退款关闭
     * PROCESSING-退款处理中
     * ABNORMAL-退款异常
     */
    private String refundStatus;

    /**
     * 退款成功时间
     */
    private String refundTime;

    /**
     * 退款入账账户
     */
    private String refundAccount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 扩展信息
     */
    private Map<String, Object> extraData;

    /**
     * 创建成功响应
     */
    public static RefundResponse success(String message) {
        return RefundResponse.builder()
            .success(true)
            .message(message)
            .build();
    }

    /**
     * 创建成功响应(带数据)
     */
    public static RefundResponse success(String message, String refundId) {
        return RefundResponse.builder()
            .success(true)
            .message(message)
            .refundId(refundId)
            .build();
    }

    /**
     * 创建失败响应
     */
    public static RefundResponse fail(String message) {
        return RefundResponse.builder()
            .success(false)
            .message(message)
            .build();
    }

    /**
     * 创建失败响应(带错误码)
     */
    public static RefundResponse fail(String errorCode, String message) {
        return RefundResponse.builder()
            .success(false)
            .errorCode(errorCode)
            .message(message)
            .build();
    }
}
