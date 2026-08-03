package plus.ruoyi.common.langchain4j.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * AI 聊天响应 DTO
 * <p>
 * 命名上避免与 langchain4j 1.x 的 dev.langchain4j.model.chat.response.ChatResponse 冲突，
 * 1.x 升级时如继续叫 ChatResponse 必须在所有用到模型层 ChatResponse 的地方写全限定类名，违反项目导入规范。
 *
 * @author 抓蛙师
 */
@Builder
@Data
@Accessors(chain = true)
public class AiChatResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * AI 回复正文（answer 阶段累积内容；流式时仅当本次增量来自 answer 阶段才有值）
     */
    private String content;

    /**
     * 深度思考内容（reasoning / chain-of-thought）
     * <p>
     * 完成态：累积全文；流式：单次增量。仅在模型支持 thinking 且配置 returnThinking=true 时返回。
     * 适用模型：DeepSeek-Reasoner、Claude Extended Thinking、Qwen3-Thinking、Ollama think、OpenAI o1/o3 等
     */
    private String reasoningContent;

    /**
     * 当前流式增量所属阶段
     * <p>
     * - thinking: 思考过程增量，前端建议折叠展示
     * - content: 正文增量，前端正常展示
     * - null: 非流式或完成消息
     */
    private String phase;

    /**
     * 是否完成
     */
    @Builder.Default
    private Boolean finished = true;

    /**
     * Token 使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 响应时间(毫秒)
     */
    private Long responseTime;

    /**
     * 引用的文档(RAG 模式)
     */
    private List<DocumentReference> references;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 流式增量阶段枚举值
     */
    public static final class Phase {
        public static final String THINKING = "thinking";
        public static final String CONTENT = "content";

        private Phase() {
        }
    }
}
