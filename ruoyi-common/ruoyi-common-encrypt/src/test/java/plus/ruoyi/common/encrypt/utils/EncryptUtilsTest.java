package plus.ruoyi.common.encrypt.utils;

import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptUtils 加密工具类测试
 *
 * @author 抓蛙师
 */
@DisplayName("EncryptUtils加密工具类测试")
public class EncryptUtilsTest extends BaseUnitTest {

    // ==================== Base64测试 ====================

    @Test
    @DisplayName("测试Base64加密-普通文本")
    public void testEncryptByBase64() {
        String data = "Hello World";
        String encrypted = EncryptUtils.encryptByBase64(data);

        assertNotNull(encrypted, "加密结果不应为null");
        assertNotEquals(data, encrypted, "加密后应与原文不同");
        assertEquals("SGVsbG8gV29ybGQ=", encrypted, "Base64编码应正确");
    }

    @Test
    @DisplayName("测试Base64加密-中文")
    public void testEncryptByBase64Chinese() {
        String data = "你好世界";
        String encrypted = EncryptUtils.encryptByBase64(data);

        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.contains("你"), "加密后不应包含原文");
    }

    @Test
    @DisplayName("测试Base64解密-普通文本")
    public void testDecryptByBase64() {
        String encrypted = "SGVsbG8gV29ybGQ=";
        String decrypted = EncryptUtils.decryptByBase64(encrypted);

        assertEquals("Hello World", decrypted, "解密结果应与原文一致");
    }

    @Test
    @DisplayName("测试Base64加解密一致性")
    public void testBase64Consistency() {
        String original = "测试Base64加解密一致性";
        String encrypted = EncryptUtils.encryptByBase64(original);
        String decrypted = EncryptUtils.decryptByBase64(encrypted);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试Base64加密-空字符串")
    public void testEncryptByBase64Empty() {
        String encrypted = EncryptUtils.encryptByBase64("");

        assertNotNull(encrypted, "加密结果不应为null");
        assertEquals("", encrypted, "空字符串Base64编码应为空");
    }

    // ==================== AES测试 ====================

    @Test
    @DisplayName("测试AES加密-16位密钥")
    public void testEncryptByAes16Key() {
        String data = "Hello AES";
        String password = "1234567890123456"; // 16位密钥

        String encrypted = EncryptUtils.encryptByAes(data, password);

        assertNotNull(encrypted, "加密结果不应为null");
        assertNotEquals(data, encrypted, "加密后应与原文不同");
    }

    @Test
    @DisplayName("测试AES加密-24位密钥")
    public void testEncryptByAes24Key() {
        String data = "Hello AES";
        String password = "123456789012345678901234"; // 24位密钥

        String encrypted = EncryptUtils.encryptByAes(data, password);

        assertNotNull(encrypted, "加密结果不应为null");
    }

    @Test
    @DisplayName("测试AES加密-32位密钥")
    public void testEncryptByAes32Key() {
        String data = "Hello AES";
        String password = "12345678901234567890123456789012"; // 32位密钥

        String encrypted = EncryptUtils.encryptByAes(data, password);

        assertNotNull(encrypted, "加密结果不应为null");
    }

    @Test
    @DisplayName("测试AES解密")
    public void testDecryptByAes() {
        String original = "Hello AES Decrypt";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptByAes(original, password);
        String decrypted = EncryptUtils.decryptByAes(encrypted, password);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试AES加密-Hex编码")
    public void testEncryptByAesHex() {
        String data = "Hello AES Hex";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptByAesHex(data, password);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.matches("[0-9a-fA-F]+"), "Hex编码应只包含十六进制字符");
    }

    @Test
    @DisplayName("测试AES解密-Hex编码数据")
    public void testDecryptByAesHex() {
        String original = "Hello AES Hex Decrypt";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptByAesHex(original, password);
        String decrypted = EncryptUtils.decryptByAes(encrypted, password);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试AES加密-空密钥应抛出异常")
    public void testEncryptByAesEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptByAes("data", "");
        }, "空密钥应抛出异常");
    }

    @Test
    @DisplayName("测试AES加密-null密钥应抛出异常")
    public void testEncryptByAesNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptByAes("data", null);
        }, "null密钥应抛出异常");
    }

    @Test
    @DisplayName("测试AES加密-密钥长度不合规应抛出异常")
    public void testEncryptByAesInvalidKeyLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptByAes("data", "12345"); // 5位密钥
        }, "密钥长度不合规应抛出异常");
    }

    @Test
    @DisplayName("测试AES解密-空密钥应抛出异常")
    public void testDecryptByAesEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.decryptByAes("encrypted", "");
        }, "空密钥应抛出异常");
    }

    @Test
    @DisplayName("测试AES加解密-中文内容")
    public void testAesChineseContent() {
        String original = "中文测试内容，包含特殊字符：！@#￥%";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptByAes(original, password);
        String decrypted = EncryptUtils.decryptByAes(encrypted, password);

        assertEquals(original, decrypted, "中文解密后应与原文一致");
    }

    // ==================== SM4测试 ====================

    @Test
    @DisplayName("测试SM4加密-Base64编码")
    public void testEncryptBySm4() {
        String data = "Hello SM4";
        String password = "1234567890123456"; // SM4要求16位密钥

        String encrypted = EncryptUtils.encryptBySm4(data, password);

        assertNotNull(encrypted, "加密结果不应为null");
        assertNotEquals(data, encrypted, "加密后应与原文不同");
    }

    @Test
    @DisplayName("测试SM4加密-Hex编码")
    public void testEncryptBySm4Hex() {
        String data = "Hello SM4 Hex";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptBySm4Hex(data, password);

        assertNotNull(encrypted, "加密结果不应为null");
        assertTrue(encrypted.matches("[0-9a-fA-F]+"), "Hex编码应只包含十六进制字符");
    }

    @Test
    @DisplayName("测试SM4解密")
    public void testDecryptBySm4() {
        String original = "Hello SM4 Decrypt";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptBySm4(original, password);
        String decrypted = EncryptUtils.decryptBySm4(encrypted, password);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试SM4解密-Hex编码数据")
    public void testDecryptBySm4Hex() {
        String original = "Hello SM4 Hex Decrypt";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptBySm4Hex(original, password);
        String decrypted = EncryptUtils.decryptBySm4(encrypted, password);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试SM4加密-空密钥应抛出异常")
    public void testEncryptBySm4EmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptBySm4("data", "");
        }, "空密钥应抛出异常");
    }

    @Test
    @DisplayName("测试SM4加密-密钥长度不是16位应抛出异常")
    public void testEncryptBySm4InvalidKeyLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptBySm4("data", "12345678901234567"); // 17位密钥
        }, "非16位密钥应抛出异常");
    }

    // ==================== SM2测试 ====================

    @Test
    @DisplayName("测试SM2密钥对生成")
    public void testGenerateSm2Key() {
        Map<String, String> keyMap = EncryptUtils.generateSm2Key();

        assertNotNull(keyMap, "密钥对Map不应为null");
        assertTrue(keyMap.containsKey(EncryptUtils.PUBLIC_KEY), "应包含公钥");
        assertTrue(keyMap.containsKey(EncryptUtils.PRIVATE_KEY), "应包含私钥");
        assertNotNull(keyMap.get(EncryptUtils.PUBLIC_KEY), "公钥不应为null");
        assertNotNull(keyMap.get(EncryptUtils.PRIVATE_KEY), "私钥不应为null");
        assertFalse(keyMap.get(EncryptUtils.PUBLIC_KEY).isEmpty(), "公钥不应为空");
        assertFalse(keyMap.get(EncryptUtils.PRIVATE_KEY).isEmpty(), "私钥不应为空");
    }

    @Test
    @DisplayName("测试SM2加解密")
    public void testSm2EncryptDecrypt() {
        Map<String, String> keyMap = EncryptUtils.generateSm2Key();
        String publicKey = keyMap.get(EncryptUtils.PUBLIC_KEY);
        String privateKey = keyMap.get(EncryptUtils.PRIVATE_KEY);

        String original = "Hello SM2";
        String encrypted = EncryptUtils.encryptBySm2(original, publicKey);
        String decrypted = EncryptUtils.decryptBySm2(encrypted, privateKey);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试SM2加密-Hex编码")
    public void testEncryptBySm2Hex() {
        Map<String, String> keyMap = EncryptUtils.generateSm2Key();
        String publicKey = keyMap.get(EncryptUtils.PUBLIC_KEY);
        String privateKey = keyMap.get(EncryptUtils.PRIVATE_KEY);

        String original = "Hello SM2 Hex";
        String encrypted = EncryptUtils.encryptBySm2Hex(original, publicKey);
        String decrypted = EncryptUtils.decryptBySm2(encrypted, privateKey);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试SM2加密-空公钥应抛出异常")
    public void testEncryptBySm2EmptyPublicKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptBySm2("data", "");
        }, "空公钥应抛出异常");
    }

    @Test
    @DisplayName("测试SM2解密-空私钥应抛出异常")
    public void testDecryptBySm2EmptyPrivateKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.decryptBySm2("data", "");
        }, "空私钥应抛出异常");
    }

    // ==================== RSA测试 ====================

    @Test
    @DisplayName("测试RSA密钥对生成")
    public void testGenerateRsaKey() {
        Map<String, String> keyMap = EncryptUtils.generateRsaKey();

        assertNotNull(keyMap, "密钥对Map不应为null");
        assertTrue(keyMap.containsKey(EncryptUtils.PUBLIC_KEY), "应包含公钥");
        assertTrue(keyMap.containsKey(EncryptUtils.PRIVATE_KEY), "应包含私钥");
        assertNotNull(keyMap.get(EncryptUtils.PUBLIC_KEY), "公钥不应为null");
        assertNotNull(keyMap.get(EncryptUtils.PRIVATE_KEY), "私钥不应为null");
    }

    @Test
    @DisplayName("测试RSA加解密")
    public void testRsaEncryptDecrypt() {
        Map<String, String> keyMap = EncryptUtils.generateRsaKey();
        String publicKey = keyMap.get(EncryptUtils.PUBLIC_KEY);
        String privateKey = keyMap.get(EncryptUtils.PRIVATE_KEY);

        String original = "Hello RSA";
        String encrypted = EncryptUtils.encryptByRsa(original, publicKey);
        String decrypted = EncryptUtils.decryptByRsa(encrypted, privateKey);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试RSA加密-Hex编码")
    public void testEncryptByRsaHex() {
        Map<String, String> keyMap = EncryptUtils.generateRsaKey();
        String publicKey = keyMap.get(EncryptUtils.PUBLIC_KEY);
        String privateKey = keyMap.get(EncryptUtils.PRIVATE_KEY);

        String original = "Hello RSA Hex";
        String encrypted = EncryptUtils.encryptByRsaHex(original, publicKey);
        String decrypted = EncryptUtils.decryptByRsa(encrypted, privateKey);

        assertEquals(original, decrypted, "解密后应与原文一致");
    }

    @Test
    @DisplayName("测试RSA加密-空公钥应抛出异常")
    public void testEncryptByRsaEmptyPublicKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.encryptByRsa("data", "");
        }, "空公钥应抛出异常");
    }

    @Test
    @DisplayName("测试RSA解密-空私钥应抛出异常")
    public void testDecryptByRsaEmptyPrivateKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtils.decryptByRsa("data", "");
        }, "空私钥应抛出异常");
    }

    // ==================== 摘要算法测试 ====================

    @Test
    @DisplayName("测试MD5加密")
    public void testEncryptByMd5() {
        String data = "Hello MD5";
        String hash = EncryptUtils.encryptByMd5(data);

        assertNotNull(hash, "MD5结果不应为null");
        assertEquals(32, hash.length(), "MD5结果应为32位");
        assertTrue(hash.matches("[0-9a-fA-F]+"), "MD5结果应为十六进制");
    }

    @Test
    @DisplayName("测试MD5加密-相同内容结果一致")
    public void testEncryptByMd5Consistency() {
        String data = "Test MD5 Consistency";
        String hash1 = EncryptUtils.encryptByMd5(data);
        String hash2 = EncryptUtils.encryptByMd5(data);

        assertEquals(hash1, hash2, "相同内容的MD5应一致");
    }

    @Test
    @DisplayName("测试MD5加密-不同内容结果不同")
    public void testEncryptByMd5Different() {
        String hash1 = EncryptUtils.encryptByMd5("data1");
        String hash2 = EncryptUtils.encryptByMd5("data2");

        assertNotEquals(hash1, hash2, "不同内容的MD5应不同");
    }

    @Test
    @DisplayName("测试SHA256加密")
    public void testEncryptBySha256() {
        String data = "Hello SHA256";
        String hash = EncryptUtils.encryptBySha256(data);

        assertNotNull(hash, "SHA256结果不应为null");
        assertEquals(64, hash.length(), "SHA256结果应为64位");
        assertTrue(hash.matches("[0-9a-fA-F]+"), "SHA256结果应为十六进制");
    }

    @Test
    @DisplayName("测试SHA256加密-相同内容结果一致")
    public void testEncryptBySha256Consistency() {
        String data = "Test SHA256 Consistency";
        String hash1 = EncryptUtils.encryptBySha256(data);
        String hash2 = EncryptUtils.encryptBySha256(data);

        assertEquals(hash1, hash2, "相同内容的SHA256应一致");
    }

    @Test
    @DisplayName("测试SM3加密")
    public void testEncryptBySm3() {
        String data = "Hello SM3";
        String hash = EncryptUtils.encryptBySm3(data);

        assertNotNull(hash, "SM3结果不应为null");
        assertEquals(64, hash.length(), "SM3结果应为64位");
        assertTrue(hash.matches("[0-9a-fA-F]+"), "SM3结果应为十六进制");
    }

    @Test
    @DisplayName("测试SM3加密-相同内容结果一致")
    public void testEncryptBySm3Consistency() {
        String data = "Test SM3 Consistency";
        String hash1 = EncryptUtils.encryptBySm3(data);
        String hash2 = EncryptUtils.encryptBySm3(data);

        assertEquals(hash1, hash2, "相同内容的SM3应一致");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-手机号加密")
    public void testPhoneEncryption() {
        String phone = "13800138000";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptByAes(phone, password);
        String decrypted = EncryptUtils.decryptByAes(encrypted, password);

        assertEquals(phone, decrypted, "手机号解密后应与原文一致");
    }

    @Test
    @DisplayName("测试业务场景-身份证号加密")
    public void testIdCardEncryption() {
        String idCard = "110101199003071234";
        String password = "1234567890123456";

        String encrypted = EncryptUtils.encryptByAes(idCard, password);
        String decrypted = EncryptUtils.decryptByAes(encrypted, password);

        assertEquals(idCard, decrypted, "身份证号解密后应与原文一致");
    }

    @Test
    @DisplayName("测试业务场景-密码加密存储")
    public void testPasswordStorage() {
        String password = "MyP@ssw0rd!";
        String hash = EncryptUtils.encryptBySha256(password);

        // 验证密码时，对输入的密码进行同样的Hash
        String inputPassword = "MyP@ssw0rd!";
        String inputHash = EncryptUtils.encryptBySha256(inputPassword);

        assertEquals(hash, inputHash, "相同密码的Hash应一致");
    }

    @Test
    @DisplayName("测试业务场景-API数据签名")
    public void testApiDataSign() {
        String data = "{\"userId\":123,\"amount\":100}";
        String timestamp = "1700000000000";
        String signData = data + timestamp;

        String sign = EncryptUtils.encryptBySha256(signData);

        assertNotNull(sign, "签名不应为null");
        assertEquals(64, sign.length(), "签名长度应为64");
    }

    // ==================== 常量测试 ====================

    @Test
    @DisplayName("测试常量-PUBLIC_KEY")
    public void testConstantPublicKey() {
        assertEquals("publicKey", EncryptUtils.PUBLIC_KEY, "PUBLIC_KEY常量值应为publicKey");
    }

    @Test
    @DisplayName("测试常量-PRIVATE_KEY")
    public void testConstantPrivateKey() {
        assertEquals("privateKey", EncryptUtils.PRIVATE_KEY, "PRIVATE_KEY常量值应为privateKey");
    }
}
