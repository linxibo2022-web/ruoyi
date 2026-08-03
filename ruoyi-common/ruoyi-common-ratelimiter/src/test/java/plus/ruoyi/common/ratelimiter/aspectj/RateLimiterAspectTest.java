package plus.ruoyi.common.ratelimiter.aspectj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.ratelimiter.annotation.RateLimiter;
import plus.ruoyi.common.ratelimiter.enums.LimitType;
import plus.ruoyi.common.test.base.BaseUnitTest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimiterAspect 限流切面测试
 * <p>
 * 测试限流切面的核心功能（纯单元测试，不依赖 Spring 容器和 Redis）:
 * <ul>
 *   <li>切面实例化测试</li>
 *   <li>注解读取测试</li>
 *   <li>测试类注解配置</li>
 * </ul>
 * <p>
 * 注：完整的限流功能测试需要集成测试环境（Spring + Redis）
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("RateLimiterAspect 限流切面测试")
class RateLimiterAspectTest extends BaseUnitTest {

    private RateLimiterAspect rateLimiterAspect;

    @BeforeEach
    void init() {
        rateLimiterAspect = new RateLimiterAspect();
    }

    // ==================== 切面实例化测试 ====================

    @Nested
    @DisplayName("切面实例化测试")
    class AspectInstantiationTests {

        @Test
        @DisplayName("切面应能正常实例化")
        void testAspectInstantiation() {
            assertNotNull(rateLimiterAspect, "切面实例不应为null");
        }

        @Test
        @DisplayName("多次实例化应创建不同对象")
        void testMultipleInstantiation() {
            RateLimiterAspect aspect1 = new RateLimiterAspect();
            RateLimiterAspect aspect2 = new RateLimiterAspect();

            assertNotNull(aspect1, "第一个实例不应为null");
            assertNotNull(aspect2, "第二个实例不应为null");
            assertNotSame(aspect1, aspect2, "应创建不同对象");
        }
    }

    // ==================== 测试类注解配置验证 ====================

    @Nested
    @DisplayName("测试服务类注解配置验证")
    class TestServiceAnnotationTests {

        @Test
        @DisplayName("defaultLimitMethod - 应使用默认配置")
        void testDefaultLimitMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("defaultLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals("", annotation.key(), "key应为空");
            assertEquals(60, annotation.time(), "time应为默认值60");
            assertEquals(100, annotation.count(), "count应为默认值100");
            assertEquals(LimitType.DEFAULT, annotation.limitType(), "limitType应为DEFAULT");
            assertEquals(86400, annotation.timeout(), "timeout应为默认值86400");
        }

        @Test
        @DisplayName("ipLimitMethod - 应配置IP限流")
        void testIpLimitMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("ipLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals(LimitType.IP, annotation.limitType(), "limitType应为IP");
        }

        @Test
        @DisplayName("clusterLimitMethod - 应配置集群限流")
        void testClusterLimitMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("clusterLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals(LimitType.CLUSTER, annotation.limitType(), "limitType应为CLUSTER");
        }

        @Test
        @DisplayName("customMessageMethod - 应配置自定义消息")
        void testCustomMessageMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customMessageMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals("自定义限流消息", annotation.message(), "message应为自定义消息");
        }

        @Test
        @DisplayName("spelSimpleMethod - 应配置简单SpEL表达式key")
        void testSpelSimpleMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("spelSimpleMethod", String.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals("#userId", annotation.key(), "key应为简单SpEL表达式");
        }

        @Test
        @DisplayName("spelTemplateMethod - 应配置模板SpEL表达式key")
        void testSpelTemplateMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("spelTemplateMethod", String.class, Integer.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals("#{#userId + ':' + #type}", annotation.key(), "key应为模板SpEL表达式");
        }

        @Test
        @DisplayName("fixedKeyMethod - 应配置固定key")
        void testFixedKeyMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("fixedKeyMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals("fixed_key", annotation.key(), "key应为固定字符串");
        }

