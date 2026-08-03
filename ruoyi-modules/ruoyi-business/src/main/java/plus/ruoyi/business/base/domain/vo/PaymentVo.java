package plus.ruoyi.business.base.domain.vo;

import java.util.Date;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMappers;
import plus.ruoyi.business.base.domain.Payment;
import plus.ruoyi.business.base.domain.bo.PaymentBo;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.excel.annotation.ExcelDictFormat;
import plus.ruoyi.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 支付配置视图对象 b_payment
 *
 * @author 抓蛙师
 * @date 2025-05-14
 */
@Data
@ExcelIgnoreUnannotated
@AutoMappers({
    @AutoMapper(target = Payment.class),
    @AutoMapper(target = PaymentBo.class)
})
public class PaymentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支付配置id
     */
    @ExcelProperty(value = "支付配置id")
    private Long id;

    /**
     * 商户类型
     */
    @ExcelProperty(value = "商户类型")
    private String type;

    /**
     * 商户名称
     */
    @ExcelProperty(value = "商户名称")
    private String mchName;

    /**
     * 商户号
     */
    @ExcelProperty(value = "商户号")
    private String mchId;

    /**
     * 商户密钥
     */
    @ExcelProperty(value = "商户密钥")
    private String mchKey;

    /**
     * APIv3密钥
     */
    @ExcelProperty(value = "APIv3密钥")
    private String apiV3Key;

    /**
     * 证书路径
     */
    @ExcelProperty(value = "证书路径")
    private String certPath;

    /**
     * 密钥路径
     */
    @ExcelProperty(value = "密钥路径")
    private String keyPath;

    /**
     * 平台证书路径
     */
    @ExcelProperty(value = "平台证书路径")
    private String platformCertPath;

    /**
     * 证书序列号
     */
    @ExcelProperty(value = "证书序列号")
    private String certSerialNo;

    /**
     * 微信支付公钥ID(V3公钥模式专用，格式 PUB_KEY_ID_xxx)
     */
    @ExcelProperty(value = "微信支付公钥ID")
    private String publicKeyId;

    /**
     * dev证书路径
     */
    @ExcelProperty(value = "dev证书路径")
    private String devCertPath;

    /**
     * dev密钥路径
     */
    @ExcelProperty(value = "dev密钥路径")
    private String devKeyPath;

    /**
     * dev平台证书路径
     */
    @ExcelProperty(value = "dev平台证书路径")
    private String devPlatformCertPath;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = DictEnableStatus.DICT_TYPE)
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
