package plus.ruoyi.common.ratelimiter.aspectj;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.ratelimiter.annotation.RateLimiter;
import plus.ruoyi.common.ratelimiter.enums.LimitType;
import plus.ruoyi.common.redis.utils.RedisUtils;
import org.redisson.api.RateType;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * 限流处理切面
 *
 * <p>基于AOP和Redis实现分布式限流功能，支持多种限流策略：
 * <ul>
 *   <li>IP限流：基于客户端IP地址进行限流</li>
 *   <li>集群限流：基于Redis客户端实例进行限流</li>
 *   <li>全局限流：所有请求统一限流</li>
 * </ul>
 *
 * <p>使用Redisson的令牌桶算法实现限流控制，当请求超过限制时抛出ServiceException异常。
 * 支持SpEL表达式动态生成限流key，提供灵活的限流配置。
 *
 * @author Lion Li
 */
@Slf4j
@Aspect
public class RateLimiterAspect {

    /**
     * SpEL表达式解析器
     * 用于解析@RateLimiter注解中的动态key表达式
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * SpEL表达式解析模板上下文
     * 定义表达式的前缀和后缀，默认为 #{} 格式
     */
    private final ParserContext parserContext = new TemplateParserContext();

    /**
     * 方法参数名称发现器
     * 用于获取方法参数名称，配合SpEL表达式使用方法参数
     */
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();

    /**
     * 限流前置处理方法
     *
     * <p>在被@RateLimiter注解标记的方法执行前进行限流检查：
     * <ol>
     *   <li>解析注解参数，获取限流配置</li>
     *   <li>生成限流缓存key</li>
     *   <li>调用Redis限流器进行令牌获取</li>
     *   <li>检查限流结果，超限时抛出异常</li>
     * </ol>
     *
     * @param point 切点对象，包含被拦截方法的信息
     * @param rateLimiter 限流注解实例，包含限流配置参数
     * @throws ServiceException 当触发限流时抛出业务异常
     * @throws RuntimeException 当系统异常时抛出运行时异常
     */
    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        // 获取限流配置参数
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        int timeout = rateLimiter.timeout();
        try {
            // 生成限流缓存key
            String combineKey = getCombineKey(rateLimiter, point);

            // 根据限流类型设置Redisson的限流模式
            RateType rateType = RateType.OVERALL;
            if (rateLimiter.limitType() == LimitType.CLUSTER) {
                rateType = RateType.PER_CLIENT;
            }

            // 调用Redis限流器获取令牌
            // 返回值：-1表示获取失败（触发限流），>=0表示剩余令牌数
            long number = RedisUtils.rateLimiter(combineKey, rateType, count, time, timeout);

            // 检查是否触发限流
            if (number == -1) {
                // 抛出业务异常，阻止方法执行
                throw ServiceException.of(rateLimiter.message());
            }

            // 记录限流信息日志
            log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}'", count, number, combineKey);

        } catch (Exception e) {
            // 异常处理：区分业务异常和系统异常
            if (e instanceof ServiceException) {
                // 业务异常直接抛出（限流异常）
                throw e;
            } else {
                // 系统异常包装后抛出
                throw new RuntimeException("服务器限流异常，请稍候再试", e);
            }
        }
    }

    /**
     * 生成限流缓存key
     *
     * <p>根据限流类型和配置生成唯一的缓存key，格式为：
     * <pre>
     * rate_limit:请求URI:限流标识:自定义key
     * </pre>
     *
     * <p>限流标识根据limitType确定：
     * <ul>
     *   <li>IP限流：使用客户端IP地址</li>
     *   <li>集群限流：使用Redis客户端实例ID</li>
     *   <li>全局限流：无额外标识</li>
     * </ul>
     *
     * <p>自定义key支持SpEL表达式，可以使用：
     * <ul>
     *   <li>方法参数：#参数名</li>
     *   <li>Spring Bean：@beanName</li>
     *   <li>复杂表达式：#{表达式内容}</li>
     * </ul>
     *
     * @param rateLimiter 限流注解实例
     * @param point 切点对象，用于获取方法信息和参数
     * @return 生成的限流缓存key
     */
    private String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        String key = rateLimiter.key();

        // 处理动态key：检查是否包含SpEL表达式
        if (StringUtils.isNotBlank(key) && StringUtils.containsAny(key, "#")) {
            // 获取方法签名和参数信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            Object[] args = point.getArgs();

            // 创建SpEL表达式求值上下文
            MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(null, targetMethod, args, pnd);
            // 设置Bean工厂解析器，支持在表达式中引用Spring Bean
            context.setBeanResolver(new BeanFactoryResolver(SpringUtils.getBeanFactory()));

            Expression expression;
            // 判断表达式格式并解析
            if (StringUtils.startsWith(key, parserContext.getExpressionPrefix())
                && StringUtils.endsWith(key, parserContext.getExpressionSuffix())) {
                // 模板表达式：#{expression} 格式
                expression = parser.parseExpression(key, parserContext);
            } else {
                // 简单表达式：#variable 格式
                expression = parser.parseExpression(key);
            }

            // 求值并转换为字符串
            key = expression.getValue(context, String.class);
        }

        // 构建完整的缓存key
        StringBuilder stringBuffer = new StringBuilder(GlobalConstants.RATE_LIMIT_KEY);

        // 添加请求URI，实现不同接口独立限流
        stringBuffer.append(ServletUtils.getRequest().getRequestURI()).append(":");

        // 根据限流类型添加对应的标识符
        if (rateLimiter.limitType() == LimitType.IP) {
            // IP限流：添加客户端IP地址
            stringBuffer.append(ServletUtils.getClientIP()).append(":");
        } else if (rateLimiter.limitType() == LimitType.CLUSTER) {
            // 集群限流：添加Redis客户端实例ID
            stringBuffer.append(RedisUtils.getClient().getId()).append(":");
        }
        // 全局限流：不添加额外标识符

        // 添加自定义key并返回
        return stringBuffer.append(key).toString();
    }
}
