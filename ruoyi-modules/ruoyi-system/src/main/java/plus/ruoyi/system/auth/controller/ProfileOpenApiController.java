package plus.ruoyi.system.auth.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.core.domain.vo.OpenApiInfoVo;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.idempotent.annotation.RepeatSubmit;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.openapi.config.OpenApiProperties;
import plus.ruoyi.common.openapi.service.OpenApiScanService;
import plus.ruoyi.common.satoken.utils.LoginHelper;

import plus.ruoyi.system.openapi.domain.bo.SysApiKeyBo;
import plus.ruoyi.system.openapi.domain.vo.OpenApiSecretVo;
import plus.ruoyi.system.openapi.domain.vo.SysApiKeyVo;
import plus.ruoyi.system.openapi.service.ISysApiKeyService;

/**
 * 个人中心 - API密钥管理
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/profile/openApi")
public class ProfileOpenApiController {

    // 业务服务
    private final ISysApiKeyService apiKeyService;

    // 配置属性
    private final OpenApiProperties openApiProperties;
    private final OpenApiScanService openApiScanService;

    /**
     * 查询当前用户的API密钥列表
     */
    @GetMapping("/listMyApiKeys")
    public R<List<SysApiKeyVo>> listMyApiKeys() {
        Long userId = LoginHelper.getUserId();
        SysApiKeyBo query = new SysApiKeyBo();
        query.setUserId(userId);
        return R.ok(apiKeyService.list(query));
    }

    /**
     * 生成个人API密钥
     */
    @Log(title = "个人API密钥", operType = DictOperType.INSERT)
    @RepeatSubmit()
    @PostMapping("/generateMyApiKey")
    public R<OpenApiSecretVo> generateMyApiKey(@Validated(AddGroup.class) @RequestBody SysApiKeyBo bo) {
        Long userId = LoginHelper.getUserId();

        // 检查是否超过限制
        SysApiKeyBo query = new SysApiKeyBo();
        query.setUserId(userId);
        Long existingKeys = apiKeyService.count(query);
        if (existingKeys >= openApiProperties.getMaxKeys()) {
            return R.fail("您最多只能创建" + openApiProperties.getMaxKeys() + "个API密钥");
        }

        // 设置为当前用户
        bo.setUserId(userId);

        return R.ok(apiKeyService.generate(bo));
    }

    /**
     * 删除个人API密钥
     */
    @Log(title = "个人API密钥", operType = DictOperType.DELETE)
    @DeleteMapping("/deleteMyApiKey/{id}")
    public R<Void> deleteMyApiKey(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();

        // 验证是否是当前用户的密钥
        SysApiKeyVo apiKey = apiKeyService.get(id);
        if (apiKey == null || !userId.equals(apiKey.getUserId())) {
            return R.fail("无权删除此密钥");
        }

        return R.status(apiKeyService.batchDelete(List.of(id)));
    }

    /**
     * 重置个人API密钥
     */
    @Log(title = "个人API密钥", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/resetMyApiKey/{id}")
    public R<OpenApiSecretVo> resetMyApiKey(@PathVariable Long id) {
        Long userId = LoginHelper.getUserId();

        // 验证是否是当前用户的密钥
        SysApiKeyVo apiKey = apiKeyService.get(id);
        if (apiKey == null || !userId.equals(apiKey.getUserId())) {
            return R.fail("无权重置此密钥");
        }

        return R.ok(apiKeyService.resetSecret(id));
    }

    /**
     * 更新个人API密钥的白名单
     */
    @Log(title = "个人API密钥", operType = DictOperType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/updateMyApiKeyWhiteIps/{id}")
    public R<Void> updateMyApiKeyWhiteIps(@PathVariable Long id, @RequestBody SysApiKeyBo bo) {
        Long userId = LoginHelper.getUserId();

        // 验证是否是当前用户的密钥
        SysApiKeyVo apiKey = apiKeyService.get(id);
        if (apiKey == null || !userId.equals(apiKey.getUserId())) {
            return R.fail("无权修改此密钥");
        }

        // 只更新白名单
        bo.setId(id);
        return R.status(apiKeyService.update(bo));
    }

    /**
     * 获取最大密钥数量配置
     */
    @GetMapping("/getMaxKeys")
    public R<Integer> getMaxKeys() {
        return R.ok(openApiProperties.getMaxKeys());
    }

    /**
     * 获取当前用户可访问的开放接口列表
     */
    @GetMapping("/listMyOpenApis")
    public R<List<OpenApiInfoVo>> listMyOpenApis() {
        return R.ok(openApiScanService.scanUserOpenApis());
    }
}


