package plus.ruoyi.common.excel.core;

import cn.idev.excel.read.listener.ReadListener;

/**
 * Excel 导入监听器接口
 * <p>
 * 继承自 EasyExcel 的 ReadListener，用于处理 Excel 读取过程中的数据和错误
 * 提供统一的结果获取方法
 * <p>
 *
 * @author Lion Li
 */
public interface ExcelListener<T> extends ReadListener<T> {

    /**
     * 获取 Excel 导入结果
     * <p>
     * 包含成功解析的数据、错误信息和表头等信息
     *
     * @return Excel 导入结果对象
     */
    ExcelResult<T> getExcelResult();

}
