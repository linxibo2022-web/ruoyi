package plus.ruoyi.common.web.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Configuration;
import plus.ruoyi.common.web.config.properties.XssProperties;
import plus.ruoyi.common.web.filter.RepeatableFilter;
import plus.ruoyi.common.web.filter.XssFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Web过滤器配置类
 * <p>
 * 配置系统中的Web过滤器，包括XSS过滤器和可重复读取请求体的过滤器。
 * 过滤器按照优先级顺序执行。
 * </p>
 *
 * @author Lion Li
 */
@Configuration
@EnableConfigurationProperties(XssProperties.class)
public class FilterConfiguration {

    /**
     * XSS过滤器
     * <p>
     * 当配置xss.enabled=true时启用，用于防范XSS攻击。
     * 对请求参数和请求头进行XSS过滤处理，不直接读取请求体流。
     * 执行优先级：最高优先级+1
     * </p>
     *
     * @return XSS过滤器注册Bean
     */
    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    @FilterRegistration(
        name = "xssFilter",
        urlPatterns = "/*",
        order = FilterRegistrationBean.HIGHEST_PRECEDENCE + 1,
        dispatcherTypes = DispatcherType.REQUEST
    )
    public XssFilter xssFilter() {
        return new XssFilter();
    }

    /**
     * 可重复读取请求体过滤器
     * <p>
     * 允许多次读取HTTP请求体内容，解决流只能读取一次的问题。
     * 仅对Content-Type为application/json的请求进行处理。
     * 执行优先级：最低优先级，确保在其他过滤器处理完成后统一管理请求体流。order = FilterRegistrationBean.LOWEST_PRECEDENCE 默认值就是这个所以可以忽略
     * </p>
     *
     * @return 可重复读取过滤器注册Bean
     */
    @Bean
    @FilterRegistration(
        name = "repeatableFilter",
        urlPatterns = "/*"
    )
    public RepeatableFilter repeatableFilter() {
        return new RepeatableFilter();
    }

}
