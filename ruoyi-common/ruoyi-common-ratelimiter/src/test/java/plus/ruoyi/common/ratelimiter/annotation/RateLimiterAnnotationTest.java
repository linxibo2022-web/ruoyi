package plus.ruoyi.common.ratelimiter.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.ratelimiter.enums.LimitType;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimiter 限流注解测试
 * <p>
 * 测试限流注解的各项功能:
 * <ul>
 *   <li>注解元数据验证</li>
 *   <li>默认值验证</li>
 *   <li>自定义值验证</li>
 *   <li>注解读取功能</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("RateLimiter 限流注解测试")
class RateLimiterAnnotationTest extends BaseUnitTest {

    // ==================== 注解元数据测试 ====================

    @Nested
    @DisplayName("注解元数据测试")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("@Target - 应只能用于方法")
        void testTargetAnnotation() {
            var targets = RateLimiter.class.getAnnotation(java.lang.annotation.Target.class);
            assertNotNull(targets, "@Target 注解应存在");

            ElementType[] value = targets.value();
            assertEquals(1, value.length, "应只有一个目标类型");
            assertEquals(ElementType.METHOD, value[0], "目标类型应为 METHOD");
        }

        @Test
        @DisplayName("@Retention - 应为运行时保留")
        void testRetentionAnnotation() {
            var retention = RateLimiter.class.getAnnotation(Retention.class);
            assertNotNull(retention, "@Retention 注解应存在");
            assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为 RUNTIME");
        }

        @Test
        @DisplayName("@Documented - 应被标记为文档化")
        void testDocumentedAnnotation() {
            var documented = RateLimiter.class.getAnnotation(java.lang.annotation.Documented.class);
            assertNotNull(documented, "@Documented 注解应存在");
        }

