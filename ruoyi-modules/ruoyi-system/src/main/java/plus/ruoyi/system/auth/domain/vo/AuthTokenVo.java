package plus.ruoyi.system.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登录验证结果对象
 * 封装登录成功后返回的令牌信息
 *
 * @author Michelle.Chung
 */
@Data
public class AuthTokenVo {

    /**
     * 访问令牌
     * 用于API接口的身份验证，需要在后续请求的Header中携带
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌
     * 用于在访问令牌过期后获取新的访问令牌
     * 避免用户频繁登录 未实现
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 访问令牌有效期
     * 令牌的有效时间（秒）
     * 客户端可根据此值进行令牌续期的预判断
     */
    @JsonProperty("expire_in")
    private Long expireIn;

    /**
     * 刷新令牌有效期
     * 刷新令牌的有效时间（秒）
     */
    @JsonProperty("refresh_expire_in")
    private Long refreshExpireIn;

    /**
     * 令牌权限范围
     * 标识令牌可以访问的资源范围
     * 例如：read, write, all等
     */
    private String scope;


}
