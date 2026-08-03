---
name: message-queue
description: |
  当需要使用 RocketMQ 消息队列进行异步通信、系统解耦、削峰填谷时自动使用此 Skill。

  触发场景：
  - 需要发送异步消息（同步/异步/单向/延迟/事务消息）
  - 需要实现消息消费者监听处理
  - 需要管理 Topic（创建/删除/查询/验证路由）
  - 需要延迟消息实现定时业务（订单超时取消等）
  - 需要事务消息保证分布式数据一致性
  - 需要排查 RocketMQ 连接和路由问题

  触发词：RocketMQ、消息队列、MQ、异步消息、延迟消息、事务消息、RMSendUtil、RMTopicUtil、DelayLevel、Topic、消费者、生产者、削峰填谷、系统解耦、sendAsync、sendDelay、sendTransaction、RocketMQMessageListener
---

# 消息队列（RocketMQ）开发指南

## 概述

本项目通过 `ruoyi-common-rocketmq` 模块封装 Apache RocketMQ，提供简化的消息发送、Topic 管理和故障诊断能力。模块默认禁用（`rocketmq.enabled=false`），仅在高并发、系统解耦、事务消息等场景按需启用。

**何时使用 RocketMQ（而非 Redis）**：

| 场景 | 推荐方案 | 原因 |
|------|---------|------|
| 简单异步任务 | Redis Streams | 轻量、无需额外部署 |
| 延迟队列（简单） | RedissonDelayedQueue | 已内置、配置简单 |
| 高吞吐削峰填谷 | **RocketMQ** | 持久化、高可用、百万级 TPS |
| 事务消息 | **RocketMQ** | 原生支持半消息+回查 |
| 顺序消息 | **RocketMQ** | 支持全局/分区有序 |
| 系统解耦（多消费者） | **RocketMQ** | 支持广播/集群消费模式 |
| 消息回溯 | **RocketMQ** | 支持按时间戳回溯 |

---

## 模块结构

```
ruoyi-common/ruoyi-common-rocketmq/
└── src/main/java/plus/ruoyi/common/rocketmq/
    ├── config/
    │   ├── RocketMQProperties.java        # 配置属性（112行）
    │   └── RocketMQAutoConfiguration.java # 自动配置（111行）
    ├── enums/
    │   └── DelayLevel.java                # 延迟级别枚举（168行）
    └── util/
        ├── RMSendUtil.java                # 消息发送工具类（543行）⭐
        ├── RMTopicUtil.java               # Topic 管理工具类（297行）
        └── RMDiagnosticUtil.java          # 诊断工具类（167行）
```

---

## 配置

### application.yml

```yaml
rocketmq:
  # 启用开关（默认关闭，需显式开启）
  enabled: ${ROCKETMQ_ENABLED:false}
  # NameServer 地址（多个用分号分隔）
  name-server: ${ROCKETMQ_NAME_SERVER:127.0.0.1:9876}
  # 集群名称（用于 Topic 管理）
  cluster-name: ${ROCKETMQ_CLUSTER_NAME:RuoYiCluster}
  # Broker 地址（用于 Topic 管理）
  broker-addr: ${ROCKETMQ_BROKER_ADDR:127.0.0.1:10911}

  # 生产者配置
  producer:
    group: ${ROCKETMQ_PRODUCER_GROUP:ruoyi-producer-group}
    send-message-timeout: 3000         # 发送超时（毫秒）
    max-message-size: 4194304          # 最大消息 4MB
    retry-times-when-send-failed: 2    # 同步发送失败重试
    retry-times-when-send-async-failed: 2  # 异步发送失败重试
    compress-message-body-threshold: 4096  # 压缩阈值
    auto-create-topic: true            # 自动创建 Topic
    batch-size: 100                    # 批量发送最大数量
    enable-log: true                   # 发送日志

  # 消费者配置
  consumer:
    consume-thread-min: 20             # 最小消费线程
    consume-thread-max: 64             # 最大消费线程
    pull-batch-size: 32                # 拉取批次大小
    max-reconsume-times: 16            # 消费失败最大重试
```

