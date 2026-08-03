package plus.ruoyi.common.excel.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.idev.excel.FastExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.fill.FillConfig;
import cn.idev.excel.write.metadata.fill.FillWrapper;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.IndexedColors;
import plus.ruoyi.common.core.utils.DateUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.file.FileUtils;
import plus.ruoyi.common.excel.convert.ExcelBigNumberConvert;
import plus.ruoyi.common.excel.core.*;
import plus.ruoyi.common.excel.handler.DataWriteHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类 - 提供Excel导入导出的核心功能
 *
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>Excel文件导入 - 支持同步/异步导入，数据校验</li>
 *   <li>Excel文件导出 - 支持基本导出，模板导出，多Sheet导出</li>
 *   <li>单元格合并 - 支持相同数据的单元格自动合并</li>
 *   <li>下拉框设置 - 支持单级和级联下拉选择</li>
 *   <li>数据格式化 - 支持字典转换，枚举转换，大数字处理</li>
 *   <li>样式设置 - 支持自定义表头样式，内容样式</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 导入Excel
 * List<User> users = ExcelUtil.importExcel(inputStream, User.class);
 *
 * // 导出Excel
 * ExcelUtil.exportExcel(userList, "用户信息", User.class, response);
 *
 * // 模板导出
 * ExcelUtil.exportTemplate(dataList, "报表", "templates/report.xlsx", response);
 * }</pre>
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

    // ==================== 导入相关方法 ====================

    /**
     * 同步导入Excel文件（适用于小数据量）
     *
     * <p>将Excel文件转换为Map集合，第一行为表头，后续行为数据</p>
     * <p>返回的List中第一个Map为表头信息，后续为数据行</p>
     *
     * @param is Excel文件输入流
     * @return 转换后的数据集合，格式为List&lt;Map&lt;列索引, 单元格值&gt;&gt;
     * @throws RuntimeException 当读取Excel文件失败时抛出
     *
     * @example
     * <pre>{@code
     * // 读取Excel文件
     * List<Map<Integer, String>> maps = ExcelUtil.importExcel(file.getInputStream());
     *
     * // 遍历数据
     * for (int i = 0; i < maps.size(); i++) {
     *     Map<Integer, String> map = maps.get(i);
     *     if (i == 0) {
     *         // 第一行为表头
     *         System.out.println("表头: " + map);
     *     } else {
     *         // 数据行
     *         System.out.println("第" + i + "行: " + map);
     *     }
     * }
     * }</pre>
     */
    public static List<Map<Integer, String>> importExcel(InputStream is) {
        DefaultExcelListener<Map<Integer, String>> listener = new DefaultExcelListener<>(false);
        FastExcel.read(is, listener).sheet().doRead();
        ExcelResult<Map<Integer, String>> excelResult = listener.getExcelResult();
        Map<Integer, String> excelHead = excelResult.getHead();
        List<Map<Integer, String>> list = excelResult.getList();
        list.add(0, excelHead);
        return list;
    }

    /**
     * 同步导入Excel文件并转换为指定对象类型（适用于小数据量）
     *
     * <p>根据实体类的注解自动映射Excel列到对象属性</p>
     * <p>要求实体类使用@ExcelProperty注解标注属性与Excel列的对应关系</p>
     *
     * @param <T> 目标对象类型
     * @param is Excel文件输入流
     * @param clazz 目标对象的Class类型
     * @return 转换后的对象集合
     * @throws RuntimeException 当读取或转换失败时抛出
     *
     * @example
     * <pre>{@code
     * // 定义实体类
     * public class User {
     *     @ExcelProperty("姓名")
     *     private String name;
     *
     *     @ExcelProperty("年龄")
     *     private Integer age;
     * }
     *
     * // 导入数据
     * List<User> users = ExcelUtil.importExcel(inputStream, User.class);
     * }</pre>
     */
    public static <T> List<T> importExcel(InputStream is, Class<T> clazz) {
        return FastExcel.read(is).head(clazz).autoCloseStream(false).sheet().doReadSync();
    }

    /**
     * 使用校验监听器异步导入Excel文件
     *
     * <p>支持数据校验功能，可以检测导入数据的合法性</p>
     * <p>返回ExcelResult对象，包含成功数据、失败数据和错误信息</p>
     *
     * @param <T> 目标对象类型
     * @param is Excel文件输入流
     * @param clazz 目标对象的Class类型
     * @param isValidate 是否启用Bean Validation校验，默认为true
     * @return Excel导入结果，包含数据列表和错误信息
     * @throws RuntimeException 当读取失败时抛出
     *
     * @example
     * <pre>{@code
     * // 导入并校验数据
     * ExcelResult<User> result = ExcelUtil.importExcel(inputStream, User.class, true);
     *
     * // 获取成功导入的数据
     * List<User> successList = result.getList();
     *
     * // 获取错误信息
     * List<String> errors = result.getErrorList();
     * if (!errors.isEmpty()) {
     *     System.out.println("导入失败: " + String.join(", ", errors));
     * }
     * }</pre>
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, boolean isValidate) {
        DefaultExcelListener<T> listener = new DefaultExcelListener<>(isValidate);
        FastExcel.read(is, clazz, listener).sheet().doRead();
        return listener.getExcelResult();
    }

    /**
     * 使用自定义监听器异步导入Excel文件
     *
     * <p>允许使用自定义的监听器来处理导入逻辑</p>
     * <p>适用于需要特殊处理逻辑的场景，如数据转换、实时处理等</p>
     *
     * @param <T> 目标对象类型
     * @param is Excel文件输入流
     * @param clazz 目标对象的Class类型
     * @param listener 自定义监听器，实现ExcelListener接口
     * @return Excel导入结果
     * @throws RuntimeException 当读取失败时抛出
     *
     * @example
     * <pre>{@code
     * // 自定义监听器
     * ExcelListener<User> customListener = new ExcelListener<User>() {
     *     @Override
     *     public void invoke(User data, AnalysisContext context) {
     *         // 自定义处理逻辑
     *         processUser(data);
     *     }
     * };
     *
     * // 使用自定义监听器导入
     * ExcelResult<User> result = ExcelUtil.importExcel(inputStream, User.class, customListener);
     * }</pre>
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, ExcelListener<T> listener) {
        FastExcel.read(is, clazz, listener).sheet().doRead();
        return listener.getExcelResult();
    }

    // ==================== 基础导出方法 ====================

    /**
     * 导出Excel文件到HTTP响应
     *
     * <p>基础导出方法，使用默认样式和设置</p>
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型，用于获取注解信息
     * @param response HTTP响应对象
     * @throws RuntimeException 当导出失败时抛出
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response) {
        try {
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();
            exportExcel(list, sheetName, clazz, false, os, null);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出Excel文件到HTTP响应（支持下拉选项）
     *
     * <p>支持在Excel中添加下拉选择框，提升数据录入体验</p>
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型
     * @param response HTTP响应对象
     * @param options 下拉选项配置列表，支持单级和级联下拉
     * @throws RuntimeException 当导出失败时抛出
     *
     * @example
     * <pre>{@code
     * // 创建下拉选项
     * List<DropDownOptions> options = Arrays.asList(
     *     new DropDownOptions(2, Arrays.asList("男", "女")), // 第3列为性别下拉
     *     new DropDownOptions(3, Arrays.asList("在职", "离职")) // 第4列为状态下拉
     * );
     *
     * ExcelUtil.exportExcel(userList, "用户信息", User.class, response, options);
     * }</pre>
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response, List<DropDownOptions> options) {
        try {
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();
            exportExcel(list, sheetName, clazz, false, os, options);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出Excel文件到HTTP响应（支持单元格合并）
     *
     * <p>支持相同内容的单元格自动合并，适用于分组数据展示</p>
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型
     * @param merge 是否启用单元格合并功能
     * @param response HTTP响应对象
     * @throws RuntimeException 当导出失败时抛出
     *
     * @example
     * <pre>{@code
     * // 数据对象需要使用@CellMerge注解
     * public class DepartmentUser {
     *     @CellMerge // 相同部门名称的单元格将被合并
     *     @ExcelProperty("部门")
     *     private String department;
     *
     *     @ExcelProperty("姓名")
     *     private String name;
     * }
     *
     * ExcelUtil.exportExcel(deptUsers, "部门用户", DepartmentUser.class, true, response);
     * }</pre>
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, HttpServletResponse response) {
        try {
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();
            exportExcel(list, sheetName, clazz, merge, os, null);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出Excel文件到HTTP响应（完整功能版本）
     *
     * <p>支持单元格合并和下拉选项的完整功能版本</p>
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型
     * @param merge 是否启用单元格合并功能
     * @param response HTTP响应对象
     * @param options 下拉选项配置列表
     * @throws RuntimeException 当导出失败时抛出
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, HttpServletResponse response, List<DropDownOptions> options) {
        try {
            resetResponse(sheetName, response);
            ServletOutputStream os = response.getOutputStream();
            exportExcel(list, sheetName, clazz, merge, os, options);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    // ==================== 输出流导出方法 ====================

    /**
     * 导出Excel文件到输出流（基础版本）
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型
     * @param os 输出流
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, OutputStream os) {
        exportExcel(list, sheetName, clazz, false, os, null);
    }

    /**
     * 导出Excel文件到输出流（支持下拉选项）
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型
     * @param os 输出流
     * @param options 下拉选项配置列表
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, OutputStream os, List<DropDownOptions> options) {
        exportExcel(list, sheetName, clazz, false, os, options);
    }

    /**
     * 导出Excel文件到输出流（完整功能版本）
     *
     * <p>核心导出方法，所有其他导出方法最终都会调用此方法</p>
     * <p>支持的功能：</p>
     * <ul>
     *   <li>自动列宽适配</li>
     *   <li>大数字防失真转换</li>
     *   <li>单元格合并</li>
     *   <li>下拉选项设置</li>
     *   <li>数据校验和格式化</li>
     *   <li>样式设置</li>
     * </ul>
     *
     * @param <T> 数据对象类型
     * @param list 要导出的数据集合
     * @param sheetName 工作表名称
     * @param clazz 数据对象的Class类型
     * @param merge 是否启用单元格合并功能
     * @param os 输出流
     * @param options 下拉选项配置列表，可为null
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge,
                                       OutputStream os, List<DropDownOptions> options) {
        ExcelWriterSheetBuilder builder = FastExcel.write(os, clazz)
            .autoCloseStream(false)
            // 自动适配列宽，根据内容长度调整
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            // 大数值自动转换，防止Excel显示科学计数法
            .registerConverter(new ExcelBigNumberConvert())
            // 数据写入处理器，处理批注和必填字段样式
            .registerWriteHandler(new DataWriteHandler(clazz))
            // 初始化表头和内容的默认样式
            .registerWriteHandler(initCellStyle())
            .sheet(sheetName);

        if (merge) {
            // 注册单元格合并处理器
            builder.registerWriteHandler(new CellMergeStrategy(list, true));
        }

        // 注册下拉框处理器
        builder.registerWriteHandler(new ExcelDownHandler(options));

        // 执行写入操作
        builder.doWrite(list);
    }

    /**
     * 初始化Excel表格样式
     *
     * <p>设置表头和内容的默认样式：</p>
     * <ul>
     *   <li>表头：灰色背景，白色字体，微软雅黑10号字</li>
     *   <li>内容：默认样式</li>
     * </ul>
     *
     * @return 水平单元格样式策略
     */
    public static HorizontalCellStyleStrategy initCellStyle() {
        // 表头样式设置
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 背景设置为50%灰色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 10);
        headWriteCellStyle.setWriteFont(headWriteFont);
        headWriteFont.setColor(IndexedColors.WHITE.getIndex());
        headWriteFont.setFontName("微软雅黑");

        // 内容样式设置（使用默认样式）
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
			/*contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
			// 背景颜色
			contentWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
			WriteFont contentWriteFont = new WriteFont();
			// 字体大小
			contentWriteFont.setFontHeightInPoints((short)12);
			contentWriteCellStyle.setWriteFont(contentWriteFont);*/
        // 返回水平样式策略：表头使用headWriteCellStyle，内容使用contentWriteCellStyle
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    // ==================== 模板导出方法 ====================

    /**
     * 单表多数据模板导出到HTTP响应
     *
     * <p>使用预定义的Excel模板进行数据填充导出</p>
     * <p>模板格式：使用 {.属性名} 作为占位符</p>
     * <p>适用场景：报表导出、格式固定的数据导出</p>
     *
     * @param <T> 数据对象类型
     * @param data 要填充到模板的数据列表
     * @param filename 导出文件名（不含扩展名）
     * @param templatePath 模板文件路径，相对于resources目录
     * @param response HTTP响应对象
     * @throws RuntimeException 当模板不存在或导出失败时抛出
     * @throws IllegalArgumentException 当数据为空时抛出
     *
     * @example
     * <pre>{@code
     * // 模板文件：resources/templates/user-report.xlsx
     * // 模板内容：{.name} {.age} {.department}
     *
     * List<User> users = getUserList();
     * ExcelUtil.exportTemplate(users, "用户报表", "templates/user-report.xlsx", response);
     * }</pre>
     */
    public static <T> void exportTemplate(List<T> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            resetResponse(filename, response);
            ServletOutputStream os = response.getOutputStream();
            exportTemplate(data, templatePath, os);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 单表多数据模板导出到输出流
     *
     * <p>核心模板导出方法，支持循环填充数据到模板</p>
     *
     * @param <T> 数据对象类型
     * @param data 要填充的数据列表
     * @param templatePath 模板文件路径，相对于resources目录
     * @param os 输出流
     * @throws RuntimeException 当模板读取失败时抛出
     */
    public static <T> void exportTemplate(List<T> data, String templatePath, OutputStream os) {
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        ExcelWriter excelWriter = FastExcel.write(os)
            .withTemplate(templateResource.getStream())
            .autoCloseStream(false)
            // 大数值自动转换防止失真
            .registerConverter(new ExcelBigNumberConvert())
            .registerWriteHandler(new DataWriteHandler(data.get(0).getClass()))
            .build();
        WriteSheet writeSheet = FastExcel.writerSheet().build();
        // 每次填充数据都强制新行，避免数据覆盖
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();

        // 循环填充每一条数据到模板
        for (T d : data) {
            excelWriter.fill(d, fillConfig, writeSheet);
        }
        excelWriter.finish();
    }

    /**
     * 多表多数据模板导出到HTTP响应
     *
     * <p>支持在一个模板中填充多个不同类型的数据表</p>
     * <p>模板格式：使用 {key.属性名} 作为占位符，其中key对应数据Map的键</p>
     *
     * @param data 多表数据，Map的键为表标识，值为对应的数据列表或对象
     * @param filename 导出文件名
     * @param templatePath 模板文件路径
     * @param response HTTP响应对象
     * @throws RuntimeException 当导出失败时抛出
     * @throws IllegalArgumentException 当数据为空时抛出
     *
     * @example
     * <pre>{@code
     * // 模板内容：
     * // 用户信息：{userInfo.name} {userInfo.department}
     * // 订单列表：{orders.orderNo} {orders.amount}
     *
     * Map<String, Object> data = new HashMap<>();
     * data.put("userInfo", userObject);
     * data.put("orders", orderList);
     *
     * ExcelUtil.exportTemplateMultiList(data, "综合报表", "templates/multi-report.xlsx", response);
     * }</pre>
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            resetResponse(filename, response);
            ServletOutputStream os = response.getOutputStream();
            exportTemplateMultiList(data, templatePath, os);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 多Sheet模板导出到HTTP响应
     *
     * <p>支持导出多个工作表，每个工作表使用相同的模板但填充不同的数据</p>
     *
     * @param data 多Sheet数据，List中每个Map对应一个Sheet的数据
     * @param filename 导出文件名
     * @param templatePath 模板文件路径
     * @param response HTTP响应对象
     * @throws RuntimeException 当导出失败时抛出
     * @throws IllegalArgumentException 当数据为空时抛出
     *
     * @example
     * <pre>{@code
     * List<Map<String, Object>> sheetDataList = new ArrayList<>();
     *
     * // 第一个Sheet的数据
     * Map<String, Object> sheet1Data = new HashMap<>();
     * sheet1Data.put("users", userList1);
     * sheetDataList.add(sheet1Data);
     *
     * // 第二个Sheet的数据
     * Map<String, Object> sheet2Data = new HashMap<>();
     * sheet2Data.put("users", userList2);
     * sheetDataList.add(sheet2Data);
     *
     * ExcelUtil.exportTemplateMultiSheet(sheetDataList, "多部门报表", "templates/dept-report.xlsx", response);
     * }</pre>
     */
    public static void exportTemplateMultiSheet(List<Map<String, Object>> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            resetResponse(filename, response);
            ServletOutputStream os = response.getOutputStream();
            exportTemplateMultiSheet(data, templatePath, os);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 多表多数据模板导出到输出流
     *
     * <p>核心多表导出方法</p>
     *
     * @param data 多表数据Map
     * @param templatePath 模板文件路径
     * @param os 输出流
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String templatePath, OutputStream os) {
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        ExcelWriter excelWriter = FastExcel.write(os)
            .withTemplate(templateResource.getStream())
            .autoCloseStream(false)
            // 大数值自动转换防止失真
            .registerConverter(new ExcelBigNumberConvert())
            .build();
        WriteSheet writeSheet = FastExcel.writerSheet().build();

        for (Map.Entry<String, Object> map : data.entrySet()) {
            // 设置列表后续还有数据，强制新行
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            if (map.getValue() instanceof Collection) {
                // 集合数据必须使用FillWrapper包装
                excelWriter.fill(new FillWrapper(map.getKey(), (Collection<?>) map.getValue()), fillConfig, writeSheet);
            } else {
                // 单个对象直接填充
                excelWriter.fill(map.getValue(), fillConfig, writeSheet);
            }
        }
        excelWriter.finish();
    }

    /**
     * 多Sheet模板导出到输出流
     *
     * <p>核心多Sheet导出方法</p>
     *
     * @param data 多Sheet数据列表
     * @param templatePath 模板文件路径
     * @param os 输出流
     */
    public static void exportTemplateMultiSheet(List<Map<String, Object>> data, String templatePath, OutputStream os) {
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        ExcelWriter excelWriter = FastExcel.write(os)
            .withTemplate(templateResource.getStream())
            .autoCloseStream(false)
            // 大数值自动转换防止失真
            .registerConverter(new ExcelBigNumberConvert())
            .build();

        // 为每个Sheet填充数据
        for (int i = 0; i < data.size(); i++) {
            WriteSheet writeSheet = FastExcel.writerSheet(i).build();
            for (Map.Entry<String, Object> map : data.get(i).entrySet()) {
                // 设置列表后续还有数据，强制新行
                FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
                if (map.getValue() instanceof Collection) {
                    // 集合数据必须使用FillWrapper包装
                    excelWriter.fill(new FillWrapper(map.getKey(), (Collection<?>) map.getValue()), fillConfig, writeSheet);
                } else {
                    // 单个对象直接填充
                    excelWriter.fill(map.getValue(), writeSheet);
                }
            }
        }
        excelWriter.finish();
    }

    // ==================== 工具方法 ====================

    /**
     * 重置HTTP响应头，设置Excel文件下载相关信息
     *
     * <p>设置响应头信息：</p>
     * <ul>
     *   <li>Content-Type: Excel文件MIME类型</li>
     *   <li>Content-Disposition: 附件下载，文件名包含时间戳</li>
     * </ul>
     *
     * @param sheetName 工作表名称，用作文件名
     * @param response HTTP响应对象
     * @throws UnsupportedEncodingException 当文件名编码失败时抛出
     */
    private static void resetResponse(String sheetName, HttpServletResponse response) throws UnsupportedEncodingException {
        String filename = encodingFilename(sheetName);
        FileUtils.setAttachmentResponseHeader(response, filename);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
    }

    /**
     * 解析导出值转换表达式
     *
     * <p>将代码值转换为显示文本，支持多值分隔</p>
     * <p>转换规则格式：0=男,1=女,2=未知</p>
     *
     * @param propertyValue 要转换的属性值（代码值）
     * @param converterExp 转换表达式，格式为"代码=文本,代码=文本"
     * @param separator 多值分隔符
     * @return 转换后的显示文本
     *
     * @example
     * <pre>{@code
     * // 单值转换
     * String result = ExcelUtil.convertByExp("1", "0=男,1=女,2=未知", ",");
     * // 结果：女
     *
     * // 多值转换
     * String result = ExcelUtil.convertByExp("0,1", "0=男,1=女,2=未知", ",");
     * // 结果：男,女
     * }</pre>
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(StringUtils.SEPARATOR);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                // 处理多值情况
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1] + separator);
                        break;
                    }
                }
            } else {
                // 处理单值情况
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值转换表达式
     *
     * <p>将显示文本转换为代码值，支持多值分隔</p>
     * <p>主要用于Excel导入时将用户可读的文本转换为系统使用的代码</p>
     *
     * @param propertyValue 要转换的显示文本
     * @param converterExp 转换表达式，格式为"代码=文本,代码=文本"
     * @param separator 多值分隔符
     * @return 转换后的代码值
     *
     * @example
     * <pre>{@code
     * // 单值反向转换
     * String result = ExcelUtil.reverseByExp("女", "0=男,1=女,2=未知", ",");
     * // 结果：1
     *
     * // 多值反向转换
     * String result = ExcelUtil.reverseByExp("男,女", "0=男,1=女,2=未知", ",");
     * // 结果：0,1
     * }</pre>
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(StringUtils.SEPARATOR);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                // 处理多值情况
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0] + separator);
                        break;
                    }
                }
            } else {
                // 处理单值情况
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 编码文件名，添加时间戳防止文件名冲突
     *
     * <p>文件名格式：原始名称 + 年月日时分秒 + .xlsx</p>
     * <p>例如：用户信息20231201143022.xlsx</p>
     *
     * @param filename 原始文件名
     * @return 编码后的文件名，包含时间戳和扩展名
     */
    public static String encodingFilename(String filename) {
        // 附加当前时间戳到文件名，格式：yyyyMMddHHmmss
        return filename + DateUtils.dateTimeNow() + ".xlsx";
        // 备选方案：使用UUID
        // return IdUtil.fastSimpleUUID() + "_" + filename + ".xlsx";
    }
}
