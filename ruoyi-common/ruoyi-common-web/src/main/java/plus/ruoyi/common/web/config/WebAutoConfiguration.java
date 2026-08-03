package plus.ruoyi.common.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Web 模块自动配置
 * <p>
 * 统一管理 Web 相关的所有配置类，包括：
 * - CaptchaConfiguration: 验证码配置（圆圈、线段、扭曲）
 * - FilterConfiguration: 过滤器配置（XSS、可重复读取）
 * - I18nConfiguration: 国际化配置
 * - ResourcesConfiguration: 资源处理、拦截器、跨域、异常处理
 * - UndertowConfiguration: Undertow 服务器定制
 * </p>
 *
 * @author 抓蛙师
 */
@Slf4j
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@Import({
    CaptchaConfiguration.class,
    FilterConfiguration.class,
    I18nConfiguration.class,
    ResourcesConfiguration.class,
    UndertowConfiguration.class
})
public class WebAutoConfiguration {

    public WebAutoConfiguration() {
        log.info("初始化 Web 模块自动配置");
    }
}
