package plus.ruoyi.common.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典枚举-业务操作类型
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum DictOperType {
    /**
     * 新增
     */
    INSERT("1", "新增"),

    /**
     * 修改
     */
    UPDATE("2", "修改"),

    /**
     * 删除
     */
    DELETE("3", "删除"),

    /**
     * 授权
     */
    GRANT("4", "授权"),

    /**
     * 导出
     */
    EXPORT("5", "导出"),

    /**
     * 导入
     */
    IMPORT("6", "导入"),

    /**
     * 强退
     */
    FORCE("7", "强退"),

    /**
     * 生成代码
     */
    GENCODE("8", "生成代码"),

    /**
     * 清空数据
     */
    CLEAN("9", "清空数据"),

    /**
     * 其它
     */
    OTHER("99", "其他");

    /**
     * 业务操作字典类型
     */
    public static final String DICT_TYPE = "sys_oper_type";

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
    public static DictOperType getByValue(String value) {
        for (DictOperType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 根据字典标签获取枚举
     */
    public static DictOperType getByLabel(String label) {
        for (DictOperType type : values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        return OTHER;
    }
}
