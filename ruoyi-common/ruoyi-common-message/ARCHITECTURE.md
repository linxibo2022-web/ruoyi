# 统一消息推送模块 - 架构设计文档

## 📐 设计目标

本模块旨在提供一套统一、可扩展的消息推送解决方案，解决以下问题：

1. **接口不统一**：WebSocket、SSE、短信、小程序、公众号等各有各的API，使用复杂
2. **依赖臃肿**：不同通道耦合在一起，难以按需引入
3. **缺乏降级**：单一通道失败后无法自动切换备用通道
4. **扩展困难**：新增通道需要修改大量代码
5. **难以监控**：缺乏统一的健康检查和统计机制

---

## 🏗️ 架构设计

### 核心设计原则

#### 1. 接口隔离原则 (ISP)

**接口定义在 Core 模块，实现在各自模块**

```
ruoyi-common-core (核心接口)
    ├── MessageChannel (通道接口)
    ├── MessageContext (消息上下文 DTO)
    └── MessageResult (发送结果 DTO)

ruoyi-common-websocket
    └── WebSocketMessageChannel implements MessageChannel

ruoyi-common-sse
    └── SseMessageChannel implements MessageChannel

ruoyi-common-sms
    └── SmsMessageChannel implements MessageChannel

ruoyi-common-miniapp
    └── MiniappMessageChannel implements MessageChannel

ruoyi-common-mp
    └── MpMessageChannel implements MessageChannel
```

**优势**：
- ✅ 零循环依赖：Core 不依赖任何实现模块
- ✅ 按需引入：业务模块只引入需要的通道
- ✅ 独立演进：各通道模块独立开发、测试、发布
- ✅ 接口统一：所有通道遵循相同的接口规范

#### 2. 依赖倒置原则 (DIP)

**高层模块不依赖低层模块，都依赖抽象**

```java
// ❌ 错误设计：直接依赖具体实现
@Service
public class OrderService {
    private final WebSocketUtils websocketUtils;  // 耦合具体实现
    private final SseMessageUtils sseUtils;
    private final SmsBlend smsBlend;
    // ...
}

// ✅ 正确设计：依赖抽象接口
@Service
public class OrderService {
    private final MessageChannel websocketChannel;  // 依赖接口
    private final MessageChannel sseChannel;
    // 或使用统一调度服务
    private final MessagePushService messagePushService;
}
```

#### 3. 开闭原则 (OCP)

**对扩展开放，对修改关闭**

新增通道无需修改现有代码：

```java
// 新增邮件通道
@Component
public class EmailMessageChannel implements MessageChannel {
    // 实现接口方法
}

// Spring 自动发现，无需修改 MessagePushService
```

#### 4. 单一职责原则 (SRP)

**每个模块只负责一个职责**

| 模块 | 职责 |
|------|------|
| `ruoyi-common-core` | 定义接口和 DTO |
| `ruoyi-common-websocket` | WebSocket 实时推送 |
| `ruoyi-common-sse` | SSE 服务端推送 |
| `ruoyi-common-sms` | 短信发送 |
| `ruoyi-common-miniapp` | 小程序订阅消息 |
| `ruoyi-common-mp` | 公众号模板消息 |
| `ruoyi-common-message` | 统一调度与路由 |

---

## 📦 模块设计

### 1. Core 模块（核心接口）

**位置**：`ruoyi-common/ruoyi-common-core/src/main/java/plus/ruoyi/common/core/message/`

#### MessageChannel 接口

```java
public interface MessageChannel {

    /**
     * 获取通道类型
     * @return websocket / sse / sms / miniapp / mp / email 等
     */
    String getChannelType();

    /**
     * 获取通道名称（用于显示）
     */
    String getChannelName();

    /**
     * 发送消息（核心方法）
     * @param context 消息上下文
     * @return 发送结果
     */
    MessageResult send(MessageContext context);

    /**
     * 通道是否启用
     * @return true=启用 false=禁用
     */
    boolean isEnabled();

    /**
     * 获取通道优先级
     * @return 数值越小优先级越高（用于降级和自动选择）
     */
    int getPriority();

    /**
     * 健康检查
     * @return true=健康 false=不健康
     */
    default boolean healthCheck() {
        return isEnabled();
    }

    /**
     * 是否支持指定租户
     * @param tenantId 租户ID
     * @return true=支持 false=不支持
     */
    default boolean supportTenant(String tenantId) {
        return true;
    }
}
```

