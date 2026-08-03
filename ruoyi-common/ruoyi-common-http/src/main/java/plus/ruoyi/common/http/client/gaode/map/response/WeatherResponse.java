package plus.ruoyi.common.http.client.gaode.map.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 天气查询响应
 *
 * @author ye
 */
@NoArgsConstructor
@Data
public class WeatherResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("count")
    private String count;

    @JsonProperty("info")
    private String info;

    @JsonProperty("infocode")
    private String infocode;

    @JsonProperty("lives")
    private List<WeatherData> lives;

    @NoArgsConstructor
    @Data
    public static class WeatherData {

        @JsonProperty("province")
        private String province;

        @JsonProperty("city")
        private String city;

        @JsonProperty("adcode")
        private String adcode;

        @JsonProperty("weather")
        private String weather;

        @JsonProperty("temperature")
        private String temperature;

        @JsonProperty("winddirection")
        private String winddirection;

        @JsonProperty("windpower")
        private String windpower;

        @JsonProperty("humidity")
        private String humidity;

        @JsonProperty("reporttime")
        private String reporttime;
    }
}
