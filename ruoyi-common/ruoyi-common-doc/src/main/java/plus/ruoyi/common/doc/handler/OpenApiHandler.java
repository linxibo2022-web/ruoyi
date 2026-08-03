package plus.ruoyi.common.doc.handler;

import cn.hutool.core.io.IoUtil;
import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义OpenAPI处理器
 * 继承并增强SpringDoc的OpenAPIService功能
 * 主要改进：使用Java注释的首行作为Tag名称
 *
 * @author Lion Li
 */
@Slf4j
@SuppressWarnings("all")
public class OpenApiHandler extends OpenAPIService {

    /** 基础错误控制器类 */
    private static Class<?> basicErrorController;

    /** 安全解析服务 */
    private final SecurityService securityParser;

    /** 映射关系缓存 */
    private final Map<String, Object> mappingsMap = new HashMap<>();

    /** SpringDoc标签缓存 */
    private final Map<HandlerMethod, Tag> springdocTags = new HashMap<>();

    /** OpenAPI构建自定义器列表 */
    private final Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers;

    /** 服务器基础URL自定义器列表 */
    private final Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers;

    /** SpringDoc配置属性 */
    private final SpringDocConfigProperties springDocConfigProperties;

    /** 缓存的OpenAPI对象 */
    private final Map<String, OpenAPI> cachedOpenAPI = new HashMap<>();

    /** 属性解析工具 */
    private final PropertyResolverUtils propertyResolverUtils;

    /** Javadoc提供器 */
    private final Optional<JavadocProvider> javadocProvider;

    /** Spring应用上下文 */
    private ApplicationContext context;

    /** OpenAPI配置对象 */
    private OpenAPI openAPI;

    /** 是否存在服务器配置 */
    private boolean isServersPresent;

    /** 服务器基础URL */
    private String serverBaseUrl;

    /**
     * 构造函数
     * 初始化自定义OpenAPI处理器
     *
     * @param openAPI OpenAPI配置对象
     * @param securityParser 安全解析器
     * @param springDocConfigProperties SpringDoc配置属性
     * @param propertyResolverUtils 属性解析工具
     * @param openApiBuilderCustomizers OpenAPI构建自定义器列表
     * @param serverBaseUrlCustomizers 服务器基础URL自定义器列表
     * @param javadocProvider Javadoc提供器
     */
    public OpenApiHandler(Optional<OpenAPI> openAPI, SecurityService securityParser,
                          SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils,
                          Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers,
                          Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers,
                          Optional<JavadocProvider> javadocProvider) {
        super(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils,
              openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);

        // 初始化OpenAPI配置
        if (openAPI.isPresent()) {
            this.openAPI = openAPI.get();
            if (this.openAPI.getComponents() == null)
                this.openAPI.setComponents(new Components());
            if (this.openAPI.getPaths() == null)
                this.openAPI.setPaths(new Paths());
            if (!CollectionUtils.isEmpty(this.openAPI.getServers()))
                this.isServersPresent = true;
        }

        // 初始化各种服务组件
        this.propertyResolverUtils = propertyResolverUtils;
        this.securityParser = securityParser;
        this.springDocConfigProperties = springDocConfigProperties;
        this.openApiBuilderCustomisers = openApiBuilderCustomizers;
        this.serverBaseUrlCustomizers = serverBaseUrlCustomizers;
        this.javadocProvider = javadocProvider;

        // 配置是否使用全限定名
        if (springDocConfigProperties.isUseFqn())
            TypeNameResolver.std.setUseFqn(true);
    }

