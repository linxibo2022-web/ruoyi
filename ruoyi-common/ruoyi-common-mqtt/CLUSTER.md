# MQTT 集群部署指南

本文档说明如何在多节点环境下部署 MQTT 客户端，使用**共享订阅**避免消息重复消费问题。

---

## 📊 问题背景

### 单节点部署（正常）

```
设备发送消息 → MQTT Broker → 后端服务节点 → 处理消息 ✅
```

### 多节点部署（问题）

```
                   ┌─────────────┐
设备发送消息 → MQTT Broker
                   └──────┬──────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
      ┌───▼────┐      ┌───▼────┐      ┌───▼────┐
      │ Node 1 │      │ Node 2 │      │ Node 3 │
      │ 处理消息│      │ 处理消息│      │ 处理消息│
      └────────┘      └────────┘      └────────┘
         ❌             ❌             ❌
```

**问题**：
- ❌ 每个节点都收到相同的消息
- ❌ 消息被重复处理 3 次
- ❌ 导致业务逻辑重复执行（如重复发送通知、重复写数据库）

---

## ✅ 解决方案：共享订阅

### 什么是共享订阅？

共享订阅允许多个客户端组成一个组，共同订阅一个主题，**每条消息只会被组内的一个客户端接收**。

```
                   ┌─────────────┐
设备发送消息 → MQTT Broker (自动负载均衡)
                   └──────┬──────┘
                          │ 只发给一个节点
          ┌───────────────┼───────────────┐
          │               ❌              ❌
      ┌───▼────┐      ┌────────┐      ┌────────┐
      │ Node 1 │      │ Node 2 │      │ Node 3 │
      │ 处理消息│      │ 等待   │      │ 等待   │
      └────────┘      └────────┘      └────────┘
         ✅
```

### 配置步骤

#### ✅ 检查 MQTT Broker 是否支持

| Broker | MQTT 5.0 | MQTT 3.1.1 扩展 |
|--------|----------|----------------|
| **EMQX** | ✅ 支持 | ✅ 支持 `$share/` |
| **Mosquitto 2.0+** | ✅ 支持 | ✅ 支持 `$share/` |
| **HiveMQ** | ✅ 支持 | ✅ 支持 `$share/` |
| **Azure IoT Hub** | ✅ 支持 | ❌ 不支持 |
| **AWS IoT Core** | ❌ 不支持 | ❌ 不支持 |

> 💡 **推荐**：使用 EMQX 或 Mosquitto 2.0+

#### ✅ 修改配置文件

`application-dev.yml`：

```yaml
mqtt:
  client:
    enabled: true
    ip: 192.168.168.168
    port: 1883

    # ⚠️ 不要配置固定 client-id（让框架自动生成）
    # client-id: fixed-id  # ❌ 错误！会导致节点冲突

    # ✅ 使用共享订阅
    global-subscribe:
      # 格式：$share/{GroupName}/{Topic}
      - topic: $share/backend-cluster/device/+/status
        qos: QOS1
      - topic: $share/backend-cluster/sensor/#
        qos: QOS1
```

**主题格式说明**：

```
$share/backend-cluster/device/+/status
│      │                │
│      │                └── 实际主题（支持通配符）
│      └─────────────────── 订阅组名称
└────────────────────────── 共享订阅标识
```

#### ✅ 验证效果

启动 3 个节点后，发送测试消息：

```bash
# 使用 mosquitto_pub 发送消息
mosquitto_pub -h 192.168.168.168 -t "device/001/status" -m '{"status":"online"}'
```

**预期结果**：
- ✅ 只有 1 个节点打印处理日志
- ✅ 其他节点不会收到消息
- ✅ 多次发送消息会在节点间轮询分配

---

### 共享订阅 vs 普通订阅

```yaml
# ✅ 共享订阅（推荐用于集群）
global-subscribe:
  - topic: $share/backend-cluster/device/+/status
    qos: QOS1

# ❌ 普通订阅（会导致重复消费）
global-subscribe:
  - topic: device/+/status  # 所有节点都会收到
    qos: QOS1
```

---

### 混合使用场景

如果部分消息需要广播（所有节点都处理），部分消息需要负载均衡（只有一个节点处理）：

```yaml
global-subscribe:
  # ✅ 共享订阅 - 设备数据（负载均衡）
  - topic: $share/backend-cluster/device/+/data
    qos: QOS1

  # ✅ 普通订阅 - 系统广播（所有节点接收）
  - topic: /system/broadcast
    qos: QOS1
```

