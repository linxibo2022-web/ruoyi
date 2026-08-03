package plus.ruoyi.common.log.aspect;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.utils.ServletUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.log.event.OperLogEvent;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import java.util.*;

/**
 * 操作日志切面处理
 *
 * <p>通过 AOP 环绕通知拦截带有 @Log 注解的方法，记录操作日志信息，包括：
 * <ul>
 *   <li>操作人信息（用户名、租户、部门等）</li>
 *   <li>请求信息（IP、URL、请求方式、参数等）</li>
 *   <li>执行结果（成功/失败、返回值、异常信息）</li>
 *   <li>性能信息（方法执行耗时）</li>
 * </ul>
 *
 * <p>特性：
 * <ul>
 *   <li>支持敏感字段过滤（如密码字段）</li>
 *   <li>支持自定义排除参数</li>
 *   <li>支持普通用户和 OpenAPI 用户区分</li>
 *   <li>异步事件发布，不影响业务性能</li>
 * </ul>
 *
 * @author Lion Li
 */
@Slf4j
@Aspect
public class LogAspect {

    /**
     * 排除敏感属性字段
     * 这些字段在日志记录时会被自动过滤，防止敏感信息泄露
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 环绕通知：拦截带有 @Log 注解的方法
     *
     * <p>执行流程：
     * <ol>
     *   <li>启动计时器</li>
     *   <li>执行目标方法</li>
     *   <li>捕获异常（如有）</li>
     *   <li>停止计时并记录日志</li>
     *   <li>重新抛出异常（不影响业务流程）</li>
     * </ol>
     *
     * @param joinPoint     切点，包含目标方法的信息和参数
     * @param controllerLog 日志注解对象，包含日志配置信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常会被重新抛出
     */
    @Around("@annotation(controllerLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        // 开始计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = null;
        Exception exception = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            // 捕获异常，用于日志记录
            exception = e;
            // 重新抛出异常，保证业务流程不受影响
            throw e;
        } finally {
            // 停止计时
            stopWatch.stop();
            long costTime = stopWatch.getDuration().toMillis();

            // 记录操作日志（无论成功或失败都会执行）
            handleLog(joinPoint, controllerLog, exception, result, costTime);
        }
    }

    /**
     * 处理日志记录的核心方法
     *
     * <p>负责收集和组装操作日志信息，然后通过事件发布到消息队列，
     * 由异步监听器负责持久化到数据库，避免阻塞业务线程。
     *
     * @param joinPoint     切点对象，包含目标方法信息
     * @param controllerLog 日志注解配置
     * @param e             方法执行时抛出的异常（如果有）
     * @param jsonResult    方法执行的返回结果
     * @param costTime      方法执行耗时（毫秒）
     */
    protected void handleLog(final ProceedingJoinPoint joinPoint, Log controllerLog,
                             final Exception e, Object jsonResult, long costTime) {
        try {
            // ========== 初始化日志对象 ==========
            OperLogEvent operLog = new OperLogEvent();
            // 默认设置为成功状态
            operLog.setStatus(DictOperResult.SUCCESS.getValue());

            // ========== 设置请求基本信息 ==========
            // 获取客户端真实 IP 地址
            String ip = ServletUtils.getClientIP();
            operLog.setOperIp(ip);
            // 获取请求 URI，最长 255 字符
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));

            // ========== 设置操作人信息 ==========
            try {
                // 尝试获取当前登录用户信息
                LoginUser loginUser = LoginHelper.getLoginUser();
                if (ObjectUtil.isNotNull(loginUser)) {
                    // 设置租户信息（用于多租户系统）
                    operLog.setTenantId(loginUser.getTenantId());
                    // 设置部门名称
                    operLog.setDeptName(loginUser.getDeptName());

                    // 根据用户类型设置不同的操作人信息
                    UserType openapiUser = UserType.OPENAPI_USER;
                    if (openapiUser.getUserType().equals(loginUser.getUserType())) {
                        // OpenAPI 用户请求：格式为 "设备类型:用户名"
                        operLog.setOperName(openapiUser.getDeviceType() + ":" + loginUser.getUserName());
                        operLog.setOperatorType(openapiUser.getUserType());
                    } else {
                        // 普通用户请求
                        operLog.setOperName(loginUser.getUserName());
                        operLog.setOperatorType(loginUser.getUserType());
                    }
                } else {
                    // 未登录用户（如访问公开接口）
                    operLog.setOperName("匿名用户");
                    operLog.setOperatorType("anonymous");
                }
            } catch (Exception ex) {
                // 获取用户信息失败时，标记为匿名用户
                operLog.setOperName("匿名用户");
                operLog.setOperatorType("anonymous");
            }

            // ========== 设置异常信息 ==========
            if (e != null) {
                // 标记为失败状态
                operLog.setStatus(DictOperResult.FAIL.getValue());
                // 记录异常消息，最长 65535 字符
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 65535));
            }

            // ========== 设置方法信息 ==========
            // 获取完整的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取方法名
            String methodName = joinPoint.getSignature().getName();
            // 组装完整方法签名：包名.类名.方法名()
            operLog.setMethod(className + "." + methodName + "()");

            // ========== 设置请求方式 ==========
            // 如：GET、POST、PUT、DELETE 等
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());

            // ========== 处理注解配置的参数 ==========
            // 根据 @Log 注解的配置，决定是否保存请求参数和响应结果
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);

            // ========== 设置性能信息 ==========
            // 记录方法执行耗时（毫秒）
            operLog.setCostTime(costTime);

            // ========== 发布事件，异步保存到数据库 ==========
            // 通过 Spring 事件机制发布日志事件，由监听器异步处理
            SpringUtils.context().publishEvent(operLog);
        } catch (Exception exp) {
            // 日志记录本身出现异常时，只记录到本地日志，不影响业务
            log.error("日志记录异常:{}", exp.getMessage(), exp);
        }
    }

    /**
     * 根据注解配置提取方法描述信息
     *
     * <p>根据 @Log 注解的配置决定：
     * <ul>
     *   <li>是否保存请求参数</li>
     *   <li>是否保存响应结果</li>
     *   <li>哪些参数需要排除</li>
     * </ul>
     *
     * @param joinPoint     切点对象
     * @param logAnnotation 日志注解对象
     * @param operLog       操作日志事件对象
     * @param jsonResult    方法返回结果
     */
    public void getControllerMethodDescription(ProceedingJoinPoint joinPoint, Log logAnnotation,
                                               OperLogEvent operLog, Object jsonResult) {
        try {
            // 设置操作类型（如：增加、修改、删除、查询等）
            operLog.setOperType(logAnnotation.operType().getValue());
            // 设置业务标题（如：用户管理、角色管理等）
            operLog.setTitle(logAnnotation.title());
            // 设置操作人类别（保持与前面设置的一致）
            operLog.setOperatorType(operLog.getOperatorType());

            // 是否需要保存请求参数
            if (logAnnotation.isSaveRequestData()) {
                // 获取请求参数并保存，同时排除敏感字段和自定义排除字段
                setRequestValue(joinPoint, operLog, logAnnotation.excludeParamNames());
            }

            // 是否需要保存响应结果
            if (logAnnotation.isSaveResponseData() && ObjectUtil.isNotNull(jsonResult)) {
                // 将返回结果序列化为 JSON 字符串，最长 65535 字符
                operLog.setJsonResult(StringUtils.substring(JsonUtils.toJsonString(jsonResult), 0, 65535));
            }
        } catch (Exception e) {
            // 处理过程中出现异常，记录到本地日志
            log.error("获取方法描述异常:{}", e.getMessage(), e);
        }
    }

    /**
     * 获取请求参数并设置到日志对象中
     *
     * <p>参数获取策略：
     * <ul>
     *   <li>GET 请求：从 URL 查询参数中获取</li>
     *   <li>POST/PUT/DELETE 请求：从请求体中获取</li>
     *   <li>自动过滤敏感字段和排除字段</li>
     * </ul>
     *
     * @param joinPoint         切点对象，用于获取方法参数
     * @param operLog           操作日志对象
     * @param excludeParamNames 需要排除的参数名称数组
     */
    private void setRequestValue(ProceedingJoinPoint joinPoint, OperLogEvent operLog,
                                 String[] excludeParamNames) {
        try {
            // 获取 URL 查询参数（适用于 GET 请求）
            Map<String, String> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
            String requestMethod = operLog.getRequestMethod();

            // 对于 POST/PUT/DELETE 请求且查询参数为空时，从方法参数中获取
            if (MapUtil.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod,
                    HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {
                // 从方法参数数组中提取参数
                String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
                operLog.setOperParam(StringUtils.substring(params, 0, 65535));
            } else {
                // 从查询参数 Map 中移除敏感字段
                MapUtil.removeAny(paramsMap, EXCLUDE_PROPERTIES);
                // 移除自定义排除字段
                MapUtil.removeAny(paramsMap, excludeParamNames);
                // 将参数 Map 序列化为 JSON 字符串
                operLog.setOperParam(StringUtils.substring(JsonUtils.toJsonString(paramsMap), 0, 65535));
            }
        } catch (Exception e) {
            log.error("获取请求参数异常:{}", e.getMessage(), e);
        }
    }

    /**
     * 将方法参数数组转换为 JSON 字符串
     *
     * <p>处理逻辑：
     * <ul>
     *   <li>过滤掉不需要记录的对象（文件、Request、Response 等）</li>
     *   <li>对于 List 类型，遍历每个元素并过滤敏感字段</li>
     *   <li>对于普通对象，转换为 JSON 并过滤敏感字段</li>
     *   <li>多个参数之间用空格分隔</li>
     * </ul>
     *
     * @param paramsArray       方法参数数组
     * @param excludeParamNames 需要排除的字段名称数组
     * @return 参数的 JSON 字符串表示，多个参数用空格分隔
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        // 使用空格作为分隔符
        StringJoiner params = new StringJoiner(" ");

        // 参数数组为空时直接返回
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }

        // 合并默认排除字段和自定义排除字段
        String[] exclude = ArrayUtil.addAll(excludeParamNames, EXCLUDE_PROPERTIES);

        // 遍历每个参数
        for (Object o : paramsArray) {
            // 跳过 null 值和需要过滤的对象
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                String str = "";
                try {
                    // 处理 List 类型参数
                    if (o instanceof List<?> list) {
                        List<Dict> list1 = new ArrayList<>();
                        for (Object obj : list) {
                            // 将每个元素转换为 JSON
                            String str1 = JsonUtils.toJsonString(obj);
                            // 解析为 Map 以便过滤字段
                            Dict dict = JsonUtils.parseMap(str1);
                            if (MapUtil.isNotEmpty(dict)) {
                                // 移除敏感字段
                                MapUtil.removeAny(dict, exclude);
                                list1.add(dict);
                            }
                        }
                        // 将处理后的 List 序列化为 JSON
                        str = JsonUtils.toJsonString(list1);
                    } else {
                        // 处理普通对象
                        str = JsonUtils.toJsonString(o);
                        // 解析为 Map 以便过滤字段
                        Dict dict = JsonUtils.parseMap(str);
                        if (MapUtil.isNotEmpty(dict)) {
                            // 移除敏感字段
                            MapUtil.removeAny(dict, exclude);
                            str = JsonUtils.toJsonString(dict);
                        }
                    }
                    params.add(str);
                } catch (Exception e) {
                    // 参数序列化失败时记录错误，继续处理下一个参数
                    log.error("参数拼装异常:{}", e.getMessage());
                }
            }
        }
        return params.toString();
    }

    /**
     * 判断对象是否需要过滤（不记录到日志中）
     *
     * <p>以下类型的对象会被过滤：
     * <ul>
     *   <li>MultipartFile：文件上传对象（内容过大）</li>
     *   <li>HttpServletRequest：请求对象（包含大量无用信息）</li>
     *   <li>HttpServletResponse：响应对象（包含大量无用信息）</li>
     *   <li>BindingResult：参数绑定结果对象（Spring 内部使用）</li>
     *   <li>包含上述类型的数组、集合、Map</li>
     * </ul>
     *
     * @param o 需要判断的对象
     * @return true：需要过滤；false：不需要过滤
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();

        // 判断是否为数组类型
        if (clazz.isArray()) {
            // 判断数组元素类型是否为 MultipartFile
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        }
        // 判断是否为 Collection 类型
        else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            // 检查集合中是否包含 MultipartFile
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        }
        // 判断是否为 Map 类型
        else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            // 检查 Map 的值中是否包含 MultipartFile
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }

        // 判断是否为需要过滤的特定类型
        return o instanceof MultipartFile
                || o instanceof HttpServletRequest
                || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
