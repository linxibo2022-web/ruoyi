package plus.ruoyi.common.websocket.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;
import plus.ruoyi.common.core.constant.I18nKeys;
import plus.ruoyi.common.core.constant.TenantConstants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;
import plus.ruoyi.common.websocket.holder.WebSocketSessionHolder;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket 管理控制器
 * <p>
 * 提供WebSocket连接管理、消息发送、统计查询等管理功能
 * 支持多租户隔离，租户管理员只能管理本租户的连接
 * 超级管理员可以查看和管理所有租户的连接
 *
 * @author 抓蛙师
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/webSocket")
@ConditionalOnProperty(value = "websocket.enabled", havingValue = "true")
public class WebSocketAdminController {

    /**
     * 获取WebSocket连接统计信息
     * <p>
     * 租户管理员：返回当前租户的统计信息
     * 超级管理员：返回全局统计信息
     *
     * @return 连接统计信息
     */
    @GetMapping("/getStats")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<WebSocketSessionHolder.ConnectionStats> getStats() {
        WebSocketSessionHolder.ConnectionStats stats;

        if (LoginHelper.isSuperAdmin()) {
            // 超级管理员查看全局统计
            stats = WebSocketUtils.getGlobalConnectionStats();
        } else {
            // 租户管理员查看本租户统计
            stats = WebSocketUtils.getConnectionStats();
        }

        return R.ok(stats);
    }

    /**
     * 获取在线用户ID列表
     * <p>
     * 租户管理员：返回当前租户的在线用户
     * 超级管理员：可选择查看全局或特定租户的在线用户
     *
     * @param global 是否查看全局（仅超级管理员有效）
     * @return 在线用户ID集合
     */
    @GetMapping("/getOnlineUsers")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Set<Long>> getOnlineUsers(@RequestParam(required = false, defaultValue = "false") boolean global) {
        Set<Long> onlineUsers;

        if (global && LoginHelper.isSuperAdmin()) {
            // 超级管理员查看全局在线用户
            onlineUsers = WebSocketSessionHolder.getGlobalAllUserIds();
        } else {
            // 查看当前租户在线用户
            onlineUsers = WebSocketSessionHolder.getAllUserIds();
        }

        return R.ok(onlineUsers);
    }

    /**
     * 获取指定用户的所有连接会话ID
     * <p>
     * 自动在当前租户范围内查询
     *
     * @param userId 用户ID
     * @return 用户的所有会话ID集合
     */
    @GetMapping("/getUserSessions/{userId}")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Set<String>> getUserSessions(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long userId) {
        Map<String, org.springframework.web.socket.WebSocketSession> sessions =
            WebSocketSessionHolder.getUserSessions(userId);
        return R.ok(sessions.keySet());
    }

    /**
     * 检查用户是否在线
     * <p>
     * 自动在当前租户范围内检查
     *
     * @param userId 用户ID
     * @return 用户在线状态
     */
    @GetMapping("/checkUserOnline/{userId}")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Boolean> checkUserOnline(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long userId) {
        boolean online = WebSocketSessionHolder.isUserOnline(userId);
        return R.ok(online);
    }

    /**
     * 获取用户连接详情
     * <p>
     * 自动在当前租户范围内查询
     *
     * @param userId 用户ID
     * @return 用户连接详情
     */
    @GetMapping("/getUserDetails/{userId}")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<UserConnectionDetails> getUserDetails(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long userId) {
        Map<String, org.springframework.web.socket.WebSocketSession> sessions =
            WebSocketSessionHolder.getUserSessions(userId);

        UserConnectionDetails details = new UserConnectionDetails();
        details.setUserId(userId);
        details.setTenantId(TenantHelper.getTenantId());
        details.setOnline(WebSocketSessionHolder.isUserOnline(userId));
        details.setConnectionCount(sessions.size());
        details.setSessionIds(sessions.keySet());

        return R.ok(details);
    }

    /**
     * 向指定用户发送消息
     * <p>
     * 向当前租户内的用户发送消息
     *
     * @param request 发送消息请求对象
     * @return 操作结果
     */
    @PostMapping("/sendUserMessage")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> sendUserMessage(@RequestBody SendUserMessageRequest request) {
        WebSocketUtils.publishMessage(WebSocketMessageDto.of(request.getUserId(),request.getMessage()));
        return R.ok();
    }

