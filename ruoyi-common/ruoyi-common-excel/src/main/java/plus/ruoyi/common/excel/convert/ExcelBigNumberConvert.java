package plus.ruoyi.common.excel.convert;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 大数值转换处理器
 *
 * <p><strong>解决问题：</strong></p>
 * <p>Excel对数值精度有限制，当数值长度超过15位时会自动转换为科学计数法，
 * 导致精度丢失。这对于长整型数据（如ID、手机号、身份证号等）是不可接受的。</p>
 *
 * <p><strong>解决方案：</strong></p>
 * <ul>
 *   <li>导出时：超过15位的数值转换为字符串格式存储</li>
 *   <li>导入时：将字符串或数值转换回Long类型</li>
 * </ul>
 *
 * <p>此转换器会自动注册到ExcelUtil中，无需手动配置。
 * 所有Long类型字段都会自动使用此转换器。</p>
 *
 * <p><strong>示例：</strong></p>
 * <pre>{@code
 * public class User {
 *     @ExcelProperty("用户ID")
 *     private Long userId; // 如：1234567890123456789L
 * }
 * }</pre>
 *
 * @author Lion Li
 */
@Slf4j
public class ExcelBigNumberConvert implements Converter<Long> {

    /**
     * Excel数值精度限制：超过15位会丢失精度
     */
    private static final int EXCEL_NUMBER_PRECISION_LIMIT = 15;

    /**
     * 支持的Java类型
     *
     * @return Long.class 专门处理长整型数据
     */
    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    /**
     * 支持的Excel单元格类型
     *
     * @return null 表示支持所有Excel单元格类型，确保转换器能够被匹配到
     */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;  // ✅ 返回null，覆盖FastExcel内置的Long转换器
    }

    /**
     * Excel数据转换为Java Long类型（导入过程）
     *
     * <p>支持从Excel的数值类型或字符串类型转换为Long</p>
     * <p>转换过程容错性强，能处理各种输入格式</p>
     *
     * @param cellData Excel单元格数据
     * @param contentProperty Excel内容属性
     * @param globalConfiguration 全局配置
     * @return 转换后的Long值
     */
    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        // 使用hutool的转换工具，支持多种格式的数据转换
        return Convert.toLong(cellData.getStringValue());
    }

    /**
     * Java Long类型转换为Excel数据（导出过程）
     *
     * <p>转换策略：</p>
     * <ul>
     *   <li>长度 ≤ 15位：保持数值格式，Excel可以正常显示和计算</li>
     *   <li>长度 > 15位：转换为字符串格式，防止精度丢失</li>
     * </ul>
     *
     * @param object Long类型的数值
     * @param contentProperty Excel内容属性
     * @param globalConfiguration 全局配置
     * @return Excel单元格数据，可能是数值或字符串格式
     */
    @Override
    public WriteCellData<Object> convertToExcelData(Long object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNotNull(object)) {
            String str = Convert.toStr(object);

            // 判断数值长度是否超过Excel精度限制
            if (str.length() > EXCEL_NUMBER_PRECISION_LIMIT) {
                // 超过15位，使用字符串格式防止精度丢失
                return new WriteCellData<>(str);
            }

            // 15位以内，使用数值格式，便于Excel进行数值计算
            WriteCellData<Object> cellData = new WriteCellData<>(new BigDecimal(object));
            cellData.setType(CellDataTypeEnum.NUMBER);
            return cellData;
        }

        // 如果object为null，返回空的单元格数据
        return new WriteCellData<>("");
    }
}