> **注意**：`enabled` 默认 `false`。启用前需确保 NameServer 和 Broker 已部署。所有配置项均支持环境变量覆盖。

### 条件加载

```java
// RocketMQAutoConfiguration.java
@AutoConfiguration
@ConditionalOnProperty(prefix = "rocketmq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RocketMQProperties.class)
public class RocketMQAutoConfiguration { ... }
```

仅当 `rocketmq.enabled=true` 时，自动配置类才会加载，初始化 `RMSendUtil`、`RMTopicUtil`、`RMDiagnosticUtil` 三个静态工具类，并在启动时自动诊断 Broker 连接状态。

---

## 一、消息发送（RMSendUtil）

`RMSendUtil` 是核心工具类（543 行），封装 `RocketMQTemplate` 提供 19 个静态方法。

### 方法速查表

| 方法 | 发送模式 | 返回值 | 适用场景 |
|------|---------|--------|---------|
| `send(topic, msg)` | 同步 | `SendResult` | 普通消息，需确认 |
| `send(topic, msg, timeout)` | 同步 | `SendResult` | 大消息/慢网络 |
| `sendWithAutoCreate(topic, msg)` | 同步 | `SendResult` | 强制自动创建 Topic |
| `sendWithAutoCreate(topic, msg, timeout)` | 同步 | `SendResult` | 同上+自定义超时 |
| `sendAsync(topic, msg, callback)` | 异步 | void | 不阻塞调用方 |
| `sendAsync(topic, msg, callback, timeout)` | 异步 | void | 同上+自定义超时 |
| `sendAsync(topic, msg, successCb)` | 异步 | void | 简化回调 |
| `sendOneWay(topic, msg)` | 单向 | void | 日志、不重要消息 |
| `sendDelay(topic, msg, DelayLevel)` | 延迟 | `SendResult` | 定时业务（枚举） |
| `sendDelay(topic, msg, level)` | 延迟 | `SendResult` | 定时业务（数字） |
| `sendDelay(topic, msg, level, timeout)` | 延迟 | `SendResult` | 同上+自定义超时 |
| `sendWithTag(topic, tag, msg)` | 同步+标签 | `SendResult` | 消息分类过滤 |
| `sendWithTag(topic, tag, msg, timeout)` | 同步+标签 | `SendResult` | 同上+自定义超时 |
| `sendBatch(topic, messages)` | 批量单向 | void | 大量消息 |
| `sendTransaction(topic, msg)` | 事务 | void | 分布式事务 |
| `sendTransaction(topic, msg, txId)` | 事务 | void | 自定义事务 ID |
| `sendTransaction(topic, msg, txId, arg)` | 事务 | void | 完整事务参数 |

### 1. 同步发送

```java
import plus.ruoyi.common.rocketmq.util.RMSendUtil;
import org.apache.rocketmq.client.producer.SendResult;

// 基础同步发送（默认超时 3000ms）
SendResult result = RMSendUtil.send("order-topic", orderMessage);
log.info("消息发送成功, msgId={}", result.getMsgId());

// 自定义超时（大消息场景）
SendResult result = RMSendUtil.send("file-topic", largeMessage, 10000);

// 强制自动创建 Topic（开发环境常用）
SendResult result = RMSendUtil.sendWithAutoCreate("dev-topic", testMessage);
```

### 2. 异步发送

```java
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

// 完整回调
RMSendUtil.sendAsync("order-topic", orderMessage, new SendCallback() {
    @Override
    public void onSuccess(SendResult sendResult) {
        log.info("异步发送成功: {}", sendResult.getMsgId());
    }
    @Override
    public void onException(Throwable e) {
        log.error("异步发送失败", e);
    }
});

// 简化回调（只处理成功）
RMSendUtil.sendAsync("log-topic", logMessage, result -> {
    log.info("日志消息发送成功: {}", result.getMsgId());
});

// 自定义超时
RMSendUtil.sendAsync("order-topic", message, callback, 5000);
```

