package plus.ruoyi.system.tenant.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.lock.annotation.Lock4j;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.encrypt.annotation.ApiEncrypt;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import plus.ruoyi.system.tenant.domain.bo.SysTenantBo;
import plus.ruoyi.system.tenant.domain.vo.SysTenantVo;
import plus.ruoyi.system.tenant.service.ISysTenantService;

/**
 * 租户管理
 *
 * @author Michelle.Chung
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/tenant")
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class SysTenantController {

    // 业务服务
    private final ISysTenantService tenantService;

    /**
     * 查询租户列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:query")
    @GetMapping("/pageTenants")
    public R<PageResult<SysTenantVo>> pageTenants(SysTenantBo bo, PageQuery pageQuery) {
        return R.ok(tenantService.page(bo, pageQuery));
    }

    /**
     * 获取租户详细信息
     *
     * @param id 主键
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:query")
    @GetMapping("/getTenant/{id}")
    public R<SysTenantVo> getTenant(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(tenantService.get(id));
    }

    /**
     * 新增租户
     */
    @ApiEncrypt
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:add")
    @Log(title = "租户管理", operType = DictOperType.INSERT)
    @Lock4j(keys = {"#bo.companyName"}, expire = 10000)
    @RepeatSubmit()
    @PostMapping("/addTenant")
    public R<Void> addTenant(@Validated(AddGroup.class) @RequestBody SysTenantBo bo) {
        if (!tenantService.checkCompanyNameUnique(bo)) {
            return R.fail("新增租户'" + bo.getCompanyName() + "'失败，企业名称已存在");
        }
        TenantHelper.ignore(() -> tenantService.insertTenant(bo));
        return R.ok();
    }

    /**
     * 修改租户
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:update")
    @Log(title = "租户管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateTenant")
    public R<Void> updateTenant(@Validated(EditGroup.class) @RequestBody SysTenantBo bo) {
        tenantService.checkTenantAllowed(bo.getTenantId());
        if (!tenantService.checkCompanyNameUnique(bo)) {
            return R.fail("修改租户'" + bo.getCompanyName() + "'失败，公司名称已存在");
        }
        return R.status(tenantService.updateTenant(bo));
    }

    /**
     * 状态修改
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:update")
    @Log(title = "租户管理", operType = DictOperType.UPDATE)
    @PutMapping("/changeTenantStatus")
    public R<Void> changeTenantStatus(@RequestBody SysTenantBo bo) {
        tenantService.checkTenantAllowed(bo.getTenantId());
        return R.status(tenantService.updateTenantStatus(bo));
    }

    /**
     * 删除租户
     *
     * @param ids 主键串
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:delete")
    @Log(title = "租户管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteTenants/{ids}")
    public R<Void> deleteTenants(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(tenantService.batchDelete(List.of(ids)));
    }

    /**
     * 动态切换租户
     *
     * @param tenantId 租户ID
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/setDynamicTenant/{tenantId}")
    public R<Void> setDynamicTenant(@NotBlank(message = I18nKeys.Tenant.ID_REQUIRED) @PathVariable String tenantId) {
        TenantHelper.setDynamic(tenantId, true);
        return R.ok();
    }

    /**
     * 清除动态租户
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/clearDynamicTenant")
    public R<Void> clearDynamicTenant() {
        TenantHelper.clearDynamic();
        return R.ok();
    }


    /**
     * 同步租户套餐
     *
     * @param tenantId  租户id
     * @param packageId 套餐id
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:update")
    @Log(title = "租户管理", operType = DictOperType.UPDATE)
    @GetMapping("/syncTenantPackage")
    public R<Void> syncTenantPackage(@NotBlank(message = "租户ID不能为空") String tenantId,
                                     @NotNull(message = "套餐ID不能为空") Long packageId) {
        TenantHelper.ignore(() -> tenantService.syncTenantPackage(tenantId, packageId));
        return R.ok(I18nKeys.Tenant.SYNC_PACKAGE_SUCCESS);
    }

    /**
     * 同步租户角色
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @Log(title = "租户管理", operType = DictOperType.INSERT)
    @GetMapping("/syncTenantRoles")
    public R<Void> syncTenantRoles() {
        if (!TenantHelper.isEnable()) {
            return R.fail(I18nKeys.Tenant.MODE_NOT_ENABLED);
        }
        tenantService.syncTenantRoles();
        return R.ok(I18nKeys.Tenant.SYNC_ROLES_SUCCESS);
    }

    /**
     * 同步租户字典
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @Log(title = "租户管理", operType = DictOperType.INSERT)
    @GetMapping("/syncTenantDicts")
    public R<Void> syncTenantDicts() {
        if (!TenantHelper.isEnable()) {
            return R.fail(I18nKeys.Tenant.MODE_NOT_ENABLED);
        }
        tenantService.syncTenantDicts();
        return R.ok(I18nKeys.Tenant.SYNC_DICTS_SUCCESS);
    }

    /**
     * 同步租户参数配置
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @Log(title = "租户管理", operType = DictOperType.INSERT)
    @Lock4j
    @GetMapping("/syncTenantConfigs")
    public R<Void> syncTenantConfigs() {
        if (!TenantHelper.isEnable()) {
            return R.fail("当前未开启租户模式");
        }
        tenantService.syncTenantConfigs();
        return R.ok("同步租户参数配置成功");
    }

    /**
     * 导出租户列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:export")
    @Log(title = "租户管理", operType = DictOperType.EXPORT)
    @PostMapping("/exportTenants")
    public void exportTenants(SysTenantBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysTenantVo> pageResult = tenantService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "租户", SysTenantVo.class, response);
    }

}
