package plus.ruoyi.system.dict.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.system.dict.domain.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 字典类型视图对象 sys_dict_type
 *
 * @author Michelle.Chung
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysDictType.class)
public class SysDictTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典主键
     */
    @ExcelProperty(value = "字典主键")
    private Long dictId;

    /**
     * 字典名称
     */
    @ExcelProperty(value = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /**
     * 字典编码
     */
    @ExcelProperty(value = "字典编码")
    @NotBlank(message = "字典编码不能为空")
    private String dictType;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;

    /**
     * 是否系统级字典
     * 0 - 租户自定义字典（允许修改）
     * 1 - 系统级字典（租户不允许修改）
     */
    @ExcelProperty(value = "是否系统级")
    private String isSystem;

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

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

}
