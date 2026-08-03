package plus.ruoyi.system.core.domain.bo;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建角色邀请 业务对象
 */
@Data
public class CreateRoleInviteBo {

    /** 角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 部门ID */
    @NotNull(message = "部门ID不能为空")
    private Long deptId;

    /** 有效期(小时) */
    @Min(value = 1, message = "有效期至少1小时")
    private Integer validHours;

    /** 最大使用次数 */
    @Min(value = -1, message = "使用次数不能小于-1")
    private Integer maxUseCount;

    /** 备注 */
    private String remark;

    /** 是否需要审核 */
    private Boolean needApproval;
}
