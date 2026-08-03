package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-通知类型
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictNoticeType {
    /**
     * 通知
     */
    NOTICE("1", "通知"),

    /**
     * 公告
     */
    ANNOUNCEMENT("2", "公告");

    /**
     * 字典值
     */
    private final String value;

    /**
     * 字典标签
     */
    private final String label;

    /**
     * 通知类型字典类型
     */
    public static final String DICT_TYPE = "sys_notice_type";

    /**
     * 根据字典值获取枚举
     */
    public static DictNoticeType getByValue(String value) {
        for (DictNoticeType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        // 默认返回通知
        return NOTICE;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictNoticeType getByLabel(String label) {
        for (DictNoticeType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        // 默认返回通知
        return NOTICE;
    }

    /**
     * 是否为通知
     */
    public static boolean isNotice(String value) {
        return NOTICE.getValue().equals(value);
    }

    /**
     * 是否为公告
     */
    public static boolean isAnnouncement(String value) {
        return ANNOUNCEMENT.getValue().equals(value);
    }
}
