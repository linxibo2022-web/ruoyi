package plus.ruoyi.common.core.message;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通道接口 (桥接模式-实现化角色)
 * <p>
 * 所有消息推送通道的统一抽象接口
 * 各推送模块(websocket/sms/miniapp等)实现此接口
 * </p>
 * <p>
 * 设计原则：
 * 1. 接口定义在 core 模块，不依赖任何具体实现
 * 2. 各推送模块独立实现此接口，避免依赖臃肿
 * 3. 通过 Spring 自动发现所有实现类
 * 4. 支持按优先级排序，实现智能降级
 * </p>
 *
 * @author YourName
 */
public interface MessageChannel {

    /**
     * 通道类型标识
     * <p>
     * 用于唯一标识消息通道类型，建议使用小写字母和下划线
     * </p>
     *
     * @return websocket/sse/sms/miniapp/mp/email/app_push 等
     */
    String getChannelType();

    /**
     * 通道名称 (用于日志和展示)
     * <p>
     * 用户友好的通道名称，用于日志输出和管理界面展示
     * </p>
     *
     * @return 通道中文名称，如：WebSocket实时推送、短信推送等
     */
    String getChannelName();

    /**
     * 发送消息
     * <p>
     * 向指定用户发送消息的核心方法
     * 实现类应处理具体的消息发送逻辑和异常情况
     * </p>
     *
     * @param context 消息上下文，包含目标用户、消息内容、扩展参数等
     * @return 发送结果，包含成功状态、错误信息、耗时等
     */
    MessageResult send(MessageContext context);

    /**
     * 批量发送消息 (默认实现)
     * <p>
     * 默认实现为串行发送，子类可重写此方法实现并行发送以提升性能
     * </p>
     *
     * @param contexts 消息上下文列表
     * @return 发送结果列表
     */
    default List<MessageResult> batchSend(List<MessageContext> contexts) {
        return contexts.stream()
            .map(this::send)
            .collect(Collectors.toList());
    }

    /**
     * 通道是否启用
     * <p>
     * 用于动态控制通道的启用状态，通常从配置文件读取
     * 未启用的通道不会被统一调度服务使用
     * </p>
     *
     * @return true-已启用，false-未启用
     */
    boolean isEnabled();

    /**
     * 通道优先级 (用于降级策略)
     * <p>
     * 数字越小优先级越高，用于智能降级和自动选择最佳通道
     * 建议值：
     * - 1: 实时推送(WebSocket/SSE)
     * - 3: 短信
     * - 5: 第三方推送(微信/钉钉/邮件)
     * - 10: 系统通知(站内信)
     * </p>
     *
     * @return 优先级数字，默认值5
     */
    default int getPriority() {
        return 5;
    }

    /**
     * 健康检查 (可选实现)
     * <p>
     * 用于检测通道是否可用，默认仅检查是否启用
     * 子类可重写此方法，添加更复杂的健康检查逻辑
     * 例如：检查第三方API连接状态、配置完整性等
     * </p>
     *
     * @return true-健康，false-不健康
     */
    default boolean healthCheck() {
        return isEnabled();
    }

    /**
     * 是否支持指定租户 (可选实现)
     * <p>
     * 用于多租户场景，某些通道可能仅对特定租户开放
     * 默认返回true表示支持所有租户
     * </p>
     *
     * @param tenantId 租户ID
     * @return true-支持，false-不支持
     */
    default boolean supportTenant(String tenantId) {
        return true;
    }
}
