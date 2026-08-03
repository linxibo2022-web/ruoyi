package plus.ruoyi.common.openapi.service;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import plus.ruoyi.common.core.domain.vo.OpenApiInfoVo;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.openapi.annotation.OpenApi;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 开放接口扫描服务
 *
 * @author 抓蛙师
 */
@Slf4j
public class OpenApiScanService {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 类缓存 - 避免重复扫描
     */
    private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    /**
     * ClassPath 扫描结果缓存 - 避免重复扫描整个ClassPath
     */
    private volatile Set<BeanDefinition> classPathScanCache;

    /**
     * 是否已完成ClassPath初始化扫描
     */
    private volatile boolean classPathScanInitialized = false;

    /**
     * 构造函数
     *
     * @param requestMappingHandlerMapping Spring MVC 请求映射处理器
     */
    public OpenApiScanService(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    /**
     * 扫描所有开放接口并根据当前用户权限过滤
     */
    public List<OpenApiInfoVo> scanUserOpenApis() {
        // 获取当前用户的权限和角色
        Set<String> userPermissions = LoginHelper.getLoginUser().getMenuPermission();
        Set<String> userRoles = LoginHelper.getLoginUser().getRolePermission();

        // 扫描所有开放接口
        List<OpenApiInfoVo> allOpenApis = scanAllOpenApis();

        // 根据权限过滤
        return allOpenApis.stream()
            .filter(api -> hasPermission(api, userPermissions, userRoles))
            .collect(Collectors.toList());
    }

    /**
     * 扫描所有开放接口
     */
    private List<OpenApiInfoVo> scanAllOpenApis() {
        List<OpenApiInfoVo> openApis = new ArrayList<>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
            requestMappingHandlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // 检查方法或类上是否有@OpenApi注解
            OpenApi openApi = handlerMethod.getMethodAnnotation(OpenApi.class);
            if (openApi == null) {
                openApi = handlerMethod.getBeanType().getAnnotation(OpenApi.class);
            }

            // 如果没有@OpenApi注解,跳过
            if (openApi == null) {
                continue;
            }

            // 提取接口信息
            OpenApiInfoVo apiInfo = buildOpenApiInfo(mappingInfo, handlerMethod, openApi);
            if (apiInfo != null) {
                openApis.add(apiInfo);
            }
        }

        // 按模块和路径排序
        openApis.sort(Comparator.comparing(OpenApiInfoVo::getModule)
            .thenComparing(OpenApiInfoVo::getPath));

        return openApis;
    }

    /**
     * 构建开放接口信息
     */
    private OpenApiInfoVo buildOpenApiInfo(RequestMappingInfo mappingInfo,
                                           HandlerMethod handlerMethod,
                                           OpenApi openApi) {
        Method method = handlerMethod.getMethod();

        // 获取请求路径
        Set<String> patterns = mappingInfo.getPatternValues();
        if (patterns.isEmpty()) {
            return null;
        }
        String path = patterns.iterator().next();

        // 获取请求方法
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        String httpMethod = methods.isEmpty() ? "ALL" : methods.iterator().next().name();

        // 提取模块名 (从路径第一段提取)
        String module = extractModule(path);

        // 获取权限要求
        SaCheckPermission permissionAnnotation = method.getAnnotation(SaCheckPermission.class);
        if (permissionAnnotation == null) {
            permissionAnnotation = handlerMethod.getBeanType().getAnnotation(SaCheckPermission.class);
        }
        String permission = permissionAnnotation != null ?
            String.join(",", permissionAnnotation.value()) : null;
        String permissionMode = permissionAnnotation != null ?
            permissionAnnotation.mode().toString() : null;

        // 获取角色要求
        SaCheckRole roleAnnotation = method.getAnnotation(SaCheckRole.class);
        if (roleAnnotation == null) {
            roleAnnotation = handlerMethod.getBeanType().getAnnotation(SaCheckRole.class);
        }
        String roleCode = roleAnnotation != null ?
            String.join(",", roleAnnotation.value()) : null;
        String roleMode = roleAnnotation != null ?
            roleAnnotation.mode().toString() : null;

        // 检查是否无权限限制
        SaIgnore ignoreAnnotation = method.getAnnotation(SaIgnore.class);
        if (ignoreAnnotation == null) {
            ignoreAnnotation = handlerMethod.getBeanType().getAnnotation(SaIgnore.class);
        }
        boolean noAuth = ignoreAnnotation != null;

        // 提取参数信息
        List<OpenApiInfoVo.ParameterInfo> parameters = extractParameters(method);

        // 获取响应类型
        String responseType = method.getReturnType().getSimpleName();

        // 提取响应信息(包含泛型和字段详情)
        OpenApiInfoVo.ResponseInfo responseInfo = extractResponseInfo(method);

        return OpenApiInfoVo.builder()
            .path(path)
            .method(httpMethod)
            .description(StringUtils.isNotBlank(openApi.value()) ? openApi.value() : method.getName())
            .module(module)
            .className(handlerMethod.getBeanType().getSimpleName())
            .methodName(method.getName())
            .permission(permission)
            .permissionMode(permissionMode)
            .roleCode(roleCode)
            .roleMode(roleMode)
            .noAuth(noAuth)
            .parameters(parameters)
            .responseType(responseType)
            .responseInfo(responseInfo)
            .build();
    }

