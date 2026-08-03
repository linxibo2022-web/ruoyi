package plus.ruoyi.common.excel.annotation;

import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 必填字段标识注解
 * <p>
 * 用于标识 Excel 导入导出时的必填字段，通过修改字体颜色进行视觉提醒 是否必填 此注解仅用于单表头 不支持多层级表头
 * <p>
 * 使用示例：
 * <pre>
 * @ExcelRequired
 * private String userName;
 * </pre>
 *
 * @author guzhouyanyu
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelRequired {

    /**
     * 必填字段的字体颜色
     * <p>
     * 用于在 Excel 中突出显示必填字段，提供视觉提醒
     *
     * @return 字体颜色，默认为红色
     */
    IndexedColors fontColor() default IndexedColors.RED;
}
