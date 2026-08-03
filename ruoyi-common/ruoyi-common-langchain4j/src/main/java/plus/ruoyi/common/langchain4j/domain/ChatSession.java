package plus.ruoyi.common.langchain4j.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话实体
 */
@Data
@Accessors(chain = true)
public class ChatSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 模型提供商
     */
    private String provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 消息列表
     */
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 总Token数
     */
    private Integer totalTokens = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 是否已归档
     */
    private Boolean archived = false;
}
