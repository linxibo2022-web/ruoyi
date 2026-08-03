package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-操作结果
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictOperResult {
    /**
     * 成功
     */
    SUCCESS("1", "成功"),

    /**
     * 失败
     */
    FAIL("0", "失败");

    /**
     * 操作结果字典类型
     */
    public static final String DICT_TYPE = "sys_oper_result";

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
    public static DictOperResult getByValue(String value) {
        for (DictOperResult result : values()) {
            if (result.getValue().equals(value)) {
                return result;
            }
        }
        // 默认返回失败
        return FAIL;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictOperResult getByLabel(String label) {
        for (DictOperResult result : values()) {
            if (result.getLabel().equals(label)) {
                return result;
            }
        }
        // 默认返回失败
        return FAIL;
    }
}
