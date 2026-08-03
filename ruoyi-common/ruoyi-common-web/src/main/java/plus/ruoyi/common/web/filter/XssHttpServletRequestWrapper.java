package plus.ruoyi.common.web.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * XSS过滤请求包装器
 * 对请求参数和请求体进行HTML标签清理，防止XSS攻击
 *
 * @author ruoyi
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }
        // 清理HTML标签并去除前后空格
        return HtmlUtil.cleanHtmlTag(value).trim();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> valueMap = super.getParameterMap();
        if (MapUtil.isEmpty(valueMap)) {
            return valueMap;
        }

        // 复制参数Map避免修改原始数据
        Map<String, String[]> map = new HashMap<>(valueMap.size());
        map.putAll(valueMap);

        // 对每个参数值进行XSS过滤
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                int length = values.length;
                String[] escapseValues = new String[length];
                for (int i = 0; i < length; i++) {
                    // 清理HTML标签并去除前后空格
                    escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
                }
                map.put(entry.getKey(), escapseValues);
            }
        }
        return map;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (ArrayUtil.isEmpty(values)) {
            return values;
        }

        // 对参数数组中的每个值进行XSS过滤
        int length = values.length;
        String[] escapseValues = new String[length];
        for (int i = 0; i < length; i++) {
            // 防xss攻击和过滤前后空格
            escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
        }
        return escapseValues;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 非JSON请求直接返回原始输入流
        if (!isJsonRequest()) {
            return super.getInputStream();
        }

        // 读取请求体内容
        String json = StrUtil.str(IoUtil.readBytes(super.getInputStream(), false), StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(json)) {
            return super.getInputStream();
        }

        // 对JSON内容进行XSS过滤
        json = HtmlUtil.cleanHtmlTag(json).trim();
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream bis = IoUtil.toStream(jsonBytes);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public int available() throws IOException {
                return jsonBytes.length;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不需要实现
            }

            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };
    }

    /**
     * 判断是否为JSON请求
     * @return true-JSON请求, false-其他类型请求
     */
    public boolean isJsonRequest() {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }
}
