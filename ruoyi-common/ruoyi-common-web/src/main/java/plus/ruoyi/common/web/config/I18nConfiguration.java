package plus.ruoyi.common.web.config;

import org.springframework.context.annotation.Configuration;
import plus.ruoyi.common.web.core.I18nLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 国际化配置类
 * <p>
 * 配置系统的国际化支持，通过请求头中的语言信息自动识别用户的语言环境。
 * 在WebMvcAutoConfiguration之前执行，确保国际化配置优先生效。
 * </p>
 *
 * @author Lion Li
 */
@Configuration
public class I18nConfiguration {

    /**
     * 国际化语言解析器
     * <p>
     * 从HTTP请求头的content-language字段中解析用户的语言环境，
     * 支持多语言切换功能。
     * </p>
     *
     * @return 自定义的语言环境解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new I18nLocaleResolver();
    }

}
