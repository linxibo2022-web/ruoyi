package plus.ruoyi.common.sse.config;

import plus.ruoyi.common.sse.channel.SseMessageChannel;
import plus.ruoyi.common.sse.controller.SseController;
import plus.ruoyi.common.sse.core.SseEmitterManager;
import plus.ruoyi.common.sse.listener.SseTopicListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * SSE（Server-Sent Events）自动配置类
 * <p>
 * 当配置项sse.enabled=true时自动装配SSE相关组件，
 * 提供服务器主动推送消息给客户端的能力
 *
 * @author Lion Li
 */
@AutoConfiguration
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    /**
     * 配置SSE连接管理器
     * <p>
     * 负责管理所有客户端的SSE连接，包括连接的创建、维护和销毁
     *
     * @return SSE连接管理器实例
     */
    @Bean
    public SseEmitterManager sseEmitterManager() {
        return new SseEmitterManager();
    }

    /**
     * 配置SSE主题监听器
     * <p>
     * 监听特定主题的消息，用于实现基于主题的消息推送功能
     *
     * @return SSE主题监听器实例
     */
    @Bean
    public SseTopicListener sseTopicListener() {
        return new SseTopicListener();
    }

    /**
     * 配置SSE控制器
     * <p>
     * 提供SSE相关的HTTP接口，处理客户端的连接请求和消息推送
     *
     * @param sseEmitterManager SSE连接管理器
     * @return SSE控制器实例
     */
    @Bean
    public SseController sseController(SseEmitterManager sseEmitterManager) {
        return new SseController(sseEmitterManager);
    }

    /**
     * 注册SSE消息通道
     * <p>
     * 实现统一消息接口，支持通过 MessagePushService 发送 SSE 消息
     * </p>
     *
     * @return SSE消息通道实例
     */
    @Bean
    public SseMessageChannel sseMessageChannel() {
        return new SseMessageChannel();
    }

}
