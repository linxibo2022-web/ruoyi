package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-文件类型
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictFileType {
    /**
     * 图片
     */
    IMAGE("image", "图片"),

    /**
     * 文档
     */
    DOCUMENT("document", "文档"),

    /**
     * 视频
     */
    VIDEO("video", "视频"),

    /**
     * 音频
     */
    AUDIO("audio", "音频"),

    /**
     * 压缩包
     */
    ARCHIVE("archive", "压缩包"),

    /**
     * 其他
     */
    OTHER("other", "其他");

    /**
     * 文件类型字典类型
     */
    public static final String DICT_TYPE = "sys_file_type";

    /**
     * 字典值
     */
    private final String value;

    /**
     * 字典标签
     */
    private final String label;

    /**
     * 根据字典值获取枚举
     */
    public static DictFileType getByValue(String value) {
        for (DictFileType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        // 默认返回其他
        return OTHER;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictFileType getByLabel(String label) {
        for (DictFileType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 是否为媒体文件
     */
    public static boolean isMediaFile(String value) {
        return IMAGE.getValue().equals(value) ||
            VIDEO.getValue().equals(value) ||
            AUDIO.getValue().equals(value);
    }
}
