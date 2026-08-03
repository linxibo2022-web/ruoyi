package plus.ruoyi.common.tenant.exception;

import plus.ruoyi.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 多租户异常类
 * <p>
 * 用于处理多租户功能相关的异常情况
 *
 * @author Lion Li
 */
public class TenantException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造多租户异常
     *
     * @param code 错误码
     * @param args 错误参数
     */
    public TenantException(String code, Object... args) {
        super("tenant", code, args, null);
    }
}
