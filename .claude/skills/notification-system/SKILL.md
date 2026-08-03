---
name: notification-system
description: |
  当需要发送通知消息、短信验证码、邮件通知、统一消息推送时自动使用此 Skill。

  触发场景：
  - 需要发送短信（验证码、通知、营销）
  - 需要发送邮件（验证码、通知、HTML邮件）
  - 需要使用统一消息推送服务（多通道路由、降级、广播）
  - 需要了解消息通道接口规范和扩展方式
  - 需要为业务模块集成消息推送能力
  - 需要配置短信/邮件服务

  触发词：短信、SMS、邮件、Mail、Email、消息推送、MessagePushService、MessageChannel、通知、验证码、SmsFactory、MailUtils、sendText、sendHtml、统一消息、消息路由、多通道
---

# 通知消息系统开发指南

## 概述

本项目提供三层消息能力：**短信**（SMS4J 多厂商）、**邮件**（Jakarta Mail）、**统一消息推送**（MessagePushService 多通道路由）。三者可独立使用，也可通过统一消息服务自动编排。

| 模块 | 用途 | 核心类 | 依赖 |
|------|------|--------|------|
| `ruoyi-common-sms` | 短信发送 | `SmsFactory` / `SmsMessageChannel` | SMS4J (`org.dromara.sms4j`) |
| `ruoyi-common-mail` | 邮件发送 | `MailUtils` | Jakarta Mail + Hutool Mail |
| `ruoyi-common-message` | 统一消息推送 | `MessagePushService` | 仅依赖 core（零耦合） |

### 选型决策

```
需要发送消息？
├─ 直接发送短信 → SmsFactory / SmsMessageChannel
├─ 直接发送邮件 → MailUtils
├─ 需要多通道路由/降级/广播 → MessagePushService
│   ├─ sendByMessageType() → 按消息类型自动路由
│   ├─ sendWithFallback() → 多通道降级
│   ├─ broadcast() → 多通道广播
│   └─ sendAuto() → 按优先级自动选择
└─ WebSocket/SSE 实时推送 → 参考 realtime-communication 技能
```

---

## 一、短信模块（ruoyi-common-sms）

### 核心文件

| 文件 | 行数 | 用途 |
|------|------|------|
| `config/SmsAutoConfiguration.java` | - | 自动配置，注册 PlusSmsDao、SmsExceptionHandler、SmsMessageChannel |
| `core/dao/PlusSmsDao.java` | - | SMS4J DAO 实现，使用 Redis 缓存短信配置 |
| `handler/SmsExceptionHandler.java` | - | 全局异常处理，捕获 `SmsBlendException` |
| `channel/SmsMessageChannel.java` | 211 | MessageChannel 实现，channelType="sms"，priority=3 |

### 配置（application.yml）

```yaml
sms:
  config-type: yaml          # 配置方式：yaml
  restricted: true            # 启用频率限制
  minute-max: 1               # 每分钟最多发送 1 条（同手机号）
  account-max: 30             # 每日最多发送 30 条（同手机号）
  blends:
    config1:                  # 配置ID（发送时引用）
      supplier: alibaba       # 供应商：alibaba / tencent
      access-key-id: ${SMS_ALI_ACCESS_KEY:您的accessKey}
      access-key-secret: ${SMS_ALI_ACCESS_SECRET:您的accessKeySecret}
      signature: ${SMS_ALI_SIGNATURE:您的短信签名}
      sdk-app-id: ${SMS_ALI_SDK_APP_ID:您的sdkAppId}
    config2:
      supplier: tencent
      access-key-id: ${SMS_TENCENT_ACCESS_KEY:您的accessKey}
      access-key-secret: ${SMS_TENCENT_ACCESS_SECRET:您的accessKeySecret}
      signature: ${SMS_TENCENT_SIGNATURE:您的短信签名}
      sdk-app-id: ${SMS_TENCENT_SDK_APP_ID:您的sdkAppId}
```

> **关键配置项**：`sms.restricted=true` 开启频率限制，`sms.minute-max` 和 `sms.account-max` 控制发送频率。SMS4J 自身通过 Redis（`PlusSmsDao`）存储频率计数。

