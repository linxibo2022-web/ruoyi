package plus.ruoyi.common.langchain4j.core.chat;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;
import plus.ruoyi.common.langchain4j.domain.dto.AiChatResponse;
import plus.ruoyi.common.langchain4j.domain.dto.ChatRequest;
import plus.ruoyi.common.langchain4j.domain.dto.TokenUsage;
import plus.ruoyi.common.langchain4j.factory.ModelFactory;
import plus.ruoyi.common.langchain4j.factory.ModelFactory.ThinkingOptions;

import java.util.List;
import java.util.function.Consumer;

/**
 * 对话服务（langchain4j 1.x）
 * <p>
 * 提供 AI 同步 / 流式对话能力，统一封装 SINGLE / CONTINUOUS / RAG / FUNCTION 四种对话模式。
 * 支持 thinking / reasoning_content 透传（依赖 ModelFactory 注入推理参数）。
 *
 * @author 抓蛙师
 */
@Slf4j
public class ChatService {

    private final ModelFactory modelFactory;
    private final ChatMemoryManager memoryManager;
    private final LangChain4jProperties properties;

    public ChatService(ModelFactory modelFactory,
                       ChatMemoryManager memoryManager,
                       LangChain4jProperties properties) {
        this.modelFactory = modelFactory;
        this.memoryManager = memoryManager;
        this.properties = properties;
    }

    /**
     * 同步对话
     */
    public AiChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String sessionId = getOrGenerateSessionId(request);
            String provider = StrUtil.isNotBlank(request.getProvider())
                ? request.getProvider()
                : properties.getDefaultProvider();

            ChatModel chatModel = modelFactory.createChatModel(
                provider, request.getModelName(), resolveThinkingOptions(request));

            AiChatResponse response = switch (request.getMode()) {
                case SINGLE -> handleSingleChat(chatModel, request);
                case CONTINUOUS -> handleContinuousChat(chatModel, request, sessionId);
                case RAG -> handleRagChat(chatModel, request, sessionId);
                case FUNCTION -> handleFunctionChat(chatModel, request, sessionId);
            };

