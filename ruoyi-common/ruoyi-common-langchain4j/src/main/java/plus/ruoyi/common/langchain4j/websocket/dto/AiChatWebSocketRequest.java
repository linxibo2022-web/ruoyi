package plus.ruoyi.common.langchain4j.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.websocket.dto.WebSocketRequest;

import java.io.Serial;

/**
 * AI聊天WebSocket请求DTO
 * <p>
 * 用于接收客户端发送的AI聊天请求参数
 *
 * @author zendwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatWebSocketRequest extends WebSocketRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 会话ID(可选,不传则自动生成)
     */
    private String sessionId;

    /**
     * 模型提供商(可选,不传则使用默认值)
     */
    private String provider;

    /**
     * 模型名称(可选,不传则使用默认值)
     */
    private String modelName;

    /**
     * 系统提示词(可选)
     */
    private String systemPrompt;

    /**
     * 对话模式(可选,默认为CONTINUOUS)
     */
    private String mode;

    /**
     * 温度参数(可选,范围0-1)
     */
    private Double temperature;

    /**
     * 最大Token数(可选)
     */
    private Integer maxTokens;

    /**
     * 本次对话是否启用深度思考(可选)
     * <p>null 表示沿用后端配置；为防止把后端未支持的模型强行开启思考，
     * 该值只能"收窄"——即后端 enableThinking=false 时本字段传 true 也不会生效，
     * 后端 enableThinking=true 时本字段传 false 可临时关闭
     */
    private Boolean thinkingEnabled;

    /**
     * 思考预算 Token 数(可选,仅 Claude / 通义千问 等支持预算的提供商生效)
     * <p>null 表示沿用后端配置；用于"标准 / 深度"等档位切换
     */
    private Integer thinkingBudgetTokens;
}
