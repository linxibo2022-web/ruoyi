package plus.ruoyi.common.media.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缩放模式枚举
 * <p>
 * 用于定义图片或媒体文件的缩放处理模式，包含等比缩放、填充裁剪和拉伸变形三种模式
 */
@Getter
@AllArgsConstructor
public enum ResizeMode {
    /**
     * 等比缩放，保持宽高比
     * <p>
     * 按照原始宽高比例进行缩放，确保图片不变形，可能会有空白区域
     */
    FIT("fit", "等比缩放"),

    /**
     * 填充模式，裁剪多余部分
     * <p>
     * 将图片填充到指定尺寸，超出部分进行裁剪，保持图片充满显示区域
     */
    FILL("fill", "填充裁剪"),

    /**
     * 拉伸模式，可能变形
     * <p>
     * 强制拉伸到指定尺寸，不保持原始宽高比，可能导致图片变形
     */
    STRETCH("stretch", "拉伸变形");

    /**
     * 缩放模式标识符
     */
    private final String mode;

    /**
     * 缩放模式描述信息
     */
    private final String description;
}