**设计要点**：
- 接口方法精简，只定义核心功能
- 使用 `default` 方法提供默认实现，降低实现成本
- `getPriority()` 用于智能降级和自动选择
- `healthCheck()` 用于实时监控通道可用性

#### MessageContext (消息上下文)

```java
@Data
public class MessageContext implements Serializable {

    /** 消息ID（自动生成UUID） */
    private String messageId;

    /** 租户ID（多租户支持） */
    private String tenantId;

    /** 目标用户ID列表 */
    private List<Long> userIds;

    /** 消息内容（纯文本） */
    private String content;

    /** 扩展参数（通道特定参数） */
    private Map<String, Object> params;

    /** 消息类型（用于自动路由） */
    private String messageType;

    // 工厂方法
    public static MessageContext of(Long userId, String content) { ... }
    public static MessageContext of(List<Long> userIds, String content) { ... }
    public static MessageContext ofParams(Long userId, Map<String, Object> params) { ... }
}
```

**设计要点**：
- 使用建造者模式，支持链式调用
- `params` 用于传递通道特定参数（如短信的手机号、小程序的 openid）
- `messageType` 用于自动路由（如 verify_code、order、promotion）
- 自动生成 `messageId`，用于日志追踪

#### MessageResult (发送结果)

```java
@Data
public class MessageResult implements Serializable {

    /** 是否成功 */
    private Boolean success;

    /** 消息ID */
    private String messageId;

    /** 通道类型 */
    private String channelType;

    /** 目标用户ID */
    private Long userId;

    /** 错误码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    /** 第三方返回的消息ID（如短信的 bizId、微信的 msgId） */
    private String thirdPartyMsgId;

    /** 发送耗时（毫秒） */
    private Long costTime;

    /** 额外信息 */
    private String extra;

    // 工厂方法
    public static MessageResult success(String messageId, String channelType, Long userId) { ... }
    public static MessageResult fail(String messageId, String channelType, Long userId, String errorCode, String errorMessage) { ... }
}
```

**设计要点**：
- 封装所有发送结果信息
- `thirdPartyMsgId` 用于追踪第三方平台的消息
- `costTime` 用于性能监控
- 提供工厂方法，简化构造

---

### 2. 通道实现模块

每个通道模块独立实现 `MessageChannel` 接口。

#### WebSocket 通道实现

```java
@Component
@ConditionalOnProperty(prefix = "websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "websocket";
    }

    @Override
    public MessageResult send(MessageContext context) {
        // 包装现有的 WebSocketUtils
        WebSocketMessageDto dto = WebSocketMessageDto.of(
            context.getUserIds(),
            context.getContent()
        );
        dto.setTenantId(context.getTenantId());
        WebSocketUtils.publishMessage(dto);
        // ...
    }

    @Override
    public int getPriority() {
        return 1;  // 最高优先级
    }
}
```

**设计要点**：
- 使用 `@ConditionalOnProperty` 条件注册
- 包装现有工具类，保持向后兼容
- 统一异常处理和日志记录
- 支持多租户隔离

#### SMS 通道实现

```java
@Component
@ConditionalOnProperty(prefix = "sms", name = "enabled", havingValue = "true")
public class SmsMessageChannel implements MessageChannel {

    private final SmsBlend smsBlend;  // SMS4J 框架

    @Override
    public MessageResult send(MessageContext context) {
        String phone = (String) context.getParams().get("phone");
        String templateId = (String) context.getParams().get("templateId");

        SmsResponse response = smsBlend.sendMessage(phone, templateId);

        if (response.isSuccess()) {
            return MessageResult.success(...)
                .setThirdPartyMsgId(response.getBizId());
        } else {
            return MessageResult.fail(...);
        }
    }

    @Override
    public int getPriority() {
        return 3;  // 中等优先级（成本较高）
    }
}
```

**设计要点**：
- 从 `params` 中提取通道特定参数
- 支持模板短信和普通短信
- 记录第三方返回的消息ID
- 统一返回 `MessageResult`

#### Miniapp 通道实现

```java
@Component
@ConditionalOnProperty(prefix = "miniapp", name = "enabled", havingValue = "true")
public class MiniappMessageChannel implements MessageChannel {

    @Override
    public MessageResult send(MessageContext context) {
        String appid = (String) params.get("appid");
        String openid = (String) params.get("openid");
        String templateId = (String) params.get("templateId");
        Map<String, String> data = (Map<String, String>) params.get("data");

        boolean success = WxMaSubscribeUtils.send(appid, openid, templateId, data, page);

        return success
            ? MessageResult.success(...)
            : MessageResult.fail(...);
    }

    @Override
    public int getPriority() {
        return 4;  // 较高优先级
    }
}
```

