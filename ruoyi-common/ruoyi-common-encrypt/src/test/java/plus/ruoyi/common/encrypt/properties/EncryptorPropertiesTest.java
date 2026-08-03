package plus.ruoyi.common.encrypt.properties;

import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptorProperties 加密配置属性测试
 *
 * @author 抓蛙师
 */
@DisplayName("EncryptorProperties加密配置属性测试")
public class EncryptorPropertiesTest extends BaseUnitTest {

    // ==================== 注解测试 ====================

    @Test
    @DisplayName("测试注解-ConfigurationProperties前缀")
    public void testConfigurationPropertiesPrefix() {
        ConfigurationProperties annotation = EncryptorProperties.class.getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation, "应有ConfigurationProperties注解");
        assertEquals("mybatis-encryptor", annotation.prefix(), "前缀应为mybatis-encryptor");
    }

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-默认构造")
    public void testDefaultConstructor() {
        EncryptorProperties properties = new EncryptorProperties();

        assertNull(properties.getEnable(), "enable默认应为null");
        assertNull(properties.getAlgorithm(), "algorithm默认应为null");
        assertNull(properties.getPassword(), "password默认应为null");
        assertNull(properties.getPublicKey(), "publicKey默认应为null");
        assertNull(properties.getPrivateKey(), "privateKey默认应为null");
        assertNull(properties.getEncode(), "encode默认应为null");
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-enable")
    public void testSetGetEnable() {
        EncryptorProperties properties = new EncryptorProperties();

        properties.setEnable(true);
        assertTrue(properties.getEnable(), "enable应为true");

        properties.setEnable(false);
        assertFalse(properties.getEnable(), "enable应为false");
    }

    @Test
    @DisplayName("测试Setter/Getter-algorithm")
    public void testSetGetAlgorithm() {
        EncryptorProperties properties = new EncryptorProperties();

        properties.setAlgorithm(AlgorithmType.AES);
        assertEquals(AlgorithmType.AES, properties.getAlgorithm(), "algorithm应为AES");

        properties.setAlgorithm(AlgorithmType.RSA);
        assertEquals(AlgorithmType.RSA, properties.getAlgorithm(), "algorithm应为RSA");

        properties.setAlgorithm(AlgorithmType.SM2);
        assertEquals(AlgorithmType.SM2, properties.getAlgorithm(), "algorithm应为SM2");

        properties.setAlgorithm(AlgorithmType.SM4);
        assertEquals(AlgorithmType.SM4, properties.getAlgorithm(), "algorithm应为SM4");

        properties.setAlgorithm(AlgorithmType.BASE64);
        assertEquals(AlgorithmType.BASE64, properties.getAlgorithm(), "algorithm应为BASE64");
    }

    @Test
    @DisplayName("测试Setter/Getter-password")
    public void testSetGetPassword() {
        EncryptorProperties properties = new EncryptorProperties();

        properties.setPassword("1234567890123456");
        assertEquals("1234567890123456", properties.getPassword(), "password应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-publicKey")
    public void testSetGetPublicKey() {
        EncryptorProperties properties = new EncryptorProperties();

        properties.setPublicKey("testPublicKey");
        assertEquals("testPublicKey", properties.getPublicKey(), "publicKey应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-privateKey")
    public void testSetGetPrivateKey() {
        EncryptorProperties properties = new EncryptorProperties();

        properties.setPrivateKey("testPrivateKey");
        assertEquals("testPrivateKey", properties.getPrivateKey(), "privateKey应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-encode")
    public void testSetGetEncode() {
        EncryptorProperties properties = new EncryptorProperties();

        properties.setEncode(EncodeType.BASE64);
        assertEquals(EncodeType.BASE64, properties.getEncode(), "encode应为BASE64");

        properties.setEncode(EncodeType.HEX);
        assertEquals(EncodeType.HEX, properties.getEncode(), "encode应为HEX");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-AES加密配置")
    public void testAesConfig() {
        EncryptorProperties properties = new EncryptorProperties();
        properties.setEnable(true);
        properties.setAlgorithm(AlgorithmType.AES);
        properties.setPassword("1234567890123456");
        properties.setEncode(EncodeType.BASE64);

        assertTrue(properties.getEnable(), "应启用加密");
        assertEquals(AlgorithmType.AES, properties.getAlgorithm(), "应使用AES算法");
        assertEquals("1234567890123456", properties.getPassword(), "应有16位密钥");
        assertEquals(EncodeType.BASE64, properties.getEncode(), "应使用BASE64编码");
    }

    @Test
    @DisplayName("测试业务场景-RSA加密配置")
    public void testRsaConfig() {
        EncryptorProperties properties = new EncryptorProperties();
        properties.setEnable(true);
        properties.setAlgorithm(AlgorithmType.RSA);
        properties.setPublicKey("MIIBIjANBgkqhkiG9w0...");
        properties.setPrivateKey("MIIEvQIBADANBgkqhkiG...");
        properties.setEncode(EncodeType.BASE64);

        assertTrue(properties.getEnable(), "应启用加密");
        assertEquals(AlgorithmType.RSA, properties.getAlgorithm(), "应使用RSA算法");
        assertNotNull(properties.getPublicKey(), "应有公钥");
        assertNotNull(properties.getPrivateKey(), "应有私钥");
    }

    @Test
    @DisplayName("测试业务场景-SM4国密配置")
    public void testSm4Config() {
        EncryptorProperties properties = new EncryptorProperties();
        properties.setEnable(true);
        properties.setAlgorithm(AlgorithmType.SM4);
        properties.setPassword("1234567890123456");
        properties.setEncode(EncodeType.HEX);

        assertTrue(properties.getEnable(), "应启用加密");
        assertEquals(AlgorithmType.SM4, properties.getAlgorithm(), "应使用SM4算法");
        assertEquals(16, properties.getPassword().length(), "SM4密钥应为16位");
        assertEquals(EncodeType.HEX, properties.getEncode(), "应使用HEX编码");
    }

    @Test
    @DisplayName("测试业务场景-禁用加密")
    public void testDisableEncrypt() {
        EncryptorProperties properties = new EncryptorProperties();
        properties.setEnable(false);

        assertFalse(properties.getEnable(), "应禁用加密");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-空字符串密钥")
    public void testEmptyPassword() {
        EncryptorProperties properties = new EncryptorProperties();
        properties.setPassword("");

        assertEquals("", properties.getPassword(), "空字符串密钥应被接受");
    }

    @Test
    @DisplayName("测试边界值-null值")
    public void testNullValues() {
        EncryptorProperties properties = new EncryptorProperties();
        properties.setEnable(null);
        properties.setAlgorithm(null);
        properties.setPassword(null);
        properties.setPublicKey(null);
        properties.setPrivateKey(null);
        properties.setEncode(null);

        assertNull(properties.getEnable(), "enable可为null");
        assertNull(properties.getAlgorithm(), "algorithm可为null");
        assertNull(properties.getPassword(), "password可为null");
        assertNull(properties.getPublicKey(), "publicKey可为null");
        assertNull(properties.getPrivateKey(), "privateKey可为null");
        assertNull(properties.getEncode(), "encode可为null");
    }
}
