---
name: scheduled-jobs
description: |
  定时任务开发指南。涵盖 Redisson 延迟队列、@Scheduled、SnailJob 三种方案，支持分布式任务调度、失败重试、工作流编排。

  触发场景：
  - 订单自动取消、支付回调重试等事件驱动场景（Redisson 延迟队列）
  - 每日数据汇总、定期清理等周期性任务（@Scheduled）
  - 分布式复杂业务、失败重试、可视化管理（SnailJob）
  - 任务分片、MapReduce 分布式计算

  触发词：定时任务、SnailJob、延迟队列、任务调度、重试机制、工作流、@JobExecutor、@Scheduled、Redisson、分布式任务

  核心特性：
  - 方案 0：Redisson 延迟队列（毫秒级延迟、事件驱动）
  - 方案 1：@Scheduled（简单周期任务、框架内置）
  - 方案 2：SnailJob（分布式集群、可视化管理、失败重试、工作流编排）
---

# 定时任务开发技能指南

## 核心概念

### 什么时候使用定时任务？

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| 订单自动取消（事件驱动、延迟） | **Redisson 延迟队列** | 基于订单创建事件，反应式触发，支持大量订单 |
| 每日数据汇总（分布式、重试） | **SnailJob** | 需要分布式调度、失败重试 |
| 报表生成（每天凌晨） | `@Scheduled` | 周期固定、简单逻辑、框架内置 |
| 复杂业务流程调度 | **SnailJob** | 支持工作流、失败重试、告警 |
| 库存对账（关键业务） | **SnailJob** | 需要可靠性、失败通知 |
| 定期清理日志 | `@Scheduled` | 简单、低频率 |
| 支付回调重试 | **SnailJob** | 需要灵活重试策略、幂等性 |
| 批量数据处理（海量） | **SnailJob Map/MapReduce** | 支持分片、分布式计算 |

### @Scheduled vs SnailJob 对比

| 特性 | @Scheduled | SnailJob |
|------|-----------|----------|
| **部署** | 单机/集群 | 分布式集群 |
| **可视化管理** | ❌ | ✅ Web 界面 |
| **失败重试** | ❌ 不支持 | ✅ 完整支持 |
| **工作流编排** | ❌ | ✅ 可视化流程 |
| **任务监控** | ❌ 需自己写日志 | ✅ 实时日志、告警 |
| **调度粒度** | CRON/固定频率 | CRON/秒级/固定频率 |
| **分布式模式** | ❌ | ✅ 集群/广播/分片/Map/MapReduce |
| **容错能力** | 低（单点故障） | 高（可自动转移） |
| **学习成本** | 低（框架内置） | 中（需学习控制台） |
| **部署成本** | 最低（无依赖） | 低（仅依赖数据库） |
| **适用规模** | <1万任务 | 10万+任务 |

**决策树**：
```
任务数 < 100 且逻辑简单？
├─ 是 → @Scheduled
└─ 否 → SnailJob 必需要求的特性？
       ├─ 需要可视化管理、失败重试、工作流 → SnailJob
       └─ 仅需简单定时、无特殊需求 → @Scheduled
```

---

## 方案 0：使用 Redisson 延迟队列（事件驱动、大量订单）

### 核心特性

**Redisson 延迟队列**是基于 Redis 的队列实现，特别适合以下场景：
- ✅ 基于事件触发的延迟任务（订单创建 → 1小时后自动取消）
- ✅ 大量并发任务（每秒数千个订单）
- ✅ 无需持久化、快速响应
- ✅ 支持分布式部署（所有节点共用 Redis）

### 快速开始

#### Step 1：添加依赖

```xml
<!-- 项目已集成 Redisson，无需额外添加 -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.52.0</version>
</dependency>
```

#### Step 2：订单创建时添加延迟任务

```java
import plus.ruoyi.common.redis.utils.QueueUtils;
import java.util.concurrent.TimeUnit;
import lombok.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private IOrderDao orderDao;

    @Override
    public Long add(OrderBo bo) {
        // 创建订单
        Order order = MapstructUtils.convert(bo, Order.class);
        orderDao.insert(order);

        // ✅ 添加延迟任务：1小时后自动取消
        // 使用 QueueUtils 添加延迟队列（项目封装的 Redis 工具）
        QueueUtils.addDelayedQueueObject(
            "order_cancel_queue",
            order.getId().toString(),
            1,
            TimeUnit.HOURS
        );

        log.info("订单已创建，1小时后将自动取消，订单ID: {}", order.getId());
        return order.getId();
    }
}
```

#### Step 3：异步消费队列中的任务

