package plus.ruoyi.common.openapi.utils;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 开放API签名工具类
 *
 * @author 抓蛙师
 */
public class OpenApiSignUtils {

    /**
     * 生成签名
     * 签名算法: MD5(appKey + timestamp + appSecret)
     *
     * @param appKey    AppKey
     * @param timestamp 时间戳
     * @param appSecret AppSecret
     * @return 签名
     */
    public static String generateSign(String appKey, String timestamp, String appSecret) {
        String content = appKey + timestamp + appSecret;
        return DigestUtil.md5Hex(content);
    }

    /**
     * 验证签名
     *
     * @param appKey    AppKey
     * @param timestamp 时间戳
     * @param appSecret AppSecret
     * @param sign      待验证的签名
     * @return 是否验证通过
     */
    public static boolean verifySign(String appKey, String timestamp, String appSecret, String sign) {
        String correctSign = generateSign(appKey, timestamp, appSecret);
        return correctSign.equals(sign);
    }

    /**
     * 验证时间戳是否在有效期内
     *
     * @param timestamp   时间戳(毫秒)
     * @param expireSeconds 有效期(秒)
     * @return 是否有效
     */
    public static boolean verifyTimestamp(Long timestamp, long expireSeconds) {
        if (timestamp == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long diff = Math.abs(now - timestamp);
        return diff <= expireSeconds * 1000;
    }

}
