package plus.ruoyi.system.config.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.utils.MapstructUtils;
import plus.ruoyi.common.excel.core.ExcelResult;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.config.domain.bo.SysConfigBo;
import plus.ruoyi.system.config.domain.vo.SysConfigVo;
import plus.ruoyi.system.config.service.ISysConfigService;

/**
 * 参数配置 信息操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController {

    // 业务服务
    private final ISysConfigService configService;

    /**
     * 查询参数配置列表
     */
    @SaCheckPermission("system:config:query")
    @GetMapping("/pageConfigs")
    public R<PageResult<SysConfigVo>> pageConfigs(SysConfigBo config, PageQuery pageQuery) {
        return R.ok(configService.page(config, pageQuery));
    }


    /**
     * 获取参数配置详细信息
     *
     * @param configId 参数ID
     */
    @SaCheckPermission("system:config:query")
    @GetMapping("/getConfig/{configId}")
    public R<SysConfigVo> getConfig(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long configId) {

        return R.ok(configService.get(configId));
    }

    /**
     * 根据参数键名查询参数值
     *
     * @param configKey 参数Key
     */
    @GetMapping(value = "/getByConfigKey/{configKey}")
    public R<String> getByConfigKey(@NotBlank(message = "配置key不能为空") @PathVariable String configKey) {
        return R.ok(I18nKeys.Oper.QUERY_SUCCESS, configService.getConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @SaCheckPermission("system:config:add")
    @Log(title = "参数管理", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addConfig")
    public R<Void> addConfig(@Validated @RequestBody SysConfigBo config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        configService.insertConfig(config);
        return R.ok();
    }

    /**
     * 修改参数配置
     */
    @SaCheckPermission("system:config:update")
    @Log(title = "参数管理", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateConfig")
    public R<Void> updateConfig(@Validated @RequestBody SysConfigBo config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 根据参数键名修改参数配置
     */
    @SaCheckPermission("system:config:update")
    @Log(title = "参数管理", operType = DictOperType.UPDATE)
    @PutMapping("/updateConfigByKey")
    public R<Void> updateConfigByKey(@RequestBody SysConfigBo config) {
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 删除参数配置
     *
     * @param configIds 参数ID串
     */
    @SaCheckPermission("system:config:delete")
    @Log(title = "参数管理", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteConfigs/{configIds}")
    public R<Void> deleteConfigs(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] configIds) {
        configService.deleteConfigByIds(List.of(configIds));
        return R.ok();
    }

    /**
     * 清除参数缓存
     */
    @SaCheckPermission("system:config:delete")
    @Log(title = "参数管理", operType = DictOperType.CLEAN)
    @DeleteMapping("/clearConfigCache")
    public R<Void> clearConfigCache() {
        configService.clearConfigCache();
        return R.ok();
    }

    /**
     * 导出参数配置列表
     */
    @SaCheckPermission("system:config:export")
    @Log(title = "参数配置", operType = DictOperType.EXPORT)
    @PostMapping("/exportConfigs")
    public void exportConfigs(SysConfigBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysConfigVo> pageResult = configService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "参数配置", SysConfigVo.class, response);
    }

    /**
     * 获取参数配置导入模板
     */
    @PostMapping("/templateConfigs")
    public void templateConfigs(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "参数配置模板", SysConfigVo.class, response);
    }

    /**
     * 导入参数配置
     *
     * @param file 导入文件
     */
    @Log(title = "参数配置", operType = DictOperType.IMPORT)
    @SaCheckPermission("system:config:add")
    @PostMapping(value = "/importConfigs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importConfigs(MultipartFile file) throws Exception {
        ExcelResult<SysConfigVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), SysConfigVo.class, true);
        List<SysConfigBo> boList = MapstructUtils.convert(excelResult.getList(), SysConfigBo.class);
        configService.batchSave(boList);
        configService.clearConfigCache();
        return R.ok(excelResult.getAnalysis());
    }
}
