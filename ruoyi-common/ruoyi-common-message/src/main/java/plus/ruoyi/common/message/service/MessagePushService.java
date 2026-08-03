package plus.ruoyi.common.message.service;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一消息推送服务 (可选模块)
 * <p>
 * 提供消息路由、智能降级、批量发送等高级功能
 * 业务模块可选择性依赖此模块
 * </p>
 * <p>
 * 核心功能：
 * 1. 消息路由 - 根据通道类型发送消息
 * 2. 智能降级 - 按优先级尝试多个通道，确保消息送达
 * 3. 广播推送 - 同时向多个通道发送消息
 * 4. 自动选择 - 根据通道优先级自动选择最佳通道
 * 5. 通道管理 - 获取所有可用通道信息
 * </p>
 * <p>
 * 使用场景：
 * - 需要智能降级的场景(如：验证码发送，短信失败自动切换邮件)
 * - 需要广播的场景(如：重要通知同时推送多个通道)
 * - 需要自动选择最佳通道的场景
 * </p>
 * <p>
 * 注意事项：
 * - 本服务通过 MessageAutoConfiguration 自动装配
 * - Spring 会自动注入所有 MessageChannel 实现
 * - 不需要显式配置通道列表
 * - 新增通道只需实现 MessageChannel 接口即可被自动发现
 * </p>
 *
 * @author YourName
 */
@Slf4j
public class MessagePushService {

    /**
     * Spring 自动注入所有 MessageChannel 实现
     * 当某个模块(websocket/sms/miniapp/mp)被引入时，其对应的 Channel 会自动注册到 Spring 容器
     * 本服务无需显式依赖具体通道，通过依赖注入实现动态发现
     */
    private final List<MessageChannel> channels;

    /**
     * 构造函数
     * <p>
     * 由 MessageAutoConfiguration 调用，注入所有 MessageChannel 实现
     * </p>
     *
     * @param channels 所有消息通道实现
     */
    public MessagePushService(List<MessageChannel> channels) {
        this.channels = channels;
    }

    /**
     * 发送消息到指定通道
     * <p>
     * 最基础的消息发送方法，直接调用指定通道发送消息
     * </p>
     *
     * @param channelType 通道类型 (websocket/sse/sms/miniapp/mp)
     * @param context     消息上下文
     * @return 发送结果
     */
    public MessageResult send(String channelType, MessageContext context) {
        // 获取通道
        MessageChannel channel = getChannel(channelType);

        if (channel == null) {
            log.warn("消息通道不存在: channelType={}, 可用通道: {}",
                channelType, getAvailableChannelTypes());
            return MessageResult.fail(
                context.getMessageId(),
                channelType,
                null,
                "CHANNEL_NOT_FOUND",
                "通道不存在: " + channelType
            );
        }

        if (!channel.isEnabled()) {
            log.warn("消息通道未启用: channelType={}", channelType);
            return MessageResult.fail(
                context.getMessageId(),
                channelType,
                null,
                "CHANNEL_DISABLED",
                "通道未启用: " + channelType
            );
        }

        log.debug("准备发送消息: channelType={}, messageId={}, userIds={}",
            channelType, context.getMessageId(), context.getUserIds());

        return channel.send(context);
    }

    /**
     * 智能降级发送 (按优先级尝试多个通道)
     * <p>
     * 按顺序尝试多个通道，直到某个通道发送成功
     * 常用于需要确保消息送达的场景
     * </p>
     * <p>
     * 使用示例：
     * <pre>
     * // 验证码发送：短信失败自动切换邮件
     * sendWithFallback(List.of("sms", "email"), context);
     *
     * // 实时通知：WebSocket失败自动切换SSE
     * sendWithFallback(List.of("websocket", "sse"), context);
     * </pre>
     *
     * @param channelTypes 通道类型列表 (按优先级排序)
     * @param context      消息上下文
     * @return 第一个成功的发送结果，或最后一个失败结果
     */
    public MessageResult sendWithFallback(List<String> channelTypes, MessageContext context) {
        if (channelTypes == null || channelTypes.isEmpty()) {
            log.warn("降级通道列表为空");
            return MessageResult.fail(
                context.getMessageId(),
                "fallback",
                null,
                "PARAM_ERROR",
                "降级通道列表不能为空"
            );
        }

        log.info("开始智能降级发送: messageId={}, channelTypes={}",
            context.getMessageId(), channelTypes);

        MessageResult lastResult = null;

        for (String channelType : channelTypes) {
            MessageResult result = send(channelType, context);

            if (result.isSuccess()) {
                log.info("消息发送成功: channelType={}, messageId={}",
                    channelType, context.getMessageId());
                return result;
            }

            log.warn("通道 {} 发送失败，尝试下一个通道: errorMessage={}",
                channelType, result.getErrorMessage());

            lastResult = result;
        }

        log.error("所有通道均发送失败: messageId={}, channelTypes={}",
            context.getMessageId(), channelTypes);

        return lastResult != null ? lastResult : MessageResult.fail(
            context.getMessageId(),
            "all",
            null,
            "ALL_CHANNEL_FAILED",
            "所有通道均发送失败"
        );
    }

