package plus.ruoyi.system.core.service;

import plus.ruoyi.common.core.service.PermissionService;

import java.util.Set;

/**
 * 用户权限处理
 * <p>
 * 与 {@link PermissionService} 接口相同
 * 不过本服务是服务于系统模块.PermissionService是通用服务
 *
 * @author Lion Li
 */
public interface ISysPermissionService {

    /**
     * 获取角色数据权限列表
     *
     * @param userId 用户id
     * @return 角色权限信息
     */
    Set<String> listRolePermissions(Long userId);

    /**
     * 获取菜单数据权限列表
     *
     * @param userId 用户id
     * @return 菜单权限信息
     */
    Set<String> listMenuPermissions(Long userId);

}
