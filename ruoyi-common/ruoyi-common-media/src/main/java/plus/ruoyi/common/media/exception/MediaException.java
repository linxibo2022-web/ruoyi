package plus.ruoyi.common.media.exception;

/**
 * 媒体处理异常
 *
 * @author 抓蛙师
 */
public class MediaException extends RuntimeException {

    /**
     * 构造一个带有指定详细消息的媒体异常
     *
     * @param message 异常的详细消息
     */
    public MediaException(String message) {
        super(message);
    }

    /**
     * 构造一个带有指定详细消息和原因的媒体异常
     *
     * @param message 异常的详细消息
     * @param cause   异常的原因
     */
    public MediaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个带有指定原因的媒体异常
     *
     * @param cause 异常的原因
     */
    public MediaException(Throwable cause) {
        super(cause);
    }
}

