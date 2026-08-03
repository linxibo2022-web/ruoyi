package plus.ruoyi.common.pay.alipay.initializer;

import com.alipay.api.AlipayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.alipay.config.AlipayClientBuilder;
import plus.ruoyi.common.pay.alipay.registry.AlipayClientRegistry;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.core.initializer.PayInitializer;

import java.util.List;

/**
 * 支付宝初始化器
 *
 * 负责创建和注册 AlipayClient
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class AlipayInitializer implements PayInitializer {

    private final AlipayClientRegistry registry;

    @Override
    public String getPaymentType() {
        return DictPaymentMethod.ALIPAY.getValue();
    }

    @Override
    public void initConfigs(List<PayConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.warn("支付宝配置列表为空,跳过初始化");
            return;
        }

        log.info("开始初始化支付宝配置,共{}个", configs.size());

        for (PayConfig config : configs) {
            try {
                initialize(config);
            } catch (Exception e) {
                log.error("支付宝配置初始化失败,继续处理下一个: appid={}", config.getAppid(), e);
            }
        }

        log.info("支付宝配置初始化完成,成功注册{}个实例", getRegisteredCount());
    }

    /**
     * 初始化单个配置
     */
    private void initialize(PayConfig config) {
        try {
            log.debug("初始化支付宝配置: appid={}", config.getAppid());

            // 构建 AlipayClient
            AlipayClient alipayClient = AlipayClientBuilder.build(config);

            // 注册到注册器
            registry.register(config.getAppid(), alipayClient);

            log.info("支付宝配置初始化成功: appid={}, mchId={}",
                config.getAppid(), config.getMchId());

        } catch (Exception e) {
            log.error("支付宝配置初始化失败: appid={}, error={}",
                config.getAppid(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 销毁配置
     */
    public void destroy(String appid) {
        try {
            registry.unregister(appid);
            log.info("支付宝配置已销毁: appid={}", appid);
        } catch (Exception e) {
            log.error("销毁支付宝配置失败: appid={}, error={}",
                appid, e.getMessage(), e);
        }
    }

    /**
     * 获取已注册实例数量
     */
    private int getRegisteredCount() {
        return registry.size();
    }
}