    /**
     * 从路径提取模块名
     */
    private String extractModule(String path) {
        if (StringUtils.isBlank(path) || !path.startsWith("/")) {
            return "未知";
        }
        String[] parts = path.substring(1).split("/");
        return parts.length > 0 ? parts[0] : "未知";
    }

    /**
     * 提取方法参数信息
     */
    private List<OpenApiInfoVo.ParameterInfo> extractParameters(Method method) {
        List<OpenApiInfoVo.ParameterInfo> parameters = new ArrayList<>();

        Parameter[] methodParameters = method.getParameters();
        for (Parameter parameter : methodParameters) {
            // 跳过特殊参数类型
            Class<?> paramType = parameter.getType();
            String typeName = paramType.getSimpleName();

            if (typeName.contains("HttpServletRequest") ||
                typeName.contains("HttpServletResponse") ||
                typeName.contains("BindingResult") ||
                typeName.equals("PageQuery")) {
                continue;
            }

            // 检查参数位置和是否必填
            NotNull notNullAnnotation = parameter.getAnnotation(NotNull.class);
            RequestParam requestParamAnnotation = parameter.getAnnotation(RequestParam.class);
            PathVariable pathVariableAnnotation = parameter.getAnnotation(PathVariable.class);
            RequestBody requestBodyAnnotation = parameter.getAnnotation(RequestBody.class);

            // 确定参数位置
            String location = determineParameterLocation(
                requestParamAnnotation,
                pathVariableAnnotation,
                requestBodyAnnotation
            );

            boolean required = notNullAnnotation != null ||
                (requestParamAnnotation != null && requestParamAnnotation.required()) ||
                pathVariableAnnotation != null ||
                requestBodyAnnotation != null;

            String paramName = parameter.getName();
            if (requestParamAnnotation != null && StringUtils.isNotBlank(requestParamAnnotation.value())) {
                paramName = requestParamAnnotation.value();
            } else if (pathVariableAnnotation != null && StringUtils.isNotBlank(pathVariableAnnotation.value())) {
                paramName = pathVariableAnnotation.value();
            }

            // 提取参数说明
            Schema schema = parameter.getAnnotation(Schema.class);
            String description = schema != null ? schema.description() : "";

            // 判断是否是复杂对象类型
            List<OpenApiInfoVo.FieldInfo> fields = null;
            if (isComplexType(paramType)) {
                fields = extractFieldsFromClass(paramType);
            }

            parameters.add(OpenApiInfoVo.ParameterInfo.builder()
                .name(paramName)
                .type(typeName)
                .required(required)
                .description(description)
                .location(location)
                .fields(fields)
                .build());
        }

        return parameters;
    }

    /**
     * 确定参数位置
     */
    private String determineParameterLocation(
        RequestParam requestParamAnnotation,
        PathVariable pathVariableAnnotation,
        RequestBody requestBodyAnnotation) {

        if (pathVariableAnnotation != null) {
            return OpenApiInfoVo.ParameterLocation.PATH;
        } else if (requestBodyAnnotation != null) {
            return OpenApiInfoVo.ParameterLocation.BODY;
        } else if (requestParamAnnotation != null) {
            return OpenApiInfoVo.ParameterLocation.QUERY;
        } else {
            // 默认为QUERY参数
            return OpenApiInfoVo.ParameterLocation.QUERY;
        }
    }

