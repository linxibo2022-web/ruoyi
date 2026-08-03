package plus.ruoyi.business.mall.domain.dto;

import lombok.Data;

/**
 * 物流信息DTO
 */
@Data
public class ShippingInfoDto {

    /**
     * 物流公司
     */
    private String company;

    /**
     * 物流单号
     */
    private String trackingNumber;

    /**
     * 发货时间
     */
    private String shipTime;

    /**
     * 静态构造方法
     */
    public static ShippingInfoDto of(String company, String trackingNumber) {
        ShippingInfoDto dto = new ShippingInfoDto();
        dto.company = company;
        dto.trackingNumber = trackingNumber;
        return dto;
    }

    /**
     * 完整构造方法
     */
    public static ShippingInfoDto of(String company, String trackingNumber, String shipTime) {
        ShippingInfoDto dto = of(company, trackingNumber);
        dto.shipTime = shipTime;
        return dto;
    }
}
