package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.constant.I18nKeys;

/**
 * 三方登录对象
 *
 * @author Lion Li
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SocialLoginBody extends LoginBody {

    /**
     * 第三方登录平台
     */
    @NotBlank(message = I18nKeys.SocialLogin.SOURCE_REQUIRED)
    private String source;

    /**
     * 第三方登录code
     */
    @NotBlank(message = I18nKeys.SocialLogin.AUTH_CODE_REQUIRED)
    private String socialCode;

    /**
     * 第三方登录socialState
     */
    @NotBlank(message = I18nKeys.SocialLogin.STATE_REQUIRED)
    private String socialState;

}
