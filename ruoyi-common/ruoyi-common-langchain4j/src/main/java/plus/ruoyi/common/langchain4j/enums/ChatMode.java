package plus.ruoyi.common.langchain4j.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对话模式枚举
 */
@Getter
@AllArgsConstructor
public enum ChatMode {
    /**
     * 单轮对话
     */
    SINGLE("single", "单轮对话"),

    /**
     * 多轮对话
     */
    CONTINUOUS("continuous", "多轮对话"),

    /**
     * RAG增强对话
     */
    RAG("rag", "知识库问答"),

    /**
     * 函数调用
     */
    FUNCTION("function", "函数调用");

    private final String code;
    private final String name;
}
