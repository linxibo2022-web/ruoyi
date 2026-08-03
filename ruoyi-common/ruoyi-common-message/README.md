# 统一消息推送模块 - 使用文档

## 📖 模块简介

统一消息推送模块提供了一套标准化的消息推送接口，支持多种消息通道（WebSocket、SSE、短信、小程序、公众号等），并提供智能降级、广播推送等高级功能。

### 核心特性

- ✅ **统一接口**：所有消息通道遵循统一的 `MessageChannel` 接口
- ✅ **零依赖设计**：接口定义在 Core 模块，各通道独立实现，无循环依赖
- ✅ **可选模块**：业务模块可直接使用各通道，也可引入统一调度服务
- ✅ **智能降级**：按优先级尝试多个通道，确保消息送达
- ✅ **广播推送**：同时向多个通道发送相同消息
- ✅ **自动发现**：通过 Spring 自动注入所有可用通道
- ✅ **多租户支持**：内置租户隔离机制
- ✅ **健康检查**：实时监控通道可用性
- ✅ **兼容现有代码**：不影响现有的 `WebSocketUtils`、`SseMessageUtils` 等工具类

---

## 🏗️ 架构设计

### 三层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    业务层 (Controller/Service)                │
│  可选择：直接使用通道 OR 使用统一调度服务                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│          统一调度层 (ruoyi-common-message, 可选)              │
│  MessagePushService: 路由、降级、广播、自动选择                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      通道实现层                               │
│  WebSocket | SSE | SMS | Miniapp | MP | ...                 │
│  各模块独立实现 MessageChannel 接口                           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     核心接口层 (Core)                         │
│  MessageChannel | MessageContext | MessageResult             │
└─────────────────────────────────────────────────────────────┘
```

### 模块依赖关系

```
ruoyi-common-core (核心接口)
    ↑
    ├── ruoyi-common-websocket (WebSocket 实现)
    ├── ruoyi-common-sse (SSE 实现)
    ├── ruoyi-common-sms (短信实现)
    ├── ruoyi-common-miniapp (小程序实现)
    ├── ruoyi-common-mp (公众号实现)
    └── ruoyi-common-message (可选的统一调度服务)
```

**设计优势**：
- 接口在 Core，所有模块都可使用，无循环依赖
- 各通道模块独立，按需引入
- 统一调度服务可选，不强制使用

---

## 🚀 快速开始

### 方式一：直接使用具体通道（推荐简单场景）

如果你的业务场景明确知道要使用哪个通道，可以直接注入对应的 `MessageChannel` 实现。

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    // 直接注入具体通道
    private final WebSocketMessageChannel websocketChannel;
    private final SmsMessageChannel smsChannel;

    public void notifyOrderSuccess(Long userId, String orderNo) {
        // 使用 WebSocket 推送
        MessageContext context = MessageContext.of(userId, "您的订单 " + orderNo + " 已支付成功");
        MessageResult result = websocketChannel.send(context);

        if (!result.isSuccess()) {
            // WebSocket 失败，改用短信
            MessageContext smsContext = MessageContext.ofParams(userId, Map.of(
                "phone", "13800138000",
                "templateId", "SMS_ORDER_SUCCESS",
                "templateParams", Map.of("orderNo", orderNo)
            ));
            smsChannel.send(smsContext);
        }
    }
}
```

### 方式二：使用统一调度服务（推荐复杂场景）

如果需要智能降级、广播、自动选择等高级功能，引入统一调度服务。

**1. 添加依赖**

```xml
<!-- 在你的业务模块 pom.xml 中添加 -->
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-message</artifactId>
</dependency>
```

**2. 使用统一服务**

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    // 注入统一调度服务
    private final MessagePushService messagePushService;

    public void notifyOrderSuccess(Long userId, String orderNo) {
        // 方式1: 智能降级（WebSocket失败自动切换SSE）
        MessageContext context = MessageContext.of(userId, "订单支付成功");
        messagePushService.sendWithFallback(List.of("websocket", "sse"), context);

        // 方式2: 根据消息类型自动选择通道
        MessageContext context2 = MessageContext.of(userId, "订单发货通知")
            .setMessageType("order");
        messagePushService.sendByMessageType(context2);

        // 方式3: 广播到多个通道（重要通知）
        messagePushService.broadcast(List.of("websocket", "sms", "miniapp"), context);
    }
}
```

---

## 📚 使用示例

### 1. WebSocket 实时推送

```java
// 场景：系统通知、实时消息
MessageContext context = MessageContext.of(userId, "您有新的系统消息");
MessageResult result = websocketChannel.send(context);

