package plus.ruoyi.common.core.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StringUtils工具类测试
 *
 * @author 抓蛙师
 */
@DisplayName("StringUtils工具类测试")
public class StringUtilsTest extends BaseUnitTest {

    @Test
    @DisplayName("测试isBlank-空字符串应返回true")
    public void testIsBlank() {
        // 测试null
        assertTrue(StringUtils.isBlank(null), "null应该返回true");

        // 测试空字符串
        assertTrue(StringUtils.isBlank(""), "空字符串应该返回true");

        // 测试空格字符串
        assertTrue(StringUtils.isBlank("  "), "空格字符串应该返回true");
        assertTrue(StringUtils.isBlank("\t"), "制表符应该返回true");
        assertTrue(StringUtils.isBlank("\n"), "换行符应该返回true");

        // 测试正常字符串
        assertFalse(StringUtils.isBlank("test"), "正常字符串应该返回false");
        assertFalse(StringUtils.isBlank(" test "), "包含内容的字符串应该返回false");
    }

    @Test
    @DisplayName("测试isNotBlank-正常字符串应返回true")
    public void testIsNotBlank() {
        // 正常字符串
        assertTrue(StringUtils.isNotBlank("test"));
        assertTrue(StringUtils.isNotBlank("hello world"));
        assertTrue(StringUtils.isNotBlank(" test "));

        // 空值
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("  "));
    }

    @Test
    @DisplayName("测试isEmpty-空字符串应返回true")
    public void testIsEmpty() {
        // 空值
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));

        // 非空(包括空格)
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("test"));
    }

    @Test
    @DisplayName("测试isNotEmpty-非空字符串应返回true")
    public void testIsNotEmpty() {
        // 非空
        assertTrue(StringUtils.isNotEmpty("test"));
        assertTrue(StringUtils.isNotEmpty(" "));

        // 空值
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
    }

    @Test
    @DisplayName("测试trim-去除首尾空格")
    public void testTrim() {
        assertEquals("test", StringUtils.trim("  test  "));
        assertEquals("test", StringUtils.trim("test"));
        assertEquals("", StringUtils.trim("  "));
        assertNull(StringUtils.trim(null));
    }

    @Test
    @DisplayName("测试substring-字符串截取")
    public void testSubstring() {
        String str = "hello world";

        // 正常截取
        assertEquals("hello", StringUtils.substring(str, 0, 5));
        assertEquals("world", StringUtils.substring(str, 6, 11));

        // 边界情况
        assertEquals("", StringUtils.substring(str, 0, 0));
        assertEquals("hello world", StringUtils.substring(str, 0, 100));

        // null处理
        assertNull(StringUtils.substring(null, 0, 5));
    }

    @Test
    @DisplayName("测试format-字符串格式化")
    public void testFormat() {
        // 占位符格式化
        String result = StringUtils.format("用户{}登录成功", "admin");
        assertEquals("用户admin登录成功", result);

        // 多个占位符
        result = StringUtils.format("用户{}在{}登录", "admin", "2025-01-01");
        assertEquals("用户admin在2025-01-01登录", result);

        // 无占位符
        result = StringUtils.format("测试", "参数");
        assertEquals("测试", result);
    }

    @Test
    @DisplayName("测试startsWith-判断字符串开头")
    public void testStartsWith() {
        String str = "hello world";

        assertTrue(str.startsWith("hello"));
        assertFalse(str.startsWith("world"));

        // 空字符串
        assertTrue("".startsWith(""));
    }

    @Test
    @DisplayName("测试endsWith-判断字符串结尾")
    public void testEndsWith() {
        String str = "hello world";

        assertTrue(str.endsWith("world"));
        assertFalse(str.endsWith("hello"));

        // 空字符串
        assertTrue("".endsWith(""));
    }

    @Test
    @DisplayName("测试contains-判断字符串包含")
    public void testContains() {
        String str = "hello world";

        assertTrue(str.contains("world"));
        assertTrue(str.contains("hello"));
        assertFalse(str.contains("test"));
    }

    @Test
    @DisplayName("测试equals-字符串相等判断")
    public void testEquals() {
        assertTrue(StringUtils.equals("test", "test"));
        assertFalse(StringUtils.equals("test", "Test"));
        assertFalse(StringUtils.equals("test", null));
        assertTrue(StringUtils.equals(null, null));
    }

    @Test
    @DisplayName("测试split-字符串分割")
    public void testSplit() {
        String str = "a,b,c,d";
        String[] arr = str.split(",");

        assertEquals(4, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("d", arr[3]);
    }
}
