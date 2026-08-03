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
 * Sm2Encryptor SM2国密加密器测试
 *
 * @author 抓蛙师
 */
@DisplayName("Sm2Encryptor SM2国密加密器测试")
public class Sm2EncryptorTest extends BaseUnitTest {

    private String publicKey;
    private String privateKey;

    @BeforeEach
    public void setUp() {
        Map<String, String> keyMap = EncryptUtils.generateSm2Key();
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

        Sm2Encryptor encryptor = new Sm2Encryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
    }

    @Test
    @DisplayName("测试构造方法-缺少公钥应抛出异常")
    public void testConstructorMissingPublicKey() {
        EncryptContext context = new EncryptContext();
        context.setPrivateKey(privateKey);

        assertThrows(IllegalArgumentException.class, () -> {
            new Sm2Encryptor(context);
        }, "缺少公钥应抛出异常");
    }

    @Test
    @DisplayName("测试构造方法-缺少私钥应抛出异常")
    public void testConstructorMissingPrivateKey() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);

        assertThrows(IllegalArgumentException.class, () -> {
            new Sm2Encryptor(context);
        }, "缺少私钥应抛出异常");
    }

    @Test
    @DisplayName("测试构造方法-公私钥都为空应抛出异常")
    public void testConstructorBothEmpty() {
        EncryptContext context = new EncryptContext();

        assertThrows(IllegalArgumentException.class, () -> {
            new Sm2Encryptor(context);
        }, "公私钥都为空应抛出异常");
    }

    // ==================== algorithm测试 ====================

    @Test
    @DisplayName("测试algorithm-应返回SM2类型")
    public void testAlgorithm() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);

        assertEquals(AlgorithmType.SM2, encryptor.algorithm(), "算法类型应为SM2");
    }

    // ==================== encrypt测试 ====================

    @Test
    @DisplayName("测试encrypt-Base64编码")
    public void testEncryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String encrypted = encryptor.encrypt("Hello SM2", EncodeType.BASE64);

        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.isEmpty(), "加密结果不应为空");
    }

    @Test
    @DisplayName("测试encrypt-Hex编码")
    public void testEncryptHex() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String encrypted = encryptor.encrypt("Hello SM2", EncodeType.HEX);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.matches("[0-9a-fA-F]+"), "Hex编码应只包含十六进制字符");
    }

    @Test
    @DisplayName("测试encrypt-DEFAULT编码应使用Base64")
    public void testEncryptDefault() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String encryptedDefault = encryptor.encrypt("Hello SM2", EncodeType.DEFAULT);

        assertNotNull(encryptedDefault, "加密结果不应为null");
    }

    // ==================== decrypt测试 ====================

    @Test
    @DisplayName("测试decrypt-Base64编码数据")
    public void testDecryptBase64() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String original = "Hello SM2 Decrypt";
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

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String original = "Hello SM2 Hex Decrypt";
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

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String original = "Test SM2 Consistency";

        for (int i = 0; i < 5; i++) {
            String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
            String decrypted = encryptor.decrypt(encrypted);
            assertEquals(original, decrypted, "第" + i + "次解密后应与原文一致");
        }
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-敏感数据加密")
    public void testSensitiveDataEncryption() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);

        // 手机号
        String phone = "13800138000";
        String encryptedPhone = encryptor.encrypt(phone, EncodeType.BASE64);
        assertEquals(phone, encryptor.decrypt(encryptedPhone), "手机号解密应正确");

        // 身份证号
        String idCard = "110101199003071234";
        String encryptedIdCard = encryptor.encrypt(idCard, EncodeType.BASE64);
        assertEquals(idCard, encryptor.decrypt(encryptedIdCard), "身份证号解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-中文内容加密")
    public void testChineseContent() {
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String original = "国密SM2加密测试";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "中文内容解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-短字符串加密")
    public void testShortString() {
        // SM2算法不支持空字符串加密，测试短字符串
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);
        String original = "A";
        String encrypted = encryptor.encrypt(original, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted, "短字符串解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-新密钥对独立性")
    public void testDifferentKeyPairs() {
        // 生成两对不同的SM2密钥
        Map<String, String> keyMap1 = EncryptUtils.generateSm2Key();
        Map<String, String> keyMap2 = EncryptUtils.generateSm2Key();

        EncryptContext context1 = new EncryptContext();
        context1.setPublicKey(keyMap1.get(EncryptUtils.PUBLIC_KEY));
        context1.setPrivateKey(keyMap1.get(EncryptUtils.PRIVATE_KEY));

        EncryptContext context2 = new EncryptContext();
        context2.setPublicKey(keyMap2.get(EncryptUtils.PUBLIC_KEY));
        context2.setPrivateKey(keyMap2.get(EncryptUtils.PRIVATE_KEY));

        Sm2Encryptor encryptor1 = new Sm2Encryptor(context1);
        Sm2Encryptor encryptor2 = new Sm2Encryptor(context2);

        String original = "Test Different SM2 Keys";

        // 各自加密解密应正常
        String encrypted1 = encryptor1.encrypt(original, EncodeType.BASE64);
        String encrypted2 = encryptor2.encrypt(original, EncodeType.BASE64);

        assertEquals(original, encryptor1.decrypt(encrypted1), "密钥对1解密应正确");
        assertEquals(original, encryptor2.decrypt(encrypted2), "密钥对2解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-国密合规性")
    public void testNationalCryptographyCompliance() {
        // SM2是中国国家密码管理局制定的椭圆曲线公钥密码算法
        EncryptContext context = new EncryptContext();
        context.setPublicKey(publicKey);
        context.setPrivateKey(privateKey);

        Sm2Encryptor encryptor = new Sm2Encryptor(context);

        // 验证算法类型为SM2
        assertEquals(AlgorithmType.SM2, encryptor.algorithm(), "应为SM2国密算法");

        // 验证加解密功能正常
        String data = "国密SM2测试数据";
        String encrypted = encryptor.encrypt(data, EncodeType.BASE64);
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(data, decrypted, "SM2加解密应正确");
    }
}
