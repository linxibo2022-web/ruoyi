# 统一消息推送模块 - 使用示例集合

本文档提供统一消息推送模块的实际业务场景示例代码。

---

## 📋 目录

1. [用户认证场景](#用户认证场景)
2. [订单业务场景](#订单业务场景)
3. [营销推广场景](#营销推广场景)
4. [系统通知场景](#系统通知场景)
5. [错误处理与重试](#错误处理与重试)
6. [异步消息发送](#异步消息发送)
7. [批量消息推送](#批量消息推送)
8. [监控与统计](#监控与统计)

---

## 用户认证场景

### 1. 发送验证码（智能降级）

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MessagePushService messagePushService;

    /**
     * 发送登录验证码
     * 策略：优先短信，失败自动切换邮件
     */
    public void sendLoginVerifyCode(String phone, String email) {
        // 1. 生成验证码
        String code = generateVerifyCode();

        // 2. 存储到 Redis（5分钟过期）
        String key = "verify_code:" + phone;
        RedisUtils.setCacheObject(key, code, 5, TimeUnit.MINUTES);

        // 3. 发送验证码（短信失败自动切换邮件）
        MessageContext context = MessageContext.of(null, "您的验证码是：" + code)
            .setParams(Map.of(
                "phone", phone,
                "email", email,
                "templateId", "SMS_VERIFY_CODE",
                "templateParams", Map.of("code", code)
            ))
            .setMessageType("verify_code");

        MessageResult result = messagePushService.sendWithFallback(
            List.of("sms", "email"),
            context
        );

        if (!result.isSuccess()) {
            throw ServiceException.of("验证码发送失败，请稍后重试");
        }

        log.info("验证码发送成功: phone={}, channel={}, cost={}ms",
            phone, result.getChannelType(), result.getCostTime());
    }

    private String generateVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(999999));
    }
}
```

### 2. 登录成功通知

```java
/**
 * 用户登录成功，多渠道通知
 */
public void notifyLoginSuccess(Long userId, String device, String location) {
    // 1. 实时推送到 WebSocket（在线用户立即收到）
    MessageContext wsContext = MessageContext.of(
        userId,
        String.format("您的账号于 %s 在 %s 登录",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            location
        )
    );
    messagePushService.send("websocket", wsContext);

    // 2. 如果是新设备登录，额外发送短信提醒（安全策略）
    if (isNewDevice(userId, device)) {
        MessageContext smsContext = MessageContext.ofParams(userId, Map.of(
            "phone", getUserPhone(userId),
            "templateId", "SMS_NEW_DEVICE_LOGIN",
            "templateParams", Map.of(
                "device", device,
                "location", location,
                "time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        ));
        messagePushService.send("sms", smsContext);
    }
}
```

---

## 订单业务场景

### 1. 订单创建成功

```java
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 订单创建成功，实时通知
     */
    public void notifyOrderCreated(Long userId, String orderNo, BigDecimal totalAmount) {
        // 1. 实时推送（WebSocket优先，SSE备用）
        MessageContext context = MessageContext.of(
            userId,
            String.format("订单提交成功！订单号：%s，金额：¥%.2f", orderNo, totalAmount)
        );

        messagePushService.sendWithFallback(List.of("websocket", "sse"), context);

        log.info("订单创建通知已发送: userId={}, orderNo={}", userId, orderNo);
    }
}
```

### 2. 订单支付成功（多通道广播）

```java
/**
 * 订单支付成功，重要通知多通道推送
 */
public void notifyOrderPaid(Long userId, OrderPaidDto orderInfo) {
    String orderNo = orderInfo.getOrderNo();
    BigDecimal amount = orderInfo.getAmount();
    String openid = orderInfo.getOpenid();

    // 构建消息上下文
    MessageContext context = MessageContext.of(
        userId,
        String.format("订单 %s 支付成功，金额 ¥%.2f", orderNo, amount)
    ).setMessageType("order");

    // 1. 广播到所有实时通道（WebSocket + SSE）
    messagePushService.broadcast(List.of("websocket", "sse"), context);

    // 2. 发送小程序订阅消息（异步）
    CompletableFuture.runAsync(() -> {
        sendMiniappOrderPaidMessage(userId, openid, orderInfo);
    });

    // 3. 高价值订单额外发送短信（金额 > 500元）
    if (amount.compareTo(new BigDecimal("500")) > 0) {
        sendHighValueOrderSms(userId, orderNo, amount);
    }

    // 4. VIP用户同时发送公众号模板消息
    if (isVipUser(userId)) {
        sendMpOrderPaidMessage(userId, orderInfo);
    }
}

/**
 * 发送小程序订阅消息
 */
private void sendMiniappOrderPaidMessage(Long userId, String openid, OrderPaidDto orderInfo) {
    MessageContext miniappContext = MessageContext.ofParams(userId, Map.of(
        "appid", "wx1234567890",
        "openid", openid,
        "templateId", "order_pay_success",
        "data", Map.of(
            "character_string1", orderInfo.getOrderNo(),  // 订单号
            "amount2", String.format("¥%.2f", orderInfo.getAmount()),  // 金额
            "date3", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),  // 时间
            "thing4", "感谢您的购买，我们将尽快为您发货"  // 备注
        ),
        "page", "pages/order/detail?id=" + orderInfo.getOrderNo()
    ));

    MessageResult result = messagePushService.send("miniapp", miniappContext);

    if (!result.isSuccess()) {
        log.warn("小程序订阅消息发送失败: userId={}, error={}", userId, result.getErrorMessage());
    }
}
```

### 3. 订单发货通知

```java
/**
 * 订单发货，根据消息类型自动选择通道
 */
public void notifyOrderShipped(Long userId, String orderNo, String expressNo, String expressCompany) {
    // 方式1: 使用消息类型自动路由
    MessageContext context = MessageContext.of(
        userId,
        String.format("您的订单 %s 已发货，物流公司：%s，运单号：%s", orderNo, expressCompany, expressNo)
    ).setMessageType("order");

    // 自动选择通道：websocket → miniapp → mp
    messagePushService.sendByMessageType(context);

    // 方式2: 手动指定通道（更精确控制）
    messagePushService.sendWithFallback(List.of("websocket", "miniapp", "mp"), context);
}
```

### 4. 订单退款成功

```java
/**
 * 退款成功，多渠道确保送达
 */
public void notifyRefundSuccess(Long userId, String orderNo, BigDecimal refundAmount, String openid) {
    // 1. 实时推送
    MessageContext realtimeContext = MessageContext.of(
        userId,
        String.format("退款成功！订单 %s，退款金额 ¥%.2f 将在 1-3 个工作日内到账", orderNo, refundAmount)
    );
    messagePushService.sendWithFallback(List.of("websocket", "sse"), realtimeContext);

    // 2. 小程序订阅消息
    MessageContext miniappContext = MessageContext.ofParams(userId, Map.of(
        "appid", "wx1234567890",
        "openid", openid,
        "templateId", "refund_success",
        "data", Map.of(
            "character_string1", orderNo,
            "amount2", String.format("¥%.2f", refundAmount),
            "date3", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "thing4", "退款将在 1-3 个工作日内到账，请注意查收"
        ),
        "page", "pages/order/detail?id=" + orderNo
    ));
    messagePushService.send("miniapp", miniappContext);

    // 3. 重要：退款金额较大时发送短信确认（> 200元）
    if (refundAmount.compareTo(new BigDecimal("200")) > 0) {
        MessageContext smsContext = MessageContext.ofParams(userId, Map.of(
            "phone", getUserPhone(userId),
            "templateId", "SMS_REFUND_SUCCESS",
            "templateParams", Map.of(
                "orderNo", orderNo,
                "amount", refundAmount.toString()
            )
        ));
        messagePushService.send("sms", smsContext);
    }
}
```

---

## 营销推广场景

### 1. 优惠券到期提醒

```java
@Service
@RequiredArgsConstructor
public class CouponNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 优惠券即将过期提醒（批量）
     * 策略：小程序优先，公众号备用，不打扰用户（不发短信）
     */
    public void notifyCouponExpiring(List<CouponExpiringDto> expiringList) {
        expiringList.forEach(item -> {
            MessageContext context = MessageContext.of(
                item.getUserId(),
                String.format("您有一张 %s 优惠券即将过期，有效期至 %s",
                    item.getCouponName(),
                    item.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
            ).setMessageType("promotion");

            // 营销消息：小程序 → 公众号 → 停止（不发短信）
            messagePushService.sendWithFallback(List.of("miniapp", "mp"), context);
        });

        log.info("优惠券过期提醒已发送，数量: {}", expiringList.size());
    }
}
```

### 2. 新品上市推送

```java
/**
 * 新品上市，定向推送给关注用户
 */
public void notifyNewProductLaunched(Long productId, String productName, BigDecimal price, List<Long> subscribedUserIds) {
    // 构建消息
    MessageContext context = MessageContext.of(
        subscribedUserIds,
        String.format("新品上市：%s，限时特价 ¥%.2f", productName, price)
    ).setMessageType("promotion");

    // 方式1: 广播到所有实时通道（在线用户立即看到）
    messagePushService.broadcast(List.of("websocket", "sse"), context);

    // 方式2: 异步发送小程序订阅消息（需要用户提前订阅）
    subscribedUserIds.forEach(userId -> {
        CompletableFuture.runAsync(() -> {
            String openid = getUserOpenid(userId);
            if (openid != null) {
                MessageContext miniappContext = MessageContext.ofParams(userId, Map.of(
                    "appid", "wx1234567890",
                    "openid", openid,
                    "templateId", "new_product_launch",
                    "data", Map.of(
                        "thing1", productName,
                        "amount2", String.format("¥%.2f", price),
                        "thing3", "限时优惠，快来抢购！"
                    ),
                    "page", "pages/product/detail?id=" + productId
                ));
                messagePushService.send("miniapp", miniappContext);
            }
        });
    });
}
```

### 3. 限时活动通知

```java
/**
 * 限时活动开始通知（VIP 用户全渠道推送）
 */
public void notifyFlashSaleStarted(String activityName, LocalDateTime startTime, List<Long> vipUserIds) {
    String message = String.format("限时活动【%s】即将开始！活动时间：%s",
        activityName,
        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

    MessageContext context = MessageContext.of(vipUserIds, message);

    // VIP 用户全渠道推送（确保送达）
    vipUserIds.forEach(userId -> {
        // 1. 实时推送
        messagePushService.sendWithFallback(List.of("websocket", "sse"), context);

        // 2. 小程序/公众号
        messagePushService.sendWithFallback(List.of("miniapp", "mp"), context);

        // 3. 短信提醒（VIP 专属）
        MessageContext smsContext = MessageContext.ofParams(userId, Map.of(
            "phone", getUserPhone(userId),
            "templateId", "SMS_FLASH_SALE",
            "templateParams", Map.of(
                "activityName", activityName,
                "startTime", startTime.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"))
            )
        ));
        messagePushService.send("sms", smsContext);
    });

    log.info("限时活动通知已发送，VIP 用户数: {}", vipUserIds.size());
}
```

---

## 系统通知场景

### 1. 系统维护通知

```java
@Service
@RequiredArgsConstructor
public class SystemNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 系统维护通知（全站广播）
     */
    public void notifySystemMaintenance(LocalDateTime maintenanceTime, Integer durationMinutes) {
        String message = String.format(
            "系统将于 %s 进行维护，预计持续 %d 分钟，维护期间暂停服务，给您带来不便敬请谅解。",
            maintenanceTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            durationMinutes
        );

        // 1. 实时推送给所有在线用户（WebSocket + SSE）
        MessageContext realtimeContext = MessageContext.of(
            getAllOnlineUserIds(),
            message
        ).setMessageType("system_notice");

        messagePushService.broadcast(List.of("websocket", "sse"), realtimeContext);

        // 2. 重要通知：同时发送小程序/公众号通知
        getAllUserIds().forEach(userId -> {
            MessageContext context = MessageContext.of(userId, message);
            messagePushService.sendWithFallback(List.of("miniapp", "mp"), context);
        });

        log.info("系统维护通知已发送，在线用户数: {}", getAllOnlineUserIds().size());
    }

    private List<Long> getAllOnlineUserIds() {
        // 获取所有在线用户ID
        return List.of(1001L, 1002L, 1003L);
    }

    private List<Long> getAllUserIds() {
        // 获取所有用户ID
        return List.of(1001L, 1002L, 1003L);
    }
}
```

### 2. 账户安全提醒

```java
/**
 * 账户异常登录提醒（高优先级）
 */
public void notifyAbnormalLogin(Long userId, String ipAddress, String location, String device) {
    String message = String.format(
        "检测到您的账户在 %s (%s) 尝试登录，设备：%s。如非本人操作，请立即修改密码。",
        location, ipAddress, device
    );

    MessageContext context = MessageContext.of(userId, message)
        .setMessageType("important");

    // 重要安全通知：全渠道推送，确保用户收到
    // 策略：sms → websocket → miniapp → mp
    MessageResult result = messagePushService.sendWithFallback(
        List.of("sms", "websocket", "miniapp", "mp"),
        context
    );

    if (!result.isSuccess()) {
        // 如果所有通道都失败，记录告警日志
        log.error("账户安全提醒发送失败: userId={}, ip={}, location={}", userId, ipAddress, location);
        // 可以触发人工干预或其他安全措施
    }
}
```

### 3. 实名认证审核结果

```java
/**
 * 实名认证审核通过
 */
public void notifyRealNameVerified(Long userId, String userName, String openid) {
    // 1. 实时推送
    MessageContext realtimeContext = MessageContext.of(
        userId,
        String.format("恭喜您，实名认证已通过！姓名：%s", userName)
    );
    messagePushService.sendWithFallback(List.of("websocket", "sse"), realtimeContext);

    // 2. 小程序订阅消息
    MessageContext miniappContext = MessageContext.ofParams(userId, Map.of(
        "appid", "wx1234567890",
        "openid", openid,
        "templateId", "real_name_verified",
        "data", Map.of(
            "thing1", userName,
            "phrase2", "审核通过",
            "date3", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "thing4", "您已完成实名认证，现在可以使用全部功能"
        ),
        "page", "pages/my/profile"
    ));
    messagePushService.send("miniapp", miniappContext);
}

/**
 * 实名认证审核失败
 */
public void notifyRealNameRejected(Long userId, String reason, String openid) {
    String message = String.format("实名认证审核未通过，原因：%s，请重新提交资料", reason);

    // 失败通知需要多渠道确保送达
    MessageContext context = MessageContext.of(userId, message);
    messagePushService.sendWithFallback(List.of("websocket", "miniapp", "sms"), context);
}
```

---

## 错误处理与重试

### 1. 检查发送结果

```java
@Service
@RequiredArgsConstructor
public class RobustNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 发送消息并处理失败情况
     */
    public void sendWithErrorHandling(Long userId, String content) {
        MessageContext context = MessageContext.of(userId, content);

        MessageResult result = messagePushService.send("websocket", context);

        if (result.isSuccess()) {
            log.info("消息发送成功: userId={}, channel={}, cost={}ms",
                userId, result.getChannelType(), result.getCostTime());
        } else {
            log.warn("消息发送失败: userId={}, channel={}, errorCode={}, errorMsg={}",
                userId, result.getChannelType(), result.getErrorCode(), result.getErrorMessage());

            // 失败处理策略
            handleSendFailure(userId, content, result);
        }
    }

    private void handleSendFailure(Long userId, String content, MessageResult failedResult) {
        // 策略1: 尝试备用通道
        MessageContext context = MessageContext.of(userId, content);
        MessageResult retryResult = messagePushService.send("sse", context);

        if (retryResult.isSuccess()) {
            log.info("备用通道发送成功: userId={}, channel={}", userId, retryResult.getChannelType());
            return;
        }

        // 策略2: 保存到失败队列，稍后重试
        saveToFailureQueue(userId, content, failedResult);
    }

    private void saveToFailureQueue(Long userId, String content, MessageResult failedResult) {
        // 保存到 Redis 或数据库，后续通过定时任务重试
        log.error("消息发送失败，已加入重试队列: userId={}, content={}", userId, content);
    }
}
```

### 2. 异步重试机制

```java
@Service
@RequiredArgsConstructor
public class MessageRetryService {

    private final MessagePushService messagePushService;

    /**
     * 发送消息，失败自动加入重试队列
     */
    public void sendWithAutoRetry(Long userId, String content, String channelType) {
        MessageContext context = MessageContext.of(userId, content);

        MessageResult result = messagePushService.send(channelType, context);

        if (!result.isSuccess()) {
            // 加入重试队列
            addToRetryQueue(userId, content, channelType);
        }
    }

    private void addToRetryQueue(Long userId, String content, String channelType) {
        String retryKey = "message:retry:" + UUID.randomUUID();
        Map<String, String> retryData = Map.of(
            "userId", userId.toString(),
            "content", content,
            "channelType", channelType,
            "retryCount", "0",
            "createTime", LocalDateTime.now().toString()
        );

        RedisUtils.setCacheMap(retryKey, retryData);
        RedisUtils.expire(retryKey, 24, TimeUnit.HOURS);  // 24小时后自动删除

        log.warn("消息发送失败，已加入重试队列: retryKey={}", retryKey);
    }

    /**
     * 定时任务：处理重试队列
     */
    @Scheduled(fixedRate = 60000)  // 每分钟执行一次
    public void processRetryQueue() {
        Collection<String> retryKeys = RedisUtils.keys("message:retry:*");
        if (retryKeys == null || retryKeys.isEmpty()) {
            return;
        }

        retryKeys.forEach(retryKey -> {
            Map<Object, Object> retryData = RedisUtils.getCacheMap(retryKey);

            Long userId = Long.valueOf((String) retryData.get("userId"));
            String content = (String) retryData.get("content");
            String channelType = (String) retryData.get("channelType");
            int retryCount = Integer.parseInt((String) retryData.get("retryCount"));

            if (retryCount >= 3) {
                // 重试次数超过3次，放弃重试
                log.error("消息重试失败次数过多，放弃重试: userId={}, retryKey={}", userId, retryKey);
                RedisUtils.deleteObject(retryKey);
                return;
            }

            // 重试发送
            MessageContext context = MessageContext.of(userId, content);
            MessageResult result = messagePushService.send(channelType, context);

            if (result.isSuccess()) {
                // 重试成功，删除队列
                log.info("消息重试成功: userId={}, retryKey={}", userId, retryKey);
                RedisUtils.deleteObject(retryKey);
            } else {
                // 重试失败，增加重试次数
                retryData.put("retryCount", String.valueOf(retryCount + 1));
                RedisUtils.setCacheMap(retryKey, retryData);
                log.warn("消息重试失败，将继续重试: userId={}, retryCount={}", userId, retryCount + 1);
            }
        });
    }
}
```

---

## 异步消息发送

### 1. 使用 @Async 异步发送

```java
@Service
@RequiredArgsConstructor
public class AsyncNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 异步发送消息（不阻塞主流程）
     */
    @Async("messageExecutor")  // 使用自定义线程池
    public void sendAsync(Long userId, String content, String channelType) {
        MessageContext context = MessageContext.of(userId, content);
        MessageResult result = messagePushService.send(channelType, context);

        if (result.isSuccess()) {
            log.info("异步消息发送成功: userId={}, channel={}", userId, channelType);
        } else {
            log.warn("异步消息发送失败: userId={}, error={}", userId, result.getErrorMessage());
        }
    }

    /**
     * 异步广播消息
     */
    @Async("messageExecutor")
    public void broadcastAsync(List<Long> userIds, String content, List<String> channelTypes) {
        userIds.forEach(userId -> {
            MessageContext context = MessageContext.of(userId, content);
            messagePushService.broadcast(channelTypes, context);
        });

        log.info("异步广播完成: userCount={}, channels={}", userIds.size(), channelTypes);
    }
}

/**
 * 自定义线程池配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("messageExecutor")
    public Executor messageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("message-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### 2. 使用 CompletableFuture 并行发送

```java
/**
 * 并行发送多个通道，提高效率
 */
public void sendParallel(Long userId, String content, List<String> channelTypes) {
    MessageContext context = MessageContext.of(userId, content);

    // 并行发送到多个通道
    List<CompletableFuture<MessageResult>> futures = channelTypes.stream()
        .map(channelType -> CompletableFuture.supplyAsync(() ->
            messagePushService.send(channelType, context)
        ))
        .toList();

    // 等待所有发送完成
    CompletableFuture<Void> allOf = CompletableFuture.allOf(
        futures.toArray(new CompletableFuture[0])
    );

    allOf.thenRun(() -> {
        // 统计发送结果
        long successCount = futures.stream()
            .map(CompletableFuture::join)
            .filter(MessageResult::isSuccess)
            .count();

        log.info("并行发送完成: userId={}, total={}, success={}, fail={}",
            userId, channelTypes.size(), successCount, channelTypes.size() - successCount);
    });
}
```

---

## 批量消息推送

### 1. 批量发送（分批处理）

```java
@Service
@RequiredArgsConstructor
public class BatchNotificationService {

    private final MessagePushService messagePushService;

    /**
     * 批量发送消息（分批处理，避免一次性处理过多）
     */
    public void sendBatch(List<Long> userIds, String content, String channelType) {
        int batchSize = 100;  // 每批100个用户
        int totalBatches = (int) Math.ceil((double) userIds.size() / batchSize);

        log.info("开始批量发送消息: totalUsers={}, batchSize={}, totalBatches={}",
            userIds.size(), batchSize, totalBatches);

        for (int i = 0; i < totalBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, userIds.size());
            List<Long> batchUserIds = userIds.subList(start, end);

            // 异步发送每一批
            CompletableFuture.runAsync(() -> {
                sendBatchInternal(batchUserIds, content, channelType);
            });

            // 避免瞬间大量请求，每批之间间隔100ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("批量发送任务已提交: totalBatches={}", totalBatches);
    }

    private void sendBatchInternal(List<Long> userIds, String content, String channelType) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        userIds.forEach(userId -> {
            MessageContext context = MessageContext.of(userId, content);
            MessageResult result = messagePushService.send(channelType, context);

            if (result.isSuccess()) {
                successCount.incrementAndGet();
            } else {
                failCount.incrementAndGet();
                log.warn("批量发送失败: userId={}, error={}", userId, result.getErrorMessage());
            }
        });

        log.info("批量发送完成: batchSize={}, success={}, fail={}",
            userIds.size(), successCount.get(), failCount.get());
    }
}
```

### 2. 定向推送（标签用户）

```java
/**
 * 根据用户标签定向推送
 */
public void sendToTaggedUsers(String tag, String content, List<String> channelTypes) {
    // 1. 根据标签查询用户
    List<Long> taggedUserIds = getUserIdsByTag(tag);

    if (taggedUserIds.isEmpty()) {
        log.warn("标签用户为空，跳过推送: tag={}", tag);
        return;
    }

    log.info("开始定向推送: tag={}, userCount={}", tag, taggedUserIds.size());

    // 2. 分批推送
    MessageContext context = MessageContext.of(taggedUserIds, content);
    List<MessageResult> results = messagePushService.broadcast(channelTypes, context);

    // 3. 统计结果
    long successCount = results.stream().filter(MessageResult::isSuccess).count();
    log.info("定向推送完成: tag={}, total={}, success={}, fail={}",
        tag, results.size(), successCount, results.size() - successCount);
}

private List<Long> getUserIdsByTag(String tag) {
    // 根据标签查询用户ID
    // 例如：VIP用户、活跃用户、沉默用户等
    return List.of(1001L, 1002L, 1003L);
}
```

---

## 监控与统计

### 1. 消息发送统计

```java
@Service
@RequiredArgsConstructor
public class MessageStatisticsService {

    private final MessagePushService messagePushService;

    /**
     * 发送消息并记录统计
     */
    public MessageResult sendWithStats(Long userId, String content, String channelType) {
        long startTime = System.currentTimeMillis();

        MessageContext context = MessageContext.of(userId, content);
        MessageResult result = messagePushService.send(channelType, context);

        long costTime = System.currentTimeMillis() - startTime;

        // 记录统计数据
        recordStats(channelType, result.isSuccess(), costTime);

        return result;
    }

    private void recordStats(String channelType, boolean success, long costTime) {
        String date = LocalDate.now().toString();

        // 统计总数
        String totalKey = String.format("message:stats:%s:total:%s", channelType, date);
        RedisUtils.incrBy(totalKey, 1);

        // 统计成功数
        if (success) {
            String successKey = String.format("message:stats:%s:success:%s", channelType, date);
            RedisUtils.incrBy(successKey, 1);
        } else {
            String failKey = String.format("message:stats:%s:fail:%s", channelType, date);
            RedisUtils.incrBy(failKey, 1);
        }

        // 统计耗时（累加，用于计算平均值）
        String costKey = String.format("message:stats:%s:cost:%s", channelType, date);
        RedisUtils.incrBy(costKey, costTime);

        // 设置过期时间（保留30天）
        RedisUtils.expire(totalKey, 30, TimeUnit.DAYS);
    }

    /**
     * 获取今日统计数据
     */
    public Map<String, Object> getTodayStats(String channelType) {
        String date = LocalDate.now().toString();

        String totalKey = String.format("message:stats:%s:total:%s", channelType, date);
        String successKey = String.format("message:stats:%s:success:%s", channelType, date);
        String failKey = String.format("message:stats:%s:fail:%s", channelType, date);
        String costKey = String.format("message:stats:%s:cost:%s", channelType, date);

        Long total = getLongValue(totalKey);
        Long success = getLongValue(successKey);
        Long fail = getLongValue(failKey);
        Long totalCost = getLongValue(costKey);

        double successRate = total > 0 ? (double) success / total * 100 : 0;
        double avgCost = total > 0 ? (double) totalCost / total : 0;

        return Map.of(
            "channelType", channelType,
            "date", date,
            "total", total,
            "success", success,
            "fail", fail,
            "successRate", String.format("%.2f%%", successRate),
            "avgCostTime", String.format("%.2fms", avgCost)
        );
    }

    private Long getLongValue(String key) {
        String value = RedisUtils.getCacheObject(key);
        return value != null ? Long.parseLong(value) : 0L;
    }
}
```

### 2. 健康监控接口

```java
@RestController
@RequestMapping("/admin/message")
@RequiredArgsConstructor
public class MessageMonitorController {

    private final MessagePushService messagePushService;
    private final MessageStatisticsService statisticsService;

    /**
     * 获取所有可用通道
     */
    @GetMapping("/channels/available")
    public R<List<String>> getAvailableChannels() {
        List<String> channels = messagePushService.getAvailableChannelTypes();
        return R.ok(channels);
    }

    /**
     * 获取通道详细信息
     */
    @GetMapping("/channel/{type}/info")
    public R<String> getChannelInfo(@PathVariable String type) {
        String info = messagePushService.getChannelInfo(type);
        return R.ok(info);
    }

    /**
     * 获取通道统计数据
     */
    @GetMapping("/channel/{type}/stats")
    public R<Map<String, Object>> getChannelStats(@PathVariable String type) {
        Map<String, Object> stats = statisticsService.getTodayStats(type);
        return R.ok(stats);
    }

    /**
     * 获取所有通道统计数据
     */
    @GetMapping("/stats/all")
    public R<List<Map<String, Object>>> getAllStats() {
        List<String> channelTypes = messagePushService.getAvailableChannelTypes();

        List<Map<String, Object>> allStats = channelTypes.stream()
            .map(statisticsService::getTodayStats)
            .toList();

        return R.ok(allStats);
    }

    /**
     * 测试通道连通性
     */
    @PostMapping("/channel/{type}/test")
    public R<String> testChannel(@PathVariable String type, @RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String content = params.get("content").toString();

        MessageContext context = MessageContext.of(userId, content).setParams(params);
        MessageResult result = messagePushService.send(type, context);

        if (result.isSuccess()) {
            return R.ok("测试成功：" + result.getThirdPartyMsgId());
        } else {
            return R.fail("测试失败：" + result.getErrorMessage());
        }
    }
}
```

---

## 📝 总结

本文档提供了统一消息推送模块在各种业务场景下的实际使用示例，涵盖：

- ✅ 用户认证（验证码、登录通知）
- ✅ 订单业务（创建、支付、发货、退款）
- ✅ 营销推广（优惠券、新品、活动）
- ✅ 系统通知（维护、安全、审核）
- ✅ 错误处理与重试机制
- ✅ 异步消息发送
- ✅ 批量消息推送
- ✅ 监控与统计

**核心原则**：
1. **简单场景**：直接注入具体通道
2. **复杂场景**：使用统一调度服务（降级、广播、自动选择）
3. **异步处理**：不阻塞主流程
4. **失败重试**：确保消息送达
5. **监控统计**：实时掌握发送情况

更多信息请参考 [README.md](./README.md)。
