package plus.ruoyi.common.excel.core;

import cn.hutool.core.util.StrUtil;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import cn.idev.excel.exception.ExcelAnalysisException;
import cn.idev.excel.exception.ExcelDataConvertException;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.ValidatorUtils;
import plus.ruoyi.common.json.utils.JsonUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * 默认Excel导入监听器
 *
 * <p>Excel导入过程中的事件监听和处理器，提供以下核心功能：</p>
 * <ul>
 *   <li><strong>数据接收</strong>：逐行接收Excel解析后的数据</li>
 *   <li><strong>数据校验</strong>：可选的Bean Validation校验</li>
 *   <li><strong>异常处理</strong>：统一的异常捕获和错误信息收集</li>
 *   <li><strong>结果汇总</strong>：提供导入结果的统计和分析</li>
 * </ul>
 *
 * <p><strong>工作流程：</strong></p>
 * <ol>
 *   <li>解析表头信息（invokeHeadMap）</li>
 *   <li>逐行解析数据（invoke）</li>
 *   <li>执行数据校验（可选）</li>
 *   <li>收集成功数据和错误信息</li>
 *   <li>完成解析（doAfterAllAnalysed）</li>
 * </ol>
 *
 * <p><strong>异常处理能力：</strong></p>
 * <ul>
 *   <li>数据转换异常：精确定位到行列</li>
 *   <li>数据校验异常：详细的校验错误信息</li>
 *   <li>其他解析异常：通用异常处理</li>
 * </ul>
 *
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * // 创建监听器（启用校验）
 * DefaultExcelListener<User> listener = new DefaultExcelListener<>(true);
 *
 * // 执行Excel读取
 * FastExcel.read(inputStream, User.class, listener).sheet().doRead();
 *
 * // 获取结果
 * ExcelResult<User> result = listener.getExcelResult();
 * List<User> successList = result.getList();
 * List<String> errorList = result.getErrorList();
 * }</pre>
 *
 * @author Yjoioooo
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor
public class DefaultExcelListener<T> extends AnalysisEventListener<T> implements ExcelListener<T> {

    /**
     * 是否启用Bean Validation校验
     *
     * <p>当为true时，会对每一行数据执行JSR-303校验注解检查</p>
     * <p>校验失败的数据会被记录到错误列表中，不会添加到成功列表</p>
     */
    private Boolean isValidate = Boolean.TRUE;

    /**
     * Excel表头数据映射
     *
     * <p>Key：列索引（从0开始）</p>
     * <p>Value：表头文本内容</p>
     * <p>用于异常信息中的列名显示，提升错误信息的可读性</p>
     */
    private Map<Integer, String> headMap;

    /**
     * 导入结果容器
     *
     * <p>存储导入过程中的所有信息：</p>
     * <ul>
     *   <li>成功解析的数据列表</li>
     *   <li>错误信息列表</li>
     *   <li>表头信息</li>
     *   <li>导入统计分析</li>
     * </ul>
     */
    private ExcelResult<T> excelResult;

    /**
     * 构造函数
     *
     * @param isValidate 是否启用数据校验
     */
    public DefaultExcelListener(boolean isValidate) {
        this.excelResult = new DefaultExcelResult<>();
        this.isValidate = isValidate;
    }

    /**
     * 异常处理方法
     *
     * <p>统一处理Excel解析过程中的各种异常，提供详细的错误定位信息</p>
     *
     * <p><strong>支持的异常类型：</strong></p>
     * <ul>
     *   <li><strong>ExcelDataConvertException</strong>：数据转换异常，如类型不匹配</li>
     *   <li><strong>ConstraintViolationException</strong>：数据校验异常，如@NotNull校验失败</li>
     *   <li><strong>其他异常</strong>：通用异常处理</li>
     * </ul>
     *
     * @param exception 发生的异常
     * @param context   Excel分析上下文，包含当前处理位置信息
     * @throws Exception 重新抛出ExcelAnalysisException，包含详细错误信息
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        String errMsg = null;

        // 处理数据转换异常
        if (exception instanceof ExcelDataConvertException excelDataConvertException) {
            // 获取异常发生的精确位置
            Integer rowIndex = excelDataConvertException.getRowIndex();
            Integer columnIndex = excelDataConvertException.getColumnIndex();

            // 构建详细的错误信息，包含行号、列号、表头名称
            errMsg = StrUtil.format("第{}行-第{}列-表头{}: 解析异常<br/>",
                rowIndex + 1, columnIndex + 1, headMap.get(columnIndex));

            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }

        // 处理数据校验异常
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();

            // 合并所有校验错误信息
            String constraintViolationsMsg = StreamUtils.join(constraintViolations, ConstraintViolation::getMessage, ", ");

            // 构建校验异常信息
            errMsg = StrUtil.format("第{}行数据校验异常: {}",
                context.readRowHolder().getRowIndex() + 1, constraintViolationsMsg);

            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }

        // 将错误信息添加到结果中
        excelResult.getErrorList().add(errMsg);

        // 抛出Excel分析异常，中断当前行的处理但不影响后续行
        throw new ExcelAnalysisException(errMsg);
    }

    /**
     * 表头处理方法
     *
     * <p>在Excel表头解析完成后调用，保存表头信息用于后续的错误信息展示</p>
     *
     * @param headMap 表头映射，Key为列索引，Value为表头文本
     * @param context Excel分析上下文
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        log.debug("解析到一条表头数据: {}", JsonUtils.toJsonString(headMap));
    }

    /**
     * 数据行处理方法
     *
     * <p>每解析完一行数据后调用此方法</p>
     *
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>接收解析后的数据对象</li>
     *   <li>执行数据校验（如果启用）</li>
     *   <li>校验通过的数据添加到成功列表</li>
     *   <li>校验失败会抛出异常，由onException方法处理</li>
     * </ol>
     *
     * @param data    解析后的数据对象
     * @param context Excel分析上下文
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        if (isValidate) {
            // 执行Bean Validation校验
            // 如果校验失败，会抛出ConstraintViolationException
            ValidatorUtils.validate(data);
        }

        // 校验通过，添加到成功列表
        excelResult.getList().add(data);
    }

    /**
     * 解析完成后的处理方法
     *
     * <p>所有数据解析完成后调用，可用于执行清理工作或最终统计</p>
     *
     * @param context Excel分析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.debug("所有数据解析完成！");
    }

    /**
     * 获取Excel导入结果
     *
     * <p>返回完整的导入结果，包括：</p>
     * <ul>
     *   <li>成功导入的数据列表</li>
     *   <li>错误信息列表</li>
     *   <li>表头信息</li>
     *   <li>导入分析结果</li>
     */
    @Override
    public ExcelResult<T> getExcelResult() {
        return excelResult;
    }

}
