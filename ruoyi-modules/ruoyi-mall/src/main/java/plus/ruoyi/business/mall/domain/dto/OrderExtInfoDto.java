package plus.ruoyi.business.mall.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单扩展信息DTO
 */
@Data
public class OrderExtInfoDto {

    /**
     * 订单来源
     */
    private String orderSource;

    /**
     * 配送方式
     */
    private String deliveryMethod;

    /**
     * 优惠信息
     */
    private DiscountInfoDto discount;

    /**
     * 运费
     */
    private BigDecimal shippingFee;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 小程序appid
     * 用于支付状态查询等场景
     */
    private String appid;

    /**
     * 商品详情页路径(小程序路径)
     * 用于微信订单管理跳转
     * 例如: pages/goods/detail?id=123
     */
    private String goodsDetailPath;

    /**
     * 优惠信息内部类
     */
    @Data
    public static class DiscountInfoDto {
        /**
         * 优惠券ID
         */
        private Long couponId;

        /**
         * 优惠金额
         */
        private BigDecimal discountAmount;

        public static DiscountInfoDto of(Long couponId, BigDecimal discountAmount) {
            DiscountInfoDto dto = new DiscountInfoDto();
            dto.couponId = couponId;
            dto.discountAmount = discountAmount;
            return dto;
        }
    }

    /**
     * 静态构造方法
     */
    public static OrderExtInfoDto of(String orderSource, String deliveryMethod) {
        OrderExtInfoDto dto = new OrderExtInfoDto();
        dto.orderSource = orderSource;
        dto.deliveryMethod = deliveryMethod;
        return dto;
    }

    /**
     * 完整构造方法
     */
    public static OrderExtInfoDto of(String orderSource, String deliveryMethod,
                                    DiscountInfoDto discount, BigDecimal shippingFee, String invoiceType) {
        OrderExtInfoDto dto = of(orderSource, deliveryMethod);
        dto.discount = discount;
        dto.shippingFee = shippingFee;
        dto.invoiceType = invoiceType;
        return dto;
    }
}
