package plus.ruoyi.system.monitor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志表 sys_login_log
 *
 * @author Lion Li
 */

@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "info_id")
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
     * 登录状态
     */
    private String status;

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
     * 提示消息
     */
    private String msg;

    /**
     * 访问时间
     */
    private Date loginTime;

}
