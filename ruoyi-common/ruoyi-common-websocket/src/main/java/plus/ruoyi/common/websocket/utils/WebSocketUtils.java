package plus.ruoyi.common.websocket.utils;

import cn.hutool.core.collection.CollUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;
import plus.ruoyi.common.websocket.holder.WebSocketSessionHolder;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static plus.ruoyi.common.websocket.constant.WebSocketConstants.WEB_SOCKET_TOPIC;

/**
 * WebSocket 工具类（支持多连接和多租户）
 * <p>
 * 提供 WebSocket 消息发送、订阅发布等核心功能
 * 支持单点发送、批量发送、群发等多种消息分发模式
 * 支持多租户隔离，确保消息不会跨租户传递
 *
 * @author zendwang
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketUtils {

    /**
     * 向指定用户发送消息（发送到用户的所有连接）
     * <p>
     * 向当前租户内用户的所有活跃连接发送相同消息，确保多设备/多标签页都能收到
     * 自动进行租户隔离，只能发送给当前租户的用户
     * 注意：此方法只能发送给当前服务实例的用户，跨实例需要使用publishMessage
     *
     * @param userId  目标用户ID
     * @param message 消息内容
     */
    public static void sendMessage(Long userId, String message) {
        if (userId == null || StringUtils.isBlank(message)) {
            log.warn("发送消息失败 - 用户ID或消息内容为空");
            return;
        }

        String tenantId = TenantHelper.getTenantId();
        Map<String, WebSocketSession> userSessions = WebSocketSessionHolder.getUserSessions(userId);

        if (userSessions.isEmpty()) {
            log.debug("租户 {} 用户 {} 当前无活跃连接", tenantId, userId);
            return;
        }

        int successCount = 0;
        int failCount = 0;
        List<String> failedSessions = new ArrayList<>();

        // 向用户的所有连接发送消息
        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            String sessionId = entry.getKey();
            WebSocketSession session = entry.getValue();

            if (sendMessage(session, message)) {
                successCount++;
            } else {
                failCount++;
                failedSessions.add(sessionId);
            }
        }

        // 清理失败的连接
        for (String sessionId : failedSessions) {
            WebSocketSessionHolder.removeSessionById(userId, sessionId);
        }

        log.debug("租户 {} 向用户 {} 发送消息完成 - 成功: {}, 失败: {}",
            tenantId, userId, successCount, failCount);
    }

    /**
     * 向指定用户的特定连接发送消息
     * <p>
     * 精确向当前租户内用户的某个特定连接发送消息
     *
     * @param userId    目标用户ID
     * @param sessionId 目标会话ID
     * @param message   消息内容
     * @return 发送是否成功
     */
    public static boolean sendMessage(Long userId, String sessionId, String message) {
        String tenantId = TenantHelper.getTenantId();
        WebSocketSession session = WebSocketSessionHolder.getSession(userId, sessionId);

        if (session == null) {
            log.warn("发送消息失败 - 租户 {} 用户 {} 的会话 {} 不存在",
                tenantId, userId, sessionId);
            return false;
        }

        boolean success = sendMessage(session, message);
        if (!success) {
            // 发送失败时清理无效连接
            WebSocketSessionHolder.removeSessionById(userId, sessionId);
        }

        return success;
    }

    /**
     * 订阅 WebSocket 消息主题
     * <p>
     * 用于监听来自 Redis 的跨服务实例消息分发
     * 注意：订阅处理器中没有租户上下文，必须从消息中获取租户信息
     *
     * @param consumer 消息处理函数
     */
    public static void subscribeMessage(Consumer<WebSocketMessageDto> consumer) {
        RedisUtils.subscribe(WEB_SOCKET_TOPIC, WebSocketMessageDto.class, consumer);
    }

    /**
     * 发布 WebSocket 消息
     * <p>
     * 智能分发策略：
     * 1. 优先在当前服务实例内直接发送消息
     * 2. 对于不在当前实例的用户，通过 Redis 发布订阅机制分发到其他实例
     * 3. 支持多连接场景，确保用户的所有连接都能收到消息
     * 4. 自动进行租户隔离，消息只会发送给指定租户的用户
     *
     * @param webSocketMessage 待发送的消息对象
     */
    public static void publishMessage(WebSocketMessageDto webSocketMessage) {
        if (webSocketMessage == null || StringUtils.isBlank(webSocketMessage.getMessage())) {
            log.warn("发布消息失败 - 消息对象或内容为空");
            return;
        }

        List<Long> userIds = webSocketMessage.getUserIds();
        if (CollUtil.isEmpty(userIds)) {
            log.warn("发布消息失败 - 目标用户列表为空");
            return;
        }

        // 确保消息对象有租户ID（如果没有则设置当前租户）
        if (StringUtils.isBlank(webSocketMessage.getTenantId())) {
            webSocketMessage.setTenantId(TenantHelper.getTenantId());
        }

        String targetTenantId = webSocketMessage.getTenantId();
        List<Long> unsentUserIds = new ArrayList<>();

        // 处理当前服务实例内的用户连接
        for (Long userId : userIds) {
            if (WebSocketSessionHolder.isUserOnline(userId)) {
                // 用户在当前实例，直接发送到所有连接
                sendMessage(userId, webSocketMessage.getMessage());
            } else {
                // 用户不在当前实例，记录待发布的用户
                unsentUserIds.add(userId);
            }
        }

        // 通过 Redis 发布订阅处理其他服务实例的用户
        if (CollUtil.isNotEmpty(unsentUserIds)) {
            // 创建新的消息对象用于Redis发布
            WebSocketMessageDto broadcastMessage = WebSocketMessageDto.of(unsentUserIds, webSocketMessage.getMessage());
            // 保持原有的租户ID
            broadcastMessage.setTenantId(targetTenantId);

            RedisUtils.publish(WEB_SOCKET_TOPIC, broadcastMessage, consumer -> {
                log.info("WebSocket 发送租户 {} 主题订阅消息 - topic: {}, 目标用户: {}, 消息: {}",
                    targetTenantId, WEB_SOCKET_TOPIC, unsentUserIds, webSocketMessage.getMessage());
            });
        }
    }

    /**
     * 群发消息给当前租户的所有在线用户
     * <p>
     * 向当前租户的所有在线用户的所有连接发送相同消息
     * 这是默认的群发行为，确保租户隔离
     *
     * @param message 消息内容
     */
    public static void publishAll(String message) {
        if (StringUtils.isBlank(message)) {
            log.warn("群发消息失败 - 消息内容为空");
            return;
        }

        // 使用 WebSocketMessageDto 的静态方法创建群发消息
        WebSocketMessageDto broadcastMessage = WebSocketMessageDto.broadcast(message);

        RedisUtils.publish(WEB_SOCKET_TOPIC, broadcastMessage, consumer -> {
            log.info("WebSocket 发送租户 {} 群发消息 - topic: {}, 消息: {}",
                broadcastMessage.getTenantId(), WEB_SOCKET_TOPIC, message);
        });
    }

    /**
     * 群发消息给所有租户的所有在线用户（超级管理员专用）
     * <p>
     * 向系统中所有租户的所有在线用户发送消息
     * 通常用于系统维护通知等场景
     * 需要超级管理员权限
     *
     * @param message 消息内容
     */
    public static void publishGlobal(String message) {
        if (StringUtils.isBlank(message)) {
            log.warn("全局群发消息失败 - 消息内容为空");
            return;
        }

        // 权限检查
        if (!LoginHelper.isSuperAdmin()) {
            log.error("全局群发消息失败 - 无超级管理员权限，当前用户: {}",
                LoginHelper.getUserId());
            throw new RuntimeException("无权限执行全局群发操作");
        }

        // 使用 WebSocketMessageDto 的静态方法创建全局消息
        WebSocketMessageDto globalMessage = WebSocketMessageDto.globalBroadcast(message);

        RedisUtils.publish(WEB_SOCKET_TOPIC, globalMessage, consumer -> {
            log.info("WebSocket 发送全局群发消息 - topic: {}, 消息: {}",
                WEB_SOCKET_TOPIC, message);
        });
    }

    /**
     * 跨租户发送消息（超级管理员专用）
     * <p>
     * 向指定租户的指定用户发送消息
     * 需要超级管理员权限
     *
     * @param tenantId 目标租户ID
     * @param userIds  目标用户ID列表
     * @param message  消息内容
     */
    public static void publishCrossTenant(String tenantId, List<Long> userIds, String message) {
        if (StringUtils.isBlank(tenantId) || CollUtil.isEmpty(userIds) || StringUtils.isBlank(message)) {
            log.warn("跨租户发送消息失败 - 参数不完整");
            return;
        }

        // 权限检查
        if (!LoginHelper.isSuperAdmin()) {
            log.error("跨租户发送消息失败 - 无超级管理员权限，当前用户: {}",
                LoginHelper.getUserId());
            throw new RuntimeException("无权限执行跨租户发送操作");
        }

        // 使用 WebSocketMessageDto 的静态方法创建跨租户消息
        WebSocketMessageDto crossTenantMessage = WebSocketMessageDto.crossTenant(tenantId, userIds, message);

        RedisUtils.publish(WEB_SOCKET_TOPIC, crossTenantMessage, consumer -> {
            log.info("WebSocket 跨租户发送消息 - 目标租户: {}, 目标用户: {}, 消息: {}",
                tenantId, userIds, message);
        });
    }

    /**
     * 强制断开用户的所有连接
     * <p>
     * 通常在用户登出或账号异常时使用
     * 只能断开当前租户的用户连接
     *
     * @param userId 用户ID
     */
    public static void disconnectUser(Long userId) {
        if (userId == null) {
            return;
        }

        String tenantId = TenantHelper.getTenantId();
        WebSocketSessionHolder.removeAllSessions(userId);
        log.info("强制断开租户 {} 用户 {} 的所有WebSocket连接", tenantId, userId);
    }

    /**
     * 发送心跳响应消息
     *
     * @param session WebSocket 会话
     */
    public static void sendPongMessage(WebSocketSession session) {
        sendMessage(session, new PongMessage());
    }

    /**
     * 发送文本消息到指定会话
     * <p>
     * 对外提供的便捷方法，内部调用通用发送方法
     *
     * @param session WebSocket 会话
     * @param message 文本消息内容
     * @return 发送是否成功
     */
    public static boolean sendMessage(WebSocketSession session, String message) {
        return sendMessage(session, new TextMessage(message));
    }

    /**
     * 发送 WebSocket 消息（底层实现）
     * <p>
     * 底层消息发送方法，处理消息发送的具体逻辑和异常情况
     * 支持线程安全的消息发送
     *
     * @param session WebSocket 会话
     * @param message WebSocket 消息对象
     * @return 发送是否成功
     */
    private static synchronized boolean sendMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (session == null) {
            log.warn("发送消息失败 - WebSocket 会话为空");
            return false;
        }

        if (!session.isOpen()) {
            log.warn("发送消息失败 - WebSocket 会话已关闭, sessionId: {}", session.getId());
            return false;
        }

        try {
            session.sendMessage(message);
            return true;
        } catch (IOException e) {
            log.error("WebSocket 消息发送失败 - sessionId: {}, 消息: {}, 异常: {}",
                session.getId(), message.getPayload(), e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前租户的WebSocket连接统计信息
     *
     * @return 当前租户的连接统计信息
     */
    public static WebSocketSessionHolder.ConnectionStats getConnectionStats() {
        return WebSocketSessionHolder.getConnectionStats();
    }

    /**
     * 获取全局WebSocket连接统计信息（超级管理员专用）
     *
     * @return 全局连接统计信息
     */
    public static WebSocketSessionHolder.ConnectionStats getGlobalConnectionStats() {
        // 权限检查
        if (!LoginHelper.isSuperAdmin()) {
            log.warn("获取全局统计失败 - 无超级管理员权限，当前用户: {}",
                LoginHelper.getUserId());
            // 返回当前租户的统计信息
            return getConnectionStats();
        }

        return WebSocketSessionHolder.getGlobalConnectionStats();
    }
}
