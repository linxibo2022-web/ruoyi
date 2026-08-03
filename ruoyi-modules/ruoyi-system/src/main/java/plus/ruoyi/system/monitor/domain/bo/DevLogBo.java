package plus.ruoyi.system.monitor.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 开发日志业务对象
 *
 * @author 抓蛙师
 */
@Data
public class DevLogBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志列表
     */
    @Valid
    @NotEmpty(message = "日志列表不能为空")
    private List<LogItem> logs;

    /**
     * 日志项
     */
    @Data
    public static class LogItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 日志级别
         */
        private String level;

        /**
         * 日志内容
         */
        private String message;

        /**
         * 时间戳
         */
        private Long timestamp;

        /**
         * 页面路径
         */
        private String path;

        /**
         * 用户ID(可选)
         */
        private String userId;
    }
}
