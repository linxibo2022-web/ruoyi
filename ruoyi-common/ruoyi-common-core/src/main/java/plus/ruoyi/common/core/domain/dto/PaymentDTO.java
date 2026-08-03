package plus.ruoyi.common.core.domain.dto;

import lombok.Data;
import plus.ruoyi.common.core.constant.Constants;
import plus.ruoyi.common.core.dict.DictEnableStatus;
import plus.ruoyi.common.core.utils.SpringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 支付配置数据传输对象
 *
 * @author 抓蛙师
 */
@Data
public class PaymentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 支付配置id
     */
    private Long id;

    /**
     * 商户类型
     */
    private String type;

    /**
     * 商户名称
     */
    private String mchName;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户密钥
     */
    private String mchKey;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 证书路径
     */
    private String certPath;

    /**
     * 密钥路径
     */
    private String keyPath;

    /**
     * 平台证书路径（用于验证回调签名）
     */
    private String platformCertPath;

    /**
     * 证书序列号
     */
    private String certSerialNo;

    /**
     * 微信支付公钥ID(V3公钥模式专用，格式 PUB_KEY_ID_xxx，从微信商户平台获取，与证书序列号不同)
     */
    private String publicKeyId;

    /**
     * p12证书路径(V2退款专用)
     */
    private String p12CertPath;

    /**
     * 状态（1-启用，0-禁用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 租户编号
     */
    private String tenantId;

    // ============= 便捷方法 =============

    /**
     * 判断是否为启用状态
     */
    public boolean isEnabled() {
        return DictEnableStatus.ENABLE.getValue().equals(this.status);
    }

    /**
     * 判断是否为微信支付
     */
    public boolean isWechatPayment() {
        return "WECHAT".equals(this.type);
    }

    /**
     * 判断是否为支付宝支付
     */
    public boolean isAlipayPayment() {
        return "ALIPAY".equals(this.type);
    }

    /**
     * 判断是否为银联支付
     */
    public boolean isUnionpayPayment() {
        return "UNIONPAY".equals(this.type);
    }

    /**
     * 判断是否为余额支付
     */
    public boolean isBalancePayment() {
        return "BALANCE".equals(this.type);
    }

    /**
     * 判断是否配置了APIv3
     */
    public boolean hasApiV3Config() {
        return apiV3Key != null && !apiV3Key.trim().isEmpty()
            && certSerialNo != null && !certSerialNo.trim().isEmpty();
    }

    /**
     * 判断是否有完整的v3配置（包含平台证书）
     */
    public boolean hasCompleteV3Config() {
        return hasApiV3Config()
            && certPath != null && !certPath.trim().isEmpty()
            && keyPath != null && !keyPath.trim().isEmpty()
            && platformCertPath != null && !platformCertPath.trim().isEmpty();
    }

    /**
     * 掩码处理商户密钥（用于日志输出）
     *
     * @return 掩码后的密钥
     */
    public String getMaskedMchKey() {
        if (mchKey == null || mchKey.length() <= 8) {
            return "****";
        }
        return mchKey.substring(0, 4) + "****" + mchKey.substring(mchKey.length() - 4);
    }

    /**
     * 掩码处理APIv3密钥（用于日志输出）
     *
     * @return 掩码后的密钥
     */
    public String getMaskedApiV3Key() {
        if (apiV3Key == null || apiV3Key.length() <= 8) {
            return "****";
        }
        return apiV3Key.substring(0, 4) + "****" + apiV3Key.substring(apiV3Key.length() - 4);
    }

    /**
     * 检查必要的配置项是否完整
     *
     * @return true-配置完整，false-配置不完整
     */
    public boolean isConfigComplete() {
        // 基础配置检查
        if (mchId == null || mchId.trim().isEmpty()) {
            return false;
        }
        if (mchName == null || mchName.trim().isEmpty()) {
            return false;
        }

        if (type == null || type.trim().isEmpty()) {
            return false;
        }

        // 根据支付类型验证不同的必要参数
        return switch (type.toLowerCase()) {
            case "wechat" -> {
                // 微信支付需要商户密钥和证书
                boolean basicValid = mchKey != null && !mchKey.trim().isEmpty()
                    && certPath != null && !certPath.trim().isEmpty()
                    && keyPath != null && !keyPath.trim().isEmpty();

                // 如果有v3配置，验证v3完整性（包括平台证书）
                if (hasApiV3Config()) {
                    yield basicValid && platformCertPath != null && !platformCertPath.trim().isEmpty();
                }
                yield basicValid;
            }
            case "alipay" ->
                // 支付宝支付需要商户密钥（实际上是应用私钥，但存在mchKey字段中）
                mchKey != null && !mchKey.trim().isEmpty();
            case "unionpay" ->
                // 银联支付需要商户密钥和证书
                mchKey != null && !mchKey.trim().isEmpty()
                    && certPath != null && !certPath.trim().isEmpty();
            case "balance" ->
                // 余额支付不需要额外配置
                true;
            default -> false;
        };
    }

    /**
     * 获取配置摘要信息（用于日志）
     *
     * @return 配置摘要
     */
    public String getConfigSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("商户[").append(mchName).append("]");
        summary.append("(").append(mchId).append(")");
        summary.append(" 类型:").append(type != null ? type : "未知");
        summary.append(" 状态:").append("1".equals(status) ? "启用" : "禁用");

        if (hasApiV3Config()) {
            summary.append(" [支持APIv3]");
        }
        if (hasCompleteV3Config()) {
            summary.append(" [完整v3配置]");
        }
        if (p12CertPath != null && !p12CertPath.trim().isEmpty()) {
            summary.append(" [支持V2退款]");
        }
        return summary.toString();
    }

    @Override
    public String toString() {
        return "PaymentDTO{" +
            "id=" + id +
            ", mchName='" + mchName + '\'' +
            ", mchId='" + mchId + '\'' +
            ", paymentType='" + type + '\'' +
            ", mchKey='" + getMaskedMchKey() + '\'' +
            ", status='" + status + '\'' +
            ", hasApiV3=" + hasApiV3Config() +
            ", hasCompleteV3=" + hasCompleteV3Config() +
            ", hasP12Cert=" + (p12CertPath != null && !p12CertPath.trim().isEmpty()) +
            '}';
    }
}
