package plus.ruoyi.common.pay.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.domain.dto.PaymentDTO;
import plus.ruoyi.common.core.domain.dto.PlatformDTO;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 支付配置类 (多租户版本)
 *
 * 支持多租户隔离 + wxjava + 支付宝SDK
 * 增强证书智能处理能力
 *
 * @author 抓蛙师
 */
@Slf4j
@Data
public class PayConfig {

    /**
     * 配置唯一标识
     * 格式: {tenantId}:{paymentMethod}:{appid}
     */
    private String configId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 支付方式
     */
    private DictPaymentMethod paymentMethod;

    /**
     * 应用ID (微信appid/支付宝appid等)
     */
    private String appid;

    /**
     * 商户号 (微信mch_id/支付宝app_id等)
     */
    private String mchId;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 平台类型 (mp_weixin/mp_official_account等)
     */
    private String platformType;

    /**
     * 商户密钥 (微信key/支付宝私钥等)
     */
    private String mchKey;

    /**
     * 支付宝公钥 (仅支付宝使用)
     */
    private String alipayPublicKey;

    /**
     * 证书路径
     */
    private String certPath;

    /**
     * 证书密钥路径
     */
    private String keyPath;

    /**
     * 平台证书路径（用于验证回调签名）
     */
    private String platformCertPath;

    /**
     * p12证书路径(V2退款专用)
     */
    private String p12CertPath;

    /**
     * API v3密钥 (微信支付v3)
     */
    private String apiV3Key;

    /**
     * 证书序列号 (微信支付v3)
     */
    private String certSerialNo;

    /**
     * 微信支付公钥ID (微信支付v3公钥模式专用)
     * <p>
     * 格式为 PUB_KEY_ID_xxx，从微信商户平台「账户中心 → API安全 → 微信支付公钥」获取。
     * 注意: 与证书序列号(certSerialNo)是完全不同的两个值，公钥模式下验签依赖此 ID 匹配响应头 Wechatpay-Serial。
     */
    private String publicKeyId;

    /**
     * 是否为默认配置
     */
    private boolean defaultConfig;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 默认构造函数
     */
    public PayConfig() {
    }

    /**
     * 从 DTO 创建配置对象的静态工厂方法（多租户版本）
     *
     * @param payment 支付配置DTO
     * @param platform 平台配置DTO
     * @return PayConfig实例
     */
    public static PayConfig from(PaymentDTO payment, PlatformDTO platform) {
        if (payment == null || platform == null) {
            throw new IllegalArgumentException("支付配置和平台配置不能为空");
        }

        PayConfig config = new PayConfig();

        // 租户ID
        config.tenantId = platform.getTenantId();

        // 支付方式
        config.paymentMethod = DictPaymentMethod.getByValue(
            StringUtils.isNotBlank(payment.getType()) ? payment.getType().toLowerCase() : null
        );

        // 应用ID
        config.appid = platform.getAppid();

        // 构建配置ID
        if (StringUtils.isNotBlank(config.tenantId)) {
            // 多租户模式
            config.configId = buildConfigId(config.tenantId, config.paymentMethod.getValue(), config.appid);
        } else {
            // 单租户模式（兼容）
            config.configId = buildConfigIdSingleTenant(config.paymentMethod.getValue(), config.appid);
        }

        // 支付配置
        config.mchId = payment.getMchId();
        config.mchName = payment.getMchName();
        config.mchKey = payment.getMchKey();
        config.certPath = payment.getCertPath();
        config.keyPath = payment.getKeyPath();
        config.platformCertPath = payment.getPlatformCertPath();
        config.p12CertPath = payment.getP12CertPath();
        config.apiV3Key = payment.getApiV3Key();
        config.certSerialNo = payment.getCertSerialNo();
        config.publicKeyId = payment.getPublicKeyId();

        // 平台配置
        config.platformName = platform.getName();
        config.platformType = platform.getType();

        // 状态配置
        config.defaultConfig = false;
        config.enabled = true;

        // 支付宝特殊处理：支付宝公钥存储在 platformCertPath 字段
        if (config.paymentMethod == DictPaymentMethod.ALIPAY) {
            config.alipayPublicKey = payment.getPlatformCertPath();
        }

        return config;
    }

