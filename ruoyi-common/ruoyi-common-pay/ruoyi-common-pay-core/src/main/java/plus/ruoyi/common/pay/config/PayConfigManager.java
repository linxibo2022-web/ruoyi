package plus.ruoyi.common.pay.config;

import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.pay.exception.ConfigNotFoundException;
import plus.ruoyi.common.tenant.helper.TenantHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 支付配置管理器
 *
 * 管理所有支付配置(微信、支付宝等)
 * 支持多租户、多商户场景
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
public class PayConfigManager {

    /**
     * 配置缓存
     * Key: configId (格式: tenantId:paymentMethod:appid)
     * Value: PayConfig
     */
    private final Map<String, PayConfig> configMap = new ConcurrentHashMap<>();

    /**
     * appid索引
     * Key: appid
     * Value: configId
     */
    private final Map<String, String> appidIndex = new ConcurrentHashMap<>();

    /**
     * mchId索引
     * Key: mchId
     * Value: configId
     */
    private final Map<String, String> mchIdIndex = new ConcurrentHashMap<>();

    /**
     * 添加配置
     */
    public void addConfig(PayConfig config) {
        if (config == null || !config.isValid()) {
            log.warn("配置无效,忽略添加: {}", config);
            return;
        }

        String configId = config.getConfigId();
        configMap.put(configId, config);
        appidIndex.put(config.getAppid(), configId);

        // 添加 mchId 索引
        if (StringUtils.isNotBlank(config.getMchId())) {
            mchIdIndex.put(config.getMchId(), configId);
        }

        log.debug("支付配置已添加: {} [{}] appid={} mchId={}",
            config.getPaymentMethod().getLabel(),
            config.getMchName(),
            config.getAppid(),
            config.getMchId());
    }

    /**
     * 注册配置（addConfig的别名，保持与PayConfigManager一致）
     */
    public void registerConfig(PayConfig config) {
        addConfig(config);
    }

    /**
     * 批量添加配置
     */
    public void addConfigs(List<PayConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.warn("配置列表为空");
            return;
        }

