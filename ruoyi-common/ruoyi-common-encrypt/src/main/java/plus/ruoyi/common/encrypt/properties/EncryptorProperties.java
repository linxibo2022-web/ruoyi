package plus.ruoyi.common.encrypt.properties;

import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis加解密配置属性类
 * <p>
 * 对应配置文件中的mybatis-encryptor前缀配置项
 * <p>
 * 配置示例：
 * <pre>
 * mybatis-encryptor:
 *   enable: true                    # 是否启用加密功能
 *   algorithm: AES                  # 默认加密算法
 *   password: "1234567890123456"    # 默认密钥（AES/SM4使用）
 *   encode: BASE64                  # 默认编码方式
 *   public-key: "MIIBIj..."        # 默认RSA/SM2公钥
 *   private-key: "MIIEvQ..."       # 默认RSA/SM2私钥
 * </pre>
 *
 * @author 老马
 * @version 4.6.0
 */
@Data
@ConfigurationProperties(prefix = "mybatis-encryptor")
public class EncryptorProperties {

    /**
     * 加密功能总开关
     * <p>
     * true: 启用MyBatis字段加解密
     * false: 禁用加解密功能
     */
    private Boolean enable;

    /**
     * 默认加密算法
     * <p>
     * 当@EncryptField注解中algorithm为DEFAULT时使用此配置
     */
    private AlgorithmType algorithm;

    /**
     * 默认对称加密密钥
     * <p>
     * AES要求：16、24或32位
     * SM4要求：16位
     */
    private String password;

    /**
     * 默认非对称加密公钥
     * <p>
     * RSA/SM2算法使用，用于加密操作
     */
    private String publicKey;

    /**
     * 默认非对称加密私钥
     * <p>
     * RSA/SM2算法使用，用于解密操作
     */
    private String privateKey;

    /**
     * 默认编码方式
     * <p>
     * BASE64: Base64编码（推荐）
     * HEX: 十六进制编码
     */
    private EncodeType encode;

}
