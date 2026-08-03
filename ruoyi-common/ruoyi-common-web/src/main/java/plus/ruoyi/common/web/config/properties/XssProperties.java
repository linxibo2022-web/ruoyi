package plus.ruoyi.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS过滤配置属性类
 * <p>
 * 用于配置系统的XSS攻击防护功能，包括开关控制和路径排除规则。
 * 配置前缀：xss
 * </p>
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "xss")
public class XssProperties {

    /**
     * XSS过滤开关
     * <p>true：启用XSS过滤，false：禁用XSS过滤</p>
     */
    private Boolean enabled;

    /**
     * XSS过滤排除路径列表
     * <p>配置不需要进行XSS过滤的URL路径，支持通配符匹配</p>
     */
    private List<String> excludeUrls = new ArrayList<>();

}