```java
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import plus.ruoyi.common.redis.utils.QueueUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import lombok.Slf4j;

@Slf4j
@Component
public class OrderCancelConsumer implements ApplicationRunner {

    @Autowired
    private IOrderService orderService;

    @Override
    public void run(ApplicationArguments args) {
        // ✅ 使用订阅模式消费延迟队列（异步事件驱动）
        QueueUtils.subscribeBlockingQueue(
            "order_cancel_queue",
            this::processOrderCancellation,
            true  // isDelayed = true，表示延迟队列
        );
    }

    // ✅ 异步处理函数，返回 CompletionStage<Void>
    private CompletionStage<Void> processOrderCancellation(String orderId) {
        return CompletableFuture.runAsync(() -> {
            log.info("处理超期订单，订单ID: {}", orderId);
            try {
                orderService.cancelUnpaidOrder(Long.parseLong(orderId));
                log.info("订单已自动取消，订单ID: {}", orderId);
            } catch (Exception e) {
                log.error("取消订单失败，订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
                // 失败重试：30秒后重新入队
                QueueUtils.addDelayedQueueObject(
                    "order_cancel_queue",
                    orderId,
                    30,
                    TimeUnit.SECONDS
                );
            }
        });
    }
}
```

### Redisson 延迟队列 vs @Scheduled vs SnailJob

| 特性 | Redisson | @Scheduled | SnailJob |
|------|----------|-----------|----------|
| **触发方式** | 事件驱动（反应式） | 周期执行（主动式） | 定点执行（主动式） |
| **适用场景** | 订单取消、支付超时 | 每日报表、定期清理 | 复杂业务、分布式事务 |
| **延迟精度** | 毫秒级 | 秒级以上 | 秒级 |
| **可扩展性** | 支持大量任务 | 受线程池限制 | 支持数十万任务 |
| **持久化** | 依赖 Redis 持久化 | 无持久化 | Web 持久化、可视化 |
| **失败重试** | 手动实现 | ❌ | ✅ 完整支持 |
| **部署成本** | 仅需 Redis | 无依赖 | 需部署 SnailJob 服务 |
| **学习成本** | 低 | 低 | 中 |

### 最佳实践

```java
// ✅ 推荐：使用 OrderCancelMessage 对象而非字符串
public class OrderCancelMessage {
    private Long orderId;
    private LocalDateTime createTime;
    private Integer retryCount;
}

// 添加任务时
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private IOrderDao orderDao;

    @Override
    public Long add(OrderBo bo) {
        Order order = MapstructUtils.convert(bo, Order.class);
        orderDao.insert(order);

        // ✅ 使用 QueueUtils 添加带重试计数的消息对象
        OrderCancelMessage message = new OrderCancelMessage();
        message.setOrderId(order.getId());
        message.setCreateTime(LocalDateTime.now());
        message.setRetryCount(0);

        QueueUtils.addDelayedQueueObject(
            "order_cancel_queue",
            message,
            1,
            TimeUnit.HOURS
        );

        log.info("订单已创建，1小时后将自动取消，订单ID: {}", order.getId());
        return order.getId();
    }
}

// ✅ 消费时（新模式：ApplicationRunner + 订阅）
@Slf4j
@Component
public class OrderCancelConsumer implements ApplicationRunner {

    @Autowired
    private IOrderService orderService;

    @Override
    public void run(ApplicationArguments args) {
        // ✅ 使用订阅模式消费延迟队列（异步事件驱动）
        QueueUtils.subscribeBlockingQueue(
            "order_cancel_queue",
            this::processOrderCancellation,
            true  // isDelayed = true，表示延迟队列
        );
    }

    // ✅ 异步处理函数，返回 CompletionStage<Void>
    private CompletionStage<Void> processOrderCancellation(Object messageObj) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 处理类型转换（支持 OrderCancelMessage 对象或字符串 ID）
                Long orderId;
                if (messageObj instanceof OrderCancelMessage) {
                    OrderCancelMessage message = (OrderCancelMessage) messageObj;
                    orderId = message.getOrderId();
                } else if (messageObj instanceof String) {
                    orderId = Long.parseLong(messageObj.toString());
                } else {
                    // JSON 反序列化
                    OrderCancelMessage message = JSON.parseObject(messageObj.toString(), OrderCancelMessage.class);
                    orderId = message.getOrderId();
                }

                log.info("处理超期订单，订单ID: {}", orderId);
                orderService.cancelUnpaidOrder(orderId);
                log.info("订单已自动取消，订单ID: {}", orderId);

            } catch (Exception e) {
                log.error("取消订单失败，错误: {}", e.getMessage(), e);
                // 失败重试：30秒后重新入队
                if (messageObj != null) {
                    QueueUtils.addDelayedQueueObject(
                        "order_cancel_queue",
                        messageObj,
                        30,
                        TimeUnit.SECONDS
                    );
                }
            }
        });
    }
}
```

### 常见问题

| 问题 | 解决方案 |
|------|---------|
| Redis 宕机，延迟任务丢失 | 使用 Redis 持久化（RDB/AOF）或 Sentinel 高可用 |
| 消费线程异常导致任务堆积 | 添加监控告警，异常时自动重启消费线程 |
| 大量订单导致 Redis 内存溢出 | 使用 Redis 集群扩容，或定期清理已完成的订单 |
| 订单取消幂等性 | 检查订单状态，已取消则直接返回 |

---

