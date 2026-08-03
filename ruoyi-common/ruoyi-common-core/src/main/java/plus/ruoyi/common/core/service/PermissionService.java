package plus.ruoyi.common.core.service;

import java.util.Set;

/**
 * 用户权限处理
 *
 * @author Lion Li
 */
public interface PermissionService {

    /**
     * 获取角色数据权限列表
     *
     * @param userId  用户id
     * @return 角色权限信息
     */
    Set<String> listRolePermissions(Long userId);

    /**
     * 获取菜单数据权限列表
     *
     * @param userId  用户id
     * @return 菜单权限信息
     */
    Set<String> listMenuPermissions(Long userId);

}
