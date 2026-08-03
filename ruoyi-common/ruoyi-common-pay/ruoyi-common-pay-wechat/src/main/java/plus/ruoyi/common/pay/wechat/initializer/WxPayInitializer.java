package plus.ruoyi.common.pay.wechat.initializer;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.core.initializer.PayInitializer;
import plus.ruoyi.common.pay.wechat.config.WxPayConfigBuilder;
import plus.ruoyi.common.pay.wechat.registry.WxPayServiceRegistry;

import java.util.List;

/**
 * 微信支付初始化器
 *
 * 职责:
 * 1. 启动时加载微信支付配置
 * 2. 创建 WxPayService 实例
 * 3. 注册到服务注册中心
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
@ConditionalOnClass(WxPayService.class)
@RequiredArgsConstructor
public class WxPayInitializer implements PayInitializer {

    private final WxPayConfigBuilder configBuilder;
    private final WxPayServiceRegistry registry;

    @Override
    public String getPaymentType() {
        return DictPaymentMethod.WECHAT.getValue();
    }

    @Override
    public void initConfigs(List<PayConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.info("微信支付无可用配置");
            return;
        }

        // 过滤出微信支付配置
        List<PayConfig> wxConfigs = configs.stream()
            .filter(PayConfig::isValid)
            .filter(config -> DictPaymentMethod.WECHAT.equals(config.getPaymentMethod()))
            .toList();

        if (wxConfigs.isEmpty()) {
            log.warn("未找到有效的微信支付配置");
            return;
        }

        log.debug("开始初始化微信支付配置,共 {} 个", wxConfigs.size());

        int successCount = 0;

        for (PayConfig config : wxConfigs) {
            try {
                // 1. 构建 WxPayConfig
                WxPayConfig wxConfig = configBuilder.build(config);

                // 2. 创建 WxPayService
                WxPayService wxPayService = new WxPayServiceImpl();
                wxPayService.setConfig(wxConfig);

                // 3. 注册到注册中心
                registry.register(config.getAppid(), wxPayService);

                successCount++;

                log.debug("微信支付配置初始化成功: 商户[{}] appid[{}] mchId[{}] v3={}",
                    config.getMchName(),
                    config.getAppid(),
                    config.getMchId(),
                    config.hasApiV3Config() ? "是" : "否");

            } catch (Exception e) {
                log.error("微信支付配置初始化失败: 商户[{}] appid[{}] 错误: {}",
                    config.getMchName(),
                    config.getAppid(),
                    e.getMessage(), e);
            }
        }

        if (successCount > 0) {
            log.info("微信支付初始化完成: 成功 {}/{}", successCount, wxConfigs.size());
        }
    }
}
