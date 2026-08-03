package plus.ruoyi.common.pay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.service.PaymentService;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.pay.core.handler.PayHandler;
import plus.ruoyi.common.pay.core.initializer.PayInitializer;
import plus.ruoyi.common.pay.initializer.PayConfigInitializer;
import plus.ruoyi.common.pay.service.PayService;
import plus.ruoyi.common.pay.service.impl.PayServiceImpl;

import java.util.List;

/**
 * Pay核心模块自动配置类(多租户版本)
 * <p>
 * 负责注册支付核心组件:
 * - PayConfigManager: 支付配置管理器
 * - PayService: 统一支付服务
 * - PayConfigInitializer: 支付配置初始化器
 * <p>
 * 只有当支付模块启用时才加载此配置
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class PayCoreAutoConfiguration {

    /**
     * 注册支付配置管理器
     * <p>
     * 使用多租户Map结构管理多平台、多支付方式的配置
     * 配置key格式：{tenantId}:{paymentMethod}:{appid}
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public PayConfigManager payConfigManager() {
        log.info("创建支付配置管理器 PayConfigManager");
        return new PayConfigManager();
    }

    /**
     * 注册支付服务
     * <p>
     * 统一的支付管理服务，自动注入所有 PayHandler 实现
     * 支持微信支付、支付宝、余额支付等多种支付方式
     * </p>
     *
     * @param handlers 所有支付处理器实现
     */
    @Bean
    @ConditionalOnMissingBean
    public PayService payService(List<PayHandler> handlers) {
        log.info("创建支付服务 PayService，处理器数量: {}", handlers.size());
        return new PayServiceImpl(handlers);
    }

    /**
     * 注册支付配置初始化器
     * <p>
     * 应用启动时自动从数据库加载支付配置
     * 支持多租户、多平台、多支付方式的动态配置管理
     * </p>
     *
     * @param appProperties    应用配置属性
     * @param paymentService   支付配置服务
     * @param platformService  平台配置服务
     * @param payService      支付服务
     * @param configManager    支付配置管理器
     * @param payInitializers  所有支付初始化器实现
     */
    @Bean
    @ConditionalOnMissingBean
    public PayConfigInitializer payConfigInitializer(
        AppProperties appProperties,
        PaymentService paymentService,
        PlatformService platformService,
        PayService payService,
        PayConfigManager configManager,
        List<PayInitializer> payInitializers) {
        log.info("创建支付配置初始化器 PayConfigInitializer，初始化器数量: {}", payInitializers.size());
        return new PayConfigInitializer(
            appProperties,
            paymentService,
            platformService,
            payService,
            configManager,
            payInitializers
        );
    }
}
