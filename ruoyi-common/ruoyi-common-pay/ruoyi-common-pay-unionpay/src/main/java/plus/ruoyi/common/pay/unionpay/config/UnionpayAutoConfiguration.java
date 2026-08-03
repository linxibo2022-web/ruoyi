package plus.ruoyi.common.pay.unionpay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.pay.config.PayConfigManager;
import plus.ruoyi.common.pay.unionpay.handler.UnionpayHandler;
import plus.ruoyi.common.pay.unionpay.initializer.UnionpayInitializer;
import plus.ruoyi.common.pay.unionpay.registry.UnionpayClientRegistry;
import plus.ruoyi.common.pay.unionpay.strategy.UnionpayStrategy;

/**
 * 银联支付自动配置类
 *
 * 负责注册银联支付相关组件:
 * - UnionpayClientRegistry: 银联客户端注册表
 * - UnionpayStrategy: 银联支付策略
 * - UnionpayHandler: 银联支付处理器
 * - UnionpayInitializer: 银联支付初始化器
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class UnionpayAutoConfiguration {

    /**
     * 显式构造函数：打印配置信息（早期执行）
     */
    public UnionpayAutoConfiguration() {
        log.info("========================================");
        log.info("银联支付模块初始化开始");
        log.info("支持功能: 二维码支付、WAP支付、APP支付、统一支付接口");
        log.info("认证方式: RSA签名、SM2国密签名");
        log.info("========================================");
    }

    /**
     * 注册银联客户端注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public UnionpayClientRegistry unionpayClientRegistry() {
        log.info("创建银联客户端注册表 UnionpayClientRegistry");
        return new UnionpayClientRegistry();
    }

    /**
     * 注册银联支付策略
     */
    @Bean
    @ConditionalOnMissingBean
    public UnionpayStrategy unionpayStrategy(AppProperties appProperties) {
        log.info("创建银联支付策略 UnionpayStrategy");
        return new UnionpayStrategy(appProperties);
    }

    /**
     * 注册银联支付处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public UnionpayHandler unionpayHandler(
        PayConfigManager configManager,
        UnionpayStrategy unionpayStrategy) {
        log.info("创建银联支付处理器 UnionpayHandler");
        return new UnionpayHandler(configManager, unionpayStrategy);
    }

    /**
     * 注册银联支付初始化器
     */
    @Bean
    @ConditionalOnMissingBean
    public UnionpayInitializer unionpayInitializer(UnionpayClientRegistry clientRegistry) {
        log.info("创建银联支付初始化器 UnionpayInitializer");
        return new UnionpayInitializer(clientRegistry);
    }
}
