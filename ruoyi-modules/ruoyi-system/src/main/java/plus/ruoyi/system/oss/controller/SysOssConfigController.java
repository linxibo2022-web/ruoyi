package plus.ruoyi.system.oss.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.CacheNames;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.core.validate.QueryGroup;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;
import plus.ruoyi.common.redis.utils.CacheUtils;

import plus.ruoyi.system.oss.domain.bo.SysOssConfigBo;
import plus.ruoyi.system.oss.domain.vo.SysOssConfigVo;
import plus.ruoyi.system.oss.service.ISysOssConfigService;

/**
 * 对象存储配置
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/ossConfig")
public class SysOssConfigController {

    // 业务服务
    private final ISysOssConfigService ossConfigService;

    /**
     * 查询对象存储配置列表
     */
    @SaCheckPermission("system:ossConfig:query")
    @GetMapping("/pageOssConfigs")
    public R<PageResult<SysOssConfigVo>> pageOssConfigs(@Validated(QueryGroup.class) SysOssConfigBo bo, PageQuery pageQuery) {
        return R.ok(ossConfigService.page(bo, pageQuery));
    }

    /**
     * 获取对象存储配置详细信息
     *
     * @param ossConfigId OSS配置ID
     */
    @SaCheckPermission("system:ossConfig:query")
    @GetMapping("/getOssConfig/{ossConfigId}")
    public R<SysOssConfigVo> getOssConfig(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long ossConfigId) {
        return R.ok(ossConfigService.get(ossConfigId));
    }

    /**
     * 新增对象存储配置
     */
    @SaCheckPermission("system:ossConfig:add")
    @Log(title = "对象存储配置", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addOssConfig")
    public R<Long> addOssConfig(@Validated(AddGroup.class) @RequestBody SysOssConfigBo bo) {
        // 新增时主键由后端雪花算法生成，bo 中没有 ID，必须用 add 返回的 ID 查询，否则查不到导致 NPE
        Long ossConfigId = ossConfigService.add(bo);
        // 从数据库查询完整的数据做缓存
        SysOssConfigVo configVo = ossConfigService.get(ossConfigId);
        CacheUtils.put(CacheNames.SYS_OSS_CONFIG, configVo.getConfigKey(), JsonUtils.toJsonString(configVo));
        return R.ok();
    }

    /**
     * 修改对象存储配置
     */
    @SaCheckPermission("system:ossConfig:update")
    @Log(title = "对象存储配置", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateOssConfig")
    public R<Void> updateOssConfig(@Validated(EditGroup.class) @RequestBody SysOssConfigBo bo) {
        ossConfigService.update(bo);
        // 从数据库查询完整的数据做缓存
        SysOssConfigVo configVo = ossConfigService.get(bo.getOssConfigId());
        CacheUtils.put(CacheNames.SYS_OSS_CONFIG, configVo.getConfigKey(), JsonUtils.toJsonString(configVo));
        return R.ok();
    }

    /**
     * 删除对象存储配置
     *
     * @param ossConfigIds OSS配置ID串
     */
    @SaCheckPermission("system:ossConfig:delete")
    @Log(title = "对象存储配置", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteOssConfigs/{ossConfigIds}")
    public R<Void> deleteOssConfigs(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ossConfigIds) {
        List<SysOssConfigVo> ossConfigVos = ossConfigService.listByIds(List.of(ossConfigIds));
        ossConfigService.batchDelete(List.of(ossConfigIds));
        for (SysOssConfigVo configVo : ossConfigVos) {
            CacheUtils.evict(CacheNames.SYS_OSS_CONFIG, configVo.getConfigKey());
        }
        return R.ok();
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:ossConfig:update")
    @Log(title = "对象存储状态修改", operType = DictOperType.UPDATE)
    @PutMapping("/changeOssConfigStatus")
    public R<Void> changeOssConfigStatus(@RequestBody SysOssConfigBo bo) {
        return R.status(ossConfigService.updateOssConfigStatus(bo));
    }
}
