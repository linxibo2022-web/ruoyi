package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import plus.ruoyi.common.core.constant.I18nKeys;

/**
 * 用户注册对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterBody extends LoginBody {

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
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).*$", message = I18nKeys.User.PASSWORD_COMPLEXITY_INVALID)
    private String password;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 邀请码
     */
    private String inviteCode;

}
