package plus.ruoyi.system.monitor.domain.vo;

import java.util.Date;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import plus.ruoyi.common.core.dict.DictOperType;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import plus.ruoyi.system.monitor.domain.SysLoginLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;



/**
 * 登录日志视图对象 sys_login_log
 *
 * @author Michelle.Chung
 * @date 2023-02-07
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysLoginLog.class)
public class SysLoginLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问ID
     */
    @ExcelProperty(value = "序号")
    private Long infoId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private String userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 设备类型
     */
    @ExcelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 登录状态
     */
    @ExcelProperty(value = "登录状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictOperType.DICT_TYPE)
    private String status;

    /**
     * 登录IP地址
     */
    @ExcelProperty(value = "登录地址")
    private String ipaddr;

    /**
     * 登录地点
     */
    @ExcelProperty(value = "登录地点")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @ExcelProperty(value = "浏览器")
    private String browser;

    /**
     * 操作系统
     */
    @ExcelProperty(value = "操作系统")
    private String os;


    /**
     * 提示消息
     */
    @ExcelProperty(value = "提示消息")
    private String msg;

    /**
     * 访问时间
     */
    @ExcelProperty(value = "访问时间")
    private Date loginTime;


}
