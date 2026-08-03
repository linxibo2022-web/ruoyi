package plus.ruoyi.common.sse.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * SSE消息传输对象
 * <p>
 * 用于在Redis发布订阅中传输SSE消息，支持指定用户推送和广播
 *
 * @author zendwang
 */
@Data
public class SseMessageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID列表
     * <p>
     * 为空时表示广播消息，非空时表示向指定用户推送
     */
    private List<Long> userIds;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 默认构造函数
     */
    public static SseMessageDto of(List<Long> userIds, String message) {
        SseMessageDto dto = new SseMessageDto();
        dto.setUserIds(userIds);
        dto.setMessage(message);
        return dto;
    }
}
