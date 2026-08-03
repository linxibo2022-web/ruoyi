package plus.ruoyi.common.media.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片质量枚举
 *
 * 该枚举定义了图片压缩的质量等级，包含低质量、中等质量和高质量三个等级，
 * 每个等级对应不同的压缩质量和描述信息。
 */
@Getter
@AllArgsConstructor
public enum ImageQuality {
    /**
     * 低质量图片压缩等级
     * quality: 0.6f - 压缩质量系数
     * description: "低质量" - 等级描述
     */
    LOW(0.6f, "低质量"),

    /**
     * 中等质量图片压缩等级
     * quality: 0.8f - 压缩质量系数
     * description: "中等质量" - 等级描述
     */
    MEDIUM(0.8f, "中等质量"),

    /**
     * 高质量图片压缩等级
     * quality: 0.95f - 压缩质量系数
     * description: "高质量" - 等级描述
     */
    HIGH(0.95f, "高质量");

    /**
     * 压缩质量系数
     */
    private final float quality;

    /**
     * 质量等级描述
     */
    private final String description;
}

