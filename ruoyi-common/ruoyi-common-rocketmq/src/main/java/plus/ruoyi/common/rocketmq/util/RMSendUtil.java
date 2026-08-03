package plus.ruoyi.common.rocketmq.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import plus.ruoyi.common.rocketmq.config.RocketMQProperties;
import plus.ruoyi.common.rocketmq.enums.DelayLevel;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * RocketMQ 消息生产者工具类
 * <p>
 * 提供简化的消息发送 API，封装 RocketMQTemplate 的常用操作
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 同步发送
 * SendResult result = RMProducerUtil.send("topic", message);
 *
 * // 异步发送
 * RMProducerUtil.sendAsync("topic", message, result -> {
 *     log.info("发送成功: {}", result.getMsgId());
 * });
 *
 * // 单向发送
 * RMProducerUtil.sendOneWay("topic", message);
 *
 * // 延迟消息
 * RMProducerUtil.sendDelay("topic", message, DelayLevel.TEN_SECONDS);
 *
 * // 带标签消息
 * RMProducerUtil.sendWithTag("topic", "VIP", message);
 *
 * // 事务消息
 * RMProducerUtil.sendTransaction("topic", message);
 * </pre>
 * </p>
 *
 * @author 路北
 * @date 2025-11-03
 */
@Slf4j
public class RMSendUtil {

    /**
     * RocketMQ 模板（Spring Bean 注入）
     */
    private static RocketMQTemplate rocketMQTemplate;

    /**
     * RocketMQ 配置（Spring Bean 注入）
     */
    private static RocketMQProperties properties;

    /**
     * 私有构造器，防止实例化
     */
    private RMSendUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 初始化工具类
     * <p>由 RocketMQAutoConfiguration 在 Spring 容器启动时调用</p>
     * <p>将 RocketMQTemplate 和配置注入到静态字段中</p>
     *
     * @param template RocketMQ 模板
     * @param props    RocketMQ 配置
     */
    public static void init(RocketMQTemplate template, RocketMQProperties props) {
        rocketMQTemplate = template;
        properties = props;
        log.debug("RMSendUtil 初始化完成");
    }

    // ==================== 核心功能：同步发送 ====================

    /**
     * 同步发送消息（使用默认超时时间）
     * <p>
     * 最简单的发送方式，阻塞等待响应，保证可靠性
     * </p>
     * <p>示例：{@code SendResult result = RMProducerUtil.send("order-topic", orderMessage);}</p>
     *
     * @param topic   Topic 名称
     * @param message 消息对象
     * @return 发送结果
     */
    public static SendResult send(String topic, Object message) {
        return send(topic, message, properties.getProducer().getSendMsgTimeout());
    }

    /**
     * 同步发送消息（自定义超时时间）
     * <p>示例：{@code SendResult result = RMProducerUtil.send("order-topic", orderMessage, 5000);}</p>
     *
     * @param topic   Topic 名称
     * @param message 消息对象
     * @param timeout 超时时间（毫秒）
     * @return 发送结果
     */
    public static SendResult send(String topic, Object message, int timeout) {
        checkTemplateAvailable();

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        return doSyncSend(() -> rocketMQTemplate.syncSend(topic, message, timeout), topic, "同步发送");
    }

    /**
     * 同步发送消息（带自动创建 Topic）
     * <p>
     * 显式要求自动创建 Topic，忽略全局配置
     * </p>
     * <p>示例：{@code SendResult result = RMProducerUtil.sendWithAutoCreate("order-topic", orderMessage);}</p>
     *
     * @param topic   Topic 名称
     * @param message 消息对象
     * @return 发送结果
     */
    public static SendResult sendWithAutoCreate(String topic, Object message) {
        return sendWithAutoCreate(topic, message, properties.getProducer().getSendMsgTimeout());
    }

