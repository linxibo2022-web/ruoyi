package plus.ruoyi.common.langchain4j.factory;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties.ModelConfig;
import plus.ruoyi.common.langchain4j.enums.ModelProvider;

import java.time.Duration;

/**
 * 模型工厂
 * 负责创建不同提供商的语言模型实例（支持 thinking/reasoning 透传）
 * <p>
 * 支持「按请求覆盖思考参数」：调用方可传入 {@link ThinkingOptions} 临时调整本次对话的
 * 深度思考开关与预算，覆盖只能"收窄"（后端 enableThinking=false 时请求传 true 不生效），
 * 预算可上调（用于"标准 / 深度"档位切换）。
 *
 * @author 抓蛙师
 */
@Slf4j
public class ModelFactory {

    private final LangChain4jProperties properties;

    public ModelFactory(LangChain4jProperties properties) {
        this.properties = properties;
    }

    /**
     * 按请求覆盖的思考参数
     *
     * @param enabled      本次是否启用深度思考；null 表示沿用后端配置
     * @param budgetTokens 思考预算 token 数（仅 Claude / Qwen 生效）；null 表示沿用后端配置
     */
    public record ThinkingOptions(Boolean enabled, Integer budgetTokens) {
        /** 无覆盖（完全沿用后端配置） */
        public static final ThinkingOptions NONE = new ThinkingOptions(null, null);

        /** 是否携带任何覆盖项 */
        public boolean isEmpty() {
            return enabled == null && budgetTokens == null;
        }
    }

    /**
     * 创建聊天模型(非流式)
     */
    public ChatModel createChatModel(String providerCode) {
        return createChatModel(providerCode, null, ThinkingOptions.NONE);
    }

    /**
     * 创建聊天模型(非流式)
     */
    public ChatModel createChatModel(String providerCode, String modelName) {
        return createChatModel(providerCode, modelName, ThinkingOptions.NONE);
    }

    /**
     * 创建聊天模型(非流式，支持按请求覆盖思考参数)
     */
    public ChatModel createChatModel(String providerCode, String modelName, ThinkingOptions thinking) {
        ModelProvider provider = ModelProvider.fromCode(providerCode);
        ThinkingOptions opts = thinking != null ? thinking : ThinkingOptions.NONE;

        return switch (provider) {
            case DEEPSEEK -> createDeepSeekChatModel(modelName, opts);
            case QIANWEN -> createQianWenChatModel(modelName, opts);
            case CLAUDE -> createClaudeChatModel(modelName, opts);
            case OPENAI -> createOpenAiChatModel(modelName, opts);
            case OLLAMA -> createOllamaChatModel(modelName, opts);
        };
    }

    /**
     * 创建流式聊天模型
     */
    public StreamingChatModel createStreamingChatModel(String providerCode) {
        return createStreamingChatModel(providerCode, null, ThinkingOptions.NONE);
    }

    /**
     * 创建流式聊天模型
     */
    public StreamingChatModel createStreamingChatModel(String providerCode, String modelName) {
        return createStreamingChatModel(providerCode, modelName, ThinkingOptions.NONE);
    }

    /**
     * 创建流式聊天模型(支持按请求覆盖思考参数)
     */
    public StreamingChatModel createStreamingChatModel(String providerCode, String modelName, ThinkingOptions thinking) {
        ModelProvider provider = ModelProvider.fromCode(providerCode);
        ThinkingOptions opts = thinking != null ? thinking : ThinkingOptions.NONE;

        return switch (provider) {
            case DEEPSEEK -> createDeepSeekStreamingChatModel(modelName, opts);
            case QIANWEN -> createQianWenStreamingChatModel(modelName, opts);
            case CLAUDE -> createClaudeStreamingChatModel(modelName, opts);
            case OPENAI -> createOpenAiStreamingChatModel(modelName, opts);
            case OLLAMA -> createOllamaStreamingChatModel(modelName, opts);
        };
    }

    // ==================== DeepSeek ====================

