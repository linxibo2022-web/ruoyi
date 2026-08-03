package plus.ruoyi.common.langchain4j.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import plus.ruoyi.common.core.factory.YmlPropertySourceFactory;
import plus.ruoyi.common.langchain4j.core.chat.ChatMemoryManager;
import plus.ruoyi.common.langchain4j.core.chat.ChatService;
import plus.ruoyi.common.langchain4j.core.embedding.EmbeddingService;
import plus.ruoyi.common.langchain4j.core.rag.RagService;
import plus.ruoyi.common.langchain4j.factory.ModelFactory;
import plus.ruoyi.common.langchain4j.store.redis.RedisChatStore;

/**
 * LangChain4j 自动配置类
 *
 * <p>功能说明：
 * <ul>
 *   <li>通过 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 自动加载</li>
 *   <li>负责注册 LangChain4j 模块的所有核心 Bean</li>
 *   <li>支持通过配置文件控制功能开关</li>
 *   <li>采用 @ConditionalOnMissingBean 允许用户自定义覆盖默认实现</li>
 * </ul>
 *
 * <p>配置项：
 * <ul>
 *   <li>langchain4j.enabled - 总开关，控制整个模块是否启用（默认true）</li>
 *   <li>langchain4j.chat.memory-enabled - 对话记忆功能开关（默认true）</li>
 *   <li>langchain4j.rag.enabled - RAG功能开关（默认false）</li>
 * </ul>
 *
 * <p>Bean 注册顺序：
 * <ol>
 *   <li>ModelFactory - 模型工厂，负责创建各种AI模型实例</li>
 *   <li>RedisChatStore - Redis存储，用于持久化对话历史</li>
 *   <li>ChatMemoryManager - 对话记忆管理器（依赖 RedisChatStore）</li>
 *   <li>ChatService - 对话服务（依赖 ModelFactory 和 ChatMemoryManager）</li>
 *   <li>EmbeddingService - 向量嵌入服务</li>
 *   <li>RagService - RAG检索增强生成服务（依赖 EmbeddingService，可选）</li>
 * </ol>
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration  // 标记为自动配置类，由 Spring Boot 自动加载
@EnableConfigurationProperties(LangChain4jProperties.class)  // 启用配置属性类
@ConditionalOnProperty(
    prefix = "langchain4j",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@PropertySource(value = "classpath:langchain4j.yml", factory = YmlPropertySourceFactory.class)
public class LangChain4jAutoConfiguration {

    /**
     * 构造函数：打印模块初始化信息
     *
     * @param properties 配置属性对象，由 Spring 自动注入
     */
    public LangChain4jAutoConfiguration(LangChain4jProperties properties) {
        log.info("========================================");
        log.info("LangChain4j 模块初始化完成");
        log.info("默认提供商: {}", properties.getDefaultProvider());
        log.info("默认模型: {}", properties.getDefaultModel());
        log.info("对话记忆功能: {}", properties.getChat().getMemoryEnabled() ? "已启用" : "已禁用");
        log.info("对话存储类型: {}", properties.getChat().getMemoryStoreType());
        log.info("RAG功能: {}", properties.getRag().getEnabled() ? "已启用" : "已禁用");
        log.info("========================================");
    }

    /**
     * 注册模型工厂 Bean
     *
     * <p>负责创建和管理各种 AI 模型实例（DeepSeek、通义千问、Claude、OpenAI、Ollama 等）
     *
     * @param properties 配置属性
     * @return ModelFactory 实例
     */
    @Bean
    @ConditionalOnMissingBean  // 如果容器中已存在 ModelFactory，则跳过注册
    public ModelFactory modelFactory(LangChain4jProperties properties) {
        return new ModelFactory(properties);
    }

    /**
     * 注册 Redis 聊天存储 Bean
     *
     * <p>用于将对话历史持久化到 Redis，支持分布式部署
     *
     * @param properties 配置属性
     * @return RedisChatStore 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisChatStore redisChatStore(LangChain4jProperties properties) {
        return new RedisChatStore(properties);
    }

    /**
     * 注册对话记忆管理器 Bean
     *
     * <p>管理会话的消息历史，支持内存和 Redis 两种存储方式
     * <p>条件：需要 langchain4j.chat.memory-enabled=true（默认启用）
     *
     * @param properties 配置属性
     * @param redisChatStore Redis存储实例
     * @return ChatMemoryManager 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "langchain4j.chat",
        name = "memory-enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public ChatMemoryManager chatMemoryManager(
            LangChain4jProperties properties,
            RedisChatStore redisChatStore) {
        return new ChatMemoryManager(properties, redisChatStore);
    }

    /**
     * 注册对话服务 Bean
     *
     * <p>核心服务，提供同步和流式对话功能，支持多种对话模式：
     * <ul>
     *   <li>SINGLE - 单轮对话</li>
     *   <li>CONTINUOUS - 多轮连续对话</li>
     *   <li>RAG - 检索增强生成</li>
     *   <li>FUNCTION - 函数调用</li>
     * </ul>
     *
     * @param modelFactory 模型工厂
     * @param chatMemoryManager 对话记忆管理器
     * @param properties 配置属性
     * @return ChatService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatService chatService(
            ModelFactory modelFactory,
            ChatMemoryManager chatMemoryManager,
            LangChain4jProperties properties) {
        return new ChatService(modelFactory, chatMemoryManager, properties);
    }

    /**
     * 注册向量嵌入服务 Bean
     *
     * <p>将文本转换为向量表示，用于语义搜索和 RAG 功能
     * <p>支持 OpenAI、DeepSeek 等在线模型，以及本地模型
     *
     * @param properties 配置属性
     * @return EmbeddingService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService(LangChain4jProperties properties) {
        return new EmbeddingService(properties);
    }

    /**
     * 注册 RAG 服务 Bean
     *
     * <p>检索增强生成服务，提供文档向量化、检索等功能
     * <p>条件：需要 langchain4j.rag.enabled=true（默认关闭）
     *
     * @param properties 配置属性
     * @param embeddingService 向量嵌入服务
     * @return RagService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "langchain4j.rag", name = "enabled", havingValue = "true")
    public RagService ragService(
            LangChain4jProperties properties,
            EmbeddingService embeddingService) {
        return new RagService(properties, embeddingService);
    }
}
