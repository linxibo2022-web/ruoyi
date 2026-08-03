package plus.ruoyi.common.core.exception;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceException 业务异常测试
 *
 * @author 抓蛙师
 */
@DisplayName("ServiceException业务异常测试")
public class ServiceExceptionTest extends BaseUnitTest {

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-基本消息")
    public void testConstructorWithMessage() {
        String message = "用户不存在";

        ServiceException exception = new ServiceException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getBusinessCode());
        assertNull(exception.getDetailMessage());
    }

    @Test
    @DisplayName("测试构造方法-消息带占位符")
    public void testConstructorWithMessageAndArgs() {
        String template = "用户{}不存在，ID: {}";
        String userName = "张三";
        Long userId = 123L;

        ServiceException exception = new ServiceException(template, userName, userId);

        assertEquals("用户张三不存在，ID: 123", exception.getMessage());
    }

    @Test
    @DisplayName("测试构造方法-消息和业务码")
    public void testConstructorWithMessageAndCode() {
        String message = "余额不足";
        Integer code = 10001;

        ServiceException exception = new ServiceException(message, code);

        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试构造方法-消息、业务码和占位符")
    public void testConstructorWithMessageCodeAndArgs() {
        String template = "余额{}元，需要{}元";
        Integer code = 10001;

        ServiceException exception = new ServiceException(template, code, 10, 100);

        assertEquals("余额10元，需要100元", exception.getMessage());
        assertEquals(code, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试构造方法-消息、业务码和详细信息")
    public void testConstructorWithMessageCodeAndDetail() {
        String message = "支付失败";
        Integer code = 10002;
        String detail = "第三方支付接口返回错误";

        ServiceException exception = new ServiceException(message, code, detail);

        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getBusinessCode());
        assertEquals(detail, exception.getDetailMessage());
    }

    @Test
    @DisplayName("测试构造方法-消息和原因异常")
    public void testConstructorWithMessageAndCause() {
        String message = "数据库操作失败";
        RuntimeException cause = new RuntimeException("连接超时");

        ServiceException exception = new ServiceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("测试构造方法-消息、原因异常和占位符")
    public void testConstructorWithMessageCauseAndArgs() {
        String template = "查询用户{}失败";
        String userName = "admin";
        RuntimeException cause = new RuntimeException("SQL异常");

        ServiceException exception = new ServiceException(template, cause, userName);

        assertEquals("查询用户admin失败", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    // ==================== 静态工厂方法 of() 测试 ====================

    @Test
    @DisplayName("测试of-创建基本异常")
    public void testOfWithMessage() {
        String message = "操作失败";

        ServiceException exception = ServiceException.of(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("测试of-创建带占位符的异常")
    public void testOfWithMessageAndArgs() {
        ServiceException exception = ServiceException.of("文件{}不存在", "test.txt");

        assertEquals("文件test.txt不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试of-创建带业务码的异常")
    public void testOfWithMessageAndCode() {
        ServiceException exception = ServiceException.of("权限不足", 403);

        assertEquals("权限不足", exception.getMessage());
        assertEquals(403, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试of-创建带业务码和占位符的异常")
    public void testOfWithMessageCodeAndArgs() {
        ServiceException exception = ServiceException.of(
            "用户{}没有{}权限",
            403,
            "张三",
            "删除"
        );

        assertEquals("用户张三没有删除权限", exception.getMessage());
        assertEquals(403, exception.getBusinessCode());
    }

    // ==================== 条件抛出 throwIf() 测试 ====================

    @Test
    @DisplayName("测试throwIf-条件为true应抛出异常")
    public void testThrowIfConditionTrue() {
        boolean condition = true;

        assertThrows(ServiceException.class,
            () -> ServiceException.throwIf(condition, "条件满足，抛出异常"));
    }

    @Test
    @DisplayName("测试throwIf-条件为false不应抛出异常")
    public void testThrowIfConditionFalse() {
        boolean condition = false;

        assertDoesNotThrow(
            () -> ServiceException.throwIf(condition, "条件不满足，不抛异常"));
    }

    @Test
    @DisplayName("测试throwIf-带占位符，条件为true")
    public void testThrowIfWithArgsConditionTrue() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> ServiceException.throwIf(true, "用户{}已存在", "admin"));

        assertEquals("用户admin已存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试throwIf-带业务码，条件为true")
    public void testThrowIfWithCodeConditionTrue() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> ServiceException.throwIf(true, "账号已锁定", 10003));

        assertEquals("账号已锁定", exception.getMessage());
        assertEquals(10003, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试throwIf-带业务码和占位符，条件为true")
    public void testThrowIfWithCodeAndArgsConditionTrue() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> ServiceException.throwIf(true, "登录失败{}次", 10004, 3));

        assertEquals("登录失败3次", exception.getMessage());
        assertEquals(10004, exception.getBusinessCode());
    }

    // ==================== 非空检查 notNull() 测试 ====================

    @Test
    @DisplayName("测试notNull-对象为null应抛出异常")
    public void testNotNullObjectIsNull() {
        Object obj = null;

        assertThrows(ServiceException.class,
            () -> ServiceException.notNull(obj, "对象不能为空"));
    }

    @Test
    @DisplayName("测试notNull-对象不为null不应抛出异常")
    public void testNotNullObjectIsNotNull() {
        Object obj = new Object();

        assertDoesNotThrow(
            () -> ServiceException.notNull(obj, "对象不能为空"));
    }

    @Test
    @DisplayName("测试notNull-带占位符，对象为null")
    public void testNotNullWithArgsObjectIsNull() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> ServiceException.notNull(null, "{}不能为空", "用户名"));

        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试notNull-检查字符串为null")
    public void testNotNullCheckString() {
        String str = null;

        assertThrows(ServiceException.class,
            () -> ServiceException.notNull(str, "字符串不能为空"));
    }

    @Test
    @DisplayName("测试notNull-检查数字为null")
    public void testNotNullCheckNumber() {
        Integer num = null;

        assertThrows(ServiceException.class,
            () -> ServiceException.notNull(num, "数字不能为null"));
    }

    // ==================== 占位符功能测试 ====================

    @Test
    @DisplayName("测试占位符-单个参数")
    public void testPlaceholderSingleArg() {
        // 使用构造函数避免 Integer 被误认为 businessCode
        // 需要显式转换为 Object 以避免匹配 Integer businessCode 参数
        ServiceException exception = new ServiceException("值为{}", (Object) 123);

        assertEquals("值为123", exception.getMessage());
    }

    @Test
    @DisplayName("测试占位符-多个参数")
    public void testPlaceholderMultipleArgs() {
        // 使用构造函数避免方法重载问题
        ServiceException exception = new ServiceException(
            "用户{}, 年龄{}, 邮箱{}",
            "张三",
            25,
            "zhangsan@example.com"
        );

        assertEquals("用户张三, 年龄25, 邮箱zhangsan@example.com", exception.getMessage());
    }

    @Test
    @DisplayName("测试占位符-参数为null")
    public void testPlaceholderNullArg() {
        // 使用构造函数，参数已经转型为 Object
        ServiceException exception = new ServiceException("值为{}", (Object) null);

        assertEquals("值为null", exception.getMessage());
    }

    @Test
    @DisplayName("测试占位符-参数数量不匹配")
    public void testPlaceholderArgCountMismatch() {
        // 占位符多于参数
        ServiceException exception1 = new ServiceException("A:{}, B:{}, C:{}", "1", "2");
        assertTrue(exception1.getMessage().contains("A:1"));
        assertTrue(exception1.getMessage().contains("B:2"));

        // 参数多于占位符
        ServiceException exception2 = new ServiceException("A:{}", "1", "2", "3");
        assertEquals("A:1", exception2.getMessage());
    }

    @Test
    @DisplayName("测试占位符-没有占位符")
    public void testPlaceholderNoPlaceholder() {
        ServiceException exception = ServiceException.of("固定消息", "arg1", "arg2");

        assertEquals("固定消息", exception.getMessage());
    }

    // ==================== 实际业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-用户不存在")
    public void testBusinessScenarioUserNotFound() {
        Long userId = 123L;

        ServiceException exception = ServiceException.of("用户ID: {} 不存在", userId);

        assertEquals("用户ID: 123 不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试业务场景-余额不足")
    public void testBusinessScenarioInsufficientBalance() {
        double balance = 50.0;
        double required = 100.0;

        ServiceException exception = ServiceException.of(
            "余额不足，当前余额: {}元，需要: {}元",
            10001,
            balance,
            required
        );

        assertEquals("余额不足，当前余额: 50.0元，需要: 100.0元", exception.getMessage());
        assertEquals(10001, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试业务场景-权限检查")
    public void testBusinessScenarioPermissionCheck() {
        boolean hasPermission = false;
        String operation = "删除用户";

        assertThrows(ServiceException.class,
            () -> ServiceException.throwIf(!hasPermission, "无权限执行: {}", operation));
    }

    @Test
    @DisplayName("测试业务场景-参数校验")
    public void testBusinessScenarioParameterValidation() {
        String email = null;

        ServiceException exception = assertThrows(ServiceException.class,
            () -> ServiceException.notNull(email, "邮箱不能为空"));

        assertEquals("邮箱不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试业务场景-库存不足")
    public void testBusinessScenarioOutOfStock() {
        String productName = "iPhone 15";
        int stock = 0;
        int required = 1;

        ServiceException exception = ServiceException.of(
            "商品{}库存不足，当前库存: {}，需要: {}",
            productName,
            stock,
            required
        );

        assertTrue(exception.getMessage().contains("iPhone 15"));
        assertTrue(exception.getMessage().contains("0"));
        assertTrue(exception.getMessage().contains("1"));
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试边界情况-空消息")
    public void testBoundaryEmptyMessage() {
        ServiceException exception = ServiceException.of("");

        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("测试边界情况-超长消息")
    public void testBoundaryLongMessage() {
        String longMessage = "错误".repeat(1000);

        ServiceException exception = ServiceException.of(longMessage);

        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    @DisplayName("测试边界情况-业务码为0")
    public void testBoundaryCodeZero() {
        ServiceException exception = ServiceException.of("测试", 0);

        assertEquals(0, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试边界情况-业务码为负数")
    public void testBoundaryNegativeCode() {
        ServiceException exception = ServiceException.of("测试", -1);

        assertEquals(-1, exception.getBusinessCode());
    }

    @Test
    @DisplayName("测试边界情况-大量占位符")
    public void testBoundaryManyPlaceholders() {
        // 使用构造函数避免方法重载问题
        ServiceException exception = new ServiceException(
            "A:{}, B:{}, C:{}, D:{}, E:{}",
            "1", "2", "3", "4", "5"
        );

        assertEquals("A:1, B:2, C:3, D:4, E:5", exception.getMessage());
    }

    @Test
    @DisplayName("测试边界情况-特殊字符")
    public void testBoundarySpecialChars() {
        ServiceException exception = ServiceException.of(
            "错误: {}",
            "包含特殊字符: !@#$%^&*()"
        );

        assertTrue(exception.getMessage().contains("!@#$%^&*()"));
    }
}
