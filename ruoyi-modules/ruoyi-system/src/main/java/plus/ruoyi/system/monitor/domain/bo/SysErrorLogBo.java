package plus.ruoyi.system.monitor.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.core.validate.EditGroup;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.system.monitor.domain.SysErrorLog;
import plus.ruoyi.system.monitor.domain.vo.SysErrorLogVo;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 错误日志业务对象
 *
 * @author 抓蛙师
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = SysErrorLog.class, reverseConvertGenerate = false),
    @AutoMapper(target = SysErrorLogVo.class)
})
public class SysErrorLogBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 主键ID集合
     */
    private List<Long> ids;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 严重级别(ERROR/WARN/FATAL)
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
     * 请求IP
     */
    private String requestIp;

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
     * 业务模块
     */
    private String moduleName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务关键字
     */
    private String businessKey;

    /**
     * 平台类型
     */
    private String clientType;

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
}
