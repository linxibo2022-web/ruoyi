package plus.ruoyi.common.ratelimiter.annotation;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.ratelimiter.enums.LimitType;
import plus.ruoyi.common.ratelimiter.aspectj.RateLimiterAspect;

import java.lang.annotation.*;

/**
 * 限流注解
 *
 * <p>用于标记需要进行限流控制的方法，基于Redis和令牌桶算法实现分布式限流。
 * 支持多种限流策略和灵活的配置方式，可以有效防止接口被恶意刷新或超频访问。
 *
 * <p>支持的限流策略：
 * <ul>
 *   <li><b>全局限流</b>：所有用户共享限流配额</li>
 *   <li><b>IP限流</b>：基于客户端IP地址进行独立限流</li>
 *   <li><b>集群限流</b>：基于集群节点进行限流</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 基本用法：60秒内最多访问10次
 * @RateLimiter(time = 60, count = 10)
 * public void basicMethod() { }
 *
 * // IP限流：每个IP在60秒内最多访问10次
 * @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
 * public void ipLimitMethod() { }
 *
 * // 动态key：基于用户ID进行限流
 * @RateLimiter(key = "#userId", time = 60, count = 10)
 * public void userLimitMethod(String userId) { }
 *
 * // 复杂SpEL表达式
 * @RateLimiter(key = "#{#user.id + ':' + #action}", time = 60, count = 5)
 * public void complexKeyMethod(User user, String action) { }
 *
 * // 自定义错误消息
 * @RateLimiter(time = 60, count = 10, message = "访问过于频繁，请稍后再试")
 * public void customMessageMethod() { }
 * </pre>
 *
 * @author Lion Li
 * @see LimitType 限流类型枚举
 * @see RateLimiterAspect 限流切面处理器
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流缓存key
     *
     * <p>用于生成Redis中的限流缓存key，支持以下几种格式：
     * <ul>
     *   <li><b>固定字符串</b>：直接使用指定的字符串作为key</li>
     *   <li><b>SpEL表达式</b>：动态计算key值，支持引用方法参数和Spring Bean</li>
     * </ul>
     *
     * <p>最终生成的完整缓存key格式为：
     * <pre>rate_limit:请求URI:限流标识:自定义key</pre>
     *
     * @return 限流key，默认为空字符串
     */
    String key() default "";

    /**
     * 限流时间窗口
     *
     * <p>指定限流的时间窗口长度，单位为秒。
     * 在指定的时间窗口内，请求次数不能超过{@link #count()}设置的值。
     *
     * <p>时间窗口采用滑动窗口机制，每次请求都会重新计算时间窗口。
     *
     * <p>常用配置：
     * <ul>
     *   <li>60：1分钟时间窗口</li>
     *   <li>300：5分钟时间窗口</li>
     *   <li>3600：1小时时间窗口</li>
     *   <li>86400：1天时间窗口</li>
     * </ul>
     *
     * @return 时间窗口长度，单位秒，默认60秒
     */
    int time() default 60;

    /**
     * 限流次数阈值
     *
     * <p>指定在{@link #time()}时间窗口内允许的最大请求次数。
     * 当请求次数超过此阈值时，将触发限流，抛出异常阻止方法执行。
     *
     * <p>建议根据业务场景合理设置：
     * <ul>
     *   <li><b>登录接口</b>：建议5-10次/分钟</li>
     *   <li><b>查询接口</b>：建议100-1000次/分钟</li>
     *   <li><b>写入接口</b>：建议10-50次/分钟</li>
     *   <li><b>敏感操作</b>：建议1-5次/分钟</li>
     * </ul>
     *
     * @return 限流次数阈值，默认100次
     */
    int count() default 100;

    /**
     * 限流类型策略
     *
     * <p>指定限流的作用范围和策略类型：
     * <ul>
     *   <li><b>{@link LimitType#DEFAULT}</b>：全局限流，所有请求共享限流配额</li>
     *   <li><b>{@link LimitType#IP}</b>：IP限流，每个客户端IP独立计算限流</li>
     *   <li><b>{@link LimitType#CLUSTER}</b>：集群限流，每个集群节点独立限流</li>
     * </ul>
     *
     * <p>选择建议：
     * <ul>
     *   <li>防止恶意攻击：使用IP限流</li>
     *   <li>保护系统资源：使用全局限流</li>
     *   <li>集群环境负载均衡：使用集群限流</li>
     * </ul>
     *
     * @return 限流类型，默认为全局限流
     * @see LimitType 限流类型枚举定义
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流触发时的提示消息
     *
     * <p>当触发限流时向用户显示的错误提示信息。
     * 支持两种格式：
     * <ul>
     *   <li><b>普通字符串</b>：直接显示指定的文本</li>
     *   <li><b>国际化格式</b>：使用 {@code message.key} 格式，支持多语言</li>
     * </ul>
     *
     * <p>国际化消息处理：
     * <ul>
     *   <li>格式：{@code {消息key}} - 如 {@code request.control.rate.limit.exceeded}</li>
     *   <li>会根据当前用户的语言环境自动选择对应的消息文本</li>
     *   <li>需要在国际化资源文件中定义对应的消息key</li>
     * </ul>
     *
     * <p>示例：
     * <pre>
     * message = "请求过于频繁，请稍后再试"           // 固定消息
     * message = "request.control.rate.limit.exceeded"        // 国际化消息
     * message = "user.login.limit.exceeded"   // 自定义国际化消息
     * </pre>
     *
     * @return 提示消息，默认使用国际化消息 "request.control.rate.limit.exceeded"
     */
    String message() default I18nKeys.Request.RATE_LIMIT_EXCEEDED;

    /**
     * 限流策略超时时间
     *
     * <p>指定限流策略在Redis中的存活时间，单位为秒。
     * 超过此时间后，Redis中的限流数据将被自动清理，限流计数器重置。
     *
     * <p>作用说明：
     * <ul>
     *   <li>防止Redis中积累大量过期的限流数据</li>
     *   <li>确保限流策略能够自动恢复</li>
     *   <li>避免长期限流导致的服务不可用</li>
     * </ul>
     *
     * <p>设置建议：
     * <ul>
     *   <li>通常设置为时间窗口的数倍，确保数据及时清理</li>
     *   <li>不宜设置过短，避免频繁的Redis操作</li>
     *   <li>考虑业务特性，平衡性能和存储成本</li>
     * </ul>
     *
     * <p>常用配置：
     * <ul>
     *   <li>86400：1天（默认值）</li>
     *   <li>3600：1小时</li>
     *   <li>7200：2小时</li>
     * </ul>
     *
     * @return 超时时间，单位秒，默认86400秒（1天）
     */
    int timeout() default 86400;
}
