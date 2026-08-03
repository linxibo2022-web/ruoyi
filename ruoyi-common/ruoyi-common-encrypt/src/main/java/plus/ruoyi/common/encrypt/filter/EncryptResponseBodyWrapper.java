package plus.ruoyi.common.encrypt.filter;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import plus.ruoyi.common.encrypt.utils.EncryptUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 加密响应体包装器
 *
 * 继承HttpServletResponseWrapper，实现响应体的缓存和加密功能
 *
 * 工作原理：
 * 1. 拦截响应写入操作，将内容写入内存缓冲区
 * 2. 在过滤器中调用getEncryptContent方法获取加密后的内容
 * 3. 生成随机AES密钥加密响应体
 * 4. 用RSA公钥加密AES密钥放入响应头
 * 5. 返回加密后的响应内容
 *
 * @author Michelle.Chung
 */
public class EncryptResponseBodyWrapper extends HttpServletResponseWrapper {

    /**
     * 内存输出流，用于缓存响应内容
     */
    private final ByteArrayOutputStream byteArrayOutputStream;

    /**
     * 自定义的ServletOutputStream
     */
    private final ServletOutputStream servletOutputStream;

    /**
     * PrintWriter包装器
     */
    private final PrintWriter printWriter;

    /**
     * 构造函数，初始化缓冲区和包装器
     *
     * @param response 原始响应对象
     * @throws IOException 初始化失败时抛出
     */
    public EncryptResponseBodyWrapper(HttpServletResponse response) throws IOException {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.servletOutputStream = this.getOutputStream();
        this.printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream));
    }

    /**
     * 重写getWriter方法，返回自定义的PrintWriter
     */
    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    /**
     * 刷新缓冲区
     */
    @Override
    public void flushBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    /**
     * 重置缓冲区
     */
    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    /**
     * 获取响应数据的字节数组
     *
     * @return 响应内容的字节数组
     * @throws IOException 刷新缓冲区失败时抛出
     */
    public byte[] getResponseData() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 获取响应内容的字符串形式
     *
     * @return 响应内容字符串
     * @throws IOException 刷新缓冲区失败时抛出
     */
    public String getContent() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toString();
    }

    /**
     * 获取加密后的响应内容
     *
     * 加密流程：
     * 1. 生成32位随机AES密钥
     * 2. 对AES密钥进行Base64编码
     * 3. 用RSA公钥加密Base64编码后的AES密钥
     * 4. 将加密后的AES密钥放入响应头
     * 5. 用AES密钥加密响应体内容
     *
     * @param servletResponse 响应对象，用于设置响应头
     * @param publicKey RSA公钥，用于加密AES密钥
     * @param headerFlag 响应头中存放加密密钥的标识
     * @return 加密后的响应内容
     * @throws IOException 获取响应内容失败时抛出
     */
    public String getEncryptContent(HttpServletResponse servletResponse, String publicKey, String headerFlag)
            throws IOException {

        // 1. 生成32位随机AES密钥
        String aesPassword = RandomUtil.randomString(32);

        // 2. 对AES密钥进行Base64编码
        String encryptAes = EncryptUtils.encryptByBase64(aesPassword);

        // 3. 用RSA公钥加密Base64编码后的AES密钥
        String encryptPassword = EncryptUtils.encryptByRsa(encryptAes, publicKey);

        // 4. 设置CORS相关响应头和加密密钥头
        servletResponse.addHeader("Access-Control-Expose-Headers", headerFlag);
        servletResponse.setHeader("Access-Control-Allow-Origin", "*");
        servletResponse.setHeader("Access-Control-Allow-Methods", "*");
        servletResponse.setHeader(headerFlag, encryptPassword);
        servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        // 5. 获取原始响应内容
        String originalBody = this.getContent();

        // 6. 用AES密钥加密响应内容
        return EncryptUtils.encryptByAes(originalBody, aesPassword);
    }

    /**
     * 重写getOutputStream方法，返回自定义的ServletOutputStream
     *
     * 该输出流将内容写入内存缓冲区而不是直接输出到客户端
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byteArrayOutputStream.write(b, off, len);
            }
        };
    }
}
