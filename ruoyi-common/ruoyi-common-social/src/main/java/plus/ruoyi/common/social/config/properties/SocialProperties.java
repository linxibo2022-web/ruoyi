package plus.ruoyi.common.social.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 社交登录配置属性映射类
 * <p>
 * 从配置文件中读取justauth前缀的社交登录配置
 *
 * @author thiszhc
 */
@Data
@Component
@ConfigurationProperties(prefix = "justauth")
public class SocialProperties {

    /**
     * 社交登录类型配置映射
     * <p>
     * key: 登录类型（如：wechat_enterprise、github等）
     * value: 对应的登录配置
     */
    private Map<String, SocialLoginConfigProperties> type;
}
