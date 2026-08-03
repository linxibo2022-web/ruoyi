package plus.ruoyi.common.core.exception.file;

import plus.ruoyi.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 文件操作异常类
 * <p>
 * 用于处理文件上传、下载、读写等操作中的异常情况
 * </p>
 *
 * @author ruoyi
 */
public class FileException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件模块标识
     */
    private static final String MODULE = "file";

    /**
     * 构造文件异常
     *
     * @param code 错误码
     * @param args 错误码对应的参数
     */
    public FileException(String code, Object... args) {
        super(MODULE, code, args, null);
    }

    /**
     * 构造文件异常（带默认消息）
     *
     * @param code           错误码
     * @param args           错误码对应的参数
     * @param defaultMessage 默认错误消息
     */
    public FileException(String code, Object[] args, String defaultMessage) {
        super(MODULE, code, args, defaultMessage);
    }

    /**
     * 构造文件异常（仅默认消息）
     *
     * @param defaultMessage 默认错误消息
     */
    public FileException(String defaultMessage) {
        super(MODULE, defaultMessage);
    }

    /**
     * 构造文件异常（带原因）
     *
     * @param code  错误码
     * @param args  错误码对应的参数
     * @param cause 原因异常
     */
    public FileException(String code, Object[] args, Throwable cause) {
        super(MODULE, code, args, null, cause);
    }

    /**
     * 快速创建异常
     *
     * @param code 错误码
     * @param args 错误码对应的参数
     * @return 文件异常实例
     */
    public static FileException of(String code, Object... args) {
        return new FileException(code, args);
    }

    /**
     * 快速创建异常
     *
     * @param defaultMessage 默认错误消息
     * @return 文件异常实例
     */
    public static FileException of(String defaultMessage) {
        return new FileException(defaultMessage);
    }
}
