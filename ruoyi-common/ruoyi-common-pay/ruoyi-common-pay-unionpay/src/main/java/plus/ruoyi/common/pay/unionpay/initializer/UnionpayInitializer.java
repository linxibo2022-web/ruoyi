package plus.ruoyi.common.pay.unionpay.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.core.initializer.PayInitializer;
import plus.ruoyi.common.pay.unionpay.registry.UnionpayClientRegistry;

import java.util.List;

/**
 * 银联支付初始化器
 *
 * 负责初始化银联支付配置
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class UnionpayInitializer implements PayInitializer {

    private final UnionpayClientRegistry registry;

    @Override
    public String getPaymentType() {
        return DictPaymentMethod.UNIONPAY.getValue();
    }

    @Override
    public void initConfigs(List<PayConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.warn("银联配置列表为空,跳过初始化");
            return;
        }

        log.info("开始初始化银联配置,共{}个", configs.size());

        for (PayConfig config : configs) {
            try {
                initialize(config);
            } catch (Exception e) {
                log.error("银联配置初始化失败,继续处理下一个: appid={}", config.getAppid(), e);
            }
        }

        log.info("银联配置初始化完成,成功注册{}个实例", getRegisteredCount());
    }

    /**
     * 初始化单个配置
     */
    private void initialize(PayConfig config) {
        try {
            log.debug("初始化银联配置: appid={}", config.getAppid());

            // 验证配置完整性
            if (!validateConfig(config)) {
                throw new IllegalArgumentException("银联配置不完整: appid=" + config.getAppid());
            }

            // 注册到注册表
            registry.register(config.getAppid(), config);

            log.info("银联配置初始化成功: appid={}, mchId={}",
                config.getAppid(), config.getMchId());

        } catch (Exception e) {
            log.error("银联配置初始化失败: appid={}, error={}",
                config.getAppid(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证配置完整性
     */
    private boolean validateConfig(PayConfig config) {
        // 检查必需字段
        if (config.getAppid() == null || config.getAppid().trim().isEmpty()) {
            log.error("银联配置缺少AppId");
            return false;
        }

        if (config.getMchKey() == null || config.getMchKey().trim().isEmpty()) {
            log.error("银联配置缺少AppSecret: appid={}", config.getAppid());
            return false;
        }

        // 检查签名密钥（RSA私钥或Signature）
        if (config.getCertPath() == null || config.getCertPath().trim().isEmpty()) {
            log.error("银联配置缺少RSA私钥: appid={}", config.getAppid());
            return false;
        }

        // 检查验签公钥
        if (config.getPlatformCertPath() == null || config.getPlatformCertPath().trim().isEmpty()) {
            log.warn("银联配置缺少平台公钥，回调验签可能失败: appid={}", config.getAppid());
        }

        return true;
    }

    /**
     * 销毁配置
     */
    public void destroy(String appid) {
        try {
            registry.unregister(appid);
            log.info("银联配置已销毁: appid={}", appid);
        } catch (Exception e) {
            log.error("销毁银联配置失败: appid={}, error={}",
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
