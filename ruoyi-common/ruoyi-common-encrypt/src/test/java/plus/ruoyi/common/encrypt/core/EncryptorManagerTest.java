package plus.ruoyi.common.encrypt.core;

import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.encrypt.core.encryptor.AesEncryptor;
import plus.ruoyi.common.encrypt.core.encryptor.Base64Encryptor;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptorManager 加密器管理器测试
 *
 * @author 抓蛙师
 */
@DisplayName("EncryptorManager加密器管理器测试")
public class EncryptorManagerTest extends BaseUnitTest {

    private static final String PASSWORD_16 = "1234567890123456";

    // ==================== 构造方法测试 ====================

    @Test
    @DisplayName("测试构造方法-默认构造")
    public void testDefaultConstructor() {
        EncryptorManager manager = new EncryptorManager();

        assertNotNull(manager, "管理器不应为null");
        assertTrue(manager.encryptorMap.isEmpty(), "加密器缓存应为空");
        assertTrue(manager.fieldCache.isEmpty(), "字段缓存应为空");
    }

    // ==================== registAndGetEncryptor测试 ====================

    @Test
    @DisplayName("测试registAndGetEncryptor-AES加密器")
    public void testRegistAndGetAesEncryptor() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        IEncryptor encryptor = manager.registAndGetEncryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
        assertTrue(encryptor instanceof AesEncryptor, "应返回AesEncryptor实例");
        assertEquals(AlgorithmType.AES, encryptor.algorithm(), "算法应为AES");
    }

    @Test
    @DisplayName("测试registAndGetEncryptor-Base64加密器")
    public void testRegistAndGetBase64Encryptor() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.BASE64);
        context.setEncode(EncodeType.BASE64);

        IEncryptor encryptor = manager.registAndGetEncryptor(context);

        assertNotNull(encryptor, "加密器不应为null");
        assertTrue(encryptor instanceof Base64Encryptor, "应返回Base64Encryptor实例");
    }

    @Test
    @DisplayName("测试registAndGetEncryptor-缓存复用")
    public void testRegistAndGetEncryptorCache() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        IEncryptor encryptor1 = manager.registAndGetEncryptor(context);
        IEncryptor encryptor2 = manager.registAndGetEncryptor(context);

        assertSame(encryptor1, encryptor2, "相同配置应返回同一实例");
    }

    @Test
    @DisplayName("测试registAndGetEncryptor-不同配置不共享")
    public void testRegistAndGetEncryptorDifferentConfig() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context1 = new EncryptContext();
        context1.setAlgorithm(AlgorithmType.AES);
        context1.setPassword(PASSWORD_16);
        context1.setEncode(EncodeType.BASE64);

        EncryptContext context2 = new EncryptContext();
        context2.setAlgorithm(AlgorithmType.AES);
        context2.setPassword("abcdefghijklmnop");
        context2.setEncode(EncodeType.BASE64);

        IEncryptor encryptor1 = manager.registAndGetEncryptor(context1);
        IEncryptor encryptor2 = manager.registAndGetEncryptor(context2);

        assertNotSame(encryptor1, encryptor2, "不同配置应返回不同实例");
    }

    // ==================== removeEncryptor测试 ====================

    @Test
    @DisplayName("测试removeEncryptor-移除缓存")
    public void testRemoveEncryptor() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        // 先注册
        IEncryptor encryptor1 = manager.registAndGetEncryptor(context);
        assertNotNull(encryptor1, "应成功注册加密器");

        // 移除
        manager.removeEncryptor(context);

        // 再次注册应返回新实例
        IEncryptor encryptor2 = manager.registAndGetEncryptor(context);
        assertNotSame(encryptor1, encryptor2, "移除后应返回新实例");
    }

    @Test
    @DisplayName("测试removeEncryptor-移除不存在的加密器")
    public void testRemoveNonExistentEncryptor() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);

        // 不应抛出异常
        assertDoesNotThrow(() -> manager.removeEncryptor(context), "移除不存在的加密器不应抛出异常");
    }

    // ==================== encrypt测试 ====================

    @Test
    @DisplayName("测试encrypt-正常加密")
    public void testEncrypt() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String encrypted = manager.encrypt("Hello World", context);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.startsWith(Constants.ENCRYPT_HEADER), "加密结果应以ENC_开头");
    }

    @Test
    @DisplayName("测试encrypt-已加密数据不重复加密")
    public void testEncryptAlreadyEncrypted() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String encrypted1 = manager.encrypt("Hello World", context);
        String encrypted2 = manager.encrypt(encrypted1, context);

        assertEquals(encrypted1, encrypted2, "已加密数据不应重复加密");
    }

    // ==================== decrypt测试 ====================

    @Test
    @DisplayName("测试decrypt-正常解密")
    public void testDecrypt() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String original = "Hello World";
        String encrypted = manager.encrypt(original, context);
        String decrypted = manager.decrypt(encrypted, context);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试decrypt-未加密数据直接返回")
    public void testDecryptNotEncrypted() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String plainText = "Plain Text Without Prefix";
        String result = manager.decrypt(plainText, context);

        assertEquals(plainText, result, "未加密数据应直接返回");
    }

    // ==================== 加解密一致性测试 ====================

    @Test
    @DisplayName("测试一致性-多次加解密")
    public void testEncryptDecryptConsistency() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String original = "Test Consistency";

        for (int i = 0; i < 10; i++) {
            String encrypted = manager.encrypt(original, context);
            String decrypted = manager.decrypt(encrypted, context);
            assertEquals(original, decrypted, "第" + i + "次解密后应与原文一致");
        }
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-手机号加密")
    public void testPhoneEncryption() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.AES);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String phone = "13800138000";
        String encrypted = manager.encrypt(phone, context);
        String decrypted = manager.decrypt(encrypted, context);

        assertEquals(phone, decrypted, "手机号解密应正确");
        assertTrue(encrypted.startsWith(Constants.ENCRYPT_HEADER), "加密后应有前缀");
    }

    @Test
    @DisplayName("测试业务场景-RSA非对称加密")
    public void testRsaEncryption() {
        EncryptorManager manager = new EncryptorManager();

        Map<String, String> keyMap = EncryptUtils.generateRsaKey();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.RSA);
        context.setPublicKey(keyMap.get(EncryptUtils.PUBLIC_KEY));
        context.setPrivateKey(keyMap.get(EncryptUtils.PRIVATE_KEY));
        context.setEncode(EncodeType.BASE64);

        String data = "RSA加密数据";
        String encrypted = manager.encrypt(data, context);
        String decrypted = manager.decrypt(encrypted, context);

        assertEquals(data, decrypted, "RSA解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-SM2国密加密")
    public void testSm2Encryption() {
        EncryptorManager manager = new EncryptorManager();

        Map<String, String> keyMap = EncryptUtils.generateSm2Key();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.SM2);
        context.setPublicKey(keyMap.get(EncryptUtils.PUBLIC_KEY));
        context.setPrivateKey(keyMap.get(EncryptUtils.PRIVATE_KEY));
        context.setEncode(EncodeType.BASE64);

        String data = "SM2国密加密数据";
        String encrypted = manager.encrypt(data, context);
        String decrypted = manager.decrypt(encrypted, context);

        assertEquals(data, decrypted, "SM2解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-SM4国密加密")
    public void testSm4Encryption() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.SM4);
        context.setPassword(PASSWORD_16);
        context.setEncode(EncodeType.BASE64);

        String data = "SM4国密加密数据";
        String encrypted = manager.encrypt(data, context);
        String decrypted = manager.decrypt(encrypted, context);

        assertEquals(data, decrypted, "SM4解密应正确");
    }

    @Test
    @DisplayName("测试业务场景-Base64编码")
    public void testBase64Encoding() {
        EncryptorManager manager = new EncryptorManager();

        EncryptContext context = new EncryptContext();
        context.setAlgorithm(AlgorithmType.BASE64);
        context.setEncode(EncodeType.BASE64);

        String data = "Base64编码测试";
        String encrypted = manager.encrypt(data, context);
        String decrypted = manager.decrypt(encrypted, context);

        assertEquals(data, decrypted, "Base64解码应正确");
    }

    // ==================== getFieldCache测试 ====================

    @Test
    @DisplayName("测试getFieldCache-空缓存返回null")
    public void testGetFieldCacheEmpty() {
        EncryptorManager manager = new EncryptorManager();

        assertNull(manager.getFieldCache(String.class), "空缓存应返回null");
    }
}