### 3. 单向发送

```java
// 不等待响应，性能最高（适合日志、监控数据等不重要消息）
RMSendUtil.sendOneWay("monitor-topic", metricsData);
```

### 4. 延迟消息

```java
import plus.ruoyi.common.rocketmq.enums.DelayLevel;

// 使用枚举（推荐）
// 订单 30 分钟未支付自动取消
RMSendUtil.sendDelay("order-cancel-topic", orderId, DelayLevel.THIRTY_MINUTES);

// 使用级别数字
RMSendUtil.sendDelay("reminder-topic", reminder, 5);  // 级别5 = 1分钟

// 自定义超时
RMSendUtil.sendDelay("order-cancel-topic", orderId, 16, 5000);  // 级别16 = 30分钟
```

### 5. 带标签消息

```java
// 发送 VIP 标签消息（消费者可按标签过滤）
RMSendUtil.sendWithTag("order-topic", "VIP", vipOrderMessage);

// 自定义超时
RMSendUtil.sendWithTag("order-topic", "NORMAL", normalOrder, 5000);
```

### 6. 批量发送

```java
import java.util.List;

// 批量单向发送（注意：单向模式，不保证到达）
List<OrderMessage> messages = List.of(msg1, msg2, msg3);
RMSendUtil.sendBatch("batch-topic", messages);
```

> **限制**：批量消息数量受 `producer.batch-size`（默认 100）和 `producer.max-message-size`（默认 4MB）限制。

### 7. 事务消息

```java
// 自动生成事务 ID
RMSendUtil.sendTransaction("order-topic", orderMessage);

// 自定义事务 ID（便于追踪）
RMSendUtil.sendTransaction("order-topic", orderMessage, "TX-ORDER-" + orderId);

// 完整参数（附带额外参数传给本地事务执行器）
RMSendUtil.sendTransaction("order-topic", orderMessage, "TX-" + orderId, orderContext);
```

事务消息需配合 `RocketMQLocalTransactionListener` 使用：

```java
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RocketMQTransactionListener
public class OrderTransactionListener implements RocketMQLocalTransactionListener {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            // 执行本地事务（如：创建订单、扣减库存）
            orderService.createOrder(arg);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 事务回查：检查本地事务是否已执行成功
        String transactionId = msg.getHeaders().get("rocketmq_TRANSACTION_ID", String.class);
        boolean exists = orderService.existsByTransactionId(transactionId);
        return exists ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.UNKNOWN;
    }
}
```

---

## 二、延迟级别（DelayLevel）

RocketMQ 开源版本使用固定的 18 个延迟级别（非任意时间延迟）。

### 完整级别表

| 枚举值 | level | code | 延迟时间 | 典型场景 |
|--------|-------|------|----------|---------|
| `ONE_SECOND` | 1 | "1s" | 1 秒 | 即时重试 |
| `FIVE_SECONDS` | 2 | "5s" | 5 秒 | 短暂延迟 |
| `TEN_SECONDS` | 3 | "10s" | 10 秒 | 验证码过期检查 |
| `THIRTY_SECONDS` | 4 | "30s" | 30 秒 | 短期超时 |
| `ONE_MINUTE` | 5 | "1m" | 1 分钟 | 支付状态轮询 |
| `TWO_MINUTES` | 6 | "2m" | 2 分钟 | - |
| `THREE_MINUTES` | 7 | "3m" | 3 分钟 | - |
| `FOUR_MINUTES` | 8 | "4m" | 4 分钟 | - |
| `FIVE_MINUTES` | 9 | "5m" | 5 分钟 | 订单支付提醒 |
| `SIX_MINUTES` | 10 | "6m" | 6 分钟 | - |
| `SEVEN_MINUTES` | 11 | "7m" | 7 分钟 | - |
| `EIGHT_MINUTES` | 12 | "8m" | 8 分钟 | - |
| `NINE_MINUTES` | 13 | "9m" | 9 分钟 | - |
| `TEN_MINUTES` | 14 | "10m" | 10 分钟 | 缓存预热 |
| `TWENTY_MINUTES` | 15 | "20m" | 20 分钟 | 中期超时 |
| `THIRTY_MINUTES` | 16 | "30m" | 30 分钟 | 订单超时取消 |
| `ONE_HOUR` | 17 | "1h" | 1 小时 | 长期超时 |
| `TWO_HOURS` | 18 | "2h" | 2 小时 | 最长延迟 |

