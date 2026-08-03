---
name: iot-mqtt
description: |
  当需要使用 MQTT 协议进行物联网设备通信、消息发布订阅、设备状态管理时自动使用此 Skill。

  触发场景：
  - 需要与 IoT 设备进行 MQTT 消息通信（发布/订阅）
  - 需要配置 MQTT 客户端连接（Broker、认证、SSL）
  - 需要实现设备数据采集和指令下发
  - 需要处理设备上下线状态监控
  - 需要在集群环境下部署 MQTT 客户端（共享订阅）
  - 需要选择合适的 QoS 等级和 Topic 设计

  触发词：MQTT、物联网、IoT、设备通信、设备消息、mica-mqtt、MqttClientTemplate、publish、subscribe、QoS、Topic、EMQX、Mosquitto、共享订阅、设备上线、设备离线、遗嘱消息、保留消息、传感器数据
---

# IoT MQTT 通信开发指南

## 概述

本项目通过 `ruoyi-common-mqtt` 模块集成 **mica-mqtt**（Dromara 开源社区）框架，提供开箱即用的 MQTT 客户端能力。支持消息发布/订阅、QoS 0/1/2、集群共享订阅、SSL/TLS 加密、遗嘱消息等完整特性。

**核心依赖**：mica-mqtt 2.5.7（基于 t-io 高性能异步非阻塞 IO 框架）

**适用场景**：设备数据采集、指令下发、设备状态监控、告警推送、系统广播

---

## 核心类

| 类名 | 来源 | 用途 |
|------|------|------|
| `MqttClientTemplate` | mica-mqtt（自动注入） | 消息发布/订阅/连接管理（最常用） |
| `MqttAutoConfiguration` | `plus.ruoyi.common.mqtt.config` | 自动配置，注册默认监听器 |
| `DefaultMqttClientConnectListener` | `plus.ruoyi.common.mqtt.listener` | 连接/断开事件监听 |
| `DefaultMqttClientMessageListener` | `plus.ruoyi.common.mqtt.listener` | 全局消息接收监听 |
| `IMqttClientConnectListener` | mica-mqtt 接口 | 连接事件监听器接口 |
| `IMqttClientGlobalMessageListener` | mica-mqtt 接口 | 全局消息监听器接口 |
| `MqttQoS` | mica-mqtt 枚举 | QoS 等级（QOS0/QOS1/QOS2） |

---

## 配置

### application.yml

```yaml
mqtt:
  client:
    enabled: true                          # 启用 MQTT 客户端
    ip: ${MQTT_BROKER_HOST:127.0.0.1}      # MQTT Broker 地址
    port: 1883                             # MQTT Broker 端口
    username: ${MQTT_USERNAME:}            # 认证用户名（可选）
    password: ${MQTT_PASSWORD:}            # 认证密码（可选）

    # ⚠️ 集群部署时不要配置固定 client-id（会导致互相踢线）
    # client-id: my-client-id

    version: MQTT_5                        # MQTT 协议版本
    reconnect: true                        # 自动重连
    re-interval: 5000                      # 重连间隔（毫秒）
    keep-alive-secs: 60                    # 心跳间隔（秒）

    # 全局订阅（启动自动订阅，由全局监听器处理）
    global-subscribe:
      - topic: $share/backend/device/+/status    # 共享订阅（集群负载均衡）
        qos: QOS1
      - topic: $share/backend/sensor/#           # 多级通配符
        qos: QOS0
```

### 完整配置属性

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `mqtt.client.enabled` | Boolean | true | 是否启用 MQTT 客户端 |
| `mqtt.client.ip` | String | 127.0.0.1 | Broker 地址 |
| `mqtt.client.port` | Integer | 1883 | Broker 端口 |
| `mqtt.client.username` | String | null | 认证用户名 |
| `mqtt.client.password` | String | null | 认证密码 |
| `mqtt.client.client-id` | String | 自动生成 | 客户端 ID |
| `mqtt.client.version` | Enum | MQTT_5 | 协议版本 |
| `mqtt.client.clean-start` | Boolean | true | 清除会话 |
| `mqtt.client.keep-alive-secs` | Integer | 60 | 心跳间隔（秒） |
| `mqtt.client.reconnect` | Boolean | true | 自动重连 |
| `mqtt.client.re-interval` | Long | 5000 | 重连间隔（毫秒） |
| `mqtt.client.timeout` | Integer | 30 | 连接超时（秒） |

