package plus.ruoyi.common.pay.wechat.strategy;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.wechat.enums.WxPayVersion;

/**
 * 支付版本选择器
 *
 * 智能选择使用 v2 还是 v3 API
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
public class PayVersionSelector {

    /**
     * 选择微信支付版本
     *
     * 判断逻辑:
     * 1. 有 apiV3Key + 证书 → 使用v3
     * 2. 只有 mchKey → 使用v2
     * 3. 两者都有 → 优先v3
     *
     * @param config 支付配置
     * @return 支付版本
     */
    public WxPayVersion selectWxPayVersion(PayConfig config) {
        if (config == null) {
            log.warn("配置为空,默认使用v2");
            return WxPayVersion.V2;
        }

        // 判断是否有v3配置
        boolean hasV3Config = config.hasCompleteV3Config();

        if (hasV3Config) {
            log.debug("检测到v3配置,使用微信支付v3: appid={}", config.getAppid());
            return WxPayVersion.V3;
        } else {
            log.debug("未检测到v3配置,使用微信支付v2: appid={}", config.getAppid());
            return WxPayVersion.V2;
        }
    }

    /**
     * 是否应该使用v3
     */
    public boolean shouldUseV3(PayConfig config) {
        return selectWxPayVersion(config) == WxPayVersion.V3;
    }

    /**
     * 是否应该使用v2
     */
    public boolean shouldUseV2(PayConfig config) {
        return selectWxPayVersion(config) == WxPayVersion.V2;
    }
}
