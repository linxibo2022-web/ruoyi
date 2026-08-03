package plus.ruoyi.common.encrypt.core;

import plus.ruoyi.common.encrypt.enumd.AlgorithmType;
import plus.ruoyi.common.encrypt.enumd.EncodeType;

/**
 * 加密器接口
 *
 * 定义了加密器的基本行为，所有具体的加密实现都需要实现此接口
 *
 * 实现类：
 * - Base64Encryptor: Base64编码器
 * - AesEncryptor: AES对称加密器
 * - RsaEncryptor: RSA非对称加密器
 * - Sm2Encryptor: SM2国密加密器
 * - Sm4Encryptor: SM4国密加密器
 *
 * @author 老马
 * @version 4.6.0
 */
public interface IEncryptor {

    /**
     * 获取当前加密器支持的算法类型
     *
     * @return 算法类型枚举
     */
    AlgorithmType algorithm();

    /**
     * 加密方法
     *
     * @param value 待加密的明文字符串
     * @param encodeType 加密后的编码方式（BASE64或HEX）
     * @return 加密后的密文字符串
     */
    String encrypt(String value, EncodeType encodeType);

    /**
     * 解密方法
     *
     * @param value 待解密的密文字符串
     * @return 解密后的明文字符串
     */
    String decrypt(String value);
}
