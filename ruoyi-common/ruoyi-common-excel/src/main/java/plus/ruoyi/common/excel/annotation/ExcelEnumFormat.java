package plus.ruoyi.common.excel.annotation;

import java.lang.annotation.*;

/**
 * 枚举格式化注解
 *
 * <p>用于Excel导入导出时枚举类型的自动转换</p>
 * <p>支持将枚举的某个字段值与Excel单元格内容进行双向转换：</p>
 * <ul>
 *   <li>导出时：将枚举对象的指定字段值转换为Excel中的显示文本</li>
 *   <li>导入时：将Excel中的显示文本转换回对应的枚举值</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 枚举定义
 * public enum UserStatus {
 *     ACTIVE(1, "激活"),
 *     INACTIVE(0, "禁用");
 *
 *     private final Integer value;
 *     private final String label;
 *
 *     // 构造函数和getter方法...
 * }
 *
 * // 实体类字段
 * public class User {
 *     @ExcelProperty("状态")
 *     @ExcelEnumFormat(enumClass = UserStatus.class, valueField = "value", labelField = "label")
 *     private Integer status; // 存储枚举的value值
 * }
 *
 * // 导出时：status=1 -> Excel显示"激活"
 * // 导入时：Excel的"激活" -> status=1
 * }</pre>
 *
 * @author Liang
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelEnumFormat {

    /**
     * 指定枚举类型
     *
     * <p>必须是Enum的子类，用于确定转换时使用的枚举范围</p>
     *
     * @return 枚举类的Class对象
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 枚举值字段名称
     *
     * <p>指定枚举中用作实际存储值的字段名，通常对应数据库中的存储值</p>
     * <p>该字段的值将与实体类属性的值进行匹配</p>
     *
     * @return 枚举中值字段的名称，默认为"value"
     */
    String valueField() default "value";

    /**
     * 枚举标签字段名称
     *
     * <p>指定枚举中用作显示文本的字段名，该值将在Excel中显示</p>
     * <p>导出时显示此字段的值，导入时根据此字段的值查找对应的枚举</p>
     *
     * @return 枚举中标签字段的名称，默认为"label"
     */
    String labelField() default "label";
}
