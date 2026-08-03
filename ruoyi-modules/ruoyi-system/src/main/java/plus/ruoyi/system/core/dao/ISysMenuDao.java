package plus.ruoyi.system.core.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import plus.ruoyi.common.mybatis.core.dao.IBaseDao;
import plus.ruoyi.common.mybatis.core.query.PlusLambdaQuery;
import plus.ruoyi.system.core.domain.SysMenu;
import plus.ruoyi.system.core.domain.bo.SysMenuBo;

import java.util.Collection;
import java.util.List;

/**
 * 菜单数据访问层
 *
 * @author Lion Li
 */
public interface ISysMenuDao extends IBaseDao<SysMenu> {

    /**
     * 构建查询条件
     *
     * @param bo 查询条件
     * @return 查询构造器
     */
    PlusLambdaQuery<SysMenu> buildQueryWrapper(SysMenuBo bo);

    /**
     * 根据用户ID查询菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> listMenusByUserId(Long userId);

    /**
     * 根据用户ID查询菜单列表(包含按钮权限)
     *
     * @param menuBo 查询条件
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> listMenusWithButtonsByUserId(SysMenuBo menuBo, Long userId);

    /**
     * 根据用户ID查询菜单权限
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> listMenuPermsByUserId(Long userId);

    /**
     * 根据角色ID查询菜单权限
     *
     * @param roleId 角色ID
     * @return 权限标识列表
     */
    List<String> listMenuPermsByRoleId(Long roleId);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @param menuCheckStrictly 是否父子联动(true:过滤父节点,只返回叶子节点; false:返回所有节点)
     * @return 菜单ID列表
     */
    List<Long> listMenuIdsByRoleId(Long roleId, boolean menuCheckStrictly);

    /**
     * 根据租户套餐ID查询菜单ID列表
     *
     * @param packageId 租户套餐ID
     * @return 菜单ID列表
     */
    List<Long> listMenuIdsByPackageId(Long packageId);

    /**
     * 根据菜单ID列表查询父级菜单ID列表
     *
     * @param menuIds 菜单ID列表
     * @return 父级菜单ID列表
     */
    List<Long> listParentIdsByMenuIds(List<Long> menuIds);

    /**
     * 根据菜单ID列表查询菜单ID，排除父级菜单ID
     *
     * @param menuIds         菜单ID列表
     * @param excludeParentIds 需要排除的父级菜单ID列表
     * @return 菜单ID列表
     */
    List<Long> listMenuIdsByMenuIdsExcludeParents(List<Long> menuIds, List<Long> excludeParentIds);

    /**
     * 检查菜单名称唯一性
     *
     * @param menuName       菜单名称
     * @param parentId       父菜单ID
     * @param menuId         菜单ID(排除自己)
     * @param excludeMenuIds 需要排除的菜单ID集合
     * @return true表示唯一
     */
    boolean checkMenuNameUnique(String menuName, Long parentId, Long menuId, Collection<Long> excludeMenuIds);

    /**
     * 检查是否有子菜单
     *
     * @param menuId           菜单ID
     * @param excludeMenuIds   需要排除的菜单ID集合
     * @return true表示有子菜单
     */
    boolean hasChildByMenuId(Long menuId, Collection<Long> excludeMenuIds);

    /**
     * 根据用户ID查询系统菜单列表（使用联表查询）
     *
     * @param queryWrapper 查询条件
     * @return 菜单列表
     */
    List<SysMenu> listMenuListByUserId(Wrapper<SysMenu> queryWrapper);

    /**
     * 查询所有正常状态的目录和菜单（用于构建菜单树）
     *
     * @return 菜单列表
     */
    List<SysMenu> listMenuTreeAll();

    /**
     * 超级管理员查询菜单列表(支持排除指定菜单)
     *
     * @param menuBo 查询条件
     * @param excludeMenuIds 需要排除的菜单ID集合
     * @return 菜单列表
     */
    List<SysMenu> listMenusForAdmin(SysMenuBo menuBo, Collection<Long> excludeMenuIds);

    /**
     * 普通用户查询菜单列表(支持排除指定菜单)
     *
     * @param menuBo 查询条件
     * @param userId 用户ID
     * @param excludeMenuIds 需要排除的菜单ID集合
     * @return 菜单列表
     */
    List<SysMenu> listMenusForUser(SysMenuBo menuBo, Long userId, Collection<Long> excludeMenuIds);

    /**
     * 根据权限字符串检查菜单是否存在
     * <p>用于代码生成器自动导入菜单时的去重判断</p>
     *
     * @param perms 权限字符串（如: base:ad:view）
     * @return 是否存在
     */
    boolean existsByPerms(String perms);

    /**
     * 根据权限字符串获取菜单名称
     * <p>用于权限校验失败时提供友好的错误提示</p>
     *
     * @param perms 权限字符串（如: base:ad:query）
     * @return 菜单名称，如果未找到则返回null
     */
    String getMenuNameByPerms(String perms);
}
