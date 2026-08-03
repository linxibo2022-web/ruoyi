package plus.ruoyi.common.http.client.volcengine.tts;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Headers;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.http.client.volcengine.tts.properties.VolcengineTtsProperties;
import plus.ruoyi.common.http.client.volcengine.tts.request.VolcengineTtsRequest;
import plus.ruoyi.common.http.client.volcengine.tts.response.VolcengineTtsResponse;

/**
 * 火山引擎TTS客户端
 *
 * @author 抓蛙师
 */
@BaseRequest(
    baseURL = "https://openspeech.bytedance.com",
    interceptor = VolcengineTtsInterceptor.class
)
public interface VolcengineTtsClient {

    /**
     * 语音合成
     *
     * @param request 合成请求
     * @return 合成响应
     */
    @Post(url = "/api/v1/tts")
    @Headers("Content-Type: application/json")
    ForestResponse<VolcengineTtsResponse> synthesize(@Body VolcengineTtsRequest request);

    /**
     * 语音合成（简化版 - 使用默认配置）
     *
     * @param text      待合成的文本
     * @param voiceType 音色类型
     * @return 合成响应
     */
    default ForestResponse<VolcengineTtsResponse> synthesize(String text, String voiceType) {
        VolcengineTtsProperties properties = SpringUtils.getBean(VolcengineTtsProperties.class);

        VolcengineTtsRequest request = VolcengineTtsRequest.builder()
            .app(VolcengineTtsRequest.AppConfig.builder()
                .appid(properties.getAppId())
                .cluster(properties.getCluster())
                .build())
            .request(VolcengineTtsRequest.RequestConfig.builder()
                .text(text)
                .build())
            .audio(VolcengineTtsRequest.AudioConfig.builder()
                .voiceType(voiceType)
                .encoding(properties.getEncoding())
                .sampleRate(properties.getSampleRate())
                .speedRatio(properties.getSpeedRatio())
                .volumeRatio(properties.getVolumeRatio())
                .pitchRatio(properties.getPitchRatio())
                .build())
            .build();
        return synthesize(request);
    }

    /**
     * 语音合成（最简版 - 使用默认音色）
     *
     * @param text 待合成的文本
     * @return 合成响应
     */
    default ForestResponse<VolcengineTtsResponse> synthesize(String text) {
        VolcengineTtsProperties properties = SpringUtils.getBean(VolcengineTtsProperties.class);

        VolcengineTtsRequest request = VolcengineTtsRequest.builder()
            .app(VolcengineTtsRequest.AppConfig.builder()
                .appid(properties.getAppId())
                .cluster(properties.getCluster())
                .build())
            .request(VolcengineTtsRequest.RequestConfig.builder()
                .text(text)
                .build())
            .audio(VolcengineTtsRequest.AudioConfig.builder()
                .voiceType(properties.getDefaultVoice())
                .encoding(properties.getEncoding())
                .sampleRate(properties.getSampleRate())
                .speedRatio(properties.getSpeedRatio())
                .volumeRatio(properties.getVolumeRatio())
                .pitchRatio(properties.getPitchRatio())
                .build())
            .build();
        return synthesize(request);
    }
}