### 遗嘱消息与 SSL 配置

```yaml
mqtt:
  client:
    # 遗嘱消息（客户端异常断开时 Broker 自动发送）
    will-message:
      topic: /system/node/offline
      message: backend-node-offline
      qos: QOS1
      retain: false

    # SSL/TLS 加密（可选）
    ssl:
      enabled: false
      keystore-path: classpath:/certs/keystore.jks
      keystore-pass: password
```

---

## 自动配置

`MqttAutoConfiguration` 在 `mqtt.client.enabled=true` 时自动激活，注册两个默认监听器：

```java
@AutoConfiguration
@ConditionalOnProperty(prefix = "mqtt.client", name = "enabled", havingValue = "true")
public class MqttAutoConfiguration {

    /**
     * 默认连接监听器（可被自定义 Bean 覆盖）
     */
    @Bean
    @ConditionalOnMissingBean
    public IMqttClientConnectListener mqttClientConnectListener() {
        return new DefaultMqttClientConnectListener();
    }

    /**
     * 默认全局消息监听器（可被自定义 Bean 覆盖）
     */
    @Bean
    @ConditionalOnMissingBean
    public IMqttClientGlobalMessageListener mqttClientGlobalMessageListener() {
        return new DefaultMqttClientMessageListener();
    }
}
```

**覆盖默认监听器**：只需在业务模块中定义同类型的 Bean，`@ConditionalOnMissingBean` 会自动让步。

---

## 消息发布

### MqttClientTemplate API

```java
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.dromara.mica.mqtt.codec.MqttQoS;

@Service
@RequiredArgsConstructor
public class DeviceCommandService {

    private final MqttClientTemplate mqttClientTemplate;

    /**
     * 发送普通消息（QoS 0，最快，可能丢失）
     */
    public void sendData(String topic, String data) {
        mqttClientTemplate.publish(topic, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 发送消息（指定 QoS）
     */
    public void sendCommand(String deviceId, String command) {
        String topic = "device/" + deviceId + "/command";
        byte[] payload = command.getBytes(StandardCharsets.UTF_8);
        mqttClientTemplate.publish(topic, payload, MqttQoS.QOS1);
    }

    /**
     * 发送保留消息（新订阅者立即收到最新值）
     */
    public void updateDeviceStatus(String deviceId, String status) {
        String topic = "device/" + deviceId + "/status";
        byte[] payload = status.getBytes(StandardCharsets.UTF_8);
        mqttClientTemplate.publish(topic, payload, MqttQoS.QOS1, true);
    }

    /**
     * 发送 JSON 数据
     */
    public void sendSensorData(String deviceId, SensorData data) {
        String topic = "sensor/" + deviceId + "/data";
        byte[] payload = JsonUtils.toJsonString(data).getBytes(StandardCharsets.UTF_8);
        mqttClientTemplate.publish(topic, payload, MqttQoS.QOS1);
    }
}
```

### 发布方法签名

| 方法 | QoS | 保留 | 说明 |
|------|-----|------|------|
| `publish(topic, payload)` | 0 | 否 | 最快发送，可能丢失 |
| `publish(topic, payload, qos)` | 自定义 | 否 | 指定 QoS 等级 |
| `publish(topic, payload, qos, retain)` | 自定义 | 自定义 | 完整参数 |

---

## 消息订阅

### 方式一：配置文件订阅（推荐）

```yaml
mqtt:
  client:
    global-subscribe:
      - topic: device/+/status       # + 匹配单级
        qos: QOS1
      - topic: sensor/#              # # 匹配多级
        qos: QOS0
```

