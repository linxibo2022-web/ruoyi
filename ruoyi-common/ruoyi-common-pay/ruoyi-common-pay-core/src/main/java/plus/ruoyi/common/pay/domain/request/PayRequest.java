package plus.ruoyi.common.pay.domain.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.domain.bo.GoodsDetailBo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 支付请求对象
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID(微信appid、支付宝appid等)
     */
    private String appid;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 支付方式
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品详情描述(微信订单管理必填)
     * 用于微信订单中心展示商品信息
     * 格式: 商品名称 x 数量
     */
    private String description;

    /**
     * 商品详情列表
     * 用于微信/支付宝支付时传递商品明细
     */
    private List<GoodsDetailBo> goodsDetail;

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 支付金额(元)
     */
    private BigDecimal totalFee;

    /**
     * 货币类型(默认CNY人民币)
     */
    private String feeType;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 同步返回地址
     */
    private String returnUrl;

    /**
     * 交易类型
     * 微信：JSAPI-公众号支付, NATIVE-扫码支付, APP-APP支付, MWEB-H5支付
     * 支付宝：PAGE-电脑网站支付, WAP-手机网站支付, APP-APP支付,
     *        NATIVE-当面付(用户扫商家二维码), BARCODE-当面付(商家扫用户付款码)
     */
    private String tradeType;

    /**
     * 用户OpenId(微信支付JSAPI必需)
     */
    private String openId;

    /**
     * 用户子标识(微信支付服务商模式)
     */
    private String subOpenId;

    /**
     * 子商户号(服务商模式)
     */
    private String subMchId;

    /**
     * 子商户appid(服务商模式)
     */
    private String subAppid;

    /**
     * 订单失效时间
     */
    private String timeExpire;

    /**
     * 订单生成时间
     */
    private String timeStart;

    /**
     * 商品标记
     */
    private String goodsTag;


    /**
     * 场景信息
     */
    private String sceneInfo;

    /**
     * 附加数据
     */
    private String attach;

    /**
     * 签名类型
     */
    private String signType;

    /**
     * 设备号
     */
    private String deviceInfo;

    /**
     * 用户标识类型(支付宝)
     */
    private String buyerLogonId;

    /**
     * 用户ID(支付宝)
     */
    private String buyerId;

    /**
     * 卖家ID(支付宝)
     */
    private String sellerId;

    /**
     * 商户门店编号
     */
    private String storeId;

    /**
     * 操作员编号
     */
    private String operatorId;

    /**
     * 终端编号
     */
    private String terminalId;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 用户付款码(支付宝/微信条码支付必填)
     * 用户用支付宝/微信打开"付款"功能后展示的 25-30 位数字串,商家扫码枪扫描得到
     * 1 分钟有效,过期后用户需重新生成
     */
    private String authCode;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

    /**
     * 创建微信JSAPI支付请求
     */
    public static PayRequest createWxJsapiRequest(String appid, String mchId, String body,
                                                  String outTradeNo, BigDecimal totalFee,
                                                  String openId, String clientIp) {
        return PayRequest.builder()
            .appid(appid)
            .mchId(mchId)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .openId(openId)
            .clientIp(clientIp)
            .tradeType("JSAPI")
            .feeType("CNY")
            .build();
    }

    /**
     * 创建微信扫码支付请求
     */
    public static PayRequest createWxNativeRequest(String appid, String mchId, String body,
                                                   String outTradeNo, BigDecimal totalFee,
                                                   String clientIp) {
        return PayRequest.builder()
            .appid(appid)
            .mchId(mchId)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .clientIp(clientIp)
            .tradeType("NATIVE")
            .feeType("CNY")
            .build();
    }

    /**
     * 创建微信APP支付请求
     */
    public static PayRequest createWxAppRequest(String appid, String mchId, String body,
                                                String outTradeNo, BigDecimal totalFee,
                                                String clientIp) {
        return PayRequest.builder()
            .appid(appid)
            .mchId(mchId)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .clientIp(clientIp)
            .tradeType("APP")
            .feeType("CNY")
            .build();
    }

    /**
     * 创建微信H5支付请求
     * 用于在手机浏览器或APP内置浏览器中调起微信支付
     */
    public static PayRequest createWxH5Request(String appid, String mchId, String body,
                                               String outTradeNo, BigDecimal totalFee,
                                               String clientIp) {
        return PayRequest.builder()
            .appid(appid)
            .mchId(mchId)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .clientIp(clientIp)
            .tradeType("MWEB")
            .feeType("CNY")
            .build();
    }

    /**
     * 创建微信H5支付请求(带场景信息)
     * 场景信息用于风控和统计
     */
    public static PayRequest createWxH5Request(String appid, String mchId, String body,
                                               String outTradeNo, BigDecimal totalFee,
                                               String clientIp, String sceneInfo) {
        return PayRequest.builder()
            .appid(appid)
            .mchId(mchId)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .clientIp(clientIp)
            .tradeType("MWEB")
            .sceneInfo(sceneInfo)
            .feeType("CNY")
            .build();
    }

    /**
     * 创建支付宝手机网站支付请求
     */
    public static PayRequest createAlipayWapRequest(String appid, String body,
                                                    String outTradeNo, BigDecimal totalFee,
                                                    String returnUrl) {
        return PayRequest.builder()
            .appid(appid)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .returnUrl(returnUrl)
            .tradeType("WAP")
            .build();
    }

    /**
     * 创建支付宝电脑网站支付请求
     */
    public static PayRequest createAlipayPageRequest(String appid, String body,
                                                     String outTradeNo, BigDecimal totalFee,
                                                     String returnUrl) {
        return PayRequest.builder()
            .appid(appid)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .returnUrl(returnUrl)
            .tradeType("PAGE")
            .build();
    }

    /**
     * 创建支付宝APP支付请求(已移除notifyUrl参数)
     */
    public static PayRequest createAlipayAppRequest(String appid, String body,
                                                    String outTradeNo, BigDecimal totalFee) {
        return PayRequest.builder()
            .appid(appid)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .tradeType("APP")
            .build();
    }

    /**
     * 创建支付宝当面付(扫码支付)请求
     * 商家展示二维码 → 用户用支付宝扫码完成支付,适用于 PC 收银台、线下门店等场景
     * 底层调用支付宝 alipay.trade.precreate 接口
     */
    public static PayRequest createAlipayNativeRequest(String appid, String body,
                                                       String outTradeNo, BigDecimal totalFee) {
        return PayRequest.builder()
            .appid(appid)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .tradeType("NATIVE")
            .build();
    }

    /**
     * 创建支付宝当面付(条码/付款码支付)请求
     * 用户展示付款码 → 商家用扫码枪/摄像头扫描完成扣款,适用于线下门店收银
     * 底层调用支付宝 alipay.trade.pay 接口,同步返回支付结果
     */
    public static PayRequest createAlipayBarcodeRequest(String appid, String body,
                                                        String outTradeNo, BigDecimal totalFee,
                                                        String authCode) {
        return PayRequest.builder()
            .appid(appid)
            .body(body)
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .authCode(authCode)
            .tradeType("BARCODE")
            .build();
    }

    /**
     * 创建余额支付请求
     */
    public static PayRequest createBalanceRequest(String outTradeNo, BigDecimal totalFee,
                                                  String body, String clientIp) {
        return PayRequest.builder()
            .outTradeNo(outTradeNo)
            .totalFee(totalFee)
            .body(body)
            .clientIp(clientIp)
            .tradeType(DictPaymentMethod.BALANCE.getValue())
            .build();
    }

    /**
     * 验证必需参数
     */
    public boolean isValid() {
        return outTradeNo != null && !outTradeNo.trim().isEmpty()
            && totalFee != null && totalFee.compareTo(BigDecimal.ZERO) > 0;
    }
}
