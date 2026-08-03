package plus.ruoyi.common.pay.domain.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 支付回调请求对象
 *
 * @author 抓蛙师
 */
@Data
@Builder
public class NotifyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * XML数据(微信支付v2回调)
     */
    private String xmlData;

    /**
     * JSON数据(微信支付v3回调)
     */
    private String jsonData;

    /**
     * 表单数据(支付宝、银联等回调)
     */
    private Map<String, String> formData;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 应用ID
     */
    private String appid;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 请求时间戳
     */
    private Long requestTime;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 回调类型
     * PAY-支付回调
     * REFUND-退款回调
     */
    private String notifyType;

    /**
     * 签名方式
     */
    private String signType;

    /**
     * 字符编码
     */
    private String charset;

    /**
     * 回调版本
     */
    private String version;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

    /**
     * 微信v3回调专用字段 - 请求头
     */
    private Map<String, String> headers;

    /**
     * 创建微信支付v2回调请求
     */
    public static NotifyRequest createWxPayNotifyRequest(String xmlData, String mchId, String clientIp) {
        return NotifyRequest.builder()
            .xmlData(xmlData)
            .mchId(mchId)
            .clientIp(clientIp)
            .requestTime(System.currentTimeMillis())
            .notifyType("PAY")
            .build();
    }

    /**
     * 创建微信支付v3回调请求
     *
     * @param jsonData 回调JSON数据
     * @param mchId    商户号
     * @param clientIp 客户端IP
     * @param headers  微信v3回调请求头(包含签名信息)
     */
    public static NotifyRequest createWxPayV3NotifyRequest(
        String jsonData,
        String mchId,
        String clientIp,
        Map<String, String> headers) {
        return NotifyRequest.builder()
            .jsonData(jsonData)
            .mchId(mchId)
            .clientIp(clientIp)
            .headers(headers)
            .requestTime(System.currentTimeMillis())
            .notifyType("PAY")
            .build();
    }

    /**
     * 创建支付宝回调请求
     */
    public static NotifyRequest createAlipayNotifyRequest(Map<String, String> formData,
                                                         String appId, String clientIp) {
        return NotifyRequest.builder()
            .formData(formData)
            .mchId(appId) // 支付宝使用appId
            .clientIp(clientIp)
            .requestTime(System.currentTimeMillis())
            .notifyType("PAY")
            .build();
    }

    /**
     * 创建退款回调请求
     */
    public static NotifyRequest createRefundNotifyRequest(String xmlData, String mchId, String clientIp) {
        return NotifyRequest.builder()
            .xmlData(xmlData)
            .mchId(mchId)
            .clientIp(clientIp)
            .requestTime(System.currentTimeMillis())
            .notifyType("REFUND")
            .build();
    }

    /**
     * 是否为支付回调
     */
    public boolean isPayNotify() {
        return "PAY".equals(notifyType);
    }

    /**
     * 是否为退款回调
     */
    public boolean isRefundNotify() {
        return "REFUND".equals(notifyType);
    }

    /**
     * 是否有XML数据
     */
    public boolean hasXmlData() {
        return xmlData != null && !xmlData.trim().isEmpty();
    }

    /**
     * 是否有JSON数据
     */
    public boolean hasJsonData() {
        return jsonData != null && !jsonData.trim().isEmpty();
    }

    /**
     * 是否有表单数据
     */
    public boolean hasFormData() {
        return formData != null && !formData.isEmpty();
    }
}