### 直接发送短信

```java
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.api.entity.SmsResponse;

// 方式一：发送模板短信（推荐）
SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");  // 配置ID
LinkedHashMap<String, String> params = new LinkedHashMap<>();
params.put("code", "123456");
SmsResponse response = smsBlend.sendMessage(phone, templateId, params);

// 方式二：发送普通短信
SmsResponse response = smsBlend.sendMessage(phone, "您的验证码是 123456");

// 检查发送结果
if (response.isSuccess()) {
    log.info("短信发送成功: {}", response.getData());
} else {
    log.error("短信发送失败: {}", response.getData());
}
```

### 实际使用示例：验证码发送

```java
// 参考：CaptchaController.java（286行）
@RateLimiter(key = "#phone", time = 60, count = 1)
public void smsCodeImpl(String phone) {
    String code = RandomUtil.randomNumbers(4);
    // 缓存验证码到 Redis
    RedisUtils.setCacheObject(
        GlobalConstants.CAPTCHA_CODE_KEY + phone,
        code,
        Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION)
    );
    // 发送短信
    LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
    map.put("code", code);
    SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");
    SmsResponse smsResponse = smsBlend.sendMessage(phone, smsProperties.getTemplateId(), map);
    if (!smsResponse.isSuccess()) {
        log.error("验证码短信发送异常 => {}", smsResponse);
        throw new ServiceException("短信发送异常");
    }
}
```

### PlusSmsDao（Redis 缓存）

SMS4J 框架通过 `SmsDao` 接口存取短信配置和频率数据。本项目使用 Redis 实现：

```java
// PlusSmsDao 使用 Redis 缓存，key 前缀为 GlobalConstants.GLOBAL_REDIS_KEY
// 自动注册为 @Primary Bean，替代 SMS4J 默认实现
// 所有方法委托给 RedisUtils
```

---

## 二、邮件模块（ruoyi-common-mail）

### 核心文件

| 文件 | 行数 | 用途 |
|------|------|------|
| `config/MailAutoConfiguration.java` | - | 自动配置，条件：`mail.enabled=true` |
| `config/properties/MailProperties.java` | - | 配置属性：host/port/auth/user/pass/from 等 |
| `utils/MailUtils.java` | 470 | 邮件发送工具类（全部 static 方法） |

### 配置（application.yml）

```yaml
mail:
  enabled: ${MAIL_ENABLED:false}   # 默认禁用，需显式启用
  host: ${MAIL_HOST:smtp.163.com}  # SMTP 服务器
  port: ${MAIL_PORT:465}           # SMTP 端口
  auth: true                       # 需要认证
  from: ${MAIL_FROM:xxx@163.com}   # 发件人地址
  user: ${MAIL_USERNAME:xxx@163.com}  # 认证用户名
  pass: ${MAIL_PASSWORD:xxxxxxxxxx}   # 认证密码（授权码）
  starttlsEnable: true             # STARTTLS
  sslEnable: true                  # SSL
  timeout: 0                       # 超时（0=默认）
  connectionTimeout: 0             # 连接超时
```

> **注意**：`mail.enabled` 默认为 `false`，使用前必须在 `application.yml` 中改为 `true` 或通过环境变量 `MAIL_ENABLED=true` 启用。

### MailUtils 常用方法

```java
import plus.ruoyi.common.mail.utils.MailUtils;

// 1. 发送文本邮件
String messageId = MailUtils.sendText("to@example.com", "标题", "纯文本内容");

// 2. 发送 HTML 邮件
String messageId = MailUtils.sendHtml("to@example.com", "标题", "<h1>HTML内容</h1>");

// 3. 发送带附件的邮件
File attachment = new File("/path/to/file.pdf");
String messageId = MailUtils.sendHtml("to@example.com", "标题", "<p>内容</p>", attachment);

// 4. 发送带抄送/密送的邮件
String messageId = MailUtils.send(
    "to@example.com",       // 收件人
    "cc@example.com",       // 抄送
    "bcc@example.com",      // 密送
    "标题",
    "<p>HTML内容</p>",
    true,                   // isHtml
    attachment              // 附件（可选）
);

// 5. 群发邮件
Collection<String> tos = List.of("user1@example.com", "user2@example.com");
String messageId = MailUtils.sendHtml(tos, "标题", "<p>内容</p>");

// 6. 发送带内联图片的邮件
Map<String, InputStream> imageMap = new HashMap<>();
imageMap.put("logo", new FileInputStream("logo.png"));
String messageId = MailUtils.sendHtml(
    "to@example.com",
    "标题",
    "<p>内容</p><img src='cid:logo'>",  // cid:图片名
    imageMap
);

// 7. 使用自定义邮件账户发送
MailAccount account = MailUtils.getMailAccount("from@qq.com", "user@qq.com", "password");
String messageId = MailUtils.send(account, tos, "标题", "内容", true);
```

