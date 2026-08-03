package plus.ruoyi.common.http;

import cn.hutool.core.lang.Console;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.ruoyi.common.http.client.volcengine.tts.VolcengineTtsClient;
import plus.ruoyi.common.http.client.volcengine.tts.properties.VolcengineTtsProperties;
import plus.ruoyi.common.http.client.volcengine.tts.request.VolcengineTtsRequest;
import plus.ruoyi.common.http.client.volcengine.tts.response.VolcengineTtsResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 火山引擎TTS客户端测试
 * <p>
 * 测试前需配置环境变量:
 * <ul>
 *   <li>VOLCENGINE_TTS_ENABLED=true</li>
 *   <li>VOLCENGINE_TTS_APP_ID=你的AppId</li>
 *   <li>VOLCENGINE_TTS_ACCESS_TOKEN=你的AccessToken</li>
 * </ul>
 *
 * @author 抓蛙师
 */
@Slf4j
@SpringBootTest
@DisplayName("火山引擎TTS客户端测试")
public class VolcengineTtsTest {

    @Autowired(required = false)
    private VolcengineTtsClient ttsClient;

    @Autowired
    private VolcengineTtsProperties ttsProperties;

    /**
     * 检查TTS服务是否已配置并启用
     */
    boolean isTtsEnabled() {
        return ttsProperties != null
            && Boolean.TRUE.equals(ttsProperties.getEnabled())
            && ttsProperties.getAppId() != null
            && !ttsProperties.getAppId().trim().isEmpty()
            && ttsProperties.getAccessToken() != null
            && !ttsProperties.getAccessToken().trim().isEmpty();
    }

    @Test
    @DisplayName("测试配置属性加载")
    void testPropertiesLoaded() {
        Assertions.assertNotNull(ttsProperties, "TTS配置属性应该被加载");
        Console.log("TTS配置: enabled={}, appId={}, cluster={}, defaultVoice={}",
            ttsProperties.getEnabled(),
            ttsProperties.getAppId() != null ? "***" : "未配置",
            ttsProperties.getCluster(),
            ttsProperties.getDefaultVoice());
    }

    @Test
    @DisplayName("测试请求对象构建")
    void testRequestBuilder() {
        VolcengineTtsRequest request = VolcengineTtsRequest.builder()
            .app(VolcengineTtsRequest.AppConfig.builder()
                .appid("test-app-id")
                .cluster("volcano_tts")
                .build())
            .request(VolcengineTtsRequest.RequestConfig.builder()
                .text("测试文本")
                .textType("plain")
                .build())
            .audio(VolcengineTtsRequest.AudioConfig.builder()
                .voiceType("BV001_streaming")
                .encoding("pcm")
                .sampleRate(24000)
                .speedRatio(1.0)
                .volumeRatio(1.0)
                .pitchRatio(1.0)
                .build())
            .build();

        Assertions.assertNotNull(request);
        Assertions.assertEquals("test-app-id", request.getApp().getAppid());
        Assertions.assertEquals("volcano_tts", request.getApp().getCluster());
        Assertions.assertEquals("测试文本", request.getRequest().getText());
        Assertions.assertEquals("BV001_streaming", request.getAudio().getVoiceType());
        Assertions.assertEquals("pcm", request.getAudio().getEncoding());
        Assertions.assertEquals(24000, request.getAudio().getSampleRate());

        Console.log("请求对象构建测试通过: {}", request);
    }

    @Test
    @DisplayName("测试响应对象")
    void testResponse() {
        VolcengineTtsResponse response = new VolcengineTtsResponse();
        response.setCode(VolcengineTtsResponse.CODE_SUCCESS); // 3000
        response.setMessage("Success");
        response.setReqid("test-req-id");
        response.setData("dGVzdC1hdWRpby1kYXRh"); // Base64编码的测试数据

        Assertions.assertTrue(response.isSuccess());
        Assertions.assertEquals(3000, response.getCode());
        Assertions.assertEquals("Success", response.getMessage());
        Assertions.assertNotNull(response.getData());

        // 测试失败响应
        VolcengineTtsResponse failResponse = new VolcengineTtsResponse();
        failResponse.setCode(1001);
        failResponse.setMessage("Invalid token");
        Assertions.assertFalse(failResponse.isSuccess());

        Console.log("响应对象测试通过");
    }

    @Test
    @DisplayName("测试语音合成 - 最简版")
    @EnabledIf("isTtsEnabled")
    void testSynthesizeSimple() {
        Assertions.assertNotNull(ttsClient, "TTS客户端应该被注入");

        String text = "你好，这是火山引擎语音合成测试。";
        ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(text);

        Assertions.assertNotNull(response, "响应不应为空");

        if (response.isSuccess()) {
            VolcengineTtsResponse ttsResponse = response.getResult();
            Assertions.assertNotNull(ttsResponse, "TTS响应不应为空");

            if (ttsResponse.isSuccess()) {
                Assertions.assertNotNull(ttsResponse.getData(), "音频数据不应为空");
                byte[] audioData = Base64.getDecoder().decode(ttsResponse.getData());
                Assertions.assertTrue(audioData.length > 0, "音频数据长度应大于0");
                Console.log("语音合成成功，音频数据大小: {} bytes", audioData.length);
            } else {
                Console.log("TTS服务返回错误: code={}, message={}",
                    ttsResponse.getCode(), ttsResponse.getMessage());
            }
        } else {
            Console.log("HTTP请求失败: status={}", response.getStatusCode());
        }
    }

