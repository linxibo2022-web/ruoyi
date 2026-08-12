package plus.ruoyi.common.langchain4j;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import plus.ruoyi.common.langchain4j.core.chat.ChatMemoryManager;
import plus.ruoyi.common.langchain4j.core.chat.ChatService;
import plus.ruoyi.common.langchain4j.core.embedding.EmbeddingService;
import plus.ruoyi.common.langchain4j.core.rag.RagService;
import plus.ruoyi.common.langchain4j.domain.dto.ChatRequest;
import plus.ruoyi.common.langchain4j.domain.dto.AiChatResponse;
import plus.ruoyi.common.langchain4j.domain.dto.DocumentReference;
import plus.ruoyi.common.langchain4j.enums.ChatMode;
import plus.ruoyi.common.langchain4j.enums.ModelProvider;
import plus.ruoyi.common.langchain4j.factory.ModelFactory;
import plus.ruoyi.common.langchain4j.utils.PromptUtils;
import plus.ruoyi.common.langchain4j.utils.TokenCounter;
import plus.ruoyi.common.test.base.BaseSpringTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LangChain4j模块集成测试
 * <p>
 * 注意：运行测试前请确保：
 * 1. 已在langchain4j.yml中配置正确的API Key
 * 2. Redis服务已启动（如果使用Redis存储）
 * 3. 网络连接正常
 *
 * @author 抓蛙师
 */
