package plus.ruoyi.common.media.model;

import lombok.Data;
import plus.ruoyi.common.media.enums.PosterItemType;

import java.awt.*;

/**
 * 海报元素
 */
@Data
public class PosterItem {
    /**
     * 元素类型
     */
    private PosterItemType type;

    /**
     * 字体颜色
     */
    private Color fontColor;

    /**
     * 字体
     */
    private Font font;

    /**
     * 元素在海报中的X坐标
     */
    private int x;

    /**
     * 元素在海报中的Y坐标
     */
    private int y;

    /**
     * 元素宽度
     */
    private int width;

    /**
     * 元素高度
     */
    private int height;

    /**
     * 是否为圆形元素
     */
    private Boolean isCircle = false;

    /**
     * 元素内容
     */
    private String content;

    /**
     * 构造函数 - 用于创建文本类型的海报元素
     *
     * @param type 元素类型
     * @param font 字体
     * @param fontColor 字体颜色
     * @param x 元素X坐标
     * @param y 元素Y坐标
     * @param content 元素内容
     */
    public PosterItem(PosterItemType type, Font font, Color fontColor, int x, int y, String content) {
        this.type = type;
        this.fontColor = fontColor;
        this.font = font;
        this.x = x;
        this.y = y;
        this.content = content;
    }

    /**
     * 构造函数 - 用于创建图形类型的海报元素
     *
     * @param type 元素类型
     * @param x 元素X坐标
     * @param y 元素Y坐标
     * @param width 元素宽度
     * @param height 元素高度
     * @param isCircle 是否为圆形
     * @param content 元素内容
     */
    public PosterItem(PosterItemType type, int x, int y, int width, int height, Boolean isCircle, String content) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isCircle = isCircle;
        this.content = content;
    }

    /**
     * 构造函数 - 用于创建矩形类型的海报元素
     *
     * @param type 元素类型
     * @param x 元素X坐标
     * @param y 元素Y坐标
     * @param width 元素宽度
     * @param height 元素高度
     * @param content 元素内容
     */
    public PosterItem(PosterItemType type, int x, int y, int width, int height, String content) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.content = content;
    }
}

