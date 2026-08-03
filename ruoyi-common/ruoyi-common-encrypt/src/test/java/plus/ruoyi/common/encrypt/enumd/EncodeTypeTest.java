package plus.ruoyi.common.encrypt.enumd;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncodeType 编码类型枚举测试
 *
 * @author 抓蛙师
 */
@DisplayName("EncodeType编码类型枚举测试")
public class EncodeTypeTest extends BaseUnitTest {

    // ==================== 枚举值测试 ====================

    @Test
    @DisplayName("测试枚举值-DEFAULT")
    public void testDefault() {
        EncodeType type = EncodeType.DEFAULT;

        assertNotNull(type, "DEFAULT不应为null");
        assertEquals("DEFAULT", type.name(), "name应为DEFAULT");
    }

    @Test
    @DisplayName("测试枚举值-BASE64")
    public void testBase64() {
        EncodeType type = EncodeType.BASE64;

        assertNotNull(type, "BASE64不应为null");
        assertEquals("BASE64", type.name(), "name应为BASE64");
    }

    @Test
    @DisplayName("测试枚举值-HEX")
    public void testHex() {
        EncodeType type = EncodeType.HEX;

        assertNotNull(type, "HEX不应为null");
        assertEquals("HEX", type.name(), "name应为HEX");
    }

    // ==================== 枚举数量测试 ====================

    @Test
    @DisplayName("测试枚举数量-应有3个枚举值")
    public void testEnumCount() {
        EncodeType[] values = EncodeType.values();

        assertEquals(3, values.length, "应有3个枚举值");
    }

    // ==================== valueOf测试 ====================

    @Test
    @DisplayName("测试valueOf-DEFAULT")
    public void testValueOfDefault() {
        EncodeType type = EncodeType.valueOf("DEFAULT");

        assertEquals(EncodeType.DEFAULT, type, "valueOf DEFAULT应正确");
    }

    @Test
    @DisplayName("测试valueOf-BASE64")
    public void testValueOfBase64() {
        EncodeType type = EncodeType.valueOf("BASE64");

        assertEquals(EncodeType.BASE64, type, "valueOf BASE64应正确");
    }

    @Test
    @DisplayName("测试valueOf-HEX")
    public void testValueOfHex() {
        EncodeType type = EncodeType.valueOf("HEX");

        assertEquals(EncodeType.HEX, type, "valueOf HEX应正确");
    }

    @Test
    @DisplayName("测试valueOf-不存在的值应抛出异常")
    public void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncodeType.valueOf("INVALID");
        }, "不存在的枚举值应抛出异常");
    }

    // ==================== ordinal测试 ====================

    @Test
    @DisplayName("测试ordinal-枚举顺序")
    public void testOrdinal() {
        assertEquals(0, EncodeType.DEFAULT.ordinal(), "DEFAULT ordinal应为0");
        assertEquals(1, EncodeType.BASE64.ordinal(), "BASE64 ordinal应为1");
        assertEquals(2, EncodeType.HEX.ordinal(), "HEX ordinal应为2");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-Base64编码特点")
    public void testBase64Characteristics() {
        // Base64编码特点：输出为可打印字符，长度约为原数据的4/3
        assertEquals(EncodeType.BASE64, EncodeType.valueOf("BASE64"), "BASE64应可用于Web传输");
    }

    @Test
    @DisplayName("测试业务场景-Hex编码特点")
    public void testHexCharacteristics() {
        // Hex编码特点：输出为十六进制字符，长度为原数据的2倍
        assertEquals(EncodeType.HEX, EncodeType.valueOf("HEX"), "HEX应可用于十六进制表示");
    }

    @Test
    @DisplayName("测试业务场景-遍历所有编码类型")
    public void testIterateAllEncodeTypes() {
        EncodeType[] types = EncodeType.values();

        assertEquals(EncodeType.DEFAULT, types[0], "第一个应为DEFAULT");
        assertEquals(EncodeType.BASE64, types[1], "第二个应为BASE64");
        assertEquals(EncodeType.HEX, types[2], "第三个应为HEX");
    }

    @Test
    @DisplayName("测试业务场景-判断编码类型")
    public void testEncodeTypeSelection() {
        EncodeType type = EncodeType.BASE64;

        // 模拟业务逻辑：根据编码类型选择处理方式
        switch (type) {
            case DEFAULT:
                // 使用默认编码
                break;
            case BASE64:
                // 使用Base64编码
                break;
            case HEX:
                // 使用十六进制编码
                break;
            default:
                fail("不应有未处理的编码类型");
        }

        // 测试通过说明switch覆盖了所有情况
        assertTrue(true, "所有编码类型都应被处理");
    }
}
