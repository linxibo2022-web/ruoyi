package plus.ruoyi.common.sms.handler;

import cn.hutool.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.test.base.BaseUnitTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SmsExceptionHandler 短信异常处理器测试
 * <p>
 * 测试短信异常处理器的核心功能:
 * <ul>
 *   <li>捕获 SmsBlendException 异常</li>
 *   <li>返回统一的错误响应格式</li>
 *   <li>记录错误日志信息</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("SmsExceptionHandler 短信异常处理器测试")
class SmsExceptionHandlerTest extends BaseUnitTest {

    private SmsExceptionHandler smsExceptionHandler;

    @Override
    protected void setUp() {
        smsExceptionHandler = new SmsExceptionHandler();
    }

    // ==================== handleSmsBlendException 测试 ====================

    @Nested
    @DisplayName("短信发送异常处理测试")
    class HandleSmsBlendExceptionTests {

        @Test
        @DisplayName("处理短信发送异常 - 应返回500错误码")
        void testHandleSmsBlendExceptionReturnsInternalError() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            SmsBlendException exception = new SmsBlendException("短信发送失败");

            R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertNotNull(result, "返回结果不应为null");
            assertEquals(HttpStatus.HTTP_INTERNAL_ERROR, result.getCode(), "应返回500错误码");
        }

        @Test
        @DisplayName("处理短信发送异常 - 应返回友好错误信息")
        void testHandleSmsBlendExceptionReturnsErrorMessage() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            SmsBlendException exception = new SmsBlendException("连接超时");

            R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertNotNull(result, "返回结果不应为null");
            assertEquals("短信发送失败，请稍后再试...", result.getMsg(), "应返回友好错误信息");
        }

        @Test
        @DisplayName("处理短信发送异常 - 应获取请求URI")
        void testHandleSmsBlendExceptionGetsRequestUri() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/business/sms/verify");

            SmsBlendException exception = new SmsBlendException("验证码发送失败");

            smsExceptionHandler.handleSmsBlendException(exception, request);

            // 验证获取了请求URI（用于日志记录）
            verify(request, times(1)).getRequestURI();
        }

        @Test
        @DisplayName("处理短信发送异常 - 不同请求路径")
        void testHandleSmsBlendExceptionWithDifferentPaths() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            SmsBlendException exception = new SmsBlendException("短信服务不可用");

            // 测试不同的请求路径
            String[] paths = {
                "/api/v1/sms/send",
                "/system/sms/batch",
                "/mobile/sms/verify",
                "/admin/sms/template"
            };

            for (String path : paths) {
                when(request.getRequestURI()).thenReturn(path);

                R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

                assertNotNull(result, "路径 " + path + " 返回结果不应为null");
                assertEquals(HttpStatus.HTTP_INTERNAL_ERROR, result.getCode(),
                    "路径 " + path + " 应返回500错误码");
            }
        }

        @Test
        @DisplayName("处理短信发送异常 - 带有嵌套异常")
        void testHandleSmsBlendExceptionWithCause() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            // 创建带有根因的异常
            RuntimeException cause = new RuntimeException("网络连接失败");
            SmsBlendException exception = new SmsBlendException(cause.getMessage());

            R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertNotNull(result, "返回结果不应为null");
            assertEquals(HttpStatus.HTTP_INTERNAL_ERROR, result.getCode(), "应返回500错误码");
            assertEquals("短信发送失败，请稍后再试...", result.getMsg(), "无论具体原因，都应返回友好信息");
        }

        @Test
        @DisplayName("处理短信发送异常 - 空异常消息")
        void testHandleSmsBlendExceptionWithEmptyMessage() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            SmsBlendException exception = new SmsBlendException("");

            R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertNotNull(result, "返回结果不应为null");
            assertEquals(HttpStatus.HTTP_INTERNAL_ERROR, result.getCode(), "应返回500错误码");
            assertEquals("短信发送失败，请稍后再试...", result.getMsg(), "即使异常消息为空也应返回友好信息");
        }

        @Test
        @DisplayName("处理短信发送异常 - null异常消息")
        void testHandleSmsBlendExceptionWithNullMessage() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            SmsBlendException exception = new SmsBlendException((String) null);

            R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertNotNull(result, "返回结果不应为null");
            assertEquals(HttpStatus.HTTP_INTERNAL_ERROR, result.getCode(), "应返回500错误码");
        }
    }

    // ==================== 响应结果验证测试 ====================

    @Nested
    @DisplayName("响应结果格式验证")
    class ResponseFormatTests {

        @Test
        @DisplayName("响应结果 - data 应为 null")
        void testResponseDataIsNull() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            SmsBlendException exception = new SmsBlendException("测试异常");

            R<Void> result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertNull(result.getData(), "错误响应的 data 应为 null");
        }

        @Test
        @DisplayName("响应结果 - 返回类型为 R<Void>")
        void testResponseTypeIsRVoid() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn("/api/sms/send");

            SmsBlendException exception = new SmsBlendException("测试异常");

            Object result = smsExceptionHandler.handleSmsBlendException(exception, request);

            assertTrue(result instanceof R, "返回类型应为 R");
        }
    }

    // ==================== @RestControllerAdvice 注解验证 ====================

    @Nested
    @DisplayName("注解配置验证")
    class AnnotationTests {

        @Test
        @DisplayName("类应有 @RestControllerAdvice 注解")
        void testHasRestControllerAdviceAnnotation() {
            assertTrue(
                SmsExceptionHandler.class.isAnnotationPresent(
                    org.springframework.web.bind.annotation.RestControllerAdvice.class
                ),
                "SmsExceptionHandler 应有 @RestControllerAdvice 注解"
            );
        }

        @Test
        @DisplayName("handleSmsBlendException 方法应有 @ExceptionHandler 注解")
        void testHandleMethodHasExceptionHandlerAnnotation() throws NoSuchMethodException {
            var method = SmsExceptionHandler.class.getMethod(
                "handleSmsBlendException",
                SmsBlendException.class,
                HttpServletRequest.class
            );

            assertTrue(
                method.isAnnotationPresent(
                    org.springframework.web.bind.annotation.ExceptionHandler.class
                ),
                "handleSmsBlendException 方法应有 @ExceptionHandler 注解"
            );

            // 验证注解配置的异常类型
            var annotation = method.getAnnotation(
                org.springframework.web.bind.annotation.ExceptionHandler.class
            );
            Class<?>[] exceptionTypes = annotation.value();

            assertEquals(1, exceptionTypes.length, "应只处理一种异常类型");
            assertEquals(SmsBlendException.class, exceptionTypes[0], "应处理 SmsBlendException");
        }
    }
}