配置文件订阅的消息由全局消息监听器（`IMqttClientGlobalMessageListener`）统一接收。

### 方式二：代码动态订阅

```java
@Service
@RequiredArgsConstructor
public class MqttSubscribeService {

    private final MqttClientTemplate mqttClientTemplate;

    /**
     * 动态订阅单个设备
     */
    public void subscribeDevice(String deviceId) {
        String topic = "device/" + deviceId + "/data";

        IMqttClientMessageListener listener = (context, topicName, message, payload) -> {
            String payloadStr = new String(payload, StandardCharsets.UTF_8);
            log.info("设备 {} 数据: {}", deviceId, payloadStr);
        };

        mqttClientTemplate.subQos1(topic, listener);
    }

    /**
     * 取消订阅
     */
    public void unsubscribeDevice(String deviceId) {
        mqttClientTemplate.unSubscribe("device/" + deviceId + "/data");
    }
}
```

### 订阅方法签名

| 方法 | QoS | 说明 |
|------|-----|------|
| `subQos0(topic, listener)` | 0 | 最多一次 |
| `subQos1(topic, listener)` | 1 | 至少一次（推荐） |
| `subQos2(topic, listener)` | 2 | 恰好一次 |
| `unSubscribe(topics...)` | - | 取消订阅（支持可变参数） |

### Topic 通配符

| 通配符 | 含义 | 示例 | 匹配 |
|--------|------|------|------|
| `+` | 单级匹配 | `device/+/status` | `device/001/status`、`device/002/status` |
| `#` | 多级匹配 | `sensor/#` | `sensor/temp`、`sensor/room1/temp` |

---

## 自定义监听器

### 连接监听器

```java
import org.dromara.mica.mqtt.spring.client.event.IMqttClientConnectListener;
import org.tio.core.ChannelContext;

@Slf4j
@Component
public class DeviceConnectListener implements IMqttClientConnectListener {

    @Override
    public void onConnected(ChannelContext context, boolean isReconnect) {
        if (isReconnect) {
            log.info("MQTT 重连成功");
            // 重连后重新订阅等操作
        } else {
            log.info("MQTT 首次连接成功");
        }
    }

    @Override
    public void onDisconnect(ChannelContext context, Throwable throwable,
                             String remark, boolean isRemove) {
        log.warn("MQTT 连接断开: remark={}, isRemove={}", remark, isRemove);
        if (throwable != null) {
            log.error("断开原因:", throwable);
        }
        // 更新设备离线状态等
    }
}
```

### 全局消息监听器

```java
import org.dromara.mica.mqtt.spring.client.event.IMqttClientGlobalMessageListener;
import org.dromara.mica.mqtt.codec.MqttPublishMessage;
import org.tio.core.ChannelContext;

@Slf4j
@Component
public class DeviceMessageListener implements IMqttClientGlobalMessageListener {

    @Override
    public void onMessage(ChannelContext context, String topic,
                          MqttPublishMessage message, byte[] payload) {
        String payloadStr = new String(payload, StandardCharsets.UTF_8);
        int qos = message.fixedHeader().qosLevel().value();

        log.info("收到 MQTT 消息: topic={}, qos={}, payload={}", topic, qos, payloadStr);

        // 根据 Topic 路由分发
        if (topic.startsWith("device/")) {
            handleDeviceMessage(topic, payloadStr);
        } else if (topic.startsWith("sensor/")) {
            handleSensorData(topic, payloadStr);
        } else if (topic.startsWith("alarm/")) {
            handleAlarmMessage(topic, payloadStr);
        }
    }

    private void handleDeviceMessage(String topic, String payload) {
        // 解析 Topic: device/{deviceId}/status
        String[] parts = topic.split("/");
        if (parts.length >= 3) {
            String deviceId = parts[1];
            log.info("设备 {} 状态更新: {}", deviceId, payload);
        }
    }

    private void handleSensorData(String topic, String payload) {
        // 处理传感器数据
    }

    private void handleAlarmMessage(String topic, String payload) {
        // 处理告警消息
    }
}
```

