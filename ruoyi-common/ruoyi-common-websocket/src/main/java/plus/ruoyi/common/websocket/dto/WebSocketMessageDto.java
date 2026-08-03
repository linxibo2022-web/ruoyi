package plus.ruoyi.common.websocket.dto;

import lombok.Data;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * WebSocket 消息传输对象
 * <p>
 * 用于封装需要通过 WebSocket 发送的消息内容和目标会话信息
 * 支持多租户隔离，确保消息在租户间正确路由
 *
 * @author zendwang/抓蛙师
 */
@Data
public class WebSocketMessageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID列表
     * <p>
     * 指定消息需要推送给哪些用户，如果为空则表示群发消息
     */
    private List<Long> userIds;

    /**
     * 消息内容
     * <p>
     * 实际要发送给客户端的消息数据
     */
    private String message;

    /**
     * 租户ID
     * <p>
     * 标识消息所属的租户，用于跨实例消息分发时的租户隔离
     * 如果为null，表示使用当前租户
     */
    private String tenantId;

    /**
     * 是否全局消息
     * <p>
     * true: 发送给所有租户（需要超级管理员权限）
     * false: 只发送给指定租户（默认）
     */
    private boolean global = false;

    /**
     * 创建单用户消息对象
     * <p>
     * 快速创建向单个用户发送消息的对象
     *
     * @param userId  目标用户ID
     * @param message 消息内容
     * @return 消息传输对象
     */
    public static WebSocketMessageDto of(Long userId, String message) {
        WebSocketMessageDto dto = new WebSocketMessageDto();
        dto.setUserIds(List.of(userId));
        dto.setMessage(message);
        dto.setTenantId(TenantHelper.getTenantId());
        return dto;
    }

    /**
     * 创建多用户消息对象
     * <p>
     * 快速创建向多个用户发送消息的对象
     *
     * @param userIds 目标用户ID列表
     * @param message 消息内容
     * @return 消息传输对象
     */
    public static WebSocketMessageDto of(List<Long> userIds, String message) {
        WebSocketMessageDto dto = new WebSocketMessageDto();
        dto.setUserIds(userIds);
        dto.setMessage(message);
        dto.setTenantId(TenantHelper.getTenantId());
        return dto;
    }

    /**
     * 创建租户群发消息对象
     * <p>
     * 创建向当前租户所有用户群发的消息对象
     *
     * @param message 消息内容
     * @return 消息传输对象
     */
    public static WebSocketMessageDto broadcast(String message) {
        WebSocketMessageDto dto = new WebSocketMessageDto();
        dto.setMessage(message);
        dto.setTenantId(TenantHelper.getTenantId());
        dto.setGlobal(false);
        // sessionKeys为null表示群发
        return dto;
    }

    /**
     * 创建全局群发消息对象
     * <p>
     * 创建向所有租户所有用户群发的消息对象（需要超级管理员权限）
     *
     * @param message 消息内容
     * @return 消息传输对象
     */
    public static WebSocketMessageDto globalBroadcast(String message) {
        WebSocketMessageDto dto = new WebSocketMessageDto();
        dto.setMessage(message);
        dto.setGlobal(true);
        // global为true时忽略tenantId
        return dto;
    }

    /**
     * 创建跨租户消息对象
     * <p>
     * 创建向指定租户的指定用户发送消息的对象（需要超级管理员权限）
     *
     * @param tenantId 目标租户ID
     * @param userIds  目标用户ID列表
     * @param message  消息内容
     * @return 消息传输对象
     */
    public static WebSocketMessageDto crossTenant(String tenantId, List<Long> userIds, String message) {
        WebSocketMessageDto dto = new WebSocketMessageDto();
        dto.setUserIds(userIds);
        dto.setMessage(message);
        dto.setTenantId(tenantId);
        dto.setGlobal(false);
        return dto;
    }

    /**
     * 判断是否为群发消息
     *
     * @return true表示群发，false表示定向发送
     */
    public boolean isBroadcast() {
        return userIds == null || userIds.isEmpty();
    }

    /**
     * 判断是否为租户内消息
     *
     * @return true表示租户内消息，false表示全局消息
     */
    public boolean isTenantScoped() {
        return !global;
    }

    /**
     * 获取消息目标描述
     * <p>
     * 用于日志记录等场景
     *
     * @return 消息目标的文字描述
     */
    public String getTargetDescription() {
        if (global) {
            return "全局广播";
        } else if (isBroadcast()) {
            return String.format("租户[%s]广播", tenantId);
        } else {
            return String.format("租户[%s]用户%s", tenantId, userIds);
        }
    }
}
