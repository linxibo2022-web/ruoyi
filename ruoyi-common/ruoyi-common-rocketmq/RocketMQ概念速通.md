# RocketMQ概念 5分钟速通

## 📚 一、生产者组名 vs 消费者组名

### 🔵 生产者组（Producer Group）

**作用**：
- **事务回查标识**：主要用于事务消息的回查
- **发送失败重试**：在集群模式下，如果某个生产者实例宕机，其他同组生产者可以接管重试
- **负载均衡标识**：同一个生产者组的实例共享负载

**特点**：
```java
// 配置
private String group = "default-producer-group";

// 实际使用场景
Producer producer1 = new Producer("order-service-producer");
Producer producer2 = new Producer("order-service-producer");  // 同组
```

**关键点**：
- ✅ 一般一个应用使用一个生产者组名
- ✅ 组名相同的生产者可以互相接管事务回查
- ✅ 对普通消息发送影响不大（主要影响事务消息）

---

### 🟢 消费者组（Consumer Group）

**作用**：
- **消息消费进度管理**：同组消费者共享消费进度
- **负载均衡**：同组多个消费者实例之间负载均衡消费消息
- **消息重复消费**：不同组的消费者可以独立消费同一条消息

**特点**：
```java
// 不同消费者组，可以消费相同的消息
@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "order-service-consumer")
public class OrderConsumer1 {}

@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "inventory-service-consumer")
public class OrderConsumer2 {}  // 不同组，都能收到消息

// 同一消费者组，负载均衡消费
@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "order-service-consumer")
public class OrderConsumer3 {}  // 同组，与 OrderConsumer1 负载均衡
```

**关键点**：
- ✅ **不同组可以重复消费**：订单服务和库存服务可以同时消费同一条消息
- ✅ **同组负载均衡**：同一组的多个实例分摊消费压力
- ✅ **消费进度独立**：每个组的消费进度独立管理

---

### 📊 对比表格

| 对比项 | 生产者组 | 消费者组 |
|--------|---------|---------|
| **主要作用** | 事务回查标识 | 消息消费负载均衡 |
| **组内实例关系** | 互为备份（事务） | 负载均衡消费 |
| **是否共享进度** | 不涉及进度 | ✅ 共享消费进度 |
| **是否重复消费** | 不涉及 | ✅ 不同组独立消费 |
| **典型命名** | `order-service-producer` | `order-service-consumer`<br>`inventory-service-consumer` |
| **重要程度** | 中等（主要影响事务） | 🔥 **非常重要**（影响消费行为） |

---

## 🎯 二、生产者与消费者的解耦设计

### 核心问题：生产者发送消息需要指定消费者组名吗？

**答案：不需要！** 这是 RocketMQ 发布-订阅模式的核心设计。

---

### 🔵 生产者发送消息时

**只需要指定**：
```java
// 生产者发送消息
rocketMQTemplate.syncSend(
    "ORDER_TOPIC",           // ✅ Topic（必需）
    orderMessage             // ✅ 消息内容（必需）
);

// 或者带 Tag
rocketMQTemplate.syncSend(
    "ORDER_TOPIC:CREATE",    // ✅ Topic:Tag
    orderMessage
);
```

**不需要知道**：
- ❌ 消费者组名
- ❌ 谁会消费这条消息
- ❌ 有多少个消费者

### 🟢 消费者消费消息时

**需要指定**：
```java
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",              // ✅ 订阅的 Topic
    consumerGroup = "order-consumer"    // ✅ 自己的消费者组名
)
public class OrderConsumer implements RocketMQListener<OrderMessage> {
    @Override
    public void onMessage(OrderMessage message) {
        // 处理消息
    }
}
```

---

### 📊 消息流转示意图

