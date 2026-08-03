package plus.ruoyi.common.http.client.volcengine.tts.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 火山引擎TTS语音合成请求
 *
 * @author 抓蛙师
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolcengineTtsRequest {

    /**
     * 应用配置
     */
    @Builder.Default
    private AppConfig app = new AppConfig();

    /**
     * 用户配置
     */
    @Builder.Default
    private UserConfig user = new UserConfig();

    /**
     * 音频配置
     */
    @Builder.Default
    private AudioConfig audio = new AudioConfig();

    /**
     * 请求配置
     */
    @Builder.Default
    private RequestConfig request = new RequestConfig();

    /**
     * 应用配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppConfig {
        /**
         * 应用ID
         */
        private String appid;

        /**
         * 业务集群
         */
        private String cluster;
    }

    /**
     * 用户配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserConfig {
        /**
         * 用户ID（默认值）
         */
        @Builder.Default
        private String uid = "default_uid";
    }

    /**
     * 音频配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioConfig {
        /**
         * 音色类型
         */
        @JsonProperty("voice_type")
        private String voiceType;

        /**
         * 音频编码格式（mp3, wav, pcm）
         */
        @Builder.Default
        private String encoding = "mp3";

        /**
         * 音频采样率（Hz）
         * 16000: 16kHz语音标准
         * 24000: 24kHz默认值
         */
        @JsonProperty("sample_rate")
        private Integer sampleRate;

        /**
         * 语速比例（0.5-2.0）
         */
        @Builder.Default
        @JsonProperty("speed_ratio")
        private Double speedRatio = 1.0;

        /**
         * 音量比例（0.5-2.0）
         */
        @Builder.Default
        @JsonProperty("volume_ratio")
        private Double volumeRatio = 1.0;

        /**
         * 音调比例（0.5-2.0）
         */
        @Builder.Default
        @JsonProperty("pitch_ratio")
        private Double pitchRatio = 1.0;
    }

    /**
     * 请求配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestConfig {
        /**
         * 请求ID
         */
        @Builder.Default
        private String reqid = UUID.randomUUID().toString();

        /**
         * 待合成的文本内容
         */
        private String text;

        /**
         * 文本类型（plain=纯文本, ssml=SSML标记语言）
         */
        @Builder.Default
        @JsonProperty("text_type")
        private String textType = "plain";

        /**
         * 操作类型（query=查询）
         */
        @Builder.Default
        private String operation = "query";
    }
}