## 方案 1：使用 @Scheduled（简单场景）

### 快速开始

```java
import org.springframework.scheduling.annotation.Scheduled;
import lombok.Slf4j;

@Slf4j
@Component
public class OrderScheduledTask {

    @Autowired
    private IOrderService orderService;

    // ✅ 每天凌晨 2 点执行一次（CRON）
    @Scheduled(cron = "0 0 2 * * ?")
    public void cancelExpiredOrders() {
        log.info("开始清理过期订单");
        try {
            orderService.cancelExpiredOrders();
            log.info("过期订单清理完成");
        } catch (Exception e) {
            log.error("清理过期订单异常", e);
            // 业务监控、告警等
        }
    }

    // ✅ 固定频率：每隔 60 秒执行一次
    @Scheduled(fixedRate = 60000)
    public void syncInventory() {
        log.info("开始同步库存");
        inventoryService.sync();
    }

    // ✅ 固定延迟：上次执行结束后延迟 30 秒再执行
    @Scheduled(fixedDelay = 30000)
    public void checkDeviceStatus() {
        log.info("检查设备在线状态");
        deviceService.checkOnlineStatus();
    }

    // ✅ 初始延迟：启动 10 秒后才第一次执行
    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    public void reportDailyStats() {
        log.info("生成日报表");
        reportService.generateDailyReport();
    }
}
```

### CRON 表达式说明

```
┌───────────── 秒 (0 - 59)
│ ┌───────────── 分钟 (0 - 59)
│ │ ┌───────────── 小时 (0 - 23)
│ │ │ ┌───────────── 日期 (1 - 31)
│ │ │ │ ┌───────────── 月份 (1 - 12)
│ │ │ │ │ ┌───────────── 星期 (0 - 7，0 和 7 都是周日)
│ │ │ │ │ │
│ │ │ │ │ │
* * * * * *
```

| 表达式 | 说明 |
|--------|------|
| `0 0 2 * * ?` | 每天 2:00:00 执行 |
| `0 */5 * * * ?` | 每 5 分钟执行一次 |
| `0 0 */6 * * ?` | 每 6 小时执行一次 |
| `0 0 0 * * MON` | 每周一 0:00:00 执行 |
| `0 0 0 1 * ?` | 每月 1 号 0:00:00 执行 |
| `0 0 0 ? * MON-FRI` | 每周一至五 0:00:00 执行 |
| `0 0 0 L * ?` | 每月最后一天 0:00:00 执行 |

### 配置启用

```yaml
# application.yml
spring:
  task:
    scheduling:
      # 线程池大小
      pool:
        size: 10
      # 任务名前缀
      thread-name-prefix: scheduled-
      # 线程池关闭前等待时间
      shutdown:
        await-termination: true
        await-termination-period: 60s
      # 未捕获异常处理
      execution:
        pool:
          queue-capacity: 100
```

---

## 方案 2：使用 SnailJob（分布式复杂场景）

### 核心特性对标

| 特性 | 支持程度 | 说明 |
|------|---------|------|
| **调度方式** | ⭐⭐⭐⭐⭐ | CRON、秒级、固定频率都支持 |
| **执行模式** | ⭐⭐⭐⭐⭐ | 集群、广播、分片、Map、MapReduce |
| **失败重试** | ⭐⭐⭐⭐⭐ | 多策略：指数退避、固定间隔、CRON |
| **任务监控** | ⭐⭐⭐⭐⭐ | 可视化界面、实时日志 |
| **工作流编排** | ⭐⭐⭐⭐⭐ | 可视化流程、决策节点 |
| **告警通知** | ⭐⭐⭐⭐⭐ | 邮件、钉钉、飞书、企微、Webhook |
| **死信队列** | ⭐⭐⭐⭐⭐ | 自动失败转入、手动回滚 |
| **幂等性** | ⭐⭐⭐⭐⭐ | 支持业务幂等ID |
| **分布式锁** | ⭐⭐⭐⭐ | 防止同时执行 |

### 快速集成

#### Step 1：添加依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.aizuda</groupId>
    <artifactId>snail-job-spring-boot-starter</artifactId>
    <version>2.6.0</version>
</dependency>
```

#### Step 2：配置

```yaml
# application-dev.yml（开发环境配置）
snail-job:
  # 是否启用定时任务
  enabled: ${SNAIL_JOB_ENABLED:false}
  # 应用分组（需要在 SnailJob 后台组管理创建对应名称的组）
  group: ${app.id}
  # SnailJob 接入验证令牌（详见 script/sql/ry_job.sql `sj_group_config` 表）
  token: ${SNAIL_JOB_TOKEN:SJ_cKqBTPzCsWA3VyuCfFoccmuIEGXjr5KT}
  # 调度中心服务器配置
  server:
    # 调度中心地址（⚠️ 环境变量必须带 SERVER 前缀，避免被 Relaxed Binding 映射为 snail-job.host 覆盖客户端配置）
    host: ${SNAIL_JOB_SERVER_HOST:127.0.0.1}
    # 调度中心端口（同理，避免覆盖 snail-job.port 客户端端口）
    port: ${SNAIL_JOB_SERVER_PORT:17888}
  # 命名空间UUID（详见 script/sql/ry_job.sql `sj_namespace` 表 `unique_id` 字段）
  namespace: ${spring.profiles.active}
  # 客户端配置
  # 随主应用端口漂移（如主应用 5500，则客户端为 25500）
  port: 2${server.port}
  # 客户端 IP 指定（为空则自动检测）
  host: ${SNAIL_JOB_CLIENT_HOST:}
  # RPC 类型（netty 或 grpc）
  rpc-type: grpc