    /**
     * 构建配置ID（多租户版本）
     */
    private static String buildConfigId(String tenantId, String paymentMethod, String appid) {
        return StringUtils.format("{}:{}:{}", tenantId, paymentMethod, appid);
    }

    /**
     * 构建配置ID（单租户版本，用于兼容）
     */
    private static String buildConfigIdSingleTenant(String paymentMethod, String appid) {
        return StringUtils.format("{}:{}", paymentMethod.toLowerCase(), appid);
    }

    /**
     * 设置配置ID（单租户版本兼容方法）
     */
    public void updateConfigId() {
        if (paymentMethod != null && StringUtils.isNotBlank(appid)) {
            if (StringUtils.isNotBlank(tenantId)) {
                // 多租户模式
                this.configId = buildConfigId(tenantId, paymentMethod.getValue(), appid);
            } else {
                // 单租户模式
                this.configId = buildConfigIdSingleTenant(paymentMethod.getValue(), appid);
            }
        }
    }

    /**
     * 验证配置是否完整
     */
    public boolean isValid() {
        if (StringUtils.isAnyBlank(tenantId, appid) || paymentMethod == null) {
            return false;
        }

        // 微信支付配置验证
        if (paymentMethod == DictPaymentMethod.WECHAT) {
            return StringUtils.isNotBlank(mchId) &&
                (StringUtils.isNotBlank(mchKey) || StringUtils.isNotBlank(apiV3Key));
        }

        // 支付宝配置验证
        if (paymentMethod == DictPaymentMethod.ALIPAY) {
            boolean hasCert = StringUtils.isNoneBlank(certPath, keyPath);
            boolean hasPublicKey = StringUtils.isNotBlank(alipayPublicKey);
            return hasCert || hasPublicKey;
        }

        // 余额支付和积分支付无需额外验证
        if (paymentMethod == DictPaymentMethod.BALANCE ||
            paymentMethod == DictPaymentMethod.POINTS) {
            return true;
        }

        return false;
    }

    /**
     * 是否支持API v3 (微信支付)
     */
    public boolean hasApiV3Config() {
        return StringUtils.isNoneBlank(apiV3Key, certSerialNo);
    }

    /**
     * 是否具备完整的v3配置（包含平台证书路径）
     */
    public boolean hasCompleteV3Config() {
        return hasApiV3Config() && hasV3CertPath() && hasPlatformCert();
    }

