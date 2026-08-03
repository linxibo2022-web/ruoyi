package plus.ruoyi.common.core.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 开放API密钥视图对象
 *
 * @author 抓蛙师
 */
@Data
public class OpenApiVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API密钥ID
     */
    private Long id;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * AppKey(公开)
     */
    private String appKey;

    /**
     * AppSecret
     */
    private String appSecret;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 关联用户名称
     */
    private String userName;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 状态(0停用 1正常)
     */
    private String status;

    /**
     * IP白名单,逗号分隔
     */
    private String whiteIps;

    /**
     * 调用次数
     */
    private Long callCount;

    /**
     * 最后调用时间
     */
    private Date lastCallTime;

}
