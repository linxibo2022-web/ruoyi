package plus.ruoyi.common.idempotent.annotation;

import plus.ruoyi.common.core.constant.I18nKeys;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交注解
 * <p>
 * 用于防止用户在短时间内重复提交表单或请求，基于 Redis 实现分布式锁机制
 *
 * @author Lion Li
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 重复提交检测间隔时间
     * <p>
     * 在此时间内的重复请求将被拦截，默认5秒
     *
     * @return 间隔时间数值
     */
    int interval() default 5000;

    /**
     * 时间单位
     * <p>
     * 配合 interval 使用，指定时间间隔的单位
     *
     * @return 时间单位，默认毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 重复提交时的提示消息
     * <p>
     * 支持国际化配置，格式为 {messageKey}
     * 如：{request.control.duplicate.submit} 会从国际化资源文件中获取对应消息
     *
     * @return 提示消息内容
     */
    String message() default I18nKeys.Request.DUPLICATE_SUBMIT;

}
