package plus.ruoyi.common.http.client.gaode.map.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 高德地图配置属性
 *
 * @author ye
 */
@Data
@ConfigurationProperties(prefix = "gaode.map")
public class GaodeMapProperties {

    /**
     * 高德地图API密钥
     */
    private String apiKey;

    /**
     * API请求超时时间（毫秒）
     */
    private Integer timeout = 5000;

    /**
     * 是否启用高德地图服务
     */
    private Boolean enabled = true;
}
