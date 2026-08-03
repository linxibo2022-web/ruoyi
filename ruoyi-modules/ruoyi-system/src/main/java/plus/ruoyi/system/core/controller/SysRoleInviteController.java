package plus.ruoyi.system.core.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.log.annotation.Log;

import plus.ruoyi.system.core.domain.bo.CreateRoleInviteBo;
import plus.ruoyi.system.core.domain.bo.RoleInviteQueryBo;
import plus.ruoyi.system.core.domain.vo.RoleInviteVo;
import plus.ruoyi.system.core.service.ISysRoleInviteService;

/**
 * 角色邀请控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/role")
public class SysRoleInviteController {

    /* 业务服务 */
    private final ISysRoleInviteService roleInviteService;

    /**
     * 创建角色邀请码
     */
    @PostMapping("/createRoleInvite")
    @SaCheckPermission("system:role:update")
    @Log(title = "角色邀请", operType = DictOperType.INSERT)
    public R<RoleInviteVo> createRoleInvite(@Validated @RequestBody CreateRoleInviteBo bo) {
        RoleInviteVo result = roleInviteService.createRoleInvite(bo);
        return R.ok(result);
    }

    /**
     * 获取角色邀请码列表
     */
    @GetMapping("/listRoleInvites")
    @SaCheckPermission("system:role:update")
    public R<List<RoleInviteVo>> listRoleInvites(RoleInviteQueryBo queryBo) {
        List<RoleInviteVo> result = roleInviteService.listRoleInvites(queryBo);
        return R.ok(result);
    }

    /**
     * 验证角色邀请码(注册时调用，无需权限)
     */
    @SaIgnore
    @GetMapping("/validateRoleInvite/{inviteCode}")
    public R<RoleInviteVo> validateRoleInvite(@PathVariable String inviteCode) {
        RoleInviteVo result = roleInviteService.validateRoleInvite(inviteCode);
        return R.ok(result);
    }

    /**
     * 删除角色邀请码
     */
    @DeleteMapping("/deleteRoleInvite/{inviteCode}")
    @SaCheckPermission("system:role:update")
    @Log(title = "角色邀请", operType = DictOperType.DELETE)
    public R<Void> deleteRoleInvite(@PathVariable String inviteCode) {
        boolean success = roleInviteService.deleteRoleInvite(inviteCode);
        return success ? R.ok() : R.fail("删除失败");
    }
}
