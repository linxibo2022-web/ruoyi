package plus.ruoyi.common.core.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpStatus;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Servlet工具类
 * <p>基于HuTool的JakartaServletUtil进行扩展，提供HTTP请求参数获取、响应处理、会话管理等功能</p>
 * <p>主要用于简化Web开发中的常见操作，如参数解析、Ajax判断、客户端信息获取等</p>
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServletUtils extends JakartaServletUtil {

    /**
     * 获取指定名称的String类型请求参数
     * <p>示例：{@code String userName = ServletUtils.getParameter("userName")}</p>
     *
     * @param name 参数名
     * @return 参数值，如果参数不存在则返回null
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取指定名称的String类型请求参数，支持默认值
     * <p>示例：{@code String username = ServletUtils.getParameter("username", "guest")}</p>
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static String getParameter(String name, String defaultValue) {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的Integer类型请求参数
     * <p>示例：{@code Integer pageNum = ServletUtils.getParameterToInt("pageNum")}</p>
     *
     * @param name 参数名
     * @return 参数值，如果参数不存在或转换失败则返回null
     */
    public static Integer getParameterToInt(String name) {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的Integer类型请求参数，支持默认值
     * <p>示例：{@code Integer pageSize = ServletUtils.getParameterToInt("pageSize", 10)}</p>
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的Boolean类型请求参数
     * <p>示例：{@code Boolean isActive = ServletUtils.getParameterToBool("isActive")}</p>
     *
     * @param name 参数名
     * @return 参数值，如果参数不存在或转换失败则返回null
     */
    public static Boolean getParameterToBool(String name) {
        return Convert.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的Boolean类型请求参数，支持默认值
     * <p>示例：{@code boolean enabled = ServletUtils.getParameterToBool("enabled", false)}</p>
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static boolean getParameterToBool(String name, Boolean defaultValue) {
        return Convert.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取所有请求参数（原始格式）
     * <p>示例：{@code Map<String, String[]> params = ServletUtils.getParams(request)}</p>
     * <p>返回格式：{"name": ["value1"], "tags": ["tag1", "tag2"]}</p>
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 请求参数的Map，键为参数名，值为参数值数组（不可修改）
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获取所有请求参数（字符串格式）
     * <p>示例：{@code Map<String, String> paramMap = ServletUtils.getParamMap(request)}</p>
     * <p>返回格式：{"name": "value1", "tags": "tag1,tag2"}</p>
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 请求参数的Map，键为参数名，值为拼接后的字符串
     */
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), StringUtils.SEPARATOR));
        }
        return params;
    }

    /**
     * 获取当前HTTP请求对象
     * <p>示例：{@code HttpServletRequest request = ServletUtils.getRequest()}</p>
     *
     * @return 当前HTTP请求对象，如果获取失败则返回null
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前HTTP响应对象
     * <p>示例：{@code HttpServletResponse response = ServletUtils.getResponse()}</p>
     *
     * @return 当前HTTP响应对象，如果获取失败则返回null
     */
    public static HttpServletResponse getResponse() {
        try {
            return getRequestAttributes().getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前请求的HttpSession对象
     * <p>示例：{@code HttpSession session = ServletUtils.getSession()}</p>
     * <p>如果当前请求已经关联了一个会话（即已经存在有效的session ID），
     * 则返回该会话对象；如果没有关联会话，则会创建一个新的会话对象并返回</p>
     * <p>HttpSession用于存储会话级别的数据，如用户登录信息、购物车内容等，
     * 可以在多个请求之间共享会话数据</p>
     *
     * @return 当前请求的HttpSession对象
     */
    public static HttpSession getSession() {
        return Objects.requireNonNull(getRequest()).getSession();
    }

    /**
     * 获取当前请求的请求属性
     * <p>示例：{@code ServletRequestAttributes attrs = ServletUtils.getRequestAttributes()}</p>
     *
     * @return {@link ServletRequestAttributes} 请求属性对象，如果获取失败则返回null
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取指定请求头的值
     * <p>示例：{@code String userAgent = ServletUtils.getHeader(request, "User-Agent")}</p>
     *
     * @param request 请求对象
     * @param name    头部名称
     * @return 头部值，如果头部为空则返回空字符串，自动进行URL解码
     */
    public static String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return urlDecode(value);
    }

    /**
     * 获取所有请求头的Map
     * <p>示例：{@code Map<String, String> headers = ServletUtils.getHeaders(request)}</p>
     * <p>返回格式：{"Content-Type": "application/json", "User-Agent": "Mozilla/5.0..."}</p>
     *
     * @param request 请求对象
     * @return 请求头的Map，键为头部名称，值为头部值（忽略大小写）
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedCaseInsensitiveMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 将字符串渲染到客户端（JSON格式）
     * <p>示例：{@code ServletUtils.renderString(response, "{\"success\": true}")}</p>
     *
     * @param response 响应对象
     * @param string   待渲染的字符串内容
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(HttpStatus.HTTP_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前请求是否为Ajax异步请求
     * <p>示例：{@code boolean isAjax = ServletUtils.isAjaxRequest(request)}</p>
     * <p>判断依据：Accept头包含application/json、X-Requested-With为XMLHttpRequest、
     * URI后缀为.json/.xml、参数__ajax为json/xml</p>
     *
     * @param request 请求对象
     * @return true表示是Ajax请求，false表示不是
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {

        // 判断 Accept 头部是否包含 application/json
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 判断 X-Requested-With 头部是否包含 XMLHttpRequest
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            return true;
        }

        // 判断 URI 后缀是否为 .json 或 .xml
        String uri = request.getRequestURI();
        if (StringUtils.equalsAnyIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }

        // 判断请求参数 __ajax 是否为 json 或 xml
        String ajax = request.getParameter("__ajax");
        return StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 获取客户端IP地址
     * <p>示例：{@code String clientIp = ServletUtils.getClientIP()}</p>
     *
     * @return 客户端IP地址字符串
     */
    public static String getClientIP() {
        String clientIP = getClientIP(Objects.requireNonNull(getRequest()));
        return clientIP.contains("0:0:0:0:0:0:0:1") ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(clientIP);
    }

    /**
     * 对内容进行URL编码
     * <p>示例：{@code String encoded = ServletUtils.urlEncode("中文参数")}</p>
     *
     * @param str 待编码的内容
     * @return URL编码后的内容
     */
    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * 对内容进行URL解码
     * <p>示例：{@code String decoded = ServletUtils.urlDecode("%E4%B8%AD%E6%96%87")}</p>
     *
     * @param str 待解码的内容
     * @return URL解码后的内容
     */
    public static String urlDecode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

}
