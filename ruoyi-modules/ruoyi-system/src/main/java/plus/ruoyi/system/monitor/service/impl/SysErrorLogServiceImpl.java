package plus.ruoyi.system.monitor.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.util.SaTokenConsts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerMapping;
import plus.ruoyi.common.core.constant.GlobalConstants;
import plus.ruoyi.common.core.domain.model.ErrorLogContext;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.ErrorLogService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.RequestIdUtils;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.monitor.dao.ISysErrorLogDao;
import plus.ruoyi.system.monitor.domain.SysErrorLog;
import plus.ruoyi.system.monitor.domain.bo.SysErrorLogBo;
import plus.ruoyi.system.monitor.domain.vo.SysErrorLogVo;
import plus.ruoyi.system.monitor.service.ISysErrorLogService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 错误日志服务实现
 *
 * @author 抓蛙师
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysErrorLogServiceImpl implements ISysErrorLogService, ErrorLogService {

    private final ISysErrorLogDao errorLogDao;

    /**
     * Redis 去重 Key 前缀
     */
    private static final String ERROR_DEDUP_PREFIX = GlobalConstants.GLOBAL_REDIS_KEY + "error:dedup:";

    /**
     * 去重时间窗口（秒）
     */
    private static final long DEDUP_WINDOW_SECONDS = 60;

    /**
     * 堆栈最大长度
     */
    private static final int MAX_STACK_LENGTH = 5000;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public SysErrorLogVo get(Long id) {
        SysErrorLog entity = errorLogDao.getById(id);
        return MapstructUtils.convert(entity, SysErrorLogVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysErrorLogVo> list(SysErrorLogBo bo) {
        PlusLambdaQuery<SysErrorLog> wrapper = errorLogDao.buildQueryWrapper(bo);
        List<SysErrorLog> entities = errorLogDao.list(wrapper);
        return MapstructUtils.convert(entities, SysErrorLogVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysErrorLogVo> page(SysErrorLogBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysErrorLog> wrapper = errorLogDao.buildQueryWrapper(bo);
        PageResult<SysErrorLog> entityPage = errorLogDao.page(wrapper, pageQuery);
        return entityPage.convert(SysErrorLogVo.class);
    }

    /**
     * 更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注）
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHandleStatus(SysErrorLogBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("ID不能为空");
        }
        if (!errorLogDao.exists(bo.getId())) {
            throw ServiceException.of("错误日志不存在");
        }

        return errorLogDao.updateHandleStatusById(
                bo.getId(),
                bo.getHandleStatus(),
                LoginHelper.getUserId(),
                new Date(),
                bo.getHandleRemark()
        );
    }

    /**
     * 按ID集合批量更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注、ID集合）
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHandleStatusBatch(SysErrorLogBo bo) {
        if (bo == null || CollUtil.isEmpty(bo.getIds())) {
            throw ServiceException.of("ID集合不能为空");
        }
        String handleStatus = resolveHandleStatus(bo);
        return errorLogDao.updateHandleStatusBatch(
                bo.getIds(),
                handleStatus,
                LoginHelper.getUserId(),
                new Date(),
                bo.getHandleRemark()
        );
    }

    /**
     * 按相同错误批量更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注、ID）
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHandleStatusBySameError(SysErrorLogBo bo) {
        SysErrorLog baseLog = getErrorLogForHandle(bo);
        String handleStatus = resolveHandleStatus(bo);
        String requestPattern = resolveRequestPattern(baseLog);
        String requestMethod = baseLog.getRequestMethod();

        if (StrUtil.isBlank(requestPattern)) {
            throw ServiceException.of("请求路径不能为空");
        }
        if (StrUtil.isBlank(requestMethod)) {
            throw ServiceException.of("请求方法不能为空");
        }
        if (StrUtil.isBlank(baseLog.getErrorType())) {
            throw ServiceException.of("异常类型不能为空");
        }

        return errorLogDao.updateHandleStatusBySameError(
                baseLog.getTenantId(),
                baseLog.getErrorType(),
                baseLog.getErrorCode(),
                baseLog.getErrorMessage(),
                requestPattern,
                requestMethod,
                handleStatus,
                LoginHelper.getUserId(),
                new Date(),
                bo.getHandleRemark()
        );
    }

    /**
     * 按相同接口批量更新处理状态
     *
     * @param bo 业务对象（包含处理状态、处理人、处理备注、ID）
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHandleStatusBySameRequest(SysErrorLogBo bo) {
        SysErrorLog baseLog = getErrorLogForHandle(bo);
        String handleStatus = resolveHandleStatus(bo);
        String requestPattern = resolveRequestPattern(baseLog);
        String requestMethod = baseLog.getRequestMethod();

        if (StrUtil.isBlank(requestPattern)) {
            throw ServiceException.of("请求路径不能为空");
        }
        if (StrUtil.isBlank(requestMethod)) {
            throw ServiceException.of("请求方法不能为空");
        }

        return errorLogDao.updateHandleStatusBySameRequest(
                baseLog.getTenantId(),
                requestPattern,
                requestMethod,
                handleStatus,
                LoginHelper.getUserId(),
                new Date(),
                bo.getHandleRemark()
        );
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        return errorLogDao.deleteByIds(ids);
    }

    /**
     * 清空错误日志
     */
    @Override
    public void clearErrorLogs() {
        errorLogDao.delete(null);
    }

    private SysErrorLog getErrorLogForHandle(SysErrorLogBo bo) {
        if (bo == null || bo.getId() == null) {
            throw ServiceException.of("ID不能为空");
        }
        SysErrorLog baseLog = errorLogDao.getById(bo.getId());
        if (baseLog == null) {
            throw ServiceException.of("错误日志不存在");
        }
        return baseLog;
    }

    private String resolveHandleStatus(SysErrorLogBo bo) {
        if (bo == null || StrUtil.isBlank(bo.getHandleStatus())) {
            throw ServiceException.of("处理状态不能为空");
        }
        return bo.getHandleStatus();
    }

    private String resolveRequestPattern(SysErrorLog errorLog) {
        if (errorLog == null) {
            return null;
        }
        if (StrUtil.isNotBlank(errorLog.getRequestPattern())) {
            return errorLog.getRequestPattern();
        }
        return errorLog.getRequestUri();
    }

    /**
     * 构建错误日志上下文快照
     *
     * @param exception 异常对象
     * @param request   请求对象
     * @return 错误日志上下文快照
     */
    @Override
    public ErrorLogContext buildContext(Exception exception, HttpServletRequest request) {
        ErrorLogContext context = new ErrorLogContext();
        if (exception == null) {
            return context;
        }

        context.setErrorLevel(determineSeverityLevel(exception));
        context.setErrorType(exception.getClass().getName());
        context.setErrorMessage(exception.getMessage());
        context.setErrorStack(getStackTrace(exception));

        if (exception instanceof ServiceException se) {
            context.setErrorCode(se.getBusinessCode() != null ? se.getBusinessCode().toString() : null);
        }

        if (request != null) {
            context.setTraceId(RequestIdUtils.getRequestId());
            context.setRequestUri(request.getRequestURI());
            context.setRequestPattern(resolveRequestPattern(request));
            context.setRequestMethod(request.getMethod());
            context.setRequestIp(ServletUtils.getClientIP(request));
            context.setRequestParams(truncateString(getRequestParams(request), 2000));
            context.setUserAgent(request.getHeader("User-Agent"));
            context.setClientVersion(request.getHeader("X-Client-Version"));
        }

        // 用户信息（可能未登录，需要安全获取）
        LoginUser loginUser = null;
        try {
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            log.debug("获取登录用户失败，可能用户未登录: {}", e.getMessage());
        }
        if (loginUser == null) {
            loginUser = resolveLoginUser(request);
        }
        if (loginUser != null) {
            context.setUserId(loginUser.getUserId());
            context.setUserName(loginUser.getUserName());
            context.setDeptId(loginUser.getDeptId());
        }

        context.setTenantId(TenantHelper.getTenantId());

        return context;
    }

    /**
     * 异步记录错误日志
     *
     * @param context 错误日志上下文快照
     */
    @Override
    @Async
    public void recordErrorAsync(ErrorLogContext context) {
        try {
            if (context == null) {
                return;
            }
            // 1. 构建错误标识（用于去重）
            String exceptionClass = context.getErrorType();
            String errorMessage = context.getErrorMessage();
            String requestUri = context.getRequestUri();
            String requestPattern = context.getRequestPattern();
            String dedupKey = buildDedupKey(exceptionClass, errorMessage, requestUri, requestPattern, context.getTenantId());

            // 2. 去重检查（最近 1 分钟内相同错误只记录一次，增加 occurrence_count）
            String redisKey = ERROR_DEDUP_PREFIX + dedupKey;
            Long existingErrorLogId = RedisUtils.getCacheObject(redisKey);
            if (existingErrorLogId != null) {
                // 如果 Redis 中存在该 key，说明 1 分钟内已记录过，尝试更新 occurrence_count 和 last_time
                int updated = errorLogDao.incrementOccurrenceCount(existingErrorLogId);
                if (updated > 0) {
                    log.debug("错误已在去重窗口内记录过，已更新统计: errorLogId={}", existingErrorLogId);
                    return;
                }
                // 原记录已被删除（如管理员清空日志），清除过期的 Redis key，继续新增流程
                RedisUtils.deleteObject(redisKey);
                log.debug("去重记录已失效（原日志可能已删除），将重新记录: errorLogId={}", existingErrorLogId);
            }

            // 3. 构建错误日志对象
            SysErrorLog sysErrorLog = new SysErrorLog();

            // 基本信息
            sysErrorLog.setErrorLevel(context.getErrorLevel());
            sysErrorLog.setErrorType(exceptionClass);
            sysErrorLog.setErrorMessage(truncateString(context.getErrorMessage(), 500));
            sysErrorLog.setErrorStack(truncateString(context.getErrorStack(), MAX_STACK_LENGTH));
            sysErrorLog.setErrorCode(context.getErrorCode());

            // 请求信息
            sysErrorLog.setTraceId(context.getTraceId());
            sysErrorLog.setRequestUri(requestUri);
            sysErrorLog.setRequestPattern(requestPattern);
            sysErrorLog.setRequestMethod(context.getRequestMethod());
            sysErrorLog.setRequestIp(context.getRequestIp());
            sysErrorLog.setRequestParams(context.getRequestParams());
            sysErrorLog.setUserAgent(context.getUserAgent());

            // 用户信息（可能未登录）
            sysErrorLog.setUserId(context.getUserId());
            sysErrorLog.setUserName(context.getUserName());
            sysErrorLog.setDeptId(context.getDeptId());
            sysErrorLog.setTenantId(context.getTenantId());

            // 平台类型（从 User-Agent 判断）
            sysErrorLog.setClientType(determinePlatformType(context.getUserAgent()));

            // 客户端版本（从请求头获取）
            sysErrorLog.setClientVersion(context.getClientVersion());

            // 模块信息（从 URI 推断）
            sysErrorLog.setModuleName(extractModuleName(requestUri));

            // 服务器环境信息
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                sysErrorLog.setServerName(localHost.getHostName());
                sysErrorLog.setServerIp(localHost.getHostAddress());
            } catch (Exception e) {
                log.debug("获取服务器信息失败: {}", e.getMessage());
            }

            // 应用版本信息
            sysErrorLog.setAppVersion(SpringUtil.getProperty("app.version"));

            // 初始化统计字段
            sysErrorLog.setOccurrenceCount(1);
            sysErrorLog.setFirstTime(new Date());
            sysErrorLog.setLastTime(new Date());

            // 处理状态
            sysErrorLog.setHandleStatus("0"); // 0-待处理

            // 审计字段手动填充（@Async 线程中 Sa-Token 上下文丢失，MP 自动填充可能失败）
            Date now = new Date();
            sysErrorLog.setCreateBy(context.getUserId());
            sysErrorLog.setCreateDept(context.getDeptId());
            sysErrorLog.setCreateTime(now);
            sysErrorLog.setUpdateBy(context.getUserId());
            sysErrorLog.setUpdateTime(now);

            // 4. 插入数据库
            errorLogDao.insert(sysErrorLog);

            // 5. 设置去重标记（1 分钟过期），存储错误日志 ID 用于后续更新
            RedisUtils.setCacheObject(redisKey, sysErrorLog.getId(), Duration.ofSeconds(DEDUP_WINDOW_SECONDS));

            log.debug("错误日志记录成功: {}", sysErrorLog.getId());

        } catch (Exception e) {
            // 记录错误日志本身失败，不应影响正常业务，只打印日志
            log.error("记录错误日志失败", e);
        }
    }

    /**
     * 构建去重 Key
     */
    private String buildDedupKey(String exceptionClass, String errorMessage, String requestUri, String requestPattern, String tenantId) {
        String routeKey = StrUtil.isNotBlank(requestPattern) ? requestPattern : requestUri;
        String rawKey = exceptionClass + "|" + (routeKey != null ? routeKey : "")
                + "|" + (errorMessage != null ? errorMessage : "") + "|" + (tenantId != null ? tenantId : "");
        return DigestUtil.md5Hex(rawKey);
    }

    private LoginUser resolveLoginUser(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return LoginHelper.getLoginUser(token);
    }

    /**
     * 从请求中解析 Token
     * <p>
     * 支持两种场景：
     * 1. OpenAPI 请求：从 request.getAttribute(JUST_CREATED) 获取
     * 2. 普通请求：从请求头获取
     *
     * @param request HTTP 请求
     * @return 纯 token（已去除前缀），如果未找到则返回 null
     */
    private String resolveToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 1. 优先从 JUST_CREATED 获取（OpenAPI 场景）
        Object justCreated = request.getAttribute(SaTokenConsts.JUST_CREATED);
        if (justCreated instanceof String) {
            return removeTokenPrefix((String) justCreated);
        }

        // 2. 从请求头获取（普通请求场景）
        String tokenName = SaManager.getConfig().getTokenName();
        String tokenValue = request.getHeader(tokenName);
        if (StrUtil.isNotBlank(tokenValue)) {
            return removeTokenPrefix(tokenValue);
        }

        return null;
    }

    /**
     * 去除 Token 前缀
     * <p>
     * 例如：将 "Bearer token123" 转换为 "token123"
     *
     * @param tokenValue 带前缀的 token
     * @return 纯 token
     */
    private String removeTokenPrefix(String tokenValue) {
        if (StrUtil.isBlank(tokenValue)) {
            return tokenValue;
        }

        String prefix = SaManager.getConfig().getTokenPrefix();
        if (StrUtil.isNotBlank(prefix)) {
            String prefixWithSpace = prefix + " ";
            if (StrUtil.startWithIgnoreCase(tokenValue, prefixWithSpace)) {
                return tokenValue.substring(prefixWithSpace.length());
            }
        }

        return tokenValue;
    }

    /**
     * 获取异常堆栈字符串
     */
    private String getStackTrace(Exception exception) {
        if (exception == null) {
            return null;
        }
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            exception.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return exception.toString();
        }
    }

    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (StrUtil.isBlank(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                return request.getQueryString();
            } else {
                // POST/PUT/DELETE 请求：尝试读取请求体（需要 RepeatedlyRequestWrapper 支持）
                String body = readRequestBody(request);
                if (StrUtil.isNotBlank(body)) {
                    return body;
                }
                // 降级：返回表单参数
                if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
                    return JsonUtils.toJsonString(request.getParameterMap());
                }
                return null;
            }
        } catch (Exception e) {
            log.debug("获取请求参数失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 读取请求体内容
     * <p>
     * 依赖 RepeatedlyRequestWrapper 支持重复读取，如果请求未被包装则可能读取失败。
     * 直接读 InputStream + UTF-8 解码，避免容器在 Content-Type 不带 charset 时
     * 按 ISO-8859-1 解码 getReader() 导致日志中文乱码。
     */
    private String readRequestBody(HttpServletRequest request) {
        try (InputStream is = request.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            is.transferTo(bos);
            return bos.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("读取请求体失败（可能未被 RepeatedlyRequestWrapper 包装）: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求路径模板
     */
    private String resolveRequestPattern(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern != null) {
            String patternValue = pattern.toString();
            if (StrUtil.isNotBlank(patternValue)) {
                return patternValue;
            }
        }
        return request.getRequestURI();
    }

    /**
     * 判断严重级别
     */
    private String determineSeverityLevel(Exception exception) {
        if (exception instanceof ServiceException) {
            return "WARN"; // 业务异常为警告级别
        }
        if (exception instanceof NullPointerException
                || exception instanceof IllegalArgumentException
                || exception instanceof IllegalStateException) {
            return "ERROR"; // 常见运行时异常为错误级别
        }
        return "ERROR"; // 默认错误级别
    }

    /**
     * 判断平台类型
     */
    private String determinePlatformType(String userAgent) {
        if (StrUtil.isBlank(userAgent)) {
            return "UNKNOWN";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("micromessenger")) {
            return "WECHAT";
        } else if (userAgent.contains("android")) {
            return "ANDROID";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "IOS";
        } else if (userAgent.contains("mobile")) {
            return "H5";
        } else {
            return "PC";
        }
    }

    /**
     * 从 URI 提取模块名
     */
    private String extractModuleName(String requestUri) {
        if (StrUtil.isBlank(requestUri)) {
            return null;
        }
        // URI 格式一般为 /模块名/控制器/方法
        String[] parts = requestUri.split("/");
        if (parts.length >= 2) {
            return parts[1]; // 返回第一级路径
        }
        return null;
    }
}
