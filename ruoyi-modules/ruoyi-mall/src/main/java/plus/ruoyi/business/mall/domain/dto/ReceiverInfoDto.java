package plus.ruoyi.business.mall.domain.dto;

import lombok.Data;

/**
 * 收货信息DTO
 */
@Data
public class ReceiverInfoDto {

    /**
     * 收货人姓名
     */
    private String name;

    /**
     * 收货人电话
     */
    private String phone;

    /**
     * 地区
     */
    private String area;

    /**
     * 地区编码
     */
    private String areaCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 静态构造方法
     */
    public static ReceiverInfoDto of(String name, String phone, String area, String areaCode, String address) {
        ReceiverInfoDto dto = new ReceiverInfoDto();
        dto.name = name;
        dto.phone = phone;
        dto.area = area;
        dto.areaCode = areaCode;
        dto.address = address;
        return dto;
    }
}
