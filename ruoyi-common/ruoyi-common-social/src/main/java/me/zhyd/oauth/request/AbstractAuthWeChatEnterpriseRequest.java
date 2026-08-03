package me.zhyd.oauth.request;

import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.enums.AuthUserGender;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.StringUtils;
import me.zhyd.oauth.utils.UrlBuilder;

/**
 * 企业微信登录认证抽象基类
 *
 * 提供企业微信OAuth认证的通用实现，包括token获取、用户信息获取等核心功能
 *
 * @author liguanhua
 * @since 1.15.9
 */
public abstract class AbstractAuthWeChatEnterpriseRequest extends AuthDefaultRequest {

    /**
     * 构造方法
     *
     * @param config 认证配置信息
     * @param source 认证来源信息
     */
    public AbstractAuthWeChatEnterpriseRequest(AuthConfig config, AuthSource source) {
        super(config, source);
    }

    /**
     * 构造方法（带状态缓存）
     *
     * @param config         认证配置信息
     * @param source         认证来源信息
     * @param authStateCache 认证状态缓存
     */
    public AbstractAuthWeChatEnterpriseRequest(AuthConfig config, AuthSource source, AuthStateCache authStateCache) {
        super(config, source, authStateCache);
    }

    /**
     * 获取访问令牌
     *
     * @param authCallback 认证回调信息
     * @return 访问令牌对象
     */
    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        String response = doGetAuthorizationCode(accessTokenUrl(null));
        JSONObject object = this.checkResponse(response);

        return AuthToken.builder()
            .accessToken(object.getString("access_token"))
            .expireIn(object.getIntValue("expires_in"))
            .code(authCallback.getCode())
            .build();
    }

    /**
     * 获取用户信息
     *
     * @param authToken 访问令牌
     * @return 用户信息对象
     * @throws AuthException 当用户不属于当前企业时抛出异常
     */
    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String response = doGetUserInfo(authToken);
        JSONObject object = this.checkResponse(response);

        // 返回 OpenId 或其他，均代表非当前企业用户，不支持
        // https://github.com/justauth/JustAuth/issues/227 修复bug
        if (!object.containsKey("userid")) {
            throw new AuthException(AuthResponseStatus.UNIDENTIFIED_PLATFORM, source);
        }

        String userId = object.getString("userid");
        String userTicket = object.getString("user_ticket");
        JSONObject userDetail = getUserDetail(authToken.getAccessToken(), userId, userTicket);

        return AuthUser.builder()
            .rawUserInfo(userDetail)
            .username(userDetail.getString("name"))
            .nickname(userDetail.getString("alias"))
            .avatar(userDetail.getString("avatar"))
            .location(userDetail.getString("address"))
            .email(userDetail.getString("email"))
            .uuid(userId)
            .gender(AuthUserGender.getWechatRealGender(userDetail.getString("gender")))
            .token(authToken)
            .source(source.toString())
            .build();
    }

    /**
     * 校验API响应结果
     *
     * @param response API响应字符串
     * @return 解析后的JSON对象
     * @throws AuthException 当响应包含错误码时抛出异常
     */
    private JSONObject checkResponse(String response) {
        JSONObject object = JSONObject.parseObject(response);

        if (object.containsKey("errcode") && object.getIntValue("errcode") != 0) {
            throw new AuthException(object.getString("errmsg"), source);
        }

        return object;
    }

    /**
     * 构建获取访问令牌的URL
     *
     * @param code 授权码（此处未使用）
     * @return 访问令牌请求URL
     */
    @Override
    protected String accessTokenUrl(String code) {
        return UrlBuilder.fromBaseUrl(source.accessToken())
            .queryParam("corpid", config.getClientId())
            .queryParam("corpsecret", config.getClientSecret())
            .build();
    }

    /**
     * 构建获取用户信息的URL
     *
     * @param authToken 用户授权令牌
     * @return 用户信息请求URL
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(source.userInfo())
            .queryParam("access_token", authToken.getAccessToken())
            .queryParam("code", authToken.getCode())
            .build();
    }

    /**
     * 获取用户详细信息
     *
     * 先获取用户基础信息，如果有用户票据则获取敏感信息并合并
     *
     * @param accessToken 访问令牌
     * @param userId      企业内用户ID
     * @param userTicket  用户票据，用于获取敏感信息
     * @return 完整的用户信息
     */
    private JSONObject getUserDetail(String accessToken, String userId, String userTicket) {
        // 获取用户基础信息
        String userInfoUrl = UrlBuilder.fromBaseUrl("https://qyapi.weixin.qq.com/cgi-bin/user/get")
            .queryParam("access_token", accessToken)
            .queryParam("userid", userId)
            .build();
        String userInfoResponse = new HttpUtils(config.getHttpConfig()).get(userInfoUrl).getBody();
        JSONObject userInfo = checkResponse(userInfoResponse);

        // 获取用户敏感信息（如果有票据）
        if (StringUtils.isNotEmpty(userTicket)) {
            String userDetailUrl = UrlBuilder.fromBaseUrl("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail")
                .queryParam("access_token", accessToken)
                .build();
            JSONObject param = new JSONObject();
            param.put("user_ticket", userTicket);
            String userDetailResponse = new HttpUtils(config.getHttpConfig()).post(userDetailUrl, param.toJSONString()).getBody();
            JSONObject userDetail = checkResponse(userDetailResponse);

            userInfo.putAll(userDetail);
        }
        return userInfo;
    }
}
