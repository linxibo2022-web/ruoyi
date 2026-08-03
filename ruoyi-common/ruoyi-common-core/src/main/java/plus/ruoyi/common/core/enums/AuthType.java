package plus.ruoyi.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.ruoyi.common.core.constant.I18nKeys;

/**
 * 认证类型
 *
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum AuthType {

    /**
     * 密码认证
     */
    PASSWORD(I18nKeys.User.PASSWORD_RETRY_LOCKED, I18nKeys.User.PASSWORD_RETRY_COUNT),

    /**
     * 短信认证
     */
    SMS(I18nKeys.VerifyCode.SMS_RETRY_LOCKED, I18nKeys.VerifyCode.SMS_RETRY_COUNT),

    /**
     * 邮箱认证
     */
    EMAIL(I18nKeys.VerifyCode.EMAIL_RETRY_LOCKED, I18nKeys.VerifyCode.EMAIL_RETRY_COUNT),

    /**
     * 小程序认证
     */
    MINIAPP("", ""),

    /**
     * 公众号认证
     */
    MP("", ""),

    /**
     * 社交账号认证
     */
    SOCIAL("", "");

    /**
     * 认证重试超出限制提示
     */
    final String retryLimitExceed;

    /**
     * 认证重试限制计数提示
     */
    final String retryLimitCount;
}