            response.setSessionId(sessionId);
            response.setResponseTime(System.currentTimeMillis() - startTime);
            return response;

        } catch (Exception e) {
            log.error("Chat error: {}", e.getMessage(), e);
            return AiChatResponse.builder()
                .error(e.getMessage())
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    /**
     * 流式对话
     */
    public void streamChat(ChatRequest request, Consumer<AiChatResponse> responseConsumer) {
        try {
            String sessionId = getOrGenerateSessionId(request);
            String provider = StrUtil.isNotBlank(request.getProvider())
                ? request.getProvider()
                : properties.getDefaultProvider();

            StreamingChatModel streamingModel = modelFactory.createStreamingChatModel(
                provider, request.getModelName(), resolveThinkingOptions(request));

            switch (request.getMode()) {
                case SINGLE -> handleSingleStreamChat(streamingModel, request, responseConsumer);
                case CONTINUOUS -> handleContinuousStreamChat(streamingModel, request, sessionId, responseConsumer);
                case RAG -> handleRagStreamChat(streamingModel, request, sessionId, responseConsumer);
                case FUNCTION -> handleFunctionStreamChat(streamingModel, request, sessionId, responseConsumer);
            }

        } catch (Exception e) {
            log.error("Stream chat error: {}", e.getMessage(), e);
            responseConsumer.accept(AiChatResponse.builder()
                .error(e.getMessage())
                .finished(true)
                .build());
        }
    }

    /**
     * 单轮对话(不保存历史)
     */
    private AiChatResponse handleSingleChat(ChatModel chatModel, ChatRequest request) {
        UserMessage userMessage = UserMessage.from(request.getMessage());

        ChatResponse response;
        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            SystemMessage systemMessage = SystemMessage.from(request.getSystemPrompt());
            response = chatModel.chat(systemMessage, userMessage);
        } else {
            response = chatModel.chat(userMessage);
        }

        return buildResponse(response);
    }

    /**
     * 多轮对话(保存历史)
     */
    private AiChatResponse handleContinuousChat(
        ChatModel chatModel,
        ChatRequest request,
        String sessionId) {

        var memory = memoryManager.getOrCreateMemory(sessionId);

        if (memory.messages().isEmpty() && StrUtil.isNotBlank(request.getSystemPrompt())) {
            memory.add(SystemMessage.from(request.getSystemPrompt()));
        }

        UserMessage userMessage = UserMessage.from(request.getMessage());
        memory.add(userMessage);

        ChatResponse response = chatModel.chat(memory.messages());

        // 保存 AI 回复到 memory（仅保留 text，不持久化 thinking）
        memory.add(response.aiMessage());

        return buildResponse(response);
    }

    /**
     * RAG对话(暂时简化实现)
     */
    private AiChatResponse handleRagChat(
        ChatModel chatModel,
        ChatRequest request,
        String sessionId) {
        // TODO: 实现RAG检索逻辑
        return handleContinuousChat(chatModel, request, sessionId);
    }

    /**
     * 函数调用对话(暂时简化实现)
     */
    private AiChatResponse handleFunctionChat(
        ChatModel chatModel,
        ChatRequest request,
        String sessionId) {
        // TODO: 实现函数调用逻辑
        return handleContinuousChat(chatModel, request, sessionId);
    }

    /**
     * 单轮流式对话
     */
    private void handleSingleStreamChat(
        StreamingChatModel streamingModel,
        ChatRequest request,
        Consumer<AiChatResponse> responseConsumer) {

        UserMessage userMessage = UserMessage.from(request.getMessage());
        String messageId = IdUtil.fastSimpleUUID();
        StringBuilder fullContent = new StringBuilder();

        var handler = new StreamChatHandler(messageId, responseConsumer, fullContent);

        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            SystemMessage systemMessage = SystemMessage.from(request.getSystemPrompt());
            streamingModel.chat(List.of(systemMessage, userMessage), handler);
        } else {
            streamingModel.chat(List.of(userMessage), handler);
        }
    }

    /**
     * 多轮流式对话
     */
    private void handleContinuousStreamChat(
        StreamingChatModel streamingModel,
        ChatRequest request,
        String sessionId,
        Consumer<AiChatResponse> responseConsumer) {

        var memory = memoryManager.getOrCreateMemory(sessionId);

        if (memory.messages().isEmpty() && StrUtil.isNotBlank(request.getSystemPrompt())) {
            memory.add(SystemMessage.from(request.getSystemPrompt()));
        }

        UserMessage userMessage = UserMessage.from(request.getMessage());
        memory.add(userMessage);

        String messageId = IdUtil.fastSimpleUUID();
        StringBuilder fullContent = new StringBuilder();

        var handler = new StreamChatHandler(messageId, responseConsumer, fullContent, () -> {
            // 流式完成后，保存 AI 回复（仅 text）到 memory
            memory.add(AiMessage.from(fullContent.toString()));
        });

        streamingModel.chat(memory.messages(), handler);
    }

    /**
     * RAG流式对话
     */
    private void handleRagStreamChat(
        StreamingChatModel streamingModel,
        ChatRequest request,
        String sessionId,
        Consumer<AiChatResponse> responseConsumer) {
        // TODO: 实现RAG流式检索
        handleContinuousStreamChat(streamingModel, request, sessionId, responseConsumer);
    }

    /**
     * 函数调用流式对话
     */
    private void handleFunctionStreamChat(
        StreamingChatModel streamingModel,
        ChatRequest request,
        String sessionId,
        Consumer<AiChatResponse> responseConsumer) {
        // TODO: 实现函数调用流式
        handleContinuousStreamChat(streamingModel, request, sessionId, responseConsumer);
    }

    /**
     * 构建同步响应对象（含 thinking 内容）
     */
    private AiChatResponse buildResponse(ChatResponse response) {
        AiMessage aiMessage = response.aiMessage();
        // langchain4j 1.x 在 AiMessage.thinking() 上承载推理内容（不带 <think> 标签）
        String thinking = aiMessage != null ? aiMessage.thinking() : null;
        String text = aiMessage != null ? aiMessage.text() : null;

        AiChatResponse chatResponse = AiChatResponse.builder()
            .messageId(IdUtil.fastSimpleUUID())
            .content(text)
            .reasoningContent(StrUtil.isNotBlank(thinking) ? thinking : null)
            .phase(AiChatResponse.Phase.CONTENT)
            .finished(true)
            .build();

        if (response.tokenUsage() != null) {
            TokenUsage tokenUsage = new TokenUsage();
            tokenUsage.setPromptTokens(response.tokenUsage().inputTokenCount());
            tokenUsage.setCompletionTokens(response.tokenUsage().outputTokenCount());
            tokenUsage.setTotalTokens(response.tokenUsage().totalTokenCount());
            chatResponse.setTokenUsage(tokenUsage);
        }

        return chatResponse;
    }

    /**
     * 获取或生成会话ID
     */
    private String getOrGenerateSessionId(ChatRequest request) {
        return StrUtil.isNotBlank(request.getSessionId())
            ? request.getSessionId()
            : memoryManager.generateSessionId();
    }

    /**
     * 把请求里的思考开关 / 预算转换为 ModelFactory 的覆盖项；都为空时返回 NONE（沿用后端配置）
     */
    private ThinkingOptions resolveThinkingOptions(ChatRequest request) {
        if (request.getThinkingEnabled() == null && request.getThinkingBudgetTokens() == null) {
            return ThinkingOptions.NONE;
        }
        return new ThinkingOptions(request.getThinkingEnabled(), request.getThinkingBudgetTokens());
    }

    /**
     * 清除会话
     */
    public void clearSession(String sessionId) {
        memoryManager.clearMemory(sessionId);
    }

    /**
     * 获取会话消息历史
     */
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return memoryManager.getMessages(sessionId);
    }
}
