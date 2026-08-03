package plus.ruoyi.common.http.client.volcengine.tts.response;

import lombok.Data;

/**
 * 火山引擎TTS语音合成响应
 *
 * @author 抓蛙师
 */
@Data
public class VolcengineTtsResponse {

    /**
     * 成功响应码
     */
    public static final int CODE_SUCCESS = 3000;

    /**
     * 响应码（3000=成功）
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 请求ID
     */
    private String reqid;

    /**
     * 响应数据（Base64编码的音频数据）
     */
    private String data;

    /**
     * 附加信息
     */
    private Addition addition;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == CODE_SUCCESS;
    }

    /**
     * 附加信息
     */
    @Data
    public static class Addition {
        /**
         * 音频时长（毫秒）
         */
        private String duration;

        /**
         * 首包时间（毫秒）
         */
        private String firstPkg;
    }
}
