package plus.ruoyi.common.websocket.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

import static plus.ruoyi.common.websocket.constant.WebSocketConstants.LOGIN_USER;

/**
 * WebSocket 握手拦截器
 * <p>
 * 在 WebSocket 握手阶段进行用户身份认证，确保只有已登录的用户才能建立连接
 *
 * @author zendwang
 */
@Slf4j
public class PlusWebSocketInterceptor implements HandshakeInterceptor {

    /**
     * 握手前置处理
     * <p>
     * 验证用户登录状态，将用户信息存储到会话属性中
     *
     * @param request    握手请求
     * @param response   握手响应
     * @param wsHandler  WebSocket 处理器
     * @param attributes 会话属性映射，用于在握手和后续处理中传递数据
     * @return true 允许握手继续，false 拒绝握手
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // 验证用户登录状态并获取用户信息
            LoginUser loginUser = LoginHelper.getLoginUser();
            attributes.put(LOGIN_USER, loginUser);
            return true;
        } catch (NotLoginException e) {
            log.error("WebSocket 认证失败: {}, 无法访问系统资源", e.getMessage());
            return false;
        }
    }

    /**
     * 握手后置处理
     * <p>
     * 握手成功后的回调方法，可在此进行一些后续处理
     *
     * @param request   握手请求
     * @param response  握手响应
     * @param wsHandler WebSocket 处理器
     * @param exception 握手过程中的异常（如果有）
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 可在此进行握手成功后的后续处理，如记录日志等
    }
}
