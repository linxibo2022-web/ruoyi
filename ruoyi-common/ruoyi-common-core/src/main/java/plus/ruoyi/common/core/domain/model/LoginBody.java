package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import plus.ruoyi.common.core.constant.I18nKeys;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录对象
 *
 * @author Lion Li
 */

@Data
public class LoginBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 认证方式
     */
    @NotBlank(message = I18nKeys.Auth.TYPE_REQUIRED)
    private String authType;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;

}
