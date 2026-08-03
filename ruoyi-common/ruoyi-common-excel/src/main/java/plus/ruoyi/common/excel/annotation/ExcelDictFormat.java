package plus.ruoyi.common.excel.annotation;

import plus.ruoyi.common.core.utils.StringUtils;

import java.lang.annotation.*;

/**
 * Excel 字典格式化注解
 * <p>
 * 用于 Excel 导入导出时进行字典值转换，支持数据库字典和自定义映射两种方式
 * <p>
 * 使用示例：
 * <pre>
 * // 使用系统字典
 * @ExcelDictFormat(dictType = DictUserGender.DICT_TYPE)
 * private String gender;
 *
 * // 使用自定义映射
 * @ExcelDictFormat(readConverterExp = "0=男,1=女,2=未知")
 * private String gender;
 *
 * // 处理多值字段
 * @ExcelDictFormat(dictType = "sys_role", separator = ",")
 * private String roles;
 * </pre>
 *
 * @author Lion Li
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelDictFormat {

    /**
     * 系统字典类型标识
     * <p>
     * 指定使用系统中已配置的字典类型进行值转换
     * 优先级高于 readConverterExp
     *
     * @return 字典类型，如 "sys_user_gender"，默认为空表示不使用系统字典
     */
    String dictType() default "";

    /**
     * 自定义转换表达式
     * <p>
     * 当不使用系统字典时，可通过此表达式定义键值对映射关系
     * 格式：key1=value1,key2=value2
     * <p>
     * 导入时：Excel中的value转换为key存储到数据库<br>
     * 导出时：数据库中的key转换为value显示在Excel中
     *
     * @return 转换表达式，如 "0=男,1=女,2=未知"，默认为空
     */
    String readConverterExp() default "";

    /**
     * 字段值分隔符
     * <p>
     * 当字段包含多个值时使用的分隔符，主要用于处理如角色、权限等
     * 可能包含多个选项的字段
     *
     * @return 分隔符字符，默认使用系统配置的分隔符
     */
    String separator() default StringUtils.SEPARATOR;

}
