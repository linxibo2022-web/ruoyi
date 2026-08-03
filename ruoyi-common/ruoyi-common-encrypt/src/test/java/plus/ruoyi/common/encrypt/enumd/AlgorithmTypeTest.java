package plus.ruoyi.common.encrypt.enumd;

import plus.ruoyi.common.encrypt.core.encryptor.*;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlgorithmType 算法类型枚举测试
 *
 * @author 抓蛙师
 */
@DisplayName("AlgorithmType算法类型枚举测试")
public class AlgorithmTypeTest extends BaseUnitTest {

    // ==================== 枚举值测试 ====================

    @Test
    @DisplayName("测试枚举值-DEFAULT")
    public void testDefault() {
        AlgorithmType type = AlgorithmType.DEFAULT;

        assertNull(type.getClazz(), "DEFAULT的clazz应为null");
    }

    @Test
    @DisplayName("测试枚举值-BASE64")
    public void testBase64() {
        AlgorithmType type = AlgorithmType.BASE64;

        assertEquals(Base64Encryptor.class, type.getClazz(), "BASE64应对应Base64Encryptor");
    }

    @Test
    @DisplayName("测试枚举值-AES")
    public void testAes() {
        AlgorithmType type = AlgorithmType.AES;

        assertEquals(AesEncryptor.class, type.getClazz(), "AES应对应AesEncryptor");
    }

    @Test
    @DisplayName("测试枚举值-RSA")
    public void testRsa() {
        AlgorithmType type = AlgorithmType.RSA;

        assertEquals(RsaEncryptor.class, type.getClazz(), "RSA应对应RsaEncryptor");
    }

    @Test
    @DisplayName("测试枚举值-SM2")
    public void testSm2() {
        AlgorithmType type = AlgorithmType.SM2;

        assertEquals(Sm2Encryptor.class, type.getClazz(), "SM2应对应Sm2Encryptor");
    }

    @Test
    @DisplayName("测试枚举值-SM4")
    public void testSm4() {
        AlgorithmType type = AlgorithmType.SM4;

        assertEquals(Sm4Encryptor.class, type.getClazz(), "SM4应对应Sm4Encryptor");
    }

    // ==================== 枚举数量测试 ====================

    @Test
    @DisplayName("测试枚举数量-应有6个枚举值")
    public void testEnumCount() {
        AlgorithmType[] values = AlgorithmType.values();

        assertEquals(6, values.length, "应有6个枚举值");
    }

    // ==================== valueOf测试 ====================

    @Test
    @DisplayName("测试valueOf-DEFAULT")
    public void testValueOfDefault() {
        AlgorithmType type = AlgorithmType.valueOf("DEFAULT");

        assertEquals(AlgorithmType.DEFAULT, type, "valueOf DEFAULT应正确");
    }

    @Test
    @DisplayName("测试valueOf-AES")
    public void testValueOfAes() {
        AlgorithmType type = AlgorithmType.valueOf("AES");

        assertEquals(AlgorithmType.AES, type, "valueOf AES应正确");
    }

    @Test
    @DisplayName("测试valueOf-不存在的值应抛出异常")
    public void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            AlgorithmType.valueOf("INVALID");
        }, "不存在的枚举值应抛出异常");
    }

    // ==================== ordinal测试 ====================

    @Test
    @DisplayName("测试ordinal-枚举顺序")
    public void testOrdinal() {
        assertEquals(0, AlgorithmType.DEFAULT.ordinal(), "DEFAULT ordinal应为0");
        assertEquals(1, AlgorithmType.BASE64.ordinal(), "BASE64 ordinal应为1");
        assertEquals(2, AlgorithmType.AES.ordinal(), "AES ordinal应为2");
        assertEquals(3, AlgorithmType.RSA.ordinal(), "RSA ordinal应为3");
        assertEquals(4, AlgorithmType.SM2.ordinal(), "SM2 ordinal应为4");
        assertEquals(5, AlgorithmType.SM4.ordinal(), "SM4 ordinal应为5");
    }

    // ==================== name测试 ====================

    @Test
    @DisplayName("测试name-枚举名称")
    public void testName() {
        assertEquals("DEFAULT", AlgorithmType.DEFAULT.name(), "DEFAULT name应正确");
        assertEquals("BASE64", AlgorithmType.BASE64.name(), "BASE64 name应正确");
        assertEquals("AES", AlgorithmType.AES.name(), "AES name应正确");
        assertEquals("RSA", AlgorithmType.RSA.name(), "RSA name应正确");
        assertEquals("SM2", AlgorithmType.SM2.name(), "SM2 name应正确");
        assertEquals("SM4", AlgorithmType.SM4.name(), "SM4 name应正确");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-对称加密算法")
    public void testSymmetricAlgorithms() {
        // AES和SM4是对称加密算法
        assertNotNull(AlgorithmType.AES.getClazz(), "AES应有对应的加密器类");
        assertNotNull(AlgorithmType.SM4.getClazz(), "SM4应有对应的加密器类");
    }

    @Test
    @DisplayName("测试业务场景-非对称加密算法")
    public void testAsymmetricAlgorithms() {
        // RSA和SM2是非对称加密算法
        assertNotNull(AlgorithmType.RSA.getClazz(), "RSA应有对应的加密器类");
        assertNotNull(AlgorithmType.SM2.getClazz(), "SM2应有对应的加密器类");
    }

    @Test
    @DisplayName("测试业务场景-国密算法")
    public void testNationalCryptographyAlgorithms() {
        // SM2和SM4是国密算法
        assertEquals(Sm2Encryptor.class, AlgorithmType.SM2.getClazz(), "SM2应为国密非对称加密");
        assertEquals(Sm4Encryptor.class, AlgorithmType.SM4.getClazz(), "SM4应为国密对称加密");
    }

    @Test
    @DisplayName("测试业务场景-遍历所有算法")
    public void testIterateAllAlgorithms() {
        int withClazzCount = 0;
        int withoutClazzCount = 0;

        for (AlgorithmType type : AlgorithmType.values()) {
            if (type.getClazz() != null) {
                withClazzCount++;
            } else {
                withoutClazzCount++;
            }
        }

        assertEquals(5, withClazzCount, "应有5个算法有对应的加密器类");
        assertEquals(1, withoutClazzCount, "应有1个算法没有对应的加密器类(DEFAULT)");
    }
}
