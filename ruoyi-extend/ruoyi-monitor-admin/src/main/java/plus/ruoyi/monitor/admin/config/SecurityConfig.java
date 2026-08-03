package plus.ruoyi.monitor.admin.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Spring Boot Admin 监控安全配置
 * 配置访问权限、登录页面和安全策略
 *
 * @author Lion Li
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final String adminContextPath;

    public SecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    /**
     * 配置安全过滤器链
     * 定义哪些路径需要认证，哪些可以公开访问
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 配置登录成功后的跳转处理器
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");
        PathPatternRequestMatcher.Builder mvc = PathPatternRequestMatcher.withDefaults();
        return httpSecurity
            // 禁用 X-Frame-Options，允许页面嵌入到 iframe 中
            .headers((header) ->
                header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            // 配置请求授权规则
            .authorizeHttpRequests((authorize) ->
                authorize.requestMatchers(
                        mvc.matcher(adminContextPath + "/assets/**"),
                        mvc.matcher(adminContextPath + "/login")
                    ).permitAll()
                    .anyRequest().authenticated()) // 其他请求需要认证
            // 配置表单登录
            .formLogin((formLogin) ->
                formLogin.loginPage(adminContextPath + "/login").successHandler(successHandler))
            // 配置登出
            .logout((logout) ->
                logout.logoutUrl(adminContextPath + "/logout"))
            // 启用 HTTP Basic 认证
            .httpBasic(Customizer.withDefaults())
            // 禁用 CSRF 保护（适用于 API 场景）
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}
