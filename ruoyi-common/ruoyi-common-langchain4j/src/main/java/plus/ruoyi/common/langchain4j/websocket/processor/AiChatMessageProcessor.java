package plus.ruoyi.common.langchain4j.websocket.processor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import plus.ruoyi.common.core.domain.model.LoginUser;
import plus.ruoyi.common.json.utils.JsonUtils;
import plus.ruoyi.common.langchain4j.core.chat.ChatService;
import plus.ruoyi.common.langchain4j.domain.dto.ChatRequest;
import plus.ruoyi.common.langchain4j.domain.dto.AiChatResponse;
import plus.ruoyi.common.langchain4j.enums.ChatMode;
import plus.ruoyi.common.langchain4j.websocket.dto.*;
import plus.ruoyi.common.websocket.processor.MessageProcessor;
import plus.ruoyi.common.websocket.utils.WebSocketUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI聊天消息处理器
 * <p>
 * 处理AI聊天请求,支持流式响应
 * 负责参数解析、校验、转换以及调用ChatService进行AI对话
 *
 * @author zendwang
 */
@Slf4j
@Component
@Order(2)
public class AiChatMessageProcessor implements MessageProcessor {

    @Autowired(required = false)
    private  ChatService chatService;

    @Override
    public boolean support(String type) {
        return "ai_chat".equalsIgnoreCase(type);
    }

    @Override
    public void process(WebSocketSession session, LoginUser loginUser, String payload) {
        String sessionId = null;
        try {
            // 1. 解析请求参数为强类型对象
            AiChatWebSocketRequest wsRequest = parseRequest(payload);
            sessionId = wsRequest.getSessionId();

            // 2. 参数校验
            validateRequest(wsRequest);

            // 3. 转换为ChatRequest
            ChatRequest chatRequest = convertToChatRequest(wsRequest);

            // 4. 记录日志
            log.info("用户 {} 发起 AI 聊天请求 - sessionId: {}, provider: {}, message: {}",
                loginUser.getUserId(), chatRequest.getSessionId(),
                chatRequest.getProvider(), chatRequest.getMessage());

            // 5. 处理流式聊天
            handleStreamChat(session, chatRequest);

        } catch (IllegalArgumentException e) {
            // 参数校验失败
            log.warn("AI聊天请求参数校验失败 - userId: {}, error: {}",
                loginUser.getUserId(), e.getMessage());
            sendErrorMessage(session, sessionId, e.getMessage());
        } catch (Exception e) {
            // 其他异常
            log.error("处理 AI 聊天请求失败 - userId: {}", loginUser.getUserId(), e);
            sendErrorMessage(session, sessionId, "处理请求失败,请稍后重试");
        }
    }