// 批量推送
MessageContext batchContext = MessageContext.of(List.of(1001L, 1002L, 1003L), "系统维护通知");
websocketChannel.send(batchContext);
```

### 2. SSE 服务端推送

```java
// 场景：进度推送、实时更新
MessageContext context = MessageContext.of(userId, "任务进度: 50%");
sseChannel.send(context);
```

### 3. 短信发送

```java
// 方式1: 发送纯文本短信
MessageContext context = MessageContext.of(userId, "您的验证码是123456")
    .setParams(Map.of("phone", "13800138000"));
smsChannel.send(context);

// 方式2: 发送模板短信（推荐）
MessageContext templateContext = MessageContext.ofParams(userId, Map.of(
    "phone", "13800138000",
    "templateId", "SMS_VERIFY_CODE",
    "templateParams", Map.of("code", "123456")
));
smsChannel.send(templateContext);
```

### 4. 小程序订阅消息

```java
// 场景：订单状态、物流信息、预约提醒
MessageContext context = MessageContext.ofParams(userId, Map.of(
    "appid", "wx1234567890",
    "openid", "oABC123",
    "templateId", "tpl_order_status",
    "data", Map.of(
        "thing1", "订单编号123456",
        "time2", "2025-01-01 10:00:00",
        "thing3", "您的订单已发货"
    ),
    "page", "pages/order/detail?id=123"  // 可选：跳转页面
));
miniappChannel.send(context);
```

### 5. 公众号模板消息

```java
// 场景：支付成功、订单通知、活动提醒
MessageContext context = MessageContext.ofParams(userId, Map.of(
    "appid", "wx9876543210",
    "openid", "oXYZ456",
    "templateId", "tpl_order_success",
    "data", Map.of(
        "first", "您的订单已发货",
        "keyword1", "订单编号123456",
        "keyword2", "2025-01-01 10:00:00",
        "remark", "感谢您的购买"
    ),
    "url", "https://example.com/order/123"  // 跳转H5
));
mpChannel.send(context);

// 跳转小程序
MessageContext miniContext = MessageContext.ofParams(userId, Map.of(
    "appid", "wx9876543210",
    "openid", "oXYZ456",
    "templateId", "tpl_order_success",
    "data", Map.of(...),
    "miniAppid", "wx1234567890",
    "miniPath", "pages/order/detail?id=123"
));
mpChannel.send(miniContext);
```

---

## 🎯 高级功能

### 1. 智能降级（确保消息送达）

按优先级尝试多个通道，直到某个通道发送成功。

```java
// 场景1: 验证码发送（短信失败自动切换邮件）
MessageContext context = MessageContext.of(userId, "验证码：123456")
    .setParams(Map.of("phone", "13800138000"));
MessageResult result = messagePushService.sendWithFallback(
    List.of("sms", "email"),
    context
);

// 场景2: 实时通知（WebSocket失败自动切换SSE）
messagePushService.sendWithFallback(
    List.of("websocket", "sse"),
    MessageContext.of(userId, "您有新订单")
);
```

### 2. 广播推送（多通道同时发送）

重要通知需要多渠道送达时使用。

```java
// 重要系统公告同时推送多个通道
MessageContext context = MessageContext.of(userId, "系统将于今晚22:00维护");
List<MessageResult> results = messagePushService.broadcast(
    List.of("websocket", "sms", "miniapp", "mp"),
    context
);

// 统计发送结果
long successCount = results.stream().filter(MessageResult::isSuccess).count();
log.info("广播完成：成功 {}, 失败 {}", successCount, results.size() - successCount);
```

### 3. 自动选择最佳通道

根据通道优先级自动选择，不关心具体通道。

```java
MessageContext context = MessageContext.of(userId, "您的账户余额不足");
messagePushService.sendAuto(context);
// 自动选择优先级最高的可用通道（websocket > sse > sms > miniapp > mp）
```

### 4. 根据消息类型自动路由

预设不同消息类型的通道策略。

```java
// 验证码：优先短信
MessageContext verifyCode = MessageContext.of(userId, "验证码")
    .setMessageType("verify_code");
