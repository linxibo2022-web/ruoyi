package plus.ruoyi.common.http.client.gaode.map.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 逆地理编码响应
 *
 * @author ye
 */
@NoArgsConstructor
@Data
public class ReverseGeocodingResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("info")
    private String info;

    @JsonProperty("infocode")
    private String infocode;

    @JsonProperty("regeocode")
    private ReverseGeocodingData regeocode;

    @NoArgsConstructor
    @Data
    public static class ReverseGeocodingData {

        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("addressComponent")
        private AddressComponent addressComponent;

        @NoArgsConstructor
        @Data
        public static class AddressComponent {

            @JsonProperty("country")
            private String country;

            @JsonProperty("province")
            private String province;

            @JsonProperty("city")
            private String city;

            @JsonProperty("citycode")
            private String citycode;

            @JsonProperty("district")
            private List<String> district;  // 改为 List

            @JsonProperty("adcode")
            private String adcode;

            @JsonProperty("township")
            private String township;

            @JsonProperty("towncode")
            private String towncode;

            @JsonProperty("streetNumber")
            private StreetNumber streetNumber;

            @JsonProperty("businessAreas")
            private List<BusinessArea> businessAreas;

            @JsonProperty("building")
            private BuildingInfo building;

            @JsonProperty("neighborhood")
            private NeighborhoodInfo neighborhood;

            @NoArgsConstructor
            @Data
            public static class StreetNumber {
                @JsonProperty("street")
                private String street;

                @JsonProperty("number")
                private String number;

                @JsonProperty("location")
                private String location;

                @JsonProperty("direction")
                private String direction;

                @JsonProperty("distance")
                private String distance;
            }

            @NoArgsConstructor
            @Data
            public static class BuildingInfo {

                @JsonProperty("name")
                private List<String> name;

                @JsonProperty("type")
                private List<String> type;
            }

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
            public static class BusinessArea {

                @JsonProperty("location")
                private String location;

                @JsonProperty("name")
                private String name;

                @JsonProperty("id")
                private String id;
            }
        }
    }
}
