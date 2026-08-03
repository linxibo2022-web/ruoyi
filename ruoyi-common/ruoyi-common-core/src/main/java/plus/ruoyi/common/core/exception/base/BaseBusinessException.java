package plus.ruoyi.common.core.exception.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 业务异常基类
 * <p>
 * 用于处理业务逻辑中的异常，支持业务错误码和详细错误信息
 * </p>
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class BaseBusinessException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务错误码
     */
    private Integer businessCode;

    /**
     * 详细错误信息，用于内部调试
     */
    private String detailMessage;

    /**
     * 构造业务异常
     *
     * @param message 错误提示
     */
    public BaseBusinessException(String message) {
        super(message);
    }

    /**
     * 构造业务异常
     *
     * @param message      错误提示
     * @param businessCode 业务错误码
     */
    public BaseBusinessException(String message, Integer businessCode) {
        super(message);
        this.businessCode = businessCode;
    }

    /**
     * 构造业务异常
     *
     * @param message        错误提示
     * @param businessCode   业务错误码
     * @param detailMessage  详细错误信息
     */
    public BaseBusinessException(String message, Integer businessCode, String detailMessage) {
        super(message);
        this.businessCode = businessCode;
        this.detailMessage = detailMessage;
    }

    /**
     * 构造业务异常（带原因）
     *
     * @param message 错误提示
     * @param cause   原因异常
     */
    public BaseBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 设置错误提示
     *
     * @param message 错误提示
     * @return 当前对象，支持链式调用
     */
    public BaseBusinessException setMessage(String message) {
        super.setDefaultMessage(message);
        return this;
    }

    /**
     * 设置详细错误信息
     *
     * @param detailMessage 详细错误信息
     * @return 当前对象，支持链式调用
     */
    public BaseBusinessException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    /**
     * 设置业务错误码
     *
     * @param businessCode 业务错误码
     * @return 当前对象，支持链式调用
     */
    public BaseBusinessException setBusinessCode(Integer businessCode) {
        this.businessCode = businessCode;
        return this;
    }

    /**
     * 重写 toString() 方法，包含完整的错误信息
     * <p>
     * Lombok @Data 生成的 toString() 不会包含父类的 getMessage()，
     * 导致日志中看不到实际的错误消息，只显示 businessCode 和 detailMessage。
     * </p>
     *
     * @return 包含错误消息的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("(message=").append(getMessage());
        if (businessCode != null) {
            sb.append(", businessCode=").append(businessCode);
        }
        if (detailMessage != null) {
            sb.append(", detailMessage=").append(detailMessage);
        }
        sb.append(")");
        return sb.toString();
    }
}