    /**
     * 构建API操作的标签信息
     * 重写父类方法，增强标签生成逻辑
     *
     * @param handlerMethod 处理器方法
     * @param operation API操作对象
     * @param openAPI OpenAPI配置对象
     * @param locale 本地化设置
     * @return 处理后的操作对象
     */
    @Override
    public Operation buildTags(HandlerMethod handlerMethod, Operation operation, OpenAPI openAPI, Locale locale) {

        Set<Tag> tags = new HashSet<>();
        Set<String> tagsStr = new HashSet<>();

        // 从方法和类上构建标签
        buildTagsFromMethod(handlerMethod.getMethod(), tags, tagsStr, locale);
        buildTagsFromClass(handlerMethod.getBeanType(), tags, tagsStr, locale);

        // 解析标签字符串中的属性占位符
        if (!CollectionUtils.isEmpty(tagsStr))
            tagsStr = tagsStr.stream()
                .map(str -> propertyResolverUtils.resolve(str, locale))
                .collect(Collectors.toSet());

        // 处理缓存的SpringDoc标签
        if (springdocTags.containsKey(handlerMethod)) {
            io.swagger.v3.oas.models.tags.Tag tag = springdocTags.get(handlerMethod);
            tagsStr.add(tag.getName());
            if (openAPI.getTags() == null || !openAPI.getTags().contains(tag)) {
                openAPI.addTagsItem(tag);
            }
        }

        // 为操作设置标签
        if (!CollectionUtils.isEmpty(tagsStr)) {
            if (CollectionUtils.isEmpty(operation.getTags()))
                operation.setTags(new ArrayList<>(tagsStr));
            else {
                Set<String> operationTagsSet = new HashSet<>(operation.getTags());
                operationTagsSet.addAll(tagsStr);
                operation.getTags().clear();
                operation.getTags().addAll(operationTagsSet);
            }
        }

        // 自动生成类级别标签
        if (isAutoTagClasses(operation)) {
            if (javadocProvider.isPresent()) {
                String description = javadocProvider.get().getClassJavadoc(handlerMethod.getBeanType());
                if (StringUtils.isNotBlank(description)) {
                    io.swagger.v3.oas.models.tags.Tag tag = new io.swagger.v3.oas.models.tags.Tag();

                    // 【自定义增强】使用Javadoc注释的第一行作为标签名称
                    List<String> list = IoUtil.readLines(new StringReader(description), new ArrayList<>());
                    // 使用第一行作为标签名
                    tag.setName(list.get(0));
                    operation.addTagsItem(list.get(0));

                    tag.setDescription(description);
                    if (openAPI.getTags() == null || !openAPI.getTags().contains(tag)) {
                        openAPI.addTagsItem(tag);
                    }
                }
            } else {
                // 使用类名的驼峰分割作为标签名
                String tagAutoName = splitCamelCase(handlerMethod.getBeanType().getSimpleName());
                operation.addTagsItem(tagAutoName);
            }
        }

        // 处理现有标签
        if (!CollectionUtils.isEmpty(tags)) {
            List<io.swagger.v3.oas.models.tags.Tag> openApiTags = openAPI.getTags();
            if (!CollectionUtils.isEmpty(openApiTags))
                tags.addAll(openApiTags);
            openAPI.setTags(new ArrayList<>(tags));
        }

        // 处理操作级别的安全要求
        io.swagger.v3.oas.annotations.security.SecurityRequirement[] securityRequirements = securityParser
            .getSecurityRequirements(handlerMethod);
        if (securityRequirements != null) {
            if (securityRequirements.length == 0)
                operation.setSecurity(Collections.emptyList());
            else
                securityParser.buildSecurityRequirement(securityRequirements, operation);
        }

        return operation;
    }

    /**
     * 从方法上构建标签信息
     *
     * @param method 方法对象
     * @param tags 标签集合
     * @param tagsStr 标签字符串集合
     * @param locale 本地化设置
     */
    private void buildTagsFromMethod(Method method, Set<io.swagger.v3.oas.models.tags.Tag> tags, Set<String> tagsStr, Locale locale) {
        // 获取方法上的Tags注解
        Set<Tags> tagsSet = AnnotatedElementUtils.findAllMergedAnnotations(method, Tags.class);
        Set<io.swagger.v3.oas.annotations.tags.Tag> methodTags = tagsSet.stream()
            .flatMap(x -> Stream.of(x.value())).collect(Collectors.toSet());
        methodTags.addAll(AnnotatedElementUtils.findAllMergedAnnotations(method, io.swagger.v3.oas.annotations.tags.Tag.class));

        // 处理方法标签
        if (!CollectionUtils.isEmpty(methodTags)) {
            tagsStr.addAll(StreamUtils.toSet(methodTags, tag -> propertyResolverUtils.resolve(tag.name(), locale)));
            List<io.swagger.v3.oas.annotations.tags.Tag> allTags = new ArrayList<>(methodTags);
            addTags(allTags, tags, locale);
        }
    }

    /**
     * 添加标签到标签集合
     *
     * @param sourceTags 源标签列表
     * @param tags 目标标签集合
     * @param locale 本地化设置
     */
    private void addTags(List<io.swagger.v3.oas.annotations.tags.Tag> sourceTags, Set<io.swagger.v3.oas.models.tags.Tag> tags, Locale locale) {
        Optional<Set<io.swagger.v3.oas.models.tags.Tag>> optionalTagSet = AnnotationsUtils
            .getTags(sourceTags.toArray(new io.swagger.v3.oas.annotations.tags.Tag[0]), true);
        optionalTagSet.ifPresent(tagsSet -> {
            tagsSet.forEach(tag -> {
                // 解析标签名称和描述中的属性占位符
                tag.name(propertyResolverUtils.resolve(tag.getName(), locale));
                tag.description(propertyResolverUtils.resolve(tag.getDescription(), locale));
                // 避免重复添加相同名称的标签
                if (tags.stream().noneMatch(t -> t.getName().equals(tag.getName())))
                    tags.add(tag);
            });
        });
    }
}
