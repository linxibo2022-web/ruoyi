package plus.ruoyi.system.tenant.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 租户套餐视图对象 sys_tenant_package
 *
 * @author Michelle.Chung
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysTenantPackage.class)
public class SysTenantPackageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户套餐id
     */
    @ExcelProperty(value = "租户套餐id")
    private Long packageId;

    /**
     * 套餐名称
     */
    @ExcelProperty(value = "套餐名称")
    private String packageName;

    /**
     * 关联菜单id
     */
    @ExcelProperty(value = "关联菜单id")
    private String menuIds;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 菜单树选择项是否关联显示
     */
    @ExcelProperty(value = "菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;


}
