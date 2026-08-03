package plus.ruoyi.common.langchain4j.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import plus.ruoyi.common.langchain4j.enums.ChatMode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 聊天请求DTO
 */
@Data
@Accessors(chain = true)
public class ChatRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 对话模式
     */
    private ChatMode mode = ChatMode.CONTINUOUS;

    /**
     * 模型提供商
     */
    private String provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 是否流式返回
     */
    private Boolean stream = true;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 本次对话是否启用深度思考；null 表示沿用后端配置
     * <p>仅能"收窄"：后端 enableThinking=false 时该值为 true 也不生效
     */
    private Boolean thinkingEnabled;

    /**
     * 思考预算 Token 数；null 表示沿用后端配置（仅 Claude / 通义千问等生效）
     */
    private Integer thinkingBudgetTokens;

    /**
     * 知识库ID列表(RAG模式使用)
     */
    private List<Long> knowledgeBaseIds;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;
}
