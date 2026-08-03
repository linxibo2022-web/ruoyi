package plus.ruoyi.common.pay.unionpay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 银联支付交易类型枚举
 *
 * @author 抓蛙师
 */
@Getter
@AllArgsConstructor
public enum UnionpayTradeType {

    /**
     * 二维码支付 - PC端展示二维码，用户扫码支付
     */
    QRCODE("qrcode", "二维码支付"),

    /**
     * WAP支付 - 手机网页支付（H5）
     */
    WAP("wap", "手机网页支付"),

    /**
     * APP支付 - 移动端APP唤起云闪付支付
     */
    APP("app", "APP支付"),

    /**
     * PC网关支付 - 跳转到银联页面
     */
    WEB("web", "PC网关支付"),

    /**
     * 小程序支付 - 微信/支付宝小程序内支付
     */
    JSAPI("jsapi", "小程序支付");

    /**
     * 交易类型代码
     */
    private final String code;

    /**
     * 交易类型名称
     */
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static UnionpayTradeType getByCode(String code) {
        for (UnionpayTradeType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的银联交易类型: " + code);
    }
}
