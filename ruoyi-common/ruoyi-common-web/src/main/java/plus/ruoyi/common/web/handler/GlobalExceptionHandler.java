package plus.ruoyi.common.web.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.model.ErrorLogContext;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.exception.SseException;
import plus.ruoyi.common.core.exception.base.BaseException;
import plus.ruoyi.common.core.service.ErrorLogService;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常，提供友好的错误响应
 *
 * @author Lion Li
 */
@Slf4j
@RestControllerAdvice
@Order(Integer.MAX_VALUE)
public class GlobalExceptionHandler {

    /**
     * 处理HTTP请求方法不支持异常
     * 场景：客户端使用了不被支持的HTTP方法（如POST接口使用GET请求）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 405 方法不支持属客户端调用错误（4xx），用 warn 而非 error，避免淹没真正的服务端 error 日志
        log.warn("请求地址'{}',不支持'{}'请求", requestUri, e.getMethod());
        return R.fail(HttpStatus.HTTP_BAD_METHOD, e.getMessage());
    }

    /**
     * 处理业务逻辑异常
     * 场景：自定义的业务异常，通常包含特定的业务错误码
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        // 业务异常是预期内的校验结果（如"应用不存在""密码错误"），用 warn 而非 error；
        // 补上请求 URI，便于排查是哪个接口、何种业务校验触发（仅记 message，无需堆栈）
        log.warn("业务异常 => URL[{}] msg[{}]", request.getRequestURI(), e.getMessage());
        // 异步记录错误日志
        recordErrorLogAsync(e, request);
        Integer businessCode = e.getBusinessCode();
        // 如果有业务错误码则使用，否则使用默认错误响应
        return ObjectUtil.isNotNull(businessCode) ? R.fail(businessCode, e.getMessage()) : R.fail(e.getMessage());
    }

    /**
     * 处理SSE认证失败异常
     * 场景：Server-Sent Events连接时认证失败
     * 注意：返回String类型以适配SSE响应格式
     */
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(SseException.class)
    public String handleNotLoginException(SseException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.debug("请求地址'{}',认证失败'{}',无法访问系统资源", requestUri, e.getMessage());
        return JsonUtils.toJsonString(R.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源"));
    }

    /**
     * 处理Servlet异常
     * 场景：Servlet容器级别的异常
     */
    @ExceptionHandler(ServletException.class)
    public R<Void> handleServletException(ServletException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestUri, e);
        return R.fail("发生未知异常，请联系管理员");
    }

    /**
     * 处理基础业务异常
     * 场景：继承自BaseException的自定义异常
     */
    @ExceptionHandler(BaseException.class)
    public R<Void> handleBaseException(BaseException e, HttpServletRequest request) {
        // 基础业务异常同 ServiceException，属预期内控制流，降为 warn 并补请求 URI
        log.warn("基础业务异常 => URL[{}] msg[{}]", request.getRequestURI(), e.getMessage());
        // 异步记录错误日志
        recordErrorLogAsync(e, request);
        return R.fail(e.getMessage());
    }

    /**
     * 处理路径变量缺失异常
     * 场景：@PathVariable注解的参数在URL中缺失
     * 例如：接口定义为/user/{id}，但请求URL为/user/
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public R<Void> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 路径变量缺失属客户端请求错误（4xx），用 warn
        log.warn("请求路径中缺少必需的路径变量'{}'.", requestUri);
        return R.fail(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 处理方法参数类型不匹配异常
     * 场景：请求参数无法转换为目标类型
     * 例如：接口期望Integer类型，但传入了非数字字符串
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 参数类型不匹配属客户端请求错误（4xx），用 warn
        log.warn("请求参数类型不匹配'{}'.", requestUri);

        // 将Java类型转换为友好的中文描述
        String typeName = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型";
        String friendlyType = convertJavaTypeToFriendly(typeName);

        return R.fail(String.format("参数[%s]格式错误，应为%s类型，实际输入：%s",
                e.getName(), friendlyType, e.getValue()));
    }

    /**
     * 将Java类型转换为友好的中文描述
     *
     * @param javaType Java类型简单名称
     * @return 友好的中文类型描述
     */
    private String convertJavaTypeToFriendly(String javaType) {
        return switch (javaType) {
            case "Integer", "int" -> "整数";
            case "Long", "long" -> "长整数";
            case "Double", "double", "Float", "float" -> "数字";
            case "Boolean", "boolean" -> "布尔值(true/false)";
            case "String" -> "文本";
            case "Date", "LocalDate" -> "日期";
            case "LocalDateTime", "Instant", "ZonedDateTime" -> "日期时间";
            case "LocalTime" -> "时间";
            case "BigDecimal" -> "精确数字";
            default -> javaType; // 未匹配到的保留原类型名
        };
    }

