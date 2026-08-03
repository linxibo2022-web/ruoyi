# MQTT 使用示例

## 📌 集群部署配置（推荐）

### 1. application-dev.yml 配置

```yaml
mqtt:
  client:
    enabled: true
    ip: 192.168.168.168    # EMQX Broker 地址
    port: 1883
    username:              # 如需认证则填写
    password:              # 如需认证则填写
    client-id:             # 留空，让框架自动生成
    clean-start: true      # 集群部署建议 true
```

### 2. 订阅主题（业务代码）

在你的业务模块中创建订阅器：

```java
package plus.ruoyi.business.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.core.client.IMqttClientSession;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * MQTT 主题订阅器
 * <p>应用启动后自动订阅主题</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttSubscriber implements ApplicationRunner {

    private final IMqttClientSession mqttClientSession;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始订阅 MQTT 主题...");

        // ✅ 集群部署：使用共享订阅（$share/GroupName/Topic）
        // 效果：同一组内的多个实例，每条消息只被一个实例接收

        // 订阅设备状态主题（支持通配符 +）
        mqttClientSession.subscribe("$share/backend-cluster/device/+/status", 1);

        // 订阅所有传感器消息（支持通配符 #）
        mqttClientSession.subscribe("$share/backend-cluster/sensor/#", 1);

        // 订阅告警主题
        mqttClientSession.subscribe("$share/backend-cluster/alarm/+", 1);

        log.info("✅ MQTT 主题订阅完成");
    }
}
```

### 3. 消息处理器（业务代码）

创建自定义消息监听器：

```java
package plus.ruoyi.business.mqtt;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.codec.message.MqttPublishMessage;
import org.dromara.mica.mqtt.core.client.IMqttClientGlobalMessageListener;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import plus.ruoyi.business.device.service.IDeviceService;

import java.nio.charset.StandardCharsets;

/**
 * 自定义 MQTT 全局消息监听器
 * <p>此 Bean 会自动覆盖默认的 DefaultMqttClientMessageListener</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomMqttMessageListener implements IMqttClientGlobalMessageListener {

    private final IDeviceService deviceService;

    @Override
    public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        log.info("========================================");
        log.info("收到 MQTT 消息");
        log.info("Topic: {}", topic);
        log.info("QoS: {}", message.fixedHeader().qosLevel());
        log.info("Payload: {}", payloadStr);
        log.info("========================================");

        // 根据 topic 路由到不同的处理器
        if (topic.startsWith("device/")) {
            handleDeviceMessage(topic, payloadStr);
        } else if (topic.startsWith("sensor/")) {
            handleSensorMessage(topic, payloadStr);
        } else if (topic.startsWith("alarm/")) {
            handleAlarmMessage(topic, payloadStr);
        }
    }

    /**
     * 处理设备消息
     */
    private void handleDeviceMessage(String topic, String payload) {
        try {
            // 解析 topic 提取设备 ID
            // topic 格式: device/{deviceId}/status
            String[] parts = topic.split("/");
            if (parts.length >= 2) {
                String deviceId = parts[1];

                // 解析 JSON 消息体
                DeviceStatusDto statusDto = JSONUtil.toBean(payload, DeviceStatusDto.class);

                // 调用业务服务处理
                deviceService.updateDeviceStatus(deviceId, statusDto);

                log.info("✅ 设备消息处理完成 - DeviceId: {}", deviceId);
            }
        } catch (Exception e) {
            log.error("处理设备消息失败 - Topic: {}, Payload: {}", topic, payload, e);
        }
    }

    /**
     * 处理传感器消息
     */
    private void handleSensorMessage(String topic, String payload) {
        try {
            log.info("处理传感器消息 - Topic: {}, Payload: {}", topic, payload);

            // TODO: 实现传感器数据存储逻辑
            // 1. 解析 JSON
            // 2. 存储到数据库
            // 3. 触发告警规则

        } catch (Exception e) {
            log.error("处理传感器消息失败", e);
        }
    }

    /**
     * 处理告警消息
     */
    private void handleAlarmMessage(String topic, String payload) {
        try {
            log.info("处理告警消息 - Topic: {}, Payload: {}", topic, payload);

            // TODO: 实现告警处理逻辑
            // 1. 解析告警信息
            // 2. 存储告警记录
            // 3. 发送通知（短信、邮件等）

        } catch (Exception e) {
            log.error("处理告警消息失败", e);
        }
    }
}
```

