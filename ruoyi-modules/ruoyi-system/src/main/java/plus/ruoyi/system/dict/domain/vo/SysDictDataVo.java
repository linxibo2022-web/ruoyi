package plus.ruoyi.system.dict.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.system.dict.domain.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 字典数据视图对象 sys_dict_data
 *
 * @author Michelle.Chung
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysDictData.class)
public class SysDictDataVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典编码
     */
    @ExcelProperty(value = "字典编码")
    private Long dictDataId;

    /**
     * 字典排序
     */
    @ExcelProperty(value = "字典排序")
    private Integer dictSort;

    /**
     * 字典标签
     */
    @ExcelProperty(value = "字典标签")
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /**
     * 字典键值
     */
    @ExcelProperty(value = "字典键值")
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    /**
     * 字典类型
     */
    @ExcelProperty(value = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    @ExcelProperty(value = "样式属性")
    private String cssClass;

    /**
     * 表格回显样式
     */
    @ExcelProperty(value = "表格回显样式")
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @ExcelProperty(value = "是否默认", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictBooleanFlag.DICT_TYPE)
    private String isDefault;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
