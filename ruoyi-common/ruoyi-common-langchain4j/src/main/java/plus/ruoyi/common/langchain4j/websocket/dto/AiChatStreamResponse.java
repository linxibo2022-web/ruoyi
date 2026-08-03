package plus.ruoyi.common.langchain4j.websocket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI聊天流式内容响应
 * <p>
 * 通过 phase 区分两种增量：
 * <ul>
 *   <li>thinking - 推理 / 思考阶段（reasoningContent 字段有值）</li>
 *   <li>content - 最终回复阶段（content 字段有值）</li>
 * </ul>
 *
 * @author zendwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatStreamResponse extends AiChatWebSocketResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 内容片段（content 阶段使用）
     */
    private String content;

    /**
     * 推理内容片段（thinking 阶段使用）
     */
    private String reasoningContent;

    /**
     * 阶段：thinking | content
     */
    private String phase;

    /**
     * 是否完成
     */
    private Boolean finished = false;

    /**
     * 无参构造器（用于序列化）
     */
    public AiChatStreamResponse() {
        this.setType("ai_chat_stream");
    }

    /**
     * content 阶段构造器
     */
    public AiChatStreamResponse(String sessionId, String messageId, String content) {
        this();
        this.setSessionId(sessionId);
        this.messageId = messageId;
        this.content = content;
        this.phase = "content";
    }

    /**
     * 静态工厂：content 阶段（最终回复增量）
     */
    public static AiChatStreamResponse of(String sessionId, String messageId, String content) {
        return new AiChatStreamResponse(sessionId, messageId, content);
    }

    /**
     * 静态工厂：thinking 阶段（推理增量）
     * <p>
     * 与 of(...) 区分开：thinking 阶段填 reasoningContent 字段而非 content
     */
    public static AiChatStreamResponse ofThinking(String sessionId, String messageId, String reasoningContent) {
        AiChatStreamResponse response = new AiChatStreamResponse();
        response.setSessionId(sessionId);
        response.messageId = messageId;
        response.reasoningContent = reasoningContent;
        response.phase = "thinking";
        return response;
    }
}
