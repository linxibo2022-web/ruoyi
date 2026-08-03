package plus.ruoyi.common.social.gitea;

import cn.hutool.core.lang.Dict;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
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
 * Gitea认证请求处理类
 * <p>
 * 实现Gitea平台的OAuth2认证流程，支持自建Gitea服务器
 *
 * @author lcry
 */
@Slf4j
public class AuthGiteaRequest extends AuthDefaultRequest {

    /**
     * Gitea服务器地址，从配置文件读取
     */
    public static final String SERVER_URL = SpringUtils.getProperty("justauth.type.gitea.server-url");

    /**
     * 构造方法
     *
     * @param config 认证配置
     */
    public AuthGiteaRequest(AuthConfig config) {
        super(config, AuthGiteaSource.GITEA);
    }

    /**
     * 构造方法（带状态缓存）
     *
     * @param config         认证配置
     * @param authStateCache 认证状态缓存
     */
    public AuthGiteaRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthGiteaSource.GITEA, authStateCache);
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
     * 发送POST请求获取授权码
     *
     * @param code 授权码
     * @return 响应结果
     */
    @Override
    protected String doPostAuthorizationCode(String code) {
        HttpRequest request = HttpRequest.post(source.accessToken())
            .form("client_id", config.getClientId())
            .form("client_secret", config.getClientSecret())
            .form("grant_type", "authorization_code")
            .form("code", code)
            .form("redirect_uri", config.getRedirectUri());
        HttpResponse response = request.execute();
        return response.body();
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
            .uuid(object.getStr("sub"))
            .username(object.getStr("name"))
            .nickname(object.getStr("preferred_username"))
            .avatar(object.getStr("picture"))
            .email(object.getStr("email"))
            .token(authToken)
            .source(source.toString())
            .build();
    }

}