    private ChatModel createDeepSeekChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getDeepseek(), thinking);
        validateConfig(config, "DeepSeek");

        // DeepSeek 使用 OpenAI 兼容 API；启用 thinking 时映射 reasoning_content 字段
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(getBaseUrl(config, ModelProvider.DEEPSEEK))
                .modelName(getModelName(modelName, config, "deepseek-chat"))
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens())
                .timeout(properties.getTimeout())
                .maxRetries(properties.getMaxRetries())
                .logRequests(log.isDebugEnabled())
                .logResponses(log.isDebugEnabled());

        if (Boolean.TRUE.equals(config.getEnableThinking())) {
            // OpenAI 兼容协议下 DeepSeek 通过 reasoning_content 字段返回推理内容，
            // langchain4j 1.x OpenAi 客户端在 returnThinking=true 时会自动识别该字段
            builder.returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
        }

        return builder.build();
    }

    private StreamingChatModel createDeepSeekStreamingChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getDeepseek(), thinking);
        validateConfig(config, "DeepSeek");

        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(getBaseUrl(config, ModelProvider.DEEPSEEK))
                .modelName(getModelName(modelName, config, "deepseek-chat"))
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens())
                .timeout(properties.getTimeout())
                .logRequests(log.isDebugEnabled())
                .logResponses(log.isDebugEnabled());

        if (Boolean.TRUE.equals(config.getEnableThinking())) {
            // 同上：DeepSeek streaming 也走 OpenAI 兼容客户端，自动识别 reasoning_content
            builder.returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
        }

        return builder.build();
    }

    // ==================== 通义千问 ====================

    private ChatModel createQianWenChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getQianwen(), thinking);
        validateConfig(config, "QianWen");

        QwenChatModel.QwenChatModelBuilder builder = QwenChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(getModelName(modelName, config, "qwen-turbo"))
                .temperature(config.getTemperature().floatValue())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens());

        // Qwen 1.x 通过 defaultRequestParameters 注入 enable_thinking / thinking_budget
        if (Boolean.TRUE.equals(config.getEnableThinking())) {
            builder.defaultRequestParameters(buildQwenThinkingParameters(config));
        }

        return builder.build();
    }

    private StreamingChatModel createQianWenStreamingChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getQianwen(), thinking);
        validateConfig(config, "QianWen");

        QwenStreamingChatModel.QwenStreamingChatModelBuilder builder = QwenStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(getModelName(modelName, config, "qwen-turbo"))
                .temperature(config.getTemperature().floatValue())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens());

        if (Boolean.TRUE.equals(config.getEnableThinking())) {
            builder.defaultRequestParameters(buildQwenThinkingParameters(config));
        }

        return builder.build();
    }

    /**
     * 构造 Qwen 思考参数：enable_thinking=true 时同时透传预算 token 数
     */
    private QwenChatRequestParameters buildQwenThinkingParameters(ModelConfig config) {
        QwenChatRequestParameters.Builder builder = QwenChatRequestParameters.builder()
                .enableThinking(Boolean.TRUE);
        Integer budget = config.getThinkingBudgetTokens();
        if (budget != null && budget > 0) {
            builder.thinkingBudget(budget);
        }
        return builder.build();
    }

    // ==================== Anthropic Claude ====================

    private ChatModel createClaudeChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getClaude(), thinking);
        validateConfig(config, "Claude");

        AnthropicChatModel.AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(getBaseUrl(config, ModelProvider.CLAUDE))
                .modelName(getModelName(modelName, config, "claude-3-5-sonnet-20241022"))
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens())
                .timeout(properties.getTimeout())
                .maxRetries(properties.getMaxRetries())
                .logRequests(log.isDebugEnabled())
                .logResponses(log.isDebugEnabled());

        applyClaudeThinking(builder, config);
        return builder.build();
    }

    private StreamingChatModel createClaudeStreamingChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getClaude(), thinking);
        validateConfig(config, "Claude");

        AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder = AnthropicStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(getBaseUrl(config, ModelProvider.CLAUDE))
                .modelName(getModelName(modelName, config, "claude-3-5-sonnet-20241022"))
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens())
                .timeout(properties.getTimeout())
                .logRequests(log.isDebugEnabled())
                .logResponses(log.isDebugEnabled());

        applyClaudeStreamingThinking(builder, config);
        return builder.build();
    }

    /**
     * Claude 同步：thinkingType=enabled + thinkingBudgetTokens 必填
     */
    private void applyClaudeThinking(AnthropicChatModel.AnthropicChatModelBuilder builder, ModelConfig config) {
        if (!Boolean.TRUE.equals(config.getEnableThinking())) {
            return;
        }
        Integer budget = ensureClaudeBudget(config);
        builder.thinkingType("enabled")
                .thinkingBudgetTokens(budget)
                .returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
    }

    /**
     * Claude 流式：同上
     */
    private void applyClaudeStreamingThinking(AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder, ModelConfig config) {
        if (!Boolean.TRUE.equals(config.getEnableThinking())) {
            return;
        }
        Integer budget = ensureClaudeBudget(config);
        builder.thinkingType("enabled")
                .thinkingBudgetTokens(budget)
                .returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
    }

    /**
     * 校验 Claude thinkingBudgetTokens < maxTokens（Anthropic API 强约束）
     */
    private Integer ensureClaudeBudget(ModelConfig config) {
        Integer budget = config.getThinkingBudgetTokens();
        Integer maxTokens = config.getMaxTokens();
        if (budget == null || budget <= 0) {
            budget = 1024;
        }
        if (maxTokens != null && budget >= maxTokens) {
            log.warn("Claude thinkingBudgetTokens({}) 必须小于 maxTokens({})，自动调整为 maxTokens/2", budget, maxTokens);
            budget = Math.max(1024, maxTokens / 2);
        }
        return budget;
    }


    // ==================== OpenAI ====================

    private ChatModel createOpenAiChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getOpenai(), thinking);
        validateConfig(config, "OpenAI");

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(getBaseUrl(config, ModelProvider.OPENAI))
                .modelName(getModelName(modelName, config, "gpt-4o-mini"))
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens())
                .timeout(properties.getTimeout())
                .maxRetries(properties.getMaxRetries())
                .logRequests(log.isDebugEnabled())
                .logResponses(log.isDebugEnabled());

        applyOpenAiReasoning(builder, config);
        return builder.build();
    }

    private StreamingChatModel createOpenAiStreamingChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getOpenai(), thinking);
        validateConfig(config, "OpenAI");

        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(getBaseUrl(config, ModelProvider.OPENAI))
                .modelName(getModelName(modelName, config, "gpt-4o-mini"))
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .maxTokens(config.getMaxTokens())
                .timeout(properties.getTimeout())
                .logRequests(log.isDebugEnabled())
                .logResponses(log.isDebugEnabled());

        applyOpenAiStreamingReasoning(builder, config);
        return builder.build();
    }

    /**
     * OpenAI 同步：reasoningEffort 仅 o1/o3/gpt-5-thinking 等推理模型生效
     */
    private void applyOpenAiReasoning(OpenAiChatModel.OpenAiChatModelBuilder builder, ModelConfig config) {
        if (!Boolean.TRUE.equals(config.getEnableThinking())) {
            return;
        }
        if (StrUtil.isNotBlank(config.getReasoningEffort())) {
            builder.reasoningEffort(config.getReasoningEffort());
        }
        builder.returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
    }

    /**
     * OpenAI 流式：同上
     */
    private void applyOpenAiStreamingReasoning(OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder, ModelConfig config) {
        if (!Boolean.TRUE.equals(config.getEnableThinking())) {
            return;
        }
        if (StrUtil.isNotBlank(config.getReasoningEffort())) {
            builder.reasoningEffort(config.getReasoningEffort());
        }
        builder.returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
    }

    // ==================== Ollama ====================

    private ChatModel createOllamaChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getOllama(), thinking);

        OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                .baseUrl(getBaseUrl(config, ModelProvider.OLLAMA))
                .modelName(getModelName(modelName, config, "llama3.2"))
                .temperature(config.getTemperature())
                .timeout(Duration.ofMinutes(5));

        if (Boolean.TRUE.equals(config.getEnableThinking())) {
            builder.think(Boolean.TRUE)
                    .returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
        }

        return builder.build();
    }

    private StreamingChatModel createOllamaStreamingChatModel(String modelName, ThinkingOptions thinking) {
        ModelConfig config = effectiveConfig(properties.getOllama(), thinking);

        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = OllamaStreamingChatModel.builder()
                .baseUrl(getBaseUrl(config, ModelProvider.OLLAMA))
                .modelName(getModelName(modelName, config, "llama3.2"))
                .temperature(config.getTemperature())
                .timeout(Duration.ofMinutes(5));

        if (Boolean.TRUE.equals(config.getEnableThinking())) {
            builder.think(Boolean.TRUE)
                    .returnThinking(Boolean.TRUE.equals(config.getReturnThinking()));
        }

        return builder.build();
    }

    // ==================== 辅助方法 ====================

    private void validateConfig(ModelConfig config, String providerName) {
        if (StrUtil.isBlank(config.getApiKey())) {
            throw new IllegalStateException(
                    StrUtil.format("{} API Key is not configured", providerName)
            );
        }
    }

    private String getBaseUrl(ModelConfig config, ModelProvider provider) {
        return StrUtil.isNotBlank(config.getBaseUrl())
                ? config.getBaseUrl()
                : provider.getDefaultBaseUrl();
    }

    private String getModelName(String requestModelName, ModelConfig config, String defaultModel) {
        if (StrUtil.isNotBlank(requestModelName)) {
            return requestModelName;
        }
        if (StrUtil.isNotBlank(config.getModelName())) {
            return config.getModelName();
        }
        return defaultModel;
    }

    /**
     * 计算"生效配置"：在不污染共享 {@link ModelConfig} 实例的前提下，把请求级思考覆盖合并进去
     * <p>
     * 覆盖规则：
     * <ul>
     *   <li>enableThinking 只能"收窄"——后端为 true 时请求可改 false（临时关闭）；后端为 false 时请求传 true 无效</li>
     *   <li>thinkingBudgetTokens 可上调（用于"标准 / 深度"档位切换），&lt;=0 视为无效</li>
     * </ul>
     * 无覆盖项时直接返回原配置，避免不必要的拷贝。
     * <p>
     * ⚠️ {@link ModelConfig} 新增字段时需同步此处的浅拷贝。
     */
    private ModelConfig effectiveConfig(ModelConfig base, ThinkingOptions thinking) {
        if (thinking == null || thinking.isEmpty()) {
            return base;
        }
        ModelConfig eff = new ModelConfig();
        eff.setApiKey(base.getApiKey());
        eff.setBaseUrl(base.getBaseUrl());
        eff.setModelName(base.getModelName());
        eff.setTemperature(base.getTemperature());
        eff.setTopP(base.getTopP());
        eff.setMaxTokens(base.getMaxTokens());
        eff.setEnabled(base.getEnabled());
        eff.setEnableThinking(base.getEnableThinking());
        eff.setThinkingBudgetTokens(base.getThinkingBudgetTokens());
        eff.setReasoningEffort(base.getReasoningEffort());
        eff.setReturnThinking(base.getReturnThinking());
        eff.setExtraParams(base.getExtraParams());

        // 只能收窄：后端开了思考才允许请求改写
        if (thinking.enabled() != null && Boolean.TRUE.equals(base.getEnableThinking())) {
            eff.setEnableThinking(thinking.enabled());
        }
        // 预算覆盖（仅在思考最终生效时才有意义，但提前写入无副作用）
        if (thinking.budgetTokens() != null && thinking.budgetTokens() > 0) {
            eff.setThinkingBudgetTokens(thinking.budgetTokens());
        }
        return eff;
    }
}
