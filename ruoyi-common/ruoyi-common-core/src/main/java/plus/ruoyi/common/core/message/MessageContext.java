package plus.ruoyi.common.core.message;

import cn.hutool.core.lang.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息上下文 - 轻量级DTO
 * <p>
 * 封装消息发送所需的所有信息，作为各通道间传递的统一数据载体
 * 设计为轻量级对象，不包含任何业务逻辑
 * </p>
 * <p>
 * 使用场景：
 * 1. 简单文本消息：只需设置 userIds 和 content
 * 2. 短信消息：需设置 params.phone 和 content
 * 3. 小程序/公众号：需设置 params 中的 appid、openid、templateId 等
 * </p>
 *
 * @author YourName
 */
@Data
@Accessors(chain = true)
public class MessageContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID (唯一标识)
     * <p>
     * 用于消息追踪和去重，自动生成UUID
     * </p>
     */
    private String messageId;

    /**
     * 租户ID
     * <p>
     * 多租户场景下标识消息所属租户
     * 某些通道(如WebSocket/SSE)需要租户隔离
     * </p>
     */
    private String tenantId;

    /**
     * 目标用户ID列表
     * <p>
     * 消息接收方的用户ID列表
     * 单个用户也用List包装，保持接口一致性
     * </p>
     */
    private List<Long> userIds;

    /**
     * 消息内容 (纯文本或JSON)
     * <p>
     * 对于简单文本消息，直接填写消息内容
     * 对于结构化消息(如小程序/公众号)，可为空，使用 params 传递数据
     * </p>
     */
    private String content;

    /**
     * 扩展参数 (各通道特有参数)
     * <p>
     * 不同通道需要不同的参数：
     * <ul>
     * <li>WebSocket: 无需额外参数</li>
     * <li>SSE: 无需额外参数</li>
     * <li>SMS: phone(手机号)</li>
     * <li>Miniapp: appid, openid, templateId, data(Map), page(可选)</li>
     * <li>MP: appid, openid, templateId, data(Map), url(可选)</li>
     * </ul>
     * </p>
     */
    private Map<String, Object> params;

    /**
     * 消息类型 (可选)
     * <p>
     * 用于业务分类和统计，如：order(订单)、verify_code(验证码)、promotion(营销)等
     * 统一调度服务可根据此字段选择最佳通道
     * </p>
     */
    private String messageType;

    /**
     * 创建时间
     * <p>
     * 消息创建的时间戳，用于追踪和统计
     * </p>
     */
    private LocalDateTime createTime;

    /**
     * 过期时间 (可选)
     * <p>
     * 某些消息有时效性(如验证码)，超过此时间不再发送
     * </p>
     */
    private LocalDateTime expireTime;

    /**
     * 优先级 (可选)
     * <p>
     * 消息优先级，0-10，数字越大优先级越高
     * 用于消息队列场景，优先处理高优先级消息
     * 默认值: 5
     * </p>
     */
    private Integer priority = 5;

    /**
     * 是否需要持久化 (可选)
     * <p>
     * 是否将消息发送记录保存到数据库
     * 用于消息追溯和统计分析
     * 默认值: false
     * </p>
     */
    private Boolean persistent = false;

    /**
     * 重试次数 (可选)
     * <p>
     * 当前已重试次数，用于失败重试机制
     * 初始值: 0
     * </p>
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数 (可选)
     * <p>
     * 允许的最大重试次数
     * 默认值: 0 (不重试)
     * </p>
     */
    private Integer maxRetry = 0;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建简单消息上下文 (多个用户)
     * <p>
     * 适用于简单文本消息，如：WebSocket推送、SSE推送
     * </p>
     *
     * @param userIds 目标用户ID列表
     * @param content 消息内容
     * @return 消息上下文对象
     */
    public static MessageContext of(List<Long> userIds, String content) {
        return new MessageContext()
            .setMessageId(UUID.fastUUID().toString(true))
            .setUserIds(userIds)
            .setContent(content)
            .setCreateTime(LocalDateTime.now());
    }

    /**
     * 创建简单消息上下文 (单个用户)
     * <p>
     * 适用于单用户简单文本消息
     * </p>
     *
     * @param userId  目标用户ID
     * @param content 消息内容
     * @return 消息上下文对象
     */
    public static MessageContext of(Long userId, String content) {
        return of(List.of(userId), content);
    }

    /**
     * 创建带扩展参数的消息上下文
     * <p>
     * 适用于需要额外参数的通道，如：短信、小程序、公众号
     * </p>
     *
     * @param userIds 目标用户ID列表
     * @param content 消息内容
     * @param params  扩展参数
     * @return 消息上下文对象
     */
    public static MessageContext of(List<Long> userIds, String content, Map<String, Object> params) {
        return new MessageContext()
            .setMessageId(UUID.fastUUID().toString(true))
            .setUserIds(userIds)
            .setContent(content)
            .setParams(params)
            .setCreateTime(LocalDateTime.now());
    }

    /**
     * 创建仅带参数的消息上下文 (无content)
     * <p>
     * 适用于结构化消息(如小程序/公众号模板消息)，内容通过 params 传递
     * </p>
     *
     * @param userIds 目标用户ID列表
     * @param params  扩展参数
     * @return 消息上下文对象
     */
    public static MessageContext ofParams(List<Long> userIds, Map<String, Object> params) {
        return new MessageContext()
            .setMessageId(UUID.fastUUID().toString(true))
            .setUserIds(userIds)
            .setParams(params)
            .setCreateTime(LocalDateTime.now());
    }

    /**
     * 判断消息是否已过期
     *
     * @return true-已过期，false-未过期
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 判断是否可以重试
     *
     * @return true-可以重试，false-不可重试
     */
    public boolean canRetry() {
        return retryCount < maxRetry;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount++;
    }
}
