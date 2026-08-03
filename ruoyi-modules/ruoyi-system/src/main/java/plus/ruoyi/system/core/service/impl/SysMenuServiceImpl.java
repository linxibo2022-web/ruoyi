package plus.ruoyi.system.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictDisplaySetting;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.MenuService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.core.utils.TreeBuildUtils;
import plus.ruoyi.common.openapi.config.OpenApiProperties;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.system.core.dao.ISysMenuDao;
import plus.ruoyi.system.core.dao.ISysRoleDao;
import plus.ruoyi.system.core.dao.ISysRoleMenuDao;
import plus.ruoyi.system.core.domain.SysMenu;
import plus.ruoyi.system.core.domain.SysRole;
import plus.ruoyi.system.tenant.domain.SysTenantPackage;
import plus.ruoyi.system.core.domain.bo.SysMenuBo;
import plus.ruoyi.system.core.domain.vo.MetaVo;
import plus.ruoyi.system.core.domain.vo.RouterVo;
import plus.ruoyi.system.core.domain.vo.SysMenuVo;
import plus.ruoyi.system.tenant.dao.ISysTenantPackageDao;
import plus.ruoyi.system.core.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl implements ISysMenuService, MenuService {

    private final ISysMenuDao menuDao;
    private final ISysTenantPackageDao tenantPackageDao;
    private final ISysRoleMenuDao roleMenuDao;
    private final ISysRoleDao roleDao;
    @Autowired(required = false)
    private OpenApiProperties openApiProperties;
    /**
     * 租户相关的菜单ID集合
     * 包含租户管理目录、菜单和相关按钮权限
     */
    private static final Set<Long> TENANT_MENU_IDS = Set.of(
        // 租户管理目录
        6L,
        // 租户管理菜单
        121L,
        // 租户套餐菜单
        122L,
        // 租户管理按钮
        1606L, 1607L, 1608L, 1609L, 1610L,
        // 租户套餐按钮
        1611L, 1612L, 1613L, 1614L, 1615L
    );

    /**
     * 租户套餐不允许分配的菜单ID集合
     * 包含系统核心管理功能，不应开放给租户
     */
    private static final Set<Long> TENANT_PACKAGE_EXCLUDED_MENU_IDS = Set.of(
        // 租户管理相关（租户不能管理租户）
        6L, 121L, 122L,
        1606L, 1607L, 1608L, 1609L, 1610L,
        1611L, 1612L, 1613L, 1614L, 1615L,

        // 菜单管理相关（租户不能管理系统菜单）
        102L, 1013L, 1014L, 1015L, 1016L,

        // 文件管理OSS配置相关
        1620L, 1621L, 1622L, 1623L,

        // 系统监控相关
        117L, 120L,

        // 系统工具相关
        115L, 1055L, 1056L, 1057L, 1058L, 1059L, 1060L
    );

    /**
     * 开放平台相关的菜单ID集合
     * 包括开放平台目录、菜单和相关按钮权限
     */
    private static final Set<Long> OPEN_API_MENU_IDS = Set.of(
        // 开放平台目录
        1120L,
        // API密钥管理菜单
        1121L,
        // API密钥管理按钮
        1122L, 1123L, 1124L, 1125L, 1126L
        // 根据实际的菜单ID修改
    );

    // ================ 菜单查询相关方法 =================

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuVo> listMenuByUserId(Long userId) {
        return listMenus(new SysMenuBo(), userId);
    }

    /**
     * 查询系统菜单列表
     * <p>
     * 根据用户权限和查询条件获取菜单列表，支持管理员和普通用户不同的查询逻辑
     *
     * @param menuBo 菜单查询条件
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuVo> listMenus(SysMenuBo menuBo, Long userId) {
        List<SysMenuVo> menuList;
        List<SysMenu> list;

        // 构建需要排除的菜单ID集合
        Set<Long> excludeMenuIds = new HashSet<>();

        // 如果租户功能未开启，排除租户相关菜单
        if (!TenantHelper.isEnable()) {
            excludeMenuIds.addAll(TENANT_MENU_IDS);
        }
        // 如果开放平台功能未开启,排除开放平台相关菜单
        if (!isOpenApiEnabled()) {
            excludeMenuIds.addAll(OPEN_API_MENU_IDS);
        }

        // 管理员显示所有菜单信息
        if (LoginHelper.isSuperAdmin(userId)) {
            // 使用 DAO 层方法查询
            list = menuDao.listMenusForAdmin(menuBo, excludeMenuIds);
        } else {
            // 普通用户根据角色查询菜单
            list = menuDao.listMenusForUser(menuBo, userId, excludeMenuIds);
        }

        // 转换为VO对象并设置国际化键
        menuList = MapstructUtils.convert(list, SysMenuVo.class);

        // 一次性获取所有菜单数据，避免重复查询
        Map<Long, SysMenu> allMenuMap = getAllMenuMap();

        // 为菜单列表添加国际化键
        if (CollUtil.isNotEmpty(menuList)) {
            // 为每个菜单VO设置国际化键
            for (int i = 0; i < list.size(); i++) {
                SysMenu originalMenu = list.get(i);
                SysMenuVo menuVo = menuList.get(i);

                // 使用原始菜单对象生成国际化键
                String i18nKey = generateI18nKey(originalMenu, allMenuMap);
                menuVo.setI18nKey(i18nKey);
            }
        }

        return menuList;
    }

    /**
     * 根据用户ID查询菜单树
     *
     * @param userId 用户ID
     * @return 菜单树列表
     */
    @Override
    public List<SysMenu> listMenuTreeByUserId(Long userId) {
        List<SysMenu> menuList;
        if (LoginHelper.isSuperAdmin(userId)) {
            menuList = menuDao.listMenuTreeAll();
        } else {
            menuList = menuDao.listMenusByUserId(userId);
        }

        // 过滤租户相关菜单
        menuList = filterTenantMenus(menuList);

        // 过滤开放平台相关菜单
        menuList = filterOpenApiMenus(menuList);

        return getChildPerms(menuList, 0);
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenuVo getMenuById(Long menuId) {
        // 检查是否为租户相关菜单
        if (!TenantHelper.isEnable() && TENANT_MENU_IDS.contains(menuId)) {
            return null;
        }

        // 检查是否为开放平台相关菜单
        if (!isOpenApiEnabled() && OPEN_API_MENU_IDS.contains(menuId)) {
            return null;
        }

        SysMenu menu = menuDao.getById(menuId);
        return MapstructUtils.convert(menu, SysMenuVo.class);
    }

    // ================ 权限查询相关方法 =================

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> listMenuPermissionsByUserId(Long userId) {
        List<String> perms = menuDao.listMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(StringUtils.splitToList(perm.trim()));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> listMenuPermissionsByRoleId(Long roleId) {
        List<String> perms = menuDao.listMenuPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(StringUtils.splitToList(perm.trim()));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        // 获取角色的menuCheckStrictly属性
        SysRole role = roleDao.getById(roleId);
        boolean menuCheckStrictly = role != null && role.getMenuCheckStrictly();
        // 根据menuCheckStrictly决定是否过滤父节点
        List<Long> menuIds = menuDao.listMenuIdsByRoleId(roleId, menuCheckStrictly);

        // 如果租户功能未开启，过滤掉租户相关菜单ID
        if (!TenantHelper.isEnable()) {
            menuIds = menuIds.stream()
                .filter(menuId -> !TENANT_MENU_IDS.contains(menuId))
                .collect(Collectors.toList());
        }

        // 如果开放平台功能未开启,过滤掉开放平台相关菜单ID
        if (!isOpenApiEnabled()) {
            menuIds = menuIds.stream()
                .filter(menuId -> !OPEN_API_MENU_IDS.contains(menuId))
                .collect(Collectors.toList());
        }

        return menuIds;
    }

    /**
     * 根据租户套餐ID查询菜单id列表
     *
     * @param packageId 租户套餐ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        SysTenantPackage tenantPackage = tenantPackageDao.getById(packageId);
        List<Long> menuIds = StringUtils.splitToList(tenantPackage.getMenuIds(), Convert::toLong);
        if (CollUtil.isEmpty(menuIds)) {
            return List.of();
        }

        List<Long> parentIds = null;
        if (tenantPackage.getMenuCheckStrictly()) {
            parentIds = menuDao.listParentIdsByMenuIds(menuIds);
        }

        return menuDao.listMenuIdsByMenuIdsExcludeParents(menuIds, parentIds);
    }

    // ================ 路由构建相关方法 =================

    /**
     * 构建前端路由所需要的菜单
     * <p>
     * 路由name命名规则：path首字母转大写 + id
     *
     * @param menuList 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildRouters(List<SysMenu> menuList) {
        // 一次性获取全部菜单并创建映射，避免重复查询
        Map<Long, SysMenu> allMenuMap = getAllMenuMap();
        return buildMenusWithMenuMap(menuList, allMenuMap);
    }

    /**
     * 使用预先加载的菜单映射构建路由
     *
     * @param menuList 菜单列表
     * @param menuMap  菜单映射
     * @return 路由列表
     */
    private List<RouterVo> buildMenusWithMenuMap(List<SysMenu> menuList, Map<Long, SysMenu> menuMap) {
        List<RouterVo> routers = new LinkedList<>();
        for (SysMenu menu : menuList) {
            String name = menu.getRouteName() + menu.getMenuId();
            RouterVo router = new RouterVo();
            router.setHidden(DictDisplaySetting.HIDE.getValue().equals(menu.getVisible()));
            router.setName(name);
            router.setPath(menu.getRouterPath());
            router.setComponent(menu.getComponentInfo());
            router.setQuery(menu.getQueryParam());

            // 生成菜单的国际化键 - 使用预加载的菜单映射
            String i18nKey = generateI18nKey(menu, menuMap);

            // 处理外链菜单 - 直接在新窗口打开，不需要子路由
            if (menu.isExternalLink()) {
                router.setMeta(new MetaVo(
                    menu.getMenuName(),
                    menu.getIcon(),
                    false, // 外链不需要缓存
                    menu.getPath(), // 使用原始URL
                    i18nKey
                ));
                // 外链不需要子菜单处理
                routers.add(router);
                continue;
            }

            // 创建包含国际化键的 MetaVo
            router.setMeta(new MetaVo(
                menu.getMenuName(),
                menu.getIcon(),
                StringUtils.equals(DictBooleanFlag.NO.getValue(), menu.getIsCache()),
                menu.getPath(),
                i18nKey
            ));

            List<SysMenu> cMenus = menu.getChildren();
            if (CollUtil.isNotEmpty(cMenus) && SystemConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenusWithMenuMap(cMenus, menuMap));
            } else if (menu.isMenuFrame()) {
                // 内链菜单框架处理
                String frameName = StringUtils.capitalize(menu.getPath()) + menu.getMenuId();
                router.setMeta(null);
                router.setName(StringUtils.EMPTY);  // 父路由不设置name，避免潜在冲突
                router.setComponent(SystemConstants.LAYOUT);  // 父路由使用Layout组件
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(frameName);

                // 为子菜单设置国际化键
                children.setMeta(new MetaVo(
                    menu.getMenuName(),
                    menu.getIcon(),
                    StringUtils.equals(DictBooleanFlag.NO.getValue(), menu.getIsCache()),
                    menu.getPath(),
                    i18nKey
                ));

                children.setQuery(menu.getQueryParam());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().intValue() == Constants.TOP_PARENT_ID && SystemConstants.TYPE_MENU.equals(menu.getMenuType()) && !menu.isInnerLink()) {
                // 一级普通菜单处理（非内链）- 需要创建 Layout + 单个子路由结构
                router.setMeta(null);
                router.setName(StringUtils.EMPTY);  // 父路由不设置name，避免与子路由冲突
                router.setComponent(SystemConstants.LAYOUT);  // 父路由使用Layout组件
                router.setPath("/" + menu.getPath());
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());  // 子路由使用具体组件
                children.setName(name);  // 只有子路由使用name

                // 为子菜单设置国际化键
                children.setMeta(new MetaVo(
                    menu.getMenuName(),
                    menu.getIcon(),
                    StringUtils.equals(DictBooleanFlag.NO.getValue(), menu.getIsCache()),
                    menu.getPath(),
                    i18nKey
                ));

                children.setQuery(menu.getQueryParam());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().intValue() == Constants.TOP_PARENT_ID && menu.isInnerLink()) {
                // 顶级内链处理
                router.setMeta(new MetaVo(
                    menu.getMenuName(),
                    menu.getIcon(),
                    false,
                    null,
                    i18nKey
                ));

                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                String routerPath = SysMenu.innerLinkReplaceEach(menu.getPath());
                String innerLinkName = StringUtils.capitalize(routerPath) + menu.getMenuId();
                children.setPath(routerPath);
                children.setComponent(SystemConstants.INNER_LINK);
                children.setName(innerLinkName);

                // 为内链子菜单设置国际化键
                children.setMeta(new MetaVo(
                    menu.getMenuName(),
                    menu.getIcon(),
                    false,
                    menu.getPath(),
                    i18nKey
                ));

                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    // ================ 树形结构构建方法 =================

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menuVoList 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildMenuTreeOptions(List<SysMenuVo> menuVoList) {
        if (CollUtil.isEmpty(menuVoList)) {
            return CollUtil.newArrayList();
        }

        // 过滤租户相关菜单
        menuVoList = filterTenantMenuVos(menuVoList);

        // 过滤开放平台相关菜单
        menuVoList = filterOpenApiMenuVos(menuVoList);

        return TreeBuildUtils.build(menuVoList, (menuVo, tree) -> {
            Tree<Long> menuTree = tree.setId(menuVo.getMenuId())
                .setParentId(menuVo.getParentId())
                .setName(menuVo.getMenuName())
                .setWeight(menuVo.getOrderNum());
            menuTree.put("menuType", menuVo.getMenuType());
            menuTree.put("icon", menuVo.getIcon());
        });
    }

    /**
     * 构建租户套餐菜单下拉树结构
     * 专门用于租户套餐分配菜单，会过滤掉系统核心管理功能
     *
     * @param menuVoList 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildTenantPackageMenuTreeOptions(List<SysMenuVo> menuVoList) {
        if (CollUtil.isEmpty(menuVoList)) {
            return CollUtil.newArrayList();
        }

        // 过滤租户套餐不允许的菜单
        menuVoList = filterTenantPackageMenuVos(menuVoList);

        return TreeBuildUtils.build(menuVoList, (menuVo, tree) -> {
            Tree<Long> menuTree = tree.setId(menuVo.getMenuId())
                .setParentId(menuVo.getParentId())
                .setName(menuVo.getMenuName())
                .setWeight(menuVo.getOrderNum());
            menuTree.put("menuType", menuVo.getMenuType());
            menuTree.put("icon", menuVo.getIcon());
        });
    }

    // ================ 业务校验相关方法 =================

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        // 构建需要排除的菜单ID集合
        Set<Long> excludeMenuIds = new HashSet<>();

        // 如果租户功能未开启，排除租户相关菜单
        if (!TenantHelper.isEnable()) {
            excludeMenuIds.addAll(TENANT_MENU_IDS);
        }

        // 如果开放平台功能未开启,排除开放平台相关菜单
        if (!isOpenApiEnabled()) {
            excludeMenuIds.addAll(OPEN_API_MENU_IDS);
        }

        return menuDao.hasChildByMenuId(menuId, excludeMenuIds);
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        return roleMenuDao.countByMenuId(menuId) > 0;
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenuBo menuBo) {
        // 构建需要排除的菜单ID集合
        Set<Long> excludeMenuIds = new HashSet<>();

        // 如果租户功能未开启，排除租户相关菜单
        if (!TenantHelper.isEnable()) {
            excludeMenuIds.addAll(TENANT_MENU_IDS);
        }

        // 如果开放平台功能未开启,排除开放平台相关菜单
        if (!isOpenApiEnabled()) {
            excludeMenuIds.addAll(OPEN_API_MENU_IDS);
        }

        return menuDao.checkMenuNameUnique(menuBo.getMenuName(), menuBo.getParentId(), menuBo.getMenuId(), excludeMenuIds);
    }

    // ================ 增删改相关方法 =================

    /**
     * 新增保存菜单信息
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    @Override
    public Long insertMenu(SysMenuBo menuBo) {
        // 检查是否尝试创建租户相关菜单（租户功能未开启时）
        if (!TenantHelper.isEnable() && menuBo.getParentId() != null
            && TENANT_MENU_IDS.contains(menuBo.getParentId())) {
            throw ServiceException.of("租户功能未开启，不允许在租户相关目录下创建菜单");
        }

        // 检查是否尝试创建开放平台相关菜单(开放平台功能未开启时)
        if (!isOpenApiEnabled() && menuBo.getParentId() != null
            && OPEN_API_MENU_IDS.contains(menuBo.getParentId())) {
            throw ServiceException.of("开放平台功能未开启,不允许在开放平台相关目录下创建菜单");
        }

        // 如果没有提供图标，设置默认值 #
        if (StringUtils.isBlank(menuBo.getIcon())) {
            menuBo.setIcon("#");
        }

        SysMenu menu = MapstructUtils.convert(menuBo, SysMenu.class);
        menuDao.insert(menu);
        // 设置菜单ID
        menuBo.setMenuId(menu.getMenuId());
        return menu.getMenuId();
    }

    /**
     * 修改保存菜单信息
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    @Override
    public boolean updateMenu(SysMenuBo menuBo) {
        // 检查是否为租户相关菜单
        if (!TenantHelper.isEnable() && menuBo.getMenuId() != null
            && TENANT_MENU_IDS.contains(menuBo.getMenuId())) {
            throw ServiceException.of("租户功能未开启，不允许修改租户相关菜单");
        }

        // 检查是否为开放平台相关菜单
        if (!isOpenApiEnabled() && menuBo.getMenuId() != null
            && OPEN_API_MENU_IDS.contains(menuBo.getMenuId())) {
            throw ServiceException.of("开放平台功能未开启,不允许修改开放平台相关菜单");
        }

        // 如果没有提供图标，设置默认值 #
        if (StringUtils.isBlank(menuBo.getIcon())) {
            menuBo.setIcon("#");
        }

        SysMenu menu = MapstructUtils.convert(menuBo, SysMenu.class);
        return menuDao.updateById(menu) > 0;
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean deleteMenuById(Long menuId) {
        // 检查是否为租户相关菜单
        if (!TenantHelper.isEnable() && TENANT_MENU_IDS.contains(menuId)) {
            throw ServiceException.of("租户功能未开启，不允许删除租户相关菜单");
        }

        // 检查是否为开放平台相关菜单
        if (!isOpenApiEnabled() && OPEN_API_MENU_IDS.contains(menuId)) {
            throw ServiceException.of("开放平台功能未开启,不允许删除开放平台相关菜单");
        }
        return menuDao.deleteById(menuId) > 0;
    }

    // ================ 私有辅助方法 =================

    /**
     * 检查开放平台功能是否启用
     *
     * @return 是否启用
     */
    private boolean isOpenApiEnabled() {
        return openApiProperties != null && openApiProperties.getEnabled();
    }

    /**
     * 一次性获取所有菜单数据并构建映射
     *
     * @return 菜单ID到菜单对象的映射
     */
    private Map<Long, SysMenu> getAllMenuMap() {
        // 一次性查询所有菜单
        List<SysMenu> allMenus = menuDao.listAll();

        // 过滤租户相关菜单
        allMenus = filterTenantMenus(allMenus);

        // 过滤开放平台相关菜单
        allMenus = filterOpenApiMenus(allMenus);

        // 构建ID到菜单对象的映射
        return allMenus.stream().collect(
            Collectors.toMap(SysMenu::getMenuId, menu -> menu, (v1, v2) -> v1));
    }

    /**
     * 过滤租户相关的菜单
     *
     * @param menuList 原始菜单列表
     * @return 过滤后的菜单列表
     */
    private List<SysMenu> filterTenantMenus(List<SysMenu> menuList) {
        // 如果租户功能已开启，不需要过滤
        if (TenantHelper.isEnable()) {
            return menuList;
        }

        return menuList.stream()
            .filter(menu -> !TENANT_MENU_IDS.contains(menu.getMenuId()))
            .collect(Collectors.toList());
    }

    /**
     * 过滤租户相关的菜单VO
     *
     * @param menuVoList 原始菜单VO列表
     * @return 过滤后的菜单VO列表
     */
    private List<SysMenuVo> filterTenantMenuVos(List<SysMenuVo> menuVoList) {
        // 如果租户功能已开启，不需要过滤
        if (TenantHelper.isEnable()) {
            return menuVoList;
        }

        return menuVoList.stream()
            .filter(menu -> !TENANT_MENU_IDS.contains(menu.getMenuId()))
            .collect(Collectors.toList());
    }

    /**
     * 过滤租户套餐不允许的菜单VO
     *
     * @param menuVoList 原始菜单VO列表
     * @return 过滤后的菜单VO列表
     */
    private List<SysMenuVo> filterTenantPackageMenuVos(List<SysMenuVo> menuVoList) {
        return menuVoList.stream()
            .filter(menu -> !TENANT_PACKAGE_EXCLUDED_MENU_IDS.contains(menu.getMenuId()))
            .collect(Collectors.toList());
    }

    /**
     * 过滤开放平台相关的菜单
     *
     * @param menuList 原始菜单列表
     * @return 过滤后的菜单列表
     */
    private List<SysMenu> filterOpenApiMenus(List<SysMenu> menuList) {
        // 如果开放平台功能已开启,不需要过滤
        if (isOpenApiEnabled()) {
            return menuList;
        }

        return menuList.stream()
            .filter(menu -> !OPEN_API_MENU_IDS.contains(menu.getMenuId()))
            .collect(Collectors.toList());
    }

    /**
     * 过滤开放平台相关的菜单VO
     *
     * @param menuVoList 原始菜单VO列表
     * @return 过滤后的菜单VO列表
     */
    private List<SysMenuVo> filterOpenApiMenuVos(List<SysMenuVo> menuVoList) {
        // 如果开放平台功能已开启,不需要过滤
        if (isOpenApiEnabled()) {
            return menuVoList;
        }

        return menuVoList.stream()
            .filter(menu -> !OPEN_API_MENU_IDS.contains(menu.getMenuId()))
            .collect(Collectors.toList());
    }

    /**
     * 生成菜单的国际化键
     *
     * @param menu    菜单对象
     * @param menuMap 菜单ID到菜单对象的映射
     * @return 国际化键
     */
    private String generateI18nKey(SysMenu menu, Map<Long, SysMenu> menuMap) {
        // 按钮类型，使用权限标识作为键
        if (SystemConstants.TYPE_BUTTON.equals(menu.getMenuType()) && StringUtils.isNotEmpty(menu.getPerms())) {
            return "button." + menu.getPerms().replace(":", ".");
        }

        // 递归构建完整的菜单路径链
        List<String> pathChain = new ArrayList<>();
        buildMenuPathChain(menu, pathChain, menuMap);

        // 如果成功构建了路径链
        if (!pathChain.isEmpty()) {
            String keyPath = String.join(".", pathChain);

            // 目录节点需要追加 _self 属性
            if (SystemConstants.TYPE_DIR.equals(menu.getMenuType())) {
                return "menu." + keyPath + "._self";
            }
            return "menu." + keyPath;
        }

        // 对于内链，使用特殊前缀
        if (menu.isInnerLink()) {
            return "menu.link." + menu.getMenuId();
        }

        // 默认情况：使用菜单ID作为降级方案
        return "menu.id." + menu.getMenuId();
    }

    /**
     * 递归构建菜单路径链
     *
     * @param menu      当前菜单
     * @param pathChain 路径链集合
     * @param menuMap   菜单ID到菜单对象的映射
     */
    private void buildMenuPathChain(SysMenu menu, List<String> pathChain, Map<Long, SysMenu> menuMap) {
        if (menu == null) {
            return;
        }

        // 获取当前菜单路径
        String currentPath = menu.getPath();
        if (StringUtils.isNotEmpty(currentPath)) {
            currentPath = currentPath.startsWith("/") ? currentPath.substring(1) : currentPath;
            // 将当前路径添加到链头部
            pathChain.add(0, currentPath);
        }

        // 如果有父菜单，递归处理
        if (menu.getParentId() != null && menu.getParentId() != 0) {
            SysMenu parentMenu = menuMap.get(menu.getParentId());
            if (parentMenu != null) {
                buildMenuPathChain(parentMenu, pathChain, menuMap);
            }
        }
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     菜单列表
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    private List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (SysMenu menu : list) {
            // 根据传入的父节点ID，遍历该父节点的所有子节点
            if (menu.getParentId() == parentId) {
                recursionFn(list, menu);
                returnList.add(menu);
            }
        }
        return returnList;
    }

    /**
     * 递归列表构建菜单树
     *
     * @param list 菜单列表
     * @param menu 当前菜单
     */
    private void recursionFn(List<SysMenu> list, SysMenu menu) {
        // 得到子节点列表
        List<SysMenu> childList = StreamUtils.filter(list, n -> n.getParentId().equals(menu.getMenuId()));
        menu.setChildren(childList);

        for (SysMenu childMenu : childList) {
            // 判断是否有子节点
            if (list.stream().anyMatch(n -> n.getParentId().equals(childMenu.getMenuId()))) {
                recursionFn(list, childMenu);
            }
        }
    }

    /**
     * 根据权限字符串检查菜单是否存在
     * <p>用于代码生成器自动导入菜单时的去重判断</p>
     *
     * @param perms 权限字符串（如: base:ad:view）
     * @return 是否存在
     */
    @Override
    public boolean existsByPerms(String perms) {
        // Service 层只调用 DAO 方法，不构建查询条件
        return menuDao.existsByPerms(perms);
    }

    /**
     * 根据权限字符串获取菜单名称
     * <p>用于权限校验失败时提供友好的错误提示</p>
     *
     * @param perms 权限字符串（如: base:ad:query）
     * @return 菜单名称，如果未找到则返回null
     */
    @Override
    public String getMenuNameByPerms(String perms) {
        // Service 层只调用 DAO 方法
        return menuDao.getMenuNameByPerms(perms);
    }
}