```
生产者 A                     Broker (Topic)                消费者
  │                              │                           │
  │  发送消息到 ORDER_TOPIC       │                           │
  ├──────────────────────────────>│                           │
  │  (不关心谁来消费)             │                           │
  │                              │                           │
  │                              │   订阅 ORDER_TOPIC         │
  │                              │<──────────────────────────┤
  │                              │   consumerGroup: order-consumer
  │                              │                           │
  │                              │   拉取消息                 │
  │                              ├──────────────────────────>│
  │                              │                           │
  │                              │   订阅 ORDER_TOPIC         │
  │                              │<──────────────────────────┤
  │                              │   consumerGroup: inventory-consumer
  │                              │                           │
  │                              │   拉取消息                 │
  │                              ├──────────────────────────>│
```

**关键点**：
- 生产者只把消息发送到 **Topic**
- Broker 存储消息
- 多个消费者组可以**独立订阅**同一个 Topic
- 消费者组名是消费者自己的标识，与生产者无关

---

### 💡 实际场景：订单创建事件

```java
// ============ 生产者（订单服务）============
@Service
public class OrderService {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void createOrder(Order order) {
        // 保存订单
        orderDao.save(order);

        // 发送订单创建消息
        rocketMQTemplate.syncSend(
            "ORDER_CREATED_TOPIC",   // 只需要指定 Topic
            order
        );
        // ❌ 不需要知道谁会消费
        // ❌ 不需要指定消费者组名
    }
}

// ============ 消费者1（库存服务）============
@Component
@RocketMQMessageListener(
    topic = "ORDER_CREATED_TOPIC",
    consumerGroup = "inventory-service-consumer"  // 库存服务的组名
)
public class InventoryConsumer implements RocketMQListener<Order> {
    @Override
    public void onMessage(Order order) {
        // 扣减库存
        inventoryService.deduct(order);
    }
}

// ============ 消费者2（物流服务）============
@Component
@RocketMQMessageListener(
    topic = "ORDER_CREATED_TOPIC",
    consumerGroup = "logistics-service-consumer"  // 物流服务的组名
)
public class LogisticsConsumer implements RocketMQListener<Order> {
    @Override
    public void onMessage(Order order) {
        // 创建物流单
        logisticsService.create(order);
    }
}

// ============ 消费者3（积分服务）============
@Component
@RocketMQMessageListener(
    topic = "ORDER_CREATED_TOPIC",
    consumerGroup = "points-service-consumer"  // 积分服务的组名
)
public class PointsConsumer implements RocketMQListener<Order> {
    @Override
    public void onMessage(Order order) {
        // 赠送积分
        pointsService.add(order);
    }
}
```

**结果**：
- ✅ 订单服务只管发送消息到 `ORDER_CREATED_TOPIC`
- ✅ 3个消费者**独立订阅**同一个 Topic
- ✅ 每个消费者都能收到消息
- ✅ 每个消费者有自己的消费者组名
- ✅ 生产者完全不知道有这些消费者存在

---

### 🔑 发布-订阅模式

RocketMQ 使用的是**发布-订阅（Pub-Sub）模式**：

```
发布者（生产者）                    订阅者（消费者）
     │                                │
     ├─ 发布到 Topic ──┐              │
     │                 │              │
     │            ┌────▼────┐         │
     │            │  Topic  │         │
     │            │  Broker │         │
     │            └────┬────┘         │
     │                 │              │
     │                 ├──────────────┼─ 订阅者1（组A）
     │                 ├──────────────┼─ 订阅者2（组B）
     │                 └──────────────┼─ 订阅者3（组C）
```

**特点**：
- 生产者和消费者**完全解耦**
- 生产者不知道消费者的存在
- 消费者可以动态增加/减少
- 通过 **Topic** 关联，而不是通过组名

---

### 📋 参数对比

#### 生产者发送消息需要的参数

```java
rocketMQTemplate.syncSend(
    destination,    // Topic 或 Topic:Tag  ✅ 必需
    message,        // 消息内容           ✅ 必需
    timeout         // 超时时间（可选）
);
```

#### 消费者订阅消息需要的参数

```java
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",        // ✅ 订阅的 Topic（必需）
    consumerGroup = "my-consumer", // ✅ 消费者组名（必需）
    selectorExpression = "*",      // Tag 过滤表达式（可选）
    messageModel = MessageModel.CLUSTERING  // 消息模式（可选）
)
```

