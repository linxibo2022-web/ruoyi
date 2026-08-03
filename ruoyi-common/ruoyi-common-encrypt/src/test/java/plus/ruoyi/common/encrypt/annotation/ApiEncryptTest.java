package plus.ruoyi.common.encrypt.annotation;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiEncrypt API加密注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("ApiEncrypt API加密注解测试")
public class ApiEncryptTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为METHOD")
    public void testAnnotationTarget() {
        Target target = ApiEncrypt.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.METHOD, target.value()[0], "目标应为METHOD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = ApiEncrypt.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    @Test
    @DisplayName("测试注解-应被文档化")
    public void testAnnotationDocumented() {
        assertTrue(ApiEncrypt.class.isAnnotationPresent(java.lang.annotation.Documented.class),
            "应有Documented注解");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-response默认为false")
    public void testDefaultResponse() throws Exception {
        Method method = TestController.class.getMethod("defaultMethod");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertFalse(annotation.response(), "response默认值应为false");
    }

    // ==================== 自定义值测试 ====================

    @Test
    @DisplayName("测试自定义值-response为true")
    public void testResponseTrue() throws Exception {
        Method method = TestController.class.getMethod("encryptResponseMethod");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertTrue(annotation.response(), "response应为true");
    }

    @Test
    @DisplayName("测试自定义值-response为false")
    public void testResponseFalse() throws Exception {
        Method method = TestController.class.getMethod("noEncryptResponseMethod");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertFalse(annotation.response(), "response应为false");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有带注解方法")
    public void testGetAllAnnotatedMethods() {
        Method[] methods = TestController.class.getDeclaredMethods();
        int annotatedCount = 0;

        for (Method method : methods) {
            if (method.isAnnotationPresent(ApiEncrypt.class)) {
                annotatedCount++;
            }
        }

        assertEquals(3, annotatedCount, "应有3个带ApiEncrypt注解的方法");
    }

    @Test
    @DisplayName("测试反射-无注解方法")
    public void testMethodWithoutAnnotation() throws Exception {
        Method method = TestController.class.getMethod("normalMethod");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertNull(annotation, "无注解方法应返回null");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-登录接口加密")
    public void testLoginEncrypt() throws Exception {
        Method method = UserController.class.getMethod("login");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertNotNull(annotation, "login方法应有ApiEncrypt注解");
        assertTrue(annotation.response(), "登录接口应加密响应");
    }

    @Test
    @DisplayName("测试业务场景-获取用户信息不加密")
    public void testGetUserNoEncrypt() throws Exception {
        Method method = UserController.class.getMethod("getUser");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertNotNull(annotation, "getUser方法应有ApiEncrypt注解");
        assertFalse(annotation.response(), "获取用户信息不应加密响应");
    }

    @Test
    @DisplayName("测试业务场景-支付接口加密")
    public void testPaymentEncrypt() throws Exception {
        Method method = UserController.class.getMethod("payment");
        ApiEncrypt annotation = method.getAnnotation(ApiEncrypt.class);

        assertNotNull(annotation, "payment方法应有ApiEncrypt注解");
        assertTrue(annotation.response(), "支付接口应加密响应");
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用控制器
     */
    public static class TestController {
        @ApiEncrypt
        public void defaultMethod() {}

        @ApiEncrypt(response = true)
        public void encryptResponseMethod() {}

        @ApiEncrypt(response = false)
        public void noEncryptResponseMethod() {}

        public void normalMethod() {}
    }

    /**
     * 用户控制器 - 业务场景测试
     */
    public static class UserController {
        @ApiEncrypt(response = true)
        public void login() {}

        @ApiEncrypt(response = false)
        public void getUser() {}

        @ApiEncrypt(response = true)
        public void payment() {}

        public void publicInfo() {}
    }
}
