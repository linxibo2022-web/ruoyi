package plus.ruoyi.common.message.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.message.service.MessagePushService;

import java.util.List;

/**
 * 统一消息推送自动配置
 * <p>
 * 负责自动装配 MessagePushService
 * Spring 会自动注入所有 MessageChannel 实现到 channels 参数
 * </p>
 * <p>
 * 特点：
 * - 无需显式启用，只要依赖了 ruoyi-common-message 模块即可自动装配
 * - 自动发现并注入所有 MessageChannel 实现（websocket/sse/sms/miniapp/mp）
 * - 业务模块可选择性依赖具体通道模块
 * </p>
 * <p>
 * 使用说明：
 * 1. 业务模块添加依赖：
 *    <dependency>
 *        <groupId>plus.ruoyi</groupId>
 *        <artifactId>ruoyi-common-message</artifactId>
 *    </dependency>
 *
 * 2. 根据需要添加通道依赖（可选）：
 *    <dependency>
 *        <groupId>plus.ruoyi</groupId>
 *        <artifactId>ruoyi-common-websocket</artifactId>  <!-- WebSocket 通道 -->
 *    </dependency>
 *    <dependency>
 *        <groupId>plus.ruoyi</groupId>
 *        <artifactId>ruoyi-common-sms</artifactId>        <!-- 短信通道 -->
 *    </dependency>
 *
 * 3. 注入使用：
 *    @Autowired
 *    private MessagePushService messagePushService;
 *
 *    messagePushService.send("websocket", context);
 * </p>
 *
 * @author YourName
 */
@Slf4j
@AutoConfiguration
public class MessageAutoConfiguration {

    /**
     * 注册统一消息推送服务
     * <p>
     * Spring 会自动注入容器中所有的 MessageChannel 实现
     * 当业务模块引入具体通道模块时，对应的 Channel 会自动被注入
     * </p>
     *
     * @param channels 所有 MessageChannel 实现（自动注入）
     * @return MessagePushService 实例
     */
    @Bean
    public MessagePushService messagePushService(List<MessageChannel> channels) {
        log.info("初始化统一消息推送服务, 发现 {} 个消息通道", channels.size());

        // 打印所有注册的通道
        channels.forEach(channel ->
            log.info("注册消息通道: type={}, name={}, priority={}, enabled={}",
                channel.getChannelType(),
                channel.getChannelName(),
                channel.getPriority(),
                channel.isEnabled())
        );

        return new MessagePushService(channels);
    }
}
