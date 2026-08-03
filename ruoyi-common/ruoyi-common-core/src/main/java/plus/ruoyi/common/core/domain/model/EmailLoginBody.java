package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.constant.I18nKeys;

/**
 * 邮件登录对象
 *
 * @author Lion Li
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class EmailLoginBody extends LoginBody {

    /**
     * 邮箱
     */
    @NotBlank(message = I18nKeys.User.EMAIL_REQUIRED)
    @Email(message = I18nKeys.User.EMAIL_FORMAT_INVALID)
    private String email;

    /**
     * 邮箱code
     */
    @NotBlank(message = I18nKeys.VerifyCode.EMAIL_REQUIRED)
    private String emailCode;

}
