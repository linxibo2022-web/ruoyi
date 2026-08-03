package plus.ruoyi.common.langchain4j.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识文档实体
 */
@Data
@Accessors(chain = true)
public class KnowledgeDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文档类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 分块数量
     */
    private Integer chunkCount;

    /**
     * 向量化状态: 0-待处理, 1-处理中, 2-已完成, 3-失败
     */
    private Integer embeddingStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
