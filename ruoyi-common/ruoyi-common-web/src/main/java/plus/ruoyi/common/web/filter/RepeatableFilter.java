package plus.ruoyi.common.web.filter;

import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.http.MediaType;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * 可重复读取请求体的过滤器
 * 用于包装HttpServletRequest，允许多次读取请求体内容
 *
 * @author ruoyi
 */
public class RepeatableFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 过滤器初始化，无需特殊处理
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        ServletRequest requestWrapper = null;

        // 仅对HTTP请求且Content-Type为application/json的请求进行包装
        if (request instanceof HttpServletRequest
            && StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
            // 创建可重复读取的请求包装器
            requestWrapper = new RepeatedlyRequestWrapper((HttpServletRequest) request, response);
        }

        // 根据是否需要包装选择传递原始请求还是包装后的请求
        if (null == requestWrapper) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void destroy() {
        // 过滤器销毁，无需特殊处理
    }
}
