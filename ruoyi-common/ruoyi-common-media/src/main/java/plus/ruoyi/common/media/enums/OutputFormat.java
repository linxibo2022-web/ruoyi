package plus.ruoyi.common.media.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 输出格式枚举
 * <p>
 * 该枚举定义了支持的图像输出格式，包括文件扩展名和对应的MIME类型
 */
@Getter
@AllArgsConstructor
public enum OutputFormat {
    /**
     * JPG格式
     */
    JPG("jpg", "image/jpeg"),

    /**
     * JPEG格式
     */
    JPEG("jpeg", "image/jpeg"),

    /**
     * PNG格式
     */
    PNG("png", "image/png"),

    /**
     * GIF格式
     */
    GIF("gif", "image/gif"),

    /**
     * WEBP格式
     */
    WEBP("webp", "image/webp"),

    /**
     * BMP格式
     */
    BMP("bmp", "image/bmp");

    /**
     * 文件扩展名
     */
    private final String extension;

    /**
     * MIME类型
     */
    private final String mimeType;
}

