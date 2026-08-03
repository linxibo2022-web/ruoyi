package plus.ruoyi.common.pay.wechat.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.exception.WxPayException;

import java.io.File;

/**
 * 微信支付配置构建器
 * <p>
 * 将 PayConfig 转换为 wxjava 的 WxPayConfig
 * <p>
 * 支持两种证书配置方式:
 * 1. 文件路径模式 - 通过文件路径或classpath路径读取证书
 * 2. 证书内容模式 - 直接配置PEM格式的证书内容
 * <p>
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
public class WxPayConfigBuilder {

    /**
     * 构建 WxPayConfig
     *
     * @param config Pay配置
     * @return WxPayConfig
     */
    public WxPayConfig build(PayConfig config) {
        if (config == null) {
            throw new WxPayException("配置不能为空" );
        }

        WxPayConfig wxConfig = new WxPayConfig();

        // 基础配置
        wxConfig.setAppId(config.getAppid());
        wxConfig.setMchId(config.getMchId());

        // v2 密钥
        if (StringUtils.isNotBlank(config.getMchKey())) {
            wxConfig.setMchKey(config.getMchKey());
        }

        // v2 p12证书配置 (用于V2退款)
        String p12CertPath = config.getP12CertPath();
        if (StringUtils.isNotBlank(p12CertPath)) {
            // p12证书必须是文件路径,不支持内容模式
            wxConfig.setKeyPath(resolvePath(p12CertPath));
            log.debug("配置V2退款p12证书: {}", resolvePath(p12CertPath));
        }

        // v3 配置
        if (config.hasApiV3Config()) {
            wxConfig.setApiV3Key(config.getApiV3Key());
            wxConfig.setCertSerialNo(config.getCertSerialNo());

            // 设置证书 - 支持路径和内容两种模式
            String keyPath = config.getKeyPath();
            String certPath = config.getCertPath();

            if (StringUtils.isNotBlank(keyPath)) {
                if (isCertificateContent(keyPath)) {
                    // 证书内容模式
                    wxConfig.setPrivateKeyContent(keyPath.getBytes());
                    log.debug("使用私钥内容配置(PEM格式)" );
                } else {
                    // 文件路径模式
                    wxConfig.setPrivateKeyPath(resolvePath(keyPath));
                    log.debug("使用私钥路径配置: {}", resolvePath(keyPath));
                }
            }

            if (StringUtils.isNotBlank(certPath)) {
                if (isCertificateContent(certPath)) {
                    // 证书内容模式
                    wxConfig.setPrivateCertContent(certPath.getBytes());
                    log.debug("使用证书内容配置(PEM格式)" );
                } else {
                    // 文件路径模式
                    wxConfig.setPrivateCertPath(resolvePath(certPath));
                    log.debug("使用证书路径配置: {}", resolvePath(certPath));
                }
            }

            // 公钥模式检测与配置
            if (config.isWxPublicKeyMode()) {
                // 公钥模式: 使用微信支付公钥而非平台证书
                String platformCertPath = config.getPlatformCertPath();
                String publicKeyContent = config.getContentOrPath(platformCertPath);

                if (StringUtils.isBlank(publicKeyContent)) {
                    // 公钥内容为空: 不启用公钥模式，交由 wxjava 走平台证书自动更新
                    log.warn("检测到公钥模式但公钥内容为空，已退回平台证书模式, appid={}", config.getAppid());
                } else if (!config.hasValidWxPublicKeyId()) {
                    // 公钥ID 缺失时绝不能拿 certSerialNo 顶替，原因有三:
                    // 1. 该值会被 wxjava 作为 Wechatpay-Serial 请求头发给微信，填商户API证书序列号微信侧无法识别，请求会被直接拒绝;
                    // 2. 不携带该请求头时微信支付会自动改用平台证书签名，因此"不启用公钥模式"是更安全的降级路径;
                    // 3. 塞一个非空的错误值会绕过 wxjava 对 fullPublicKeyModel + publicKeyId 的启动校验，
                    //    把本可在启动期暴露的配置缺失，推迟成运行期难以定位的请求失败。
                    log.warn("微信支付公钥模式缺少公钥ID(publicKeyId)，已退回平台证书模式, appid={}。"
                                    + "请在微信商户平台「账户中心 → API安全 → 微信支付公钥」获取 PUB_KEY_ID_ 开头的公钥ID 后补充配置",
                            config.getAppid());
                } else {
                    // 设置公钥内容
                    wxConfig.setPublicKeyContent(publicKeyContent.getBytes());

                    // 设置公钥ID: 必须是微信商户平台生成的真实公钥ID，作为 Wechatpay-Serial 请求头标识验签所用公钥
                    wxConfig.setPublicKeyId(config.getPublicKeyId());

                    // 启用完全公钥模式
                    wxConfig.setFullPublicKeyModel(true);

                    // 格式可疑只告警不阻断: 最常见的误配就是把商户API证书序列号填进了公钥ID
                    if (!config.isStandardWxPublicKeyIdFormat()) {
                        log.warn("微信支付公钥ID 不是 PUB_KEY_ID_ 开头，疑似误填商户API证书序列号，请求可能被微信拒绝, appid={}",
                                config.getAppid());
                    }

                    log.debug("微信支付v3配置: 使用公钥模式, appid={}, publicKeyId={}",
                            config.getAppid(), config.getPublicKeyId());
                }
            } else {
                // 平台证书模式: 让 wxjava 自动获取平台证书
                log.debug("微信支付v3配置: 使用平台证书模式(自动更新), appid={}", config.getAppid());
            }
        }

        // 通用配置
        wxConfig.setNotifyUrl("" ); // 回调地址由业务层动态设置
        wxConfig.setSignType("MD5" ); // v2默认签名类型

        log.debug("WxPayConfig构建完成: appid={}, mchId={}, 支持v3={}, 公钥模式={}",
                config.getAppid(), config.getMchId(), config.hasApiV3Config(), config.isWxPublicKeyMode());

        return wxConfig;
    }

    /**
     * 判断输入是证书内容还是文件路径
     * <p>
     * 判断依据:
     * - 以 "-----BEGIN" 开头的是 PEM 格式证书内容
     * - 否则视为文件路径
     *
     * @param input 输入字符串
     * @return true=证书内容, false=文件路径
     */
    private boolean isCertificateContent(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }

        String trimmed = input.trim();

        // PEM格式的证书内容特征:
        // - 证书: -----BEGIN CERTIFICATE-----
        // - 私钥: -----BEGIN PRIVATE KEY----- 或 -----BEGIN RSA PRIVATE KEY-----
        return trimmed.startsWith("-----BEGIN" );
    }

    /**
     * 解析路径
     * 支持 classpath: 前缀和绝对路径
     */
    private String resolvePath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }

        // 如果是classpath:开头,保持不变,wxjava会处理
        if (path.startsWith("classpath:" )) {
            return path;
        }

        // 如果是绝对路径,验证文件是否存在
        File file = new File(path);
        if (!file.exists()) {
            log.warn("证书文件不存在: {}", path);
        }

        return path;
    }
}
