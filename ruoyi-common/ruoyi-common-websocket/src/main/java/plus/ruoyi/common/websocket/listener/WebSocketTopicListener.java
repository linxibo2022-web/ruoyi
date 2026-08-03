package plus.ruoyi.common.websocket.listener;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.websocket.holder.WebSocketSessionHolder;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * WebSocket 主题订阅监听器（支持多连接和多租户）
 * <p>
 * 在应用启动时初始化 Redis 订阅监听，实现跨服务实例的 WebSocket 消息分发
 * 支持定向推送和群发消息两种模式，优化了多连接场景下的消息处理
 * 支持多租户隔离，确保消息不会跨租户传递
 * 重要：监听器中没有租户上下文，必须从消息中获取租户信息
 *
 * @author zendwang/抓蛙师
 */
@Slf4j
public class WebSocketTopicListener implements ApplicationRunner, Ordered {

    /**
     * 应用启动时执行初始化
     * <p>
     * 订阅 Redis 主题，监听来自其他服务实例的 WebSocket 消息分发请求
     * 支持向用户的所有连接发送消息，确保多设备场景下的消息可达性
     * 支持多租户隔离，只处理本租户的消息
     *
     * @param args 应用程序启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 订阅 WebSocket 消息主题
        WebSocketUtils.subscribeMessage((message) -> {
            try {
                // 处理全局消息（所有租户）
                if (message.isGlobal()) {
                    log.info("WebSocket 收到全局群发消息 - 消息内容: {}", message.getMessage());
                    handleGlobalMessage(message);
                    return;
                }

                // 获取消息的目标租户ID（从消息中获取，不能依赖当前上下文）
                String messageTenantId = message.getTenantId();
                if (messageTenantId == null) {
                    log.warn("收到无租户ID的消息，忽略处理 - 消息: {}", message.getMessage());
                    return;
                }

                log.info("WebSocket 租户 {} 主题订阅收到消息 - 目标用户: {}, 消息内容: {}",
                    messageTenantId, message.getUserIds(), message.getMessage());

                // 在指定租户上下文中处理消息
                TenantHelper.dynamic(messageTenantId, () -> {
                    if (CollUtil.isNotEmpty(message.getUserIds())) {
                        // 定向推送：向指定用户发送消息
                        handleTargetedMessage(message, messageTenantId);
                    } else {
                        // 群发消息：向当前租户的所有在线用户发送消息
                        handleBroadcastMessage(message, messageTenantId);
                    }
                });

            } catch (Exception e) {
                log.error("处理WebSocket消息异常: {}", e.getMessage(), e);
            }
        });

        log.info("初始化 WebSocket 主题订阅监听器成功");

        // 输出当前连接统计（使用默认租户上下文）
        WebSocketSessionHolder.ConnectionStats stats = WebSocketSessionHolder.getConnectionStats();
        log.info("WebSocket服务启动完成 - 当前租户 {}", stats);
    }

    /**
     * 处理全局消息
     */
    private void handleGlobalMessage(plus.ruoyi.common.websocket.dto.WebSocketMessageDto message) {
        // 获取所有租户的所有在线用户
        var allUserIds = WebSocketSessionHolder.getGlobalAllUserIds();
        int totalUsers = allUserIds.size();
        int totalConnections = 0;
        int successCount = 0;

        // 需要遍历所有租户来发送消息
        for (String tenantId : WebSocketSessionHolder.getAllTenantIds()) {
            try {
                // 在每个租户上下文中发送消息
                TenantHelper.dynamic(tenantId, () -> {
                    var tenantUserIds = WebSocketSessionHolder.getAllUserIds();
                    for (Long userId : tenantUserIds) {
                        try {
                            WebSocketUtils.sendMessage(userId, message.getMessage());
                        } catch (Exception e) {
                            log.error("发送全局消息给租户 {} 用户 {} 失败: {}", tenantId, userId, e.getMessage());
                        }
                    }
                });
                successCount++;
            } catch (Exception e) {
                log.error("处理租户 {} 的全局消息失败: {}", tenantId, e.getMessage());
            }
        }

        log.info("全局消息发送完成 - 目标租户数: {}, 成功处理: {}, 在线用户: {}",
            WebSocketSessionHolder.getAllTenantIds().size(), successCount, totalUsers);
    }

    /**
     * 处理定向消息
     */
    private void handleTargetedMessage(plus.ruoyi.common.websocket.dto.WebSocketMessageDto message, String tenantId) {
        int totalTargetUsers = message.getUserIds().size();
        int actualSentUsers = 0;
        int totalConnections = 0;

        for (Long userId : message.getUserIds()) {
            if (WebSocketSessionHolder.isUserOnline(userId)) {
                // 用户在当前服务实例，向其所有连接发送消息
                int userConnections = WebSocketSessionHolder.getUserSessions(userId).size();
                WebSocketUtils.sendMessage(userId, message.getMessage());

                actualSentUsers++;
                totalConnections += userConnections;

                log.debug("租户 {} 向用户 {} 的 {} 个连接发送消息成功",
                    tenantId, userId, userConnections);
            } else {
                log.debug("租户 {} 用户 {} 不在当前服务实例", tenantId, userId);
            }
        }

        log.info("租户 {} 定向消息发送完成 - 目标用户: {}, 实际发送: {}, 总连接数: {}",
            tenantId, totalTargetUsers, actualSentUsers, totalConnections);
    }

    /**
     * 处理广播消息
     */
    private void handleBroadcastMessage(plus.ruoyi.common.websocket.dto.WebSocketMessageDto message, String tenantId) {
        var allUserIds = WebSocketSessionHolder.getAllUserIds();
        int totalUsers = allUserIds.size();
        int totalConnections = 0;

        for (Long userId : allUserIds) {
            int userConnections = WebSocketSessionHolder.getUserSessions(userId).size();
            WebSocketUtils.sendMessage(userId, message.getMessage());
            totalConnections += userConnections;
        }

        log.info("租户 {} 群发消息发送完成 - 在线用户: {}, 总连接数: {}",
            tenantId, totalUsers, totalConnections);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
