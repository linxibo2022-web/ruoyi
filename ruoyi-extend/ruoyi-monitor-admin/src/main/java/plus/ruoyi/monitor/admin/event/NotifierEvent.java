package plus.ruoyi.monitor.admin.event;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务状态变更通知事件
 * 用于在服务实例状态发生变化时传递相关信息
 *
 * @author AprilWind
 */
@Data
public class NotifierEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例注册名称（服务名）
     */
    private String registerName;

    /**
     * 实例状态的中文描述
     */
    private String statusName;

    /**
     * 实例的唯一标识符
     */
    private String instanceId;

    /**
     * 实例当前状态（UP、DOWN、OFFLINE等）
     */
    private String status;

    /**
     * 服务的访问地址
     */
    private String serviceUrl;
}