### 辅助方法

```java
import plus.ruoyi.common.rocketmq.enums.DelayLevel;

// 根据级别数字获取枚举
DelayLevel level = DelayLevel.fromLevel(16);  // THIRTY_MINUTES

// 根据 code 获取枚举
DelayLevel level = DelayLevel.fromCode("30m");  // THIRTY_MINUTES

// 获取中文描述
String desc = DelayLevel.THIRTY_MINUTES.getDescription();  // "30分钟"
```

---

## 三、消费者开发

### 基础消费者

```java
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
    topic = "order-topic",                    // 监听的 Topic
    consumerGroup = "order-consumer-group"    // 消费者组名（全局唯一）
)
public class OrderMessageListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("收到订单消息: {}", message);
        // 业务处理
    }
}
```

### 带标签过滤的消费者

```java
@Component
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "vip-order-consumer-group",
    selectorExpression = "VIP || SVIP"    // 只消费 VIP 和 SVIP 标签
)
public class VipOrderListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        // 只处理 VIP/SVIP 订单
    }
}
```

### 对象类型消费者

```java
@Component
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "order-obj-consumer-group"
)
public class OrderObjListener implements RocketMQListener<OrderMessage> {

    @Override
    public void onMessage(OrderMessage order) {
        log.info("收到订单: orderId={}, amount={}", order.getOrderId(), order.getAmount());
    }
}
```

### 广播模式消费者

```java
import org.apache.rocketmq.spring.annotation.MessageModel;

@Component
@RocketMQMessageListener(
    topic = "config-update-topic",
    consumerGroup = "config-broadcast-group",
    messageModel = MessageModel.BROADCASTING    // 广播模式：每个实例都收到
)
public class ConfigUpdateListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        // 所有实例都会执行（如刷新本地缓存）
        cacheManager.refresh(message);
    }
}
```

### 消费者组命名规范

```
{业务模块}-{功能}-consumer-group

示例：
order-create-consumer-group    # 订单创建
order-cancel-consumer-group    # 订单取消
payment-notify-consumer-group  # 支付通知
stock-deduct-consumer-group    # 库存扣减
```

---

## 四、Topic 管理（RMTopicUtil）

`RMTopicUtil`（297 行）提供 Topic 的创建、删除、查询、路由验证功能。

### 方法速查

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `createTopic(name)` | Topic 名称 | void | 创建 Topic（默认 8 队列） |
| `createTopic(name, queueNum)` | 名称+队列数 | void | 创建 Topic（指定队列数） |
| `createTopicIfNotExists(ns, broker, cluster, name, queueNum)` | 完整参数 | void | 底层方法 |
| `deleteTopic(name)` | Topic 名称 | void | 删除 Topic |
| `deleteTopic(ns, cluster, name)` | 完整参数 | void | 底层方法 |
| `listTopics()` | 无 | `Set<String>` | 查询所有 Topic |
| `listTopics(ns)` | NameServer | `Set<String>` | 底层方法 |
| `verifyTopicRoute(name)` | Topic 名称 | boolean | 验证路由是否可用 |
| `verifyTopicRoute(ns, name)` | 完整参数 | boolean | 底层方法 |

### 使用示例

