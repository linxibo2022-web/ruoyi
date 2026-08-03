package plus.ruoyi.system.core.domain.bo;

import lombok.Data;

/**
 * 角色邀请查询 业务对象
 */
@Data
public class RoleInviteQueryBo {

    /** 角色ID(可选) */
    private Long roleId;

    /** 状态过滤(可选): valid-有效, expired-过期, exhausted-用尽 */
    private String status;
}
