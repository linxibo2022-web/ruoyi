package plus.ruoyi.common.media.options;

import lombok.Data;

/**
 * 滤镜选项
 * <p>
 * 用于配置图像处理时的滤镜效果参数，包括亮度、对比度、饱和度、色调等调整选项。
 * 支持链式调用方式设置各个参数。
 * </p>
 */
@Data
public class FilterOptions {
    /**
     * 亮度调整 (-100 到 100)
     */
    private int brightness = 0;

    /**
     * 对比度调整 (0.5 到 2.0)
     */
    private float contrast = 1.0f;

    /**
     * 饱和度调整 (0.0 到 2.0)
     */
    private float saturation = 1.0f;

    /**
     * 色调调整 (-180 到 180)
     */
    private int hue = 0;

    /**
     * 是否转为灰度
     */
    private boolean grayscale = false;

    /**
     * 模糊半径
     */
    private float blurRadius = 0;

    /**
     * 锐化强度 (0.0 到 2.0)
     */
    private float sharpen = 0;

    /**
     * 创建一个默认配置的滤镜选项实例
     *
     * @return 默认配置的滤镜选项实例
     */
    public static FilterOptions defaults() {
        return new FilterOptions();
    }

    /**
     * 设置亮度值，并限制在 [-100, 100] 范围内
     *
     * @param brightness 亮度调整值，范围 -100 到 100
     * @return 当前滤镜选项实例，支持链式调用
     */
    public FilterOptions brightness(int brightness) {
        this.brightness = Math.max(-100, Math.min(100, brightness));
        return this;
    }

    /**
     * 设置对比度值，并限制在 [0.5, 2.0] 范围内
     *
     * @param contrast 对比度调整值，范围 0.5 到 2.0
     * @return 当前滤镜选项实例，支持链式调用
     */
    public FilterOptions contrast(float contrast) {
        this.contrast = Math.max(0.5f, Math.min(2.0f, contrast));
        return this;
    }

    /**
     * 设置饱和度值，并限制在 [0.0, 2.0] 范围内
     *
     * @param saturation 饱和度调整值，范围 0.0 到 2.0
     * @return 当前滤镜选项实例，支持链式调用
     */
    public FilterOptions saturation(float saturation) {
        this.saturation = Math.max(0.0f, Math.min(2.0f, saturation));
        return this;
    }

    /**
     * 启用灰度模式
     *
     * @return 当前滤镜选项实例，支持链式调用
     */
    public FilterOptions grayscale() {
        this.grayscale = true;
        return this;
    }

    /**
     * 设置模糊半径，不允许负数
     *
     * @param radius 模糊半径，必须大于等于 0
     * @return 当前滤镜选项实例，支持链式调用
     */
    public FilterOptions blur(float radius) {
        this.blurRadius = Math.max(0, radius);
        return this;
    }

    /**
     * 设置锐化强度，并限制在 [0.0, 2.0] 范围内
     *
     * @param strength 锐化强度，范围 0.0 到 2.0
     * @return 当前滤镜选项实例，支持链式调用
     */
    public FilterOptions sharpen(float strength) {
        this.sharpen = Math.max(0, Math.min(2.0f, strength));
        return this;
    }
}
