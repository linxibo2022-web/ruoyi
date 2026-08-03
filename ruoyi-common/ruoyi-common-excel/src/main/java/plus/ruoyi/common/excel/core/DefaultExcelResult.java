package plus.ruoyi.common.excel.core;

import cn.hutool.core.util.StrUtil;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入结果默认实现类
 * <p>
 * 用于封装 Excel 导入操作的结果，包含表头信息、成功解析的数据和错误信息
 * <p>
 * 使用示例：
 * <pre>
 * DefaultExcelResult<User>< result = new DefaultExcelResult<User>();
 * result.setList(userList);
 * result.setErrorList(errorList);
 * String analysis = result.getAnalysis(); // 获取导入统计信息
 * </pre>
 *
 * @param <T> 数据对象类型
 * @author Yjoioooo
 * @author Lion Li
 */
public class DefaultExcelResult<T> implements ExcelResult<T> {

    /**
     * Excel 表头信息
     * <p>
     * key: 列索引，value: 列标题
     */
    @Setter
    private Map<Integer,String> head;

    /**
     * 成功解析的数据对象列表
     */
    @Setter
    private List<T> list;

    /**
     * 导入过程中的错误信息列表
     */
    @Setter
    private List<String> errorList;

    /**
     * 默认构造方法
     * <p>
     * 初始化空的表头、数据列表和错误列表
     */
    public DefaultExcelResult() {
        this.head = new HashMap<>();
        this.list = new ArrayList<>();
        this.errorList = new ArrayList<>();
    }

    /**
     * 带参数的构造方法
     *
     * @param list 数据对象列表
     * @param errorList 错误信息列表
     */
    public DefaultExcelResult(List<T> list, List<String> errorList) {
        this.head = new HashMap<>();
        this.list = list;
        this.errorList = errorList;
    }

    /**
     * 复制构造方法
     *
     * @param excelResult 源 ExcelResult 对象
     */
    public DefaultExcelResult(ExcelResult<T> excelResult) {
        this.head = excelResult.getHead();
        this.list = excelResult.getList();
        this.errorList = excelResult.getErrorList();
    }

    /**
     * 获取表头信息
     *
     * @return 表头映射，key为列索引，value为列标题
     */
    @Override
    public Map<Integer, String> getHead() {
        return head;
    }

    /**
     * 获取成功解析的数据列表
     *
     * @return 数据对象列表
     */
    @Override
    public List<T> getList() {
        return list;
    }

    /**
     * 获取错误信息列表
     *
     * @return 错误信息列表
     */
    @Override
    public List<String> getErrorList() {
        return errorList;
    }

    /**
     * 获取导入结果分析报告
     * <p>
     * 根据成功和失败的数量生成相应的提示信息
     *
     * @return 导入结果描述信息
     */
    @Override
    public String getAnalysis() {
        int successCount = list.size();
        int errorCount = errorList.size();
        if (successCount == 0) {
            return "读取失败，未解析到数据";
        } else {
            if (errorCount == 0) {
                return StrUtil.format("恭喜您，全部读取成功！共{}条", successCount);
            } else {
                return StrUtil.format("读取完成！成功{}条，失败{}条", successCount, errorCount);
            }
        }
    }
}