```java
import plus.ruoyi.common.rocketmq.util.RMTopicUtil;

// 创建 Topic（默认 8 个队列）
RMTopicUtil.createTopic("order-topic");

// 创建 Topic（指定 16 个队列，高吞吐场景）
RMTopicUtil.createTopic("high-throughput-topic", 16);

// 验证 Topic 路由是否可用
boolean ready = RMTopicUtil.verifyTopicRoute("order-topic");
if (!ready) {
    log.warn("Topic 路由不可用，可能 Broker 未注册");
}

// 查询所有 Topic
Set<String> topics = RMTopicUtil.listTopics();
topics.forEach(t -> log.info("Topic: {}", t));

// 删除 Topic（⚠️ 生产环境慎用）
RMTopicUtil.deleteTopic("test-topic");
```

### Topic 命名规范

```
{业务模块}-{功能}-topic

示例：
order-topic          # 订单消息
order-cancel-topic   # 订单取消延迟消息
payment-topic        # 支付消息
stock-topic          # 库存消息
log-topic            # 日志消息
config-update-topic  # 配置更新广播
```

> **注意**：`createTopic` 内部会自动检查 Topic 是否已存在，避免重复创建。创建后会等待路由信息同步到 NameServer（最多 30 秒）。

---

## 五、故障诊断（RMDiagnosticUtil）

`RMDiagnosticUtil`（167 行）提供 NameServer 连接测试和 Broker 注册状态检查。

### 方法说明

| 方法 | 用途 | 调用时机 |
|------|------|---------|
| `quickDiagnose(namesrvAddr)` | 测试 NameServer 网络连通性 | 连接失败时 |
| `diagnose()` | 完整诊断（NameServer + Broker） | 启动异常时 |
| `checkBrokerRegistration(namesrvAddr)` | 检查 Broker 是否注册到 NameServer | No route info 错误时 |

### 使用示例

```java
import plus.ruoyi.common.rocketmq.util.RMDiagnosticUtil;

// 快速诊断 NameServer 连接
RMDiagnosticUtil.quickDiagnose("127.0.0.1:9876");
// 输出示例：
// ✅ NameServer 连接成功: 127.0.0.1:9876
// 或
// ❌ NameServer 连接失败: Connection refused

// 完整诊断（使用配置文件中的地址）
RMDiagnosticUtil.diagnose();
// 输出：NameServer 连接状态 + Broker 注册状态

// 单独检查 Broker 注册
RMDiagnosticUtil.checkBrokerRegistration("127.0.0.1:9876");
```

### 自动诊断

`RocketMQAutoConfiguration` 在启动时自动调用诊断：

```java
@Bean
public Object rocketMQStartupLogger() {
    // 1. 打印完整配置信息
    // 2. 自动检查 Broker 连接状态
    // 3. 如果连接失败，输出排查建议
    return new Object();
}
```

启动日志示例：
```
[RocketMQ] ========== 配置信息 ==========
[RocketMQ] NameServer: 127.0.0.1:9876
[RocketMQ] Cluster: RuoYiCluster
[RocketMQ] Producer Group: ruoyi-producer-group
[RocketMQ] ========== 诊断结果 ==========
[RocketMQ] ✅ Broker 已注册，路由信息可用
```

---

## 六、实战场景

### 场景 1：订单超时自动取消

**需求**：订单创建后 30 分钟未支付，自动取消。

#### 生产者（创建订单时发送延迟消息）

```java
@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    @Transactional
    public Long createOrder(OrderBo bo) {
        // 1. 创建订单
        Order order = MapstructUtils.convert(bo, Order.class);
        order.setStatus("0");  // 待支付
        orderDao.insert(order);

        // 2. 发送 30 分钟延迟消息
        RMSendUtil.sendDelay("order-cancel-topic", order.getId(), DelayLevel.THIRTY_MINUTES);
        log.info("订单创建成功, orderId={}, 30分钟后自动取消检查", order.getId());

        return order.getId();
    }
}
```

