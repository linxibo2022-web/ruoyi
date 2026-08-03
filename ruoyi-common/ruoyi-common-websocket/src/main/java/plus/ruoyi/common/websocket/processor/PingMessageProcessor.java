package plus.ruoyi.common.websocket.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;

import static plus.ruoyi.common.websocket.constant.WebSocketConstants.PONG;

/**
 * 心跳消息处理器
 * <p>
 * 处理客户端的心跳检测请求,响应pong消息
 *
 * @author zendwang
 */
@Slf4j
@Component
@Order(1)
public class PingMessageProcessor implements MessageProcessor {

    @Override
    public boolean support(String type) {
        return "ping".equalsIgnoreCase(type);
    }

    @Override
    public void process(WebSocketSession session, LoginUser loginUser, String payload) {
        WebSocketUtils.sendMessage(session, PONG);
        log.debug("响应用户 {} 的心跳检测", loginUser.getUserId());
    }
}
