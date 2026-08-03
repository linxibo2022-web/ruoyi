package plus.ruoyi.common.http.client.gaode.map.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 地理编码响应
 *
 * @author ye
 */
@NoArgsConstructor
@Data
public class GeocodingResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("info")
    private String info;

    @JsonProperty("infocode")
    private String infocode;

    @JsonProperty("count")
    private String count;

    @JsonProperty("geocodes")
    private List<GeocodingData> geocodes;

    @NoArgsConstructor
    @Data
    public static class GeocodingData {

        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("country")
        private String country;

        @JsonProperty("province")
        private String province;

        @JsonProperty("citycode")
        private String citycode;

        @JsonProperty("city")
        private String city;

        @JsonProperty("district")
        private List<String> district;

        @JsonProperty("township")
        private List<String> township;

        @JsonProperty("neighborhood")
        private NeighborhoodInfo neighborhood;

        @JsonProperty("building")
        private BuildingInfo building;

        @JsonProperty("adcode")
        private String adcode;

        @JsonProperty("street")
        private List<String> street;

        @JsonProperty("number")
        private List<String> number;

        @JsonProperty("location")
        private String location;

        @JsonProperty("level")
        private String level;

        @NoArgsConstructor
        @Data
        public static class NeighborhoodInfo {
            @JsonProperty("name")
            private List<String> name;

            @JsonProperty("type")
            private List<String> type;
        }

        @NoArgsConstructor
        @Data
        public static class BuildingInfo {
            @JsonProperty("name")
            private List<String> name;

            @JsonProperty("type")
            private List<String> type;
        }
    }
}
