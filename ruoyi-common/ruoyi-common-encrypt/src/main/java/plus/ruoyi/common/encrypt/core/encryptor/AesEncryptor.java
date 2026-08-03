package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;

/**
 * AES对称加密器实现
 *
 * 支持AES-128、AES-192、AES-256三种密钥长度
 * 支持BASE64和HEX两种编码输出
 *
 * 使用示例：
 * <pre>
 * EncryptContext context = new EncryptContext();
 * context.setPassword("1234567890123456");  // 16位密钥
 * context.setEncode(EncodeType.BASE64);
 *
 * AesEncryptor encryptor = new AesEncryptor(context);
 * String encrypted = encryptor.encrypt("hello world", EncodeType.BASE64);
 * String decrypted = encryptor.decrypt(encrypted);
 * </pre>
 *
 * @author 老马
 * @version 4.6.0
 */
public class AesEncryptor extends AbstractEncryptor {

    private final EncryptContext context;

    /**
     * 构造函数
     *
     * @param context 加密上下文，必须包含16/24/32位的password
     */
    public AesEncryptor(EncryptContext context) {
        super(context);
        this.context = context;
    }

    /**
     * 获得当前算法类型
     *
     * @return AES算法类型
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.AES;
    }

    /**
     * AES加密
     *
     * @param value 待加密字符串
     * @param encodeType 加密后的编码格式（BASE64或HEX）
     * @return 加密后的字符串
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (encodeType == EncodeType.HEX) {
            return EncryptUtils.encryptByAesHex(value, context.getPassword());
        } else {
            return EncryptUtils.encryptByAes(value, context.getPassword());
        }
    }

    /**
     * AES解密
     *
     * @param value 待解密字符串（自动识别BASE64或HEX编码）
     * @return 解密后的字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptByAes(value, context.getPassword());
    }
}
