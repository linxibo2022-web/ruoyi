package plus.ruoyi.common.openapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import plus.ruoyi.common.core.service.OpenApiService;
import plus.ruoyi.common.openapi.interceptor.OpenApiInterceptor;
import plus.ruoyi.common.openapi.service.OpenApiScanService;
import plus.ruoyi.common.security.config.properties.SecurityProperties;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 开放 API 自动配置
 * <p>
 * 配置开放 API 相关组件，包括：
 * <ul>
 * <li>OpenApiProperties：开放平台配置属性</li>
 * <li>OpenApiInterceptor：API 认证拦截器（AppKey + 签名验证）</li>
 * <li>OpenApiScanService：接口扫描服务</li>
 * </ul>
 * <p>
 * 功能特性：
 * - AppKey + AppSecret 认证
 * - 请求签名验证
 * - 时间戳防重放攻击
 * - IP 白名单控制
 * - 权限和角色验证
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiAutoConfiguration implements WebMvcConfigurer {

    private final OpenApiService openApiService;
    private final OpenApiProperties openApiProperties;
    private final SecurityProperties securityProperties;

    /**
     * 构造函数
     *
     * @param openApiService     开放 API 服务
     * @param openApiProperties  开放 API 配置属性
     * @param securityProperties 安全配置属性
     */
    public OpenApiAutoConfiguration(OpenApiService openApiService,
                                    OpenApiProperties openApiProperties,
                                    SecurityProperties securityProperties) {
        this.openApiService = openApiService;
        this.openApiProperties = openApiProperties;
        this.securityProperties = securityProperties;
    }

    /**
     * 注册开放 API 拦截器
     * <p>
     * 拦截所有请求，对标注了 @OpenApi 注解的接口进行认证和鉴权
     * 执行优先级：最高（确保在其他拦截器之前执行）
     * </p>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OpenApiInterceptor(openApiService, openApiProperties, securityProperties))
            .addPathPatterns("/**")
            .order(Ordered.HIGHEST_PRECEDENCE); // 最先执行

        log.info("开放 API 拦截器已注册，启用状态: {}", openApiProperties.getEnabled());
    }

    /**
     * 注册开放 API 接口扫描服务
     * <p>
     * 用于扫描和管理所有标注了 @OpenApi 注解的接口
     * 提供接口列表查询、权限过滤等功能
     * </p>
     *
     * @param requestMappingHandlerMapping Spring MVC 请求映射处理器
     * @return 接口扫描服务实例
     */
    @Bean
    public OpenApiScanService openApiScanService(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new OpenApiScanService(requestMappingHandlerMapping);
    }
}
