package plus.ruoyi.common.http.client.volcengine.tts;

import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.ForestInterceptor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.http.client.volcengine.tts.properties.VolcengineTtsProperties;

/**
 * 火山引擎TTS请求拦截器
 * 自动添加Authorization头并验证配置
 *
 * @author 抓蛙师
 */
@Slf4j
public class VolcengineTtsInterceptor implements ForestInterceptor {

    /**
     * 请求发送前的处理
     *
     * @param req Forest请求对象
     * @return true-继续执行请求，false-终止请求
     */
    @Override
    public boolean beforeExecute(ForestRequest req) {
        // 获取火山TTS配置
        VolcengineTtsProperties properties = SpringUtils.getBean(VolcengineTtsProperties.class);

        if (!properties.getEnabled()) {
            log.warn("火山引擎TTS服务已禁用，跳过请求");
            return false;
        }

        // 验证必要配置
        if (properties.getAppId() == null || properties.getAppId().trim().isEmpty()) {
            log.error("火山引擎TTS AppId未配置，请检查配置文件");
            return false;
        }

        if (properties.getAccessToken() == null || properties.getAccessToken().trim().isEmpty()) {
            log.error("火山引擎TTS AccessToken未配置，请检查配置文件");
            return false;
        }

        // 添加Authorization头
        req.addHeader("Authorization", "Bearer;" + properties.getAccessToken());
        log.debug("火山引擎TTS请求配置完成 - AppId: {}, Cluster: {}",
            properties.getAppId(), properties.getCluster());

        return true;
    }
}
