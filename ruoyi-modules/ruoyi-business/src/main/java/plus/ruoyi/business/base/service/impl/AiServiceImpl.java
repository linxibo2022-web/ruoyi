package plus.ruoyi.business.base.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import plus.ruoyi.business.base.domain.bo.AiChatBo;
import plus.ruoyi.business.base.domain.vo.AiChatVo;
import plus.ruoyi.business.base.service.IAiService;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.langchain4j.core.chat.ChatService;
import plus.ruoyi.common.langchain4j.domain.dto.ChatRequest;
import plus.ruoyi.common.langchain4j.domain.dto.AiChatResponse;
import plus.ruoyi.common.langchain4j.enums.ChatMode;

/**
 * AI助手服务实现
 *
 * <p>条件启用: 需要 langchain4j.enabled=true 且 langchain4j.chat.enabled=true
 *
 * @author 抓蛙师
 * @date 2025-01-26
 */
@Slf4j
@Service
@ConditionalOnProperty(
    prefix = "langchain4j",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AiServiceImpl implements IAiService {

    @Autowired(required = false)
    private ChatService chatService;

    /**
     * 统一AI对话入口
     */
    @Override
    public AiChatVo aiChat(AiChatBo bo) {
        // 根据AI功能类型分发到不同的处理方法
        if (StrUtil.isNotBlank(bo.getAiFeature())) {
            return switch (bo.getAiFeature()) {
                case "optimize" -> aiOptimize(bo);
                case "generate" -> aiGenerate(bo);
                case "review" -> aiReview(bo);
                case "translate" -> aiTranslate(bo);
                default -> processChat(bo, null);
            };
        }
        // 默认处理
        return processChat(bo, null);
    }

    /**
     * 文本优化
     */
    @Override
    public AiChatVo aiOptimize(AiChatBo bo) {
        String systemPrompt = StrUtil.isNotBlank(bo.getSystemPrompt())
            ? bo.getSystemPrompt()
            : "你是一个专业的内容优化助手。你的任务是优化用户提供的文本,使其更加流畅、专业、易读。请直接输出优化后的内容,不要添加额外的解释。";

        return processChat(bo, systemPrompt);
    }

    /**
     * 数据生成
     */
    @Override
    public AiChatVo aiGenerate(AiChatBo bo) {
        String systemPrompt = StrUtil.isNotBlank(bo.getSystemPrompt())
            ? bo.getSystemPrompt()
            : "你是一个专业的测试数据生成助手。你的任务是根据用户提供的字段结构生成合理的测试数据。返回JSON数组格式,不要添加markdown标记。";

        return processChat(bo, systemPrompt);
    }

    /**
     * 内容审核
     */
    @Override
    public AiChatVo aiReview(AiChatBo bo) {
        String systemPrompt = StrUtil.isNotBlank(bo.getSystemPrompt())
            ? bo.getSystemPrompt()
            : "你是一个专业的内容审核专家。你的任务是审核用户提供的内容,检查是否存在问题(如敏感词、格式错误、逻辑问题等)。返回JSON格式的审核结果,包含status、score、issues等字段。";

        return processChat(bo, systemPrompt);
    }

    /**
     * 文本翻译
     */
    @Override
    public AiChatVo aiTranslate(AiChatBo bo) {
        String systemPrompt = StrUtil.isNotBlank(bo.getSystemPrompt())
            ? bo.getSystemPrompt()
            : "你是一个专业的翻译助手。你的任务是将用户提供的文本翻译成目标语言。请直接输出翻译结果,保持原文的格式和风格。";

        return processChat(bo, systemPrompt);
    }

    /**
     * 处理AI对话请求
     *
     * @param bo           业务对象
     * @param systemPrompt 系统提示词(如果为null则使用bo中的)
     * @return AI回复
     */
    private AiChatVo processChat(AiChatBo bo, String systemPrompt) {
        try {
            // 构建ChatRequest
            ChatRequest request = new ChatRequest();
            request.setMessage(bo.getMessage());
            request.setProvider(bo.getProvider());
            request.setModelName(bo.getModelName());
            request.setSystemPrompt(systemPrompt != null ? systemPrompt : bo.getSystemPrompt());
            request.setTemperature(bo.getTemperature());
            request.setMaxTokens(bo.getMaxTokens());
            request.setStream(false); // 使用同步模式
            request.setMode(ChatMode.SINGLE); // 使用单轮对话模式(不保存历史)

            // 调用ChatService
            AiChatResponse response = chatService.chat(request);

            // 检查错误
            if (StrUtil.isNotBlank(response.getError())) {
                throw ServiceException.of("AI调用失败: " + response.getError());
            }

            // 转换为VO
            return convertToVo(response);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI对话处理失败", e);
            throw ServiceException.of("AI对话处理失败: " + e.getMessage());
        }
    }

    /**
     * 转换AiChatResponse为AiChatVo
     */
    private AiChatVo convertToVo(AiChatResponse response) {
        AiChatVo vo = AiChatVo.builder()
            .content(response.getContent())
            .responseTime(response.getResponseTime())
            .build();

        // 转换Token使用情况
        if (response.getTokenUsage() != null) {
            vo.setTokenUsage(AiChatVo.TokenUsageVo.builder()
                .promptTokens(response.getTokenUsage().getPromptTokens())
                .completionTokens(response.getTokenUsage().getCompletionTokens())
                .totalTokens(response.getTokenUsage().getTotalTokens())
                .build());
        }

        return vo;
    }
}
