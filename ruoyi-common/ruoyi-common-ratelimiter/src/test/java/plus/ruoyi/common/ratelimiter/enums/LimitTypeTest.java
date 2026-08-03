package plus.ruoyi.common.ratelimiter.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.test.base.BaseUnitTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LimitType 限流类型枚举测试
 * <p>
 * 测试限流类型枚举的各项功能:
 * <ul>
 *   <li>枚举值定义验证</li>
 *   <li>枚举数量验证</li>
 *   <li>枚举转换功能</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Tag("dev")
@DisplayName("LimitType 限流类型枚举测试")
class LimitTypeTest extends BaseUnitTest {

    // ==================== 枚举值定义测试 ====================

    @Nested
    @DisplayName("枚举值定义测试")
    class EnumValueTests {

        @Test
        @DisplayName("DEFAULT - 默认策略全局限流应存在")
        void testDefaultExists() {
            assertNotNull(LimitType.DEFAULT, "DEFAULT 枚举值应存在");
            assertEquals("DEFAULT", LimitType.DEFAULT.name(), "枚举名称应为 DEFAULT");
        }

        @Test
        @DisplayName("IP - IP限流策略应存在")
        void testIpExists() {
            assertNotNull(LimitType.IP, "IP 枚举值应存在");
            assertEquals("IP", LimitType.IP.name(), "枚举名称应为 IP");
        }

        @Test
        @DisplayName("CLUSTER - 集群限流策略应存在")
        void testClusterExists() {
            assertNotNull(LimitType.CLUSTER, "CLUSTER 枚举值应存在");
            assertEquals("CLUSTER", LimitType.CLUSTER.name(), "枚举名称应为 CLUSTER");
        }
    }

    // ==================== 枚举数量与顺序测试 ====================

    @Nested
    @DisplayName("枚举数量与顺序测试")
    class EnumCountAndOrderTests {

        @Test
        @DisplayName("枚举总数应为3个")
        void testEnumCount() {
            LimitType[] values = LimitType.values();
            assertEquals(3, values.length, "限流类型枚举应有3个值");
        }

        @Test
        @DisplayName("枚举顺序应正确")
        void testEnumOrder() {
            LimitType[] values = LimitType.values();

            assertEquals(LimitType.DEFAULT, values[0], "第一个枚举应为 DEFAULT");
            assertEquals(LimitType.IP, values[1], "第二个枚举应为 IP");
            assertEquals(LimitType.CLUSTER, values[2], "第三个枚举应为 CLUSTER");
        }

        @Test
        @DisplayName("枚举序号应正确")
        void testEnumOrdinal() {
            assertEquals(0, LimitType.DEFAULT.ordinal(), "DEFAULT 序号应为 0");
            assertEquals(1, LimitType.IP.ordinal(), "IP 序号应为 1");
            assertEquals(2, LimitType.CLUSTER.ordinal(), "CLUSTER 序号应为 2");
        }
    }

    // ==================== 枚举转换测试 ====================

    @Nested
    @DisplayName("枚举转换测试")
    class EnumConversionTests {

        @Test
        @DisplayName("valueOf - 有效名称应正确转换")
        void testValueOfValid() {
            assertEquals(LimitType.DEFAULT, LimitType.valueOf("DEFAULT"),
                "DEFAULT 字符串应转换为 DEFAULT 枚举");
            assertEquals(LimitType.IP, LimitType.valueOf("IP"),
                "IP 字符串应转换为 IP 枚举");
            assertEquals(LimitType.CLUSTER, LimitType.valueOf("CLUSTER"),
                "CLUSTER 字符串应转换为 CLUSTER 枚举");
        }

        @Test
        @DisplayName("valueOf - 无效名称应抛出异常")
        void testValueOfInvalid() {
            assertThrows(IllegalArgumentException.class, () -> LimitType.valueOf("INVALID"),
                "无效名称应抛出 IllegalArgumentException");
            assertThrows(IllegalArgumentException.class, () -> LimitType.valueOf("default"),
                "小写名称应抛出 IllegalArgumentException");
            assertThrows(IllegalArgumentException.class, () -> LimitType.valueOf(""),
                "空字符串应抛出 IllegalArgumentException");
        }

        @Test
        @DisplayName("valueOf - null应抛出空指针异常")
        void testValueOfNull() {
            assertThrows(NullPointerException.class, () -> LimitType.valueOf(null),
                "null 应抛出 NullPointerException");
        }

        @Test
        @DisplayName("name - 应返回正确的名称字符串")
        void testName() {
            assertEquals("DEFAULT", LimitType.DEFAULT.name(), "DEFAULT 名称应正确");
            assertEquals("IP", LimitType.IP.name(), "IP 名称应正确");
            assertEquals("CLUSTER", LimitType.CLUSTER.name(), "CLUSTER 名称应正确");
        }