#### 消费者（检查并取消超时订单）

```java
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "order-cancel-topic",
    consumerGroup = "order-cancel-consumer-group"
)
public class OrderCancelListener implements RocketMQListener<String> {

    @Resource
    private IOrderService orderService;

    @Override
    public void onMessage(String orderIdStr) {
        Long orderId = Long.parseLong(orderIdStr);
        Order order = orderService.getById(orderId);

        if (order == null) {
            log.warn("订单不存在: {}", orderId);
            return;
        }

        // 只取消"待支付"状态的订单
        if ("0".equals(order.getStatus())) {
            orderService.cancelOrder(orderId, "超时未支付，系统自动取消");
            log.info("订单超时取消: orderId={}", orderId);
        } else {
            log.info("订单已处理，无需取消: orderId={}, status={}", orderId, order.getStatus());
        }
    }
}
```

### 场景 2：支付成功异步通知

**需求**：支付成功后异步更新订单状态、扣减库存、发送通知。

#### 生产者

```java
@Service
public class PaymentServiceImpl implements IPaymentService {

    @Override
    public void handlePaymentCallback(PaymentNotify notify) {
        // 1. 更新支付记录
        paymentDao.updateStatus(notify.getPaymentId(), "PAID");

        // 2. 异步发送消息（不阻塞支付回调响应）
        PaymentSuccessMessage message = new PaymentSuccessMessage();
        message.setOrderId(notify.getOrderId());
        message.setPaymentId(notify.getPaymentId());
        message.setAmount(notify.getAmount());

        RMSendUtil.sendAsync("payment-success-topic", message, result -> {
            log.info("支付成功消息发送: orderId={}", notify.getOrderId());
        });
    }
}
```

#### 消费者 1：更新订单状态

```java
@Component
@RocketMQMessageListener(
    topic = "payment-success-topic",
    consumerGroup = "order-status-consumer-group"
)
public class OrderStatusListener implements RocketMQListener<PaymentSuccessMessage> {

    @Override
    public void onMessage(PaymentSuccessMessage msg) {
        orderService.updateStatus(msg.getOrderId(), "1");  // 已支付
    }
}
```

#### 消费者 2：扣减库存

```java
@Component
@RocketMQMessageListener(
    topic = "payment-success-topic",
    consumerGroup = "stock-deduct-consumer-group"
)
public class StockDeductListener implements RocketMQListener<PaymentSuccessMessage> {

    @Override
    public void onMessage(PaymentSuccessMessage msg) {
        stockService.deductStock(msg.getOrderId());
    }
}
```

#### 消费者 3：发送通知

```java
@Component
@RocketMQMessageListener(
    topic = "payment-success-topic",
    consumerGroup = "notify-consumer-group"
)
public class PaymentNotifyListener implements RocketMQListener<PaymentSuccessMessage> {

    @Override
    public void onMessage(PaymentSuccessMessage msg) {
        // 通过统一消息推送发送通知
        WebSocketUtils.publishMessage(
            WebSocketMessageDto.of(msg.getUserId(), "订单支付成功")
        );
    }
}
```

### 场景 3：事务消息保证一致性

**需求**：创建订单同时扣减库存，保证两者一致。

```java
@Service
public class OrderServiceImpl implements IOrderService {

    @Override
    public void createOrderWithStock(OrderBo bo) {
        // 发送事务消息
        OrderStockMessage message = new OrderStockMessage(bo);
        RMSendUtil.sendTransaction(
            "order-stock-topic",
            message,
            "TX-" + IdUtils.fastSimpleUUID(),
            bo  // 传递给本地事务执行器的参数
        );
    }
}

@Component
@RocketMQTransactionListener
public class OrderStockTransactionListener implements RocketMQLocalTransactionListener {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        OrderBo bo = (OrderBo) arg;
        try {
            // 本地事务：创建订单
            orderService.insertOrder(bo);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 回查：检查订单是否创建成功
        String txId = msg.getHeaders().get("rocketmq_TRANSACTION_ID", String.class);
        boolean exists = orderService.existsByTransactionId(txId);
        return exists ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.UNKNOWN;
    }
}
```

