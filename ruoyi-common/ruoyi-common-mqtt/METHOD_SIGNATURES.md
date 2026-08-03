# MqttClientTemplate 方法签名完整列表

## 📌 重要说明

通过深度分析 `mica-mqtt 2.5.7` 源码,以下是 `MqttClientTemplate` 的**正确方法签名**。

> ⚠️ **常见错误**: 订阅方法 `subQos0/subQos1/subQos2` **必须提供** `IMqttClientMessageListener` 参数!

---

## ✅ 发布消息 (Publish)

### 1. 发送普通消息

```java
// 默认 QoS 0
boolean publish(String topic, Object payload)

// 示例
mqttClientTemplate.publish("/test/topic", "Hello".getBytes());
```

### 2. 指定 QoS

```java
// 指定 QoS
boolean publish(String topic, Object payload, MqttQoS qos)

// 示例
mqttClientTemplate.publish("/test/topic", "Hello".getBytes(), MqttQoS.QOS1);
```

### 3. 发送保留消息

```java
// 指定保留标志
boolean publish(String topic, Object payload, boolean retain)

// 示例
mqttClientTemplate.publish("/test/topic", "Hello".getBytes(), true);
```

### 4. 完整参数

```java
// QoS + 保留标志
boolean publish(String topic, Object payload, MqttQoS qos, boolean retain)

// 示例
mqttClientTemplate.publish("/test/topic", "Hello".getBytes(), MqttQoS.QOS1, true);
```

### 5. 带属性 (MQTT 5.0)

```java
// QoS + 保留 + MQTT 属性
boolean publish(String topic, Object payload, MqttQoS qos, boolean retain, MqttProperties properties)

// 示例
MqttProperties props = new MqttProperties();
mqttClientTemplate.publish("/test/topic", "Hello".getBytes(), MqttQoS.QOS1, false, props);
```

---

## ✅ 订阅主题 (Subscribe)

> **⚠️ 关键**: 所有订阅方法都需要提供 `IMqttClientMessageListener` 参数!

### 1. 订阅 QoS 0

```java
// ✅ 正确签名
MqttClient subQos0(String topicFilter, IMqttClientMessageListener listener)

// ❌ 错误调用 (缺少 listener)
mqttClientTemplate.subQos0("/device/+/status");  // 编译错误!

// ✅ 正确调用
IMqttClientMessageListener listener = (context, topic, message, payload) -> {
    log.info("收到消息: {}", new String(payload, StandardCharsets.UTF_8));
};
mqttClientTemplate.subQos0("/device/+/status", listener);
```

### 2. 订阅 QoS 1

```java
// ✅ 正确签名
MqttClient subQos1(String topicFilter, IMqttClientMessageListener listener)

// ✅ 正确调用
mqttClientTemplate.subQos1("/device/+/status", listener);
```

### 3. 订阅 QoS 2

```java
// ✅ 正确签名
MqttClient subQos2(String topicFilter, IMqttClientMessageListener listener)

// ✅ 正确调用
mqttClientTemplate.subQos2("/device/+/status", listener);
```

### 4. 通用订阅方法

```java
// 方式1: QoS + Topic + Listener
MqttClient subscribe(MqttQoS mqttQoS, String topicFilter, IMqttClientMessageListener listener)

// 方式2: Topic + QoS + Listener
MqttClient subscribe(String topicFilter, MqttQoS mqttQoS, IMqttClientMessageListener listener)

// 方式3: Topic + QoS + Listener + Properties (MQTT 5.0)
MqttClient subscribe(String topicFilter, MqttQoS mqttQoS, IMqttClientMessageListener listener, MqttProperties properties)
```

### 5. 批量订阅

```java
// 多主题订阅
MqttClient subscribe(String[] topicFilters, MqttQoS mqttQoS, IMqttClientMessageListener listener)

// 示例
String[] topics = {"/device/+/status", "/device/+/data"};
mqttClientTemplate.subscribe(topics, MqttQoS.QOS1, listener);
```

### 6. 订阅列表

```java
// 订阅对象列表
MqttClient subscribe(List<MqttClientSubscription> subscriptionList)
MqttClient subscribe(List<MqttClientSubscription> subscriptionList, MqttProperties properties)
```

---

## ✅ 取消订阅 (Unsubscribe)

### 1. 取消单个或多个主题

```java
// 可变参数
MqttClient unSubscribe(String... topicFilters)

// 示例
mqttClientTemplate.unSubscribe("/device/+/status");  // 单个
mqttClientTemplate.unSubscribe("/topic1", "/topic2", "/topic3");  // 多个
```

### 2. 取消主题列表

```java
// List 参数
MqttClient unSubscribe(List<String> topicFilters)

// 示例
List<String> topics = Arrays.asList("/topic1", "/topic2");
mqttClientTemplate.unSubscribe(topics);
```

---

## ✅ 连接管理

### 1. 检查连接状态

```java
// 是否已连接
boolean isConnected()

// 是否已断开
boolean isDisconnected()

// 示例
if (mqttClientTemplate.isConnected()) {
    log.info("MQTT 已连接");
}
```

### 2. 重连

```java
// 重连到当前 Broker
void reconnect()

// 重连到新的 Broker
boolean reconnect(String ip, int port)

// 示例
mqttClientTemplate.reconnect();
mqttClientTemplate.reconnect("192.168.1.100", 1883);
```

### 3. 断开连接

```java
// 断开连接
boolean disconnect()

// 示例
mqttClientTemplate.disconnect();
```

---

## ✅ 定时任务

```java
// 添加定时任务
TimerTask schedule(Runnable command, long delay)
TimerTask schedule(Runnable command, long delay, Executor executor)

// 添加一次性定时任务
TimerTask scheduleOnce(Runnable command, long delay)
TimerTask scheduleOnce(Runnable command, long delay, Executor executor)
```

---

## ✅ 获取底层对象

```java
// 获取 MqttClient
MqttClient getMqttClient()

// 获取 TioClient
TioClient getTioClient()

// 获取 TioClientConfig
TioClientConfig getClientTioConfig()

// 获取 ClientChannelContext
ClientChannelContext getContext()

// 获取 MqttClientCreator
MqttClientCreator getClientCreator()
```

---

## 💡 推荐使用方式

### ✅ 推荐: 配置文件订阅 (自动使用全局监听器)

```yaml
mqtt:
  client:
    # 全局订阅 (自动订阅,由 IMqttClientGlobalMessageListener 处理)
    global-subscribe:
      - topic: /device/+/status
        qos: QOS1
      - topic: /device/#
        qos: QOS1
```

**优势**:
- ✅ 无需手动创建监听器
- ✅ 自动订阅,启动即生效
- ✅ 统一由全局监听器处理
- ✅ 配置集中管理

### ⚠️ 仅在特殊场景使用动态订阅

动态订阅适用于:
- 运行时根据业务动态订阅主题
- 需要为不同主题提供不同监听器

---

## 📖 参考资料

- **完整示例**: `MqttClientUsageTest.java`
- **详细文档**: `README.md`
- **快速参考**: `USAGE.md`
- **官方源码**: `D:\desktop\my\study\mica-mqtt\starter\mica-mqtt-client-spring-boot-starter\src\main\java\org\dromara\mica\mqtt\spring\client\MqttClientTemplate.java`
