package plus.ruoyi.system.core.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.serialmap.annotation.SerialMap;
import plus.ruoyi.common.serialmap.constant.SerialMapConstant;
import plus.ruoyi.system.core.domain.SysPost;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 岗位信息视图对象 sys_post
 *
 * @author Michelle.Chung
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysPost.class)
public class SysPostVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 岗位ID
     */
    @ExcelProperty(value = "岗位序号")
    private Long postId;

    /**
     * 部门id
     */
    @ExcelProperty(value = "部门id")
    private Long deptId;

    /**
     * 岗位编码
     */
    @ExcelProperty(value = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @ExcelProperty(value = "岗位名称")
    private String postName;

    /**
     * 岗位类别编码
     */
    @ExcelProperty(value = "类别编码")
    private String postCategory;

    /**
     * 显示顺序
     */
    @ExcelProperty(value = "岗位排序")
    private Integer postSort;

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

    /**
     * 部门名
     */
    @SerialMap(converter = SerialMapConstant.DEPT_ID_TO_NAME, source = "deptId")
    private String deptName;

}