---

### ❓ 常见误解

#### ❌ 误解1：生产者需要指定消费者组名
```java
// ❌ 错误想法
rocketMQTemplate.syncSend(
    "ORDER_TOPIC",
    message,
    "order-consumer"  // ← 不存在这个参数！
);
```

#### ✅ 正确理解
```java
// ✅ 正确做法
rocketMQTemplate.syncSend("ORDER_TOPIC", message);

// 消费者自己指定组名
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",
    consumerGroup = "order-consumer"  // ← 消费者自己的标识
)
```

---

#### ❌ 误解2：一个 Topic 只能有一个消费者组
```java
// ❌ 错误想法：以为只能有一个消费者
@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "consumer1")
```

#### ✅ 正确理解
```java
// ✅ 同一个 Topic 可以被多个消费者组订阅
@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "consumer1")
@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "consumer2")
@RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "consumer3")
// 三个消费者组都能收到消息
```

---

### ✅ 解耦设计总结

| 问题 | 答案 |
|------|------|
| **生产者发送消息需要指定消费者组名吗？** | ❌ **不需要** |
| **生产者只需要指定什么？** | ✅ Topic（和可选的 Tag） |
| **消费者组名是谁指定的？** | ✅ 消费者自己指定 |
| **生产者和消费者如何关联？** | ✅ 通过 **Topic** 关联 |
| **一个 Topic 能有多个消费者组吗？** | ✅ 可以，每个组独立消费 |
| **生产者知道有哪些消费者吗？** | ❌ 不知道，完全解耦 |

**核心原则**：
- 🎯 生产者：我只管往 Topic 发消息
- 🎯 消费者：我指定自己的组名，订阅 Topic 消费消息
- 🎯 解耦：生产者和消费者通过 Topic 关联，互不感知

---

## 🔄 三、客户端注册机制

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        NameServer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  路由信息表                                            │   │
│  │  - Broker 列表                                        │   │
│  │  - Topic 路由信息                                     │   │
│  │  - 生产者/消费者信息                                   │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
         ↑ 心跳注册           ↑ 心跳注册           ↑ 路由查询
         │ (30秒一次)         │ (30秒一次)         │ (30秒一次)
         │                   │                   │
    ┌────┴────┐         ┌────┴────┐         ┌────┴────┐
    │  Broker │         │ Producer│         │ Consumer│
    │         │────────→│         │────────→│         │
    │  存储   │  拉消息   │  发消息 │  推消息   │  消费   │
    └─────────┘         └─────────┘         └─────────┘
```

---

### 🎯 注册流程详解

#### 1️⃣ **Broker 注册到 NameServer**

**时机**：Broker 启动时

```java
// Broker 启动时（RocketMQServerRunner.java）
brokerController.start();  // 触发注册

// 注册内容：
{
    "brokerName": "base-broker",
    "clusterName": "base-cluster",
    "brokerAddr": "127.0.0.1:10912",
    "haServerAddr": "127.0.0.1:10912"
}

// 心跳维持：每 30 秒向所有 NameServer 发送心跳
// 超时时间：120 秒（4个心跳周期）没有心跳，NameServer 剔除该 Broker
```

**作用**：
- ✅ 告诉 NameServer "我是 Broker，我的地址是..."
- ✅ 告诉 NameServer "我有哪些 Topic 和 Queue"
- ✅ 定期心跳保持在线状态

---

#### 2️⃣ **Producer 注册流程**

**时机**：Producer 启动时

```java
// Producer 启动
DefaultMQProducer producer = new DefaultMQProducer("order-producer-group");
producer.setNamesrvAddr("127.0.0.1:9876");
producer.start();  // ← 触发注册流程

