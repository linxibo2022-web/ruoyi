package plus.ruoyi.common.excel.core;

import java.util.List;
import java.util.Map;

/**
 * Excel 导入结果接口
 * <p>
 * 定义 Excel 导入操作的标准结果结构，包含表头信息、解析数据、错误信息和分析报告
 * <p>
 * 使用示例：
 *
 * @author Lion Li
 */
public interface ExcelResult<T> {

    /**
     * 获取 Excel 表头信息
     * <p>
     * 表头映射关系，key 为列索引（从0开始），value 为列标题名称
     *
     * @return 表头映射，例如 {0="姓名", 1="年龄", 2="邮箱"}
     */
    Map<Integer,String> getHead();

    /**
     * 获取成功解析的数据对象列表
     * <p>
     * 包含所有通过验证并成功转换的 Excel 行数据
     *
     * @return 数据对象列表
     */
    List<T> getList();

    /**
     * 获取导入过程中的错误信息列表
     * <p>
     * 包含数据验证失败、格式转换错误等各种异常信息
     *
     * @return 错误信息列表
     */
    List<String> getErrorList();

    /**
     * 获取导入结果分析报告
     * <p>
     * 提供导入操作的统计信息，如成功条数、失败条数等摘要信息
     *
     * @return 导入结果描述，例如 "成功导入100条，失败5条"
     */
    String getAnalysis();
}
