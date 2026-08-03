package plus.ruoyi.common.excel.core;

import cn.hutool.core.collection.CollUtil;
import cn.idev.excel.metadata.Head;
import cn.idev.excel.write.handler.WorkbookWriteHandler;
import cn.idev.excel.write.handler.context.WorkbookWriteHandlerContext;
import cn.idev.excel.write.merge.AbstractMergeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

/**
 * Excel 列值重复合并策略
 * <p>
 * 用于合并相邻行中相同值的单元格，支持自定义合并区域或通过数据自动计算合并区域
 * 继承自 EasyExcel 的 AbstractMergeStrategy，同时实现 WorkbookWriteHandler 接口
 * 提供灵活的单元格合并功能
 * </p>
 *
 * @author Lion Li
 */
@Slf4j
public class CellMergeStrategy extends AbstractMergeStrategy implements WorkbookWriteHandler {

    /**
     * 合并单元格区域列表
     * 存储所有需要合并的单元格区域信息
     */
    private final List<CellRangeAddress> cellList;

    /**
     * 构造方法 - 直接指定合并区域
     * <p>
     * 适用于已知具体合并区域的场景
     * </p>
     *
     * @param cellList 预定义的合并单元格区域列表
     */
    public CellMergeStrategy(List<CellRangeAddress> cellList) {
        this.cellList = cellList;
    }

    /**
     * 构造方法 - 根据数据自动计算合并区域
     * <p>
     * 通过 CellMergeHandler 分析数据列表，自动生成需要合并的单元格区域
     * 适用于需要根据数据动态计算合并区域的场景
     * </p>
     *
     * @param list     数据列表，用于分析合并区域
     * @param hasTitle 是否包含标题行，影响合并区域的起始行位置
     */
    public CellMergeStrategy(List<?> list, boolean hasTitle) {
        this.cellList = CellMergeHandler.of(hasTitle).handle(list);
    }

    /**
     * 单元格合并处理
     * <p>
     * 在单元格写入时调用，如果该单元格在合并区域内但不是首行，则清空内容
     * 这样可以避免合并区域内出现重复的数据显示
     * </p>
     *
     * @param sheet            当前工作表
     * @param cell             当前单元格
     * @param head             表头信息
     * @param relativeRowIndex 相对行索引
     */
    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (CollUtil.isEmpty(cellList)){
            return;
        }
        // 单元格写入了，遍历合并区域，如果该Cell在区域内，但非首行，则清空
        final int rowIndex = cell.getRowIndex();
        for (CellRangeAddress cellAddresses : cellList) {
            final int firstRow = cellAddresses.getFirstRow();
            if (cellAddresses.isInRange(cell) && rowIndex != firstRow){
                cell.setBlank();
            }
        }
    }

    /**
     * 工作簿处理完成后回调
     * <p>
     * 在所有数据写入完成后，统一将所有合并区域添加到工作表中
     * 这样可以确保合并操作在数据完全写入后进行，避免合并冲突
     * </p>
     *
     * @param context 工作簿写入处理器上下文，包含当前工作表等信息
     */
    @Override
    public void afterWorkbookDispose(final WorkbookWriteHandlerContext context) {
        if (CollUtil.isEmpty(cellList)){
            return;
        }
        // 当前表格写完后，统一写入合并区域
        for (CellRangeAddress item : cellList) {
            context.getWriteContext().writeSheetHolder().getSheet().addMergedRegion(item);
        }
    }
}