```

**配置说明**：
- `enabled`: 是否启用 SnailJob（默认关闭）
- `group`: 应用分组（对应后台的组管理）
- `token`: 认证令牌（从数据库表获取）
- `server.host/port`: SnailJob 调度中心地址
- `port`: 客户端通信端口（需要随主应用端口漂移）
- `rpc-type`: 推荐使用 grpc（性能更好）

#### Step 3：定义任务执行器

```java
import com.aizuda.snailjob.client.core.annotation.JobExecutor;
import lombok.Slf4j;

@Slf4j
@Component
public class OrderJobExecutors {

    @Autowired
    private IOrderService orderService;

    // ✅ 定义一个任务执行器
    // 任务 ID = "cancelExpiredOrders"（必须全局唯一）
    @JobExecutor(name = "cancelExpiredOrders")
    public void cancelExpiredOrders(String jobContext) {
        log.info("开始清理过期订单, 任务上下文: {}", jobContext);
        try {
            orderService.cancelExpiredOrders();
            log.info("清理过期订单完成");
        } catch (Exception e) {
            log.error("清理过期订单失败", e);
            throw e;  // SnailJob 会捕获异常并重试
        }
    }

    // ✅ 带参数的任务
    @JobExecutor(name = "generateDailyReport")
    public void generateDailyReport(String reportType) {
        log.info("生成日报表, 类型: {}", reportType);
        reportService.generateReport(reportType);
    }

    // ✅ 集群模式任务（多个节点竞争执行，只有一个节点执行）
    @JobExecutor(name = "syncInventory")
    public void syncInventory(String jobContext) {
        log.info("同步库存数据");
        inventoryService.sync();
    }

    // ✅ 广播模式任务（所有节点都执行）
    @JobExecutor(name = "clearCache")
    public void clearCache(String jobContext) {
        log.info("清理本机缓存");
        cacheService.clearAll();
    }

    // ✅ 分片模式任务（任务被分成多个分片，不同节点执行不同分片）
    @JobExecutor(name = "processUserData")
    public void processUserData(String jobContext) {
        // jobContext 包含分片信息：{"shardIndex": 0, "shardTotal": 3}
        log.info("处理用户数据, 分片上下文: {}", jobContext);
        userService.processShardData(jobContext);
    }
}
```

### 在 SnailJob 控制台创建任务

**访问地址**：`http://localhost:8080/snail-job`（假设部署在 8080）

**创建流程**：
1. **新增任务** → **集群模式**
2. 设置基本信息：
   - 任务 ID：`cancelExpiredOrders`（必须与 @JobExecutor 一致）
   - 任务描述：清理过期订单
   - 执行器名称：选择对应的应用
3. 设置调度方式：
   - 触发类型：**CRON**
   - CRON 表达式：`0 0 2 * * ?`（每天 2 点）
4. 设置重试策略：
   - 重试次数：3
   - 重试间隔：60 秒
   - 退避策略：指数退避
5. 点击发布，任务启动

---

## SnailJob 任务执行模式详解

### 1️⃣ 集群模式（Cluster）

**特点**：多个节点竞争执行，只有一个节点执行任务。

**适用场景**：
- 订单自动取消
- 库存数据汇总
- 数据备份

**配置示例**：
```
触发类型：CRON
CRON 表达式：0 0 2 * * ?
执行器类型：JAVA
路由策略：轮询（Round-Robin）
```

**代码**：
```java
@JobExecutor(name = "cancelExpiredOrders")
public void cancelExpiredOrders(String jobContext) {
    // 只有一个节点执行
    log.info("取消过期订单");
}
```

### 2️⃣ 广播模式（Broadcast）

**特点**：所有节点都执行该任务，通常用于节点本地操作。

**适用场景**：
- 清理本机缓存
- 更新本机配置
- 节点健康检查

**配置示例**：
```
触发类型：CRON
执行器类型：JAVA
执行模式：广播
阻塞策略：并行（所有节点同时执行）
```

**代码**：
```java
@JobExecutor(name = "clearCache")
public void clearCache(String jobContext) {
    // 每个节点都会执行
    log.info("本机清理缓存");
    cacheService.clearAll();
}
```

### 3️⃣ 静态分片模式（Static Sharding）

**特点**：任务被静态分成多个分片（如 4 个），不同节点执行不同分片。

**适用场景**：
- 批量数据处理（用户、订单、商品）
- 数据迁移
- 大数据量统计

