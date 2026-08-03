package plus.ruoyi.common.core.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 错误日志上下文快照
 *
 * @author 抓蛙师
 */
@Data
public class ErrorLogContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 严重级别
     */
    private String errorLevel;

    /**
     * 异常类名
     */
    private String errorType;

    /**
     * 业务错误码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 异常堆栈
     */
    private String errorStack;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求路径模板
     */
    private String requestPattern;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String userName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 客户端版本（从请求头 X-Client-Version 获取）
     */
    private String clientVersion;
}
