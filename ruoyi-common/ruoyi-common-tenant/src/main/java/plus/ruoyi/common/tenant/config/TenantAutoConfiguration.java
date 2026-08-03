package plus.ruoyi.common.tenant.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;
import plus.ruoyi.common.redis.config.RedisAutoConfiguration;
import plus.ruoyi.common.redis.config.properties.RedissonProperties;
import plus.ruoyi.common.tenant.core.TenantSaTokenDao;
import plus.ruoyi.common.tenant.handle.PlusTenantLineHandler;
import plus.ruoyi.common.tenant.handle.TenantKeyPrefixHandler;
import plus.ruoyi.common.tenant.manager.TenantSpringCacheManager;
import plus.ruoyi.common.tenant.properties.TenantProperties;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 多租户插件自动配置类
 * <p>
 * 所有组件都始终启用租户隔离，确保数据访问的完全一致性
 * tenant.enable 只控制业务功能，不影响底层数据隔离
 *
 * @author Lion Li
 */
@EnableConfigurationProperties(TenantProperties.class)
@AutoConfiguration(after = {RedisAutoConfiguration.class})
public class TenantAutoConfiguration {

    /**
     * MyBatis-Plus多租户配置（始终启用）
     * <p>
     * 数据库级别的租户隔离始终生效，确保所有SQL都包含tenant_id条件
     */
    @ConditionalOnClass(TenantLineInnerInterceptor.class)
    @AutoConfiguration
    static class MybatisPlusConfiguration {

        /**
         * 配置MyBatis-Plus多租户插件（始终启用）
         * <p>
         * 移除条件注解，确保数据库查询始终包含租户条件
         * 这是数据一致性的基础保障
         *
         * @param tenantProperties 租户配置属性
         * @return 多租户拦截器实例
         */
        @Bean
        public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantProperties tenantProperties) {
            return new TenantLineInnerInterceptor(new PlusTenantLineHandler(tenantProperties));
        }
    }

    /**
     * 配置Redisson多租户键前缀处理器（始终启用）
     * <p>
     * Redis键始终包含租户前缀，确保缓存数据的租户隔离
     * 这样可以避免开关租户功能导致的缓存数据混乱
     *
     * @param redissonProperties Redisson配置属性
     * @return Redisson自定义配置器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer tenantRedissonCustomizer(RedissonProperties redissonProperties) {
        return config -> {
            TenantKeyPrefixHandler nameMapper = new TenantKeyPrefixHandler(redissonProperties.getKeyPrefix());

            // 获取单机配置
            SingleServerConfig singleServerConfig = ReflectUtils.invokeGetter(config, "singleServerConfig");
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 设置单机模式的多租户Redis键前缀
                singleServerConfig.setNameMapper(nameMapper);
            }

            // 获取集群配置
            ClusterServersConfig clusterServersConfig = ReflectUtils.invokeGetter(config, "clusterServersConfig");
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                // 设置集群模式的多租户Redis键前缀
                clusterServersConfig.setNameMapper(nameMapper);
            }
        };
    }

    /**
     * 配置多租户缓存管理器（始终启用）
     * <p>
     * Spring Cache始终包含租户前缀，确保缓存的租户隔离
     *
     * @return 多租户缓存管理器实例
     */
    @Primary
    @Bean
    public CacheManager tenantCacheManager() {
        return new TenantSpringCacheManager();
    }

    /**
     * 配置多租户认证数据持久层（始终启用）
     * <p>
     * SaToken认证数据始终包含租户前缀，确保认证会话的租户隔离
     *
     * @return 多租户SaToken DAO实例
     */
    @Primary
    @Bean
    public SaTokenDao tenantSaTokenDao() {
        return new TenantSaTokenDao();
    }

}
