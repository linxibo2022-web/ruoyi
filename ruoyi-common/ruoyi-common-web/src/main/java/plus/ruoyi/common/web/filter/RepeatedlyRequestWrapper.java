package plus.ruoyi.common.web.filter;

import cn.hutool.core.io.IoUtil;
import plus.ruoyi.common.core.constant.Constants;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取请求体的包装器
 * 解决HttpServletRequest的InputStream只能读取一次的问题
 *
 * @author ruoyi
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 缓存的请求体内容
     */
    private final byte[] body;

    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException {
        super(request);
        // 设置字符编码为UTF-8
        request.setCharacterEncoding(Constants.UTF8);
        response.setCharacterEncoding(Constants.UTF8);

        // 读取并缓存请求体内容
        body = IoUtil.readBytes(request.getInputStream(), false);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // 必须显式传入 UTF-8：构造函数里的 request.setCharacterEncoding(UTF8) 只影响底层 request 的 reader，
        // 这里 new InputStreamReader 绕开了底层 request 的字符集设置，不传字符集就会使用 JVM 默认编码（平台依赖）
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 基于缓存的请求体创建新的输入流
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public int available() throws IOException {
                return body.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不需要实现
            }
        };
    }
}
