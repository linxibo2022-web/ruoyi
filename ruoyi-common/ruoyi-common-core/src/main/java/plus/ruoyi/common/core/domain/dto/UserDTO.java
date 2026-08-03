package plus.ruoyi.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 用户
 *
 * @author Michelle.Chung
 */
@Data
@NoArgsConstructor
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
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
     * 帐号状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;

}
