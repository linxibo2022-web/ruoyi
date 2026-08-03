package plus.ruoyi.common.http.client.gaode.map;

import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.ForestInterceptor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.http.client.gaode.map.properties.GaodeMapProperties;

/**
 * 高德地图请求拦截器 - 自动添加API Key
 *
 * @author ye
 */
@Slf4j
public class GaodeMapInterceptor implements ForestInterceptor {

    /**
     * 请求发送前的处理
     *
     * @param req Forest请求对象
     * @return true-继续执行请求，false-终止请求
     */
    @Override
    public boolean beforeExecute(ForestRequest req) {
        // 获取高德地图配置
        GaodeMapProperties gaodeMapProperties = SpringUtils.getBean(GaodeMapProperties.class);
        if (!gaodeMapProperties.getEnabled()) {
            log.warn("高德地图服务已禁用，跳过请求");
            return false;
        }

        String apiKey = gaodeMapProperties.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("高德地图API密钥未配置，请检查配置文件");
            return false;
        }

        // 添加API Key到请求参数
        req.addQuery("key", apiKey);
        log.debug("已添加高德地图API Key到请求参数");
        return true;
    }
}
