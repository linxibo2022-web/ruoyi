package plus.ruoyi.business.base.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import plus.ruoyi.common.core.dict.DictPlatformType;

/**
 * 手机号绑定业务对象
 *
 * @author 抓蛙师
 * @date 2025/8/21
 */
@Data
public class PhoneBindBo {

    /**
     * 平台授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;

    /**
     * 平台类型（mp-weixin/mp-alipay/mp-official-account等）
     */
    private String platform = DictPlatformType.MP_WEIXIN.getValue();
}
