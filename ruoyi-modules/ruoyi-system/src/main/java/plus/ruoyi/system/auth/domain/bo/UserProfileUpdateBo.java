package plus.ruoyi.system.auth.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


/**
 * 用户资料更新请求对象
 * 用于接收用户头像和昵称更新的请求参数
 *
 * @author 抓蛙师
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserProfileUpdateBo {

    /**
     * 用户昵称
     * 用于显示的用户昵称，不能为空
     */
    @NotBlank(message = "用户昵称不能为空")
    @Size(min = 1, max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    /**
     * 用户头像
     * 用户头像URL地址，不能为空
     */
    @NotBlank(message = "用户头像不能为空")
    @Size(max = 255, message = "用户头像URL长度不能超过255个字符")
    private String avatar;
}
