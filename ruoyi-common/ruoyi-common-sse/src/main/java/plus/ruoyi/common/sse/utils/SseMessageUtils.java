package plus.ruoyi.common.sse.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.sse.core.SseEmitterManager;
import plus.ruoyi.common.sse.dto.SseMessageDto;

/**
 * SSE消息工具类
 * <p>
 * 提供静态方法用于发送SSE消息，内部检查SSE功能是否开启
 * 简化业务代码中的SSE消息发送操作
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessageUtils {

    /**
     * SSE功能开关，从配置文件读取
     */
    private final static Boolean SSE_ENABLE = SpringUtils.getProperty("sse.enabled", Boolean.class, true);

    /**
     * SSE连接管理器实例
     */
    private static SseEmitterManager MANAGER;

    static {
        // 只有在SSE功能开启时才初始化管理器
        if (isEnable() && MANAGER == null) {
            MANAGER = SpringUtils.getBean(SseEmitterManager.class);
        }
    }

    /**
     * 向指定用户发送消息（本地）
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    public static void sendMessage(Long userId, String message) {
        if (!isEnable()) {
            return;
        }
        MANAGER.sendMessage(userId, message);
    }

    /**
     * 向本机所有用户发送消息
     *
     * @param message 消息内容
     */
    public static void sendMessage(String message) {
        if (!isEnable()) {
            return;
        }
        MANAGER.sendMessage(message);
    }

    /**
     * 发布SSE订阅消息（集群）
     * <p>
     * 通过Redis发布订阅实现跨节点消息推送
     *
     * @param sseMessageDto SSE消息对象
     */
    public static void publishMessage(SseMessageDto sseMessageDto) {
        if (!isEnable()) {
            return;
        }
        MANAGER.publishMessage(sseMessageDto);
    }

    /**
     * 向所有用户发布广播消息（集群）
     *
     * @param message 消息内容
     */
    public static void publishAll(String message) {
        if (!isEnable()) {
            return;
        }
        MANAGER.publishAll(message);
    }

    /**
     * 检查SSE功能是否开启
     *
     * @return true：已开启，false：未开启
     */
    public static Boolean isEnable() {
        return SSE_ENABLE;
    }
}
