package plus.ruoyi.common.excel.core;

import java.util.Set;

/**
 * Excel 动态下拉选项提供者接口
 * <p>
 * 实现此接口并注册为 Spring Bean，用于为 Excel 导出提供动态下拉选项
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1. 实现 ExcelOptionsProvider 接口
 * @Component
 * public class DeptOptionsProvider implements ExcelOptionsProvider {
 *     @Resource
 *     private ISysDeptService deptService;
 *
 *     @Override
 *     public Set<String> getOptions() {
 *         return deptService.list().stream()
 *             .map(SysDept::getDeptName)
 *             .collect(Collectors.toSet());
 *     }
 * }
 *
 * // 2. 在 VO 类中使用 @ExcelDynamicOptions 注解
 * public class UserVo {
 *     @ExcelProperty("部门")
 *     @ExcelDynamicOptions(providerClass = DeptOptionsProvider.class)
 *     private String deptName;
 * }
 * }</pre>
 *
 * @author 秋辞未寒
 */
public interface ExcelOptionsProvider {

    /**
     * 获取下拉选项
     *
     * @return 下拉选项列表
     */
    Set<String> getOptions();

}
