package plus.ruoyi.common.encrypt.annotation;

import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;

import java.lang.annotation.*;

/**
 * 字段加密注解
 * <p>
 * 使用示例：
 * <pre>
 * public class User {
 *     @EncryptField(algorithm = AlgorithmType.AES, password = "1234567890123456")
 *     private String phone;
 *
 *     @EncryptField(algorithm = AlgorithmType.RSA, publicKey = "...", privateKey = "...")
 *     private String email;
 * }
 * </pre>
 * <p>
 * 支持的加密算法：
 * - BASE64: 简单编码，不需要密钥
 * - AES: 对称加密，需要password（16/24/32位）
 * - RSA: 非对称加密，需要publicKey和privateKey
 * - SM2: 国密非对称加密，需要publicKey和privateKey
 * - SM4: 国密对称加密，需要password（16位）
 *
 * @author 老马
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {
    /**
     * 加密算法类型
     *
     * @return 算法类型，默认使用配置文件中的算法
     */
    AlgorithmType algorithm() default AlgorithmType.DEFAULT;

    /**
     * 对称加密密钥（AES、SM4算法使用）
     * <p>
     * AES要求：16位、24位或32位
     * SM4要求：16位
     *
     * @return 密钥字符串，为空时使用配置文件中的密钥
     */
    String password() default "";

    /**
     * 非对称加密公钥（RSA、SM2算法使用）
     * <p>
     * 用于加密操作
     *
     * @return 公钥字符串，为空时使用配置文件中的公钥
     */
    String publicKey() default "";

    /**
     * 非对称加密私钥（RSA、SM2算法使用）
     * <p>
     * 用于解密操作
     *
     * @return 私钥字符串，为空时使用配置文件中的私钥
     */
    String privateKey() default "";

    /**
     * 加密结果的编码方式
     * <p>
     * 支持BASE64和HEX两种编码
     * 注意：对BASE64算法本身不起作用
     *
     * @return 编码类型，默认使用配置文件中的编码方式
     */
    EncodeType encode() default EncodeType.DEFAULT;

}
