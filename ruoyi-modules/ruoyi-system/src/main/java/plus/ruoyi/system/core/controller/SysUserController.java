package plus.ruoyi.system.core.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.core.utils.ObjectUtils;
import plus.ruoyi.common.core.utils.StreamUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.encrypt.annotation.ApiEncrypt;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import plus.ruoyi.system.core.domain.bo.SysPostBo;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.*;
import plus.ruoyi.system.core.listener.SysUserImportListener;
import plus.ruoyi.system.core.service.ISysDeptService;
import plus.ruoyi.system.core.service.ISysPostService;
import plus.ruoyi.system.core.service.ISysRoleService;
import plus.ruoyi.system.core.service.ISysUserService;
import plus.ruoyi.system.tenant.service.ISysTenantService;

/**
 * 用户信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    /* 业务服务 */
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysPostService postService;
    private final ISysDeptService deptService;
    private final ISysTenantService tenantService;

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public R<UserInfoVo> getUserInfo() {
        UserInfoVo userInfoVo = new UserInfoVo();
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (TenantHelper.isEnable() && LoginHelper.isSuperAdmin()) {
            // 超级管理员 如果重新加载用户信息需清除动态租户
            TenantHelper.clearDynamic();
        }
        SysUserVo user = userService.getUserWithRolesById(loginUser.getUserId());
        if (ObjectUtil.isNull(user)) {
            return R.fail("没有权限访问用户数据!");
        }
        userInfoVo.setUser(user);
        userInfoVo.setPermissions(loginUser.getMenuPermission());
        userInfoVo.setRoles(loginUser.getRolePermission());
        return R.ok(userInfoVo);
    }

    /**
     * 查询用户列表
     */
    @SaCheckPermission(value = {"system:user:query", "system:notice:add", "system:notice:update"}, mode = SaMode.OR)
    @GetMapping("/pageUsers")
    public R<PageResult<SysUserVo>> pageUsers(SysUserBo user, PageQuery pageQuery) {
        return R.ok(userService.pageUsers(user, pageQuery));
    }

    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping(value = {"/getUser/", "/getUser/{userId}"})
    public R<SysUserInfoVo> getUser(@PathVariable(value = "userId", required = false) Long userId) {
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        List<Long> userRoleIds = List.of();
        if (ObjectUtil.isNotNull(userId)) {
            userService.checkUserDataScope(userId);
            SysUserVo sysUser = userService.getUserWithRolesById(userId);
            userInfoVo.setUser(sysUser);
            userRoleIds = roleService.listRoleIdsByUserId(userId);
            userInfoVo.setRoleIds(userRoleIds);
            Long deptId = sysUser.getDeptId();
            if (ObjectUtil.isNotNull(deptId)) {
                SysPostBo postBo = new SysPostBo();
                postBo.setDeptId(deptId);
                userInfoVo.setPosts(ObjectUtils.defaultIfNull(postService.list(postBo), List.of()));
                userInfoVo.setPostIds(ObjectUtils.defaultIfNull(postService.listPostIdsByUserId(userId), List.of()));
            } else {
                userInfoVo.setPosts(List.of());
                userInfoVo.setPostIds(List.of());
            }
        }
        // 查询当前用户有权限分配的角色
        SysRoleBo roleBo = new SysRoleBo();
        roleBo.setStatus(DictEnableStatus.ENABLE.getValue());
        List<SysRoleVo> roles = roleService.list(roleBo);
        roles = LoginHelper.isSuperAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isSuperAdmin());
        Set<Long> permissionRoleIds = StreamUtils.toSet(roles, SysRoleVo::getRoleId);

        // 合并用户已有角色（确保回显）：将不在权限范围内但用户已有的角色也加入列表
        if (ObjectUtil.isNotNull(userId) && !userRoleIds.isEmpty()) {
            // 找出用户已有但不在权限列表中的角色ID
            List<Long> missingRoleIds = userRoleIds.stream()
                .filter(id -> !permissionRoleIds.contains(id))
                .toList();
            if (!missingRoleIds.isEmpty()) {
                // 查询这些角色并合并（不带数据权限，因为用户已有这些角色）
                List<SysRoleVo> userExistingRoles = roleService.listRolesByIds(missingRoleIds);
                // 标记为禁用状态（当前操作者无权限操作）
                userExistingRoles.forEach(role -> role.setDisabled(true));
                roles = new ArrayList<>(roles);
                roles.addAll(userExistingRoles);
            }
        }
        userInfoVo.setRoles(roles);
        return R.ok(userInfoVo);
    }

    /**
     * 新增用户
     */
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addUser")
    public R<Long> addUser(@Validated @RequestBody SysUserBo bo) {
        deptService.checkDeptDataScope(bo.getDeptId());
        if (!userService.isUserNameUnique(bo.getUserName(), bo.getUserId())) {
            return R.fail("新增用户'" + bo.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(bo.getPhone()) && !userService.isPhoneUnique(bo.getPhone(), bo.getUserId())) {
            return R.fail("新增用户'" + bo.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(bo.getEmail()) && !userService.isEmailUnique(bo.getEmail(), bo.getUserId())) {
            return R.fail("新增用户'" + bo.getUserName() + "'失败，邮箱账号已存在");
        }
        if (TenantHelper.isEnable()) {
            if (!tenantService.checkAccountBalance(TenantHelper.getTenantId())) {
                return R.fail("当前租户下用户名额不足，请联系管理员");
            }
        }
        bo.setPassword(BCrypt.hashpw(bo.getPassword()));
        return R.ok(userService.insertUser(bo));
    }

    /**
     * 修改用户
     */
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateUser")
    public R<Void> updateUser(@Validated @RequestBody SysUserBo bo) {
        userService.checkUserAllowed(bo.getUserId());
        userService.checkUserDataScope(bo.getUserId());
        deptService.checkDeptDataScope(bo.getDeptId());
        if (!userService.isUserNameUnique(bo.getUserName(), bo.getUserId())) {
            return R.fail("修改用户'" + bo.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(bo.getPhone()) && !userService.isPhoneUnique(bo.getPhone(), bo.getUserId())) {
            return R.fail("修改用户'" + bo.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(bo.getEmail()) && !userService.isEmailUnique(bo.getEmail(), bo.getUserId())) {
            return R.fail("修改用户'" + bo.getUserName() + "'失败，邮箱账号已存在");
        }
        userService.updateUser(bo);
        return R.ok();
    }

    /**
     * 删除用户
     *
     * @param userIds 角色ID串
     */
    @SaCheckPermission("system:user:delete")
    @Log(title = "用户管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteUsers/{userIds}")
    public R<Void> deleteUsers(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            return R.fail("当前用户不能删除");
        }
        return R.status(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @ApiEncrypt
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", operType = DictOperType.UPDATE)
    @PutMapping("/resetUserPwd")
    public R<Void> resetUserPwd(@RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        userService.resetUserPwd(user.getUserId(), user.getPassword());
        return R.ok();
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:user:update")
    @Log(title = "用户管理", operType = DictOperType.UPDATE)
    @PutMapping("/changeUserStatus")
    public R<Void> changeUserStatus(@RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        userService.updateUserStatus(user.getUserId(), user.getStatus());
        return R.ok();
    }

    /**
     * 根据用户编号获取授权角色
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/getUserWithRoles/{userId}")
    public R<SysUserInfoVo> getUserWithRoles(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long userId) {
        userService.checkUserDataScope(userId);
        SysUserVo user = userService.getUserWithRolesById(userId);
        List<SysRoleVo> roles = roleService.listRolesWithAuthByUserId(userId);
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        userInfoVo.setUser(user);
        userInfoVo.setRoles(LoginHelper.isSuperAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isSuperAdmin()));
        return R.ok(userInfoVo);
    }


    /**
     * 根据用户ID串批量获取用户基础信息
     *
     * @param userIds 用户ID串
     * @param deptId  部门ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/getUserOptions")
    public R<List<SysUserVo>> getUserOptions(@RequestParam(required = false) Long[] userIds,
                                             @RequestParam(required = false) Long deptId) {
        return R.ok(userService.listUsersByIdsAndDeptId(ArrayUtil.isEmpty(userIds) ? null : List.of(userIds), deptId));
    }

    /**
     * 获取部门下的所有用户信息
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/listUsersByDeptId/{deptId}")
    public R<List<SysUserVo>> listUsersByDeptId(@NotNull(message = "部门id不为空") @PathVariable Long deptId) {
        return R.ok(userService.listUsersByDeptId(deptId));
    }

    /**
     * 导出用户列表
     */
    @Log(title = "用户管理", operType = DictOperType.EXPORT)
    @SaCheckPermission("system:user:export")
    @PostMapping("/exportUsers")
    public void exportUsers(SysUserBo user, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysUserExportVo> pageResult = userService.pageUserExports(user, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "用户数据", SysUserExportVo.class, response);
    }

    /**
     * 获取导入模板
     */
    @PostMapping("/templateUsers")
    public void templateUsers(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "用户数据模板", SysUserImportVo.class, response);
    }

    /**
     * 导入数据
     *
     * @param file 导入文件
     */
    @Log(title = "用户管理", operType = DictOperType.IMPORT)
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/importUsers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importUsers(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<SysUserImportVo> result = ExcelUtil.importExcel(file.getInputStream(), SysUserImportVo.class, new SysUserImportListener());
        return R.ok(result.getAnalysis());
    }
}
