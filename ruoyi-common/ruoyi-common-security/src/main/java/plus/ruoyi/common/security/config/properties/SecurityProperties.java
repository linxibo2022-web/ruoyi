package plus.ruoyi.common.security.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security安全配置属性
 *
 * <p>用于配置Spring Security相关的安全参数，支持从配置文件中读取设置。
 *
 * <p>配置示例：
 * <pre>
 * security:
 *   excludes:
 *     - /login
 *     - /register
 *     - /api/public/**
 *     - /v3/api-docs/**
 * </pre>
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 安全认证排除路径
     *
     * <p>配置不需要进行安全认证的URL路径，支持Ant风格的路径匹配：
     * <ul>
     *   <li>? 匹配一个字符</li>
     *   <li>* 匹配零个或多个字符</li>
     *   <li>** 匹配零个或多个目录</li>
     * </ul>
     *
     * <p>常见排除路径：
     * <ul>
     *   <li>登录注册相关：/login, /register, /captcha</li>
     *   <li>静态资源：/static/**, /public/**, /assets/**</li>
     *   <li>API文档：/swagger-ui/**, /v3/api-docs/**</li>
     *   <li>健康检查：/actuator/health, /actuator/info</li>
     * </ul>
     */
    private String[] excludes;

}
