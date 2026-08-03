package plus.ruoyi.business.base.domain.bo;

import plus.ruoyi.business.base.domain.Platform;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 平台配置业务对象 b_platform
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Platform.class, reverseConvertGenerate = false)
public class PlatformBo extends BaseEntity {

    /**
     * 平台配置id
     */
    @NotNull(message = "平台配置id不能为空", groups = { EditGroup.class })
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
     * 是否仅更新订阅配置
     * 如果为 true，则不触发平台配置和支付配置的刷新
     */
    private Boolean onlyTemplateUpdate;
}
