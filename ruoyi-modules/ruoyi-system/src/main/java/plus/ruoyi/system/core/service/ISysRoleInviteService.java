package plus.ruoyi.system.core.service;

import plus.ruoyi.system.core.domain.bo.CreateRoleInviteBo;
import plus.ruoyi.system.core.domain.bo.RoleInviteQueryBo;
import plus.ruoyi.system.core.domain.vo.RoleInviteVo;
import java.util.List;

/**
 * 角色邀请服务接口
 */
public interface ISysRoleInviteService {

    /**
     * 创建角色邀请码
     */
    RoleInviteVo createRoleInvite(CreateRoleInviteBo bo);

    /**
     * 获取用户的邀请码列表
     */
    List<RoleInviteVo> listRoleInvites(RoleInviteQueryBo queryBo);

    /**
     * 验证邀请码有效性
     */
    RoleInviteVo validateRoleInvite(String inviteCode);

    /**
     * 使用邀请码(注册时调用)
     */
    boolean useRoleInvite(String inviteCode);

    /**
     * 删除邀请码
     */
    boolean deleteRoleInvite(String inviteCode);

    /**
     * 检查邀请码是否属于当前用户
     */
    boolean checkRoleInviteOwnership(String inviteCode);
}
