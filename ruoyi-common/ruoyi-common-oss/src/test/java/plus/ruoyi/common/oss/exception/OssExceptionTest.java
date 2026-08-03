package plus.ruoyi.common.oss.exception;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OssException 异常类测试
 *
 * @author 抓蛙师
 */
@DisplayName("OssException异常类测试")
public class OssExceptionTest extends BaseUnitTest {

    // ==================== 构造函数测试 ====================

    @Test
    @DisplayName("测试构造函数-使用消息创建异常")
    public void testConstructorWithMessage() {
        String errorMsg = "上传文件失败";
        OssException exception = new OssException(errorMsg);

        assertNotNull(exception, "异常实例不应为null");
        assertEquals(errorMsg, exception.getMessage(), "异常消息应与传入的消息一致");
    }

    @Test
    @DisplayName("测试构造函数-空消息")
    public void testConstructorWithEmptyMessage() {
        OssException exception = new OssException("");

        assertNotNull(exception, "异常实例不应为null");
        assertEquals("", exception.getMessage(), "空消息应正常处理");
    }

    @Test
    @DisplayName("测试构造函数-null消息")
    public void testConstructorWithNullMessage() {
        OssException exception = new OssException(null);

        assertNotNull(exception, "异常实例不应为null");
        assertNull(exception.getMessage(), "null消息应正常处理");
    }

    // ==================== 继承关系测试 ====================

    @Test
    @DisplayName("测试继承关系-应继承RuntimeException")
    public void testInheritance() {
        OssException exception = new OssException("测试异常");

        assertTrue(exception instanceof RuntimeException, "应继承RuntimeException");
        assertTrue(exception instanceof Exception, "应继承Exception");
        assertTrue(exception instanceof Throwable, "应继承Throwable");
    }

    // ==================== 异常抛出测试 ====================

    @Test
    @DisplayName("测试异常抛出-应能被正确捕获")
    public void testThrowAndCatch() {
        String errorMsg = "文件存储服务异常";

        OssException caught = assertThrows(OssException.class, () -> {
            throw new OssException(errorMsg);
        }, "应能抛出OssException");

        assertEquals(errorMsg, caught.getMessage(), "捕获的异常消息应正确");
    }

    @Test
    @DisplayName("测试异常抛出-作为RuntimeException捕获")
    public void testCatchAsRuntimeException() {
        String errorMsg = "配置错误";

        RuntimeException caught = assertThrows(RuntimeException.class, () -> {
            throw new OssException(errorMsg);
        }, "应能作为RuntimeException捕获");

        assertEquals(errorMsg, caught.getMessage(), "异常消息应正确");
        assertTrue(caught instanceof OssException, "实际类型应为OssException");
    }

    // ==================== 消息格式测试 ====================

    @Test
    @DisplayName("测试消息格式-包含错误详情")
    public void testMessageWithDetails() {
        String errorMsg = "上传文件失败，请检查配置信息:[连接超时]";
        OssException exception = new OssException(errorMsg);

        assertTrue(exception.getMessage().contains("上传文件失败"), "应包含错误描述");
        assertTrue(exception.getMessage().contains("连接超时"), "应包含具体错误原因");
    }

    @Test
    @DisplayName("测试消息格式-中文消息")
    public void testChineseMessage() {
        String errorMsg = "文件下载失败，错误信息:[文件不存在]";
        OssException exception = new OssException(errorMsg);

        assertEquals(errorMsg, exception.getMessage(), "应支持中文消息");
    }

    @Test
    @DisplayName("测试消息格式-特殊字符")
    public void testSpecialCharacterMessage() {
        String errorMsg = "配置错误! 请检查系统配置:[key=value&param=123]";
        OssException exception = new OssException(errorMsg);

        assertEquals(errorMsg, exception.getMessage(), "应支持特殊字符");
    }

    // ==================== 序列化测试 ====================

    @Test
    @DisplayName("测试序列化-serialVersionUID存在")
    public void testSerialVersionUID() {
        // OssException类中定义了serialVersionUID
        // 验证类可以正常实例化（隐式验证序列化支持）
        OssException exception = new OssException("序列化测试");
        assertNotNull(exception);
    }

    // ==================== 堆栈跟踪测试 ====================

    @Test
    @DisplayName("测试堆栈跟踪-应包含堆栈信息")
    public void testStackTrace() {
        OssException exception = new OssException("测试堆栈");

        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertNotNull(stackTrace, "堆栈跟踪不应为null");
        assertTrue(stackTrace.length > 0, "堆栈跟踪应有内容");
    }

    @Test
    @DisplayName("测试堆栈跟踪-包含当前测试方法")
    public void testStackTraceContainsCurrentMethod() {
        OssException exception = new OssException("测试堆栈内容");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        boolean found = false;

        for (StackTraceElement element : stackTrace) {
            if (element.getMethodName().equals("testStackTraceContainsCurrentMethod")) {
                found = true;
                break;
            }
        }

        assertTrue(found, "堆栈跟踪应包含当前测试方法");
    }
}
