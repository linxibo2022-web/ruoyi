package plus.ruoyi.common.idempotent.config;

import plus.ruoyi.common.idempotent.aspectj.RepeatSubmitAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;

/**
 * 幂等性功能自动配置类
 * <p>
 * 自动注册防重复提交相关的 Bean，确保在 Redis 配置完成后进行初始化
 * <p>
 * 配置内容：
 * <ul>
 * <li>注册 RepeatSubmitAspect 切面处理器</li>
 * <li>依赖 Redis 配置，用于分布式锁实现</li>
 * </ul>
 *
 * @author Lion Li
 */
@AutoConfiguration(after = RedisConfiguration.class)
public class IdempotentAutoConfiguration {

    /**
     * 注册防重复提交切面处理器
     * <p>
     * 提供基于 AOP 的防重复提交功能支持
     *
     * @return RepeatSubmitAspect 实例
     */
    @Bean
    public RepeatSubmitAspect repeatSubmitAspect() {
        return new RepeatSubmitAspect();
    }

}
