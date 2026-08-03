package plus.ruoyi.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用平台登录对象
 * <p>
 * 用于处理各种应用平台（微信小程序/公众号、抖音小程序、百度小程序等）的授权登录请求。
 * 继承自基础登录对象LoginBody，增加了平台应用授权登录特有的属性。
 * 与SocialLoginBody不同，本类主要用于各平台的应用直接授权流程，无需跳转网页授权。
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlatformLoginBody extends LoginBody {

    /**
     * 应用ID
     * 用于区分不同平台的不同应用（如微信小程序AppID、抖音小程序AppID等）
     */
    private String appid;

    /**
     * 平台类型
     * 例如：微信小程序mp-weixin、抖音小程序mp-toutiao、百度小程序等
     */
    @NotBlank(message = "平台类型不能为空")
    private String platform;

    /**
     * 平台授权码
     * 平台返回的授权码，用于后续获取用户唯一标识和会话信息
     * 例如：微信的js_code、抖音的code、百度的code等
     */
    @NotBlank(message = "平台授权码不能为空")
    private String platformCode;

}
