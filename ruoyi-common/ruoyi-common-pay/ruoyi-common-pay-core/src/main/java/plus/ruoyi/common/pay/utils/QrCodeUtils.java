package plus.ruoyi.common.pay.utils;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.media.builder.QrCodeBuilder;

import java.util.Base64;

/**
 * 二维码工具类
 *
 * 统一处理二维码生成逻辑,避免重复代码
 *
 * @author 抓蛙师
 */
@Slf4j
public class QrCodeUtils {

    /**
     * 默认二维码尺寸
     */
    public static final int DEFAULT_QR_CODE_SIZE = 300;

    /**
     * 生成二维码Base64字符串
     *
     * @param content 二维码内容(如URL)
     * @return Base64格式的二维码图片 (data:image/png;base64,xxx), 失败时返回null
     */
    public static String generateBase64(String content) {
        return generateBase64(content, DEFAULT_QR_CODE_SIZE);
    }

    /**
     * 生成二维码Base64字符串
     *
     * @param content 二维码内容(如URL)
     * @param size    二维码尺寸(宽高相同)
     * @return Base64格式的二维码图片 (data:image/png;base64,xxx), 失败时返回null
     */
    public static String generateBase64(String content, int size) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        try {
            byte[] qrCodeBytes = new QrCodeBuilder(content)
                .size(size)
                .toBytes();

            String base64Data = Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + base64Data;

        } catch (Exception e) {
            log.error("生成二维码Base64失败: content={}, size={}, error={}",
                content, size, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成二维码字节数组
     *
     * @param content 二维码内容
     * @return 二维码图片字节数组, 失败时返回null
     */
    public static byte[] generateBytes(String content) {
        return generateBytes(content, DEFAULT_QR_CODE_SIZE);
    }

    /**
     * 生成二维码字节数组
     *
     * @param content 二维码内容
     * @param size    二维码尺寸
     * @return 二维码图片字节数组, 失败时返回null
     */
    public static byte[] generateBytes(String content, int size) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        try {
            return new QrCodeBuilder(content)
                .size(size)
                .toBytes();
        } catch (Exception e) {
            log.error("生成二维码字节数组失败: content={}, size={}, error={}",
                content, size, e.getMessage(), e);
            return null;
        }
    }
}
