package plus.ruoyi.system.core.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 用户对象 sys_user
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends TenantEntity {

    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户性别
     */
    private String gender;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    @TableField(
        insertStrategy = FieldStrategy.NOT_EMPTY,
        updateStrategy = FieldStrategy.NOT_EMPTY,
        whereStrategy = FieldStrategy.NOT_EMPTY
    )
    private String password;

    /**
     * 帐号状态
     */
    private String status;

    /**
     * 是否删除
     */
    @TableLogic
    private String isDeleted;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 备注
     */
    private String remark;


    public SysUser(Long userId) {
        this.userId = userId;
    }

    public boolean isSuperAdmin() {
        return SystemConstants.SUPER_ADMIN_ID.equals(this.userId);
    }

}
