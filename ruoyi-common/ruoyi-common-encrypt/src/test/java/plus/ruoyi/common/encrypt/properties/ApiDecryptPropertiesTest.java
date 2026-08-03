package plus.ruoyi.common.encrypt.properties;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiDecryptProperties API解密配置属性测试
 *
 * @author 抓蛙师
 */
@DisplayName("ApiDecryptProperties API解密配置属性测试")
public class ApiDecryptPropertiesTest extends BaseUnitTest {

    // ==================== 注解测试 ====================

    @Test
    @DisplayName("测试注解-ConfigurationProperties前缀")
    public void testConfigurationPropertiesPrefix() {
        ConfigurationProperties annotation = ApiDecryptProperties.class.getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation, "应有ConfigurationProperties注解");
        assertEquals("api-decrypt", annotation.prefix(), "前缀应为api-decrypt");
    }

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-默认构造")
    public void testDefaultConstructor() {
        ApiDecryptProperties properties = new ApiDecryptProperties();

        assertNull(properties.getEnabled(), "enabled默认应为null");
        assertNull(properties.getHeaderFlag(), "headerFlag默认应为null");
        assertNull(properties.getPublicKey(), "publicKey默认应为null");
        assertNull(properties.getPrivateKey(), "privateKey默认应为null");
    }

    // ==================== Setter/Getter测试 ====================

    @Test
    @DisplayName("测试Setter/Getter-enabled")
    public void testSetGetEnabled() {
        ApiDecryptProperties properties = new ApiDecryptProperties();

        properties.setEnabled(true);
        assertTrue(properties.getEnabled(), "enabled应为true");

        properties.setEnabled(false);
        assertFalse(properties.getEnabled(), "enabled应为false");
    }

    @Test
    @DisplayName("测试Setter/Getter-headerFlag")
    public void testSetGetHeaderFlag() {
        ApiDecryptProperties properties = new ApiDecryptProperties();

        properties.setHeaderFlag("encrypt-key");
        assertEquals("encrypt-key", properties.getHeaderFlag(), "headerFlag应为设置的值");

        properties.setHeaderFlag("crypto-key");
        assertEquals("crypto-key", properties.getHeaderFlag(), "headerFlag应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-publicKey")
    public void testSetGetPublicKey() {
        ApiDecryptProperties properties = new ApiDecryptProperties();

        properties.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...");
        assertEquals("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...", properties.getPublicKey(), "publicKey应为设置的值");
    }

    @Test
    @DisplayName("测试Setter/Getter-privateKey")
    public void testSetGetPrivateKey() {
        ApiDecryptProperties properties = new ApiDecryptProperties();

        properties.setPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcw...");
        assertEquals("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcw...", properties.getPrivateKey(), "privateKey应为设置的值");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-标准API加密配置")
    public void testStandardApiConfig() {
        ApiDecryptProperties properties = new ApiDecryptProperties();
        properties.setEnabled(true);
        properties.setHeaderFlag("encrypt-key");
        properties.setPublicKey("MIIBIjANBgkqhkiG9w0...");
        properties.setPrivateKey("MIIEvQIBADANBgkqhkiG...");

        assertTrue(properties.getEnabled(), "应启用API加解密");
        assertEquals("encrypt-key", properties.getHeaderFlag(), "请求头标识应正确");
        assertNotNull(properties.getPublicKey(), "应有公钥");
        assertNotNull(properties.getPrivateKey(), "应有私钥");
    }

    @Test
    @DisplayName("测试业务场景-禁用API加密")
    public void testDisableApiEncrypt() {
        ApiDecryptProperties properties = new ApiDecryptProperties();
        properties.setEnabled(false);

        assertFalse(properties.getEnabled(), "应禁用API加解密");
    }

    @Test
    @DisplayName("测试业务场景-自定义请求头标识")
    public void testCustomHeaderFlag() {
        ApiDecryptProperties properties = new ApiDecryptProperties();
        properties.setEnabled(true);
        properties.setHeaderFlag("X-Crypto-Key");

        assertEquals("X-Crypto-Key", properties.getHeaderFlag(), "应支持自定义请求头标识");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("测试边界值-空字符串值")
    public void testEmptyValues() {
        ApiDecryptProperties properties = new ApiDecryptProperties();
        properties.setHeaderFlag("");
        properties.setPublicKey("");
        properties.setPrivateKey("");

        assertEquals("", properties.getHeaderFlag(), "空字符串headerFlag应被接受");
        assertEquals("", properties.getPublicKey(), "空字符串publicKey应被接受");
        assertEquals("", properties.getPrivateKey(), "空字符串privateKey应被接受");
    }

    @Test
    @DisplayName("测试边界值-null值")
    public void testNullValues() {
        ApiDecryptProperties properties = new ApiDecryptProperties();
        properties.setEnabled(null);
        properties.setHeaderFlag(null);
        properties.setPublicKey(null);
        properties.setPrivateKey(null);

        assertNull(properties.getEnabled(), "enabled可为null");
        assertNull(properties.getHeaderFlag(), "headerFlag可为null");
        assertNull(properties.getPublicKey(), "publicKey可为null");
        assertNull(properties.getPrivateKey(), "privateKey可为null");
    }

    @Test
    @DisplayName("测试边界值-长密钥")
    public void testLongKeys() {
        ApiDecryptProperties properties = new ApiDecryptProperties();

        // 模拟真实的RSA密钥长度
        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longKey.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        }

        properties.setPublicKey(longKey.toString());
        properties.setPrivateKey(longKey.toString());

        assertEquals(longKey.toString(), properties.getPublicKey(), "应支持长公钥");
        assertEquals(longKey.toString(), properties.getPrivateKey(), "应支持长私钥");
    }
}
