package plus.ruoyi.common.websocket.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置属性类
 * <p>
 * 用于读取配置文件中以 "websocket" 为前缀的配置项
 *
 * @author zendwang
 */
@ConfigurationProperties("websocket")
@Data
public class WebSocketProperties {

    /**
     * 是否启用 WebSocket 功能
     */
    private Boolean enabled;

    /**
     * WebSocket 服务端点路径
     * <p>
     * 默认值："/websocket"
     */
    private String path;

    /**
     * 允许跨域访问的源地址
     * <p>
     * 支持单个地址或通配符 "*"，默认值："*"
     */
    private String allowedOrigins;
}
