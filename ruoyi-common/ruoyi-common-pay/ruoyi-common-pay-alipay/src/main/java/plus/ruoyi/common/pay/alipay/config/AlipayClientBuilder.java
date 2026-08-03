package plus.ruoyi.common.pay.alipay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.exception.PayException;

import java.util.Arrays;

/**
 * AlipayClient 构建器
 * <p>
 * 根据 PayConfig 创建 AlipayClient 实例
 *
 * @author 抓蛙师
 */
@Slf4j
public class AlipayClientBuilder {

    /**
     * 支付宝网关地址 - 正式环境
     */
    private static final String ALIPAY_GATEWAY = "https://openapi.alipay.com/gateway.do";

    /**
     * 支付宝网关地址 - 沙箱环境
     */
    private static final String ALIPAY_SANDBOX_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /**
     * 默认格式
     */
    private static final String FORMAT = "json";

    /**
     * 默认字符集
     */
    private static final String CHARSET = "UTF-8";

    /**
     * 默认签名类型
     */
    private static final String SIGN_TYPE = "RSA2";

    /**
     * 根据配置创建 AlipayClient
     */
    public static AlipayClient build(PayConfig config) {
        try {
            log.debug("开始构建AlipayClient: appid={}", config.getAppid());

            // 验证必要参数
            validateConfig(config);

            // 判断是否使用公钥证书模式
            boolean useCert = StringUtils.isNotBlank(config.getCertPath());

            AlipayClient client;
            if (useCert) {
                // 证书模式
                client = buildWithCert(config);
            } else {
                // 公钥模式
                client = buildWithPublicKey(config);
            }

            log.debug("AlipayClient构建成功: appid={}, mode={}",
                config.getAppid(), useCert ? "证书模式" : "公钥模式");

            return client;

        } catch (Exception e) {
            log.error("构建AlipayClient失败: appid={}, error={}",
                config.getAppid(), e.getMessage(), e);
            throw new PayException("构建AlipayClient失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用公钥模式构建
     */
    private static AlipayClient buildWithPublicKey(PayConfig config) {
        // 智能获取私钥内容（支持文件路径和直接内容）
        String privateKey = config.getContentOrPath(config.getKeyPath());
        // 智能获取支付宝公钥内容
        String alipayPublicKey = config.getContentOrPath(config.getAlipayPublicKey());

        return new DefaultAlipayClient(
            getGatewayUrl(),
            config.getAppid(),
            privateKey,              // 应用私钥
            FORMAT,
            CHARSET,
            alipayPublicKey,         // 支付宝公钥
            SIGN_TYPE
        );
    }

    /**
     * 使用证书模式构建
     */
    private static AlipayClient buildWithCert(PayConfig config) {
        try {
            // 智能获取证书内容（支持文件路径和直接内容）
            String privateKey = config.getContentOrPath(config.getKeyPath());
            String appCertContent = config.getContentOrPath(config.getCertPath());
            String alipayPublicCert = config.getContentOrPath(config.getAlipayPublicKey());
            String alipayCertPath = config.getContentOrPath(config.getPlatformCertPath());

            return new DefaultAlipayClient(
                getGatewayUrl(),
                config.getAppid(),
                privateKey,           // 应用私钥
                FORMAT,
                CHARSET,
                alipayPublicCert,     // 支付宝公钥证书
                SIGN_TYPE,
                appCertContent,       // 应用公钥证书
                alipayCertPath        // 支付宝根证书
            );
        } catch (Exception e) {
            throw new PayException("证书模式构建AlipayClient失败", e);
        }
    }

    /**
     * 获取网关地址
     * 开发环境自动使用沙箱，生产环境使用正式环境
     */
    private static String getGatewayUrl() {
        // 是否使用沙箱环境
        boolean enableSandbox = false;
        if (enableSandbox && isSandboxEnvironment()) {
            log.info("当前为开发环境，使用支付宝沙箱网关: {}", ALIPAY_SANDBOX_GATEWAY);
            return ALIPAY_SANDBOX_GATEWAY;
        } else {
            log.debug("当前为生产环境，使用支付宝正式网关: {}", ALIPAY_GATEWAY);
            return ALIPAY_GATEWAY;
        }
    }

    /**
     * 判断是否为沙箱环境
     * 开发环境(dev)使用沙箱，其他环境使用正式
     *
     * @return true=沙箱环境, false=正式环境
     */
    private static boolean isSandboxEnvironment() {
        try {
            // 判断当前激活的环境中是否包含 dev
            return Arrays.stream(SpringUtils.getActiveProfiles())
                .anyMatch(Constants.DEV::equalsIgnoreCase);
        } catch (Exception e) {
            log.warn("获取Spring环境配置失败，默认使用正式环境: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证配置完整性
     */
    private static void validateConfig(PayConfig config) {
        if (StringUtils.isBlank(config.getAppid())) {
            throw new PayException("支付宝appid不能为空");
        }

        if (StringUtils.isBlank(config.getKeyPath())) {
            throw new PayException("支付宝应用私钥不能为空");
        }

        // 验证是否至少有一种认证方式
        boolean hasPublicKey = StringUtils.isNotBlank(config.getAlipayPublicKey());
        boolean hasCert = StringUtils.isNotBlank(config.getCertPath());

        if (!hasPublicKey && !hasCert) {
            throw new PayException("支付宝公钥或证书至少需要配置一种");
        }

        // 证书模式需要完整的证书配置
        if (hasCert) {
            if (StringUtils.isBlank(config.getPlatformCertPath())) {
                throw new PayException("证书模式需要配置支付宝根证书路径");
            }
        }
    }
}