### MailUtils 方法速查

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `sendText(to, subject, content, files...)` | 单收件人 | String (message-id) | 纯文本 |
| `sendText(tos, subject, content, files...)` | 多收件人 | String (message-id) | 纯文本群发 |
| `sendHtml(to, subject, content, files...)` | 单收件人 | String (message-id) | HTML |
| `sendHtml(tos, subject, content, files...)` | 多收件人 | String (message-id) | HTML群发 |
| `sendHtml(to, subject, content, imageMap, files...)` | 带内联图片 | String (message-id) | HTML+图片 |
| `send(to, cc, bcc, subject, content, isHtml, files...)` | 抄送/密送 | String (message-id) | 完整参数 |
| `send(account, tos, ccs, bccs, subject, content, imageMap, isHtml, files...)` | 自定义账户 | String (message-id) | 最完整 |
| `getMailAccount()` | 无 | MailAccount | 获取默认账户 |
| `getMailAccount(from, user, pass)` | 自定义 | MailAccount | 创建自定义账户 |

### 实际使用示例：邮件验证码

```java
// 参考：CaptchaController.java
@RateLimiter(key = "#email", time = 60, count = 1)
public void emailCodeImpl(String email) {
    String code = RandomUtil.randomNumbers(4);
    // 缓存验证码到 Redis
    RedisUtils.setCacheObject(
        GlobalConstants.CAPTCHA_CODE_KEY + email,
        code,
        Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION)
    );
    // 发送邮件
    MailUtils.sendText(email, "登录验证码",
        "您本次验证码为：" + code + "，有效性为"
        + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。");
}
```

---

## 三、统一消息推送（ruoyi-common-message）

### 架构概览

```
MessagePushService（编排器，358行）
    │
    ├── 发现所有 MessageChannel 实现（Spring 自动注入）
    │
    ├── 通道优先级排序
    │   ├── websocket (priority=1)  ← ruoyi-common-websocket
    │   ├── sse (priority=2)        ← ruoyi-common-sse
    │   ├── sms (priority=3)        ← ruoyi-common-sms
    │   ├── miniapp (priority=4)    ← ruoyi-common-miniapp
    │   └── mp (priority=5)         ← ruoyi-common-mp
    │
    ├── 消息类型路由策略
    │   ├── verify_code → sms, email
    │   ├── order → websocket, miniapp, mp
    │   ├── promotion → miniapp, mp, sms
    │   ├── system_notice → websocket, sse
    │   ├── important → sms, websocket, miniapp, mp
    │   └── default → websocket
    │
    └── 发送模式
        ├── send()             → 指定通道发送
        ├── sendWithFallback() → 依次降级
        ├── broadcast()        → 多通道广播
        ├── sendAuto()         → 按优先级自动
        └── sendByMessageType()→ 按类型路由
```

### 核心接口

#### MessageChannel（通道接口，127行）

```java
// 位置：ruoyi-common-core/src/main/java/plus/ruoyi/common/core/message/MessageChannel.java

public interface MessageChannel {
    /** 通道类型标识（如 "sms"、"websocket"） */
    String getChannelType();

    /** 通道名称（如 "短信推送"、"WebSocket推送"） */
    String getChannelName();

    /** 发送消息 */
    MessageResult send(MessageContext context);

    /** 批量发送（默认逐个调用 send） */
    default List<MessageResult> batchSend(List<MessageContext> contexts);

    /** 是否启用 */
    boolean isEnabled();

    /** 优先级（数字越小优先级越高） */
    default int getPriority() { return 5; }

    /** 健康检查 */
    default boolean healthCheck() { return isEnabled(); }

    /** 是否支持指定租户 */
    default boolean supportTenant(String tenantId) { return true; }
}
```

