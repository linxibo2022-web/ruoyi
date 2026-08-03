package plus.ruoyi.system.monitor.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.system.monitor.domain.SysErrorLog;
import plus.ruoyi.system.monitor.domain.bo.SysErrorLogBo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 错误日志视图对象
 *
 * @author 抓蛙师
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = SysErrorLog.class),
    @AutoMapper(target = SysErrorLogBo.class)
})
public class SysErrorLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 租户ID
     */
    @ExcelProperty(value = "租户ID")
    private String tenantId;

    /**
     * 严重级别
     */
    @ExcelProperty(value = "严重级别")
    private String errorLevel;

    /**
     * 异常类名
     */
    @ExcelProperty(value = "异常类名")
    private String errorType;

    /**
     * 业务错误码
     */
    @ExcelProperty(value = "业务错误码")
    private String errorCode;

    /**
     * 错误消息
     */
    @ExcelProperty(value = "错误消息")
    private String errorMessage;

    /**
     * 异常堆栈
     */
    private String errorStack;

    /**
     * 链路追踪ID
     */
    @ExcelProperty(value = "链路追踪ID")
    private String traceId;

    /**
     * 请求URI
     */
    @ExcelProperty(value = "请求URI")
    private String requestUri;

    /**
     * 请求路径模板
     */
    @ExcelProperty(value = "请求模板")
    private String requestPattern;

    /**
     * 请求方法
     */
    @ExcelProperty(value = "请求方法")
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求IP
     */
    @ExcelProperty(value = "请求IP")
    private String requestIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 操作用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 操作用户名
     */
    @ExcelProperty(value = "用户名")
    private String userName;

    /**
     * 所属部门ID
     */
    @ExcelProperty(value = "部门ID")
    private Long deptId;

    /**
     * SQL语句
     */
    private String sqlStatement;

    /**
     * SQL参数
     */
    private String sqlParams;

    /**
     * SQL执行耗时(ms)
     */
    @ExcelProperty(value = "SQL耗时(ms)")
    private Integer sqlDuration;

    /**
     * 业务模块
     */
    @ExcelProperty(value = "业务模块")
    private String moduleName;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String businessType;

    /**
     * 业务关键字
     */
    @ExcelProperty(value = "业务关键字")
    private String businessKey;

    /**
     * 平台类型
     */
    @ExcelProperty(value = "平台类型")
    private String clientType;

    /**
     * 客户端版本
     */
    @ExcelProperty(value = "客户端版本")
    private String clientVersion;

    /**
     * 服务器名称
     */
    @ExcelProperty(value = "服务器名称")
    private String serverName;

    /**
     * 服务器IP
     */
    @ExcelProperty(value = "服务器IP")
    private String serverIp;

    /**
     * 应用版本
     */
    @ExcelProperty(value = "应用版本")
    private String appVersion;

    /**
     * 处理状态(0未处理 1已处理 2已忽略)
     */
    @ExcelProperty(value = "处理状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_error_handle_status")
    private String handleStatus;

    /**
     * 处理人ID
     */
    @ExcelProperty(value = "处理人ID")
    private Long handleBy;

    /**
     * 处理时间
     */
    @ExcelProperty(value = "处理时间")
    private Date handleTime;

    /**
     * 处理备注
     */
    @ExcelProperty(value = "处理备注")
    private String handleRemark;

    /**
     * 相同错误出现次数
     */
    @ExcelProperty(value = "出现次数")
    private Integer occurrenceCount;

    /**
     * 首次出现时间
     */
    @ExcelProperty(value = "首次出现时间")
    private Date firstTime;

    /**
     * 最后出现时间
     */
    @ExcelProperty(value = "最后出现时间")
    private Date lastTime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;
}
