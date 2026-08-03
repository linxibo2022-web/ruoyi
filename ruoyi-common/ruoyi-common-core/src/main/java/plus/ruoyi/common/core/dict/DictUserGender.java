package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-用户性别
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictUserGender {
    /**
     * 女
     */
    FEMALE("0", "女"),

    /**
     * 男
     */
    MALE("1", "男"),

    /**
     * 未知
     */
    UNKNOWN("2", "未知");

    /**
     * 用户性别字典类型
     */
    public static final String DICT_TYPE = "sys_user_gender";

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
    public static DictUserGender getByValue(String value) {
        for (DictUserGender gender : values()) {
            if (gender.getValue().equals(value)) {
                return gender;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictUserGender getByLabel(String label) {
        for (DictUserGender gender : values()) {
            if (gender.getLabel().equals(label)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
