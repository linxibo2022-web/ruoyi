package plus.ruoyi.common.pay.core.initializer;

import plus.ruoyi.common.pay.config.PayConfig;

import java.util.List;

/**
 * 支付初始化器接口
 *
 * 每种支付方式需要实现自己的初始化器,负责:
 * 1. 加载和验证配置
 * 2. 初始化SDK客户端
 * 3. 注册服务实例
 *
 * @author 抓蛙师
 */
public interface PayInitializer {

    /**
     * 获取支付类型
     *
     * @return 支付类型(wechat/alipay/balance等)
     */
    String getPaymentType();

    /**
     * 初始化支付配置
     *
     * @param configs 支付配置列表(可能有多个租户、多个商户)
     */
    void initConfigs(List<PayConfig> configs);
}
