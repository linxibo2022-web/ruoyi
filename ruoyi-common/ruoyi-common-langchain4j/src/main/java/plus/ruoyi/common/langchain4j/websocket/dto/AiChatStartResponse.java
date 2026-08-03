package plus.ruoyi.common.langchain4j.websocket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI聊天开始响应
 *
 * @author zendwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatStartResponse extends AiChatWebSocketResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 无参构造器（用于序列化）
     */
    public AiChatStartResponse() {
        this.setType("ai_chat_start");
    }

    /**
     * 全参构造器
     */
    public AiChatStartResponse(String sessionId, String messageId) {
        this();
        this.setSessionId(sessionId);
        this.messageId = messageId;
    }

    /**
     * 静态工厂方法（推荐使用）
     */
    public static AiChatStartResponse of(String sessionId, String messageId) {
        return new AiChatStartResponse(sessionId, messageId);
    }
}
