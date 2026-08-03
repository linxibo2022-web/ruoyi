package plus.ruoyi.common.core.exception;

import cn.hutool.core.text.StrFormatter;
import plus.ruoyi.common.core.exception.base.BaseBusinessException;

import java.io.Serial;

/**
 * 业务异常（支持占位符 {} ）
 * <p>
 * 用于封装业务逻辑处理中的异常信息
 * </p>
 *
 * @author ruoyi
 */
public final class ServiceException extends BaseBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造业务异常
     *
     * @param message 错误提示
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * 构造业务异常（支持占位符）
     *
     * @param message 错误提示（可包含占位符 {}）
     * @param args    占位符参数
     */
    public ServiceException(String message, Object... args) {
        super(StrFormatter.format(message, args));
    }

    /**
     * 构造业务异常
     *
     * @param message      错误提示
     * @param businessCode 业务错误码
     */
    public ServiceException(String message, Integer businessCode) {
        super(message, businessCode);
    }

    /**
     * 构造业务异常（支持占位符）
     *
     * @param message      错误提示（可包含占位符 {}）
     * @param businessCode 业务错误码
     * @param args         占位符参数
     */
    public ServiceException(String message, Integer businessCode, Object... args) {
        super(StrFormatter.format(message, args), businessCode);
    }

    /**
     * 构造业务异常
     *
     * @param message       错误提示
     * @param businessCode  业务错误码
     * @param detailMessage 详细错误信息
     */
    public ServiceException(String message, Integer businessCode, String detailMessage) {
        super(message, businessCode, detailMessage);
    }

    /**
     * 构造业务异常（带原因）
     *
     * @param message 错误提示
     * @param cause   原因异常
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造业务异常（带原因，支持占位符）
     *
     * @param message 错误提示（可包含占位符 {}）
     * @param cause   原因异常
     * @param args    占位符参数
     */
    public ServiceException(String message, Throwable cause, Object... args) {
        super(StrFormatter.format(message, args), cause);
    }

    /**
     * 快速创建异常
     *
     * @param message 错误提示
     * @return 业务异常实例
     */
    public static ServiceException of(String message) {
        return new ServiceException(message);
    }

    /**
     * 快速创建异常（支持占位符）
     *
     * @param message 错误提示（可包含占位符 {}）
     * @param args    占位符参数
     * @return 业务异常实例
     */
    public static ServiceException of(String message, Object... args) {
        return new ServiceException(message, args);
    }

    /**
     * 快速创建异常
     *
     * @param message      错误提示
     * @param businessCode 业务错误码
     * @return 业务异常实例
     */
    public static ServiceException of(String message, Integer businessCode) {
        return new ServiceException(message, businessCode);
    }

    /**
     * 快速创建异常（支持占位符）
     *
     * @param message      错误提示（可包含占位符 {}）
     * @param businessCode 业务错误码
     * @param args         占位符参数
     * @return 业务异常实例
     */
    public static ServiceException of(String message, Integer businessCode, Object... args) {
        return new ServiceException(message, businessCode, args);
    }

    /**
     * 条件抛出异常
     *
     * @param condition 条件
     * @param message   错误提示
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new ServiceException(message);
        }
    }

    /**
     * 条件抛出异常（支持占位符）
     *
     * @param condition 条件
     * @param message   错误提示（可包含占位符 {}）
     * @param args      占位符参数
     */
    public static void throwIf(boolean condition, String message, Object... args) {
        if (condition) {
            throw new ServiceException(message, args);
        }
    }

    /**
     * 条件抛出异常
     *
     * @param condition    条件
     * @param message      错误提示
     * @param businessCode 业务错误码
     */
    public static void throwIf(boolean condition, String message, Integer businessCode) {
        if (condition) {
            throw new ServiceException(message, businessCode);
        }
    }

    /**
     * 条件抛出异常（支持占位符）
     *
     * @param condition    条件
     * @param message      错误提示（可包含占位符 {}）
     * @param businessCode 业务错误码
     * @param args         占位符参数
     */
    public static void throwIf(boolean condition, String message, Integer businessCode, Object... args) {
        if (condition) {
            throw new ServiceException(message, businessCode, args);
        }
    }

    /**
     * 非空检查
     *
     * @param object  待检查对象
     * @param message 错误提示
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ServiceException(message);
        }
    }

    /**
     * 非空检查（支持占位符）
     *
     * @param object  待检查对象
     * @param message 错误提示（可包含占位符 {}）
     * @param args    占位符参数
     */
    public static void notNull(Object object, String message, Object... args) {
        if (object == null) {
            throw new ServiceException(message, args);
        }
    }
}
