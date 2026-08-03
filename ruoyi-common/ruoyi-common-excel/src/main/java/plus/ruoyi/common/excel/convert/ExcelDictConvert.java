package plus.ruoyi.common.excel.convert;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.core.service.DictService;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 字典格式化转换处理器
 *
 * <p>实现Excel与系统字典数据之间的双向转换：</p>
 * <ul>
 *   <li>导出时：将字典键值转换为用户友好的显示文本</li>
 *   <li>导入时：将显示文本转换回对应的字典键值</li>
 * </ul>
 *
 * <p>支持两种转换模式：</p>
 * <ul>
 *   <li><strong>字典服务模式</strong>：通过dictType从DictService获取字典数据</li>
 *   <li><strong>表达式模式</strong>：通过readConverterExp直接定义转换规则</li>
 * </ul>
 *
 * <p>使用场景示例：</p>
 * <ul>
 *   <li>用户性别：0 <-> "男"，1 <-> "女"</li>
 *   <li>用户状态：0 <-> "正常"，1 <-> "停用"</li>
 *   <li>数据权限：1 <-> "全部数据权限"，2 <-> "自定数据权限"</li>
 * </ul>
 *
 * <p>特殊功能：</p>
 * <ul>
 *   <li>支持多值转换：通过分隔符处理多个字典值</li>
 *   <li>自动空值处理：空值转换为空字符串</li>
 *   <li>灵活配置：支持自定义分隔符</li>
 * </ul>
 *
 * @author Lion Li
 */
@Slf4j
public class ExcelDictConvert implements Converter<Object> {

    /**
     * 支持的Java类型
     *
     * @return Object.class 表示支持任意类型
     */
    @Override
    public Class<Object> supportJavaTypeKey() {
        return Object.class;
    }

    /**
     * 支持的Excel单元格类型
     *
     * @return null 表示支持所有Excel单元格类型
     */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    /**
     * Excel数据转换为Java对象（导入过程）
     *
     * <p>转换流程：</p>
     * <ol>
     *   <li>从Excel单元格获取显示文本（label）</li>
     *   <li>根据注解配置选择转换方式：字典服务 或 表达式</li>
     *   <li>将显示文本转换为对应的字典键值（value）</li>
     *   <li>转换为目标字段类型并返回</li>
     * </ol>
     *
     * @param cellData Excel单元格数据
     * @param contentProperty Excel内容属性，包含字段信息和注解
     * @param globalConfiguration 全局配置
     * @return 转换后的字典键值
     */
    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        // 获取字段上的字典格式化注解
        ExcelDictFormat anno = getAnnotation(contentProperty.getField());
        String type = anno.dictType();
        // 从Excel获取显示文本
        String label = cellData.getStringValue();
        String value;

        if (StringUtils.isBlank(type)) {
            // 表达式模式：使用readConverterExp进行反向转换
            value = ExcelUtil.reverseByExp(label, anno.readConverterExp(), anno.separator());
        } else {
            // 字典服务模式：通过DictService获取字典值
            value = SpringUtils.getBean(DictService.class).getDictValue(type, label, anno.separator());
        }

        // 转换为目标字段类型
        return Convert.convert(contentProperty.getField().getType(), value);
    }

    /**
     * Java对象转换为Excel数据（导出过程）
     *
     * <p>转换流程：</p>
     * <ol>
     *   <li>获取Java对象的字典键值（value）</li>
     *   <li>根据注解配置选择转换方式：字典服务 或 表达式</li>
     *   <li>将字典键值转换为用户友好的显示文本（label）</li>
     *   <li>返回包含显示文本的Excel单元格数据</li>
     * </ol>
     *
     * @param object 要转换的Java对象值（字典键值）
     * @param contentProperty Excel内容属性
     * @param globalConfiguration 全局配置
     * @return 包含显示文本的Excel单元格数据
     */
    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNull(object)) {
            return new WriteCellData<>("");
        }

        // 获取字段上的字典格式化注解
        ExcelDictFormat anno = getAnnotation(contentProperty.getField());
        String type = anno.dictType();
        // 将对象转换为字符串（字典键值）
        String value = Convert.toStr(object);
        String label;

        if (StringUtils.isBlank(type)) {
            // 表达式模式：使用readConverterExp进行转换
            label = ExcelUtil.convertByExp(value, anno.readConverterExp(), anno.separator());
        } else {
            // 字典服务模式：通过DictService获取字典标签
            label = SpringUtils.getBean(DictService.class).getDictLabel(type, value, anno.separator());
        }

        return new WriteCellData<>(label);
    }

    /**
     * 获取字段上的@ExcelDictFormat注解
     *
     * @param field 字段对象
     * @return ExcelDictFormat注解实例
     */
    private ExcelDictFormat getAnnotation(Field field) {
        return AnnotationUtil.getAnnotation(field, ExcelDictFormat.class);
    }
}
