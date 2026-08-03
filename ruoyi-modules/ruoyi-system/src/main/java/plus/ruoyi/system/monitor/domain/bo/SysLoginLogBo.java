package plus.ruoyi.system.monitor.domain.bo;

import plus.ruoyi.system.monitor.domain.SysLoginLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录日志业务对象 sys_LoginLog
 *
 * @author Michelle.Chung
 */

@Data
@AutoMapper(target = SysLoginLog.class, reverseConvertGenerate = false)
public class SysLoginLogBo {

    /**
     * 访问ID
     */
    private Long infoId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态
     */
    private String status;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 访问时间
     */
    private Date loginTime;

    /**
     * 搜索值
     */
    private String searchValue;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();


}