**优先级建议值**：

| 优先级 | 通道类型 | 说明 |
|--------|---------|------|
| 1 | WebSocket/SSE | 实时推送，成本最低 |
| 3 | SMS | 短信，成本较高 |
| 5 | Miniapp/MP/Email | 第三方推送（默认值） |
| 10 | 站内信 | 系统通知 |

#### MessageContext（消息上下文，245行）

```java
// 位置：ruoyi-common-core/src/main/java/plus/ruoyi/common/core/message/MessageContext.java

// 核心字段
private String messageId;            // 消息ID（唯一标识）
private String tenantId;             // 租户ID
private List<Long> userIds;          // 目标用户ID列表
private String content;              // 消息内容（纯文本或JSON）
private Map<String, Object> params;  // 扩展参数（各通道特有参数）
private String messageType;          // 消息类型（用于路由）
private LocalDateTime createTime;    // 创建时间
private LocalDateTime expireTime;    // 过期时间（可选）
private Integer priority = 5;        // 优先级（0-10）
private Boolean persistent = false;  // 是否需要持久化
private Integer retryCount = 0;      // 当前重试次数
private Integer maxRetry = 0;        // 最大重试次数

// 工厂方法
MessageContext.of(userId, content)                   // 单用户
MessageContext.of(userIds, content)                   // 多用户
MessageContext.of(userIds, content, params)           // 带扩展参数
MessageContext.ofParams(userIds, params)              // 仅参数（无content）

// 业务方法
context.isExpired()       // 是否已过期
context.canRetry()        // 是否可重试
context.incrementRetry()  // 增加重试次数
```

**params 扩展参数说明**（不同通道需要不同参数）：

| 通道 | 必填参数 | 可选参数 |
|------|---------|---------|
| websocket | 无 | 无 |
| sse | 无 | 无 |
| sms | `phone` | `configId`(默认"config1"), `templateId` |
| miniapp | `appid`, `openid`, `templateId`, `data`(Map) | `page` |
| mp | `appid`, `openid`, `templateId`, `data`(Map) | `url` |

#### MessageResult（发送结果，178行）

```java
// 位置：ruoyi-common-core/src/main/java/plus/ruoyi/common/core/message/MessageResult.java

// 核心字段
private Boolean success;            // 是否发送成功
private String messageId;           // 消息ID
private String channelType;         // 通道类型
private Long userId;                // 目标用户ID
private String errorMessage;        // 错误信息
private String errorCode;           // 错误码
private String thirdPartyMsgId;     // 第三方消息ID
private LocalDateTime sendTime;     // 发送时间
private Long costTime;              // 耗时（毫秒）
private String extra;               // 扩展信息

// 工厂方法
MessageResult.success(messageId, channelType, userId)
MessageResult.fail(messageId, channelType, userId, errorMessage)
MessageResult.fail(messageId, channelType, userId, errorCode, errorMessage)

// 业务方法
result.isSuccess()  // 判断成功
result.isFail()     // 判断失败
```

### MessagePushService 使用方法

```java
import plus.ruoyi.common.message.service.MessagePushService;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;

@Autowired
private MessagePushService messagePushService;
```

#### 1. 指定通道发送

```java
// 通过 WebSocket 发送
MessageContext context = MessageContext.of(userId, "您有一条新消息");
MessageResult result = messagePushService.send("websocket", context);

// 通过短信发送（需要 params）
Map<String, Object> params = new HashMap<>();
params.put("phone", "13800138000");
params.put("templateId", "SMS_123456");
MessageContext smsContext = MessageContext.of(userId, null, params);
MessageResult result = messagePushService.send("sms", smsContext);
```

#### 2. 降级发送（推荐）

```java
// 优先 WebSocket，失败则短信
MessageContext context = MessageContext.of(userId, "订单已发货");
context.setParams(Map.of("phone", "13800138000"));
MessageResult result = messagePushService.sendWithFallback(
    List.of("websocket", "sms"),
    context
);
// sendWithFallback 会按顺序尝试，第一个成功即返回
```

