package com.aizuda.snailjob.server.starter.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 安全配置类
 * 用于配置Actuator端点的认证过滤器
 *
 * @author Lion Li
 */
@Configuration
public class SecurityConfig {

    /**
     * 从配置文件中读取认证用户名
     */
    @Value("${spring.boot.admin.client.username}")
    private String username;

    /**
     * 从配置文件中读取认证密码
     */
    @Value("${spring.boot.admin.client.password}")
    private String password;

    /**
     * 注册Actuator认证过滤器
     * 为/actuator路径及其子路径添加HTTP Basic认证
     *
     * @return 过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<ActuatorAuthFilter> actuatorFilterRegistrationBean() {
        FilterRegistrationBean<ActuatorAuthFilter> registrationBean = new FilterRegistrationBean<>();

        // 创建认证过滤器实例
        registrationBean.setFilter(new ActuatorAuthFilter(username, password));

        // 设置过滤器拦截的URL模式
        registrationBean.addUrlPatterns("/actuator", "/actuator/*");

        return registrationBean;
    }
}