**配置示例**：
```
执行模式：静态分片
分片总数：4
并行数：2（同时执行 2 个分片）
路由策略：一致性哈希（同一分片总是路由到同一节点）
```

**代码**：
```java
@JobExecutor(name = "processUserData")
public void processUserData(String jobContext) {
    // jobContext = {"shardIndex": 0, "shardTotal": 4}
    // 表示处理第 0 个分片，共 4 个分片

    JSONObject shardInfo = JSON.parseObject(jobContext);
    int shardIndex = shardInfo.getInteger("shardIndex");
    int shardTotal = shardInfo.getInteger("shardTotal");

    log.info("处理用户数据，分片 {}/{}", shardIndex, shardTotal);

    // 处理当前分片的数据
    userService.processShardData(shardIndex, shardTotal);
}
```

### 4️⃣ Map 模式

**特点**：先 map，后由用户决定是否 reduce。

**适用场景**：
- 数据映射转换
- 数据预处理

### 5️⃣ MapReduce 模式

**特点**：先 map 分片，再 reduce 聚合结果。

**适用场景**：
- 分布式计算（求和、平均、排序）
- 数据分析（Top N、分组统计）
- 报表生成

**配置示例**：
```
执行模式：MapReduce
子任务数：10（map 分成 10 个小任务）
```

**代码**：
```java
// Map 阶段：分片处理
@JobExecutor(name = "mapComputeRevenue")
public void mapComputeRevenue(String jobContext) {
    JSONObject shardInfo = JSON.parseObject(jobContext);
    int shardIndex = shardInfo.getInteger("shardIndex");
    int shardTotal = shardInfo.getInteger("shardTotal");

    // 计算当前分片的收入
    BigDecimal shardRevenue = orderService.computeRevenue(shardIndex, shardTotal);

    // 回传结果给 SnailJob（用于 reduce 阶段）
    return shardRevenue.toPlainString();
}

// Reduce 阶段：汇总结果
@JobExecutor(name = "reduceComputeRevenue")
public void reduceComputeRevenue(String jobContext) {
    // 接收所有 map 阶段的结果并汇总
    List<String> mapResults = getMapResults();  // SnailJob 提供
    BigDecimal totalRevenue = mapResults.stream()
        .map(BigDecimal::new)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info("总收入: {}", totalRevenue);
}
```

---

## SnailJob 重试机制与异常处理

### 重试策略

| 策略 | 说明 | 使用场景 |
|------|------|---------|
| **固定间隔** | 每次间隔时间相同 | 网络抖动、偶发错误 |
| **指数退避** | 间隔时间逐倍增加 | 目标服务恢复中（逐步探测） |
| **CRON** | 按 CRON 表达式重试 | 定点重试（如每天凌晨重试） |

### 异常配置

```java
import com.aizuda.snailjob.client.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.core.annotation.Retryable;
import lombok.Slf4j;

@Slf4j
@Component
public class PaymentJobExecutors {

    @Autowired
    private IPaymentService paymentService;

    // ✅ 基础重试配置
    @JobExecutor(name = "reconcilePayment")
    @Retryable(
        localTimes = 3,                    // 本地重试 3 次
        localInterval = 60,                // 本地重试间隔 60 秒
        bizNo = "#orderId",                // 业务号（确保幂等性）
        idempotentId = "#orderId"          // 幂等 ID
    )
    public void reconcilePayment(String orderId) {
        log.info("开始支付对账, 订单: {}", orderId);
        try {
            paymentService.reconcile(orderId);
        } catch (Exception e) {
            log.error("对账失败: {}", e.getMessage());
            throw e;  // SnailJob 会自动重试
        }
    }

    // ✅ 异常白名单（遇到这些异常不重试，直接失败）
    @JobExecutor(name = "validateUser")
    @Retryable(
        localTimes = 3,
        // 这些异常不会触发重试
        excludeException = {
            IllegalArgumentException.class,
            IllegalStateException.class
        }
    )
    public void validateUser(String userId) {
        User user = userService.getById(userId);
        if (user == null) {
            // 这个异常会被重试
            throw new NullPointerException("用户不存在");
        }
        // 这个异常不会被重试（白名单中）
        if (!user.isValid()) {
            throw new IllegalStateException("用户非法");
        }
    }

    // ✅ 异常黑名单（仅重试这些异常，其他异常不重试）
    @JobExecutor(name = "callThirdPartyApi")
    @Retryable(
        localTimes = 3,
        // 只重试这些异常
        includeException = {
            SocketTimeoutException.class,
            ConnectException.class,
            IOException.class
        }
    )
    public void callThirdPartyApi(String apiUrl) {
        try {
            httpClient.get(apiUrl);
        } catch (SocketTimeoutException e) {
            // 会重试
            throw e;
        } catch (BusinessException e) {
            // 不会重试（黑名单中没有）
            throw e;
        }
    }
}
```

### 死信队列处理

当任务重试多次仍然失败时，会进入死信队列。可以在 SnailJob 控制台手动回滚或自动处理。

