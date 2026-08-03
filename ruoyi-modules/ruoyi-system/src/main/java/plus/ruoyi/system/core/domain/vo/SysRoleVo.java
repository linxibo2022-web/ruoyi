package plus.ruoyi.system.core.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.common.mybatis.enums.DataScopeType;
import plus.ruoyi.system.core.domain.SysRole;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 角色信息视图对象 sys_role
 *
 * @author Michelle.Chung
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysRole.class)
public class SysRoleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @ExcelProperty(value = "角色序号")
    private Long roleId;

    /**
     * 角色名称
     */
    @ExcelProperty(value = "角色名称")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @ExcelProperty(value = "角色权限")
    private String roleKey;

    /**
     * 显示顺序
     */
    @ExcelProperty(value = "角色排序")
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）
     */
    @ExcelProperty(value = "数据范围", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DataScopeType.DICT_TYPE)
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    @ExcelProperty(value = "菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    @ExcelProperty(value = "部门树选择项是否关联显示")
    private Boolean deptCheckStrictly;

    /**
     * 角色状态
     */
    @ExcelProperty(value = "角色状态", converter = ExcelDictConvert.class)
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
     * 用户是否存在此角色标识 默认不存在
     */
    private boolean flag = false;

    /**
     * 是否禁用（当前操作者无权限操作此角色时为true）
     * 用于前端下拉框禁用显示
     */
    private boolean disabled = false;

    public boolean isSuperAdmin() {
        return SystemConstants.SUPER_ADMIN_ID.equals(this.roleId);
    }

}
