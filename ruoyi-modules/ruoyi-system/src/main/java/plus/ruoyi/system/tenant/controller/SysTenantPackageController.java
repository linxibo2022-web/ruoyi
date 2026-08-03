package plus.ruoyi.system.tenant.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.tenant.domain.bo.SysTenantPackageBo;
import plus.ruoyi.system.tenant.domain.vo.SysTenantPackageVo;
import plus.ruoyi.system.tenant.service.ISysTenantPackageService;

/**
 * 租户套餐
 *
 * @author Michelle.Chung
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/tenant")
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class SysTenantPackageController {

    // 业务服务
    private final ISysTenantPackageService tenantPackageService;

    /**
     * 查询租户套餐列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:query")
    @GetMapping("/pageTenantPackages")
    public R<PageResult<SysTenantPackageVo>> pageTenantPackages(SysTenantPackageBo bo, PageQuery pageQuery) {
        return R.ok(tenantPackageService.page(bo, pageQuery));
    }

    /**
     * 查询租户套餐下拉选列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:query")
    @GetMapping("/listTenantPackages")
    public R<List<SysTenantPackageVo>> listTenantPackages() {
        SysTenantPackageBo bo = new SysTenantPackageBo();
        bo.setStatus(DictEnableStatus.ENABLE.getValue());
        return R.ok(tenantPackageService.list(bo));
    }

    /**
     * 获取租户套餐详细信息
     *
     * @param packageId 主键
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:query")
    @GetMapping("/getTenantPackage/{packageId}")
    public R<SysTenantPackageVo> getTenantPackage(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long packageId) {
        return R.ok(tenantPackageService.get(packageId));
    }

    /**
     * 新增租户套餐
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:add")
    @Log(title = "租户套餐", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addTenantPackage")
    @Transactional(rollbackFor = Exception.class)
    public R<Long> addTenantPackage(@Validated(AddGroup.class) @RequestBody SysTenantPackageBo bo) {
        if (!tenantPackageService.checkPackageNameUnique(bo)) {
            return R.fail("新增套餐'" + bo.getPackageName() + "'失败，套餐名称已存在");
        }
        return R.ok(tenantPackageService.add(bo));
    }

    /**
     * 修改租户套餐
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:update")
    @Log(title = "租户套餐", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateTenantPackage")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateTenantPackage(@Validated(EditGroup.class) @RequestBody SysTenantPackageBo bo) {
        if (!tenantPackageService.checkPackageNameUnique(bo)) {
            return R.fail("修改套餐'" + bo.getPackageName() + "'失败，套餐名称已存在");
        }
        return R.status(tenantPackageService.update(bo));
    }

    /**
     * 状态修改
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:update")
    @Log(title = "租户套餐", operType = DictOperType.UPDATE)
    @PutMapping("/changeTenantPackageStatus")
    public R<Void> changeTenantPackageStatus(@RequestBody SysTenantPackageBo bo) {
        return R.status(tenantPackageService.updatePackageStatus(bo));
    }

    /**
     * 删除租户套餐
     *
     * @param packageIds 主键串
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:delete")
    @Log(title = "租户套餐", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteTenantPackages/{packageIds}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> deleteTenantPackages(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] packageIds) {
        return R.status(tenantPackageService.batchDelete(List.of(packageIds)));
    }


    /**
     * 导出租户套餐列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenantPackage:export")
    @Log(title = "租户套餐", operType = DictOperType.EXPORT)
    @PostMapping("/exportTenantPackages")
    public void exportTenantPackages(SysTenantPackageBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysTenantPackageVo> pageResult = tenantPackageService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "租户套餐", SysTenantPackageVo.class, response);
    }
}
