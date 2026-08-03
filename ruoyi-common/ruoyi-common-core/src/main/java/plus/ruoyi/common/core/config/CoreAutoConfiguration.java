package plus.ruoyi.common.core.config;

import jakarta.annotation.PreDestroy;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.config.properties.ModuleProperties;
import plus.ruoyi.common.core.config.properties.ThreadPoolProperties;
import plus.ruoyi.common.core.validate.message.I18nMessageInterceptor;
import plus.ruoyi.common.core.converter.LongListConverter;
import plus.ruoyi.common.core.converter.StringListConverter;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.ThreadUtils;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 核心组件自动配置
 * <p>
 * 统一管理核心模块的所有自动配置，包括：
 * - 核心工具类和类型转换器
 * - Spring 功能特性（AOP、异步）
 * - Bean Validation 国际化配置
 * - 定时任务线程池配置
 * - 模块配置属性管理
 * </p>
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration(before = ValidationAutoConfiguration.class)
@EnableConfigurationProperties({AppProperties.class, ThreadPoolProperties.class, ModuleProperties.class})
@EnableAspectJAutoProxy
@EnableAsync(proxyTargetClass = true)
public class CoreAutoConfiguration {

    /**
     * 定时任务线程池实例，用于在销毁时优雅关闭
     */
    private ScheduledExecutorService scheduledExecutorService;

    // ==================== 核心工具类 ====================

    /**
     * 注册 Spring 工具类
     * <p>
     * 提供获取 Bean、ApplicationContext 等 Spring 容器操作的便捷方法
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public SpringUtils springUtils() {
        return new SpringUtils();
    }

    /**
     * 注册字符串列表转换器
     * <p>
     * 用于 MapStruct 等映射框架，实现字符串与字符串列表之间的相互转换
     * 如：@Mapper(uses = StringListConverter.class)
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public StringListConverter stringListConverter() {
        return new StringListConverter();
    }

    /**
     * 注册长整数列表转换器
     * <p>
     * 用于 MapStruct 等映射框架，实现字符串与长整数列表之间的相互转换
     * 如：@Mapper(uses = LongListConverter.class)
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public LongListConverter longListConverter() {
        return new LongListConverter();
    }

    // ==================== 校验框架配置 ====================

    /**
     * 配置校验框架
     * <p>
     * 主要特性：
     * - 快速失败模式：发现第一个校验错误后立即返回
     * - 国际化支持：使用自定义消息拦截器，支持简化的国际化键格式
     * - HibernateValidator：使用Hibernate Validator作为校验器实现
     * </p>
     *
     * @param messageSource Spring消息源，用于国际化消息解析
     * @return 配置好的Validator实例
     */
    @Bean
    @ConditionalOnMissingBean
    public Validator validator(MessageSource messageSource) {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            // 设置自定义的国际化消息拦截器（完全接管消息插值工作）
            factoryBean.setMessageInterpolator(new I18nMessageInterceptor(messageSource));

            // 设置使用 HibernateValidator 校验器
            factoryBean.setProviderClass(HibernateValidator.class);

            // 配置HibernateValidator特定属性
            Properties properties = new Properties();
            // 设置快速失败模式：校验过程中一旦遇到失败，立即停止并返回错误
            properties.setProperty("hibernate.validator.fail_fast", "true");
            factoryBean.setValidationProperties(properties);

            // 应用配置并初始化校验器工厂
            factoryBean.afterPropertiesSet();
            return factoryBean.getValidator();
        }
    }

    // ==================== 线程池配置 ====================

    /**
     * 核心线程数 = CPU 核心数 + 1
     * 这是一种常见的线程池大小设置策略，适合IO密集型任务
     */
    private final int core = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 创建定时任务线程池
     * <p>
     * 用于执行周期性或定时任务，支持虚拟线程和传统线程两种模式
     *
     * @return 定时任务执行器
     */
    @Bean(name = "scheduledExecutorService")
    @ConditionalOnMissingBean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        // 创建线程工厂构建器，daemon设为true确保不阻止JVM退出
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder().daemon(true);

        // 根据系统支持情况选择虚拟线程或传统线程
        if (SpringUtils.isVirtual()) {
            // 使用虚拟线程，设置线程名称前缀为"virtual-schedule-pool-"
            builder.namingPattern("virtual-schedule-pool-%d")
                .wrappedFactory(new VirtualThreadTaskExecutor().getVirtualThreadFactory());
        } else {
            // 使用传统线程，设置线程名称前缀为"schedule-pool-"
            builder.namingPattern("schedule-pool-%d");
        }

        // 创建定时线程池执行器
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
            core,
            builder.build(),
            new ThreadPoolExecutor.CallerRunsPolicy()) {

            // 重写afterExecute方法，用于捕获和打印任务执行中的异常
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                // 使用工具类打印任务异常，便于排查问题
                ThreadUtils.logException(r, t);
            }
        };

        // 保存实例引用，用于后续销毁
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
        return scheduledThreadPoolExecutor;
    }

    /**
     * 应用关闭时的销毁处理
     * <p>
     * 确保线程池能够优雅关闭，避免任务中断或资源泄露
     * </p>
     */
    @PreDestroy
    public void destroy() {
        try {
            log.info("====关闭后台任务线程池====");
            // 使用工具类关闭线程池，会等待任务完成
            ThreadUtils.shutdownGracefully(scheduledExecutorService);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
