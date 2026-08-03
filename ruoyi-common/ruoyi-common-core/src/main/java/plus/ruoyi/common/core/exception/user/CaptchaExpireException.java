package plus.ruoyi.common.core.exception.user;

import plus.ruoyi.common.core.constant.I18nKeys;

import java.io.Serial;

/**
 * 验证码失效异常类
 * <p>
 * 用于处理验证码超时失效的情况
 * </p>
 *
 * @author ruoyi
 */
public class CaptchaExpireException extends UserException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 默认错误码
     */
    private static final String DEFAULT_CODE = I18nKeys.VerifyCode.CAPTCHA_EXPIRED;

    /**
     * 构造验证码失效异常（使用默认错误码）
     */
    public CaptchaExpireException() {
        super(DEFAULT_CODE, "");
    }

    /**
     * 快速创建异常
     *
     * @return 验证码失效异常实例
     */
    public static CaptchaExpireException of() {
        return new CaptchaExpireException();
    }
}
