package plus.ruoyi.system.oss.domain.vo;

import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.system.oss.domain.SysOssDirectory;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * OSS目录视图对象 sys_oss_directory
 *
 * @author 抓蛙师
 * @date 2025-04-14
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysOssDirectory.class)
public class SysOssDirectoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目录ID
     */
    @ExcelProperty(value = "目录ID")
    private Long directoryId;

    /**
     * 父目录ID
     */
    @ExcelProperty(value = "父目录ID")
    private Long parentId;

    /**
     * 祖级列表
     */
    @ExcelProperty(value = "祖级列表")
    private String ancestors;

    /**
     * 目录名称
     */
    @ExcelProperty(value = "目录名称")
    private String directoryName;

    /**
     * 目录路径
     */
    @ExcelProperty(value = "目录路径")
    private String directoryPath;

    /**
     * 显示顺序
     */
    @ExcelProperty(value = "显示顺序")
    private Long orderNum;

    /**
     * 目录状态
     */
    @ExcelProperty(value = "目录状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;

    /**
     * 是否默认目录
     */
    @ExcelProperty(value = "是否默认目录", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictBooleanFlag.DICT_TYPE)
    private String isDefault;

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

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