---

## QoS 等级选择

| QoS | 名称 | 语义 | 性能 | 适用场景 |
|-----|------|------|------|---------|
| 0 | 最多一次 | 可能丢失 | 最高 | 高频传感器数据、环境监测 |
| 1 | 至少一次 | 可能重复 | 中等 | 设备状态、指令下发（推荐） |
| 2 | 恰好一次 | 不丢不重 | 最低 | 金融交易、计费数据 |

```java
// ✅ QoS 0：高频传感器数据（允许偶尔丢失）
mqttClientTemplate.publish("sensor/001/temp", data, MqttQoS.QOS0);

// ✅ QoS 1：设备指令（必须送达，可接受重复）
mqttClientTemplate.publish("device/001/command", cmd, MqttQoS.QOS1);

// ✅ QoS 2：支付通知（不能丢失也不能重复）
mqttClientTemplate.publish("payment/notify", data, MqttQoS.QOS2);
```

---

## 集群部署

### 问题：多节点重复消费

```
普通订阅（有问题）：
设备消息 → Broker → 节点A ✓ 处理
                 → 节点B ✓ 处理
                 → 节点C ✓ 处理
                 ❌ 消息被重复处理 3 次！
```

### 解决方案：共享订阅（Shared Subscription）

```
共享订阅（推荐）：
设备消息 → Broker（负载均衡）→ 节点A ✓ 处理
                             → 节点B ❌ 不接收
                             → 节点C ❌ 不接收
                 ✅ 只有一个节点处理
```

### 共享订阅格式

```
$share/{GroupName}/{Topic}
```

```yaml
mqtt:
  client:
    global-subscribe:
      # ✅ 共享订阅：同 GroupName 内只有一个节点接收
      - topic: $share/backend-cluster/device/+/status
        qos: QOS1
      - topic: $share/backend-cluster/sensor/#
        qos: QOS0

      # ✅ 广播（不使用 $share）：所有节点都接收
      - topic: /system/broadcast/config-update
        qos: QOS1
```

### 集群注意事项

| 注意点 | 说明 |
|--------|------|
| **不要配置固定 client-id** | 集群中每个节点的 client-id 必须唯一，让框架自动生成 |
| **使用共享订阅** | `$share/GroupName/Topic` 避免消息重复消费 |
| **广播用普通订阅** | 配置更新等需要所有节点收到的消息，不加 `$share` 前缀 |
| **Broker 兼容性** | EMQX/Mosquitto 2.0+/HiveMQ 支持共享订阅，AWS IoT Core 不支持 |

---

## Topic 设计规范

### 推荐命名方式

```
{业务域}/{设备ID或类型}/{数据类型}

示例：
device/{deviceId}/status         # 设备状态
device/{deviceId}/command        # 设备指令
sensor/{deviceId}/temperature    # 温度数据
sensor/{deviceId}/humidity       # 湿度数据
alarm/{deviceId}/{alarmType}     # 告警消息
system/broadcast/{eventType}     # 系统广播
```

### 多租户 Topic 隔离

```
{tenantId}/{业务域}/{设备ID}/{数据类型}

示例：
000001/device/001/status    # 租户 000001 的设备状态
000002/device/001/status    # 租户 000002 的设备状态

配置：
mqtt:
  client:
    global-subscribe:
      - topic: $share/backend/+/device/+/status    # 第一级为 tenantId
        qos: QOS1
```

---

## 完整业务示例：设备管理模块

### 1. 配置文件

```yaml
mqtt:
  client:
    enabled: true
    ip: ${MQTT_BROKER_HOST:127.0.0.1}
    port: 1883
    username: ${MQTT_USERNAME:admin}
    password: ${MQTT_PASSWORD:public}
    version: MQTT_5
    reconnect: true
    re-interval: 5000
    keep-alive-secs: 60

    global-subscribe:
      - topic: $share/backend/device/+/status
        qos: QOS1
      - topic: $share/backend/device/+/data
        qos: QOS0
      - topic: $share/backend/alarm/+
        qos: QOS1

    will-message:
      topic: /system/node/offline
      message: backend-node-offline
      qos: QOS1
```

