package plus.ruoyi.system.core.dao;

import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.domain.SysDept;
import plus.ruoyi.system.core.domain.bo.SysDeptBo;

import java.util.List;

/**
 * 部门管理DAO接口
 *
 * @author Lion Li
 */
public interface ISysDeptDao extends IBaseDao<SysDept> {

    /**
     * 根据业务对象构建查询条件
     *
     * @param bo 业务对象
     * @return 查询条件
     */
    PlusLambdaQuery<SysDept> buildQueryWrapper(SysDeptBo bo);

    /**
     * 根据部门ID列表查询正常状态的部门
     *
     * @param deptIds 部门ID列表
     * @return 部门列表
     */
    List<SysDept> listNormalDeptsByIds(List<Long> deptIds);

    /**
     * 根据部门ID查询所有子部门数量（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数量
     */
    long countNormalChildrenByDeptId(Long deptId);

    /**
     * 检查是否存在子部门
     *
     * @param deptId 部门ID
     * @return 是否存在子部门
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 校验部门名称在同级部门中的唯一性
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @param deptId   部门ID（用于排除自身）
     * @return 是否唯一
     */
    boolean checkDeptNameUnique(String deptName, Long parentId, Long deptId);

    /**
     * 统计指定部门ID的部门数量（数据权限）
     *
     * @param deptId 部门ID
     * @return 部门数量
     */
    long countDeptById(Long deptId);

    /**
     * 查询所有正常状态的部门
     *
     * @return 部门列表
     */
    List<SysDept> listNormalDepts();

    /**
     * 查询所有子部门（包括孙子部门等）
     *
     * @param deptId 部门ID
     * @return 子部门列表
     */
    List<SysDept> listChildrenByDeptId(Long deptId);

    /**
     * 查询所有正常状态的子部门ID列表（包含自身）
     *
     * @param deptId 部门ID
     * @return 子部门ID列表
     */
    List<Long> listChildrenDeptIds(Long deptId);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(Long roleId, boolean deptCheckStrictly);

    /**
     * 批量启用父部门状态
     *
     * @param parentDeptIds 父部门ID数组
     * @return 更新数量
     */
    int batchEnableParentDepts(Long[] parentDeptIds);

    /**
     * 根据部门ID列表查询部门名称映射
     *
     * @param deptIds 部门ID列表
     * @return 部门列表
     */
    List<SysDept> listDeptNamesById(List<Long> deptIds);

    /**
     * 根据部门ID列表查询部门列表
     *
     * @param deptIds 部门ID列表
     * @return 部门列表
     */
    List<SysDept> listByDeptIds(List<Long> deptIds);

    /**
     * 根据部门ID查询部门名称
     *
     * @param deptId 部门ID
     * @return 部门名称
     */
    String getDeptNameById(Long deptId);
}
