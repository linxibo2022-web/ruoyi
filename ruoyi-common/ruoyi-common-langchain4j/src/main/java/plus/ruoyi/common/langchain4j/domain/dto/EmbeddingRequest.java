package plus.ruoyi.common.langchain4j.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 嵌入请求DTO
 */
@Data
public class EmbeddingRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文本内容
     */
    private List<String> texts;

    /**
     * 模型名称
     */
    private String modelName;
}
