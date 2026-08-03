package plus.ruoyi.common.core.service;

import plus.ruoyi.common.core.domain.dto.PaymentDTO;

import java.util.List;

/**
 * 支付配置服务接口（供通用模块调用）
 *
 * 这个接口定义在 common-core 模块中，供 common-payment 模块调用
 * 避免 common-payment 模块直接依赖 business-base 模块
 *
 * @author 抓蛙师
 */
public interface PaymentService {

    /**
     * 根据商户号获取支付配置
     *
     * @param mchId 商户号
     * @return 支付配置，如果不存在返回null
     */
    PaymentDTO getByMchId(String mchId);

    /**
     * 根据支付类型获取支付配置列表
     *
     * @param type 支付类型（如：wxpay, alipay等）
     * @param tenantId 租户id
     * @return 支付配置列表
     */
    List<PaymentDTO> listPaymentByType(String type, String tenantId);

    /**
     * 根据支付配置ID获取支付配置
     *
     * @param paymentId 支付配置ID
     * @return 支付配置，如果不存在返回null
     */
    PaymentDTO getById(Long paymentId);

    /**
     * 检查商户号是否存在且有效
     *
     * @param mchId 商户号
     * @return true-存在且有效，false-不存在或无效
     */
    boolean existsValidMchId(String mchId);

    /**
     * 获取支付配置总数
     *
     * @return 支付配置总数
     */
    long countPayments();


}