        @Test
        @DisplayName("toString - 应与name一致")
        void testToString() {
            assertEquals(LimitType.DEFAULT.name(), LimitType.DEFAULT.toString(),
                "toString 应与 name 一致");
            assertEquals(LimitType.IP.name(), LimitType.IP.toString(),
                "toString 应与 name 一致");
            assertEquals(LimitType.CLUSTER.name(), LimitType.CLUSTER.toString(),
                "toString 应与 name 一致");
        }
    }

    // ==================== 枚举比较测试 ====================

    @Nested
    @DisplayName("枚举比较测试")
    class EnumComparisonTests {

        @Test
        @DisplayName("相同枚举值应相等")
        void testSameEnumEquals() {
            assertEquals(LimitType.DEFAULT, LimitType.DEFAULT, "相同枚举应相等");
            assertEquals(LimitType.IP, LimitType.IP, "相同枚举应相等");
            assertEquals(LimitType.CLUSTER, LimitType.CLUSTER, "相同枚举应相等");
        }

        @Test
        @DisplayName("不同枚举值应不相等")
        void testDifferentEnumNotEquals() {
            assertNotEquals(LimitType.DEFAULT, LimitType.IP, "不同枚举应不相等");
            assertNotEquals(LimitType.DEFAULT, LimitType.CLUSTER, "不同枚举应不相等");
            assertNotEquals(LimitType.IP, LimitType.CLUSTER, "不同枚举应不相等");
        }

        @Test
        @DisplayName("枚举与null应不相等")
        void testEnumNotEqualsNull() {
            assertNotEquals(null, LimitType.DEFAULT, "枚举与null应不相等");
            assertNotEquals(null, LimitType.IP, "枚举与null应不相等");
            assertNotEquals(null, LimitType.CLUSTER, "枚举与null应不相等");
        }

        @Test
        @DisplayName("可以使用==比较枚举")
        void testEnumIdentity() {
            LimitType type1 = LimitType.DEFAULT;
            LimitType type2 = LimitType.DEFAULT;
            assertSame(type1, type2, "相同枚举值应是同一对象引用");
        }
    }

    // ==================== switch语句测试 ====================

    @Nested
    @DisplayName("switch语句测试")
    class SwitchStatementTests {

        @Test
        @DisplayName("switch - DEFAULT应匹配正确分支")
        void testSwitchDefault() {
            String result = getTypeDescription(LimitType.DEFAULT);
            assertEquals("全局限流", result, "DEFAULT 应匹配全局限流分支");
        }

        @Test
        @DisplayName("switch - IP应匹配正确分支")
        void testSwitchIp() {
            String result = getTypeDescription(LimitType.IP);
            assertEquals("IP限流", result, "IP 应匹配IP限流分支");
        }

        @Test
        @DisplayName("switch - CLUSTER应匹配正确分支")
        void testSwitchCluster() {
            String result = getTypeDescription(LimitType.CLUSTER);
            assertEquals("集群限流", result, "CLUSTER 应匹配集群限流分支");
        }

        /**
         * 辅助方法：根据限流类型返回描述
         */
        private String getTypeDescription(LimitType type) {
            return switch (type) {
                case DEFAULT -> "全局限流";
                case IP -> "IP限流";
                case CLUSTER -> "集群限流";
            };
        }
    }

    // ==================== 业务场景测试 ====================

    @Nested
    @DisplayName("业务场景测试")
    class BusinessScenarioTests {

        @Test
        @DisplayName("判断是否为IP限流")
        void testIsIpLimit() {
            assertTrue(LimitType.IP == LimitType.IP, "应判断为IP限流");
            assertFalse(LimitType.DEFAULT == LimitType.IP, "DEFAULT不是IP限流");
            assertFalse(LimitType.CLUSTER == LimitType.IP, "CLUSTER不是IP限流");
        }

        @Test
        @DisplayName("判断是否为集群限流")
        void testIsClusterLimit() {
            assertTrue(LimitType.CLUSTER == LimitType.CLUSTER, "应判断为集群限流");
            assertFalse(LimitType.DEFAULT == LimitType.CLUSTER, "DEFAULT不是集群限流");
            assertFalse(LimitType.IP == LimitType.CLUSTER, "IP不是集群限流");
        }

        @Test
        @DisplayName("判断是否为全局限流")
        void testIsDefaultLimit() {
            assertTrue(LimitType.DEFAULT == LimitType.DEFAULT, "应判断为全局限流");
            assertFalse(LimitType.IP == LimitType.DEFAULT, "IP不是全局限流");
            assertFalse(LimitType.CLUSTER == LimitType.DEFAULT, "CLUSTER不是全局限流");
        }
    }
}
