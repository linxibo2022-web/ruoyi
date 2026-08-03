package plus.ruoyi.system.core.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.system.core.domain.SysRoleDept;

import java.util.List;

/**
 * 角色部门关联数据访问接口
 *
 * @author Lion Li
 */
public interface ISysRoleDeptDao extends IBaseDao<SysRoleDept> {

    /**
     * 根据角色ID删除角色部门关联
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    int deleteByRoleId(Long roleId);

    /**
     * 根据角色ID列表批量删除角色部门关联
     *
     * @param roleIds 角色ID列表
     * @return 删除行数
     */
    int batchDeleteByRoleIds(List<Long> roleIds);

    /**
     * 批量插入角色部门关联
     *
     * @param list 角色部门关联列表
     * @return 是否成功
     */
    boolean batchInsertRoleDepts(List<SysRoleDept> list);

    /**
     * 根据角色ID查询角色部门关联列表
     *
     * @param roleId 角色ID
     * @return 角色部门关联列表
     */
    List<SysRoleDept> listByRoleId(Long roleId);

    /**
     * 根据角色ID列表查询角色部门关联列表
     *
     * @param roleIds 角色ID列表
     * @return 角色部门关联列表
     */
    List<SysRoleDept> listByRoleIds(List<Long> roleIds);
}
