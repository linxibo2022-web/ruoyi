package plus.ruoyi.system.openapi.domain.bo;

import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.system.openapi.domain.SysApiKey;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.AddGroup;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import plus.ruoyi.system.openapi.domain.vo.SysApiKeyVo;

import java.util.Date;

/**
 * API密钥业务对象 sys_api_key
 *
 * @author 抓蛙师
 * @date 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMappers({
    @AutoMapper(target = SysApiKey.class, reverseConvertGenerate = false),
    @AutoMapper(target = SysApiKeyVo.class)
})
public class SysApiKeyBo extends BaseEntity {

    /**
     * API密钥ID
     */
    @NotNull(message = "API密钥ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空", groups = { AddGroup.class, EditGroup.class })
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