```java
// 定义死信处理任务
@JobExecutor(name = "handlePaymentDeadLetter")
public void handlePaymentDeadLetter(String jobContext) {
    // jobContext 包含失败任务的信息
    log.info("处理支付对账死信: {}", jobContext);

    // 人工审核或告警
    alertService.sendAlert("支付对账失败，需要人工处理: " + jobContext);
}
```

---

## 与 ruoyi-plus 框架集成

### 建议的项目结构

```
ruoyi-modules/ruoyi-business/src/main/java/plus/ruoyi/business/
├── job/                           # 定时任务模块（新增）
│   ├── controller/
│   │   └── SnailJobController.java    # 任务管理 API（可选）
│   ├── executor/
│   │   ├── BaseJobExecutors.java      # 基础任务执行器
│   │   ├── OrderJobExecutors.java     # 订单相关任务
│   │   ├── PaymentJobExecutors.java   # 支付相关任务
│   │   └── ReportJobExecutors.java    # 报表相关任务
│   └── listener/
│       └── JobExecutionListener.java  # 任务事件监听（可选）
```

### 最佳实践：分离执行器

```java
// 基础执行器父类
@Slf4j
public abstract class BaseJobExecutor {

    protected void executeWithErrorHandling(String jobName, Consumer<String> task, String context) {
        log.info("[{}] 任务开始执行, 上下文: {}", jobName, context);
        long startTime = System.currentTimeMillis();

        try {
            task.accept(context);
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] 任务执行成功, 耗时: {}ms", jobName, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] 任务执行失败, 耗时: {}ms, 错误: {}", jobName, duration, e.getMessage(), e);
            throw new ServiceException("任务执行失败: " + e.getMessage());
        }
    }
}

// 订单任务执行器
@Slf4j
@Component
public class OrderJobExecutors extends BaseJobExecutor {

    @Autowired
    private IOrderService orderService;

    @JobExecutor(name = "cancelExpiredOrders")
    @Retryable(localTimes = 3, localInterval = 60)
    public void cancelExpiredOrders(String jobContext) {
        executeWithErrorHandling("cancelExpiredOrders", context -> {
            int count = orderService.cancelExpiredOrders();
            log.info("取消订单数: {}", count);
        }, jobContext);
    }

    @JobExecutor(name = "syncOrderStatus")
    public void syncOrderStatus(String jobContext) {
        executeWithErrorHandling("syncOrderStatus", context -> {
            JSONObject shardInfo = JSON.parseObject(context);
            int shardIndex = shardInfo.getInteger("shardIndex");
            int shardTotal = shardInfo.getInteger("shardTotal");

            orderService.syncStatusBySharding(shardIndex, shardTotal);
        }, jobContext);
    }
}
```

### 集成业务服务

```java
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private IOrderDao orderDao;

    @Override
    public int cancelExpiredOrders() {
        // 查询过期订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatusEnum.UNPAID.getValue())
               .le(Order::getCreateTime, LocalDateTime.now().minusHours(1));

        List<Order> expiredOrders = orderDao.selectList(wrapper);

        if (expiredOrders.isEmpty()) {
            log.info("无过期订单");
            return 0;
        }

        // 更新订单状态
        expiredOrders.forEach(order -> {
            order.setStatus(OrderStatusEnum.CANCELED.getValue());
            order.setRemark("系统自动取消");
        });

        orderDao.updateBatch(expiredOrders);

        // 触发业务事件（如退款、库存恢复）
        expiredOrders.forEach(order -> {
            EventPublisher.publishOrderCancelledEvent(order);
        });

        return expiredOrders.size();
    }

    @Override
    public void syncStatusBySharding(int shardIndex, int shardTotal) {
        // 计算当前分片的数据范围
        long pageSize = 1000;
        long offset = shardIndex * pageSize;
        long limit = pageSize;

        List<Order> orders = orderDao.selectPageBySharding(offset, limit);

        for (Order order : orders) {
            // 从三方服务同步订单状态
            OrderStatusDto remoteStatus = thirdPartyService.queryOrderStatus(order.getOrderNo());

            if (!order.getStatus().equals(remoteStatus.getStatus())) {
                order.setStatus(remoteStatus.getStatus());
                orderDao.updateById(order);

                log.info("订单状态已更新: {} -> {}", order.getOrderNo(), remoteStatus.getStatus());
            }
        }
    }
}
```

---

## 常见错误及排查

### ❌ 错误 1：@JobExecutor 名称与控制台不一致

```java
// ❌ 错误：执行器名字不匹配
@JobExecutor(name = "cancelOrder")  // 这里是 cancelOrder
public void cancel(String context) { }

// 控制台配置的是 cancelExpiredOrders
// 结果：任务无法找到执行器，执行失败

// ✅ 正确：保持一致
@JobExecutor(name = "cancelExpiredOrders")
public void cancel(String context) { }
```

### ❌ 错误 2：任务执行时间过长，超过超时设置

