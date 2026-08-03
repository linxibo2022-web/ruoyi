package plus.ruoyi.common.core.exception.user;

import plus.ruoyi.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 用户信息异常类
 * <p>
 * 用于处理用户相关操作的异常，如登录、注册、权限验证等
 * </p>
 *
 * @author ruoyi
 */
public class UserException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户模块标识
     */
    private static final String MODULE = "user";

    /**
     * 构造用户异常
     *
     * @param code 错误码
     * @param args 错误码对应的参数
     */
    public UserException(String code, Object... args) {
        super(MODULE, code, args, null);
    }

    /**
     * 构造用户异常（带默认消息）
     *
     * @param code           错误码
     * @param args           错误码对应的参数
     * @param defaultMessage 默认错误消息
     */
    public UserException(String code, Object[] args, String defaultMessage) {
        super(MODULE, code, args, defaultMessage);
    }

    /**
     * 构造用户异常（仅默认消息）
     *
     * @param defaultMessage 默认错误消息
     */
    public UserException(String defaultMessage) {
        super(MODULE, defaultMessage);
    }

    /**
     * 构造用户异常（带原因）
     *
     * @param code  错误码
     * @param args  错误码对应的参数
     * @param cause 原因异常
     */
    public UserException(String code, Object[] args, Throwable cause) {
        super(MODULE, code, args, null, cause);
    }

    // ========== 静态工厂方法 ==========

    /**
     * 快速创建用户异常
     *
     * @param code 错误码
     * @param args 错误码对应的参数
     * @return 用户异常实例
     */
    public static UserException of(String code, Object... args) {
        return new UserException(code, args);
    }

    /**
     * 快速创建用户异常（带默认消息）
     *
     * @param code           错误码
     * @param args           错误码对应的参数
     * @param defaultMessage 默认错误消息
     * @return 用户异常实例
     */
    public static UserException of(String code, Object[] args, String defaultMessage) {
        return new UserException(code, args, defaultMessage);
    }

    /**
     * 快速创建用户异常（仅默认消息）
     *
     * @param defaultMessage 默认错误消息
     * @return 用户异常实例
     */
    public static UserException of(String defaultMessage) {
        return new UserException(defaultMessage);
    }

    /**
     * 条件抛出用户异常
     *
     * @param condition 条件
     * @param code      错误码
     * @param args      错误码对应的参数
     */
    public static void throwIf(boolean condition, String code, Object... args) {
        if (condition) {
            throw new UserException(code, args);
        }
    }

    /**
     * 条件抛出用户异常（仅默认消息）
     *
     * @param condition      条件
     * @param defaultMessage 默认错误消息
     */
    public static void throwIf(boolean condition, String defaultMessage) {
        if (condition) {
            throw new UserException(defaultMessage);
        }
    }

    /**
     * 非空检查
     *
     * @param object         待检查对象
     * @param code           错误码
     * @param args           错误码对应的参数
     */
    public static void notNull(Object object, String code, Object... args) {
        if (object == null) {
            throw new UserException(code, args);
        }
    }

    /**
     * 非空检查（仅默认消息）
     *
     * @param object         待检查对象
     * @param defaultMessage 默认错误消息
     */
    public static void notNull(Object object, String defaultMessage) {
        if (object == null) {
            throw new UserException(defaultMessage);
        }
    }
}
