package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base64Encryptor Base64编码器测试
 *
 * @author 抓蛙师
 */
@DisplayName("Base64Encryptor Base64编码器测试")
public class Base64EncryptorTest extends BaseUnitTest {

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-无需密钥")
    public void testConstructor() {
        EncryptContext context = new EncryptContext();

        Base64Encryptor encryptor = new Base64Encryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    // ==================== algorithm测试 ====================

    @Test
    @DisplayName("测试algorithm-应返回BASE64类型")
    public void testAlgorithm() {
        EncryptContext context = new EncryptContext();

        Base64Encryptor encryptor = new Base64Encryptor(context);

        assertEquals(AlgorithmType.BASE64, encryptor.algorithm(), "算法类型应为BASE64");
    }

    // ==================== encrypt测试 ====================

    @Test
    @DisplayName("测试encrypt-普通文本")
    public void testEncryptNormalText() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String encrypted = encryptor.encrypt("Hello World", EncodeType.BASE64);

        assertEquals("SGVsbG8gV29ybGQ=", encrypted, "Base64编码应正确");
    }

    @Test
    @DisplayName("测试encrypt-中文")
    public void testEncryptChinese() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "你好世界";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);

        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.contains("你"), "加密后不应包含原文");
    }

    @Test
    @DisplayName("测试encrypt-encodeType参数被忽略")
    public void testEncryptIgnoresEncodeType() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "Test Data";
        String encryptedBase64 = encryptor.encrypt(original, EncodeType.BASE64);
        String encryptedHex = encryptor.encrypt(original, EncodeType.HEX);
        String encryptedDefault = encryptor.encrypt(original, EncodeType.DEFAULT);

        // Base64编码器忽略encodeType参数，都使用Base64编码
        assertEquals(encryptedBase64, encryptedHex, "不同编码类型结果应相同");
        assertEquals(encryptedBase64, encryptedDefault, "不同编码类型结果应相同");
    }

    // ==================== decrypt测试 ====================

    @Test
    @DisplayName("测试decrypt-普通文本")
    public void testDecryptNormalText() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String decrypted = encryptor.decrypt("SGVsbG8gV29ybGQ=");

        assertEquals("Hello World", decrypted, "解密结果应正确");
    }

    @Test
    @DisplayName("测试decrypt-中文")
    public void testDecryptChinese() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "你好世界";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "中文解密应正确");
    }

    // ==================== 加解密一致性测试 ====================

    @Test
    @DisplayName("测试一致性-多次加解密")
    public void testConsistency() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "Test Consistency";

        for (int i = 0; i < 10; i++) {
            String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
            String decrypted = encryptor.decrypt(encrypted);
            assertEquals(original, decrypted, "第" + i + "次解密后应与原文一致");
        }
    }

    @Test
    @DisplayName("测试一致性-相同输入相同输出")
    public void testSameInputSameOutput() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "Same Input Test";
        String encrypted1 = encryptor.encrypt(original, EncodeType.BASE64);
        String encrypted2 = encryptor.encrypt(original, EncodeType.BASE64);

        assertEquals(encrypted1, encrypted2, "相同输入应产生相同输出");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-URL安全编码")
    public void testUrlSafeContent() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "https://example.com?name=test&id=123";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "URL解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-JSON数据编码")
    public void testJsonData() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "{\"name\":\"张三\",\"age\":25,\"address\":\"北京市\"}";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "JSON数据解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-空字符串")
    public void testEmptyString() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String encrypted = encryptor.encrypt("", EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals("", decrypted, "空字符串解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-特殊字符")
    public void testSpecialCharacters() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        String original = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "特殊字符解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-长文本")
    public void testLongText() {
        EncryptContext context = new EncryptContext();
        Base64Encryptor encryptor = new Base64Encryptor(context);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Base64测试").append(i);
        }
        String original = sb.toString();

        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "长文本解密应正确");
    }
}