---

## 🔧 故障排查

### 问题 1：多节点互相踢线

**现象**：
```
Node 1 连接成功
Node 2 连接成功，Node 1 断开
Node 3 连接成功，Node 2 断开
```

**原因**：配置了固定的 `client-id`

**解决**：
```yaml
# ❌ 错误配置
client-id: my-fixed-client-id

# ✅ 正确配置（留空，让框架自动生成）
client-id: ${MQTT_CLIENT_ID:}
```

---

### 问题 2：共享订阅不生效

**现象**：所有节点都收到消息

**排查步骤**：

1. **检查 Broker 是否支持共享订阅**：
   ```bash
   # EMQX 检查
   docker exec emqx emqx ctl broker | grep shared_subscription

   # Mosquitto 检查版本（需要 2.0+）
   mosquitto -h | grep version
   ```

2. **检查主题格式**：
   ```yaml
   # ❌ 错误：缺少 $share/ 前缀
   topic: backend-cluster/device/+/status

   # ✅ 正确
   topic: $share/backend-cluster/device/+/status
   ```

3. **检查 MQTT 协议版本**：
   ```yaml
   mqtt:
     client:
       version: MQTT_5  # ✅ 推荐使用 MQTT 5.0
   ```

---

## 📝 最佳实践

### 1. 推荐配置（生产环境）

```yaml
mqtt:
  client:
    enabled: true
    name: RuoYi-Plus-Backend
    ip: ${MQTT_BROKER_HOST}
    port: 1883
    username: ${MQTT_USERNAME}
    password: ${MQTT_PASSWORD}

    # ✅ 不配置 client-id（自动生成）
    client-id: ${MQTT_CLIENT_ID:}

    # ✅ 使用 MQTT 5.0
    version: MQTT_5

    # ✅ 自动重连
    reconnect: true
    re-interval: 5000

    # ✅ 共享订阅
    global-subscribe:
      - topic: $share/backend-cluster/device/+/status
        qos: QOS1
      - topic: $share/backend-cluster/sensor/#
        qos: QOS1

    # ✅ 遗嘱消息（节点下线通知）
    will-message:
      topic: /system/node/offline
      message: backend-node-offline
      qos: QOS1
      retain: false
```

---

### 2. QoS 选择建议

| 场景 | 推荐 QoS | 原因 |
|------|----------|------|
| 高频传感器数据 | QoS 0 | 允许丢失，性能最好 |
| 设备状态上报 | QoS 1 | 至少送达一次，推荐 |
| 支付/订单消息 | QoS 2 | 恰好一次，无重复 |

---

### 3. 订阅组命名规范

```
$share/{AppName}-{Module}-cluster/{Topic}
```

**示例**：
- `$share/ruoyiplus-backend-cluster/device/+/status`
- `$share/ruoyiplus-iot-cluster/sensor/#`
- `$share/ruoyiplus-notify-cluster/alert/+/event`

**好处**：
- 不同应用/模块互不影响
- 易于监控和管理

---

### 4. 监控指标

**关键指标**：
- 每个节点的消息接收数（应该大致相等）
- MQTT 连接状态（是否频繁断开重连）
- 消息处理延迟

**监控示例**：
```java
@Component
public class MqttMetrics {

    private final AtomicLong messageCount = new AtomicLong(0);

    @Scheduled(fixedRate = 60000)
    public void reportMetrics() {
        log.info("MQTT 消息处理数: {}", messageCount.get());
        // 可以上报到 Prometheus/Grafana
    }
}
```

---

## 🎯 总结

### 集群部署核心要点

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| **client-id** | 留空（自动生成） | 避免节点冲突 |
| **订阅方式** | 共享订阅 `$share/GroupName/Topic` | 自动负载均衡 |
| **MQTT 版本** | MQTT_5 | 性能最佳 |
| **QoS** | QOS1 | 大部分场景推荐 |
| **Broker** | EMQX / Mosquitto 2.0+ | 原生支持共享订阅 |

### 性能特点

- ✅ **零代码**：共享订阅由 Broker 原生支持，无需额外开发
- ✅ **高性能**：每条消息只发给一个节点，节省带宽
- ✅ **低延迟**：Broker 级别的负载均衡，无额外开销
- ✅ **易运维**：配置简单，与单节点部署几乎一致

---

**强烈推荐**：优先使用共享订阅方案！
