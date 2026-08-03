# MQTT 模块使用指南

## 📦 核心类包路径速查表

### ✅ 必须使用的正确包路径

| 类名 | 正确包路径 | 说明 |
|------|----------|------|
| **MqttClientTemplate** | `org.dromara.mica.mqtt.spring.client.MqttClientTemplate` | Spring Boot 版本的 MQTT 客户端模板 ⭐ |
| **MqttQoS** | `org.dromara.mica.mqtt.codec.MqttQoS` | QoS 枚举: QOS0/QOS1/QOS2 |
| **IMqttClientGlobalMessageListener** | `org.dromara.mica.mqtt.core.client.IMqttClientGlobalMessageListener` | 全局消息监听器接口 |
| **IMqttClientConnectListener** | `org.dromara.mica.mqtt.core.client.IMqttClientConnectListener` | 连接监听器接口 |
| **MqttPublishMessage** | `org.dromara.mica.mqtt.codec.MqttPublishMessage` | 发布消息对象 |
| **ChannelContext** | `org.tio.core.ChannelContext` | t-io 通道上下文 |
| **MqttClientProperties** | `org.dromara.mica.mqtt.spring.client.config.MqttClientProperties` | 官方配置属性类 |

### ❌ 常见错误包路径

| 错误包路径 | 说明 |
|-----------|------|
| `org.dromara.mica.mqtt.core.client.MqttClientTemplate` | ❌ 核心包中没有此类 |
| `org.dreamlu.iot.mqtt.*` | ❌ 旧版本包名,2.5.7 已废弃 |

---

## 🚀 快速开始代码示例

### 1. 注入 MqttClientTemplate (Service 层)

```java
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;  // ✅ 正确的包
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final MqttClientTemplate mqttClientTemplate;  // ✅ 自动注入

    /**
     * 发送设备状态
     */
    public void sendDeviceStatus(String deviceId, String status) {
        String topic = "/device/" + deviceId + "/status";

        boolean success = mqttClientTemplate.publish(
            topic,
            status.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );

        log.info("发送设备状态: deviceId={}, status={}, success={}", deviceId, status, success);
    }
}
```

### 2. 创建全局消息监听器

```java
package com.example.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.codec.MqttPublishMessage;  // ✅ 正确的包
import org.dromara.mica.mqtt.core.client.IMqttClientGlobalMessageListener;  // ✅ 正确的包
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;  // ✅ 正确的包

import java.nio.charset.StandardCharsets;

/**
 * 自定义 MQTT 全局消息监听器
 * 注意: 创建此 Bean 会覆盖默认的 DefaultMqttClientMessageListener
 */
@Slf4j
@Component
public class MyMqttMessageListener implements IMqttClientGlobalMessageListener {

    @Override
    public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        log.info("接收 MQTT 消息: topic={}, payload={}", topic, payloadStr);

        // 根据 topic 分发到不同的业务处理
        if (topic.startsWith("/device/")) {
            handleDeviceMessage(topic, payloadStr);
        }
    }

    private void handleDeviceMessage(String topic, String payload) {
        // TODO: 处理设备消息
        log.info("处理设备消息: {}", payload);
    }
}
```

### 3. 创建连接监听器 (可选)

```java
package com.example.listener;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.core.client.IMqttClientConnectListener;  // ✅ 正确的包
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;  // ✅ 正确的包

/**
 * 自定义 MQTT 连接监听器
 */
@Slf4j
@Component
public class MyMqttConnectListener implements IMqttClientConnectListener {

    @Override
    public void onConnected(ChannelContext context, boolean isReconnect) {
        if (isReconnect) {
            log.info("✅ MQTT 重连成功: {}", context.getClientNode());
        } else {
            log.info("✅ MQTT 首次连接成功: {}", context.getClientNode());
        }
    }

    @Override
    public void onDisconnect(ChannelContext context, Throwable throwable, String remark, boolean isRemove) {
        if (throwable != null) {
            log.warn("❌ MQTT 断开连接: {}, 原因: {}, 异常: {}",
                context.getClientNode(), remark, throwable.getMessage());
        } else {
            log.info("MQTT 正常断开: {}", context.getClientNode());
        }
    }
}
```

