package plus.ruoyi.common.pay.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.domain.dto.PaymentDTO;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.exception.ServiceException;
import plus.ruoyi.common.core.service.PaymentService;
import plus.ruoyi.common.core.service.PlatformService;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.config.PayConfigManager;
import plus.ruoyi.common.pay.core.initializer.PayInitializer;
import plus.ruoyi.common.pay.service.PayService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pay 支付配置初始化器
 * <p>
 * 应用启动时自动从数据库加载支付配置并初始化各支付平台
 * 支持多租户、多平台、多支付方式的动态配置管理
 * </p>
 *
 * @author 抓蛙师
 */
@Slf4j
public class PayConfigInitializer implements ApplicationRunner {

    private final AppProperties appProperties;
    private final PaymentService paymentService;
    private final PlatformService platformService;
    private final PayService payService;
    private final PayConfigManager configManager;
    private final List<PayInitializer> payInitializers;

    /**
     * 构造函数
     */
    public PayConfigInitializer(AppProperties appProperties,
                                PaymentService paymentService,
                                PlatformService platformService,
                                PayService payService,
                                PayConfigManager configManager,
                                List<PayInitializer> payInitializers) {
        this.appProperties = appProperties;
        this.paymentService = paymentService;
        this.platformService = platformService;
        this.payService = payService;
        this.configManager = configManager;
        this.payInitializers = payInitializers;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 应用启动时初始化所有租户的支付配置
        initAllTenants();
    }

