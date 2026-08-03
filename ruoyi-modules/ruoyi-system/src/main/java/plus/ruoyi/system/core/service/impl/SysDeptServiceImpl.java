package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.domain.dto.DeptDTO;
import plus.ruoyi.common.core.domain.dto.RoleDTO;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.DeptService;
import plus.ruoyi.common.core.utils.*;
import plus.ruoyi.common.mybatis.enums.DataScopeType;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.dao.ISysDeptDao;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.dao.ISysUserDao;
import plus.ruoyi.system.core.domain.SysDept;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.SysUser;
import plus.ruoyi.system.core.domain.bo.SysDeptBo;
import plus.ruoyi.system.core.domain.vo.SysDeptVo;
import plus.ruoyi.system.core.service.ISysDataScopeService;
import plus.ruoyi.system.core.service.ISysDeptService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理Service业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysDeptServiceImpl implements ISysDeptService, DeptService {

    private final ISysDeptDao deptDao;
    private final ISysRoleDao roleDao;
    private final ISysUserDao userDao;
    private final ISysDataScopeService dataScopeService;

    /**
     * 根据ID查询（带缓存）
     */
    @Cacheable(cacheNames = CacheNames.SYS_DEPT, key = "#deptId")
    @Override
    public SysDeptVo get(Long deptId) {
        SysDept entity = deptDao.getById(deptId);
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        SysDeptVo vo = MapstructUtils.convert(entity, SysDeptVo.class);

        // 查询父部门名称
        if (entity.getParentId() != null) {
            String parentName = deptDao.getDeptNameById(entity.getParentId());
            vo.setParentName(parentName);
        }

        return vo;
    }

    /**
     * 查询列表
     */
    @Override
    public List<SysDeptVo> list(SysDeptBo bo) {
        PlusLambdaQuery<SysDept> wrapper = deptDao.buildQueryWrapper(bo);
        List<SysDept> entities = deptDao.list(wrapper);
        return MapstructUtils.convert(entities, SysDeptVo.class);
    }

    /**
     * 查询所有部门列表
     */
    @Override
    public List<SysDeptVo> listAll() {
        return list(new SysDeptBo());
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<SysDeptVo> page(SysDeptBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysDept> wrapper = deptDao.buildQueryWrapper(bo);
        PageResult<SysDept> entityPage = deptDao.page(wrapper, pageQuery);
        return entityPage.convert(SysDeptVo.class);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        return deptDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysDeptBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysDept> entities = MapstructUtils.convert(boList, SysDept.class);
        return deptDao.batchSave(entities);
    }

    /**
     * 查询部门树结构信息
     */
    @Override
    public List<Tree<Long>> getDeptTree(SysDeptBo bo) {
        List<SysDeptVo> deptList = list(bo);
        return buildDeptTreeSelect(deptList);
    }

    /**
     * 构建前端所需要下拉树结构
     */
    @Override
    public List<Tree<Long>> buildDeptTreeSelect(List<SysDeptVo> deptList) {
        if (CollUtil.isEmpty(deptList)) {
            return CollUtil.newArrayList();
        }

        List<Tree<Long>> treeList = CollUtil.newArrayList();

        // 遍历部门列表，识别顶级节点
        for (SysDeptVo dept : deptList) {
            Long parentId = dept.getParentId();

            // 在当前列表中查找父节点，如果找不到则说明是顶级节点
            SysDeptVo parentDept = StreamUtils.findFirstValue(deptList,
                it -> it.getDeptId().longValue() == parentId);

            if (ObjectUtil.isNull(parentDept)) {
                // 构建以当前节点为根的树结构
                List<Tree<Long>> trees = TreeBuildUtils.build(deptList, parentId, (deptVo, tree) ->
                    tree.setId(deptVo.getDeptId())
                        .setParentId(deptVo.getParentId())
                        .setName(deptVo.getDeptName())
                        .setWeight(deptVo.getOrderNum())
                        .putExtra("disabled", DictEnableStatus.DISABLED.getValue().equals(deptVo.getStatus())));

                // 找到当前部门对应的树节点并添加到结果中
                Tree<Long> currentTree = StreamUtils.findFirstValue(trees,
                    it -> it.getId().longValue() == dept.getDeptId());
                if (ObjectUtil.isNotNull(currentTree)) {
                    treeList.add(currentTree);
                }
            }
        }

        return treeList;
    }

    /**
     * 根据角色ID查询部门树信息
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        SysRole role = roleDao.getById(roleId);
        return deptDao.selectDeptListByRoleId(roleId, role.getDeptCheckStrictly());
    }

    /**
     * 根据部门ID列表查询正常状态的部门信息
     */
    @Override
    public List<SysDeptVo> listNormalDeptsByIds(List<Long> deptIds) {
        List<SysDept> entities = deptDao.listNormalDeptsByIds(deptIds);
        return MapstructUtils.convert(entities, SysDeptVo.class);
    }

    /**
     * 通过部门ID字符串查询部门名称字符串
     */
    @Override
    public String getDeptNameByIds(String deptIds) {
        List<String> deptNameList = new ArrayList<>();
        for (Long deptId : StringUtils.splitToList(deptIds, Convert::toLong)) {
            SysDeptVo deptVo = SpringUtils.getAopProxy(this).get(deptId);
            if (ObjectUtil.isNotNull(deptVo)) {
                deptNameList.add(deptVo.getDeptName());
            }
        }
        return StringUtils.joinComma(deptNameList);
    }

    /**
     * 根据部门ID查询部门负责人
     */
    @Override
    public Long getDeptLeaderById(Long deptId) {
        SysDeptVo deptVo = SpringUtils.getAopProxy(this).get(deptId);
        return ObjectUtil.isNotNull(deptVo) ? deptVo.getLeader() : null;
    }

    /**
     * 查询所有正常状态的部门
     */
    @Override
    public List<DeptDTO> listNormalDepts() {
        List<SysDept> deptList = deptDao.listNormalDepts();
        return BeanUtil.copyToList(deptList, DeptDTO.class);
    }

    /**
     * 根据部门ID查询所有子部门数量（正常状态）
     */
    @Override
    public long getNormalChildrenDeptById(Long deptId) {
        return deptDao.countNormalChildrenByDeptId(deptId);
    }

    /**
     * 检查是否存在子部门
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        return deptDao.hasChildByDeptId(deptId);
    }

    /**
     * 检查部门是否存在关联用户
     */
    @Override
    public boolean checkDeptExistUser(Long deptId) {
        return userDao.existsByDeptId(deptId);
    }

    /**
     * 校验部门名称在同级部门中的唯一性
     */
    @Override
    public boolean checkDeptNameUnique(SysDeptBo dept) {
        return deptDao.checkDeptNameUnique(
            dept.getDeptName(),
            dept.getParentId(),
            ObjectUtil.defaultIfNull(dept.getDeptId(), -1L)
        );
    }

    /**
     * 校验部门数据权限
     */
    @Override
    public void checkDeptDataScope(Long deptId) {
        if (ObjectUtil.isNull(deptId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (deptDao.countDeptById(deptId) == 0) {
            throw ServiceException.of("没有权限访问部门数据！");
        }
    }

    /**
     * 校验是否允许在指定部门下新增子部门
     * <p>
     * 限制说明：
     * - 仅本部门数据权限(DEPT)：不允许在自己部门下新增子部门，因为新增后自己看不到
     * - 部门及以下数据权限(DEPT_AND_CHILD)：允许新增子部门，新增后可见
     * - 自定义数据权限(CUSTOM)：不允许新增子部门，因为新增的部门不在授权列表中
     * - 全部数据权限(ALL)：允许新增子部门
     * </p>
     *
     * @param parentDeptId 父部门ID
     */
    private void checkCanAddChildDept(Long parentDeptId) {
        // 超级管理员跳过检查
        if (LoginHelper.isSuperAdmin() || LoginHelper.isTenantAdmin()) {
            return;
        }

        // 获取当前用户信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (ObjectUtil.isNull(loginUser) || CollUtil.isEmpty(loginUser.getRoles())) {
            return;
        }

        // 检查用户的所有角色
        for (RoleDTO role : loginUser.getRoles()) {
            DataScopeType dataScopeType = DataScopeType.getByValue(role.getDataScope());
            if (ObjectUtil.isNull(dataScopeType)) {
                continue;
            }

            // 1. 仅本部门数据权限的限制
            if (dataScopeType == DataScopeType.DEPT) {
                // 如果要在自己所在的部门下新增子部门
                if (parentDeptId.equals(loginUser.getDeptId())) {
                    throw ServiceException.of("您的数据权限为【仅本部门】，新增的子部门将无法查看，不允许操作！\n" +
                        "建议：请联系管理员调整数据权限范围为【本部门及以下】");
                }
            }

            // 2. 自定义数据权限的限制
            if (dataScopeType == DataScopeType.CUSTOM) {
                // 获取该角色的自定义授权部门列表
                String deptIdsStr = dataScopeService.getRoleCustom(role.getRoleId());
                if (StringUtils.isNotBlank(deptIdsStr) && !"-1".equals(deptIdsStr)) {
                    List<Long> authorizedDeptIds = StringUtils.splitToList(deptIdsStr, Convert::toLong);
                    // 如果父部门在授权列表中，则禁止新增子部门
                    if (authorizedDeptIds.contains(parentDeptId)) {
                        throw ServiceException.of("您的数据权限为【自定义数据权限】，新增的子部门不在授权范围内，将无法查看，不允许操作！\n" +
                            "建议：\n" +
                            "1. 请联系管理员在角色权限中添加新部门的授权\n" +
                            "2. 或调整数据权限范围为【本部门及以下】");
                    }
                }
            }
        }
    }

    /**
     * 新增部门信息
     */
    @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertDept(SysDeptBo bo) {
        // 校验父部门的数据权限
        checkDeptDataScope(bo.getParentId());

        // 校验是否允许在该部门下新增子部门
        checkCanAddChildDept(bo.getParentId());

        SysDept parentDept = deptDao.getById(bo.getParentId());
        if (ObjectUtil.isNull(parentDept)) {
            throw ServiceException.of("父部门不存在");
        }
        if (!DictEnableStatus.ENABLE.getValue().equals(parentDept.getStatus())) {
            throw ServiceException.of("部门停用，不允许新增");
        }

        SysDept dept = MapstructUtils.convert(bo, SysDept.class);
        dept.setAncestors(parentDept.getAncestors() + StringUtils.SEPARATOR + dept.getParentId());
        deptDao.insert(dept);
        bo.setDeptId(dept.getDeptId());
        return dept.getDeptId();
    }

    /**
     * 修改部门信息
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT, key = "#bo.deptId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    })
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(SysDeptBo bo) {
        SysDept dept = MapstructUtils.convert(bo, SysDept.class);
        SysDept oldDept = deptDao.getById(dept.getDeptId());
        if (ObjectUtil.isNull(oldDept)) {
            throw ServiceException.of("部门不存在，无法修改");
        }

        // 处理父部门变更的情况
        if (!oldDept.getParentId().equals(dept.getParentId())) {
            this.checkDeptDataScope(dept.getParentId());
            SysDept newParentDept = deptDao.getById(dept.getParentId());
            if (ObjectUtil.isNotNull(newParentDept)) {
                String newAncestors = newParentDept.getAncestors() + StringUtils.SEPARATOR + newParentDept.getDeptId();
                String oldAncestors = oldDept.getAncestors();
                dept.setAncestors(newAncestors);
                updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
            }
        } else {
            dept.setAncestors(oldDept.getAncestors());
        }

        int result = deptDao.updateById(dept);

        // 启用子部门时自动启用所有父部门
        if (DictEnableStatus.ENABLE.getValue().equals(dept.getStatus())
            && StringUtils.isNotEmpty(dept.getAncestors())
            && !StringUtils.equals(SystemConstants.ROOT_DEPT_ANCESTORS, dept.getAncestors())) {
            updateParentDeptStatusNormal(dept);
        }

        return result > 0;
    }

    /**
     * 批量启用父部门状态
     */
    private void updateParentDeptStatusNormal(SysDept dept) {
        String ancestors = dept.getAncestors();
        Long[] parentDeptIds = Convert.toLongArray(ancestors);
        deptDao.batchEnableParentDepts(parentDeptIds);
    }

    /**
     * 级联更新子部门的ancestors路径
     */
    private void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDept> childrenList = deptDao.listChildrenByDeptId(deptId);
        List<SysDept> updateList = new ArrayList<>();
        for (SysDept child : childrenList) {
            SysDept updateDept = new SysDept();
            updateDept.setDeptId(child.getDeptId());
            updateDept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            updateList.add(updateDept);
        }

        if (CollUtil.isNotEmpty(updateList)) {
            // 批量更新
            for (SysDept dept : updateList) {
                deptDao.updateById(dept);
                CacheUtils.evict(CacheNames.SYS_DEPT, dept.getDeptId());
            }
        }
    }

    /**
     * 删除部门信息
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT, key = "#deptId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, key = "#deptId")
    })
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDeptById(Long deptId) {
        return deptDao.deleteById(deptId);
    }

    /**
     * 根据部门ID获取所有子部门ID列表（包含自身）
     */
    @Override
    public List<Long> getChildrenDeptIds(Long deptId) {
        if (ObjectUtil.isNull(deptId)) {
            return CollUtil.newArrayList();
        }

        List<Long> deptIds = deptDao.listChildrenDeptIds(deptId);

        // 添加当前部门ID
        if (!deptIds.contains(deptId)) {
            deptIds.add(deptId);
        }

        return deptIds;
    }

    // ================ DeptService 通用接口实现 =================

    /**
     * 根据部门ID列表查询部门名称映射关系
     *
     * @param deptIds 部门ID列表
     * @return Map，其中key为部门ID，value为对应的部门名称
     */
    @Override
    public Map<Long, String> mapDeptNames(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyMap();
        }

        return deptDao.listDeptNamesById(deptIds)
            .stream()
            .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName));
    }
}