#### 3. 广播发送

```java
// 同时通过 WebSocket 和短信发送
MessageContext context = MessageContext.of(userIds, "重要通知");
context.setParams(Map.of("phone", "13800138000"));
List<MessageResult> results = messagePushService.broadcast(
    List.of("websocket", "sms"),
    context
);
// broadcast 会向所有通道发送，返回每个通道的结果
```

#### 4. 自动选择通道

```java
// 按优先级自动选择第一个可用通道
MessageContext context = MessageContext.of(userId, "通知消息");
MessageResult result = messagePushService.sendAuto(context);
// 按 priority 排序：websocket(1) → sse(2) → sms(3) → miniapp(4) → mp(5)
```

#### 5. 按消息类型路由（推荐）

```java
// 验证码 → 自动路由到 sms, email
MessageContext codeContext = MessageContext.of(userId, null);
codeContext.setMessageType("verify_code");
codeContext.setParams(Map.of("phone", "13800138000"));
MessageResult result = messagePushService.sendByMessageType(codeContext);

// 订单通知 → 自动路由到 websocket, miniapp, mp
MessageContext orderContext = MessageContext.of(userId, "订单已发货");
orderContext.setMessageType("order");
MessageResult result = messagePushService.sendByMessageType(orderContext);

// 系统通知 → 自动路由到 websocket, sse
MessageContext noticeContext = MessageContext.of(userIds, "系统维护通知");
noticeContext.setMessageType("system_notice");
MessageResult result = messagePushService.sendByMessageType(noticeContext);
```

**消息类型路由表**：

| messageType | 路由通道（按顺序降级） | 适用场景 |
|-------------|---------------------|---------|
| `verify_code` | sms → email | 验证码 |
| `order` | websocket → miniapp → mp | 订单通知 |
| `promotion` | miniapp → mp → sms | 营销消息 |
| `system_notice` | websocket → sse | 系统通知 |
| `important` | sms → websocket → miniapp → mp | 重要通知（全通道） |
| 默认 | websocket | 其他消息 |

#### 6. 查询可用通道

```java
// 获取所有已启用的通道
List<MessageChannel> channels = messagePushService.getAvailableChannels();

// 获取所有已启用的通道类型名
List<String> types = messagePushService.getAvailableChannelTypes();
// 例如：["websocket", "sse", "sms"]

// 获取指定通道的详细信息
String info = messagePushService.getChannelInfo("sms");
```

---

## 四、SmsMessageChannel 实现参考

`SmsMessageChannel` 是 `MessageChannel` 接口的短信实现（211行），可作为自定义通道的参考：

```java
// 参考：ruoyi-common-sms/channel/SmsMessageChannel.java

@Slf4j
@Component
public class SmsMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() { return "sms"; }

    @Override
    public String getChannelName() { return "短信推送"; }

    @Override
    public int getPriority() { return 3; }  // 短信成本较高，优先级中等

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public boolean supportTenant(String tenantId) { return true; }

    @Override
    public MessageResult send(MessageContext context) {
        // 1. 提取参数
        String phone = (String) context.getParams().get("phone");
        if (StringUtils.isBlank(phone)) {
            return MessageResult.fail(context.getMessageId(), getChannelType(),
                null, "MISSING_PHONE", "手机号不能为空");
        }

        // 2. 获取 SMS4J 实例
        String configId = (String) context.getParams()
            .getOrDefault("configId", "config1");
        SmsBlend smsBlend = SmsFactory.getSmsBlend(configId);

        // 3. 发送（模板短信 or 普通短信）
        String templateId = (String) context.getParams().get("templateId");
        SmsResponse response;
        if (StringUtils.isNotBlank(templateId)) {
            // 模板短信（推荐）
            response = smsBlend.sendMessage(phone, templateId, ...);
        } else {
            // 普通短信
            response = smsBlend.sendMessage(phone, context.getContent());
        }

        // 4. 构建结果
        if (response.isSuccess()) {
            MessageResult result = MessageResult.success(
                context.getMessageId(), getChannelType(), null);
            result.setThirdPartyMsgId(String.valueOf(response.getData()));
            result.setExtra(String.valueOf(response.getData()));
            return result;
        } else {
            return MessageResult.fail(
                context.getMessageId(), getChannelType(),
                null, String.valueOf(response.getData()));
        }
    }
}
```

