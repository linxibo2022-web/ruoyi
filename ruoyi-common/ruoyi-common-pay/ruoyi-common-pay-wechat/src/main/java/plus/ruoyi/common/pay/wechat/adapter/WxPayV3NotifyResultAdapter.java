package plus.ruoyi.common.pay.wechat.adapter;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.pay.utils.PayUtils;

import java.math.BigDecimal;

/**
 * 微信支付v3回调结果适配器
 *
 * 将 WxPayNotifyV3Result.DecryptNotifyResult 适配为统一接口
 *
 * @author 抓蛙师
 */
@RequiredArgsConstructor
public class WxPayV3NotifyResultAdapter implements WxPayNotifyResultAdapter {

    private final WxPayNotifyV3Result.DecryptNotifyResult result;

    @Override
    public String getOutTradeNo() {
        return result.getOutTradeNo();
    }

    @Override
    public String getTransactionId() {
        return result.getTransactionId();
    }

    @Override
    public BigDecimal getTotalFeeInYuan() {
        // v3的amount.total是分,需要转换为元
        if (result.getAmount() != null && result.getAmount().getTotal() != null) {
            return PayUtils.fenToYuan(result.getAmount().getTotal());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String getMchId() {
        // v3的字段名是 mchid (小写id)
        return result.getMchid();
    }

    @Override
    public String getAppId() {
        return result.getAppid();
    }

    @Override
    public String getOpenId() {
        // v3的openid在payer对象中,需要空值保护
        if (result.getPayer() != null) {
            return result.getPayer().getOpenid();
        }
        return null;
    }

    @Override
    public String getAttach() {
        return result.getAttach();
    }

    @Override
    public String getCurrency() {
        // v3的currency在amount对象中
        if (result.getAmount() != null && result.getAmount().getCurrency() != null) {
            return result.getAmount().getCurrency();
        }
        return "CNY";
    }
}
