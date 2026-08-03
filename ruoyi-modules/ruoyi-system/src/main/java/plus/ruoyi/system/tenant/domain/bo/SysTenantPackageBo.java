package plus.ruoyi.system.tenant.domain.bo;

import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import plus.ruoyi.common.mybatis.core.domain.BaseEntity;

/**
 * 租户套餐业务对象 sys_tenant_package
 *
 * @author Michelle.Chung
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysTenantPackage.class, reverseConvertGenerate = false)
public class SysTenantPackageBo extends BaseEntity {

    /**
     * 租户套餐id
     */
    @NotNull(message = "租户套餐id不能为空", groups = { EditGroup.class })
    private Long packageId;

    /**
     * 套餐名称
     */
    @NotBlank(message = "套餐名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String packageName;

    /**
     * 关联菜单id
     */
    @NotBlank(message = "关联菜单id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String menuIds;

    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单树选择项是否关联显示
     */
    private Boolean menuCheckStrictly;

    /**
     * 状态
     */
    private String status;


}
