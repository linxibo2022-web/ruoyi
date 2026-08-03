package plus.ruoyi.common.websocket.constant;

/**
 * WebSocket 常量定义
 * <p>
 * 定义 WebSocket 功能中使用的常量值
 *
 * @author zendwang
 */
public interface WebSocketConstants {

    /**
     * WebSocket Session 中存储登录用户信息的键名
     */
    String LOGIN_USER = "loginUser";

    /**
     * Redis 发布订阅的主题名称
     * <p>
     * 用于跨服务实例的 WebSocket 消息分发
     */
    String WEB_SOCKET_TOPIC = "global:websocket";

    /**
     * 客户端心跳检测命令
     * <p>
     * 客户端发送此命令进行心跳检测
     */
    String PING = "ping";

    /**
     * 服务端心跳响应命令
     * <p>
     * 服务端响应客户端心跳检测的消息
     */
    String PONG = "pong";
}
