package plus.ruoyi.common.media.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 字体工具类
 * 提供字体创建和文本尺寸计算等常用字体操作方法
 */
public class FontUtils {

    /**
     * 创建字体
     * @param fontName 字体名称
     * @param style 字体样式，如Font.PLAIN、Font.BOLD、Font.ITALIC等
     * @param size 字体大小
     * @return 创建的字体对象
     */
    public static Font createFont(String fontName, int style, int size) {
        return new Font(fontName, style, size);
    }

    /**
     * 创建默认字体
     * 使用微软雅黑字体，普通样式
     * @param size 字体大小
     * @return 创建的默认字体对象
     */
    public static Font createDefaultFont(int size) {
        return createFont("微软雅黑", Font.PLAIN, size);
    }

    /**
     * 创建粗体字体
     * @param fontName 字体名称
     * @param size 字体大小
     * @return 创建的粗体字体对象
     */
    public static Font createBoldFont(String fontName, int size) {
        return createFont(fontName, Font.BOLD, size);
    }

    /**
     * 获取文本尺寸
     * 通过创建临时图像上下文来计算指定字体下文本的宽度和高度
     * @param text 要测量的文本
     * @param font 用于测量的字体
     * @return 包含文本宽度和高度的Dimension对象
     */
    public static Dimension getTextSize(String text, Font font) {
        // 创建临时图像用于获取Graphics2D上下文
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = tempImage.createGraphics();
        FontMetrics fm = g2d.getFontMetrics(font);
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.dispose();
        return new Dimension(width, height);
    }
}

