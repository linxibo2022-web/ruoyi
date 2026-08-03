package plus.ruoyi.system.core.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictUserGender;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户对象导入VO
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
// @Accessors(chain = true) // 导入不允许使用 会找不到set方法
public class SysUserImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户序号")
    private Long userId;

    /**
     * 部门ID
     */
    @ExcelProperty(value = "部门编号")
    private Long deptId;

    /**
     * 用户账号
     */
    @ExcelProperty(value = "登录名称")
    private String userName;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "用户名称")
    private String nickName;

    /**
     * 用户邮箱
     */
    @ExcelProperty(value = "用户邮箱")
    private String email;

    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号码")
    private String phone;

    /**
     * 用户性别
     */
    @ExcelProperty(value = "用户性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictUserGender.DICT_TYPE)
    private String gender;

    /**
     * 帐号状态
     */
    @ExcelProperty(value = "帐号状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;

}