// 注册流程：
// 1. 连接 NameServer，获取 Broker 列表
// 2. 创建到 Broker 的网络连接
// 3. 向 Broker 注册自己（包含组名、客户端ID等）
// 4. 定期更新 Topic 路由信息（30秒）
```

**注册信息**：
```json
{
    "producerGroup": "order-producer-group",
    "clientID": "127.0.0.1@12345",  // IP@进程ID
    "language": "JAVA",
    "version": "5.3.0"
}
```

**心跳机制**：
- 每 **30 秒** 向 NameServer 拉取最新的 Broker 和 Topic 路由信息
- 每 **30 秒** 向所有 Broker 发送心跳
- Broker 超过 **120 秒** 没收到心跳，会剔除该 Producer

---

#### 3️⃣ **Consumer 注册流程**

**时机**：Consumer 启动时

```java
// Consumer 启动
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",
    consumerGroup = "order-consumer-group"
)
public class OrderConsumer implements RocketMQListener<OrderMessage> {
    // 容器启动时自动注册
}

// 注册流程：
// 1. 连接 NameServer，获取 Broker 列表
// 2. 向 Broker 注册消费者信息
// 3. 向 Broker 订阅 Topic
// 4. 获取 Queue 分配结果（负载均衡）
// 5. 开始拉取消息
```

**注册信息**：
```json
{
    "consumerGroup": "order-consumer-group",
    "clientID": "127.0.0.1@12346",
    "subscriptionData": {
        "ORDER_TOPIC": {
            "subString": "*",  // 订阅所有 Tag
            "classFilterMode": false
        }
    },
    "consumeType": "CONSUME_ACTIVELY",  // 主动拉取
    "messageModel": "CLUSTERING",       // 集群模式（负载均衡）
    "consumeFromWhere": "CONSUME_FROM_LAST_OFFSET"
}
```

**心跳和重平衡**：
- 每 **30 秒** 向 NameServer 拉取路由信息
- 每 **30 秒** 向 Broker 发送心跳
- 每 **20 秒** 进行一次负载均衡（Rebalance）
- Broker 超过 **120 秒** 没收到心跳，剔除该 Consumer 并触发重平衡

---

### 🔄 完整注册时序图

```
应用启动
  │
  ├─→ Broker 启动
  │     ├─→ 连接 NameServer
  │     ├─→ 注册 Broker 信息
  │     └─→ 每30秒心跳
  │
  ├─→ Producer 启动
  │     ├─→ 查询 NameServer 获取 Broker 列表
  │     ├─→ 连接 Broker
  │     ├─→ 向 Broker 注册 Producer
  │     └─→ 每30秒拉取路由 + 心跳
  │
  └─→ Consumer 启动
        ├─→ 查询 NameServer 获取 Broker 列表
        ├─→ 连接 Broker
        ├─→ 向 Broker 注册 Consumer
        ├─→ 订阅 Topic
        ├─→ 负载均衡获取 Queue 分配
        ├─→ 开始拉取消息
        └─→ 每30秒拉取路由 + 心跳 + 每20秒重平衡
```

---

## 🎯 关键时间参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| **心跳间隔** | 30秒 | 客户端向 NameServer/Broker 发送心跳 |
| **心跳超时** | 120秒 | 超过此时间没心跳，认为客户端下线 |
| **路由更新** | 30秒 | Producer/Consumer 更新路由信息 |
| **Rebalance** | 20秒 | Consumer 重新分配 Queue |
| **Broker注册** | 30秒 | Broker 向 NameServer 注册/更新 |

---

## 💡 实际应用场景

### 场景1：消费者组负载均衡

```yaml
# 订单服务集群（3个实例）
订单服务实例1:
  consumerGroup: order-service-consumer
  订阅: ORDER_TOPIC

订单服务实例2:
  consumerGroup: order-service-consumer  # 同组
  订阅: ORDER_TOPIC

订单服务实例3:
  consumerGroup: order-service-consumer  # 同组
  订阅: ORDER_TOPIC

# 结果：3个实例负载均衡消费，每条消息只被消费一次
# Queue 分配示例：
# 实例1: Queue0, Queue1
# 实例2: Queue2, Queue3
# 实例3: Queue4, Queue5
```

### 场景2：多个服务独立消费

```yaml
# 订单服务
订单服务:
  consumerGroup: order-service-consumer
  订阅: ORDER_CREATED_TOPIC

