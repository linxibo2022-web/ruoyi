package plus.ruoyi.system.core.dao.impl;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.annotation.DataColumn;
import plus.ruoyi.common.mybatis.annotation.DataPermission;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.helper.DataBaseHelper;
import plus.ruoyi.system.core.dao.ISysDeptDao;
import plus.ruoyi.system.core.domain.SysDept;
import plus.ruoyi.system.core.domain.bo.SysDeptBo;
import plus.ruoyi.system.core.mapper.SysDeptMapper;

import java.util.List;

/**
 * 部门管理数据访问实现
 *
 * @author Lion Li
 */
@Repository
public class SysDeptDaoImpl extends BaseDaoImpl<SysDeptMapper, SysDept>
    implements ISysDeptDao {

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysDept> buildQueryWrapper(SysDeptBo bo) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class);

        // 基础过滤条件：排除已删除数据
        lqw.eq(SysDept::getIsDeleted, DictBooleanFlag.NO.getValue());

        // 精确匹配条件
        lqw.eq(SysDept::getDeptId, bo.getDeptId());
        lqw.eq(SysDept::getParentId, bo.getParentId());
        lqw.eq(SysDept::getStatus, bo.getStatus());

        // 模糊匹配条件
        lqw.like(SysDept::getDeptName, bo.getDeptName());
        lqw.like(SysDept::getDeptCategory, bo.getDeptCategory());
        lqw.like(SysDept::getAreaCode, bo.getAreaCode());

        // 排序规则：确保树形结构的正确显示顺序
        lqw.orderByAsc(SysDept::getAncestors);
        lqw.orderByAsc(SysDept::getParentId);
        lqw.orderByAsc(SysDept::getOrderNum);
        lqw.orderByAsc(SysDept::getDeptId);

        // 部门树搜索：查询指定部门及其所有子部门
        if (ObjectUtil.isNotNull(bo.getBelongDeptId())) {
            lqw.and(w -> {
                Long parentId = bo.getBelongDeptId();
                // 递归查询所有子部门
                List<SysDept> childDeptList = selectListByParentId(parentId);
                List<Long> deptIds = StreamUtils.toList(childDeptList, SysDept::getDeptId);
                // 包含当前部门本身
                deptIds.add(parentId);
                w.in(SysDept::getDeptId, deptIds);
            });
        }

        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = bo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(w -> w
                .like(SysDept::getDeptName, searchValue)         // String
                .or()
                .like(SysDept::getDeptCategory, searchValue)     // String
                .or()
                .likeCast(SysDept::getDeptId, searchValue)       // Long
            );
        }
        return lqw;
    }

    /**
     * 根据父部门ID查询其所有子部门的列表（带数据权限）
     */
    private List<SysDept> selectListByParentId(Long parentId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .select(SysDept::getDeptId)
            .apply(DataBaseHelper.findInSet(parentId, "ancestors"));
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 根据部门ID列表查询正常状态的部门（带数据权限）
     */
    @Override
    public List<SysDept> listNormalDeptsByIds(List<Long> deptIds) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .select(SysDept::getDeptId, SysDept::getDeptName, SysDept::getLeader)
            .eq(SysDept::getStatus, DictEnableStatus.ENABLE.getValue())
            .in(SysDept::getDeptId, deptIds);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 根据部门ID查询所有子部门数量（正常状态）
     */
    @Override
    public long countNormalChildrenByDeptId(Long deptId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .eq(SysDept::getStatus, DictEnableStatus.ENABLE.getValue())
            .apply(DataBaseHelper.findInSet(deptId, "ancestors"));
        return count(lqw);
    }

    /**
     * 检查是否存在子部门
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .eq(SysDept::getParentId, deptId);
        return exists(lqw);
    }

    /**
     * 校验部门名称在同级部门中的唯一性
     */
    @Override
    public boolean checkDeptNameUnique(String deptName, Long parentId, Long deptId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .eq(SysDept::getDeptName, deptName)
            .eq(SysDept::getParentId, parentId)
            .ne(SysDept::getDeptId, deptId);
        return !exists(lqw);
    }

    /**
     * 统计指定部门ID的部门数量（数据权限）
     */
    @Override
    public long countDeptById(Long deptId) {
        return baseMapper.countDeptById(deptId);
    }

    /**
     * 查询所有正常状态的部门（带数据权限）
     */
    @Override
    public List<SysDept> listNormalDepts() {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .select(SysDept::getDeptId, SysDept::getDeptName, SysDept::getParentId)
            .eq(SysDept::getStatus, DictEnableStatus.ENABLE.getValue());
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 查询所有子部门（包括孙子部门等）（带数据权限）
     */
    @Override
    public List<SysDept> listChildrenByDeptId(Long deptId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .apply(DataBaseHelper.findInSet(deptId, "ancestors"));
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 查询所有正常状态的子部门ID列表（包含自身）
     */
    @Override
    public List<Long> listChildrenDeptIds(Long deptId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .select(SysDept::getDeptId)
            .eq(SysDept::getStatus, DictEnableStatus.ENABLE.getValue())
            .apply(DataBaseHelper.findInSet(deptId, "ancestors"));
        return baseMapper.selectObjs(lqw);
    }

    /**
     * 根据角色ID查询部门树信息
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId, boolean deptCheckStrictly) {
        return baseMapper.selectDeptListByRoleId(roleId, deptCheckStrictly);
    }

    /**
     * 批量启用父部门状态
     */
    @Override
    public int batchEnableParentDepts(Long[] parentDeptIds) {
        return lambdaUpdate()
            .set(SysDept::getStatus, DictEnableStatus.ENABLE.getValue())
            .in(SysDept::getDeptId, List.of(parentDeptIds))
            .update();
    }

    /**
     * 根据部门ID列表查询部门名称映射（带数据权限）
     */
    @Override
    public List<SysDept> listDeptNamesById(List<Long> deptIds) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .select(SysDept::getDeptId, SysDept::getDeptName)
            .in(SysDept::getDeptId, deptIds);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 根据部门ID列表查询部门列表（带数据权限）
     */
    @Override
    public List<SysDept> listByDeptIds(List<Long> deptIds) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .in(SysDept::getDeptId, deptIds);
        return SpringUtils.getAopProxy(this).list(lqw);
    }

    /**
     * 根据部门ID查询部门名称
     */
    @Override
    public String getDeptNameById(Long deptId) {
        PlusLambdaQuery<SysDept> lqw = PlusLambdaQuery.of(SysDept.class)
            .select(SysDept::getDeptName)
            .eq(SysDept::getDeptId, deptId);
        SysDept dept = getOne(lqw);
        return dept != null ? dept.getDeptName() : null;
    }

    // ==================== 带数据权限的通用方法（重写父类方法） ====================

    /**
     * 条件查询列表（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     * <p>
     * 数据权限说明：
     * - deptName -> dept_id: 限制只能查看自己所属部门及子部门的数据
     *
     * @param wrapper 查询条件
     * @return 部门列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    @Override
    public List<SysDept> list(PlusLambdaQuery<SysDept> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页查询（带数据权限）
     * <p>
     * 默认带数据权限过滤，需要忽略权限时请使用 DataPermissionHelper.ignore() 包装调用
     *
     * @param wrapper   查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    @Override
    public PageResult<SysDept> page(PlusLambdaQuery<SysDept> wrapper, PageQuery pageQuery) {
        return PageResult.of(baseMapper.selectPage(pageQuery.build(), wrapper));
    }
}