    /**
     * 同步发送消息（带自动创建 Topic 和自定义超时）
     * <p>示例：{@code SendResult result = RMProducerUtil.sendWithAutoCreate("order-topic", orderMessage, 5000);}</p>
     *
     * @param topic   Topic 名称
     * @param message 消息对象
     * @param timeout 超时时间（毫秒）
     * @return 发送结果
     */
    public static SendResult sendWithAutoCreate(String topic, Object message, int timeout) {
        checkTemplateAvailable();

        // 强制自动创建 Topic
        autoCreateTopic(topic);

        return doSyncSend(() -> rocketMQTemplate.syncSend(topic, message, timeout), topic, "同步发送");
    }

    // ==================== 核心功能：异步发送 ====================

    /**
     * 异步发送消息（完整回调）
     * <p>
     * 不阻塞主线程，通过回调通知发送结果
     * </p>
     * <p>
     * 示例：
     * <pre>
     * RMProducerUtil.sendAsync("order-topic", message, new SendCallback() {
     *     public void onSuccess(SendResult result) {
     *         log.info("发送成功: {}", result.getMsgId());
     *     }
     *     public void onException(Throwable e) {
     *         log.error("发送失败", e);
     *     }
     * });
     * </pre>
     * </p>
     *
     * @param topic    Topic 名称
     * @param message  消息对象
     * @param callback 回调接口
     */
    public static void sendAsync(String topic, Object message, SendCallback callback) {
        sendAsync(topic, message, callback, properties.getProducer().getSendMsgTimeout());
    }

    /**
     * 异步发送消息（完整回调 + 自定义超时）
     * <p>示例：{@code RMProducerUtil.sendAsync("order-topic", message, callback, 5000);}</p>
     *
     * @param topic    Topic 名称
     * @param message  消息对象
     * @param callback 回调接口
     * @param timeout  超时时间（毫秒）
     */
    public static void sendAsync(String topic, Object message, SendCallback callback, int timeout) {
        checkTemplateAvailable();

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        if (properties.getProducer().getEnableLog()) {
            log.info("📤 异步发送消息: topic={}, message={}", topic, message);
        }

        rocketMQTemplate.asyncSend(topic, message, callback, timeout);
    }

