package plus.ruoyi.common.mqtt;

import plus.ruoyi.common.test.base.BaseSpringTest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * MQTT 客户端使用示例测试
 * <p>演示如何使用 MqttClientTemplate 发送和接收消息</p>
 * <p>✅ 在 MQTT 模块下,依赖明确,无需在 admin 引入</p>
 * <p>
 * ⚠️ 运行前提:
 * 1. 启动外部 MQTT Broker (如 EMQX)
 *    Docker 命令: docker run -d --name emqx -p 1883:1883 -p 18083:18083 emqx/emqx:latest
 * 2. 配置 application-dev.yml 中的 mqtt.client.enabled=true
 * 3. 配置正确的 Broker 地址: mqtt.client.ip=127.0.0.1
 * </p>
 *
 * @author 抓蛙师
 * @date 2025-11-24
 */
@Slf4j
@DisplayName("MQTT 客户端使用示例")
public class MqttClientUsageTest extends BaseSpringTest {

    @Autowired(required = false)
    private MqttClientTemplate mqttClientTemplate;

    @BeforeEach
    void checkConnection() {
        if (mqttClientTemplate == null) {
            log.warn("⚠️ MqttClientTemplate 未注入，请检查:");
            log.warn("1. application-dev.yml 中 mqtt.client.enabled=true");
            log.warn("2. 外部 MQTT Broker 是否启动 (docker run -d --name emqx -p 1883:1883 emqx/emqx:latest)");
            return;
        }

        if (!mqttClientTemplate.isConnected()) {
            log.warn("⚠️ MQTT 客户端未连接到 Broker，跳过测试");
        }
    }

    // ==================== 基础发送示例 ====================

    @Test
    @DisplayName("示例1: 发送普通消息 (默认 QoS 0)")
    void example1_PublishSimpleMessage() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/test/simple";
        String message = "Hello MQTT!";

        // 发送消息 (默认 QoS 0)
        boolean success = mqttClientTemplate.publish(topic, message.getBytes(StandardCharsets.UTF_8));

