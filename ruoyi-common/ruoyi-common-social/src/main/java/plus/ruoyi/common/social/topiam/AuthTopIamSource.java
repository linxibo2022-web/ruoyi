package plus.ruoyi.common.social.topiam;

import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthDefaultRequest;

/**
 * TopIAM OAuth2接口配置枚举
 * <p>
 * 定义TopIAM身份管理平台的OAuth2认证接口地址
 *
 * @author xlsea
 * @since 2024-01-06
 */
public enum AuthTopIamSource implements AuthSource {

    /**
     * TopIAM身份管理平台
     */
    TOPIAM {
        /**
         * 获取授权接口地址
         *
         * @return 授权URL
         */
        @Override
        public String authorize() {
            return AuthTopIamRequest.SERVER_URL + "/oauth2/auth";
        }

        /**
         * 获取访问令牌接口地址
         *
         * @return 令牌获取URL
         */
        @Override
        public String accessToken() {
            return AuthTopIamRequest.SERVER_URL + "/oauth2/token";
        }

        /**
         * 获取用户信息接口地址
         *
         * @return 用户信息获取URL
         */
        @Override
        public String userInfo() {
            return AuthTopIamRequest.SERVER_URL + "/oauth2/userinfo";
        }

        /**
         * 平台对应的认证请求实现类
         *
         * @return 认证请求类
         */
        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return AuthTopIamRequest.class;
        }

    }
}
