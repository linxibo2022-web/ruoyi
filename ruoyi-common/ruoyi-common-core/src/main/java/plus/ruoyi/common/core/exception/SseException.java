package plus.ruoyi.common.core.exception;

import plus.ruoyi.common.core.exception.base.BaseBusinessException;

import java.io.Serial;

/**
 * SSE（Server-Sent Events）专用异常
 * <p>
 * 用于处理服务端推送事件相关的异常情况
 * </p>
 *
 * @author LionLi
 */
public final class SseException extends BaseBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造SSE异常
     *
     * @param message 错误提示
     */
    public SseException(String message) {
        super(message);
    }

    /**
     * 构造SSE异常
     *
     * @param message      错误提示
     * @param businessCode 业务错误码
     */
    public SseException(String message, Integer businessCode) {
        super(message, businessCode);
    }

    /**
     * 构造SSE异常
     *
     * @param message       错误提示
     * @param businessCode  业务错误码
     * @param detailMessage 详细错误信息
     */
    public SseException(String message, Integer businessCode, String detailMessage) {
        super(message, businessCode, detailMessage);
    }

    /**
     * 构造SSE异常（带原因）
     *
     * @param message 错误提示
     * @param cause   原因异常
     */
    public SseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 快速创建异常
     *
     * @param message 错误提示
     * @return SSE异常实例
     */
    public static SseException of(String message) {
        return new SseException(message);
    }

    /**
     * 快速创建异常
     *
     * @param message      错误提示
     * @param businessCode 业务错误码
     * @return SSE异常实例
     */
    public static SseException of(String message, Integer businessCode) {
        return new SseException(message, businessCode);
    }
}
