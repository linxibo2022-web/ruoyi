package plus.ruoyi.common.satoken.core.service;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.ObjectUtil;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PermissionService;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token权限管理实现类
 * <p>
 * 实现Sa-Token的StpInterface接口，提供权限和角色的查询功能
 * 支持两种查询模式：
 * 1. 当前登录用户权限查询：直接从LoginUser获取权限信息
 * 2. 跨用户权限查询：通过PermissionService查询其他用户权限（适配Sa-Token的API需求）
 * <p>
 * 应用场景：
 * - StpUtil.hasPermission() 权限验证
 * - StpUtil.hasRole() 角色验证
 * - 未登录接口的权限查询
 *
 * @author Lion Li
 */
public class SaPermissionImpl implements StpInterface {

    /**
     * 获取用户菜单权限列表
     * <p>
     * 支持两种查询方式：
     * 1. 当前用户权限：从登录用户信息中直接获取
     * 2. 跨用户权限查询：通过PermissionService查询指定用户权限
     *
     * @param loginId  登录ID，格式通常为 "authType:userId"
     * @param authType 认证类型
     * @return 菜单权限标识列表，如 ["system:user:query", "system:role:add"]
     * @throws ServiceException 当PermissionService实现类不存在时抛出
     */
    @Override
    public List<String> getPermissionList(Object loginId, String authType) {
        LoginUser loginUser = LoginHelper.getLoginUser();

        // 判断是否为跨用户权限查询（A用户查询B用户权限）
        if (ObjectUtil.isNull(loginUser) || !loginUser.getLoginId().equals(loginId)) {
            return getPermissionFromService(loginId);
        }

        // 当前用户权限查询
        return new ArrayList<>(loginUser.getMenuPermission());
    }

    /**
     * 获取用户角色权限列表
     * <p>
     * 支持两种查询方式：
     * 1. 当前用户角色：从登录用户信息中直接获取
     * 2. 跨用户角色查询：通过PermissionService查询指定用户角色
     *
     * @param loginId  登录ID，格式通常为 "authType:userId"
     * @param authType 认证类型
     * @return 角色标识列表，如 ["admin", "user", "guest"]
     * @throws ServiceException 当PermissionService实现类不存在时抛出
     */
    @Override
    public List<String> getRoleList(Object loginId, String authType) {
        LoginUser loginUser = LoginHelper.getLoginUser();

        // 判断是否为跨用户角色查询（A用户查询B用户角色）
        if (ObjectUtil.isNull(loginUser) || !loginUser.getLoginId().equals(loginId)) {
            return getRoleFromService(loginId);
        }

        // 当前用户角色查询
        return new ArrayList<>(loginUser.getRolePermission());
    }

    /**
     * 通过PermissionService查询指定用户的菜单权限
     *
     * @param loginId 登录ID，格式为 "authType:userId"
     * @return 菜单权限列表
     * @throws ServiceException 当PermissionService实现类不存在时抛出
     */
    private List<String> getPermissionFromService(Object loginId) {
        PermissionService permissionService = getPermissionService();
        if (ObjectUtil.isNotNull(permissionService)) {
            // 解析loginId获取真实的用户ID
            List<String> list = StringUtils.splitToList(loginId.toString(), ":");
            Long userId = Long.parseLong(list.get(1));
            return new ArrayList<>(permissionService.listMenuPermissions(userId));
        } else {
            throw ServiceException.of("PermissionService 实现类不存在");
        }
    }

    /**
     * 通过PermissionService查询指定用户的角色权限
     *
     * @param loginId 登录ID，格式为 "authType:userId"
     * @return 角色权限列表
     * @throws ServiceException 当PermissionService实现类不存在时抛出
     */
    private List<String> getRoleFromService(Object loginId) {
        PermissionService permissionService = getPermissionService();
        if (ObjectUtil.isNotNull(permissionService)) {
            // 解析loginId获取真实的用户ID
            List<String> list = StringUtils.splitToList(loginId.toString(), ":");
            Long userId = Long.parseLong(list.get(1));
            return new ArrayList<>(permissionService.listRolePermissions(userId));
        } else {
            throw ServiceException.of("PermissionService 实现类不存在");
        }
    }

    /**
     * 获取权限服务实例
     * <p>
     * 尝试从Spring容器中获取PermissionService实现类
     * 如果获取失败则返回null，由调用方处理异常情况
     *
     * @return PermissionService实例，获取失败返回null
     */
    private PermissionService getPermissionService() {
        try {
            return SpringUtils.getBean(PermissionService.class);
        } catch (Exception e) {
            return null;
        }
    }
}
