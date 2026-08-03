package plus.ruoyi.common.langchain4j.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Token使用情况
 */
@Data
public class TokenUsage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
