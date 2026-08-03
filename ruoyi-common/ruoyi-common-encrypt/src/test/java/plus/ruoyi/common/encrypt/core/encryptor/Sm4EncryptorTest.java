package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sm4Encryptor SM4国密加密器测试
 *
 * @author 抓蛙师
 */
@DisplayName("Sm4Encryptor SM4国密加密器测试")
public class Sm4EncryptorTest extends BaseUnitTest {

    private static final String PASSWORD_16 = "1234567890123456"; // SM4要求16位密钥

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-16位密钥")
    public void testConstructor() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    // ==================== algorithm测试 ====================

    @Test
    @DisplayName("测试algorithm-应返回SM4类型")
    public void testAlgorithm() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);

        assertEquals(AlgorithmType.SM4, encryptor.algorithm(), "算法类型应为SM4");
    }

    // ==================== encrypt测试 ====================

    @Test
    @DisplayName("测试encrypt-Base64编码")
    public void testEncryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String encrypted = encryptor.encrypt("Hello SM4", EncodeType.BASE64);

        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.isEmpty(), "加密结果不应为空");
    }

    @Test
    @DisplayName("测试encrypt-Hex编码")
    public void testEncryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String encrypted = encryptor.encrypt("Hello SM4", EncodeType.HEX);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.matches("[0-9a-fA-F]+"), "Hex编码应只包含十六进制字符");
    }

    @Test
    @DisplayName("测试encrypt-DEFAULT编码应使用Base64")
    public void testEncryptDefault() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String encryptedDefault = encryptor.encrypt("Hello SM4", EncodeType.DEFAULT);
        String encryptedBase64 = encryptor.encrypt("Hello SM4", EncodeType.BASE64);

        // DEFAULT编码应与Base64编码结果一致
        assertEquals(encryptedDefault, encryptedBase64, "DEFAULT编码应使用Base64");
    }

    // ==================== decrypt测试 ====================

    @Test
    @DisplayName("测试decrypt-Base64编码数据")
    public void testDecryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String original = "Hello SM4 Decrypt";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试decrypt-Hex编码数据")
    public void testDecryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String original = "Hello SM4 Hex Decrypt";
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

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String original = "Test SM4 Consistency";

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
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String original = "Same Input Test";

        String encrypted1 = encryptor.encrypt(original, EncodeType.BASE64);
        String encrypted2 = encryptor.encrypt(original, EncodeType.BASE64);

        // SM4是对称加密，相同输入应产生相同输出
        assertEquals(encrypted1, encrypted2, "相同输入应产生相同输出");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-敏感数据加密")
    public void testSensitiveDataEncryption() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);

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

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String original = "国密SM4加密测试，包含中文内容";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "中文内容解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-空字符串加密")
    public void testEmptyString() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String encrypted = encryptor.encrypt("", EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals("", decrypted, "空字符串解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-长文本加密")
    public void testLongText() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("SM4测试").append(i);
        }
        String original = sb.toString();

        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "长文本解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-不同密钥独立性")
    public void testDifferentKeys() {
        String password1 = "1234567890123456";
        String password2 = "abcdefghijklmnop";

        EncryptContext context1 = new EncryptContext();
        context1.setPassword(password1);

        EncryptContext context2 = new EncryptContext();
        context2.setPassword(password2);

        Sm4Encryptor encryptor1 = new Sm4Encryptor(context1);
        Sm4Encryptor encryptor2 = new Sm4Encryptor(context2);

        String original = "Test Different Keys";

        // 使用不同密钥加密应产生不同结果
        String encrypted1 = encryptor1.encrypt(original, EncodeType.BASE64);
        String encrypted2 = encryptor2.encrypt(original, EncodeType.BASE64);

        assertNotEquals(encrypted1, encrypted2, "不同密钥加密结果应不同");

        // 各自解密应正确
        assertEquals(original, encryptor1.decrypt(encrypted1), "密钥1解密应正确");
        assertEquals(original, encryptor2.decrypt(encrypted2), "密钥2解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-国密合规性")
    public void testNationalCryptographyCompliance() {
        // SM4是中国国家密码管理局制定的分组对称加密算法
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);

        // 验证算法类型为SM4
        assertEquals(AlgorithmType.SM4, encryptor.algorithm(), "应为SM4国密算法");

        // 验证加解密功能正常
        String data = "国密SM4测试数据";
        String encrypted = encryptor.encrypt(data, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(data, decrypted, "SM4加解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-特殊字符")
    public void testSpecialCharacters() {
        EncryptContext context = new EncryptContext();
        context.setPassword(PASSWORD_16);

        Sm4Encryptor encryptor = new Sm4Encryptor(context);
        String original = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "特殊字符解密应正确");
    }
}
