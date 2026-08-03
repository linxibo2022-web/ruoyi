package plus.ruoyi.system.monitor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 错误日志对象 sys_error_log
 *
 * @author 抓蛙师
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_error_log")
public class SysErrorLog extends TenantEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 严重级别(ERROR/WARN/FATAL)
     */
    private String errorLevel;

    /**
     * 异常类名(ServiceException/SQLException等)
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
     * 异常堆栈(限制5000字符)
     */
    private String errorStack;

    /**
     * 链路追踪ID(MDC中的traceId)
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
     * 请求方法(GET/POST)
     */
    private String requestMethod;

    /**
     * 请求参数(JSON格式，已脱敏)
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
     * 执行的SQL语句
     */
    private String sqlStatement;

    /**
     * SQL参数(JSON格式)
     */
    private String sqlParams;

    /**
     * SQL执行耗时(ms)
     */
    private Integer sqlDuration;

    /**
     * 业务模块(base/mall/iot/crm)
     */
    private String moduleName;

    /**
     * 业务类型(查询/新增/修改/删除)
     */
    private String businessType;

    /**
     * 业务关键字(订单号/商品ID/设备ID等)
     */
    private String businessKey;

    /**
     * 平台类型(PC/H5/MINIAPP/APP)
     */
    private String clientType;

    /**
     * 客户端版本
     */
    private String clientVersion;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 服务器IP
     */
    private String serverIp;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * 处理状态(0未处理 1已处理 2已忽略)
     */
    private String handleStatus;

    /**
     * 处理人ID
     */
    private Long handleBy;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 相同错误出现次数
     */
    private Integer occurrenceCount;

    /**
     * 首次出现时间
     */
    private Date firstTime;

    /**
     * 最后出现时间
     */
    private Date lastTime;

    /**
     * 备注
     */
    private String remark;

}
