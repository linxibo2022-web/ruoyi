package plus.ruoyi.system.core.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysRoleDeptDao;
import plus.ruoyi.system.core.domain.SysRoleDept;
import plus.ruoyi.system.core.mapper.SysRoleDeptMapper;

import java.util.List;

/**
 * 角色部门关联数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysRoleDeptDaoImpl extends BaseDaoImpl<SysRoleDeptMapper, SysRoleDept> implements ISysRoleDeptDao {

    /**
     * 根据角色ID删除角色部门关联
     */
    @Override
    public int deleteByRoleId(Long roleId) {
        PlusLambdaQuery<SysRoleDept> lqw = PlusLambdaQuery.of(SysRoleDept.class)
            .eq(SysRoleDept::getRoleId, roleId);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据角色ID列表批量删除角色部门关联
     */
    @Override
    public int batchDeleteByRoleIds(List<Long> roleIds) {
        PlusLambdaQuery<SysRoleDept> lqw = PlusLambdaQuery.of(SysRoleDept.class)
            .in(SysRoleDept::getRoleId, roleIds);
        return baseMapper.delete(lqw);
    }

    /**
     * 批量插入角色部门关联
     * 使用batchInsert而不是batchSave，避免因为没有主键导致的插入变更新问题
     */
    @Override
    public boolean batchInsertRoleDepts(List<SysRoleDept> list) {
        return batchInsert(list) > 0;
    }

    /**
     * 根据角色ID查询角色部门关联列表
     */
    @Override
    public List<SysRoleDept> listByRoleId(Long roleId) {
        PlusLambdaQuery<SysRoleDept> lqw = PlusLambdaQuery.of(SysRoleDept.class)
            .select(SysRoleDept::getDeptId)
            .eq(SysRoleDept::getRoleId, roleId);
        return list(lqw);
    }

    /**
     * 根据角色ID列表查询角色部门关联列表
     */
    @Override
    public List<SysRoleDept> listByRoleIds(List<Long> roleIds) {
        PlusLambdaQuery<SysRoleDept> lqw = PlusLambdaQuery.of(SysRoleDept.class)
            .in(SysRoleDept::getRoleId, roleIds);
        return list(lqw);
    }
}
