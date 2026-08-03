package com.aizuda.snailjob.server.starter.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Actuator端点认证过滤器
 * 用于保护Spring Boot Actuator监控端点，使用HTTP Basic认证
 *
 * @author Lion Li
 */
public class ActuatorAuthFilter implements Filter {

    private final String username;
    private final String password;

    /**
     * 构造函数
     *
     * @param username 认证用户名
     * @param password 认证密码
     */
    public ActuatorAuthFilter(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取HTTP Basic认证头
        String authHeader = request.getHeader("Authorization");

        // 检查认证头格式
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            sendUnauthorizedResponse(response);
            return;
        }

        // 解码Base64编码的认证信息
        String base64Credentials = authHeader.substring("Basic ".length());
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        String[] split = credentials.split(":");

        // 验证认证信息格式
        if (split.length != 2) {
            sendUnauthorizedResponse(response);
            return;
        }

        // 验证用户名和密码
        if (!username.equals(split[0]) || !password.equals(split[1])) {
            sendUnauthorizedResponse(response);
            return;
        }

        // 认证成功，继续处理请求
        filterChain.doFilter(request, response);
    }

    /**
     * 发送401未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"realm\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // 过滤器初始化，无需特殊处理
    }

    @Override
    public void destroy() {
        // 过滤器销毁，无需特殊处理
    }
}
