package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RsaEncryptor RSA加密器测试
 *
 * @author 抓蛙师
 */
@DisplayName("RsaEncryptor RSA加密器测试")
public class RsaEncryptorTest extends BaseUnitTest {

    private String publicKey;
    private String privateKey;

    @BeforeEach
    public void setUp() {
        Map<String, String> keyMap = EncryptUtils.generateRsaKey();
        publicKey = keyMap.get(EncryptUtils.PUBLIC_KEY);
        privateKey = keyMap.get(EncryptUtils.PRIVATE_KEY);
    }

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-正常构造")
    public void testConstructor() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    @Test
    @DisplayName("测试构造方法-缺少公钥应抛出异常")
    public void testConstructorMissingPublicKey() {
        EncryptContext context = new EncryptContext();
        context.setPrivateKey(privateKey);

        assertThrows(IllegalArgumentException.class, () -> {
            new RsaEncryptor(context);
        }, "缺少公钥应抛出异常");
    }

    @Test
    @DisplayName("测试构造方法-缺少私钥应抛出异常")
    public void testConstructorMissingPrivateKey() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);

        assertThrows(IllegalArgumentException.class, () -> {
            new RsaEncryptor(context);
        }, "缺少私钥应抛出异常");
    }

    @Test
    @DisplayName("测试构造方法-公私钥都为空应抛出异常")
    public void testConstructorBothEmpty() {
        EncryptContext context = new EncryptContext();

        assertThrows(IllegalArgumentException.class, () -> {
            new RsaEncryptor(context);
        }, "公私钥都为空应抛出异常");
    }

    @Test
    @DisplayName("测试构造方法-空字符串密钥应抛出异常")
    public void testConstructorEmptyKeys() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey("");
        context.setPrivateKey("");

        assertThrows(IllegalArgumentException.class, () -> {
            new RsaEncryptor(context);
        }, "空字符串密钥应抛出异常");
    }

    // ==================== algorithm测试 ====================

    @Test
    @DisplayName("测试algorithm-应返回RSA类型")
    public void testAlgorithm() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);

        assertEquals(AlgorithmType.RSA, encryptor.algorithm(), "算法类型应为RSA");
    }

    // ==================== encrypt测试 ====================

    @Test
    @DisplayName("测试encrypt-Base64编码")
    public void testEncryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String encrypted = encryptor.encrypt("Hello RSA", EncodeType.BASE64);

        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.isEmpty(), "加密结果不应为空");
    }

    @Test
    @DisplayName("测试encrypt-Hex编码")
    public void testEncryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String encrypted = encryptor.encrypt("Hello RSA", EncodeType.HEX);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.matches("[0-9a-fA-F]+"), "Hex编码应只包含十六进制字符");
    }

    @Test
    @DisplayName("测试encrypt-DEFAULT编码应使用Base64")
    public void testEncryptDefault() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String encryptedDefault = encryptor.encrypt("Hello RSA", EncodeType.DEFAULT);

        assertNotNull(encryptedDefault, "加密结果不应为null");
        // RSA每次加密结果可能不同，所以只验证非空
    }

    // ==================== decrypt测试 ====================

    @Test
    @DisplayName("测试decrypt-Base64编码数据")
    public void testDecryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String original = "Hello RSA Decrypt";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试decrypt-Hex编码数据")
    public void testDecryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String original = "Hello RSA Hex Decrypt";
        String encrypted = encryptor.encrypt(original, EncodeType.HEX);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    // ==================== 加解密一致性测试 ====================

    @Test
    @DisplayName("测试一致性-多次加解密")
    public void testConsistency() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String original = "Test RSA Consistency";

        for (int i = 0; i < 5; i++) {
            String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
            String decrypted = encryptor.decrypt(encrypted);
            assertEquals(original, decrypted, "第" + i + "次解密后应与原文一致");
        }
    }

    @Test
    @DisplayName("测试一致性-每次加密结果可能不同")
    public void testEncryptRandomness() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String original = "Test Randomness";

        String encrypted1 = encryptor.encrypt(original, EncodeType.BASE64);
        String encrypted2 = encryptor.encrypt(original, EncodeType.BASE64);

        // RSA加密每次结果可能不同（取决于实现），但解密都应正确
        assertEquals(original, encryptor.decrypt(encrypted1), "第一次解密应正确");
        assertEquals(original, encryptor.decrypt(encrypted2), "第二次解密应正确");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-敏感数据加密")
    public void testSensitiveDataEncryption() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);

        // 手机号
        String phone = "13800138000";
        String encryptedPhone = encryptor.encrypt(phone, EncodeType.BASE64);
        assertEquals(phone, encryptor.decrypt(encryptedPhone), "手机号解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-中文内容加密")
    public void testChineseContent() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String original = "中文测试";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "中文内容解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-空字符串加密")
    public void testEmptyString() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        RsaEncryptor encryptor = new RsaEncryptor(context);
        String encrypted = encryptor.encrypt("", EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals("", decrypted, "空字符串解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-新密钥对独立性")
    public void testDifferentKeyPairs() {
        // 生成两对不同的密钥
        Map<String, String> keyMap1 = EncryptUtils.generateRsaKey();
        Map<String, String> keyMap2 = EncryptUtils.generateRsaKey();

        EncryptContext context1 = new EncryptContext();
        context1.setPublicKey(keyMap1.get(EncryptUtils.PUBLIC_KEY));
        context1.setPrivateKey(keyMap1.get(EncryptUtils.PRIVATE_KEY));

        EncryptContext context2 = new EncryptContext();
        context2.setPublicKey(keyMap2.get(EncryptUtils.PUBLIC_KEY));
        context2.setPrivateKey(keyMap2.get(EncryptUtils.PRIVATE_KEY));

        RsaEncryptor encryptor1 = new RsaEncryptor(context1);
        RsaEncryptor encryptor2 = new RsaEncryptor(context2);

        String original = "Test Different Keys";

        // 各自加密解密应正常
        String encrypted1 = encryptor1.encrypt(original, EncodeType.BASE64);
        String encrypted2 = encryptor2.encrypt(original, EncodeType.BASE64);

        assertEquals(original, encryptor1.decrypt(encrypted1), "密钥对1解密应正确");
        assertEquals(original, encryptor2.decrypt(encrypted2), "密钥对2解密应正确");
    }
}
