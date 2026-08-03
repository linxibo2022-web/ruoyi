package plus.ruoyi.common.media.options;

import lombok.Data;
import plus.ruoyi.common.media.enums.ResizeMode;

import java.awt.*;

/**
 * 动画选项
 * 用于配置动画处理的各种参数，包括尺寸、延迟、循环等选项
 */
@Data
public class AnimationOptions {
    /**
     * 输出宽度
     */
    private int width = 400;

    /**
     * 输出高度
     */
    private int height = 300;

    /**
     * 默认帧延迟(毫秒)
     */
    private int defaultDelay = 500;

    /**
     * 是否循环播放
     */
    private boolean loop = true;

    /**
     * 背景颜色
     */
    private Color backgroundColor = Color.WHITE;

    /**
     * 缩放模式
     */
    private ResizeMode resizeMode = ResizeMode.FIT;

    /**
     * 图像质量(1-100)
     */
    private int quality = 80;

    /**
     * 创建默认的动画选项实例
     * @return 返回使用默认配置的AnimationOptions实例
     */
    public static AnimationOptions defaults() {
        return new AnimationOptions();
    }

    /**
     * 设置输出尺寸
     * @param width 输出宽度
     * @param height 输出高度
     * @return 返回当前实例，支持链式调用
     */
    public AnimationOptions size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * 设置默认帧延迟
     * @param delay 帧延迟时间(毫秒)
     * @return 返回当前实例，支持链式调用
     */
    public AnimationOptions delay(int delay) {
        this.defaultDelay = delay;
        return this;
    }

    /**
     * 设置是否循环播放
     * @param loop true表示循环播放，false表示不循环
     * @return 返回当前实例，支持链式调用
     */
    public AnimationOptions loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * 设置背景颜色
     * @param color 背景颜色
     * @return 返回当前实例，支持链式调用
     */
    public AnimationOptions backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * 设置缩放模式
     * @param mode 缩放模式枚举值
     * @return 返回当前实例，支持链式调用
     */
    public AnimationOptions resizeMode(ResizeMode mode) {
        this.resizeMode = mode;
        return this;
    }

    /**
     * 设置图像质量
     * 限制质量值在1-100范围内
     * @param quality 图像质量值(1-100)
     * @return 返回当前实例，支持链式调用
     */
    public AnimationOptions quality(int quality) {
        this.quality = Math.max(1, Math.min(100, quality));
        return this;
    }
}

