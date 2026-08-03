package plus.ruoyi.common.langchain4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * LangChain4j 配置属性
 *
 * @author 抓蛙师
 */
@Data
@ConfigurationProperties(prefix = "langchain4j")
public class LangChain4jProperties {

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 默认模型提供商
     */
    private String defaultProvider = "deepseek";

    /**
     * 默认模型名称
     */
    private String defaultModel = "deepseek-chat";

    /**
     * 请求超时时间
     */
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * DeepSeek配置
     */
    private ModelConfig deepseek = new ModelConfig();
    /**
     * 通义千问配置
     */
    private ModelConfig qianwen = new ModelConfig();
    /**
     * Claude配置
     */
    private ModelConfig claude = new ModelConfig();
    /**
     * OpenAI配置
     */
    private ModelConfig openai = new ModelConfig();
    /**
     * Ollama配置
     */
    private ModelConfig ollama = new ModelConfig();

    /**
     * 对话配置
     */
    private ChatConfig chat = new ChatConfig();

    /**
     * 嵌入配置
     */
    private EmbeddingConfig embedding = new EmbeddingConfig();

    /**
     * RAG配置
     */
    private RagConfig rag = new RagConfig();

    /**
     * 模型配置基类
     */
    @Data
    public static class ModelConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API地址
         */
        private String baseUrl;

        /**
         * 模型名称
         */
        private String modelName;

        /**
         * 温度参数 (0-2)
         */
        private Double temperature = 0.7;

        /**
         * Top P参数
         */
        private Double topP = 1.0;

        /**
         * 最大Token数
         */
        private Integer maxTokens = 2048;

        /**
         * 是否启用
         */
        private Boolean enabled = false;

        /**
         * 是否开启深度思考（reasoning / chain-of-thought）
         * <p>
         * - DeepSeek: 配合 modelName=deepseek-reasoner 使用，自动解析 reasoning_content
         * - 通义千问: Qwen3-Thinking / qwq 系列必须开启
         * - Ollama: 仅 deepseek-r1 / qwq 等推理模型支持
         * - OpenAI: 仅 o1 / o3 / gpt-5-thinking 等推理模型支持（实际通过 reasoningEffort 控制）
         * - Claude: 等同于 thinkingType=enabled
         */
        private Boolean enableThinking = false;

        /**
         * 思考预算 Token 数（仅 Anthropic Claude 必填，其他 provider 忽略）
         * <p>
         * Claude 要求 thinkingBudgetTokens < maxTokens；推荐 1024 起步，复杂推理可调到 8192-16384
         */
        private Integer thinkingBudgetTokens = 1024;

        /**
         * OpenAI 推理强度（仅 OpenAI 系列推理模型生效，如 o1/o3/gpt-5-thinking）
         * <p>
         * 取值: low | medium | high
         */
        private String reasoningEffort;

        /**
         * 是否在响应中返回思考内容（默认 true，关闭后只能在服务端日志看到 reasoning，不会推送到客户端）
         */
        private Boolean returnThinking = true;

        /**
         * 额外参数
         */
        private Map<String, Object> extraParams = new HashMap<>();
    }

    /**
     * 对话配置
     */
    @Data
    public static class ChatConfig {
        /**
         * 是否启用流式响应
         */
        private Boolean streamEnabled = true;

        /**
         * 历史消息保留数量
         */
        private Integer historySize = 10;

        /**
         * 会话超时时间(分钟)
         */
        private Integer sessionTimeout = 30;

        /**
         * 是否启用内存管理
         */
        private Boolean memoryEnabled = true;

        /**
         * 内存存储类型: memory, redis
         */
        private String memoryStoreType = "redis";
    }

    /**
     * 嵌入配置
     */
    @Data
    public static class EmbeddingConfig {
        /**
         * 嵌入模型名称
         */
        private String modelName = "text-embedding-3-small";

        /**
         * 向量维度
         */
        private Integer dimension = 1536;

        /**
         * 批处理大小
         */
        private Integer batchSize = 100;
    }

    /**
     * RAG配置
     */
    @Data
    public static class RagConfig {
        /**
         * 是否启用RAG
         */
        private Boolean enabled = false;

        /**
         * 检索结果数量
         */
        private Integer maxResults = 5;

        /**
         * 最小相似度分数
         */
        private Double minScore = 0.7;

        /**
         * 文档分块大小
         */
        private Integer chunkSize = 500;

        /**
         * 分块重叠大小
         */
        private Integer chunkOverlap = 50;

        /**
         * 向量存储类型: memory, milvus, pgvector
         */
        private String vectorStoreType = "memory";

        /**
         * Milvus配置
         */
        private MilvusConfig milvus = new MilvusConfig();
    }

    /**
     * Milvus配置
     */
    @Data
    public static class MilvusConfig {
        private String host = "localhost";
        private Integer port = 19530;
        private String collectionName = "documents";
        private String databaseName = "default";
    }
}
