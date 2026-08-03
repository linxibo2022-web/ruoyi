package plus.ruoyi.business.api.common;

import cn.hutool.core.util.IdUtil;
import dev.langchain4j.data.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.langchain4j.core.chat.ChatService;
import plus.ruoyi.common.langchain4j.domain.dto.ChatRequest;
import plus.ruoyi.common.langchain4j.domain.dto.AiChatResponse;
import plus.ruoyi.common.langchain4j.enums.ChatMode;

import java.io.IOException;
import java.util.List;

/**
 * AI 聊天接口
 *
 * @author 抓蛙师
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/common/ai/chat")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "langchain4j", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiChatController {

    /** 对话服务 */
    private final ChatService chatService;

    /**
     * 同步对话
     *
     * @param request 对话请求参数
     * @return 对话回复
     */
    @PostMapping("/syncChat")
    public R<AiChatResponse> syncChat(@Validated @RequestBody ChatRequest request) {
        AiChatResponse response = chatService.chat(request);
        return R.ok(response);
    }

    /**
     * 流式对话
     *
     * @param request 对话请求参数
     * @return SSE 推送对象
     */
    @PostMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Validated @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步处理流式响应
        new Thread(() -> {
            try {
                chatService.streamChat(request, response -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .id(response.getMessageId())
                                .data(response));

                        // 如果响应完成，关闭连接
                        if (response.getFinished()) {
                            emitter.complete();
                        }
                    } catch (IOException e) {
                        log.error("Error sending SSE event", e);
                        emitter.completeWithError(e);
                    }
                });
            } catch (Exception e) {
                log.error("Stream chat error", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 创建会话
     *
     * @return 会话标识
     */
    @PostMapping("/createSession")
    public R<String> createSession() {
        String sessionId = IdUtil.fastSimpleUUID();
        // 使用 R.ok(null, sessionId) 确保 sessionId 放到 data 字段
        // R.ok(sessionId) 会匹配 R.ok(String msg)，导致 sessionId 被放到 msg 字段
        return R.ok(null, sessionId);
    }

    /**
     * 清除会话
     *
     * @param sessionId 会话标识
     * @return 操作结果
     */
    @DeleteMapping("/deleteSession/{sessionId}")
    public R<Void> deleteSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return R.ok();
    }

    /**
     * 查询会话历史
     *
     * @param sessionId 会话标识
     * @return 消息列表
     */
    @GetMapping("/getSessionMessages/{sessionId}")
    public R<List<ChatMessage>> getSessionMessages(@PathVariable String sessionId) {
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        return R.ok(messages);
    }

    /**
     * 快速测试
     *
     * @param message 测试问题
     * @return 对话回复
     */
    @GetMapping("/testChat")
    public R<AiChatResponse> testChat(@RequestParam(defaultValue = "你好") String message) {
        ChatRequest request = new ChatRequest()
            .setMessage(message)
            .setMode(ChatMode.SINGLE)
            .setStream(false);
        AiChatResponse response = chatService.chat(request);
        return R.ok(response);
    }
}
