package plus.ruoyi.common.encrypt.annotation;

import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.test.base.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptField 字段加密注解测试
 *
 * @author 抓蛙师
 */
@DisplayName("EncryptField字段加密注解测试")
public class EncryptFieldTest extends BaseUnitTest {

    // ==================== 注解元信息测试 ====================

    @Test
    @DisplayName("测试注解-Target应为FIELD")
    public void testAnnotationTarget() {
        Target target = EncryptField.class.getAnnotation(Target.class);

        assertNotNull(target, "应有Target注解");
        assertEquals(1, target.value().length, "应只有1个目标");
        assertEquals(ElementType.FIELD, target.value()[0], "目标应为FIELD");
    }

    @Test
    @DisplayName("测试注解-Retention应为RUNTIME")
    public void testAnnotationRetention() {
        Retention retention = EncryptField.class.getAnnotation(Retention.class);

        assertNotNull(retention, "应有Retention注解");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(), "保留策略应为RUNTIME");
    }

    @Test
    @DisplayName("测试注解-应可被继承")
    public void testAnnotationInherited() {
        assertTrue(EncryptField.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
            "应有Inherited注解");
    }

    @Test
    @DisplayName("测试注解-应被文档化")
    public void testAnnotationDocumented() {
        assertTrue(EncryptField.class.isAnnotationPresent(java.lang.annotation.Documented.class),
            "应有Documented注解");
    }

    // ==================== 默认值测试 ====================

    @Test
    @DisplayName("测试默认值-algorithm默认为DEFAULT")
    public void testDefaultAlgorithm() throws Exception {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(AlgorithmType.DEFAULT, annotation.algorithm(), "algorithm默认值应为DEFAULT");
    }

    @Test
    @DisplayName("测试默认值-password默认为空字符串")
    public void testDefaultPassword() throws Exception {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals("", annotation.password(), "password默认值应为空字符串");
    }

    @Test
    @DisplayName("测试默认值-publicKey默认为空字符串")
    public void testDefaultPublicKey() throws Exception {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals("", annotation.publicKey(), "publicKey默认值应为空字符串");
    }

    @Test
    @DisplayName("测试默认值-privateKey默认为空字符串")
    public void testDefaultPrivateKey() throws Exception {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals("", annotation.privateKey(), "privateKey默认值应为空字符串");
    }

    @Test
    @DisplayName("测试默认值-encode默认为DEFAULT")
    public void testDefaultEncode() throws Exception {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(EncodeType.DEFAULT, annotation.encode(), "encode默认值应为DEFAULT");
    }

    // ==================== 自定义值测试 ====================

    @Test
    @DisplayName("测试自定义值-algorithm为AES")
    public void testCustomAlgorithmAes() throws Exception {
        Field field = TestEntity.class.getDeclaredField("aesField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(AlgorithmType.AES, annotation.algorithm(), "algorithm应为AES");
        assertEquals("1234567890123456", annotation.password(), "password应为设置的值");
    }

    @Test
    @DisplayName("测试自定义值-algorithm为RSA")
    public void testCustomAlgorithmRsa() throws Exception {
        Field field = TestEntity.class.getDeclaredField("rsaField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(AlgorithmType.RSA, annotation.algorithm(), "algorithm应为RSA");
        assertEquals("testPublicKey", annotation.publicKey(), "publicKey应为设置的值");
        assertEquals("testPrivateKey", annotation.privateKey(), "privateKey应为设置的值");
    }

    @Test
    @DisplayName("测试自定义值-algorithm为SM2")
    public void testCustomAlgorithmSm2() throws Exception {
        Field field = TestEntity.class.getDeclaredField("sm2Field");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(AlgorithmType.SM2, annotation.algorithm(), "algorithm应为SM2");
    }

    @Test
    @DisplayName("测试自定义值-algorithm为SM4")
    public void testCustomAlgorithmSm4() throws Exception {
        Field field = TestEntity.class.getDeclaredField("sm4Field");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(AlgorithmType.SM4, annotation.algorithm(), "algorithm应为SM4");
        assertEquals("1234567890123456", annotation.password(), "SM4 password应为16位");
    }

    @Test
    @DisplayName("测试自定义值-algorithm为BASE64")
    public void testCustomAlgorithmBase64() throws Exception {
        Field field = TestEntity.class.getDeclaredField("base64Field");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(AlgorithmType.BASE64, annotation.algorithm(), "algorithm应为BASE64");
    }

    @Test
    @DisplayName("测试自定义值-encode为HEX")
    public void testCustomEncodeHex() throws Exception {
        Field field = TestEntity.class.getDeclaredField("hexEncodeField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(EncodeType.HEX, annotation.encode(), "encode应为HEX");
    }

    @Test
    @DisplayName("测试自定义值-encode为BASE64")
    public void testCustomEncodeBase64() throws Exception {
        Field field = TestEntity.class.getDeclaredField("base64EncodeField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertEquals(EncodeType.BASE64, annotation.encode(), "encode应为BASE64");
    }

    // ==================== 反射获取测试 ====================

    @Test
    @DisplayName("测试反射-获取所有带注解字段")
    public void testGetAllAnnotatedFields() {
        Field[] fields = TestEntity.class.getDeclaredFields();
        int annotatedCount = 0;

        for (Field field : fields) {
            if (field.isAnnotationPresent(EncryptField.class)) {
                annotatedCount++;
            }
        }

        assertEquals(9, annotatedCount, "应有9个带EncryptField注解的字段");
    }

    @Test
    @DisplayName("测试反射-无注解字段")
    public void testFieldWithoutAnnotation() throws Exception {
        Field field = TestEntity.class.getDeclaredField("normalField");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertNull(annotation, "无注解字段应返回null");
    }

    // ==================== 业务场景测试 ====================

    @Test
    @DisplayName("测试业务场景-手机号加密字段")
    public void testPhoneEncryptField() throws Exception {
        Field field = UserEntity.class.getDeclaredField("phone");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertNotNull(annotation, "phone应有EncryptField注解");
        assertEquals(AlgorithmType.AES, annotation.algorithm(), "phone应使用AES加密");
    }

    @Test
    @DisplayName("测试业务场景-身份证号加密字段")
    public void testIdCardEncryptField() throws Exception {
        Field field = UserEntity.class.getDeclaredField("idCard");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertNotNull(annotation, "idCard应有EncryptField注解");
        assertEquals(AlgorithmType.SM4, annotation.algorithm(), "idCard应使用SM4加密");
    }

    @Test
    @DisplayName("测试业务场景-银行卡号加密字段")
    public void testBankCardEncryptField() throws Exception {
        Field field = UserEntity.class.getDeclaredField("bankCard");
        EncryptField annotation = field.getAnnotation(EncryptField.class);

        assertNotNull(annotation, "bankCard应有EncryptField注解");
        assertEquals(AlgorithmType.RSA, annotation.algorithm(), "bankCard应使用RSA加密");
    }

    // ==================== 测试用类 ====================

    /**
     * 测试用实体类 - 包含各种EncryptField配置
     */
    private static class TestEntity {
        @EncryptField
        private String defaultField;

        @EncryptField(algorithm = AlgorithmType.AES, password = "1234567890123456")
        private String aesField;

        @EncryptField(algorithm = AlgorithmType.RSA, publicKey = "testPublicKey", privateKey = "testPrivateKey")
        private String rsaField;

        @EncryptField(algorithm = AlgorithmType.SM2, publicKey = "sm2PublicKey", privateKey = "sm2PrivateKey")
        private String sm2Field;

        @EncryptField(algorithm = AlgorithmType.SM4, password = "1234567890123456")
        private String sm4Field;

        @EncryptField(algorithm = AlgorithmType.BASE64)
        private String base64Field;

        @EncryptField(algorithm = AlgorithmType.AES, password = "1234567890123456", encode = EncodeType.HEX)
        private String hexEncodeField;

        @EncryptField(algorithm = AlgorithmType.AES, password = "1234567890123456", encode = EncodeType.BASE64)
        private String base64EncodeField;

        @EncryptField(algorithm = AlgorithmType.AES, password = "1234567890123456", encode = EncodeType.DEFAULT)
        private String defaultEncodeField;

        private String normalField;
    }

    /**
     * 用户实体类 - 业务场景测试
     */
    private static class UserEntity {
        private Long id;

        private String userName;

        @EncryptField(algorithm = AlgorithmType.AES, password = "1234567890123456")
        private String phone;

        @EncryptField(algorithm = AlgorithmType.SM4, password = "1234567890123456")
        private String idCard;

        @EncryptField(algorithm = AlgorithmType.RSA, publicKey = "rsaPublicKey", privateKey = "rsaPrivateKey")
        private String bankCard;

        @EncryptField(algorithm = AlgorithmType.BASE64)
        private String email;
    }
}