### 场景 4：配置变更广播

**需求**：修改配置后通知所有服务实例刷新本地缓存。

```java
// 生产者：修改配置后广播
@Service
public class ConfigServiceImpl implements IConfigService {

    @Override
    public void updateConfig(ConfigBo bo) {
        configDao.update(bo);

        // 广播消息（所有实例都需要收到）
        RMSendUtil.send("config-update-topic", bo.getConfigKey());
    }
}

// 消费者：广播模式，每个实例都接收
@Component
@RocketMQMessageListener(
    topic = "config-update-topic",
    consumerGroup = "config-broadcast-group",
    messageModel = MessageModel.BROADCASTING
)
public class ConfigRefreshListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String configKey) {
        // 刷新本地缓存
        CacheUtils.evict(CacheNames.SYS_CONFIG, configKey);
        log.info("配置缓存已刷新: {}", configKey);
    }
}
```

---

## 七、常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 使用 DelayLevel 枚举而非硬编码数字
RMSendUtil.sendDelay("topic", msg, DelayLevel.THIRTY_MINUTES);  // ✅ 语义清晰

// 2. 消费者做幂等处理（消息可能重复投递）
@Override
public void onMessage(String orderId) {
    if (orderService.isProcessed(orderId)) {
        log.info("订单已处理，跳过: {}", orderId);
        return;  // ✅ 幂等检查
    }
    orderService.processOrder(orderId);
}

// 3. 每个消费者组只消费一个 Topic
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "order-create-consumer-group"  // ✅ 专用消费者组
)

// 4. 同步发送检查返回结果
SendResult result = RMSendUtil.send("order-topic", message);
if (result.getSendStatus() != SendStatus.SEND_OK) {
    log.error("消息发送异常: status={}", result.getSendStatus());
}

// 5. 生产环境关闭自动创建 Topic
// application-prod.yml
// rocketmq.producer.auto-create-topic: false
```

### ❌ 常见错误

```java
// 1. 使用硬编码延迟级别
RMSendUtil.sendDelay("topic", msg, 16);  // ❌ 不知道16是什么意思
RMSendUtil.sendDelay("topic", msg, DelayLevel.THIRTY_MINUTES);  // ✅

// 2. 消费者不做幂等处理
@Override
public void onMessage(String orderId) {
    orderService.deductStock(orderId);  // ❌ 重复消费会多扣库存
}

// 3. 多个消费者组名相同但消费不同 Topic
@RocketMQMessageListener(topic = "order-topic", consumerGroup = "shared-group")  // ❌
@RocketMQMessageListener(topic = "stock-topic", consumerGroup = "shared-group")  // ❌ 冲突！
// 同一消费者组只能订阅同一个 Topic

// 4. 在消费者中抛出异常不处理
@Override
public void onMessage(String message) {
    // ❌ 异常会导致消息重试，可能无限循环
    JSON.parseObject(message, OrderMessage.class);
}
// ✅ 捕获异常，记录日志，人工介入
@Override
public void onMessage(String message) {
    try {
        OrderMessage order = JsonUtils.parseObject(message, OrderMessage.class);
        processOrder(order);
    } catch (Exception e) {
        log.error("消息处理失败，需人工介入: {}", message, e);
        // 不抛异常 = 消费成功（避免无限重试）
    }
}

