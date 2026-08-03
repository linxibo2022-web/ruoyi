package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-逻辑标志
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictBooleanFlag {
    /**
     * 是
     */
    YES("1", "是"),

    /**
     * 否
     */
    NO("0", "否");

    /**
     * 逻辑标志字典类型
     */
    public static final String DICT_TYPE = "sys_boolean_flag";

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
    public static DictBooleanFlag getByValue(String value) {
        for (DictBooleanFlag flag : values()) {
            if (flag.getValue().equals(value)) {
                return flag;
            }
        }
        return NO;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictBooleanFlag getByLabel(String label) {
        for (DictBooleanFlag flag : values()) {
            if (flag.getLabel().equals(label)) {
                return flag;
            }
        }
        return NO;
    }

    /**
     * 判断是否为"是"
     */
    public static boolean isYes(String value) {
        return YES.getValue().equals(value);
    }

    /**
     * 判断是否为"否"
     */
    public static boolean isNo(String value) {
        return NO.getValue().equals(value);
    }

}
