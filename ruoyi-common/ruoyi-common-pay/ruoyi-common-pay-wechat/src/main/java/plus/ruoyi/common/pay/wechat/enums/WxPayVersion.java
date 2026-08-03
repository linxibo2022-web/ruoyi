package plus.ruoyi.common.pay.wechat.enums;

import lombok.Getter;

/**
 * 微信支付版本枚举
 *
 * @author 抓蛙师
 */
@Getter
public enum WxPayVersion {

    /**
     * 微信支付 v2 API
     */
    V2("v2", "微信支付v2"),

    /**
     * 微信支付 v3 API
     */
    V3("v3", "微信支付v3");

    /**
     * 版本值
     */
    private final String value;

    /**
     * 版本描述
     */
    private final String description;

    WxPayVersion(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static WxPayVersion fromValue(String value) {
        for (WxPayVersion version : values()) {
            if (version.getValue().equals(value)) {
                return version;
            }
        }
        return V2; // 默认v2
    }
}
