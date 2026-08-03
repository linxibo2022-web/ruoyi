package plus.ruoyi.common.core.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平台配置变更消息DTO
 * <p>
 * 用于集群部署时通过 Redis 发布订阅同步平台配置变更到所有节点
 *
 * @author 抓蛙师
 */
@Data
public class PlatformConfigChangeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 操作类型：add-添加配置, remove-移除配置, refresh_pay-刷新支付配置
     */
    private String action;

    /**
     * 应用ID
     */
    private String appid;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 发送节点标识（用于防止发送节点重复处理）
     */
    private String sourceNodeId;

    /**
     * 添加配置
     */
    public static final String ACTION_ADD = "add";

    /**
     * 移除配置
     */
    public static final String ACTION_REMOVE = "remove";

    /**
     * 刷新支付配置
     */
    public static final String ACTION_REFRESH_PAY = "refresh_pay";
}
