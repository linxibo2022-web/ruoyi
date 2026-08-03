package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AesEncryptor AES加密器测试
 *
 * @author 抓蛙师
 */
@DisplayName("AesEncryptor AES加密器测试")
public class AesEncryptorTest extends BaseUnitTest {

    private static final String PASSWORD_16 = "1234567890123456";
    private static final String PASSWORD_24 = "123456789012345678901234";
    private static final String PASSWORD_32 = "12345678901234567890123456789012";

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-16位密钥")
    public void testConstructor16Key() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    @Test
    @DisplayName("测试构造方法-24位密钥")
    public void testConstructor24Key() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_24);

        AesEncryptor encryptor = new AesEncryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    @Test
    @DisplayName("测试构造方法-32位密钥")
    public void testConstructor32Key() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_32);

        AesEncryptor encryptor = new AesEncryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    // ==================== algorithm测试 ====================

    @Test
    @DisplayName("测试algorithm-应返回AES类型")
    public void testAlgorithm() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);

        assertEquals(AlgorithmType.AES, encryptor.algorithm(), "算法类型应为AES");
    }

    // ==================== encrypt测试 ====================

    @Test
    @DisplayName("测试encrypt-Base64编码")
    public void testEncryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String encrypted = encryptor.encrypt("Hello AES", EncodeType.BASE64);

        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.isEmpty(), "加密结果不应为空");
    }

    @Test
    @DisplayName("测试encrypt-Hex编码")
    public void testEncryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String encrypted = encryptor.encrypt("Hello AES", EncodeType.HEX);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.matches("[0-9a-fA-F]+"), "Hex编码应只包含十六进制字符");
    }

    @Test
    @DisplayName("测试encrypt-DEFAULT编码应使用Base64")
    public void testEncryptDefault() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String encryptedDefault = encryptor.encrypt("Hello AES", EncodeType.DEFAULT);
        String encryptedBase64 = encryptor.encrypt("Hello AES", EncodeType.BASE64);

        // DEFAULT编码应与Base64编码结果一致
        assertEquals(encryptedDefault, encryptedBase64, "DEFAULT编码应使用Base64");
    }

    // ==================== decrypt测试 ====================

    @Test
    @DisplayName("测试decrypt-Base64编码数据")
    public void testDecryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String original = "Hello AES Decrypt";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试decrypt-Hex编码数据")
    public void testDecryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String original = "Hello AES Hex Decrypt";
        String encrypted = encryptor.encrypt(original, EncodeType.HEX);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    // ==================== 加解密一致性测试 ====================

    @Test
    @DisplayName("测试一致性-多次加解密")
    public void testConsistency() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String original = "Test Consistency";

        for (int i = 0; i < 10; i++) {
            String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
            String decrypted = encryptor.decrypt(encrypted);
            assertEquals(original, decrypted, "第" + i + "次解密后应与原文一致");
        }
    }

    @Test
    @DisplayName("测试一致性-不同密钥长度")
    public void testDifferentKeyLengths() {
        String original = "Test Different Keys";

        // 16位密钥
        EncryptContext context16 = new EncryptContext();
        context16.setPassword(PASSWORD_16);
        AesEncryptor encryptor16 = new AesEncryptor(context16);
        String encrypted16 = encryptor16.encrypt(original, EncodeType.BASE64);
        assertEquals(original, encryptor16.decrypt(encrypted16), "16位密钥加解密应一致");

        // 24位密钥
        EncryptContext context24 = new EncryptContext();
        context24.setPassword(PASSWORD_24);
        AesEncryptor encryptor24 = new AesEncryptor(context24);
        String encrypted24 = encryptor24.encrypt(original, EncodeType.BASE64);
        assertEquals(original, encryptor24.decrypt(encrypted24), "24位密钥加解密应一致");

        // 32位密钥
        EncryptContext context32 = new EncryptContext();
        context32.setPassword(PASSWORD_32);
        AesEncryptor encryptor32 = new AesEncryptor(context32);
        String encrypted32 = encryptor32.encrypt(original, EncodeType.BASE64);
        assertEquals(original, encryptor32.decrypt(encrypted32), "32位密钥加解密应一致");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-敏感数据加密")
    public void testSensitiveDataEncryption() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);

        // 手机号
        String phone = "13800138000";
        String encryptedPhone = encryptor.encrypt(phone, EncodeType.BASE64);
        assertEquals(phone, encryptor.decrypt(encryptedPhone), "手机号解密应正确");

        // 身份证号
        String idCard = "110101199003071234";
        String encryptedIdCard = encryptor.encrypt(idCard, EncodeType.BASE64);
        assertEquals(idCard, encryptor.decrypt(encryptedIdCard), "身份证号解密应正确");

        // 银行卡号
        String bankCard = "6222021234567890123";
        String encryptedBankCard = encryptor.encrypt(bankCard, EncodeType.BASE64);
        assertEquals(bankCard, encryptor.decrypt(encryptedBankCard), "银行卡号解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-中文内容加密")
    public void testChineseContent() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String original = "中文测试内容，包含特殊字符：！@#￥%……&*（）";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "中文内容解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-空字符串加密")
    public void testEmptyString() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);
        String encrypted = encryptor.encrypt("", EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals("", decrypted, "空字符串解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-长文本加密")
    public void testLongText() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        AesEncryptor encryptor = new AesEncryptor(context);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("测试文本").append(i);
        }
        String original = sb.toString();

        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "长文本解密应正确");
    }
}
