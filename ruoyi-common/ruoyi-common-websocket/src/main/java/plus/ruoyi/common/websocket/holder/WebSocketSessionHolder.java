package plus.ruoyi.common.websocket.holder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket 会话管理器（支持多连接和多租户）
 * <p>
 * 负责管理当前服务实例中所有活跃的 WebSocket 连接会话
 * 支持同一用户在不同设备/浏览器建立多个连接，避免互相挤号
 * 支持多租户隔离，确保租户间的连接和消息完全独立
 * 使用三级映射结构：tenantId -> userId -> (sessionId -> WebSocketSession)
 *
 * @author zendwang/抓蛙师
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketSessionHolder {

    /**
     * 租户会话映射表（支持多租户多连接）
     * <p>
     * 最外层Map - Key: 租户ID，Value: 该租户的用户会话映射
     * 中间层Map - Key: 用户ID，Value: 该用户的所有会话映射
     * 内层Map - Key: 会话ID，Value: 对应的 WebSocket 会话对象
     * <p>
     * 结构示例：
     * {
     * "000000": {  // 默认租户
     * 1001: {
     * "session_001": WebSocketSession1,
     * "session_002": WebSocketSession2
     * }
     * },
     * "100001": {  // 租户1
     * 2001: {
     * "session_003": WebSocketSession3
     * }
     * }
     * }
     */
    private static final Map<String, Map<Long, Map<String, WebSocketSession>>> TENANT_SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 获取所有租户ID（用于全局消息处理）
     *
     * @return 所有租户ID集合
     */
    public static Set<String> getAllTenantIds() {
        return TENANT_SESSION_MAP.keySet();
    }

    /**
     * 添加用户会话
     * <p>
     * 为指定用户添加新的WebSocket会话，支持同一用户多个连接并存
     * 自动获取当前租户ID进行隔离存储
     *
     * @param userId    用户ID
     * @param sessionId 会话唯一标识符（通常使用WebSocket Session的ID）
     * @param session   WebSocket 会话对象
     */
    public static void addSession(Long userId, String sessionId, WebSocketSession session) {
        String tenantId = TenantHelper.getTenantId();

        // 添加同步控制
        synchronized (TENANT_SESSION_MAP) {
            Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.computeIfAbsent(
                tenantId, k -> new ConcurrentHashMap<>()
            );

            Map<String, WebSocketSession> userSessions = tenantUsers.computeIfAbsent(
                userId, k -> new ConcurrentHashMap<>()
            );

            // 处理已存在的会话
            WebSocketSession existingSession = userSessions.get(sessionId);
            if (existingSession != null && existingSession.isOpen()) {
                try {
                    existingSession.close(CloseStatus.NORMAL);
                } catch (Exception e) {
                    log.warn("关闭旧会话异常: {}", e.getMessage());
                }
            }

            userSessions.put(sessionId, session);

            log.debug("租户 {} 用户 {} 添加会话 {}，当前该用户连接数: {}",
                tenantId, userId, sessionId, userSessions.size());
        }
    }

    /**
     * 移除指定用户的特定会话
     * <p>
     * 根据用户ID和会话ID精确移除单个会话，不影响用户的其他连接
     * 自动在当前租户范围内操作
     *
     * @param userId    用户ID
     * @param sessionId 会话唯一标识符
     */
    public static void removeSessionById(Long userId, String sessionId) {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            Map<String, WebSocketSession> userSessions = tenantUsers.get(userId);
            if (userSessions != null) {
                WebSocketSession session = userSessions.remove(sessionId);
                if (session != null) {
                    try {
                        // 安全关闭WebSocket连接
                        if (session.isOpen()) {
                            session.close(CloseStatus.NORMAL);
                        }
                    } catch (Exception e) {
                        log.warn("关闭会话 {} 时发生异常: {}", sessionId, e.getMessage());
                    }

                    log.debug("租户 {} 用户 {} 移除会话 {}，剩余连接数: {}",
                        tenantId, userId, sessionId, userSessions.size());
                }

                // 如果用户没有任何连接了，清理用户记录
                if (userSessions.isEmpty()) {
                    tenantUsers.remove(userId);
                    log.debug("租户 {} 用户 {} 所有连接已清理", tenantId, userId);
                }

                // 如果租户没有任何用户了，清理租户记录
                if (tenantUsers.isEmpty()) {
                    TENANT_SESSION_MAP.remove(tenantId);
                    log.debug("租户 {} 所有连接已清理", tenantId);
                }
            }
        }
    }

    /**
     * 移除用户的所有会话
     * <p>
     * 强制断开用户的所有WebSocket连接，通常在用户登出时使用
     * 自动在当前租户范围内操作
     *
     * @param userId 用户ID
     */
    public static void removeAllSessions(Long userId) {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            Map<String, WebSocketSession> userSessions = tenantUsers.remove(userId);
            if (userSessions != null && !userSessions.isEmpty()) {
                // 关闭该用户的所有连接
                for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
                    try {
                        WebSocketSession session = entry.getValue();
                        if (session.isOpen()) {
                            session.close(CloseStatus.GOING_AWAY);
                        }
                    } catch (Exception e) {
                        log.warn("关闭租户 {} 用户 {} 的会话 {} 时发生异常: {}",
                            tenantId, userId, entry.getKey(), e.getMessage());
                    }
                }
                log.info("租户 {} 用户 {} 的所有 {} 个连接已强制断开",
                    tenantId, userId, userSessions.size());
            }

            // 如果租户没有任何用户了，清理租户记录
            if (tenantUsers.isEmpty()) {
                TENANT_SESSION_MAP.remove(tenantId);
            }
        }
    }

    /**
     * 获取用户的特定会话
     * <p>
     * 自动在当前租户范围内查找
     *
     * @param userId    用户ID
     * @param sessionId 会话唯一标识符
     * @return WebSocket 会话对象，不存在时返回 null
     */
    public static WebSocketSession getSession(Long userId, String sessionId) {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            Map<String, WebSocketSession> userSessions = tenantUsers.get(userId);
            return userSessions != null ? userSessions.get(sessionId) : null;
        }
        return null;
    }

    /**
     * 获取用户的所有会话
     * <p>
     * 返回指定用户的所有活跃WebSocket连接
     * 自动在当前租户范围内查找
     *
     * @param userId 用户ID
     * @return 用户的所有会话映射，用户不存在时返回空Map
     */
    public static Map<String, WebSocketSession> getUserSessions(Long userId) {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            return tenantUsers.getOrDefault(userId, new ConcurrentHashMap<>());
        }
        return new ConcurrentHashMap<>();
    }

    /**
     * 获取当前租户的所有在线用户ID
     *
     * @return 当前租户所有在线用户的ID集合
     */
    public static Set<Long> getAllUserIds() {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            return tenantUsers.keySet();
        }
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * 获取所有租户的所有在线用户ID（超级管理员专用）
     *
     * @return 系统所有在线用户的ID集合
     */
    public static Set<Long> getGlobalAllUserIds() {
        return TENANT_SESSION_MAP.values().stream()
            .flatMap(tenantUsers -> tenantUsers.keySet().stream())
            .collect(Collectors.toSet());
    }

    /**
     * 检查用户是否在线
     * <p>
     * 只要用户有任意一个活跃连接就认为在线
     * 自动在当前租户范围内检查
     *
     * @param userId 用户ID
     * @return 用户是否存在活跃会话
     */
    public static boolean isUserOnline(Long userId) {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            Map<String, WebSocketSession> userSessions = tenantUsers.get(userId);
            return userSessions != null && !userSessions.isEmpty();
        }
        return false;
    }

    /**
     * 检查特定会话是否存在
     * <p>
     * 自动在当前租户范围内检查
     *
     * @param userId    用户ID
     * @param sessionId 会话唯一标识符
     * @return 指定会话是否存在且活跃
     */
    public static boolean isSessionExists(Long userId, String sessionId) {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            Map<String, WebSocketSession> userSessions = tenantUsers.get(userId);
            if (userSessions != null) {
                WebSocketSession session = userSessions.get(sessionId);
                return session != null && session.isOpen();
            }
        }
        return false;
    }

    /**
     * 获取当前租户的连接统计信息
     *
     * @return 当前租户的连接统计信息
     */
    public static ConnectionStats getConnectionStats() {
        String tenantId = TenantHelper.getTenantId();
        Map<Long, Map<String, WebSocketSession>> tenantUsers = TENANT_SESSION_MAP.get(tenantId);

        if (tenantUsers != null) {
            int totalUsers = tenantUsers.size();
            int totalConnections = tenantUsers.values().stream()
                .mapToInt(Map::size)
                .sum();
            return new ConnectionStats(tenantId, totalUsers, totalConnections);
        }

        return new ConnectionStats(tenantId, 0, 0);
    }

    /**
     * 获取全局连接统计信息（超级管理员专用）
     *
     * @return 所有租户的连接统计信息
     */
    public static ConnectionStats getGlobalConnectionStats() {
        int totalTenants = TENANT_SESSION_MAP.size();
        int totalUsers = TENANT_SESSION_MAP.values().stream()
            .mapToInt(Map::size)
            .sum();
        int totalConnections = TENANT_SESSION_MAP.values().stream()
            .flatMap(tenantUsers -> tenantUsers.values().stream())
            .mapToInt(Map::size)
            .sum();

        return new ConnectionStats("global", totalUsers, totalConnections, totalTenants);
    }

    /**
     * 连接统计信息内部类
     */
    public static class ConnectionStats {
        private final String tenantId;
        private final int onlineUsers;
        private final int totalConnections;
        private final Integer totalTenants;

        public ConnectionStats(String tenantId, int onlineUsers, int totalConnections) {
            this(tenantId, onlineUsers, totalConnections, null);
        }

        public ConnectionStats(String tenantId, int onlineUsers, int totalConnections, Integer totalTenants) {
            this.tenantId = tenantId;
            this.onlineUsers = onlineUsers;
            this.totalConnections = totalConnections;
            this.totalTenants = totalTenants;
        }

        public String getTenantId() {
            return tenantId;
        }

        public int getOnlineUsers() {
            return onlineUsers;
        }

        public int getTotalConnections() {
            return totalConnections;
        }

        public Integer getTotalTenants() {
            return totalTenants;
        }

        @Override
        public String toString() {
            if (totalTenants != null) {
                return String.format("全局统计 - 租户数: %d, 在线用户: %d, 总连接数: %d",
                    totalTenants, onlineUsers, totalConnections);
            }
            return String.format("租户 %s - 在线用户: %d, 总连接数: %d",
                tenantId, onlineUsers, totalConnections);
        }
    }
}
