package plus.ruoyi.common.openapi.interceptor;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.domain.vo.OpenApiVo;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.OpenApiService;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.openapi.annotation.OpenApi;
import plus.ruoyi.common.openapi.config.OpenApiProperties;
import plus.ruoyi.common.openapi.utils.OpenApiSignUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import plus.ruoyi.common.security.config.properties.SecurityProperties;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 开放 API 认证拦截器
 * <p>
 * 验证流程：
 * 1. 检查是否为 @OpenApi 接口
 * 2. 验证时间戳（防重放攻击）
 * 3. 验证签名防重复（Redis 缓存）
 * 4. 验证 AppKey 和 AppSecret
 * 5. 检查密钥状态和有效期
 * 6. 验证 IP 白名单
 * 7. 创建或获取 Token
 * 8. 设置租户上下文
 * 9. 记录调用统计
 *
 * @author 抓蛙师
 */
@Slf4j
public class OpenApiInterceptor implements HandlerInterceptor {

    private final OpenApiService openApiService;
    private final OpenApiProperties openApiProperties;
    private final SecurityProperties securityProperties;

    private static final String SIGN_CACHE_PREFIX = GlobalConstants.GLOBAL_REDIS_KEY + "openapi:sign:";
    private static final String TOKEN_CACHE_PREFIX = GlobalConstants.GLOBAL_REDIS_KEY + "openapi:token:";
    private static final String HEADER_APP_KEY = "X-App-Key";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_SIGN = "X-Sign";

    // URL 参数名
    private static final String PARAM_APP_KEY = "appKey";
    private static final String PARAM_TIMESTAMP = "timestamp";
    private static final String PARAM_SIGN = "sign";

    /**
     * 构造函数
     *
     * @param openApiService     开放 API 服务
     * @param openApiProperties  开放 API 配置属性
     * @param securityProperties 安全配置属性
     */
    public OpenApiInterceptor(OpenApiService openApiService,
                              OpenApiProperties openApiProperties,
                              SecurityProperties securityProperties) {
        this.openApiService = openApiService;
        this.openApiProperties = openApiProperties;
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只处理Controller方法
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查方法级别的 @SaIgnore
        if (handlerMethod.hasMethodAnnotation(SaIgnore.class)) {
            return true;
        }
        // 检查类级别的 @SaIgnore
        if (handlerMethod.getBeanType().isAnnotationPresent(SaIgnore.class)) {
            return true;
        }

        // 检查当前路径是否在 security.excludes 配置中
        String requestPath = request.getRequestURI();
        if (isPathExcluded(requestPath)) {
            return true;
        }

        // 检查是否是@OpenApi接口
        OpenApi openApi = handlerMethod.getMethodAnnotation(OpenApi.class);
        if (openApi == null) {
            openApi = handlerMethod.getBeanType().getAnnotation(OpenApi.class);
        }

        // 不是开放接口,跳过验证
        if (openApi == null) {
            return true;
        }

        // 如果用户已经通过普通方式登录（携带token），直接放行
        if (StpUtil.isLogin()) {
            log.debug("用户已登录，跳过 OpenAPI 认证");
            return true;
        }

        // 检查开放平台是否启用
        if (!openApiProperties.getEnabled()) {
            throw ServiceException.of("开放平台功能未启用");
        }

        // 1. 提取认证信息（兼容请求头和URL参数）
        String appKey = getParameter(request, HEADER_APP_KEY, PARAM_APP_KEY);
        String timestampStr = getParameter(request, HEADER_TIMESTAMP, PARAM_TIMESTAMP);
        String sign = getParameter(request, HEADER_SIGN, PARAM_SIGN);

        if (StringUtils.isAnyBlank(appKey, timestampStr, sign)) {
            throw ServiceException.of("缺少认证参数", HttpStatus.HTTP_UNAUTHORIZED);
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw ServiceException.of("时间戳格式错误");
        }

        // 2. 时间戳验证(防重放攻击)
        if (!OpenApiSignUtils.verifyTimestamp(timestamp, openApiProperties.getTimestampExpireSeconds())) {
            throw ServiceException.of("请求已过期");
        }

        // 3. 防重复签名(Redis缓存已验证的签名)
        String signCacheKey = SIGN_CACHE_PREFIX + sign;
        if (RedisUtils.hasKey(signCacheKey)) {
            throw ServiceException.of("请求重复");
        }

        // 4. 获取密钥信息(优先从缓存)
        OpenApiVo apiInfo = openApiService.getByAppKey(appKey);
        if (apiInfo == null) {
            throw ServiceException.of("无效的AppKey");
        }

        // 检查状态
        if (!DictEnableStatus.ENABLE.getValue().equals(apiInfo.getStatus())) {
            throw ServiceException.of("API密钥已禁用");
        }

        // 检查过期时间
        if (apiInfo.getExpireTime() != null && apiInfo.getExpireTime().before(new Date())) {
            throw ServiceException.of("API密钥已过期");
        }

        if (apiInfo.getUserId() == null) {
            throw ServiceException.of("API密钥未关联用户");
        }

        // 检查IP白名单
        if (StringUtils.isNotBlank(apiInfo.getWhiteIps())) {
            String clientIp = ServletUtils.getClientIP();
            List<String> whiteIpList = Arrays.asList(apiInfo.getWhiteIps().split(","));
            if (!whiteIpList.contains(clientIp)) {
                throw ServiceException.of("IP地址不在白名单中");
            }
        }

        // 5. 签名验证
        if (!OpenApiSignUtils.verifySign(appKey, timestampStr, apiInfo.getAppSecret(), sign)) {
            throw ServiceException.of("签名验证失败");
        }

        // 6. 获取或创建 Token
        String token = getOrCreateToken(apiInfo);

        // 7. 将 token 注入到请求头中（让 Sa-Token 能识别）
        request.setAttribute(SaTokenConsts.JUST_CREATED, SaManager.getConfig().getTokenPrefix() + " " + token);

        // 8. 设置租户上下文
        LoginUser loginUser = LoginHelper.getLoginUser(token);
        if (loginUser != null && StringUtils.isNotBlank(loginUser.getTenantId())) {
            TenantHelper.setDynamic(loginUser.getTenantId());
        }

        // 9. 缓存签名
        RedisUtils.setCacheObject(signCacheKey, "1",
            Duration.ofSeconds(openApiProperties.getTimestampExpireSeconds()));

        // 10. 异步更新调用统计
        CompletableFuture.runAsync(() -> openApiService.recordCall(appKey));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantHelper.clearDynamicLocal();
    }

