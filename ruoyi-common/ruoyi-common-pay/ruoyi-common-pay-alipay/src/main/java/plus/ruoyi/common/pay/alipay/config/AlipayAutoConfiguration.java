package plus.ruoyi.common.pay.alipay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.pay.config.PayConfigManager;
import plus.ruoyi.common.pay.alipay.handler.AlipayHandler;
import plus.ruoyi.common.pay.alipay.handler.AlipayStrategy;
import plus.ruoyi.common.pay.alipay.initializer.AlipayInitializer;
import plus.ruoyi.common.pay.alipay.registry.AlipayClientRegistry;

/**
 * 支付宝支付自动配置类
 * <p>
 * 负责注册支付宝支付相关组件:
 * - AlipayClientRegistry: 支付宝客户端注册表
 * - AlipayClientBuilder: 支付宝客户端构建器
 * - AlipayStrategy: 支付宝支付策略
 * - AlipayHandler: 支付宝支付处理器
 * - AlipayInitializer: 支付宝支付初始化器
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class AlipayAutoConfiguration {

    /**
     * 注册支付宝客户端注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayClientRegistry alipayClientRegistry() {
        log.info("创建支付宝客户端注册表 AlipayClientRegistry");
        return new AlipayClientRegistry();
    }

    /**
     * 注册支付宝客户端构建器
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayClientBuilder alipayClientBuilder() {
        log.info("创建支付宝客户端构建器 AlipayClientBuilder");
        return new AlipayClientBuilder();
    }

    /**
     * 注册支付宝支付策略
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayStrategy alipayStrategy(
        AlipayClientRegistry registry,
        AppProperties appProperties) {
        log.info("创建支付宝支付策略 AlipayStrategy");
        return new AlipayStrategy(registry, appProperties);
    }

    /**
     * 注册支付宝支付处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayHandler alipayHandler(
        PayConfigManager configManager,
        AlipayStrategy alipayStrategy) {
        log.info("创建支付宝支付处理器 AlipayHandler");
        return new AlipayHandler(configManager, alipayStrategy);
    }

    /**
     * 注册支付宝支付初始化器
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayInitializer alipayInitializer(AlipayClientRegistry clientRegistry) {
        log.info("创建支付宝支付初始化器 AlipayInitializer");
        return new AlipayInitializer(clientRegistry);
    }
}