    /**
     * 向指定用户的特定连接发送消息
     * <p>
     * 精确向当前租户内用户的某个特定WebSocket连接发送消息
     *
     * @param request 发送会话消息请求对象
     * @return 操作结果
     */
    @PostMapping("/sendSessionMessage")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> sendSessionMessage(@RequestBody SendSessionMessageRequest request) {
        boolean success = WebSocketUtils.sendMessage(request.getUserId(), request.getSessionId(), request.getMessage());
        if (success) {
            return R.ok();
        } else {
            return R.fail("消息发送失败，会话可能已断开");
        }
    }

    /**
     * 向多个用户批量发送消息
     * <p>
     * 批量向当前租户内的多个用户发送相同消息
     *
     * @param request 批量发送请求对象
     * @return 操作结果
     */
    @PostMapping("/batchSendMessage")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> batchSendMessage(@RequestBody BatchSendMessageRequest request) {
        WebSocketUtils.publishMessage(WebSocketMessageDto.of(request.getUserIds(),request.getMessage()));
        return R.ok();
    }

    /**
     * 向所有在线用户群发消息
     * <p>
     * 租户管理员：群发给当前租户的所有用户
     * 超级管理员：可选择群发给所有租户
     *
     * @param request 广播消息请求对象
     * @return 操作结果
     */
    @PostMapping("/broadcastMessage")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> broadcastMessage(@RequestBody BroadcastMessageRequest request) {
        if (request.isGlobal() && LoginHelper.isSuperAdmin()) {
            // 超级管理员全局群发
            WebSocketUtils.publishGlobal(request.getMessage());
        } else {
            // 当前租户群发
            WebSocketUtils.publishAll(request.getMessage());
        }
        return R.ok();
    }

    /**
     * 跨租户发送消息（超级管理员专用）
     * <p>
     * 向指定租户的指定用户发送消息
     *
     * @param request 跨租户发送请求对象
     * @return 操作结果
     */
    @PostMapping("/crossTenantMessage")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> crossTenantMessage(@RequestBody CrossTenantMessageRequest request) {
        if (!LoginHelper.isSuperAdmin()) {
            return R.fail("无权限执行跨租户发送操作");
        }

        WebSocketUtils.publishCrossTenant(request.getTenantId(),
            request.getUserIds(), request.getMessage());
        return R.ok();
    }

    /**
     * 强制断开用户的所有连接
     * <p>
     * 强制断开当前租户内指定用户的所有WebSocket连接
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/disconnectUser/{userId}")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> disconnectUser(@NotNull(message = I18nKeys.Common.ID_REQUIRED) @PathVariable Long userId) {
        WebSocketUtils.disconnectUser(userId);
        return R.ok();
    }

    /**
     * 断开用户的特定连接
     * <p>
     * 断开当前租户内用户的某个特定WebSocket连接
     *
     * @param request 断开会话请求对象
     * @return 操作结果
     */
    @PostMapping("/disconnectSession")
    @SaCheckRole(value = {TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    public R<Void> disconnectSession(@RequestBody DisconnectSessionRequest request) {
        WebSocketSessionHolder.removeSessionById(request.getUserId(), request.getSessionId());
        return R.ok();
    }


    /**
     * 发送用户消息请求对象
     */
    @Data
    public static class SendUserMessageRequest {
        /**
         * 目标用户ID
         */
        private Long userId;

        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 发送会话消息请求对象
     */
    @Data
    public static class SendSessionMessageRequest {
        /**
         * 目标用户ID
         */
        private Long userId;

        /**
         * 目标会话ID
         */
        private String sessionId;

        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 批量发送消息请求对象
     */
    @Data
    public static class BatchSendMessageRequest {
        /**
         * 目标用户ID列表
         */
        private List<Long> userIds;

        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 广播消息请求对象
     */
    @Data
    public static class BroadcastMessageRequest {
        /**
         * 是否全局广播
         * <p>
         * true: 全局广播（需要超级管理员权限）
         * false: 租户广播（默认）
         */
        private boolean global = false;
        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 跨租户发送请求对象
     */
    @Data
    public static class CrossTenantMessageRequest {
        /**
         * 目标租户ID
         */
        private String tenantId;

        /**
         * 目标用户ID列表
         */
        private List<Long> userIds;

        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 断开会话请求对象
     */
    @Data
    public static class DisconnectSessionRequest {
        /**
         * 目标用户ID
         */
        private Long userId;

        /**
         * 目标会话ID
         */
        private String sessionId;
    }

    /**
     * 用户连接详情对象
     */
    @Data
    public static class UserConnectionDetails {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 所属租户ID
         */
        private String tenantId;

        /**
         * 是否在线
         */
        private boolean online;

        /**
         * 连接数量
         */
        private int connectionCount;

        /**
         * 会话ID集合
         */
        private Set<String> sessionIds;
    }
}
