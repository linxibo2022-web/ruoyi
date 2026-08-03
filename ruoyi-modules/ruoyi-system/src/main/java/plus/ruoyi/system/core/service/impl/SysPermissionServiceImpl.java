package plus.ruoyi.system.core.service.impl;

import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.service.PermissionService;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.service.ISysMenuService;
import plus.ruoyi.system.core.service.ISysPermissionService;
import plus.ruoyi.system.core.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户权限Service业务层处理
 * <p>
 * 负责处理用户权限相关的核心业务逻辑，为权限控制和认证授权提供数据支持：
 *
 * @author ruoyi
 */
@RequiredArgsConstructor
@Service
public class SysPermissionServiceImpl implements ISysPermissionService, PermissionService {

    /**
     * 角色管理Service
     */
    private final ISysRoleService roleService;

    /**
     * 菜单管理Service
     */
    private final ISysMenuService menuService;

    // ================ 角色权限相关方法 =================

    /**
     * 获取用户角色权限标识集合
     * <p>
     *
     * @param userId 用户ID，不能为null
     * @return 角色权限标识集合，如："admin", "common"等
     */
    @Override
    public Set<String> listRolePermissions(Long userId) {
        Set<String> roles = new HashSet<>();

        // 超级管理员拥有所有权限
        if (LoginHelper.isSuperAdmin(userId)) {
            roles.add(TenantConstants.SUPER_ADMIN_ROLE_KEY);
        } else {
            // 查询用户实际分配的角色权限
            roles.addAll(roleService.listRolePermissionsByUserId(userId));
        }

        return roles;
    }

    // ================ 菜单权限相关方法 =================

    /**
     * 获取用户菜单权限标识集合
     * <p>
     *
     * @param userId 用户ID，不能为null
     * @return 菜单权限标识集合，如："system:user:list", "system:role:add"等
     */
    @Override
    public Set<String> listMenuPermissions(Long userId) {
        Set<String> perms = new HashSet<>();

        // 超级管理员拥有所有权限
        if (LoginHelper.isSuperAdmin(userId)) {
            perms.add("*:*:*");
        } else {
            // 查询用户实际分配的菜单权限
            perms.addAll(menuService.listMenuPermissionsByUserId(userId));
        }

        return perms;
    }
}
