package plus.ruoyi.system.core.service;

import cn.hutool.core.lang.tree.Tree;
import plus.ruoyi.system.core.domain.SysMenu;
import plus.ruoyi.system.core.domain.bo.SysMenuBo;
import plus.ruoyi.system.core.domain.vo.RouterVo;
import plus.ruoyi.system.core.domain.vo.SysMenuVo;

import java.util.List;
import java.util.Set;

/**
 * 菜单 业务层
 *
 * @author Lion Li
 */
public interface ISysMenuService {

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenuVo> listMenuByUserId(Long userId);

    /**
     * 根据用户查询系统菜单列表
     *
     * @param menuBo   菜单信息
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenuVo> listMenus(SysMenuBo menuBo, Long userId);

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> listMenuPermissionsByUserId(Long userId);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    Set<String> listMenuPermissionsByRoleId(Long roleId);

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> listMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    List<Long> listMenuIdsByRoleId(Long roleId);

    /**
     * 根据租户套餐ID查询菜单id列表
     *
     * @param packageId 租户套餐ID
     * @return 选中菜单列表
     */
    List<Long> listMenuIdsByPackageId(Long packageId);

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menuList 菜单列表
     * @return 路由列表
     */
    List<RouterVo> buildRouters(List<SysMenu> menuList);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menuVoList 菜单列表
     * @return 下拉树结构列表
     */
    List<Tree<Long>> buildMenuTreeOptions(List<SysMenuVo> menuVoList);

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    SysMenuVo getMenuById(Long menuId);

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果 true 存在 false 不存在
     */
    boolean hasChildByMenuId(Long menuId);

    /**
     * 查询菜单是否存在角色
     *
     * @param menuId 菜单ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkMenuExistRole(Long menuId);

    /**
     * 新增保存菜单信息
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    Long insertMenu(SysMenuBo menuBo);

    /**
     * 修改保存菜单信息
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    boolean updateMenu(SysMenuBo menuBo);

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    boolean deleteMenuById(Long menuId);

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    boolean checkMenuNameUnique(SysMenuBo menuBo);

    /**
     * 构建租户套餐菜单下拉树结构
     * 专门用于租户套餐分配菜单，会过滤掉系统核心管理功能
     *
     * @param menuVoList 菜单列表
     * @return 下拉树结构列表
     */
    List<Tree<Long>> buildTenantPackageMenuTreeOptions(List<SysMenuVo> menuVoList);
}
