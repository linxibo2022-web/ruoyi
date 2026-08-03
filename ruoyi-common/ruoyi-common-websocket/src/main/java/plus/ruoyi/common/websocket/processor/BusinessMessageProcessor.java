package plus.ruoyi.common.websocket.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;

/**
 * 业务消息处理器
 * <p>
 * 处理通用业务消息,支持集群环境下的消息分发
 * 这是默认的消息处理器,用于处理未明确类型的消息(向后兼容)
 *
 * @author zendwang
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE)
public class BusinessMessageProcessor implements MessageProcessor {

    @Override
    public boolean support(String type) {
        // 业务消息处理器作为兜底处理,支持所有未被其他处理器处理的消息
        // 实际使用时,通过优先级或顺序来确保最后调用
        return true;
    }

    @Override
    public void process(WebSocketSession session, LoginUser loginUser, String payload) {
        log.debug("处理业务消息 - userId: {}, message: {}", loginUser.getUserId(), payload);

        // 构建消息DTO
        WebSocketMessageDto messageDto = WebSocketMessageDto.of(loginUser.getUserId(), payload);

        // 发布消息,支持集群环境下的消息分发
        WebSocketUtils.publishMessage(messageDto);
    }
}
