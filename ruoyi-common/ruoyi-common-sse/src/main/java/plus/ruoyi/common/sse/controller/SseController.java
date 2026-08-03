package plus.ruoyi.common.sse.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.satoken.utils.LoginHelper;
import plus.ruoyi.common.sse.core.SseEmitterManager;
import plus.ruoyi.common.sse.dto.SseMessageDto;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * SSE（Server-Sent Events）控制器
 * <p>
 * 提供SSE连接管理和消息推送的HTTP接口，只有在sse.enabled=true时才生效
 *
 * @author Lion Li
 */
@RestController
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    private final SseEmitterManager sseEmitterManager;

    /**
     * 建立SSE连接
     * <p>
     * 客户端通过此接口建立与服务器的长连接，用于接收实时推送消息
     * 需要用户登录状态验证
     *
     * @return SSE连接对象，返回text/event-stream格式的响应
     */
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        StpUtil.checkLogin();
        String tokenValue = StpUtil.getTokenValue();
        Long userId = LoginHelper.getUserId();
        return sseEmitterManager.connect(userId, tokenValue);
    }

    /**
     * 关闭SSE连接
     * <p>
     * 主动断开当前用户的SSE连接，释放相关资源
     * 此接口忽略权限验证
     *
     * @return 操作结果
     */
    @SaIgnore
    @GetMapping(value = "${sse.path}/close")
    public R<Void> close() {
        String tokenValue = StpUtil.getTokenValue();
        Long userId = LoginHelper.getUserId();
        sseEmitterManager.disconnect(userId, tokenValue);
        return R.ok();
    }

    /**
     * 向指定用户发送消息
     * <p>
     * 通过Redis发布订阅机制向特定用户推送消息，支持集群环境
     *
     * @param userId 目标用户ID
     * @param msg    消息内容
     * @return 操作结果
     */
    @GetMapping(value = "${sse.path}/send")
    public R<Void> send(Long userId, String msg) {
        SseMessageDto dto = new SseMessageDto();
        dto.setUserIds(List.of(userId));
        dto.setMessage(msg);
        sseEmitterManager.publishMessage(dto);
        return R.ok();
    }

    /**
     * 向所有用户广播消息
     * <p>
     * 通过Redis发布订阅机制向所有在线用户推送消息
     *
     * @param msg 消息内容
     * @return 操作结果
     */
    @GetMapping(value = "${sse.path}/sendAll")
    public R<Void> send(String msg) {
        sseEmitterManager.publishAll(msg);
        return R.ok();
    }

    /**
     * 销毁时的资源清理
     * <p>
     * 实现DisposableBean接口，在Bean销毁时调用
     * 当前实现为空，避免因未实现而导致的错误
     */
    @Override
    public void destroy() throws Exception {
        // 销毁时不需要特殊处理, 此方法避免无用操作报错
    }

}
