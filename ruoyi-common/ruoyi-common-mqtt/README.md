# ruoyi-common-mqtt

MQTT 通讯模块 - 基于 mica-mqtt 2.5.7 提供高性能物联网消息通信能力

## 📖 模块说明

本模块集成了 [mica-mqtt](https://github.com/dromara/mica-mqtt) (Dromara 开源社区),提供开箱即用的 MQTT 客户端功能,支持:

- ✅ **高性能**: 基于 t-io 异步非阻塞,支持百万级并发连接
- ✅ **客户端模式**: 仅 Client 模式,连接外部 MQTT Broker (EMQX/Mosquitto等)
- ✅ **协议支持**: MQTT v3.1/v3.1.1/v5.0 完整支持
- ✅ **QoS 0/1/2**: 三种服务质量等级,灵活选择
- ✅ **共享订阅**: 原生支持 `$share` 共享订阅,集群自动负载均衡
- ✅ **遗嘱消息**: 支持 Last Will 异常断线通知
- ✅ **保留消息**: 支持 Retained Message 持久化
- ✅ **SSL/TLS**: 支持加密连接

> **注意**: 本模块**不包含**内置 MQTT Broker,需要外部 MQTT 服务器 (如 EMQX、Mosquitto)。

---

## 🚀 快速开始

> **💡 查看完整示例**: `src/test/java/plus/ruoyi/common/mqtt/MqttClientUsageTest.java`
> 包含 12 个实用示例,涵盖发送、订阅、QoS、保留消息等所有功能。

### 0. 准备外部 MQTT Broker

**方式1: Docker 快速启动 EMQX (推荐)**

```bash
# 启动 EMQX (包含 Web 管理界面)
docker run -d --name emqx \
  -p 1883:1883 \
  -p 18083:18083 \
  emqx/emqx:latest

# 访问管理界面: http://localhost:18083
# 默认账号: admin / public
```

**方式2: Docker 启动 Mosquitto**

```bash
# 启动 Mosquitto (轻量级)
docker run -d --name mosquitto \
  -p 1883:1883 \
  eclipse-mosquitto:latest
```

**方式3: 使用公共测试服务器**

```yaml
mqtt:
  client:
    ip: broker.emqx.io  # 公共测试 Broker
    port: 1883
```

### 1. 添加依赖

在需要使用 MQTT 的模块 `pom.xml` 中添加:

```xml
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-mqtt</artifactId>
</dependency>
```

### 2. 配置文件

在 `application-dev.yml` 中配置:

```yaml
mqtt:
  client:
    enabled: true  # 启用 MQTT 客户端
    ip: 127.0.0.1  # MQTT Broker 地址
    port: 1883     # MQTT Broker 端口
    username: admin  # 用户名 (如果 Broker 需要认证)
    password: public  # 密码 (如果 Broker 需要认证)
    client-id: ${spring.application.name}-${random.value}  # 客户端 ID
    version: MQTT_5  # 协议版本: MQTT_3_1 / MQTT_3_1_1 / MQTT_5
    clean-start: true  # 是否清除会话

    # 全局订阅主题 (可选)
    global-subscribe:
      - topic: /device/+/status  # + 是单级通配符
        qos: QOS1
      - topic: /device/#  # # 是多级通配符
        qos: QOS1
```

完整配置请参考: `ruoyi-admin/src/main/resources/application-dev.yml` 中的 `mqtt` 配置段。

### 3. 发送消息

**方式1: 注入 MqttClientTemplate (推荐)**

```java
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final MqttClientTemplate mqttClientTemplate;

    /**
     * 发送设备状态
     */
    public void sendDeviceStatus(String deviceId, String status) {
        String topic = "/device/" + deviceId + "/status";
        mqttClientTemplate.publish(topic, status.getBytes());
    }

    /**
     * 发送 JSON 数据 (指定 QoS)
     */
    public void sendDeviceData(String deviceId, String jsonData) {
        String topic = "/device/" + deviceId + "/data";
        mqttClientTemplate.publish(topic, jsonData.getBytes(), MqttQoS.QOS1);
    }

    /**
     * 发送保留消息 (新订阅者会立即收到)
     */
    public void sendDeviceConfig(String deviceId, String config) {
        String topic = "/device/" + deviceId + "/config";
        mqttClientTemplate.publish(topic, config.getBytes(), MqttQoS.QOS1, true);
    }
}
```

### 4. 接收消息

**创建全局消息监听器** (处理所有订阅的消息)

```java
package plus.ruoyi.business.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.dromara.mica.mqtt.codec.MqttPublishMessage;
import org.dromara.mica.mqtt.core.client.IMqttClientGlobalMessageListener;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;

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

        log.info("========================================");
        log.info("MQTT 消息接收");
        log.info("Topic: {}", topic);
        log.info("QoS: {}", message.fixedHeader().qosLevel());
        log.info("Payload: {}", payloadStr);
        log.info("========================================");

        // 根据 topic 分发到不同的业务处理逻辑
        if (topic.startsWith("/device/")) {
            handleDeviceMessage(topic, payloadStr);
        } else if (topic.startsWith("/system/")) {
            handleSystemMessage(topic, payloadStr);
        }
    }

    /**
     * 处理设备消息
     */
    private void handleDeviceMessage(String topic, String payload) {
        // TODO: 解析 payload,更新设备状态到数据库
        log.info("📩 处理设备消息 - Topic: {}, Payload: {}", topic, payload);
    }

    /**
     * 处理系统消息
     */
    private void handleSystemMessage(String topic, String payload) {
        // TODO: 处理系统通知
        log.info("📩 处理系统消息 - Topic: {}, Payload: {}", topic, payload);
    }
}
```

### 5. 订阅主题 (推荐配置文件方式)

**推荐方式: 配置文件自动订阅**

```yaml
mqtt:
  client:
    # 全局订阅 (自动订阅,由全局监听器处理)
    global-subscribe:
      - topic: /device/+/status  # + 单级通配符
        qos: QOS1
      - topic: /device/#          # # 多级通配符
        qos: QOS1
```

**动态订阅 (需要提供监听器)**

```java
import org.dromara.mica.mqtt.spring.client.MqttClientTemplate;
import org.dromara.mica.mqtt.core.client.IMqttClientMessageListener;

@Service
@RequiredArgsConstructor
public class MqttSubscribeService {

    private final MqttClientTemplate mqttClientTemplate;

    /**
     * 动态订阅主题 (需要提供监听器)
     */
    public void subscribeDevice(String deviceId) {
        String topic = "/device/" + deviceId + "/status";

        // 创建消息监听器
        IMqttClientMessageListener listener = (context, topic1, message, payload) -> {
            String payloadStr = new String(payload, StandardCharsets.UTF_8);
            log.info("收到消息: topic={}, payload={}", topic1, payloadStr);
        };

        // 订阅主题并指定监听器
        mqttClientTemplate.subQos1(topic, listener);
    }

    /**
     * 取消订阅
     */
    public void unsubscribeDevice(String deviceId) {
        String topic = "/device/" + deviceId + "/status";
        mqttClientTemplate.unSubscribe(topic);  // 支持可变参数
    }
}
```

---

## 📚 核心 API

### MqttClientTemplate (官方 API)

| 方法 | 说明 |
|------|------|
| `publish(topic, payload)` | 发送消息 (默认 QoS 0) |
| `publish(topic, payload, qos)` | 发送消息 (指定 QoS) |
| `publish(topic, payload, qos, retain)` | 发送消息 (完整参数) |
| `subQos0(topic, listener)` | 订阅主题 (QoS 0, 需要监听器) |
| `subQos1(topic, listener)` | 订阅主题 (QoS 1, 需要监听器) |
| `subQos2(topic, listener)` | 订阅主题 (QoS 2, 需要监听器) |
| `unSubscribe(topic...)` | 取消订阅 (可变参数) |
| `isConnected()` | 检查是否已连接 |
| `disconnect()` | 断开连接 |

> **💡 提示**: 订阅方法需要提供 `IMqttClientMessageListener`,推荐使用配置文件的 `global-subscribe` 方式。

### MqttQoS (官方枚举)

| 枚举值 | 等级 | 说明 | 适用场景 |
|--------|------|------|---------|
| `QOS0` | 0 | 最多一次,可能丢失 | 传感器数据采集 |
| `QOS1` | 1 | 至少一次,可能重复 | 大部分业务场景 (推荐) |
| `QOS2` | 2 | 恰好一次,性能较低 | 计费系统、金融交易 |

---

## 🌐 集群部署指南

### 多节点部署问题

在多节点环境部署时,如果不做特殊处理,会出现**消息重复消费**问题:

```
设备发送消息 → MQTT Broker → 多个后端节点都收到相同消息 → 重复处理 ❌
```

**问题影响**:
- 业务逻辑重复执行(如重复发送通知、重复写数据库)
- 浪费服务器资源和网络带宽
- 可能导致数据不一致

### 解决方案：共享订阅（推荐）

**原理**: 使用 MQTT 共享订阅功能,让 Broker 自动负载均衡,每条消息只发给一个节点。

**Broker 支持情况**:
- ✅ **EMQX**: 完美支持 (推荐)
- ✅ **Mosquitto 2.0+**: 支持
- ✅ **HiveMQ**: 支持
- ❌ **Mosquitto 1.x**: 不支持
- ❌ **AWS IoT Core**: 不支持

### 使用共享订阅（零代码方案）

#### 1. 订阅格式

只需在订阅主题时加上 `$share/GroupName/` 前缀:

```java
@Component
@RequiredArgsConstructor
public class MqttSubscriber implements ApplicationRunner {

    private final IMqttClientSession mqttClientSession;

    @Override
    public void run(ApplicationArguments args) {
        // ✅ 共享订阅：多实例自动负载均衡
        mqttClientSession.subscribe("$share/backend-cluster/device/+/status", 1);
        mqttClientSession.subscribe("$share/backend-cluster/sensor/#", 1);

        // 主题格式说明：
        // $share/backend-cluster/device/+/status
        // │      │                 │
        // │      │                 └── 原始主题（支持通配符 + 和 #）
        // │      └───────────────────── 订阅组名称（同组负载均衡）
        // └──────────────────────────── 共享订阅前缀（固定）
    }
}
```

#### 2. 工作原理

```
┌─────────────────────────────────────────┐
│         MQTT Broker (EMQX)              │
│  Topic: device/+/status                 │
└────────────┬────────────────────────────┘
             │
             │ 共享订阅: $share/backend-cluster/device/+/status
             │
    ┌────────┴────────┐
    │  Broker 负载均衡 │ ← 同一订阅组内只有一个实例收到消息
    └────────┬────────┘
             │
    ┌────────┼────────┐
    ▼        ▼        ▼
┌────────┐┌────────┐┌────────┐
│实例 A  ││实例 B  ││实例 C  │
│✅ 接收 ││❌ 不接收││❌ 不接收│
└────────┘└────────┘└────────┘
```

- **同一订阅组内**: 消息由该组的**一个订阅者**接收（Broker 自动负载均衡）
- **不同订阅组**: 消息会被转发到**所有组**
- **支持通配符**: `+` (单级) 和 `#` (多级) 都支持

#### 3. 配置要点

```yaml
mqtt:
  client:
    enabled: true
    ip: 192.168.168.168
    port: 1883

    # ⚠️ 不要配置固定 client-id (让框架自动生成唯一 ID)
    client-id: ${MQTT_CLIENT_ID:}

    # ⚠️ 共享订阅建议使用 clean_session=true
    clean-start: true
```

#### 4. 消息处理器（保持原样）

```java
@Slf4j
@Component
public class MyMqttMessageListener implements IMqttClientGlobalMessageListener {

    @Override
    public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        // 直接处理业务逻辑，Broker 已保证消息不重复
        log.info("收到消息 - Topic: {}, Payload: {}", topic, payloadStr);

        if (topic.startsWith("device/")) {
            handleDeviceMessage(topic, payloadStr);
        }
    }

    private void handleDeviceMessage(String topic, String payload) {
        // 业务逻辑: 解析 JSON、存储数据库、触发流程
    }
}
```

### 优势对比

| 特性 | 共享订阅 `$share` | 普通订阅 |
|------|------------------|---------|
| **代码改动** | ✅ 只改订阅主题 | ❌ 需要额外去重逻辑 |
| **性能** | ✅ Broker 原生支持 | ❌ 所有实例都收到 |
| **延迟** | ✅ 零额外延迟 | ❌ 可能需要队列中转 |
| **复杂度** | ✅ 零配置 | ❌ 需要 Redis 或数据库 |
| **资源消耗** | ✅ 最小化 | ❌ 重复处理浪费 |

### 注意事项

1. **Broker 版本要求**:
   - EMQX: 任意版本
   - Mosquitto: 需要 2.0+
   - 旧版 Mosquitto 不支持共享订阅

2. **持久会话风险**:
   - 若客户端具有持久会话且长期断开连接,内部消息队列可能溢出
   - **建议**: 共享订阅使用 `clean-start: true`

3. **订阅组名称**:
   - 可自定义,建议使用应用名称（如 `backend-cluster`）
   - 同组内的实例会负载均衡,不同组会都收到消息

4. **幂等性**:
   - 虽然 Broker 保证同组只有一个实例收到,但**仍建议业务层做幂等性设计**
   - 原因: 网络重传、客户端重连等可能导致消息重复

---

## 🔧 高级配置

### 遗嘱消息 (Last Will)

当客户端异常断开时,Broker 会自动发送遗嘱消息:

```yaml
mqtt:
  client:
    will-message:
      topic: /system/offline
      message: '{"app":"ruoyi-plus","status":"offline"}'
      qos: QOS1
      retain: false
```

### SSL/TLS 加密连接

```yaml
mqtt:
  client:
    ssl:
      enabled: true
      keystore-path: classpath:/certs/keystore.jks
      keystore-pass: password
      truststore-path: classpath:/certs/truststore.jks
      truststore-pass: password
```

### 多租户隔离

通过 Topic 前缀实现多租户隔离:

```java
String tenantId = TenantHelper.getTenantId();
String topic = "/" + tenantId + "/device/123/status";
mqttClientTemplate.publish(topic, "online".getBytes());
```

### 集成 RocketMQ

MQTT 接收设备消息后,转发到 RocketMQ 做异步处理:

```java
@Override
public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
    String payloadStr = new String(payload, StandardCharsets.UTF_8);

    // 转发到 RocketMQ
    RMSendUtil.asyncSend("device-message", payloadStr);
}
```

---

## 🛠️ 故障排查

### 1. 连接失败: "远程计算机拒绝网络连接"

**原因**: 外部 MQTT Broker 未启动

**解决方案**:
```bash
# 检查 Broker 是否启动
telnet 127.0.0.1 1883

# 或启动 Docker EMQX
docker run -d --name emqx -p 1883:1883 -p 18083:18083 emqx/emqx:latest
```

### 2. 配置后无法连接

- 检查防火墙是否开放 1883 端口
- 检查用户名密码是否正确
- 检查 `enabled: true` 是否生效

### 3. 订阅无效

- 检查主题通配符是否正确 (`+` 是单级, `#` 是多级)
- 检查 QoS 等级格式 (必须是 `QOS0`, `QOS1`, `QOS2`, 不是数字)

### 4. 消息未接收

- 检查是否实现了自定义 `IMqttClientGlobalMessageListener`
- 检查日志中是否有连接断开的警告
- 检查 `global-subscribe` 配置是否正确

---

## 📄 技术栈

- **mica-mqtt 2.5.7**: Dromara 开源高性能 MQTT 客户端
- **MQTT 协议**: v3.1/v3.1.1/v5.0
- **t-io**: 异步非阻塞 IO

---

## 📖 参考文档

- mica-mqtt 官方文档: https://gitee.com/dromara/mica-mqtt
- MQTT 协议规范: https://mqtt.org/
- Dromara 社区: https://dromara.org/
- EMQX 官网: https://www.emqx.io/

---

## ✨ 贡献者

- **抓蛙师** - 初始封装 (2025-11-24)

---

## 📄 许可证

本模块遵循项目根目录的许可证。
