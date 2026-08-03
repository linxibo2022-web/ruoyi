package plus.ruoyi.system.core.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.annotation.DataColumn;
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusQuery;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.mapper.SysRoleMapper;

import java.util.List;
import java.util.Map;

/**
 * 角色数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysRoleDaoImpl extends BaseDaoImpl<SysRoleMapper, SysRole> implements ISysRoleDao {

    /**
     * 创建PlusQuery查询构造器(支持表别名)
     */
    protected PlusQuery<SysRole> query() {
        return PlusQuery.of(SysRole.class);
    }

    /**
     * 构建查询条件（不带表别名，用于普通查询）
     */
    @Override
    public PlusLambdaQuery<SysRole> buildQueryWrapper(SysRoleBo bo) {
        Map<String, Object> params = bo.getParams();
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class);
        lqw.eq(SysRole::getRoleId, bo.getRoleId())
            .like(SysRole::getRoleName, bo.getRoleName())
            .eq(SysRole::getStatus, bo.getStatus())
            .like(SysRole::getRoleKey, bo.getRoleKey())
            .between(SysRole::getCreateTime, params.get("beginTime"), params.get("endTime"))
            .orderByAsc(SysRole::getRoleSort, SysRole::getCreateTime);

        // 模糊查询（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w.like(SysRole::getRoleName, searchValue)     // String
                .or().like(SysRole::getRoleKey, searchValue)           // String
                .or().likeCast(SysRole::getRoleId, searchValue));      // Long
        }
        return lqw;
    }

    // ==================== 带数据权限的通用方法（重写父类方法） ====================

    /**
     * 条件查询列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     * <p>
     * 数据权限说明：
     * - deptName -> create_dept: 限制只能查看自己部门创建的角色
     * - userName -> create_by: 限制只能查看自己创建的角色
     *
     * @param wrapper 查询条件
     * @return 角色列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    @Override
    public List<SysRole> list(PlusLambdaQuery<SysRole> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页查询（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     * <p>
     * 数据权限说明：
     * - deptName -> create_dept: 限制只能查看自己部门创建的角色
     * - userName -> create_by: 限制只能查看自己创建的角色
     *
     * @param wrapper   查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    @Override
    public PageResult<SysRole> page(PlusLambdaQuery<SysRole> wrapper, PageQuery pageQuery) {
        return PageResult.of(baseMapper.selectPage(pageQuery.build(), wrapper));
    }

    // ==================== 业务方法 ====================

    /**
     * 根据用户ID查询角色列表
     */
    @Override
    public List<SysRole> listRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }


    /**
     * 根据角色ID列表查询正常状态的角色（带数据权限）
     */
    @Override
    public List<SysRole> listNormalRolesByIds(List<Long> roleIds) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getStatus, DictEnableStatus.ENABLE.getValue())
            .in(SysRole::getRoleId, roleIds);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 查询正常状态的角色列表
     * <p>
     * 注意：此方法会受数据权限限制（通过重写的list方法），用于角色选项等场景
     */
    @Override
    public List<SysRole> listNormalRoles() {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getStatus, DictEnableStatus.ENABLE.getValue())
            .orderByAsc(SysRole::getRoleSort);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 校验角色名称的唯一性
     */
    @Override
    public boolean checkRoleNameUnique(String roleName, Long roleId) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getRoleName, roleName)
            .ne(SysRole::getRoleId, roleId);
        return !exists(lqw);
    }

    /**
     * 校验角色权限标识的唯一性
     */
    @Override
    public boolean checkRoleKeyUnique(String roleKey, Long roleId) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getRoleKey, roleKey)
            .ne(SysRole::getRoleId, roleId);
        return !exists(lqw);
    }

    /**
     * 统计角色数量(带数据权限)
     * <p>
     * 数据权限说明:
     * - deptName -> create_dept: 限制只能查看自己部门创建的角色
     * - userName -> create_by: 限制只能查看自己创建的角色
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    @Override
    public long countRoleById(Long roleId) {
        return count(PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getRoleId, roleId));
    }

    /**
     * 根据租户ID查询角色列表（带数据权限）
     */
    @Override
    public List<SysRole> listByTenantId(String tenantId) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getTenantId, tenantId);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 根据角色权限标识查询角色
     */
    @Override
    public SysRole getByRoleKey(String roleKey) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getRoleKey, roleKey);
        return getOne(lqw, false);
    }

    /**
     * 根据角色ID列表查询角色名称映射（带数据权限）
     */
    @Override
    public List<SysRole> listRoleNamesById(List<Long> roleIds) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .select(SysRole::getRoleId, SysRole::getRoleName)
            .in(SysRole::getRoleId, roleIds);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 根据角色ID查询角色（使用联表查询）
     */
    @Override
    public SysRole getRoleByIdWithJoin(Long roleId) {
        return baseMapper.selectRoleById(roleId);
    }

    /**
     * 根据角色ID更新角色状态
     */
    @Override
    public boolean updateRoleStatus(Long roleId, String status) {
        return lambdaUpdate()
            .set(SysRole::getStatus, status)
            .eq(SysRole::getRoleId, roleId)
            .update() > 0;
    }

    /**
     * 根据角色ID列表查询角色列表（带数据权限）
     */
    @Override
    public List<SysRole> listByRoleIds(List<Long> roleIds) {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .in(SysRole::getRoleId, roleIds);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 查询默认租户的普通业务角色（带数据权限）
     * (排除租户管理员角色和超管角色)
     */
    @Override
    public List<SysRole> listDefaultTenantNormalRoles() {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .eq(SysRole::getTenantId, TenantConstants.DEFAULT_TENANT_ID)
            .ne(SysRole::getRoleKey, TenantConstants.TENANT_ADMIN_ROLE_KEY)
            .ne(SysRole::getRoleKey, TenantConstants.SUPER_ADMIN_ROLE_KEY);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 查询租户的普通业务角色（带数据权限）
     * (排除租户管理员角色和超管角色)
     */
    @Override
    public List<SysRole> listTenantNormalRoles() {
        PlusLambdaQuery<SysRole> lqw = PlusLambdaQuery.of(SysRole.class)
            .ne(SysRole::getRoleKey, TenantConstants.TENANT_ADMIN_ROLE_KEY)
            .ne(SysRole::getRoleKey, TenantConstants.SUPER_ADMIN_ROLE_KEY);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

}
