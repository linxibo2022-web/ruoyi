package plus.ruoyi.common.websocket.processor;

import org.springframework.web.socket.WebSocketSession;
import plus.ruoyi.common.core.domain.model.LoginUser;

/**
 * WebSocket消息处理器接口
 * <p>
 * 定义消息处理器的统一规范,所有具体的消息处理器都需要实现此接口
 * 使用策略模式,便于扩展新的消息类型
 *
 * @author zendwang
 */
public interface MessageProcessor {

    /**
     * 判断是否支持处理该类型的消息
     *
     * @param type 消息类型
     * @return true表示支持,false表示不支持
     */
    boolean support(String type);

    /**
     * 处理消息
     *
     * @param session   WebSocket会话
     * @param loginUser 登录用户信息
     * @param payload   消息内容(JSON字符串)
     */
    void process(WebSocketSession session, LoginUser loginUser, String payload);
}
