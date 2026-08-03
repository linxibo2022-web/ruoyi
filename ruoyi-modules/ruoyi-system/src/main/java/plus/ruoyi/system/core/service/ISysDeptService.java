package plus.ruoyi.system.core.service;

import cn.hutool.core.lang.tree.Tree;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.system.core.domain.bo.SysDeptBo;
import plus.ruoyi.system.core.domain.vo.SysDeptVo;

import java.util.Collection;
import java.util.List;

/**
 * 部门管理 服务层
 *
 * @author Lion Li
 */
public interface ISysDeptService {

    /**
     * 根据ID查询
     *
     * @param deptId 部门ID
     * @return 部门VO
     */
    SysDeptVo get(Long deptId);

    /**
     * 查询列表
     *
     * @param bo 业务对象
     * @return 部门VO列表
     */
    List<SysDeptVo> list(SysDeptBo bo);

    /**
     * 查询所有部门列表
     *
     * @return 部门VO列表
     */
    List<SysDeptVo> listAll();

    /**
     * 分页查询
     *
     * @param bo        业务对象
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SysDeptVo> page(SysDeptBo bo, PageQuery pageQuery);

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
    int batchSave(List<SysDeptBo> boList);

    /**
     * 查询部门树结构信息
     *
     * @param dept 部门信息
     * @return 部门树信息集合
     */
    List<Tree<Long>> getDeptTree(SysDeptBo dept);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    List<Tree<Long>> buildDeptTreeSelect(List<SysDeptVo> depts);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(Long roleId);

    /**
     * 通过部门ID串查询部门
     *
     * @param deptIds 部门id串
     * @return 部门列表信息
     */
    List<SysDeptVo> listNormalDeptsByIds(List<Long> deptIds);

    /**
     * 根据ID查询所有子部门数（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    long getNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在部门子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    boolean checkDeptNameUnique(SysDeptBo dept);

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
    void checkDeptDataScope(Long deptId);

    /**
     * 新增保存部门信息
     *
     * @param bo 部门信息
     * @return 结果
     */
    Long insertDept(SysDeptBo bo);

    /**
     * 修改保存部门信息
     *
     * @param bo 部门信息
     * @return 结果
     */
    boolean updateDept(SysDeptBo bo);

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 影响行数
     */
    int deleteDeptById(Long deptId);

    /**
     * 根据部门ID获取所有子部门ID列表（包含自身）
     *
     * @param deptId 部门ID
     * @return 子部门ID列表（包含传入的部门ID）
     */
    List<Long> getChildrenDeptIds(Long deptId);
}