        configs.forEach(this::addConfig);
        log.info("批量添加配置完成,共{}个", configs.size());
    }

    /**
     * 根据 appid 获取配置
     *
     * @param appid 应用ID
     * @return 配置对象
     * @throws ConfigNotFoundException 配置不存在时抛出
     */
    public PayConfig getConfigByAppid(String appid) {
        String configId = appidIndex.get(appid);
        if (configId == null) {
            throw ConfigNotFoundException.of(TenantHelper.getTenantId(), appid);
        }

        return configMap.get(configId);
    }

    /**
     * 根据 mchId 获取配置
     *
     * @param mchId 商户号
     * @return 配置对象
     * @throws ConfigNotFoundException 配置不存在时抛出
     */
    public PayConfig getConfigByMchId(String mchId) {
        String configId = mchIdIndex.get(mchId);
        if (configId == null) {
            throw ConfigNotFoundException.of(TenantHelper.getTenantId(), "商户号: " + mchId);
        }

        return configMap.get(configId);
    }

    /**
     * 根据租户ID、支付方式、appid获取配置
     *
     * @param tenantId 租户ID
     * @param paymentMethod 支付方式
     * @param appid 应用ID
     * @return 配置对象
     */
    public PayConfig getConfig(String tenantId, String paymentMethod, String appid) {
        String configId = buildConfigId(tenantId, paymentMethod, appid);
        PayConfig config = configMap.get(configId);

        if (config == null) {
            throw ConfigNotFoundException.of(tenantId, appid);
        }

        return config;
    }

    /**
     * 根据支付方式和appid获取配置（兼容单租户调用方式）
     * 如果appid为空，则返回该支付方式下的第一个配置
     *
     * @param paymentMethod 支付方式
     * @param appid 应用ID（可为空）
     * @return 配置对象
     */
    public PayConfig getConfig(String paymentMethod, String appid) {
        if (StringUtils.isBlank(paymentMethod)) {
            return null;
        }

        // 获取当前租户ID
        String tenantId = TenantHelper.getTenantId();

        // 如果appid不为空，按精确匹配查找
        if (StringUtils.isNotBlank(appid)) {
            String configId = buildConfigId(tenantId, paymentMethod, appid);
            return configMap.get(configId);
        }

        // 如果appid为空，返回该支付方式下的第一个配置
        List<PayConfig> configs = getConfigsByPaymentMethod(DictPaymentMethod.getByValue(paymentMethod));
        return configs.isEmpty() ? null : configs.get(0);
    }

    /**
     * 根据配置ID获取配置
     *
     * @param configId 配置ID
     * @return 配置对象
     */
    public PayConfig getConfig(String configId) {
        return configMap.get(configId);
    }

    /**
     * 获取指定支付方式的所有配置
     *
     * @param paymentMethod 支付方式
     * @return 配置列表
     */
    public List<PayConfig> getConfigsByPaymentMethod(DictPaymentMethod paymentMethod) {
        return configMap.values().stream()
            .filter(config -> config.getPaymentMethod().equals(paymentMethod))
            .toList();
    }

    /**
     * 获取当前租户的默认微信支付配置
     * 优先返回第一个可用的微信支付配置
     *
     * @return 默认微信支付配置
     * @throws ConfigNotFoundException 当前租户没有任何微信支付配置时抛出
     */
    public PayConfig getDefaultWxPayConfig() {
        String tenantId = TenantHelper.getTenantId();

        List<PayConfig> wxConfigs = configMap.values().stream()
            .filter(config -> config.getPaymentMethod().equals(DictPaymentMethod.WECHAT))
            .filter(config -> config.getTenantId().equals(tenantId))
            .toList();

        if (wxConfigs.isEmpty()) {
            throw ConfigNotFoundException.of(tenantId, "无可用的微信支付配置");
        }

        PayConfig defaultConfig = wxConfigs.get(0);
        log.info("使用默认微信支付配置: appid={}, mchId={}, mchName={}",
            defaultConfig.getAppid(), defaultConfig.getMchId(), defaultConfig.getMchName());

        return defaultConfig;
    }

    /**
     * 移除配置
     */
    public void removeConfig(String appid) {
        String configId = appidIndex.remove(appid);
        if (configId != null) {
            PayConfig config = configMap.remove(configId);
            if (config != null && StringUtils.isNotBlank(config.getMchId())) {
                mchIdIndex.remove(config.getMchId());
            }
            log.info("配置已移除: appid={}", appid);
        }
    }

    /**
     * 清空所有配置
     */
    public void clear() {
        int size = configMap.size();
        configMap.clear();
        appidIndex.clear();
        mchIdIndex.clear();
        log.info("配置管理器已清空,共移除{}个配置", size);
    }

    /**
     * 获取配置数量
     */
    public int size() {
        return configMap.size();
    }

    /**
     * 检查配置是否存在
     */
    public boolean exists(String appid) {
        return appidIndex.containsKey(appid);
    }

    /**
     * 获取指定租户的所有配置
     *
     * @param tenantId 租户ID
     * @return 配置列表
     */
    public List<PayConfig> getConfigsByTenant(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            return Collections.emptyList();
        }

        String keyPrefix = tenantId + ":";

        return configMap.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(keyPrefix))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    /**
     * 清空指定租户的所有配置
     *
     * @param tenantId 租户ID
     */
    public void clearConfigsByTenant(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            return;
        }

        String keyPrefix = tenantId + ":";
        Set<String> keysToRemove = configMap.keySet().stream()
            .filter(key -> key.startsWith(keyPrefix))
            .collect(Collectors.toSet());

        int removedCount = keysToRemove.size();

        // 同时清理appidIndex
        keysToRemove.forEach(configId -> {
            PayConfig config = configMap.get(configId);
            if (config != null) {
                appidIndex.remove(config.getAppid());
            }
            configMap.remove(configId);
        });

        if (removedCount > 0) {
            log.info("清空租户[{}]的所有配置完成，共清空 {} 个配置", tenantId, removedCount);
        }
    }

    /**
     * 获取配置统计信息
     *
     * @param tenantId 租户ID
     * @return 支付方式 -> 配置数量
     */
    public Map<String, Integer> getConfigStats(String tenantId) {
        return getConfigsByTenant(tenantId).stream()
            .collect(Collectors.groupingBy(
                config -> config.getPaymentMethod().getValue(),
                Collectors.summingInt(config -> 1)
            ));
    }

    /**
     * 输出指定租户的配置信息到日志
     *
     * @param tenantId 租户ID
     */
    public void logConfigInfo(String tenantId) {
        List<PayConfig> tenantConfigs = getConfigsByTenant(tenantId);

        if (tenantConfigs.isEmpty()) {
            log.info("租户[{}]当前没有任何支付配置", tenantId);
            return;
        }

        log.info("租户[{}]当前支付配置概览:", tenantId);
        log.info("  配置总数: {}", tenantConfigs.size());

        Map<DictPaymentMethod, List<PayConfig>> groupedConfigs = tenantConfigs.stream()
            .collect(Collectors.groupingBy(PayConfig::getPaymentMethod));

        for (Map.Entry<DictPaymentMethod, List<PayConfig>> entry : groupedConfigs.entrySet()) {
            DictPaymentMethod paymentMethod = entry.getKey();
            List<PayConfig> configs = entry.getValue();

            log.info("  {}支付 - {} 个配置:", paymentMethod.getLabel(), configs.size());
            int index = 1;
            for (PayConfig config : configs) {
                log.info("    {}. appid={}, mchId={}, mchName={}",
                    index++, config.getAppid(), config.getMchId(), config.getMchName());
            }
        }
    }

    /**
     * 获取所有支付方式
     *
     * @return 支付方式集合
     */
    public Set<String> getAllPaymentMethods() {
        return configMap.keySet().stream()
            .map(this::extractPaymentMethodFromConfigId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * 获取指定租户支持的所有支付方式
     *
     * @param tenantId 租户ID
     * @return 支付方式列表
     */
    public List<String> getSupportedPaymentMethodsByTenant(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            return Collections.emptyList();
        }

        return getConfigsByTenant(tenantId).stream()
            .map(config -> config.getPaymentMethod().getValue())
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 获取指定AppId支持的所有支付方式
     *
     * @param appId 应用ID
     * @return 支付方式列表
     */
    public List<String> getSupportedPaymentMethods(String appId) {
        if (StringUtils.isBlank(appId)) {
            return Collections.emptyList();
        }

        return configMap.values().stream()
            .filter(config -> appId.equals(config.getAppid()))
            .map(config -> config.getPaymentMethod().getValue())
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 根据appid查找所有相关配置
     *
     * @param appid 应用ID
     * @return 配置列表
     */
    public List<PayConfig> getConfigsByAppid(String appid) {
        if (StringUtils.isBlank(appid)) {
            return Collections.emptyList();
        }

        return configMap.values().stream()
            .filter(config -> appid.equals(config.getAppid()))
            .collect(Collectors.toList());
    }

    /**
     * 检查是否有指定支付方式的配置
     *
     * @param paymentMethod 支付方式
     * @return 是否有配置
     */
    public boolean hasConfigs(DictPaymentMethod paymentMethod) {
        return !getConfigsByPaymentMethod(paymentMethod).isEmpty();
    }

    /**
     * 检查是否有指定支付方式的配置（字符串版本）
     *
     * @param paymentMethod 支付方式
     * @return 是否有配置
     */
    public boolean hasConfigs(String paymentMethod) {
        DictPaymentMethod pm = DictPaymentMethod.getByValue(paymentMethod);
        return pm != null && hasConfigs(pm);
    }

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    public List<PayConfig> getAllConfigs() {
        return new ArrayList<>(configMap.values());
    }

    /**
     * 清空所有配置（clear的别名）
     */
    public void clearAllConfigs() {
        clear();
    }

    /**
     * 移除指定配置（支持多种方式）
     *
     * @param paymentMethod 支付方式
     * @param appid 应用ID
     */
    public void removeConfig(String paymentMethod, String appid) {
        if (StringUtils.isAnyBlank(paymentMethod, appid)) {
            return;
        }

        String tenantId = TenantHelper.getTenantId();
        String configId = buildConfigId(tenantId, paymentMethod, appid);
        PayConfig removed = configMap.remove(configId);

        if (removed != null) {
            appidIndex.remove(removed.getAppid());
            if (StringUtils.isNotBlank(removed.getMchId())) {
                mchIdIndex.remove(removed.getMchId());
            }
            log.info("移除配置: 租户={}, 支付方式={}, appid={}",
                tenantId, paymentMethod, appid);
        }
    }

    /**
     * 从配置ID中提取支付方式
     */
    private String extractPaymentMethodFromConfigId(String configId) {
        if (StringUtils.isBlank(configId)) {
            return null;
        }

        String[] parts = configId.split(":");
        // 格式: tenantId:paymentMethod:appid
        return parts.length >= 2 ? parts[1] : null;
    }

    /**
     * 构建配置ID
     */
    private String buildConfigId(String tenantId, String paymentMethod, String appid) {
        return String.format("%s:%s:%s", tenantId, paymentMethod, appid);
    }
}