    /**
     * 异步发送消息（简化回调，仅处理成功）
     * <p>
     * 简化版异步发送，只需要提供成功回调，失败自动记录日志
     * </p>
     * <p>
     * 示例：
     * <pre>
     * RMProducerUtil.sendAsync("order-topic", message, result -> {
     *     log.info("发送成功: {}", result.getMsgId());
     * });
     * </pre>
     * </p>
     *
     * @param topic           Topic 名称
     * @param message         消息对象
     * @param successCallback 成功回调（只处理成功情况）
     */
    public static void sendAsync(String topic, Object message, Consumer<SendResult> successCallback) {
        sendAsync(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                successCallback.accept(sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("❌ 异步消息发送失败: topic={}", topic, e);
            }
        });
    }

    // ==================== 核心功能：单向发送 ====================

    /**
     * 单向发送消息（不等待响应）
     * <p>
     * 性能最高但不保证可靠性，适合日志、监控等不重要消息
     * </p>
     * <p>示例：{@code RMProducerUtil.sendOneWay("log-topic", logMessage);}</p>
     *
     * @param topic   Topic 名称
     * @param message 消息对象
     */
    public static void sendOneWay(String topic, Object message) {
        checkTemplateAvailable();

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        if (properties.getProducer().getEnableLog()) {
            log.info("📤 单向发送消息: topic={}, message={}", topic, message);
        }

        rocketMQTemplate.sendOneWay(topic, message);
    }

    // ==================== 常用功能：延迟消息 ====================

    /**
     * 发送延迟消息（使用枚举）
     * <p>
     * RocketMQ 支持 18 个固定延迟级别，不支持自定义延迟时间
     * </p>
     * <p>示例：{@code RMProducerUtil.sendDelay("order-topic", message, DelayLevel.TEN_SECONDS);}</p>
     *
     * @param topic      Topic 名称
     * @param message    消息对象
     * @param delayLevel 延迟级别枚举
     * @return 发送结果
     */
    public static SendResult sendDelay(String topic, Object message, DelayLevel delayLevel) {
        return sendDelay(topic, message, delayLevel.getLevel());
    }

    /**
     * 发送延迟消息（使用级别数字）
     * <p>示例：{@code RMProducerUtil.sendDelay("order-topic", message, 3); // 10秒后消费}</p>
     *
     * @param topic      Topic 名称
     * @param message    消息对象
     * @param delayLevel 延迟级别（1-18）
     * @return 发送结果
     */
    public static SendResult sendDelay(String topic, Object message, int delayLevel) {
        return sendDelay(topic, message, delayLevel, properties.getProducer().getSendMsgTimeout());
    }

    /**
     * 发送延迟消息（完整参数）
     * <p>示例：{@code RMProducerUtil.sendDelay("order-topic", message, 3, 5000);}</p>
     *
     * @param topic      Topic 名称
     * @param message    消息对象
     * @param delayLevel 延迟级别（1-18）
     * @param timeout    超时时间（毫秒）
     * @return 发送结果
     */
    public static SendResult sendDelay(String topic, Object message, int delayLevel, int timeout) {
        checkTemplateAvailable();

        // 验证延迟级别
        if (delayLevel < 1 || delayLevel > 18) {
            throw new IllegalArgumentException("延迟级别必须在 1-18 之间，当前值: " + delayLevel);
        }

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        DelayLevel level = DelayLevel.fromLevel(delayLevel);
        if (properties.getProducer().getEnableLog()) {
            log.info("📤 发送延迟消息: topic={}, delayLevel={} ({})", topic, delayLevel, level.getDescription());
        }

        Message<Object> msg = MessageBuilder.withPayload(message).build();
        return doSyncSend(
            () -> rocketMQTemplate.syncSend(topic, msg, timeout, delayLevel),
            topic,
            "延迟消息发送 (delay=" + level.getDescription() + ")"
        );
    }

    // ==================== 常用功能：带标签消息 ====================

    /**
     * 发送带标签的消息
     * <p>
     * 消费者可以通过标签过滤消息
     * </p>
     * <p>示例：{@code RMProducerUtil.sendWithTag("order-topic", "VIP", message);}</p>
     *
     * @param topic   Topic 名称
     * @param tag     消息标签
     * @param message 消息对象
     * @return 发送结果
     */
    public static SendResult sendWithTag(String topic, String tag, Object message) {
        return sendWithTag(topic, tag, message, properties.getProducer().getSendMsgTimeout());
    }

    /**
     * 发送带标签的消息（自定义超时）
     * <p>示例：{@code RMProducerUtil.sendWithTag("order-topic", "VIP", message, 5000);}</p>
     *
     * @param topic   Topic 名称
     * @param tag     消息标签
     * @param message 消息对象
     * @param timeout 超时时间（毫秒）
     * @return 发送结果
     */
    public static SendResult sendWithTag(String topic, String tag, Object message, int timeout) {
        checkTemplateAvailable();

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        String destination = topic + ":" + tag;
        if (properties.getProducer().getEnableLog()) {
            log.info("📤 发送带标签消息: destination={}, tag={}", destination, tag);
        }

        return doSyncSend(
            () -> rocketMQTemplate.syncSend(destination, message, timeout),
            destination,
            "带标签消息发送 (tag=" + tag + ")"
        );
    }

    // ==================== 常用功能：批量发送 ====================

    /**
     * 批量发送消息（单向发送）
     * <p>
     * 批量发送使用单向模式，性能最高
     * </p>
     * <p>示例：{@code RMProducerUtil.sendBatch("order-topic", messageList);}</p>
     *
     * @param topic    Topic 名称
     * @param messages 消息集合
     */
    public static void sendBatch(String topic, Collection<?> messages) {
        checkTemplateAvailable();

        if (messages == null || messages.isEmpty()) {
            log.warn("⚠️ 批量发送消息为空，跳过发送");
            return;
        }

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        if (properties.getProducer().getEnableLog()) {
            log.info("📤 批量发送消息: topic={}, count={}", topic, messages.size());
        }

        // 分批发送（避免单次发送过多消息）
        int batchSize = properties.getProducer().getBatchSize();
        int count = 0;

        for (Object message : messages) {
            rocketMQTemplate.sendOneWay(topic, message);
            count++;

            if (count % batchSize == 0 && properties.getProducer().getEnableLog()) {
                log.info("   已发送 {} / {} 条消息", count, messages.size());
            }
        }

        if (properties.getProducer().getEnableLog()) {
            log.info("✅ 批量发送完成: 共发送 {} 条消息", messages.size());
        }
    }

    // ==================== 高级功能：事务消息 ====================

    /**
     * 发送事务消息（自动生成事务ID）
     * <p>
     * 事务消息需要配合 @RocketMQTransactionListener 使用
     * </p>
     * <p>示例：{@code RMProducerUtil.sendTransaction("order-topic", message);}</p>
     *
     * @param topic   Topic 名称
     * @param message 消息对象
     */
    public static void sendTransaction(String topic, Object message) {
        String transactionId = String.valueOf(System.currentTimeMillis());
        sendTransaction(topic, message, transactionId, null);
    }

    /**
     * 发送事务消息（自定义事务ID）
     * <p>示例：{@code RMProducerUtil.sendTransaction("order-topic", message, "tx-12345");}</p>
     *
     * @param topic         Topic 名称
     * @param message       消息对象
     * @param transactionId 事务ID（用于回查）
     */
    public static void sendTransaction(String topic, Object message, String transactionId) {
        sendTransaction(topic, message, transactionId, null);
    }

    /**
     * 发送事务消息（完整参数）
     * <p>示例：{@code RMProducerUtil.sendTransaction("order-topic", message, "tx-12345", extraArgs);}</p>
     *
     * @param topic         Topic 名称
     * @param message       消息对象
     * @param transactionId 事务ID（用于回查）
     * @param arg           传递给监听器的参数
     */
    public static void sendTransaction(String topic, Object message, String transactionId, Object arg) {
        checkTemplateAvailable();

        // 自动创建 Topic（如果启用）
        if (properties.getProducer().getAutoCreateTopic()) {
            autoCreateTopic(topic);
        }

        if (properties.getProducer().getEnableLog()) {
            log.info("📤 发送事务消息: topic={}, transactionId={}", topic, transactionId);
        }

        Message<?> msg = MessageBuilder.withPayload(message)
            .setHeader("transactionId", transactionId)
            .build();

        rocketMQTemplate.sendMessageInTransaction(topic, msg, arg);

        if (properties.getProducer().getEnableLog()) {
            log.info("✅ 事务消息已提交: transactionId={}", transactionId);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查 RocketMQTemplate 是否可用
     */
    private static void checkTemplateAvailable() {
        if (rocketMQTemplate == null) {
            throw new IllegalStateException(
                "RocketMQTemplate 未初始化，请检查：\n" +
                    "1. rocketmq.enabled 是否设置为 true\n" +
                    "2. rocketmq-spring-boot-starter 依赖是否已引入\n" +
                    "3. Spring 容器是否已启动"
            );
        }
    }

    /**
     * 自动创建 Topic（如果不存在）
     */
    private static void autoCreateTopic(String topic) {
        try {
            // 解析 topic:tag 格式，只创建 topic 部分
            String topicName = topic.contains(":") ? topic.split(":")[0] : topic;
            RMTopicUtil.createTopic(topicName);
        } catch (Exception e) {
            log.warn("⚠️ 自动创建Topic失败: {}, 错误: {}", topic, e.getMessage());
        }
    }

    /**
     * 执行同步发送（统一异常处理）
     */
    private static SendResult doSyncSend(SendResultSupplier sender, String topic, String operation) {
        try {
            SendResult result = sender.get();

            if (properties.getProducer().getEnableLog()) {
                log.info("✅ {} 成功: topic={}, msgId={}, queueId={}",
                    operation, topic, result.getMsgId(), result.getMessageQueue().getQueueId());
            }

            return result;

        } catch (Exception e) {
            log.error("❌ {} 失败: topic={}", operation, topic, e);
            throw new RuntimeException(operation + "失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送结果提供者（函数式接口）
     */
    @FunctionalInterface
    private interface SendResultSupplier {
        SendResult get() throws Exception;
    }
}