    @Test
    @DisplayName("测试语音合成 - 指定音色")
    @EnabledIf("isTtsEnabled")
    void testSynthesizeWithVoice() {
        Assertions.assertNotNull(ttsClient, "TTS客户端应该被注入");

        String text = "这是指定音色的语音合成测试。";
        String voiceType = "BV001_streaming";

        ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(text, voiceType);

        Assertions.assertNotNull(response, "响应不应为空");

        if (response.isSuccess() && response.getResult() != null && response.getResult().isSuccess()) {
            byte[] audioData = Base64.getDecoder().decode(response.getResult().getData());
            Console.log("指定音色合成成功，音色: {}, 音频大小: {} bytes", voiceType, audioData.length);
        }
    }

    @Test
    @DisplayName("测试语音合成 - 完整配置")
    @EnabledIf("isTtsEnabled")
    void testSynthesizeWithFullConfig() {
        Assertions.assertNotNull(ttsClient, "TTS客户端应该被注入");

        VolcengineTtsRequest request = VolcengineTtsRequest.builder()
            .app(VolcengineTtsRequest.AppConfig.builder()
                .appid(ttsProperties.getAppId())
                .cluster(ttsProperties.getCluster())
                .build())
            .request(VolcengineTtsRequest.RequestConfig.builder()
                .text("这是完整配置的语音合成测试，语速加快，音量提高。")
                .textType("plain")
                .build())
            .audio(VolcengineTtsRequest.AudioConfig.builder()
                .voiceType(ttsProperties.getDefaultVoice())
                .encoding("pcm")
                .sampleRate(24000)
                .speedRatio(1.2)
                .volumeRatio(1.5)
                .pitchRatio(1.0)
                .build())
            .build();

        ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(request);

        Assertions.assertNotNull(response, "响应不应为空");

        if (response.isSuccess() && response.getResult() != null && response.getResult().isSuccess()) {
            byte[] audioData = Base64.getDecoder().decode(response.getResult().getData());
            Console.log("完整配置合成成功，音频大小: {} bytes", audioData.length);
        }
    }

    @Test
    @DisplayName("测试长文本语音合成")
    @EnabledIf("isTtsEnabled")
    void testSynthesizeLongText() {
        Assertions.assertNotNull(ttsClient, "TTS客户端应该被注入");

        String longText = """
            火山引擎是字节跳动旗下的企业级技术服务平台，
            提供云计算、大数据、人工智能等多种技术服务。
            语音合成是其中的重要能力之一，
            支持多种音色和语言，
            可以将文本转换为自然流畅的语音。
            """;

        ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(longText);

        Assertions.assertNotNull(response, "响应不应为空");

        if (response.isSuccess() && response.getResult() != null && response.getResult().isSuccess()) {
            byte[] audioData = Base64.getDecoder().decode(response.getResult().getData());
            Console.log("长文本合成成功，文本长度: {}, 音频大小: {} bytes",
                longText.length(), audioData.length);
        }
    }

    @Test
    @DisplayName("测试语音合成 - 生成MP3文件")
    @EnabledIf("isTtsEnabled")
    void testSynthesizeToMp3File() throws IOException {
        Assertions.assertNotNull(ttsClient, "TTS客户端应该被注入");

        String text = "你好，这是火山引擎语音合成测试，本次测试将生成MP3音频文件。";

        // 使用 mp3 编码格式
        VolcengineTtsRequest request = VolcengineTtsRequest.builder()
            .app(VolcengineTtsRequest.AppConfig.builder()
                .appid(ttsProperties.getAppId())
                .cluster(ttsProperties.getCluster())
                .build())
            .request(VolcengineTtsRequest.RequestConfig.builder()
                .text(text)
                .build())
            .audio(VolcengineTtsRequest.AudioConfig.builder()
                .voiceType(ttsProperties.getDefaultVoice())
                .encoding("mp3")  // 使用 mp3 编码
                .sampleRate(24000)
                .speedRatio(1.0)
                .volumeRatio(1.0)
                .pitchRatio(1.0)
                .build())
            .build();

        ForestResponse<VolcengineTtsResponse> response = ttsClient.synthesize(request);

        Assertions.assertNotNull(response, "响应不应为空");

        if (response.isSuccess()) {
            VolcengineTtsResponse ttsResponse = response.getResult();
            Assertions.assertNotNull(ttsResponse, "TTS响应不应为空");

            if (ttsResponse.isSuccess()) {
                Assertions.assertNotNull(ttsResponse.getData(), "音频数据不应为空");

                // 解码 Base64 音频数据
                byte[] audioData = Base64.getDecoder().decode(ttsResponse.getData());
                Assertions.assertTrue(audioData.length > 0, "音频数据长度应大于0");

                // 保存为 MP3 文件
                Path outputDir = Paths.get("target", "test-output");
                Files.createDirectories(outputDir);
                Path mp3File = outputDir.resolve("tts_output_" + System.currentTimeMillis() + ".mp3");
                Files.write(mp3File, audioData);

                Console.log("MP3文件生成成功: {}", mp3File.toAbsolutePath());
                Console.log("文件大小: {} bytes", audioData.length);

                // 验证文件存在
                Assertions.assertTrue(Files.exists(mp3File), "MP3文件应该存在");
                Assertions.assertTrue(Files.size(mp3File) > 0, "MP3文件大小应大于0");
            } else {
                Console.log("TTS服务返回错误: code={}, message={}",
                    ttsResponse.getCode(), ttsResponse.getMessage());
                Assertions.fail("TTS合成失败: " + ttsResponse.getMessage());
            }
        } else {
            Console.log("HTTP请求失败: status={}", response.getStatusCode());
            Assertions.fail("HTTP请求失败: " + response.getStatusCode());
        }
    }
}
