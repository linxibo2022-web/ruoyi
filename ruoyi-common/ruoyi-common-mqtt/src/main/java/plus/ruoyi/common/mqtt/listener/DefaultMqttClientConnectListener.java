package plus.ruoyi.common.mqtt.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.core.client.IMqttClientConnectListener;
import org.tio.core.ChannelContext;

/**
 * 默认 MQTT 客户端连接监听器
 * <p>监听客户端连接、断开事件，生产环境请自定义实现 {@link IMqttClientConnectListener}</p>
 *
 * @author 抓蛙师
 * @date 2025-11-24
 */
@Slf4j
public class DefaultMqttClientConnectListener implements IMqttClientConnectListener {

    @Override
    public void onConnected(ChannelContext context, boolean isReconnect) {
        if (isReconnect) {
            log.info("MQTT 客户端重连成功: {}", context.getClientNode());
        } else {
            log.info("MQTT 客户端连接成功: {}", context.getClientNode());
        }
    }

    @Override
    public void onDisconnect(ChannelContext context, Throwable throwable, String remark, boolean isRemove) {
        if (throwable != null) {
            log.warn("MQTT 客户端断开连接: {}, 原因: {}, 异常: {}, 是否移除: {}",
                context.getClientNode(), remark, throwable.getMessage(), isRemove);
        } else {
            log.warn("MQTT 客户端断开连接: {}, 原因: {}, 是否移除: {}",
                context.getClientNode(), remark, isRemove);
        }
    }
}
