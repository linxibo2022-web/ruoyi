package plus.ruoyi.system.core.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.dao.ISysRoleMenuDao;
import plus.ruoyi.system.core.domain.SysRoleMenu;
import plus.ruoyi.system.core.mapper.SysRoleMenuMapper;

import java.util.List;

/**
 * 角色菜单关联数据访问实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Repository
public class SysRoleMenuDaoImpl extends BaseDaoImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuDao {

    /**
     * 根据角色ID删除角色菜单关联
     */
    @Override
    public int deleteByRoleId(Long roleId) {
        PlusLambdaQuery<SysRoleMenu> lqw = PlusLambdaQuery.of(SysRoleMenu.class)
            .eq(SysRoleMenu::getRoleId, roleId);
        return baseMapper.delete(lqw);
    }

    /**
     * 根据角色ID列表批量删除角色菜单关联
     */
    @Override
    public int batchDeleteByRoleIds(List<Long> roleIds) {
        PlusLambdaQuery<SysRoleMenu> lqw = PlusLambdaQuery.of(SysRoleMenu.class)
            .in(SysRoleMenu::getRoleId, roleIds);
        return baseMapper.delete(lqw);
    }

    /**
     * 批量插入角色菜单关联
     * 使用batchInsert而不是batchSave，避免因为没有主键导致的插入变更新问题
     */
    @Override
    public boolean batchInsertRoleMenus(List<SysRoleMenu> list) {
        return batchInsert(list) > 0;
    }

    /**
     * 根据菜单ID统计被角色关联的数量
     */
    @Override
    public long countByMenuId(Long menuId) {
        PlusLambdaQuery<SysRoleMenu> lqw = PlusLambdaQuery.of(SysRoleMenu.class)
            .eq(SysRoleMenu::getMenuId, menuId);
        return count(lqw);
    }

    /**
     * 根据角色ID列表查询角色菜单关联列表
     */
    @Override
    public List<SysRoleMenu> listByRoleIds(List<Long> roleIds) {
        PlusLambdaQuery<SysRoleMenu> lqw = PlusLambdaQuery.of(SysRoleMenu.class)
            .in(SysRoleMenu::getRoleId, roleIds);
        return list(lqw);
    }

    /**
     * 删除指定角色的角色菜单关联，但排除指定的菜单ID
     */
    @Override
    public int deleteByRoleIdsExcludeMenuIds(List<Long> roleIds, List<Long> excludeMenuIds) {
        PlusLambdaQuery<SysRoleMenu> lqw = PlusLambdaQuery.of(SysRoleMenu.class)
            .in(SysRoleMenu::getRoleId, roleIds)
            .notIn(SysRoleMenu::getMenuId, excludeMenuIds);
        return baseMapper.delete(lqw);
    }
}
