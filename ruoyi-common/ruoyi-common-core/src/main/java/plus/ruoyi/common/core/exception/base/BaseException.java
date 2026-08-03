package plus.ruoyi.common.core.exception.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.utils.MessageUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.regex.RegexValidator;

import java.io.Serial;

/**
 * 基础异常类
 * <p>
 * 系统中所有自定义异常的基类，提供了统一的异常信息管理机制。
 * 支持国际化消息、错误码、模块标识等功能。
 * </p>
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    @Override
    public String getMessage() {
        String message = null;
        if (StringUtils.isNotBlank(code) && RegexValidator.isValidI18nKey(code)) {
            message = MessageUtils.message(code, args);
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }

    /**
     * 带原因异常的构造方法
     */
    public BaseException(String defaultMessage, Throwable cause) {
        super(cause);
        this.defaultMessage = defaultMessage;
    }

    /**
     * 带原因异常的完整构造方法
     */
    public BaseException(String module, String code, Object[] args, String defaultMessage, Throwable cause) {
        super(cause);
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }
}
