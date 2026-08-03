package plus.ruoyi.common.social.maxkey;

import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.request.AuthDefaultRequest;

/**
 * MaxKey OAuth2接口配置枚举
 * <p>
 * 定义自建MaxKey身份认证系统的OAuth2认证接口地址
 *
 * @author 长春叭哥 2023年03月26日
 */
public enum AuthMaxKeySource implements AuthSource {

    /**
     * 自建MaxKey私有服务器
     */
    MAXKEY {
        /**
         * 获取授权接口地址
         *
         * @return 授权URL
         */
        @Override
        public String authorize() {
            return AuthMaxKeyRequest.SERVER_URL + "/sign/authz/oauth/v20/authorize";
        }

        /**
         * 获取访问令牌接口地址
         *
         * @return 令牌获取URL
         */
        @Override
        public String accessToken() {
            return AuthMaxKeyRequest.SERVER_URL + "/sign/authz/oauth/v20/token";
        }

        /**
         * 获取用户信息接口地址
         *
         * @return 用户信息获取URL
         */
        @Override
        public String userInfo() {
            return AuthMaxKeyRequest.SERVER_URL + "/sign/api/oauth/v20/me";
        }

        /**
         * 平台对应的认证请求实现类
         *
         * @return 认证请求类
         */
        @Override
        public Class<? extends AuthDefaultRequest> getTargetClass() {
            return AuthMaxKeyRequest.class;
        }

    }
}