---

## 五、扩展自定义通道

实现 `MessageChannel` 接口即可接入统一消息推送：

```java
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;

@Slf4j
@Component
public class DingTalkMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() { return "dingtalk"; }

    @Override
    public String getChannelName() { return "钉钉推送"; }

    @Override
    public int getPriority() { return 6; }

    @Override
    public boolean isEnabled() {
        // 根据配置判断是否启用
        return SpringUtils.getProperty("dingtalk.enabled", Boolean.class, false);
    }

    @Override
    public MessageResult send(MessageContext context) {
        try {
            String webhook = (String) context.getParams().get("webhook");
            // 调用钉钉 API...
            return MessageResult.success(
                context.getMessageId(), getChannelType(), null);
        } catch (Exception e) {
            return MessageResult.fail(
                context.getMessageId(), getChannelType(),
                null, e.getMessage());
        }
    }
}
```

注册为 Spring Bean 后，`MessagePushService` 会自动发现并纳入通道列表。

---

## 六、公告通知系统（SysNotice）

系统公告通过 WebSocket 推送给目标用户，不使用短信/邮件通道。

### 推送目标类型

| 推送目标 | 说明 |
|---------|------|
| 全部用户 | 广播给所有在线用户 |
| 指定部门 | 推送给部门下所有用户 |
| 指定角色 | 推送给角色下所有用户 |
| 指定用户 | 点对点推送 |

### 使用示例

```java
// 参考：SysNoticeServiceImpl.java（441行）
public void sendNoticeNotification(SysNoticeBo bo) {
    if (StringUtils.isNotBlank(bo.getTargetUserIds())
        && DictEnableStatus.isEnabled(bo.getStatus())) {
        String type = DictNoticeType.getByValue(bo.getNoticeType()).getLabel();
        String message = "[" + type + "] " + bo.getNoticeTitle();
        List<Long> userIds = StringUtils.splitToList(
            bo.getTargetUserIds(), Convert::toLong);
        WebSocketUtils.publishMessage(
            WebSocketMessageDto.of(userIds, message));
    }
}
```

> 如需同时发送短信/邮件通知，可结合 `MessagePushService` 使用。

---

## 七、完整业务集成示例

### 场景：订单发货通知（多通道）

```java
@Service
public class OrderNotificationServiceImpl {

    @Autowired
    private MessagePushService messagePushService;

    /**
     * 订单发货通知（WebSocket + 小程序订阅消息）
     */
    public void notifyShipped(Long userId, String orderNo, String expressNo) {
        // 1. 构建消息
        String content = JsonUtils.toJsonString(Map.of(
            "type", "order_shipped",
            "orderNo", orderNo,
            "expressNo", expressNo
        ));
        MessageContext context = MessageContext.of(userId, content);
        context.setMessageType("order");

        // 2. 按消息类型自动路由（order → websocket, miniapp, mp）
        MessageResult result = messagePushService.sendByMessageType(context);

        if (result.isFail()) {
            log.warn("订单发货通知发送失败: userId={}, error={}",
                userId, result.getErrorMessage());
        }
    }

    /**
     * 重要通知（带短信降级）
     */
    public void notifyImportant(Long userId, String phone, String message) {
        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);

        MessageContext context = MessageContext.of(userId, message, params);
        context.setMessageType("important");

        // important → sms, websocket, miniapp, mp（降级发送）
        MessageResult result = messagePushService.sendByMessageType(context);
    }
}
```

### 场景：验证码发送

