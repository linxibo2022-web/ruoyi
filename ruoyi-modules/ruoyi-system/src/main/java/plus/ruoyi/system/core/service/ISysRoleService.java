package plus.ruoyi.system.core.service;

import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.SysUserRole;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 角色业务层
 *
 * @author Lion Li
 */
public interface ISysRoleService {

    /**
     * 根据ID查询
     *
     * @param roleId 主键ID
     * @return 角色VO
     */
    SysRoleVo get(Long roleId);

    /**
     * 查询列表
     *
     * @param bo 业务对象
     * @return 角色VO列表
     */
    List<SysRoleVo> list(SysRoleBo bo);

    /**
     * 分页查询
     *
     * @param bo 业务对象
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysRoleVo> page(SysRoleBo bo, PageQuery pageQuery);

    /**
     * 批量删除
     *
     * @param ids 主键ID集合
     * @return 影响行数
     */
    int batchDelete(Collection<Long> ids);

    /**
     * 批量保存
     *
     * @param boList 业务对象列表
     * @return 影响行数
     */
    int batchSave(List<SysRoleBo> boList);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRoleVo> listRolesByUserId(Long userId);

    /**
     * 根据用户ID查询角色列表(包含被授权状态)
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRoleVo> listRolesWithAuthByUserId(Long userId);

    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> listRolePermissionsByUserId(Long userId);

    /**
     * 根据用户ID获取角色id列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    List<Long> listRoleIdsByUserId(Long userId);

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    SysRoleVo getRoleById(Long roleId);

    /**
     * 通过角色ID串查询角色
     *
     * @param roleIds 角色ID串
     * @return 角色列表信息
     */
    List<SysRoleVo> listRolesByIds(List<Long> roleIds);

    /**
     * 查询角色选项列表
     *
     * @return 角色列表
     */
    List<SysRoleVo> listRoleOptions();

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    boolean checkRoleNameUnique(SysRoleBo role);

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    boolean checkRoleKeyUnique(SysRoleBo role);

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    void checkRoleAllowed(SysRoleBo role);

    /**
     * 校验角色是否有数据权限
     *
     * @param roleId 角色id
     */
    void checkRoleDataScope(Long roleId);

    /**
     * 校验数据权限范围是否越权
     * 用户不能设置超出自己最高数据权限的范围
     *
     * @param dataScope 要设置的数据权限范围
     */
    void checkDataScopeLevel(String dataScope);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    long countUsersByRoleId(Long roleId);

    /**
     * 新增保存角色信息
     *
     * @param bo 角色信息
     * @return 结果
     */
    Long insertRole(SysRoleBo bo);

    /**
     * 修改保存角色信息
     *
     * @param bo 角色信息
     * @return 结果
     */
    boolean updateRole(SysRoleBo bo);

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param status 角色状态
     * @return 结果
     */
    boolean updateRoleStatus(Long roleId, String status);

    /**
     * 修改数据权限信息
     *
     * @param bo 角色信息
     * @return 结果
     */
    boolean authDataScope(SysRoleBo bo);

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    boolean deleteRoleById(Long roleId);

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    boolean deleteRoleByIds(Long[] roleIds);

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    boolean deleteAuthUser(SysUserRole userRole);

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    boolean deleteAuthUsers(Long roleId, Long[] userIds);

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    boolean insertAuthUsers(Long roleId, Long[] userIds);

    /**
     * 清除在线用户角色缓存
     *
     * @param roleId 角色ID
     */
    void cleanOnlineUserByRole(Long roleId);

    /**
     * 清除在线用户缓存
     *
     * @param userIds 用户ID列表
     */
    void cleanOnlineUser(List<Long> userIds);
}
