package plus.ruoyi.common.social.topiam;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.xkcoding.http.support.HttpHeader;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.UrlBuilder;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;

import static plus.ruoyi.common.social.topiam.AuthTopIamSource.TOPIAM;

/**
 * TopIAM认证请求处理类
 * <p>
 * 实现TopIAM身份管理平台的OAuth2认证流程，支持标准OAuth2协议
 *
 * @author xlsea
 * @since 2024-01-06
 */
@Slf4j
public class AuthTopIamRequest extends AuthDefaultRequest {

    /**
     * TopIAM服务器地址，从配置文件读取
     */
    public static final String SERVER_URL = SpringUtils.getProperty("justauth.type.topiam.server-url");

    /**
     * 构造方法
     *
     * @param config 认证配置
     */
    public AuthTopIamRequest(AuthConfig config) {
        super(config, TOPIAM);
    }

    /**
     * 构造方法（带状态缓存）
     *
     * @param config         认证配置
     * @param authStateCache 认证状态缓存
     */
    public AuthTopIamRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, TOPIAM, authStateCache);
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
        checkResponse(object);

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
        checkResponse(object);

        return AuthUser.builder()
            .uuid(object.getStr("sub"))
            .username(object.getStr("preferred_username"))
            .nickname(object.getStr("nickname"))
            .avatar(object.getStr("picture"))
            .email(object.getStr("email"))
            .token(authToken)
            .source(source.toString())
            .build();
    }

    /**
     * 发送POST请求获取授权码
     * <p>
     * 使用Basic认证方式发送客户端凭证
     *
     * @param code 授权码
     * @return 响应结果
     */
    @Override
    protected String doPostAuthorizationCode(String code) {
        HttpRequest request = HttpRequest.post(source.accessToken())
            .header("Authorization", "Basic " + Base64.encode("%s:%s".formatted(config.getClientId(), config.getClientSecret())))
            .form("grant_type", "authorization_code")
            .form("code", code)
            .form("redirect_uri", config.getRedirectUri());
        HttpResponse response = request.execute();
        return response.body();
    }

    /**
     * 获取用户信息
     * <p>
     * 使用Bearer token方式认证
     *
     * @param authToken 访问令牌
     * @return 用户信息响应
     */
    @Override
    protected String doGetUserInfo(AuthToken authToken) {
        return new HttpUtils(config.getHttpConfig()).get(source.userInfo(), null, new HttpHeader()
            .add("Content-Type", "application/json")
            .add("Authorization", "Bearer " + authToken.getAccessToken()), false).getBody();
    }

    /**
     * 构建授权URL
     * <p>
     * 添加scope参数到授权请求中
     *
     * @param state 状态参数
     * @return 完整的授权URL
     */
    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
            .queryParam("scope", StrUtil.join("%20", config.getScopes()))
            .build();
    }

    /**
     * 检查API响应结果
     *
     * @param object 响应对象
     * @throws AuthException 当响应包含错误时抛出异常
     */
    private static void checkResponse(Dict object) {
        // 检查OAuth令牌验证异常
        if (object.containsKey("error")) {
            throw new AuthException(object.getStr("error_description"));
        }
        // 检查用户验证异常
        if (object.containsKey("message")) {
            throw new AuthException(object.getStr("message"));
        }
    }

}
