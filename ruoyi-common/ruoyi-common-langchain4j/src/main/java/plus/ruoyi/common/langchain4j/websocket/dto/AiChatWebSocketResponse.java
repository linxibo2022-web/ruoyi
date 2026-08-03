package plus.ruoyi.common.langchain4j.websocket.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI聊天WebSocket响应基类
 *
 * @author zendwang
 */
@Data
public class AiChatWebSocketResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应类型
     */
    private String type;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 时间戳
     */
    private Long timestamp;

    public AiChatWebSocketResponse() {
        this.timestamp = System.currentTimeMillis();
    }
}
