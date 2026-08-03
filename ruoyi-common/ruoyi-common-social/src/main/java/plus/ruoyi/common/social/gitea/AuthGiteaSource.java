package plus.ruoyi.common.social.gitea;

import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthDefaultRequest;

/**
 * Gitea OAuth2接口配置枚举
 * <p>
 * 定义自建Gitea服务器的OAuth2认证接口地址
 *
 * @author lcry
 */
public enum AuthGiteaSource implements AuthSource {

    /**
     * 自建Gitea私有服务器
     */
    GITEA {
        /**
         * 获取授权接口地址
         *
         * @return 授权URL
         */
        @Override
        public String authorize() {
            return AuthGiteaRequest.SERVER_URL + "/login/oauth/authorize";
        }

        /**
         * 获取访问令牌接口地址
         *
         * @return 令牌获取URL
         */
        @Override
        public String accessToken() {
            return AuthGiteaRequest.SERVER_URL + "/login/oauth/access_token";
        }

        /**
         * 获取用户信息接口地址
         *
         * @return 用户信息获取URL
         */
        @Override
        public String userInfo() {
            return AuthGiteaRequest.SERVER_URL + "/login/oauth/userinfo";
        }

        /**
         * 平台对应的认证请求实现类
         *
         * @return 认证请求类
         */
        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return AuthGiteaRequest.class;
        }

    }
}
