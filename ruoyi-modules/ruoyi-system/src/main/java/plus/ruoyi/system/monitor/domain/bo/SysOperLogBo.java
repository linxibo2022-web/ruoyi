package plus.ruoyi.system.monitor.domain.bo;

import plus.ruoyi.common.log.event.OperLogEvent;
import plus.ruoyi.system.monitor.domain.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志业务对象 sys_oper_log
 *
 * @author Michelle.Chung
 * @date 2023-02-07
 */

@Data
@AutoMappers({
    @AutoMapper(target = SysOperLog.class, reverseConvertGenerate = false),
    @AutoMapper(target = OperLogEvent.class)
})
public class SysOperLogBo {

    /**
     * 日志主键
     */
    private Long operId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 操作类型
     */
    private String operType;

    /**
     * 方法名称
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
     * 请求URL
     */
    private String operUrl;

    /**
     * 主机地址
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

    /**
     * 搜索值
     */
    private String searchValue;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
