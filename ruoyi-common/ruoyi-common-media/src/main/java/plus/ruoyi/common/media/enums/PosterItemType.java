package plus.ruoyi.common.media.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 海报元素类型枚举
 * <p>
 * 用于定义海报中可包含的元素类型，包括文本、图片和二维码三种类型
 */
@Getter
@AllArgsConstructor
public enum PosterItemType {
    /**
     * 文本类型元素
     */
    TEXT("text", "文本"),

    /**
     * 图片类型元素
     */
    IMAGE("image", "图片"),

    /**
     * 二维码类型元素
     */
    QRCODE("qrcode", "二维码"),

    /**
     * 矩形类型元素
     */
    RECTANGLE("rectangle", "矩形"),

    /**
     * 圆形类型元素
     */
    CIRCLE("circle", "圆形");

    /**
     * 元素类型标识符
     */
    private final String type;

    /**
     * 元素类型名称
     */
    private final String name;

    /**
     * 根据类型标识符获取枚举值
     *
     * @param type 类型标识符
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果类型标识符不存在
     */
    public static PosterItemType fromType(String type) {
        for (PosterItemType itemType : values()) {
            if (itemType.getType().equals(type)) {
                return itemType;
            }
        }
        throw new IllegalArgumentException("未知的海报元素类型: " + type);
    }

    /**
     * 判断是否为几何图形类型
     *
     * @return 如果是几何图形类型则返回true
     */
    public boolean isGeometry() {
        return this == RECTANGLE || this == CIRCLE;
    }

    /**
     * 判断是否为媒体类型（图片、二维码）
     *
     * @return 如果是媒体类型则返回true
     */
    public boolean isMedia() {
        return this == IMAGE || this == QRCODE;
    }

    /**
     * 判断是否为文本类型
     *
     * @return 如果是文本类型则返回true
     */
    public boolean isText() {
        return this == TEXT;
    }
}