---

## 📚 完整 API 示例

### 发送消息

```java
// 1. 发送普通消息 (默认 QoS 0)
mqttClientTemplate.publish("/test/topic", "Hello".getBytes());

// 2. 发送消息 (指定 QoS 1)
mqttClientTemplate.publish("/test/topic", "Hello".getBytes(), MqttQoS.QOS1);

// 3. 发送保留消息
mqttClientTemplate.publish("/test/topic", "Hello".getBytes(), MqttQoS.QOS1, true);
```

### 订阅主题

**⚠️ 重要**: `subQos0/subQos1/subQos2` 方法需要提供监听器参数!

**推荐方式 1: 配置文件订阅 (自动使用全局监听器)**

```yaml
mqtt:
  client:
    global-subscribe:
      - topic: /device/+/status
        qos: QOS1
      - topic: /device/#
        qos: QOS1
```

**方式 2: 动态订阅 (需要提供监听器)**

```java
// 创建监听器
IMqttClientMessageListener listener = (context, topic, message, payload) -> {
    String payloadStr = new String(payload, StandardCharsets.UTF_8);
    log.info("收到消息: {}", payloadStr);
};

// 订阅主题
mqttClientTemplate.subQos0("/device/+/status", listener);  // QoS 0
mqttClientTemplate.subQos1("/device/+/data", listener);    // QoS 1
mqttClientTemplate.subQos2("/device/+/command", listener); // QoS 2

// 取消订阅 (可变参数)
mqttClientTemplate.unSubscribe("/device/+/status", "/device/+/data");
```

### 检查连接状态

```java
if (mqttClientTemplate.isConnected()) {
    log.info("MQTT 已连接");
} else {
    log.warn("MQTT 未连接");
}
```

---

## 🧪 单元测试示例

查看完整的单元测试示例:
```
src/test/java/plus/ruoyi/common/mqtt/MqttClientUsageTest.java
```

包含 12 个实战示例:
- ✅ 发送普通消息
- ✅ 发送 JSON 数据
- ✅ 发送保留消息
- ✅ 批量发送
- ✅ 订阅主题
- ✅ 订阅通配符
- ✅ 取消订阅
- ✅ 设备上线通知
- ✅ 传感器数据上报
- ✅ 连接状态检查
- ✅ 发布/订阅模式
- ✅ QoS 级别对比

---

## 🔧 配置文件

### application-dev.yml

```yaml
mqtt:
  client:
    enabled: true
    ip: 127.0.0.1
    port: 1883
    username: admin      # 如果 Broker 需要认证
    password: public     # 如果 Broker 需要认证
    version: MQTT_5      # MQTT_3_1 / MQTT_3_1_1 / MQTT_5
    clean-start: true    # 是否清除会话

    # 全局订阅 (可选)
    global-subscribe:
      - topic: /device/+/status
        qos: QOS1
      - topic: /device/#
        qos: QOS1

    # 遗嘱消息 (可选)
    will-message:
      topic: /system/offline
      message: '{"status":"offline"}'
      qos: QOS1
      retain: false
```

---

## 🛠️ 故障排查

### 问题 1: Cannot resolve symbol 'MqttClientTemplate'

**原因**: 导入了错误的包路径

**解决方案**:
```java
// ❌ 错误
import org.dromara.mica.mqtt.core.client.MqttClientTemplate;

// ✅ 正确
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
```

### 问题 2: MqttClientTemplate 注入失败

**原因**: MQTT 客户端未启用或配置错误

**解决方案**:
1. 检查 `mqtt.client.enabled=true`
2. 确保外部 MQTT Broker 已启动
3. 检查 IP 和端口配置

### 问题 3: 连接失败 - 远程计算机拒绝网络连接

**原因**: MQTT Broker 未启动

**解决方案**:
```bash
# 启动 EMQX
docker run -d --name emqx -p 1883:1883 -p 18083:18083 emqx/emqx:latest

# 检查连接
telnet 127.0.0.1 1883
```

---

## 📖 参考资料

- **mica-mqtt 官方文档**: https://gitee.com/dromara/mica-mqtt
- **完整示例代码**: `MqttClientUsageTest.java`
- **详细文档**: `README.md`
