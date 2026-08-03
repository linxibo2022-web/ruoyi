package plus.ruoyi.common.web.config;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.web.handler.GlobalExceptionHandler;
import plus.ruoyi.common.web.interceptor.PlusWebInvokeTimeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Date;

/**
 * Web通用配置类
 * <p>
 * 配置Web应用的通用功能，包括拦截器、静态资源处理、跨域支持和全局异常处理。
 * 提供完整的Web应用基础配置支持。
 * </p>
 *
 * @author Lion Li
 */
@Configuration
public class ResourcesConfiguration implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public ResourcesConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 添加拦截器
     * <p>注册全局访问性能监控拦截器，用于统计接口响应时间</p>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全局访问性能拦截
        registry.addInterceptor(new PlusWebInvokeTimeInterceptor());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 全局日期格式转换配置
        registry.addConverter(String.class, Date.class, source -> {
            DateTime parse = DateUtil.parse(source);
            if (ObjectUtils.isNull(parse)) {
                return null;
            }
            return parse.toJdkDate();
        });
    }

    /**
     * 添加静态资源处理器
     * <p>配置文件上传路径的静态资源访问，支持通过/resources/**路径访问上传的文件</p>
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
            .addResourceLocations("file:" + appProperties.getUploadPath() + "/");
    }

    /**
     * 跨域配置过滤器
     * <p>
     * 允许所有来源的跨域请求，支持携带凭证信息。
     * 配置包括：允许所有Origin、Header和Method，缓存时间30分钟。
     * </p>
     *
     * @return 跨域过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 设置访问源地址
        config.addAllowedOriginPattern("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }

    /**
     * 全局异常处理器
     * <p>统一处理系统中的异常情况，提供友好的错误响应</p>
     *
     * @return 全局异常处理器实例
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