messagePushService.sendByMessageType(verifyCode);
// 自动选择：sms → email

// 订单通知：优先实时推送
MessageContext order = MessageContext.of(userId, "订单通知")
    .setMessageType("order");
messagePushService.sendByMessageType(order);
// 自动选择：websocket → miniapp → mp

// 营销消息：优先小程序/公众号
MessageContext promotion = MessageContext.of(userId, "促销活动")
    .setMessageType("promotion");
messagePushService.sendByMessageType(promotion);
// 自动选择：miniapp → mp → sms
```

**支持的消息类型**（可在 MessagePushService 中扩展）：

| 消息类型 | 通道策略 | 使用场景 |
|---------|---------|---------|
| `verify_code` | sms → email | 验证码 |
| `order` | websocket → miniapp → mp | 订单通知 |
| `promotion` | miniapp → mp → sms | 营销消息 |
| `system_notice` | websocket → sse | 系统通知 |
| `important` | sms → websocket → miniapp → mp | 重要通知 |
| 默认 | websocket | 其他 |

---

## 🔧 配置说明

### 通道启用控制

各通道通过配置文件控制启用状态：

```yaml
# application.yml

# WebSocket 配置
websocket:
  enabled: true  # 默认启用

# SSE 配置
sse:
  enabled: true  # 默认启用

# 短信配置
sms:
  enabled: true

# 小程序配置
miniapp:
  enabled: true

# 公众号配置
mp:
  enabled: true
```

### 通道优先级

优先级数值越小，优先级越高（降级和自动选择时优先使用）。

| 通道 | 优先级 | 说明 |
|------|-------|------|
| WebSocket | 1 | 最高，实时性最好 |
| SSE | 2 | 次高，适合服务端推送 |
| SMS | 3 | 中等，成本较高但送达率高 |
| Miniapp | 4 | 较高，需用户授权 |
| MP | 5 | 中等，官方通道 |

---

## 🧪 健康检查与监控

### 查看可用通道

```java
@RestController
@RequestMapping("/admin/message")
@RequiredArgsConstructor
public class MessageAdminController {

    private final MessagePushService messagePushService;

    @GetMapping("/channels")
    public R<List<String>> getAvailableChannels() {
        // 获取所有启用且健康的通道
        List<String> channels = messagePushService.getAvailableChannelTypes();
        return R.ok(channels);
    }

    @GetMapping("/channel/{type}")
    public R<String> getChannelInfo(@PathVariable String type) {
        String info = messagePushService.getChannelInfo(type);
        return R.ok(info);
    }
}
```

### 健康检查

```java
// 检查某个通道是否健康
boolean isHealthy = websocketChannel.healthCheck();

// 获取所有健康的通道
List<MessageChannel> healthyChannels = messagePushService.getAvailableChannels();
```

---

## 🔌 扩展新通道

### 1. 实现 MessageChannel 接口

```java
package plus.ruoyi.common.email.channel;

import org.springframework.stereotype.Component;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;

@Component
public class EmailMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "email";  // 通道唯一标识
    }

    @Override
    public String getChannelName() {
        return "邮件推送";
    }

    @Override
    public MessageResult send(MessageContext context) {
        // 实现邮件发送逻辑
        String email = (String) context.getParams().get("email");
        // ... 发送邮件
        return MessageResult.success(context.getMessageId(), "email", context.getUserIds().get(0));
    }

    @Override
    public boolean isEnabled() {
        // 从配置读取启用状态
        return true;
    }

    @Override
    public int getPriority() {
        return 6;  // 设置优先级
    }

    @Override
    public boolean healthCheck() {
        // 检查邮件服务是否可用
        return true;
    }

    @Override
    public boolean supportTenant(String tenantId) {
        return true;
    }
}
```

### 2. Spring 自动发现

只要实现类标注了 `@Component`，Spring 会自动注入到 `MessagePushService` 的 `channels` 列表中，无需额外配置！

### 3. 使用新通道

```java
// 方式1: 直接注入使用
@Autowired
private EmailMessageChannel emailChannel;

// 方式2: 通过统一服务使用
messagePushService.send("email", context);

// 方式3: 加入降级策略
messagePushService.sendWithFallback(List.of("sms", "email"), context);
```

---

## 🔄 兼容性说明

### 与现有代码完全兼容

本模块**不会影响**现有的消息推送代码，你可以继续使用：

```java
// ✅ 现有代码继续有效
WebSocketUtils.publishMessage(dto);
SseMessageUtils.publishMessage(dto);