// 5. 忘记启用 RocketMQ
// ❌ rocketmq.enabled 默认 false，调用 RMSendUtil 会 NPE
// ✅ 使用前确保 application.yml 中 rocketmq.enabled=true
```

---

## 八、与 Redis 方案对比

| 维度 | RocketMQ | Redis Streams | Redisson DelayedQueue |
|------|----------|---------------|-----------------------|
| **部署** | 需独立部署 NameServer + Broker | 已内置 | 已内置 |
| **吞吐量** | 百万级 TPS | 十万级 | 万级 |
| **持久化** | 磁盘持久化、可回溯 | 有限持久化 | 内存 |
| **事务消息** | 原生支持 | 不支持 | 不支持 |
| **延迟消息** | 18 个固定级别 | 不原生支持 | 任意时间 |
| **广播模式** | 原生支持 | 不支持 | 不支持 |
| **消息回溯** | 按时间戳回溯 | 有限 | 不支持 |
| **适用场景** | 高并发、解耦、事务 | 轻量级异步 | 简单延迟任务 |

**决策建议**：

```
日常业务 → Redis（已内置，无需额外部署）
高并发/事务/解耦 → RocketMQ（专业消息中间件）
```

---

## 九、与其他技能的关系

| 技能 | 关系 |
|------|------|
| `redis-cache` | Redis Streams/DelayedQueue 是轻量级替代方案 |
| `scheduled-jobs` | 定时任务是另一种延迟执行方案，适合固定周期 |
| `realtime-communication` | WebSocket/SSE 用于实时推送，MQ 用于异步解耦 |
| `notification-system` | 消息通知可通过 MQ 异步发送 |
| `architecture-design` | MQ 是系统解耦的核心架构组件 |

---

## 十、参考文件索引

| 文件 | 行数 | 说明 |
|------|------|------|
| `ruoyi-common/ruoyi-common-rocketmq/` | - | 模块根目录 |
| `config/RocketMQProperties.java` | 112 | 配置属性类 |
| `config/RocketMQAutoConfiguration.java` | 111 | 自动配置类 |
| `enums/DelayLevel.java` | 168 | 延迟级别枚举 |
| `util/RMSendUtil.java` | 543 | 消息发送工具类（核心） |
| `util/RMTopicUtil.java` | 297 | Topic 管理工具类 |
| `util/RMDiagnosticUtil.java` | 167 | 诊断工具类 |

---

## 十一、FAQ

### Q1: RocketMQ 默认是关闭的吗？

**A**: 是。`rocketmq.enabled` 默认 `false`，需在 `application.yml` 中改为 `true` 并确保 NameServer 和 Broker 已部署。

### Q2: 如何选择发送模式？

**A**:

| 模式 | 可靠性 | 性能 | 适用场景 |
|------|--------|------|---------|
| 同步（send） | 最高 | 低 | 订单、支付等重要消息 |
| 异步（sendAsync） | 高 | 中 | 通知、日志（需回调确认） |
| 单向（sendOneWay） | 低 | 最高 | 监控数据、不重要日志 |

### Q3: 延迟消息能指定任意时间吗？

**A**: 开源版 RocketMQ 只支持 18 个固定级别（1s ~ 2h）。如果需要任意时间延迟：
- 简单场景：使用 `RedissonDelayedQueue`（已内置）
- 复杂场景：升级 RocketMQ 商业版或使用多级延迟组合

### Q4: 消费失败会怎样？

**A**: 消费失败（抛异常）会触发重试，最多 `max-reconsume-times` 次（默认 16 次）。重试间隔按延迟级别递增。超过最大重试次数后进入死信队列（`%DLQ%` 前缀）。

### Q5: 多个消费者可以订阅同一个 Topic 吗？

**A**: 可以，但需使用**不同的消费者组名**。同一组内的实例采用负载均衡（每条消息只被一个实例消费）；不同组各自独立消费所有消息。

### Q6: 启动时报 "No route info" 怎么办？

**A**: 这通常是 Broker 未注册到 NameServer。排查步骤：
1. 调用 `RMDiagnosticUtil.diagnose()` 查看诊断结果
2. 确认 NameServer 地址正确且可访问
3. 确认 Broker 已启动并注册到 NameServer
4. 开发环境可设置 `auto-create-topic: true` 自动创建 Topic