**设计要点**：
- 调用框架现有的 `WxMaSubscribeUtils`
- 支持小程序页面跳转
- 支持不同小程序版本（developer/trial/formal）

---

### 3. 统一调度服务（可选）

**位置**：`ruoyi-common/ruoyi-common-message/src/main/java/plus/ruoyi/common/message/service/MessagePushService.java`

```java
@Service
@RequiredArgsConstructor
public class MessagePushService {

    /**
     * Spring 自动注入所有 MessageChannel 实现
     * 新增通道时自动发现，无需修改代码
     */
    private final List<MessageChannel> channels;

    /**
     * 发送消息到指定通道
     */
    public MessageResult send(String channelType, MessageContext context) {
        MessageChannel channel = getChannel(channelType);
        return channel.send(context);
    }

    /**
     * 智能降级发送（按优先级尝试多个通道）
     */
    public MessageResult sendWithFallback(List<String> channelTypes, MessageContext context) {
        for (String channelType : channelTypes) {
            MessageResult result = send(channelType, context);
            if (result.isSuccess()) {
                return result;  // 成功，停止尝试
            }
        }
        return lastFailResult;
    }

    /**
     * 广播消息到多个通道
     */
    public List<MessageResult> broadcast(List<String> channelTypes, MessageContext context) {
        return channelTypes.stream()
            .map(type -> send(type, context))
            .collect(Collectors.toList());
    }

    /**
     * 自动选择最佳通道
     */
    public MessageResult sendAuto(MessageContext context) {
        List<MessageChannel> enabledChannels = channels.stream()
            .filter(MessageChannel::isEnabled)
            .sorted(Comparator.comparingInt(MessageChannel::getPriority))
            .toList();

        MessageChannel bestChannel = enabledChannels.get(0);
        return bestChannel.send(context);
    }

    /**
     * 根据消息类型自动路由
     */
    public MessageResult sendByMessageType(MessageContext context) {
        List<String> channelTypes = selectChannelsByMessageType(context.getMessageType());
        return sendWithFallback(channelTypes, context);
    }

    private List<String> selectChannelsByMessageType(String messageType) {
        return switch (messageType) {
            case "verify_code" -> List.of("sms", "email");
            case "order" -> List.of("websocket", "miniapp", "mp");
            case "promotion" -> List.of("miniapp", "mp", "sms");
            case "important" -> List.of("sms", "websocket", "miniapp", "mp");
            default -> List.of("websocket");
        };
    }
}
```

**设计要点**：
- 通过 Spring 依赖注入自动发现所有通道
- 不依赖具体通道实现，只依赖接口
- 提供多种发送策略（直接发送、智能降级、广播、自动选择、类型路由）
- 可扩展消息类型的路由策略

---

## 🔄 工作流程

### 1. 直接使用通道

```
业务代码
   ↓
注入具体通道 (WebSocketMessageChannel)
   ↓
调用 send(MessageContext)
   ↓
WebSocketUtils.publishMessage()
   ↓
返回 MessageResult
```

### 2. 使用统一调度服务

```
业务代码
   ↓
注入 MessagePushService
   ↓
调用 sendWithFallback(["websocket", "sse"], context)
   ↓
尝试 websocket → 失败
   ↓
尝试 sse → 成功
   ↓
返回 MessageResult (channelType=sse)
```

### 3. Spring 自动发现通道

```
应用启动
   ↓
Spring 扫描 @Component
   ↓
发现所有实现 MessageChannel 的 Bean
   ↓
注入到 MessagePushService 的 channels 列表
   ↓
新增通道时自动添加，无需修改配置
```

---

## 🎯 关键技术决策

### 决策1：接口放在 Core 还是独立模块？

**选择**：放在 `ruoyi-common-core`

**理由**：
- ✅ Core 模块是所有模块的基础依赖，避免循环依赖
- ✅ 接口定义轻量级，不会给 Core 增加负担
- ✅ 所有模块都可以直接使用接口，无需额外依赖

**备选方案**：
- ❌ 创建独立的 `ruoyi-common-message-api` 模块
  - 缺点：增加了模块复杂度，所有通道都需要额外依赖这个模块

