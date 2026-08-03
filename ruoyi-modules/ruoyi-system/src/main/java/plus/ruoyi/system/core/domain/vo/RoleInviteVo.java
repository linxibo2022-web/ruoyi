package plus.ruoyi.system.core.domain.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 角色邀请码 视图对象
 */
@Data
public class RoleInviteVo implements Serializable {

    /** 邀请码 */
    private String inviteCode;

    /** 邀请人所在租户 */
    private String tenantId;

    /** 邀请人用户ID */
    private Long userId;

    /** 邀请人姓名 */
    private String userName;

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 最大使用次数 */
    private Integer maxUseCount;

    /** 当前使用次数 */
    private Integer currentUseCount;

    /** 有效期时间戳 */
    private Long validUntil;

    /** 创建时间戳 */
    private Long createTime;

    /** 备注 */
    private String remark;

    /** 是否需要审核 */
    private Boolean needApproval;

    /** 邀请码状态 */
    private String inviteStatus;

    /** 剩余次数(动态计算) */
    private Integer remainingCount;

    /** 是否有效(动态计算) */
    private Boolean isValid;
}
