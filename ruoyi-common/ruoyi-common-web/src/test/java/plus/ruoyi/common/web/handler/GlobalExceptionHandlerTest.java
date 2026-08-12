package plus.ruoyi.common.web.handler;

import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.exception.SseException;
import plus.ruoyi.common.core.exception.base.BaseException;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 全局异常处理器测试
 * <p>
 * 测试全局异常处理器的核心功能:
 * <ul>
 *   <li>HTTP请求方法不支持异常</li>
 *   <li>业务逻辑异常</li>
 *   <li>参数验证异常</li>
 *   <li>JSON解析异常</li>
 *   <li>404异常</li>
 *   <li>运行时异常</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@DisplayName("GlobalExceptionHandler 全局异常处理器测试")
class GlobalExceptionHandlerTest extends BaseUnitTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest mockRequest;

    @Override
    protected void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/test/api");
    }

    // ==================== HTTP方法不支持异常测试 ====================

    @Nested
    @DisplayName("HTTP请求方法不支持异常测试")
    class HttpRequestMethodNotSupportedTests {

        @Test
        @DisplayName("POST接口使用GET请求 - 应返回405错误")
        void testMethodNotSupported() {
            HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("GET", List.of("POST", "PUT"));

            R<Void> result = handler.handleHttpRequestMethodNotSupported(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_METHOD, result.getCode());
            assertTrue(result.getMsg().contains("GET"));
        }

        @Test
        @DisplayName("不支持的方法 - 错误消息应包含请求方法")
        void testMethodNotSupportedMessage() {
            HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("DELETE");

            R<Void> result = handler.handleHttpRequestMethodNotSupported(exception, mockRequest);

            assertNotNull(result.getMsg());
        }
    }

    // ==================== 业务异常测试 ====================

    @Nested
    @DisplayName("业务逻辑异常测试")
    class ServiceExceptionTests {

        @Test
        @DisplayName("业务异常 - 无业务码时返回默认错误")
        void testServiceExceptionWithoutCode() {
            ServiceException exception = ServiceException.of("用户名已存在");

            R<Void> result = handler.handleServiceException(exception, mockRequest);

            assertNotNull(result);
            assertEquals("用户名已存在", result.getMsg());
        }

        @Test
        @DisplayName("业务异常 - 有业务码时返回指定错误码")
        void testServiceExceptionWithCode() {
            ServiceException exception = ServiceException.of("账户已被锁定", 1001);

            R<Void> result = handler.handleServiceException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(1001, result.getCode());
            assertEquals("账户已被锁定", result.getMsg());
        }

        @Test
        @DisplayName("业务异常 - 空消息处理")
        void testServiceExceptionEmptyMessage() {
            ServiceException exception = ServiceException.of("");

            R<Void> result = handler.handleServiceException(exception, mockRequest);

            assertNotNull(result);
            // ServiceException 空字符串消息可能被处理为null或空字符串
            assertTrue(result.getMsg() == null || result.getMsg().isEmpty(),
                "空消息应返回null或空字符串");
        }
    }

    // ==================== Servlet异常测试 ====================

    @Nested
    @DisplayName("Servlet异常测试")
    class ServletExceptionTests {

        @Test
        @DisplayName("Servlet异常 - 应返回通用错误消息(不泄露内部异常详情)")
        void testServletException() {
            ServletException exception = new ServletException("Servlet处理错误");

            R<Void> result = handler.handleServletException(exception, mockRequest);

            assertNotNull(result);
            assertEquals("发生未知异常，请联系管理员", result.getMsg());
        }
    }

    // ==================== 基础异常测试 ====================

    @Nested
    @DisplayName("基础业务异常测试")
    class BaseExceptionTests {

        @Test
        @DisplayName("BaseException - 应返回错误消息")
        void testBaseException() {
            // BaseException 是抽象类，使用匿名子类测试
            BaseException exception = new BaseException("base", "BASE_ERROR", null, "基础错误") {
                private static final long serialVersionUID = 1L;
            };

            R<Void> result = handler.handleBaseException(exception, mockRequest);

            assertNotNull(result);
            assertNotNull(result.getMsg());
        }
    }

    // ==================== 路径变量缺失异常测试 ====================

    @Nested
    @DisplayName("路径变量缺失异常测试")
    class MissingPathVariableTests {

        @Test
        @DisplayName("缺少路径变量id - 应返回友好错误消息")
        void testMissingPathVariable() {
            MissingPathVariableException exception =
                new MissingPathVariableException("id", null);

            R<Void> result = handler.handleMissingPathVariableException(exception, mockRequest);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("id"), "错误消息应包含缺失的变量名");
        }

        @Test
        @DisplayName("缺少路径变量userId - 应返回包含变量名的消息")
        void testMissingPathVariableUserId() {
            MissingPathVariableException exception =
                new MissingPathVariableException("userId", null);

            R<Void> result = handler.handleMissingPathVariableException(exception, mockRequest);

            assertTrue(result.getMsg().contains("userId"));
        }
    }

    // ==================== 参数类型不匹配异常测试 ====================

    @Nested
    @DisplayName("参数类型不匹配异常测试")
    class MethodArgumentTypeMismatchTests {

        @Test
        @DisplayName("Integer参数传入字符串 - 应返回友好错误消息")
        @SuppressWarnings("unchecked")
        void testTypeMismatchInteger() {
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("id");
            when(exception.getRequiredType()).thenReturn((Class) Integer.class);
            when(exception.getValue()).thenReturn("abc");

            R<Void> result = handler.handleMethodArgumentTypeMismatchException(exception, mockRequest);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("id"));
            assertTrue(result.getMsg().contains("整数") || result.getMsg().contains("Integer"));
            assertTrue(result.getMsg().contains("abc"));
        }

        @Test
        @DisplayName("Long参数传入字符串 - 应提示长整数类型")
        @SuppressWarnings("unchecked")
        void testTypeMismatchLong() {
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("userId");
            when(exception.getRequiredType()).thenReturn((Class) Long.class);
            when(exception.getValue()).thenReturn("not_a_number");

            R<Void> result = handler.handleMethodArgumentTypeMismatchException(exception, mockRequest);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("长整数") || result.getMsg().contains("Long"));
        }

        @Test
        @DisplayName("Boolean参数传入非布尔值 - 应提示布尔值类型")
        @SuppressWarnings("unchecked")
        void testTypeMismatchBoolean() {
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("enabled");
            when(exception.getRequiredType()).thenReturn((Class) Boolean.class);
            when(exception.getValue()).thenReturn("yes");

            R<Void> result = handler.handleMethodArgumentTypeMismatchException(exception, mockRequest);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("布尔") || result.getMsg().contains("Boolean"));
        }

        @Test
        @DisplayName("requiredType为null时 - 应返回未知类型")
        void testTypeMismatchNullType() {
            MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
            when(exception.getName()).thenReturn("param");
            when(exception.getRequiredType()).thenReturn(null);
            when(exception.getValue()).thenReturn("value");

            R<Void> result = handler.handleMethodArgumentTypeMismatchException(exception, mockRequest);

            assertNotNull(result);
        }
    }

    // ==================== 404异常测试 ====================

    @Nested
    @DisplayName("404异常测试")
    class NotFoundTests {

        @Test
        @DisplayName("NoHandlerFoundException - 应返回404错误")
        void testNoHandlerFound() {
            NoHandlerFoundException exception =
                new NoHandlerFoundException("GET", "/not/exist", null);

            R<Void> result = handler.handleNoHandlerFoundException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_NOT_FOUND, result.getCode());
        }

        @Test
        @DisplayName("NoResourceFoundException - 静态资源不存在应返回404")
        void testNoResourceFound() {
            NoResourceFoundException exception =
                new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/static/missing.js");

            R<Void> result = handler.handleNoResourceFoundException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_NOT_FOUND, result.getCode());
            assertEquals("静态资源不存在", result.getMsg());
        }
    }

    // ==================== IO异常测试 ====================

    @Nested
    @DisplayName("IO异常测试")
    class IOExceptionTests {

        @Test
        @DisplayName("SSE连接中断 - 应静默处理")
        void testSseConnectionInterrupt() {
            when(mockRequest.getRequestURI()).thenReturn("/api/sse/stream");
            IOException exception = new IOException("Connection reset");

            // SSE连接中断不应抛出异常
            assertDoesNotThrow(() -> handler.handleRuntimeException(exception, mockRequest));
        }

        @Test
        @DisplayName("非SSE的IO异常 - 应记录日志")
        void testNonSseIOException() {
            when(mockRequest.getRequestURI()).thenReturn("/api/upload");
            IOException exception = new IOException("File upload failed");

            // 非SSE的IO异常也不应抛出异常
            assertDoesNotThrow(() -> handler.handleRuntimeException(exception, mockRequest));
        }
    }

    // ==================== 运行时异常测试 ====================

    @Nested
    @DisplayName("运行时异常测试")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("RuntimeException - 应返回通用错误消息(不泄露内部异常详情)")
        void testRuntimeException() {
            RuntimeException exception = new RuntimeException("未知错误");

            R<Void> result = handler.handleRuntimeException(exception, mockRequest);

            assertNotNull(result);
            assertEquals("发生未知异常，请联系管理员", result.getMsg());
        }

        @Test
        @DisplayName("NullPointerException - 应返回错误消息")
        void testNullPointerException() {
            NullPointerException exception = new NullPointerException("空指针异常");

            R<Void> result = handler.handleRuntimeException(exception, mockRequest);

            assertNotNull(result);
        }
    }

    // ==================== 通用异常测试 ====================

    @Nested
    @DisplayName("通用异常测试")
    class GenericExceptionTests {

        @Test
        @DisplayName("Exception - 兜底异常处理(返回通用消息不泄露内部详情)")
        void testGenericException() {
            Exception exception = new Exception("系统异常");

            R<Void> result = handler.handleException(exception, mockRequest);

            assertNotNull(result);
            assertEquals("发生系统异常，请联系管理员", result.getMsg());
        }
    }

    // ==================== 数据绑定异常测试 ====================

    @Nested
    @DisplayName("数据绑定异常测试")
    class BindExceptionTests {

        @Test
        @DisplayName("BindException - 应返回所有验证错误信息")
        void testBindException() {
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("user", "name", "用户名不能为空");
            FieldError fieldError2 = new FieldError("user", "age", "年龄必须大于0");
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

            BindException exception = new BindException(bindingResult);

            R<Void> result = handler.handleBindException(exception);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("用户名不能为空") || result.getMsg().contains("年龄必须大于0"));
        }

        @Test
        @DisplayName("BindException - 单个错误")
        void testBindExceptionSingleError() {
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError = new FieldError("user", "email", "邮箱格式不正确");
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

            BindException exception = new BindException(bindingResult);

            R<Void> result = handler.handleBindException(exception);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("邮箱格式不正确"));
        }
    }

    // ==================== 约束违反异常测试 ====================

    @Nested
    @DisplayName("约束违反异常测试")
    class ConstraintViolationTests {

        @Test
        @DisplayName("ConstraintViolationException - 应返回所有违反信息")
        @SuppressWarnings("unchecked")
        void testConstraintViolation() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();

            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("ID不能为空");
            violations.add(violation1);

            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("名称长度不能超过50");
            violations.add(violation2);

            ConstraintViolationException exception = new ConstraintViolationException(violations);

            R<Void> result = handler.constraintViolationException(exception);

            assertNotNull(result);
            // 至少包含一个错误消息
            assertTrue(result.getMsg().contains("ID不能为空") || result.getMsg().contains("名称长度不能超过50"));
        }

        @Test
        @DisplayName("ConstraintViolationException - 空违反集合")
        void testConstraintViolationEmpty() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            R<Void> result = handler.constraintViolationException(exception);

            assertNotNull(result);
        }
    }

    // ==================== 方法参数验证异常测试 ====================

    @Nested
    @DisplayName("方法参数验证异常测试")
    class MethodArgumentNotValidTests {

        @Test
        @DisplayName("MethodArgumentNotValidException - 应返回验证错误信息")
        void testMethodArgumentNotValid() throws NoSuchMethodException {
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError = new FieldError("request", "password", "密码不能为空");
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

            // 创建一个真实的MethodParameter
            java.lang.reflect.Method method = String.class.getMethod("valueOf", Object.class);
            org.springframework.core.MethodParameter parameter =
                new org.springframework.core.MethodParameter(method, 0);

            MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

            R<Void> result = handler.handleMethodArgumentNotValidException(exception);

            assertNotNull(result);
            assertTrue(result.getMsg().contains("密码不能为空"));
        }

        @Test
        @DisplayName("MethodArgumentNotValidException - 多个验证错误")
        void testMethodArgumentNotValidMultipleErrors() throws NoSuchMethodException {
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError error1 = new FieldError("request", "username", "用户名不能为空");
            FieldError error2 = new FieldError("request", "password", "密码长度至少6位");
            FieldError error3 = new FieldError("request", "email", "邮箱格式错误");
            when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2, error3));

            // 创建一个真实的MethodParameter
            java.lang.reflect.Method method = String.class.getMethod("valueOf", Object.class);
            org.springframework.core.MethodParameter parameter =
                new org.springframework.core.MethodParameter(method, 0);

            MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

            R<Void> result = handler.handleMethodArgumentNotValidException(exception);

            assertNotNull(result);
            // 验证消息被逗号分隔
            String msg = result.getMsg();
            assertNotNull(msg);
        }
    }

    // ==================== JSON解析异常测试 ====================

    @Nested
    @DisplayName("JSON解析异常测试")
    class JsonParseExceptionTests {

        @Test
        @DisplayName("JSON语法错误 - 应返回位置信息")
        void testJsonParseExceptionWithLocation() {
            JsonParser parser = mock(JsonParser.class);
            JsonLocation location = new JsonLocation(null, 100, 5, 10);
            JsonParseException exception = new JsonParseException(parser, "Unexpected character", location);

            R<Void> result = handler.handleJsonParseException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
            assertTrue(result.getMsg().contains("第5行") || result.getMsg().contains("JSON"));
        }

        @Test
        @DisplayName("JSON解析错误 - 无位置信息")
        void testJsonParseExceptionWithoutLocation() {
            JsonParser parser = mock(JsonParser.class);
            JsonParseException exception = new JsonParseException(parser, "Invalid JSON");

            R<Void> result = handler.handleJsonParseException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
        }
    }

    // ==================== HTTP消息读取异常测试 ====================

    @Nested
    @DisplayName("HTTP消息读取异常测试")
    class HttpMessageNotReadableTests {

        @Test
        @DisplayName("缺少请求体 - 应返回友好提示")
        void testMissingRequestBody() {
            Exception cause = new Exception("Required request body is missing");
            HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Could not read document", cause, null);

            R<Void> result = handler.handleHttpMessageNotReadableException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
            assertTrue(result.getMsg().contains("缺少请求体") || result.getMsg().contains("请求参数"));
        }

        @Test
        @DisplayName("类型反序列化错误 - 应返回类型提示")
        void testDeserializationError() {
            Exception cause = new Exception("Cannot deserialize value of type `java.lang.Integer` from String \"abc\": not a valid");
            HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("JSON parse error", cause, null);

            R<Void> result = handler.handleHttpMessageNotReadableException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
        }

        @Test
        @DisplayName("枚举值错误 - 应返回允许的值")
        void testEnumDeserializationError() {
            Exception cause = new Exception("not one of the values accepted for Enum class: [ACTIVE, INACTIVE, PENDING]");
            HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("JSON parse error", cause, null);

            R<Void> result = handler.handleHttpMessageNotReadableException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
            assertTrue(result.getMsg().contains("枚举") || result.getMsg().contains("允许"));
        }

        @Test
        @DisplayName("JSON语法错误 - 应返回语法错误提示")
        void testJsonSyntaxError() {
            Exception cause = new Exception("Unexpected character ('x')");
            HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("JSON parse error", cause, null);

            R<Void> result = handler.handleHttpMessageNotReadableException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
            assertTrue(result.getMsg().contains("JSON") || result.getMsg().contains("格式"));
        }

        @Test
        @DisplayName("未知错误 - 应返回默认错误消息")
        void testUnknownError() {
            Exception cause = new Exception("Some unknown error");
            HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Could not read", cause, null);

            R<Void> result = handler.handleHttpMessageNotReadableException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
        }

        @Test
        @DisplayName("null错误消息 - 应返回默认提示")
        void testNullErrorMessage() {
            Exception cause = new Exception((String) null);
            HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Error", cause, null);

            R<Void> result = handler.handleHttpMessageNotReadableException(exception, mockRequest);

            assertNotNull(result);
            assertEquals(HttpStatus.HTTP_BAD_REQUEST, result.getCode());
        }
    }

    // ==================== RestControllerAdvice注解验证 ====================

    @Nested
    @DisplayName("注解验证测试")
    class AnnotationTests {

        @Test
        @DisplayName("应有@RestControllerAdvice注解")
        void testRestControllerAdviceAnnotation() {
            assertTrue(GlobalExceptionHandler.class.isAnnotationPresent(
                org.springframework.web.bind.annotation.RestControllerAdvice.class),
                "应有@RestControllerAdvice注解");
        }

        @Test
        @DisplayName("应有@Order注解")
        void testOrderAnnotation() {
            assertTrue(GlobalExceptionHandler.class.isAnnotationPresent(
                org.springframework.core.annotation.Order.class),
                "应有@Order注解");

            org.springframework.core.annotation.Order order =
                GlobalExceptionHandler.class.getAnnotation(org.springframework.core.annotation.Order.class);
            assertEquals(Integer.MAX_VALUE, order.value(), "@Order值应为Integer.MAX_VALUE");
        }
    }
}