    /**
     * 解析JSON请求为强类型对象
     */
    private AiChatWebSocketRequest parseRequest(String payload) {
        try {
            return JsonUtils.parseObject(payload, AiChatWebSocketRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("消息格式错误,无法解析JSON");
        }
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(AiChatWebSocketRequest request) {
        if (StrUtil.isBlank(request.getMessage())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        // 校验温度参数范围
        if (request.getTemperature() != null
            && (request.getTemperature() < 0 || request.getTemperature() > 2)) {
            throw new IllegalArgumentException("温度参数必须在0-2之间");
        }

        // 校验最大Token数
        if (request.getMaxTokens() != null && request.getMaxTokens() <= 0) {
            throw new IllegalArgumentException("最大Token数必须大于0");
        }
    }

    /**
     * 转换为ChatRequest对象
     */
    private ChatRequest convertToChatRequest(AiChatWebSocketRequest wsRequest) {
        // 生成或使用传入的会话ID
        String sessionId = StrUtil.isNotBlank(wsRequest.getSessionId())
            ? wsRequest.getSessionId()
            : IdUtil.fastSimpleUUID();

        // 解析对话模式
        ChatMode mode = ChatMode.CONTINUOUS;
        if (StrUtil.isNotBlank(wsRequest.getMode())) {
            try {
                mode = ChatMode.valueOf(wsRequest.getMode().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的对话模式: {}, 使用默认模式CONTINUOUS", wsRequest.getMode());
            }
        }

        return new ChatRequest()
            .setSessionId(sessionId)
            .setMessage(wsRequest.getMessage())
            .setMode(mode)
            .setProvider(wsRequest.getProvider())
            .setModelName(wsRequest.getModelName())
            .setSystemPrompt(wsRequest.getSystemPrompt())
            .setTemperature(wsRequest.getTemperature())
            .setMaxTokens(wsRequest.getMaxTokens())
            // 深度思考开关与预算（null 时沿用后端配置）
            .setThinkingEnabled(wsRequest.getThinkingEnabled())
            .setThinkingBudgetTokens(wsRequest.getThinkingBudgetTokens())
            .setStream(true);
    }

    /**
     * 处理流式聊天
     */
    private void handleStreamChat(WebSocketSession session, ChatRequest request) {
        String sessionId = request.getSessionId();

        // 使用AtomicBoolean确保线程安全
        AtomicBoolean isFirstChunk = new AtomicBoolean(true);

        chatService.streamChat(request, response -> {
            try {
                // 第一次响应时发送开始消息
                if (isFirstChunk.getAndSet(false)) {
                    sendStartMessage(session, sessionId, response.getMessageId());
                }

                // 根据完成状态发送不同类型的消息
                if (Boolean.TRUE.equals(response.getFinished())) {
                    sendCompleteMessage(session, sessionId, response);
                } else {
                    sendStreamMessage(session, sessionId, response);
                }
            } catch (Exception e) {
                log.error("发送 AI 流式响应失败 - sessionId: {}", sessionId, e);
                sendErrorMessage(session, sessionId, "发送响应失败");
            }
        });
    }

    /**
     * 发送AI聊天开始消息
     */
    private void sendStartMessage(WebSocketSession session, String sessionId, String messageId) {
        AiChatStartResponse response = AiChatStartResponse.of(sessionId, messageId);
        WebSocketUtils.sendMessage(session, JsonUtils.toJsonString(response));
    }

    /**
     * 发送AI聊天流式内容
     * <p>
     * 按 phase 区分：thinking 阶段推送 reasoningContent，content 阶段推送 content
     */
    private void sendStreamMessage(WebSocketSession session, String sessionId, AiChatResponse chatResponse) {
        AiChatStreamResponse response;
        if (AiChatResponse.Phase.THINKING.equals(chatResponse.getPhase())) {
            response = AiChatStreamResponse.ofThinking(
                sessionId,
                chatResponse.getMessageId(),
                chatResponse.getReasoningContent()
            );
        } else {
            response = AiChatStreamResponse.of(
                sessionId,
                chatResponse.getMessageId(),
                chatResponse.getContent()
            );
        }
        WebSocketUtils.sendMessage(session, JsonUtils.toJsonString(response));
    }

    /**
     * 发送AI聊天完成消息（附带完整 reasoningContent）
     */
    private void sendCompleteMessage(WebSocketSession session, String sessionId, AiChatResponse chatResponse) {
        AiChatCompleteResponse response = AiChatCompleteResponse.of(
            sessionId,
            chatResponse.getMessageId(),
            chatResponse.getContent()
        )
        .withTokenUsage(chatResponse.getTokenUsage())
        .withReferences(chatResponse.getReferences())
        .withReasoningContent(chatResponse.getReasoningContent());

        WebSocketUtils.sendMessage(session, JsonUtils.toJsonString(response));
    }

    /**
     * 发送AI聊天错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String sessionId, String error) {
        AiChatErrorResponse response = AiChatErrorResponse.of(sessionId, sanitizeErrorMessage(error));
        WebSocketUtils.sendMessage(session, JsonUtils.toJsonString(response));
    }

    /**
     * 脱敏错误信息
     */
    private String sanitizeErrorMessage(String error) {
        return StrUtil.isBlank(error) ? "未知错误" : error;
    }
}