// ✅ 新代码使用统一接口
messagePushService.send("websocket", context);
```

### 逐步迁移建议

1. **新功能**：直接使用统一接口
2. **现有功能**：保持不变，逐步重构
3. **复杂场景**：优先使用统一调度服务（降级、广播等）

---

## 📊 完整示例：订单支付成功通知

```java
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 订单支付成功，多渠道通知用户
     */
    public void notifyOrderPaid(Long userId, String orderNo, BigDecimal amount, String openid) {
        // 1. 实时推送（WebSocket/SSE）
        MessageContext realtimeContext = MessageContext.of(
            userId,
            String.format("订单 %s 支付成功，金额 ¥%.2f", orderNo, amount)
        );
        messagePushService.sendWithFallback(List.of("websocket", "sse"), realtimeContext);

        // 2. 小程序订阅消息（异步，不阻塞主流程）
        CompletableFuture.runAsync(() -> {
            MessageContext miniappContext = MessageContext.ofParams(userId, Map.of(
                "appid", "wx1234567890",
                "openid", openid,
                "templateId", "tpl_order_paid",
                "data", Map.of(
                    "character_string1", orderNo,  // 订单编号
                    "amount2", String.format("¥%.2f", amount),  // 支付金额
                    "date3", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),  // 支付时间
                    "thing4", "订单已支付成功，我们将尽快为您发货"  // 温馨提示
                ),
                "page", "pages/order/detail?id=" + orderNo
            ));
            messagePushService.send("miniapp", miniappContext);
        });

        // 3. 如果金额超过1000元，额外发送短信通知（重要订单）
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            MessageContext smsContext = MessageContext.ofParams(userId, Map.of(
                "phone", getUserPhone(userId),
                "templateId", "SMS_HIGH_VALUE_ORDER",
                "templateParams", Map.of(
                    "orderNo", orderNo,
                    "amount", amount.toString()
                )
            ));
            messagePushService.send("sms", smsContext);
        }
    }

    private String getUserPhone(Long userId) {
        // 查询用户手机号
        return "13800138000";
    }
}
```

---

## ❓ 常见问题

### Q1: 为什么要把接口放在 Core 模块？

**A**: 避免循环依赖和依赖臃肿。接口在 Core，所有模块都可使用；实现在各自模块，按需引入。

### Q2: 是否必须使用 MessagePushService？

**A**: 不是必须的。简单场景可以直接注入具体通道使用，复杂场景（降级、广播、自动选择）才需要统一调度服务。

### Q3: 如何动态禁用某个通道？

**A**: 修改配置文件中的 `enabled` 属性，或者在通道实现中的 `isEnabled()` 方法中实现动态开关逻辑。

### Q4: 通道优先级如何调整？

**A**: 修改各通道实现类的 `getPriority()` 方法返回值，数值越小优先级越高。

### Q5: 如何处理发送失败？

**A**:
- 方式1：检查 `MessageResult.isSuccess()`
- 方式2：使用智能降级 `sendWithFallback()`，自动尝试备用通道
- 方式3：记录日志，异步重试

### Q6: 支持事务吗？

**A**: 消息发送通常是异步操作，不建议在事务中发送。推荐做法：
```java
@Transactional
public void createOrder() {
    // 1. 保存订单到数据库
    orderMapper.insert(order);
}

// 2. 事务提交后发送消息
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderCreated(OrderCreatedEvent event) {
    messagePushService.send("websocket", context);
}
```

### Q7: 如何实现消息模板？

**A**: 各通道支持不同的模板机制：
- 短信：使用 SMS4J 的模板ID
- 小程序：使用微信的订阅消息模板
- 公众号：使用微信的模板消息
- 自定义：可在业务层封装模板引擎（如 Freemarker、Thymeleaf）

---

## 📞 技术支持

如有问题或建议，请联系开发团队或提交 Issue。

**相关文档**：
- MessageChannel 接口：`ruoyi-common-core/src/main/java/plus/ruoyi/common/core/message/MessageChannel.java`
- MessagePushService 实现：`ruoyi-common-message/src/main/java/plus/ruoyi/common/message/service/MessagePushService.java`
- 各通道实现：对应模块的 `channel` 包下
