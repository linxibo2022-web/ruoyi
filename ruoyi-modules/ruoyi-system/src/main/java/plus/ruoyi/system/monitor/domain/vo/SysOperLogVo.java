package plus.ruoyi.system.monitor.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.core.dict.DictOperResult;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.system.monitor.domain.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 操作日志视图对象 sys_oper_log
 *
 * @author Michelle.Chung
 * @date 2023-02-07
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysOperLog.class)
public class SysOperLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    @ExcelProperty(value = "日志主键")
    private Long operId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 模块标题
     */
    @ExcelProperty(value = "操作模块")
    private String title;

    /**
     * 操作类型
     */
    @ExcelProperty(value = "操作类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictOperType.DICT_TYPE)
    private String operType;

    /**
     * 方法名称
     */
    @ExcelProperty(value = "请求方法")
    private String method;

    /**
     * 请求方式
     */
    @ExcelProperty(value = "请求方式")
    private String requestMethod;

    /**
     * 操作用户类别
     */
    @ExcelProperty(value = "操作类别")
    private String operatorType;

    /**
     * 操作人员
     */
    @ExcelProperty(value = "操作人员")
    private String operName;

    /**
     * 部门名称
     */
    @ExcelProperty(value = "部门名称")
    private String deptName;

    /**
     * 请求URL
     */
    @ExcelProperty(value = "请求地址")
    private String operUrl;

    /**
     * 主机地址
     */
    @ExcelProperty(value = "操作地址")
    private String operIp;

    /**
     * 操作地点
     */
    @ExcelProperty(value = "操作地点")
    private String operLocation;

    /**
     * 请求参数
     */
    @ExcelProperty(value = "请求参数")
    private String operParam;

    /**
     * 返回参数
     */
    @ExcelProperty(value = "返回参数")
    private String jsonResult;

    /**
     * 操作结果（0正常 1异常）
     */
    @ExcelProperty(value = "操作结果", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictOperResult.DICT_TYPE)
    private String status;

    /**
     * 错误消息
     */
    @ExcelProperty(value = "错误消息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @ExcelProperty(value = "操作时间")
    private Date operTime;

    /**
     * 消耗时间
     */
    @ExcelProperty(value = "消耗时间")
    private Long costTime;
}
