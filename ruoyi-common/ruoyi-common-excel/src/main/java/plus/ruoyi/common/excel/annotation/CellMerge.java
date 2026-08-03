package plus.ruoyi.common.excel.annotation;

import plus.ruoyi.common.excel.core.CellMergeStrategy;

import java.lang.annotation.*;

/**
 * Excel 列单元格合并注解
 * <p>
 * 用于标记需要合并相同值的单元格列，当相邻行的指定列值相同时自动合并单元格
 * <p>
 * 使用示例：
 * <pre>
 * public class ExportData {
 *     @CellMerge()
 *     private String department;
 *
 *     @CellMerge(index = 1, mergeBy = {"department"})
 *     private String team;
 * }
 * </pre>
 *
 * 注意：需要配合 {@link CellMergeStrategy} 策略使用
 *
 * @author Lion Li
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CellMerge {

    /**
     * 列索引位置
     * <p>
     * 指定当前字段在 Excel 中对应的列位置（从0开始）
     *
     * @return 列索引，默认 -1 表示自动推断
     */
    int index() default -1;

    /**
     * 合并依赖的字段名称数组
     * <p>
     * 指定合并当前列时需要同时考虑的其他字段，只有当这些依赖字段的值
     * 在相邻行中也相同时，当前列才会进行合并
     * <p>
     * 例如：team 字段依赖 department 字段，只有 department 相同的行
     * 中 team 才会合并
     *
     * @return 依赖字段名称数组，默认为空数组表示无依赖
     */
    String[] mergeBy() default {};

}
