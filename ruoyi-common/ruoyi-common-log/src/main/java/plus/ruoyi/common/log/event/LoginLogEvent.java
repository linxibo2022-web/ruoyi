package plus.ruoyi.common.log.event;

import lombok.Data;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录事件
 *
 * @author Lion Li
 */

@Data
public class LoginLogEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 登录状态
     */
    private String status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 请求体
     */
    private HttpServletRequest request;

    /**
     * 其他参数
     */
    private Object[] args;

}