    /**
     * 是否有v3证书配置（支持文件路径和直接内容）
     */
    public boolean hasV3CertPath() {
        if (StringUtils.isBlank(certPath)) {
            return false;
        }

        // 如果是PEM格式的证书内容，直接返回true
        if (isPemContent(certPath)) {
            log.debug("检测到v3证书内容（非文件路径）");
            return true;
        }

        // 处理classpath资源路径
        if (certPath.startsWith("classpath:")) {
            try {
                return getClass().getClassLoader().getResourceAsStream(
                        certPath.substring("classpath:".length())) != null;
            } catch (Exception e) {
                return false;
            }
        }

        // 处理文件系统路径
        try {
            File file = new File(certPath);
            return file.exists() && file.canRead();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证是否有平台证书配置（支持文件路径和直接内容）
     */
    public boolean hasPlatformCert() {
        if (StringUtils.isBlank(platformCertPath)) {
            return false;
        }

        // 如果是PEM格式的证书内容，直接返回true
        if (isPemContent(platformCertPath)) {
            log.debug("检测到平台证书内容（非文件路径）");
            return true;
        }

        // 处理classpath资源路径
        if (platformCertPath.startsWith("classpath:")) {
            try {
                return getClass().getClassLoader().getResourceAsStream(
                        platformCertPath.substring("classpath:".length())) != null;
            } catch (Exception e) {
                return false;
            }
        }

        // 处理文件系统路径
        try {
            File file = new File(platformCertPath);
            return file.exists() && file.canRead();
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 是否使用证书模式 (支付宝)
     */
    public boolean isCertMode() {
        if (paymentMethod != DictPaymentMethod.ALIPAY) {
            return false;
        }
        return hasValidCertConfig(certPath) && hasValidCertConfig(keyPath);
    }

    /**
     * 验证证书配置是否有效（支持文件路径和直接内容）
     */
    private boolean hasValidCertConfig(String certConfig) {
        if (StringUtils.isBlank(certConfig)) {
            return false;
        }

        // 如果是PEM格式的证书内容，直接返回true
        if (isPemContent(certConfig)) {
            return true;
        }

        // 处理classpath资源路径
        if (certConfig.startsWith("classpath:")) {
            try {
                return getClass().getClassLoader().getResourceAsStream(
                        certConfig.substring("classpath:".length())) != null;
            } catch (Exception e) {
                return false;
            }
        }

        // 处理文件系统路径
        try {
            File file = new File(certConfig);
            return file.exists() && file.canRead();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 智能检测微信支付是否使用公钥模式
     */
    public boolean isWxPublicKeyMode() {
        if (paymentMethod != DictPaymentMethod.WECHAT) {
            return false;
        }

        if (StringUtils.isBlank(platformCertPath)) {
            return false;
        }

        try {
            if (isPublicKeyByFileName(platformCertPath)) {
                log.debug("通过文件名检测到微信公钥模式: {}", platformCertPath);
                return true;
            }

            if (isPublicKeyByContent(platformCertPath)) {
                log.debug("通过文件内容检测到微信公钥模式: {}", platformCertPath);
                return true;
            }

            log.debug("检测为微信平台证书模式: {}", platformCertPath);
            return false;

        } catch (Exception e) {
            log.warn("检测微信支付文件类型失败，默认使用平台证书模式: path={}, error={}",
                    platformCertPath, e.getMessage());
            return false;
        }
    }

    /**
     * 微信支付公钥ID 的固定前缀
     * <p>
     * 依据微信支付官方文档：微信支付公钥ID 形如 PUB_KEY_ID_0000000000000000000000000000，
     * 与 40 位十六进制的商户API证书序列号(certSerialNo)是完全不同的两个值。
     */
    private static final String WX_PUBLIC_KEY_ID_PREFIX = "PUB_KEY_ID_";

    /**
     * 是否已配置可用的微信支付公钥ID
     * <p>
     * 该值会被 wxjava 作为 Wechatpay-Serial 请求头发送给微信支付。
     * 绝不能用 certSerialNo 顶替：商户证书序列号在微信支付侧无法识别，会导致请求被拒；
     * 而不携带该请求头时微信支付会自动改用平台证书签名，因此"不配置"远比"配错值"安全。
     *
     * @return true=已配置公钥ID，可启用完全公钥模式
     */
    public boolean hasValidWxPublicKeyId() {
        return StringUtils.isNotBlank(publicKeyId);
    }

    /**
     * 公钥ID 是否符合微信支付官方格式(PUB_KEY_ID_ 前缀)
     * <p>
     * 仅用于配置健康检查告警，不阻断流程：万一微信后续调整格式，这里不应把可用配置判死。
     * 常见误配是把商户API证书序列号填进了公钥ID，此方法可在启动阶段把该错误暴露出来。
     *
     * @return true=格式符合官方约定
     */
    public boolean isStandardWxPublicKeyIdFormat() {
        return StringUtils.isNotBlank(publicKeyId)
                && publicKeyId.trim().startsWith(WX_PUBLIC_KEY_ID_PREFIX);
    }

    /**
     * 基于文件名检测是否为公钥文件
     */
    private boolean isPublicKeyByFileName(String certPath) {
        if (StringUtils.isBlank(certPath)) {
            return false;
        }

        String fileName = certPath.toLowerCase();
        return fileName.contains("public") ||
                fileName.contains("pubkey") ||
                fileName.contains("pub_key") ||
                fileName.contains("publickey");
    }

    /**
     * 基于文件内容检测是否为公钥文件
     */
    private boolean isPublicKeyByContent(String certPath) {
        try {
            String content = getContentOrPath(certPath);
            return StringUtils.isNotBlank(content) &&
                    content.trim().startsWith("-----BEGIN PUBLIC KEY-----");
        } catch (Exception e) {
            log.debug("检测公钥内容失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 智能获取证书/公钥内容
     */
    public String getContentOrPath(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        try {
            if (isPemContent(input)) {
                log.debug("检测到直接粘贴的证书/公钥内容");
                return cleanPemContent(input);
            }

            String content = readFileContent(input);
            // 如果读取的文件内容是 PEM 格式，也需要清理
            if (isPemContent(content)) {
                return cleanPemContent(content);
            }
            return content;

        } catch (Exception e) {
            log.warn("读取证书内容失败，可能是无效的路径或内容: error={}", e.getMessage());
            return input.trim();
        }
    }

    /**
     * 清理 PEM 格式内容
     * 移除多余的空格、换行符，确保格式正确
     * 自动转换 PKCS#1 格式私钥为 PKCS#8 格式（支付宝要求）
     */
    private String cleanPemContent(String pemContent) {
        if (StringUtils.isBlank(pemContent)) {
            return pemContent;
        }

        // 1. trim 首尾空格
        String cleaned = pemContent.trim();

        // 2. 确保只保留从 BEGIN 到 END 的内容
        int beginIndex = cleaned.indexOf("-----BEGIN");
        int endIndex = cleaned.lastIndexOf("-----");

        if (beginIndex >= 0 && endIndex > beginIndex) {
            // 找到最后一个 "-----" 后面是否有 "END"
            String afterEnd = cleaned.substring(endIndex);
            if (afterEnd.contains("END")) {
                // 计算完整的结束标记位置
                int fullEndIndex = cleaned.indexOf("-----", endIndex + 5);
                if (fullEndIndex > endIndex) {
                    cleaned = cleaned.substring(beginIndex, fullEndIndex + 5);
                } else {
                    cleaned = cleaned.substring(beginIndex, endIndex + 5);
                }
            } else {
                cleaned = cleaned.substring(beginIndex);
            }
        }

        // 3. 规范化换行符（统一使用 \n）
        cleaned = cleaned.replace("\r\n", "\n").replace("\r", "\n");

        // 4. 如果是 PKCS#1 格式的 RSA 私钥，转换为 PKCS#8 格式（支付宝SDK要求）
        if (cleaned.contains("-----BEGIN RSA PRIVATE KEY-----")) {
            log.info("检测到 PKCS#1 格式私钥，自动转换为 PKCS#8 格式");
            cleaned = convertPkcs1ToPkcs8(cleaned);
        }

        log.debug("PEM内容清理完成: 原始长度={}, 清理后长度={}", pemContent.length(), cleaned.length());

        return cleaned;
    }

    /**
     * 将 PKCS#1 格式的 RSA 私钥转换为 PKCS#8 格式
     * 支付宝 SDK 只支持 PKCS#8 格式
     */
    private String convertPkcs1ToPkcs8(String pkcs1Key) {
        try {
            // 移除 PEM 头尾和换行符，得到纯 Base64 内容
            String base64 = pkcs1Key
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

            // PKCS#1 -> PKCS#8 转换
            // PKCS#8 = 头部 + PKCS#1内容
            // 头部固定为: 30 82 (长度) 02 01 00 30 0d 06 09 2a 86 48 86 f7 0d 01 01 01 05 00 04 82 (长度-8)
            byte[] pkcs1Bytes = java.util.Base64.getDecoder().decode(base64);

            // 构建 PKCS#8 包装器
            int totalLength = pkcs1Bytes.length + 22;  // 22 是固定头部长度
            byte[] pkcs8Header = new byte[]{
                0x30, (byte) 0x82,
                (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff),  // 总长度
                0x02, 0x01, 0x00,  // 版本号
                0x30, 0x0d,  // AlgorithmIdentifier
                0x06, 0x09, 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01,  // RSA OID
                0x05, 0x00,  // null
                0x04, (byte) 0x82,
                (byte) ((pkcs1Bytes.length >> 8) & 0xff), (byte) (pkcs1Bytes.length & 0xff)  // PKCS#1 长度
            };

            // 合并头部和 PKCS#1 内容
            byte[] pkcs8Bytes = new byte[pkcs8Header.length + pkcs1Bytes.length];
            System.arraycopy(pkcs8Header, 0, pkcs8Bytes, 0, pkcs8Header.length);
            System.arraycopy(pkcs1Bytes, 0, pkcs8Bytes, pkcs8Header.length, pkcs1Bytes.length);

            // 转换为 Base64 并添加 PEM 头尾
            String pkcs8Base64 = java.util.Base64.getEncoder().encodeToString(pkcs8Bytes);

            // 每64个字符换行（PEM 标准格式）
            StringBuilder result = new StringBuilder();
            result.append("-----BEGIN PRIVATE KEY-----\n");
            for (int i = 0; i < pkcs8Base64.length(); i += 64) {
                result.append(pkcs8Base64, i, Math.min(i + 64, pkcs8Base64.length())).append("\n");
            }
            result.append("-----END PRIVATE KEY-----");

            log.info("PKCS#1 转 PKCS#8 成功: 原长度={}, 新长度={}", pkcs1Key.length(), result.length());
            return result.toString();

        } catch (Exception e) {
            log.error("PKCS#1 转 PKCS#8 失败，返回原内容: {}", e.getMessage());
            return pkcs1Key;
        }
    }

    /**
     * 判断是否为PEM格式的证书/公钥内容
     */
    private boolean isPemContent(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }

        String trimmedContent = content.trim();
        return trimmedContent.startsWith("-----BEGIN") && (
                trimmedContent.endsWith("-----END CERTIFICATE-----") ||
                        trimmedContent.endsWith("-----END PUBLIC KEY-----") ||
                        trimmedContent.endsWith("-----END PRIVATE KEY-----") ||
                        trimmedContent.endsWith("-----END RSA PRIVATE KEY-----") ||
                        trimmedContent.endsWith("-----END EC PRIVATE KEY-----")
        );
    }

    /**
     * 读取文件内容（支持classpath和文件系统路径）
     */
    private String readFileContent(String filePath) throws Exception {
        if (filePath.startsWith("classpath:")) {
            String resourcePath = filePath.substring("classpath:".length());
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new IllegalArgumentException("classpath资源不存在: " + resourcePath);
                }
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PayConfig that = (PayConfig) obj;
        return Objects.equals(configId, that.configId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.format("[{}] {}-{} 租户={} appid={} mchId={}",
                paymentMethod.getLabel(),
                StringUtils.blankToDefault(mchName, "未命名"),
                StringUtils.blankToDefault(platformName, "未命名"),
                StringUtils.blankToDefault(tenantId, "未设置"),
                StringUtils.blankToDefault(appid, "未设置"),
                StringUtils.blankToDefault(mchId, "未设置")));

        if (defaultConfig) {
            sb.append(" [默认]");
        }

        if (hasApiV3Config()) {
            sb.append(" [v3]");
        }

        if (hasCompleteV3Config()) {
            sb.append(" [完整v3]");
        }

        return sb.toString();
    }
}
