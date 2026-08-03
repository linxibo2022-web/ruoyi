package plus.ruoyi.system.core.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.system.core.domain.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单关联数据访问接口
 *
 * @author Lion Li
 */
public interface ISysRoleMenuDao extends IBaseDao<SysRoleMenu> {

    /**
     * 根据角色ID删除角色菜单关联
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    int deleteByRoleId(Long roleId);

    /**
     * 根据角色ID列表批量删除角色菜单关联
     *
     * @param roleIds 角色ID列表
     * @return 删除行数
     */
    int batchDeleteByRoleIds(List<Long> roleIds);

    /**
     * 批量插入角色菜单关联
     *
     * @param list 角色菜单关联列表
     * @return 是否成功
     */
    boolean batchInsertRoleMenus(List<SysRoleMenu> list);

    /**
     * 根据菜单ID统计被角色关联的数量
     *
     * @param menuId 菜单ID
     * @return 关联数量
     */
    long countByMenuId(Long menuId);

    /**
     * 根据角色ID列表查询角色菜单关联列表
     *
     * @param roleIds 角色ID列表
     * @return 角色菜单关联列表
     */
    List<SysRoleMenu> listByRoleIds(List<Long> roleIds);

    /**
     * 删除指定角色的角色菜单关联，但排除指定的菜单ID
     *
     * @param roleIds       角色ID列表
     * @param excludeMenuIds 需要排除的菜单ID列表
     * @return 删除行数
     */
    int deleteByRoleIdsExcludeMenuIds(List<Long> roleIds, List<Long> excludeMenuIds);
}
