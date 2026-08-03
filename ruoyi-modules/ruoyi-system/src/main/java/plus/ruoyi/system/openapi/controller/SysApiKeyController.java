package plus.ruoyi.system.openapi.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.excel.utils.ExcelUtil;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.mybatis.core.page.PageQuery;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import plus.ruoyi.system.openapi.domain.bo.SysApiKeyBo;
import plus.ruoyi.system.openapi.domain.vo.OpenApiSecretVo;
import plus.ruoyi.system.openapi.domain.vo.SysApiKeyVo;
import plus.ruoyi.system.openapi.service.ISysApiKeyService;

/**
 * API密钥管理
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/openApi")
public class SysApiKeyController {

    // 业务服务
    private final ISysApiKeyService apiKeyService;

    /**
     * 查询API密钥列表
     */
    @SaCheckPermission("system:apiKey:query")
    @GetMapping("/pageApiKeys")
    public R<PageResult<SysApiKeyVo>> pageApiKeys(SysApiKeyBo bo, PageQuery pageQuery) {
        return R.ok(apiKeyService.page(bo, pageQuery));
    }

    /**
     * 获取API密钥详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:apiKey:query")
    @GetMapping("/getApiKey/{id}")
    public R<SysApiKeyVo> getApiKey(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(apiKeyService.get(id));
    }

    /**
     * 生成新的API密钥
     */
    @SaCheckPermission("system:apiKey:add")
    @Log(title = "API密钥", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/generateApiKey")
    public R<OpenApiSecretVo> generateApiKey(@Validated(AddGroup.class) @RequestBody SysApiKeyBo bo) {
        return R.ok(apiKeyService.generate(bo));
    }

    /**
     * 修改API密钥
     */
    @SaCheckPermission("system:apiKey:update")
    @Log(title = "API密钥", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateApiKey")
    public R<Void> updateApiKey(@Validated(EditGroup.class) @RequestBody SysApiKeyBo bo) {
        return R.status(apiKeyService.update(bo));
    }

    /**
     * 重置密钥(重新生成AppSecret)
     *
     * @param id 主键
     */
    @SaCheckPermission("system:apiKey:update")
    @Log(title = "API密钥", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/resetSecret/{id}")
    public R<OpenApiSecretVo> resetSecret(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long id) {
        return R.ok(apiKeyService.resetSecret(id));
    }

    /**
     * 删除API密钥
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:apiKey:delete")
    @Log(title = "API密钥", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteApiKeys/{ids}")
    public R<Void> deleteApiKeys(@NotEmpty(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long[] ids) {
        return R.status(apiKeyService.batchDelete(List.of(ids)));
    }

    /**
     * 导出API密钥列表
     */
    @SaCheckPermission("system:apiKey:export")
    @Log(title = "API密钥", operType = DictOperType.EXPORT)
    @PostMapping("/exportApiKeys")
    public void exportApiKeys(SysApiKeyBo bo, PageQuery pageQuery, HttpServletResponse response) {
        PageResult<SysApiKeyVo> pageResult = apiKeyService.page(bo, pageQuery);
        ExcelUtil.exportExcel(pageResult.getRecords(), "API密钥", SysApiKeyVo.class, response);
    }
}
