package plus.ruoyi.system.tenant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.enums.UserType;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.TenantService;
import plus.ruoyi.common.core.utils.*;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.redis.utils.CacheUtils;
import plus.ruoyi.common.tenant.core.TenantEntity;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.config.dao.ISysConfigDao;
import plus.ruoyi.system.config.domain.SysConfig;
import plus.ruoyi.system.core.dao.*;
import plus.ruoyi.system.core.domain.*;
import plus.ruoyi.system.dict.dao.ISysDictDataDao;
import plus.ruoyi.system.dict.dao.ISysDictTypeDao;
import plus.ruoyi.system.dict.domain.SysDictData;
import plus.ruoyi.system.dict.domain.SysDictType;
import plus.ruoyi.system.tenant.dao.ISysTenantDao;
import plus.ruoyi.system.tenant.dao.ISysTenantPackageDao;
import plus.ruoyi.system.tenant.domain.bo.SysTenantBo;
import plus.ruoyi.system.tenant.domain.SysTenant;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import plus.ruoyi.system.tenant.domain.vo.SysTenantVo;
import plus.ruoyi.system.tenant.service.ISysTenantService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

/**
 * 租户Service业务层处理
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
public class SysTenantServiceImpl implements ISysTenantService, TenantService {

    private final ISysTenantDao tenantDao;
    private final ISysTenantPackageDao tenantPackageDao;
    private final ISysUserDao userDao;
    private final ISysDeptDao deptDao;
    private final ISysRoleDao roleDao;
    private final ISysRoleMenuDao roleMenuDao;
    private final ISysRoleDeptDao roleDeptDao;
    private final ISysUserRoleDao userRoleDao;
    private final ISysDictTypeDao dictTypeDao;
    private final ISysDictDataDao dictDataDao;
    private final ISysConfigDao configDao;

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 视图对象
     */
    @Override
    public SysTenantVo get(Long id) {
        SysTenant entity = tenantDao.getById(id);
        return MapstructUtils.convert(entity, SysTenantVo.class);
    }

    /**
     * 查询列表
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @Override
    public List<SysTenantVo> list(SysTenantBo bo) {
        PlusLambdaQuery<SysTenant> wrapper = tenantDao.buildQueryWrapper(bo);
        List<SysTenant> entities = tenantDao.list(wrapper);
        return MapstructUtils.convert(entities, SysTenantVo.class);
    }

    /**
     * 分页查询
     *
     * @param bo        查询参数
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<SysTenantVo> page(SysTenantBo bo, PageQuery pageQuery) {
        PlusLambdaQuery<SysTenant> wrapper = tenantDao.buildQueryWrapper(bo);
        PageResult<SysTenant> entityPage = tenantDao.page(wrapper, pageQuery);
        return entityPage.convert(SysTenantVo.class);
    }

    /**
     * 新增
     *
     * @param bo 业务对象
     * @return 主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SysTenantBo bo) {
        SysTenant entity = MapstructUtils.convert(bo, SysTenant.class);
        beforeSave(entity);
        tenantDao.insert(entity);
        return entity.getId();
    }

    /**
     * 修改
     *
     * @param bo 业务对象
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(SysTenantBo bo) {
        if (bo.getId() == null) {
            throw ServiceException.of("租户ID不能为空");
        }
        if (!tenantDao.exists(bo.getId())) {
            throw ServiceException.of("租户不存在");
        }
        SysTenant entity = MapstructUtils.convert(bo, SysTenant.class);
        beforeSave(entity);
        return tenantDao.updateById(entity);
    }

    /**
     * 批量删除
     *
     * @param ids ID集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw ServiceException.of("ID集合不能为空");
        }
        beforeDelete(ids);
        return tenantDao.deleteByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param boList 业务对象集合
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<SysTenantBo> boList) {
        if (CollUtil.isEmpty(boList)) {
            return 0;
        }
        List<SysTenant> entities = new ArrayList<>(boList.size());
        for (SysTenantBo bo : boList) {
            SysTenant entity = MapstructUtils.convert(bo, SysTenant.class);
            beforeSave(entity);
            entities.add(entity);
        }
        return tenantDao.batchSave(entities);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 待保存实体
     */
    protected void beforeSave(SysTenant entity) {
        // 可以在这里添加保存前的通用校验逻辑
    }

    /**
     * 删除前的业务规则校验
     *
     * @param ids 待删除数据ID集合
     */
    protected void beforeDelete(Collection<Long> ids) {
        // 做一些业务上的校验,判断是否需要校验
        if (ids.contains(TenantConstants.SUPER_ADMIN_ID)) {
            throw ServiceException.of("超管租户不能删除");
        }
        CacheUtils.clear(CacheNames.SYS_TENANT);
    }

    /**
     * 基于租户ID查询租户
     */
    @Cacheable(cacheNames = CacheNames.SYS_TENANT, key = "#tenantId")
    @Override
    public SysTenantVo getTenantByTenantId(String tenantId) {
        SysTenant entity = tenantDao.getByTenantId(tenantId);
        return MapstructUtils.convert(entity, SysTenantVo.class);
    }

    /**
     * 新增租户
     *
     * @param bo 租户业务对象
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertTenant(SysTenantBo bo) {
        SysTenant add = MapstructUtils.convert(bo, SysTenant.class);

        // 获取所有租户id
        List<String> tenantIds = tenantDao.listAllTenantIds();
        String tenantId = generateTenantId(tenantIds);
        add.setTenantId(tenantId);
        // 如果域名不为空，则去掉前面的协议部分
        add.setDomain(StringUtils.contains(add.getDomain(), "//") ?
            StringUtils.splitToList(add.getDomain(), "//").get(1) : add.getDomain());
        boolean flag = tenantDao.insert(add) > 0;
        if (!flag) {
            throw ServiceException.of("创建租户失败");
        }
        bo.setId(add.getId());

        // 根据套餐创建角色
        Long roleId = createTenantRole(tenantId, bo.getPackageId());
        // 同步默认租户的普通业务角色到新租户
        syncDefaultRolesToNewTenant(tenantId, bo.getPackageId());

        // 创建部门: 公司名是部门名称
        SysDept dept = new SysDept();
        dept.setTenantId(tenantId);
        dept.setDeptName(bo.getCompanyName());
        dept.setParentId(Constants.TOP_PARENT_ID);
        dept.setAncestors(Constants.TOP_PARENT_ID.toString());
        deptDao.insert(dept);
        Long deptId = dept.getDeptId();

        // 角色和部门关联表
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setRoleId(roleId);
        roleDept.setDeptId(deptId);
        roleDeptDao.insert(roleDept);

        // 创建系统用户
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUserName(bo.getUserName());
        user.setNickName(bo.getUserName());
        user.setPassword(BCrypt.hashpw(bo.getPassword()));
        user.setDeptId(deptId);
        user.setUserType(UserType.PC_USER.getUserType());
        userDao.insert(user);
        //新增系统用户后，默认当前用户为部门的负责人
        SysDept sd = new SysDept();
        sd.setLeader(user.getUserId());
        sd.setDeptId(deptId);
        deptDao.updateById(sd);

        // 用户和角色关联表
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(roleId);
        userRoleDao.insert(userRole);

        // 从默认租户复制字典和配置数据到新租户
        String defaultTenantId = TenantConstants.DEFAULT_TENANT_ID;

        // 因为在忽略租户中查询，所以必须手动构建租户id过滤 否则查询出来的是所有数据
        List<SysDictType> dictTypeList = dictTypeDao.listByTenantId(defaultTenantId);
        List<SysDictData> dictDataList = dictDataDao.listByTenantId(defaultTenantId);
        List<SysConfig> sysConfigList = configDao.listByTenantId(defaultTenantId);

        // 修改租户ID并清空审计字段，保留 isSystem 属性
        for (SysDictType dictType : dictTypeList) {
            dictType.setDictId(null);
            dictType.setTenantId(tenantId);
            dictType.setCreateDept(null);
            dictType.setCreateBy(null);
            dictType.setCreateTime(null);
            dictType.setUpdateBy(null);
            dictType.setUpdateTime(null);
            // isSystem 属性会保留，用于标记系统级字典
        }
        for (SysDictData dictData : dictDataList) {
            dictData.setDictDataId(null);
            dictData.setTenantId(tenantId);
            dictData.setCreateDept(null);
            dictData.setCreateBy(null);
            dictData.setCreateTime(null);
            dictData.setUpdateBy(null);
            dictData.setUpdateTime(null);
        }
        for (SysConfig config : sysConfigList) {
            config.setConfigId(null);
            config.setTenantId(tenantId);
            config.setCreateDept(null);
            config.setCreateBy(null);
            config.setCreateTime(null);
            config.setUpdateBy(null);
            config.setUpdateTime(null);
        }

        // 批量插入到新租户
        dictTypeDao.batchInsert(dictTypeList);
        dictDataDao.batchInsert(dictDataList);
        configDao.batchInsert(sysConfigList);

        return true;
    }

    /**
     * 生成租户id
     *
     * @param tenantIds 已有租户id列表
     * @return 租户id
     */
    private String generateTenantId(List<String> tenantIds) {
        // 随机生成6位
        String numbers = RandomUtil.randomNumbers(6);
        // 判断是否存在，如果存在则重新生成
        if (tenantIds.contains(numbers)) {
            return generateTenantId(tenantIds);
        }
        return numbers;
    }

    /**
     * 根据租户菜单创建租户角色
     *
     * @param tenantId  租户id
     * @param packageId 租户套餐id
     * @return 角色id
     */
    private Long createTenantRole(String tenantId, Long packageId) {
        // 获取租户套餐
        SysTenantPackage tenantPackage = tenantPackageDao.getById(packageId);
        if (ObjectUtil.isNull(tenantPackage)) {
            throw ServiceException.of("套餐不存在");
        }
        // 获取套餐菜单id
        List<Long> menuIds = StringUtils.splitToList(tenantPackage.getMenuIds(), Convert::toLong);

        // 创建角色
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setRoleName(TenantConstants.TENANT_ADMIN_ROLE_NAME);
        role.setRoleKey(TenantConstants.TENANT_ADMIN_ROLE_KEY);
        role.setRoleSort(1);
        role.setStatus(DictEnableStatus.ENABLE.getValue());
        roleDao.insert(role);
        Long roleId = role.getRoleId();

        // 创建角色菜单
        List<SysRoleMenu> roleMenus = new ArrayList<>(menuIds.size());
        menuIds.forEach(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        });
        roleMenuDao.batchInsert(roleMenus);

        return roleId;
    }

    /**
     * 同步默认租户的普通业务角色到新租户
     *
     * @param tenantId  新租户ID
     * @param packageId 租户套餐ID
     */
    private void syncDefaultRolesToNewTenant(String tenantId, Long packageId) {
        // 获取租户套餐允许的菜单权限
        Set<Long> tenantAllowedMenuIds = getTenantAllowedMenuIds(tenantId);
        if (CollUtil.isEmpty(tenantAllowedMenuIds)) {
            return;
        }

        // 查询默认租户的普通业务角色(排除租户管理员角色和超管角色)
        List<SysRole> defaultRoles = roleDao.listDefaultTenantNormalRoles();

        if (CollUtil.isEmpty(defaultRoles)) {
            return;
        }

        // 获取默认角色的菜单权限
        List<Long> defaultRoleIds = StreamUtils.toList(defaultRoles, SysRole::getRoleId);
        List<SysRoleMenu> defaultRoleMenus = roleMenuDao.listByRoleIds(defaultRoleIds);

        // 准备新租户的角色数据
        List<SysRole> newTenantRoles = new ArrayList<>();
        Map<Long, Long> roleIdMapping = new HashMap<>();

        // 创建角色
        for (SysRole defaultRole : defaultRoles) {
            SysRole newRole = BeanUtil.toBean(defaultRole, SysRole.class);
            newRole.setRoleId(null);
            newRole.setTenantId(tenantId);
            newRole.setCreateTime(null);
            newRole.setUpdateTime(null);
            newRole.setCreateDept(null);
            newRole.setCreateBy(null);
            newRole.setUpdateBy(null);

            newTenantRoles.add(newRole);
        }

        // 批量插入角色
        if (CollUtil.isNotEmpty(newTenantRoles)) {
            roleDao.batchSave(newTenantRoles);

            // 建立角色ID映射关系
            for (int i = 0; i < defaultRoles.size(); i++) {
                roleIdMapping.put(defaultRoles.get(i).getRoleId(), newTenantRoles.get(i).getRoleId());
            }
        }

        // 准备角色菜单关联数据
        List<SysRoleMenu> newRoleMenus = new ArrayList<>();
        for (SysRoleMenu defaultRoleMenu : defaultRoleMenus) {
            Long newRoleId = roleIdMapping.get(defaultRoleMenu.getRoleId());
            Long menuId = defaultRoleMenu.getMenuId();

            // 只分配租户套餐内的菜单权限
            if (newRoleId != null && tenantAllowedMenuIds.contains(menuId)) {
                SysRoleMenu newRoleMenu = new SysRoleMenu();
                newRoleMenu.setRoleId(newRoleId);
                newRoleMenu.setMenuId(menuId);
                newRoleMenus.add(newRoleMenu);
            }
        }

        // 批量插入角色菜单关联
        if (CollUtil.isNotEmpty(newRoleMenus)) {
            roleMenuDao.batchInsert(newRoleMenus);
        }
    }

    /**
     * 修改租户
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "#bo.tenantId"),
        @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "'domain:' + #bo.domain",
            condition = "#bo.domain != null and #bo.domain != ''")
    })
    @Override
    public boolean updateTenant(SysTenantBo bo) {
        SysTenant tenant = MapstructUtils.convert(bo, SysTenant.class);
        tenant.setTenantId(null);
        tenant.setPackageId(null);
        // 如果域名不为空,则去掉前面的协议部分
        tenant.setDomain(StringUtils.contains(tenant.getDomain(), "//") ?
            StringUtils.splitToList(tenant.getDomain(), "//").get(1) : tenant.getDomain());
        return tenantDao.updateById(tenant) > 0;
    }

    /**
     * 修改租户状态
     *
     * @param bo 租户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "#bo.tenantId")
    @Override
    public boolean updateTenantStatus(SysTenantBo bo) {
        SysTenant tenant = new SysTenant();
        tenant.setId(bo.getId());
        tenant.setStatus(bo.getStatus());
        return tenantDao.updateById(tenant) > 0;
    }

    /**
     * 校验租户是否允许操作
     *
     * @param tenantId 租户ID
     */
    @Override
    public void checkTenantAllowed(String tenantId) {
        if (ObjectUtil.isNotNull(tenantId) && TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            throw ServiceException.of("不允许操作管理租户");
        }
    }

    /**
     * 校验企业名称是否唯一
     */
    @Override
    public boolean checkCompanyNameUnique(SysTenantBo bo) {
        boolean exist = tenantDao.existsByCompanyName(bo.getCompanyName(), bo.getTenantId());
        return !exist;
    }

    /**
     * 校验账号余额
     */
    @Override
    public boolean checkAccountBalance(String tenantId) {
        SysTenantVo tenant = SpringUtils.getAopProxy(this).getTenantByTenantId(tenantId);
        // 如果余额为-1代表不限制
        if (tenant.getAccountCount() == -1) {
            return true;
        }
        Long userNumber = userDao.count(null);
        // 如果余额大于0代表还有可用名额
        return tenant.getAccountCount() - userNumber > 0;
    }

    /**
     * 校验有效期
     */
    @Override
    public boolean checkExpireTime(String tenantId) {
        SysTenantVo tenant = SpringUtils.getAopProxy(this).getTenantByTenantId(tenantId);
        // 如果未设置过期时间代表不限制
        if (ObjectUtil.isNull(tenant.getExpireTime())) {
            return true;
        }
        // 如果当前时间在过期时间之前则通过
        return new Date().before(tenant.getExpireTime());
    }

    /**
     * 同步租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncTenantPackage(String tenantId, Long packageId) {
        SysTenantPackage tenantPackage = tenantPackageDao.getById(packageId);
        List<SysRole> roles = roleDao.listByTenantId(tenantId);
        List<Long> roleIds = new ArrayList<>(roles.size() - 1);
        List<Long> menuIds = StringUtils.splitToList(tenantPackage.getMenuIds(), Convert::toLong);
        roles.forEach(item -> {
            if (TenantConstants.TENANT_ADMIN_ROLE_KEY.equals(item.getRoleKey())) {
                List<SysRoleMenu> roleMenus = new ArrayList<>(menuIds.size());
                menuIds.forEach(menuId -> {
                    SysRoleMenu roleMenu = new SysRoleMenu();
                    roleMenu.setRoleId(item.getRoleId());
                    roleMenu.setMenuId(menuId);
                    roleMenus.add(roleMenu);
                });
                roleMenuDao.deleteByRoleId(item.getRoleId());
                roleMenuDao.batchInsert(roleMenus);
            } else {
                roleIds.add(item.getRoleId());
            }
        });
        if (!roleIds.isEmpty() && !menuIds.isEmpty()) {
            roleMenuDao.deleteByRoleIdsExcludeMenuIds(roleIds, menuIds);
        }
        return true;
    }

    /**
     * 同步租户角色
     * 将默认租户的角色数据同步到其他启用的租户中
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncTenantRoles() {
        // 查询超管租户的所有角色数据
        List<SysRole> defaultRoles = new ArrayList<>();
        List<SysRoleMenu> defaultRoleMenus = new ArrayList<>();
        List<SysRoleDept> defaultRoleDepts = new ArrayList<>();

        TenantHelper.ignore(() -> {
            // 获取默认租户的角色数据(排除租户管理员角色)
            defaultRoles.addAll(roleDao.listDefaultTenantNormalRoles());

            // 获取默认租户的角色菜单关联数据
            if (CollUtil.isNotEmpty(defaultRoles)) {
                List<Long> defaultRoleIds = StreamUtils.toList(defaultRoles, SysRole::getRoleId);
                defaultRoleMenus.addAll(roleMenuDao.listByRoleIds(defaultRoleIds));

                // 获取默认租户的角色部门关联数据
                defaultRoleDepts.addAll(roleDeptDao.listByRoleIds(defaultRoleIds));
            }
        });

        if (CollUtil.isEmpty(defaultRoles)) {
            return;
        }

        // 获取所有启用的租户ID(排除默认租户)
        List<String> tenantIds = getEnabledTenantIds();

        if (CollUtil.isEmpty(tenantIds)) {
            return;
        }

        // 准备批量插入的数据
        List<SysRole> saveRoleList = new ArrayList<>();
        List<SysRoleMenu> saveRoleMenuList = new ArrayList<>();
        List<SysRoleDept> saveRoleDeptList = new ArrayList<>();
        Set<String> updatedTenantSet = new HashSet<>();

        for (String tenantId : tenantIds) {
            TenantHelper.dynamic(tenantId, () -> {
                // 查询该租户现有的角色(排除租户管理员角色和超管角色)
                List<SysRole> existingRoles = roleDao.listTenantNormalRoles();

                Map<String, SysRole> existingRoleMap = StreamUtils.toIdentityMap(existingRoles, SysRole::getRoleKey);

                // 记录新增的角色ID映射关系 (默认角色ID -> 新角色ID)
                Map<Long, Long> roleIdMapping = new HashMap<>();

                // 同步角色数据
                for (SysRole defaultRole : defaultRoles) {
                    if (!existingRoleMap.containsKey(defaultRole.getRoleKey())) {
                        // 创建新角色
                        SysRole newRole = BeanUtil.toBean(defaultRole, SysRole.class);
                        newRole.setRoleId(null);
                        newRole.setTenantId(tenantId);

                        saveRoleList.add(newRole);
                        updatedTenantSet.add(tenantId);
                    } else {
                        // 记录已存在的角色ID映射
                        SysRole existingRole = existingRoleMap.get(defaultRole.getRoleKey());
                        roleIdMapping.put(defaultRole.getRoleId(), existingRole.getRoleId());
                    }
                }
            });
        }

        // 批量插入角色数据
        TenantHelper.ignore(() -> {
            if (CollUtil.isNotEmpty(saveRoleList)) {
                roleDao.batchSave(saveRoleList);
            }
        });

        // 重新处理角色关联数据，因为需要获取新插入角色的ID
        for (String tenantId : tenantIds) {
            TenantHelper.dynamic(tenantId, () -> {

                // 重新查询该租户的所有角色
                List<SysRole> tenantRoles = roleDao.listAll();

                Map<String, Long> tenantRoleKeyToIdMap = StreamUtils.toMap(tenantRoles, SysRole::getRoleKey, SysRole::getRoleId);

                // 构建角色ID映射关系
                Map<Long, Long> roleIdMapping = new HashMap<>();
                for (SysRole defaultRole : defaultRoles) {
                    Long tenantRoleId = tenantRoleKeyToIdMap.get(defaultRole.getRoleKey());
                    if (tenantRoleId != null) {
                        roleIdMapping.put(defaultRole.getRoleId(), tenantRoleId);
                    }
                }

                // 查询该租户现有的角色菜单关联
                List<Long> tenantRoleIds = new ArrayList<>(roleIdMapping.values());
                if (CollUtil.isEmpty(tenantRoleIds)) {
                    return;
                }

                List<SysRoleMenu> existingRoleMenus = roleMenuDao.listByRoleIds(tenantRoleIds);

                Set<String> existingRoleMenuKeys = StreamUtils.toSet(existingRoleMenus,
                    rm -> rm.getRoleId() + "_" + rm.getMenuId());

                // 获取该租户的套餐菜单权限范围
                Set<Long> tenantMenuIds = getTenantAllowedMenuIds(tenantId);

                // 同步角色菜单关联数据，但只能分配租户套餐内的菜单权限
                for (SysRoleMenu defaultRoleMenu : defaultRoleMenus) {
                    Long newRoleId = roleIdMapping.get(defaultRoleMenu.getRoleId());
                    Long menuId = defaultRoleMenu.getMenuId();

                    // 关键检查：只有租户套餐包含的菜单才能分配给角色
                    if (newRoleId != null && tenantMenuIds.contains(menuId)) {
                        String key = newRoleId + "_" + menuId;
                        if (!existingRoleMenuKeys.contains(key)) {
                            SysRoleMenu newRoleMenu = new SysRoleMenu();
                            newRoleMenu.setRoleId(newRoleId);
                            newRoleMenu.setMenuId(menuId);
                            saveRoleMenuList.add(newRoleMenu);
                            updatedTenantSet.add(tenantId);
                        }
                    }
                }

                // 查询该租户现有的角色部门关联
                List<SysRoleDept> existingRoleDepts = roleDeptDao.listByRoleIds(tenantRoleIds);

                // 获取该租户的部门ID映射(这里简化处理，实际可能需要更复杂的部门同步逻辑)
                List<SysDept> tenantDepts = deptDao.listAll();

                if (CollUtil.isNotEmpty(tenantDepts)) {
                    Set<String> existingRoleDeptKeys = StreamUtils.toSet(existingRoleDepts,
                        rd -> rd.getRoleId() + "_" + rd.getDeptId());

                    // 同步角色部门关联数据
                    for (SysRoleDept defaultRoleDept : defaultRoleDepts) {
                        Long newRoleId = roleIdMapping.get(defaultRoleDept.getRoleId());
                        if (newRoleId != null) {
                            // 这里简化处理，使用租户的第一个部门
                            // 实际应用中可能需要更sophisticated的部门映射逻辑
                            Long tenantDeptId = tenantDepts.get(0).getDeptId();
                            String key = newRoleId + "_" + tenantDeptId;
                            if (!existingRoleDeptKeys.contains(key)) {
                                SysRoleDept newRoleDept = new SysRoleDept();
                                newRoleDept.setRoleId(newRoleId);
                                newRoleDept.setDeptId(tenantDeptId);
                                saveRoleDeptList.add(newRoleDept);
                                updatedTenantSet.add(tenantId);
                            }
                        }
                    }
                }
            });
        }

        // 批量插入关联数据
        TenantHelper.ignore(() -> {
            if (CollUtil.isNotEmpty(saveRoleMenuList)) {
                roleMenuDao.batchInsert(saveRoleMenuList);
            }
            if (CollUtil.isNotEmpty(saveRoleDeptList)) {
                roleDeptDao.batchInsert(saveRoleDeptList);
            }
        });

    }

    /**
     * 获取租户允许的菜单ID集合
     * 根据租户套餐获取该租户可以使用的所有菜单权限
     *
     * @param tenantId 租户ID
     * @return 租户允许的菜单ID集合
     */
    private Set<Long> getTenantAllowedMenuIds(String tenantId) {
        // 查询租户信息，获取套餐ID
        SysTenant tenant = tenantDao.getPackageIdByTenantId(tenantId);

        if (ObjectUtil.isNull(tenant) || ObjectUtil.isNull(tenant.getPackageId())) {
            return new HashSet<>();
        }

        // 查询租户套餐，获取菜单权限
        SysTenantPackage tenantPackage = tenantPackageDao.getById(tenant.getPackageId());
        if (ObjectUtil.isNull(tenantPackage) || StringUtils.isBlank(tenantPackage.getMenuIds())) {
            return new HashSet<>();
        }

        // 解析套餐菜单ID列表
        List<Long> menuIds = StringUtils.splitToList(tenantPackage.getMenuIds(), Convert::toLong);
        return new HashSet<>(menuIds);
    }

    /**
     * 获取所有启用的租户ID(排除默认租户)
     */
    private List<String> getEnabledTenantIds() {
        return tenantDao.listEnabledTenantIds(TenantConstants.DEFAULT_TENANT_ID);
    }

    /**
     * 同步租户字典
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncTenantDicts() {
        // 查询所有租户的字典数据（需要查询全部用于对比）
        List<SysDictType> dictTypeList = new ArrayList<>();
        List<SysDictData> dictDataList = new ArrayList<>();
        TenantHelper.ignore(() -> {
            dictTypeList.addAll(dictTypeDao.listAll());
            dictDataList.addAll(dictDataDao.listAll());
        });
        Map<String, List<SysDictType>> typeMap = StreamUtils.groupByKey(dictTypeList, TenantEntity::getTenantId);
        Map<String, Map<String, List<SysDictData>>> typeDataMap = StreamUtils.groupBy2Key(
            dictDataList, TenantEntity::getTenantId, SysDictData::getDictType);
        // 管理租户字典数据
        List<SysDictType> defaultTypeMap = typeMap.get(TenantConstants.DEFAULT_TENANT_ID);
        Map<String, List<SysDictData>> defaultTypeDataMap = typeDataMap.get(TenantConstants.DEFAULT_TENANT_ID);

        // 获取所有租户id
        List<String> tenantIds = getEnabledTenantIds();
        tenantIds.add(TenantConstants.DEFAULT_TENANT_ID); // 添加默认租户用于后续判断
        List<SysDictType> saveTypeList = new ArrayList<>();
        List<SysDictData> saveDataList = new ArrayList<>();
        Set<String> set = new HashSet<>();
        // 存储需要更新 isSystem 属性的字典类型
        List<SysDictType> updateTypeList = new ArrayList<>();

        for (String tenantId : tenantIds) {
            if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
                continue;
            }
            for (SysDictType dictType : defaultTypeMap) {
                List<SysDictData> dataList = defaultTypeDataMap.get(dictType.getDictType());
                List<SysDictType> existingTypes = typeMap.get(tenantId);

                // 检查租户中是否已存在该字典类型
                SysDictType existingType = null;
                if (CollUtil.isNotEmpty(existingTypes)) {
                    for (SysDictType existing : existingTypes) {
                        if (existing.getDictType().equals(dictType.getDictType())) {
                            existingType = existing;
                            break;
                        }
                    }
                }

                if (existingType != null) {
                    // 字典类型已存在，检查是否需要更新 isSystem 属性
                    if (!Objects.equals(existingType.getIsSystem(), dictType.getIsSystem())) {
                        SysDictType updateType = new SysDictType();
                        updateType.setDictId(existingType.getDictId());
                        updateType.setDictType(dictType.getDictType());
                        updateType.setIsSystem(dictType.getIsSystem());
                        updateType.setTenantId(tenantId);
                        updateTypeList.add(updateType);
                        set.add(tenantId);
                    }

                    List<SysDictData> tenantDictDataList = typeDataMap.get(tenantId).get(dictType.getDictType());
                    Map<String, SysDictData> map = StreamUtils.toIdentityMap(tenantDictDataList, SysDictData::getDictValue);
                    for (SysDictData dictData : dataList) {
                        if (!map.containsKey(dictData.getDictValue())) {
                            SysDictData data = BeanUtil.toBean(dictData, SysDictData.class);
                            // 设置字典编码为 null
                            data.setDictDataId(null);
                            data.setTenantId(tenantId);
                            data.setCreateTime(null);
                            data.setUpdateTime(null);
                            data.setCreateDept(null);
                            data.setCreateBy(null);
                            data.setUpdateBy(null);
                            set.add(tenantId);
                            saveDataList.add(data);
                        }
                    }
                } else {
                    SysDictType type = BeanUtil.toBean(dictType, SysDictType.class);
                    type.setDictId(null);
                    type.setTenantId(tenantId);
                    type.setCreateTime(null);
                    type.setUpdateTime(null);
                    // isSystem 属性会保留，用于标记系统级字典
                    type.setCreateDept(null);
                    type.setCreateBy(null);
                    type.setUpdateBy(null);
                    set.add(tenantId);
                    saveTypeList.add(type);
                    if (CollUtil.isNotEmpty(dataList)) {
                        // 筛选出 dictType 对应的 data
                        for (SysDictData dictData : dataList) {
                            SysDictData data = BeanUtil.toBean(dictData, SysDictData.class);
                            // 设置字典编码为 null
                            data.setDictDataId(null);
                            data.setTenantId(tenantId);
                            data.setCreateTime(null);
                            data.setUpdateTime(null);
                            data.setCreateDept(null);
                            data.setCreateBy(null);
                            data.setUpdateBy(null);
                            set.add(tenantId);
                            saveDataList.add(data);
                        }
                    }
                }
            }
        }
        TenantHelper.ignore(() -> {
            // 批量插入新增的字典类型
            if (CollUtil.isNotEmpty(saveTypeList)) {
                dictTypeDao.batchInsert(saveTypeList);
            }
            // 批量插入新增的字典数据
            if (CollUtil.isNotEmpty(saveDataList)) {
                dictDataDao.batchInsert(saveDataList);
            }
            // 批量更新已存在字典类型的 isSystem 属性
            if (CollUtil.isNotEmpty(updateTypeList)) {
                for (SysDictType updateType : updateTypeList) {
                    // 使用 SQL 直接更新，确保 isSystem 字段被更新
                    PlusLambdaQuery<SysDictType> updateWrapper = PlusLambdaQuery.of();
                    updateWrapper.eq(SysDictType::getDictId, updateType.getDictId());
                    dictTypeDao.update(updateType, updateWrapper);
                }
            }
        });
        for (String tenantId : set) {
            TenantHelper.dynamic(tenantId, () -> CacheUtils.clear(CacheNames.SYS_DICT));
        }
    }

    /**
     * 获取租户id
     *
     * @return 租户id
     */
    @Override
    public String getTenantId() {
        return TenantHelper.getTenantId();
    }


    /**
     * 根据请求获取租户ID
     * 专门处理从请求中提取租户信息的逻辑：域名识别 + 请求头获取
     *
     * @return 租户ID，如果获取不到返回null
     */
    @Override
    public String getTenantIdByRequest() {
        // 获取当前请求对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (!TenantHelper.isEnable() || request == null) {
            return null;
        }

        // 1. 优先通过域名识别租户
        String host = extractHostFromRequest(request);
        String tenantId = SpringUtils.getAopProxy(this).getTenantIdByDomain(host);

        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId;
        }

        // 2. 从请求头 X-Tenant-Id 获取
        try {
            tenantId = request.getHeader("X-Tenant-Id");
            if (StringUtils.isNotBlank(tenantId)) {
                return tenantId;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * 根据域名获取租户ID
     *
     * @param domain 域名
     * @return 匹配的租户ID，如果没有匹配则返回null
     */
    @Cacheable(cacheNames = CacheNames.SYS_TENANT, key = "'domain:' + #domain")
    public String getTenantIdByDomain(String domain) {
        if (StringUtils.isBlank(domain)) {
            return null;
        }
        //根据域名和状态查询租户信息
        SysTenant tenant = tenantDao.getByDomainAndStatus(domain, DictEnableStatus.ENABLE.getValue(), false);
        if (ObjectUtil.isNotNull(tenant)) {
            return tenant.getTenantId();
        }
        return null;
    }

    /**
     * 从请求中提取域名
     *
     * @param request HTTP请求对象
     * @return 域名
     */
    private String extractHostFromRequest(HttpServletRequest request) {
        try {
            String referer = request.getHeader("referer");
            if (StringUtils.isNotBlank(referer)) {
                // 从referer中取值，方便本地环境使用hosts添加虚拟域名调试
                return referer.split("//")[1].split("/")[0];
            } else {
                // 从请求URL中提取域名
                return URI.create(request.getRequestURL().toString()).getHost();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前租户的标题
     * <p>
     * 如果租户ID不为空，则查询租户信息并返回公司名称，否则返回空字符串
     *
     * @return 当前租户的标题
     */
    @Override
    public String getTenantTitle() {
        String tenantId = TenantHelper.getTenantId();
        if (StringUtils.isNotBlank(tenantId)) {
            SysTenantVo tenant = SpringUtils.getAopProxy(this).getTenantByTenantId(tenantId);
            if (ObjectUtil.isNotNull(tenant)) {
                return tenant.getCompanyName();
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 同步租户参数配置
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncTenantConfigs() {
        // 查询超管 所有参数配置
        List<SysConfig> configList = TenantHelper.ignore(configDao::listAll);

        // 所有租户参数配置
        Map<String, List<SysConfig>> configMap = StreamUtils.groupByKey(configList, TenantEntity::getTenantId);

        // 默认租户字典类型列表
        List<SysConfig> defaultConfigList = configMap.get(TenantConstants.DEFAULT_TENANT_ID);

        // 获取所有租户编号
        List<String> tenantIds = getEnabledTenantIds();
        tenantIds.add(TenantConstants.DEFAULT_TENANT_ID); // 添加默认租户用于后续判断
        // 待入库的字典类型和字典数据
        List<SysConfig> saveConfigList = new ArrayList<>();
        // 待同步的租户编号（用于清除对于租户的字典缓存）
        Set<String> syncTenantIds = new HashSet<>();
        // 循环所有租户，处理需要同步的数据
        for (String tenantId : tenantIds) {
            // 排除默认租户
            if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
                continue;
            }
            // 根据默认租户的字典类型进行数据同步
            for (SysConfig config : defaultConfigList) {
                // 获取当前租户的字典类型列表
                List<String> typeList = StreamUtils.toList(configMap.get(tenantId), SysConfig::getConfigKey);
                if (!typeList.contains(config.getConfigKey())) {
                    SysConfig type = BeanUtil.toBean(config, SysConfig.class);
                    type.setConfigId(null);
                    type.setTenantId(tenantId);
                    type.setCreateTime(null);
                    type.setUpdateTime(null);
                    syncTenantIds.add(tenantId);
                    saveConfigList.add(type);
                }
            }
        }
        TenantHelper.ignore(() -> {
            if (CollUtil.isNotEmpty(saveConfigList)) {
                configDao.batchInsert(saveConfigList);
            }
        });
        for (String tenantId : syncTenantIds) {
            TenantHelper.dynamic(tenantId, () -> CacheUtils.clear(CacheNames.SYS_CONFIG));
        }
    }
}