# 库存服务
库存服务:
  consumerGroup: inventory-service-consumer  # 不同组
  订阅: ORDER_CREATED_TOPIC

# 物流服务
物流服务:
  consumerGroup: logistics-service-consumer  # 不同组
  订阅: ORDER_CREATED_TOPIC

# 结果：一条订单创建消息，三个服务都能收到（独立消费）
```

---

## 📦 项目配置示例

### ruoyi-common-rocketmq 默认配置

```java
// RocketMQProperties.java
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMQProperties {
    private Boolean enabled = false;
    private String nameServer = "127.0.0.1:9876";

    // 生产者配置
    public static class Producer {
        private String group = "default-producer-group";
        private Integer sendMsgTimeout = 3000;
        private Integer maxMessageSize = 4194304; // 4MB
        private Integer retryTimesWhenSendFailed = 2;
    }

    // 消费者配置
    public static class Consumer {
        private Integer consumeThreadMin = 20;
        private Integer consumeThreadMax = 64;
        private Integer pullBatchSize = 32;
        private Integer maxReconsumeTimes = 16;
    }
}
```

### 主应用覆盖配置

```yaml
# application.yml
rocketmq:
  enabled: true                                    # 启用 RocketMQ
  name-server: 192.168.1.100:9876                 # 覆盖 NameServer 地址
  producer:
    group: my-producer-group                       # 覆盖生产者组名
    send-msg-timeout: 5000                         # 覆盖发送超时时间
    retry-times-when-send-failed: 5                # 覆盖重试次数
  consumer:
    consume-thread-min: 10                         # 覆盖最小消费线程数
    consume-thread-max: 128                        # 覆盖最大消费线程数
```

**配置覆盖优先级**（从高到低）：
1. **命令行参数** ← 最高优先级
2. **application.yml 配置** ← yml 配置覆盖
3. **Java 类默认值** ← 最低优先级（被覆盖）

---

## ✅ 核心要点总结

### 关于组名
1. **生产者组名**：主要用于事务回查，一般一个应用一个组名
2. **消费者组名**：🔥 **非常重要**，决定了消息的消费行为（负载均衡/重复消费）
3. **同组消费者**：负载均衡消费，共享消费进度
4. **不同组消费者**：独立消费，可以重复消费同一条消息

### 关于注册
1. **Broker 注册**：启动时向 NameServer 注册，30秒心跳，120秒超时
2. **Producer 注册**：启动时从 NameServer 获取 Broker 列表，向 Broker 注册
3. **Consumer 注册**：启动时注册并订阅 Topic，20秒重平衡，30秒心跳
4. **高可用保证**：心跳机制 + 自动重平衡 + 故障剔除

### 关于配置
1. **默认值**：在 `RocketMQProperties.java` 中定义
2. **覆盖机制**：yml 配置可以完全覆盖默认值
3. **启用条件**：必须设置 `rocketmq.enabled=true`
4. **部分覆盖**：只需配置需要修改的属性，其他使用默认值

---

## 🚀 快速上手

### 1. 启动 RocketMQ Server

```bash
# 使用 Spring Boot 内嵌方式
java -jar ruoyi-rocketmq-server.jar

# 或使用 Docker Compose
cd ruoyi-extend/ruoyi-rocketmq-server
docker-compose up -d
```

### 2. 配置客户端

```yaml
# application.yml
rocketmq:
  enabled: true
  name-server: 127.0.0.1:9876
  producer:
    group: my-app-producer
```

### 3. 发送消息

```java
@Autowired
private RocketMQTemplate rocketMQTemplate;

// 同步发送
rocketMQTemplate.syncSend("ORDER_TOPIC", message);

// 异步发送
rocketMQTemplate.asyncSend("ORDER_TOPIC", message, callback);
```

### 4. 消费消息

```java
@Component
@RocketMQMessageListener(
    topic = "ORDER_TOPIC",
    consumerGroup = "my-app-consumer"
)
public class OrderConsumer implements RocketMQListener<OrderMessage> {
    @Override
    public void onMessage(OrderMessage message) {
        // 处理消息
        System.out.println("收到订单: " + message);
    }
}
```

