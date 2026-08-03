package plus.ruoyi.system.core.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.core.domain.bo.SysDeptBo;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.vo.DeptTreeSelectVo;
import plus.ruoyi.system.core.domain.vo.SysRoleVo;
import plus.ruoyi.system.core.service.ISysDeptService;
import plus.ruoyi.system.core.service.ISysRoleService;

/**
 * 角色信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    /* 业务服务 */
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;

    /**
     * 获取角色信息列表
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/pageRoles")
    public R<PageResult<SysRoleVo>> pageRoles(SysRoleBo role, PageQuery pageQuery) {
        return R.ok(roleService.page(role, pageQuery));
    }

    /**
     * 根据角色编号获取详细信息
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/getRole/{roleId}")
    public R<SysRoleVo> getRole(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long roleId) {
        roleService.checkRoleDataScope(roleId);
        return R.ok(roleService.getRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addRole")
    public R<Long> addRole(@Validated @RequestBody SysRoleBo bo) {
        roleService.checkRoleAllowed(bo);
        roleService.checkDataScopeLevel(bo.getDataScope());
        if (!roleService.checkRoleNameUnique(bo)) {
            return R.fail("新增角色'" + bo.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(bo)) {
            return R.fail("新增角色'" + bo.getRoleName() + "'失败，角色权限已存在");
        }
        return R.ok(roleService.insertRole(bo));

    }

    /**
     * 修改保存角色
     */
    @SaCheckPermission("system:role:update")
    @Log(title = "角色管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateRole")
    public R<Void> updateRole(@Validated @RequestBody SysRoleBo bo) {
        roleService.checkRoleAllowed(bo);
        roleService.checkRoleDataScope(bo.getRoleId());
        roleService.checkDataScopeLevel(bo.getDataScope());
        if (!roleService.checkRoleNameUnique(bo)) {
            return R.fail("修改角色'" + bo.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(bo)) {
            return R.fail("修改角色'" + bo.getRoleName() + "'失败，角色权限已存在");
        }

        if (roleService.updateRole(bo)) {
            roleService.cleanOnlineUserByRole(bo.getRoleId());
            return R.ok();
        }
        return R.fail("修改角色'" + bo.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 修改保存数据权限
     */
    @SaCheckPermission("system:role:update")
    @Log(title = "角色管理", operType = DictOperType.UPDATE)
    @PutMapping("/updateRoleDataScope")
    public R<Void> updateRoleDataScope(@RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleService.checkDataScopeLevel(role.getDataScope());
        roleService.authDataScope(role);
        return R.ok();
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:role:update")
    @Log(title = "角色管理", operType = DictOperType.UPDATE)
    @PutMapping("/changeRoleStatus")
    public R<Void> changeRoleStatus(@RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleService.updateRoleStatus(role.getRoleId(), role.getStatus());
        return R.ok();
    }

    /**
     * 删除角色
     *
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:role:delete")
    @Log(title = "角色管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteRoles/{roleIds}")
    public R<Void> deleteRoles(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] roleIds) {
        return R.status(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     *
     * @param roleIds 角色ID串（可选，用于回显已选角色）
     */
    @SaCheckPermission(value = {"system:role:query", "system:notice:add", "system:notice:update"}, mode = SaMode.OR)
    @GetMapping("/getRoleOptions")
    public R<List<SysRoleVo>> getRoleOptions(@RequestParam(required = false) Long[] roleIds) {
        if (roleIds != null && roleIds.length > 0) {
            return R.ok(roleService.listRolesByIds(List.of(roleIds)));
        }
        return R.ok(roleService.listRoleOptions());
    }

    /**
     * 获取对应角色部门树列表
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:role:query")
    @GetMapping(value = "/getRoleDeptTree/{roleId}")
    public R<DeptTreeSelectVo> getRoleDeptTree(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable("roleId") Long roleId) {
        DeptTreeSelectVo selectVo = new DeptTreeSelectVo();
        selectVo.setCheckedKeys(deptService.selectDeptListByRoleId(roleId));
        selectVo.setDepts(deptService.getDeptTree(new SysDeptBo()));
        return R.ok(selectVo);
    }

    /**
     * 导出角色信息列表
     */
    @Log(title = "角色管理", operType = DictOperType.EXPORT)
    @SaCheckPermission("system:role:export")
    @PostMapping("/exportRoles")
    public void exportRoles(SysRoleBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysRoleVo> pageResult = roleService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "角色数据", SysRoleVo.class, response);
    }

}
