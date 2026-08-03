package plus.ruoyi.common.media.options;

import lombok.Data;
import plus.ruoyi.common.media.utils.ColorUtils;

import java.awt.*;

/**
 * 水印选项
 * <p>
 * 用于配置水印的位置、透明度、大小等参数，支持文本水印和图片水印两种类型。
 * </p>
 */
@Data
public class WatermarkOptions {
    /**
     * 水印位置枚举
     * <p>
     * 定义了九种常见的水印位置：左上、中上、右上、左中、居中、右中、左下、中下、右下。
     * </p>
     */
    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    /**
     * 水印位置，默认为右下角
     */
    private Position position = Position.BOTTOM_RIGHT;

    /**
     * 水平偏移量（像素），默认为10
     */
    private int offsetX = 10;

    /**
     * 垂直偏移量（像素），默认为10
     */
    private int offsetY = 10;

    /**
     * 水印透明度，取值范围 0.1 ~ 1.0，默认为 0.6
     */
    private float opacity = 0.6f;

    /**
     * 是否自动调整水印尺寸，默认为 true
     */
    private boolean autoSize = true;

    /**
     * 固定宽度（像素），仅在 autoSize 为 false 时生效，默认为 100
     */
    private int fixedWidth = 100;

    /**
     * 固定高度（像素），仅在 autoSize 为 false 时生效，默认为 50
     */
    private int fixedHeight = 50;

    // 文本水印相关属性

    /**
     * 文本水印内容
     */
    private String text;

    /**
     * 文本水印字体，默认为微软雅黑，大小24
     */
    private Font font;

    /**
     * 文本颜色，默认为灰色
     */
    private Color textColor = Color.GRAY;

    // 图片水印相关属性

    /**
     * 图片水印路径
     */
    private String imagePath;

    /**
     * 创建一个文本水印配置实例
     *
     * @param text 要添加的水印文本内容
     * @return 返回初始化后的 WatermarkOptions 实例
     */
    public static WatermarkOptions text(String text) {
        WatermarkOptions options = new WatermarkOptions();
        options.text = text;
        options.font = new Font("微软雅黑", Font.PLAIN, 24);
        return options;
    }

    /**
     * 创建一个图片水印配置实例
     *
     * @param imagePath 图片水印文件路径
     * @return 返回初始化后的 WatermarkOptions 实例
     */
    public static WatermarkOptions image(String imagePath) {
        WatermarkOptions options = new WatermarkOptions();
        options.imagePath = imagePath;
        return options;
    }

    /**
     * 设置水印位置
     *
     * @param position 水印位置枚举值
     * @return 返回当前 WatermarkOptions 实例以支持链式调用
     */
    public WatermarkOptions position(Position position) {
        this.position = position;
        return this;
    }

    /**
     * 设置水印偏移量
     *
     * @param x 水平方向偏移量（像素）
     * @param y 垂直方向偏移量（像素）
     * @return 返回当前 WatermarkOptions 实例以支持链式调用
     */
    public WatermarkOptions offset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
        return this;
    }

    /**
     * 设置水印透明度，会将值限制在 0.1 到 1.0 之间
     *
     * @param opacity 透明度值
     * @return 返回当前 WatermarkOptions 实例以支持链式调用
     */
    public WatermarkOptions opacity(float opacity) {
        this.opacity = Math.max(0.1f, Math.min(1.0f, opacity));
        return this;
    }

    /**
     * 设置文本水印字体
     *
     * @param font 字体对象
     * @return 返回当前 WatermarkOptions 实例以支持链式调用
     */
    public WatermarkOptions font(Font font) {
        this.font = font;
        return this;
    }

    /**
     * 设置文本水印颜色
     *
     * @param colorStr 颜色字符串，例如 "#FF0000" 或 "red"
     * @return 返回当前 WatermarkOptions 实例以支持链式调用
     */
    public WatermarkOptions textColor(String colorStr) {
        this.textColor = ColorUtils.parseColor(colorStr);
        return this;
    }

    /**
     * 设置水印固定尺寸
     *
     * @param width  水印宽度（像素）
     * @param height 水印高度（像素）
     * @return 返回当前 WatermarkOptions 实例以支持链式调用
     */
    public WatermarkOptions size(int width, int height) {
        this.autoSize = false;
        this.fixedWidth = width;
        this.fixedHeight = height;
        return this;
    }
}
