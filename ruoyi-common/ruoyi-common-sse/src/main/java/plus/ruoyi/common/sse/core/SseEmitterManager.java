package plus.ruoyi.common.sse.core;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.redis.utils.RedisUtils;
import plus.ruoyi.common.sse.dto.SseMessageDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE连接管理器
 * <p>
 * 负责管理所有用户的SSE连接，提供连接建立、断开、消息发送等功能
 * 支持一个用户多个连接（不同token）的场景
 *
 * @author Lion Li
 */
@Slf4j
public class SseEmitterManager {

    /**
     * Redis发布订阅的主题名称
     */
    private final static String SSE_TOPIC = "global:sse";

    /**
     * 用户连接映射表
     * 结构：userId -> (token -> SseEmitter)
     * 支持一个用户在不同设备/浏览器上建立多个连接
     */
    private final static Map<Long, Map<String, SseEmitter>> USER_TOKEN_EMITTERS = new ConcurrentHashMap<>();

    /**
     * 建立SSE连接
     *
     * @param userId 用户ID，用于标识不同用户
     * @param token  用户令牌，用于区分同一用户的多个连接
     * @return SSE连接对象，超时时间设置为无限制
     */
    public SseEmitter connect(Long userId, String token) {
        // 获取或创建用户的连接映射表
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        // 创建一个新的 SseEmitter 实例，超时时间设置为一天 避免连接之后直接关闭浏览器导致连接停滞
        SseEmitter emitter = new SseEmitter(86400000L);
        emitters.put(token, emitter);

        // 设置连接生命周期回调，确保连接结束时能正确清理资源
        emitter.onCompletion(() -> cleanupEmitter(emitters, token));
        emitter.onTimeout(() -> cleanupEmitter(emitters, token));
        emitter.onError((e) -> cleanupEmitter(emitters, token));

        try {
            // 发送连接成功的确认消息
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            // 发送失败则立即清理连接
            emitters.remove(token);
        }
        return emitter;
    }

    /**
     * 断开SSE连接
     *
     * @param userId 用户ID
     * @param token  用户令牌
     */
    public void disconnect(Long userId, String token) {
        if (userId == null || token == null) {
            return;
        }

        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (MapUtil.isNotEmpty(emitters)) {
            try {
                SseEmitter sseEmitter = emitters.get(token);
                if (sseEmitter != null) {
                    // 发送断开连接消息并完成连接
                    sseEmitter.send(SseEmitter.event().comment("disconnected"));
                    sseEmitter.complete();
                }
            } catch (Exception ignore) {
                // 忽略断开连接时的异常
            }
            emitters.remove(token);

            // 如果用户没有其他连接，清理用户记录
            if (emitters.isEmpty()) {
                USER_TOKEN_EMITTERS.remove(userId);
            }
        }
    }

    /**
     * 订阅SSE消息主题
     * <p>
     * 监听Redis发布的SSE消息，实现集群环境下的消息分发
     *
     * @param consumer 消息处理函数
     */
    public void subscribeMessage(Consumer<SseMessageDto> consumer) {
        RedisUtils.subscribe(SSE_TOPIC, SseMessageDto.class, consumer);
    }

    /**
     * 向指定用户发送消息（本地）
     * <p>
     * 直接向本机上的用户连接发送消息，不通过Redis
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    public void sendMessage(Long userId, String message) {
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (MapUtil.isNotEmpty(emitters)) {
            // 向用户的所有连接发送消息
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event()
                        .name("message")
                        .data(message));
                } catch (Exception e) {
                    // 发送失败则清理该连接
                    cleanupEmitter(emitters, entry.getKey());
                }
            }

            // 如果用户没有有效连接，清理用户记录
            if (emitters.isEmpty()) {
                USER_TOKEN_EMITTERS.remove(userId);
            }
        }
    }

    /**
     * 向本机所有用户发送消息
     *
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        for (Long userId : USER_TOKEN_EMITTERS.keySet()) {
            sendMessage(userId, message);
        }
    }

    /**
     * 发布SSE消息到Redis主题
     * <p>
     * 通过Redis发布订阅实现集群环境下的消息分发
     *
     * @param sseMessageDto SSE消息对象
     */
    public void publishMessage(SseMessageDto sseMessageDto) {
        SseMessageDto broadcastMessage = new SseMessageDto();
        broadcastMessage.setMessage(sseMessageDto.getMessage());
        broadcastMessage.setUserIds(sseMessageDto.getUserIds());

        RedisUtils.publish(SSE_TOPIC, broadcastMessage, consumer -> {
            log.info("SSE发送主题订阅消息topic:{} session keys:{} message:{}",
                SSE_TOPIC, sseMessageDto.getUserIds(), sseMessageDto.getMessage());
        });
    }

    /**
     * 向所有用户发布广播消息
     *
     * @param message 消息内容
     */
    public void publishAll(String message) {
        SseMessageDto broadcastMessage = new SseMessageDto();
        broadcastMessage.setMessage(message);

        RedisUtils.publish(SSE_TOPIC, broadcastMessage, consumer -> {
            log.info("SSE发送主题订阅消息topic:{} message:{}", SSE_TOPIC, message);
        });
    }

    /**
     * 清理连接资源的通用方法
     *
     * @param emitters 连接映射表
     * @param token    要清理的token
     */
    private void cleanupEmitter(Map<String, SseEmitter> emitters, String token) {
        SseEmitter remove = emitters.remove(token);
        if (remove != null) {
            remove.complete();
        }
    }
}
