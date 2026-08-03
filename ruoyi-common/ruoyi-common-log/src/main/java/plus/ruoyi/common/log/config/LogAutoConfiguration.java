package plus.ruoyi.common.log.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.log.aspect.LogAspect;
import plus.ruoyi.common.log.publisher.LoginLogPublisher;

/**
 * 日志模块自动配置
 * <p>
 * 负责注册日志模块的核心组件，包括：
 * - 操作日志切面（LogAspect）- 拦截 @Log 注解，记录操作日志
 * - 登录日志发布器（LoginLogPublisher）- 统一发布登录相关日志事件
 * </p>
 *
 * @author Lion Li
 */
@AutoConfiguration
public class LogAutoConfiguration {

    /**
     * 注册操作日志切面
     * <p>
     * 通过AOP拦截标注了 @Log 注解的方法，自动记录操作日志
     * 包括：请求参数、响应结果、执行时间、异常信息等
     * </p>
     */
    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }

    /**
     * 注册登录日志发布器
     * <p>
     * 提供统一的登录日志事件发布接口，用于记录：
     * - 用户登录、登出
     * - 用户注册
     * - 认证失败等场景
     * </p>
     */
    @Bean
    public LoginLogPublisher loginLogPublisher() {
        return new LoginLogPublisher();
    }
}
