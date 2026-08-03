package plus.ruoyi.common.log.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志事件
 *
 * @author Lion Li
 */

@Data
public class OperLogEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    private Long operId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 操作模块
     */
    private String title;

    /**
     * 操作类型
     */
    private String operType;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作用户类别
     */
    private String operatorType;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请求url
     */
    private String operUrl;

    /**
     * 操作地址
     */
    private String operIp;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作结果
     */
    private String status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private Date operTime;

    /**
     * 消耗时间
     */
    private Long costTime;
}
