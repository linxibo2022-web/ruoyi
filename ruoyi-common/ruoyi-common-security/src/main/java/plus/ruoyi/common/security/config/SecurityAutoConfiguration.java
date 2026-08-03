package plus.ruoyi.common.security.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.util.SaTokenConsts;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import plus.ruoyi.common.core.constant.HttpStatus;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.security.config.properties.SecurityProperties;
import plus.ruoyi.common.security.handler.AllUrlHandler;

/**
 * Security 模块自动配置
 * <p>
 * 负责配置安全相关组件，包括：
 * - Sa-Token 登录验证拦截器
 * - URL 路径收集器（AllUrlHandler）
 * - Actuator 监控端点的 HTTP Basic 认证
 * - 路径排除规则（白名单机制）
 * - SSE（Server-Sent Events）路径排除
 * </p>
 * <p>
 * 配置项：
 * - security.excludes: 全局排除路径配置
 * - sse.path: SSE 服务路径配置
 * - spring.boot.admin.client: Admin 监控账号配置
 * </p>
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    /**
     * SSE（Server-Sent Events）服务路径
     * 从配置文件中注入，用于排除 SSE 相关路径的登录验证
     */
    @Value("${sse.path}")
    private String ssePath;

    /**
     * 构造函数
     *
     * @param securityProperties Security 安全配置属性
     */
    public SecurityAutoConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    // ==================== Bean 注册 ====================

    /**
     * 注册 URL 路径收集器
     * <p>
     * 自动收集 Spring MVC 中所有 Controller 的 URL 映射路径
     * 用于安全拦截器的路径匹配和动态权限控制
     * </p>
     *
     * @return URL 路径收集器实例
     */
    @Bean
    public AllUrlHandler allUrlHandler() {
        return new AllUrlHandler();
    }

    // ==================== 拦截器配置 ====================

    /**
     * 注册 Sa-Token 登录验证拦截器
     * <p>
     * 拦截器工作机制：
     * 1. 匹配所有路径（/**）
     * 2. 通过 AllUrlHandler 获取需要验证的 URL 列表
     * 3. 排除配置的白名单路径和 SSE 路径
     * 4. 对剩余路径执行登录 token 验证
     * </p>
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册 Sa-Token 路由拦截器，自定义验证规则
        registry.addInterceptor(new SaInterceptor(handler -> {
                // 获取 URL 处理器，用于动态获取需要验证的路径
                AllUrlHandler allUrlHandler = SpringUtils.getBean(AllUrlHandler.class);

                // 登录验证逻辑
                // 验证用户是否已登录，检查 token 有效性
                SaRouter
                    // 匹配所有需要验证的 URL 路径
                    .match(allUrlHandler.getUrls())
                    // 对匹配的路径执行登录检查
                    .check(StpUtil::checkLogin);
            }))
            // 拦截所有路径
            .addPathPatterns("/**")
            // 排除安全配置中定义的白名单路径
            .excludePathPatterns(securityProperties.getExcludes())
            // 排除 SSE 服务路径，避免影响实时通信
            .excludePathPatterns(ssePath);
    }

    // ==================== Actuator 监控配置 ====================

    /**
     * 配置 Actuator 监控端点的 HTTP Basic 认证
     * <p>
     * 为 Spring Boot Admin 监控功能提供安全保护：
     * 1. 仅对 /actuator 相关路径生效
     * 2. 使用 HTTP Basic 认证方式
     * 3. 账号密码从 spring.boot.admin.client 配置中获取
     * 4. 认证失败返回 401 未授权状态码
     * </p>
     *
     * @return 配置好的 SaServletFilter 实例
     */
    @Bean
    public SaServletFilter getSaServletFilter() {

        // 从配置文件获取 Admin 客户端认证信息
        String username = SpringUtils.getProperty("spring.boot.admin.client.username");
        String password = SpringUtils.getProperty("spring.boot.admin.client.password");

        return new SaServletFilter()
            // 仅拦截 Actuator 相关路径
            .addInclude("/actuator", "/actuator/**")
            // 设置 HTTP Basic 认证逻辑
            .setAuth(obj -> {
                SaHttpBasicUtil.check(username + ":" + password);
            })
            // 设置认证失败时的错误响应
            .setError(e -> {
                HttpServletResponse response = ServletUtils.getResponse();
                response.setContentType(SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED);
            });
    }
}
