package plus.ruoyi.common.sse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSE（Server-Sent Events）配置属性类
 * <p>
 * 从配置文件中读取sse前缀的配置项，用于控制SSE功能的开启和相关参数
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties("sse")
public class SseProperties {

    /**
     * 是否启用SSE功能
     * <p>
     * 默认为false，需要在配置文件中设置为true才能启用SSE服务
     */
    private Boolean enabled;

    /**
     * SSE服务的访问路径
     * <p>
     * 客户端连接SSE服务时使用的URL路径，如：/sse/connect
     */
    private String path;
}
