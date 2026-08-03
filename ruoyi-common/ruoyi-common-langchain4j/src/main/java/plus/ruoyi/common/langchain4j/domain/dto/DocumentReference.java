package plus.ruoyi.common.langchain4j.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档引用
 */
@Data
public class DocumentReference implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long documentId;
    private String documentName;
    private String content;
    private Double score;
}
