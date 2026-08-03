package plus.ruoyi.system.core.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;

import java.util.List;

/**
 * 角色数据访问层
 * <p>
 * 数据权限说明：
 * - list() 和 page() 方法默认带数据权限过滤
 * - 需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
 *
 * @author Lion Li
 */
public interface ISysRoleDao extends IBaseDao<SysRole> {

    /**
     * 构建查询条件
     *
     * @param bo 查询条件
     * @return 查询构造器
     */
    PlusLambdaQuery<SysRole> buildQueryWrapper(SysRoleBo bo);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> listRolesByUserId(Long userId);

    /**
     * 根据角色ID列表查询正常状态的角色
     *
     * @param roleIds 角色ID列表
     * @return 角色列表
     */
    List<SysRole> listNormalRolesByIds(List<Long> roleIds);

    /**
     * 查询正常状态的角色列表
     * <p>
     * 此方法通过重写的list()方法自动带数据权限
     *
     * @return 角色列表
     */
    List<SysRole> listNormalRoles();

    /**
     * 校验角色名称的唯一性
     *
     * @param roleName 角色名称
     * @param roleId 角色ID(排除自己)
     * @return true表示唯一
     */
    boolean checkRoleNameUnique(String roleName, Long roleId);

    /**
     * 校验角色权限标识的唯一性
     *
     * @param roleKey 角色权限标识
     * @param roleId 角色ID(排除自己)
     * @return true表示唯一
     */
    boolean checkRoleKeyUnique(String roleKey, Long roleId);

    /**
     * 统计角色数量(带数据权限)
     *
     * @param roleId 角色ID
     * @return 数量
     */
    long countRoleById(Long roleId);

    /**
     * 根据租户ID查询角色列表
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<SysRole> listByTenantId(String tenantId);

    /**
     * 根据角色权限标识查询角色
     *
     * @param roleKey 角色权限标识
     * @return 角色信息
     */
    SysRole getByRoleKey(String roleKey);

    /**
     * 根据角色ID列表查询角色名称映射
     *
     * @param roleIds 角色ID列表
     * @return 角色列表
     */
    List<SysRole> listRoleNamesById(List<Long> roleIds);

    /**
     * 根据角色ID查询角色（使用联表查询）
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    SysRole getRoleByIdWithJoin(Long roleId);

    /**
     * 根据角色ID更新角色状态
     *
     * @param roleId 角色ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateRoleStatus(Long roleId, String status);

    /**
     * 根据角色ID列表查询角色列表
     *
     * @param roleIds 角色ID列表
     * @return 角色列表
     */
    List<SysRole> listByRoleIds(List<Long> roleIds);

    /**
     * 查询默认租户的普通业务角色
     * (排除租户管理员角色和超管角色)
     *
     * @return 角色列表
     */
    List<SysRole> listDefaultTenantNormalRoles();

    /**
     * 查询租户的普通业务角色
     * (排除租户管理员角色和超管角色)
     *
     * @return 角色列表
     */
    List<SysRole> listTenantNormalRoles();
}
