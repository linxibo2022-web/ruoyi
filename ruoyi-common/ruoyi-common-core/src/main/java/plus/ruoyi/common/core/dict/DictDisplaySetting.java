package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-显示设置
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictDisplaySetting {
    /**
     * 显示
     */
    SHOW("1", "显示"),

    /**
     * 隐藏
     */
    HIDE("0", "隐藏");

    /**
     * 显示设置字典类型
     */
    public static final String DICT_TYPE = "sys_display_setting";

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
    public static DictDisplaySetting getByValue(String value) {
        for (DictDisplaySetting setting : values()) {
            if (setting.getValue().equals(value)) {
                return setting;
            }
        }
        // 默认返回隐藏
        return HIDE;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictDisplaySetting getByLabel(String label) {
        for (DictDisplaySetting setting : values()) {
            if (setting.getLabel().equals(label)) {
                return setting;
            }
        }
        // 默认返回隐藏
        return HIDE;
    }

    /**
     * 是否显示
     */
    public static boolean isShow(String value) {
        return SHOW.getValue().equals(value);
    }

    /**
     * 是否隐藏
     */
    public static boolean isHide(String value) {
        return HIDE.getValue().equals(value);
    }
}
