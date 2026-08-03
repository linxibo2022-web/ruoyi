package plus.ruoyi.common.idempotent.aspectj;

import cn.dev33.satoken.SaManager;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 防重复提交切面处理器
 * <p>
 * 基于 AOP 和 Redis 实现的防重复提交机制，参考美团 GTIS 防重系统设计
 * <p>
 * 工作原理：
 * <ul>
 * <li>请求前：生成唯一标识存入 Redis，设置过期时间</li>
 * <li>请求成功：保留 Redis 数据，防止重复提交</li>
 * <li>请求失败：删除 Redis 数据，允许重新提交</li>
 * </ul>
 *
 * @author Lion Li
 */
@Aspect
public class RepeatSubmitAspect {

    /**
     * 线程本地变量，存储当前请求的 Redis 缓存 key
     */
    private static final ThreadLocal<String> KEY_CACHE = new ThreadLocal<>();

    /**
     * 方法执行前的防重复提交检查
     * <p>
     * 根据请求参数、URL 和用户标识生成唯一 key，
     * 尝试在 Redis 中设置该 key，如果设置失败则表示重复提交
     *
     * @param point        切入点信息
     * @param repeatSubmit 重复提交注解
     * @throws Throwable 重复提交或其他异常
     */
    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
        // 计算间隔时间（毫秒）
        long interval = repeatSubmit.timeUnit().toMillis(repeatSubmit.interval());

        if (interval < 1000) {
            throw ServiceException.of("重复提交间隔时间不能小于'1'秒");
        }
        HttpServletRequest request = ServletUtils.getRequest();
        String nowParams = argsArrayToString(point.getArgs());

        // 请求地址（作为存放cache的key值）
        String url = request.getRequestURI();

        // 获取用户唯一标识（从请求头中获取token）
        String submitKey = StringUtils.trimToEmpty(request.getHeader(SaManager.getConfig().getTokenName()));

        // 生成防重复提交的唯一标识
        submitKey = SecureUtil.md5(submitKey + ":" + nowParams);
        // 构建完整的缓存key（前缀 + URL + 唯一标识）
        String cacheRepeatKey = GlobalConstants.REPEAT_SUBMIT_KEY + url + submitKey;

        if (RedisUtils.setObjectIfAbsent(cacheRepeatKey, "", Duration.ofMillis(interval))) {
            // 成功设置缓存，记录key用于后续清理
            KEY_CACHE.set(cacheRepeatKey);
        } else {
            // 缓存已存在，表示重复提交，抛出异常
            throw ServiceException.of(repeatSubmit.message());
        }
    }

    /**
     * 方法正常返回后的处理
     * <p>
     * 根据业务执行结果决定是否清理 Redis 缓存：
     * - 成功：保留缓存，防止在有效期内重复提交
     * - 失败：清理缓存，允许用户重新提交
     *
     * @param joinPoint    切点信息
     * @param repeatSubmit 重复提交注解
     * @param jsonResult   方法返回结果
     */
    @AfterReturning(pointcut = "@annotation(repeatSubmit)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Object jsonResult) {
        try {
            if (jsonResult instanceof R<?> r) {
                // 成功则不删除redis数据 保证在有效时间内无法重复提交
                if (r.getCode() == R.SUCCESS) {
                    return;
                }
                RedisUtils.deleteObject(KEY_CACHE.get());
            }
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 方法异常后的处理
     * <p>
     * 发生异常时清理 Redis 缓存，允许用户重新提交
     *
     * @param joinPoint    切点信息
     * @param repeatSubmit 重复提交注解
     * @param e            异常信息
     */
    @AfterThrowing(value = "@annotation(repeatSubmit)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Exception e) {
        RedisUtils.deleteObject(KEY_CACHE.get());
        KEY_CACHE.remove();
    }

    /**
     * 将方法参数数组转换为字符串
     * <p>
     * 用于生成请求的唯一标识，过滤掉不参与计算的对象类型
     *
     * @param paramsArray 方法参数数组
     * @return 参数字符串
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                params.add(JsonUtils.toJsonString(o));
            }
        }
        return params.toString();
    }

    /**
     * 判断对象是否需要过滤
     * <p>
     * 过滤掉不适合参与唯一标识计算的对象类型，如：
     * - 文件上传对象
     * - HTTP 请求/响应对象
     * - 数据绑定结果对象
     *
     * @param o 待检查的对象
     * @return true-需要过滤，false-不需要过滤
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
            || o instanceof BindingResult;
    }

}
