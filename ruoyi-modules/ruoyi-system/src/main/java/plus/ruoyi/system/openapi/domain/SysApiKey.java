package plus.ruoyi.system.openapi.domain;

import io.github.linpeilie.annotations.AutoMapper;
import plus.ruoyi.common.core.domain.vo.OpenApiVo;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * API密钥对象 sys_api_key
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_api_key")
@AutoMapper(target = OpenApiVo.class)
public class SysApiKey extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API密钥ID
     */
    @TableId(value = "id")
    private Long id;

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

    /**
     * 备注
     */
    private String remark;
}
