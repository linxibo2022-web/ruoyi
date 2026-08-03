package plus.ruoyi.common.doc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.doc.config.properties.SpringDocProperties;
import plus.ruoyi.common.doc.handler.OpenApiHandler;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SpringDoc接口文档自动配置类
 * 配置OpenAPI文档生成相关的Bean
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@AutoConfiguration(before = SpringDocConfiguration.class)
@EnableConfigurationProperties(SpringDocProperties.class)
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class DocAutoConfiguration {

    private final ServerProperties serverProperties;

    /**
     * 创建OpenAPI文档配置对象
     * 整合自定义配置属性，生成完整的API文档配置
     *
     * @param properties 自定义配置属性
     * @return OpenAPI配置对象
     */
    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI openApi(SpringDocProperties properties) {
        OpenAPI openApi = new OpenAPI();

        // 设置文档基本信息
        SpringDocProperties.InfoProperties infoProperties = properties.getInfo();
        Info info = convertInfo(infoProperties);
        openApi.info(info);

        // 设置扩展文档、标签、路径、组件等配置
        openApi.externalDocs(properties.getExternalDocs());
        openApi.tags(properties.getTags());
        openApi.paths(properties.getPaths());
        openApi.components(properties.getComponents());

        // 配置全局安全认证要求
        if (properties.getComponents() != null) {
            openApi.components(properties.getComponents());
            Set<String> keySet = properties.getComponents().getSecuritySchemes().keySet();
            List<SecurityRequirement> list = new ArrayList<>();
            SecurityRequirement securityRequirement = new SecurityRequirement();
            keySet.forEach(securityRequirement::addList);
            list.add(securityRequirement);
            openApi.security(list);
        }

        return openApi;
    }

    /**
     * 转换Info属性对象
     * 将自定义配置转换为OpenAPI标准的Info对象
     *
     * @param infoProperties 自定义Info配置
     * @return OpenAPI标准Info对象
     */
    private Info convertInfo(SpringDocProperties.InfoProperties infoProperties) {
        Info info = new Info();
        info.setTitle(infoProperties.getTitle());
        info.setDescription(infoProperties.getDescription());
        info.setContact(infoProperties.getContact());
        info.setLicense(infoProperties.getLicense());
        info.setVersion(infoProperties.getVersion());
        return info;
    }

    /**
     * 创建自定义OpenAPI服务处理器
     * 使用自定义的OpenApiHandler替换默认实现
     *
     * @param openAPI OpenAPI配置对象
     * @param securityParser 安全解析器
     * @param springDocConfigProperties SpringDoc配置属性
     * @param propertyResolverUtils 属性解析工具
     * @param openApiBuilderCustomisers OpenAPI构建自定义器列表
     * @param serverBaseUrlCustomisers 服务器基础URL自定义器列表
     * @param javadocProvider Javadoc提供器
     * @return 自定义OpenAPI服务实例
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openAPI,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties,
                                         PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomisers,
                                         Optional<JavadocProvider> javadocProvider) {
        return new OpenApiHandler(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils,
                                openApiBuilderCustomisers, serverBaseUrlCustomisers, javadocProvider);
    }

    /**
     * 创建OpenAPI自定义器
     * 为所有API路径添加应用上下文路径前缀
     *
     * @return OpenAPI自定义器
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        // 获取应用上下文路径
        String contextPath = serverProperties.getServlet().getContextPath();
        String finalContextPath;
        if (StringUtils.isBlank(contextPath) || "/".equals(contextPath)) {
            finalContextPath = "";
        } else {
            finalContextPath = contextPath;
        }

        // 返回路径前缀处理逻辑
        return openApi -> {
            Paths oldPaths = openApi.getPaths();
            // 避免重复处理
            if (oldPaths instanceof PlusPaths) {
                return;
            }
            // 为所有路径添加上下文前缀
            PlusPaths newPaths = new PlusPaths();
            oldPaths.forEach((k, v) -> newPaths.addPathItem(finalContextPath + k, v));
            openApi.setPaths(newPaths);
        };
    }

    /**
     * 自定义Paths类
     * 用于标识已处理过的路径，避免重复添加上下文前缀
     *
     * @author Lion Li
     */
    static class PlusPaths extends Paths {
        public PlusPaths() {
            super();
        }
    }
}
