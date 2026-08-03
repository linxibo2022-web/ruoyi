package plus.ruoyi.common.redis.handler;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeyPrefixHandler Redis key前缀处理器测试
 *
 * @author 抓蛙师
 */
@DisplayName("KeyPrefixHandler前缀处理器测试")
public class KeyPrefixHandlerTest extends BaseUnitTest {

    // ==================== map方法测试 ====================

    @Test
    @DisplayName("测试map-正常key应添加前缀")
    public void testMapWithNormalKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        String result = handler.map("user:123");

        assertEquals("myapp:user:123", result, "应该添加前缀myapp:");
    }

    @Test
    @DisplayName("测试map-已有前缀的key不重复添加")
    public void testMapWithExistingPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        String result = handler.map("myapp:user:123");

        assertEquals("myapp:user:123", result, "已有前缀的key不应重复添加");
    }

    @Test
    @DisplayName("测试map-空前缀配置应直接返回原key")
    public void testMapWithEmptyPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("");

        String result = handler.map("user:123");

        assertEquals("user:123", result, "空前缀配置应直接返回原key");
    }

    @Test
    @DisplayName("测试map-null前缀配置应直接返回原key")
    public void testMapWithNullPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler(null);

        String result = handler.map("user:123");

        assertEquals("user:123", result, "null前缀配置应直接返回原key");
    }

    @Test
    @DisplayName("测试map-空白前缀配置应直接返回原key")
    public void testMapWithBlankPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("   ");

        String result = handler.map("user:123");

        assertEquals("user:123", result, "空白前缀配置应直接返回原key");
    }

    @Test
    @DisplayName("测试map-空key应返回null")
    public void testMapWithEmptyKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        assertNull(handler.map(""), "空key应返回null");
    }

    @Test
    @DisplayName("测试map-null key应返回null")
    public void testMapWithNullKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        assertNull(handler.map(null), "null key应返回null");
    }

    @Test
    @DisplayName("测试map-空白key应返回null")
    public void testMapWithBlankKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        assertNull(handler.map("   "), "空白key应返回null");
    }

    // ==================== unmap方法测试 ====================

    @Test
    @DisplayName("测试unmap-带前缀的key应去除前缀")
    public void testUnmapWithPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        String result = handler.unmap("myapp:user:123");

        assertEquals("user:123", result, "应该去除前缀myapp:");
    }

    @Test
    @DisplayName("测试unmap-不带前缀的key应直接返回")
    public void testUnmapWithoutPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        String result = handler.unmap("other:user:123");

        assertEquals("other:user:123", result, "不带配置前缀的key应直接返回");
    }

    @Test
    @DisplayName("测试unmap-空前缀配置应直接返回原key")
    public void testUnmapWithEmptyPrefixConfig() {
        KeyPrefixHandler handler = new KeyPrefixHandler("");

        String result = handler.unmap("myapp:user:123");

        assertEquals("myapp:user:123", result, "空前缀配置应直接返回原key");
    }

    @Test
    @DisplayName("测试unmap-null前缀配置应直接返回原key")
    public void testUnmapWithNullPrefixConfig() {
        KeyPrefixHandler handler = new KeyPrefixHandler(null);

        String result = handler.unmap("myapp:user:123");

        assertEquals("myapp:user:123", result, "null前缀配置应直接返回原key");
    }

    @Test
    @DisplayName("测试unmap-空key应返回null")
    public void testUnmapWithEmptyKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        assertNull(handler.unmap(""), "空key应返回null");
    }

    @Test
    @DisplayName("测试unmap-null key应返回null")
    public void testUnmapWithNullKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        assertNull(handler.unmap(null), "null key应返回null");
    }

    @Test
    @DisplayName("测试unmap-空白key应返回null")
    public void testUnmapWithBlankKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        assertNull(handler.unmap("   "), "空白key应返回null");
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试map和unmap-往返转换应保持一致")
    public void testMapUnmapRoundTrip() {
        KeyPrefixHandler handler = new KeyPrefixHandler("prod");
        String originalKey = "cache:session:abc123";

        // map后再unmap应该恢复原值
        String mapped = handler.map(originalKey);
        String unmapped = handler.unmap(mapped);

        assertEquals(originalKey, unmapped, "往返转换应保持一致");
    }

    @Test
    @DisplayName("测试map-前缀包含特殊字符")
    public void testMapWithSpecialCharPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("my-app_v1");

        String result = handler.map("user:123");

        assertEquals("my-app_v1:user:123", result, "特殊字符前缀应正常工作");
    }

    @Test
    @DisplayName("测试unmap-前缀包含特殊字符")
    public void testUnmapWithSpecialCharPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("my-app_v1");

        String result = handler.unmap("my-app_v1:user:123");

        assertEquals("user:123", result, "特殊字符前缀应正常去除");
    }

    @Test
    @DisplayName("测试map-key只包含冒号")
    public void testMapWithColonOnlyKey() {
        KeyPrefixHandler handler = new KeyPrefixHandler("myapp");

        String result = handler.map(":");

        assertEquals("myapp::", result, "冒号key应正常添加前缀");
    }

    @Test
    @DisplayName("测试map-前缀与key完全相同")
    public void testMapWithSameAsPrefix() {
        KeyPrefixHandler handler = new KeyPrefixHandler("user");

        // key是"user"，前缀是"user:"，所以"user"不以"user:"开头，应该添加前缀
        String result = handler.map("user");

        assertEquals("user:user", result, "key与前缀相同时应添加前缀");
    }

    @Test
    @DisplayName("测试map-前缀部分匹配key开头不应被误判")
    public void testMapWithPartialPrefixMatch() {
        KeyPrefixHandler handler = new KeyPrefixHandler("my");

        // key是"myapp:user"，前缀是"my:"，key不以"my:"开头
        String result = handler.map("myapp:user");

        assertEquals("my:myapp:user", result, "部分匹配不应被视为已有前缀");
    }
}
