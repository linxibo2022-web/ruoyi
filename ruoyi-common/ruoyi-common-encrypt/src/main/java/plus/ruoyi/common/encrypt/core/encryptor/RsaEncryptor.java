package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;


/**
 * RSA非对称加密器实现
 *
 * 使用公钥加密，私钥解密的方式
 * 支持BASE64和HEX两种编码输出
 *
 * 特点：
 * - 安全性高，密钥分离
 * - 加密速度较慢，适合小数据量
 * - 密钥长度通常为1024、2048或4096位
 *
 * @author 老马
 * @version 4.6.0
 */
public class RsaEncryptor extends AbstractEncryptor {

    private final EncryptContext context;

    /**
     * 构造函数
     *
     * @param context 加密上下文，必须包含publicKey和privateKey
     * @throws IllegalArgumentException 当公钥或私钥为空时抛出
     */
    public RsaEncryptor(EncryptContext context) {
        super(context);
        String privateKey = context.getPrivateKey();
        String publicKey = context.getPublicKey();
        if (StringUtils.isAnyEmpty(privateKey, publicKey)) {
            throw new IllegalArgumentException("RSA公私钥均需要提供，公钥加密，私钥解密。");
        }
        this.context = context;
    }

    /**
     * 获得当前算法类型
     *
     * @return RSA算法类型
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.RSA;
    }

    /**
     * RSA公钥加密
     *
     * @param value 待加密字符串
     * @param encodeType 加密后的编码格式（BASE64或HEX）
     * @return 加密后的字符串
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (encodeType == EncodeType.HEX) {
            return EncryptUtils.encryptByRsaHex(value, context.getPublicKey());
        } else {
            return EncryptUtils.encryptByRsa(value, context.getPublicKey());
        }
    }

    /**
     * RSA私钥解密
     *
     * @param value 待解密字符串（自动识别BASE64或HEX编码）
     * @return 解密后的字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtils.decryptByRsa(value, context.getPrivateKey());
    }
}
