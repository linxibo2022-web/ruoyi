package plus.ruoyi.common.excel.annotation;

import plus.ruoyi.common.excel.core.ExcelOptionsProvider;

import java.lang.annotation.*;

/**
 * Excel 动态下拉选项注解
 *
 * @author 秋辞未寒
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelDynamicOptions {

    /**
     * 提供者类全限定名
     * <p>
     * {@link plus.ruoyi.common.excel.core.ExcelOptionsProvider} 接口实现类 class
     */
    Class<? extends ExcelOptionsProvider> providerClass();
}