### 决策2：是否必须使用统一调度服务？

**选择**：可选使用

**理由**：
- ✅ 简单场景直接注入具体通道即可，更直观
- ✅ 复杂场景（降级、广播）才需要统一调度服务
- ✅ 业务模块按需引入，不强制依赖

**适用场景**：
- 直接使用：明确知道使用哪个通道，无降级需求
- 统一调度：需要智能降级、广播、自动选择、类型路由

### 决策3：如何实现通道的自动发现？

**选择**：Spring 依赖注入 `List<MessageChannel>`

**理由**：
- ✅ Spring 原生支持，无需额外框架
- ✅ 新增通道只需标注 `@Component`，自动注册
- ✅ 运行时动态发现，支持热插拔

**实现代码**：
```java
@Service
@RequiredArgsConstructor
public class MessagePushService {
    // Spring 自动注入所有 MessageChannel 实现
    private final List<MessageChannel> channels;
}
```

### 决策4：如何处理通道特定参数？

**选择**：使用 `params: Map<String, Object>`

**理由**：
- ✅ 灵活，支持任意通道的特定参数
- ✅ 通用接口保持简洁，不被特定通道污染
- ✅ 类型安全由各通道实现自己保证

**示例**：
```java
// 短信通道需要 phone、templateId
MessageContext smsContext = MessageContext.ofParams(userId, Map.of(
    "phone", "13800138000",
    "templateId", "SMS_123456"
));

// 小程序通道需要 appid、openid、templateId、data
MessageContext miniappContext = MessageContext.ofParams(userId, Map.of(
    "appid", "wx123",
    "openid", "oABC",
    "templateId", "tpl_123",
    "data", Map.of(...)
));
```

### 决策5：如何处理向后兼容？

**选择**：包装现有工具类，不替换

**理由**：
- ✅ 现有代码继续有效，降低迁移风险
- ✅ 新代码可以使用统一接口
- ✅ 逐步迁移，平滑过渡

**实现**：
```java
// ✅ 现有代码继续有效
WebSocketUtils.publishMessage(dto);

// ✅ 新代码使用统一接口
websocketChannel.send(context);

// 内部实现：包装现有工具类
public MessageResult send(MessageContext context) {
    WebSocketMessageDto dto = convertToDto(context);
    WebSocketUtils.publishMessage(dto);  // 调用现有工具类
    return MessageResult.success(...);
}
```

---

## 🚀 扩展性设计

### 新增通道的步骤

1. **创建新模块**（如 `ruoyi-common-email`）

```xml
<dependency>
    <groupId>plus.ruoyi</groupId>
    <artifactId>ruoyi-common-core</artifactId>
</dependency>
```

2. **实现 MessageChannel 接口**

```java
@Component
@ConditionalOnProperty(prefix = "email", name = "enabled", havingValue = "true")
public class EmailMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public MessageResult send(MessageContext context) {
        // 实现邮件发送逻辑
        return MessageResult.success(...);
    }

    @Override
    public int getPriority() {
        return 6;
    }
}
```

3. **无需修改现有代码**，Spring 自动发现

4. **使用新通道**

```java
// 方式1: 直接注入
@Autowired
private EmailMessageChannel emailChannel;

// 方式2: 通过统一服务
messagePushService.send("email", context);
```

### 扩展消息类型路由

在 `MessagePushService` 中扩展 `selectChannelsByMessageType()` 方法：

```java
private List<String> selectChannelsByMessageType(String messageType) {
    return switch (messageType) {
        case "verify_code" -> List.of("sms", "email");
        case "order" -> List.of("websocket", "miniapp", "mp");
        case "promotion" -> List.of("miniapp", "mp", "sms");
        case "important" -> List.of("sms", "websocket", "miniapp", "mp");
        case "payment" -> List.of("sms", "miniapp", "mp", "email");  // 新增支付类型
        case "logistics" -> List.of("miniapp", "mp");  // 新增物流类型
        default -> List.of("websocket");
    };
}
```

---

## 🔍 性能优化

### 1. 异步发送

```java
// 方式1: @Async
@Async("messageExecutor")
public void sendAsync(MessageContext context) {
    messagePushService.send("websocket", context);
}

// 方式2: CompletableFuture
CompletableFuture.runAsync(() -> {
    messagePushService.send("websocket", context);
});
```

### 2. 批量发送优化

