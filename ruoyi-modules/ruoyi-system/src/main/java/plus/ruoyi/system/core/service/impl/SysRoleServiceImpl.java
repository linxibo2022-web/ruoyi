package plus.ruoyi.system.core.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.domain.dto.RoleDTO;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.RoleService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.enums.DataScopeType;
import plus.ruoyi.common.mybatis.helper.DataPermissionHelper;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.dao.ISysRoleDeptDao;
import plus.ruoyi.system.core.dao.ISysRoleMenuDao;
import plus.ruoyi.system.core.dao.ISysUserRoleDao;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.core.domain.SysRoleDept;
import plus.ruoyi.system.core.domain.SysRoleMenu;
import plus.ruoyi.system.core.domain.SysUserRole;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;
import plus.ruoyi.system.core.service.ISysRoleService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl implements ISysRoleService, RoleService {

    private final ISysRoleDao roleDao;
    private final ISysRoleMenuDao roleMenuDao;
    private final ISysRoleDeptDao roleDeptDao;
    private final ISysUserRoleDao userRoleDao;

    // ================ 基础CRUD实现 =================

    /**
     * 根据ID查询
     */
    @Override
    public SysRoleVo get(Long roleId) {
        SysRole entity = roleDao.getById(roleId);
        return MapstructUtils.convert(entity, SysRoleVo.class);
    }

    /**
     * 查询列表
     */
    @Override
    public List<SysRoleVo> list(SysRoleBo bo) {
        PlusLambdaQuery<SysRole> wrapper = roleDao.buildQueryWrapper(bo);
        List<SysRole> entities = roleDao.list(wrapper);
        return MapstructUtils.convert(entities, SysRoleVo.class);
    }

    /**
     * 分页查询
     */
    @Override
    public PageResult<SysRoleVo> page(SysRoleBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysRole> wrapper = roleDao.buildQueryWrapper(bo);
        PageResult<SysRole> entityPage = roleDao.page(wrapper, pageQuery);
        return entityPage.convert(SysRoleVo.class);
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
        return roleDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysRoleBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysRole> entities = MapstructUtils.convert(boList, SysRole.class);
        return roleDao.batchSave(entities);
    }

    // ================ 角色查询相关方法 =================

    /**
     * 根据用户ID查询角色
     */
    @Override
    public List<SysRoleVo> listRolesByUserId(Long userId) {
        List<SysRole> roles = roleDao.listRolesByUserId(userId);
        return MapstructUtils.convert(roles, SysRoleVo.class);
    }

    /**
     * 根据用户ID查询角色列表(包含被授权状态)
     */
    @Override
    public List<SysRoleVo> listRolesWithAuthByUserId(Long userId) {
        // 查询用户已有的角色
        List<SysRole> userRoles = roleDao.listRolesByUserId(userId);
        Set<Long> userRoleIds = StreamUtils.toSet(userRoles, SysRole::getRoleId);

        // 查询当前操作者有权限分配的角色（带数据权限过滤，只查正常状态）
        SysRoleBo roleBo = new SysRoleBo();
        roleBo.setStatus(DictEnableStatus.ENABLE.getValue());
        PlusLambdaQuery<SysRole> wrapper = roleDao.buildQueryWrapper(roleBo);
        List<SysRole> permissionRoles = roleDao.list(wrapper);
        Set<Long> permissionRoleIds = StreamUtils.toSet(permissionRoles, SysRole::getRoleId);

        List<SysRoleVo> roleVos = new ArrayList<>(MapstructUtils.convert(permissionRoles, SysRoleVo.class));

        // 合并用户已有但不在权限范围内的角色（确保回显）
        List<Long> missingRoleIds = userRoleIds.stream()
            .filter(id -> !permissionRoleIds.contains(id))
            .toList();
        if (!missingRoleIds.isEmpty()) {
            List<SysRoleVo> userExistingRoles = listRolesByIds(missingRoleIds);
            userExistingRoles.forEach(role -> role.setDisabled(true));
            roleVos.addAll(userExistingRoles);
        }

        // 标记已授权的角色
        for (SysRoleVo roleVo : roleVos) {
            if (userRoleIds.contains(roleVo.getRoleId())) {
                roleVo.setFlag(true);
            }
        }

        return roleVos;
    }

    /**
     * 根据用户ID查询权限
     */
    @Override
    public Set<String> listRolePermissionsByUserId(Long userId) {
        List<SysRole> perms = roleDao.listRolesByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (ObjectUtil.isNotNull(perm)) {
                permsSet.addAll(StringUtils.splitToList(perm.getRoleKey().trim()));
            }
        }
        return permsSet;
    }

    /**
     * 根据用户ID获取角色选择框列表
     */
    @Override
    public List<Long> listRoleIdsByUserId(Long userId) {
        List<SysRole> list = roleDao.listRolesByUserId(userId);
        return StreamUtils.toList(list, SysRole::getRoleId);
    }

    /**
     * 通过角色ID查询角色
     */
    @Override
    public SysRoleVo getRoleById(Long roleId) {
        return MapstructUtils.convert(roleDao.getRoleByIdWithJoin(roleId), SysRoleVo.class);
    }

    /**
     * 通过角色ID串查询角色（忽略数据权限）
     * <p>
     * 用于用户详情回显已有角色，不受当前操作者数据权限限制
     */
    @Override
    public List<SysRoleVo> listRolesByIds(List<Long> roleIds) {
        // 忽略数据权限：用于回显用户已有角色，不是分配新角色
        List<SysRole> entities = DataPermissionHelper.ignore(() -> roleDao.listNormalRolesByIds(roleIds));
        return MapstructUtils.convert(entities, SysRoleVo.class);
    }

    /**
     * 查询角色选项列表
     */
    @Override
    public List<SysRoleVo> listRoleOptions() {
        List<SysRole> entities = roleDao.listNormalRoles();
        return MapstructUtils.convert(entities, SysRoleVo.class);
    }

    // ================ 业务校验相关方法 =================

    /**
     * 校验角色名称是否唯一
     */
    @Override
    public boolean checkRoleNameUnique(SysRoleBo role) {
        return roleDao.checkRoleNameUnique(
            role.getRoleName(),
            ObjectUtil.defaultIfNull(role.getRoleId(), -1L)
        );
    }

    /**
     * 校验角色权限标识是否唯一
     */
    @Override
    public boolean checkRoleKeyUnique(SysRoleBo role) {
        return roleDao.checkRoleKeyUnique(
            role.getRoleKey(),
            ObjectUtil.defaultIfNull(role.getRoleId(), -1L)
        );
    }

    /**
     * 校验角色是否允许操作
     * 检查是否为系统内置的管理员角色，内置角色不允许修改
     */
    @Override
    public void checkRoleAllowed(SysRoleBo role) {
        if (ObjectUtil.isNotNull(role.getRoleId()) && LoginHelper.isSuperAdmin(role.getRoleId())) {
            throw ServiceException.of("不允许操作超级管理员角色");
        }
        if (ObjectUtil.isNotNull(role.getRoleId()) && LoginHelper.isTenantAdmin(Set.of(role.getRoleKey()))) {
            throw ServiceException.of("不允许操作管理员角色");
        }

        String[] keys = new String[]{TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY};

        // 新增不允许使用管理员标识符
        if (ObjectUtil.isNull(role.getRoleId()) && StringUtils.equalsAny(role.getRoleKey(), keys)) {
            throw ServiceException.of("不允许使用系统内置管理员角色标识符!");
        }

        // 修改不允许修改管理员标识符
        if (ObjectUtil.isNotNull(role.getRoleId())) {
            SysRole sysRole = roleDao.getById(role.getRoleId());
            // 如果标识符不相等，判断为修改了管理员标识符
            if (!StringUtils.equals(sysRole.getRoleKey(), role.getRoleKey())) {
                if (StringUtils.equalsAny(sysRole.getRoleKey(), keys)) {
                    throw ServiceException.of("不允许修改系统内置管理员角色标识符!");
                } else if (StringUtils.equalsAny(role.getRoleKey(), keys)) {
                    throw ServiceException.of("不允许使用系统内置管理员角色标识符!");
                }
            }
        }
    }

    /**
     * 校验角色是否有数据权限
     */
    @Override
    public void checkRoleDataScope(Long roleId) {
        if (ObjectUtil.isNull(roleId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (roleDao.countRoleById(roleId) == 0) {
            throw ServiceException.of("没有权限访问角色数据！");
        }
    }

    /**
     * 校验数据权限范围是否越权
     * 用户不能设置超出自己最高数据权限的范围
     * <p>
     * 数据权限等级映射（用于权限比较）：
     * - 3（本部门）和 4（本部门及以下）视为同级权限
     * - 5（仅本人）和 6（部门及以下或本人）视为同级权限
     */
    @Override
    public void checkDataScopeLevel(String dataScope) {
        if (StringUtils.isBlank(dataScope)) {
            return;
        }
        // 超级管理员不受限制
        if (LoginHelper.isSuperAdmin()) {
            return;
        }

        // 获取当前用户的最高数据权限等级
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        if (CollUtil.isEmpty(roles)) {
            throw ServiceException.of("当前用户没有任何角色，无法设置数据权限！");
        }

        // 找出用户所有角色中最高的数据权限等级（等级数值最小）
        int userHighestLevel = roles.stream()
            .map(RoleDTO::getDataScope)
            .filter(StringUtils::isNotBlank)
            .mapToInt(this::getDataScopeLevel)
            .min()
            .orElse(4); // 默认为仅本人级别

        // 获取要设置的权限等级
        int targetLevel = getDataScopeLevel(dataScope);

        // 比较要设置的权限等级与用户最高权限等级
        // 等级数值越小权限越大，如果要设置的权限等级小于用户最高权限等级，则越权
        if (targetLevel < userHighestLevel) {
            DataScopeType targetScope = DataScopeType.getByValue(dataScope);
            String targetLabel = targetScope != null ? targetScope.getLabel() : dataScope;
            throw ServiceException.of("不允许设置超出自己权限范围的数据权限！不能设置【{}】", targetLabel);
        }
    }

    /**
     * 获取数据权限等级
     * 将数据权限值映射为等级，等级越小权限越大
     * - 3（本部门）和 4（本部门及以下）视为同级权限（等级3）
     * - 5（仅本人）和 6（部门及以下或本人）视为同级权限（等级4）
     *
     * @param dataScope 数据权限值
     * @return 权限等级
     */
    private int getDataScopeLevel(String dataScope) {
        if (StringUtils.isBlank(dataScope)) {
            return 99;
        }
        return switch (dataScope) {
            case "1" -> 1; // 全部数据权限
            case "2" -> 2; // 自定义数据权限
            case "3", "4" -> 3; // 本部门 / 本部门及以下（同级）
            case "5", "6" -> 4; // 仅本人 / 部门及以下或本人（同级）
            default -> 99;
        };
    }

    /**
     * 通过角色ID查询角色使用数量
     */
    @Override
    public long countUsersByRoleId(Long roleId) {
        return userRoleDao.countUsersByRoleId(roleId);
    }

    // ================ 角色增删改相关方法 =================

    /**
     * 新增保存角色信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertRole(SysRoleBo bo) {
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        // 新增角色信息
        roleDao.insert(role);
        bo.setRoleId(role.getRoleId());
        // 新增角色与菜单关联
        insertRoleMenu(bo);
        // 新增角色与部门关联（自定义数据权限时）
        insertRoleDept(bo);
        return role.getRoleId();
    }

    /**
     * 修改保存角色信息
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#bo.roleId")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRoleBo bo) {
        SysRole role = MapstructUtils.convert(bo, SysRole.class);

        if (DictEnableStatus.DISABLED.getValue().equals(role.getStatus()) && this.countUsersByRoleId(role.getRoleId()) > 0) {
            throw ServiceException.of("角色已分配，不能禁用!");
        }

        // 修改角色信息
        roleDao.updateById(role);

        // 删除角色与菜单关联
        roleMenuDao.deleteByRoleId(role.getRoleId());
        // 新增角色与菜单关联
        insertRoleMenu(bo);

        // 删除角色与部门关联
        roleDeptDao.deleteByRoleId(role.getRoleId());
        // 新增角色与部门关联（自定义数据权限时）
        insertRoleDept(bo);

        return true;
    }

    /**
     * 修改角色状态
     */
    @Override
    public boolean updateRoleStatus(Long roleId, String status) {
        if (DictEnableStatus.DISABLED.getValue().equals(status) && this.countUsersByRoleId(roleId) > 0) {
            throw ServiceException.of("角色已分配，不能禁用!");
        }
        return roleDao.updateRoleStatus(roleId, status);
    }

    /**
     * 修改数据权限信息
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#bo.roleId")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean authDataScope(SysRoleBo bo) {
        // 校验数据权限范围是否越权
        checkDataScopeLevel(bo.getDataScope());

        SysRole role = MapstructUtils.convert(bo, SysRole.class);

        // 修改角色信息
        roleDao.updateById(role);

        // 删除角色与部门关联
        roleDeptDao.deleteByRoleId(role.getRoleId());

        // 新增角色和部门信息（数据权限）
        return insertRoleDept(bo) > 0;
    }

    /**
     * 通过角色ID删除角色
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#roleId")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleById(Long roleId) {
        // 删除角色与菜单关联
        roleMenuDao.deleteByRoleId(roleId);

        // 删除角色与部门关联
        roleDeptDao.deleteByRoleId(roleId);

        return roleDao.deleteById(roleId) > 0;
    }

    /**
     * 批量删除角色信息
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            SysRole role = roleDao.getById(roleId);
            // 角色不存在时应该抛出友好提示
            if (ObjectUtil.isNull(role)) {
                throw ServiceException.of("角色不存在或已被删除，角色ID: {}", roleId);
            }
            checkRoleAllowed(BeanUtil.toBean(role, SysRoleBo.class));
            checkRoleDataScope(roleId);
            if (countUsersByRoleId(roleId) > 0) {
                throw ServiceException.of("{}已分配，不能删除!", role.getRoleName());
            }
        }

        List<Long> ids = List.of(roleIds);

        // 删除角色与菜单关联
        roleMenuDao.batchDeleteByRoleIds(ids);

        // 删除角色与部门关联
        roleDeptDao.batchDeleteByRoleIds(ids);

        return roleDao.deleteByIds(ids) > 0;
    }

    // ================ 用户角色授权相关方法 =================

    /**
     * 取消授权用户角色
     */
    @Override
    public boolean deleteAuthUser(SysUserRole userRole) {
        // 超级管理员不允许被操作角色
        if (LoginHelper.isSuperAdmin(userRole.getUserId())) {
            throw new ServiceException("不允许操作超级管理员的角色!");
        }
        // 如果操作的是当前用户自己，需要确保撤销后至少还有一个角色
        if (LoginHelper.getUserId().equals(userRole.getUserId())) {
            Long roleCount = userRoleDao.count(PlusLambdaQuery.of(SysUserRole.class)
                .eq(SysUserRole::getUserId, userRole.getUserId()));
            if (roleCount != null && roleCount <= 1) {
                throw new ServiceException("不允许撤销当前用户的最后一个角色!");
            }
        }
        int rows = userRoleDao.deleteUserRole(userRole.getRoleId(), userRole.getUserId());
        if (rows > 0) {
            cleanOnlineUser(List.of(userRole.getUserId()));
        }
        return rows > 0;
    }

    /**
     * 批量取消授权用户角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAuthUsers(Long roleId, Long[] userIds) {
        // 过滤掉超级管理员，超管不允许被操作角色
        List<Long> ids = StreamUtils.filter(List.of(userIds),
            userId -> !LoginHelper.isSuperAdmin(userId));
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("不允许操作超级管理员的角色!");
        }
        // 如果批量撤销包含当前用户，需要确保撤销后至少还有一个角色
        Long currentUserId = LoginHelper.getUserId();
        if (ids.contains(currentUserId)) {
            Long roleCount = userRoleDao.count(PlusLambdaQuery.of(SysUserRole.class)
                .eq(SysUserRole::getUserId, currentUserId));
            if (roleCount != null && roleCount <= 1) {
                throw ServiceException.of("不允许撤销当前用户的最后一个角色!");
            }
        }
        int rows = userRoleDao.batchDeleteUserRoles(roleId, ids);

        if (rows > 0) {
            cleanOnlineUser(ids);
        }
        return rows > 0;
    }

    /**
     * 批量选择授权用户角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertAuthUsers(Long roleId, Long[] userIds) {
        int rows = 1;
        // 过滤掉超级管理员，超管拥有所有权限，不需要分配角色
        List<Long> ids = StreamUtils.filter(List.of(userIds),
            userId -> !LoginHelper.isSuperAdmin(userId));
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("超级管理员拥有所有权限，无需分配角色!");
        }
        List<SysUserRole> list = StreamUtils.toList(ids, userId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            return ur;
        });

        if (CollUtil.isNotEmpty(list)) {
            rows = userRoleDao.batchInsertUserRoles(list) ? list.size() : 0;
        }

        if (rows > 0) {
            cleanOnlineUser(ids);
        }
        return rows > 0;
    }

    // ================ 在线用户管理相关方法 =================

    /**
     * 根据角色ID清理在线用户
     */
    @Override
    public void cleanOnlineUserByRole(Long roleId) {
        // 如果角色未绑定用户直接返回
        Long num = userRoleDao.countUsersByRoleId(roleId);
        if (num == 0) {
            return;
        }

        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        if (CollUtil.isEmpty(keys)) {
            return;
        }

        // 角色关联的在线用户量过大会导致redis阻塞卡顿，谨慎操作
        keys.parallelStream().forEach(key -> {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }

            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (ObjectUtil.isNull(loginUser) || CollUtil.isEmpty(loginUser.getRoles())) {
                return;
            }

            if (loginUser.getRoles().stream().anyMatch(r -> r.getRoleId().equals(roleId))) {
                try {
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                }
            }
        });
    }

    /**
     * 根据用户ID列表清理在线用户
     */
    @Override
    public void cleanOnlineUser(List<Long> userIds) {
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        if (CollUtil.isEmpty(keys)) {
            return;
        }

        // 角色关联的在线用户量过大会导致redis阻塞卡顿，谨慎操作
        keys.parallelStream().forEach(key -> {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }

            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (ObjectUtil.isNull(loginUser)) {
                return;
            }

            if (userIds.contains(loginUser.getUserId())) {
                try {
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                }
            }
        });
    }

    // ================ 私有辅助方法 =================

    /**
     * 新增角色菜单信息
     */
    private boolean insertRoleMenu(SysRoleBo role) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds()) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }

        if (CollUtil.isNotEmpty(list)) {
            rows = roleMenuDao.batchInsertRoleMenus(list) ? list.size() : 0;
        }
        return rows > 0;
    }

    /**
     * 新增角色部门信息(数据权限)
     */
    private int insertRoleDept(SysRoleBo role) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<>();
        for (Long deptId : role.getDeptIds()) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }

        if (CollUtil.isNotEmpty(list)) {
            rows = roleDeptDao.batchInsertRoleDepts(list) ? list.size() : 0;
        }
        return rows;
    }

    // ================ RoleService 通用接口实现 =================

    /**
     * 根据角色ID列表查询角色名称映射关系
     *
     * @param roleIds 角色ID列表
     * @return Map，其中key为角色ID，value为对应的角色名称
     */
    @Override
    public Map<Long, String> mapRoleNames(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }

        return roleDao.listRoleNamesById(roleIds)
            .stream()
            .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));
    }
}
