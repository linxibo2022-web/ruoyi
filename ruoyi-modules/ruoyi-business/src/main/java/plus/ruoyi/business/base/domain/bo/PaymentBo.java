package plus.ruoyi.business.base.domain.bo;

import plus.ruoyi.business.base.domain.Payment;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 支付配置业务对象 b_payment
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Payment.class, reverseConvertGenerate = false)
public class PaymentBo extends BaseEntity {

    /**
     * 支付配置id
     */
    @NotNull(message = "支付配置id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商户类型
     */
    private String type;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户密钥
     */
    private String mchKey;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * 证书路径
     */
    private String certPath;

    /**
     * 密钥路径
     */
    private String keyPath;

    /**
     * 平台证书路径
     */
    private String platformCertPath;

    /**
     * 证书序列号
     */
    private String certSerialNo;

    /**
     * 微信支付公钥ID(V3公钥模式专用，格式 PUB_KEY_ID_xxx，从微信商户平台获取，与证书序列号不同)
     */
    private String publicKeyId;

    /**
     * p12证书路径(V2退款专用)
     */
    private String p12CertPath;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;


}
