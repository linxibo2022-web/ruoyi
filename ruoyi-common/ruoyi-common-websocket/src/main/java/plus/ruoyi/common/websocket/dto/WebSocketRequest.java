package plus.ruoyi.common.websocket.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebSocket消息请求基类
 * <p>
 * 所有通过WebSocket发送的消息都应该包含type字段用于消息分发
 *
 * @author zendwang
 */
@Data
public class WebSocketRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     * <p>
     * 用于消息路由,如: ping, ai_chat, business 等
     */
    private String type;
}
