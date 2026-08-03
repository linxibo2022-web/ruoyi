package plus.ruoyi.system.core.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysUserRoleDao;
import plus.ruoyi.system.core.domain.SysUserRole;
import plus.ruoyi.system.core.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.Set;

/**
 * 用户角色关联数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysUserRoleDaoImpl extends BaseDaoImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleDao {

    /**
     * 根据角色ID查询关联的用户ID列表
     */
    @Override
    public List<Long> listUserIdsByRoleId(Long roleId) {
        return baseMapper.selectUserIdsByRoleId(roleId);
    }

    /**
     * 删除用户角色关联
     */
    @Override
    public int deleteUserRole(Long roleId, Long userId) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .eq(SysUserRole::getRoleId, roleId)
            .eq(SysUserRole::getUserId, userId);
        return baseMapper.delete(lqw);
    }

    /**
     * 批量删除用户角色关联
     */
    @Override
    public int batchDeleteUserRoles(Long roleId, List<Long> userIds) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .eq(SysUserRole::getRoleId, roleId)
            .in(SysUserRole::getUserId, userIds);
        return baseMapper.delete(lqw);
    }

    /**
     * 批量插入用户角色关联
     * 使用batchInsert而不是batchSave，避免因为没有主键导致的插入变更新问题
     */
    @Override
    public boolean batchInsertUserRoles(List<SysUserRole> list) {
        return batchInsert(list) > 0;
    }

    /**
     * 根据用户ID删除用户角色关联
     */
    @Override
    public int deleteByUserId(Long userId) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .eq(SysUserRole::getUserId, userId);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据用户ID和角色ID集合删除用户角色关联
     * 只删除指定角色范围内的关联，保留其他角色
     */
    @Override
    public int deleteByUserIdAndRoleIds(Long userId, Set<Long> roleIds) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .eq(SysUserRole::getUserId, userId)
            .in(SysUserRole::getRoleId, roleIds);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据用户ID列表批量删除用户角色关联
     */
    @Override
    public int batchDeleteByUserIds(List<Long> userIds) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .in(SysUserRole::getUserId, userIds);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据角色ID列表查询用户角色关联
     */
    @Override
    public List<SysUserRole> listByRoleIds(List<Long> roleIds) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .in(SysUserRole::getRoleId, roleIds);
        return list(lqw);
    }

    /**
     * 根据角色ID统计用户数量
     */
    @Override
    public Long countUsersByRoleId(Long roleId) {
        PlusLambdaQuery<SysUserRole> lqw = PlusLambdaQuery.of(SysUserRole.class)
            .eq(SysUserRole::getRoleId, roleId);
        return count(lqw);
    }
}