    /**
     * 提取响应信息(包含泛型和字段详情)
     */
    private OpenApiInfoVo.ResponseInfo extractResponseInfo(Method method) {
        try {
            Class<?> returnType = method.getReturnType();
            String fullType = returnType.getSimpleName();

            // 获取泛型信息
            String dataType = extractGenericType(method);
            List<OpenApiInfoVo.FieldInfo> fields = null;

            // 如果是复杂对象类型,提取字段信息
            if (StringUtils.isNotBlank(dataType) && !dataType.equals("?")) {
                Class<?> dataClass = resolveClass(dataType);
                if (dataClass != null) {
                    if (isComplexType(dataClass)) {
                        // 复杂对象类型：提取所有字段
                        fields = extractFieldsFromClass(dataClass);
                    } else {
                        // 基本类型（如Long、String等）：创建一个虚拟字段表示响应值
                        fields = createBasicTypeField(dataClass);
                    }
                }
            }

            return OpenApiInfoVo.ResponseInfo.builder()
                .fullType(fullType)
                .dataType(dataType)
                .fields(fields)
                .build();
        } catch (Exception e) {
            log.warn("提取响应信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 为基本类型创建虚拟字段
     * 用于表示简单类型的响应（如 R<Long>、R<String> 等）
     */
    private List<OpenApiInfoVo.FieldInfo> createBasicTypeField(Class<?> type) {
        String typeName = type.getSimpleName();
        String exampleValue = generateExampleValueForType(type);

        OpenApiInfoVo.FieldInfo field = OpenApiInfoVo.FieldInfo.builder()
            .name("value")
            .type(typeName)
            .required(true)
            .description("响应值")
            .example(exampleValue)
            .build();

        return List.of(field);
    }

    /**
     * 根据类型生成示例值
     */
    private String generateExampleValueForType(Class<?> type) {
        if (type.equals(String.class)) {
            return "示例文本";
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return "1";
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return "1";
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return "1.0";
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return "1.0";
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return "true";
        } else if (type.equals(BigDecimal.class)) {
            return "100.00";
        } else if (type.equals(Date.class) || type.equals(LocalDateTime.class)) {
            return "2024-01-01 12:00:00";
        } else if (type.equals(LocalDate.class)) {
            return "2024-01-01";
        }
        return "";
    }

    /**
     * 提取方法返回类型的泛型参数
     * 例如: R<PageResult<AdVo>> → PageResult<AdVo>
     */
    private String extractGenericType(Method method) {
        try {
            java.lang.reflect.Type returnType = method.getGenericReturnType();
            if (returnType instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) returnType;
                java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    java.lang.reflect.Type firstArg = typeArgs[0];
                    if (firstArg instanceof java.lang.reflect.ParameterizedType) {
                        // 嵌套泛型: R<PageResult<AdVo>>
                        java.lang.reflect.ParameterizedType nestedPt = (java.lang.reflect.ParameterizedType) firstArg;
                        String baseName = ((Class<?>) nestedPt.getRawType()).getSimpleName();
                        java.lang.reflect.Type[] nestedArgs = nestedPt.getActualTypeArguments();
                        if (nestedArgs.length > 0) {
                            String innerType = ((Class<?>) nestedArgs[0]).getSimpleName();
                            return baseName + "<" + innerType + ">";
                        }
                        return baseName;
                    } else if (firstArg instanceof Class<?>) {
                        // 单层泛型: R<AdVo>
                        return ((Class<?>) firstArg).getSimpleName();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取泛型参数失败: {}", e.getMessage());
        }
        return method.getReturnType().getSimpleName();
    }

    /**
     * 根据类名解析Class对象
     * 优先使用缓存，然后尝试直接加载，最后动态扫描 ClassPath
     */
    private Class<?> resolveClass(String className) {
        try {
            // 处理泛型类型: PageResult<AdVo> → AdVo
            if (className.contains("<") && className.contains(">")) {
                String innerType = className.substring(className.lastIndexOf("<") + 1, className.lastIndexOf(">"));
                className = innerType;
            }

            // 跳过基本类型和JDK内置类型 - 这些不需要解析
            if (isBasicOrBuiltInType(className)) {
                return null;
            }

            // 优先从缓存获取
            if (classCache.containsKey(className)) {
                return classCache.get(className);
            }

            // 尝试从当前ClassLoader加载
            try {
                Class<?> clazz = Class.forName(className);
                classCache.put(className, clazz);
                return clazz;
            } catch (ClassNotFoundException ignored) {
                // 继续使用扫描的方式
            }

            // 动态扫描 plus.ruoyi 包下的所有类
            Class<?> resolvedClass = scanAndResolveClass(className);
            if (resolvedClass != null) {
                classCache.put(className, resolvedClass);
                return resolvedClass;
            }

            // 基本类型和JDK内置类型不记录warn日志
            return null;
        } catch (Exception e) {
            log.debug("解析类时出错 [{}]: {}", className, e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否是基本类型或JDK内置类型
     */
    private boolean isBasicOrBuiltInType(String className) {
        // JDK基本类型
        if (className.equals("String") || className.equals("Integer") || className.equals("Long") ||
            className.equals("Double") || className.equals("Float") || className.equals("Boolean") ||
            className.equals("Byte") || className.equals("Short") || className.equals("Character") ||
            className.equals("BigDecimal") || className.equals("BigInteger") ||
            className.equals("Date") || className.equals("LocalDate") || className.equals("LocalDateTime") ||
            className.equals("LocalTime") || className.equals("Instant") ||
            className.equals("int") || className.equals("long") || className.equals("double") ||
            className.equals("float") || className.equals("boolean") || className.equals("byte") ||
            className.equals("short") || className.equals("char") ||
            className.equals("void") || className.equals("Void")) {
            return true;
        }

        // JDK内置集合和常用类
        if (className.startsWith("java.") || className.startsWith("javax.") ||
            className.startsWith("sun.") || className.startsWith("jdk.")) {
            return true;
        }

        return false;
    }

    /**
     * 动态扫描 ClassPath 中的类
     * 通过缓存避免重复扫描，提高性能
     */
    private Class<?> scanAndResolveClass(String className) {
        try {
            // 第一次调用时进行全量扫描并缓存结果
            if (!classPathScanInitialized) {
                synchronized (this) {
                    if (!classPathScanInitialized) {
                        log.info("开始扫描 ClassPath 中的 domain 类...");
                        long startTime = System.currentTimeMillis();

                        ClassPathScanningCandidateComponentProvider provider =
                            new ClassPathScanningCandidateComponentProvider(false);

                        // 添加过滤器：匹配 domain 包下的所有类
                        provider.addIncludeFilter(new RegexPatternTypeFilter(
                            Pattern.compile(".*domain\\..*")));

                        // 扫描 plus.ruoyi 包
                        classPathScanCache = provider.findCandidateComponents("plus/ruoyi");

                        long duration = System.currentTimeMillis() - startTime;
                        log.info("ClassPath 扫描完成，找到 {} 个 domain 类，耗时: {}ms",
                            classPathScanCache.size(), duration);

                        classPathScanInitialized = true;
                    }
                }
            }

            // 从缓存中查找类
            if (classPathScanCache != null) {
                for (BeanDefinition candidate : classPathScanCache) {
                    String beanClassName = candidate.getBeanClassName();
                    if (beanClassName != null && beanClassName.endsWith(className)) {
                        try {
                            Class<?> clazz = Class.forName(beanClassName);
                            log.debug("从缓存中找到类: {} -> {}", className, beanClassName);
                            return clazz;
                        } catch (ClassNotFoundException e) {
                            log.debug("找到类定义但加载失败: {}", beanClassName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("扫描 ClassPath 时出错: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 判断是否是复杂对象类型
     */
    private boolean isComplexType(Class<?> type) {
        String typeName = type.getSimpleName();
        // 基本类型和常用类型不算复杂类型
        if (type.isPrimitive() ||
            type.equals(String.class) ||
            type.equals(Integer.class) ||
            type.equals(Long.class) ||
            type.equals(Double.class) ||
            type.equals(Float.class) ||
            type.equals(Boolean.class) ||
            type.equals(BigDecimal.class) ||
            type.equals(Date.class) ||
            type.equals(LocalDate.class) ||
            type.equals(LocalDateTime.class) ||
            Collection.class.isAssignableFrom(type) ||
            Map.class.isAssignableFrom(type)) {
            return false;
        }
        // Bo、Vo、Query、DTO等结尾的是复杂对象
        return typeName.endsWith("Bo") ||
            typeName.endsWith("Vo") ||
            typeName.endsWith("Query") ||
            typeName.endsWith("DTO") ||
            typeName.endsWith("Form") ||
            typeName.endsWith("Request");
    }

    /**
     * 从类中提取字段信息
     */
    private List<OpenApiInfoVo.FieldInfo> extractFieldsFromClass(Class<?> clazz) {
        List<OpenApiInfoVo.FieldInfo> fieldInfos = new ArrayList<>();

        // 获取所有字段(包括父类字段)
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        for (Field field : allFields) {
            // 跳过静态字段和常量
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            String fieldType = field.getType().getSimpleName();

            // 检查是否必填
            NotNull notNull = field.getAnnotation(NotNull.class);
            NotBlank notBlank = field.getAnnotation(NotBlank.class);
            NotEmpty notEmpty = field.getAnnotation(NotEmpty.class);
            boolean required = notNull != null || notBlank != null || notEmpty != null;

            // 获取字段说明 - 优先使用 @Schema 注解
            String description = extractFieldDescription(field);

            // 生成示例值
            String example = generateExampleValue(field);

            fieldInfos.add(OpenApiInfoVo.FieldInfo.builder()
                .name(fieldName)
                .type(fieldType)
                .required(required)
                .description(description)
                .example(example)
                .build());
        }

        return fieldInfos;
    }

    /**
     * 提取字段说明 - 从 @Schema 注解获取
     * 注: 使用注解方式而非源代码注释，确保生产环境中也能正常工作
     */
    private String extractFieldDescription(Field field) {
        String description = "";

        // 从 @Schema 注解获取字段说明
        Schema schema = field.getAnnotation(Schema.class);
        if (schema != null) {
            String schemaDescription = schema.description();
            // 如果 description 有值,使用它
            if (StringUtils.isNotBlank(schemaDescription)) {
                return schemaDescription;
            }
            // 如果 description 为空,尝试使用 title
            String title = schema.title();
            if (StringUtils.isNotBlank(title)) {
                return title;
            }
        }

        // 如果没有注解,返回空字符串
        return description;
    }

    /**
     * 生成字段示例值
     */
    private String generateExampleValue(Field field) {
        Class<?> type = field.getType();

        // 检查Schema注解中的example
        Schema schema = field.getAnnotation(Schema.class);
        if (schema != null && StringUtils.isNotBlank(schema.example())) {
            return schema.example();
        }

        // 检查JsonFormat注解
        JsonFormat jsonFormat = field.getAnnotation(JsonFormat.class);
        if (jsonFormat != null && type.equals(LocalDateTime.class)) {
            return "2024-01-01 12:00:00";
        }
        if (jsonFormat != null && type.equals(LocalDate.class)) {
            return "2024-01-01";
        }

        // 根据类型生成默认示例
        if (type.equals(String.class)) {
            return "示例文本";
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return "1";
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return "1";
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return "1.0";
        } else if (type.equals(BigDecimal.class)) {
            return "100.00";
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return "true";
        } else if (type.equals(LocalDateTime.class)) {
            return "2024-01-01 12:00:00";
        } else if (type.equals(LocalDate.class)) {
            return "2024-01-01";
        } else if (type.equals(Date.class)) {
            return "2024-01-01 12:00:00";
        }

        return "";
    }

    /**
     * 检查用户是否有权限访问该接口
     */
    private boolean hasPermission(OpenApiInfoVo api,
                                  Set<String> userPermissions,
                                  Set<String> userRoles) {
        // 如果是无权限限制的接口,直接返回true
        if (Boolean.TRUE.equals(api.getNoAuth())) {
            return true;
        }

        // 检查是否是超级管理员(拥有所有权限)
        if (userRoles.contains("superadmin")) {
            return true;
        }

        // 检查是否有通配符权限(拥有所有权限)
        if (userPermissions.contains("*:*:*")) {
            return true;
        }

        // 检查权限
        if (StringUtils.isNotBlank(api.getPermission())) {
            String[] requiredPermissions = api.getPermission().split(",");
            String mode = api.getPermissionMode();

            // AND模式: 需要拥有所有权限
            if ("AND".equals(mode)) {
                boolean hasAll = true;
                for (String permission : requiredPermissions) {
                    if (!userPermissions.contains(permission.trim())) {
                        hasAll = false;
                        break;
                    }
                }
                if (hasAll) {
                    return true;
                }
            } else {
                // OR模式(默认): 拥有任意一个权限即可
                for (String permission : requiredPermissions) {
                    if (userPermissions.contains(permission.trim())) {
                        return true;
                    }
                }
            }
        }

        // 检查角色
        if (StringUtils.isNotBlank(api.getRoleCode())) {
            String[] requiredRoles = api.getRoleCode().split(",");
            String mode = api.getRoleMode();

            // AND模式: 需要拥有所有角色
            if ("AND".equals(mode)) {
                boolean hasAll = true;
                for (String role : requiredRoles) {
                    if (!userRoles.contains(role.trim())) {
                        hasAll = false;
                        break;
                    }
                }
                if (hasAll) {
                    return true;
                }
            } else {
                // OR模式(默认): 拥有任意一个角色即可
                for (String role : requiredRoles) {
                    if (userRoles.contains(role.trim())) {
                        return true;
                    }
                }
            }
        }

        // 如果既没有权限要求也没有角色要求,说明可能是公开接口
        if (StringUtils.isBlank(api.getPermission()) && StringUtils.isBlank(api.getRoleCode())) {
            return true;
        }

        return false;
    }
}
