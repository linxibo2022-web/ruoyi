package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.constant.I18nKeys;

/**
 * 短信登录对象
 *
 * @author Lion Li
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SmsLoginBody extends LoginBody {

    /**
     * 手机号
     */
    @NotBlank(message = I18nKeys.User.PHONE_REQUIRED)
    private String phone;

    /**
     * 短信code
     */
    @NotBlank(message = I18nKeys.VerifyCode.SMS_REQUIRED)
    private String smsCode;

}
