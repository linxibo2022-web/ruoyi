package plus.ruoyi.common.pay.wechat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.pay.config.PayConfigManager;
import plus.ruoyi.common.pay.wechat.strategy.PayVersionSelector;
import plus.ruoyi.common.pay.wechat.handler.WxPayHandler;
import plus.ruoyi.common.pay.wechat.handler.v2.WxPayV2Strategy;
import plus.ruoyi.common.pay.wechat.handler.v3.WxPayV3Strategy;
import plus.ruoyi.common.pay.wechat.initializer.WxPayInitializer;
import plus.ruoyi.common.pay.wechat.registry.WxPayServiceRegistry;

/**
 * 微信支付自动配置类
 * <p>
 * 负责注册微信支付相关组件:
 * - WxPayServiceRegistry: 微信支付服务注册表
 * - WxPayConfigBuilder: 微信支付配置构建器
 * - PayVersionSelector: 支付版本选择器
 * - WxPayV2Strategy: 微信支付v2策略
 * - WxPayV3Strategy: 微信支付v3策略
 * - WxPayHandler: 微信支付处理器
 * - WxPayInitializer: 微信支付初始化器
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class WechatPayAutoConfiguration {

    /**
     * 注册微信支付服务注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayServiceRegistry wxPayServiceRegistry() {
        log.info("创建微信支付服务注册表 WxPayServiceRegistry");
        return new WxPayServiceRegistry();
    }

    /**
     * 注册微信支付配置构建器
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayConfigBuilder wxPayConfigBuilder() {
        log.info("创建微信支付配置构建器 WxPayConfigBuilder");
        return new WxPayConfigBuilder();
    }

    /**
     * 注册支付版本选择器
     */
    @Bean
    @ConditionalOnMissingBean
    public PayVersionSelector payVersionSelector() {
        log.info("创建支付版本选择器 PayVersionSelector");
        return new PayVersionSelector();
    }

    /**
     * 注册微信支付 v2 策略
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayV2Strategy wxPayV2Strategy(
        WxPayServiceRegistry registry,
        AppProperties appProperties) {
        log.info("创建微信支付 v2 策略 WxPayV2Strategy");
        return new WxPayV2Strategy(registry, appProperties);
    }

    /**
     * 注册微信支付 v3 策略
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayV3Strategy wxPayV3Strategy(
        WxPayServiceRegistry registry,
        AppProperties appProperties) {
        log.info("创建微信支付 v3 策略 WxPayV3Strategy");
        return new WxPayV3Strategy(registry, appProperties);
    }

    /**
     * 注册微信支付处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayHandler wxPayHandler(
        PayConfigManager configManager,
        PayVersionSelector versionSelector,
        WxPayV2Strategy v2Strategy,
        WxPayV3Strategy v3Strategy) {
        log.info("创建微信支付处理器 WxPayHandler");
        return new WxPayHandler(configManager, versionSelector, v2Strategy, v3Strategy);
    }

    /**
     * 注册微信支付初始化器
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayInitializer wxPayInitializer(
        WxPayConfigBuilder configBuilder,
        WxPayServiceRegistry serviceRegistry) {
        log.info("创建微信支付初始化器 WxPayInitializer");
        return new WxPayInitializer(configBuilder, serviceRegistry);
    }
}
