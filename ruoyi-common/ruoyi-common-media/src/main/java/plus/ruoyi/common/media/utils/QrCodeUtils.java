package plus.ruoyi.common.media.utils;

import plus.ruoyi.common.media.builder.QrCodeBuilder;
import plus.ruoyi.common.media.options.QrCodeOptions;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 二维码工具类
 *
 * @author 抓蛙师
 */
public class QrCodeUtils {

    /**
     * 生成基本二维码
     *
     * @param content 二维码内容
     * @return 生成的二维码图像
     */
    public static BufferedImage generate(String content) {
        return new QrCodeBuilder(content).build();
    }

    /**
     * 生成指定尺寸的二维码
     *
     * @param content 二维码内容
     * @param size 二维码尺寸
     * @return 生成的二维码图像
     */
    public static BufferedImage generate(String content, int size) {
        return new QrCodeBuilder(content)
            .size(size)
            .build();
    }

    /**
     * 生成带Logo的二维码
     *
     * @param content 二维码内容
     * @param size 二维码尺寸
     * @param logoPath Logo图片路径
     * @return 生成的带Logo二维码图像
     */
    public static BufferedImage generateWithLogo(String content, int size, String logoPath) {
        return new QrCodeBuilder(content)
            .size(size)
            .logo(logoPath)
            .build();
    }

    /**
     * 生成彩色二维码
     *
     * @param content 二维码内容
     * @param size 二维码尺寸
     * @param foreground 前景色
     * @param background 背景色
     * @return 生成的彩色二维码图像
     */
    public static BufferedImage generateColorful(String content, int size,
                                                Color foreground, Color background) {
        return new QrCodeBuilder(content)
            .size(size)
            .colors(foreground, background)
            .build();
    }

    /**
     * 保存二维码到文件
     *
     * @param content 二维码内容
     * @param options 二维码配置选项
     * @param filePath 保存文件路径
     */
    public static void saveQrCode(String content, QrCodeOptions options, String filePath) {
        new QrCodeBuilder(content)
            .size(options.getSize())
            .margin(options.getMargin())
            .colors(options.getForegroundColor(), options.getBackgroundColor())
            .logo(options.getLogoPath(), options.getLogoSizeRatio())
            .save(filePath);
    }
}

