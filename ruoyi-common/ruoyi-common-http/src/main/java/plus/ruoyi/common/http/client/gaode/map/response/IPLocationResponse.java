package plus.ruoyi.common.http.client.gaode.map.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IP位置查询响应
 *
 * @author ye
 */
@NoArgsConstructor
@Data
public class IPLocationResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("info")
    private String info;

    @JsonProperty("infocode")
    private String infocode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("province")
    private String province;

    @JsonProperty("city")
    private String city;

    @JsonProperty("district")
    private String district;

    @JsonProperty("isp")
    private String isp;

    @JsonProperty("location")
    private String location;

    @JsonProperty("ip")
    private String ip;
}
