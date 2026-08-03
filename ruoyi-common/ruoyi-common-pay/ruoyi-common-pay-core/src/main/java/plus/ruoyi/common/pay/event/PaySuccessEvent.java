package plus.ruoyi.common.pay.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;


/**
 * 支付成功事件(解决Builder和ApplicationEvent冲突)
 * <p>
 * 当支付回调验证成功后发布此事件，业务系统可以监听此事件进行后续处理
 * 如：订单状态更新、库存扣减、积分发放、消息通知等
 *
 * @author 抓蛙师
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PaySuccessEvent extends ApplicationEvent {

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 第三方交易号(微信支付的transaction_id、支付宝的trade_no等)
     */
    private String transactionId;

    /**
     * 支付金额(单位：元)
     * 微信支付和支付宝均已统一转换为元
     */
    private String totalFee;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 应用ID(微信的appid、支付宝的app_id等)
     */
    private String appId;

    /**
     * 用户标识(微信的openid、支付宝的buyer_id等)
     */
    private String openId;

    /**
     * 支付方式标识
     *
     * @see plus.ruoyi.common.core.dict.DictPaymentMethod
     */
    private String paymentMethod;

    /**
     * 支付完成时间(第三方返回的时间戳或格式化时间)
     */
    private String payTime;

    /**
     * 币种(默认CNY人民币)
     */
    private String currency;

    /**
     * 附加数据(商户自定义数据)
     */
    private String attach;

    /**
     * 基础构造函数(ApplicationEvent要求)
     */
    public PaySuccessEvent(Object source) {
        super(source);
    }

    /**
     * 完整构造函数
     */
    public PaySuccessEvent(Object source, String outTradeNo, String transactionId,
                           String totalFee, String mchId, String appId, String openId,
                           String paymentMethod, String payTime, String currency, String attach) {
        super(source);
        this.outTradeNo = outTradeNo;
        this.transactionId = transactionId;
        this.totalFee = totalFee;
        this.mchId = mchId;
        this.appId = appId;
        this.openId = openId;
        this.paymentMethod = paymentMethod;
        this.payTime = payTime;
        this.currency = currency;
        this.attach = attach;
    }

    /**
     * 创建建造器(自定义Builder模式)
     */
    public static PaymentSuccessEventBuilder builder() {
        return new PaymentSuccessEventBuilder();
    }

    /**
     * 自定义建造器类
     */
    public static class PaymentSuccessEventBuilder {
        private Object source;
        private String outTradeNo;
        private String transactionId;
        private String totalFee;
        private String mchId;
        private String appId;
        private String openId;
        private String paymentMethod;
        private String payTime;
        private String currency;
        private String attach;

        PaymentSuccessEventBuilder() {
        }

        public PaymentSuccessEventBuilder source(Object source) {
            this.source = source;
            return this;
        }

        public PaymentSuccessEventBuilder outTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
            return this;
        }

        public PaymentSuccessEventBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentSuccessEventBuilder totalFee(String totalFee) {
            this.totalFee = totalFee;
            return this;
        }

        public PaymentSuccessEventBuilder mchId(String mchId) {
            this.mchId = mchId;
            return this;
        }

        public PaymentSuccessEventBuilder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public PaymentSuccessEventBuilder openId(String openId) {
            this.openId = openId;
            return this;
        }

        public PaymentSuccessEventBuilder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentSuccessEventBuilder payTime(String payTime) {
            this.payTime = payTime;
            return this;
        }

        public PaymentSuccessEventBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentSuccessEventBuilder attach(String attach) {
            this.attach = attach;
            return this;
        }

        public PaySuccessEvent build() {
            return new PaySuccessEvent(source, outTradeNo, transactionId, totalFee,
                mchId, appId, openId, paymentMethod, payTime, currency, attach);
        }

        public String toString() {
            return "PaySuccessEvent.PaymentSuccessEventBuilder(source=" + this.source +
                ", outTradeNo=" + this.outTradeNo + ", transactionId=" + this.transactionId +
                ", totalFee=" + this.totalFee + ", mchId=" + this.mchId +
                ", appId=" + this.appId + ", openId=" + this.openId +
                ", paymentMethod=" + this.paymentMethod + ", payTime=" + this.payTime +
                ", currency=" + this.currency + ", attach=" + this.attach + ")";
        }
    }

    /**
     * 获取支付金额(元)
     * totalFee 已统一为元，直接返回
     *
     * @return 支付金额(元)
     */
    public String getTotalAmountYuan() {
        if (totalFee == null || totalFee.trim().isEmpty()) {
            return "0.00";
        }

        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(totalFee);
            return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * 获取支付金额(分)
     * totalFee 已统一为元，需要乘以100转换为分
     *
     * @return 支付金额(分)
     */
    public Long getTotalAmountFen() {
        if (totalFee == null || totalFee.trim().isEmpty()) {
            return 0L;
        }

        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(totalFee);
            return amount.multiply(java.math.BigDecimal.valueOf(100)).longValue();
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 检查是否为微信支付
     */
    public boolean isWechatPay() {
        return "wechat".equalsIgnoreCase(paymentMethod);
    }

    /**
     * 检查是否为支付宝支付
     */
    public boolean isAlipay() {
        return "alipay".equalsIgnoreCase(paymentMethod);
    }

    /**
     * 检查是否为银联支付
     */
    public boolean isUnionpay() {
        return "unionpay".equalsIgnoreCase(paymentMethod);
    }

    /**
     * 检查是否为余额支付
     */
    public boolean isBalance() {
        return "balance".equalsIgnoreCase(paymentMethod);
    }

    /**
     * 获取事件摘要信息
     */
    public String getSummary() {
        return String.format("PaySuccessEvent[outTradeNo=%s, transactionId=%s, paymentMethod=%s, totalFee=%s, appId=%s]",
            outTradeNo, transactionId, paymentMethod, totalFee, appId);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