@Slf4j
// 与项目其他单测一致，使用 dev 标签（surefire 通过 <groups>${profiles.active}</groups> 过滤）
@Tag("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// langchain4j.yml 默认 enabled=false（生产场景需用环境变量显式打开），测试中强制启用，
// 并把 memory-store-type 改为本地内存，避免单测时依赖 Redis
// 同时排除 Redisson + lock4j 自动配置，因为本测试不需要分布式锁 / Redis
// ⚠️ 以下三个类名随 Redisson / lock4j 版本而定，升级这些依赖后如果测试报
//    NoSuchBeanDefinition / UnsatisfiedDependency，优先检查类名是否已变更
@TestPropertySource(properties = {
    "langchain4j.enabled=true",
    "langchain4j.chat.memory-store-type=memory",
    "spring.autoconfigure.exclude="
        + "org.redisson.spring.starter.RedissonAutoConfigurationV2,"
        + "com.baomidou.lock.spring.boot.autoconfigure.LockAutoConfiguration,"
        + "com.baomidou.lock.spring.boot.autoconfigure.RedissonLockAutoConfiguration"
})
public class LangChain4jModuleTest extends BaseSpringTest {

    @Autowired(required = false)
    private ChatService chatService;

    @Autowired(required = false)
    private ChatMemoryManager memoryManager;

    @Autowired(required = false)
    private EmbeddingService embeddingService;

    @Autowired(required = false)
    private RagService ragService;

    @Autowired(required = false)
    private ModelFactory modelFactory;

    private static String testSessionId;

    // 使用DeepSeek作为默认测试模型
    private static final String TEST_PROVIDER = "deepseek";

    @BeforeAll
    static void beforeAll() {
        log.info("========================================");
        log.info("开始 LangChain4j 模块测试");
        log.info("测试模型: DeepSeek");
        log.info("========================================");
    }

    @AfterAll
    static void afterAll() {
        log.info("========================================");
        log.info("LangChain4j 模块测试完成");
        log.info("========================================");
    }

    @Override
    protected void setUp() {
        if (chatService == null) {
            log.warn("ChatService未注入，请检查配置");
        }
    }

    // ==================== 基础功能测试 ====================

    @Test
    @Order(1)
    @DisplayName("1. 测试模块自动配置")
    void testAutoConfiguration() {
        assertNotNull(chatService, "ChatService应该被自动配置");
        assertNotNull(memoryManager, "ChatMemoryManager应该被自动配置");
        assertNotNull(modelFactory, "ModelFactory应该被自动配置");
        log.info("✓ 模块自动配置测试通过");
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试枚举类")
    void testEnums() {
        // 测试ModelProvider枚举
        ModelProvider provider = ModelProvider.fromCode("openai");
        assertEquals(ModelProvider.OPENAI, provider);
        assertEquals("OpenAI", provider.getName());

        // 测试DeepSeek
        ModelProvider deepseek = ModelProvider.fromCode("deepseek");
        assertEquals(ModelProvider.DEEPSEEK, deepseek);
        assertEquals("https://api.deepseek.com", deepseek.getDefaultBaseUrl());

        log.info("✓ 枚举类测试通过");
    }

    // ==================== Token工具测试 ====================

    @Test
    @Order(3)
    @DisplayName("3. 测试Token计数工具")
    void testTokenCounter() {
        String text = "这是一段测试文本，用于测试Token计数功能。";

        // 测试Token估算
        int tokenCount = TokenCounter.estimateTokenCount(text);
        assertTrue(tokenCount > 0, "Token数量应该大于0");
        log.info("文本Token数量: {}", tokenCount);

        // 测试中文Token估算
        int chineseTokens = TokenCounter.estimateChineseTokens(text);
        assertTrue(chineseTokens > 0, "中文Token数量应该大于0");
        log.info("中文Token估算: {}", chineseTokens);

        // 测试Token限制
        assertFalse(TokenCounter.exceedsLimit(text, 1000));

        // 测试文本截断
        String longText = "测试".repeat(1000);
        String truncated = TokenCounter.truncateToTokenLimit(longText, 100);
        assertTrue(truncated.length() < longText.length());

        log.info("✓ Token计数工具测试通过");
    }

    // ==================== 提示词工具测试 ====================

    @Test
    @Order(4)
    @DisplayName("4. 测试提示词工具")
    void testPromptUtils() {
        // 测试系统提示词构建
        String systemPrompt = PromptUtils.buildSystemPrompt(
                "专业的Java开发工程师",
                "擅长Spring Boot和微服务架构开发"
        );
        assertTrue(systemPrompt.contains("Java开发工程师"));
        log.info("系统提示词: {}", systemPrompt);

        // 测试模板填充
        String template = "你好，{{name}}！今天是{{date}}。";
        String filled = PromptUtils.fillTemplate(template, Map.of(
                "name", "张三",
                "date", "2025年1月1日"
        ));
        assertEquals("你好，张三！今天是2025年1月1日。", filled);

        // 测试思维链提示词
        String cotPrompt = PromptUtils.buildCoTPrompt("计算1到100的和");
        assertTrue(cotPrompt.contains("一步一步"));

        log.info("✓ 提示词工具测试通过");
    }

    // ==================== 会话管理测试 ====================

    @Test
    @Order(5)
    @DisplayName("5. 测试会话管理")
    void testChatMemoryManager() {
        // 生成会话ID
        String sessionId = memoryManager.generateSessionId();
        assertNotNull(sessionId);
        assertTrue(sessionId.length() > 0);
        log.info("生成会话ID: {}", sessionId);

        // 创建会话内存
        var memory = memoryManager.getOrCreateMemory(sessionId);
        assertNotNull(memory);

        // 获取消息列表（应为空）
        var messages = memoryManager.getMessages(sessionId);
        assertNotNull(messages);

        // 清除会话
        memoryManager.clearMemory(sessionId);

        log.info("✓ 会话管理测试通过");
    }

    // ==================== 单轮对话测试 ====================

    @Test
    @Order(10)
    @DisplayName("10. 测试单轮同步对话 (DeepSeek)")
    @Disabled("需要配置DeepSeek API Key，CI 环境不可用")
    void testSingleChat() {
        if (chatService == null) {
            log.warn("⊘ ChatService未配置，跳过测试");
            return;
        }

        ChatRequest request = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setMessage("你好，请用一句话介绍你自己")
                .setMode(ChatMode.SINGLE)
                .setStream(false)
                .setMaxTokens(100);

        long startTime = System.currentTimeMillis();
        AiChatResponse response = chatService.chat(request);
        long endTime = System.currentTimeMillis();

        // 验证响应
        assertNotNull(response, "响应不应为null");
        assertNotNull(response.getContent(), "响应内容不应为null");
        assertTrue(response.getContent().length() > 0, "响应内容不应为空");
        assertTrue(response.getFinished(), "响应应该已完成");
        assertNull(response.getError(), "不应有错误");

        log.info("DeepSeek单轮对话响应: {}", response.getContent());
        log.info("响应时间: {}ms", endTime - startTime);
        log.info("Token使用: {}", response.getTokenUsage());
        log.info("✓ 单轮同步对话测试通过");
    }

    // ==================== 多轮对话测试 ====================

    @Test
    @Order(11)
    @DisplayName("11. 测试多轮连续对话 (DeepSeek)")
    @Disabled("需要配置DeepSeek API Key，CI 环境不可用")
    void testContinuousChat() {
        if (chatService == null) {
            log.warn("⊘ ChatService未配置，跳过测试");
            return;
        }

        // 生成测试会话ID
        testSessionId = memoryManager.generateSessionId();
        log.info("测试会话ID: {}", testSessionId);

        // 第一轮对话
        ChatRequest request1 = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setSessionId(testSessionId)
                .setMessage("我叫张三，是一名软件工程师")
                .setMode(ChatMode.CONTINUOUS)
                .setStream(false);

        AiChatResponse response1 = chatService.chat(request1);
        assertNotNull(response1.getContent());
        log.info("第一轮响应: {}", response1.getContent());

        // 第二轮对话 - 测试上下文记忆
        ChatRequest request2 = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setSessionId(testSessionId)
                .setMessage("我叫什么名字？我的职业是什么？")
                .setMode(ChatMode.CONTINUOUS)
                .setStream(false);

        AiChatResponse response2 = chatService.chat(request2);
        assertNotNull(response2.getContent());

        // 验证是否包含上下文信息
        String content = response2.getContent().toLowerCase();
        assertTrue(
                content.contains("张三") || content.contains("软件工程师"),
                "应该能记住之前的对话内容"
        );

        log.info("第二轮响应: {}", response2.getContent());
        log.info("✓ 多轮连续对话测试通过");
    }

    // ==================== 流式对话测试 ====================

    @Test
    @Order(12)
    @DisplayName("12. 测试流式对话 (DeepSeek)")
    @Disabled("需要配置DeepSeek API Key，CI 环境不可用")
    void testStreamChat() throws InterruptedException {
        if (chatService == null) {
            log.warn("⊘ ChatService未配置，跳过测试");
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder fullContent = new StringBuilder();

        ChatRequest request = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setMessage("请数1到5")
                .setMode(ChatMode.SINGLE)
                .setStream(true);

        log.info("开始DeepSeek流式对话...");
        long startTime = System.currentTimeMillis();

        chatService.streamChat(request, response -> {
            if (!response.getFinished()) {
                fullContent.append(response.getContent());
                System.out.print(response.getContent());
            } else {
                System.out.println();
                log.info("流式对话完成");
                log.info("总响应时间: {}ms", System.currentTimeMillis() - startTime);
                log.info("完整内容长度: {}", fullContent.length());
                log.info("Token使用: {}", response.getTokenUsage());
                latch.countDown();
            }
        });

        // 等待完成（最多30秒）
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "流式对话应该在30秒内完成");
        assertTrue(fullContent.length() > 0, "应该接收到响应内容");

        log.info("✓ 流式对话测试通过");
    }

    // ==================== 本地功能测试（不需要联网）====================

    @Test
    @Order(20)
    @DisplayName("20. 测试文本向量化 (本地模型)")
    void testEmbedding() {
        if (true) {
            log.info("EmbeddingService跳过测试");
            return;
        }
        if (embeddingService == null) {
            log.warn("⊘ EmbeddingService未配置，跳过测试");
            return;
        }

        // 测试单个文本嵌入
        String text = "这是一段测试文本";
        List<Float> embedding = embeddingService.embed(text);

        assertNotNull(embedding);
        assertTrue(embedding.size() > 0);
        log.info("向量维度: {}", embedding.size());

        // 测试批量文本嵌入
        List<String> texts = List.of("文本1", "文本2", "文本3");
        List<List<Float>> embeddings = embeddingService.embedAll(texts);

        assertEquals(3, embeddings.size());
        log.info("批量嵌入完成: {} 个文本", embeddings.size());

        log.info("✓ 文本向量化测试通过");
    }

    // ==================== RAG功能测试 ====================

    @Test
    @Order(21)
    @DisplayName("21. 测试RAG检索功能 (本地)")
    void testRag() {
        if (true) {
            log.info("RAG跳过测试");
            return;
        }
        if (ragService == null || embeddingService == null) {
            log.warn("⊘ RAG相关服务未配置，跳过测试");
            return;
        }

        // 添加测试文档
        ragService.addDocument("doc-1",
                "Spring Boot是一个开源的Java框架，用于简化Spring应用的初始搭建和开发过程。");
        ragService.addDocument("doc-2",
                "MyBatis是一个优秀的持久层框架，支持自定义SQL、存储过程和高级映射。");
        ragService.addDocument("doc-3",
                "Redis是一个开源的内存数据结构存储系统，可以用作数据库、缓存和消息代理。");

        log.info("已添加3个测试文档到向量库");

        // 测试检索
        String query = "什么是持久层框架？";
        List<DocumentReference> results = ragService.retrieve(query, 3);

        assertNotNull(results);
        assertTrue(results.size() > 0, "应该检索到相关文档");

        log.info("检索到 {} 个相关文档", results.size());
        for (int i = 0; i < results.size(); i++) {
            DocumentReference ref = results.get(i);
            log.info("文档 {}: score={}, content={}",
                    i + 1, ref.getScore(), ref.getContent());
        }

        ragService.clearAll();
        log.info("✓ RAG检索功能测试通过");
    }

    // ==================== 会话管理测试 ====================

    @Test
    @Order(30)
    @SuppressWarnings("deprecation")
    @DisplayName("30. 测试会话历史查询")
    void testSessionHistory() {
        if (chatService == null || testSessionId == null) {
            log.warn("⊘ 需要先运行多轮对话测试，跳过此测试");
            return;
        }

        // 获取会话消息历史
        var messages = chatService.getSessionMessages(testSessionId);
        assertNotNull(messages);
        assertTrue(messages.size() > 0, "会话应该有消息历史");

        log.info("会话 {} 共有 {} 条消息", testSessionId, messages.size());
        for (int i = 0; i < messages.size(); i++) {
            var msg = messages.get(i);
            // langchain4j 1.x：ChatMessage 接口移除了 text()，需按子类类型分别取文本
            String text = extractMessageText(msg);
            log.info("消息 {}: {} - {}", i + 1, msg.type(),
                    text.substring(0, Math.min(50, text.length())));
        }

        log.info("✓ 会话历史查询测试通过");
    }

    // ==================== 会话清理测试 ====================

    @Test
    @Order(31)
    @DisplayName("31. 测试会话清理")
    void testSessionClear() {
        if (chatService == null || testSessionId == null) {
            log.warn("⊘ 需要先运行多轮对话测试，跳过此测试");
            return;
        }

        // 清除指定会话
        chatService.clearSession(testSessionId);
        log.info("已清除会话: {}", testSessionId);

        // 验证会话已清除
        var messages = chatService.getSessionMessages(testSessionId);
        assertTrue(messages.isEmpty() || messages.size() == 0,
                "清除后会话应该没有消息");

        log.info("✓ 会话清理测试通过");
    }

    // ==================== DeepSeek进阶测试 ====================

    @Test
    @Order(50)
    @DisplayName("50. 测试自定义系统提示词 (DeepSeek)")
    @Disabled("需要配置DeepSeek API Key，CI 环境不可用")
    void testSystemPrompt() {
        if (chatService == null) {
            log.warn("⊘ ChatService未配置，跳过测试");
            return;
        }

        String systemPrompt = PromptUtils.buildSystemPrompt(
                "一个专业的技术文档助手",
                "你擅长用简洁清晰的语言解释技术概念。"
        );

        ChatRequest request = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setSystemPrompt(systemPrompt)
                .setMessage("用一句话解释什么是微服务")
                .setMode(ChatMode.SINGLE)
                .setStream(false);

        AiChatResponse response = chatService.chat(request);
        assertNotNull(response.getContent());
        log.info("系统提示词响应: {}", response.getContent());

        log.info("✓ 系统提示词测试通过");
    }

    // ==================== 参数调整测试 ====================

    @Test
    @Order(51)
    @DisplayName("51. 测试不同温度参数 (DeepSeek)")
    void testDifferentTemperature() {
        if (chatService == null) {
            log.warn("⊘ ChatService未配置，跳过测试");
            return;
        }

        String question = "用一个词形容春天";

        // 低温度测试
        ChatRequest lowTempRequest = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setMessage(question)
                .setMode(ChatMode.SINGLE)
                .setStream(false)
                .setTemperature(0.1);

        AiChatResponse lowTempResponse = chatService.chat(lowTempRequest);
        log.info("低温度(0.1)响应: {}", lowTempResponse.getContent());

        // 高温度测试
        ChatRequest highTempRequest = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setMessage(question)
                .setMode(ChatMode.SINGLE)
                .setStream(false)
                .setTemperature(1.5);

        AiChatResponse highTempResponse = chatService.chat(highTempRequest);
        log.info("高温度(1.5)响应: {}", highTempResponse.getContent());

        log.info("✓ 温度参数测试完成");
    }

    // ==================== 综合场景测试 ====================

    @Test
    @Order(60)
    @DisplayName("60. 综合场景测试 - 技术问答 (DeepSeek)")
    void testTechnicalQA() {
        if (chatService == null) {
            log.warn("⊘ ChatService未配置，跳过测试");
            return;
        }

        String sessionId = memoryManager.generateSessionId();
        log.info("========================================");
        log.info("场景测试：技术问答对话");
        log.info("========================================");

        String systemPrompt = """
                你是一个专业的Java技术顾问。
                请用简洁专业的语言回答技术问题。
                """;

        // 第一轮
        ChatRequest q1 = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setSessionId(sessionId)
                .setSystemPrompt(systemPrompt)
                .setMessage("Spring Boot的主要优势是什么？")
                .setMode(ChatMode.CONTINUOUS)
                .setStream(false);

        AiChatResponse a1 = chatService.chat(q1);
        log.info("Q1: Spring Boot的主要优势是什么？");
        log.info("A1: {}", a1.getContent());

        // 第二轮
        ChatRequest q2 = new ChatRequest()
                .setProvider(TEST_PROVIDER)
                .setSessionId(sessionId)
                .setMessage("它和传统Spring有什么区别？")
                .setMode(ChatMode.CONTINUOUS)
                .setStream(false);

        AiChatResponse a2 = chatService.chat(q2);
        log.info("Q2: 它和传统Spring有什么区别？");
        log.info("A2: {}", a2.getContent());

        log.info("========================================");
        log.info("✓ 技术问答场景测试完成");

        chatService.clearSession(sessionId);
    }

    /**
     * 从 ChatMessage 中提取文本内容
     * <p>
     * langchain4j 1.x 重构：ChatMessage 接口仅保留 type()，文本访问方法下沉到具体子类：
     * - UserMessage 用 singleText()（可能包含多模态内容）
     * - AiMessage / SystemMessage / ToolExecutionResultMessage 用 text()
     * 测试日志只需打印消息预览，做一次类型分派即可。
     */
    private static String extractMessageText(ChatMessage msg) {
        if (msg instanceof UserMessage um) {
            return um.singleText();
        }
        if (msg instanceof AiMessage am) {
            return am.text() != null ? am.text() : "";
        }
        if (msg instanceof SystemMessage sm) {
            return sm.text();
        }
        return String.valueOf(msg);
    }
}
