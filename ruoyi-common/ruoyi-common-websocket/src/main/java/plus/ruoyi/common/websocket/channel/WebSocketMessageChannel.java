package plus.ruoyi.common.websocket.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import plus.ruoyi.common.core.message.MessageChannel;
import plus.ruoyi.common.core.message.MessageContext;
import plus.ruoyi.common.core.message.MessageResult;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.tenant.helper.TenantHelper;
import plus.ruoyi.common.websocket.dto.WebSocketMessageDto;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;

/**
 * WebSocket 消息通道实现
 * <p>
 * 基于框架现有的 WebSocketUtils 实现统一消息接口
 * 支持多租户、集群分发、实时双向通信
 * </p>
 * <p>
 * 特点：
 * - 实时性最高，延迟最低
 * - 支持多设备/多标签页同时接收
 * - 自动进行租户隔离
 * - 通过Redis实现跨实例消息分发
 * </p>
 * <p>
 * 适用场景：
 * - 实时通知推送
 * - 在线聊天
 * - 日志实时监控
 * - 数据实时更新
 * </p>
 *
 * @author YourName
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketMessageChannel implements MessageChannel {

    @Override
    public String getChannelType() {
        return "websocket";
    }

    @Override
    public String getChannelName() {
        return "WebSocket实时推送";
    }

    @Override
    public MessageResult send(MessageContext context) {
        long startTime = System.currentTimeMillis();

        try {
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

            // 构建 WebSocket 消息对象
            WebSocketMessageDto dto = WebSocketMessageDto.of(
                context.getUserIds(),
                context.getContent()
            );

            // 设置租户ID (优先使用context中的，否则使用当前租户)
            if (StringUtils.isNotBlank(context.getTenantId())) {
                dto.setTenantId(context.getTenantId());
            } else {
                dto.setTenantId(TenantHelper.getTenantId());
            }

            // 调用框架现有的 WebSocketUtils 发送消息
            WebSocketUtils.publishMessage(dto);

            // 构建成功结果
            MessageResult result = MessageResult.success(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds().get(0)
            );
            result.setCostTime(System.currentTimeMillis() - startTime);

            log.debug("WebSocket 消息发送成功: messageId={}, tenantId={}, userIds={}, costTime={}ms",
                context.getMessageId(), dto.getTenantId(), context.getUserIds(), result.getCostTime());

            return result;

        } catch (Exception e) {
            log.error("WebSocket 消息发送失败: messageId={}, userIds={}",
                context.getMessageId(), context.getUserIds(), e);

            return MessageResult.fail(
                context.getMessageId(),
                getChannelType(),
                context.getUserIds() != null && !context.getUserIds().isEmpty()
                    ? context.getUserIds().get(0) : null,
                "SEND_ERROR",
                "WebSocket消息发送异常: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean isEnabled() {
        // 从配置文件读取 WebSocket 启用状态
        return SpringUtils.getProperty("websocket.enabled", Boolean.class, true);
    }

    @Override
    public int getPriority() {
        // WebSocket 实时性最高，优先级设为最高
        return 1;
    }

    @Override
    public boolean healthCheck() {
        // 检查 WebSocket 是否启用
        if (!isEnabled()) {
            return false;
        }

        try {
            // 可以添加更复杂的健康检查逻辑
            // 例如：检查连接池状态、Redis连接等
            return true;
        } catch (Exception e) {
            log.warn("WebSocket 健康检查失败", e);
            return false;
        }
    }

    @Override
    public boolean supportTenant(String tenantId) {
        // WebSocket 支持所有租户
        return true;
    }
}