        @Test
        @DisplayName("customTimeMethod - 应配置自定义time")
        void testCustomTimeMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customTimeMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals(30, annotation.time(), "time应为30");
        }

        @Test
        @DisplayName("customCountMethod - 应配置自定义count")
        void testCustomCountMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customCountMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals(10, annotation.count(), "count应为10");
        }

        @Test
        @DisplayName("customTimeoutMethod - 应配置自定义timeout")
        void testCustomTimeoutMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customTimeoutMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals(3600, annotation.timeout(), "timeout应为3600");
        }

        @Test
        @DisplayName("fullCustomMethod - 应配置所有自定义参数")
        void testFullCustomMethodConfig() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("fullCustomMethod", String.class, String.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertNotNull(annotation, "方法应有@RateLimiter注解");
            assertEquals("#{#userId + ':' + #action}", annotation.key(), "key应正确");
            assertEquals(120, annotation.time(), "time应为120");
            assertEquals(5, annotation.count(), "count应为5");
            assertEquals(LimitType.IP, annotation.limitType(), "limitType应为IP");
            assertEquals("操作过于频繁", annotation.message(), "message应正确");
            assertEquals(7200, annotation.timeout(), "timeout应为7200");
        }
    }

    // ==================== 限流类型映射测试 ====================

    @Nested
    @DisplayName("限流类型映射测试")
    class LimitTypeMappingTests {

        @Test
        @DisplayName("DEFAULT类型注解应存在")
        void testDefaultTypeAnnotation() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("defaultLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(LimitType.DEFAULT, annotation.limitType(),
                "默认限流类型应为DEFAULT");
        }

        @Test
        @DisplayName("IP类型注解应正确")
        void testIpTypeAnnotation() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("ipLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(LimitType.IP, annotation.limitType(),
                "IP限流类型应为IP");
        }

        @Test
        @DisplayName("CLUSTER类型注解应正确")
        void testClusterTypeAnnotation() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("clusterLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertEquals(LimitType.CLUSTER, annotation.limitType(),
                "集群限流类型应为CLUSTER");
        }
    }

    // ==================== SpEL表达式配置测试 ====================

    @Nested
    @DisplayName("SpEL表达式配置测试")
    class SpelConfigTests {

        @Test
        @DisplayName("简单SpEL表达式key应以#开头")
        void testSimpleSpelKeyFormat() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("spelSimpleMethod", String.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertTrue(annotation.key().startsWith("#"), "简单SpEL表达式应以#开头");
            assertFalse(annotation.key().startsWith("#{"), "简单SpEL表达式不应以#{开头");
        }

        @Test
        @DisplayName("模板SpEL表达式key应以#{开头并以}结尾")
        void testTemplateSpelKeyFormat() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("spelTemplateMethod", String.class, Integer.class);
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertTrue(annotation.key().startsWith("#{"), "模板SpEL表达式应以#{开头");
            assertTrue(annotation.key().endsWith("}"), "模板SpEL表达式应以}结尾");
        }

        @Test
        @DisplayName("固定key不应包含SpEL标记")
        void testFixedKeyFormat() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("fixedKeyMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertFalse(annotation.key().contains("#"), "固定key不应包含#");
            assertFalse(annotation.key().contains("$"), "固定key不应包含$");
        }
    }

    // ==================== 参数值范围测试 ====================

    @Nested
    @DisplayName("参数值范围测试")
    class ParameterRangeTests {

        @Test
        @DisplayName("time值应为正整数")
        void testTimePositive() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customTimeMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertTrue(annotation.time() > 0, "time应为正整数");
        }

        @Test
        @DisplayName("count值应为正整数")
        void testCountPositive() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customCountMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertTrue(annotation.count() > 0, "count应为正整数");
        }

        @Test
        @DisplayName("timeout值应为正整数")
        void testTimeoutPositive() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("customTimeoutMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertTrue(annotation.timeout() > 0, "timeout应为正整数");
        }

        @Test
        @DisplayName("默认参数值应合理")
        void testDefaultParametersReasonable() throws NoSuchMethodException {
            Method method = TestService.class.getMethod("defaultLimitMethod");
            RateLimiter annotation = method.getAnnotation(RateLimiter.class);

            assertTrue(annotation.time() >= 1 && annotation.time() <= 86400,
                "默认time应在合理范围内(1秒-1天)");
            assertTrue(annotation.count() >= 1 && annotation.count() <= 100000,
                "默认count应在合理范围内(1-100000)");
            assertTrue(annotation.timeout() >= annotation.time(),
                "timeout应不小于time");
        }
    }

    // ==================== 测试用服务类 ====================

    /**
     * 测试用的模拟服务类
     */
    @SuppressWarnings("unused")
    static class TestService {

        @RateLimiter
        public void defaultLimitMethod() {}

        @RateLimiter(limitType = LimitType.IP)
        public void ipLimitMethod() {}

        @RateLimiter(limitType = LimitType.CLUSTER)
        public void clusterLimitMethod() {}

        @RateLimiter(message = "自定义限流消息")
        public void customMessageMethod() {}

        @RateLimiter(key = "#userId")
        public void spelSimpleMethod(String userId) {}

        @RateLimiter(key = "#{#userId + ':' + #type}")
        public void spelTemplateMethod(String userId, Integer type) {}

        @RateLimiter(key = "fixed_key")
        public void fixedKeyMethod() {}

        @RateLimiter(time = 30)
        public void customTimeMethod() {}

        @RateLimiter(count = 10)
        public void customCountMethod() {}

        @RateLimiter(timeout = 3600)
        public void customTimeoutMethod() {}

        @RateLimiter(
            key = "#{#userId + ':' + #action}",
            time = 120,
            count = 5,
            limitType = LimitType.IP,
            message = "操作过于频繁",
            timeout = 7200
        )
        public void fullCustomMethod(String userId, String action) {}
    }
}
