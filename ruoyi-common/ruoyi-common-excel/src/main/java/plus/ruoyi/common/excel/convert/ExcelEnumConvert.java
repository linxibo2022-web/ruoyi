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
import plus.ruoyi.common.core.utils.reflect.ReflectUtils;
import plus.ruoyi.common.excel.annotation.ExcelEnumFormat;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 枚举格式化转换处理器
 *
 * <p>实现Excel与Java枚举类型之间的双向转换：</p>
 * <ul>
 *   <li>导出时：将枚举对象的指定字段值转换为Excel显示文本</li>
 *   <li>导入时：将Excel显示文本转换回对应的枚举值字段</li>
 * </ul>
 *
 * <p>工作原理：</p>
 * <ol>
 *   <li>读取@ExcelEnumFormat注解获取枚举类、值字段、标签字段信息</li>
 *   <li>构建枚举值与标签的映射关系</li>
 *   <li>根据转换方向执行相应的转换逻辑</li>
 * </ol>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户状态枚举：ACTIVE(1,"激活") <-> "激活"</li>
 *   <li>订单状态枚举：PENDING(0,"待处理") <-> "待处理"</li>
 *   <li>权限级别枚举：ADMIN(9,"管理员") <-> "管理员"</li>
 * </ul>
 *
 * <p>注意事项：</p>
 * <ul>
 *   <li>枚举类必须包含指定的valueField和labelField字段</li>
 *   <li>实体类字段类型应与枚举的valueField类型匹配</li>
 *   <li>转换失败时会抛出相应的异常</li>
 * </ul>
 *
 * @author Liang
 */
@Slf4j
public class ExcelEnumConvert implements Converter<Object> {

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
     * Excel数据转换为Java对象
     *
     * <p>转换流程：</p>
     * <ol>
     *   <li>从Excel单元格读取数据（文本值）</li>
     *   <li>根据注解信息构建枚举标签到值的映射</li>
     *   <li>通过标签查找对应的枚举值</li>
     *   <li>将枚举值转换为目标字段类型</li>
     * </ol>
     *
     * @param cellData Excel单元格数据
     * @param contentProperty Excel内容属性，包含字段信息和注解
     * @param globalConfiguration 全局配置
     * @return 转换后的Java对象值
     * @throws IllegalArgumentException 当单元格类型不支持时抛出
     */
    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        cellData.checkEmpty();

        // 根据单元格类型获取文本值
        Object textValue = switch (cellData.getType()) {
            case STRING, DIRECT_STRING, RICH_TEXT_STRING -> cellData.getStringValue();
            case NUMBER -> cellData.getNumberValue();
            case BOOLEAN -> cellData.getBooleanValue();
            default -> throw new IllegalArgumentException("单元格类型异常!");
        };

        // 处理空值情况
        if (ObjectUtil.isNull(textValue)) {
            return null;
        }

        // 构建枚举代码到文本的映射关系
        Map<Object, String> enumCodeToTextMap = beforeConvert(contentProperty);

        // 由于导出时是code转text，导入时需要反向转换：text转code
        Map<Object, Object> enumTextToCodeMap = new HashMap<>();
        enumCodeToTextMap.forEach((key, value) -> enumTextToCodeMap.put(value, key));

        // 通过显示文本查找对应的枚举值
        Object codeValue = enumTextToCodeMap.get(textValue);

        // 转换为目标字段类型并返回
        return Convert.convert(contentProperty.getField().getType(), codeValue);
    }

    /**
     * Java对象转换为Excel数据
     *
     * <p>转换流程：</p>
     * <ol>
     *   <li>获取Java对象的值（枚举的value字段值）</li>
     *   <li>根据注解信息构建枚举值到标签的映射</li>
     *   <li>通过枚举值查找对应的显示标签</li>
     *   <li>返回包含显示标签的Excel单元格数据</li>
     * </ol>
     *
     * @param object 要转换的Java对象值
     * @param contentProperty Excel内容属性
     * @param globalConfiguration 全局配置
     * @return 包含转换后文本的Excel单元格数据
     */
    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNull(object)) {
            return new WriteCellData<>("");
        }

        // 构建枚举值到显示文本的映射关系
        Map<Object, String> enumValueMap = beforeConvert(contentProperty);

        // 根据对象值查找对应的显示文本
        String value = Convert.toStr(enumValueMap.get(object), "");

        return new WriteCellData<>(value);
    }

    /**
     * 转换前的预处理，构建枚举映射关系
     *
     * <p>核心逻辑：</p>
     * <ol>
     *   <li>从字段上获取@ExcelEnumFormat注解</li>
     *   <li>获取枚举类的所有常量</li>
     *   <li>通过反射获取每个枚举常量的value和label字段值</li>
     *   <li>构建value -> label的映射Map</li>
     * </ol>
     *
     * @param contentProperty Excel内容属性，包含字段和注解信息
     * @return 枚举值到显示标签的映射Map
     */
    private Map<Object, String> beforeConvert(ExcelContentProperty contentProperty) {
        // 获取字段上的枚举格式化注解
        ExcelEnumFormat anno = getAnnotation(contentProperty.getField());

        Map<Object, String> enumValueMap = new HashMap<>();

        // 获取枚举类的所有常量
        Enum<?>[] enumConstants = anno.enumClass().getEnumConstants();

        // 遍历每个枚举常量，构建映射关系
        for (Enum<?> enumConstant : enumConstants) {
            // 通过反射获取枚举的value字段值
            Object value = ReflectUtils.invokeGetter(enumConstant, anno.valueField());
            // 通过反射获取枚举的label字段值
            String label = ReflectUtils.invokeGetter(enumConstant, anno.labelField());
            // 建立value -> label的映射关系
            enumValueMap.put(value, label);
        }

        return enumValueMap;
    }

    /**
     * 获取字段上的@ExcelEnumFormat注解
     *
     * @param field 字段对象
     * @return ExcelEnumFormat注解实例
     */
    private ExcelEnumFormat getAnnotation(Field field) {
        return AnnotationUtil.getAnnotation(field, ExcelEnumFormat.class);
    }
}
