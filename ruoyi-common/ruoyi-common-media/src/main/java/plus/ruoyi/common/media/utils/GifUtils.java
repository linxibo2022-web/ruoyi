package plus.ruoyi.common.media.utils;

import plus.ruoyi.common.media.builder.GifBuilder;
import plus.ruoyi.common.media.options.AnimationOptions;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * GIF工具类
 *
 * @author 抓蛙师
 */
public class GifUtils {

    /**
     * 快速创建GIF
     *
     * @param images 图像帧列表
     * @param delay 每帧延迟时间(毫秒)
     * @return GIF字节数组
     */
    public static byte[] createGif(List<BufferedImage> images, int delay) {
        return new GifBuilder()
            .addFrames(images)
            .delay(delay)
            .build();
    }

    /**
     * 创建指定尺寸的GIF
     *
     * @param images 图像帧列表
     * @param width GIF宽度
     * @param height GIF高度
     * @param delay 每帧延迟时间(毫秒)
     * @return GIF字节数组
     */
    public static byte[] createGif(List<BufferedImage> images, int width, int height, int delay) {
        return new GifBuilder(width, height)
            .addFrames(images)
            .delay(delay)
            .build();
    }

    /**
     * 创建带选项的GIF
     *
     * @param images 图像帧列表
     * @param options 动画选项配置
     * @return GIF字节数组
     */
    public static byte[] createGif(List<BufferedImage> images, AnimationOptions options) {
        GifBuilder builder = new GifBuilder()
            .size(options.getWidth(), options.getHeight())
            .delay(options.getDefaultDelay())
            .loop(options.isLoop())
            .backgroundColor(options.getBackgroundColor())
            .quality(options.getQuality());

        return builder.addFrames(images).build();
    }
}

