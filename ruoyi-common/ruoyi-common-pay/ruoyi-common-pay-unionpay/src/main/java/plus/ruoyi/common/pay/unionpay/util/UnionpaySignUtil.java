package plus.ruoyi.common.pay.unionpay.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import plus.ruoyi.common.pay.exception.PayException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 银联开放平台签名工具类
 *
 * 支持签名方式:
 * 1. RSA签名（推荐）
 * 2. SM2国密签名
 * 3. OAuth2.0认证
 *
 * @author 抓蛙师
 */
@Slf4j
public class UnionpaySignUtil {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_SIGN_ALGORITHM = "SHA256withRSA";
    private static final String SM2_ALGORITHM = "EC";
    private static final String SM2_SIGN_ALGORITHM = "SM3withSM2";

    static {
        // 注册BouncyCastle Provider（支持国密算法）
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * RSA签名
     *
     * @param params    请求参数
     * @param privateKeyStr 私钥字符串（Base64编码）
     * @return 签名结果（Base64）
     */
    public static String signWithRsa(Map<String, Object> params, String privateKeyStr) {
        try {
            // 1. 构建待签名字符串
            String signData = buildSignData(params);
            log.debug("银联RSA签名原文: {}", signData);

            // 2. 解析私钥
            PrivateKey privateKey = parseRsaPrivateKey(privateKeyStr);

            // 3. 使用SHA256withRSA签名
            Signature signature = Signature.getInstance(RSA_SIGN_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(signData.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();

            // 4. Base64编码
            String signResult = Base64.getEncoder().encodeToString(signBytes);
            log.debug("银联RSA签名结果: {}", signResult);

            return signResult;

        } catch (Exception e) {
            log.error("银联RSA签名失败", e);
            throw new PayException("银联RSA签名失败: " + e.getMessage());
        }
    }

    /**
     * RSA验签
     *
     * @param params    响应参数
     * @param sign      签名值
     * @param publicKeyStr 公钥字符串（Base64编码）
     * @return 验签是否成功
     */
    public static boolean verifyWithRsa(Map<String, Object> params, String sign, String publicKeyStr) {
        try {
            // 1. 移除签名字段，构建待验签字符串
            params.remove("sign");
            String signData = buildSignData(params);
            log.debug("银联RSA验签原文: {}", signData);

            // 2. 解析公钥
            PublicKey publicKey = parseRsaPublicKey(publicKeyStr);

            // 3. 验签
            Signature signature = Signature.getInstance(RSA_SIGN_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(signData.getBytes(StandardCharsets.UTF_8));

            byte[] signBytes = Base64.getDecoder().decode(sign);
            boolean result = signature.verify(signBytes);

            log.debug("银联RSA验签结果: {}", result);
            return result;

        } catch (Exception e) {
            log.error("银联RSA验签失败", e);
            return false;
        }
    }

    /**
     * SM2签名（国密）
     *
     * @param params    请求参数
     * @param privateKeyStr 私钥字符串（Base64编码）
     * @return 签名结果（Base64）
     */
    public static String signWithSm2(Map<String, Object> params, String privateKeyStr) {
        try {
            // 1. 构建待签名字符串
            String signData = buildSignData(params);
            log.debug("银联SM2签名原文: {}", signData);

            // 2. 解析私钥
            PrivateKey privateKey = parseSm2PrivateKey(privateKeyStr);

            // 3. 使用SM3withSM2签名
            Signature signature = Signature.getInstance(SM2_SIGN_ALGORITHM, "BC");
            signature.initSign(privateKey);
            signature.update(signData.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();

            // 4. Base64编码
            String signResult = Base64.getEncoder().encodeToString(signBytes);
            log.debug("银联SM2签名结果: {}", signResult);

            return signResult;

        } catch (Exception e) {
            log.error("银联SM2签名失败", e);
            throw new PayException("银联SM2签名失败: " + e.getMessage());
        }
    }

    /**
     * SM2验签（国密）
     *
     * @param params    响应参数
     * @param sign      签名值
     * @param publicKeyStr 公钥字符串（Base64编码）
     * @return 验签是否成功
     */
    public static boolean verifyWithSm2(Map<String, Object> params, String sign, String publicKeyStr) {
        try {
            // 1. 移除签名字段，构建待验签字符串
            params.remove("sign");
            String signData = buildSignData(params);
            log.debug("银联SM2验签原文: {}", signData);

            // 2. 解析公钥
            PublicKey publicKey = parseSm2PublicKey(publicKeyStr);

            // 3. 验签
            Signature signature = Signature.getInstance(SM2_SIGN_ALGORITHM, "BC");
            signature.initVerify(publicKey);
            signature.update(signData.getBytes(StandardCharsets.UTF_8));

            byte[] signBytes = Base64.getDecoder().decode(sign);
            boolean result = signature.verify(signBytes);

            log.debug("银联SM2验签结果: {}", result);
            return result;

        } catch (Exception e) {
            log.error("银联SM2验签失败", e);
            return false;
        }
    }

    /**
     * 构建待签名字符串
     *
     * 规则：
     * 1. 按参数名ASCII码排序
     * 2. 排除空值和签名字段
     * 3. 格式：key1=value1&key2=value2
     *
     * @param params 参数集合
     * @return 待签名字符串
     */
    private static String buildSignData(Map<String, Object> params) {
        // 使用TreeMap自动按key排序
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 跳过空值和签名字段
            if (value == null || "".equals(value) || "sign".equals(key)) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(value);
        }

        return sb.toString();
    }

    /**
     * 解析RSA私钥
     */
    private static PrivateKey parseRsaPrivateKey(String privateKeyStr) throws Exception {
        // 移除PEM格式的头尾
        String privateKeyPem = privateKeyStr
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 解析RSA公钥
     */
    private static PublicKey parseRsaPublicKey(String publicKeyStr) throws Exception {
        // 移除PEM格式的头尾
        String publicKeyPem = publicKeyStr
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 解析SM2私钥
     */
    private static PrivateKey parseSm2PrivateKey(String privateKeyStr) throws Exception {
        String privateKeyPem = privateKeyStr
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(SM2_ALGORITHM, "BC");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 解析SM2公钥
     */
    private static PublicKey parseSm2PublicKey(String publicKeyStr) throws Exception {
        String publicKeyPem = publicKeyStr
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(SM2_ALGORITHM, "BC");
        return keyFactory.generatePublic(keySpec);
    }
}
