package plus.ruoyi.common.langchain4j.core.chat;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.langchain4j.domain.dto.AiChatResponse;
import plus.ruoyi.common.langchain4j.domain.dto.TokenUsage;

import java.util.function.Consumer;

/**
 * 流式对话响应处理器（langchain4j 1.x StreamingChatResponseHandler）
 * <p>
 * 支持 thinking / content 两阶段流式输出：
 * <ul>
 *   <li>onPartialThinking：推送阶段为 thinking 的增量（reasoning_content）</li>
 *   <li>onPartialResponse：推送阶段为 content 的增量（最终回复）</li>
 *   <li>onCompleteResponse：推送 finished=true，附带 TokenUsage 和完整 thinking</li>
 *   <li>onError：推送 error 字段</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Slf4j
public class StreamChatHandler implements StreamingChatResponseHandler {

    private final String messageId;
    private final Consumer<AiChatResponse> responseConsumer;
    /**
     * 累积最终回复内容（content 阶段），供多轮对话保存到 memory
     */
    private final StringBuilder fullContent;
    /**
     * 累积 thinking 内容（用于完成时回填到 reasoningContent，便于前端按需展示完整推理）
     */
    private final StringBuilder fullThinking = new StringBuilder();
    private final Runnable onComplete;

    public StreamChatHandler(
            String messageId,
            Consumer<AiChatResponse> responseConsumer,
            StringBuilder fullContent) {
        this(messageId, responseConsumer, fullContent, null);
    }

    public StreamChatHandler(
            String messageId,
            Consumer<AiChatResponse> responseConsumer,
            StringBuilder fullContent,
            Runnable onComplete) {
        this.messageId = messageId;
        this.responseConsumer = responseConsumer;
        this.fullContent = fullContent;
        this.onComplete = onComplete;
    }

    /**
     * 推理 / 思考阶段增量（仅推理模型 + enableThinking=true 时触发）
     */
    @Override
    public void onPartialThinking(PartialThinking partialThinking) {
        if (partialThinking == null) {
            return;
        }
        String text = partialThinking.text();
        if (text == null || text.isEmpty()) {
            return;
        }
        fullThinking.append(text);

        AiChatResponse response = AiChatResponse.builder()
                .messageId(messageId)
                .reasoningContent(text)
                .phase(AiChatResponse.Phase.THINKING)
                .finished(false)
                .build();

        responseConsumer.accept(response);
    }

    /**
     * 最终回复增量
     */
    @Override
    public void onPartialResponse(String partialResponse) {
        if (partialResponse == null || partialResponse.isEmpty()) {
            return;
        }
        fullContent.append(partialResponse);

        AiChatResponse response = AiChatResponse.builder()
                .messageId(messageId)
                .content(partialResponse)
                .phase(AiChatResponse.Phase.CONTENT)
                .finished(false)
                .build();

        responseConsumer.accept(response);
    }

    /**
     * 流式完成：返回 TokenUsage 和完整 thinking
     */
    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        AiChatResponse finalResponse = AiChatResponse.builder()
                .messageId(messageId)
                .content("")
                .reasoningContent(fullThinking.length() > 0 ? fullThinking.toString() : null)
                .phase(AiChatResponse.Phase.CONTENT)
                .finished(true)
                .build();

        // 设置 Token 使用情况（1.x 通过 metadata 或顶层方法均可取到）
        if (completeResponse != null && completeResponse.tokenUsage() != null) {
            TokenUsage tokenUsage = new TokenUsage();
            tokenUsage.setPromptTokens(completeResponse.tokenUsage().inputTokenCount());
            tokenUsage.setCompletionTokens(completeResponse.tokenUsage().outputTokenCount());
            tokenUsage.setTotalTokens(completeResponse.tokenUsage().totalTokenCount());
            finalResponse.setTokenUsage(tokenUsage);
        }

        responseConsumer.accept(finalResponse);

        // 执行完成回调（多轮对话用于保存 AI 回复到 memory）
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public void onError(Throwable error) {
        log.error("流式错误: messageId={}, error={}", messageId, error.getMessage(), error);

        AiChatResponse errorResponse = AiChatResponse.builder()
                .messageId(messageId)
                .error(error.getMessage())
                .finished(true)
                .build();

        responseConsumer.accept(errorResponse);
    }
}
