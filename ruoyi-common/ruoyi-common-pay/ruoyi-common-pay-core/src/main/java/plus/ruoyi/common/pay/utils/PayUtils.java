package plus.ruoyi.common.pay.utils;

import cn.hutool.core.util.RandomUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 支付工具类
 *
 * @author 抓蛙师
 */
public class PayUtils {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成商户订单号
     *
     * @return 商户订单号
     */
    public static String generateOutTradeNo() {
        return generateOutTradeNo("PAY");
    }

    /**
     * 生成商户订单号
     *
     * @param prefix 前缀
     * @return 商户订单号
     */
    public static String generateOutTradeNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String randomStr = String.format("%06d", RANDOM.nextInt(1000000));
        return prefix + timestamp + randomStr;
    }

    /**
     * 生成商户退款单号
     *
     * @return 商户退款单号
     */
    public static String generateOutRefundNo() {
        return generateOutRefundNo("REF");
    }

    /**
     * 生成商户退款单号
     *
     * @param prefix 前缀
     * @return 商户退款单号
     */
    public static String generateOutRefundNo(String prefix) {
        return generateOutTradeNo(prefix);
    }

    /**
     * 元转分
     *
     * @param yuan 元(BigDecimal)
     * @return 分(int)
     */
    public static int yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.multiply(new BigDecimal("100"))
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
    }

    /**
     * 元转分(字符串)
     */
    public static String yuanToFen(String yuan) {
        if (yuan == null || yuan.trim().isEmpty()) {
            return "0";
        }
        return String.valueOf(yuanToFen(new BigDecimal(yuan)));
    }

    /**
     * 分转元
     *
     * @param fen 分(int)
     * @return 元(BigDecimal)
     */
    public static BigDecimal fenToYuan(int fen) {
        return new BigDecimal(fen)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * 分转元(字符串)
     */
    public static String fenToYuan(String fen) {
        if (fen == null || fen.trim().isEmpty()) {
            return "0.00";
        }
        return fenToYuan(Integer.parseInt(fen)).toString();
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String generateNonceStr(int length) {
        return RandomUtil.randomString(length);
    }

    /**
     * 生成随机字符串(默认32位)
     */
    public static String generateNonceStr() {
        return generateNonceStr(32);
    }

    /**
     * 生成时间戳
     *
     * @return 时间戳(秒)
     */
    public static long generateTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 生成订单过期时间
     *
     * @param minutes 有效分钟数
     * @return 格式化时间字符串(yyyyMMddHHmmss)
     */
    public static String generateTimeExpire(int minutes) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(minutes);
        return expireTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 生成订单开始时间
     *
     * @return 格式化时间字符串(yyyyMMddHHmmss)
     */
    public static String generateTimeStart() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 字符串转BigDecimal
     *
     * @param value 字符串金额
     * @return BigDecimal金额
     */
    public static BigDecimal stringToDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的金额格式: " + value, e);
        }
    }
}