```java
// ❌ 错误：任务执行超时
@JobExecutor(name = "processData")
public void processData(String context) {
    // 处理 100 万条数据，耗时 30 分钟
    // 但超时设置只有 10 分钟
    // 结果：任务被中断
    processMillionRecords();
}

// ✅ 正确：使用分片模式处理大数据
@JobExecutor(name = "processData")
public void processData(String context) {
    JSONObject shardInfo = JSON.parseObject(context);
    int shardIndex = shardInfo.getInteger("shardIndex");
    int shardTotal = shardInfo.getInteger("shardTotal");

    // 每个分片处理 100K 条数据，耗时 1 分钟以内
    processSingleShard(shardIndex, shardTotal);
}
```

### ❌ 错误 3：幂等性问题导致重复处理

```java
// ❌ 错误：没有幂等性保证
@JobExecutor(name = "deductBalance")
@Retryable(localTimes = 3)
public void deductBalance(String userId, BigDecimal amount) {
    User user = userService.getById(userId);
    user.setBalance(user.getBalance().subtract(amount));
    userService.update(user);
    // 如果重试，会多次扣费！
}

// ✅ 正确：使用幂等 ID
@JobExecutor(name = "deductBalance")
@Retryable(
    localTimes = 3,
    bizNo = "#transactionId",           // 使用交易 ID 作为业务号
    idempotentId = "#transactionId"     // 幂等 ID
)
public void deductBalance(String transactionId, String userId, BigDecimal amount) {
    // SnailJob 会保证即使重试多次，也只扣费一次
    User user = userService.getById(userId);
    user.setBalance(user.getBalance().subtract(amount));
    userService.update(user);
}
```

### ❌ 错误 4：异常处理不当

```java
// ❌ 错误：吃掉异常，导致无法重试
@JobExecutor(name = "syncData")
@Retryable(localTimes = 3)
public void syncData(String context) {
    try {
        remoteApi.sync();
    } catch (Exception e) {
        log.error("同步失败: {}", e);
        // 没有重新抛出异常！SnailJob 无法识别失败
    }
}

// ✅ 正确：重新抛出异常
@JobExecutor(name = "syncData")
@Retryable(localTimes = 3)
public void syncData(String context) {
    try {
        remoteApi.sync();
    } catch (Exception e) {
        log.error("同步失败: {}", e);
        throw new ServiceException("数据同步失败");  // 重新抛出
    }
}
```

### ❌ 错误 5：使用了尚不支持的 CRON 表达式

```java
// ❌ 错误：6 位表达式（秒级）不被支持
@Scheduled(cron = "0 0 0 0 0 0")  // SnailJob 可能不支持

// ✅ 正确：使用标准 5 位 CRON 或使用 SnailJob 的秒级配置
@Scheduled(cron = "0 0 0 * * ?")  // 标准 5 位

// 或在 SnailJob 控制台选择"秒级"触发类型
```

### ❌ 错误 6：广播模式任务没有做好幂等性

```java
// ❌ 错误：广播任务全局递增 ID，导致不幂等
@JobExecutor(name = "generateReport")
public void generateReport(String context) {
    // 所有节点都生成报表，都用全局 ID
    // 结果：生成重复报表
    Report report = new Report();
    report.setReportNo(generateGlobalId());  // 全局 ID 冲突
    reportService.save(report);
}

// ✅ 正确：使用节点标识确保幂等
@JobExecutor(name = "generateReport")
public void generateReport(String context) {
    Report report = new Report();
    report.setReportNo(UUID.randomUUID().toString());  // 唯一 ID
    report.setNodeId(getLocalNodeId());                // 标记节点
    report.setExecuteTime(LocalDateTime.now());
    reportService.save(report);
}
```

---

## 快速检查清单

### 集成检查

- [ ] **依赖添加** ✅ 已在 pom.xml 中添加 snail-job-starter
- [ ] **配置完整** ✅ application.yml 包含 snail-job 配置
- [ ] **执行器定义** ✅ @JobExecutor 注解正确使用
- [ ] **任务注册** ✅ 在 SnailJob 控制台创建对应任务
- [ ] **名称匹配** ✅ 代码中的执行器名称与控制台一致

### 开发规范

- [ ] **异常处理** ✅ 异常被正确抛出（不被 catch 吞掉）
- [ ] **幂等性** ✅ 使用 bizNo/idempotentId 保证重试幂等
- [ ] **超时设置** ✅ 任务耗时在超时设置范围内
- [ ] **日志记录** ✅ 关键步骤有日志输出
- [ ] **业务校验** ✅ 执行前验证必要条件

### 监控告警

- [ ] **告警配置** ✅ SnailJob 控制台已配置失败告警
- [ ] **告警渠道** ✅ 邮件/钉钉/飞书已配置
- [ ] **死信处理** ✅ 定义了死信任务或人工处理流程
- [ ] **监控指标** ✅ 已接入应用监控系统

### 测试验证

