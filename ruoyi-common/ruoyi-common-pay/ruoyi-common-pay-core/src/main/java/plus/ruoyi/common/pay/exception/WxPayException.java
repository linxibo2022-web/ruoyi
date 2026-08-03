package plus.ruoyi.common.pay.exception;

import java.io.Serial;

/**
 * 微信支付异常
 *
 * @author 抓蛙师
 */
public class WxPayException extends PayException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WxPayException(String message) {
        super(message);
    }

    public WxPayException(String errorCode, String message) {
        super(errorCode, message);
    }

    public WxPayException(String message, Throwable cause) {
        super(message, cause);
    }

    public static WxPayException of(String message) {
        return new WxPayException(message);
    }

    public static WxPayException of(String errorCode, String message) {
        return new WxPayException(errorCode, message);
    }
}
