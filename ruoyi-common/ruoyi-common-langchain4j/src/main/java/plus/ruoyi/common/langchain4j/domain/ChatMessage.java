package plus.ruoyi.common.langchain4j.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import plus.ruoyi.common.langchain4j.enums.MessageRole;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息实体
 */
@Data
@Accessors(chain = true)
public class ChatMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息角色
     */
    private MessageRole role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * Token数量
     */
    private Integer tokenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 函数调用(如果有)
     */
    private String functionCall;
}