### 2. 消息监听器

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class IotMessageListener implements IMqttClientGlobalMessageListener {

    private final IDeviceService deviceService;
    private final IAlarmService alarmService;

    @Override
    public void onMessage(ChannelContext context, String topic,
                          MqttPublishMessage message, byte[] payload) {
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        try {
            if (topic.matches("device/[^/]+/status")) {
                handleDeviceStatus(topic, payloadStr);
            } else if (topic.matches("device/[^/]+/data")) {
                handleDeviceData(topic, payloadStr);
            } else if (topic.startsWith("alarm/")) {
                handleAlarm(topic, payloadStr);
            }
        } catch (Exception e) {
            log.error("MQTT 消息处理失败: topic={}", topic, e);
        }
    }

    private void handleDeviceStatus(String topic, String payload) {
        String deviceId = topic.split("/")[1];
        DeviceStatusDto status = JsonUtils.parseObject(payload, DeviceStatusDto.class);
        deviceService.updateOnlineStatus(deviceId, status);
    }

    private void handleDeviceData(String topic, String payload) {
        String deviceId = topic.split("/")[1];
        SensorDataDto data = JsonUtils.parseObject(payload, SensorDataDto.class);
        deviceService.saveSensorData(deviceId, data);
    }

    private void handleAlarm(String topic, String payload) {
        AlarmDto alarm = JsonUtils.parseObject(payload, AlarmDto.class);
        alarmService.processAlarm(alarm);

        // 通过 WebSocket 推送告警到前端
        WebSocketUtils.publishMessage(
            WebSocketMessageDto.broadcast(JsonUtils.toJsonString(alarm))
        );
    }
}
```

### 3. 设备指令服务

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCommandServiceImpl implements IDeviceCommandService {

    private final MqttClientTemplate mqttClientTemplate;

    @Override
    public void sendCommand(String deviceId, DeviceCommandBo command) {
        String topic = "device/" + deviceId + "/command";
        String payload = JsonUtils.toJsonString(command);

        boolean sent = mqttClientTemplate.publish(
            topic,
            payload.getBytes(StandardCharsets.UTF_8),
            MqttQoS.QOS1
        );

        if (sent) {
            log.info("指令已发送: deviceId={}, command={}", deviceId, command.getType());
        } else {
            throw ServiceException.of("指令发送失败，请检查 MQTT 连接");
        }
    }

    @Override
    public void batchSendCommand(List<String> deviceIds, DeviceCommandBo command) {
        String payload = JsonUtils.toJsonString(command);
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        for (String deviceId : deviceIds) {
            String topic = "device/" + deviceId + "/command";
            mqttClientTemplate.publish(topic, payloadBytes, MqttQoS.QOS1);
        }
    }

    @Override
    public boolean isDeviceOnline(String deviceId) {
        return mqttClientTemplate.isConnected();
    }
}
```

### 4. 连接监听器

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class IotConnectListener implements IMqttClientConnectListener {

    private final RedisUtils redisUtils;

    @Override
    public void onConnected(ChannelContext context, boolean isReconnect) {
        String action = isReconnect ? "重连" : "首次连接";
        log.info("MQTT {} 成功", action);

        // 记录节点上线状态到 Redis
        String nodeId = context.getServerNode().toString();
        redisUtils.setCacheObject("mqtt:node:" + nodeId, "online");
    }

    @Override
    public void onDisconnect(ChannelContext context, Throwable throwable,
                             String remark, boolean isRemove) {
        log.warn("MQTT 连接断开: remark={}, isRemove={}", remark, isRemove);

        // 更新节点状态
        String nodeId = context.getServerNode().toString();
        redisUtils.deleteObject("mqtt:node:" + nodeId);

        if (throwable != null) {
            log.error("MQTT 断开异常:", throwable);
        }
    }
}
```

---

## 连接管理

```java
@Service
@RequiredArgsConstructor
public class MqttHealthService {

