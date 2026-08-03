package plus.ruoyi.system.core.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.core.domain.SysUserRole;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.system.core.service.ISysRoleService;
import plus.ruoyi.system.core.service.ISysUserService;

/**
 * 用户角色授权
 *
 * @author 抓蛙师
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/userRole")
public class SysUserRoleController {

    /* 业务服务 */
    private final ISysRoleService roleService;
    private final ISysUserService userService;

    /**
     * 分页查询角色已授权用户列表
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/pageRoleAuthorizedUsers")
    public R<PageResult<SysUserVo>> pageRoleAuthorizedUsers(SysUserBo user, PageQuery pageQuery) {
        return R.ok(userService.pageRoleAuthorizedUsers(user, pageQuery));
    }

    /**
     * 查询角色未授权用户列表
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/pageRoleUnauthorizedUsers")
    public R<PageResult<SysUserVo>> pageRoleUnauthorizedUsers(SysUserBo user, PageQuery pageQuery) {
        return R.ok(userService.pageRoleUnauthorizedUsers(user, pageQuery));
    }

    /**
     * 撤销用户角色
     */
    @SaCheckPermission("system:role:update")
    @Log(title = "角色管理", operType = DictOperType.GRANT)
    @PutMapping("/revokeUserRole")
    public R<Void> revokeUserRole(@RequestBody SysUserRole userRole) {
        roleService.checkRoleDataScope(userRole.getRoleId());
        return R.status(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量撤销用户角色
     *
     * @param roleId  角色ID
     * @param userIds 用户ID串
     */
    @SaCheckPermission("system:role:update")
    @Log(title = "角色管理", operType = DictOperType.GRANT)
    @PutMapping("/batchRevokeUserRoles")
    public R<Void> batchRevokeUserRoles(Long roleId, Long[] userIds) {
        roleService.checkRoleDataScope(roleId);
        return R.status(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 用户ID串
     */
    @SaCheckPermission("system:role:update")
    @Log(title = "角色管理", operType = DictOperType.GRANT)
    @PutMapping("/batchGrantUserRoles")
    public R<Void> batchGrantUserRoles(Long roleId, Long[] userIds) {
        roleService.checkRoleDataScope(roleId);
        for (Long userId : userIds) {
            userService.checkUserDataScope(userId);
        }
        return R.status(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户Id
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = DictOperType.GRANT)
    @PutMapping("/assignUserRoles")
    public R<Void> assignUserRoles(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        userService.assignUserRoles(userId, List.of(roleIds));
        return R.ok();
    }
}
