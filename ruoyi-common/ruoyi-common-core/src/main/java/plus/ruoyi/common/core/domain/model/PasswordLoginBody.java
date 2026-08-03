package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import plus.ruoyi.common.core.constant.I18nKeys;

/**
 * 密码登录对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordLoginBody extends LoginBody {

    /**
     * 用户名
     */
    @NotBlank(message = I18nKeys.User.USERNAME_REQUIRED)
    @Length(min = 2, max = 30, message = I18nKeys.User.USERNAME_LENGTH_INVALID)
    private String userName;

    /**
     * 用户密码
     */
    @NotBlank(message = I18nKeys.User.PASSWORD_REQUIRED)
    @Length(min = 5, max = 30, message = I18nKeys.User.PASSWORD_LENGTH_INVALID)
    private String password;

}
