package plus.ruoyi.common.langchain4j.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 向量存储类型
 */
@Getter
@AllArgsConstructor
public enum VectorStoreType {
    MEMORY("memory", "内存存储"),
    MILVUS("milvus", "Milvus向量库"),
    PGVECTOR("pgvector", "PostgreSQL向量扩展");

    private final String code;
    private final String name;
}
