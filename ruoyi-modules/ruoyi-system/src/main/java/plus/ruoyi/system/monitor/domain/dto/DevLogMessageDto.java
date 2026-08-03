package plus.ruoyi.system.monitor.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import plus.ruoyi.system.monitor.domain.bo.DevLogBo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 开发日志WebSocket消息DTO
 *
 * @author 抓蛙师
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevLogMessageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String type = "devLog";

    /**
     * 日志列表
     */
    private List<DevLogBo.LogItem> logs;

    /**
     * 创建开发日志消息
     *
     * @param logs 日志列表
     * @return 日志消息DTO
     */
    public static DevLogMessageDto of(List<DevLogBo.LogItem> logs) {
        return new DevLogMessageDto("devLog", logs);
    }
}