    private final MqttClientTemplate mqttClientTemplate;

    /**
     * 检查 MQTT 连接状态
     */
    public boolean isConnected() {
        return mqttClientTemplate.isConnected();
    }

    /**
     * 手动断开连接
     */
    public void disconnect() {
        mqttClientTemplate.disconnect();
    }

    /**
     * 获取底层 MqttClient（高级操作）
     */
    public MqttClient getMqttClient() {
        return mqttClientTemplate.getMqttClient();
    }
}
```

---

## 常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 注入 MqttClientTemplate（不要手动创建连接）
@RequiredArgsConstructor
public class MyService {
    private final MqttClientTemplate mqttClientTemplate;  // ✅ Spring 注入
}

// 2. 使用共享订阅（集群环境）
// application.yml
global-subscribe:
  - topic: $share/backend/device/+/status    // ✅ 共享订阅
    qos: QOS1

// 3. 合理选择 QoS
mqttClientTemplate.publish(topic, data, MqttQoS.QOS1);  // ✅ 大多数场景用 QoS 1

// 4. 消息体使用 UTF-8 编码
byte[] payload = data.getBytes(StandardCharsets.UTF_8);  // ✅ 显式指定编码

// 5. 自定义监听器覆盖默认实现
@Component
public class MyListener implements IMqttClientGlobalMessageListener {  // ✅ 自动覆盖默认
    @Override
    public void onMessage(...) { }
}

// 6. 全局消息监听中做异常捕获
@Override
public void onMessage(...) {
    try {
        // 业务处理
    } catch (Exception e) {
        log.error("MQTT 消息处理失败: topic={}", topic, e);  // ✅ 不让异常传播
    }
}
```

### ❌ 常见错误

```java
// 1. 手动创建 MQTT 连接（绕过框架管理）
MqttClient client = new MqttClient("tcp://localhost:1883");  // ❌ 不要手动创建
// ✅ 使用 Spring 注入的 MqttClientTemplate

// 2. 集群环境使用普通订阅（导致消息重复消费）
global-subscribe:
  - topic: device/+/status    // ❌ 所有节点都会收到每条消息
    qos: QOS1
// ✅ 使用 $share/GroupName/Topic

// 3. 配置固定 client-id（集群节点互相踢线）
mqtt:
  client:
    client-id: my-fixed-id    // ❌ 集群中 client-id 必须唯一
// ✅ 不配置，让框架自动生成

// 4. 所有消息都用 QoS 2（性能浪费）
mqttClientTemplate.publish(topic, data, MqttQoS.QOS2);  // ❌ QoS 2 性能最低
// ✅ 传感器数据用 QoS 0，普通指令用 QoS 1

// 5. 消息监听器中不捕获异常（可能导致后续消息无法处理）
@Override
public void onMessage(...) {
    String data = JsonUtils.parseObject(payload, Dto.class);  // ❌ 如果解析失败会抛出异常
}
// ✅ 用 try-catch 包裹

// 6. 导入错误的包
import io.netty.handler.codec.mqtt.MqttQoS;  // ❌ 这是 Netty 的
// ✅ 使用 org.dromara.mica.mqtt.codec.MqttQoS
```

---

## 与其他技能的关系

| 技能 | 关系 |
|------|------|
| `redis-cache` | 设备在线状态可缓存到 Redis；集群模式依赖 Redis |
| `realtime-communication` | MQTT 收到告警后通过 WebSocket 推送到前端 |
| `json-serialization` | MQTT 消息体的 JSON 序列化/反序列化 |
| `scheduled-jobs` | 定时检测设备离线状态 |
| `notification-system` | 设备告警触发短信/邮件通知 |
| `architecture-design` | IoT 模块归属 `iot_` 表前缀，`plus.ruoyi.business.iot` 包路径 |
| `multi-tenant` | Topic 前缀隔离实现多租户 |

