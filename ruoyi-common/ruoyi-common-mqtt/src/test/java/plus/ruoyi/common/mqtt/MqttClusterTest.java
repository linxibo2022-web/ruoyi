package plus.ruoyi.common.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import plus.ruoyi.common.test.base.BaseSpringTest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * MQTT 集群部署测试
 * <p>测试共享订阅功能</p>
 * <p>
 * ⚠️ 运行前提:
 * <ol>
 *   <li>启动 MQTT Broker（EMQX 或 Mosquitto 2.0+，支持共享订阅）</li>
 *   <li>配置 mqtt.client.enabled=true</li>
 *   <li>配置共享订阅主题：$share/backend-cluster/device/+/status</li>
 * </ol>
 * </p>
 *
 * @author 抓蛙师
 * @date 2025-12-07
 */
@Slf4j
@DisplayName("MQTT 集群部署测试")
public class MqttClusterTest extends BaseSpringTest {

    @Autowired(required = false)
    private MqttClientTemplate mqttClientTemplate;

    @BeforeEach
    void checkConnection() {
        if (mqttClientTemplate == null) {
            log.warn("⚠️ MqttClientTemplate 未注入，跳过测试");
            log.warn("请检查: mqtt.client.enabled=true");
            return;
        }

        if (!mqttClientTemplate.isConnected()) {
            log.warn("⚠️ MQTT 客户端未连接，跳过测试");
        }
    }

    // ==================== 共享订阅测试 ====================

    @Test
    @DisplayName("测试1: 发送消息到共享订阅主题")
    void test1_PublishToSharedTopic() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "device/001/status";  // 实际主题
        String message = "{\"deviceId\":\"001\",\"status\":\"online\"}";

        log.info("========================================");
        log.info("共享订阅测试");
        log.info("========================================");
        log.info("📌 测试说明:");
        log.info("1. 确保配置文件中已启用共享订阅:");
        log.info("   global-subscribe:");
        log.info("     - topic: $share/backend-cluster/device/+/status");
        log.info("       qos: QOS1");
        log.info("");
        log.info("2. 启动多个节点（建议 2-3 个）");
        log.info("");
        log.info("3. 运行此测试发送消息");
        log.info("");
        log.info("4. 预期结果:");
        log.info("   ✅ 只有一个节点打印处理日志");
        log.info("   ✅ 其他节点不会收到消息");
        log.info("");
        log.info("========================================");

        // 发送消息
        boolean success = mqttClientTemplate.publish(
            topic,
            message.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );

        log.info("📤 发送消息: Topic={}, Success={}", topic, success);
        log.info("💡 请检查各节点日志，确认只有一个节点处理该消息");
        log.info("========================================");
    }

    @Test
    @DisplayName("测试2: 批量发送消息测试负载均衡")
    void test2_LoadBalanceTest() throws InterruptedException {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String baseTopic = "device";
        int messageCount = 10;

        log.info("========================================");
        log.info("负载均衡测试（发送 {} 条消息）", messageCount);
        log.info("========================================");

        for (int i = 1; i <= messageCount; i++) {
            String deviceId = String.format("%03d", i);
            String topic = baseTopic + "/" + deviceId + "/status";
            String message = String.format("{\"deviceId\":\"%s\",\"status\":\"online\",\"seq\":%d}",
                deviceId, i);

            mqttClientTemplate.publish(
                topic,
                message.getBytes(StandardCharsets.UTF_8),
                MqttQoS.QOS1
            );

            log.info("📤 [{}/%d] 发送消息: Topic={}", i, messageCount, topic);

            // 间隔 100ms
            TimeUnit.MILLISECONDS.sleep(100);
        }

        log.info("========================================");
        log.info("✅ 已发送 {} 条消息", messageCount);
        log.info("💡 预期结果:");
        log.info("   - 如果有 3 个节点，每个节点应该处理约 {} 条消息", messageCount / 3);
        log.info("   - 消息应该在节点间均匀分布（轮询）");
        log.info("========================================");
    }

    // ==================== 混合场景测试 ====================

    @Test
    @DisplayName("测试3: 广播消息（所有节点都应该接收）")
    void test4_BroadcastMessageTest() {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        String topic = "/system/broadcast";
        String message = "{\"type\":\"config_update\",\"timestamp\":" + System.currentTimeMillis() + "}";

        log.info("========================================");
        log.info("广播消息测试");
        log.info("========================================");
        log.info("📌 测试说明:");
        log.info("1. 确保配置了普通订阅（非共享）:");
        log.info("   global-subscribe:");
        log.info("     - topic: /system/broadcast  # 不使用 $share");
        log.info("       qos: QOS1");
        log.info("");
        log.info("2. 预期结果:");
        log.info("   ✅ 所有节点都应该收到并处理该消息");
        log.info("   ✅ 这是广播场景，需要每个节点都执行（如配置更新）");
        log.info("");
        log.info("========================================");

        // 发送广播消息
        boolean success = mqttClientTemplate.publish(
            topic,
            message.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );

        log.info("📤 发送广播消息: Topic={}, Success={}", topic, success);
        log.info("💡 请检查所有节点日志，确认每个节点都收到消息");
        log.info("========================================");
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("测试4: 高并发消息发送")
    void test5_HighConcurrencyTest() throws InterruptedException {
        if (mqttClientTemplate == null || !mqttClientTemplate.isConnected()) {
            log.info("⏭️ 跳过测试 - MQTT 未连接");
            return;
        }

        int totalMessages = 100;
        String baseTopic = "device";

        log.info("========================================");
        log.info("高并发测试（发送 {} 条消息）", totalMessages);
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalMessages; i++) {
            String deviceId = String.format("%03d", i % 10); // 10 个设备循环
            String topic = baseTopic + "/" + deviceId + "/data";
            String message = String.format("{\"deviceId\":\"%s\",\"seq\":%d,\"timestamp\":%d}",
                deviceId, i, System.currentTimeMillis());

            mqttClientTemplate.publish(
                topic,
                message.getBytes(StandardCharsets.UTF_8),
                MqttQoS.QOS0  // 使用 QoS 0 提高性能
            );

            if (i % 10 == 0) {
                log.info("进度: {}/{}", i, totalMessages);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("========================================");
        log.info("✅ 发送完成");
        log.info("总消息数: {}", totalMessages);
        log.info("总耗时: {}ms", duration);
        log.info("平均速率: {} msg/s", totalMessages * 1000 / duration);
        log.info("========================================");
        log.info("💡 预期结果:");
        log.info("   - 共享订阅: 消息在节点间均匀分布");
        log.info("   - 每条消息只被一个节点处理（负载均衡）");
        log.info("========================================");
    }
}