- [ ] **单元测试** ✅ 编写了执行器业务逻辑的单元测试
- [ ] **集成测试** ✅ 在本地测试环境验证过任务执行
- [ ] **手动触发** ✅ 在 SnailJob 控制台手动触发过一次
- [ ] **日志检查** ✅ 查看了执行日志，确认执行成功
- [ ] **异常处理** ✅ 测试过重试、失败等场景

---

## 参考资源

- **SnailJob 官方文档**：https://snailjob.opensnail.com/docs/introduce/preface.html
- **SnailJob GitHub**：https://github.com/aizuda/snail-job
- **Spring @Scheduled**：https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling
- **CRON 表达式生成器**：https://crontab.guru/

---

## 完整示例：电商订单定时任务

```java
@Slf4j
@Component
public class EcommerceJobExecutors extends BaseJobExecutor {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private IInventoryService inventoryService;

    // 📌 每天凌晨 2 点：自动取消 1 小时未支付的订单
    @JobExecutor(name = "cancelUnpaidOrders")
    @Retryable(
        localTimes = 3,
        localInterval = 60,
        bizNo = "cancel-unpaid-orders"
    )
    public void cancelUnpaidOrders(String jobContext) {
        executeWithErrorHandling("cancelUnpaidOrders", context -> {
            int count = orderService.cancelUnpaidOrders(1);  // 1 小时
            log.info("已取消 {} 个未支付订单", count);
        }, jobContext);
    }

    // 📌 每 6 小时：同步订单状态（分片处理 100 万订单）
    @JobExecutor(name = "syncOrderStatus")
    @Retryable(localTimes = 3, localInterval = 300)
    public void syncOrderStatus(String jobContext) {
        executeWithErrorHandling("syncOrderStatus", context -> {
            JSONObject shardInfo = JSON.parseObject(context);
            int shardIndex = shardInfo.getInteger("shardIndex");
            int shardTotal = shardInfo.getInteger("shardTotal");

            int updated = orderService.syncOrderStatusBySharding(shardIndex, shardTotal);
            log.info("分片 {}/{} 同步了 {} 个订单状态", shardIndex, shardTotal, updated);
        }, jobContext);
    }

    // 📌 每晚 23:00：生成日报表（广播到所有节点，节点本地报表）
    @JobExecutor(name = "generateDailyReport")
    public void generateDailyReport(String jobContext) {
        executeWithErrorHandling("generateDailyReport", context -> {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            DailyReportVo report = new DailyReportVo();
            report.setReportDate(yesterday);
            report.setTotalOrders(orderService.countByDate(yesterday));
            report.setTotalAmount(orderService.sumAmountByDate(yesterday));
            report.setTotalUsers(orderService.countNewUsersByDate(yesterday));

            reportService.saveDailyReport(report);
            log.info("日报表已生成: {}", yesterday);
        }, jobContext);
    }

    // 📌 支付对账任务（带幂等性保证）
    @JobExecutor(name = "reconcilePayments")
    @Retryable(
        localTimes = 3,
        localInterval = 120,
        includeException = {SocketTimeoutException.class, ConnectException.class},
        bizNo = "#reconcileDate",
        idempotentId = "#reconcileDate"
    )
    public void reconcilePayments(String reconcileDate) {
        executeWithErrorHandling("reconcilePayments", context -> {
            List<Order> orders = orderService.getOrdersByDate(reconcileDate);

            for (Order order : orders) {
                PaymentStatusDto remoteStatus = paymentService.queryRemotePaymentStatus(order.getPaymentId());

                if (!order.getPaymentStatus().equals(remoteStatus.getStatus())) {
                    order.setPaymentStatus(remoteStatus.getStatus());
                    orderService.updatePaymentStatus(order);
                    log.info("支付状态已更正: {} -> {}", order.getOrderNo(), remoteStatus.getStatus());
                }
            }
        }, reconcileDate);
    }
}
```

---

## 总结：何时选择哪个方案？

```mermaid
graph TD
    A[需要定时任务吗?] -->|否| Z1[不需要]
    A -->|是| B{任务复杂度}
    B -->|简单| C{是否需要<br/>可视化管理?}
    B -->|复杂| D[使用 SnailJob]
    C -->|否| E{是否需要<br/>分布式?}
    C -->|是| D
    E -->|否| F[@Scheduled<br/>足够]
    E -->|是| D

    D --> D1[SnailJob 特性]
    D1 --> D2["✅ 可视化管理<br/>✅ 失败重试<br/>✅ 工作流编排<br/>✅ 分布式执行<br/>✅ 告警通知<br/>✅ 死信队列"]

    F --> F1[@Scheduled 特性]
    F1 --> F2["✅ 框架内置<br/>✅ 无依赖<br/>✅ 简单易用<br/>❌ 无可视化<br/>❌ 无重试<br/>❌ 单机执行"]
```

**最终建议**：
- **<100 个任务 + 简单逻辑** → 使用 `@Scheduled`
- **>100 个任务 或 复杂逻辑** → 使用 **SnailJob**
- **关键业务流程** → 优先使用 **SnailJob**（更可靠）
