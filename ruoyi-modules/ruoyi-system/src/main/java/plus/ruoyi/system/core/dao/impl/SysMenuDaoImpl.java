package plus.ruoyi.system.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springframework.stereotype.Repository;
import plus.ruoyi.common.core.constant.SystemConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.mybatis.core.dao.impl.BaseDaoImpl;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.common.mybatis.core.query.PlusQuery;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.system.core.dao.ISysMenuDao;
import plus.ruoyi.system.core.domain.SysMenu;
import plus.ruoyi.system.core.domain.bo.SysMenuBo;
import plus.ruoyi.system.core.mapper.SysMenuMapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单数据访问实现
 *
 * @author Lion Li
 */
@Repository
public class SysMenuDaoImpl extends BaseDaoImpl<SysMenuMapper, SysMenu> implements ISysMenuDao {

    /**
     * 创建PlusQuery查询构造器(支持表别名)
     */
    protected PlusQuery<SysMenu> query() {
        return PlusQuery.of(SysMenu.class);
    }

    /**
     * 构建查询条件
     */
    @Override
    public PlusLambdaQuery<SysMenu> buildQueryWrapper(SysMenuBo bo) {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class);
        lqw.like(SysMenu::getMenuName, bo.getMenuName())
            .eq(SysMenu::getVisible, bo.getVisible())
            .eq(SysMenu::getStatus, bo.getStatus())
            .orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum);
        return lqw;
    }

    /**
     * 根据用户ID查询菜单列表
     */
    @Override
    public List<SysMenu> listMenusByUserId(Long userId) {
        return baseMapper.selectMenuTreeByUserId(userId);
    }

    /**
     * 根据用户ID查询菜单列表(包含按钮权限)
     */
    @Override
    public List<SysMenu> listMenusWithButtonsByUserId(SysMenuBo menuBo, Long userId) {
        PlusQuery<SysMenu> plusQuery = query();

        if (LoginHelper.isSuperAdmin(userId)) {
            // 超级管理员查询所有菜单
            plusQuery.eq("m.visible", menuBo.getVisible())
                .eq("m.status", menuBo.getStatus())
                .like("m.menu_name", menuBo.getMenuName())
                .orderByAsc("m.parent_id").orderByAsc("m.order_num");
        } else {
            // 普通用户根据角色查询菜单
            plusQuery.eq("m.visible", menuBo.getVisible())
                .eq("m.status", menuBo.getStatus())
                .like("m.menu_name", menuBo.getMenuName())
                .eq("r.status", DictEnableStatus.ENABLE.getValue())
                .apply("r.role_id in (select role_id from sys_user_role where user_id = {0})", userId)
                .orderByAsc("m.parent_id").orderByAsc("m.order_num");
        }

        return baseMapper.selectMenuListByUserId(plusQuery.lambda());
    }

    /**
     * 根据用户ID查询菜单权限
     */
    @Override
    public List<String> listMenuPermsByUserId(Long userId) {
        List<String> perms = baseMapper.selectMenuPermsByUserId(userId);
        return perms.stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询菜单权限
     */
    @Override
    public List<String> listMenuPermsByRoleId(Long roleId) {
        List<String> perms = baseMapper.selectMenuPermsByRoleId(roleId);
        return perms.stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询菜单ID列表
     */
    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId, boolean menuCheckStrictly) {
        // menuCheckStrictly=true: 父子联动,过滤父节点,只返回叶子节点
        // menuCheckStrictly=false: 非父子联动,返回所有节点
        return baseMapper.selectMenuListByRoleId(roleId, menuCheckStrictly);
    }

    /**
     * 根据租户套餐ID查询菜单ID列表
     */
    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseMapper.selectMenuListByRoleId(packageId, false);
    }

    /**
     * 根据菜单ID列表查询父级菜单ID列表
     */
    @Override
    public List<Long> listParentIdsByMenuIds(List<Long> menuIds) {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .select(SysMenu::getParentId)
            .in(SysMenu::getMenuId, menuIds);
        return baseMapper.selectObjs(lqw)
            .stream()
            .map(obj -> (Long) obj)
            .toList();
    }

    /**
     * 根据菜单ID列表查询菜单ID，排除父级菜单ID
     */
    @Override
    public List<Long> listMenuIdsByMenuIdsExcludeParents(List<Long> menuIds, List<Long> excludeParentIds) {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .select(SysMenu::getMenuId)
            .in(SysMenu::getMenuId, menuIds)
            .notIn(SysMenu::getMenuId, excludeParentIds);
        return baseMapper.selectObjs(lqw)
            .stream()
            .map(obj -> (Long) obj)
            .toList();
    }

    /**
     * 检查菜单名称唯一性
     */
    @Override
    public boolean checkMenuNameUnique(String menuName, Long parentId, Long menuId, Collection<Long> excludeMenuIds) {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .eq(SysMenu::getMenuName, menuName)
            .eq(SysMenu::getParentId, parentId)
            .ne(SysMenu::getMenuId, menuId)
            .notIn(CollUtil.isNotEmpty(excludeMenuIds), SysMenu::getMenuId, excludeMenuIds);
        return !exists(lqw);
    }

    /**
     * 检查是否有子菜单
     */
    @Override
    public boolean hasChildByMenuId(Long menuId, Collection<Long> excludeMenuIds) {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .eq(SysMenu::getParentId, menuId)
            .notIn(CollUtil.isNotEmpty(excludeMenuIds), SysMenu::getMenuId, excludeMenuIds);
        return exists(lqw);
    }

    /**
     * 根据用户ID查询系统菜单列表（使用联表查询）
     */
    @Override
    public List<SysMenu> listMenuListByUserId(Wrapper<SysMenu> queryWrapper) {
        return baseMapper.selectMenuListByUserId(queryWrapper);
    }

    /**
     * 查询所有正常状态的目录和菜单（用于构建菜单树）
     */
    @Override
    public List<SysMenu> listMenuTreeAll() {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .in(SysMenu::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU)
            .eq(SysMenu::getStatus, DictEnableStatus.ENABLE.getValue())
            .orderByAsc(SysMenu::getParentId)
            .orderByAsc(SysMenu::getOrderNum);
        return list(lqw);
    }

    /**
     * 超级管理员查询菜单列表(支持排除指定菜单)
     */
    @Override
    public List<SysMenu> listMenusForAdmin(SysMenuBo menuBo, Collection<Long> excludeMenuIds) {
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .like(SysMenu::getMenuName, menuBo.getMenuName())
            .eq(SysMenu::getVisible, menuBo.getVisible())
            .eq(SysMenu::getStatus, menuBo.getStatus())
            .notIn(CollUtil.isNotEmpty(excludeMenuIds), SysMenu::getMenuId, excludeMenuIds);

        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = menuBo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            lqw.and(q -> q
                .like(SysMenu::getMenuName, searchValue)      // String
                .or().like(SysMenu::getPerms, searchValue)    // String
                .or().like(SysMenu::getComponent, searchValue) // String
                .or().like(SysMenu::getRemark, searchValue)   // String
                .or().like(SysMenu::getPath, searchValue)     // String
                .or().like(SysMenu::getMenuType, searchValue) // String
                .or().like(SysMenu::getIcon, searchValue)     // String
                .or().likeCast(SysMenu::getMenuId, searchValue)); // Long
        }

        return list(lqw.orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getOrderNum));
    }

    /**
     * 普通用户查询菜单列表(支持排除指定菜单)
     */
    @Override
    public List<SysMenu> listMenusForUser(SysMenuBo menuBo, Long userId, Collection<Long> excludeMenuIds) {
        PlusQuery<SysMenu> wrapper = query();
        wrapper.inSql("r.role_id", "select role_id from sys_user_role where user_id = " + userId)
            .like("m.menu_name", menuBo.getMenuName())
            .eq("m.visible", menuBo.getVisible())
            .eq("m.status", menuBo.getStatus());

        // 模糊搜索（跨数据库兼容：String 类型用 like，非 String 类型用 likeCast）
        String searchValue = menuBo.getSearchValue();
        if (StringUtils.isNotBlank(searchValue)) {
            wrapper.and(q -> q
                .like("m.menu_name", searchValue)       // String
                .or().like("m.perms", searchValue)      // String
                .or().like("m.component", searchValue)  // String
                .or().like("m.remark", searchValue)     // String
                .or().like("m.path", searchValue)       // String
                .or().like("m.menu_type", searchValue)  // String
                .or().like("m.icon", searchValue)       // String
                .or().likeCast("m.menu_id", searchValue)); // Long
        }

        // 排除指定菜单
        if (CollUtil.isNotEmpty(excludeMenuIds)) {
            wrapper.notIn("m.menu_id", excludeMenuIds);
        }

        wrapper.orderByAsc("m.parent_id").orderByAsc("m.order_num");
        return baseMapper.selectMenuListByUserId(wrapper.lambda());
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
        // 在 DAO 层构建查询条件
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class);
        lqw.eq(SysMenu::getPerms, perms)
            .eq(SysMenu::getMenuType, "C");  // 只检查菜单类型，不检查按钮
        return exists(lqw);
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
        if (StringUtils.isBlank(perms)) {
            return null;
        }
        // 在 DAO 层构建查询条件
        PlusLambdaQuery<SysMenu> lqw = PlusLambdaQuery.of(SysMenu.class)
            .select(SysMenu::getMenuName)
            .eq(SysMenu::getPerms, perms);

        SysMenu menu = getOne(lqw, false);
        return menu != null ? menu.getMenuName() : null;
    }

}