---

## 快速启动 MQTT Broker

```bash
# Docker 启动 EMQX（推荐）
docker run -d --name emqx \
  -p 1883:1883 \
  -p 8083:8083 \
  -p 18083:18083 \
  emqx/emqx:latest

# EMQX 管理界面：http://localhost:18083
# 默认账号：admin / public

# 或使用 Mosquitto（轻量级）
docker run -d --name mosquitto \
  -p 1883:1883 \
  eclipse-mosquitto:latest
```

---

## 参考文件索引

### 源代码

| 文件 | 行数 | 说明 |
|------|------|------|
| `ruoyi-common/ruoyi-common-mqtt/src/.../config/MqttAutoConfiguration.java` | 70 | 自动配置 |
| `ruoyi-common/ruoyi-common-mqtt/src/.../listener/DefaultMqttClientConnectListener.java` | 37 | 连接监听 |
| `ruoyi-common/ruoyi-common-mqtt/src/.../listener/DefaultMqttClientMessageListener.java` | 38 | 消息监听 |

### 测试文件

| 文件 | 行数 | 说明 |
|------|------|------|
| `MqttClientUsageTest.java` | 364 | 12 个实战示例 |
| `MqttClientPropertiesTest.java` | 148 | 配置属性测试 |
| `MqttClusterTest.java` | 225 | 集群部署测试 |
| `MqttCodecTest.java` | 84 | 编解码测试 |

### 文档

| 文件 | 行数 | 说明 |
|------|------|------|
| `ruoyi-common/ruoyi-common-mqtt/README.md` | 550 | 完整使用指南 |
| `ruoyi-common/ruoyi-common-mqtt/USAGE.md` | 305 | 快速参考手册 |
| `ruoyi-common/ruoyi-common-mqtt/CLUSTER.md` | 325 | 集群部署指南 |
| `ruoyi-common/ruoyi-common-mqtt/METHOD_SIGNATURES.md` | 279 | API 签名大全 |
| `ruoyi-common/ruoyi-common-mqtt/USAGE_EXAMPLE.md` | 322 | 实战示例 |

---

## FAQ

### Q1: MQTT 和 WebSocket 有什么区别？

**A**: MQTT 是物联网协议，WebSocket 是 Web 实时通信协议。
- **MQTT**：适合设备间通信，低带宽、低功耗，发布/订阅模式
- **WebSocket**：适合浏览器与服务器通信，双向全双工
- 本项目中，设备通过 MQTT 上报数据，后端收到后可通过 WebSocket 推送到前端管理页面

### Q2: 为什么消息被重复消费了？

**A**: 集群环境下，所有节点都订阅了相同 Topic。使用共享订阅 `$share/GroupName/Topic` 格式，同组内只有一个节点收到消息。

### Q3: 为什么 MQTT 连接一直断线重连？

**A**: 检查以下原因：
1. MQTT Broker 未启动或不可达
2. 认证信息错误（username/password）
3. 集群中多个节点使用了相同的固定 client-id（互相踢线）
4. 防火墙阻止了 1883 端口

### Q4: QoS 1 的消息会重复吗？

**A**: 是的，QoS 1 保证"至少一次"，网络不稳定时可能重发。业务端需要做幂等处理（如根据消息 ID 去重）。

### Q5: 如何实现设备离线检测？

**A**: 两种方式：
1. **遗嘱消息**：设备连接时设置遗嘱，异常断开后 Broker 自动发布遗嘱消息
2. **心跳超时**：设备定时发送心跳，后端通过定时任务检查最后心跳时间

### Q6: 支持哪些 MQTT Broker？

**A**: 任何标准 MQTT 3.1.1/5.0 Broker 均可：
- **EMQX**（推荐，功能最全，管理界面完善）
- **Mosquitto**（轻量级，适合开发测试）
- **HiveMQ**（企业级，集群能力强）
- **AWS IoT Core**（云服务，但不支持共享订阅）