        log.info("✅ 发送消息: Topic={}, Message={}, Success={}", topic, message, success);
    }

    @Test
    @DisplayName("示例2: 发送 JSON 数据 (QoS 1)")
    void example2_PublishJsonWithQoS1() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/device/001/data";
        String jsonData = "{\"deviceId\":\"001\",\"temperature\":25.5,\"humidity\":60}";

        // 发送 JSON 消息 (QoS 1 - 至少一次)
        boolean success = mqttClientTemplate.publish(
            topic,
            jsonData.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );

        log.info("✅ 发送 JSON: Topic={}, Data={}, Success={}", topic, jsonData, success);
    }

    @Test
    @DisplayName("示例3: 发送保留消息 (Retained)")
    void example3_PublishRetainedMessage() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/device/001/config";
        String config = "{\"interval\":10,\"enabled\":true}";

        // 发送保留消息 (新订阅者会立即收到)
        boolean success = mqttClientTemplate.publish(
            topic,
            config.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1,
            true  // retained = true
        );

        log.info("✅ 发送保留消息: Topic={}, Config={}, Success={}", topic, config, success);
    }

    @Test
    @DisplayName("示例4: 批量发送消息")
    void example4_PublishBatchMessages() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        // 模拟发送多个设备状态
        for (int i = 1; i <= 5; i++) {
            String topic = "/device/" + String.format("%03d", i) + "/status";
            String status = i % 2 == 0 ? "online" : "offline";

            mqttClientTemplate.publish(
                topic,
                status.getBytes(StandardCharsets.UTF_8),
                MqttQoS.QOS1
            );

            log.info("📤 发送设备状态: 设备{}, 状态={}", String.format("%03d", i), status);
        }
    }

    // ==================== 订阅示例 ====================

    @Test
    @DisplayName("示例5: 动态订阅单个主题 (使用全局监听器)")
    void example5_SubscribeSingleTopic() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/test/subscribe";

        // 注意: subQos0/subQos1/subQos2 需要传入 IMqttClientMessageListener
        // 如果使用全局监听器,建议在配置文件中配置 global-subscribe
        log.info("💡 提示: 动态订阅需要提供监听器");
        log.info("💡 推荐方式: 在 application.yml 中配置 global-subscribe");
        log.info("   mqtt:");
        log.info("     client:");
        log.info("       global-subscribe:");
        log.info("         - topic: {}", topic);
        log.info("           qos: QOS0");
    }

    @Test
    @DisplayName("示例6: 配置文件订阅 (推荐方式)")
    void example6_SubscribeViaConfiguration() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        log.info("✅ 推荐使用配置文件订阅主题:");
        log.info("");
        log.info("mqtt:");
        log.info("  client:");
        log.info("    global-subscribe:");
        log.info("      - topic: /device/+/status  # + 单级通配符");
        log.info("        qos: QOS1");
        log.info("      - topic: /device/#          # # 多级通配符");
        log.info("        qos: QOS1");
        log.info("");
        log.info("💡 优势: 自动订阅,由全局监听器 IMqttClientGlobalMessageListener 处理");
    }

    @Test
    @DisplayName("示例7: 取消订阅")
    void example7_Unsubscribe() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/test/unsubscribe";

        // 取消订阅 (使用可变参数)
        mqttClientTemplate.unSubscribe(topic);
        log.info("✅ 取消订阅: {}", topic);

        log.info("💡 提示: unSubscribe 可以传入多个主题");
        log.info("   mqttClientTemplate.unSubscribe(\"/topic1\", \"/topic2\", \"/topic3\");");
    }

    // ==================== 综合场景示例 ====================

    @Test
    @DisplayName("示例8: 模拟设备上线通知")
    void example8_DeviceOnlineNotification() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String deviceId = "DEVICE-001";

        // 1. 发送上线通知 (使用保留消息)
        String statusTopic = "/device/" + deviceId + "/status";
        String onlineMessage = "{\"deviceId\":\"" + deviceId + "\",\"status\":\"online\",\"timestamp\":" + System.currentTimeMillis() + "}";
        mqttClientTemplate.publish(
            statusTopic,
            onlineMessage.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1,
            true  // 保留消息
        );
        log.info("✅ 设备上线: {}", onlineMessage);

        // 2. 订阅响应主题 (需要在配置文件中配置 global-subscribe)
        String responseTopic = "/device/" + deviceId + "/response";
        log.info("💡 提示: 需要在 application.yml 中配置订阅:");
        log.info("   global-subscribe:");
        log.info("     - topic: {}", responseTopic);
        log.info("       qos: QOS1");
    }

    @Test
    @DisplayName("示例9: 模拟传感器数据上报")
    void example9_SensorDataReport() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String sensorId = "SENSOR-001";
        String topic = "/sensor/" + sensorId + "/data";

        // 模拟持续上报传感器数据 (3次)
        for (int i = 1; i <= 3; i++) {
            double temperature = 20 + Math.random() * 10;  // 20-30度
            double humidity = 50 + Math.random() * 20;     // 50-70%

            String sensorData = String.format(
                "{\"sensorId\":\"%s\",\"temperature\":%.2f,\"humidity\":%.2f,\"timestamp\":%d}",
                sensorId, temperature, humidity, System.currentTimeMillis()
            );

            mqttClientTemplate.publish(
                topic,
                sensorData.getBytes(StandardCharsets.UTF_8),
                MqttQoS.QOS0  // QoS 0 适合高频传感器数据
            );

            log.info("📊 上报传感器数据 #{}: {}", i, sensorData);

            try {
                TimeUnit.MILLISECONDS.sleep(100);  // 间隔 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("示例10: 检查连接状态")
    void example10_CheckConnectionStatus() {
        if (mqttClientTemplate == null) {
            log.warn("❌ MqttClientTemplate 未注入");
            log.warn("💡 请检查:");
            log.warn("   1. application-dev.yml 中 mqtt.client.enabled=true");
            log.warn("   2. 依赖中已添加 ruoyi-common-mqtt");
            return;
        }

        boolean connected = mqttClientTemplate.isConnected();

        if (connected) {
            log.info("✅ MQTT 客户端已连接");
            log.info("💡 可以开始发送和接收消息");
        } else {
            log.warn("❌ MQTT 客户端未连接");
            log.warn("💡 解决方案:");
            log.warn("   1. 启动 EMQX: docker run -d --name emqx -p 1883:1883 emqx/emqx:latest");
            log.warn("   2. 检查配置: mqtt.client.ip=127.0.0.1");
            log.warn("   3. 检查防火墙: 确保 1883 端口开放");
        }
    }

    // ==================== 高级示例 ====================

    @Test
    @DisplayName("示例11: 发布/订阅模式 - 完整示例")
    void example11_PubSubPattern() throws InterruptedException {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/pubsub/test";

        log.info("📡 发布/订阅测试:");
        log.info("1. 先在配置文件中订阅主题:");
        log.info("   global-subscribe:");
        log.info("     - topic: {}", topic);
        log.info("       qos: QOS1");
        log.info("");

        // 等待一下确保连接稳定
        TimeUnit.MILLISECONDS.sleep(500);

        // 发送消息
        String message = "This is a pub/sub test message";
        mqttClientTemplate.publish(
            topic,
            message.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );
        log.info("2. 📤 发送消息: {}", message);

        // 等待消息接收 (由全局监听器处理)
        TimeUnit.MILLISECONDS.sleep(500);

        log.info("3. 💡 检查控制台是否有 IMqttClientGlobalMessageListener 的日志输出");
    }

    @Test
    @DisplayName("示例12: 不同 QoS 级别对比")
    void example12_QoSComparison() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String baseTopic = "/test/qos";

        // QoS 0 - 最多一次 (可能丢失)
        mqttClientTemplate.publish(
            baseTopic + "/0",
            "QoS 0 Message".getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS0
        );
        log.info("📤 QoS 0: 最多一次，可能丢失，性能最好");

        // QoS 1 - 至少一次 (可能重复)
        mqttClientTemplate.publish(
            baseTopic + "/1",
            "QoS 1 Message".getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );
        log.info("📤 QoS 1: 至少一次，可能重复，推荐使用");

        // QoS 2 - 恰好一次 (性能较低)
        mqttClientTemplate.publish(
            baseTopic + "/2",
            "QoS 2 Message".getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS2
        );
        log.info("📤 QoS 2: 恰好一次，无重复，性能最低");

        log.info("💡 建议: 一般业务使用 QoS 1，传感器数据使用 QoS 0，金融交易使用 QoS 2");
    }
}
