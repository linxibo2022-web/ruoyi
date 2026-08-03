package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-消息类型
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictMessageType {
    /**
     * 系统通知
     */
    SYSTEM("system", "系统通知"),

    /**
     * 活动通知
     */
    ACTIVITY("activity", "活动通知"),

    /**
     * 审核通知
     */
    AUDIT("audit", "审核通知"),

    /**
     * 账户通知
     */
    ACCOUNT("account", "账户通知"),

    /**
     * 私信
     */
    PRIVATE("private", "私信");

    /**
     * 消息类型字典类型
     */
    public static final String DICT_TYPE = "sys_message_type";

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
    public static DictMessageType getByValue(String value) {
        for (DictMessageType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        // 默认返回系统通知
        return SYSTEM;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictMessageType getByLabel(String label) {
        for (DictMessageType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        // 默认返回系统通知
        return SYSTEM;
    }

    /**
     * 是否为系统级通知
     */
    public static boolean isSystemLevel(String value) {
        return SYSTEM.getValue().equals(value) ||
            AUDIT.getValue().equals(value) ||
            ACCOUNT.getValue().equals(value);
    }
}