    /**
     * 初始化所有租户的支付配置
     */
    private void initAllTenants() {
        log.info("开始初始化所有租户的支付系统...");

        try {

            // 获取所有租户并初始化
            Set<String> tenantIds = getAllTenantIds();
            if (tenantIds.isEmpty()) {
                log.warn("未找到任何租户");
                return;
            }

            log.info("找到 {} 个租户需要初始化支付配置", tenantIds.size());

            for (String tenantId : tenantIds) {
                try {
                    initByTenant(tenantId);
                } catch (Exception e) {
                    log.error("初始化租户[{}]的支付配置失败: {}", tenantId, e.getMessage(), e);
                }
            }

            log.info("所有租户的支付系统初始化完成");

        } catch (Exception e) {
            log.error("支付系统初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 按租户初始化支付配置
     *
     * @param tenantId 租户ID
     */
    public void initByTenant(String tenantId) {
        log.info("开始初始化租户[{}]的支付系统...", tenantId);

        try {
            // 1. 清理该租户的旧配置
            configManager.clearConfigsByTenant(tenantId);

            // 2. 在指定租户下查询配置
            List<PayConfig> allConfigs = buildPaymentConfigsByTenant(tenantId);

            if (allConfigs.isEmpty()) {
                log.warn("租户[{}]未找到任何支付配置，支付功能将不可用", tenantId);
                return;
            }

            // 3. 按支付方式分组并初始化
            initConfigsByPaymentMethod(tenantId, allConfigs);

            // 4. 注册配置到管理器
            registerConfigs(allConfigs);

            // 5. 输出初始化结果
            outputInitializationResult(tenantId);

            log.info("租户[{}]的支付系统初始化完成", tenantId);

        } catch (Exception e) {
            log.error("租户[{}]的支付系统初始化失败: {}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * 获取所有需要初始化的租户ID
     */
    private Set<String> getAllTenantIds() {
        Set<String> tenantIds = new HashSet<>();

        try {
            // 从平台配置获取租户ID
            List<PlatformDTO> platforms = platformService.listPlatformsByType(null, null);
            if (platforms != null) {
                platforms.stream()
                    .map(PlatformDTO::getTenantId)
                    .filter(Objects::nonNull)
                    .forEach(tenantIds::add);
            }

            // 从支付配置获取租户ID
            List<PaymentDTO> payments = paymentService.listPaymentByType(null, null);
            if (payments != null) {
                payments.stream()
                    .map(PaymentDTO::getTenantId)
                    .filter(Objects::nonNull)
                    .forEach(tenantIds::add);
            }
        } catch (Exception e) {
            log.error("获取租户ID列表失败: {}", e.getMessage(), e);
        }

        return tenantIds;
    }

    /**
     * 构建支付配置列表
     *
     * @param tenantId 租户id
     */
    private List<PayConfig> buildPaymentConfigsByTenant(String tenantId) {
        List<PayConfig> configs = new ArrayList<>();

        try {
            // 获取租户的所有有效的平台配置
            List<PlatformDTO> platformList = platformService.listPlatformsByType(null, tenantId);
            if (platformList == null || platformList.isEmpty()) {
                log.warn("未找到启用的平台配置");
                return configs;
            }

            // 获取租户的所有有效的支付配置，按ID分组
            List<PaymentDTO> paymentList = paymentService.listPaymentByType(null, tenantId);
            if (paymentList == null || paymentList.isEmpty()) {
                log.warn("未找到启用的支付配置");
                return configs;
            }

            Map<Long, PaymentDTO> paymentMap = paymentList.stream()
                .collect(Collectors.toMap(PaymentDTO::getId, p -> p));

            // 为每个平台构建支付配置
            for (PlatformDTO platform : platformList) {
                List<Long> paymentIds = platform.getPaymentIdList();
                if (paymentIds.isEmpty()) {
                    log.warn("平台[{}]未配置支付方式", platform.getName());
                    continue;
                }

                for (Long paymentId : paymentIds) {
                    PaymentDTO payment = paymentMap.get(paymentId);
                    if (payment == null) {
                        log.warn("平台[{}]配置的支付ID[{}]不存在", platform.getName(), paymentId);
                        continue;
                    }

                    try {
                        PayConfig config = PayConfig.from(payment, platform);
                        configs.add(config);

                        log.debug("创建支付配置: 平台[{}] 支付[{}] 方式[{}] appid[{}]",
                            platform.getName(),
                            payment.getMchName(),
                            payment.getType(),
                            platform.getAppid());

                    } catch (Exception e) {
                        log.error("创建支付配置失败: 平台[{}] 支付[{}] 错误: {}",
                            platform.getName(), payment.getMchName(), e.getMessage());
                    }
                }
            }

            if (configs.isEmpty()) {
                log.debug("未构建任何支付配置");
            } else {
                log.debug("构建支付配置完成，共 {} 个配置", configs.size());
            }

        } catch (Exception e) {
            log.error("构建支付配置失败: {}", e.getMessage(), e);
        }

        return configs;
    }

    /**
     * 按支付方式分组并初始化
     */
    private void initConfigsByPaymentMethod(String tenantId, List<PayConfig> allConfigs) {
        // 按支付方式分组配置
        Map<String, List<PayConfig>> configsByPaymentMethod = allConfigs.stream()
            .collect(Collectors.groupingBy(config -> config.getPaymentMethod().getValue()));

        log.debug("租户[{}]按支付方式分组的配置: {}", tenantId,
            configsByPaymentMethod.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ))
        );

        // 为每种支付方式执行初始化
        for (PayInitializer initializer : payInitializers) {

            String paymentType = initializer.getPaymentType();
            List<PayConfig> typeConfigs = configsByPaymentMethod.getOrDefault(
                paymentType.toLowerCase(), Collections.emptyList());

            if (typeConfigs.isEmpty()) {
                log.debug("租户[{}]的{}支付无可用配置，跳过初始化", tenantId, paymentType);
                continue;
            }

            try {
                log.debug("开始初始化租户[{}]的{}支付配置，共 {} 个", tenantId, paymentType, typeConfigs.size());
                initializer.initConfigs(typeConfigs);
                log.debug("租户[{}]的{}支付配置初始化完成", tenantId, paymentType);

            } catch (Exception e) {
                log.error("租户[{}]的{}支付配置初始化失败: {}", tenantId, paymentType, e.getMessage(), e);
            }
        }
    }

    /**
     * 注册配置到管理器
     */
    private void registerConfigs(List<PayConfig> allConfigs) {
        for (PayConfig config : allConfigs) {
            configManager.registerConfig(config);
        }

        log.debug("配置注册完成，已注册到配置管理器，共 {} 个配置", allConfigs.size());
    }

    /**
     * 输出初始化结果
     */
    private void outputInitializationResult(String tenantId) {
        try {
            // 输出配置统计（合并为一行）
            Map<String, Integer> configStats = configManager.getConfigStats(tenantId);
            if (!configStats.isEmpty()) {
                String statsStr = configStats.entrySet().stream()
                    .map(entry -> String.format("%s(%d)", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(", "));
                log.info("租户[{}]支付配置: {}", tenantId, statsStr);
            }

            // 输出详细配置信息（调试级别）
            if (log.isDebugEnabled()) {
                // 输出支持的支付方式
                List<DictPaymentMethod> supportedMethods = payService.getSupportedPaymentMethods();
                if (!supportedMethods.isEmpty()) {
                    String methodsStr = supportedMethods.stream()
                        .map(method -> String.format("%s(%s)", method.getLabel(), method.getValue()))
                        .collect(Collectors.joining(", "));
                    log.debug("当前支持的支付方式: {}", methodsStr);
                }
                configManager.logConfigInfo(tenantId);
            }

        } catch (Exception e) {
            log.error("输出租户[{}]初始化结果失败: {}", tenantId, e.getMessage());
        }
    }
}