```java
// 分批处理，避免一次性处理过多用户
int batchSize = 100;
for (int i = 0; i < totalBatches; i++) {
    List<Long> batchUserIds = userIds.subList(start, end);
    CompletableFuture.runAsync(() -> {
        sendBatchInternal(batchUserIds, content, channelType);
    });
    Thread.sleep(100);  // 避免瞬间大量请求
}
```

### 3. 缓存通道实例

```java
// MessagePushService 中缓存通道实例
private final Map<String, MessageChannel> channelCache = new ConcurrentHashMap<>();

private MessageChannel getChannel(String channelType) {
    return channelCache.computeIfAbsent(channelType, type ->
        channels.stream()
            .filter(ch -> ch.getChannelType().equals(type))
            .findFirst()
            .orElse(null)
    );
}
```

---

## 🛡️ 安全性设计

### 1. 多租户隔离

```java
public interface MessageChannel {
    /**
     * 是否支持指定租户
     */
    default boolean supportTenant(String tenantId) {
        return true;
    }
}

// 在发送前检查租户权限
public MessageResult send(String channelType, MessageContext context) {
    MessageChannel channel = getChannel(channelType);

    if (!channel.supportTenant(context.getTenantId())) {
        return MessageResult.fail(..., "租户不支持此通道");
    }

    return channel.send(context);
}
```

### 2. 参数校验

```java
// 各通道实现中严格校验参数
public MessageResult send(MessageContext context) {
    if (context == null || context.getParams() == null) {
        return MessageResult.fail(..., "消息上下文或扩展参数不能为空");
    }

    String phone = (String) context.getParams().get("phone");
    if (StringUtils.isBlank(phone)) {
        return MessageResult.fail(..., "缺少必填参数: params.phone");
    }

    // 手机号格式校验
    if (!phone.matches("^1[3-9]\\d{9}$")) {
        return MessageResult.fail(..., "手机号格式错误");
    }

    // ...
}
```

### 3. 敏感信息脱敏

```java
// 日志中脱敏敏感信息
log.info("短信发送成功: phone={}, messageId={}",
    desensitizePhone(phone), context.getMessageId());

private String desensitizePhone(String phone) {
    if (phone.length() >= 11) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    return phone;
}
```

---

## 📊 监控与运维

### 1. 健康检查

```java
// 实现 healthCheck() 方法
@Override
public boolean healthCheck() {
    if (!isEnabled()) {
        return false;
    }

    try {
        // 检查配置完整性
        if (smsBlend == null) {
            return false;
        }

        // 检查服务可用性（可发送测试请求）
        // ...

        return true;
    } catch (Exception e) {
        log.warn("健康检查失败", e);
        return false;
    }
}
```

### 2. 统计指标

```java
// 记录关键指标
- 发送总数
- 成功数 / 失败数
- 成功率
- 平均耗时
- 按通道统计
- 按消息类型统计
```

### 3. 告警机制

```java
// 发送失败率过高时告警
if (failRate > 0.1) {  // 失败率 > 10%
    alertService.sendAlert("消息通道异常",
        String.format("通道 %s 失败率过高: %.2f%%", channelType, failRate * 100));
}
```

---

## 📝 总结

### 设计优势

1. **零循环依赖**：接口在 Core，实现在各模块，依赖关系清晰
2. **按需引入**：业务模块只引入需要的通道，避免依赖臃肿
3. **易于扩展**：新增通道只需实现接口，无需修改现有代码
4. **统一接口**：所有通道遵循相同规范，降低学习成本
5. **向后兼容**：包装现有工具类，不影响现有代码
6. **可选调度**：简单场景直接用，复杂场景用统一服务
7. **自动发现**：Spring 依赖注入，通道自动注册
8. **多租户支持**：内置租户隔离机制
9. **健康监控**：实时检查通道可用性
10. **灵活路由**：支持降级、广播、自动选择、类型路由

### 适用场景

- ✅ 多通道消息推送
- ✅ 智能降级场景
- ✅ 广播通知场景
- ✅ 需要统一接口的场景
- ✅ 需要按需引入通道的场景
- ✅ 需要扩展新通道的场景

### 不适用场景

- ❌ 只使用单一通道且无扩展需求
- ❌ 对性能要求极高的场景（统一接口会有微小的性能损耗）

---

## 🔗 相关文档

- [使用文档 (README.md)](./README.md)
- [使用示例 (EXAMPLES.md)](./EXAMPLES.md)
