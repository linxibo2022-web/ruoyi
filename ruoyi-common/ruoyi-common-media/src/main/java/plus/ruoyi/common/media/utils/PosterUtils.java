package plus.ruoyi.common.media.utils;

import plus.ruoyi.common.media.builder.PosterBuilder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 海报工具类
 *
 * @author 抓蛙师
 */
public class PosterUtils {

    /**
     * 创建简单文字海报
     *
     * @param text 海报上的文字内容
     * @param width 海报宽度
     * @param height 海报高度
     * @return 生成的海报图片
     */
    public static BufferedImage createTextPoster(String text, int width, int height) {
        return new PosterBuilder(width, height)
            .addText(text, 36, "#000000", width / 2 - 100, height / 2)
            .build();
    }

    /**
     * 创建带背景的文字海报
     *
     * @param text 海报上的文字内容
     * @param bgUrl 背景图片URL
     * @param width 海报宽度
     * @param height 海报高度
     * @return 生成的海报图片
     */
    public static BufferedImage createTextPoster(String text, String bgUrl, int width, int height) {
        return new PosterBuilder(bgUrl, width, height)
            .addText(text, 36, "#FFFFFF", width / 2 - 100, height / 2)
            .build();
    }

    /**
     * 创建简单的产品展示海报
     *
     * @param title 产品标题
     * @param imageUrl 产品图片URL
     * @param price 产品价格
     * @param width 海报宽度
     * @param height 海报高度
     * @return 生成的海报图片
     */
    public static BufferedImage createProductPoster(String title, String imageUrl,
                                                   String price, int width, int height) {
        return new PosterBuilder(width, height)
            .background(Color.WHITE)
            .addText(title, "微软雅黑", 32, "#333333", 50, 80)
            .addImage(imageUrl, 50, 120, width - 100, height - 250)
            .addText(price, "微软雅黑", 28, "#FF0000", 50, height - 50)
            .build();
    }
}