```java
@Service
public class CaptchaServiceImpl {

    /**
     * 短信验证码（直接使用 SmsFactory）
     */
    @RateLimiter(key = "#phone", time = 60, count = 1)
    public void sendSmsCode(String phone) {
        String code = RandomUtil.randomNumbers(4);
        RedisUtils.setCacheObject(
            GlobalConstants.CAPTCHA_CODE_KEY + phone,
            code,
            Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION)
        );
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("code", code);
        SmsBlend smsBlend = SmsFactory.getSmsBlend("config1");
        SmsResponse response = smsBlend.sendMessage(
            phone, smsProperties.getTemplateId(), map);
        if (!response.isSuccess()) {
            throw new ServiceException("短信发送异常");
        }
    }

    /**
     * 邮件验证码（直接使用 MailUtils）
     */
    @RateLimiter(key = "#email", time = 60, count = 1)
    public void sendEmailCode(String email) {
        String code = RandomUtil.randomNumbers(4);
        RedisUtils.setCacheObject(
            GlobalConstants.CAPTCHA_CODE_KEY + email,
            code,
            Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION)
        );
        MailUtils.sendText(email, "登录验证码",
            "您本次验证码为：" + code + "，有效性为"
            + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。");
    }
}
```

---

## 八、常见错误与最佳实践

### ✅ 正确做法

```java
// 1. 短信发送前检查频率限制（使用 @RateLimiter）
@RateLimiter(key = "#phone", time = 60, count = 1)
public void sendSmsCode(String phone) { ... }

// 2. 邮件发送前检查是否启用
// MailAutoConfiguration 已通过 @ConditionalOnProperty 控制
// 如果 mail.enabled=false，MailUtils 不可用（Bean 未注册）

// 3. 使用 MessagePushService 统一推送（支持降级和路由）
messagePushService.sendByMessageType(context);  // 自动路由
messagePushService.sendWithFallback(channels, context);  // 降级

// 4. 短信使用模板发送（推荐）
smsBlend.sendMessage(phone, templateId, params);  // ✅ 模板短信

// 5. 消息通道实现 isEnabled() 判断启用状态
@Override
public boolean isEnabled() { return configEnabled; }
```

### ❌ 常见错误

```java
// 1. 忘记配置 mail.enabled=true
mail:
  enabled: false  // ❌ 邮件功能不可用，MailUtils Bean 未注册

// 2. 短信直接发内容而非模板
smsBlend.sendMessage(phone, "验证码: 123456");  // ⚠️ 非模板，部分厂商不支持
smsBlend.sendMessage(phone, templateId, params);  // ✅ 使用模板

// 3. 未处理发送失败
SmsResponse response = smsBlend.sendMessage(phone, templateId, params);
// ❌ 没有检查 response.isSuccess()
if (!response.isSuccess()) {  // ✅ 必须检查
    throw new ServiceException("短信发送失败");
}

// 4. MessageContext 缺少必要的 params
MessageContext context = MessageContext.of(userId, "消息");
messagePushService.send("sms", context);  // ❌ 缺少 phone 参数

// 正确写法：
context.setParams(Map.of("phone", "13800138000"));  // ✅

// 5. 发送重要消息只用一个通道
messagePushService.send("websocket", context);  // ❌ 用户可能不在线
messagePushService.sendWithFallback(             // ✅ 降级发送
    List.of("websocket", "sms"), context);
```

---

## 九、与其他技能的关系

| 技能 | 关系 |
|------|------|
| `realtime-communication` | WebSocket/SSE 通道的底层实现，本技能通过 MessageChannel 接口集成 |
| `wechat-integration` | 小程序订阅消息（miniapp）和公众号模板消息（mp）通道的底层实现 |
| `redis-cache` | PlusSmsDao 使用 Redis 存储 SMS4J 频率数据；验证码缓存 |
| `security-guard` | @RateLimiter 限制发送频率，防止短信轰炸 |
| `crud-development` | SysNotice 公告管理使用标准 CRUD 模式 |

---

## 十、参考文件索引

### 后端核心

