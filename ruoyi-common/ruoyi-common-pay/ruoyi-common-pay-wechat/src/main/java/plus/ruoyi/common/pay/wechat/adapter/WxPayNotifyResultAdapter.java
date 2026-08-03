package plus.ruoyi.common.pay.wechat.adapter;

import java.math.BigDecimal;

/**
 * 微信支付回调结果适配器接口
 *
 * 用于统一v2和v3回调数据的获取方式,解决:
 * 1. 字段名不一致 (mchId vs mchid)
 * 2. 字段路径不一致 (openid直接字段 vs payer.openid)
 * 3. 金额单位不一致 (v2需要转换,v3也需要转换)
 * 4. 空值安全问题
 *
 * @author 抓蛙师
 */
public interface WxPayNotifyResultAdapter {

    /**
     * 获取商户订单号
     */
    String getOutTradeNo();

    /**
     * 获取微信支付订单号
     */
    String getTransactionId();

    /**
     * 获取订单金额(元)
     * <p>
     * ⚠️ 注意: 统一返回元为单位,内部自动从分转换
     */
    BigDecimal getTotalFeeInYuan();

    /**
     * 获取商户号
     */
    String getMchId();

    /**
     * 获取应用ID
     */
    String getAppId();

    /**
     * 获取用户OpenID
     * <p>
     * v2: 直接获取
     * v3: 从Payer对象获取,带空值保护
     */
    String getOpenId();

    /**
     * 获取附加数据
     */
    String getAttach();

    /**
     * 获取货币类型
     * <p>
     * v2: fee_type字段
     * v3: amount.currency字段
     * 默认: CNY
     */
    default String getCurrency() {
        return "CNY";
    }
}
