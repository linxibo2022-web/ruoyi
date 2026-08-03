package plus.ruoyi.common.web.core;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

/**
 * 国际化语言环境解析器
 * <p>
 * 从HTTP请求头中获取语言信息来确定用户的语言环境。
 * 支持通过content-language请求头进行语言切换。
 * </p>
 *
 * @author Lion Li
 */
public class I18nLocaleResolver implements LocaleResolver {

    /**
     * 解析请求中的语言环境
     * <p>
     * 从请求头的content-language字段中解析语言信息。
     * 支持格式：语言_国家（如：zh_CN、en_US）
     * 如果未提供或格式错误，则返回系统默认语言环境。
     * </p>
     *
     * @param httpServletRequest HTTP请求对象
     * @return 解析后的语言环境
     */
    @NonNull
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        String language = httpServletRequest.getHeader("content-language");
        Locale locale = Locale.getDefault();
        if (language != null && !language.isEmpty()) {
            // 将 zh_CN 格式转为 BCP 47 的 zh-CN 格式，兼容 JDK 17+
            Locale parsed = Locale.forLanguageTag(language.replace("_", "-"));
            if (!parsed.getLanguage().isEmpty()) {
                locale = parsed;
            }
        }
        return locale;
    }

    /**
     * 设置语言环境
     * <p>此实现为空，不支持通过代码方式设置语言环境</p>
     *
     * @param httpServletRequest  HTTP请求对象
     * @param httpServletResponse HTTP响应对象
     * @param locale              要设置的语言环境
     */
    @Override
    public void setLocale(@NonNull HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse,
                          Locale locale) {
        // 空实现，不支持设置语言环境
    }
}
