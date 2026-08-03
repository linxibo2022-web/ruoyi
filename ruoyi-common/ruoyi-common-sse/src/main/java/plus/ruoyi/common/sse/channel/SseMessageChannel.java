package plus.ruoyi.common.sse.channel;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.sse.dto.SseMessageDto;
import plus.ruoyi.common.sse.utils.SseMessageUtils;

/**
 * SSE (Server-Sent Events) 消息通道实现
 * <p>
 * 基于框架现有的 SseMessageUtils 实现统一消息接口
 * SSE 是 HTML5 标准的服务端推送技术，基于 HTTP 长连接
 * </p>
 * <p>
 * 特点：
 * - 单向推送(服务端 → 客户端)
 * - 基于 HTTP 协议，无需额外端口
 * - 自动断线重连
 * - 轻量级，相比 WebSocket 更简单
 * </p>
 * <p>
 * 适用场景：
 * - 服务端主动推送数据
 * - 实时进度条
 * - 日志流式输出
 * - AI 流式对话
 * - 不需要客户端向服务端发送消息的场景
 * </p>
 * <p>
 * 与 WebSocket 对比：
 * - SSE: 单向推送，更轻量，自动重连，基于HTTP
 * - WebSocket: 双向通信，更强大，需要专门处理重连
 * </p>
 * <p>
 * 使用要求：
 * - userIds 必须包含至少一个用户ID（支持多用户）
 * - content 为消息内容
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 发送给单个用户
 * MessageContext context = MessageContext.of(userId, "实时消息内容");
 *
 * // 发送给多个用户
 * MessageContext context = MessageContext.of(Arrays.asList(userId1, userId2), "广播消息");
 * </pre>
 *
 * @author YourName
 */
@Slf4j
public class SseMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "sse";
    }

    @Override
    public String getChannelName() {
        return "SSE服务端推送";
    }

    @Override
    public MessageResult send(MessageContext context) {
        long startTime = System.currentTimeMillis();

        // 参数校验
        if (context == null || context.getUserIds() == null || context.getUserIds().isEmpty()) {
            return MessageResult.fail(
                context != null ? context.getMessageId() : null,
                getChannelType(),
                null,
                "PARAM_ERROR",
                "目标用户列表不能为空"
            );
        }

        if (StringUtils.isBlank(context.getContent())) {
            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds().get(0),
                "PARAM_ERROR",
                "消息内容不能为空"
            );
        }

        // 构建 SSE 消息对象
        SseMessageDto dto = SseMessageDto.of(
            context.getUserIds(),
            context.getContent()
        );

        // 调用框架现有的 SseMessageUtils 发送消息
        // SseMessageUtils.publishMessage() 内部已处理异常，无需外层 try-catch
        SseMessageUtils.publishMessage(dto);

        // 构建成功结果
        MessageResult result = MessageResult.success(
            context.getMessageId(),
            getChannelType(),
            context.getUserIds().get(0)
        );
        result.setCostTime(System.currentTimeMillis() - startTime);

        log.info("SSE 消息发送成功: messageId={}, userIds={}, costTime={}ms",
            context.getMessageId(), context.getUserIds(), result.getCostTime());

        return result;
    }

    @Override
    public boolean isEnabled() {
        // 从 SseMessageUtils 读取启用状态
        return SseMessageUtils.isEnable();
    }

    @Override
    public int getPriority() {
        // SSE 优先级略高于短信，适合实时推送
        return 2;
    }

    @Override
    public boolean healthCheck() {
        // 检查 SSE 是否启用
        return isEnabled();
    }

    @Override
    public boolean supportTenant(String tenantId) {
        // SSE 支持所有租户
        return true;
    }
}
