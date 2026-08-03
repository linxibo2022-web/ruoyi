package plus.ruoyi.common.social.config;

import me.zhyd.oauth.cache.AuthStateCache;
import plus.ruoyi.common.social.config.properties.SocialProperties;
import plus.ruoyi.common.social.utils.AuthRedisStateCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 社交登录自动配置类
 * <p>
 * 自动配置社交登录相关的Bean，启用配置属性绑定
 *
 * @author thiszhc
 */
@AutoConfiguration
@EnableConfigurationProperties(SocialProperties.class)
public class SocialAutoConfiguration {

    /**
     * 配置认证状态缓存
     * <p>
     * 使用Redis作为OAuth认证过程中的状态缓存存储
     *
     * @return 认证状态缓存实例
     */
    @Bean
    public AuthStateCache authStateCache() {
        return new AuthRedisStateCache();
    }
}
