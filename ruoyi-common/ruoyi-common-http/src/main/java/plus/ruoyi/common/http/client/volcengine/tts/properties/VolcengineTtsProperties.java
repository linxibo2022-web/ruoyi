package plus.ruoyi.common.http.client.volcengine.tts.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 火山引擎TTS配置属性
 *
 * @author 抓蛙师
 */
@Data
@ConfigurationProperties(prefix = "volcengine.tts")
public class VolcengineTtsProperties {

    /**
     * 是否启用火山TTS
     */
    private Boolean enabled = false;

    /**
     * API基础URL
     */
    private String baseUrl = "https://openspeech.bytedance.com";

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 业务集群
     * volcano_tts: 使用火山引擎内置音色
     * volcano_mega: 使用复制音色
     */
    private String cluster = "volcano_tts";

    /**
     * 默认音色类型
     */
    private String defaultVoice = "BV001_streaming";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;

    /**
     * 音频编码格式
     * pcm: 未压缩格式,转Opus效果最好
     * mp3: 压缩格式
     */
    private String encoding = "pcm";

    /**
     * 音频采样率（Hz）
     * 16000: 16kHz语音标准，适合设备提示音，文件更小
     * 24000: 24kHz默认值，音质更好（推荐）
     */
    private Integer sampleRate = 24000;

    /**
     * 语速比例（0.5-2.0）
     */
    private Double speedRatio = 1.0;

    /**
     * 音量比例（0.5-2.0）
     */
    private Double volumeRatio = 1.0;

    /**
     * 音调比例（0.5-2.0）
     */
    private Double pitchRatio = 1.0;
}
