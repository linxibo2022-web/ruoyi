package plus.ruoyi.common.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 单元格批注注解
 * <p>
 * 用于在 Excel 导出时为指定列的单元格添加批注信息 此注解仅用于单表头 不支持多层级表头
 * <p>
 * 使用示例：
 * <pre>
 * @ExcelNotation(value = "请填写真实姓名")
 * private String userName;
 *
 * </pre>
 *
 * @author guzhouyanyu
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelNotation {

    /**
     * 批注显示内容
     * <p>
     * 鼠标悬停在单元格上时显示的提示信息
     *
     * @return 批注文本内容
     */
    String value() default "";
}
