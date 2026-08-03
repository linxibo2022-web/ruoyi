package plus.ruoyi.common.pay.wechat.adapter;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import lombok.RequiredArgsConstructor;
import plus.ruoyi.common.pay.utils.PayUtils;

import java.math.BigDecimal;

/**
 * 微信支付v2回调结果适配器
 *
 * 将 WxPayOrderNotifyResult 适配为统一接口
 *
 * @author 抓蛙师
 */
@RequiredArgsConstructor
public class WxPayV2NotifyResultAdapter implements WxPayNotifyResultAdapter {

    private final WxPayOrderNotifyResult result;

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
        // v2返回的是分,需要转换为元
        Integer totalFee = result.getTotalFee();
        return totalFee != null ? PayUtils.fenToYuan(totalFee) : BigDecimal.ZERO;
    }

    @Override
    public String getMchId() {
        return result.getMchId();
    }

    @Override
    public String getAppId() {
        return result.getAppid();
    }

    @Override
    public String getOpenId() {
        return result.getOpenid();
    }

    @Override
    public String getAttach() {
        return result.getAttach();
    }

    @Override
    public String getCurrency() {
        // v2有fee_type字段,默认CNY
        String feeType = result.getFeeType();
        return feeType != null ? feeType : "CNY";
    }
}
