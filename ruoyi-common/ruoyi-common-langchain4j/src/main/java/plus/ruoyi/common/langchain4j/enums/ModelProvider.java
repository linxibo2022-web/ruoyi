package plus.ruoyi.common.langchain4j.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型提供商枚举
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum ModelProvider {

    /**
     * DeepSeek
     */
    DEEPSEEK("deepseek", "DeepSeek", "https://api.deepseek.com"),

    /**
     * 阿里云通义千问
     */
    QIANWEN("qianwen", "通义千问", "https://dashscope.aliyuncs.com/api/v1"),

    /**
     * Anthropic Claude
     */
    CLAUDE("claude", "Claude", "https://api.anthropic.com"),

    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI", "https://api.openai.com/v1"),
    /**
     * 本地Ollama
     */
    OLLAMA("ollama", "Ollama", "http://localhost:11434");

    /**
     * 提供商代码
     */
    private final String code;

    /**
     * 提供商名称
     */
    private final String name;

    /**
     * 默认API地址
     */
    private final String defaultBaseUrl;

    /**
     * 根据代码获取枚举
     */
    public static ModelProvider fromCode(String code) {
        for (ModelProvider provider : values()) {
            if (provider.getCode().equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown model provider: " + code);
    }
}