        @Test
        @DisplayName("注解应为接口类型")
        void testIsAnnotation() {
            assertTrue(RateLimiter.class.isAnnotation(), "RateLimiter 应是注解类型");
        }
    }

    // ==================== 默认值测试 ====================

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("key - 默认值应为空字符串")
        void testDefaultKey() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals("", annotation.key(), "key 默认值应为空字符串");
        }

        @Test
        @DisplayName("time - 默认值应为60秒")
        void testDefaultTime() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(60, annotation.time(), "time 默认值应为60秒");
        }

        @Test
        @DisplayName("count - 默认值应为100次")
        void testDefaultCount() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(100, annotation.count(), "count 默认值应为100次");
        }

        @Test
        @DisplayName("limitType - 默认值应为DEFAULT")
        void testDefaultLimitType() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(LimitType.DEFAULT, annotation.limitType(), "limitType 默认值应为 DEFAULT");
        }

        @Test
        @DisplayName("message - 默认值应为国际化消息key")
        void testDefaultMessage() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(I18nKeys.Request.RATE_LIMIT_EXCEEDED, annotation.message(),
                "message 默认值应为国际化消息key");
        }

        @Test
        @DisplayName("timeout - 默认值应为86400秒(1天)")
        void testDefaultTimeout() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(86400, annotation.timeout(), "timeout 默认值应为86400秒(1天)");
        }
    }

    // ==================== 自定义值测试 ====================

    @Nested
    @DisplayName("自定义值测试")
    class CustomValueTests {

        @Test
        @DisplayName("自定义 key")
        void testCustomKey() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("customKeyMethod", String.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals("#userId", annotation.key(), "自定义key应正确读取");
        }

        @Test
        @DisplayName("自定义 time")
        void testCustomTime() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("customTimeMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(30, annotation.time(), "自定义time应正确读取");
        }

        @Test
        @DisplayName("自定义 count")
        void testCustomCount() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("customCountMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(10, annotation.count(), "自定义count应正确读取");
        }

        @Test
        @DisplayName("自定义 limitType - IP")
        void testCustomLimitTypeIp() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("ipLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(LimitType.IP, annotation.limitType(), "limitType应为IP");
        }

        @Test
        @DisplayName("自定义 limitType - CLUSTER")
        void testCustomLimitTypeCluster() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("clusterLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(LimitType.CLUSTER, annotation.limitType(), "limitType应为CLUSTER");
        }

        @Test
        @DisplayName("自定义 message")
        void testCustomMessage() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("customMessageMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals("访问过于频繁，请稍后再试", annotation.message(), "自定义message应正确读取");
        }

        @Test
        @DisplayName("自定义 timeout")
        void testCustomTimeout() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("customTimeoutMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(3600, annotation.timeout(), "自定义timeout应正确读取");
        }
    }

    // ==================== 复杂配置测试 ====================

    @Nested
    @DisplayName("复杂配置测试")
    class ComplexConfigTests {

        @Test
        @DisplayName("完整配置 - 所有参数自定义")
        void testFullCustomConfig() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("fullCustomMethod", String.class, String.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals("#{#userId + ':' + #action}", annotation.key(), "key应正确");
            assertEquals(120, annotation.time(), "time应正确");
            assertEquals(5, annotation.count(), "count应正确");
            assertEquals(LimitType.IP, annotation.limitType(), "limitType应正确");
            assertEquals("操作过于频繁", annotation.message(), "message应正确");
            assertEquals(7200, annotation.timeout(), "timeout应正确");
        }

        @Test
        @DisplayName("SpEL表达式key - 简单参数引用")
        void testSpelKeySimple() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("spelSimpleMethod", Long.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals("#id", annotation.key(), "简单SpEL key应正确");
        }

        @Test
        @DisplayName("SpEL表达式key - 模板格式")
        void testSpelKeyTemplate() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("spelTemplateMethod", String.class, Integer.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals("#{#name + ':' + #type}", annotation.key(), "模板SpEL key应正确");
        }

        @Test
        @DisplayName("登录接口限流配置")
        void testLoginLimitConfig() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("loginMethod", String.class, String.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(60, annotation.time(), "登录限流时间应为60秒");
            assertEquals(5, annotation.count(), "登录限流次数应为5次");
            assertEquals(LimitType.IP, annotation.limitType(), "登录限流类型应为IP");
        }

        @Test
        @DisplayName("查询接口限流配置")
        void testQueryLimitConfig() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("queryMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(60, annotation.time(), "查询限流时间应为60秒");
            assertEquals(1000, annotation.count(), "查询限流次数应为1000次");
            assertEquals(LimitType.DEFAULT, annotation.limitType(), "查询限流类型应为DEFAULT");
        }
    }

    // ==================== 边界值测试 ====================

    @Nested
    @DisplayName("边界值测试")
    class BoundaryValueTests {

        @Test
        @DisplayName("time最小值配置")
        void testMinTimeConfig() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("minTimeMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(1, annotation.time(), "time最小值应为1");
        }

        @Test
        @DisplayName("count最小值配置")
        void testMinCountConfig() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("minCountMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(1, annotation.count(), "count最小值应为1");
        }

        @Test
        @DisplayName("大数值配置")
        void testLargeValueConfig() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("largeValueMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(86400, annotation.time(), "大time值应正确");
            assertEquals(100000, annotation.count(), "大count值应正确");
            assertEquals(604800, annotation.timeout(), "大timeout值应正确(7天)");
        }
    }

    // ==================== 注解存在性测试 ====================

    @Nested
    @DisplayName("注解存在性测试")
    class AnnotationPresenceTests {

        @Test
        @DisplayName("有注解的方法应能检测到")
        void testAnnotationPresent() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            assertTrue(method.isAnnotationPresent(RateLimiter.class),
                "应能检测到 @RateLimiter 注解");
        }

        @Test
        @DisplayName("无注解的方法应检测不到")
        void testAnnotationNotPresent() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("noLimitMethod");
            assertFalse(method.isAnnotationPresent(RateLimiter.class),
                "不应检测到 @RateLimiter 注解");
        }

        @Test
        @DisplayName("getAnnotation - 有注解应返回非null")
        void testGetAnnotationPresent() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("defaultMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);
            assertNotNull(annotation, "应返回注解实例");
        }

        @Test
        @DisplayName("getAnnotation - 无注解应返回null")
        void testGetAnnotationNotPresent() throws NoSuchMethodException {
            Method method = TestController.class.getMethod("noLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);
            assertNull(annotation, "应返回null");
        }
    }

    // ==================== 测试用控制器类 ====================

    /**
     * 测试用的模拟控制器类
     */
    @SuppressWarnings("unused")
    static class TestController {

        /**
         * 使用所有默认值
         */
        @RateLimiter
        public void defaultMethod() {}

        /**
         * 无限流注解
         */
        public void noLimitMethod() {}

        /**
         * 自定义key
         */
        @RateLimiter(key = "#userId")
        public void customKeyMethod(String userId) {}

        /**
         * 自定义time
         */
        @RateLimiter(time = 30)
        public void customTimeMethod() {}

        /**
         * 自定义count
         */
        @RateLimiter(count = 10)
        public void customCountMethod() {}

        /**
         * IP限流
         */
        @RateLimiter(limitType = LimitType.IP)
        public void ipLimitMethod() {}

        /**
         * 集群限流
         */
        @RateLimiter(limitType = LimitType.CLUSTER)
        public void clusterLimitMethod() {}

        /**
         * 自定义message
         */
        @RateLimiter(message = "访问过于频繁，请稍后再试")
        public void customMessageMethod() {}

        /**
         * 自定义timeout
         */
        @RateLimiter(timeout = 3600)
        public void customTimeoutMethod() {}

        /**
         * 完整自定义配置
         */
        @RateLimiter(
            key = "#{#userId + ':' + #action}",
            time = 120,
            count = 5,
            limitType = LimitType.IP,
            message = "操作过于频繁",
            timeout = 7200
        )
        public void fullCustomMethod(String userId, String action) {}

        /**
         * SpEL简单参数引用
         */
        @RateLimiter(key = "#id")
        public void spelSimpleMethod(Long id) {}

        /**
         * SpEL模板格式
         */
        @RateLimiter(key = "#{#name + ':' + #type}")
        public void spelTemplateMethod(String name, Integer type) {}

        /**
         * 登录接口限流
         */
        @RateLimiter(time = 60, count = 5, limitType = LimitType.IP)
        public void loginMethod(String username, String password) {}

        /**
         * 查询接口限流
         */
        @RateLimiter(time = 60, count = 1000, limitType = LimitType.DEFAULT)
        public void queryMethod() {}

        /**
         * time最小值
         */
        @RateLimiter(time = 1)
        public void minTimeMethod() {}

        /**
         * count最小值
         */
        @RateLimiter(count = 1)
        public void minCountMethod() {}

        /**
         * 大数值配置
         */
        @RateLimiter(time = 86400, count = 100000, timeout = 604800)
        public void largeValueMethod() {}
    }
}
