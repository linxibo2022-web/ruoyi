package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-启用状态
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictEnableStatus {
    /**
     * 启用
     */
    ENABLE("1", "启用"),

    /**
     * 禁用
     */
    DISABLED("0", "禁用");

    /**
     * 启用状态字典类型
     */
    public static final String DICT_TYPE = "sys_enable_status";

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
    public static DictEnableStatus getByValue(String value) {
        for (DictEnableStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        // 默认返回停用状态
        return DISABLED;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictEnableStatus getByLabel(String label) {
        for (DictEnableStatus status : values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        return DISABLED;
    }

    /**
     * 是否为启用状态
     */
    public static boolean isEnabled(String value) {
        return ENABLE.getValue().equals(value);
    }

    /**
     * 是否为停用状态
     */
    public static boolean isDisabled(String value) {
        return DISABLED.getValue().equals(value);
    }

}
