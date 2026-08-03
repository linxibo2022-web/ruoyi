package plus.ruoyi.common.langchain4j.store.redis;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import plus.ruoyi.common.langchain4j.config.LangChain4jProperties;
import plus.ruoyi.common.redis.utils.RedisUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis聊天存储实现
 * 将聊天消息持久化到Redis
 *
 * @author 抓蛙师
 */
@Slf4j
public class RedisChatStore implements ChatMemoryStore {

    private final LangChain4jProperties properties;

    private static final String CHAT_MEMORY_KEY_PREFIX = "langchain4j:chat:";

    public RedisChatStore(LangChain4jProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = buildKey(memoryId);

        try {
            List<String> jsonMessages = RedisUtils.getCacheList(key);

            if (CollUtil.isEmpty(jsonMessages)) {
                return new ArrayList<>();
            }

            // 刷新过期时间
            refreshExpiration(key);

            return jsonMessages.stream()
                    .map(ChatMessageDeserializer::messageFromJson)
                    .toList();

        } catch (Exception e) {
            log.error("获取会话消息失败，会话ID: {}", memoryId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = buildKey(memoryId);

        try {
            List<String> jsonMessages = messages.stream()
                    .map(ChatMessageSerializer::messageToJson)
                    .toList();

            // 使用RedisUtils设置缓存并指定过期时间
            Duration duration = Duration.ofMinutes(properties.getChat().getSessionTimeout());
            RedisUtils.setCacheList(key, jsonMessages, duration);

            log.debug("更新会话消息成功，会话ID: {}，消息数量: {}", memoryId, messages.size());

        } catch (Exception e) {
            log.error("更新会话消息失败，会话ID: {}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = buildKey(memoryId);

        try {
            RedisUtils.deleteObject(key);
            log.debug("删除会话消息成功，会话ID: {}", memoryId);
        } catch (Exception e) {
            log.error("删除会话消息失败，会话ID: {}", memoryId, e);
        }
    }

    /**
     * 构建Redis键
     */
    private String buildKey(Object memoryId) {
        return CHAT_MEMORY_KEY_PREFIX + memoryId;
    }

    /**
     * 刷新过期时间
     */
    private void refreshExpiration(String key) {
        long timeoutMinutes = properties.getChat().getSessionTimeout();
        RedisUtils.expire(key, Duration.ofMinutes(timeoutMinutes));
    }

    /**
     * 获取所有会话ID
     */
    public List<String> getAllSessionIds() {
        Collection<String> keys = RedisUtils.keys(CHAT_MEMORY_KEY_PREFIX + "*");
        if (CollUtil.isEmpty(keys)) {
            return List.of();
        }

        return keys.stream()
                .map(key -> key.replace(CHAT_MEMORY_KEY_PREFIX, ""))
                .toList();
    }

    /**
     * 清空所有会话
     */
    public void clearAll() {
        RedisUtils.deleteKeys(CHAT_MEMORY_KEY_PREFIX + "*");
        log.info("已清空Redis中的所有聊天会话");
    }
}
