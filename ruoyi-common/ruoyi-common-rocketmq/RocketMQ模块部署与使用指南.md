# RocketMQ 模块部署与使用指南

> **版本**: RocketMQ 5.3.1
> **架构**: 1 NameServer + 1 Master Broker + 1 Slave Broker + 1 Dashboard
> **更新时间**: 2025-11-03

---

## 📖 目录

- [一、RocketMQ 简介](#一rocketmq-简介)
- [二、服务端部署](#二服务端部署)
  - [2.1 前置准备](#21-前置准备)
  - [2.2 部署步骤](#22-部署步骤)
  - [2.3 环境变量配置](#23-环境变量配置)
  - [2.4 验证部署](#24-验证部署)
- [三、客户端配置](#三客户端配置)
  - [3.1 Maven 依赖](#31-maven-依赖)
  - [3.2 应用配置](#32-应用配置)
  - [3.3 配置说明](#33-配置说明)
- [四、工具类使用](#四工具类使用)
  - [4.1 RMSendUtil - 消息发送工具类](#41-rmsendutil---消息发送工具类)
  - [4.2 RMTopicUtil - Topic 管理工具类](#42-rmtopicutil---topic-管理工具类)
  - [4.3 RMDiagnosticUtil - 诊断工具类](#43-rmdiagnosticutil---诊断工具类)
  - [4.4 DelayLevel - 延迟级别枚举](#44-delaylevel---延迟级别枚举)
- [五、消息类型与使用示例](#五消息类型与使用示例)
  - [5.1 同步消息](#51-同步消息)
  - [5.2 异步消息](#52-异步消息)
  - [5.3 单向消息](#53-单向消息)
  - [5.4 延迟消息](#54-延迟消息)
  - [5.5 带标签消息](#55-带标签消息)
  - [5.6 批量消息](#56-批量消息)
  - [5.7 事务消息](#57-事务消息)
- [六、测试接口](#六测试接口)
- [七、常见问题与解决方案](#七常见问题与解决方案)
- [八、生产环境建议](#八生产环境建议)

---

## 一、RocketMQ 简介

Apache RocketMQ 是一款分布式消息中间件，具有以下特点：

### ✨ 核心特性
- ✅ **高性能**: 单机支持万级 TPS
- ✅ **高可靠**: 主从架构，消息持久化
- ✅ **高可用**: 支持集群部署，故障自动切换
- ✅ **消息类型丰富**: 支持普通、顺序、事务、延迟消息
- ✅ **可视化管理**: 提供 Dashboard 控制台

### 🏗️ 本项目架构
```
┌─────────────────────────────────────────┐
│           RocketMQ 集群架构              │
├─────────────────────────────────────────┤
│  NameServer (9876)                      │  路由信息管理
├─────────────────────────────────────────┤
│  Broker Master (10911)                  │  主节点：读写
│  Broker Slave  (10921)                  │  从节点：只读 + 备份
├─────────────────────────────────────────┤
│  Dashboard (8088)                       │  可视化管理
└─────────────────────────────────────────┘
```

---

## 二、服务端部署

### 2.1 前置准备

#### 系统要求
- **操作系统**: Linux / macOS / Windows
- **Docker**: 20.10+
- **Docker Compose**: 1.29+
- **磁盘空间**: 建议 10GB+
- **内存**: 建议 4GB+

#### 端口占用检查
```bash
# 检查端口是否被占用
netstat -tuln | grep -E '9876|10911|10921|8088'
```

**端口说明**:
| 服务 | 端口 | 说明 |
|------|------|------|
| NameServer | 9876 | 路由注册中心 |
| Broker Master | 10909, 10911, 10912 | 主 Broker |
| Broker Slave | 10919, 10921, 10922 | 从 Broker |
| Dashboard | 8088 | Web 控制台 |

---

### 2.2 部署步骤

#### 步骤 1: 获取部署文件

部署文件位置：`script/docker/rocketmp/`

```bash
cd script/docker/rocketmp
```

**目录结构**:
```
rocketmp/
├── RocketMQServer-compose.yml  # Docker Compose 配置
├── .env.example                # 环境变量示例（可选）
├── conf/                       # Broker 配置文件
│   ├── broker-a.conf          # Master Broker 配置
│   └── broker-a-s.conf        # Slave Broker 配置
└── data/                       # 数据持久化目录（自动创建）
    ├── namesrv/
    ├── broker-master/
    ├── broker-slave/
    └── dashboard/
```

#### 步骤 2: 配置环境变量（可选）

创建 `.env` 文件（基于 `.env.example`）:
```bash
# RocketMQ 版本
ROCKETMQ_VERSION=5.3.1

# Dashboard 版本
DASHBOARD_VERSION=latest

# 时区设置
TZ=Asia/Shanghai

# 数据目录
DATA_DIR=./data

# NameServer 地址（宿主机网络使用 127.0.0.1）
NAMESRV_ADDR=127.0.0.1:9876

# Dashboard 端口
DASHBOARD_PORT=8088

# Dashboard 登录认证（默认关闭）
DASHBOARD_LOGIN_ENABLED=false

# ===================== JVM 参数配置 =====================
# NameServer JVM 参数
NAMESRV_JAVA_OPTS=-Xms512m -Xmx512m -Xmn256m

# Master Broker JVM 参数
BROKER_MASTER_JAVA_OPTS=-Xms1g -Xmx1g -Xmn512m

# Slave Broker JVM 参数
BROKER_SLAVE_JAVA_OPTS=-Xms1g -Xmx1g -Xmn512m

# Dashboard JVM 参数
DASHBOARD_JAVA_OPTS=-Xms256m -Xmx256m
```

#### 步骤 3: 修改 Broker 配置（生产环境必须）

**本地开发环境**：
```properties
# conf/broker-a.conf
brokerIP1=127.0.0.1
```

**生产环境**：
```properties
# conf/broker-a.conf
brokerIP1=<服务器公网IP>  # ⚠️ 必须修改为实际服务器IP

# conf/broker-a-s.conf
brokerIP1=<服务器公网IP>  # ⚠️ 必须修改为实际服务器IP
```

#### 步骤 4: 启动服务

```bash
# 启动所有服务
docker-compose -f RocketMQServer-compose.yml up -d

# 查看服务状态
docker-compose -f RocketMQServer-compose.yml ps

# 查看日志
docker-compose -f RocketMQServer-compose.yml logs -f
```

**预期输出**:
```
NAME                IMAGE                                    STATUS
rmq-namesrv         apache/rocketmq:5.3.1                   Up (healthy)
rmq-broker-master   apache/rocketmq:5.3.1                   Up
rmq-broker-slave    apache/rocketmq:5.3.1                   Up
rmq-dashboard       apacherocketmq/rocketmq-dashboard:latest Up
```

#### 步骤 5: 停止服务

```bash
# 停止服务（保留数据）
docker-compose -f RocketMQServer-compose.yml down

# 停止服务并删除数据
docker-compose -f RocketMQServer-compose.yml down -v
rm -rf data/
```

---

### 2.3 环境变量配置

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `ROCKETMQ_VERSION` | 5.3.1 | RocketMQ 版本 |
| `DASHBOARD_VERSION` | latest | Dashboard 版本 |
| `TZ` | Asia/Shanghai | 时区 |
| `DATA_DIR` | ./data | 数据持久化目录 |
| `NAMESRV_ADDR` | 127.0.0.1:9876 | NameServer 地址 |
| `DASHBOARD_PORT` | 8088 | Dashboard 端口 |
| `NAMESRV_JAVA_OPTS` | -Xms512m -Xmx512m | NameServer JVM 参数 |
| `BROKER_MASTER_JAVA_OPTS` | -Xms1g -Xmx1g | Master Broker JVM 参数 |
| `BROKER_SLAVE_JAVA_OPTS` | -Xms1g -Xmx1g | Slave Broker JVM 参数 |
| `DASHBOARD_JAVA_OPTS` | -Xms256m -Xmx256m | Dashboard JVM 参数 |

---

### 2.4 验证部署

#### 1️⃣ 访问 Dashboard

打开浏览器访问：`http://服务器IP:8088`

**功能说明**:
- 📊 **集群监控**: 查看 Broker、NameServer 状态
- 📝 **Topic 管理**: 创建、删除、查看 Topic
- 📨 **消息查询**: 根据 Key、MessageId 查询消息
- 👥 **消费者管理**: 查看消费组、消费进度

#### 2️⃣ 检查 Broker 注册

```bash
# 进入 NameServer 容器
docker exec -it rmq-namesrv bash

# 查看 Broker 注册信息
sh mqadmin clusterList -n 127.0.0.1:9876
```

**预期输出**:
```
#Cluster Name     #Broker Name      #BID  #Addr                  #Version
RuoYiCluster      broker-a          0     127.0.0.1:10911        V5_3_1
RuoYiCluster      broker-a          1     127.0.0.1:10921        V5_3_1
```

#### 3️⃣ 测试消息发送

使用项目提供的测试接口（见第六节）进行功能测试。

---

## 三、客户端配置

### 3.1 Maven 依赖

#### ⚠️ 重要变更（v5.5.0+）

从 v5.5.0 开始，RocketMQ 模块采用**按需引入**策略，默认不启用，需要使用时手动引入依赖。

#### 依赖引入方式

**步骤1**: 在业务模块的 `pom.xml` 中添加依赖

```xml
<!-- 位置: ruoyi-modules/ruoyi-business/pom.xml -->

<!-- 引入 RocketMQ 模块（会自动传递 rocketmq-spring-boot-starter 依赖） -->
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-rocketmq</artifactId>
</dependency>
```

**步骤2**: 在 `application.yml` 中启用 RocketMQ

```yaml
rocketmq:
  enabled: true  # ⚠️ 必须设置为 true
  name-server: 127.0.0.1:9876
```

#### 依赖说明

| 依赖 | 作用 | 说明 |
|------|------|------|
| `ruoyi-common-rocketmq` | 提供 RocketMQ 工具类和自动配置 | 引入后会自动传递 `rocketmq-spring-boot-starter` 和 `rocketmq-tools` |

#### 不使用 RocketMQ 时

如果项目不需要使用消息队列功能，**不引入**或**注释掉**业务模块 `pom.xml` 中的 RocketMQ 依赖即可。

```xml
<!-- 不需要使用时，注释掉即可 -->
<!--
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-rocketmq</artifactId>
</dependency>
-->
```

✅ **效果**: 不会引入任何 RocketMQ 依赖 jar 包，减少打包体积

---

### 3.2 应用配置

配置文件位置：`ruoyi-admin/src/main/resources/application-{env}.yml`

**开发环境示例** (`application-local.yml`):
```yaml
rocketmq:
  # ✅ 是否启用 RocketMQ（必须设置为 true）
  enabled: ${ROCKETMQ_ENABLED:false}

  # NameServer 地址（多个用分号分隔）
  name-server: ${ROCKETMQ_NAME_SERVER:127.0.0.1:9876}

  # 集群名称（用于 Topic 管理）
  cluster-name: ${ROCKETMQ_CLUSTER_NAME:RuoYiCluster}

  # Broker 地址（用于 Topic 管理）
  broker-addr: ${ROCKETMQ_BROKER_ADDR:127.0.0.1:10911}

  # 生产者配置
  producer:
    # 生产者组名
    group: ${ROCKETMQ_PRODUCER_GROUP:ruoyi-producer-group}

    # 发送消息超时时间（毫秒）
    send-message-timeout: ${ROCKETMQ_PRODUCER_MESSAGE_TIMEOUT:3000}

    # 消息最大大小（4MB）
    max-message-size: ${ROCKETMQ_PRODUCER_MESSAGE_SIZE:4194304}

    # 发送失败重试次数
    retry-times-when-send-failed: ${ROCKETMQ_PRODUCER_RETRY_FAILED:2}

    # 异步发送失败重试次数
    retry-times-when-send-async-failed: ${ROCKETMQ_PRODUCER_RETRY_ASYNC_FAILED:2}

    # 是否自动创建 Topic（开发环境 true，生产环境 false）
    auto-create-topic: ${ROCKETMQ_PRODUCER_AUTO_CREATE_TOPIC:true}

    # 批量发送消息的最大数量
    batch-size: ${ROCKETMQ_PRODUCER_BATCH_SIZE:100}

    # 是否启用消息发送日志
    enable-log: ${ROCKETMQ_PRODUCER_ENABLE_LOG:true}
```

**生产环境配置** (`application-prod.yml`):
```yaml
rocketmq:
  enabled: true
  name-server: 生产服务器IP:9876
  cluster-name: RuoYiCluster
  broker-addr: 生产服务器IP:10911

  producer:
    group: ruoyi-producer-group
    send-message-timeout: 5000
    auto-create-topic: false  # ⚠️ 生产环境建议关闭
    enable-log: false          # ⚠️ 生产环境建议关闭
```

---

### 3.3 配置说明

#### 核心配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `rocketmq.enabled` | Boolean | false | 是否启用 RocketMQ 模块 |
| `rocketmq.name-server` | String | 127.0.0.1:9876 | NameServer 地址 |
| `rocketmq.cluster-name` | String | RuoYiCluster | 集群名称 |
| `rocketmq.broker-addr` | String | 127.0.0.1:10911 | Broker 地址 |

#### 生产者配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `producer.group` | String | ruoyi-producer-group | 生产者组名 |
| `producer.send-message-timeout` | Integer | 3000 | 发送超时时间（毫秒） |
| `producer.max-message-size` | Integer | 4194304 | 消息最大大小（4MB） |
| `producer.retry-times-when-send-failed` | Integer | 2 | 同步发送失败重试次数 |
| `producer.retry-times-when-send-async-failed` | Integer | 2 | 异步发送失败重试次数 |
| `producer.auto-create-topic` | Boolean | true | 是否自动创建 Topic |
| `producer.batch-size` | Integer | 100 | 批量发送大小 |
| `producer.enable-log` | Boolean | true | 是否启用发送日志 |

---

## 四、工具类使用

### 4.1 RMSendUtil - 消息发送工具类

**位置**: `plus.ruoyi.common.rocketmq.util.RMSendUtil`

#### 功能概览

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `send(topic, msg)` | 同步发送 | SendResult |
| `sendAsync(topic, msg, callback)` | 异步发送 | void |
| `sendOneWay(topic, msg)` | 单向发送 | void |
| `sendDelay(topic, msg, level)` | 延迟消息 | SendResult |
| `sendWithTag(topic, tag, msg)` | 带标签消息 | SendResult |
| `sendBatch(topic, msgList)` | 批量发送 | void |
| `sendTransaction(topic, msg)` | 事务消息 | void |

#### 使用示例

```java
import plus.ruoyi.common.rocketmq.util.RMSendUtil;
import plus.ruoyi.common.rocketmq.enums.DelayLevel;

// 1. 同步发送
SendResult result = RMSendUtil.send("order-topic", orderMessage);

// 2. 异步发送（简化回调）
RMSendUtil.sendAsync("order-topic", orderMessage, result -> {
    log.info("发送成功: {}", result.getMsgId());
});

// 3. 单向发送
RMSendUtil.sendOneWay("log-topic", logMessage);

// 4. 延迟消息
RMSendUtil.sendDelay("order-topic", orderMessage, DelayLevel.TEN_SECONDS);

// 5. 带标签消息
RMSendUtil.sendWithTag("order-topic", "VIP", orderMessage);

// 6. 批量发送
List<OrderMessage> messages = Arrays.asList(msg1, msg2, msg3);
RMSendUtil.sendBatch("order-topic", messages);

// 7. 事务消息
RMSendUtil.sendTransaction("order-topic", orderMessage);
```

---

### 4.2 RMTopicUtil - Topic 管理工具类

**位置**: `plus.ruoyi.common.rocketmq.util.RMTopicUtil`

#### 功能概览

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `createTopic(topicName)` | 创建 Topic | void |
| `createTopic(topicName, queueNum)` | 创建 Topic（指定队列数） | void |
| `deleteTopic(topicName)` | 删除 Topic | void |
| `listTopics()` | 查询所有 Topic | Set\<String\> |
| `verifyTopicRoute(topicName)` | 验证 Topic 路由 | boolean |

#### 使用示例

```java
import plus.ruoyi.common.rocketmq.util.RMTopicUtil;

// 1. 创建 Topic（默认 8 个队列）
RMTopicUtil.createTopic("order-topic");

// 2. 创建 Topic（指定队列数）
RMTopicUtil.createTopic("order-topic", 16);

// 3. 删除 Topic
RMTopicUtil.deleteTopic("order-topic");

// 4. 查询所有 Topic
Set<String> topics = RMTopicUtil.listTopics();
topics.forEach(topic -> log.info("Topic: {}", topic));

// 5. 验证 Topic 路由
boolean isValid = RMTopicUtil.verifyTopicRoute("order-topic");
if (isValid) {
    log.info("Topic 路由正常");
} else {
    log.warn("Topic 路由不可用");
}
```

---

### 4.3 RMDiagnosticUtil - 诊断工具类

**位置**: `plus.ruoyi.common.rocketmq.util.RMDiagnosticUtil`

#### 功能概览

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `diagnose()` | 诊断 RocketMQ 连接状态 | void |

#### 使用示例

```java
import plus.ruoyi.common.rocketmq.util.RMDiagnosticUtil;

// 诊断 RocketMQ 状态
RMDiagnosticUtil.diagnose();
```

**诊断输出**:
```
========================================
🔍 开始诊断 RocketMQ 状态...
========================================
✅ NameServer 连接正常: 127.0.0.1:9876
✅ Broker 注册正常: broker-a (Master)
✅ Broker 注册正常: broker-a (Slave)
========================================
```

---

### 4.4 DelayLevel - 延迟级别枚举

**位置**: `plus.ruoyi.common.rocketmq.enums.DelayLevel`

#### 延迟级别列表

| 枚举值 | Level | 延迟时间 |
|--------|-------|----------|
| `ONE_SECOND` | 1 | 1秒 |
| `FIVE_SECONDS` | 2 | 5秒 |
| `TEN_SECONDS` | 3 | 10秒 |
| `THIRTY_SECONDS` | 4 | 30秒 |
| `ONE_MINUTE` | 5 | 1分钟 |
| `TWO_MINUTES` | 6 | 2分钟 |
| `THREE_MINUTES` | 7 | 3分钟 |
| `FOUR_MINUTES` | 8 | 4分钟 |
| `FIVE_MINUTES` | 9 | 5分钟 |
| `SIX_MINUTES` | 10 | 6分钟 |
| `SEVEN_MINUTES` | 11 | 7分钟 |
| `EIGHT_MINUTES` | 12 | 8分钟 |
| `NINE_MINUTES` | 13 | 9分钟 |
| `TEN_MINUTES` | 14 | 10分钟 |
| `TWENTY_MINUTES` | 15 | 20分钟 |
| `THIRTY_MINUTES` | 16 | 30分钟 |
| `ONE_HOUR` | 17 | 1小时 |
| `TWO_HOURS` | 18 | 2小时 |

#### 使用示例

```java
import plus.ruoyi.common.rocketmq.enums.DelayLevel;

// 使用枚举发送延迟消息
RMSendUtil.sendDelay("order-topic", message, DelayLevel.TEN_SECONDS);

// 根据 Level 获取枚举
DelayLevel level = DelayLevel.fromLevel(3);
System.out.println(level.getDescription()); // 输出: 10秒

// 根据 Code 获取枚举
DelayLevel level = DelayLevel.fromCode("10s");
System.out.println(level.getLevel()); // 输出: 3
```

---

## 五、消息类型与使用示例

### 5.1 同步消息

**特点**: 可靠但阻塞，适合重要消息

```java
@Service
public class OrderService {

    public void createOrder(Order order) {
        // 保存订单
        orderDao.insert(order);

        // 发送订单创建消息（同步发送）
        SendResult result = RMSendUtil.send("order-topic", order);

        log.info("订单消息发送成功: MessageId={}", result.getMsgId());
    }
}
```

---

### 5.2 异步消息

**特点**: 高性能，不阻塞主线程

```java
@Service
public class OrderService {

    public void createOrder(Order order) {
        // 保存订单
        orderDao.insert(order);

        // 发送订单创建消息（异步发送）
        RMSendUtil.sendAsync("order-topic", order, result -> {
            log.info("订单消息发送成功: MessageId={}", result.getMsgId());
        });

        // 不等待发送结果，立即返回
        log.info("订单创建完成，消息已提交");
    }
}
```

---

### 5.3 单向消息

**特点**: 最快但不保证可靠，适合日志等不重要消息

```java
@Service
public class LogService {

    public void recordLog(String message) {
        // 发送日志消息（单向发送）
        RMSendUtil.sendOneWay("log-topic", message);
    }
}
```

---

### 5.4 延迟消息

**特点**: 消息延迟一段时间后才会被消费

**应用场景**:
- 订单超时自动取消
- 定时提醒
- 延迟任务

```java
@Service
public class OrderService {

    public void createOrder(Order order) {
        // 保存订单
        orderDao.insert(order);

        // 发送延迟消息：30分钟后检查订单支付状态
        RMSendUtil.sendDelay("order-check-topic", order, DelayLevel.THIRTY_MINUTES);

        log.info("订单将在30分钟后检查支付状态");
    }
}
```

**消费者示例**:
```java
@Component
@RocketMQMessageListener(
    consumerGroup = "order-check-consumer",
    topic = "order-check-topic"
)
public class OrderCheckConsumer implements RocketMQListener<Order> {

    @Override
    public void onMessage(Order order) {
        // 30分钟后执行：检查订单支付状态
        if (order.getStatus().equals("UNPAID")) {
            // 取消订单
            orderService.cancelOrder(order.getId());
            log.info("订单超时未支付，已自动取消: {}", order.getId());
        }
    }
}
```

---

### 5.5 带标签消息

**特点**: 消费者可以通过标签过滤消息

**应用场景**:
- 区分不同类型的消息（VIP订单、普通订单）
- 实现消息过滤

```java
@Service
public class OrderService {

    public void createOrder(Order order) {
        // 根据用户等级发送不同标签的消息
        String tag = order.isVip() ? "VIP" : "NORMAL";

        RMSendUtil.sendWithTag("order-topic", tag, order);

        log.info("订单消息发送成功，标签: {}", tag);
    }
}
```

**消费者示例（只消费 VIP 订单）**:
```java
@Component
@RocketMQMessageListener(
    consumerGroup = "vip-order-consumer",
    topic = "order-topic",
    selectorExpression = "VIP"  // 只消费 VIP 标签的消息
)
public class VipOrderConsumer implements RocketMQListener<Order> {

    @Override
    public void onMessage(Order order) {
        log.info("收到VIP订单: {}", order.getId());
        // VIP 订单特殊处理逻辑
    }
}
```

---

### 5.6 批量消息

**特点**: 批量发送，提高性能

```java
@Service
public class OrderService {

    public void batchCreateOrders(List<Order> orders) {
        // 保存订单
        orderDao.batchInsert(orders);

        // 批量发送订单消息
        RMSendUtil.sendBatch("order-topic", orders);

        log.info("批量发送 {} 条订单消息", orders.size());
    }
}
```

---

### 5.7 事务消息

**特点**: 保证本地事务与消息发送的一致性

**应用场景**:
- 分布式事务
- 最终一致性保证

#### 步骤 1: 发送事务消息

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(Order order) {
        // 发送事务消息（半消息）
        RMSendUtil.sendTransaction("order-topic", order, String.valueOf(order.getId()));

        // 注意：本地事务在 TransactionListener 中执行
    }
}
```

#### 步骤 2: 实现事务监听器

```java
@Component
@RocketMQTransactionListener
public class OrderTransactionListener implements RocketMQLocalTransactionListener {

    @Autowired
    private OrderService orderService;

    /**
     * 执行本地事务
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String transactionId = (String) msg.getHeaders().get("transactionId");
        log.info("执行本地事务: transactionId={}", transactionId);

        try {
            // 执行本地事务：保存订单到数据库
            Order order = (Order) msg.getPayload();
            orderService.saveOrder(order);

            log.info("本地事务执行成功，提交消息");
            return RocketMQLocalTransactionState.COMMIT;

        } catch (Exception e) {
            log.error("本地事务执行失败，回滚消息", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 回查本地事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String transactionId = (String) msg.getHeaders().get("transactionId");
        log.info("回查本地事务状态: transactionId={}", transactionId);

        // 查询数据库检查订单是否已保存
        boolean exists = orderService.checkOrderExists(Long.parseLong(transactionId));

        if (exists) {
            log.info("订单已存在，提交消息");
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            log.warn("订单不存在，回滚消息");
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
```

---

## 六、测试接口

项目提供了完整的测试接口，位置：`RocketMQTestController`

### 6.1 消息发送测试

| 接口 | 方法 | 说明 |
|------|------|------|
| `/rocketmq/test/send/sync` | GET | 同步发送测试 |
| `/rocketmq/test/send/async` | GET | 异步发送测试 |
| `/rocketmq/test/send/oneway` | GET | 单向发送测试 |
| `/rocketmq/test/send/delay?delayLevel=3` | GET | 延迟消息测试（数字级别） |
| `/rocketmq/test/send/delay/enum` | GET | 延迟消息测试（枚举） |
| `/rocketmq/test/send/withTag?tag=VIP` | GET | 带标签消息测试 |
| `/rocketmq/test/send/batch?count=10` | GET | 批量发送测试 |
| `/rocketmq/test/send/transaction` | GET | 事务消息测试 |

### 6.2 Topic 管理测试

| 接口 | 方法 | 说明 |
|------|------|------|
| `/rocketmq/test/topic/create?topicName=xxx&queueNum=8` | GET | 创建 Topic |
| `/rocketmq/test/topic/delete?topicName=xxx` | GET | 删除 Topic |
| `/rocketmq/test/topic/list` | GET | 查询所有 Topic |
| `/rocketmq/test/topic/verify?topicName=xxx` | GET | 验证 Topic 路由 |

### 6.3 诊断测试

| 接口 | 方法 | 说明 |
|------|------|------|
| `/rocketmq/test/diagnose` | GET | 诊断 RocketMQ 状态 |
| `/rocketmq/test/verify/route` | GET | 验证 Topic 路由信息 |

### 6.4 测试示例

```bash
# 测试同步发送
curl http://localhost:5500/rocketmq/test/send/sync

# 测试延迟消息（10秒延迟）
curl http://localhost:5500/rocketmq/test/send/delay?delayLevel=3

# 创建 Topic
curl "http://localhost:5500/rocketmq/test/topic/create?topicName=my-topic&queueNum=16"

# 查询所有 Topic
curl http://localhost:5500/rocketmq/test/topic/list

# 诊断 RocketMQ
curl http://localhost:5500/rocketmq/test/diagnose
```

---

## 七、常见问题与解决方案

### 7.1 No route info of this topic

**问题描述**: 发送消息时报错 `No route info of this topic`

**原因分析**:
1. Topic 不存在
2. Broker 未注册到 NameServer
3. 路由信息未同步

**解决方案**:

```java
// 方案1: 自动创建 Topic（开发环境）
RMSendUtil.sendWithAutoCreate("order-topic", message);

// 方案2: 手动创建 Topic
RMTopicUtil.createTopic("order-topic");

// 方案3: 验证路由信息
boolean isValid = RMTopicUtil.verifyTopicRoute("order-topic");
if (!isValid) {
    log.warn("Topic 路由不可用，等待30秒后重试");
    Thread.sleep(30000);
}

// 方案4: 重启 Broker 服务
docker-compose -f RocketMQServer-compose.yml restart rmq-broker-master
```

---

### 7.2 Connect to <服务器IP:10909> failed

**问题描述**: 客户端连接 Broker 失败

**原因分析**:
1. Broker 配置的 `brokerIP1` 不正确
2. 防火墙端口未开放
3. 网络不通

**解决方案**:

```bash
# 1. 修改 Broker 配置
# conf/broker-a.conf
brokerIP1=<服务器实际公网IP>

# 2. 开放防火墙端口
firewall-cmd --zone=public --add-port=9876/tcp --permanent
firewall-cmd --zone=public --add-port=10909/tcp --permanent
firewall-cmd --zone=public --add-port=10911/tcp --permanent
firewall-cmd --zone=public --add-port=10912/tcp --permanent
firewall-cmd --reload

# 3. 重启 RocketMQ
docker-compose -f RocketMQServer-compose.yml down
docker-compose -f RocketMQServer-compose.yml up -d
```

---

### 7.3 RocketMQTemplate 未初始化

**问题描述**: 使用工具类时报错 `RocketMQTemplate 未初始化`

**原因分析**:
1. `rocketmq.enabled` 未设置为 `true`
2. RocketMQ 依赖未引入
3. Spring 容器未启动

**解决方案**:

```yaml
# application.yml
rocketmq:
  enabled: true  # ⚠️ 必须设置为 true
  name-server: 127.0.0.1:9876
```

---

### 7.4 消息发送超时

**问题描述**: 发送消息超时 `RemotingTooMuchRequestException`

**原因分析**:
1. Broker 压力过大
2. 网络延迟高
3. 超时时间设置过短

**解决方案**:

```yaml
# application.yml
rocketmq:
  producer:
    send-message-timeout: 5000  # 增加超时时间
```

```java
// 代码中指定超时时间
SendResult result = RMSendUtil.send("order-topic", message, 10000);
```

---

### 7.5 消息消费失败

**问题描述**: 消费者消费消息失败，消息不断重试

**原因分析**:
1. 消费逻辑异常
2. 数据库连接失败
3. 依赖服务不可用

**解决方案**:

```java
@Component
@RocketMQMessageListener(
    consumerGroup = "order-consumer",
    topic = "order-topic",
    maxReconsumeTimes = 3  // 最大重试次数
)
public class OrderConsumer implements RocketMQListener<Order> {

    @Override
    public void onMessage(Order order) {
        try {
            // 消费逻辑
            processOrder(order);

        } catch (Exception e) {
            log.error("消息消费失败: orderId={}", order.getId(), e);

            // 记录失败消息到数据库
            saveFailedMessage(order, e.getMessage());

            // 抛出异常触发重试（或返回 RECONSUME_LATER）
            throw new RuntimeException("消息消费失败", e);
        }
    }
}
```

---

## 八、生产环境建议

### 8.1 服务器配置

**硬件要求**:
- CPU: 4核+
- 内存: 8GB+
- 磁盘: SSD 100GB+
- 网络: 千兆网卡

**JVM 参数优化**:
```bash
# NameServer
NAMESRV_JAVA_OPTS=-Xms1g -Xmx1g -Xmn512m

# Broker Master
BROKER_MASTER_JAVA_OPTS=-Xms4g -Xmx4g -Xmn2g

# Broker Slave
BROKER_SLAVE_JAVA_OPTS=-Xms4g -Xmx4g -Xmn2g
```

---

### 8.2 安全配置

#### 1️⃣ 启用 Dashboard 登录认证

```yaml
# .env
DASHBOARD_LOGIN_ENABLED=true
```

然后在 Dashboard 配置文件中设置用户名密码。

#### 2️⃣ 限制访问 IP

```bash
# 防火墙规则：只允许应用服务器访问
firewall-cmd --zone=public --add-rich-rule='rule family="ipv4" source address="应用服务器IP" port protocol="tcp" port="9876" accept' --permanent
firewall-cmd --reload
```

#### 3️⃣ 关闭自动创建 Topic

```properties
# conf/broker-a.conf
autoCreateTopicEnable=false
```

```yaml
# application.yml
rocketmq:
  producer:
    auto-create-topic: false
```

---

### 8.3 监控告警

#### 1️⃣ Dashboard 监控

- 集群状态监控
- Broker 负载监控
- Topic 流量监控
- 消费进度监控

#### 2️⃣ 日志监控

```bash
# 查看 Broker 日志
docker-compose -f RocketMQServer-compose.yml logs -f rmq-broker-master

# 查看错误日志
grep ERROR data/broker-master/logs/rocketmqlogs/broker.log
```

#### 3️⃣ 告警配置

建议配置以下告警：
- Broker 宕机告警
- 消息堆积告警
- 消费失败告警
- 磁盘空间告警

---

### 8.4 性能优化

#### 1️⃣ 批量发送

```java
// 使用批量发送代替单条发送
List<Order> orders = getOrders();
RMSendUtil.sendBatch("order-topic", orders);
```

#### 2️⃣ 异步发送

```java
// 非关键消息使用异步发送
RMSendUtil.sendAsync("log-topic", logMessage, result -> {
    log.debug("日志消息发送成功");
});
```

#### 3️⃣ 单向发送

```java
// 日志等不重要消息使用单向发送
RMSendUtil.sendOneWay("log-topic", logMessage);
```

#### 4️⃣ 消息压缩

```yaml
# application.yml
rocketmq:
  producer:
    compress-message-body-threshold: 4096  # 超过 4KB 自动压缩
```

---

### 8.5 备份与恢复

#### 1️⃣ 数据备份

```bash
# 定时备份数据目录
tar -czf rocketmq-backup-$(date +%Y%m%d).tar.gz data/

# 上传到备份服务器
scp rocketmq-backup-*.tar.gz backup-server:/backup/rocketmq/
```

#### 2️⃣ 数据恢复

```bash
# 停止服务
docker-compose -f RocketMQServer-compose.yml down

# 恢复数据
tar -xzf rocketmq-backup-20250101.tar.gz

# 启动服务
docker-compose -f RocketMQServer-compose.yml up -d
```

---

## 📚 参考资料

- [RocketMQ 官方文档](https://rocketmq.apache.org/docs/introduction/01quickstart)
- [RocketMQ Spring Boot Starter](https://github.com/apache/rocketmq-spring)
- [RocketMQ Dashboard](https://github.com/apache/rocketmq-dashboard)

---

## 📝 更新日志

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.1.0 | 2025-11-07 | 调整为可选依赖策略，需业务模块显式引入依赖 |
| v1.0.0 | 2025-11-03 | 初始版本，完整的部署与使用指南 |

---

**文档维护者**: 路北
**最后更新**: 2025-11-07