    /**
     * 广播消息到多个通道 (全部发送，不降级)
     * <p>
     * 同时向多个通道发送相同消息，不管成功失败都会发送所有通道
     * 常用于重要通知需要多渠道送达的场景
     * </p>
     * <p>
     * 使用示例：
     * <pre>
     * // 重要通知同时推送多个通道
     * broadcast(List.of("websocket", "sms", "miniapp"), context);
     * </pre>
     *
     * @param channelTypes 通道类型列表
     * @param context      消息上下文
     * @return 所有通道的发送结果列表
     */
    public List<MessageResult> broadcast(List<String> channelTypes, MessageContext context) {
        if (channelTypes == null || channelTypes.isEmpty()) {
            log.warn("广播通道列表为空");
            return List.of(MessageResult.fail(
                context.getMessageId(),
                "broadcast",
                null,
                "PARAM_ERROR",
                "广播通道列表不能为空"
            ));
        }

        log.info("开始广播消息: messageId={}, channelTypes={}",
            context.getMessageId(), channelTypes);

        List<MessageResult> results = channelTypes.stream()
            .map(type -> send(type, context))
            .collect(Collectors.toList());

        long successCount = results.stream().filter(MessageResult::isSuccess).count();
        long failCount = results.size() - successCount;

        log.info("广播消息完成: messageId={}, 成功: {}, 失败: {}",
            context.getMessageId(), successCount, failCount);

        return results;
    }

    /**
     * 自动选择最佳通道发送 (根据优先级)
     * <p>
     * 自动选择优先级最高且可用的通道发送消息
     * 适用于不关心具体通道，只需要消息能够送达的场景
     * </p>
     *
     * @param context 消息上下文
     * @return 发送结果
     */
    public MessageResult sendAuto(MessageContext context) {
        // 获取所有启用的通道，按优先级排序
        List<MessageChannel> enabledChannels = channels.stream()
            .filter(MessageChannel::isEnabled)
            .sorted(Comparator.comparingInt(MessageChannel::getPriority))
            .toList();

        if (enabledChannels.isEmpty()) {
            log.warn("没有可用的消息通道");
            return MessageResult.fail(
                context.getMessageId(),
                "auto",
                null,
                "NO_AVAILABLE_CHANNEL",
                "没有可用的消息通道"
            );
        }

        // 选择优先级最高的通道
        MessageChannel bestChannel = enabledChannels.get(0);

        log.info("自动选择通道: channelType={}, priority={}, messageId={}",
            bestChannel.getChannelType(), bestChannel.getPriority(), context.getMessageId());

        return bestChannel.send(context);
    }

    /**
     * 根据消息类型自动选择通道并降级发送
     * <p>
     * 根据消息类型预设的通道策略进行发送
     * 可扩展支持更多消息类型和通道策略
     * </p>
     *
     * @param context 消息上下文 (必须设置 messageType)
     * @return 发送结果
     */
    public MessageResult sendByMessageType(MessageContext context) {
        if (context.getMessageType() == null) {
            log.warn("消息类型未设置，使用自动选择");
            return sendAuto(context);
        }

        List<String> channelTypes = selectChannelsByMessageType(context.getMessageType());

        log.info("根据消息类型选择通道: messageType={}, channelTypes={}",
            context.getMessageType(), channelTypes);

        return sendWithFallback(channelTypes, context);
    }

    /**
     * 根据消息类型选择通道策略
     * <p>
     * 可根据实际业务需求扩展此方法
     * </p>
     *
     * @param messageType 消息类型
     * @return 通道类型列表 (按优先级排序)
     */
    private List<String> selectChannelsByMessageType(String messageType) {
        return switch (messageType) {
            case "verify_code" -> List.of("sms", "email"); // 验证码优先短信
            case "order" -> List.of("websocket", "miniapp", "mp"); // 订单通知优先实时推送
            case "promotion" -> List.of("miniapp", "mp", "sms"); // 营销消息优先小程序/公众号
            case "system_notice" -> List.of("websocket", "sse"); // 系统通知优先实时
            case "important" -> List.of("sms", "websocket", "miniapp", "mp"); // 重要通知全部尝试
            default -> List.of("websocket"); // 默认 WebSocket
        };
    }

    /**
     * 获取所有可用通道
     * <p>
     * 返回当前已启用且健康的通道列表
     * </p>
     *
     * @return 可用通道列表 (按优先级排序)
     */
    public List<MessageChannel> getAvailableChannels() {
        return channels.stream()
            .filter(MessageChannel::isEnabled)
            .filter(MessageChannel::healthCheck)
            .sorted(Comparator.comparingInt(MessageChannel::getPriority))
            .collect(Collectors.toList());
    }

    /**
     * 获取所有可用通道类型
     *
     * @return 通道类型列表
     */
    public List<String> getAvailableChannelTypes() {
        return getAvailableChannels().stream()
            .map(MessageChannel::getChannelType)
            .collect(Collectors.toList());
    }

    /**
     * 获取指定通道
     *
     * @param channelType 通道类型
     * @return 通道实例，不存在返回 null
     */
    private MessageChannel getChannel(String channelType) {
        return channels.stream()
            .filter(ch -> ch.getChannelType().equals(channelType))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取通道信息
     *
     * @param channelType 通道类型
     * @return 通道信息描述
     */
    public String getChannelInfo(String channelType) {
        MessageChannel channel = getChannel(channelType);
        if (channel == null) {
            return "通道不存在";
        }

        return String.format("%s (类型:%s, 优先级:%d, 启用:%s, 健康:%s)",
            channel.getChannelName(),
            channel.getChannelType(),
            channel.getPriority(),
            channel.isEnabled() ? "是" : "否",
            channel.healthCheck() ? "是" : "否"
        );
    }
}
