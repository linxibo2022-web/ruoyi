package plus.ruoyi.common.langchain4j.websocket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI聊天错误响应
 *
 * @author zendwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatErrorResponse extends AiChatWebSocketResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 无参构造器（用于序列化）
     */
    public AiChatErrorResponse() {
        this.setType("ai_chat_error");
    }

    /**
     * 全参构造器
     */
    public AiChatErrorResponse(String sessionId, String error) {
        this();
        this.setSessionId(sessionId);
        this.error = error;
    }

    /**
     * 静态工厂方法（推荐使用）
     */
    public static AiChatErrorResponse of(String sessionId, String error) {
        return new AiChatErrorResponse(sessionId, error);
    }
}
