package plus.ruoyi.business.base.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.dict.DictPlatformType;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.miniapp.initializer.MiniappConfigInitializer;
import plus.ruoyi.common.mp.initializer.MpConfigInitializer;
import plus.ruoyi.business.base.listener.PlatformConfigSyncListener;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.business.base.domain.vo.PlatformVo;
import plus.ruoyi.business.base.domain.bo.PlatformBo;
import plus.ruoyi.business.base.service.IPlatformService;
import plus.ruoyi.common.pay.initializer.PayConfigInitializer;
import plus.ruoyi.common.tenant.helper.TenantHelper;

/**
 * 平台配置
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/platform")
public class PlatformController {

    private final IPlatformService iPlatformService;
    private final PlatformService platformService;

    /**
     * 查询平台配置列表
     */
    @SaCheckPermission("base:platform:query")
    @GetMapping("/pagePlatforms")
    public R<PageResult<PlatformVo>> pagePlatforms(PlatformBo bo, PageQuery pageQuery) {
        return R.ok(iPlatformService.page(bo, pageQuery));
    }

    /**
     * 获取平台配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("base:platform:query")
    @GetMapping("/getPlatform/{id}")
    public R<PlatformVo> getPlatform(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(iPlatformService.get(id));
    }

    /**
     * 新增平台配置
     */
    @SaCheckPermission("base:platform:add")
    @Log(title = "平台配置", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addPlatform")
    public R<Long> addPlatform(@Validated(AddGroup.class) @RequestBody PlatformBo bo) {
        // 1. 校验appid唯一性
        validateAppidUnique(bo.getAppid(), null);

        // 2. 保存数据
        iPlatformService.add(bo);

        // 3. 处理平台配置变更
        handlePlatformConfigChange(null, bo.getAppid(), null, bo.getStatus());
        return R.ok();
    }

    /**
     * 修改平台配置
     */
    @SaCheckPermission("base:platform:update")
    @Log(title = "平台配置", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updatePlatform")
    public R<Void> updatePlatform(@Validated(EditGroup.class) @RequestBody PlatformBo bo) {
        // 1. 获取原有配置
        PlatformVo original = iPlatformService.get(bo.getId());
        if (original == null) {
            throw new ServiceException("平台配置不存在");
        }
        // 2. 校验appid唯一性
        validateAppidUnique(bo.getAppid(), bo.getId());
        // 3. 更新数据
        iPlatformService.update(bo);

        // 4. 如果不是仅更新订阅配置，则处理平台配置变更
        if (!Boolean.TRUE.equals(bo.getOnlyTemplateUpdate())) {
            handlePlatformConfigChange(original.getAppid(), bo.getAppid(), original.getStatus(), bo.getStatus());
        } else {
            log.info("仅订阅配置发生变更，跳过平台配置刷新");
        }
        return R.ok();
    }

    /**
     * 删除平台配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("base:platform:delete")
    @Log(title = "平台配置", operType = DictOperType.DELETE)
    @DeleteMapping("/deletePlatforms/{ids}")
    public R<Void> deletePlatforms(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        // 1. 获取删除的平台列表
        List<PlatformVo> platformsToDelete = Arrays.stream(ids)
            .map(iPlatformService::get)
            .filter(Objects::nonNull)
            .toList();

        // 2. 删除数据
        iPlatformService.batchDelete(List.of(ids));

        // 3. 遍历处理平台变更
        platformsToDelete.forEach(platform ->
            handlePlatformConfigChange(platform.getAppid(), null, platform.getStatus(), null));

        return R.ok();
    }

    /**
     * 导出平台配置列表
     */
    @SaCheckPermission("base:platform:export")
    @Log(title = "平台配置", operType = DictOperType.EXPORT)
    @PostMapping("/exportPlatforms")
    public void exportPlatforms(PlatformBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<PlatformVo> pageResult = iPlatformService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "平台配置", PlatformVo.class, response);
    }

    /**
     * 获取平台配置导入模板
     */
    @PostMapping("/templatePlatforms")
    public void templatePlatforms(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "平台配置模板", PlatformVo.class, response);
    }

    /**
     * 导入平台配置
     *
     * @param file 导入文件
     */
    @Log(title = "平台配置", operType = DictOperType.IMPORT)
    @SaCheckPermission("base:platform:import")
    @PostMapping(value = "/importPlatforms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importPlatforms(MultipartFile file) throws Exception {
        ExcelResult<PlatformVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), PlatformVo.class, true);
        List<PlatformVo> platforms = excelResult.getList();

        // 分别处理新增和更新
        for (PlatformVo platform : platforms) {
            handleImport(platform);
        }

        // 批量保存数据
        List<PlatformBo> boList = MapstructUtils.convert(platforms, PlatformBo.class);
        iPlatformService.batchSave(boList);

        log.info("导入平台配置完成：共{}个配置", platforms.size());
        return R.ok(excelResult.getAnalysis());
    }


    // ============== 私有方法 ==============

    /**
     * 处理导入操作（新增或更新）
     */
    private void handleImport(PlatformVo platform) {
        String oldAppid = null;
        String oldStatus = null;

        if (platform.getId() == null) {
            // 新增操作
            validateAppidUnique(platform.getAppid(), null);
        } else {
            // 更新操作
            PlatformVo original = iPlatformService.get(platform.getId());
            if (original == null) {
                throw new ServiceException(StringUtils.format("平台配置[ID:{}]不存在", platform.getId()));
            }

            oldAppid = original.getAppid();
            oldStatus = original.getStatus();

            validateAppidUnique(platform.getAppid(), platform.getId());
        }

        // 处理配置变更
        handlePlatformConfigChange(oldAppid, platform.getAppid(), oldStatus, platform.getStatus());
    }

    /**
     * 处理平台配置变更的统一入口
     */
    private void handlePlatformConfigChange(String oldAppid, String newAppid, String oldStatus, String newStatus) {
        String currentTenantId = TenantHelper.getTenantId();

        // 处理旧配置的移除
        if (shouldRemoveConfig(oldAppid, oldStatus, newAppid, newStatus)) {
            removePlatformConfig(oldAppid);
            // 广播到其他集群节点
            PlatformConfigSyncListener.publishRemove(oldAppid, currentTenantId);
        }

        // 处理新配置的添加
        if (shouldAddConfig(newAppid, newStatus)) {
            addPlatformConfig(newAppid);
            // 广播到其他集群节点
            PlatformConfigSyncListener.publishAdd(newAppid, currentTenantId);
        }

        // 重新初始化支付配置
        refreshPaymentConfigs();
        // 广播支付配置刷新到其他集群节点
        PlatformConfigSyncListener.publishRefreshPay(currentTenantId);
    }

    /**
     * 判断是否需要移除配置
     */
    private boolean shouldRemoveConfig(String oldAppid, String oldStatus, String newAppid, String newStatus) {
        if (StringUtils.isBlank(oldAppid)) {
            // 新增操作，无需移除
            return false;
        }

        if (newAppid == null) {
            // 删除操作，需要移除
            return true;
        }
        // 新旧appid不同 需要移除
        boolean appidChanged = !Objects.equals(oldAppid, newAppid);
        // 从启用修改为禁用 需要移除
        boolean statusChanged = DictEnableStatus.ENABLE.getValue().equals(oldStatus) &&
            DictEnableStatus.DISABLED.getValue().equals(newStatus);

        return appidChanged || statusChanged;
    }

    /**
     * 判断是否需要添加配置
     */
    private boolean shouldAddConfig(String newAppid, String newStatus) {
        // newAppid不为空且状态为开启
        return StringUtils.isNotBlank(newAppid) &&
            DictEnableStatus.ENABLE.getValue().equals(newStatus);
    }

    /**
     * 校验appid的全局唯一性
     */
    private void validateAppidUnique(String appid, Long excludeId) {
        if (StringUtils.isBlank(appid)) {
            throw new ServiceException("AppID不能为空");
        }

        String currentTenantId = TenantHelper.getTenantId();
        PlatformDTO existing = platformService.getPlatformByAppid(appid, null);

        if (existing != null &&
            !currentTenantId.equals(existing.getTenantId()) &&
            !Objects.equals(excludeId, existing.getId())) {
            throw new ServiceException(StringUtils.format(
                "AppID [{}] 已被其他租户使用，不能重复添加", appid
            ));
        }
    }

    /**
     * 添加单个平台配置
     */
    private void addPlatformConfig(String appid) {
        if (StringUtils.isBlank(appid)) {
            return;
        }

        PlatformDTO platform = platformService.getPlatformByAppid(appid, TenantHelper.getTenantId());
        if (platform == null) {
            log.warn("未找到appid={}的平台配置", appid);
            return;
        }

        // 检查配置状态，只有启用状态才添加
        if (!DictEnableStatus.ENABLE.getValue().equals(platform.getStatus())) {
            log.info("平台配置[{}]为禁用状态，跳过添加", appid);
            return;
        }

        // 根据平台类型分别处理
        String platformType = platform.getType();
        if (DictPlatformType.MP_WEIXIN.getValue().equals(platformType)) {
            SpringUtils.getBean(MiniappConfigInitializer.class).addConfig(platform);

        } else if (DictPlatformType.MP_OFFICIAL_ACCOUNT.getValue().equals(platformType)) {
            SpringUtils.getBean(MpConfigInitializer.class).addConfig(platform);
        }
    }

    /**
     * 移除单个平台配置
     */
    private void removePlatformConfig(String appid) {
        if (StringUtils.isBlank(appid)) {
            return;
        }

        PlatformDTO platform = platformService.getPlatformByAppid(appid, TenantHelper.getTenantId());
        if (platform == null) {
            log.warn("未找到appid={}的平台配置", appid);
            return;
        }
        // 根据平台类型分别处理
        String platformType = platform.getType();
        if (DictPlatformType.MP_WEIXIN.getValue().equals(platformType)) {
            SpringUtils.getBean(MiniappConfigInitializer.class).removeConfig(appid);
        } else if (DictPlatformType.MP_OFFICIAL_ACCOUNT.getValue().equals(platformType)) {
            SpringUtils.getBean(MpConfigInitializer.class).removeConfig(appid);
        }
    }

    /**
     * 刷新支付配置
     */
    private void refreshPaymentConfigs() {
        String currentTenantId = TenantHelper.getTenantId();

        // 刷新 pay 模块的支付配置
        SpringUtils.getBean(PayConfigInitializer.class).initByTenant(currentTenantId);

        log.info("重新初始化租户[{}]的支付配置", currentTenantId);
    }


}
