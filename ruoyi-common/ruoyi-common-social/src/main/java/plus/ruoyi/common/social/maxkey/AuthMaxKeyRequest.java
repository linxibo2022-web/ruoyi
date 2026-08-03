package plus.ruoyi.common.social.maxkey;

import cn.hutool.core.lang.Dict;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;

/**
 * MaxKey认证请求处理类
 * <p>
 * 实现MaxKey身份认证管理系统的OAuth2认证流程
 *
 * @author 长春叭哥 2023年03月26日
 */
public class AuthMaxKeyRequest extends AuthDefaultRequest {

    /**
     * MaxKey服务器地址，从配置文件读取
     */
    public static final String SERVER_URL = SpringUtils.getProperty("justauth.type.maxkey.server-url");

    /**
     * 构造方法
     *
     * @param config 认证配置
     */
    public AuthMaxKeyRequest(AuthConfig config) {
        super(config, AuthMaxKeySource.MAXKEY);
    }

    /**
     * 构造方法（带状态缓存）
     *
     * @param config         认证配置
     * @param authStateCache 认证状态缓存
     */
    public AuthMaxKeyRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthMaxKeySource.MAXKEY, authStateCache);
    }

    /**
     * 获取访问令牌
     *
     * @param authCallback 认证回调信息
     * @return 访问令牌对象
     * @throws AuthException 当认证失败时抛出异常
     */
    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        String body = doPostAuthorizationCode(authCallback.getCode());
        Dict object = JsonUtils.parseMap(body);

        // 检查OAuth令牌验证异常
        if (object.containsKey("error")) {
            throw new AuthException(object.getStr("error_description"));
        }
        // 检查用户验证异常
        if (object.containsKey("message")) {
            throw new AuthException(object.getStr("message"));
        }

        return AuthToken.builder()
            .accessToken(object.getStr("access_token"))
            .refreshToken(object.getStr("refresh_token"))
            .idToken(object.getStr("id_token"))
            .tokenType(object.getStr("token_type"))
            .scope(object.getStr("scope"))
            .build();
    }

    /**
     * 获取用户信息
     *
     * @param authToken 访问令牌
     * @return 用户信息对象
     * @throws AuthException 当获取用户信息失败时抛出异常
     */
    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String body = doGetUserInfo(authToken);
        Dict object = JsonUtils.parseMap(body);

        // 检查OAuth令牌验证异常
        if (object.containsKey("error")) {
            throw new AuthException(object.getStr("error_description"));
        }
        // 检查用户验证异常
        if (object.containsKey("message")) {
            throw new AuthException(object.getStr("message"));
        }

        return AuthUser.builder()
            .uuid(object.getStr("userId"))
            .username(object.getStr("username"))
            .nickname(object.getStr("displayName"))
            .avatar(object.getStr("avatar_url"))
            .blog(object.getStr("web_url"))
            .company(object.getStr("organization"))
            .location(object.getStr("location"))
            .email(object.getStr("email"))
            .remark(object.getStr("bio"))
            .token(authToken)
            .source(source.toString())
            .build();
    }

}