    /**
     * 获取参数值（兼容请求头和URL参数）
     * <p>
     * 优先级：请求头 > URL参数
     * <p>
     * 使用场景：
     * 1. POST/PUT 请求：推荐使用请求头（更安全，不会在URL中暴露敏感信息）
     * 2. GET 请求：可以使用URL参数（方便直接在浏览器中测试）
     *
     * @param request    HTTP请求
     * @param headerName 请求头参数名
     * @param paramName  URL参数名
     * @return 参数值
     */
    private String getParameter(HttpServletRequest request, String headerName, String paramName) {
        // 优先从请求头获取
        String value = request.getHeader(headerName);

        if (StringUtils.isBlank(value)) {
            // 如果请求头没有，则从URL参数获取
            value = request.getParameter(paramName);
        }
        return value;
    }

    /**
     * 获取或创建 Token
     * 为 OpenAPI 请求创建一个长期有效的 token
     */
    private String getOrCreateToken(OpenApiVo apiInfo) {
        String tokenCacheKey = TOKEN_CACHE_PREFIX + apiInfo.getAppKey();

        // 1. 尝试从缓存获取已存在的 token
        String cachedToken = RedisUtils.getCacheObject(tokenCacheKey);

        // 2. 验证 token 是否仍然有效
        if (StringUtils.isNotBlank(cachedToken)) {
            try {
                // 使用正确的 API 检查指定 token 是否已被冻结
                if (!StpUtil.stpLogic.isFreeze(cachedToken)) {
                    // Token 仍然有效且未被冻结
                    log.debug("使用已存在的 OpenAPI Token: {}", cachedToken);

                    // Sa-Token 框架会在每次请求时自动调用 updateLastActiveToNow()
                    // 所以这里不需要手动刷新活跃时间，框架会自动续期

                    return cachedToken;
                } else {
                    // Token 已被冻结（超过 activeTimeout 未使用）
                    log.warn("缓存的 Token 已被冻结，将删除缓存并重新创建。AppKey: {}", apiInfo.getAppKey());
                    RedisUtils.deleteObject(tokenCacheKey);

                    // 清理被冻结的 token（可选，Sa-Token 会自动处理）
                    try {
                        StpUtil.logoutByTokenValue(cachedToken);
                    } catch (Exception e) {
                        log.debug("清理冻结 token 失败（可能已被清理）: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("验证已缓存的 token 失败，将重新创建。AppKey: {}, Error: {}",
                    apiInfo.getAppKey(), e.getMessage());
                RedisUtils.deleteObject(tokenCacheKey);
            }
        }

        // 2. Token 不存在或已失效，创建新的登录会话
        LoginUser loginUser = TenantHelper.dynamic(apiInfo.getTenantId(), () ->
            openApiService.getLoginUserByUserId(apiInfo.getUserId())
        );

        if (ObjectUtil.isNull(loginUser)) {
            throw ServiceException.of("关联用户不存在或已禁用");
        }

        // 3. 执行登录，创建 token
        UserType openapiUser = UserType.OPENAPI_USER;
        SaLoginParameter loginParameter = new SaLoginParameter();
        loginParameter.setDeviceType(openapiUser.getDeviceType());
        loginParameter.setTimeout(openapiUser.getTimeout());
        loginParameter.setActiveTimeout(openapiUser.getActiveTimeout());

        LoginHelper.login(loginUser, loginParameter);

        String newToken = StpUtil.getTokenValue();

        // 4. 缓存时间设置
        // 缓存时间应该略长于 activeTimeout（建议增加 10%~20% 的缓冲时间）
        // 这样可以避免缓存过早失效，同时确保缓存的 token 在活跃期内可用
        long activeTimeout = openapiUser.getActiveTimeout();
        long cacheTimeout = (long) (activeTimeout * 1.1);  // 增加 10% 缓冲

        RedisUtils.setCacheObject(tokenCacheKey, newToken, Duration.ofSeconds(cacheTimeout));

        log.info("为 OpenAPI [{}] 创建新 Token，activeTimeout: {} 秒，缓存时间: {} 秒",
            apiInfo.getAppKey(), activeTimeout, cacheTimeout);

        return newToken;
    }

    /**
     * 检查路径是否在排除列表中
     * 使用 Ant 风格的路径匹配，支持通配符：
     * - ? 匹配一个字符
     * - * 匹配零个或多个字符（不跨越目录）
     * - ** 匹配零个或多个目录
     *
     * @param requestPath 请求路径
     * @return true 表示在排除列表中，false 表示不在
     */
    private boolean isPathExcluded(String requestPath) {
        // 如果安全配置中没有排除路径，直接返回 false
        if (securityProperties == null || securityProperties.getExcludes() == null) {
            return false;
        }

        // 遍历所有排除路径，检查当前请求是否匹配
        return StringUtils.matchesAny(requestPath, Arrays.asList(securityProperties.getExcludes()));
    }

}
