package plus.ruoyi.common.pay.exception;

import java.io.Serial;

/**
 * 支付异常基类
 *
 * @author 抓蛙师
 */
public class PayException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    public PayException(String message) {
        super(message);
    }

    public PayException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PayException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static PayException of(String message) {
        return new PayException(message);
    }

    public static PayException of(String message, Throwable cause) {
        return new PayException(message, cause);
    }

    public static PayException of(String errorCode, String message) {
        return new PayException(errorCode, message);
    }
}