    /**
     * 处理404异常 - 找不到处理器
     * 场景：请求的URL没有对应的Controller方法
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 404 路由不存在属客户端问题（含前端死链/旧路径），用 warn，避免淹没服务端故障
        log.warn("请求地址'{}'不存在.", requestUri);
        return R.fail(HttpStatus.HTTP_NOT_FOUND, "请求地址不存在");
    }

    /**
     * 处理静态资源不存在异常
     * 场景：访问的静态资源文件不存在（如图片、CSS、JS等）
     * 只记录警告信息，不抛出异常，避免污染日志
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.warn("静态资源不存在: {}", requestUri);
        return R.fail(HttpStatus.HTTP_NOT_FOUND, "静态资源不存在");
    }

    /**
     * 处理IO异常（特别处理SSE连接中断）
     * 场景：网络连接中断，特别是SSE长连接断开
     * 注意：SSE连接中断是正常现象，不返回错误响应
     */
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public void handleRuntimeException(IOException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("sse")) {
            // SSE连接经常性中断（如用户关闭浏览器），属于正常现象，直接忽略
            return;
        }
        log.error("请求地址'{}',连接中断", requestUri, e);
    }

    /**
     * sse 连接超时异常 不需要处理
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleRuntimeException(AsyncRequestTimeoutException e) {
    }

    /**
     * 处理运行时异常
     * 场景：未被其他异常处理器捕获的RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestUri, e);
        // 异步记录错误日志
        recordErrorLogAsync(e, request);
        return R.fail("发生未知异常，请联系管理员");
    }

    /**
     * 处理系统异常（兜底异常处理器）
     * 场景：所有未被上述处理器捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestUri, e);
        // 异步记录错误日志
        recordErrorLogAsync(e, request);
        return R.fail("发生系统异常，请联系管理员");
    }

    /**
     * 处理数据绑定异常
     * 场景：表单数据绑定到对象时发生的验证失败
     * 例如：@Valid注解的表单对象验证失败
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        // 表单数据绑定校验失败属客户端参数错误（4xx），用 warn
        log.warn("参数绑定校验失败: {}", e.getMessage());
        // 提取所有验证错误信息并拼接
        String message = StreamUtils.join(e.getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return R.fail(message);
    }

    /**
     * 处理约束违反异常
     * 场景：Bean Validation注解验证失败
     * 例如：@NotNull、@Size等注解验证不通过
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> constraintViolationException(ConstraintViolationException e) {
        // Bean Validation 约束校验失败属客户端参数错误（4xx），用 warn
        log.warn("参数约束校验失败: {}", e.getMessage());
        // 提取所有约束违反信息并拼接
        String message = StreamUtils.join(e.getConstraintViolations(), ConstraintViolation::getMessage, ", ");
        return R.fail(message);
    }

    /**
     * 处理方法参数验证异常
     * 场景：@RequestBody + @Valid注解的JSON对象验证失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // @RequestBody + @Valid 校验失败属客户端参数错误（4xx），用 warn
        log.warn("参数校验失败: {}", e.getMessage());
        // 提取绑定结果中的所有错误信息
        String message = StreamUtils.join(e.getBindingResult().getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return R.fail(message);
    }

    /**
     * 处理方法级别验证异常（Spring 6.1+）
     * 场景：Controller方法参数直接使用@NotNull、@Size等注解验证失败
     * 例如：public void test(@NotNull String name) {...}
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public R<Void> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        // 方法级参数校验失败属客户端参数错误（4xx），用 warn
        log.warn("方法参数校验失败: {}", e.getMessage());
        // 使用 getValueResults() 替代已废弃的 getAllValidationResults()
        String message = StreamUtils.join(e.getValueResults(), result ->
            StreamUtils.join(result.getResolvableErrors(), MessageSourceResolvable::getDefaultMessage, ", "), ", ");
        return R.fail(message);
    }

    /**
     * 处理JSON解析异常
     * 场景：请求体JSON格式错误，Jackson无法解析
     * 例如：JSON语法错误、缺少引号、括号不匹配等
     */
    @ExceptionHandler(JsonParseException.class)
    public R<Void> handleJsonParseException(JsonParseException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // JSON 解析失败属客户端请求体格式错误（4xx），用 warn
        log.warn("请求地址'{}' 发生 JSON 解析异常: {}", requestUri, e.getMessage());

        // 提取位置信息（行列号）
        String location = "";
        if (e.getLocation() != null) {
            location = String.format("(第%d行，第%d列)",
                    e.getLocation().getLineNr(),
                    e.getLocation().getColumnNr());
        }

        // 简化错误消息
        String simpleMessage = e.getOriginalMessage();
        if (simpleMessage == null || simpleMessage.isEmpty()) {
            simpleMessage = "JSON格式错误";
        }

        return R.fail(HttpStatus.HTTP_BAD_REQUEST,
                String.format("请求数据格式错误%s，请检查JSON格式是否正确", location));
    }

    /**
     * 处理HTTP消息读取异常
     * 场景：请求体无法读取或转换为目标对象
     * 例如：JSON字段类型不匹配、必填字段缺失等
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        // 请求体无法读取/转换属客户端请求错误（4xx，含请求体被截断），用 warn
        log.warn("请求地址'{}', 参数解析失败: {}", request.getRequestURI(), e.getMessage());

        // 获取根本原因
        Throwable cause = e.getMostSpecificCause();
        String errorMessage = cause.getMessage();

        // 解析并返回友好的错误消息
        String friendlyMessage = parseHttpMessageError(errorMessage);
        return R.fail(HttpStatus.HTTP_BAD_REQUEST, friendlyMessage);
    }

    /**
     * 异步记录错误日志到数据库
     * 使用 Spring 容器获取通用接口实现，避免循环依赖
     *
     * @param exception 异常对象
     * @param request   请求对象
     */
    private void recordErrorLogAsync(Exception exception, HttpServletRequest request) {
        try {
            // 延迟获取 Bean，避免循环依赖
            Map<String, ErrorLogService> beans = SpringUtils.getBeansOfType(ErrorLogService.class);
            if (beans.isEmpty()) {
                return;
            }
            ErrorLogService errorLogService = beans.values().iterator().next();
            ErrorLogContext context = errorLogService.buildContext(exception, request);
            if (context != null) {
                errorLogService.recordErrorAsync(context);
            }
        } catch (Exception e) {
            // 记录错误日志失败不应影响正常业务，只打印日志
            log.error("调用错误日志记录服务失败", e);
        }
    }

    /**
     * 解析HTTP消息读取错误，提取关键信息
     *
     * @param errorMessage 原始错误消息
     * @return 友好的错误提示
     */
    private String parseHttpMessageError(String errorMessage) {
        if (errorMessage == null) {
            return "请求参数格式错误";
        }

        // 缺少请求体
        if (errorMessage.contains("Required request body is missing")) {
            return "缺少请求体数据";
        }

        // JSON字段类型不匹配
        // 示例: Cannot deserialize value of type `java.lang.Integer` from String "abc": not a valid `java.lang.Integer` value
        if (errorMessage.contains("Cannot deserialize value of type")) {
            java.util.regex.Pattern typePattern = java.util.regex.Pattern.compile(
                "Cannot deserialize value of type `([^`]+)`.*from ([^:]+)",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher typeMatcher = typePattern.matcher(errorMessage);

            if (typeMatcher.find()) {
                String javaType = typeMatcher.group(1);
                String fromValue = typeMatcher.group(2);

                // 提取类型简单名称
                String simpleType = javaType.substring(javaType.lastIndexOf('.') + 1);
                String friendlyType = convertJavaTypeToFriendly(simpleType);

                return String.format("字段值格式错误，应为%s类型，实际输入：%s", friendlyType, fromValue.trim());
            }
            return "请求参数类型不匹配";
        }

        // 枚举值不匹配
        // 示例: not one of the values accepted for Enum class: [VALUE1, VALUE2]
        if (errorMessage.contains("not one of the values accepted for Enum")) {
            java.util.regex.Pattern enumPattern = java.util.regex.Pattern.compile(
                "\\[([^\\]]+)\\]",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher enumMatcher = enumPattern.matcher(errorMessage);

            if (enumMatcher.find()) {
                String acceptedValues = enumMatcher.group(1);
                return String.format("枚举值不正确，允许的值：[%s]", acceptedValues);
            }
            return "枚举值不正确";
        }

        // JSON语法错误
        if (errorMessage.contains("Unexpected character") ||
            errorMessage.contains("Unexpected end-of-input") ||
            errorMessage.contains("was expecting")) {
            return "JSON格式错误，请检查语法是否正确";
        }

        // 默认错误消息
        return "请求参数格式错误";
    }
}
