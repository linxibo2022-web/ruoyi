package plus.ruoyi.common.core.exception.user;

import plus.ruoyi.common.core.constant.I18nKeys;

import java.io.Serial;

/**
 * 验证码错误异常类
 * <p>
 * 用于处理验证码验证失败的情况，如验证码错误、格式不正确等
 * </p>
 *
 * @author ruoyi
 */
public class CaptchaException extends UserException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 默认错误码
     */
    private static final String DEFAULT_CODE = I18nKeys.VerifyCode.CAPTCHA_INVALID;

    /**
     * 构造验证码异常（使用默认错误码）
     */
    public CaptchaException() {
        super(DEFAULT_CODE, "");
    }

    /**
     * 快速创建异常
     *
     * @return 验证码异常实例
     */
    public static CaptchaException of() {
        return new CaptchaException();
    }
}
