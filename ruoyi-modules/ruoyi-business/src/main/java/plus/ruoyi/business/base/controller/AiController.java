package plus.ruoyi.business.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.ruoyi.business.base.domain.bo.AiChatBo;
import plus.ruoyi.business.base.domain.vo.AiChatVo;
import plus.ruoyi.business.base.service.IAiService;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.log.annotation.Log;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.openapi.annotation.OpenApi;

/**
 * AI助手控制器
 *
 * <p>条件启用: 需要 langchain4j.enabled=true
 *
 * @author 抓蛙师
 * @date 2025-01-26
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/ai")
@ConditionalOnProperty(
    prefix = "langchain4j",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AiController {

    private final IAiService aiService;

    /**
     * AI对话 - 统一入口
     */
    @SaCheckPermission("base:ai:chat")
    @Log(title = "AI对话", operType = DictOperType.OTHER)
    @PostMapping("/aiChat")
    public R<AiChatVo> aiChat(@Valid @RequestBody AiChatBo bo) {
        return R.ok(aiService.aiChat(bo));
    }

    /**
     * 文本优化
     */
    @SaCheckPermission("base:ai:chat")
    @Log(title = "AI文本优化", operType = DictOperType.OTHER)
    @PostMapping("/aiOptimize")
    public R<AiChatVo> aiOptimize(@Valid @RequestBody AiChatBo bo) {
        return R.ok(aiService.aiOptimize(bo));
    }

    /**
     * 数据生成
     */
    @SaCheckPermission("base:ai:chat")
    @Log(title = "AI数据生成", operType = DictOperType.OTHER)
    @PostMapping("/aiGenerate")
    public R<AiChatVo> aiGenerate(@Valid @RequestBody AiChatBo bo) {
        return R.ok(aiService.aiGenerate(bo));
    }

    /**
     * 内容审核
     */
    @SaCheckPermission("base:ai:chat")
    @Log(title = "AI内容审核", operType = DictOperType.OTHER)
    @PostMapping("/aiReview")
    public R<AiChatVo> aiReview(@Valid @RequestBody AiChatBo bo) {
        return R.ok(aiService.aiReview(bo));
    }

    /**
     * 文本翻译
     */
    @SaCheckPermission("base:ai:chat")
    @Log(title = "AI文本翻译", operType = DictOperType.OTHER)
    @PostMapping("/aiTranslate")
    public R<AiChatVo> aiTranslate(@Valid @RequestBody AiChatBo bo) {
        return R.ok(aiService.aiTranslate(bo));
    }
}
