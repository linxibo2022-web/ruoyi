package plus.ruoyi.business.base.domain;

import io.github.linpeilie.annotations.AutoMapper;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 平台配置对象 b_platform
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_platform")
@AutoMapper(target = PlatformDTO.class)
public class Platform extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台配置id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 平台类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * appid
     */
    private String appid;

    /**
     * 密钥
     */
    private String secret;

    /**
     * 接口token
     */
    private String token;

    /**
     * 加密密钥
     */
    private String aeskey;

    /**
     * 关联支付配置
     */
    private String paymentIds;

    /**
     * 模板配置
     */
    private String templateConfigs;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除
     */
    @TableLogic
    private String isDeleted;


}
