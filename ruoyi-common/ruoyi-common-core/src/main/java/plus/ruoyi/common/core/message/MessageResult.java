package plus.ruoyi.common.core.message;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息发送结果
 * <p>
 * 封装消息发送的结果信息，用于追踪和统计
 * </p>
 *
 * @author YourName
 */
@Data
public class MessageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否发送成功
     */
    private Boolean success;

    /**
     * 消息ID (关联MessageContext.messageId)
     */
    private String messageId;

    /**
     * 通道类型 (websocket/sms/miniapp等)
     */
    private String channelType;

    /**
     * 目标用户ID
     * <p>
     * 批量发送时，每个用户对应一个结果对象
     * </p>
     */
    private Long userId;

    /**
     * 错误信息 (失败时填写)
     */
    private String errorMessage;

    /**
     * 错误码 (可选)
     * <p>
     * 用于分类错误类型，如：参数错误、网络错误、第三方API错误等
     * </p>
     */
    private String errorCode;

    /**
     * 第三方返回的消息ID (可选)
     * <p>
     * 某些通道(如短信、公众号)会返回第三方消息ID
     * 用于在第三方平台追踪消息状态
     * </p>
     */
    private String thirdPartyMsgId;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 耗时(毫秒)
     * <p>
     * 消息发送的实际耗时，用于性能监控和优化
     * </p>
     */
    private Long costTime;

    /**
     * 扩展信息 (可选)
     * <p>
     * 用于存储通道特有的响应信息
     * </p>
     */
    private String extra;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建成功结果
     *
     * @param messageId   消息ID
     * @param channelType 通道类型
     * @param userId      目标用户ID
     * @return 成功结果对象
     */
    public static MessageResult success(String messageId, String channelType, Long userId) {
        MessageResult result = new MessageResult();
        result.setSuccess(true);
        result.setMessageId(messageId);
        result.setChannelType(channelType);
        result.setUserId(userId);
        result.setSendTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param messageId    消息ID
     * @param channelType  通道类型
     * @param userId       目标用户ID
     * @param errorMessage 错误信息
     * @return 失败结果对象
     */
    public static MessageResult fail(String messageId, String channelType,
                                     Long userId, String errorMessage) {
        MessageResult result = new MessageResult();
        result.setSuccess(false);
        result.setMessageId(messageId);
        result.setChannelType(channelType);
        result.setUserId(userId);
        result.setErrorMessage(errorMessage);
        result.setSendTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建失败结果 (带错误码)
     *
     * @param messageId    消息ID
     * @param channelType  通道类型
     * @param userId       目标用户ID
     * @param errorCode    错误码
     * @param errorMessage 错误信息
     * @return 失败结果对象
     */
    public static MessageResult fail(String messageId, String channelType,
                                     Long userId, String errorCode, String errorMessage) {
        MessageResult result = fail(messageId, channelType, userId, errorMessage);
        result.setErrorCode(errorCode);
        return result;
    }

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * 判断是否失败
     *
     * @return true-失败，false-成功
     */
    public boolean isFail() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "MessageResult{" +
            "success=" + success +
            ", messageId='" + messageId + '\'' +
            ", channelType='" + channelType + '\'' +
            ", userId=" + userId +
            ", errorMessage='" + errorMessage + '\'' +
            ", thirdPartyMsgId='" + thirdPartyMsgId + '\'' +
            ", costTime=" + costTime + "ms" +
            '}';
    }
}
