package plus.ruoyi.common.langchain4j.websocket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.langchain4j.domain.dto.DocumentReference;
import plus.ruoyi.common.langchain4j.domain.dto.TokenUsage;

import java.io.Serial;
import java.util.List;

/**
 * AI聊天完成响应
 *
 * @author zendwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatCompleteResponse extends AiChatWebSocketResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 完整内容（最终回复）
     */
    private String content;

    /**
     * 完整推理内容（推理模型 + enableThinking=true 时返回）
     */
    private String reasoningContent;

    /**
     * 是否完成
     */
    private Boolean finished = true;

    /**
     * Token使用情况
     */
    private TokenUsageInfo tokenUsage;

    /**
     * 引用的文档(RAG模式)
     */
    private List<DocumentReference> references;

    /**
     * 无参构造器（用于序列化）
     */
    public AiChatCompleteResponse() {
        this.setType("ai_chat_complete");
    }

    /**
     * 全参构造器
     */
    public AiChatCompleteResponse(String sessionId, String messageId, String content) {
        this();
        this.setSessionId(sessionId);
        this.messageId = messageId;
        this.content = content;
    }

    /**
     * 静态工厂方法（推荐使用）
     */
    public static AiChatCompleteResponse of(String sessionId, String messageId, String content) {
        return new AiChatCompleteResponse(sessionId, messageId, content);
    }

    /**
     * 设置Token使用情况
     *
     * @param tokenUsage Token使用对象
     * @return 当前对象(支持链式调用)
     */
    public AiChatCompleteResponse withTokenUsage(TokenUsage tokenUsage) {
        if (tokenUsage != null) {
            TokenUsageInfo info = new TokenUsageInfo();
            info.setPromptTokens(tokenUsage.getPromptTokens());
            info.setCompletionTokens(tokenUsage.getCompletionTokens());
            info.setTotalTokens(tokenUsage.getTotalTokens());
            this.tokenUsage = info;
        }
        return this;
    }

    /**
     * 设置引用文档
     *
     * @param references 引用文档列表
     * @return 当前对象(支持链式调用)
     */
    public AiChatCompleteResponse withReferences(List<DocumentReference> references) {
        this.references = references;
        return this;
    }

    /**
     * 设置完整推理内容
     *
     * @param reasoningContent 推理内容
     * @return 当前对象(支持链式调用)
     */
    public AiChatCompleteResponse withReasoningContent(String reasoningContent) {
        this.reasoningContent = reasoningContent;
        return this;
    }

    /**
     * Token使用信息
     */
    @Data
    public static class TokenUsageInfo implements java.io.Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
