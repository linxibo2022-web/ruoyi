package plus.ruoyi.common.encrypt.core;

import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptContext 加密上下文测试
 *
 * @author 抓蛙师
 */
@DisplayName("EncryptContext加密上下文测试")
public class EncryptContextTest extends BaseUnitTest {

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-默认构造")
    public void testDefaultConstructor() {
        EncryptContext context = new EncryptContext();

        assertNull(context.getAlgorithm(), "algorithm默认应为null");
        assertNull(context.getPassword(), "password默认应为null");
        assertNull(context.getPublicKey(), "publicKey默认应为null");
        assertNull(context.getPrivateKey(), "privateKey默认应为null");
        assertNull(context.getEncode(), "encode默认应为null");
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-algorithm")
    public void testSetGetAlgorithm() {
        EncryptContext context = new EncryptContext();

        context.setAlgorithm(AlgorithmType.AES);
        assertEquals(AlgorithmType.AES, context.getAlgorithm(), "algorithm应为AES");

        context.setAlgorithm(AlgorithmType.RSA);
        assertEquals(AlgorithmType.RSA, context.getAlgorithm(), "algorithm应为RSA");

        context.setAlgorithm(AlgorithmType.SM2);
        assertEquals(AlgorithmType.SM2, context.getAlgorithm(), "algorithm应为SM2");

        context.setAlgorithm(AlgorithmType.SM4);
        assertEquals(AlgorithmType.SM4, context.getAlgorithm(), "algorithm应为SM4");

        context.setAlgorithm(AlgorithmType.BASE64);
        assertEquals(AlgorithmType.BASE64, context.getAlgorithm(), "algorithm应为BASE64");
    }

    @Test
    @DisplayName("测试Setter/Getter-password")
    public void testSetGetPassword() {
        EncryptContext context = new EncryptContext();

        context.setPassword("1234567890123456");
        assertEquals("1234567890123456", context.getPassword(), "password应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-publicKey")
    public void testSetGetPublicKey() {
        EncryptContext context = new EncryptContext();

        context.setPublicKey("testPublicKey");
        assertEquals("testPublicKey", context.getPublicKey(), "publicKey应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-privateKey")
    public void testSetGetPrivateKey() {
        EncryptContext context = new EncryptContext();

        context.setPrivateKey("testPrivateKey");
        assertEquals("testPrivateKey", context.getPrivateKey(), "privateKey应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-encode")
    public void testSetGetEncode() {
        EncryptContext context = new EncryptContext();

        context.setEncode(EncodeType.BASE64);
        assertEquals(EncodeType.BASE64, context.getEncode(), "encode应为BASE64");

        context.setEncode(EncodeType.HEX);
        assertEquals(EncodeType.HEX, context.getEncode(), "encode应为HEX");

        context.setEncode(EncodeType.DEFAULT);
        assertEquals(EncodeType.DEFAULT, context.getEncode(), "encode应为DEFAULT");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-AES上下文配置")
    public void testAesContext() {
        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword("1234567890123456");
        context.setEncode(EncodeType.BASE64);

        assertEquals(AlgorithmType.AES, context.getAlgorithm(), "应使用AES算法");
        assertEquals("1234567890123456", context.getPassword(), "应有16位密钥");
        assertEquals(EncodeType.BASE64, context.getEncode(), "应使用BASE64编码");
    }

    @Test
    @DisplayName("测试业务场景-RSA上下文配置")
    public void testRsaContext() {
        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.RSA);
        context.setPublicKey("rsaPublicKey");
        context.setPrivateKey("rsaPrivateKey");
        context.setEncode(EncodeType.BASE64);

        assertEquals(AlgorithmType.RSA, context.getAlgorithm(), "应使用RSA算法");
        assertNotNull(context.getPublicKey(), "应有公钥");
        assertNotNull(context.getPrivateKey(), "应有私钥");
    }

    @Test
    @DisplayName("测试业务场景-SM2上下文配置")
    public void testSm2Context() {
        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.SM2);
        context.setPublicKey("sm2PublicKey");
        context.setPrivateKey("sm2PrivateKey");
        context.setEncode(EncodeType.HEX);

        assertEquals(AlgorithmType.SM2, context.getAlgorithm(), "应使用SM2算法");
        assertEquals(EncodeType.HEX, context.getEncode(), "应使用HEX编码");
    }

    @Test
    @DisplayName("测试业务场景-SM4上下文配置")
    public void testSm4Context() {
        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.SM4);
        context.setPassword("1234567890123456");
        context.setEncode(EncodeType.BASE64);

        assertEquals(AlgorithmType.SM4, context.getAlgorithm(), "应使用SM4算法");
        assertEquals(16, context.getPassword().length(), "SM4密钥应为16位");
    }

    // ==================== hashCode测试 ====================

    @Test
    @DisplayName("测试hashCode-相同配置应有相同hashCode")
    public void testHashCodeSameConfig() {
        EncryptContext context1 = new EncryptContext();
        context1.setAlgorithm(AlgorithmType.AES);
        context1.setPassword("1234567890123456");
        context1.setEncode(EncodeType.BASE64);

        EncryptContext context2 = new EncryptContext();
        context2.setAlgorithm(AlgorithmType.AES);
        context2.setPassword("1234567890123456");
        context2.setEncode(EncodeType.BASE64);

        assertEquals(context1.hashCode(), context2.hashCode(), "相同配置应有相同hashCode");
    }

    @Test
    @DisplayName("测试hashCode-不同配置应有不同hashCode")
    public void testHashCodeDifferentConfig() {
        EncryptContext context1 = new EncryptContext();
        context1.setAlgorithm(AlgorithmType.AES);
        context1.setPassword("1234567890123456");

        EncryptContext context2 = new EncryptContext();
        context2.setAlgorithm(AlgorithmType.SM4);
        context2.setPassword("1234567890123456");

        assertNotEquals(context1.hashCode(), context2.hashCode(), "不同配置应有不同hashCode");
    }

    // ==================== equals测试 ====================

    @Test
    @DisplayName("测试equals-相同配置应相等")
    public void testEqualsSameConfig() {
        EncryptContext context1 = new EncryptContext();
        context1.setAlgorithm(AlgorithmType.AES);
        context1.setPassword("1234567890123456");
        context1.setEncode(EncodeType.BASE64);

        EncryptContext context2 = new EncryptContext();
        context2.setAlgorithm(AlgorithmType.AES);
        context2.setPassword("1234567890123456");
        context2.setEncode(EncodeType.BASE64);

        assertEquals(context1, context2, "相同配置应相等");
    }

    @Test
    @DisplayName("测试equals-不同算法应不相等")
    public void testEqualsDifferentAlgorithm() {
        EncryptContext context1 = new EncryptContext();
        context1.setAlgorithm(AlgorithmType.AES);

        EncryptContext context2 = new EncryptContext();
        context2.setAlgorithm(AlgorithmType.SM4);

        assertNotEquals(context1, context2, "不同算法应不相等");
    }

    @Test
    @DisplayName("测试equals-不同密钥应不相等")
    public void testEqualsDifferentPassword() {
        EncryptContext context1 = new EncryptContext();
        context1.setAlgorithm(AlgorithmType.AES);
        context1.setPassword("1234567890123456");

        EncryptContext context2 = new EncryptContext();
        context2.setAlgorithm(AlgorithmType.AES);
        context2.setPassword("abcdefghijklmnop");

        assertNotEquals(context1, context2, "不同密钥应不相等");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-null值")
    public void testNullValues() {
        EncryptContext context = new EncryptContext();
        context.setAlgorithm(null);
        context.setPassword(null);
        context.setPublicKey(null);
        context.setPrivateKey(null);
        context.setEncode(null);

        assertNull(context.getAlgorithm(), "algorithm可为null");
        assertNull(context.getPassword(), "password可为null");
        assertNull(context.getPublicKey(), "publicKey可为null");
        assertNull(context.getPrivateKey(), "privateKey可为null");
        assertNull(context.getEncode(), "encode可为null");
    }

    @Test
    @DisplayName("测试边界值-空字符串")
    public void testEmptyStrings() {
        EncryptContext context = new EncryptContext();
        context.setPassword("");
        context.setPublicKey("");
        context.setPrivateKey("");

        assertEquals("", context.getPassword(), "空字符串password应被接受");
        assertEquals("", context.getPublicKey(), "空字符串publicKey应被接受");
        assertEquals("", context.getPrivateKey(), "空字符串privateKey应被接受");
    }
}