---

## 📤 发送消息示例

### 1. 基础发送

```java
package plus.ruoyi.business.device.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements IDeviceService {

    private final MqttClientTemplate mqttClientTemplate;

    /**
     * 发送设备控制指令
     */
    public void sendDeviceCommand(String deviceId, String command) {
        String topic = "device/" + deviceId + "/command";
        String payload = "{\"command\":\"" + command + "\",\"timestamp\":" + System.currentTimeMillis() + "}";

        // 发送消息（QoS 1）
        mqttClientTemplate.publish(topic, payload.getBytes(), 1);
    }

    /**
     * 发送设备配置（保留消息）
     */
    public void sendDeviceConfig(String deviceId, String config) {
        String topic = "device/" + deviceId + "/config";

        // 发送保留消息（QoS 1, retain = true）
        // 新订阅者会立即收到最新配置
        mqttClientTemplate.publish(topic, config.getBytes(), 1, true);
    }
}
```

### 2. 批量发送

```java
/**
 * 批量发送消息给多个设备
 */
public void broadcastCommand(List<String> deviceIds, String command) {
    String payload = "{\"command\":\"" + command + "\"}";

    for (String deviceId : deviceIds) {
        String topic = "device/" + deviceId + "/command";
        mqttClientTemplate.publish(topic, payload.getBytes(), 1);
    }
}
```

---

## 🔍 动态订阅示例

```java
/**
 * 动态订阅设备主题（需要提供监听器）
 */
public void subscribeDevice(String deviceId) {
    String topic = "device/" + deviceId + "/status";

    // 创建消息监听器
    IMqttClientMessageListener listener = (context, topic1, message, payload) -> {
        String payloadStr = new String(payload, StandardCharsets.UTF_8);
        log.info("收到消息: topic={}, payload={}", topic1, payloadStr);
    };

    // 订阅主题并指定监听器（QoS 1）
    mqttClientTemplate.subQos1(topic, listener);
}

/**
 * 取消订阅
 */
public void unsubscribeDevice(String deviceId) {
    String topic = "device/" + deviceId + "/status";
    mqttClientTemplate.unSubscribe(topic);
}
```

---

## 🎯 完整示例项目结构

```
ruoyi-modules/ruoyi-business/
└── src/main/java/plus/ruoyi/business/
    └── mqtt/
        ├── MqttSubscriber.java              # 订阅器（应用启动时订阅）
        ├── CustomMqttMessageListener.java   # 全局消息监听器
        └── dto/
            ├── DeviceStatusDto.java         # 设备状态 DTO
            └── SensorDataDto.java           # 传感器数据 DTO
```

---

## ⚠️ 重要提醒

### 1. 集群部署必须使用共享订阅

```java
// ❌ 错误：普通订阅（多实例会重复处理）
mqttClientSession.subscribe("device/+/status", 1);

// ✅ 正确：共享订阅（多实例自动负载均衡）
mqttClientSession.subscribe("$share/backend-cluster/device/+/status", 1);
```

### 2. 订阅组名称要一致

同一个应用的所有实例，订阅组名称（如 `backend-cluster`）必须一致，否则会重复处理消息。

### 3. 不要配置固定 client-id

```yaml
# ❌ 错误：多实例会冲突
mqtt:
  client:
    client-id: my-app-001

# ✅ 正确：留空让框架自动生成
mqtt:
  client:
    client-id:  # 留空或使用环境变量 ${MQTT_CLIENT_ID:}
```

### 4. 建议使用 clean-start: true

集群部署使用共享订阅时，建议设置 `clean-start: true`，避免客户端断线后消息积压。

---

## 📚 更多信息

- **模块文档**: `ruoyi-common/ruoyi-common-mqtt/README.md`
- **EMQX 共享订阅**: https://docs.emqx.com/zh/emqx/latest/messaging/mqtt-shared-subscription.html
- **mica-mqtt 官方文档**: https://gitee.com/dromara/mica-mqtt
