package plus.ruoyi.common.web.filter;

import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.web.config.properties.XssProperties;
import org.springframework.http.HttpMethod;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XSS攻击防护过滤器
 * 对请求参数进行XSS过滤处理，防止恶意脚本注入
 *
 * @author ruoyi
 */
public class XssFilter implements Filter {
    /**
     * 不需要XSS过滤的URL列表
     */
    public List<String> excludes = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 从配置中加载排除的URL列表
        XssProperties properties = SpringUtils.getBean(XssProperties.class);
        excludes.addAll(properties.getExcludeUrls());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 检查是否需要跳过XSS过滤
        if (handleExcludeUrl(req, resp)) {
            chain.doFilter(request, response);
            return;
        }

        // 使用XSS过滤包装器处理请求
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    /**
     * 判断是否跳过XSS过滤
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return true-跳过过滤, false-需要过滤
     */
    private boolean handleExcludeUrl(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();

        // GET和DELETE请求不进行XSS过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }

        // 检查URL是否在排除列表中
        return StringUtils.matchesAny(url, excludes);
    }

    @Override
    public void destroy() {
        // 过滤器销毁时的清理工作
    }
}
