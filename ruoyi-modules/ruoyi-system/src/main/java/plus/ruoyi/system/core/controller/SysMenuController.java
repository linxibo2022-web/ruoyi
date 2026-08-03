package plus.ruoyi.system.core.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictBooleanFlag;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import plus.ruoyi.system.core.domain.SysMenu;
import plus.ruoyi.system.core.domain.bo.SysMenuBo;
import plus.ruoyi.system.core.domain.vo.MenuTreeSelectVo;
import plus.ruoyi.system.core.domain.vo.RouterVo;
import plus.ruoyi.system.core.domain.vo.SysMenuVo;
import plus.ruoyi.system.core.service.ISysMenuService;

/**
 * 菜单信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    /* 业务服务 */
    private final ISysMenuService menuService;

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/getRouters")
    public R<List<RouterVo>> getRouters() {
        List<SysMenu> menuList = menuService.listMenuTreeByUserId(LoginHelper.getUserId());
        return R.ok(menuService.buildRouters(menuList));
    }

    /**
     * 获取菜单列表
     */
    @SaCheckPermission(value = "system:menu:query", orRole = TenantConstants.TENANT_ADMIN_ROLE_KEY)
    @GetMapping("/listMenus")
    public R<List<SysMenuVo>> listMenus(SysMenuBo menuBo) {
        List<SysMenuVo> menuVoList = menuService.listMenus(menuBo, LoginHelper.getUserId());
        return R.ok(menuVoList);
    }

    /**
     * 根据菜单编号获取详细信息
     *
     * @param menuId 菜单ID
     */
    @SaCheckPermission(value = "system:menu:query", orRole = TenantConstants.TENANT_ADMIN_ROLE_KEY)
    @GetMapping(value = "/getMenu/{menuId}")
    public R<SysMenuVo> getMenu(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long menuId) {
        return R.ok(menuService.getMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @SaCheckPermission(value = "system:menu:query", orRole = TenantConstants.TENANT_ADMIN_ROLE_KEY)
    @GetMapping("/getMenuTreeOptions")
    public R<List<Tree<Long>>> getMenuTreeOptions(SysMenuBo menuBo) {
        List<SysMenuVo> menuVoList = menuService.listMenus(menuBo, LoginHelper.getUserId());
        return R.ok(menuService.buildMenuTreeOptions(menuVoList));
    }

    /**
     * 加载对应角色菜单列表树
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission(value = "system:menu:query", orRole = TenantConstants.TENANT_ADMIN_ROLE_KEY)
    @GetMapping(value = "/getRoleMenuTree/{roleId}")
    public R<MenuTreeSelectVo> getRoleMenuTree(@NotNull(message = "角色id不为空") @PathVariable("roleId") Long roleId) {
        List<SysMenuVo> menuVoList = menuService.listMenuByUserId(LoginHelper.getUserId());
        MenuTreeSelectVo selectVo = new MenuTreeSelectVo();
        // 根据角色ID查询菜单ID列表(会自动根据角色的menuCheckStrictly属性决定是否过滤父节点)
        selectVo.setCheckedKeys(menuService.listMenuIdsByRoleId(roleId));
        selectVo.setMenus(menuService.buildMenuTreeOptions(menuVoList));
        return R.ok(selectVo);
    }

    /**
     * 加载对应租户套餐菜单列表树
     * 使用专门的租户套餐菜单过滤方法
     *
     * @param packageId 租户套餐ID
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/getTenantPackageMenuTree/{packageId}")
    public R<MenuTreeSelectVo> getTenantPackageMenuTree(@PathVariable("packageId") Long packageId) {
        // 获取所有菜单列表（超级管理员权限，获取完整菜单）
        List<SysMenuVo> menuVoList = menuService.listMenuByUserId(LoginHelper.getUserId());

        MenuTreeSelectVo selectVo = new MenuTreeSelectVo();

        // 获取租户套餐已选中的菜单ID（也需要过滤）
        List<Long> checkedKeys = menuService.listMenuIdsByPackageId(packageId);
        selectVo.setCheckedKeys(checkedKeys);

        // 使用专门的租户套餐菜单树构建方法，过滤系统核心功能
        selectVo.setMenus(menuService.buildTenantPackageMenuTreeOptions(menuVoList));

        return R.ok(selectVo);
    }

    /**
     * 新增菜单
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", operType = DictOperType.INSERT)
    @PostMapping("/addMenu")
    public R<Long> addMenu(@Validated @RequestBody SysMenuBo menuBo) {
        if (!menuService.checkMenuNameUnique(menuBo)) {
            return R.fail("新增菜单'" + menuBo.getMenuName() + "'失败，菜单名称已存在");
        } else if (DictBooleanFlag.YES.getValue().equals(menuBo.getIsExternalLink()) && !Validator.isUrl(menuBo.getPath())) {
            return R.fail("新增菜单'" + menuBo.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        return R.ok(menuService.insertMenu(menuBo));
    }

    /**
     * 修改菜单
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:update")
    @Log(title = "菜单管理", operType = DictOperType.UPDATE)
    @PutMapping("/updateMenu")
    public R<Void> updateMenu(@Validated @RequestBody SysMenuBo menuBo) {
        if (!menuService.checkMenuNameUnique(menuBo)) {
            return R.fail("修改菜单'" + menuBo.getMenuName() + "'失败，菜单名称已存在");
        } else if (DictBooleanFlag.YES.getValue().equals(menuBo.getIsExternalLink()) && !Validator.isUrl(menuBo.getPath())) {
            return R.fail("修改菜单'" + menuBo.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menuBo.getMenuId().equals(menuBo.getParentId())) {
            return R.fail("修改菜单'" + menuBo.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        return R.status(menuService.updateMenu(menuBo));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:delete")
    @Log(title = "菜单管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteMenu/{menuId}")
    public R<Void> deleteMenu(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return R.warn("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return R.warn("菜单已分配,不允许删除");
        }
        return R.status(menuService.deleteMenuById(menuId));
    }
}
