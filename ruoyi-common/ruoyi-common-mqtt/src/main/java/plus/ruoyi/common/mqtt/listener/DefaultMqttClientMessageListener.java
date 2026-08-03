package plus.ruoyi.common.mqtt.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.codec.message.MqttPublishMessage;
import org.dromara.mica.mqtt.core.client.IMqttClientGlobalMessageListener;
import org.tio.core.ChannelContext;

import java.nio.charset.StandardCharsets;

/**
 * 默认 MQTT 客户端全局消息监听器
 * <p>仅用于演示和开发调试，生产环境请自定义实现 {@link IMqttClientGlobalMessageListener}</p>
 * <p>
 * 📌 集群部署推荐：使用共享订阅（$share/GroupName/Topic）实现负载均衡
 * <ul>
 *   <li>示例：$share/backend-cluster/device/+/status</li>
 *   <li>效果：同一组内的多个实例，每条消息只被一个实例接收</li>
 *   <li>支持：EMQX、Mosquitto 2.0+、HiveMQ 等主流 Broker</li>
 * </ul>
 * </p>
 *
 * @author 抓蛙师
 * @date 2025-11-24
 */
@Slf4j
public class DefaultMqttClientMessageListener implements IMqttClientGlobalMessageListener {

    @Override
    public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
        log.info("========================================");
        log.info("MQTT 消息接收");
        log.info("Topic: {}", topic);
        log.info("QoS: {}", message.fixedHeader().qosLevel());
        log.info("Payload: {}", new String(payload, StandardCharsets.UTF_8));
        log.info("========================================");
    }
}
