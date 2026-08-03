package plus.ruoyi.common.langchain4j.core.chat;

import cn.hutool.core.util.IdUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;
import plus.ruoyi.common.langchain4j.store.redis.RedisChatStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话内存管理器
 * 负责管理会话的消息历史
 *
 * @author 抓蛙师
 */
@Slf4j
public class ChatMemoryManager {

    private final LangChain4jProperties properties;
    private final RedisChatStore redisChatStore;

    /**
     * 构造方法
     */
    public ChatMemoryManager(LangChain4jProperties properties, RedisChatStore redisChatStore) {
        this.properties = properties;
        this.redisChatStore = redisChatStore;
    }

    /**
     * 内存缓存 - 用于快速访问
     */
    private final Map<String, ChatMemory> memoryCache = new ConcurrentHashMap<>();

    /**
     * 获取或创建会话内存
     */
    public ChatMemory getOrCreateMemory(String sessionId) {
        if (sessionId == null) {
            sessionId = generateSessionId();
        }

        return memoryCache.computeIfAbsent(sessionId, this::createMemory);
    }

    /**
     * 创建新的会话内存
     */
    private ChatMemory createMemory(String sessionId) {
        int maxMessages = properties.getChat().getHistorySize();

        if ("redis".equalsIgnoreCase(properties.getChat().getMemoryStoreType())) {
            log.debug("Creating Redis-backed chat memory for session: {}", sessionId);
            return MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(maxMessages)
                .chatMemoryStore(redisChatStore)
                .build();
        } else {
            log.debug("Creating in-memory chat memory for session: {}", sessionId);
            return MessageWindowChatMemory.withMaxMessages(maxMessages);
        }
    }

    /**
     * 获取会话消息历史
     */
    public List<ChatMessage> getMessages(String sessionId) {
        ChatMemory memory = memoryCache.get(sessionId);
        if (memory == null) {
            return List.of();
        }
        return memory.messages();
    }

    /**
     * 清除会话内存
     */
    public void clearMemory(String sessionId) {
        ChatMemory memory = memoryCache.remove(sessionId);
        if (memory != null) {
            memory.clear();
            log.info("Cleared memory for session: {}", sessionId);
        }
    }

    /**
     * 清除所有会话内存
     */
    public void clearAllMemories() {
        memoryCache.values().forEach(ChatMemory::clear);
        memoryCache.clear();
        log.info("Cleared all chat memories");
    }

    /**
     * 生成会话ID
     */
    public String generateSessionId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return memoryCache.size();
    }

    /**
     * 移除过期会话(可配合定时任务使用)
     */
    public void removeExpiredSessions() {
        // 这里可以实现过期检查逻辑
        // 例如检查最后活动时间超过配置的超时时间的会话
        log.debug("Checking for expired sessions...");
    }
}
