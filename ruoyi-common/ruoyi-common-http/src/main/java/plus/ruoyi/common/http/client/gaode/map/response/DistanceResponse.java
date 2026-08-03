package plus.ruoyi.common.http.client.gaode.map.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 距离计算响应
 *
 * @author ye
 */
@NoArgsConstructor
@Data
public class DistanceResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("info")
    private String info;

    @JsonProperty("infocode")
    private String infocode;

    @JsonProperty("count")
    private String count;

    @JsonProperty("results")
    private List<DistanceData> results;

    @NoArgsConstructor
    @Data
    public static class DistanceData {

        @JsonProperty("origin_id")
        private String originId;

        @JsonProperty("dest_id")
        private String destId;

        @JsonProperty("distance")
        private String distance;

        @JsonProperty("duration")
        private String duration;
    }
}
