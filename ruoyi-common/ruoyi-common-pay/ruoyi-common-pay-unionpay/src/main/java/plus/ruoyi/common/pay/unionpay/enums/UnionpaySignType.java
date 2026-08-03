package plus.ruoyi.common.pay.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 银联签名类型枚举
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum UnionpaySignType {

    /**
     * OAuth2.0认证 - 使用AppSecret
     */
    OAUTH2("oauth2", "OAuth2.0认证"),

    /**
     * RSA签名 - 推荐
     */
    RSA("rsa", "RSA签名"),

    /**
     * SM2国密签名
     */
    SM2("sm2", "SM2国密签名");

    /**
     * 签名类型代码
     */
    private final String code;

    /**
     * 签名类型名称
     */
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static UnionpaySignType getByCode(String code) {
        for (UnionpaySignType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return RSA; // 默认使用RSA
    }
}
