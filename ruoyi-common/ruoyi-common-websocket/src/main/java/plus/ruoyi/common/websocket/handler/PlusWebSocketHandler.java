package plus.ruoyi.common.websocket.handler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.websocket.holder.WebSocketSessionHolder;
import plus.ruoyi.common.websocket.processor.MessageProcessor;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.List;

import static plus.ruoyi.common.websocket.constant.WebSocketConstants.LOGIN_USER;
import static plus.ruoyi.common.websocket.constant.WebSocketConstants.PING;
import static plus.ruoyi.common.websocket.constant.WebSocketConstants.PONG;

/**
 * WebSocket处理器实现类(支持多连接)
 * <p>
 * 职责:
 * 1. 管理WebSocket连接的生命周期
 * 2. 将接收到的消息路由到对应的消息处理器
 * 3. 支持同一用户建立多个连接,避免连接互相挤号
 * <p>
 * 重构说明:
 * - 使用策略模式,通过MessageProcessor接口将消息处理逻辑解耦
 * - Handler只负责消息路由,不包含具体业务逻辑
 * - 便于扩展新的消息类型,只需新增MessageProcessor实现类即可
 *
 * @author zendwang/抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class PlusWebSocketHandler extends AbstractWebSocketHandler {

    /**
     * 消息处理器列表
     * <p>
     * Spring会自动注入所有MessageProcessor的实现类
     * 处理消息时会按顺序查找支持该消息类型的处理器
     */
    private final List<MessageProcessor> messageProcessors;

    /**
     * 连接建立成功后的处理
     * <p>
     * 验证用户身份并将连接加入管理器
     * 使用会话ID作为连接标识,支持同一用户多个连接
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // 从会话属性中获取登录用户信息（在握手拦截器中设置）
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER);

        // 验证用户信息有效性
        if (ObjectUtil.isNull(loginUser)) {
            log.warn("WebSocket连接建立失败 - 无效的用户信息, sessionId: {}", session.getId());
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 将连接添加到会话管理器，使用sessionId作为唯一标识
        WebSocketSessionHolder.addSession(loginUser.getUserId(), session.getId(), session);

        // 记录连接建立日志
        log.info("WebSocket连接建立成功 - sessionId: {}, userId: {}, userType: {}",
            session.getId(), loginUser.getUserId(), loginUser.getUserType());

        // 获取连接统计信息
        WebSocketSessionHolder.ConnectionStats stats = WebSocketSessionHolder.getConnectionStats();
        log.debug("当前连接统计: {}", stats);
    }

    /**
     * 处理接收到的文本消息
     * <p>
     * 核心路由逻辑:
     * 1. 快速处理简单字符串心跳(ping)
     * 2. 尝试解析JSON格式消息,根据type字段路由到对应处理器
     * 3. 非JSON消息使用默认业务消息处理器
     *
     * @param session WebSocket会话
     * @param message 接收到的文本消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从WebSocket会话中获取登录用户信息
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER);
        String payload = message.getPayload();

        log.debug("收到来自用户 {} 的消息: {}", loginUser.getUserId(), payload);

        // 快速处理简单字符串心跳
        if (PING.equals(payload)) {
            WebSocketUtils.sendMessage(session, PONG);
            log.debug("响应用户 {} 的心跳检测", loginUser.getUserId());
            return;
        }

        // 尝试解析为JSON并路由到对应处理器
        if (tryRouteJsonMessage(session, loginUser, payload)) {
            return;
        }

        // 非JSON消息,使用默认业务处理器(向后兼容)
        routeToBusinessProcessor(session, loginUser, payload);
    }

    /**
     * 尝试将消息解析为JSON并路由到对应处理器
     *
     * @return true表示成功路由,false表示不是JSON或没有找到处理器
     */
    private boolean tryRouteJsonMessage(WebSocketSession session, LoginUser loginUser, String payload) {
        try {
            // 简单判断是否为JSON格式,避免不必要的解析开销
            if (!payload.trim().startsWith("{")) {
                return false;
            }

            JSONObject json = JSONUtil.parseObj(payload);
            String type = json.getStr("type");

            // 如果没有type字段,不是标准消息格式
            if (type == null || type.trim().isEmpty()) {
                return false;
            }

            // 查找支持该类型的处理器
            for (MessageProcessor processor : messageProcessors) {
                if (processor.support(type)) {
                    processor.process(session, loginUser, payload);
                    return true;
                }
            }

            log.warn("未找到支持消息类型 {} 的处理器", type);
            return false;

        } catch (Exception e) {
            // 解析失败,不是有效的JSON
            log.debug("消息非JSON格式,将使用默认业务处理器");
            return false;
        }
    }

    /**
     * 路由到业务消息处理器(兜底处理)
     */
    private void routeToBusinessProcessor(WebSocketSession session, LoginUser loginUser, String payload) {
        // 查找业务消息处理器(support返回true的那个)
        for (MessageProcessor processor : messageProcessors) {
            // 业务处理器通常是最后一个,support返回true作为兜底
            if (processor.getClass().getSimpleName().contains("Business")) {
                processor.process(session, loginUser, payload);
                return;
            }
        }

        log.warn("未找到业务消息处理器,消息被丢弃");
    }

    /**
     * 处理接收到的二进制消息
     * <p>
     * 当前实现为默认处理，可根据业务需要扩展
     *
     * @param session WebSocket会话
     * @param message 接收到的二进制消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER);
        log.debug("收到来自用户 {} 的二进制消息,长度: {}",
            loginUser.getUserId(), message.getPayload().remaining());

        // 可根据实际需求处理二进制消息
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的Pong消息（心跳响应）
     * <p>
     * 客户端对服务器心跳检测的响应处理
     * 收到Pong消息表示连接正常，只需记录活跃时间，无需回复
     *
     * @param session WebSocket会话
     * @param message 接收到的Pong消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER);
        log.debug("收到来自用户 {} 的Pong响应,连接正常", loginUser.getUserId());
    }

    /**
     * 处理WebSocket传输错误
     * <p>
     * 记录错误信息，但不主动关闭连接，让连接关闭回调处理清理工作
     *
     * @param session   WebSocket会话
     * @param exception 发生的异常
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER);
        Long userId = loginUser != null ? loginUser.getUserId() : null;

        log.error("WebSocket传输错误 - sessionId: {}, userId: {}, 错误信息: {}",
            session.getId(), userId, exception.getMessage());
    }

    /**
     * 在WebSocket连接关闭后执行清理操作
     * <p>
     * 从会话管理器中移除对应的连接，支持精确清理单个连接
     *
     * @param session WebSocket会话
     * @param status  关闭状态信息
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER);

        if (ObjectUtil.isNull(loginUser)) {
            log.warn("WebSocket连接关闭 - 无效的用户信息, sessionId: {}", session.getId());
            return;
        }

        // 从会话管理器中精确移除此连接
        WebSocketSessionHolder.removeSessionById(loginUser.getUserId(), session.getId());

        // 记录连接关闭日志
        log.info("WebSocket连接关闭 - sessionId: {}, userId: {}, userType: {}, 关闭原因: {}",
            session.getId(), loginUser.getUserId(), loginUser.getUserType(), status.toString());

        // 获取连接统计信息
        WebSocketSessionHolder.ConnectionStats stats = WebSocketSessionHolder.getConnectionStats();
        log.debug("当前连接统计: {}", stats);
    }

    /**
     * 指示处理程序是否支持接收部分消息
     * <p>
     * 当前不支持部分消息处理，要求消息必须完整接收
     *
     * @return 如果支持接收部分消息，则返回true；否则返回false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