| 文件 | 行数 | 说明 |
|------|------|------|
| `ruoyi-common/ruoyi-common-sms/` | - | 短信模块根目录 |
| `sms/config/SmsAutoConfiguration.java` | - | SMS 自动配置 |
| `sms/core/dao/PlusSmsDao.java` | - | Redis 缓存 DAO |
| `sms/handler/SmsExceptionHandler.java` | - | 短信异常处理 |
| `sms/channel/SmsMessageChannel.java` | 211 | MessageChannel 短信实现 |
| `ruoyi-common/ruoyi-common-mail/` | - | 邮件模块根目录 |
| `mail/config/MailAutoConfiguration.java` | - | 邮件自动配置 |
| `mail/config/properties/MailProperties.java` | - | 邮件配置属性 |
| `mail/utils/MailUtils.java` | 470 | 邮件工具类 |
| `ruoyi-common/ruoyi-common-message/` | - | 统一消息模块根目录 |
| `message/config/MessageAutoConfiguration.java` | - | 消息自动配置 |
| `message/service/MessagePushService.java` | 358 | 统一消息推送服务 |

### 核心接口（ruoyi-common-core）

| 文件 | 行数 | 说明 |
|------|------|------|
| `core/message/MessageChannel.java` | 127 | 通道接口（8个方法） |
| `core/message/MessageContext.java` | 245 | 消息上下文（12字段+4工厂方法） |
| `core/message/MessageResult.java` | 178 | 发送结果（10字段+3工厂方法） |

### 实际使用示例

| 文件 | 说明 |
|------|------|
| `ruoyi-system/auth/controller/CaptchaController.java` | 短信/邮件验证码发送 |
| `ruoyi-system/auth/strategy/impl/SmsAuthStrategy.java` | 短信登录认证 |
| `ruoyi-system/auth/strategy/impl/EmailAuthStrategy.java` | 邮件登录认证 |
| `ruoyi-system/config/service/impl/SysNoticeServiceImpl.java` | 公告 WebSocket 推送 |

### 通道实现

| 文件 | channelType | priority |
|------|-------------|----------|
| `ruoyi-common-websocket/channel/WebSocketMessageChannel.java` | websocket | 1 |
| `ruoyi-common-sse/channel/SseMessageChannel.java` | sse | 2 |
| `ruoyi-common-sms/channel/SmsMessageChannel.java` | sms | 3 |
| `ruoyi-common-miniapp/channel/MiniappMessageChannel.java` | miniapp | 4 |
| `ruoyi-common-mp/channel/MpMessageChannel.java` | mp | 5 |

---

## 十一、FAQ

### Q1: 直接使用 SmsFactory/MailUtils 还是 MessagePushService？

**A:**
- **简单场景**（只发短信或只发邮件）→ 直接使用 `SmsFactory` / `MailUtils`
- **复杂场景**（多通道降级、按类型路由、广播）→ 使用 `MessagePushService`
- `MessagePushService` 是上层编排，底层仍调用各通道的 `send()` 方法

### Q2: 如何新增短信供应商？

**A:** 在 `application.yml` 的 `sms.blends` 下新增配置：
```yaml
sms:
  blends:
    config3:
      supplier: huawei  # SMS4J 支持的供应商
      access-key-id: xxx
      access-key-secret: xxx
```
发送时指定 configId：`SmsFactory.getSmsBlend("config3")`

### Q3: 邮件模块 Bean 未注册怎么办？

**A:** 检查 `mail.enabled` 配置。`MailAutoConfiguration` 使用 `@ConditionalOnProperty(value = "mail.enabled", havingValue = "true")`，只有 `mail.enabled=true` 时才会注册 `MailAccount` Bean。

### Q4: MessagePushService 如何实现降级？

**A:** `sendWithFallback(channelTypes, context)` 按列表顺序尝试每个通道，第一个成功即返回。如果通道未启用（`isEnabled()=false`）或发送失败，自动尝试下一个。

### Q5: 如何扩展消息类型路由？

**A:** 消息类型路由在 `MessagePushService.selectChannelsByMessageType()` 方法中硬编码。如需新增类型，修改该方法的 switch 语句：
```java
case "verify_code" -> List.of("sms", "email");
case "order" -> List.of("websocket", "miniapp", "mp");
case "my_custom_type" -> List.of("dingtalk", "sms");  // 新增
```

### Q6: 短信频率限制如何工作？

**A:** 两层限制：
1. **业务层**：`@RateLimiter(key = "#phone", time = 60, count = 1)` — 每分钟1次
2. **SMS4J 层**：`sms.minute-max=1`（每分钟）、`sms.account-max=30`（每日）— 由 `PlusSmsDao`（Redis）计数
