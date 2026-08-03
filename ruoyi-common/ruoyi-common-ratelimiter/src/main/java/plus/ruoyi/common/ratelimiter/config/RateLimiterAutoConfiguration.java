package plus.ruoyi.common.ratelimiter.config;

import plus.ruoyi.common.ratelimiter.aspectj.RateLimiterAspect;
import plus.ruoyi.common.ratelimiter.annotation.RateLimiter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;

/**
 * 限流功能自动配置类
 *
 * <p>该配置类负责自动装配分布式限流功能所需的核心组件。
 * 基于Spring Boot的自动配置机制，在Redis配置完成后自动生效。
 *
 * <p>使用方式：
 * <pre>
 * @RateLimiter(time = 60, count = 10)
 * public void someMethod() {
 *     // 方法体
 * }
 * </pre>
 *
 * @author guangxin
 * @date 2023/1/18
 * @see RateLimiterAspect 限流切面处理器
 * @see RateLimiter 限流注解
 */
@AutoConfiguration(after = RedisConfiguration.class)
public class RateLimiterAutoConfiguration {

    /**
     * 注册限流切面处理器
     *
     * <p>创建并注册{@link RateLimiterAspect}实例到Spring容器中，
     * 该切面负责拦截被{@code @RateLimiter}注解标记的方法，
     * 并执行相应的限流逻辑。
     *
     * <p>切面功能特性：
     * <ul>
     *   <li>支持IP级别限流</li>
     *   <li>支持集群级别限流</li>
     *   <li>支持全局限流</li>
     *   <li>支持SpEL表达式动态key</li>
     *   <li>基于令牌桶算法实现</li>
     * </ul>
     *
     * @return 限流切面处理器实例
     * @see RateLimiterAspect#doBefore 限流前置处理方法
     */
    @Bean
    public RateLimiterAspect rateLimiterAspect() {
        return new RateLimiterAspect();
    }

}
